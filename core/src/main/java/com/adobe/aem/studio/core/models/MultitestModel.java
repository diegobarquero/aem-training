package com.adobe.aem.studio.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.*;
import org.apache.sling.models.annotations.injectorspecific.*;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Model(
        adaptables = SlingHttpServletRequest.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class MultitestModel {

    @Inject
    private Resource resource; // Injects the current component's resource

    private List<String> textValues;

    @PostConstruct
    protected void init() {
        textValues = new ArrayList<>();

        Resource multifieldParent = resource.getChild("multitest");

        if (multifieldParent != null) {
            for (Resource item : multifieldParent.getChildren()) {
                String value = item.getValueMap().get("textvalue", String.class);
                if (value != null) {
                    textValues.add(value);
                }
            }
        }
    }

    public List<String> getTextValues() {
        return textValues;
    }
}