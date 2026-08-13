#!/usr/bin/env python3
"""Generate a .DS_Store describing the DMG Finder window layout.

jpackage used to run an AppleScript (DMGSetup.scpt) against Finder to lay the
mounted volume out as the familiar "drag the app onto Applications" window.
That approach needs a live Finder / WindowServer session, which the headless
GitHub-hosted macOS runners (macos-latest, macos-15-intel) don't reliably
provide, so no .DS_Store was ever written and the dmg opened with the default
alphabetical icon arrangement.

Finder persists a window's view mode, size and per-icon positions in a
`.DS_Store` file at the volume root. We can build that file directly - no
Finder required - and drop it into the dmg staging folder before the image is
created, giving a deterministic layout on any runner.

Depends on the `ds_store` and `mac_alias` packages (pure-python, authored by
the same person as Apple's reference `dmgbuild`).
"""

import argparse
import sys


def build(output, volume_width, volume_height, icon_size, positions):
    from ds_store import DSStore

    # WindowBounds is "{{left, top}, {width, height}}" in Finder's string form.
    # Anchor near the top-left of the screen; the size is what actually matters.
    left, top = 200, 120
    window_bounds = "{{%d, %d}, {%d, %d}}" % (left, top, volume_width, volume_height)

    with DSStore.open(output, "w+") as d:
        # Window-level (the container directory, keyed as ".") records.
        d["."]["vSrn"] = ("long", 1)

        # Force the window into icon view. Without this "view style" record
        # Finder opens the volume in whatever its default view is (list/generic
        # folder presentation) and ignores the icon-view options + Iloc icon
        # positions below, so the big app icons never show. 'icnv' == icon view.
        d["."]["vstl"] = ("type", b"icnv")

        d["."]["bwsp"] = {
            "WindowBounds": window_bounds,
            "ShowStatusBar": False,
            "ShowToolbar": False,
            "ShowTabView": False,
            "ShowPathbar": False,
            "ShowSidebar": False,
        }

        d["."]["icvp"] = {
            "viewOptionsVersion": 1,
            "backgroundType": 0,
            "iconSize": float(icon_size),
            "gridSpacing": 100.0,
            "gridOffsetX": 0.0,
            "gridOffsetY": 0.0,
            "arrangeBy": "none",
            "showIconPreview": True,
            "showItemInfo": False,
            "labelOnBottom": True,
            "textSize": 12.0,
        }

        # Per-item icon positions.
        for name, (x, y) in positions.items():
            d[name]["Iloc"] = (int(x), int(y))


def parse_position(raw):
    # Format: "Name=X,Y" - Name may contain spaces but not '='.
    name, _, coords = raw.partition("=")
    if not name or not coords:
        raise argparse.ArgumentTypeError(
            "position must be 'Name=X,Y' (got %r)" % raw
        )
    try:
        xs, ys = coords.split(",")
        return name, (int(xs), int(ys))
    except ValueError:
        raise argparse.ArgumentTypeError(
            "position must be 'Name=X,Y' with integer coords (got %r)" % raw
        )


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, help="path to write .DS_Store")
    parser.add_argument("--window-width", type=int, default=600)
    parser.add_argument("--window-height", type=int, default=400)
    parser.add_argument("--icon-size", type=int, default=128)
    parser.add_argument(
        "--position",
        action="append",
        type=parse_position,
        default=[],
        metavar="NAME=X,Y",
        help="icon position for an item; repeatable",
    )
    args = parser.parse_args(argv)

    positions = dict(args.position)
    if not positions:
        parser.error("at least one --position is required")

    build(
        args.output,
        args.window_width,
        args.window_height,
        args.icon_size,
        positions,
    )
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
