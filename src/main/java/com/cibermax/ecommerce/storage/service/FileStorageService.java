package com.cibermax.ecommerce.storage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final long MAX_PIXELES = 12_000_000;

    private final Path directorio;

    public FileStorageService(
            @Value("${app.storage.location:./uploads}") String location
    ) {
        this.directorio = Path.of(location)
                .toAbsolutePath()
                .normalize()
                .resolve("empleados");

        try {
            Files.createDirectories(directorio);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta de fotos", e
            );
        }
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("La foto está vacía");
        }

        if (archivo.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException(
                    "La foto no debe superar los 5 MB"
            );
        }

        BufferedImage imagen = leerImagen(archivo);
        String nombre = UUID.randomUUID() + ".png";
        Path destino = directorio.resolve(nombre);

        try {
            if (!ImageIO.write(imagen, "png", destino.toFile())) {
                throw new IOException("No se pudo generar la imagen PNG");
            }

            return "empleados/" + nombre;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(destino);
            } catch (IOException limpieza) {
                e.addSuppressed(limpieza);
            }

            throw new IllegalStateException(
                    "No se pudo guardar la foto", e
            );
        }
    }

    public Resource recuperar(String ruta) {
        Path archivo = resolver(ruta);

        if (!Files.isRegularFile(archivo) || !Files.isReadable(archivo)) {
            throw new IllegalArgumentException("La foto no existe");
        }

        try {
            return new UrlResource(archivo.toUri());
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo recuperar la foto", e
            );
        }
    }

    public void eliminar(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolver(ruta));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo eliminar la foto", e
            );
        }
    }

    private Path resolver(String ruta) {
        if (ruta == null || !ruta.matches(
                "empleados/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
                        + "[0-9a-fA-F]{12}\\.png"
        )) {
            throw new IllegalArgumentException(
                    "La ruta de la foto no es válida"
            );
        }

        String nombre = ruta.substring("empleados/".length());
        return directorio.resolve(nombre);
    }

    private BufferedImage leerImagen(MultipartFile archivo) {
        try (InputStream entrada = archivo.getInputStream();
             ImageInputStream imagenStream =
                     ImageIO.createImageInputStream(entrada)) {

            if (imagenStream == null) {
                throw new IllegalArgumentException("La foto no es válida");
            }

            Iterator<ImageReader> lectores =
                    ImageIO.getImageReaders(imagenStream);

            if (!lectores.hasNext()) {
                throw new IllegalArgumentException(
                        "El archivo no es una imagen válida"
                );
            }

            ImageReader lector = lectores.next();

            try {
                String formato = lector.getFormatName()
                        .toUpperCase(Locale.ROOT);

                if (!Set.of("JPEG", "PNG").contains(formato)) {
                    throw new IllegalArgumentException(
                            "Solo se permiten imágenes JPEG o PNG"
                    );
                }

                lector.setInput(imagenStream);

                long pixeles =
                        (long) lector.getWidth(0) * lector.getHeight(0);

                if (pixeles > MAX_PIXELES) {
                    throw new IllegalArgumentException(
                            "La imagen no debe superar los 12 megapíxeles"
                    );
                }

                BufferedImage imagen = lector.read(0);

                if (imagen == null) {
                    throw new IllegalArgumentException(
                            "No se pudo interpretar la imagen"
                    );
                }

                return imagen;
            } finally {
                lector.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "La imagen está dañada o no se puede leer", e
            );
        }
    }
}