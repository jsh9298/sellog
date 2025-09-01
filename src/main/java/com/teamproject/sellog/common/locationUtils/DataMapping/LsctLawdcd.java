package com.teamproject.sellog.common.locationUtils.DataMapping;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "lsct_lawdcd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Immutable
public class LsctLawdcd {

    @Id
    @Column(name = "LAWD_CD", length = 10)
    private String lawdCd;

    @Column(name = "SIDO_NM", length = 30)
    private String sidoNm;

    @Column(name = "SGG_NM", length = 30)
    private String sggNm;

    @Column(name = "UMD_NM", length = 30)
    private String umdNm;

    @Column(name = "RI_NM", length = 30)
    private String riNm;

    @Column(name = "CRE_DT", length = 8)
    private String creDt;

    @Column(name = "DEL_DT", length = 8)
    private String delDt;

    @Column(name = "OLD_LAWDCD", length = 10)
    private String oldLawdcd;

    @Column(name = "FRST_REGIST_DT", nullable = false, length = 8)
    private String frstRegistDt;

    @Column(name = "LAST_UPDT_DT", nullable = false, length = 8)
    private String lastUpdtDt;
}