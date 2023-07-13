package de.conciso.junit5extensionsforintegrationtests;

import org.springframework.data.repository.CrudRepository;

public interface ShoppingCartRepository extends CrudRepository<Item, Long> {}
