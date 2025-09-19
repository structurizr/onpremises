package com.structurizr.onpremises.web.workspace.images;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.Image;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ImagesController extends AbstractWorkspaceController {

    private static final String VIEW = "images";

    @RequestMapping(value = "/workspace/{workspaceId}/images", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedImages(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        if (userCanAccessWorkspace(workspaceMetaData)) {
            List<Image> images = workspaceComponent.getImages(workspaceId);
            for (Image image : images) {
                if (workspaceMetaData.isOpen()) {
                    image.setUrl("/share/" + workspaceId + "/images/" + image.getName());
                } else if (workspaceMetaData.isShareable()) {
                    image.setUrl("/share/" + workspaceId + "/" + workspaceMetaData.getSharingToken() + "/images/" + image.getName());
                }
            }

            images = images.stream().filter(i -> !i.getName().endsWith("thumbnail.png") && !i.getName().endsWith("thumbnail-dark.png")).collect(Collectors.toList());
            images.sort(Comparator.comparing(i -> i.getName().toLowerCase()));

            model.addAttribute("images", images);

            boolean editable = workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser());

            return showAuthenticatedView(VIEW, workspaceMetaData, null, null, model, true, editable);
        }

        return show404Page(model);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/images/delete", method = RequestMethod.POST)
    public String deletePublishedImages(@PathVariable("workspaceId") long workspaceId, ModelMap model) {
        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }
        if (workspaceMetaData.hasNoUsersConfigured() || workspaceMetaData.isWriteUser(getUser())) {
            workspaceComponent.deleteImages(workspaceId);
        }

        return "redirect:/workspace/" + workspaceId + "/images";
    }

}