package com.skat.backend.api.exception;

public class NotFoundException extends RuntimeException {
	private final String field;

	public NotFoundException(String message) {
		this(message, null);
	}

	public NotFoundException(String message, String field) {
		super(message);
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
