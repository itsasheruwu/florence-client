/*
 * This file is part of the Florence Client distribution.
 * Copyright (c) Florence Development.
 */

package florencedevelopment.florenceclient.gui.design;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorsTest {
    @Test
    void packsAndUnpacksChannels() {
        int color = Colors.argb(10, 20, 30, 40);

        assertEquals(10, Colors.red(color));
        assertEquals(20, Colors.green(color));
        assertEquals(30, Colors.blue(color));
        assertEquals(40, Colors.alpha(color));
    }

    @Test
    void channelsAreClamped() {
        int color = Colors.argb(300, -5, 255, 1000);

        assertEquals(255, Colors.red(color));
        assertEquals(0, Colors.green(color));
        assertEquals(255, Colors.alpha(color));
    }

    @Test
    void hexIsOpaque() {
        assertEquals(0xFF112233, Colors.hex(0x112233));
        assertEquals(0xFF112233, Colors.hex(0xAA112233));
    }

    @Test
    void withAlphaKeepsTheColor() {
        int color = Colors.withAlpha(Colors.rgb(1, 2, 3), 128);

        assertEquals(128, Colors.alpha(color));
        assertEquals(1, Colors.red(color));
        assertEquals(3, Colors.blue(color));
    }

    @Test
    void mulAlphaScalesOnlyTheAlpha() {
        int color = Colors.mulAlpha(Colors.argb(9, 8, 7, 200), 0.5);

        assertEquals(100, Colors.alpha(color));
        assertEquals(9, Colors.red(color));
    }

    @Test
    void lerpEndsMatchTheInputs() {
        int a = Colors.argb(0, 0, 0, 0);
        int b = Colors.argb(200, 100, 50, 255);

        assertEquals(a, Colors.lerp(a, b, 0));
        assertEquals(b, Colors.lerp(a, b, 1));
        assertEquals(Colors.argb(100, 50, 25, 128), Colors.lerp(a, b, 0.5));
    }

    @Test
    void lightenAndDarkenKeepAlpha() {
        int color = Colors.argb(100, 100, 100, 77);

        assertEquals(77, Colors.alpha(Colors.lighten(color, 0.5)));
        assertEquals(77, Colors.alpha(Colors.darken(color, 0.5)));
        assertEquals(255, Colors.red(Colors.lighten(color, 1)));
        assertEquals(0, Colors.red(Colors.darken(color, 1)));
    }

    @Test
    void hsvPrimaries() {
        assertEquals(Colors.rgb(255, 0, 0), Colors.fromHsv(0, 1, 1, 255));
        assertEquals(Colors.rgb(0, 255, 0), Colors.fromHsv(1 / 3.0, 1, 1, 255));
        assertEquals(Colors.rgb(0, 0, 255), Colors.fromHsv(2 / 3.0, 1, 1, 255));
        assertEquals(Colors.rgb(255, 255, 255), Colors.fromHsv(0.4, 0, 1, 255));
        assertEquals(Colors.rgb(0, 0, 0), Colors.fromHsv(0.4, 1, 0, 255));
    }

    @Test
    void hsvRoundTrips() {
        int[] colors = {Colors.rgb(255, 128, 0), Colors.rgb(12, 200, 99), Colors.rgb(90, 20, 180), Colors.rgb(40, 40, 40)};

        for (int color : colors) {
            double[] hsv = Colors.toHsv(color);
            int back = Colors.fromHsv(hsv[0], hsv[1], hsv[2], 255);

            assertEquals(Colors.red(color), Colors.red(back), 1);
            assertEquals(Colors.green(color), Colors.green(back), 1);
            assertEquals(Colors.blue(color), Colors.blue(back), 1);
        }
    }

    @Test
    void contrastMatchesWcagReferenceValues() {
        assertEquals(21, Colors.contrast(Colors.rgb(0, 0, 0), Colors.rgb(255, 255, 255)), 1e-9);
        assertEquals(1, Colors.contrast(Colors.rgb(80, 80, 80), Colors.rgb(80, 80, 80)), 1e-9);
        // #767676 on white is the classic example of the lightest gray that still passes AA for normal text
        assertEquals(4.54, Colors.contrast(Colors.hex(0x767676), Colors.rgb(255, 255, 255)), 0.01);
    }
}
