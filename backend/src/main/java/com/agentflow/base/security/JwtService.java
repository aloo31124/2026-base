package com.agentflow.base.security;

import com.agentflow.base.service.SessionTimeoutPolicyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final SessionTimeoutPolicyService sessionTimeoutPolicyService;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        SessionTimeoutPolicyService sessionTimeoutPolicyService
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.sessionTimeoutPolicyService = sessionTimeoutPolicyService;
    }

    public String create(String username, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder().subject(username).claim("roles", roles).issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(sessionTimeoutPolicyService.currentTimeoutMinutes(), ChronoUnit.MINUTES)))
            .signWith(key).compact();
    }

    public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
