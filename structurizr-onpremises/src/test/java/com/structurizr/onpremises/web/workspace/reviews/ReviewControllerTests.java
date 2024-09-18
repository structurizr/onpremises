package com.structurizr.onpremises.web.workspace.reviews;

import com.structurizr.onpremises.component.workspace.WorkspaceMetaData;
import com.structurizr.onpremises.domain.Message;
import com.structurizr.onpremises.domain.Messages;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.ReviewType;
import com.structurizr.onpremises.util.Configuration;
import com.structurizr.onpremises.util.DateUtils;
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

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

        String result = controller.showCreateReviewPage(model);

        assertEquals("review-create", result);
    }

    @Test
    public void createReview_ReturnsAnError_WhenNoImagesAreSpecified() {
        MockHttpServletRequest request = new MockHttpServletRequest() {
            @Override
            public String[] getParameterValues(String s) {
                return new String[0];
            }
        };
        String result = controller.createReview(null, ReviewType.General, request, redirectAttributes, null);

        assertEquals("redirect:/user/review/create", result);
        Messages messages = (Messages)redirectAttributes.getFlashAttributes().get("messages");
        Message message = messages.getUnreadMessages().get(0);
        assertEquals("One or more PNG/JPG files must be specified.", message.getText());
    }

    @Test
    public void createReview_CreatesAPublicReview_WheImagesAreSpecified() {
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

        String result = controller.createReview(null, ReviewType.Risk, request, redirectAttributes, null);

        assertEquals("redirect:/review/1234567890", result);
        assertEquals("{\"id\":\"1234567890\",\"userId\":\"user@example.com\",\"type\":\"Risk\",\"locked\":false,\"diagrams\":[{\"id\":1,\"url\":\"file1\"},{\"id\":2,\"url\":\"file2\"},{\"id\":3,\"url\":\"file3\"}]}", buf.toString());
    }

    @Test
    public void createReview_CreatesAPrivateReview_WheImagesAreSpecified() {
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

        String result = controller.createReview(123456L, ReviewType.STRIDE, request, redirectAttributes, null);

        assertEquals("redirect:/review/1234567890", result);
        assertEquals("{\"id\":\"1234567890\",\"userId\":\"user@example.com\",\"workspaceId\":123456,\"type\":\"STRIDE\",\"locked\":false,\"diagrams\":[{\"id\":1,\"url\":\"file1\"},{\"id\":2,\"url\":\"file2\"},{\"id\":3,\"url\":\"file3\"}]}", buf.toString());
    }

    @Test
    public void showReview_ReturnsThe404Page_WhenTheReviewDoesNotExist() throws Exception {
        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return null;
            }
        });

        String view = controller.showReview("1234567890", model);
        assertEquals("404", view);
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheReviewIsPublicAndTheUserIsUnauthenticated() throws Exception {
        final Review review = new Review("1234567890", "user@example.com");
        review.setWorkspaceId(null);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        clearUser();
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6IkdlbmVyYWwiLCJsb2NrZWQiOmZhbHNlfQ==", model.getAttribute("reviewAsJson"));
        assertEquals("", model.getAttribute("reviewer"));
        assertEquals(false, model.getAttribute("admin"));
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheReviewIsPublicAndTheUserIsAuthenticatedAndNotTheReviewCreator() throws Exception {
        final Review review = new Review("1234567890", "user1@example.com");
        review.setWorkspaceId(null);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        setUser("user2@example.com");
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyMUBleGFtcGxlLmNvbSIsInR5cGUiOiJHZW5lcmFsIiwibG9ja2VkIjpmYWxzZX0=", model.getAttribute("reviewAsJson"));
        assertEquals("user2@example.com", model.getAttribute("reviewer"));
        assertEquals(false, model.getAttribute("admin"));
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheReviewIsPublicAndTheUserIsAuthenticatedAndTheReviewCreator() throws Exception {
        final Review review = new Review("1234567890", "user1@example.com");
        review.setWorkspaceId(null);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        setUser("user1@example.com");
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyMUBleGFtcGxlLmNvbSIsInR5cGUiOiJHZW5lcmFsIiwibG9ja2VkIjpmYWxzZX0=", model.getAttribute("reviewAsJson"));
        assertEquals("user1@example.com", model.getAttribute("reviewer"));
        assertEquals(true, model.getAttribute("admin"));
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheAssociatedWorkspaceHasNoUsersConfigured() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        final Review review = new Review("1234567890", "user1@example.com");
        review.setWorkspaceId(null);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        clearUser();
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyMUBleGFtcGxlLmNvbSIsInR5cGUiOiJHZW5lcmFsIiwibG9ja2VkIjpmYWxzZX0=", model.getAttribute("reviewAsJson"));
        assertEquals("", model.getAttribute("reviewer"));
        assertEquals(false, model.getAttribute("admin"));
    }

    @Test
    public void showReview_ReturnsThe404Page_WhenTheUserIsUnauthenticatedAndTheAssociatedWorkspaceIsPrivate() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        final Review review = new Review("1234567890", "user@example.com");
        review.setWorkspaceId(1L);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        clearUser();
        String view = controller.showReview("1234567890", model);
        assertEquals("404", view);
    }

    @Test
    public void showReview_ReturnsThe404Page_WhenTheUserIsAuthenticatedAndDoesNotHaveAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("user1@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        final Review review = new Review("1234567890", "user1@example.com");
        review.setWorkspaceId(1L);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        setUser("user2@example.com");
        String view = controller.showReview("1234567890", model);
        assertEquals("404", view);
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheUserHasReadAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addReadUser("read@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        final Review review = new Review("1234567890", "user@example.com");
        review.setWorkspaceId(1L);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        setUser("read@example.com");
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyQGV4YW1wbGUuY29tIiwid29ya3NwYWNlSWQiOjEsInR5cGUiOiJHZW5lcmFsIiwibG9ja2VkIjpmYWxzZX0=", model.getAttribute("reviewAsJson"));
        assertEquals("read@example.com", model.getAttribute("reviewer"));
        assertEquals(false, model.getAttribute("admin"));
    }

    @Test
    public void showReview_ReturnsTheReviewPage_WhenTheUserHasWriteAccessToTheWorkspace() throws Exception {
        final WorkspaceMetaData workspaceMetaData = new WorkspaceMetaData(1);
        workspaceMetaData.addWriteUser("write@example.com");
        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return workspaceMetaData;
            }
        });

        final Review review = new Review("1234567890", "user@example.com");
        review.setWorkspaceId(1L);

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Review getReview(String reviewId) {
                return review;
            }
        });

        setUser("write@example.com");
        String view = controller.showReview("1234567890", model);
        assertEquals("review", view);
        assertSame(review, model.getAttribute("review"));
        assertEquals("eyJpZCI6IjEyMzQ1Njc4OTAiLCJ1c2VySWQiOiJ1c2VyQGV4YW1wbGUuY29tIiwid29ya3NwYWNlSWQiOjEsInR5cGUiOiJHZW5lcmFsIiwibG9ja2VkIjpmYWxzZX0=", model.getAttribute("reviewAsJson"));
        assertEquals("write@example.com", model.getAttribute("reviewer"));
        assertEquals(false, model.getAttribute("admin"));
    }

    @Test
    public void showReviews_ReturnsTheReviewsPage() throws Exception {
        // standalone review - visible
        final Review review1 = new Review("1", "user@example.com");
        review1.setWorkspaceId(null);
        review1.setDateCreated(DateUtils.getNow());

        // review for open workspace - visible
        final Review review2 = new Review("2", "user@example.com");
        final WorkspaceMetaData workspaceMetaData2 = new WorkspaceMetaData(2);
        review2.setWorkspaceId(2L);
        review2.setDateCreated(DateUtils.getNow());

        // review for public workspace - visible
        final Review review3 = new Review("3", "user@example.com");
        final WorkspaceMetaData workspaceMetaData3 = new WorkspaceMetaData(3);
        workspaceMetaData3.addWriteUser("write@example.com");
        workspaceMetaData3.setPublicWorkspace(true);
        review3.setWorkspaceId(3L);
        review3.setDateCreated(DateUtils.getNow());

        // review for private workspace - not visible
        final Review review4 = new Review("4", "user@example.com");
        final WorkspaceMetaData workspaceMetaData4 = new WorkspaceMetaData(4);
        workspaceMetaData4.addWriteUser("write@example.com");
        workspaceMetaData4.setPublicWorkspace(false);
        review4.setWorkspaceId(4L);
        review4.setDateCreated(DateUtils.getNow());

        controller.setReviewComponent(new MockReviewComponent() {
            @Override
            public Collection<Review> getReviews() {
                return Set.of(review1, review2, review3, review4);
            }
        });

        controller.setWorkspaceComponent(new MockWorkspaceComponent() {
            @Override
            public WorkspaceMetaData getWorkspaceMetaData(long workspaceId) {
                return Set.of(workspaceMetaData2, workspaceMetaData3, workspaceMetaData4).stream().filter(wmd -> wmd.getId() == workspaceId).findFirst().get();
            }
        });

        clearUser();
        String view = controller.showReviews(model);
        assertEquals("reviews", view);
        Collection<Review> reviews = (Collection<Review>)model.getAttribute("reviews");
        assertEquals(3, reviews.size());
        assertTrue(reviews.contains(review1));
        assertTrue(reviews.contains(review2));
        assertTrue(reviews.contains(review3));

        setUser("write@example.com");
        controller.showReviews(model);
        reviews = (Collection<Review>)model.getAttribute("reviews");
        assertEquals(4, reviews.size());
        assertTrue(reviews.contains(review1));
        assertTrue(reviews.contains(review2));
        assertTrue(reviews.contains(review3));
        assertTrue(reviews.contains(review4));
    }

}