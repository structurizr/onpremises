package com.structurizr.onpremises.component.review;

import com.structurizr.onpremises.domain.review.Review;
import com.structurizr.onpremises.domain.review.Session;
import com.structurizr.onpremises.domain.InputStreamAndContentLength;

import java.util.Collection;
import java.util.Set;

interface ReviewDao {

    Set<String> getReviewIds() throws ReviewComponentException;

    void putReview(Review review) throws ReviewComponentException;

    Review getReview(String reviewId) throws ReviewComponentException;

    void submitReview(String reviewId, Session reviewSession) throws ReviewComponentException;

    Collection<Session> getReviewSessions(String reviewId) throws ReviewComponentException;

    void putDiagram(String reviewId, String filename, byte[] bytes) throws ReviewComponentException;

    boolean reviewExists(String reviewId) throws ReviewComponentException;

    InputStreamAndContentLength getDiagram(String reviewId, String filename) throws ReviewComponentException;

}