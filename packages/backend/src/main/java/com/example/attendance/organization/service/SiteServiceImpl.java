package com.example.attendance.organization.service;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.organization.dto.SiteRequest;
import com.example.attendance.organization.dto.SiteResponse;
import com.example.attendance.organization.entity.Site;
import com.example.attendance.organization.repository.SiteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    public SiteServiceImpl(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteResponse> getAllSites() {
        return siteRepository.findAll()
                .stream()
                .map(SiteResponse::from)
                .toList();
    }

    @Override
    public SiteResponse createSite(SiteRequest request) {
        var site = Site.builder()
                .name(request.name())
                .timezone(request.timezone())
                .build();

        var saved = siteRepository.save(site);
        return SiteResponse.from(saved);
    }

    @Override
    public SiteResponse updateSite(UUID id, SiteRequest request) {
        var site = siteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("拠点が見つかりません", HttpStatus.NOT_FOUND));

        site.setName(request.name());
        site.setTimezone(request.timezone());

        var saved = siteRepository.save(site);
        return SiteResponse.from(saved);
    }
}
