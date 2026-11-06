package com.delivery_tracker_app.app.repository;

import com.delivery_tracker_app.app.entity.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ClientRepo against the in-memory test database.
 */
@DataJpaTest
@ActiveProfiles("test")
class ClientRepoTest {

    @Autowired
    private ClientRepo clientRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should save a client and retrieve it by ID")
    void saveAndFindById_ShouldReturnSavedClient() {
        Client saved = clientRepo.save(Client.builder().name("Acme").identityId("ID-1").build());

        Optional<Client> found = clientRepo.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Acme");
        assertThat(found.get().getIdentityId()).isEqualTo("ID-1");
    }

    @Test
    @DisplayName("should find all clients")
    void findAll_ShouldReturnAllClients() {
        entityManager.persist(Client.builder().name("A").identityId("1").build());
        entityManager.persist(Client.builder().name("B").identityId("2").build());

        List<Client> clients = clientRepo.findAll();

        assertThat(clients).extracting(Client::getName).containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("should delete a client by ID")
    void deleteById_ShouldRemoveClient() {
        Client client = entityManager.persistAndFlush(Client.builder().name("A").identityId("1").build());

        clientRepo.deleteById(client.getId());

        assertThat(clientRepo.findById(client.getId())).isNotPresent();
        assertThat(clientRepo.existsById(client.getId())).isFalse();
    }

    @Test
    @DisplayName("should return empty for a non-existent client")
    void findById_ShouldReturnEmptyForUnknownId() {
        assertThat(clientRepo.findById(99L)).isNotPresent();
    }
}
