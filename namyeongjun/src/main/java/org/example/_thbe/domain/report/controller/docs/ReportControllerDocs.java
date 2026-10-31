package org.example._thbe.domain.report.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example._thbe.domain.report.dto.ReportCreateRequest;
import org.example._thbe.domain.report.dto.ReportCreateResponse;
import org.example._thbe.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Report", description = "신고 API")
public interface ReportControllerDocs {

    @Operation(summary = "게시글 신고 접수", description = "사용자는 게시글 , 댓글 등의 컨텐츠를 신고할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",description = "신고 접수 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "존재하는",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = io.swagger.v3.oas.annotations.responses.ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "존재하는"
                                    )

                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",description = "자기 자신 신고")
    })
    ResponseEntity<ApiResponse<ReportCreateResponse>> createReport(
            ReportCreateRequest request,
            @Parameter(name = "신고자 ID",example = "1", required = true) Long userId
    );

    @Operation(summary = "게시글 접수",description = "사용자는 자신이 접수한 신고를 취소할 수 있습니다.")
    @ApiResponses({

    })
    ResponseEntity<ApiResponse<Void>> deleteReport(
            @Parameter(name = "신고 접수 ID",example = "1", required = true) Long reportId,
            @Parameter(name = "신고자 ID",example = "1", required = true) Long userId
    );
}
