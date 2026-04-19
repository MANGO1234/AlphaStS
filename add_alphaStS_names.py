"""
Reads ../sts_monster_move_mapping.json, adds an alphaStS_name field to each entry
by inserting a space before each uppercase letter that follows a lowercase letter,
and writes the enriched JSON to agent/src/main/resources/sts_monster_move_mapping.json.
"""
import json
import re
from pathlib import Path

src = Path(__file__).parent.parent / "sts_monster_move_mapping.json"
dst = Path(__file__).parent / "agent/src/main/resources/sts_monster_move_mapping.json"

with open(src) as f:
    data = json.load(f)

enriched = {}
for key, value in data.items():
    alphaStS_name = re.sub(r'(?<=[a-z])(?=[A-Z])', ' ', key)
    enriched[key] = {"alphaStS_name": alphaStS_name, **value}

dst.parent.mkdir(parents=True, exist_ok=True)
with open(dst, "w") as f:
    json.dump(enriched, f, indent=4)

print(f"Written {len(enriched)} entries to {dst}")
