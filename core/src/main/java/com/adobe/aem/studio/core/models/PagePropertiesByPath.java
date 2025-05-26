package com.adobe.aem.studio.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PagePropertiesByPath {

    @Inject
    private Resource resource;

    @Inject
    @Optional
    private String pagePath;

    private ValueMap pageProperties;

    @PostConstruct
    protected void initModel() {
        Page originPage = null;
        PageManager pm = resource.getResourceResolver().adaptTo(PageManager.class);

        if (pm != null) {
            originPage = pm.getPage(pagePath);
        }

        if (originPage != null) {
            pageProperties = originPage.getProperties();
        }
    }

    public ValueMap getPageProperties() {
        return pageProperties;
    }
}
