package com.cibermax.ecommerce.empleado.mapper;

import com.cibermax.ecommerce.empleado.dto.Response.EmpleadoResponse;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoCreateRequest;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoUpdateRequest;
import com.cibermax.ecommerce.empleado.entity.Empleado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.ERROR)
public interface EmpleadoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Empleado toEmpleado(EmpleadoCreateRequest request);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "roles", source = "usuario.roles")
    @Mapping(target = "edad", source = "edad")
    EmpleadoResponse toEmpleadoResponse(Empleado empleado);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "foto", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void toUpdate(
            EmpleadoUpdateRequest request,
            @MappingTarget Empleado empleado
    );
}
