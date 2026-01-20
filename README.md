Note that this plugin has been written with the help of OpenAI's codex for adding features and solving some bugs.
This has lead to some inconsistent styling (which is interesting). Please be aware of this when
reading!

<!-- Plugin description -->

# FIX Message Viewer Plugin for IntelliJ

This is an intellij plugin for viewing FIX (Financial Information eXchange) (.fix) files.

This plugin provides rich syntax highlighting for FIX messages (`.fix` files), making complex trade messages easier to
read and debug. Each field and value is colour-coded for clarity, with support for message types, tags, values, and
separators, helping users spot issues quickly. Incorrect checksums are highlighted and can be automatically corrected.

Users can also view these messages in a transposed table view, which is much easier than scrolling horizontally. The
same fields for different messages will be shown in the same row, making comparison easier.

There is also a tree view, to show the message structure, and a communications view to show messages between two sources.

## Features

- **Syntax highlighting** for FIX messages with clear colour-coding of tags, values, and delimiters
- **Live checksum validation and highlighting** for the `10=` tag
- **Quick-fix actions** to autocorrect invalid checksums
- **Annotate invalid characters** when included in a FIX message.
- **Syntax highlighting** for invalid values for FIX types. e.g. INT cannot contain non-numbers and CHAR must be a
  single character.
- **Tooltips** showing tag descriptions (e.g., `35=8` → *Execution Report*)
- **Transposed View** to make reading messages easier including filtering and field selection
- **Tree View** to navigate message structure including groups
- **Message Hiding** in the transposed view for large message files
- **Enumerated Values** suggested as items in the table view
- **Override Dictionaries** with bespoke ones. Standard Quickfix dictionaries are used.
- **Dictionary selector** lists bundled and custom dictionaries per FIX version so you can switch parsing dynamically from the viewers.
- **Dictionary Indicator** shows whether each FIX version uses the default or a modified dictionary directly in the viewers and lets you mark defaults per FIX version.
- **Side-by-side diff viewer** for comparing two messages
- **Language injection** for FIX messages embedded in code strings
- **FpML Detection** for XML embedded in tags 351 and 213
- **Lexer support** for multi-line FpML blocks
- **Unlimited IDE compatibility** so the plugin can install on newer IntelliJ versions without a capped build range
- **Configurable verification IDE** so CI can pin plugin verification to a stable IntelliJ build
- **Verification IDE override property** to control plugin verifier targets independently of compilation
- **Updated Gradle wrapper** to keep CI builds compatible with newer Java runtimes
- **Gradle wrapper Java fallback** to keep builds working when newer JDKs are installed
- **Explicit plugin verifier IDE registration** to keep verification aligned with configured IDE builds
- **Qodana SARIF uploads** so code inspection results appear in GitHub code scanning



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
