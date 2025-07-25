package com.teamproject.sellog.domain.post.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.model.entity.HashBoard;
import com.teamproject.sellog.domain.post.model.entity.HashTag;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.TagRepository;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public PostService(final PostRepository postRepository,
            final UserRepository userRepository, final TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void posting(PostRequestDto dto) {
        Post post = createPost(dto);
        if (dto.getTagNames() != null && dto.getTagNames().size() > 0) {
            addTagsToPost(post, dto.getTagNames());
        }
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(UUID postId) {
        List<String> tagNames = new ArrayList<String>();
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Set<HashBoard> hashBoards = post.getHashBoard();
        for (HashBoard hashBoard : hashBoards) {
            Optional<HashTag> tag = tagRepository.findByHashBoard(hashBoard);
            if (tag.isPresent()) {
                tagNames.add(tag.get().getTagName());
            }
        }
        return PostResponseDto.builder()
                .type(post.getPostType())
                .title(post.getTitle())
                .userId(post.getAuthor().getUserId())
                .contents(post.getContent())
                .tagNames(tagNames)
                .place(post.getPlace())
                .price(post.getPrice())
                .build();
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Post createPost(PostRequestDto dto) {
        Post post = new Post();
        post.setAuthor(getUser(dto.getUserId()));
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContents());
        post.setThumbnail(dto.getThumbnail());
        post.setPostType(dto.getType());
        if (dto.getType() == PostType.PRODUCT) {
            post.setPrice(dto.getPrice());
            post.setPlace(dto.getPlace());
        }
        return post;
    }

    private void addTagsToPost(Post post, List<String> tags) {
        for (String tagName : tags) {
            HashTag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> {
                        HashTag newTag = new HashTag();
                        newTag.setTagName(tagName);
                        return tagRepository.save(newTag);
                    });
            HashBoard board = new HashBoard();
            post.addHash(board);
            tag.addHash(board);
        }
    }
}