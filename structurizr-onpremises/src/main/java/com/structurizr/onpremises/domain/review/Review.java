package com.structurizr.onpremises.domain.review;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Review {

    private int commentId = 0;

    private String id;
    private String userId;
    private Long workspaceId;
    private ReviewType type = ReviewType.General;
    private boolean locked = false;
    private Date dateCreated;

    private List<Diagram> diagrams = new ArrayList<>();

    Review() {
    }

    public Review(String id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public ReviewType getType() {
        return type;
    }

    public void setType(ReviewType type) {
        this.type = type;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<Diagram> getDiagrams() {
        return diagrams;
    }

    void setDiagrams(List<Diagram> diagrams) {
        this.diagrams = diagrams;
    }

    public void addDiagram(int id, String url) {
        this.diagrams.add(new Diagram(id, url));
    }

    public void addComment(Comment comment) {
        for (Diagram diagram : diagrams) {
            if (diagram.getId() == comment.getDiagramId()) {
                commentId++;
                comment.setId(commentId);
                diagram.addComment(comment);
            }
        }
    }

    public static String toJson(Review review) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(SerializationFeature.INDENT_OUTPUT);

        StringWriter writer = new StringWriter();
        writer.write(objectMapper.writeValueAsString(review));

        writer.flush();
        writer.close();

        return writer.toString();
    }

    public static Review fromJson(String json) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return objectMapper.readValue(json, Review.class);
    }

}