package com.sampath.portal.entity;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TbaadmRtlId implements Serializable {

    @Column(name = "FXD_CRNCY_CODE")
    private String fxdCrncyCode;

    @Column(name = "VAR_CRNCY_CODE")
    private String varCrncyCode;

    @Column(name = "RTLIST_NUM")
    private String rtlistNum;

    @Column(name = "RTLIST_DATE")
    private LocalDate rtlistDate;

    @Column(name = "RATECODE")
    private String rateCode;

    // ⚠️ Required for composite key: implement equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TbaadmRtlId)) return false;
        TbaadmRtlId that = (TbaadmRtlId) o;
        return fxdCrncyCode.equals(that.fxdCrncyCode)
                && varCrncyCode.equals(that.varCrncyCode)
                && rtlistNum.equals(that.rtlistNum)
                && rtlistDate.equals(that.rtlistDate)
                && rateCode.equals(that.rateCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fxdCrncyCode, varCrncyCode, rtlistNum, rtlistDate, rateCode);
    }
}
