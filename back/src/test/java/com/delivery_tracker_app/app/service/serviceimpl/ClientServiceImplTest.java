package com.delivery_tracker_app.app.service.serviceimpl;

import com.delivery_tracker_app.app.dto.v1.client.ClientResponse;
import com.delivery_tracker_app.app.dto.v1.client.CreateClientRequest;
import com.delivery_tracker_app.app.dto.v1.client.UpdateClientRequest;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.entity.Client;
import com.delivery_tracker_app.app.exception.ResourceNotFoundException;
import com.delivery_tracker_app.app.mapper.ClientMapper;
import com.delivery_tracker_app.app.repository.ClientRepo;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ClientServiceImpl; repository and mapper are mocked.
 */
@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepo clientRepo;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    @DisplayName("should create a new client successfully")
    void create_ShouldReturnClientResponse() {
        CreateClientRequest request = new CreateClientRequest("Acme", "ID-1");
        Client entity = Client.builder().name("Acme").identityId("ID-1").build();
        Client saved = Client.builder().id(1L).name("Acme").identityId("ID-1").build();
        ClientResponse expected = new ClientResponse(1L, "Acme", "ID-1");

        when(clientMapper.toEntity(request)).thenReturn(entity);
        when(clientRepo.save(entity)).thenReturn(saved);
        when(clientMapper.toDto(saved)).thenReturn(expected);

        ClientResponse actual = clientService.create(request);

        assertEquals(expected, actual);
        verify(clientRepo, times(1)).save(entity);
    }

    @Test
    @DisplayName("should update an existing client successfully")
    void update_ShouldReturnClientResponse() {
        UpdateClientRequest request = new UpdateClientRequest(1L, "Acme 2", "ID-2");
        Client existing = Client.builder().id(1L).name("Acme").identityId("ID-1").build();
        ClientResponse expected = new ClientResponse(1L, "Acme 2", "ID-2");

        when(clientRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(clientRepo.save(existing)).thenReturn(existing);
        when(clientMapper.toDto(existing)).thenReturn(expected);

        ClientResponse actual = clientService.update(request);

        assertEquals(expected, actual);
        verify(clientRepo, times(1)).findById(1L);
        verify(clientRepo, times(1)).save(existing);
        assertEquals("Acme 2", existing.getName());
        assertEquals("ID-2", existing.getIdentityId());
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException if client to update is not found")
    void update_ShouldThrowIfNotFound() {
        UpdateClientRequest request = new UpdateClientRequest(99L, "X", "Y");
        when(clientRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.update(request));
        verify(clientRepo, never()).save(any(Client.class));
    }

    @Test
    @DisplayName("should return a paginated list of clients")
    void getAll_ShouldReturnPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Client c1 = Client.builder().id(1L).build();
        Client c2 = Client.builder().id(2L).build();
        Page<Client> page = new PageImpl<>(List.of(c1, c2), pageable, 2);
        ClientResponse d1 = new ClientResponse(1L, "A", "1");
        ClientResponse d2 = new ClientResponse(2L, "B", "2");

        when(clientRepo.findAll(pageable)).thenReturn(page);
        when(clientMapper.toDto(c1)).thenReturn(d1);
        when(clientMapper.toDto(c2)).thenReturn(d2);

        PagedResponse<ClientResponse> actual = clientService.getAll(0, 10);

        assertEquals(List.of(d1, d2), actual.data());
        assertEquals(0, actual.page());
        assertEquals(10, actual.size());
        assertEquals(2, actual.totalElements());
        assertEquals(1, actual.totalPages());
        assertTrue(actual.last());
    }

    @Test
    @DisplayName("should return a client by ID")
    void getById_ShouldReturnClientResponse() {
        Client client = Client.builder().id(1L).name("Acme").identityId("ID-1").build();
        ClientResponse expected = new ClientResponse(1L, "Acme", "ID-1");
        when(clientRepo.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toDto(client)).thenReturn(expected);

        assertEquals(expected, clientService.getById(1L));
    }

    @Test
    @DisplayName("should throw ResourceNotFoundException if client by ID is not found")
    void getById_ShouldThrowIfNotFound() {
        when(clientRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.getById(99L));
        verify(clientMapper, never()).toDto(any(Client.class));
    }

    @Test
    @DisplayName("should delete an existing client")
    void delete_ShouldDeleteClient() {
        when(clientRepo.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> clientService.delete(1L));
        verify(clientRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("should throw when deleting a non-existent client")
    void delete_ShouldThrowIfNotFound() {
        when(clientRepo.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> clientService.delete(99L));
        verify(clientRepo, never()).deleteById(anyLong());
    }
}
