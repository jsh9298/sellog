package com.teamproject.sellog.domain.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PostController {

    @GetMapping("/{userId}/posts") // type= & limit=
    public ResponseEntity<?> userPostList(@PathVariable String userId, @RequestParam String sort,
            @RequestParam String type, @RequestParam String limit) {

        return null;
    }

    @GetMapping("/posts")
    public ResponseEntity<?> allPostList() {
        return null;
    }
}
