package com.example.attendance.config;

import com.example.attendance.common.enums.Role;
import com.example.attendance.organization.entity.Department;
import com.example.attendance.organization.entity.Site;
import com.example.attendance.organization.entity.User;
import com.example.attendance.organization.repository.DepartmentRepository;
import com.example.attendance.organization.repository.SiteRepository;
import com.example.attendance.organization.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("workshop")
public class WorkshopDataInitializer implements ApplicationRunner {

    private final SiteRepository siteRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkshopDataInitializer(SiteRepository siteRepository,
                                   DepartmentRepository departmentRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        this.siteRepository = siteRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        var siteId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var deptId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        var site = Site.builder().name("本社").timezone("Asia/Tokyo").build();
        try {
            var f = site.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(site, siteId);
        } catch (Exception ignored) {}
        siteRepository.save(site);

        var dept = Department.builder().name("開発部").siteId(siteId).build();
        try {
            var f = dept.getClass().getSuperclass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(dept, deptId);
        } catch (Exception ignored) {}
        departmentRepository.save(dept);

        var admin = User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("管理者")
                .role(Role.ADMIN)
                .primaryDepartmentId(deptId)
                .requirePasswordChange(false)
                .active(true)
                .hasAttendanceRecord(false)
                .failedLoginAttempts(0)
                .build();
        userRepository.save(admin);

        var general = User.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("一般ユーザ")
                .role(Role.GENERAL)
                .primaryDepartmentId(deptId)
                .requirePasswordChange(false)
                .active(true)
                .hasAttendanceRecord(false)
                .failedLoginAttempts(0)
                .build();
        userRepository.save(general);
    }
}
