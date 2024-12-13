package com.structurizr.onpremises.web.workspace.reviews;

import com.structurizr.onpremises.component.review.ReviewComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.configuration.Configuration;
import com.structurizr.onpremises.configuration.Features;
import com.structurizr.onpremises.web.workspace.AbstractWorkspaceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Controller
public class ReviewsController extends AbstractWorkspaceController {

    private ReviewComponent reviewComponent;

    private static final String VIEW = "reviews";

    @Autowired
    public void setReviewComponent(ReviewComponent reviewComponent) {
        this.reviewComponent = reviewComponent;
    }

    @RequestMapping(value = "/share/{workspaceId}/reviews", method = RequestMethod.GET)
    public String showPublicReviews(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        model.addAttribute("reviews", getReviews(workspaceId));

        return showPublicView(VIEW, workspaceId, model, true);
    }

    @RequestMapping(value = "/share/{workspaceId}/{token}/reviews", method = RequestMethod.GET)
    public String showSharedReviews(
            @PathVariable("workspaceId") long workspaceId,
            @PathVariable("token") String token,
            ModelMap model
    ) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        model.addAttribute("reviews", getReviews(workspaceId));

        return showSharedView(VIEW, workspaceId, token, model, true);
    }

    @RequestMapping(value = "/workspace/{workspaceId}/reviews", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showAuthenticatedModel(
            @PathVariable("workspaceId") long workspaceId,
            ModelMap model
    ) {

        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(workspaceId);
        if (workspaceMetaData == null) {
            return show404Page(model);
        }

        model.addAttribute("reviews", getReviews(workspaceId));

        return showAuthenticatedView(VIEW, workspaceMetaData, null, null, model, true, false);
    }

    private Collection<Review> getReviews(long workspaceId) {
        Collection<Review> reviews = reviewComponent.getReviews();
        List<Review> filteredReviews = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getWorkspaceId() != null && review.getWorkspaceId() == workspaceId) {
                filteredReviews.add(review);
            }
        }

        filteredReviews.sort((r1, r2) -> r2.getDateCreated().compareTo(r1.getDateCreated()));

        return filteredReviews;
    }

}