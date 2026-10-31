package org.example._thbe.domain.post.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example._thbe.domain.post.dto.*;
import org.example._thbe.domain.post.service.PostService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController  {
    private final PostService postService;

    // 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        return ApiResponse.success(SuccessCode.COMMON_OK,postService.getPost(postId));
    }

    // 목록 조회
    @GetMapping()
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList() {
        return ApiResponse.success(SuccessCode.COMMON_OK,postService.getPostList());
    }

    // 생성
    @PostMapping()
    public ResponseEntity<ApiResponse<PostCreateResponse>> createPost(@RequestBody @Valid PostCreateRequest request,
                                                                      Long memberId
    ) {
        return ApiResponse.success(SuccessCode.COMMON_CREATED,postService.createPost(request,memberId));
    }

    // 수정
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostUpdateResponse>> updatePost(@RequestBody @Valid PostUpdateRequest request,
                                                                      @PathVariable Long postId,
                                                                      Long memberId
    ) {
        return ApiResponse.success(SuccessCode.COMMON_OK,postService.updatePost(request,postId,memberId));
    }

    // 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId,Long memberId) {
        postService.deletePost(postId,memberId);
        return ApiResponse.success(SuccessCode.COMMON_OK);
    }
}
