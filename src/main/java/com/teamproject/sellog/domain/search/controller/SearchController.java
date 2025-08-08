package com.teamproject.sellog.domain.search.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//검색 게시글 제목, 해시태그, 사용자 id, 이름, 닉네임
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "검색", description = "검색 관련 api <br> 현재 정의된 검색 대상 : 게시글(제목,해시태그),사용자(id,이름,닉네임)")
public class SearchController {

    @GetMapping("/search")
    @Operation(summary = "통합검색(-)", description = "검색어 이외의 필터링은 차후에 논의<br>접두사 별 검색 구분 <br> 없을시 : 사용자(이름,닉네임),게시글 제목 <br> @ : 사용자id <br> # : 해시태그 <br> ")
    public ResponseEntity<?> getMethodName(@RequestParam String q,
            @RequestParam(required = false) String lastId, // 마지막으로 검색된 결과 번호
            @RequestParam(defaultValue = "10") String limit) {
        return null;
    }

}
