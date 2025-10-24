package com.skat.backend.api.controller;

import com.skat.backend.application.PlayersService;
import com.skat.backend.application.dto.PlayerListResponseTO;
import com.skat.backend.application.dto.PlayerTO;
import com.skat.backend.application.dto.PlayersQuery;
import com.skat.backend.application.dto.PlayersSort;
import com.skat.backend.application.dto.UpsertPlayerRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/players")
@Validated
@Tag(name = "Players", description = "Player management API")
public class PlayersController {

	@Autowired
	private PlayersService playersService;

	@GetMapping
	@Operation(summary = "List all players", description = "Retrieves a paginated list of players with their current score snapshot, supporting sorting and pagination")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Successfully retrieved list of players",
			content = @Content(schema = @Schema(implementation = PlayerListResponseTO.class))),
		@ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content)
	})
	public ResponseEntity<PlayerListResponseTO> listPlayers(
		@Parameter(description = "Sort order for players (NAME or SCORE_DESC)", example = "NAME")
		@RequestParam(name = "sort", required = false, defaultValue = "NAME") PlayersSort sort,
		@Parameter(description = "Starting index for pagination (0-based)", example = "0")
		@RequestParam(name = "startIndex", required = false, defaultValue = "0") @Min(0) int startIndex,
		@Parameter(description = "Number of items per page (1-200)", example = "50")
		@RequestParam(name = "pageSize", required = false, defaultValue = "50") @Min(1) @Max(200) int pageSize) {
		var query = new PlayersQuery(startIndex, pageSize, sort);
		var response = playersService.listPlayers(query);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	@Operation(summary = "Create a new player", description = "Creates a new player with unique first and last name combination")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "Player successfully created",
			content = @Content(schema = @Schema(implementation = PlayerTO.class))),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
		@ApiResponse(responseCode = "409", description = "Player with the same name already exists", content = @Content)
	})
	public ResponseEntity<PlayerTO> createPlayer(
		@Parameter(description = "Player data to create", required = true)
		@Valid @RequestBody UpsertPlayerRequest request) {
		var player = playersService.createPlayer(request);
		var location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(player.id())
			.toUri();
		return ResponseEntity.created(location).body(player);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update an existing player", description = "Updates player information by ID, enforcing uniqueness constraints")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Player successfully updated",
			content = @Content(schema = @Schema(implementation = PlayerTO.class))),
		@ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
		@ApiResponse(responseCode = "404", description = "Player not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Player with the same name already exists", content = @Content)
	})
	public ResponseEntity<PlayerTO> updatePlayer(
		@Parameter(description = "Player ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID id,
		@Parameter(description = "Updated player data", required = true)
		@Valid @RequestBody UpsertPlayerRequest request) {
		var player = playersService.updatePlayer(id, request);
		return ResponseEntity.ok(player);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a player", description = "Deletes a player by ID. Use forceDeletion=true to nullify references in games and scores before deletion")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "Player successfully deleted"),
		@ApiResponse(responseCode = "404", description = "Player not found", content = @Content),
		@ApiResponse(responseCode = "409", description = "Player has references and forceDeletion is false", content = @Content)
	})
	public ResponseEntity<Void> deletePlayer(
		@Parameter(description = "Player ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
		@PathVariable UUID id,
		@Parameter(description = "Force deletion by nullifying references", example = "false")
		@RequestParam(name = "forceDeletion", required = false, defaultValue = "false") boolean forceDeletion) {
		playersService.deletePlayer(id, forceDeletion);
		return ResponseEntity.noContent().build();
	}
}
