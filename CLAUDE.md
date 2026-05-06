# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
./gradlew buildPlugin          # Build the plugin artifact
./gradlew runIde               # Run the plugin in an IDE sandbox for manual testing
./gradlew test                 # Run unit tests
./gradlew check                # Run all tests + verifyPlugin
./gradlew verifyPlugin         # Verify plugin structure and compatibility
./gradlew runPluginVerifier    # Run IntelliJ Plugin Verifier across target IDE versions

# Run a single test class
./gradlew test --tests com.rannett.fixplugin.util.FixMessageParserTest
```

The lexer (`FixLexer.java`) is generated from `Fix.flex` using the bundled `jflex-1.9.2.jar`. Generated PSI classes live in `src/main/gen/` and are included in the source set.

## Architecture

This is a single-module IntelliJ Platform plugin (Gradle-based, `com.intellij.platform.gradle.plugin` v2, targeting IC 2024.2.6+, Java 17). The plugin provides language support, custom editors, and tooling for FIX protocol messages.

### Language Core

The FIX language pipeline follows the standard IntelliJ PSI model:

- `Fix.flex` → `FixLexer` (generated) → `FixParser` → PSI tree, wired by `FixParserDefinition`
- PSI types: `FixTokenType`, `FixElementType`, `FixTokenSets`; generated impl classes in `src/main/gen/`
- `FixElementFactory` creates new PSI elements programmatically

### Dictionary & Metadata

- `FixTagDictionary` loads FIX field definitions from QuickFIX/J XML dictionaries (bundled in `resources/dictionaries/`)
- `FixDictionaryCache` is a project-level service that caches `FixTagDictionary` instances per FIX version
- `FixViewerSettingsState` persists user-configured dictionary mappings and other settings
- `FixDocumentationProvider` surfaces hover tooltips for tags and enum values using the dictionary

### Inspections & Annotators

- `FixChecksumInspection` + `FixChecksumQuickFix` — validates tag 10 checksum, offers auto-correct
- `FixInvalidCharAnnotator` — flags characters outside valid FIX range
- `FixCheckTypeAnnotator` + `FieldTypeValidator` — validates field values against FIX type specs (INT, CHAR, etc.)

### Custom Editor (FixDualViewEditor)

`FixDualViewEditorProvider` registers a split/tabbed custom editor for `.fix` files. It hosts four views:

1. **Table view** — `FixTransposedTablePanel` + `FixTransposedTableModel`: multi-message grid where columns are messages and rows are tags
2. **Tree view** — `FixMessageTreePanel`: hierarchical group/repeating-group structure
3. **Timeline view** — `FixCommTimelinePanel`: FIX session communication timeline
4. **Text view** — native IntelliJ text editor

### Tool Window

`FixFieldLookupToolWindowFactory` + `FixFieldLookupPanel` provides a right-panel tool window for looking up FIX tag definitions.

### QuickFIX Config Support

Separate language (`QuickFixConfigLanguage`) for `.fix.cfg` / `.quickfix.cfg` files, with its own lexer, parser, file-type detector, and documentation provider. Tooltips are sourced from embedded QuickFIX/J reference data.

### Utilities

- `FixMessageParser` — wraps QuickFIX/J for parsing raw FIX message strings
- `FixUtils` — helper functions for message manipulation
- `FpmlUtils` — detects and handles FpML (XML) embedded in tags 351 and 213
- `FixStringLanguageInjector` — injects FIX language into string literals in Java/Kotlin source

## Coding Style

- Full import statements — no wildcard `*` imports
- Curly braces on all `if` statements, even single-line bodies
- Javadoc required on all public methods
- Prefer Java streams over `for` loops
- Target Java 17; use modern APIs
- Prefer explicit types over `var` unless the type is overly verbose
- No new external dependencies without explicit approval

## Constraints

- Do not change FIX protocol semantics or modify the bundled default dictionaries
- Do not push directly to `main`
- When making changes, add a description to the `Unreleased` section of `CHANGELOG.md`
