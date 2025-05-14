# FIX Viewer Plugin for IntelliJ

<!-- Plugin description -->
This is a simple intellij plugin for viewing FIX (.fix) files.
<!-- Plugin description end -->

The **FIX Plugin for IntelliJ** enhances developer productivity and accuracy when working with Financial Information eXchange 
(FIX) protocol files. Designed for financial engineers, developers, and analysts, this plugin provides rich syntax highlighting 
for `.fix` files, making complex trade messages easier to read and debug. Each field and value is colour-coded for clarity, 
with support for message types, tags, values, and separators, helping users spot issues quickly.

## Features

- **Syntax highlighting** for FIX messages with clear colour-coding of tags, values, and delimiters
- **Live checksum validation and highlighting** for the `10=` tag
- **Quick-fix actions** to auto-correct invalid checksums

## Coming Soon

- **Tooltips** showing tag descriptions (e.g., `35=8` → *Execution Report*)
- **Support for custom field delimiters** (`|` or SOH)
- **Structural parsing and navigation** of FIX fields
- **Bespoke Colours** for fix file coding

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

