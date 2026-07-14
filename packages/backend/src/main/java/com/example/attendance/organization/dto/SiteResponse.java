package com.example.attendance.organization.dto;

import com.example.attendance.organization.entity.Site;

import java.util.UUID;

public record SiteResponse(
        UUID id,
        String name,
        String timezone
) {
    public static SiteResponse from(Site site) {
        return new SiteResponse(site.getId(), site.getName(), site.getTimezone());
    }
}
