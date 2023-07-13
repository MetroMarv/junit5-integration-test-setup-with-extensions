package de.conciso.junit5extensionsforintegrationtests;

import de.conciso.junit5extensionsforintegrationtests.user.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShoppingCartController {
  private final ShoppingCartRepository shoppingCartRepository;

  private final UserDetailsService userDetailsService;

  public ShoppingCartController(ShoppingCartRepository shoppingCartRepository, UserDetailsService userDetailsService) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.userDetailsService = userDetailsService;
  }

  @PostMapping("/shopping-cart/item")
  public void addItemToShoppingCart(Item item) {
    item.setUserId(userDetailsService.getCurrentUser().authId());
    shoppingCartRepository.save(item);
  }

  @GetMapping("/shopping-cart/item")
  public Iterable<Item> getCartItems() {
    return shoppingCartRepository.findAll();
  }
}
