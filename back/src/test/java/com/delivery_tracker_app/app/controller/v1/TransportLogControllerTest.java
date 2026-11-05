package com.delivery_tracker_app.app.controller.v1;

import com.delivery_tracker_app.app.config.TestSecurityConfig;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.CreateTransportLogRequest;
import com.delivery_tracker_app.app.dto.v1.transportLog.TransportLogResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.UpdateTransportLogRequest;
import com.delivery_tracker_app.app.exception.ResourceNotFoundException;
import com.delivery_tracker_app.app.service.TransportLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MockMvc tests for TransportLogController.
 */
@WebMvcTest(controllers = TransportLogController.class)
@Import(TestSecurityConfig.class)
class TransportLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransportLogService transportLogService;

    private static final String API_BASE_URL = "/api/v1/trans_logs";
    private static final BigDecimal N = BigDecimal.TEN;

    private String json(Object o) throws Exception {
        return objectMapper.copy().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writeValueAsString(o);
    }

    private CreateTransportLogRequest createRequest(String loadLocation) {
        return new CreateTransportLogRequest(1L, 2L, LocalDate.of(2025, 1, 1), loadLocation,
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
    }

    private TransportLogResponse response() {
        return new TransportLogResponse(5L, null, null, LocalDate.of(2025, 1, 1), "A",
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
    }

    @Test
    @DisplayName("POST should create a transport log and return 201")
    void create_ShouldReturn201() throws Exception {
        when(transportLogService.create(any(CreateTransportLogRequest.class))).thenReturn(response());

        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest("A"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/trans_logs/5"))
                .andExpect(jsonPath("$.message").value("Transport log created successfully"))
                .andExpect(jsonPath("$.data.id").value(5L));
    }

    @Test
    @DisplayName("PUT should update a transport log and return 200")
    void update_ShouldReturn200() throws Exception {
        UpdateTransportLogRequest request = new UpdateTransportLogRequest(5L, 1L, 2L, LocalDate.of(2025, 1, 1), "A",
                LocalDate.of(2025, 1, 2), "B", "Dest", "note", N, N, N, N, N, N, N, "op", "com");
        when(transportLogService.update(any(UpdateTransportLogRequest.class))).thenReturn(response());

        mockMvc.perform(put(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transport log updated successfully"));
    }

    @Test
    @DisplayName("GET should return a paged list")
    void getAll_ShouldReturn200() throws Exception {
        when(transportLogService.getAll(anyInt(), anyInt()))
                .thenReturn(new PagedResponse<>(List.of(response()), 0, 10, 1L, 1, true));

        mockMvc.perform(get(API_BASE_URL).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data[0].destinationName").value("Dest"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(transportLogService, times(1)).getAll(0, 10);
    }

    @Test
    @DisplayName("GET /{id} should return a transport log")
    void getById_ShouldReturn200() throws Exception {
        when(transportLogService.getById(5L)).thenReturn(response());

        mockMvc.perform(get(API_BASE_URL + "/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5L));
    }

    @Test
    @DisplayName("DELETE /{id} should delete a transport log")
    void delete_ShouldReturn200() throws Exception {
        doNothing().when(transportLogService).delete(5L);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transport log deleted successfully"));
    }

    @Test
    @DisplayName("POST should return 400 for invalid input")
    void create_ShouldReturn400() throws Exception {
        mockMvc.perform(post(API_BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createRequest(""))))
                .andExpect(status().isBadRequest());

        verify(transportLogService, never()).create(any());
    }

    @Test
    @DisplayName("GET /{id} should return 404 when not found")
    void getById_ShouldReturn404() throws Exception {
        when(transportLogService.getById(999L)).thenThrow(new ResourceNotFoundException("Transport log not found !"));

        mockMvc.perform(get(API_BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Transport log not found !"));
    }

    @Test
    @DisplayName("DELETE /{id} should return 404 when not found")
    void delete_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Transport log not found!")).when(transportLogService).delete(999L);

        mockMvc.perform(delete(API_BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
