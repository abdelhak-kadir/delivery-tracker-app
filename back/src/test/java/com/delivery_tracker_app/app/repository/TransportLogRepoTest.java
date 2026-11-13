package com.delivery_tracker_app.app.repository;

import com.delivery_tracker_app.app.entity.Client;
import com.delivery_tracker_app.app.entity.Driver;
import com.delivery_tracker_app.app.entity.TransportLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for TransportLogRepo against the in-memory test database.
 */
@DataJpaTest
@ActiveProfiles("test")
class TransportLogRepoTest {

    @Autowired
    private TransportLogRepo transportLogRepo;

    @Autowired
    private TestEntityManager entityManager;

    private TransportLog newLog(String destination) {
        Client client = entityManager.persist(Client.builder().name("Acme").identityId("ID-1").build());
        Driver driver = entityManager.persist(Driver.builder().name("John").plateNumber("A1").build());
        return TransportLog.builder()
                .client(client)
                .driver(driver)
                .loadDate(LocalDate.of(2025, 1, 1))
                .loadLocation("A")
                .unloadDate(LocalDate.of(2025, 1, 2))
                .unloadLocation("B")
                .destinationName(destination)
                .advance(BigDecimal.TEN)
                .tripPrice(BigDecimal.valueOf(100))
                .operator("op")
                .commercial("com")
                .build();
    }

    @Test
    @DisplayName("should save a transport log and retrieve it with its relations")
    void saveAndFindById_ShouldReturnSavedLog() {
        TransportLog saved = transportLogRepo.save(newLog("Casablanca"));
        entityManager.flush();
        entityManager.clear();

        Optional<TransportLog> found = transportLogRepo.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDestinationName()).isEqualTo("Casablanca");
        assertThat(found.get().getClient().getName()).isEqualTo("Acme");
        assertThat(found.get().getDriver().getPlateNumber()).isEqualTo("A1");
        assertThat(found.get().getTripPrice()).isEqualByComparingTo("100");
    }

    @Test
    @DisplayName("should return a page of transport logs")
    void findAll_ShouldReturnPage() {
        entityManager.persist(newLog("D1"));
        entityManager.persist(newLog("D2"));
        entityManager.flush();

        var page = transportLogRepo.findAll(PageRequest.of(0, 1));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("should delete a transport log")
    void delete_ShouldRemoveLog() {
        TransportLog log = entityManager.persistAndFlush(newLog("D1"));

        transportLogRepo.delete(log);
        entityManager.flush();

        assertThat(transportLogRepo.findById(log.getId())).isNotPresent();
    }

    @Test
    @DisplayName("should return empty for a non-existent transport log")
    void findById_ShouldReturnEmptyForUnknownId() {
        assertThat(transportLogRepo.findById(99L)).isNotPresent();
    }
}
