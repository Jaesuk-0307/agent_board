package com.example.board.controller;

import com.example.board.entity.Post;
import com.example.board.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestApiController {

    private final PostService service;

    public TestApiController(PostService service) {
        this.service = service;
    }

    // 글 하나 생성 (title=hello&name=kim&content=test)
    @PostMapping("/testNew")
    public ResponseEntity<String> testNew(@Valid Post post, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }
        service.create(post);
        return ResponseEntity.ok("OK");
    }

    // 최근 글 하나 삭제 API (실패 시 최대 3회 재시도, 계속 실패하면 409)
    @PostMapping("/testDelete")
    public ResponseEntity<String> testDelete() {
        for (int i = 0; i < 3; i++) {
            if (service.deleteLatest()) {
                return ResponseEntity.ok("OK");
            }
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("삭제 실패");
    }
}
