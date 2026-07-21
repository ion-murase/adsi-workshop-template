package com.example.attendance.organization.repository;

import com.example.attendance.organization.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {
}
