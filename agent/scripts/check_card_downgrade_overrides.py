#!/usr/bin/env python3
from pathlib import Path
import re
import sys


CARD_DIR = Path(__file__).resolve().parents[1] / "src" / "main" / "java" / "com" / "alphaStS" / "card"
CLASS_RE = re.compile(
    r"\b(?:public|private|protected)?\s*(?:static\s+)?(?:abstract\s+)?class\s+(\w+)(?:\s+extends\s+([\w.]+))?"
)
METHOD_RE = re.compile(r"\bpublic\s+Card\s+(getUpgrade|getDowngrade)\s*\(")


def strip_line_comment(line):
    return line.split("//", 1)[0]


def scan_file(path):
    records = []
    stack = []
    pending_classes = []
    lines = path.read_text().splitlines()

    for line_no, line in enumerate(lines, 1):
        code = strip_line_comment(line)
        pending_classes.extend(
            (match.group(1), (match.group(2) or "").split(".")[-1], line_no)
            for match in CLASS_RE.finditer(code)
        )

        for _ in range(code.count("{")):
            if pending_classes:
                name, extends_name, class_line = pending_classes.pop(0)
                stack.append({
                    "name": name,
                    "extends": extends_name,
                    "line": class_line,
                    "brace_depth": 1,
                    "methods": set(),
                })
            elif stack:
                stack[-1]["brace_depth"] += 1

        method_match = METHOD_RE.search(code)
        if method_match and stack:
            stack[-1]["methods"].add(method_match.group(1))

        for _ in range(code.count("}")):
            if not stack:
                continue
            stack[-1]["brace_depth"] -= 1
            if stack[-1]["brace_depth"] == 0:
                records.append(stack.pop())

    return records


def main():
    missing = []
    records_by_file = {}
    for path in sorted(CARD_DIR.glob("Card*.java")):
        records_by_file[path] = scan_file(path)

    for path, records in records_by_file.items():
        records_by_name = {record["name"]: record for record in records}
        missing_by_name = {}
        for record in records:
            methods = record["methods"]
            if record["name"].endswith("P") and "getUpgrade" in methods and "getDowngrade" not in methods:
                missing_by_name[record["name"]] = record
            if not record["name"].endswith("P") and "getUpgrade" in methods:
                upgraded = records_by_name.get(record["name"] + "P")
                if upgraded and "getDowngrade" not in upgraded["methods"]:
                    missing_by_name[upgraded["name"]] = upgraded
                if record["name"].startswith("_"):
                    for upgraded in records:
                        if upgraded["name"].endswith("P") and upgraded["extends"] == record["name"]:
                            if "getDowngrade" not in upgraded["methods"]:
                                missing_by_name[upgraded["name"]] = upgraded
        missing.extend((path, record["line"], name) for name, record in missing_by_name.items())

    if missing:
        for path, line_no, class_name in sorted(missing):
            print(f"{path}:{line_no}: {class_name} overrides getUpgrade() but not getDowngrade()")
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
