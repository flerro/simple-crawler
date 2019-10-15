package flerro.scraper.support;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WebClient {

    final Proxy proxy;



    public WebClient() throws NoSuchAlgorithmException, KeyManagementException {
        this(null, null);
    }

    public WebClient(String proxyHost, String proxyPort) throws KeyManagementException, NoSuchAlgorithmException {
        if (proxyHost != null && proxyHost.length() != 0
                && proxyPort != null && proxyPort.length() != 0) {
            InetSocketAddress addr = new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort));
            proxy = new Proxy(Proxy.Type.HTTP, addr);
            trustAllCertificates();
        } else {
            proxy = null;
        }
    }

    private void trustAllCertificates() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    public WebPage download(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                            .followRedirects(true)
                            .proxy(proxy).get();

        Elements links = doc.select("a[href]");
        URL remote = new URL(url);
        String baseUrl = remote.getProtocol() + "://" + remote.getHost();

        Predicate<String> relativePath = s -> s.startsWith(".") || s.startsWith("/");
        Predicate<String> sameDomain = s -> s.startsWith(baseUrl);
        Predicate<String> nonExternalLinks = s -> relativePath.test(s) || sameDomain.test(s);

        List<String> relatedHrefs = links.stream()
                                    .map(el -> el.attr("href"))
                                    .filter(nonExternalLinks)
                                    .map(r -> r.startsWith("http") ? r : baseUrl + r)
                                    .collect(Collectors.toList());

        return WebPage.builder()
                .withLocation(url)
                .withTitle(doc.title())
                .withBody(doc.html())
                .withRelatedUrls(relatedHrefs)
                .build();
    }
}
