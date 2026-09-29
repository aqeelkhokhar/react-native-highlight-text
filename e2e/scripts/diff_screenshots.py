"""Pixel-diff two folders of screenshots taken by e2e/flows/regression.yaml.

Usage: python3 e2e/scripts/diff_screenshots.py <base_dir> <new_dir> <out_dir>

Prints the number/percentage of differing pixels per screen (threshold 24/255)
and writes a side-by-side image (base | new | differences in red) to <out_dir>
for every screen that differs. Needs Pillow (pip install pillow).
"""
import sys, os
from PIL import Image, ImageChops
base, new, out = sys.argv[1:4]
os.makedirs(out, exist_ok=True)
print(f"{'screen':24} {'size':12} {'diff px':>9} {'diff %':>8}  bbox")
for f in sorted(os.listdir(base)):
    if not f.endswith('.png'): continue
    a = Image.open(os.path.join(base, f)).convert('RGB')
    pb = os.path.join(new, f)
    if not os.path.exists(pb): print(f"{f:24} MISSING in new"); continue
    b = Image.open(pb).convert('RGB')
    if a.size != b.size: print(f"{f:24} SIZE MISMATCH {a.size} vs {b.size}"); continue
    d = ImageChops.difference(a, b).convert('L').point(lambda v: 255 if v > 24 else 0)
    pixels = d.get_flattened_data() if hasattr(d, "get_flattened_data") else d.getdata()
    n = sum(1 for v in pixels if v)
    pct = 100 * n / (a.size[0] * a.size[1])
    print(f"{f:24} {a.size[0]}x{a.size[1]:<6} {n:9d} {pct:7.3f}%  {d.getbbox()}")
    if n:
        # side by side: base | new | diff(red)
        red = Image.new('RGB', a.size, (255, 0, 0))
        overlay = Image.composite(red, b.point(lambda v: v // 3 + 170), d)
        sbs = Image.new('RGB', (a.size[0] * 3, a.size[1]), 'white')
        sbs.paste(a, (0, 0)); sbs.paste(b, (a.size[0], 0)); sbs.paste(overlay, (a.size[0] * 2, 0))
        sbs.save(os.path.join(out, f))
