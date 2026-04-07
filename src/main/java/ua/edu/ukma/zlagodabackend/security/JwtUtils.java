package ua.edu.ukma.zlagodabackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtUtils {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        EmployeeDetails userPrincipal = (EmployeeDetails) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority())
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build()
                .parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}