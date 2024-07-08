package com.structurizr.onpremises.web.api;

import com.structurizr.Workspace;
import com.structurizr.io.WorkspaceReaderException;
import com.structurizr.io.json.JsonReader;
import com.structurizr.onpremises.component.search.SearchComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.web.AbstractController;
import com.structurizr.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.StringReader;
import java.util.Base64;

/**
 * An implementation of the Structurizr web API, consisting of:
 *
 *  - GET /api/workspace/{id}
 *  - PUT /api/workspace/{id}
 *  - PUT /api/workspace/{id}/lock
 *  - DELETE /api/workspace/{id}/lock
 */
@RestController
public class ApiController extends AbstractController {

    private static final Log log = LogFactory.getLog(ApiController.class);

    private SearchComponent searchComponent;

    @Autowired
    public void setSearchComponent(SearchComponent searchComponent) {
        this.searchComponent = searchComponent;
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               @RequestParam(required = false) String version,
                               HttpServletRequest request, HttpServletResponse response) {

        return getWorkspace(workspaceId, null, version, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
    public String getWorkspace(@PathVariable("workspaceId") long workspaceId,
                               @PathVariable("branch") String branch,
                               @RequestParam(required = false) String version,
                               HttpServletRequest request, HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "GET", getPath(request, workspaceId, branch), null, request, response);

                return workspaceComponent.getWorkspace(workspaceId, branch, version);
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Something went wrong.");
        }
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @RequestBody String json,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {

        return putWorkspace(workspaceId, null, json, request, response);
    }

    @CrossOrigin
    @RequestMapping(value = "/api/workspace/{workspaceId}/branch/{branch}", method = RequestMethod.PUT, consumes = "application/json", produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse putWorkspace(@PathVariable("workspaceId")long workspaceId,
                                                  @PathVariable("branch") String branch,
                                                  @RequestBody String json,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "PUT", getPath(request, workspaceId, branch), json, request, response);

                workspaceComponent.putWorkspace(workspaceId, branch, json);

                if (json.contains("encryptionStrategy") && json.contains("ciphertext")) {
                    // remove client-side encrypted workspaces from the search index
                    try {
                        searchComponent.delete(workspaceId);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else {
                    try {
                        Workspace workspace;
                        try {
                            JsonReader jsonReader = new JsonReader();
                            StringReader stringReader = new StringReader(json);
                            workspace = jsonReader.read(stringReader);
                        } catch (WorkspaceReaderException e) {
                            throw new ApiException(e.getMessage());
                        }

                        searchComponent.index(workspace);
                    } catch (Exception e) {
                        log.error(e);
                    }
                }

                ApiResponse apiResponse = new ApiResponse("OK");
                apiResponse.setRevision(workspaceComponent.getWorkspaceMetaData(workspaceId).getRevision());
                return apiResponse;
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException(e.getMessage());
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.PUT, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse lockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                   @RequestParam(required = true) String user,
                                                   @RequestParam(required = true) String agent,
                                                   HttpServletRequest request, HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "PUT", getPath(request, workspaceId, null) + "/lock?user=" + user + "&agent=" + agent, null, request, response);

                if (workspaceComponent.lockWorkspace(workspaceId, user, agent)) {
                    return new ApiResponse("OK");
                } else {
                    WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
                    return new ApiResponse(false, "The workspace is already locked by " + workspaceMetaData.getLockedUser() + " using " + workspaceMetaData.getLockedAgent() + ".");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not lock workspace.");
        }
    }

    @RequestMapping(value = "/api/workspace/{workspaceId}/lock", method = RequestMethod.DELETE, produces = "application/json; charset=UTF-8")
    public @ResponseBody ApiResponse unlockWorkspace(@PathVariable("workspaceId") long workspaceId,
                                                     @RequestParam(required = true) String user,
                                                     @RequestParam(required = true) String agent,
                                                     HttpServletRequest request, HttpServletResponse response) {
        try {
            if (workspaceId > 0) {
                authoriseRequest(workspaceId, "DELETE", getPath(request, workspaceId, null) + "/lock?user=" + user + "&agent=" + agent, null, request, response);

                if (workspaceComponent.unlockWorkspace(workspaceId)) {
                    return new ApiResponse("OK");
                } else {
                    return new ApiResponse(false, "Could not unlock workspace.");
                }
            } else {
                throw new ApiException("Workspace ID must be greater than 1");
            }
        } catch (WorkspaceComponentException e) {
            log.error(e);
            throw new ApiException("Could not unlock workspace.");
        }
    }

    private String getPath(HttpServletRequest request, long workspaceId, String branch) {
        String contextPath = request.getContextPath();
        if (!contextPath.endsWith("/")) {
            contextPath = contextPath + "/";
        }

        if (StringUtils.isNullOrEmpty(branch)) {
            return contextPath + "api/workspace/" + workspaceId;
        } else {
            return contextPath + "api/workspace/" + workspaceId + "/branch/" + branch;
        }
    }

    private void authoriseRequest(long workspaceId, String httpMethod, String path, String content, HttpServletRequest request, HttpServletResponse response) throws WorkspaceComponentException {
        try {
            String authorizationHeaderAsString = request.getHeader(HttpHeaders.X_AUTHORIZATION);
            if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
                // fallback on the regular header
                authorizationHeaderAsString = request.getHeader(HttpHeaders.AUTHORIZATION);
            }

            if (authorizationHeaderAsString == null || authorizationHeaderAsString.trim().length() == 0) {
                throw new HttpUnauthorizedException("Authorization header must be provided");
            }

            WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
            String apiKey = workspaceMetaData.getApiKey();

            HmacAuthorizationHeader hmacAuthorizationHeader = HmacAuthorizationHeader.parse(authorizationHeaderAsString);
            String apiKeyFromAuthorizationHeader = hmacAuthorizationHeader.getApiKey();

            if (!apiKeyFromAuthorizationHeader.equals(apiKey)) {
                throw new HttpUnauthorizedException("Incorrect API key");
            }

            String contentType = request.getHeader(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.isNullOrEmpty(contentType) || contentType.startsWith(";")) {
                contentType = "";
            } else if (!contentType.contains(" ")) {
                String[] parts = contentType.split(";");
                contentType = parts[0] + "; " + parts[1];
            }

            String nonce = request.getHeader(HttpHeaders.NONCE);
            if (nonce == null || nonce.length() == 0) {
                throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.NONCE);
            }

            String contentMd5InRequest;
            String contentMd5Header = request.getHeader(HttpHeaders.CONTENT_MD5);

            if (!StringUtils.isNullOrEmpty(content)) {
                if (contentMd5Header == null || contentMd5Header.length() == 0) {
                    throw new HttpUnauthorizedException("Request header missing: " + HttpHeaders.CONTENT_MD5);
                }

                contentMd5InRequest = new String(Base64.getDecoder().decode(contentMd5Header));

                String generatedContentMd5 = new Md5Digest().generate(content);
                if (!contentMd5InRequest.equals(generatedContentMd5)) {
                    // the content has been tampered with?
                    throw new HttpUnauthorizedException("MD5 hash doesn't match content");
                }
            } else {
                contentMd5InRequest = "d41d8cd98f00b204e9800998ecf8427e"; // this is the MD5 hash of an empty string
            }

            String apiSecret = workspaceMetaData.getApiSecret();
            HashBasedMessageAuthenticationCode code = new HashBasedMessageAuthenticationCode(apiSecret);
            String hmacInRequest = hmacAuthorizationHeader.getHmac();
            HmacContent hmacContent = new HmacContent(httpMethod, path, contentMd5InRequest, contentType, nonce);
            String generatedHmac = code.generate(hmacContent.toString());
            if (!hmacInRequest.equals(generatedHmac)) {
                throw new HttpUnauthorizedException("Authorization header doesn't match");
            }
        } catch (Exception e) {
            log.error(e);
            throw new HttpUnauthorizedException(e.getMessage());
        }
    }

    @ExceptionHandler(HttpUnauthorizedException.class)
    @ResponseBody
    public ApiResponse handleCustomException(HttpUnauthorizedException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(ApiException.class)
    @ResponseBody
    public ApiResponse handleCustomException(ApiException exception, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return new ApiResponse(exception);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ApiResponse handleGeneralError(HttpServletResponse response, String message) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return new ApiResponse(false, message);
    }

}