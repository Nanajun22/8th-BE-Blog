package org.example._thbe.global.restdocs;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static com.epages.restdocs.apispec.Schema.schema;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;

import com.epages.restdocs.apispec.ParameterDescriptorWithType;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Snippet;


public final class ApiSpec {

    private static final String BASIC_SCHEMA = "ApiResponse";
    private static final String ERROR_SCHEMA = "CustomErrorResponse";
    private static final String VALIDATION_SCHEMA = "ValidationErrorResponse";

    private static final FieldDescriptor[] COMMON = {
            fieldWithPath("isSuccess").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("code").type(JsonFieldType.STRING).description("응답 코드 (예: POST_404_1)"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
    };

    private final String tag;
    private final String summary;
    private final String description;
    private final List<ParameterDescriptorWithType> pathParams = new ArrayList<>();
    private final List<ParameterDescriptorWithType> queryParams = new ArrayList<>();
    private String requestSchema;
    private FieldDescriptor[] requestFields = new FieldDescriptor[0];

    private ApiSpec(String tag, String summary, String description) {
        this.tag = tag;
        this.summary = summary;
        this.description = description;
    }

    public static ApiSpec of(String tag, String summary, String description) {
        return new ApiSpec(tag, summary, description);
    }

    public ApiSpec pathParam(String name, String description) {
        pathParams.add(parameterWithName(name).description(description));
        return this;
    }

    public ApiSpec queryParam(String name, String description) {
        queryParams.add(parameterWithName(name).description(description));
        return this;
    }

    public ApiSpec request(String schemaName, FieldDescriptor... fields) {
        this.requestSchema = schemaName;
        this.requestFields = fields;
        return this;
    }




    public Snippet success(String responseSchema, FieldDescriptor... dataFields) {
        return build(responseSchema, withCommon(dataFields), requestFields);
    }


    public Snippet success() {
        return build(BASIC_SCHEMA, withCommon(dataWithEmpty()), requestFields);
    }


    public Snippet error() {
        return build(ERROR_SCHEMA, withCommon(dataWithEmpty()), requestFields);
    }


    public Snippet validationError() {
        return build(VALIDATION_SCHEMA, withCommon(
                        subsectionWithPath("data").type(JsonFieldType.OBJECT)
                                .description("field: message 형식의 검증 결과를 반환합니다.")),
                asOptional(requestFields));
    }



    private Snippet build(String responseSchema, FieldDescriptor[] responseFields, FieldDescriptor[] requestFieldsToUse) {
        var builder = ResourceSnippetParameters.builder()
                .tag(tag)
                .summary(summary)
                .description(description)
                .responseSchema(schema(responseSchema))
                .responseFields(responseFields);

        if (!pathParams.isEmpty()) {
            builder.pathParameters(pathParams.toArray(new ParameterDescriptorWithType[0]));
        }
        if (!queryParams.isEmpty()) {
            builder.queryParameters(queryParams.toArray(new ParameterDescriptorWithType[0]));
        }
        if (requestSchema != null) {
            builder.requestSchema(schema(requestSchema)).requestFields(requestFieldsToUse);
        }
        return resource(builder.build());
    }

    private static FieldDescriptor dataWithEmpty() {
        return fieldWithPath("data").type(JsonFieldType.VARIES).optional().description("응답 본문");
    }

    private static FieldDescriptor[] withCommon(FieldDescriptor... extra) {
        return Stream.concat(Arrays.stream(COMMON), Arrays.stream(extra)).toArray(FieldDescriptor[]::new);
    }


    private static FieldDescriptor[] asOptional(FieldDescriptor[] fields) {
        return Arrays.stream(fields)
                .map(f -> fieldWithPath(f.getPath())
                        .type(f.getType())
                        .description(f.getDescription())
                        .optional())
                .toArray(FieldDescriptor[]::new);
    }
}