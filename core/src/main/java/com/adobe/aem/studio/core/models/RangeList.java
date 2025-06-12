package com.adobe.aem.studio.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class RangeList {

    @Inject
    private String maxNumber;

    private List<Integer> numbers;

    private List<String> numbersString;

    @PostConstruct
    protected void init() {

        numbers = new ArrayList<>();

        numbersString = new ArrayList<>();

        try {
            
            int max = Integer.parseInt(maxNumber);

            for (int i = 0; i < max; i++) {

                numbers.add(i);

                numbersString.add(String.valueOf(i));

            }
        } catch (NumberFormatException e) {
          // Return string error
        }
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public List<String> getNumbersString() {
        return numbersString;
    }
}

