package flerro.scraper;

import flerro.scraper.support.WebClient;
import flerro.scraper.support.MetricsRegistry;
import org.apache.camel.BindToRegistry;
import org.apache.camel.PropertyInject;

public class ScraperConfiguration {

    @BindToRegistry
    public WebClient webClient(@PropertyInject("app.proxy.host") String proxyHost,
                               @PropertyInject("app.proxy.port") String proxyPort) throws Exception {
        return new WebClient(proxyHost, proxyPort);
    }

    @BindToRegistry
    public MetricsRegistry metrics() {
        return new MetricsRegistry();
    }

}
