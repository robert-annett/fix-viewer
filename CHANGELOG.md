<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# fix-plugin2 Changelog

## [Unreleased]

### Added

- QuickFIX session config detection with safe content-based heuristics, plus dedicated syntax highlighting.
- QuickFIX session config tooltips sourced from the QuickFIX/J configuration reference.

### Fixed

- Use locale-insensitive FIX type matching so validation stays accurate under non-English locales.
- Avoid returning empty entries when splitting or extracting FIX messages from whitespace-only input.
- Fix QuickFIX config file detection wiring for the IntelliJ 2024.2 file type detector API.
- Fix QuickFIX config detection imports to compile against the IntelliJ platform ByteSequence API.

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

### Fixed
