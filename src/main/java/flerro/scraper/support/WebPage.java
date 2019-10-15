package flerro.scraper.support;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class WebPage {

    private final String location;
    private final String title;
    private final String body;
    private final int errorCode;
    private final List<String> relatedUrls;

    private WebPage(Builder builder) {
        location = builder.location;
        title = builder.title;
        body = builder.body;
        errorCode = builder.errorCode;
        relatedUrls = builder.relatedUrls;
    }

    public String getLocation() {
        return location;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTitle() {
        return title;
    }

    public String getHtml() {
        return body;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<String> getRelatedUrls() {
        return relatedUrls;
    }

    public String getBaseUrl() throws MalformedURLException {
        URL url = new URL(location);
        return url.getProtocol() + "://" + url.getHost();
    }

    public String getPath() throws MalformedURLException {
        URL url = new URL(location);
        String path = url.getPath();
        path = path.endsWith("/") ? path + "index.html" : path;

        String[] tokens = path.split("/");
        String lastPathToken = tokens[tokens.length - 1];
        path = !lastPathToken.contains(".") ? path + "/index.html" : path;

        return path;
    }

    public static final class Builder {
        private String title;
        private String body;
        private int errorCode;
        private List<String> relatedUrls;
        private String location;

        private Builder() {
        }

        public Builder withTitle(String val) {
            title = val;
            return this;
        }

        public Builder withBody(String val) {
            body = val;
            return this;
        }

        public Builder withErrorCode(int val) {
            errorCode = val;
            return this;
        }

        public Builder withRelatedUrls(List<String> val) {
            relatedUrls = val;
            return this;
        }

        public WebPage build() {
            return new WebPage(this);
        }

        public Builder withLocation(String val) {
            location = val;
            return this;
        }
    }
}
