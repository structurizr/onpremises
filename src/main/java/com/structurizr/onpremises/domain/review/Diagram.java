package com.structurizr.onpremises.domain.review;

import java.util.ArrayList;
import java.util.List;

public class Diagram {

    private int id;
    private String url;

    private List<Comment> comments = new ArrayList<>();

    Diagram() {
    }

    public Diagram(int id, String url) {
        this.id = id;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    void addComment(Comment comment) {
        this.comments.add(comment);
    }

}