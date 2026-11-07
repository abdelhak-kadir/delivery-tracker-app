package com.delivery_tracker_app.app.controller.v1;

import com.delivery_tracker_app.app.config.ApiPaths;
import com.delivery_tracker_app.app.dto.v1.common.BaseResponse;
import com.delivery_tracker_app.app.dto.v1.common.PagedResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.CreateTransportLogRequest;
import com.delivery_tracker_app.app.dto.v1.transportLog.TransportLogResponse;
import com.delivery_tracker_app.app.dto.v1.transportLog.UpdateTransportLogRequest;
import com.delivery_tracker_app.app.service.TransportLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
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

// Import your custom DTOs for error responses
import com.delivery_tracker_app.app.exception.ErrorResponse;

@RestController
@Validated
@AllArgsConstructor
@RequestMapping(ApiPaths.BASE+ApiPaths.V1+"/trans_logs")
@Tag(name = "Transport Log Management", description = "Operations related to transport log entries") // Tag for grouping
@SecurityRequirement(name = "BearerAuth")
public class TransportLogController {
    private final TransportLogService transportLogService;

    @PostMapping
    @Operation(summary = "Create a new transport log entry", description = "Records a new transport log entry in the system.")
    @ApiResponse(responseCode = "201", description = "Transport log created successfully",
                 content = @Content(schema = @Schema(implementation = TransportLogResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid transport log data provided",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<TransportLogResponse>> create(@Valid @RequestBody CreateTransportLogRequest request){
        TransportLogResponse res = transportLogService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/trans_logs/" + res.id()))
                .body(BaseResponse.ok("Transport log created successfully", res));
    }

    @PutMapping
    @Operation(summary = "Update an existing transport log entry", description = "Modifies details of an existing transport log entry.")
    @ApiResponse(responseCode = "200", description = "Transport log updated successfully",
                 content = @Content(schema = @Schema(implementation = TransportLogResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid update data provided",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Transport log not found", // Assuming service throws ResourceNotFoundException
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<TransportLogResponse>> update(@Valid @RequestBody UpdateTransportLogRequest request){
        TransportLogResponse res = transportLogService.update(request);
        return ResponseEntity.ok(BaseResponse.ok("Transport log updated successfully", res));
    }

    @GetMapping
    @Operation(summary = "Get all transport log entries with pagination", description = "Retrieves a paginated list of all transport log entries.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of transport log entries",
                 content = @Content(schema = @Schema(implementation = PagedResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<PagedResponse<TransportLogResponse>>> getAll(
            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (0-indexed)", example = "0") int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(30)
            @Parameter(description = "Number of items per page (1-30)", example = "10") int size
    ){
        PagedResponse<TransportLogResponse> res = transportLogService.getAll(page, size);
        return ResponseEntity.ok(BaseResponse.ok("Transport logs retrieved successfully", res));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transport log entry by ID", description = "Retrieves details of a single transport log entry by its unique ID.")
    @ApiResponse(responseCode = "200", description = "Transport log entry found and returned",
                 content = @Content(schema = @Schema(implementation = TransportLogResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid transport log ID format",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Transport log entry not found", // Assuming service throws ResourceNotFoundException
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<TransportLogResponse>> getById(
            @PathVariable
            @Parameter(description = "ID of the transport log entry to retrieve", example = "1") @Min(1) Long id){
        TransportLogResponse res = transportLogService.getById(id);
        return ResponseEntity.ok(BaseResponse.ok("Transport log retrieved successfully", res));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transport log entry by ID", description = "Deletes a transport log record from the system based on its unique ID.")
    @ApiResponse(responseCode = "200", description = "Transport log entry deleted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid transport log ID format",
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Transport log entry not found", // Assuming service throws ResourceNotFoundException
                 content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<BaseResponse<Void>> delete(
            @PathVariable
            @Parameter(description = "ID of the transport log entry to delete", example = "1") @Min(1) Long id){
        transportLogService.delete(id);
        return ResponseEntity.ok(BaseResponse.ok("Transport log deleted successfully", null));
    }
}