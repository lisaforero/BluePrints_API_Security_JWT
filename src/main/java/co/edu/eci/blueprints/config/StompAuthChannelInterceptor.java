package co.edu.eci.blueprints.config;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;
    private static final String AUTHENTICATION_SESSION_KEY = "STOMP_AUTH";

    public StompAuthChannelInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == null) {
            return message;
        }

        Authentication authentication = authenticateFromHeaders(accessor);
        if (authentication != null) {
            accessor.setUser(authentication);
            // Store in session for subsequent messages
            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put(AUTHENTICATION_SESSION_KEY, authentication);
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return message;
        }

        Authentication sessionAuth = null;
        if (accessor.getSessionAttributes() != null) {
            sessionAuth = (Authentication) accessor.getSessionAttributes().get(AUTHENTICATION_SESSION_KEY);
        }
        if (sessionAuth != null) {
            accessor.setUser(sessionAuth);
            SecurityContextHolder.getContext().setAuthentication(sessionAuth);
        }

        return message;
    }

    private Authentication authenticateFromHeaders(StompHeaderAccessor accessor) {
        String rawToken = extractBearerToken(accessor);
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }

        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(rawToken);
        } catch (Exception ex) {
            return null;
        }

        List<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new UsernamePasswordAuthenticationToken(
            jwt.getSubject(),
            rawToken,
            authorities
        );
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            authHeaders = accessor.getNativeHeader("authorization");
        }

        if (authHeaders != null && !authHeaders.isEmpty()) {
            String header = authHeaders.get(0);
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7).trim();
            }
        }

        return null;
    }

    private List<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object scopeClaim = jwt.getClaims().get("scope");
        if (!(scopeClaim instanceof String scopeText) || scopeText.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(scopeText.split("\\s+"))
            .filter(scope -> !scope.isBlank())
            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
            .collect(Collectors.toList());
    }
}
