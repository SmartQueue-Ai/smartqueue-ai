package com.smartqueue.inventory_service.repository;

import com.smartqueue.inventory_service.entity.AuditAction;
import com.smartqueue.inventory_service.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, UUID> {

    List<InventoryAudit> findByResourceId(UUID resourceId);

    List<InventoryAudit> findByAction(AuditAction action);
}
