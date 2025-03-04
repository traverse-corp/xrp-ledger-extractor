package kr.traverse.xrpextractor;

import kr.traverse.xrpextractor.xrpl.XrplDataPuller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class XrpExtractorApplication {

	private final XrplDataPuller xrplDataPuller;
	private final Environment env;

	public static void main(String[] args) {
		SpringApplication.run(XrpExtractorApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void init() {
		Optional<Long> startOptional = Optional.ofNullable(env.getProperty("start")).map(Long::parseLong);
		startOptional.ifPresentOrElse(
				start -> {
					log.info("startLedger={}, endLedger=FINALIZED", start);
					xrplDataPuller.setNextLedger(start);
				}
				, () -> {
					log.info("startLedger=EARLIEST, endLedger=FINALIZED");
				}
		);
	}
}
