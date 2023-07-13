package de.conciso.junit5extensionsforintegrationtests.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService {

  public User getCurrentUser() {
    final JwtAuthenticationToken jwtAuthenticationToken =
        (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    final Jwt token = jwtAuthenticationToken.getToken();
    final String authId = token.getClaim("sub");
    final String name = token.getClaim("name");
    return new User(authId, name);
  }
}
