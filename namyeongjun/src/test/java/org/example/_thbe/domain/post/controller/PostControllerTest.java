package org.example._thbe.domain.post.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.time.LocalDate;
import java.util.List;

import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.domain.post.dto.PostCreateResponse;
import org.example._thbe.domain.post.dto.PostListResponse;
import org.example._thbe.domain.post.dto.PostListResponse.PostListDetail;
import org.example._thbe.domain.post.dto.PostResponse;
import org.example._thbe.domain.post.dto.PostUpdateResponse;
import org.example._thbe.domain.post.error.PostErrorCode;
import org.example._thbe.domain.post.service.PostService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.exception.CustomException;
import org.example._thbe.global.exception.GlobalExceptionHandler;
import org.example._thbe.global.restdocs.ApiSpec;
import org.example._thbe.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(PostController.class)
@AutoConfigureRestDocs
@Import(GlobalExceptionHandler.class)
class PostControllerTest extends RestDocsSupport {

    @MockitoBean PostService postService;

    private static final String TAG = "Post";

    private static final ApiSpec GET_POST = ApiSpec
            .of(TAG, "게시글 조회", "사용자는 게시글 상세 내용을 조회할 수 있습니다.")
            .pathParam("postId", "게시글 ID");

    private static final ApiSpec LIST_POSTS = ApiSpec
            .of(TAG, "게시글 목록 조회", "사용자는 게시글 목록을 조회할 수 있습니다.");

    private static final ApiSpec CREATE_POST = ApiSpec
            .of(TAG, "게시글 생성", "사용자는 게시글을 생성할 수 있습니다.")
            .queryParam("memberId", "작성자 ID")
            .request("PostCreateRequest", titleContentFields("제목", "내용"));

    private static final ApiSpec UPDATE_POST = ApiSpec
            .of(TAG, "게시글 수정", "사용자는 자신이 작성한 게시글을 수정할 수 있습니다.")
            .pathParam("postId", "게시글 ID")
            .queryParam("memberId", "요청자 ID")
            .request("PostUpdateRequest", titleContentFields("수정할 제목", "수정할 내용"));

    private static final ApiSpec DELETE_POST = ApiSpec
            .of(TAG, "게시글 삭제", "사용자는 자신이 작성한 게시글을 삭제할 수 있습니다.")
            .pathParam("postId", "게시글 ID")
            .queryParam("memberId", "요청자 ID");



    private static final LocalDate DATE = LocalDate.of(2026, 10, 31);

    private static final String BODY_CREATE = """
            {"title":"첫 게시글","content":"안녕하세요"}
            """;
    private static final String BODY_UPDATE = """
            {"title":"수정된 제목","content":"수정된 내용"}
            """;
    private static final String BODY_TITLE_BLANK = """
            {"title":"","content":"안녕하세요"}
            """;
    private static final String BODY_CONTENT_BLANK = """
            {"title":"첫 게시글","content":""}
            """;
    private static final String BODY_BOTH_BLANK = """
            {"title":"","content":""}
            """;
    private static final String BODY_TITLE_TOO_LONG = """
            {"title":"%s","content":"안녕하세요"}
            """.formatted("가".repeat(256));


    @Test
    void getPostSuccess() throws Exception {
        given(postService.getPost(1L))
                .willReturn(new PostResponse(1L, "첫 게시글", "안녕하세요", "홍길동", DATE));

        expectSuccess(requestGet(1L), SuccessCode.COMMON_OK,
                GET_POST.success("PostResponse", postFields("data")));
    }

    @Test
    void getPostNotFound() throws Exception {
        given(postService.getPost(999L))
                .willThrow(new CustomException(PostErrorCode.POST_NOT_FOUND));

        expectError(requestGet(999L), PostErrorCode.POST_NOT_FOUND, GET_POST);
    }



    @Test
    void getPostListSuccess() throws Exception {
        given(postService.getPostList()).willReturn(new PostListResponse(List.of(
                new PostListDetail(2L, "두 번째 글", "두 번째 내용", "김철수", DATE),
                new PostListDetail(1L, "첫 게시글", "안녕하세요", "홍길동", DATE))));

        expectSuccess(requestList(), SuccessCode.COMMON_OK,
                LIST_POSTS.success("PostListResponse",
                        fieldWithPath("data.posts").type(JsonFieldType.ARRAY).description("게시글 목록"),
                        fieldWithPath("data.posts[].postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                        fieldWithPath("data.posts[].title").type(JsonFieldType.STRING).description("제목"),
                        fieldWithPath("data.posts[].content").type(JsonFieldType.STRING).description("내용"),
                        fieldWithPath("data.posts[].writer").type(JsonFieldType.STRING).description("작성자 닉네임"),
                        fieldWithPath("data.posts[].createdDate").type(JsonFieldType.STRING).description("작성일 (yyyy-MM-dd)")));
    }


    @Test
    void createPostSuccess() throws Exception {
        given(postService.createPost(any(), eq(1L)))
                .willReturn(new PostCreateResponse(1L, "첫 게시글", "안녕하세요", "홍길동", DATE));

        expectSuccess(requestCreate(1L, BODY_CREATE), SuccessCode.COMMON_CREATED,
                CREATE_POST.success("PostCreateResponse", memberPostFields()));
    }

    @Test
    void createPostTitleBlank() throws Exception {
        expectValidationError(requestCreate(1L, BODY_TITLE_BLANK), "title-blank", CREATE_POST);
    }

    @Test
    void createPostContentBlank() throws Exception {
        expectValidationError(requestCreate(1L, BODY_CONTENT_BLANK), "content-blank", CREATE_POST);
    }

    @Test
    void createPostTitleTooLong() throws Exception {
        expectValidationError(requestCreate(1L, BODY_TITLE_TOO_LONG), "title-too-long", CREATE_POST);
    }

    @Test
    void createPostBothBlank() throws Exception {
        expectValidationError(requestCreate(1L, BODY_BOTH_BLANK), "both-blank", CREATE_POST);
    }

    @Test
    void createPostMemberNotFound() throws Exception {
        given(postService.createPost(any(), eq(999L)))
                .willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        expectError(requestCreate(999L, BODY_CREATE), MemberErrorCode.MEMBER_NOT_FOUND, CREATE_POST);
    }



    @Test
    void updatePostSuccess() throws Exception {
        given(postService.updatePost(any(), eq(1L), eq(1L)))
                .willReturn(new PostUpdateResponse(1L, "수정된 제목", "수정된 내용", "홍길동", DATE));

        expectSuccess(requestUpdate(1L, 1L, BODY_UPDATE), SuccessCode.COMMON_OK,
                UPDATE_POST.success("PostUpdateResponse", memberPostFields()));
    }

    @Test
    void updatePostTitleBlank() throws Exception {
        expectValidationError(requestUpdate(1L, 1L, BODY_TITLE_BLANK), "title-blank", UPDATE_POST);
    }

    @Test
    void updatePostContentBlank() throws Exception {
        expectValidationError(requestUpdate(1L, 1L, BODY_CONTENT_BLANK), "content-blank", UPDATE_POST);
    }

    @Test
    void updatePostTitleTooLong() throws Exception {
        expectValidationError(requestUpdate(1L, 1L, BODY_TITLE_TOO_LONG), "title-too-long", UPDATE_POST);
    }

    @Test
    void updatePostBothBlank() throws Exception {
        expectValidationError(requestUpdate(1L, 1L, BODY_BOTH_BLANK), "both-blank", UPDATE_POST);
    }

    @Test
    void updatePostMemberNotFound() throws Exception {
        given(postService.updatePost(any(), eq(1L), eq(999L)))
                .willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        expectError(requestUpdate(1L, 999L, BODY_UPDATE), MemberErrorCode.MEMBER_NOT_FOUND, UPDATE_POST);
    }

    @Test
    void updatePostNotFound() throws Exception {
        given(postService.updatePost(any(), eq(999L), eq(1L)))
                .willThrow(new CustomException(PostErrorCode.POST_NOT_FOUND));

        expectError(requestUpdate(999L, 1L, BODY_UPDATE), PostErrorCode.POST_NOT_FOUND, UPDATE_POST);
    }

    @Test
    void updatePostNotMyPost() throws Exception {
        given(postService.updatePost(any(), eq(1L), eq(2L)))
                .willThrow(new CustomException(PostErrorCode.NOT_MY_POST));

        expectError(requestUpdate(1L, 2L, BODY_UPDATE), PostErrorCode.NOT_MY_POST, UPDATE_POST);
    }



    @Test
    void deletePostSuccess() throws Exception {
        expectSuccess(requestDelete(1L, 1L), SuccessCode.COMMON_OK, DELETE_POST.success());
    }

    @Test
    void deletePostMemberNotFound() throws Exception {
        willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND))
                .given(postService).deletePost(1L, 999L);

        expectError(requestDelete(1L, 999L), MemberErrorCode.MEMBER_NOT_FOUND, DELETE_POST);
    }

    @Test
    void deletePostNotFound() throws Exception {
        willThrow(new CustomException(PostErrorCode.POST_NOT_FOUND))
                .given(postService).deletePost(999L, 1L);

        expectError(requestDelete(999L, 1L), PostErrorCode.POST_NOT_FOUND, DELETE_POST);
    }

    @Test
    void deletePostNotMyPost() throws Exception {
        willThrow(new CustomException(PostErrorCode.NOT_MY_POST))
                .given(postService).deletePost(1L, 2L);

        expectError(requestDelete(1L, 2L), PostErrorCode.NOT_MY_POST, DELETE_POST);
    }


    private ResultActions requestGet(long postId) throws Exception {
        return mockMvc.perform(get("/posts/{postId}", postId));
    }

    private ResultActions requestList() throws Exception {
        return mockMvc.perform(get("/posts"));
    }

    private ResultActions requestCreate(long memberId, String body) throws Exception {
        return mockMvc.perform(post("/posts")
                .queryParam("memberId", String.valueOf(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions requestUpdate(long postId, long memberId, String body) throws Exception {
        return mockMvc.perform(patch("/posts/{postId}", postId)
                .queryParam("memberId", String.valueOf(memberId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions requestDelete(long postId, long memberId) throws Exception {
        return mockMvc.perform(delete("/posts/{postId}", postId)
                .queryParam("memberId", String.valueOf(memberId)));
    }



    private static FieldDescriptor[] titleContentFields(String titleDesc, String contentDesc) {
        return new FieldDescriptor[]{
                fieldWithPath("title").type(JsonFieldType.STRING).description(titleDesc + " (필수, 공백 불가, 최대 255자)"),
                fieldWithPath("content").type(JsonFieldType.STRING).description(contentDesc + " (필수, 공백 불가)")
        };
    }

    private static FieldDescriptor[] postFields(String prefix) {
        return new FieldDescriptor[]{
                fieldWithPath(prefix + ".postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                fieldWithPath(prefix + ".title").type(JsonFieldType.STRING).description("제목"),
                fieldWithPath(prefix + ".content").type(JsonFieldType.STRING).description("내용"),
                fieldWithPath(prefix + ".writer").type(JsonFieldType.STRING).description("작성자 닉네임"),
                fieldWithPath(prefix + ".createdDate").type(JsonFieldType.STRING).description("작성일 (yyyy-MM-dd)")
        };
    }

    private static FieldDescriptor[] memberPostFields() {
        return new FieldDescriptor[]{
                fieldWithPath("data.postId").type(JsonFieldType.NUMBER).description("게시글 ID"),
                fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
                fieldWithPath("data.content").type(JsonFieldType.STRING).description("내용"),
                fieldWithPath("data.member").type(JsonFieldType.STRING).description("작성자 닉네임"),
                fieldWithPath("data.createdDate").type(JsonFieldType.STRING).description("작성일 (yyyy-MM-dd)")
        };
    }
}