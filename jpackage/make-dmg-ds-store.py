#!/usr/bin/env python3
"""Generate a .DS_Store describing the DMG Finder window layout.

This creates the Finder metadata needed for the conventional macOS
"drag the app onto Applications" DMG presentation without requiring Finder
or a WindowServer session.

The .DS_Store is written directly into the DMG staging directory before the
disk image is created, making the layout deterministic on headless CI runners.

Depends on:
    ds_store
"""

import argparse
import sys


def build(output, volume_width, volume_height, icon_size, positions):
    from ds_store import DSStore

    # Finder stores the window bounds as:
    # "{{left, top}, {width, height}}"
    #
    # The screen-relative origin is not particularly important for a DMG;
    # the window dimensions are what matter most.
    left, top = 200, 120
    window_bounds = "{{%d, %d}, {%d, %d}}" % (
        left,
        top,
        volume_width,
        volume_height,
    )

    # Finder window settings.
    bwsp = {
        "WindowBounds": window_bounds,
        "ShowStatusBar": False,
        "ShowToolbar": False,
        "ShowTabView": False,
        "ShowPathbar": False,
        "ShowSidebar": False,
        "ContainerShowSidebar": False,
        "PreviewPaneVisibility": False,
        "SidebarWidth": 180,
    }

    # Finder icon-view settings.
    #
    # Keep this close to the structure written by dmgbuild. In particular,
    # iconSize is a folder-level icon-view setting rather than a per-item
    # property like Iloc.
    icvp = {
        "viewOptionsVersion": 1,
        "backgroundType": 0,
        "backgroundColorRed": 1.0,
        "backgroundColorGreen": 1.0,
        "backgroundColorBlue": 1.0,
        "gridOffsetX": 0.0,
        "gridOffsetY": 0.0,
        "gridSpacing": 80.0,
        "arrangeBy": "none",
        "showIconPreview": False,
        "showItemInfo": False,
        "labelOnBottom": True,
        "textSize": 12.0,
        "iconSize": float(icon_size),
        "scrollPositionX": 0.0,
        "scrollPositionY": 0.0,
    }

    with DSStore.open(output, "w+") as d:
        # Folder/window metadata.
        d["."]["vSrn"] = ("long", 1)
        d["."]["bwsp"] = bwsp
        d["."]["icvp"] = icvp

        # Tell Finder that this directory should open in icon view.
        #
        # dmgbuild uses the "icvl" record here. This is preferable to the
        # previous "vstl" record for the DMG's default Finder presentation.
        d["."]["icvl"] = (b"type", b"icnv")

        # Per-item icon positions.
        #
        # Finder stores these separately from the folder-level icon size,
        # which is why positioning can work even when icon-view settings
        # are not being applied correctly.
        for name, (x, y) in positions.items():
            d[name]["Iloc"] = (int(x), int(y))


def parse_position(raw):
    """Parse NAME=X,Y into (NAME, (X, Y))."""

    name, separator, coords = raw.partition("=")

    if not separator or not name or not coords:
        raise argparse.ArgumentTypeError(
            "position must be 'Name=X,Y' (got %r)" % raw
        )

    try:
        xs, ys = coords.split(",", 1)
        return name, (int(xs), int(ys))
    except ValueError:
        raise argparse.ArgumentTypeError(
            "position must be 'Name=X,Y' with integer coordinates "
            "(got %r)" % raw
        )


def main(argv):
    parser = argparse.ArgumentParser(description=__doc__)

    parser.add_argument(
        "--output",
        required=True,
        help="path to write .DS_Store",
    )

    parser.add_argument(
        "--window-width",
        type=int,
        default=600,
        help="Finder window width (default: 600)",
    )

    parser.add_argument(
        "--window-height",
        type=int,
        default=400,
        help="Finder window height (default: 400)",
    )

    parser.add_argument(
        "--icon-size",
        type=int,
        default=128,
        help="Finder icon size in points (default: 128)",
    )

    parser.add_argument(
        "--position",
        action="append",
        type=parse_position,
        default=[],
        metavar="NAME=X,Y",
        help="icon position for an item; repeatable",
    )

    args = parser.parse_args(argv)

    if args.window_width <= 0:
        parser.error("--window-width must be greater than zero")

    if args.window_height <= 0:
        parser.error("--window-height must be greater than zero")

    if args.icon_size <= 0:
        parser.error("--icon-size must be greater than zero")

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