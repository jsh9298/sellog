package com.teamproject.sellog.domain.post.service;

import org.springframework.stereotype.Service;

import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.ProductRepository;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PostService(final PostRepository postRepository, final ProductRepository productRepository,
            final UserRepository userRepository) {
        this.postRepository = postRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public void posting(PostRequestDto post) {
        if (post.getType() == PostType.POST) {
            Post newPost = new Post();
            newPost.setAuthor(getUser(post.getUserId()));
            newPost.setTitle(post.getTitle());
            newPost.setContent(post.getContents());
            newPost.setThumbnail(post.getThumbnail());
            postRepository.save(newPost);
        } else if (post.getType() == PostType.PRODUCT) {

        }
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
