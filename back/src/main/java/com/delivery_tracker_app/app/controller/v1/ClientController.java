package com.delivery_tracker_app.app.controller.v1;

import com.delivery_tracker_app.app.dto.v1.client.ClientResponse;
import com.delivery_tracker_app.app.dto.v1.client.CreateClientRequest;
import com.delivery_tracker_app.app.dto.v1.client.UpdateClientRequest;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.service.ClientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

// Import Swagger/OpenAPI annotations
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;

// Import your custom DTOs for responses
import com.delivery_tracker_app.app.dto.v1.common.BaseResponse;
import com.delivery_tracker_app.app.exception.ErrorResponse;


@RestController
@Validated
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client Management", description = "Operations related to client entities") // Tag for grouping
@SecurityRequirement(name = "BearerAuth")
public class ClientController {
    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Create a new client", description = "Registers a new client with the system.")
    @ApiResponse(responseCode = "201", description = "Client created successfully",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))) // Direct ClientResponse or wrapped in BaseResponse?
    @ApiResponse(responseCode = "400", description = "Invalid client data provided",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<ClientResponse>> create(@Valid @RequestBody CreateClientRequest request){
        ClientResponse res = clientService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/clients/" + res.id()))
                .body(BaseResponse.ok("Client created successfully", res));
    }

    @PutMapping
    @Operation(summary = "Update an existing client", description = "Modifies the details of an existing client based on their ID.")
    @ApiResponse(responseCode = "200", description = "Client updated successfully",
            content = @Content(schema = @Schema(implementation = ClientResponse.class))) // Direct ClientResponse or wrapped in BaseResponse?
    @ApiResponse(responseCode = "400", description = "Invalid client update data",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Client not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<ClientResponse>> update(@Valid @RequestBody UpdateClientRequest request){
        ClientResponse res = clientService.update(request);
        return ResponseEntity.ok(BaseResponse.ok("Client updated successfully", res));
    }

    @GetMapping
    @Operation(summary = "Get all clients with pagination", description = "Retrieves a paginated list of all client records.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of clients",
            content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<PagedResponse<ClientResponse>>> getAll(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100)
            @Parameter(description = "Number of items per page (1-100)", example = "10") int size
    ){
        PagedResponse<ClientResponse> res = clientService.getAll(page, size);
        return ResponseEntity.ok(BaseResponse.ok("Clients retrieved successfully", res));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID", description = "Retrieves the details of a single client by their unique identifier.")
    @ApiResponse(responseCode = "200", description = "Client found and returned",
            content = @Content(schema = @Schema(implementation = ClientResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid client ID format",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Client not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<ClientResponse>> getById(
            @PathVariable
            @Parameter(description = "ID of the client to retrieve", example = "1") @Min(1) Long id){
        ClientResponse res = clientService.getById(id);
        return ResponseEntity.ok(BaseResponse.ok("Client retrieved successfully", res));

    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client by ID", description = "Removes a client record from the system based on their unique ID.")
    @ApiResponse(responseCode = "200", description = "Client deleted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid client ID format",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Client not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable
            @Parameter(description = "ID of the client to delete", example = "1") @Min(1) Long id){
        clientService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok("Client deleted successfully", null));
    }
}