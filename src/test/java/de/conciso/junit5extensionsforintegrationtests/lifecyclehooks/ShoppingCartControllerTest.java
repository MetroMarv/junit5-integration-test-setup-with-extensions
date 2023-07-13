package de.conciso.junit5extensionsforintegrationtests.lifecyclehooks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.conciso.junit5extensionsforintegrationtests.ITSetup;
import de.conciso.junit5extensionsforintegrationtests.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(UserViaLifecycleHookInjector.class)
class ShoppingCartControllerTest extends ITSetup {

  @DynamicPropertySource
  static void dokumenteControllerProperties(DynamicPropertyRegistry registry) {
    setSpringProperties(registry);
  }

  @RandomUser
  UserViaLifecycleHookInjector.KeycloakUser user;

  TestRestTemplate restTemplate;

  @LocalServerPort
  int port;

  @BeforeEach
  void setUp() {
    restTemplate =
            new TestRestTemplate(new RestTemplateBuilder().rootUri("http://localhost:" + port));
  }

  @Test
  void withoutAccessTokenWhenAddingItemToCartThenReturnHttpUnauthorized() {
    HttpEntity<Item> httpEntity = new HttpEntity<>(new Item("Toothpaste", 5));
    restTemplate.exchange("/shopping-cart/item", HttpMethod.POST, httpEntity, String.class);
    ResponseEntity<Item[]> response =
            restTemplate.getForEntity("/shopping-cart/item", Item[].class);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  @Test
  void withAccessTokenWhenAddingItemToCartThenReturnHttpOk() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(user.token());

    HttpEntity<Item> httpEntity = new HttpEntity<>(new Item("Toothpaste", 5), headers);
    ResponseEntity<String> response =
            restTemplate.exchange("/shopping-cart/item", HttpMethod.POST, httpEntity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
