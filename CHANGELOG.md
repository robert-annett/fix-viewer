<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# fix-plugin2 Changelog

## [Unreleased]

### Added

- None yet.

### Fixed

- None yet.

## [0.0.22] - 2026-05-13

### Added

- Added split-message coverage for embedded XML data fields (212/213) and multi-message parsing with mixed valid/malformed input.
- Detect QuickFIX dictionary XML files and open a dedicated `FIX Dictionary` file-editor tab alongside the default XML editor.
- Add dictionary-aware navigation for message-level field references to their `<fields>` definitions.

### Fixed

- Upgraded QuickFIX/J core dependency from 2.3.2 to 3.0.0 and updated parser API usage for the new validation-settings signature.
- Replaced removed QuickFIX/J generated field constants with stable FIX tag literals for EncodedSecurityDescLen/EncodedSecurityDesc (350/351).
- QuickFIX session config files with multi-part names such as `*.fix.cfg` and `*.quickfix.cfg` now auto-associate with the QuickFIX Session Config file type.
- Override `FileEditor#getFile()` in the `FIX Dictionary` editor to satisfy IntelliJ's non-deprecated FileEditor contract and prevent runtime PluginException warnings.
- Fix dictionary field-reference PSI targeting so Ctrl+Click navigation now resolves reliably from message-level field `name` values to the canonical field definition.
- Add an explicit dictionary XML `GotoDeclarationHandler` fallback so Ctrl+Click / Go To Declaration works even when PSI reference navigation is not invoked by the editor path.
- Add a right-click editor action (`Go to FIX Field Definition`) for dictionary message/group field references as a manual navigation fallback.
- Improve dictionary navigation robustness by resolving from the nearest parent `XmlAttributeValue`, so Ctrl+Click/right-click works when caret lands on nested XML tokens.
- Add diagnostic logging for dictionary editor acceptance/creation and right-click navigation attempts to aid troubleshooting in IDE logs.
- Raise dictionary editor/navigation diagnostics to warning-level log entries and include goto-handler traces for easier troubleshooting.
- Return XML attribute declaration targets (instead of attribute value leaf nodes) to improve reliability of jump navigation.
- Add caret-offset fallback lookup in goto-declaration handling to recover when IntelliJ reports `XmlTokenImpl` without a direct `XmlAttributeValue` parent.
- Add XmlTag-based fallback recovery in goto-declaration handling to resolve from enclosing message/group `<field>` tags when attribute-value PSI is missing.
- Prioritize caret-near `<field>` tag resolution (excluding `<fields>` definitions) before attribute-value PSI paths to improve Ctrl+Click reliability across dictionary sections.
- Fix right-click `Go to FIX Field Definition` visibility by deriving PSI from the active editor document when popup PSI context is unavailable.
- Remove temporary verbose warning logs from dictionary navigation/editor paths after stabilizing fallback behavior.
- Add dictionary navigation support for component references, resolving `<component name="..."/>` to `<components><component .../>` definitions.
- Fix dictionary XML reference-provider parent-tag checks so message-level `<field name="..."/>` references produce navigation references correctly.
- Tighten dictionary detection to require `<fix>` as the XML document root, reducing false-positive activation in unrelated XML files.
- Resolve static-analysis warnings for nullability override annotations, sentence capitalization in dictionary-view header text, and redundant constant-condition checks.
- Bump `pluginVersion` to `0.0.22` in `gradle.properties` so IntelliJ release pickup matches the changelog release.

## [0.0.21] - 2026-05-06

### Added

- QuickFIX session config detection with safe content-based heuristics, plus dedicated syntax highlighting.
- QuickFIX session config tooltips sourced from the QuickFIX/J configuration reference.
- QuickFIX session config value inspection driven by the QuickFIX/J valid-values metadata.

### Fixed

- Prevent FIX language injection inside Markdown files so README/examples are not parsed as live FIX content.
- Use locale-insensitive FIX type matching so validation stays accurate under non-English locales.
- Avoid returning empty entries when splitting or extracting FIX messages from whitespace-only input.
- Fix QuickFIX config file detection wiring for the IntelliJ 2024.2 file type detector API.
- Fix QuickFIX config detection imports to compile against the IntelliJ platform ByteSequence API.
- Enable QuickFIX session config tooltips by providing PSI parsing for config files.
- Fix QuickFIX session config validation compilation by correcting the DateTimeException import.
- Fix QuickFIX session host/IP validation to accept IPv6 and comma-separated InetAddress-style address lists.
- Harden QuickFIX host/IP validation by removing runtime DNS lookups from editor-time validation.
- Fix timezone validation to consume normalized ZoneId values instead of ignoring the resolver result.
- Refactor QuickFIX value-validator Optional handling to satisfy functional-style static analysis rules.

## [0.0.1]

### Added

- All basic code for colourising FIX files

## [0.0.2]

### Fixed

- SVG logo to be compliant with intellij standards for approval

## [0.0.3]

### Added

- Detection and highlighting of invalid checksums
- Quick-fix actions to auto-correct invalid checksums

## [0.0.4]

### Added

- Syntax highlighting for invalid characters

### Fixed

- Highlighting of checksums for multi-line files

## [0.0.5]

### Added

- Tooltip lookup for TAG and VALUE in the fix messages

## [0.0.6]

### Added

- Transposed view to make inspection of multiple messages easier.

## [0.0.7]

### Added

- Enumerated values suggested in combobox in table view for fix items

## [0.0.8]

### Added

- Bespoke dictionaries can be associated with FIX versions and override the built in ones.

## [0.0.9]

### Added

- Syntax highlighting for invalid values for FIX types. e.g. INT cannot contain non-numbers and CHAR must be a single
  character.
- Ability to hide messages in the table format to make reading multiple messages easier.

## [0.0.10]

### Fixed

- Some build issues and warnings
- Fixed some exceptions when data not found that crashed the plugin.

## [0.0.11]

### Added

- Treeview based on the groups used within a message

## [0.0.12]

### Fixed

- NPE when a holding file was not available for a psi element
- Stopped using an obsolete API
- Correct tree implementation for a plugin

## [0.0.13]

### Added

- Side-by-side comparison of FIX messages using IntelliJ diff viewer
- Display enum descriptions alongside field names in the tree view
- Filtering in the transposed table view and ability to reset filtering and ordering.
- Multi-line FpML fields no longer split messages when parsing
- Invalid character warnings are no longer reported for FpML text in XmlData or
  EncodedSecurityDesc fields.
- Transposed table no longer shrinks columns when many messages are displayed; a horizontal scrollbar appears instead.

## [0.0.14]

### Fixed

- Invalid character warnings are no longer reported for FpML text in XmlData or
  EncodedSecurityDesc fields.

### Added

- Support for extended precision UTCTimestamps with optional trailing `Z`.
- Detection and parsing of FpML in XMLData and EncodedSecurityDesc fields.
- Fixed lexer handling of whitespace inside embedded FpML and added lexer tests.
- Added language injection for FIX messages embedded in code strings
- Added tests for language injection
- Added tests for dictionary caching and additional lexer scenarios
- Added tests for TagFilterDialog, editor provider, element factory, and more lexer cases
- Fixed TagFilterDialog tests to use built-in dictionaries by clearing custom paths
- Display field descriptions in Field Lookup using FIX.5.0SP2 phrases
- Wrapped field descriptions in Field Lookup to avoid horizontal scrolling

## [0.0.15]

### Added

- Added Message Flow view for inspecting message sequences with direction indicators
- Message Flow displays FIX message names alongside type codes
- Message Flow columns reordered to Time, Dir, MsgType, Summary with Time column widened

## [0.0.16]

### Added

- Added Message Flow view for inspecting message sequences with direction indicators
- Message Flow displays FIX message names alongside type codes
- Message Flow columns reordered to Time, Dir, MsgType, Summary with Time column widened

## [0.0.17]

### Fixed

- ensure FixMessageParser.splitMessages only treats 10= occurrences as checksums when they are delimited fields,
  preventing premature splits on values like 110=10.5

## [0.0.18]

### Added

- Display the active dictionary for each open FIX viewer, highlighting modified dictionary locations.
- Manage bundled dictionaries directly in settings, add multiple custom dictionaries per FIX version, and choose the active one from a new in-viewer combobox that re-parses messages instantly.

### Fixed

- Refresh open FIX editors immediately after updating custom dictionary mappings so IDE restarts are no longer required.
- Fix dictionary change event subscription so builds compile with the IntelliJ message bus APIs.
- Replace the dictionary mapping edit dialog with an IntelliJ DialogWrapper implementation to avoid thread context errors
  when updating dictionaries from the settings panel.
- Ensure selecting a new default dictionary in settings clears the previous default indicator for that FIX version.

## [0.0.19]

### Added

- Publish Qodana inspection results to GitHub code scanning with SARIF uploads.

### Fixed

- Remove the maximum IDE compatibility so the plugin can install on newer IntelliJ versions.
- Use the configured IntelliJ platform version for plugin verification to avoid missing IDE artifacts.
- Allow a dedicated plugin verification IDE version so CI can pin to a stable release.
- Fix plugin verification IDE selection wiring so Gradle can compile the build script.
- Update the Gradle wrapper so builds run on newer Java runtimes without failing to parse the version.
- Fallback to Java 17 in the Gradle wrapper when Java 25 is detected to keep builds running.
- Use the non-deprecated plugin verifier IDE registration to avoid selecting missing IDE artifacts.
- Fix Qodana CI execution by using full git history and compatible CLI options.

## [0.0.20]

### Added

- Strip non-FIX log prefixes and suffixes in the FIX editor to leave clean messages when logs wrap the payload.


## [0.0.21]

### Added

- Added QuickFIX session config detection with dedicated syntax highlighting and safe content-based file recognition.
- Added QuickFIX session config key tooltips and valid-value guidance sourced from QuickFIX/J references.
- Added QuickFIX session config value validation enhancements for host/IP lists, timezone normalization, and offline-safe checks.
