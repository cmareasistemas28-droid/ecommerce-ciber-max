package com.cibermax.ecommerce.acceso.enums;

import java.util.Set;

public enum Rol {
    ADMINISTRADOR(Set.of(
            Permission.CATEGORIA_READ,
            Permission.CATEGORIA_READ_ID,
            Permission.CATEGORIA_CREATE,
            Permission.CATEGORIA_UPDATE,

            Permission.SUB_CATEGORIA_READ,
            Permission.SUB_CATEGORIA_READ_ID,
            Permission.SUB_CATEGORIA_CREATE,
            Permission.SUB_CATEGORIA_UPDATE,

            Permission.EMPLEADO_READ,
            Permission.EMPLEADO_READ_ID,
            Permission.EMPLEADO_CREATE,
            Permission.EMPLEADO_UPDATE,
            Permission.EMPLEADO_DESACTIVAR,
            Permission.EMPLEADO_ACTIVAR
    )),
    VENDEDOR(Set.of(

    ));
    private final Set<Permission> permissions;

    Rol(Set<Permission> permissions) {
        this.permissions = permissions;
    }
    public Set<Permission> getPermissions(){
        return permissions;
    }
}
