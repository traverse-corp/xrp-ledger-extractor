package kr.traverse.xrpextractor.configuration;

import com.fasterxml.jackson.databind.JsonMappingException;
import kr.traverse.xrpextractor.xrpl.XrplDataPuller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ScheduledConfiguration implements SchedulingConfigurer {

    private final XrplDataPuller xrplDataPuller;
    private final AtomicReference<LocalDateTime> latest = new AtomicReference<>(null);

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setErrorHandler(throwable -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lt = latest.get();

            if ((lt == null || now.minusHours(12L).isAfter(lt)) && getThrowables(throwable).contains(JsonMappingException.class)){
                log.info("[XRP] scheduled error occurred", throwable);
                latest.set(now);
                return;
            }
            if (lt == null || now.minusHours(12L).isAfter(lt)) {
                log.warn("[XRP] scheduled error occurred", throwable);
                latest.set(now);
            }
        });
        threadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }

    @Scheduled(fixedDelay = 2 * 1000, initialDelay = 1500)
    public void call() {
        xrplDataPuller.dataPull();
    }

    private List<Class<? extends Throwable>> getThrowables(Throwable throwable) {
        var ret = new ArrayList<Class<? extends Throwable>>();
        var cause = throwable;
        while (cause != null) {
            ret.add(cause.getClass());
            cause = cause.getCause();
        }
        return ret;
    }
}

