package com.teamproject.sellog.domain.post.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamproject.sellog.common.accountsUtils.CheckStatus;
import com.teamproject.sellog.common.responseUtils.BusinessException;
import com.teamproject.sellog.common.responseUtils.CursorPageResponse;
import com.teamproject.sellog.common.responseUtils.ErrorCode;
import com.teamproject.sellog.domain.post.mapper.PostMapper;
import com.teamproject.sellog.domain.post.model.dto.request.PostRequestDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostListResponseDto;
import com.teamproject.sellog.domain.post.model.dto.response.PostResponseDto;
import com.teamproject.sellog.domain.post.model.entity.FeedBackType;
import com.teamproject.sellog.domain.post.model.entity.HashBoard;
import com.teamproject.sellog.domain.post.model.entity.HashTag;
import com.teamproject.sellog.domain.post.model.entity.Post;
import com.teamproject.sellog.domain.post.model.entity.PostFeedbackList;
import com.teamproject.sellog.domain.post.model.entity.PostType;
import com.teamproject.sellog.domain.post.repository.PostRepository;
import com.teamproject.sellog.domain.post.repository.TagRepository;
import com.teamproject.sellog.domain.post.service.event.PostCreatedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDeletedEvent;
import com.teamproject.sellog.domain.post.service.event.PostDislikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostLikedEvent;
import com.teamproject.sellog.domain.post.service.event.PostUpdatedEvent;
import com.teamproject.sellog.domain.user.model.entity.user.User;
import com.teamproject.sellog.domain.user.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostMapper postMapper;
    private final ApplicationEventPublisher eventPublisher;

    public PostService(final PostRepository postRepository,
            final UserRepository userRepository, final TagRepository tagRepository, final PostMapper postMapper,
            final ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.postMapper = postMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void posting(PostRequestDto dto) {
        Post post = createPost(dto);
        User author = post.getAuthor();
        if (author != null && author.getUserProfile() != null) {
            if (dto.getType() == PostType.PRODUCT) {
                author.getUserProfile().setProductCount(author.getUserProfile().getProductCount() + 1);
            } else {
                author.getUserProfile().setPostCount(author.getUserProfile().getPostCount() + 1);
            }
        }
        postRepository.save(post);
        if (dto.getTagNames() != null && dto.getTagNames().size() > 0) {
            addTagsToPost(post, dto.getTagNames());
        }
        if (dto.getIsPublic()) {
            eventPublisher.publishEvent(new PostCreatedEvent(this, post.getId()));
        }
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(UUID postId, Cookie postViewCookie, HttpServletResponse response) {

        if (postViewCookie != null) {
            if (!postViewCookie.getValue().contains("[" + postId + "]")) {
                postRepository.updateViewCount(postId);
                postViewCookie.setValue(postViewCookie.getValue() + "_[" + postId + "]");
                postViewCookie.setPath("/");
                postViewCookie.setMaxAge(60 * 60 * 24);
                response.addCookie(postViewCookie);
            }

        } else {
            postRepository.updateViewCount(postId); // [2]
            Cookie newCookie = new Cookie("postView", "[" + postId + "]");
            newCookie.setPath("/");
            newCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(newCookie);
        }

        List<String> tagNames = new ArrayList<String>();
        Post post = postRepository.findWithDetailsById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Set<HashBoard> hashBoards = post.getHashBoard();// N+1 위험 개선필요
        for (HashBoard hashBoard : hashBoards) {
            HashTag tag = hashBoard.getTag();
            tagNames.add(tag.getTagName());
        }
        return postMapper.EntityToResponse(post, tagNames);
    }

    private User getUser(String userId) {
        return userRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
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
        } else {
            post.setPublic(dto.getIsPublic()); // 일반 게시글 일 경우 공개/비공개 선택
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

    private void removeTagsToPost(Post post) {
        Set<HashBoard> hashBoardsToRemove = new HashSet<>(post.getHashBoard());

        for (HashBoard board : hashBoardsToRemove) {
            HashTag tag = board.getTag();
            post.removeHash(board); // Post의 컬렉션에서 제거
            if (tag != null) { // tag가 null이 아닐 경우에만 removeHash 호출
                tag.removeHash(board); // HashTag의 컬렉션에서 제거
            }
        }
        post.getHashBoard().clear();
    }

    @Transactional
    public PostResponseDto editPost(UUID postId, PostRequestDto dto, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (CheckStatus.checkSelf(post.getAuthor().getUserId(), userId)) {
            List<String> tagNames = null;
            postMapper.updatePostFromRequest(post, dto);
            removeTagsToPost(post);
            if (dto.getTagNames() != null) {
                tagNames = dto.getTagNames();
                addTagsToPost(post, tagNames);
            }
            eventPublisher.publishEvent(new PostUpdatedEvent(this, post.getId()));
            return postMapper.EntityToResponse(post, tagNames);
        }
        throw new BusinessException(ErrorCode.POST_OWNER_MISMATCH);
    }

    @Transactional
    public void deletePost(UUID postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (CheckStatus.checkSelf(post.getAuthor().getUserId(), userId)) {
            User author = post.getAuthor();
            if (author != null && author.getUserProfile() != null) {
                if (post.getPostType() == PostType.PRODUCT) {
                    author.getUserProfile().setProductCount(Math.max(0, author.getUserProfile().getProductCount() - 1));
                } else {
                    author.getUserProfile().setPostCount(Math.max(0, author.getUserProfile().getPostCount() - 1));
                }
            }
            postRepository.delete(post);
            eventPublisher.publishEvent(new PostDeletedEvent(this, postId));
        } else {
            throw new BusinessException(ErrorCode.POST_OWNER_MISMATCH);
        }
    }

    @Transactional
    public void toggleLike(UUID postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (CheckStatus.checkSelf(post.getAuthor().getUserId(), userId)) {
            throw new BusinessException(ErrorCode.POST_DENY_OWN_POST);
        }
        PostFeedbackList feedback = new PostFeedbackList();
        feedback.setPost(post);
        feedback.setUserId(userId);
        feedback.setType(FeedBackType.LIKE);
        if (post.addFeedBack(feedback, FeedBackType.LIKE)) {
            eventPublisher.publishEvent(new PostLikedEvent(this, postId, userId, true));
            return;
        } else if (post.removeFeedBack(feedback, FeedBackType.LIKE)) {
            eventPublisher.publishEvent(new PostLikedEvent(this, postId, userId, false));
            return;
        } else {
            throw new BusinessException(ErrorCode.POST_DENY_MULTIPLE);
        }
    }

    @Transactional
    public void toggleDislike(UUID postId, String userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (CheckStatus.checkSelf(post.getAuthor().getUserId(), userId)) {
            throw new BusinessException(ErrorCode.POST_DENY_OWN_POST);
        }
        PostFeedbackList feedback = new PostFeedbackList();
        feedback.setPost(post);
        feedback.setUserId(userId);
        feedback.setType(FeedBackType.DISLIKE);
        if (post.addFeedBack(feedback, FeedBackType.DISLIKE)) {
            eventPublisher.publishEvent(new PostDislikedEvent(this, postId, userId, true));
            return;
        } else if (post.removeFeedBack(feedback, FeedBackType.DISLIKE)) {
            eventPublisher.publishEvent(new PostDislikedEvent(this, postId, userId, false));
            return;
        } else {
            throw new BusinessException(ErrorCode.POST_DENY_MULTIPLE);
        }
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<PostListResponseDto> listPost(PostType type, Timestamp lastCreateAt,
            UUID lastId, int limit) {
        // 다음 페이지 확인을 위해 1개 더 조회
        Pageable pageable = PageRequest.of(0, limit + 1, Sort.by(Sort.Direction.DESC, "createAt", "id"));

        List<Post> posts = postRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // PostType 필터
            if (type != null) {
                predicates.add(cb.equal(root.get("postType"), type));
            }

            // 커서 기반 페이지네이션 조건
            if (lastCreateAt != null && lastId != null) {
                Predicate timePredicate = cb.lessThan(root.get("createAt"), lastCreateAt);
                Predicate tieBreaker = cb.and(
                        cb.equal(root.get("createAt"), lastCreateAt),
                        cb.lessThan(root.get("id"), lastId));
                predicates.add(cb.or(timePredicate, tieBreaker));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).getContent();

        boolean hasNext = posts.size() > limit;
        List<Post> pageContent = hasNext ? posts.subList(0, limit) : posts;

        List<PostListResponseDto> postDto = pageContent.stream().map(postMapper::toPostResponse)
                .collect(Collectors.toList());

        Timestamp nextCreateAt = null;
        UUID nextId = null;
        if (hasNext) {
            Post lastPost = pageContent.get(limit - 1);
            nextCreateAt = lastPost.getCreateAt();
            nextId = lastPost.getId();
        }

        // CursorPageResponse에 nextGroupId 필드가 있지만 Post 목록 조회에는 필요 없으므로 null 전달
        return new CursorPageResponse<>(postDto, hasNext, null, nextCreateAt, nextId);
    }
}