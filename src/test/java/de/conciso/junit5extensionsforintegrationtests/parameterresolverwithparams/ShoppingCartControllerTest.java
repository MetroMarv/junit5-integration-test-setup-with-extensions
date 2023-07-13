package de.conciso.junit5extensionsforintegrationtests.parameterresolverwithparams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.conciso.junit5extensionsforintegrationtests.ITSetup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(UserPropertyWithParameterResolver.class)
class ShoppingCartControllerTest extends ITSetup {

  @DynamicPropertySource
  static void dokumenteControllerProperties(DynamicPropertyRegistry registry) {
    setSpringProperties(registry);
  }

  @Test
  void withAnnotationSetUserPermission(
      @RandomUser(value = "readwrite") UserPropertyWithParameterResolver.KeycloakUser user) {

    assertEquals("readwrite", user.permission());
  }

  @Test
  void withAnnotationButNoPermissionSetDefaultPermission(
          @RandomUser UserPropertyWithParameterResolver.KeycloakUser user) {

    assertEquals("readonly", user.permission());
  }
}
