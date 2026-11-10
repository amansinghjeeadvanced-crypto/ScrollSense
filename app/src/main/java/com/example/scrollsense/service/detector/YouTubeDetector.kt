package com.example.scrollsense.service.detector

import android.view.accessibility.AccessibilityNodeInfo

class YouTubeDetector : ShortFormDetector {

    override val targetPackage: String = "com.google.android.youtube"

    // View ID fragments that uniquely belong to YouTube Shorts containers
    private val knownShortsViewIds = listOf(
        "reel_player_page",
        "reel_player_page_container",
        "reel_player_view",
        "shorts_container",
        "reel_recycler",
        "reel_watch_fragment",
        "shorts_player_fragment",
        "reel_pivot_button",
        "reel_sound_pivot",
        "reel_channel_name",
        "reel_video_title",
        "reel_action_menu",
        "reel_player",
        "reel_view_pager",
        "shorts_video_layout"
    )

    // Keywords that indicate Shorts vertical overlay controls
    private val knownShortsKeywords = listOf(
        "dislike this video",
        "dislike this short",
        "create a short using this sound",
        "sound used",
        "original sound",
        "use this sound",
        "sound details",
        "remix"
    )

    // Markers of normal YouTube videos (to reject normal 16:9 videos)
    private val normalVideoMarkers = listOf(
        "seek slider",
        "save to playlist",
        "download video",
        "clip this video"
    )

    override fun isShortFormVisible(rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        var shortsViewIdFound = false
        var shortsKeywordCount = 0
        var normalVideoMarkerFound = false

        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.add(Pair(rootNode, 0))
        var visitedCount = 0

        while (queue.isNotEmpty() && visitedCount < 250) {
            val (node, depth) = queue.removeFirst()
            visitedCount++

            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            val text = node.text?.toString()?.lowercase() ?: ""

            // Ignore bottom navigation bar "Shorts" tab
            val isBottomBar = viewId.contains("pivot_bar") ||
                    viewId.contains("bottom_navigation") ||
                    viewId.contains("tab_bar")

            if (!isBottomBar) {
                for (idPart in knownShortsViewIds) {
                    if (viewId.contains(idPart)) {
                        shortsViewIdFound = true
                        break
                    }
                }

                for (keyword in knownShortsKeywords) {
                    if (desc.contains(keyword) || text.contains(keyword)) {
                        shortsKeywordCount++
                        break
                    }
                }
            }

            // Check normal video markers
            for (normalMarker in normalVideoMarkers) {
                if (desc.contains(normalMarker) || text.contains(normalMarker) || viewId.contains("seek_bar")) {
                    normalVideoMarkerFound = true
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

        // If normal seek slider or download button is present and no shorts container, reject
        if (normalVideoMarkerFound && !shortsViewIdFound) {
            return false
        }

        return shortsViewIdFound || shortsKeywordCount >= 2
    }

    override fun extractItemFingerprint(rootNode: AccessibilityNodeInfo?): String {
        if (rootNode == null) return ""

        var channelName = ""
        var captionText = ""
        var soundText = ""

        val queue = ArrayDeque<Pair<AccessibilityNodeInfo, Int>>()
        queue.add(Pair(rootNode, 0))
        var visited = 0

        while (queue.isNotEmpty() && visited < 200) {
            val (node, depth) = queue.removeFirst()
            visited++

            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val text = node.text?.toString()?.trim() ?: ""
            val desc = node.contentDescription?.toString()?.trim() ?: ""

            // Extract channel handle/name
            if (channelName.isEmpty() && text.startsWith("@") && text.length in 2..40) {
                channelName = text
            } else if (channelName.isEmpty() && viewId.contains("channel_name") && text.isNotEmpty()) {
                channelName = text
            }

            // Extract title / caption
            if (captionText.isEmpty()) {
                if (viewId.contains("title") || viewId.contains("caption") || viewId.contains("description")) {
                    if (text.length > 3) captionText = text
                    else if (desc.length > 3) captionText = desc
                } else if (text.contains("#") || text.length > 25) {
                    if (!text.startsWith("@") && !text.contains("comment") && !text.contains("subscribe")) {
                        captionText = text.take(80)
                    }
                }
            }

            // Extract sound info
            if (soundText.isEmpty()) {
                if (viewId.contains("sound") || viewId.contains("music") || viewId.contains("pivot")) {
                    if (text.isNotEmpty()) soundText = text
                    else if (desc.isNotEmpty()) soundText = desc
                } else if (desc.lowercase().contains("sound used") || desc.lowercase().contains("original sound")) {
                    soundText = desc.take(50)
                }
            }

            if (depth < 18) {
                for (i in 0 until node.childCount) {
                    val child = node.getChild(i) ?: continue
                    queue.add(Pair(child, depth + 1))
                }
            }
        }

        val combined = "$channelName|$captionText|$soundText".trim()
        return if (combined == "||") "" else combined
    }

    override fun onAppLeft() {
        // State is centralized in ScrollSenseAccessibilityService
    }
}
