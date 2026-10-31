package com.cibermax.ecommerce.bootstrap;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_bootstrap_control")
@Getter
@Setter
@NoArgsConstructor
public class AdminBootstrapControl {

    @Id
    private Short id;

    @Column(nullable = false)
    private boolean initialized;

    private LocalDateTime initializedAt;
}
