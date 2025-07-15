package com.teamproject.sellog.domain.post.controller;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PostController {

    @GetMapping("/posts") // 페이징
    public ResponseEntity<?> postList(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Timestamp lastCreateAt,
            @RequestParam(required = false) UUID lastId,
            @RequestParam(defaultValue = "10") String limit) {

        return null;
    }

    @GetMapping("/read")
    public ResponseEntity<?> getPost() {
        return null;
    }

    @PostMapping("/posting")
    public ResponseEntity<?> posting() {
        return null;
    }

    @PatchMapping("/edit")
    public ResponseEntity<?> editPost() {
        return null;
    }

}
