package com.teadelivery.user.auth.filter;

import com.teadelivery.user.auth.service.JwtTokenProvider;
import com.teadelivery.user.profile.model.User;
import com.teadelivery.user.profile.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for processing JWT tokens in requests.
 * Follows coding standards with comprehensive token processing and security.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract JWT token from Authorization header
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // Extract user information from JWT
                String userId = jwtTokenProvider.getUserIdFromToken(jwt);
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                String[] roles = jwtTokenProvider.getRolesFromToken(jwt);
                
                log.debug("Processing JWT for user: {} with roles: {}", username, String.join(", ", roles));
                
                // Create authorities from roles
                List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Authentication set for user: {} with authorities: {}", 
                         username, authorities.stream().map(Object::toString).collect(Collectors.joining(", ")));
                
            } else if (StringUtils.hasText(jwt)) {
                log.warn("Invalid JWT token provided");
            }
            
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            // Don't throw exception, just continue with unauthenticated request
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from Authorization header.
     * 
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }

    /**
     * Extracts claims from JWT token for debugging purposes.
     * 
     * @param token JWT token
     * @return claims map
     */
    private Claims extractClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtTokenProvider.getVerificationKey())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Validates if the user exists in database.
     * 
     * @param userId user ID from JWT
     * @return true if user exists, false otherwise
     */
    private boolean validateUserExists(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            Optional<User> user = userRepository.findById(userUuid);
            return user.isPresent() && user.get().getStatus() == User.UserStatus.ACTIVE;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID format in JWT: {}", userId);
            return false;
        }
    }
} 