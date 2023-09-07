package com.structurizr.onpremises.web.review;

import com.structurizr.onpremises.component.review.ReviewComponent;
import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.Messages;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.ReviewType;
import com.structurizr.onpremises.domain.review.Session;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.Features;
import com.structurizr.onpremises.util.HtmlUtils;
import com.structurizr.onpremises.util.JsonUtils;
import com.structurizr.onpremises.web.AbstractController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ReviewController extends AbstractController {

    private static final Log log = LogFactory.getLog(ReviewController.class);

    private static final String FILE_PARAMETER_NAME = "file";

    private ReviewComponent reviewComponent;

    @Autowired
    public void setReviewComponent(ReviewComponent reviewComponent) {
        this.reviewComponent = reviewComponent;
    }

    @RequestMapping(value = "/user/review/create", method = RequestMethod.GET)
    @PreAuthorize("isAuthenticated()")
    public String showCreateReviewPage(ModelMap model) {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        addCommonAttributes(model, "Review", false);

        return "review-create";
    }

    @RequestMapping(value = "/user/review/create", method = RequestMethod.POST)
    @PreAuthorize("isAuthenticated()")
    public String createReview(
            @RequestParam(name="workspace", required=false) Long workspaceId,
            @RequestParam(name="review", required=false) ReviewType reviewType,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes,
            ModelMap model
    ) {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        try {
            Messages messages = new Messages();
            redirectAttributes.addFlashAttribute("messages", messages);

            if (reviewType == null) {
                reviewType = ReviewType.General;
            }

            String[] files = request.getParameterValues(FILE_PARAMETER_NAME);
            if (files != null && files.length > 0) {
                Review review = reviewComponent.createReview(getUser(), workspaceId, files, reviewType);

                return "redirect:/review/" + review.getId();
            } else {
                messages.addErrorMessage("One or more PNG/JPG files must be specified.");

                return "redirect:/user/review/create";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/user/review/create";
    }

    @RequestMapping(value = "/review/{reviewId}", method = RequestMethod.GET)
    public String showReview(@PathVariable String reviewId, ModelMap model) throws Exception {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        User user = getUser();

        reviewId = HtmlUtils.filterHtml(reviewId);
        Review review = reviewComponent.getReview(reviewId);
        if (!userCanAccessReview(review)) {
            return show404Page(model);
        }


        model.addAttribute("review", review);
        model.addAttribute("reviewAsJson", JsonUtils.base64(Review.toJson(review)));

        if (user != null) {
            model.addAttribute("reviewer", user.getName() != null ? user.getName() : "");
            model.addAttribute("admin", user.getUsername().equals(review.getUserId()));
        } else {
            model.addAttribute("reviewer", "");
            model.addAttribute("admin", false);
        }

        addCommonAttributes(model, "Review", false);

        return "review";
    }

    @RequestMapping(value = "/review/{reviewId}", method = RequestMethod.POST)
    public String submitReview(@PathVariable String reviewId, @RequestParam String json, ModelMap model) throws Exception {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        reviewId = HtmlUtils.filterHtml(reviewId);
        json = HtmlUtils.filterHtml(json);
        Session reviewSession = Session.fromJson(json);

        User user = getUser();
        if (user != null) {
            reviewSession.setUserId(user.getUsername());
            reviewSession.setAuthor(user.getName());
        } else {
            reviewSession.setAuthor(null);
        }

        Review review = reviewComponent.getReview(reviewId);
        if (!userCanAccessReview(review)) {
            return show404Page(model);
        }

        if (!review.isLocked()) {
            reviewComponent.submitReview(reviewId, reviewSession);
        }

        return "redirect:/review/" + reviewId;
    }

    @ResponseBody
    @RequestMapping(value = "/review/{reviewId}/{filename}.png", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getImagePNG(@PathVariable("reviewId") String reviewId,
                                @PathVariable("filename") String filename,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        reviewId = HtmlUtils.filterHtml(reviewId);
        filename = HtmlUtils.filterHtml(filename);

        Review review = reviewComponent.getReview(reviewId);
        if (!userCanAccessReview(review)) {
            response.setStatus(404);
            return null;
        }

        try {
            InputStreamAndContentLength inputStreamAndContentLength = reviewComponent.getDiagram(reviewId, filename + ".png");
            if (inputStreamAndContentLength != null) {
                return new InputStreamResource(inputStreamAndContentLength.getInputStream()) {
                    @Override
                    public long contentLength() throws IOException {
                        return inputStreamAndContentLength.getContentLength();
                    }
                };
            } else {
                response.setStatus(404);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while trying to get image " + filename + ".png for review with ID " + reviewId, e);

            response.setStatus(404);
            return null;
        }
    }

    @ResponseBody
    @RequestMapping(value = "/review/{reviewId}/{filename}.jpg", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public Resource getImageJPG(@PathVariable("reviewId") String reviewId,
                                @PathVariable("filename") String filename,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        reviewId = HtmlUtils.filterHtml(reviewId);
        filename = HtmlUtils.filterHtml(filename);

        Review review = reviewComponent.getReview(reviewId);
        if (!userCanAccessReview(review)) {
            response.setStatus(404);
            return null;
        }

        try {
            InputStreamAndContentLength inputStreamAndContentLength = reviewComponent.getDiagram(reviewId, filename + ".jpg");
            if (inputStreamAndContentLength != null) {
                return new InputStreamResource(inputStreamAndContentLength.getInputStream()) {
                    @Override
                    public long contentLength() throws IOException {
                        return inputStreamAndContentLength.getContentLength();
                    }
                };
            } else {
                response.setStatus(404);
                return null;
            }
        } catch (Exception e) {
            log.error("Error while trying to get image " + filename + ".jpg for review with ID " + reviewId, e);

            response.setStatus(404);
            return null;
        }
    }

    @RequestMapping(value = "/review/{reviewId}/lock", method = RequestMethod.GET)
    public String lockReview(@PathVariable String reviewId, ModelMap model) {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        reviewId = HtmlUtils.filterHtml(reviewId);
        Review review = reviewComponent.getReview(reviewId);
        User user = getUser();

        if (review != null && user != null && user.getUsername().equals(review.getUserId())) {
            reviewComponent.lockReview(reviewId);
        }

        return "redirect:/review/" + reviewId;
    }

    @RequestMapping(value = "/review/{reviewId}/unlock", method = RequestMethod.GET)
    public String unlockReview(@PathVariable String reviewId, ModelMap model) {
        if (!Configuration.getInstance().isFeatureEnabled(Features.DIAGRAM_REVIEWS)) {
            return showFeatureNotAvailablePage(model);
        }

        reviewId = HtmlUtils.filterHtml(reviewId);
        Review review = reviewComponent.getReview(reviewId);
        User user = getUser();

        if (review != null && user != null && user.getUsername().equals(review.getUserId())) {
            reviewComponent.unlockReview(reviewId);
        }

        return "redirect:/review/" + reviewId;
    }

    private boolean userCanAccessReview(Review review) {
        if (review == null) {
            return false;
        }

        if (review.getWorkspaceId() != null) {
            // this is a private review, so let's inherit the security settings of the workspace
            WorkspaceMetaData workspaceMetaData = workspaceComponent.getWorkspaceMetaData(review.getWorkspaceId());
            if (workspaceMetaData == null || !userCanAccessWorkspace(workspaceMetaData)) {
                return false;
            }
        }

        return true;
    }

}