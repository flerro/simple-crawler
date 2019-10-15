package flerro.scraper;


import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.Main;
import org.apache.camel.main.MainListenerSupport;
import org.apache.camel.main.MainSupport;

public class Application {

    private Application() {
    }

    public static void main(String[] args) throws Exception {
        String[] argsNoUrl = new String[args.length - 1];
        String startingWebPage = args[args.length - 1];
        System.arraycopy(args, 0, argsNoUrl, 0, argsNoUrl.length);

        Main main = new Main();
        main.addConfigurationClass(ScraperConfiguration.class);
        main.addRouteBuilder(ScraperRouteBuilder.class);

        main.addMainListener(new MainListenerSupport() {
            @Override
            public void afterStart(MainSupport main) {
                ProducerTemplate template = main.getCamelContext().createProducerTemplate();
                template.asyncSendBody("direct:start", startingWebPage);
            }
        });

        // now keep the application running until the JVM is terminated (ctrl + c or sigterm)
        main.run(argsNoUrl);
    }

}
