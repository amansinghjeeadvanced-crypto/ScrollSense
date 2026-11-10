package com.example.scrollsense.service.detector

import android.view.accessibility.AccessibilityNodeInfo

class InstagramDetector : ShortFormDetector {

    override val targetPackage: String = "com.instagram.android"

    // Known Instagram Reel container & element ID suffixes
    private val knownReelsViewIds = listOf(
        "clips_viewer_view_pager",
        "clips_video_container",
        "clips_swipe_refresh_container",
        "reel_viewer_root",
        "clips_overlay_container",
        "reel_viewer_media_container",
        "clips_video_glyph",
        "reel_viewer_tall_media_container",
        "clips_author_name",
        "clips_action_bar",
        "clips_remix_button"
    )

    private val knownReelsKeywords = listOf(
        "remix this reel",
        "original audio",
        "audio by",
        "like reel",
        "like this reel",
        "share reel",
        "send reel"
    )

    // Markers of non-Reels sections to reject
    private val directMessageMarkers = listOf("direct_", "message_composer", "row_thread_")
    private val commentMarkers = listOf("comment_composer", "layout_comment_thread")
    private val profileMarkers = listOf("profile_header", "user_profile")

    override fun isShortFormVisible(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        var reelsViewIdFound = false
        var reelsKeywordCount = 0
        var isDmScreen = false
        var isCommentSheet = false
        var isProfileScreen = false
        var isMainFeedMarkerFound = false

        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.add(Pair(rootNode, 0))
        var visited = 0

        while (queue.isNotEmpty() && visited < 250) {
            val (node, depth) = queue.removeFirst()
            visited++

            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            val text = node.text?.toString()?.lowercase() ?: ""

            // 1. Direct Message check
            if (directMessageMarkers.any { viewId.contains(it) } ||
                text == "message..." || text == "send message..."
            ) {
                isDmScreen = true
                break
            }

            // 2. Comments sheet check
            if (commentMarkers.any { viewId.contains(it) } ||
                (text == "comments" && node.isHeading)
            ) {
                isCommentSheet = true
            }

            // 3. Profile check
            if (profileMarkers.any { viewId.contains(it) } ||
                text == "edit profile" || text == "share profile"
            ) {
                isProfileScreen = true
                break
            }

            // 4. Main Feed check (Bookmark/Save buttons in regular feed posts)
            if (desc.contains("save post") || desc.contains("bookmark") || viewId.contains("row_feed_")) {
                isMainFeedMarkerFound = true
            }

            // 5. Check Reels containers & keywords
            for (idPart in knownReelsViewIds) {
                if (viewId.contains(idPart)) {
                    reelsViewIdFound = true
                    break
                }
            }

            for (keyword in knownReelsKeywords) {
                if (desc.contains(keyword) || text.contains(keyword)) {
                    reelsKeywordCount++
                    break
                }
            }

            if (depth < 20) {
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i) ?: continue
                    queue.add(Pair(child, depth + 1))
                }
            }
        }

        // If in DMs, Comments sheet, or Profile screen, do NOT count as active Reels feed
        if (isDmScreen || isCommentSheet || isProfileScreen) {
            return false
        }

        // If in main feed with bookmark button and no clips container, reject
        if (isMainFeedMarkerFound && !reelsViewIdFound) {
            return false
        }

        return reelsViewIdFound || reelsKeywordCount >= 2
    }

    override fun extractItemFingerprint(rootNode: AccessibilityNodeInfo?): String {
        if (rootNode == null) return ""

        var author = ""
        var caption = ""
        var audio = ""

        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.add(Pair(rootNode, 0))
        var visited = 0

        while (queue.isNotEmpty() && visited < 200) {
            val (node, depth) = queue.removeFirst()
            visited++

            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val text = node.text?.toString()?.trim() ?: ""
            val desc = node.contentDescription?.toString()?.trim() ?: ""

            // Extract author username
            if (author.isEmpty()) {
                if (viewId.contains("author") || viewId.contains("username")) {
                    if (text.isNotEmpty() && text.length in 2..35) author = text
                }
            }

            // Extract caption
            if (caption.isEmpty()) {
                if (viewId.contains("caption") || text.contains("#")) {
                    if (text.length > 5) caption = text.take(60)
                }
            }

            // Extract audio track
            if (audio.isEmpty()) {
                if (viewId.contains("music") || viewId.contains("audio")) {
                    if (text.isNotEmpty()) audio = text.take(40)
                    else if (desc.isNotEmpty()) audio = desc.take(40)
                } else if (desc.lowercase().contains("original audio") || desc.lowercase().contains("audio by")) {
                    audio = desc.take(40)
                }
            }

            if (depth < 18) {
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i) ?: continue
                    queue.add(Pair(child, depth + 1))
                }
            }
        }

        val combined = "$author|$caption|$audio".trim()
        return if (combined == "||") "" else combined
    }

    override fun onAppLeft() {
        // State is centralized in ScrollSenseAccessibilityService
    }
}
