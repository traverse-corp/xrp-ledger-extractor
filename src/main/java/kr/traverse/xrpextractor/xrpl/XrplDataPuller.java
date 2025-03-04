package kr.traverse.xrpextractor.xrpl;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import kr.traverse.xrpextractor.component.XrpWebClient;
import kr.traverse.xrpextractor.xrpl.dto.LedgerDto;
import kr.traverse.xrpextractor.xrpl.dto.XrpInsertDto;
import kr.traverse.xrpextractor.xrpl.dto.XrpInsertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


@Component
@RequiredArgsConstructor
@Slf4j
public class XrplDataPuller {

    @Value("${file.system.step}")
    public Long step;

    @Value("${file.system.api.batch-size}")
    public Integer batch;

    @Value("${file.system.local.base-path:#{environment.getProperty('user.dir')}}")
    final public String localBasePath = System.getProperty("user.dir");

    final private AtomicLong nextLedger = new AtomicLong(0L);

    private final Xrpl xrpl;

    private final XrplParser xrplParser;

    private final XrpWebClient xrpWebClient;

    public void setNextLedger(long startBlockNumber) {
        this.nextLedger.compareAndSet(nextLedger.get(), startBlockNumber);
    }

    public void dataPull() {
        this.dataPull(this.nextLedger.get());
    }

    private void dataPull(long startLedger) {
        final long FINALIZED_BLOCK_LEDGER = xrpl.getFinalizedLedger() - 20;
        log.info("XRP data pull start (startLedger={}, finalizedLedger={}, step={}, batch={})", startLedger, FINALIZED_BLOCK_LEDGER, step, batch);

        for (long ledgerNumber = startLedger; ledgerNumber <= FINALIZED_BLOCK_LEDGER; ledgerNumber += step) {
            long startLedgerNumber = ledgerNumber;
            long endLedgerNumber = Long.min(FINALIZED_BLOCK_LEDGER, startLedgerNumber + step - 1);
            long dataSize = endLedgerNumber - startLedgerNumber + 1;

            if (dataSize != step) {
                log.info("not exist sufficient data (startLedger={}, endBlock={}, dataSize={}, requiredSize={})", startLedgerNumber, endLedgerNumber, dataSize, step);
                break;
            }

            List<LedgerDto> ledgerResults = Flowable.rangeLong(startLedgerNumber, dataSize)
                    .subscribeOn(Schedulers.io())
                    .buffer(batch)
                    .map(ledgerIndexes -> {
                        List<CompletableFuture<LedgerDto>> futures = ledgerIndexes.stream()
                                .map(xrplParser::getLedgerWithPaymentAsync).toList();
                        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
                        return futures.stream().map(CompletableFuture::join)
                                .filter(Objects::nonNull);
                    }).toList().blockingGet().stream().flatMap(Function.identity()).toList();

            List<XrpInsertDto> xrpInsertDtos = extractData(ledgerResults);
            XrpInsertRequest xrpInsertRequest = XrpInsertRequest.builder()
                    .xrpInsertDtos(xrpInsertDtos)
                    .startLedgerNumber(startLedgerNumber)
                    .endLedgerNumber(endLedgerNumber)
                    .build();
            xrpWebClient.insert(xrpInsertRequest);

            log.info("save data to neo4j server success(startLedger={}, endLedger={})", startLedgerNumber, endLedgerNumber);
            setNextLedger(endLedgerNumber + 1);
        }
    }

    public List<XrpInsertDto> extractData(List<LedgerDto> ledgerResults) {
        var xrpInsertDtos = new ArrayList<XrpInsertDto>();

        ledgerResults.forEach(ledgerDto -> {
            Long ledgerNumber = Long.valueOf(ledgerDto.getLedgerIndex());
            Long timestamp = ledgerDto.getTimestamp();

            for (var paymentDto : ledgerDto.getPaymentDtos()) {
                String transactionHash = paymentDto.getTransactionHash();

                String fromAccountAddress = paymentDto.getFromAccountAddress();
                String toAccountAddress = paymentDto.getToAccountAddress();
                String currency = paymentDto.getCurrency();
                String amount = paymentDto.getAmount();
                String destinationTag = paymentDto.getDestinationTag().isEmpty() ? null : paymentDto.getDestinationTag();

                xrpInsertDtos.add(XrpInsertDto.builder()
                        .ledgerNumber(ledgerNumber)
                        .value(amount)
                        .timestamp(timestamp)
                        .currency(currency)
                        .destinationTag(destinationTag)
                        .fromAccountAddress(fromAccountAddress)
                        .toAccountAddress(toAccountAddress)
                        .transactionHash(transactionHash)
                        .build()
                );
            }
        });
        return xrpInsertDtos;
    }
}