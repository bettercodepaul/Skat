package com.skat.backend.api.controller;

import com.skat.backend.application.PlayersService;
import com.skat.backend.application.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/players")
@Validated
public class PlayersController {

    private final PlayersService playersService;

    public PlayersController(PlayersService playersService) {
        this.playersService = playersService;
    }

    @GetMapping
    public ResponseEntity<PlayerListResponseTO> listPlayers(
        @RequestParam(name = "sort", required = false, defaultValue = "NAME") PlayersSort sort,
        @RequestParam(name = "startIndex", required = false, defaultValue = "0") @Min(0) int startIndex,
        @RequestParam(name = "pageSize", required = false, defaultValue = "50") @Min(1) @Max(200) int pageSize
    ) {
        PlayersQuery query = new PlayersQuery(startIndex, pageSize, sort);
        PlayerListResponseTO response = playersService.listPlayers(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PlayerTO> createPlayer(@Valid @RequestBody UpsertPlayerRequest request) {
        PlayerTO player = playersService.createPlayer(request);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(player.id())
            .toUri();
        return ResponseEntity.created(location).body(player);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlayerTO> updatePlayer(
        @PathVariable UUID id,
        @Valid @RequestBody UpsertPlayerRequest request
    ) {
        PlayerTO player = playersService.updatePlayer(id, request);
        return ResponseEntity.ok(player);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(
        @PathVariable UUID id,
        @RequestParam(name = "forceDeletion", required = false, defaultValue = "false") boolean forceDeletion
    ) {
        playersService.deletePlayer(id, forceDeletion);
        return ResponseEntity.noContent().build();
    }
}
