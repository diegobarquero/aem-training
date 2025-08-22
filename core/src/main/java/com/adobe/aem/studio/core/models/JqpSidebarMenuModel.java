package com.adobe.aem.studio.core.models;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Inject;
import javax.inject.Named;

import com.day.cq.commons.Externalizer;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class JqpSidebarMenuModel {

    @ValueMapValue
    private String logoPath;

    @Inject
    private ResourceResolver resolver;

    @Inject
    private Externalizer externalizer;

    @ChildResource(name = "menuItem")
    private List<Resource> menuItems;

    public String getLogoPath() {
        return logoPath;
    }

    public List<MenuItem> getMenuItems() {
        if (menuItems == null) return Collections.emptyList();
        return menuItems.stream()
            .map(item -> new MenuItem(item, resolver, externalizer))
            .collect(Collectors.toList());
    }

    public int getMenuItemsSize() {
        return menuItems != null ? menuItems.size() : 0;
    }

    public static class MenuItem {
        private final Resource resource;
        private final ResourceResolver resolver;
        private final Externalizer externalizer;

        public MenuItem(Resource resource, ResourceResolver resolver, Externalizer externalizer) {
            this.resource = resource;
            this.resolver = resolver;
            this.externalizer = externalizer;
        }

        public String getTitle() {
            return resource.getValueMap().get("title", String.class);
        }

        public String getIcon() {
            return resource.getValueMap().get("icon", String.class);
        }

        public boolean isExternal() {
            return "external".equals(resource.getValueMap().get("linkType", String.class));
        }

        public String getUrl() {
            if (isExternal()) {
                return resource.getValueMap().get("externalLink", "");
            }

            String internalLink = resource.getValueMap().get("internalLink", String.class);
            if (internalLink != null && !internalLink.isEmpty()) {
                return externalizer.publishLink(resolver, internalLink);
            }

            return "#";
        }

        public List<MenuItem> getSubItems() {
            Resource subItems = resource.getChild("subItem");
            if (subItems == null) return Collections.emptyList();
            return StreamSupport.stream(subItems.getChildren().spliterator(), false)
                    .map(child -> new MenuItem(child, resolver, externalizer))
                    .collect(Collectors.toList());
        }
    }
}
