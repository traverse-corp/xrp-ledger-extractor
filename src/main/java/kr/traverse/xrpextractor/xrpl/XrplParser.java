package kr.traverse.xrpextractor.xrpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.primitives.UnsignedInteger;
import kr.traverse.xrpextractor.xrpl.dto.LedgerDto;
import kr.traverse.xrpextractor.xrpl.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xrpl.xrpl4j.client.JsonRpcRequest;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.ledger.ImmutableLedgerRequestParams;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class XrplParser {

    private final XrplClient xrplClient;

    public CompletableFuture<LedgerDto> getLedgerWithPaymentAsync(long ledgerNumber) {
        return CompletableFuture.supplyAsync(() -> this.getLedgerWithPayment(ledgerNumber));
    }

    public LedgerDto getLedgerWithPayment(long ledgerNumber) {
        ImmutableLedgerRequestParams params = LedgerRequestParams.builder()
                .ledgerSpecifier(LedgerSpecifier.of(UnsignedInteger.valueOf(ledgerNumber)))
                .transactions(true)
                .build();

        JsonNode jsonNode = xrplClient.getJsonRpcClient().postRpcRequest(JsonRpcRequest.builder()
                .method("ledger").addParams(params).build()
        );

        if (Optional.ofNullable(jsonNode).map(e -> e.get("result")).map(e -> e.get("error")).map(JsonNode::asText).isPresent()) {
            String errorMessage = jsonNode.get("result").get("error").asText();
            log.warn("{} (ledgerNumber={})", errorMessage, ledgerNumber);
            return null;
        }

        String ledgerIndex = jsonNode.get("result")
                .get("ledger")
                .get("ledger_index")
                .asText();

        long timestamp = jsonNode.get("result")
                .get("ledger")
                .get("close_time")
                .asLong();

        Iterator<JsonNode> transactionJsonNodes = jsonNode.get("result")
                .get("ledger")
                .get("transactions")
                .elements();

        List<PaymentDto> paymentDtos = StreamSupport.stream(Spliterators.spliteratorUnknownSize(transactionJsonNodes, Spliterator.ORDERED), false)
                .filter(transactionJsonNode -> transactionJsonNode.get("TransactionType").asText("").equalsIgnoreCase("Payment"))
                .map(transactionJsonNode -> {
                    String fromAccountAddress = transactionJsonNode.get("Account").asText();
                    String toAccountAddress = transactionJsonNode.get("Destination").asText();
                    String amount = transactionJsonNode.get("Amount").isTextual() ? transactionJsonNode.get("Amount").asText() :
                            transactionJsonNode.get("Amount").get("value").asText();
                    String currency = transactionJsonNode.get("Amount").isTextual() ? "XRP" :
                            transactionJsonNode.get("Amount").get("currency").asText();
                    String destinationTag = Optional.ofNullable(transactionJsonNode.get("DestinationTag")).map(JsonNode::asText).orElse("");
                    String transactionHash = transactionJsonNode.get("hash").asText();

                    return PaymentDto.builder()
                            .toAccountAddress(toAccountAddress)
                            .fromAccountAddress(fromAccountAddress)
                            .currency(currency)
                            .amount(amount)
                            .destinationTag(destinationTag)
                            .transactionHash(transactionHash)
                            .build();
                }).toList();

        return LedgerDto.builder()
                .ledgerIndex(ledgerIndex)
                .timestamp(timestamp)
                .paymentDtos(paymentDtos)
                .build();
    }
}
