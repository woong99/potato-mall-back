package potatowoong.potatomallback.domain.ranking.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getNoAuthDocumentRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.ranking.dto.SearchSnapshotResDto.SearchRankResDto;
import potatowoong.potatomallback.domain.ranking.dto.SearchSnapshotResDto.SearchRankingSnapshotResDto;
import potatowoong.potatomallback.domain.ranking.enums.RankState;
import potatowoong.potatomallback.domain.ranking.service.RankingService;

@WebMvcTest(controllers = RankingController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class RankingControllerTest {

    @MockBean
    private RankingService rankingService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("실시간 검색어 Top10 조회")
    class 실시간_검색어_Top10_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            SearchRankingSnapshotResDto searchRankingSnapshotResDto = SearchRankingSnapshotResDto.builder()
                .searchTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")))
                .searchRankResDtos(Collections.singletonList(SearchRankResDto.builder()
                    .keyword("감자")
                    .rank(1)
                    .rankState(RankState.UP)
                    .build()))
                .build();

            given(rankingService.getRecentTop10SearchKeyword(any())).willReturn(searchRankingSnapshotResDto);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/ranking/recent-top-10-search-keyword")
                .with(csrf().asHeader())
                .contentType("application/json"));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("data.searchTime").exists())
                .andExpect(jsonPath("data.searchRankResDtos").isArray());

            actions
                .andDo(document("recent-top-10-search-keyword",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("searchTime").description("검색 시간"),
                        fieldWithPath("searchRankResDtos").description("검색 순위 리스트"),
                        fieldWithPath("searchRankResDtos[].keyword").description("검색어"),
                        fieldWithPath("searchRankResDtos[].rank").description("순위"),
                        fieldWithPath("searchRankResDtos[].rankState").description("순위 변동 상태(UP: 순위 상승, DOWN: 순위 하락, NEW: 신규, SAME: 순위 변동 없음)")
                    )
                ));

            then(rankingService).should().getRecentTop10SearchKeyword(any());
        }
    }
}