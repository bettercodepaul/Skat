package com.skat.backend.application.dto;

public record ErrorResponseTO(
	String error,
	String message,
	String field) {
}
