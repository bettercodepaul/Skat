# ADR-009: Use Google Coding Rules with Exception for Indentation

Date: 2025-10-23

## Status

Accepted

## Context

The project aims to maintain a consistent and widely recognized coding style to improve code
readability and collaboration. Google Coding Rules provide a comprehensive and well-documented
standard for Java development. However, the team prefers using tabs instead of spaces for
indentation to allow developers to customize their viewing preferences in their IDEs.

## Decision

The project will adopt Google Coding Rules as the standard coding style, with the exception of using
tabs (`\t`) instead of spaces for indentation.

## Consequences

- **Positive**:
    - Ensures a consistent and widely recognized coding style.
    - Allows developers to adjust tab width in their IDEs for personal preference.
    - Simplifies onboarding for developers familiar with Google Coding Rules.

- **Negative**:
    - Requires configuring tools and IDEs to enforce the use of tabs.
    - May require additional effort to ensure compliance with the exception.

## Implementation

1. Configure the project to use Google Coding Rules:
    - Add the `google-java-format` plugin to the build system (e.g., Maven):
      ```xml
      <plugin>
          <groupId>com.github.sherter.google-java-format</groupId>
          <artifactId>google-java-format-maven-plugin</artifactId>
          <version>1.15.0</version>
      </plugin>
      ```

2. Modify the formatter configuration to use tabs for indentation:
    - Use the `--aosp` flag or customize the formatter to replace spaces with tabs.

3. Update IDE settings to enforce tabs for indentation:
    - For IntelliJ IDEA:
        - Go to `Preferences > Code Style > Java`.
        - Set "Use tab character" for indentation.

4. Document this decision in the project's coding standards to ensure team-wide adherence.

5. Add a pre-commit hook or CI check to validate compliance with the coding rules.
