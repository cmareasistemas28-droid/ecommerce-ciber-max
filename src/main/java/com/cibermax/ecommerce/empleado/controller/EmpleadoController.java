package com.cibermax.ecommerce.empleado.controller;

import com.cibermax.ecommerce.acceso.enums.Rol;
import com.cibermax.ecommerce.empleado.dto.Response.EmpleadoResponse;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoCreateRequest;
import com.cibermax.ecommerce.empleado.dto.request.EmpleadoUpdateRequest;
import com.cibermax.ecommerce.empleado.service.EmpleadoService;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.UUID;


@Controller
@RequestMapping("/admin/empleados")

public class EmpleadoController {
    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }
    @ModelAttribute("rolesDisponibles")
    public Rol[] rolesDisponibles() {
        return Rol.values();
    }

    @GetMapping
    public String listar(
            @PageableDefault(size = 10, sort = "apellidos")
            Pageable pageable,
            Model model
    ) {
        model.addAttribute("pagina", empleadoService.listar(pageable));

        return "admin/empleados/lista";
    }

    @GetMapping("/nuevo")
    public String mostrarCrear(Model model) {
        model.addAttribute("empleado", new EmpleadoCreateRequest(
                null,        // nombres
                null,        // apellidos
                null,        // dni
                null,        // fechaNacimiento
                null,        // telefono
                null,        // direccion
                null,        // referencia
                null,        // foto
                null,        // email
                null,        // password
                Set.of()     // roles
        ));

        return "admin/empleados/crear";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String crear(
            @Valid @ModelAttribute("empleado")
            EmpleadoCreateRequest request,
            BindingResult result,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            return "admin/empleados/crear";
        }

        try {
            empleadoService.crear(request);
        } catch (IllegalArgumentException e) {
            result.reject("empleado.crear", e.getMessage());
            return "admin/empleados/crear";
        }

        redirect.addFlashAttribute(
                "mensaje", "Empleado registrado correctamente"
        );

        return "redirect:/admin/empleados";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable UUID id,
            Model model
    ) {
        model.addAttribute(
                "empleado", empleadoService.buscarPorId(id)
        );

        return "admin/empleados/detalle";
    }

    @GetMapping("/{id}/editar")
    public String mostrarEditar(
            @PathVariable UUID id,
            Model model
    ) {
        EmpleadoResponse empleado = empleadoService.buscarPorId(id);

        model.addAttribute("empleado", new EmpleadoUpdateRequest(
                empleado.nombres(),
                empleado.apellidos(),
                empleado.dni(),
                empleado.fechaNacimiento(),
                empleado.telefono(),
                empleado.direccion(),
                empleado.referencia(),
                null,
                empleado.email(),
                empleado.roles()
        ));

        agregarDatosEdicion(model, empleado);

        return "admin/empleados/editar";
    }

    @PostMapping(
            value = "/{id}/editar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String actualizar(
            @PathVariable UUID id,
            @Valid @ModelAttribute("empleado")
            EmpleadoUpdateRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect
    ) {
        if (result.hasErrors()) {
            agregarDatosEdicion(
                    model, empleadoService.buscarPorId(id)
            );
            return "admin/empleados/editar";
        }

        try {
            empleadoService.actualizar(id, request);
        } catch (IllegalArgumentException e) {
            result.reject("empleado.actualizar", e.getMessage());
            agregarDatosEdicion(
                    model, empleadoService.buscarPorId(id)
            );
            return "admin/empleados/editar";
        }

        redirect.addFlashAttribute(
                "mensaje", "Empleado actualizado correctamente"
        );

        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        empleadoService.desactivar(id);

        redirect.addFlashAttribute(
                "mensaje", "Empleado desactivado correctamente"
        );

        return "redirect:/admin/empleados";
    }

    @PostMapping("/{id}/activar")
    public String activar(
            @PathVariable UUID id,
            RedirectAttributes redirect
    ) {
        empleadoService.activar(id);

        redirect.addFlashAttribute(
                "mensaje", "Empleado activado correctamente"
        );

        return "redirect:/admin/empleados";
    }

    private void agregarDatosEdicion(
            Model model,
            EmpleadoResponse empleado
    ) {
        model.addAttribute("empleadoId", empleado.id());
        model.addAttribute("fotoActual", empleado.foto());
    }
}
