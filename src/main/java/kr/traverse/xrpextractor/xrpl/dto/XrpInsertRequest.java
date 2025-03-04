package kr.traverse.xrpextractor.xrpl.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Builder
public class XrpInsertRequest {
    @NonNull
    private List<XrpInsertDto> xrpInsertDtos;
    @NonNull
    private Long startLedgerNumber;
    @NonNull
    private Long endLedgerNumber;
}
