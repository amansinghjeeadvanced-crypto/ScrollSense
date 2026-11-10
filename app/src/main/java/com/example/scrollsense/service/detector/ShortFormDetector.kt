package com.example.scrollsense.service.detector

import android.view.accessibility.AccessibilityNodeInfo

interface ShortFormDetector {
    val targetPackage: String

    /**
     * Inspects accessibility event and node tree to determine
     * if the user is currently watching short-form content (Reels / Shorts).
     */
    fun isShortFormVisible(rootNode: AccessibilityNodeInfo?): Boolean

    /**
     * Extracts a unique composite fingerprint for the short-form video currently visible on screen.
     * Combines creator handle/name, caption/title, and audio information.
     */
    fun extractItemFingerprint(rootNode: AccessibilityNodeInfo?): String

    /**
     * Resets session state when user leaves the protected app or exits the player.
     */
    fun onAppLeft()
}
