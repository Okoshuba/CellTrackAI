package com.kubaice.celltrackai.presentation.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object CultureList : Screen("culture_list")

    object CultureDetail : Screen("culture_detail/{cultureId}") {
        fun createRoute(cultureId: String): String = "culture_detail/$cultureId"
    }

    object CreateEditCulture : Screen("create_edit_culture?cultureId={cultureId}") {
        fun createRoute(cultureId: String? = null): String {
            return if (!cultureId.isNullOrEmpty()) {
                "create_edit_culture?cultureId=$cultureId"
            } else {
                "create_edit_culture"
            }
        }
    }

    object CreateEditEvent : Screen("create_edit_event/{cultureId}?eventId={eventId}") {
        fun createRoute(cultureId: String, eventId: String? = null): String {
            return if (!eventId.isNullOrEmpty()) {
                "create_edit_event/$cultureId?eventId=$eventId"
            } else {
                "create_edit_event/$cultureId"
            }
        }
    }

    object LiteratureSearch : Screen("literature_search?cultureId={cultureId}") {
        fun createRoute(cultureId: String? = null): String {
            return if (!cultureId.isNullOrEmpty()) {
                "literature_search?cultureId=$cultureId"
            } else {
                "literature_search"
            }
        }
    }

    object ImagePreview : Screen("image_preview?imageUri={imageUri}") {
        fun createRoute(imageUri: String): String {
            val encoded = Uri.encode(imageUri)
            return "image_preview?imageUri=$encoded"
        }
    }
}
