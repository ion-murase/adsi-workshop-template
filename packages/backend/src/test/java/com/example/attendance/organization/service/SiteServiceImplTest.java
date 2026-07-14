package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.SiteRequest;
import com.example.attendance.organization.entity.Site;
import com.example.attendance.organization.repository.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteServiceImplTest {

    @Mock
    private SiteRepository siteRepository;

    private SiteServiceImpl siteService;

    private static final UUID SITE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        siteService = new SiteServiceImpl(siteRepository);
    }

    @Test
    @DisplayName("全拠点取得: 全件がレスポンスに変換される")
    void getAllSites_returnsMappedResponses() {
        var site = Site.builder().name("東京オフィス").timezone("Asia/Tokyo").build();
        when(siteRepository.findAll()).thenReturn(List.of(site));

        var result = siteService.getAllSites();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("東京オフィス");
        assertThat(result.get(0).timezone()).isEqualTo("Asia/Tokyo");
    }

    @Test
    @DisplayName("拠点作成: リクエストから拠点が保存される")
    void createSite_savesAndReturnsResponse() {
        var request = new SiteRequest("大阪オフィス", "Asia/Tokyo");
        var saved = Site.builder().name("大阪オフィス").timezone("Asia/Tokyo").build();
        when(siteRepository.save(any(Site.class))).thenReturn(saved);

        var result = siteService.createSite(request);

        assertThat(result.name()).isEqualTo("大阪オフィス");
        verify(siteRepository).save(any(Site.class));
    }

    @Test
    @DisplayName("拠点更新: 存在するIDで更新するとレスポンスが返される")
    void updateSite_existingId_updatesAndReturns() {
        var site = Site.builder().name("旧拠点").timezone("Asia/Tokyo").build();
        when(siteRepository.findById(SITE_ID)).thenReturn(Optional.of(site));
        when(siteRepository.save(any(Site.class))).thenReturn(site);

        var request = new SiteRequest("新拠点", "America/New_York");
        var result = siteService.updateSite(SITE_ID, request);

        assertThat(result.name()).isEqualTo("新拠点");
        assertThat(result.timezone()).isEqualTo("America/New_York");
    }

    @Test
    @DisplayName("拠点更新: 存在しないIDで例外が投げられる")
    void updateSite_nonExistingId_throwsException() {
        when(siteRepository.findById(SITE_ID)).thenReturn(Optional.empty());

        var request = new SiteRequest("新拠点", "Asia/Tokyo");

        assertThatThrownBy(() -> siteService.updateSite(SITE_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("拠点が見つかりません");
    }
}
