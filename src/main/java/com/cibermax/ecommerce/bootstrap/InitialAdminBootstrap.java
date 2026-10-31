package com.cibermax.ecommerce.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order
public class InitialAdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(
            InitialAdminBootstrap.class
    );

    private final InitialAdminProperties properties;
    private final InitialAdminService service;

    public InitialAdminBootstrap(
            InitialAdminProperties properties,
            InitialAdminService service
    ) {
        this.properties = properties;
        this.service = service;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        InitialAdminService.BootstrapResult result = service.initialize();
        if (result == InitialAdminService.BootstrapResult.CREATED) {
            log.info("Bootstrap del administrador inicial completado");
        } else {
            log.info("Bootstrap del administrador inicial ya estaba completado");
        }
    }
}
