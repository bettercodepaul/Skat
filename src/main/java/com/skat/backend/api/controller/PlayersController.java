package com.skat.backend.api.controller;

import com.skat.backend.application.PlayersService;
import com.skat.backend.application.dto.PlayerListResponseTO;
import com.skat.backend.application.dto.PlayerTO;
import com.skat.backend.application.dto.PlayersQuery;
import com.skat.backend.application.dto.PlayersSort;
import com.skat.backend.application.dto.UpsertPlayerRequest;
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
public class PlayersController {

	@Autowired
	private PlayersService playersService;

	@GetMapping
	public ResponseEntity<PlayerListResponseTO> listPlayers(
		@RequestParam(name = "sort", required = false, defaultValue = "NAME") PlayersSort sort,
		@RequestParam(name = "startIndex", required = false, defaultValue = "0") @Min(0) int startIndex,
		@RequestParam(name = "pageSize", required = false, defaultValue = "50") @Min(1) @Max(200) int pageSize) {
		var query = new PlayersQuery(startIndex, pageSize, sort);
		var response = playersService.listPlayers(query);
		return ResponseEntity.ok(response);
	}

	@PostMapping
	public ResponseEntity<PlayerTO> createPlayer(@Valid @RequestBody UpsertPlayerRequest request) {
		var player = playersService.createPlayer(request);
		var location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(player.id())
			.toUri();
		return ResponseEntity.created(location).body(player);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PlayerTO> updatePlayer(
		@PathVariable UUID id,
		@Valid @RequestBody UpsertPlayerRequest request) {
		var player = playersService.updatePlayer(id, request);
		return ResponseEntity.ok(player);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePlayer(
		@PathVariable UUID id,
		@RequestParam(name = "forceDeletion", required = false, defaultValue = "false") boolean forceDeletion) {
		playersService.deletePlayer(id, forceDeletion);
		return ResponseEntity.noContent().build();
	}
}
