package com.cibermax.ecommerce.acceso.entity;

import com.cibermax.ecommerce.acceso.enums.AuthProvider;
import com.cibermax.ecommerce.acceso.enums.Permission;
import com.cibermax.ecommerce.acceso.enums.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
    @Column(unique = true)
    private String providerId;
    private boolean active = true;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(
                    name = "usuario_id"
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Set<Rol> roles = new HashSet<>();
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    public void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       Set<SimpleGrantedAuthority> authorities = new HashSet<>();
       for(Rol rol : roles){
           authorities.add(new SimpleGrantedAuthority("ROLE_"+rol.name()));
           for(Permission permission : rol.getPermissions()){
               authorities.add(new SimpleGrantedAuthority(permission.name()));
           }
       }
       return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
