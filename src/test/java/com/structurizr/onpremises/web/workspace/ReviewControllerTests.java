package com.structurizr.onpremises.web.workspace;

import com.structurizr.onpremises.component.workspace.WorkspaceComponentException;
import com.structurizr.onpremises.domain.Message;
import com.structurizr.onpremises.domain.Messages;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.ReviewType;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.web.ControllerTestsBase;
import com.structurizr.onpremises.web.MockHttpServletRequest;
import com.structurizr.onpremises.web.MockReviewComponent;
import com.structurizr.onpremises.web.MockWorkspaceComponent;
import com.structurizr.onpremises.web.review.ReviewController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReviewControllerTests extends ControllerTestsBase {

    private ReviewController controller;
    private ModelMap model;
    private RedirectAttributes redirectAttributes;

    @BeforeEach
    public void setUp() {
        controller = new ReviewController();
        model = new ModelMap();
        redirectAttributes = new RedirectAttributesModelMap();
        clearUser();
        Configuration.init();
    }

    @Test
    public void showCreateReviewPage() {
        Configuration.getInstance().setAdminUsersAndRoles();
        setUser("user@example.com");

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public long createWorkspace(User user) throws WorkspaceComponentException {
                return 1;
            }
        });
        String result = controller.showCreateReviewPage(model);

        assertEquals("review-create", result);
    }

    @Test
    public void createWorkspace_ReturnsAnError_WhenNoImagesAreSpecified() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String[] getParameterValues(String s) {
                return new String[0];
            }
        };
        String result = controller.createReview(null, ReviewType.General, request, redirectAttributes);

        assertEquals("redirect:/user/review/create", result);
        Messages messages = (Messages)redirectAttributes.getFlashAttributes().get("messages");
        Message message = messages.getUnreadMessages().get(0);
        assertEquals("One or more PNG/JPG files must be specified.", message.getText());
    }

    @Test
    public void createWorkspace_CreatesAPublicReview_WheImagesAreSpecified() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String[] getParameterValues(String s) {
                return new String[] { "file1", "file2", "file3" };
            }
        };

        final StringBuilder buf = new StringBuilder();
        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review createReview(User user, Long workspaceId, String[] files, ReviewType type) {
                Review review = new Review("1234567890", user.getUsername());
                review.setWorkspaceId(workspaceId);
                int count = 1;
                for (String file : files) {
                    review.addDiagram(count, file);
                    count++;
                }
                review.setType(type);
                try {
                    buf.append(Review.toJson(review));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return review;
            }
        });
        setUser("user@example.com");

        String result = controller.createReview(null, ReviewType.Risk, request, redirectAttributes);

        assertEquals("redirect:/review/1234567890", result);
        assertEquals("{\"id\":\"1234567890\",\"userId\":\"user@example.com\",\"type\":\"Risk\",\"locked\":false,\"diagrams\":[{\"id\":1,\"url\":\"file1\"},{\"id\":2,\"url\":\"file2\"},{\"id\":3,\"url\":\"file3\"}]}", buf.toString());
    }

    @Test
    public void createWorkspace_CreatesAPrivateReview_WheImagesAreSpecified() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String[] getParameterValues(String s) {
                return new String[] { "file1", "file2", "file3" };
            }
        };

        final StringBuilder buf = new StringBuilder();
        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review createReview(User user, Long workspaceId, String[] files, ReviewType type) {
                Review review = new Review("1234567890", user.getUsername());
                review.setWorkspaceId(workspaceId);
                int count = 1;
                for (String file : files) {
                    review.addDiagram(count, file);
                    count++;
                }
                review.setType(type);
                try {
                    buf.append(Review.toJson(review));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return review;
            }
        });
        setUser("user@example.com");

        String result = controller.createReview(123456L, ReviewType.STRIDE, request, redirectAttributes);

        assertEquals("redirect:/review/1234567890", result);
        assertEquals("{\"id\":\"1234567890\",\"userId\":\"user@example.com\",\"workspaceId\":123456,\"type\":\"STRIDE\",\"locked\":false,\"diagrams\":[{\"id\":1,\"url\":\"file1\"},{\"id\":2,\"url\":\"file2\"},{\"id\":3,\"url\":\"file3\"}]}", buf.toString());
    }

}