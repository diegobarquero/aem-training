package com.adobe.aem.studio.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RangeList {

    @Inject
    @RequestAttribute
    private int max;

    private List<Integer> numbers;

    @PostConstruct
    protected void init() {
        numbers = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            numbers.add(i);
        }
    }

    public List<Integer> getNumbers() {
        return numbers;
    }
}

