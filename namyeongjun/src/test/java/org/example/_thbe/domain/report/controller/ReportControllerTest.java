package org.example._thbe.domain.report.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Arrays;

import org.example._thbe.domain.member.error.MemberErrorCode;
import org.example._thbe.domain.post.error.PostErrorCode;
import org.example._thbe.domain.report.dto.ReportCreateResponse;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.error.ReportErrorCode;
import org.example._thbe.domain.report.service.ReportService;
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
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ReportController.class)
@AutoConfigureRestDocs
@Import(GlobalExceptionHandler.class)
class ReportControllerTest extends RestDocsSupport {

    @MockitoBean ReportService reportService;



    private static final String TAG = "Report";
    private static final String CONTENT_TYPES = Arrays.toString(ReportContentType.values());

    private static final ApiSpec CREATE_REPORT = ApiSpec
            .of(TAG, "신고 접수",
                    "사용자는 컨텐츠를 신고할 수 있습니다. 이미 신고한 대상이나 자신의 컨텐츠는 신고할 수 없으며, "
                            + "동일 컨텐츠의 신고가 일정 횟수 이상 쌓이면 자동으로 승인됩니다.")
            .queryParam("userId", "신고자 ID")
            .request("ReportCreateRequest",
                    fieldWithPath("reason").type(JsonFieldType.STRING).description("신고 사유 (필수, 공백 불가)"),
                    fieldWithPath("contentType").type(JsonFieldType.STRING).description("신고 대상 타입 (필수). " + CONTENT_TYPES),
                    fieldWithPath("contentId").type(JsonFieldType.NUMBER).description("신고 대상 ID (필수)"));

    private static final ApiSpec DELETE_REPORT = ApiSpec
            .of(TAG, "신고 취소",
                    "사용자는 자신이 접수한 신고를 취소할 수 있습니다. 이미 승인 또는 반려된 신고는 취소할 수 없습니다.")
            .pathParam("reportId", "신고 ID")
            .queryParam("userId", "신고자 ID");



    private static final String BODY_CREATE = """
            {"reason":"욕설 및 비방","contentType":"POST","contentId":10}
            """;
    private static final String BODY_CREATE_COMMENT = """
            {"reason":"욕설 및 비방","contentType":"COMMENT","contentId":10}
            """;
    private static final String BODY_REASON_BLANK = """
            {"reason":"","contentType":"POST","contentId":10}
            """;
    private static final String BODY_CONTENT_TYPE_NULL = """
            {"reason":"욕설 및 비방","contentId":10}
            """;
    private static final String BODY_CONTENT_ID_NULL = """
            {"reason":"욕설 및 비방","contentType":"POST"}
            """;
    private static final String BODY_EMPTY = """
            {}
            """;



    @Test
    void createReportSuccess() throws Exception {
        given(reportService.createReport(any(), eq(1L)))
                .willReturn(new ReportCreateResponse("욕설 및 비방", ReportContentType.POST, 1L));

        expectSuccess(requestCreate(1L, BODY_CREATE), SuccessCode.COMMON_CREATED,
                CREATE_REPORT.success("ReportCreateResponse",
                        fieldWithPath("data.reason").type(JsonFieldType.STRING).description("신고 사유"),
                        fieldWithPath("data.contentType").type(JsonFieldType.STRING).description("신고 대상 타입. " + CONTENT_TYPES),
                        fieldWithPath("data.contentId").type(JsonFieldType.NUMBER).description("신고 대상 ID")));
    }

    @Test
    void createReportReasonBlank() throws Exception {
        expectValidationError(requestCreate(1L, BODY_REASON_BLANK), "reason-blank", CREATE_REPORT);
    }

    @Test
    void createReportContentTypeNull() throws Exception {
        expectValidationError(requestCreate(1L, BODY_CONTENT_TYPE_NULL), "content-type-null", CREATE_REPORT);
    }

    @Test
    void createReportContentIdNull() throws Exception {
        expectValidationError(requestCreate(1L, BODY_CONTENT_ID_NULL), "content-id-null", CREATE_REPORT);
    }

    @Test
    void createReportAllMissing() throws Exception {
        expectValidationError(requestCreate(1L, BODY_EMPTY), "all-missing", CREATE_REPORT);
    }

    @Test
    void createReportAlreadyExist() throws Exception {
        given(reportService.createReport(any(), eq(1L)))
                .willThrow(new CustomException(ReportErrorCode.REPORT_ALREADY_EXIST));

        expectError(requestCreate(1L, BODY_CREATE), ReportErrorCode.REPORT_ALREADY_EXIST, CREATE_REPORT);
    }

    @Test
    void createReportSelfReport() throws Exception {
        given(reportService.createReport(any(), eq(1L)))
                .willThrow(new CustomException(ReportErrorCode.SELF_REPORT));

        expectError(requestCreate(1L, BODY_CREATE), ReportErrorCode.SELF_REPORT, CREATE_REPORT);
    }

    @Test
    void createReportUnsupportedContentType() throws Exception {
        given(reportService.createReport(any(), eq(1L)))
                .willThrow(new CustomException(ReportErrorCode.UNSUPPORTED_CONTENT_TYPE));

        expectError(requestCreate(1L, BODY_CREATE_COMMENT), ReportErrorCode.UNSUPPORTED_CONTENT_TYPE, CREATE_REPORT);
    }

    @Test
    void createReportContentNotFound() throws Exception {
        given(reportService.createReport(any(), eq(1L)))
                .willThrow(new CustomException(PostErrorCode.POST_NOT_FOUND));

        expectError(requestCreate(1L, BODY_CREATE), PostErrorCode.POST_NOT_FOUND, CREATE_REPORT);
    }

    @Test
    void createReportMemberNotFound() throws Exception {
        given(reportService.createReport(any(), eq(999L)))
                .willThrow(new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        expectError(requestCreate(999L, BODY_CREATE), MemberErrorCode.MEMBER_NOT_FOUND, CREATE_REPORT);
    }



    @Test
    void deleteReportSuccess() throws Exception {
        expectSuccess(requestDelete(1L, 1L), SuccessCode.COMMON_OK, DELETE_REPORT.success());
    }

    @Test
    void deleteReportNotFound() throws Exception {
        willThrow(new CustomException(ReportErrorCode.REPORT_NOT_FOUND))
                .given(reportService).deleteReport(999L, 1L);

        expectError(requestDelete(999L, 1L), ReportErrorCode.REPORT_NOT_FOUND, DELETE_REPORT);
    }

    @Test
    void deleteReportNoPermission() throws Exception {
        willThrow(new CustomException(ReportErrorCode.REPORT_DELETE_NO_PERMISSION))
                .given(reportService).deleteReport(1L, 2L);

        expectError(requestDelete(1L, 2L), ReportErrorCode.REPORT_DELETE_NO_PERMISSION, DELETE_REPORT);
    }

    @Test
    void deleteReportAlreadyHandled() throws Exception {
        willThrow(new CustomException(ReportErrorCode.REPORT_ALREADY_HANDLED))
                .given(reportService).deleteReport(1L, 1L);

        expectError(requestDelete(1L, 1L), ReportErrorCode.REPORT_ALREADY_HANDLED, DELETE_REPORT);
    }



    private ResultActions requestCreate(long userId, String body) throws Exception {
        return mockMvc.perform(post("/reports")
                .queryParam("userId", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private ResultActions requestDelete(long reportId, long userId) throws Exception {
        return mockMvc.perform(delete("/reports/{reportId}", reportId)
                .queryParam("userId", String.valueOf(userId)));
    }
}