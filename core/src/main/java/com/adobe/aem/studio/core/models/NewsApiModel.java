package com.adobe.aem.studio.core.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NewsApiModel {

    @Self
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String companyName;

    @ValueMapValue
    private String fromDate;

    @ValueMapValue
    private String toDate;

    @ValueMapValue
    private String apiKey;

    @ValueMapValue
    private String language;

    @ValueMapValue
    private String sortBy;

    private final List<Map<String, Object>> newsData = new ArrayList<>();

    @PostConstruct
    protected void init() {
        try {
            String baseUrl = "https://newsapi.org/v2/everything";

            // Parse and format ISO 8601 to "yyyy-MM-dd"
            DateTimeFormatter inputFormat = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            String from = OffsetDateTime.parse(fromDate, inputFormat).format(outputFormat);
            String to = OffsetDateTime.parse(toDate, inputFormat).format(outputFormat);

            String queryParams =
                    "q=" + URLEncoder.encode(companyName, "UTF-8") +
                            "&from=" + from +
                            "&to=" + to +
                            "&sortBy=" + sortBy +
                            "&language=" + language +
                            "&apiKey=" + URLEncoder.encode(apiKey, "UTF-8");

            URL url = new URL(baseUrl + "?" + queryParams);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream input = connection.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(input);

            JsonNode articlesNode = root.get("articles");
            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode article : articlesNode) {
                    Map<String, Object> articleMap = mapper.convertValue(article, Map.class);
                    newsData.add(articleMap);
                }
            }

            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getNewsData() {
        return newsData;
    }
}
