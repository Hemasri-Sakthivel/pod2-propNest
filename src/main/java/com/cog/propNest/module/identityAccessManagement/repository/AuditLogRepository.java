package com.cog.propNest.module.identityAccessManagement.repository;

import com.cog.propNest.module.identityAccessManagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimeStampDesc(Long userId);

    List<AuditLog> findAllByOrderByTimeStampDesc();
}
