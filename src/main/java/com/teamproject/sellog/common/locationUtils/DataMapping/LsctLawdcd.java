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
@Immutable
public class LsctLawdcd {

    @Id
    @Column(name = "LAWD_CD", length = 10, nullable = false)
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

    @Column(name = "FRST_REGIST_DT", length = 8, nullable = false)
    private String frstRegistDt;

    @Column(name = "LAST_UPDT_DT", length = 8, nullable = false)
    private String lastUpdtDt;

    // Getter methods
    public String getLawdCd() {
        return lawdCd;
    }

    public String getSidoNm() {
        return sidoNm;
    }

    public String getSggNm() {
        return sggNm;
    }

    public String getUmdNm() {
        return umdNm;
    }

    public String getRiNm() {
        return riNm;
    }

    public String getCreDt() {
        return creDt;
    }

    public String getDelDt() {
        return delDt;
    }

    public String getOldLawdcd() {
        return oldLawdcd;
    }

    public String getFrstRegistDt() {
        return frstRegistDt;
    }

    public String getLastUpdtDt() {
        return lastUpdtDt;
    }

    // Hibernate 및 JPA를 위한 기본 생성자
    protected LsctLawdcd() {
    }
}