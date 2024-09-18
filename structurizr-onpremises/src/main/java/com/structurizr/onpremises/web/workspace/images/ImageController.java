package com.structurizr.onpremises.web.workspace.images;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.web.api.ApiException;
import com.structurizr.onpremises.web.api.ApiResponse;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

@Controller
public class ImageController extends AbstractWorkspaceController {

    private static final Log log = LogFactory.getLog(ImageController.class);

    @ResponseBody
    @RequestMapping(value = "/share/{workspaceId}/images/{diagramKey}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getPublicImage(@PathVariable("workspaceId") long workspaceId,
                                    @PathVariable("diagramKey") String diagramKey,
                                    HttpServletResponse response) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            response.setStatus(404);
            return null;
        }

        if (workspaceMetaData.isOpen()) {
            return getImage(workspaceMetaData, diagramKey, response);
        }

        response.setStatus(404);
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/share/{workspaceId}/{token}/images/{diagramKey}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getSharedImage(@PathVariable("workspaceId") long workspaceId,
                             @PathVariable("diagramKey") String diagramKey,
                             @PathVariable("token") String token,
                             HttpServletResponse response) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            response.setStatus(404);
            return null;
        }

        if (!StringUtils.isNullOrEmpty(token) && token.equals(workspaceMetaData.getSharingToken()) ) {
            return getImage(workspaceMetaData, diagramKey, response);
        }

        response.setStatus(404);
        return null;
    }

    @ResponseBody
    @RequestMapping(value = "/workspace/{workspaceId}/images/{diagramKey}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Resource getAuthenticatedImage(@PathVariable("workspaceId") long workspaceId,
                             @PathVariable("diagramKey") String diagramKey,
                             HttpServletResponse response) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            response.setStatus(404);
            return null;
        }

        if (userCanAccessWorkspace(workspaceMetaData)) {
            return getImage(workspaceMetaData, diagramKey, response);
        }

        response.setStatus(404);
        return null;
    }

    Resource getImage(WorkspaceMetaData workspaceMetaData, String diagramKey, HttpServletResponse response) {
        diagramKey = HtmlUtils.filterHtml(diagramKey);

        try {
            InputStreamAndContentLength inputStreamAndContentLength = workspaceComponent.getImage(workspaceMetaData.getId(), diagramKey + ".png");
            if (inputStreamAndContentLength != null) {
                response.setStatus(200);
                return new InputStreamResource(inputStreamAndContentLength.getInputStream()) {
                    @Override
                    public long contentLength() {
                        return inputStreamAndContentLength.getContentLength();
                    }
                };
            } else {
                response.setStatus(404);
                return null;
            }
        } catch (Exception e) {
            if (diagramKey.endsWith("thumbnail")) {
                log.warn("Error while trying to get image " + diagramKey + ".png from workspace with ID " + workspaceMetaData.getId());
            } else {
                log.error("Error while trying to get image " + diagramKey + ".png from workspace with ID " + workspaceMetaData.getId(), e);
            }

            response.setStatus(404);
            return null;
        }
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.OPTIONS)
    @PreAuthorize("isAuthenticated()")
    public void optionsImage(@PathVariable("workspaceId") long workspaceId, @PathVariable("filename") String filename, HttpServletResponse response) {
        addAccessControlAllowHeaders(response);
    }

    private void addAccessControlAllowHeaders(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "accept, origin, " + HttpHeaders.CONTENT_TYPE);
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT");
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/{filename:.+}", method = RequestMethod.PUT, consumes = "text/plain", produces = "application/json; charset=UTF-8")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody ApiResponse putImage(@PathVariable("workspaceId")long workspaceId,
                                              @PathVariable("filename")String filename,
                                              @RequestBody String imageAsBase64EncodedDataUri,
                                              @ModelAttribute("remoteIpAddress") String ipAddress) {

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            throw new ApiException("404");
        }

        if (userCanAccessWorkspace(workspaceMetaData)) {
            try {
                String base64Image = imageAsBase64EncodedDataUri.split(",")[1];
                byte[] decodedImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));
                File file = File.createTempFile("structurizr", ".png");
                Files.write(file.toPath(), decodedImage);

                if (workspaceComponent.putImage(workspaceId, filename, file)) {
                    return new ApiResponse("OK");
                } else {
                    throw new ApiException("Failed to save image");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApiException("Failed to save image");
            }
        }

        throw new ApiException("Failed to save image");
    }

}