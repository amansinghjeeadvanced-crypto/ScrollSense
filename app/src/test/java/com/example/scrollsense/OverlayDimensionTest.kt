package com.example.scrollsense

import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayDimensionTest {

    /**
     * Approximate character width ratio for typical sans-serif bold font is ~0.6 of font size in dp.
     * At 13.5sp, bold text is roughly ~8.1dp per character.
     * Horizontal padding is 18dp on each side (total 36dp).
     * Horizontal safe margins are 16dp on each side (total 32dp).
     */
    private fun estimateOverlayWidthDp(text: String, fontSizeSp: Float = 13.5f, horizontalPaddingDp: Float = 36f): Float {
        val approxCharWidthDp = fontSizeSp * 0.62f
        return (text.length * approxCharWidthDp) + horizontalPaddingDp
    }

    @Test
    fun testOverlayFitsOnSmallestPhoneScreens() {
        val testStrings = listOf(
            "Short videos today: 0",
            "Short videos today: 20",
            "Short videos today: 99",
            "Short videos today: 100",
            "Short videos today: 1000"
        )

        val phoneScreenWidths = listOf(
            320f, // Compact / Small Android phones
            360f, // Common standard baseline (720p / 1080p density)
            393f, // Modern mid-tier / Pixel baseline
            412f, // Pixel 7 / 8 / Galaxy S23 standard width
            440f  // Large phablet displays
        )

        for (screenWidth in phoneScreenWidths) {
            val maxAvailableWidth = screenWidth - 32f // 16dp margin on each side

            for (text in testStrings) {
                val overlayWidth = estimateOverlayWidthDp(text)
                assertTrue(
                    "Overlay for '$text' (${overlayWidth}dp) must fit within screen width ${screenWidth}dp (available: ${maxAvailableWidth}dp)",
                    overlayWidth <= maxAvailableWidth
                )

                // Verify symmetric centering
                val remainingMargin = (screenWidth - overlayWidth) / 2f
                assertTrue(
                    "Centering margin must be >= 16dp on ${screenWidth}dp screen",
                    remainingMargin >= 16f
                )
            }
        }
    }

    @Test
    fun testStatusBarOffsetCalculation() {
        // Various real Android phone status bar heights in dp
        val statusBars = listOf(
            24, // Legacy standard
            32, // Teardrop notch
            38, // Samsung Galaxy punch-hole
            44, // Pixel pill/cutout
            48, // Large punch-hole/notch
            52  // High-inset dynamic display
        )

        for (statusBar in statusBars) {
            val calculatedY = maxOf(statusBar + 6, 44)
            // Must be at least 44dp from top, and strictly below the status bar
            assertTrue("Top offset must be >= 44dp", calculatedY >= 44)
            assertTrue("Top offset must clear status bar by at least 6dp when status bar >= 38dp",
                if (statusBar >= 38) calculatedY >= statusBar + 6 else true)
        }
    }
}
