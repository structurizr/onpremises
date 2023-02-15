package com.structurizr.onpremises.component.review;

class FileTypeAndContent {

    private final String extension;
    private final byte[] content;

    FileTypeAndContent(String extension, byte[] content) {
        this.extension = extension;
        this.content = content;
    }

    String getExtension() {
        return this.extension;
    }

    byte[] getContent() {
        return content;
    }

}