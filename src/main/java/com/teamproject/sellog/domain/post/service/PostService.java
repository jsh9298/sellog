package com.teamproject.sellog.domain.post.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.ProductRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ProductRepository productRepository;

    public PostService(final PostRepository postRepository, final ProductRepository productRepository) {
        this.postRepository = postRepository;
        this.productRepository = productRepository;
    }
}
