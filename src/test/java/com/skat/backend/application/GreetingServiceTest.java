package com.skat.backend.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit test for GreetingService following ADR-002 (Unit Testing Strategy with Maven Surefire).
 * Uses Given-When-Then pattern and AssertJ for assertions.
 * No Spring context is loaded, making this a fast unit test.
 */
class GreetingServiceTest {

    @Test
    void given_noInput_when_generateGreeting_then_returnsHallo() {
        // Given
        var service = new GreetingService();

        // When
        String result = service.generateGreeting();

        // Then
        assertThat(result).isEqualTo("Hallo");
    }

    @Test
    void given_validName_when_generatePersonalizedGreeting_then_returnsPersonalizedMessage() {
        // Given
        var service = new GreetingService();
        String name = "World";

        // When
        String result = service.generatePersonalizedGreeting(name);

        // Then
        assertThat(result).isEqualTo("Hallo World");
    }

    @Test
    void given_nullName_when_generatePersonalizedGreeting_then_returnsDefaultGreeting() {
        // Given
        var service = new GreetingService();

        // When
        String result = service.generatePersonalizedGreeting(null);

        // Then
        assertThat(result).isEqualTo("Hallo");
    }

    @Test
    void given_emptyName_when_generatePersonalizedGreeting_then_returnsDefaultGreeting() {
        // Given
        var service = new GreetingService();

        // When
        String result = service.generatePersonalizedGreeting("   ");

        // Then
        assertThat(result).isEqualTo("Hallo");
    }
}
