package com.cibermax.ecommerce.bootstrap;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AdminBootstrapControlRepository
        extends JpaRepository<AdminBootstrapControl, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from AdminBootstrapControl c where c.id = :id")
    Optional<AdminBootstrapControl> findByIdForUpdate(Short id);
}
