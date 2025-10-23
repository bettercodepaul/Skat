# REST API Overview (Draft)

This document lists planned REST endpoints for the Skat application. It names the parameters and the
structure of responses briefly; detailed field semantics and edge cases will follow in a separate
spec.

> **Conventions**
> - **IDs:** All identifiers are `UUID`.
> - **Timestamps:** Use ISO‑8601 `OffsetDateTime` (e.g., `2025-10-23T20:15:00+02:00`).
> - **Errors:** Standard problem shape (minimal) — see **Error Response Shape** below.
> - **Content-Type:** `application/json` for requests and responses.
> - **Uniqueness:** `(first_name, last_name)` must be unique for players; conflicts return **409
    Conflict**.

---

## 1) Fetch all players with current score

**GET** `/api/players`

**Query Params**

- `sort` (optional, string): e.g., `name`, `score_desc` (default TBD).
- `startIndex`, `pageSize` (optional, int): pagination hints (if pagination is applied). index
  starts with 0 for the first item.

**Response (200 OK)**

```json
[
  {
    "id": "UUID",
    "first_name": "string",
    "last_name": "string",
    "current_total_points": 123,
    "current_sequence_index": 7,
    "updated_at": "OffsetDateTime"
  }
]
```

**Errors**

- `400 Bad Request` for invalid paging/sort values.

---

## 2) Create / Update a player (first & last name must be unique)

### Create player

**POST** `/api/players`

**Request Body**

```json
{
  "first_name": "string (<=50)",
  "last_name": "string (<=50)"
}
```

**Responses**

- `201 Created` with body:
  ```json
  {
    "id": "UUID",
    "first_name": "string",
    "last_name": "string"
  }
  ```
  `Location: /api/players/{id}`
- `400 Bad Request` if names missing/too long.
- `409 Conflict` if a player with the same first & last name already exists.
  ```json
  {
    "error": "conflict",
    "message": "Player with first_name+last_name already exists",
    "field": "first_name,last_name"
  }
  ```

### Create/Update player

**POST** `/api/players` for create

**PUT** `/api/players/{id}` for update

**Path Params**

- `id` (UUID) — target player

**Request Body**

```json
{
  "first_name": "string (<=50)",
  "last_name": "string (<=50)"
}
```

**Responses**

- `200 OK` with updated player (same shape as create response).
- `400 Bad Request` for validation errors.
- `404 Not Found` if player does not exist.
- `409 Conflict` if update would violate uniqueness of `(first_name, last_name)`.

---

## 3) Load player scores starting from a given date

**GET** `/api/player-scores`

**Query Params**

- `from` (required, `OffsetDateTime`): only return score entries created **on or after** this
  timestamp.
- `player_id` (optional, UUID): filter by a specific player.
- `page`, `size` (optional): pagination (if applied).

**Response (200 OK)**

```json
[
  {
    "id": "UUID",
    "player_id": "UUID",
    "game_id": "UUID",
    "sequence_index": 8,
    "total_points": 150,
    "created_at": "OffsetDateTime"
  }
]
```

**Errors**

- `400 Bad Request` if `from` is missing or invalid.

---

## 4) Store a new game

**POST** `/api/games`

**Request Body**

```json
{
  "player1_id": "UUID",
  "player2_id": "UUID",
  "player3_id": "UUID",
  "main_player_id": "UUID",
  "bid_value": 50,
  "score": 92,
  "played_at": "OffsetDateTime"
}
```

**Responses**

- `201 Created` with body:
  ```json
  {
    "id": "UUID",
    "player1_id": "UUID",
    "player2_id": "UUID",
    "player3_id": "UUID",
    "main_player_id": "UUID",
    "bid_value": 50,
    "score": 92,
    "played_at": "OffsetDateTime"
  }
  ```
  `Location: /api/games/{id}`

**Errors**

- `400 Bad Request` for validation errors (e.g., overlapping players, invalid bid/score, missing
  fields).
- `404 Not Found` if any referenced player ID does not exist.

---

## 5) Load a specific game

**GET** `/api/games/{game_id}`

**Path Params**

- `game_id` (UUID) — the game identifier.

**Response (200 OK)**

```json
{
  "id": "UUID",
  "player1_id": "UUID",
  "player2_id": "UUID",
  "player3_id": "UUID",
  "main_player_id": "UUID",
  "bid_value": 50,
  "score": 92,
  "played_at": "OffsetDateTime"
}
```

**Errors**

- `404 Not Found` if the game does not exist.

---

## Error Response Shape (minimal)

```json
{
  "error": "string (machine readable)",
  "message": "human readable message",
  "field": "optional field or comma-separated fields"
}
```
