package com.structurizr.onpremises.util;

import com.structurizr.Workspace;
import com.structurizr.documentation.Documentation;
import com.structurizr.model.Component;
import com.structurizr.model.Container;
import com.structurizr.model.Element;
import com.structurizr.model.SoftwareSystem;

import java.util.Map;

class WikiItem {
    private Workspace workspace;
    private SoftwareSystem softwareSystem;
    private Container container;
    private Component component;
    private String name;

    public WikiItem(Workspace workspace) {
        this.workspace = workspace;
        this.name = workspace.getName();
    }


    public WikiItem(SoftwareSystem softwareSystem) {
        this.softwareSystem = softwareSystem;
        this.name = softwareSystem.getName();
    }

    public WikiItem(Container container) {
        this.container = container;
        this.name = container.getName();

    }

    public WikiItem(Component component) {
        this.component = component;
        this.name = component.getName();

    }

    public WikiItem(Element element) {
        if (element instanceof SoftwareSystem) {
            this.softwareSystem = (SoftwareSystem) element;
        } else if (element instanceof Container) {
            this.container = (Container) element;
        } else if (element instanceof Component) {
            this.component = (Component) element;
        }
    }
    public String getName() {
        return name;
    }
    public Map<String, String> getProperties() {
        if (workspace != null) {
            return workspace.getProperties();
        } else if (softwareSystem != null) {
            return softwareSystem.getProperties();
        } else if (container != null) {
            return container.getProperties();
        } else if (component != null) {
            return component.getProperties();
        }
        return null;
    }

    public Documentation getDocumentation() {
        if (workspace != null) {
            return workspace.getDocumentation();
        } else if (softwareSystem != null) {
            return softwareSystem.getDocumentation();
        } else if (container != null) {
            return container.getDocumentation();
        } else if (component != null) {
            return component.getDocumentation();
        }
        return null;
    }
}
