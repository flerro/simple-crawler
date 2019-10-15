package flerro.scraper;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ScraperRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .log(LoggingLevel.ERROR, "Error [$simple{exception.message}], Headers: $simple{headers}\nStacktrace: $simple{exception.stacktrace}")
                .to("bean:metrics?method=increment(\"errors\")")
                .handled(true);

        // Start scraping flow
        from("direct:start")
                .log("Starting from ${body}...")
                .setHeader("visited", constant(new ConcurrentHashMap<String,String>()))
                .to("vm:pageProcessor?blockWhenFull=true");

        // Download page and collect related urls
        from("vm:pageProcessor?concurrentConsumers={{app.concurrent.consumers}}")
                .to("bean:webClient?method=download")
                .log("Downloaded ${body.location}")
                .to("bean:metrics?method=delta(\"enqueued\", -1)")
                .to("vm:nextPages?waitForTaskToComplete=never")
                .to("direct:storeHtml");

        Function<Exchange, ConcurrentHashMap<String, String>> visitedHeader = e -> (ConcurrentHashMap)e.getIn().getHeader("visited");
        Predicate alreadyVisited = e -> visitedHeader.apply(e).containsKey(e.getIn().getBody().toString());
        Processor addToVisited = e -> visitedHeader.apply(e).put(e.getIn().getBody().toString(), LocalDate.now().toString());

        // Queue linked pages for download
        from("vm:nextPages")
                .log("Found #${body.getRelatedUrls().size} related locations")
                .setBody(simple("${body.getRelatedUrls()}"))
                .to("bean:metrics?method=delta(\"enqueued\", ${body.size()})")
                .split(body())
                .choice()
                    .when(alreadyVisited)
                        .to("bean:metrics?method=increment(\"revisited\")")
                        .log(LoggingLevel.DEBUG, "Skipping already visited: ${body}")
                    .otherwise()
                         .process(addToVisited)
                         .to("bean:metrics?method=increment(\"visited\")")
                         .to("vm:pageProcessor?blockWhenFull=true")
                .endChoice();

        // Dump HTML to disk
        from("direct:storeHtml")
                .setHeader("fileName", simple("${body.path}"))
                .transform(simple("${body.html}"))
                .to("file:output?fileExist=ignore&fileName=.${header.fileName}");

        // Status check
        // TODO: stop app if no more queued pages or dload limit reached
        from("timer:progress?delay=5000&fixedRate=true&period=10000")
                .to("bean:metrics?method=collect()")
                .log("Status: ${body}, press CTRL-C to stop");
    }

}
