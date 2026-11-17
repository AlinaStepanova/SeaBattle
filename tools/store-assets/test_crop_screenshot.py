import unittest

from crop_screenshot import crop, fit_args

NO_INSETS = dict(left=0, top=0, right=0, bottom=0)
# The Pixel 10 with the camera on the left: the game sits right of the screen's centre.
PIXEL_10_LANDSCAPE_INSETS = dict(left=173, top=63, right=0, bottom=63)


def layout(width, height, content, insets=NO_INSETS):
    left, top, right, bottom = content
    return dict(width=width, height=height, insets=insets,
                content=dict(left=left, top=top, right=right, bottom=bottom))


class PortraitTest(unittest.TestCase):

    def test_drops_the_system_bars_and_widens_to_two_to_one(self):
        insets = dict(left=0, top=173, right=0, bottom=63)
        trims, size = crop(layout(1080, 2424, (108, 222, 972, 2166), insets))
        self.assertEqual(trims, dict(top=173, bottom=63, left=0, right=0))
        self.assertEqual(size, (1094, 2188))

    def test_keeps_the_width_when_already_within_two_to_one(self):
        trims, size = crop(layout(1080, 2000, (100, 100, 980, 1900)))
        self.assertEqual(trims, dict(top=0, bottom=0, left=0, right=0))
        self.assertEqual(size, (1080, 2000))

    def test_refuses_content_under_a_system_bar_or_cutout(self):
        insets = dict(left=40, top=173, right=40, bottom=63)
        for edge, content in dict(left=(30, 222, 972, 2166), top=(108, 100, 972, 2166),
                                  right=(108, 222, 1050, 2166), bottom=(108, 222, 972, 2400)).items():
            with self.subTest(edge), self.assertRaises(ValueError):
                crop(layout(1080, 2424, content, insets))


class LandscapeTest(unittest.TestCase):

    def test_centres_on_the_content_at_two_to_one(self):
        trims, size = crop(layout(2424, 1080, (289, 68, 2309, 998), PIXEL_10_LANDSCAPE_INSETS))
        self.assertEqual(trims, dict(top=0, bottom=0, left=219, right=45))
        self.assertEqual(size, (2160, 1080))
        self.assertEqual(289 - trims["left"], (2424 - trims["right"]) - 2309)

    def test_narrows_to_keep_equal_margins_near_a_screen_edge(self):
        trims, size = crop(layout(2424, 1080, (50, 100, 1500, 900)))
        self.assertEqual(trims, dict(top=0, bottom=0, left=0, right=874))
        self.assertEqual(size, (1550, 1080))

    def test_stays_clear_of_a_side_navigation_bar(self):
        for side in ("left", "right"):
            with self.subTest(side):
                insets = dict(NO_INSETS, top=63, **{side: 126})
                trims, size = crop(layout(2400, 1080, (300, 100, 2100, 900), insets))
                self.assertEqual(trims, dict(top=0, bottom=0, left=126, right=126))
                self.assertEqual(size, (2148, 1080))

    def test_refuses_content_wider_than_two_to_one(self):
        with self.assertRaises(ValueError):
            crop(layout(2424, 1080, (100, 100, 2324, 900)))


class FitArgsTest(unittest.TestCase):

    def test_prints_the_query_string_and_size_the_capture_script_reads(self):
        args = fit_args(layout(2424, 1080, (289, 68, 2309, 998), PIXEL_10_LANDSCAPE_INSETS))
        self.assertEqual(args, "top=0&bottom=0&left=219&right=45 2160,1080")


if __name__ == "__main__":
    unittest.main()
