package com.skat.backend.application;

import org.springframework.stereotype.Service;

/**
 * Simple greeting service for demonstration purposes.
 * This is a pure business logic class that can be unit tested.
 */
@Service
public class GreetingService {

    /**
     * Generates a greeting message.
     *
     * @return the greeting message
     */
    public String generateGreeting() {
        return "Hallo";
    }

    /**
     * Formats a personalized greeting.
     *
     * @param name the name to include in the greeting
     * @return personalized greeting message
     */
    public String generatePersonalizedGreeting(String name) {
        if (name == null || name.trim().isEmpty()) {
            return generateGreeting();
        }
        return "Hallo " + name;
    }
}
