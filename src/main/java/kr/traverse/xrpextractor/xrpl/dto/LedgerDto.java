package kr.traverse.xrpextractor.xrpl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class LedgerDto {
    @NonNull
    private String ledgerIndex;
    @NonNull
    private Long timestamp;

    @NonNull
    private List<PaymentDto> paymentDtos;
}
