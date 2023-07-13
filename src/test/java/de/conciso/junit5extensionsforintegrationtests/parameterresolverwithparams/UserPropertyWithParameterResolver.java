package de.conciso.junit5extensionsforintegrationtests.parameterresolverwithparams;

import de.conciso.junit5extensionsforintegrationtests.ITSetup;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class UserPropertyWithParameterResolver implements ParameterResolver {

  private final Keycloak keycloakAdminClient;

  private final TestRestTemplate restTemplate;

  public UserPropertyWithParameterResolver() {
    keycloakAdminClient =
        KeycloakBuilder.builder()
            .serverUrl(ITSetup.keycloak.getAuthServerUrl())
            .realm("master")
            .clientId("admin-cli")
            .username(ITSetup.keycloak.getAdminUsername())
            .password(ITSetup.keycloak.getAdminPassword())
            .build();

    restTemplate =
        new TestRestTemplate(new RestTemplateBuilder().rootUri(ITSetup.keycloak.getAuthServerUrl()));
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return parameterContext.getParameter().getType().equals(KeycloakUser.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {

    String permission =
        parameterContext
            .findAnnotation(RandomUser.class)
            .map(RandomUser::value)
            .orElse("readonly");

    String username = createRandomUser(permission);
    String token = fetchBearerToken(username);


    return new KeycloakUser(username, token, permission);
  }

  private String createRandomUser(String permission) {
    String email = UUID.randomUUID() + "@conciso.de";

    UserRepresentation newUserRepresentation = new UserRepresentation();
    newUserRepresentation.setUsername(email);
    newUserRepresentation.setFirstName(email);
    newUserRepresentation.setLastName(email);
    newUserRepresentation.setEmail(email);
    newUserRepresentation.setEmailVerified(true);
    newUserRepresentation.setEnabled(true);

    CredentialRepresentation password = new CredentialRepresentation();
    password.setType(CredentialRepresentation.PASSWORD);
    password.setTemporary(false);
    password.setValue(email);
    newUserRepresentation.setCredentials(List.of(password));
    RealmResource realmResource = keycloakAdminClient.realm("master");
    realmResource.users().create(newUserRepresentation);

    List<UserRepresentation> search = realmResource.users().search(email, true);
    Assertions.assertNotNull(search.get(0).getId());
    return email;
  }

  private String fetchBearerToken(String username) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>(4);
    body.add("client_id", "admin-cli");
    body.add("username", username);
    body.add("password", username);
    body.add("grant_type", "password");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);
    ResponseEntity<Map<String, Object>> keycloakResponse =
        restTemplate.exchange(
            "/realms/{realm}/protocol/openid-connect/token",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<>() {},
            "master");
    if (!keycloakResponse.getStatusCode().equals(HttpStatus.OK)) {
      throw new RuntimeException("Something went wrong with the keycloak token request");
    }
    return (String) Objects.requireNonNull(keycloakResponse.getBody()).get("access_token");
  }

  public record KeycloakUser(String username, String token, String permission) {}
}
