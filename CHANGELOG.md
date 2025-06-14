<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# fix-plugin2 Changelog

## [Unreleased]

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