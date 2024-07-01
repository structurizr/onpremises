package com.structurizr.onpremises.web;

import com.structurizr.onpremises.component.review.ReviewComponent;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;
import com.structurizr.onpremises.domain.User;
import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.ReviewType;
import com.structurizr.onpremises.domain.review.Session;

import java.util.Collection;

public class MockReviewComponent implements ReviewComponent {

    @Override
    public Review createReview(User user, Long workspaceId, String[] files, ReviewType type) {
        return null;
    }

    @Override
    public Collection<Review> getReviews() {
        return null;
    }

    @Override
    public Review getReview(String reviewId) {
        return null;
    }

    @Override
    public void submitReview(String reviewId, Session reviewSession) {
    }

    @Override
    public InputStreamAndContentLength getDiagram(String reviewId, String filename) {
        return null;
    }

    @Override
    public void lockReview(String reviewId) {
    }

    @Override
    public void unlockReview(String reviewId) {
    }

}
