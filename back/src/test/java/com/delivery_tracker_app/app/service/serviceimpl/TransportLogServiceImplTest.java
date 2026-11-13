package com.delivery_tracker_app.app.service.serviceimpl;

import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.CreateTransportLogRequest;
import com.delivery_tracker_app.app.dto.v1.transportLog.TransportLogResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.UpdateTransportLogRequest;
import com.delivery_tracker_app.app.entity.Client;
import com.delivery_tracker_app.app.entity.Driver;
import com.delivery_tracker_app.app.entity.TransportLog;
import com.delivery_tracker_app.app.exception.ResourceNotFoundException;
import com.delivery_tracker_app.app.mapper.TransportLogMapper;
import com.delivery_tracker_app.app.repository.ClientRepo;
import com.delivery_tracker_app.app.repository.DriverRepo;
import com.delivery_tracker_app.app.repository.TransportLogRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransportLogServiceImpl; repositories and mapper are mocked.
 */
@ExtendWith(MockitoExtension.class)
class TransportLogServiceImplTest {

    @Mock
    private TransportLogRepo transportLogRepo;
    @Mock
    private ClientRepo clientRepo;
    @Mock
    private DriverRepo driverRepo;
    @Mock
    private TransportLogMapper transportLogMapper;

    @InjectMocks
    private TransportLogServiceImpl service;

    private static final BigDecimal N = BigDecimal.TEN;

    private CreateTransportLogRequest createRequest() {
        return new CreateTransportLogRequest(1L, 2L, LocalDate.of(2025, 1, 1), "A",
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
    }

    private UpdateTransportLogRequest updateRequest(Long id) {
        return new UpdateTransportLogRequest(id, 1L, 2L, LocalDate.of(2025, 1, 1), "A",
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
    }

    private TransportLogResponse response() {
        return new TransportLogResponse(5L, null, null, LocalDate.of(2025, 1, 1), "A",
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
    }

    @Test
    @DisplayName("should create a transport log")
    void create_ShouldReturnResponse() {
        CreateTransportLogRequest request = createRequest();
        Client client = Client.builder().id(1L).build();
        Driver driver = Driver.builder().id(2L).build();
        TransportLog entity = new TransportLog();
        TransportLog saved = new TransportLog();
        TransportLogResponse expected = response();

        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(driverRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(transportLogMapper.toEntity(request, client, driver)).thenReturn(entity);
        when(transportLogRepo.save(entity)).thenReturn(saved);
        when(transportLogMapper.toDto(saved)).thenReturn(expected);

        assertEquals(expected, service.create(request));
        verify(transportLogRepo, times(1)).save(entity);
    }

    @Test
    @DisplayName("create should throw if client is missing")
    void create_ShouldThrowIfClientMissing() {
        when(clientRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(createRequest()));
        verify(transportLogRepo, never()).save(any(TransportLog.class));
    }

    @Test
    @DisplayName("create should throw if driver is missing")
    void create_ShouldThrowIfDriverMissing() {
        when(clientRepo.findById(1L)).thenReturn(Optional.of(Client.builder().id(1L).build()));
        when(driverRepo.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(createRequest()));
        verify(transportLogRepo, never()).save(any(TransportLog.class));
    }

    @Test
    @DisplayName("should update a transport log")
    void update_ShouldReturnResponse() {
        UpdateTransportLogRequest request = updateRequest(5L);
        Client client = Client.builder().id(1L).build();
        Driver driver = Driver.builder().id(2L).build();
        TransportLog existing = new TransportLog();
        TransportLog entity = new TransportLog();
        TransportLogResponse expected = response();

        when(transportLogRepo.findById(5L)).thenReturn(Optional.of(existing));
        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(driverRepo.findById(2L)).thenReturn(Optional.of(driver));
        when(transportLogMapper.toEntity(request, client, driver)).thenReturn(entity);
        when(transportLogRepo.save(entity)).thenReturn(entity);
        when(transportLogMapper.toDto(entity)).thenReturn(expected);

        assertEquals(expected, service.update(request));
    }

    @Test
    @DisplayName("update should throw if the log is missing")
    void update_ShouldThrowIfLogMissing() {
        when(transportLogRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(updateRequest(99L)));
        verify(transportLogRepo, never()).save(any(TransportLog.class));
    }

    @Test
    @DisplayName("update should throw if client or driver is missing")
    void update_ShouldThrowIfClientOrDriverMissing() {
        when(transportLogRepo.findById(5L)).thenReturn(Optional.of(new TransportLog()));
        when(clientRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(updateRequest(5L)));

        when(clientRepo.findById(1L)).thenReturn(Optional.of(Client.builder().id(1L).build()));
        when(driverRepo.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.update(updateRequest(5L)));
        verify(transportLogRepo, never()).save(any(TransportLog.class));
    }

    @Test
    @DisplayName("should return a paginated list of transport logs")
    void getAll_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        TransportLog log = new TransportLog();
        Page<TransportLog> page = new PageImpl<>(List.of(log), pageable, 1);
        TransportLogResponse dto = response();

        when(transportLogRepo.findAll(pageable)).thenReturn(page);
        when(transportLogMapper.toDto(log)).thenReturn(dto);

        PagedResponse<TransportLogResponse> actual = service.getAll(0, 10);

        assertEquals(List.of(dto), actual.data());
        assertEquals(1, actual.totalElements());
        assertEquals(1, actual.totalPages());
        assertTrue(actual.last());
    }

    @Test
    @DisplayName("should return a transport log by ID")
    void getById_ShouldReturnResponse() {
        TransportLog log = new TransportLog();
        TransportLogResponse dto = response();
        when(transportLogRepo.findById(5L)).thenReturn(Optional.of(log));
        when(transportLogMapper.toDto(log)).thenReturn(dto);

        assertEquals(dto, service.getById(5L));
    }

    @Test
    @DisplayName("getById should throw if not found")
    void getById_ShouldThrowIfNotFound() {
        when(transportLogRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    @DisplayName("should delete a transport log")
    void delete_ShouldDelete() {
        TransportLog log = new TransportLog();
        when(transportLogRepo.findById(5L)).thenReturn(Optional.of(log));

        assertDoesNotThrow(() -> service.delete(5L));
        verify(transportLogRepo, times(1)).delete(log);
    }

    @Test
    @DisplayName("delete should throw if not found")
    void delete_ShouldThrowIfNotFound() {
        when(transportLogRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.delete(99L));
        verify(transportLogRepo, never()).delete(any(TransportLog.class));
    }
}
