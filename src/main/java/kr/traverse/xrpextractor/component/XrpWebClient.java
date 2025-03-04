package kr.traverse.xrpextractor.component;

import kr.traverse.xrpextractor.xrpl.dto.XrpInsertRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@Slf4j
public class XrpWebClient {

    private final WebClient webClient;

    public XrpWebClient(
            @Value("${neo4j.back.url}") String url,
            @Value("${neo4j.back.port}") String port

    ) {
        String baseUrl = url + ":" + port;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public void insert(XrpInsertRequest xrpInsertRequest) {
        webClient.post()
                .uri("/xrp/insert")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(xrpInsertRequest))
                .retrieve()
                .bodyToMono(Void.class)
                .retry(1)
                .block();
    }
}
