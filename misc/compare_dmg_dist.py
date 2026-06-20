"""
Compare two damage distribution JSON files produced by STATS_WRITE_DAMAGE_DISTRIBUTION_FILE_SUFFIX.

Usage:
    python compare_dmg_dist.py --dmg_dist_a <file_a> --dmg_dist_b <file_b> --dmg <damage_value>

Each JSON file is a map of damage_value -> occurrence_count (keys are strings).
The script uncompresses both maps into sorted arrays, finds the index range of
the requested damage value in A, and reports statistics for the corresponding
slice of B.
"""

import argparse
import json
from statistics import mean, median


def uncompress_dist(filepath):
    with open(filepath, "r") as f:
        dist = json.load(f)
    arr = []
    for damage, count in sorted(dist.items(), key=lambda x: int(x[0])):
        arr.extend([int(damage)] * int(count))
    return arr


def main():
    parser = argparse.ArgumentParser(description="Compare two damage distributions at a given damage value.")
    parser.add_argument("--dmg_dist_a", required=True, help="Path to damage distribution JSON file A")
    parser.add_argument("--dmg_dist_b", required=True, help="Path to damage distribution JSON file B")
    parser.add_argument("--dmg", type=int, required=True, help="Damage value to look up")
    args = parser.parse_args()

    a_arr = uncompress_dist(args.dmg_dist_a)
    b_arr = uncompress_dist(args.dmg_dist_b)

    if len(a_arr) != len(b_arr):
        print(f"Error: length mismatch — len(a)={len(a_arr)}, len(b)={len(b_arr)}")
        return

    dmg = args.dmg

    if dmg > a_arr[-1]:
        val = b_arr[-1]
        print(f"Note: damage value {dmg} exceeds the maximum in A ({a_arr[-1]}); using last element of B for all stats.")
        print(f"Min:    {val}")
        print(f"Max:    {val}")
        print(f"Mean:   {val:.5f}")
        print(f"Median: {val:.5f}")
        return

    min_idx = None
    max_idx = None
    for i, v in enumerate(a_arr):
        if v == dmg:
            if min_idx is None:
                min_idx = i
            max_idx = i

    if min_idx is None:
        print(f"Error: damage value {dmg} not found in distribution A.")
        return

    sub_b = b_arr[min_idx:max_idx + 1]
    print(f"Damage {dmg} spans indices [{min_idx}, {max_idx}] in A ({max_idx - min_idx + 1} samples).")
    print(f"B subarray stats:")
    print(f"  Min:    {min(sub_b)}")
    print(f"  Max:    {max(sub_b)}")
    print(f"  Mean:   {mean(sub_b):.5f}")
    print(f"  Median: {median(sub_b):.5f}")


if __name__ == "__main__":
    main()
