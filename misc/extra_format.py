"""Apply repository-specific formatting rules to files in the Java agent."""

from pathlib import Path


SORTED_BLOCK_START = "// SORTED BLOCK START"
SORTED_BLOCK_END = "// SORTED BLOCK END"
AGENT_DIR = Path(__file__).resolve().parents[1] / "agent"


def sort_marked_blocks(contents, path):
    lines = contents.splitlines(keepends=True)
    block_start = None

    for index, line in enumerate(lines):
        marker = line.strip()
        if marker == SORTED_BLOCK_START:
            if block_start is not None:
                raise ValueError(f"{path}: nested sorted block at line {index + 1}")
            block_start = index + 1
        elif marker == SORTED_BLOCK_END:
            if block_start is None:
                raise ValueError(f"{path}: sorted block end without start at line {index + 1}")
            lines[block_start:index] = sorted(lines[block_start:index])
            block_start = None

    if block_start is not None:
        raise ValueError(f"{path}: sorted block start without end at line {block_start}")

    return "".join(lines)


def main():
    updates = []

    for path in sorted(AGENT_DIR.rglob("*")):
        if not path.is_file() or path.is_symlink():
            continue

        try:
            contents = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue

        if SORTED_BLOCK_START not in contents and SORTED_BLOCK_END not in contents:
            continue

        formatted = sort_marked_blocks(contents, path)
        if formatted != contents:
            updates.append((path, formatted))

    for path, contents in updates:
        path.write_text(contents, encoding="utf-8")
        print(f"Sorted blocks in {path.relative_to(AGENT_DIR.parent)}")


if __name__ == "__main__":
    main()
