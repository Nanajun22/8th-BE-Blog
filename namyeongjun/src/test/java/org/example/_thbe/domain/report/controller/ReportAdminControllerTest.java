package org.example._thbe.domain.report.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Arrays;
import java.util.List;

import org.example._thbe.domain.post.error.PostErrorCode;
import org.example._thbe.domain.report.dto.ReportListResponse;
import org.example._thbe.domain.report.dto.ReportListResponse.ReportListDetail;
import org.example._thbe.domain.report.enums.ReportContentType;
import org.example._thbe.domain.report.enums.ReportStatus;
import org.example._thbe.domain.report.error.ReportErrorCode;
import org.example._thbe.domain.report.service.ReportAdminService;
import org.example._thbe.global.code.SuccessCode;
import org.example._thbe.global.exception.CustomException;
import org.example._thbe.global.exception.GlobalExceptionHandler;
import org.example._thbe.global.restdocs.ApiSpec;
import org.example._thbe.global.restdocs.RestDocsSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ReportAdminController.class)
@AutoConfigureRestDocs
@Import(GlobalExceptionHandler.class)
class ReportAdminControllerTest extends RestDocsSupport {

    @MockitoBean ReportAdminService reportAdminService;



    private static final String TAG = "Report Admin";
    private static final String CONTENT_TYPES = Arrays.toString(ReportContentType.values());
    private static final String STATUSES = Arrays.toString(ReportStatus.values());

    private static final ApiSpec GET_REPORT_LIST = ApiSpec
            .of(TAG, "신고 목록 조회", "관리자는 신고 상태별로 신고 목록을 조회할 수 있습니다.")
            .queryParam("status", "조회할 신고 상태 (필수). " + STATUSES);

    private static final ApiSpec APPROVE_REPORT = ApiSpec
            .of(TAG, "신고 승인",
                    "관리자는 신고를 승인할 수 있습니다. 승인하면 신고 대상 컨텐츠가 삭제되고, "
                            + "같은 컨텐츠에 접수된 다른 신고도 함께 승인 처리됩니다.")
            .pathParam("reportId", "신고 ID");

    private static final ApiSpec REJECT_REPORT = ApiSpec
            .of(TAG, "신고 반려", "관리자는 신고를 반려할 수 있습니다.")
            .pathParam("reportId", "신고 ID");



    @Test
    void getReportListSuccess() throws Exception {
        given(reportAdminService.getReportList(ReportStatus.PENDING))
                .willReturn(new ReportListResponse(List.of(
                        new ReportListDetail(1L, ReportContentType.POST, 10L, ReportStatus.PENDING, "홍길동"),
                        new ReportListDetail(2L, ReportContentType.COMMENT, 5L, ReportStatus.PENDING, "김철수"))));

        expectSuccess(requestList(ReportStatus.PENDING), SuccessCode.COMMON_OK,
                GET_REPORT_LIST.success("ReportListResponse",
                        fieldWithPath("data.reports").type(JsonFieldType.ARRAY).description("신고 목록"),
                        fieldWithPath("data.reports[].reportId").type(JsonFieldType.NUMBER).description("신고 ID"),
                        fieldWithPath("data.reports[].contentType").type(JsonFieldType.STRING).description("신고 대상 타입. " + CONTENT_TYPES),
                        fieldWithPath("data.reports[].contentId").type(JsonFieldType.NUMBER).description("신고 대상 ID"),
                        fieldWithPath("data.reports[].status").type(JsonFieldType.STRING).description("신고 상태. " + STATUSES),
                        fieldWithPath("data.reports[].reporterName").type(JsonFieldType.STRING).description("신고자 닉네임")));
    }



    @Test
    void approveReportSuccess() throws Exception {
        expectSuccess(requestApprove(1L), SuccessCode.COMMON_OK, APPROVE_REPORT.success());
    }

    @Test
    void approveReportNotFound() throws Exception {
        willThrow(new CustomException(ReportErrorCode.REPORT_NOT_FOUND))
                .given(reportAdminService).approveReport(999L);

        expectError(requestApprove(999L), ReportErrorCode.REPORT_NOT_FOUND, APPROVE_REPORT);
    }

    @Test
    void approveReportUnsupportedContentType() throws Exception {
        willThrow(new CustomException(ReportErrorCode.UNSUPPORTED_CONTENT_TYPE))
                .given(reportAdminService).approveReport(2L);

        expectError(requestApprove(2L), ReportErrorCode.UNSUPPORTED_CONTENT_TYPE, APPROVE_REPORT);
    }

    @Test
    void approveReportContentNotFound() throws Exception {
        willThrow(new CustomException(PostErrorCode.POST_NOT_FOUND))
                .given(reportAdminService).approveReport(3L);

        expectError(requestApprove(3L), PostErrorCode.POST_NOT_FOUND, APPROVE_REPORT);
    }



    @Test
    void rejectReportSuccess() throws Exception {
        expectSuccess(requestReject(1L), SuccessCode.COMMON_OK, REJECT_REPORT.success());
    }

    @Test
    void rejectReportNotFound() throws Exception {
        willThrow(new CustomException(ReportErrorCode.REPORT_NOT_FOUND))
                .given(reportAdminService).rejectReport(999L);

        expectError(requestReject(999L), ReportErrorCode.REPORT_NOT_FOUND, REJECT_REPORT);
    }



    private ResultActions requestList(ReportStatus status) throws Exception {
        return mockMvc.perform(get("/api/admin/reports").queryParam("status", status.name()));
    }

    private ResultActions requestApprove(long reportId) throws Exception {
        return mockMvc.perform(post("/api/admin/reports/{reportId}/approve", reportId));
    }

    private ResultActions requestReject(long reportId) throws Exception {
        return mockMvc.perform(post("/api/admin/reports/{reportId}/reject", reportId));
    }
}