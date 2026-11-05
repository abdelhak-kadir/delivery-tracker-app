package com.delivery_tracker_app.app.controller.v1;

import com.delivery_tracker_app.app.config.TestSecurityConfig;
import com.delivery_tracker_app.app.dto.v1.client.ClientResponse;
import com.delivery_tracker_app.app.dto.v1.client.CreateClientRequest;
import com.delivery_tracker_app.app.dto.v1.client.UpdateClientRequest;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.exception.ResourceNotFoundException;
import com.delivery_tracker_app.app.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for ClientController.
 */
@WebMvcTest(controllers = ClientController.class)
@Import(TestSecurityConfig.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private static final String API_BASE_URL = "/api/v1/clients";

    @Test
    @DisplayName("POST /api/v1/clients should create a client and return 201")
    void createClient_ShouldReturn201() throws Exception {
        CreateClientRequest request = new CreateClientRequest("Acme", "ID-1");
        when(clientService.create(any(CreateClientRequest.class)))
                .thenReturn(new ClientResponse(1L, "Acme", "ID-1"));

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/clients/1"))
                .andExpect(jsonPath("$.message").value("Client created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Acme"));
    }

    @Test
    @DisplayName("PUT /api/v1/clients should update a client and return 200")
    void updateClient_ShouldReturn200() throws Exception {
        UpdateClientRequest request = new UpdateClientRequest(1L, "Acme 2", "ID-2");
        when(clientService.update(any(UpdateClientRequest.class)))
                .thenReturn(new ClientResponse(1L, "Acme 2", "ID-2"));

        mockMvc.perform(put(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Acme 2"));
    }

    @Test
    @DisplayName("GET /api/v1/clients should return a paged list")
    void getAll_ShouldReturn200() throws Exception {
        PagedResponse<ClientResponse> paged = new PagedResponse<>(
                List.of(new ClientResponse(1L, "A", "1"), new ClientResponse(2L, "B", "2")), 0, 10, 2L, 1, true);
        when(clientService.getAll(anyInt(), anyInt())).thenReturn(paged);

        mockMvc.perform(get(API_BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Clients retrieved successfully"))
                .andExpect(jsonPath("$.data.data[0].name").value("A"))
                .andExpect(jsonPath("$.data.totalElements").value(2));

        verify(clientService, times(1)).getAll(0, 10);
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} should return a client")
    void getById_ShouldReturn200() throws Exception {
        when(clientService.getById(1L)).thenReturn(new ClientResponse(1L, "Acme", "ID-1"));

        mockMvc.perform(get(API_BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/clients/{id} should delete a client")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(clientService).delete(1L);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client deleted successfully"));

        verify(clientService, times(1)).delete(1L);
    }

    @Test
    @DisplayName("POST /api/v1/clients should return 400 for invalid input")
    void createClient_ShouldReturn400ForInvalidInput() throws Exception {
        CreateClientRequest invalid = new CreateClientRequest("", "ID-1");

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(clientService, never()).create(any());
    }

    @Test
    @DisplayName("GET /api/v1/clients/{id} should return 404 when the client does not exist")
    void getById_ShouldReturn404() throws Exception {
        when(clientService.getById(999L)).thenThrow(new ResourceNotFoundException("Client not found !"));

        mockMvc.perform(get(API_BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found !"));
    }

    @Test
    @DisplayName("PUT /api/v1/clients should return 404 when the client does not exist")
    void update_ShouldReturn404() throws Exception {
        UpdateClientRequest request = new UpdateClientRequest(999L, "X", "Y");
        when(clientService.update(any(UpdateClientRequest.class)))
                .thenThrow(new ResourceNotFoundException("Client not found !"));

        mockMvc.perform(put(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/clients/{id} should return 404 when the client does not exist")
    void delete_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Client not found !")).when(clientService).delete(999L);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Client not found !"));
    }
}
