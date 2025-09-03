package com.teamproject.sellog.common.locationUtils.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.teamproject.sellog.common.locationUtils.DataMapping.LsmdAdmSectUmd;

@Repository
public interface LsmdAdmSectUmdRepository extends JpaRepository<LsmdAdmSectUmd, Integer> {

}
