package kr.traverse.xrpextractor.xrpl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;

@Data
@Builder
public class XrpInsertDto {
    @NonNull
    private Long ledgerNumber;
    @NonNull
    private String value;
    @NonNull
    private Long timestamp;
    @NonNull
    private String currency;
    @Nullable
    private String destinationTag;
    @NonNull
    private String fromAccountAddress;
    @NonNull
    private String toAccountAddress;
    @NonNull
    private String transactionHash;
}
