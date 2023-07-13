package de.conciso.junit5extensionsforintegrationtests.lifecyclehooks;

import de.conciso.junit5extensionsforintegrationtests.ITSetup;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
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

public class UserViaLifecycleHookInjector implements BeforeEachCallback {

  private final Keycloak keycloakAdminClient;

  private final TestRestTemplate restTemplate;

  public UserViaLifecycleHookInjector() {
    keycloakAdminClient =
        KeycloakBuilder.builder()
            .serverUrl(ITSetup.keycloak.getAuthServerUrl())
            .realm("master")
            .clientId("admin-cli")
            .username(ITSetup.keycloak.getAdminUsername())
            .password(ITSetup.keycloak.getAdminPassword())
            .build();

    restTemplate =
        new TestRestTemplate(
            new RestTemplateBuilder().rootUri(ITSetup.keycloak.getAuthServerUrl()));
  }

  private String createRandomUser() {
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

  @Override
  public void beforeEach(ExtensionContext context) {
    List<Field> annotatedFields =
        AnnotationSupport.findAnnotatedFields(context.getRequiredTestClass(), RandomUser.class);

    annotatedFields.forEach((field) -> injectRandomUser(context.getRequiredTestInstance(), field));
  }

  private void injectRandomUser(Object testInstance, Field field) {
    String username = createRandomUser();
    String token = fetchBearerToken(username);

    KeycloakUser user = new KeycloakUser(username, token);

    field.setAccessible(true);
    try {
      field.set(testInstance, user);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public record KeycloakUser(String username, String token) {}
}
