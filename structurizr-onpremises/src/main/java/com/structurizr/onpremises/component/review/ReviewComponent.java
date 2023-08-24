package com.structurizr.onpremises.component.review;

import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.ReviewType;
import com.structurizr.onpremises.domain.review.Session;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;

/**
 * Provides access to and manages reviews.
 */
public interface ReviewComponent {

    public static final String FILE = "file";
    public static final String AMAZON_WEB_SERVICES_S3 = "aws-s3";

    Review createReview(User user, Long workspaceId, String[] files, ReviewType type);

    Review getReview(String reviewId);

    void submitReview(String reviewId, Session reviewSession);

    InputStreamAndContentLength getDiagram(String reviewId, String filename);

    void lockReview(String reviewId);

    void unlockReview(String reviewId);

}