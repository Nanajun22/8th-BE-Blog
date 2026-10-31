package org.example._thbe.global.restdocs;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import java.lang.reflect.Method;
import org.example._thbe.global.code.BaseCode;
import org.example._thbe.global.code.ErrorCode;
import org.example._thbe.global.code.SuccessCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;





public abstract class RestDocsSupport {

    @Autowired
    protected MockMvc mockMvc;

    private String testName;

    @BeforeEach
    void captureTestName(TestInfo testInfo) {
        this.testName = testInfo.getTestMethod().map(Method::getName).orElse("unknown");
    }

    protected ResultActions expectSuccess(ResultActions result, SuccessCode code, Snippet snippet) throws Exception {
        return expectAndDocument(result, code, code.getCode(), snippet);
    }

    protected ResultActions expectError(ResultActions result, BaseCode code, ApiSpec spec) throws Exception {
        return expectAndDocument(result, code, code.getCode(), spec.error());
    }

    protected ResultActions expectValidationError(ResultActions result, String caseName, ApiSpec spec) throws Exception {
        ErrorCode code = ErrorCode.VALIDATION_ERROR;
        return expectAndDocument(result, code, code.getCode() + "-" + caseName, spec.validationError());
    }

    private ResultActions expectAndDocument(ResultActions result, BaseCode code, String exampleName, Snippet snippet) throws Exception {
        return result
                .andExpect(status().is(code.getStatus().value()))
                .andDo(MockMvcRestDocumentationWrapper.document(exampleName + "__" + testName, snippet));
    }
}