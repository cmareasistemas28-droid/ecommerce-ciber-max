package com.cibermax.ecommerce.acceso.repository;

import com.cibermax.ecommerce.acceso.entity.Usuario;
import com.cibermax.ecommerce.acceso.enums.Rol;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    @Query("select (count(u) > 0) from Usuario u join u.roles r where r = :rol")
    boolean existsByRole(@Param("rol") Rol rol);
}
