# TextEd

**TextEd** is a command-line text editor inspired by the classic Linux editor [ed](https://www.gnu.org/software/ed/). It provides a simple interface for line-based text manipulation. The editor supports reading and writing files, editing line ranges, regex-based search and replace, and more.

## Features

- Line-based editing in command-line interface
- File loading and saving
- Insert, append, delete, join, move, and copy lines
- Search and replace using regular expressions (with optional flags)
- Block formatting of text
- Undo functionality
- Line addressing with absolute, relative, and shorthand notation

## Getting Started

## Command Syntax

Each command follows this general format:

```
[ADDRESS],[ADDRESS][COMMAND][DESTINATION_ADDRESS] [ARGUMENTS]
```

For example:

```text
1,5d         # delete lines 1 to 5
s/foo/bar/g  # substitute all occurrences of 'foo' with 'bar'
```

Commands can be followed by optional addresses and arguments, depending on their function.

## Line Addressing

You can specify lines using:

### Absolute
- Line numbers (e.g., `1`, `5`)

### Shorthand
- `.` — current line
- `$` — last line
- `,` — from first to last line (`1,$`)

### Relative
- `+n` — n lines below the current line
- `-n` — n lines above the current line

These can be combined for navigation or used as arguments to commands.

## Available Commands

| Command | Description |
|---------|-------------|
| `P`     | Toggle visibility of the `*` prompt indicating command mode. |
| `h`     | Show message for the last error. |
| `H`     | Toggle detailed error messages. |
| `p`     | Print lines. |
| `n`     | Print lines with line numbers. |
| `r`     | Read a file and append its content to memory. |
| `w`     | Write memory content to a file. |
| `f`     | Set or show the current filename. |
| `a`     | Append lines after the addressed line. Ends with a single dot (`.`). |
| `i`     | Insert lines before the addressed line. |
| `d`     | Delete lines in range. |
| `c`     | Replace lines in range with new input (insert mode). |
| `j`     | Join lines in range into one. |
| `m`     | Move lines to a different position. |
| `t`     | Copy lines to a new position. |
| `s/REGEX/REPLACEMENT/FLAGS` | Replace text using regex. |
| `b`     | Align text into a block with optional line width. |
| `u`     | Undo the last change. Can be toggled. |
| `q`     | Quit if no unsaved changes. |
| `Q`     | Force quit without saving. |
| `e`     | Replace current buffer with content of a file (warns if unsaved changes). |
| `E`     | Same as `e` but without warning. |

### Substitute Flags

- `g` — Replace all occurrences on a line
- `i`, `I` — Case-insensitive matching
- `n` — Show line number with output

## Examples

```text
$ r example.txt         # Read file into memory
1,10d                  # Delete lines 1 to 10
$ w output.txt          # Save to output.txt
/Hello/                # Find line containing 'Hello'
s/old/new/g            # Replace all 'old' with 'new'
q                      # Quit
```

## Error Handling

- A simple `?` is printed on errors by default.
- Use `H` to enable detailed error messages.
- Use `h` to view the most recent error message.

## Author

Adam Řeřicha  
Faculty of Mathematics and Physics, Charles University

---

Inspired by the classic [GNU ed](https://www.gnu.org/software/ed/) editor.
