package kr.traverse.xrpextractor.xrpl;

import com.google.common.primitives.UnsignedInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerResult;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class Xrpl {

    private final XrplClient xrplClient;

    public long getFinalizedLedger() {
        try {
            return xrplClient.ledger(LedgerRequestParams.builder()
                            .transactions(true)
                            .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                            .build()
                    )
                    .ledgerIndex()
                    .map(e -> e.unsignedIntegerValue().longValue())
                    .orElseThrow(() -> new IllegalArgumentException("not exist finalized ledger"));
        } catch (JsonRpcClientErrorException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<LedgerResult> getLegerAsync(long ledgerIndex) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return xrplClient.ledger(LedgerRequestParams.builder()
                        .transactions(true)
                        .ledgerSpecifier(LedgerSpecifier.of(UnsignedInteger.valueOf(ledgerIndex)))
                        .build()
                );
            } catch (JsonRpcClientErrorException e) {
                log.warn("JsonRpcClientErrorException (ledgerIndex={})", ledgerIndex, e);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}
