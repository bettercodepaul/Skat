package com.skat.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpsertPlayerRequest(
	@NotBlank(message = "first_name is required")
	@Size(max = 50, message = "first_name must not exceed 50 characters") String first_name,

	@NotBlank(message = "last_name is required")
	@Size(max = 50, message = "last_name must not exceed 50 characters") String last_name) {
}
