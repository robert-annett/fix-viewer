<!-- Plugin description -->

# FIX Message Viewer Plugin for IntelliJ

This is an intellij plugin for viewing FIX (Financial Information eXchange) (.fix) files.

This plugin provides rich syntax highlighting for FIX messages (`.fix` files), making complex trade messages easier to
read and debug. Each field and value is colour-coded for clarity, with support for message types, tags, values, and
separators, helping users spot issues quickly. Incorrect checksums are highlighted and can be automatically corrected.

Users can also view these messages in a transposed table view, which is much easier that scrolling horizontally. The
same
fields for different messages will be shown in the same row, making comparison easier.

## Features

- **Syntax highlighting** for FIX messages with clear colour-coding of tags, values, and delimiters
- **Live checksum validation and highlighting** for the `10=` tag
- **Quick-fix actions** to auto-correct invalid checksums
- **Annotate invalid characters** when included in a FIX message.
- **Tooltips** showing tag descriptions (e.g., `35=8` → *Execution Report*)
- **Transposed View** to make reading messages easier
- **Enumerated Values** suggested as items in the table view
- **Override Dictionaries** with bespoke ones. Standard Quickfix dictionaries are used. 

## Coming Soon

- **Structural parsing and navigation** of FIX fields
- **Repeating Groups** highlighting and structure

---

## What is a FIX Message?

**FIX (Financial Information eXchange)** is a standardized messaging protocol used globally by financial institutions to
communicate trading information electronically. Originally developed for equities, it is now widely adopted across asset
classes including derivatives, fixed income, and foreign exchange.

A FIX message is a structured string composed of key-value pairs (called *tags*), delimited by a special character
(commonly `|` or ASCII 0x01). Each message conveys a specific function, such as placing an order, acknowledging receipt,
or reporting a trade execution.

### Example FIX Message

    8=FIX.4.4|9=112|35=D|49=CLIENT12|56=BROKER34|55=IBM|54=1|38=100|40=2|10=004

This plugin brings the power of IntelliJ’s developer tooling to this domain, helping users work more confidently and
efficiently with FIX-formatted data.

<!-- Plugin description end -->
