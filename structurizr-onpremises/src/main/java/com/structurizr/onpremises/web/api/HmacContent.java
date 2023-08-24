package com.structurizr.onpremises.web.api;

class HmacContent {

    private final String[] strings;

    public HmacContent(String... strings) {
        this.strings = strings;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (String string : strings) {
            buf.append(string);
            buf.append("\n");
        }

        return buf.toString();
    }

}