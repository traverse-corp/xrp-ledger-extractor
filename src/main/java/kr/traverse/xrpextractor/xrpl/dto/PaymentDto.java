package kr.traverse.xrpextractor.xrpl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class PaymentDto {
    @NonNull
    private String fromAccountAddress;
    @NonNull
    private String toAccountAddress;
    @NonNull
    private String currency;
    @NonNull
    private String amount;
    @NonNull
    private String destinationTag;
    @NonNull
    private String transactionHash;
}
