package com.structurizr.onpremises.component.search;

import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Section;
import com.structurizr.model.Element;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

abstract class AbstractSearchComponentImpl implements SearchComponent {

    protected String urlEncode(String value) throws Exception {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
    }

    protected String calculateUrl(Element element, Section section) throws Exception {
        if (element == null) {
            return "#" + section.getOrder();
        } else {
            while (element.getParent() != null) {
                element = element.getParent();
            }

            return urlEncode(element.getName()) + "#" + section.getOrder();
        }
    }

    protected String calculateUrl(Element element, Decision decision) throws Exception {
        if (element == null) {
            return "#" + urlEncode("" + decision.getId());
        } else {
            return urlEncode(element.getName()) + "#" + urlEncode(decision.getId());
        }
    }

}