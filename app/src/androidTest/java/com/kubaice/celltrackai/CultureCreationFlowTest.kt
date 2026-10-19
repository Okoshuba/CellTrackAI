package com.kubaice.celltrackai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CultureCreationFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCompleteCultureCreationFlow() {
        // Step 1: Verify HomeScreen is displayed
        composeTestRule.onNodeWithText("CellTrack AI").assertIsDisplayed()
        composeTestRule.onNodeWithTag("new_culture_button").assertIsDisplayed()

        // Step 2: Navigate to Create Culture screen
        composeTestRule.onNodeWithTag("new_culture_button").performClick()
        composeTestRule.waitForIdle()

        // Verify CreateEditCultureScreen title
        composeTestRule.onNodeWithText("New Cell Culture").assertIsDisplayed()

        // Step 3: Enter Culture details
        val cultureName = "HEK293 Batch Alpha"
        val cellType = "HEK293"
        val notes = "Passage 5 seeding density 1e6 cells"

        composeTestRule.onNodeWithTag("culture_name_input").performTextInput(cultureName)
        composeTestRule.onNodeWithTag("cell_type_input").performTextInput(cellType)
        composeTestRule.onNodeWithTag("culture_notes_input").performTextInput(notes)

        // Step 4: Save culture
        composeTestRule.onNodeWithTag("save_culture_button").performClick()
        composeTestRule.waitForIdle()

        // Step 5: Verify newly created culture appears on HomeScreen active cultures list
        composeTestRule.onNodeWithText(cultureName).assertIsDisplayed()

        // Step 6: Navigate to All Cultures list screen and verify culture appears there
        composeTestRule.onNodeWithTag("all_cultures_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cell Cultures").assertIsDisplayed()
        composeTestRule.onNodeWithText(cultureName).assertIsDisplayed()

        // Step 7: Click on newly created culture item to open CultureDetailScreen
        composeTestRule.onNodeWithText(cultureName).performClick()
        composeTestRule.waitForIdle()

        // Step 8: Verify newly created culture details are displayed on detail screen
        composeTestRule.onNodeWithText(cultureName).assertIsDisplayed()
        composeTestRule.onNodeWithText(cellType).assertIsDisplayed()
        composeTestRule.onNodeWithText(notes).assertIsDisplayed()
    }
}
