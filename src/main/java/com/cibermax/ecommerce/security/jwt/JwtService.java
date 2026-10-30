package com.cibermax.ecommerce.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.expiration}")
    private long expiration;

    public String extractCorreo(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer fecha de expiración
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extraer cualquier claim
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generar token solo con correo
    public String generateToken(String correo) {

        Date now = new Date();

        return Jwts.builder()
                .subject(correo)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Generar token con correo + claims adicionales
    public String generateToken(
            String correo,
            Map<String, Object> extraClaims
    ) {

        Date now = new Date();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(correo)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Validar token comparando correo
    public boolean isTokenValid(
            String token,
            String correo
    ) {

        String correoToken = extractCorreo(token);

        return correoToken.equals(correo)
                && !isTokenExpired(token);
    }

    // Verificar si expiró
    public boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    // Extraer todos los claims y validar firma
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Crear llave para firmar/verificar JWT
    private SecretKey getSigningKey() {

        byte[] keyBytes =
                Decoders.BASE64.decode(secret);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
