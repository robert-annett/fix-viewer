Note that this plugin has been written with the help of OpenAI's codex for adding features and solving some bugs.
This has led to some inconsistent styling (which is interesting). Please be aware of this when
reading!

<!-- Plugin description -->

# FIX Message Viewer Plugin for IntelliJ

This is an IntelliJ plugin for viewing FIX (Financial Information eXchange) (`.fix`) files.

This plugin provides rich syntax highlighting for FIX messages, making complex trade messages easier to read and debug.
Each field and value is color-coded for clarity, with support for message types, tags, values, and separators, helping
users spot issues quickly. Incorrect checksums are highlighted and can be automatically corrected.

Users can also view these messages in a transposed table view, which is much easier than scrolling horizontally. The
same fields for different messages will be shown in the same row, making comparison easier.

There is also a tree view, to show the message structure, and a communications view to show messages between two sources.

## Features

### Syntax Highlighting Text View

Full syntax highlighting, with tooltips for fields based on the selected dictionary.
<br>![Syntax highlighting](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/syntax-highlighting.png)
<br>Checksum validation and quick-fix correction.
<br>![checksum](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/checksum-correction.png)

- Color-coded tags, values, message types, and delimiters.
- Live checksum validation for the `10=` tag, with quick-fix correction.
- Invalid characters and invalid FIX type values are highlighted in the editor.
- Tooltips show tag and enum descriptions, for example `35=8` as *Execution Report*.
- Clean log extraction strips non-FIX prefixes and suffixes so wrapped log lines can be inspected as pure FIX messages.

### Transposed Table View

FIX message in the transposed table view.<br>![Table View screenshot placeholder](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/table-view.png)

- Compare multiple FIX messages by aligning the same fields on the same rows.
- Filter fields, select visible columns, and hide messages when working with large files.
- Enumerated values are suggested directly in the table.

### Tree View

Component hierarchy and repeating groups in the tree view.<br>![Tree View screenshot placeholder](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/tree-view.png)

- Navigate message structure by components, repeating groups, and fields.
- Jump from field nodes to the active dictionary definition.

### Message Flow View

Message flow view with direction indicator.<br>![Message Flow screenshot placeholder](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/message-flow.png)

- Inspect message sequences with incoming and outgoing direction indicators.
- Choose a perspective CompID so arrows are calculated relative to a selected `SenderCompID(49)` or `TargetCompID(56)`.
- Use the `Auto` perspective for mixed logs.
- Jump from expanded summary fields to dictionary definitions.

### Dictionary Viewer

View QuickFIX dictionaries with component navigation between fields in messages and the dictionary viewer.<br>![Dictionary Viewer screenshot placeholder](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/dictionary-viewer.png)

- Use bundled QuickFIX dictionaries or configure custom dictionaries per FIX version.
- Switch active dictionaries from the FIX viewers.
- See whether the current FIX version is using a default or custom dictionary.
- Open QuickFIX dictionary XML files in a dedicated FIX Dictionary view.
- Navigate between message fields, component references, and canonical dictionary definitions.
- Ctrl+Click or Go To Declaration from FIX tags jumps to the active dictionary.

### Field Lookup

Field lookup tool window with tag descriptions.<br>![Field Lookup](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/field-lookup.png)

- Search FIX tags and field names without opening a dictionary file.
- Read field descriptions from the FIX 5.0 SP2 phrase data.

### QuickFIX Session Config Editor

Syntax highlighting for QuickFIX sessions.<br>![quickfix sessions](https://raw.githubusercontent.com/robert-annett/fix-viewer/main/docs/images/marketplace/session.png)

- Automatically recognizes common QuickFIX session config filenames such as `*.fix.cfg` and `*.quickfix.cfg`.
- Highlights QuickFIX session config keys and values.
- Shows tooltips for configuration keys, accepted values, and defaults.
- Validates known QuickFIX/J values, including host lists, IP addresses, and IANA timezone IDs without editor-time network lookups.

### Diff and Comparison Tools

- Compare two FIX messages side by side using the IntelliJ diff viewer.
- Clean noisy application logs into focused FIX message content before comparing or inspecting them.

### Embedded FpML and XML

- Detects embedded XML and FpML payloads in `XmlData(213)` and `EncodedSecurityDesc(351)`.
- Keeps multi-line embedded XML payloads together when splitting multi-message FIX logs.
- Avoids invalid-character warnings inside embedded XML payloads.

### FIX in Source Code

- Provides language injection for FIX messages embedded in code strings.

### Supported Protocols and Inputs

- FIX 4.0 through FIX 5.0 SP2.
- FIXT.1.1 transport dictionaries.
- Custom QuickFIX dictionaries.
- QuickFIX session configuration files.
- Multi-message logs, including mixed-quality logs where malformed messages should not block valid ones.

---

## What is a FIX Message?

**FIX (Financial Information eXchange)** is a standardized messaging protocol used globally by financial institutions to
communicate trading information electronically. Originally developed for equities, it is now widely adopted across asset
classes including derivatives, fixed income, and foreign exchange.

A FIX message is a structured string composed of key-value pairs (called *tags*), delimited by a special character
(commonly `|` or ASCII 0x01). Each message conveys a specific function, such as placing an order, acknowledging receipt,
or reporting a trade execution.

### Example FIX Message

```text
8=FIX.4.4|9=112|35=D|49=CLIENT12|56=BROKER34|55=IBM|54=1|38=100|40=2|10=113|
```

This plugin brings the power of IntelliJ's developer tooling to this domain, helping users work more confidently and
efficiently with FIX-formatted data.

<!-- Plugin description end -->
