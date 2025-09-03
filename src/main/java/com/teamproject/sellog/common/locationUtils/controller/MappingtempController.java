package com.teamproject.sellog.common.locationUtils.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teamproject.sellog.common.locationUtils.DataMapping.GeometryColumns;
import com.teamproject.sellog.common.locationUtils.DataMapping.LsmdAdmSectUmd;
import com.teamproject.sellog.common.locationUtils.DataMapping.SpatialRefSys;
import com.teamproject.sellog.common.locationUtils.repository.GeometryColumnRepository;
import com.teamproject.sellog.common.locationUtils.repository.LsctLawdcdRepository;
import com.teamproject.sellog.common.locationUtils.repository.LsmdAdmSectUmdRepository;
import com.teamproject.sellog.common.locationUtils.repository.SpatialRefSysRepository;

import lombok.RequiredArgsConstructor;

import com.teamproject.sellog.common.locationUtils.DataMapping.LsctLawdcd;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MappingtempController {
    // TODO : 임시로 만든 com.teamproject.sellog.common.locationUtils.repository의
    // repository들을 연결하여 데이터들 확인용 controller(임시 생성)작성하기. 임시라서 서비스레이어는 생략

    private final LsctLawdcdRepository lsctLawdcdRepository;
    private final SpatialRefSysRepository spatialRefSysRepository;
    private final GeometryColumnRepository geometryColumnRepository;
    private final LsmdAdmSectUmdRepository lsmdAdmSectUmdRepository;

    @GetMapping("/lsctLawdcd")
    public List<LsctLawdcd> getAllLsctLawdcd() {
        return lsctLawdcdRepository.findAll();
    }

    @GetMapping("/spatialRefSys")
    public List<SpatialRefSys> getAllSpatialRefSys() {
        return spatialRefSysRepository.findAll();
    }

    @GetMapping("/geometryColumns")
    public List<GeometryColumns> getAllGeometryColumns() {
        return geometryColumnRepository.findAll();
    }

    @GetMapping("/lsmdAdmSectUmd")
    public List<LsmdAdmSectUmd> getAllLsmdAdmSectUmd() {
        return lsmdAdmSectUmdRepository.findAll();
    }
}