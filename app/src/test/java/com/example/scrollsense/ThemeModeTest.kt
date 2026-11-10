package com.example.scrollsense

import com.example.scrollsense.data.preferences.UserPreferences
import com.example.scrollsense.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun themeMode_defaultIsDark() {
        val defaultPrefs = UserPreferences()
        assertEquals(ThemeMode.DARK, defaultPrefs.themeMode)
    }

    @Test
    fun themeMode_labelsMatchRequirements() {
        assertEquals("Dark", ThemeMode.DARK.label)
        assertEquals("Light", ThemeMode.LIGHT.label)
        assertEquals("System Default", ThemeMode.SYSTEM.label)
    }

    @Test
    fun themeMode_entriesContainAllThreeOptions() {
        val entries = ThemeMode.entries
        assertEquals(3, entries.size)
        assertEquals(listOf(ThemeMode.DARK, ThemeMode.LIGHT, ThemeMode.SYSTEM), entries)
    }
}
