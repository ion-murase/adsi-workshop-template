package com.example.attendance.organization.service;

import com.example.attendance.organization.dto.SiteRequest;
import com.example.attendance.organization.dto.SiteResponse;

import java.util.List;
import java.util.UUID;

public interface SiteService {

    List<SiteResponse> getAllSites();

    SiteResponse createSite(SiteRequest request);

    SiteResponse updateSite(UUID id, SiteRequest request);
}
