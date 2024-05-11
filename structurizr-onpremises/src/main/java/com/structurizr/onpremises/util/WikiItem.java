package com.structurizr.onpremises.util;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentation;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.model.SoftwareSystem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

class WikiItem {
    private Workspace workspace;
    private SoftwareSystem softwareSystem;
    private Container container;
    private Component component;
    private Element element;
    private final String wikiDocumentId;
    private final ElementType type;

    public WikiItem(Workspace workspace) {
        this.workspace = workspace;
        type = ElementType.WORKSPACE;
        this.wikiDocumentId = workspace.getProperties().get("wiki.document.id");
    }

    public WikiItem(Element element) throws IllegalArgumentException {
        this.wikiDocumentId = element.getProperties().get("wiki.document.id");
        if (wikiDocumentId == null) {
            throw new IllegalArgumentException("Element " + element.getName() + " does not have a wiki document id");
        }
        this.element = element;
        switch ((Object) element) {
            case SoftwareSystem s:
                this.softwareSystem = s;
                type = ElementType.SOFTWARE_SYSTEM;
                break;
            case Container c:
                this.container = c;
                type = ElementType.CONTAINER;
                break;
            case Component c:
                this.component = c;
                type = ElementType.COMPONENT;
                break;
            default:
                throw new IllegalArgumentException("Unexpected value: " + element);
        }

    }
    public String getName() {
        if (type == ElementType.WORKSPACE) {
            return workspace.getName();
        } else {
            return element.getName();
        }
    }
    public String getWikiDocumentId() {
        return wikiDocumentId;
    }
    public Map<String, String> getProperties() {
        return switch (type) {
            case WORKSPACE -> workspace.getProperties();
            case SOFTWARE_SYSTEM -> softwareSystem.getProperties();
            case CONTAINER -> container.getProperties();
            case COMPONENT -> component.getProperties();
        };
    }

    public Documentation getDocumentation() {
        return switch (type) {
            case WORKSPACE -> workspace.getDocumentation();
            case SOFTWARE_SYSTEM -> softwareSystem.getDocumentation();
            case CONTAINER -> container.getDocumentation();
            case COMPONENT -> component.getDocumentation();
        };
    }
    public boolean inScope(List<String> urlScope){
        if(type == ElementType.WORKSPACE && urlScope.size() == 1){
            return true;
        }else return type.getIndex() == urlScope.size() - 1 && Arrays.equals(urlScope.getLast().getBytes(), getName().getBytes());
    }

    private enum ElementType {
        WORKSPACE(0),
        SOFTWARE_SYSTEM(1),
        CONTAINER(2),
        COMPONENT(3);

        private final int index;

        ElementType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

}
