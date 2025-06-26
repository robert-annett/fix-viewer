# AGENTS.md

This file provides guidance for automated tools (AI agents, Codex, bots) on how to safely and usefully contribute to
this project.

## Project Overview

This is an IntelliJ Platform plugin named `fix-viewer`. It provides advanced views for analyzing FIX (Financial
Information eXchange) messages.

## Allowed Changes

Agents may:

- Refactor code for readability and idiomatic Kotlin/Java
- Add missing Javadoc for public APIs
- Remove unused imports and dead code
- Propose tests or add coverage for uncovered plugin features
- Generate plugin documentation (`README`, `CHANGELOG`, `docs/`)
- Include a description of the change in the Unreleased section of the CHANGELOG.md file
- Suggest UX/UI tweaks for plugin panels (tree/table views)

## Forbidden Changes

Agents should **not**:

- Change FIX protocol semantics or default dictionaries
- Push changes to the `main` branch directly

## Coding Style

- Use full import statements (no wildcard `*`)
- No one-line `if` statements — use curly braces even for single statements
- All public methods must have Javadoc
- Avoid external dependencies unless approved
- Prefer java streams over for loops
- Prefer modern java (version 17)
- Prefer full class names over var unless they are verbose

## Review

All PRs must go through manual review unless tagged as `auto-reviewed`.

---

This document will evolve. Please append guidelines as new capabilities are added.
