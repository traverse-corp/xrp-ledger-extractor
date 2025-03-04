package kr.traverse.xrpextractor.configuration;

import okhttp3.HttpUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xrpl.xrpl4j.client.XrplClient;

@Configuration
public class Xrpl4jConfiguration {

    @Value("${xrpl.url}")
    private String url;

    @Bean
    public XrplClient apiWrapper() {
        HttpUrl rippledUrl = HttpUrl.get(url);
        return new XrplClient(rippledUrl);
    }
}
