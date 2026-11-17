#!/usr/bin/env python3
"""Works out how to crop one store screenshot from the layout the screenshot test saved next to it.

The capture is the app's window only, so behind the status bar and a gesture bar there is plain
paper. Portrait drops the system bar areas and keeps the full width, and fit_screenshot.html
widens it to Play's 2:1 limit. Landscape keeps the full height but stays inside the side insets,
where a 3-button navigation bar draws its backdrop. It is centred on the game: the crop is
symmetric around the boards and fleets, as wide as the screen and the 2:1 limit allow, so there
is the same amount of paper on both sides.

Usage: crop_screenshot.py <layout.json>
Prints the fit_screenshot.html query string and the output size ("W,H").
"""
import json
import math
import sys


def crop(layout):
    width, height = layout["width"], layout["height"]
    insets, content = layout["insets"], layout["content"]
    if width <= height:
        left, top = insets["left"], insets["top"]
        right, bottom = width - insets["right"], height - insets["bottom"]
    else:
        centre = (content["left"] + content["right"]) / 2
        half = min(centre - insets["left"], width - insets["right"] - centre, height)
        left, top = math.ceil(centre - half), 0
        right, bottom = math.floor(centre + half), height
    if (content["left"] < left or content["right"] > right
            or content["top"] < top or content["bottom"] > bottom):
        raise ValueError(f"content {content} doesn't fit the crop "
                         f"left={left} top={top} right={right} bottom={bottom}")
    crop_width, crop_height = right - left, bottom - top
    # The same widening rule as fit_screenshot.html.
    out_width = math.ceil(crop_height / 2) if crop_height > 2 * crop_width else crop_width
    trims = dict(top=top, bottom=height - bottom, left=left, right=width - right)
    return trims, (out_width, crop_height)


def fit_args(layout):
    trims, (out_width, out_height) = crop(layout)
    return "&".join(f"{key}={value}" for key, value in trims.items()) + f" {out_width},{out_height}"


if __name__ == "__main__":
    with open(sys.argv[1]) as f:
        try:
            print(fit_args(json.load(f)))
        except ValueError as error:
            sys.exit(f"{sys.argv[1]}: {error}")
