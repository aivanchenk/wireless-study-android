package com.example.wirelesslocationstud.ui.home

import android.util.Log
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.wirelesslocationstud.R
import org.hamcrest.CoreMatchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    private lateinit var scenario: FragmentScenario<HomeFragment>

    @Before
    fun setup() {
        Log.d("HomeFragmentTest", "Setting up test - launching HomeFragment")
        scenario = launchFragmentInContainer<HomeFragment>(themeResId = R.style.Theme_WirelessLocationStud)
    }

    @Test
    fun testSegmentedButtonsExist() {
        Log.d("HomeFragmentTest", "Testing segmented buttons exist")

        // Verify segmented button group exists
        onView(withId(R.id.segmented_button_group))
            .check(matches(isDisplayed()))

        // Verify Canvas button exists
        onView(withId(R.id.button_canvas))
            .check(matches(isDisplayed()))
            .check(matches(withText("Canvas")))

        // Verify Image button exists
        onView(withId(R.id.button_image))
            .check(matches(isDisplayed()))
            .check(matches(withText("Image")))

        Log.d("HomeFragmentTest", "✓ All segmented buttons exist and are displayed")
    }

    @Test
    fun testCanvasTabIsSelectedByDefault() {
        Log.d("HomeFragmentTest", "Testing Canvas tab is selected by default")

        // Verify Canvas button is checked by default
        onView(withId(R.id.button_canvas))
            .check(matches(isChecked()))

        // Verify canvas content is displayed
        onView(withId(R.id.text_content))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("Canvas"))))

        Log.d("HomeFragmentTest", "✓ Canvas tab is selected by default and shows canvas content")
    }

    @Test
    fun testSwitchToImageTab() {
        Log.d("HomeFragmentTest", "Testing switch to Image tab")

        // Click on Image button
        onView(withId(R.id.button_image))
            .perform(click())

        // Verify Image button is now checked
        onView(withId(R.id.button_image))
            .check(matches(isChecked()))

        // Verify image content is displayed
        onView(withId(R.id.text_content))
            .check(matches(withText(containsString("Image"))))

        Log.d("HomeFragmentTest", "✓ Successfully switched to Image tab and shows image content")
    }

    @Test
    fun testSwitchBetweenTabs() {
        Log.d("HomeFragmentTest", "Testing switching between tabs")

        // Initial state - Canvas selected
        onView(withId(R.id.text_content))
            .check(matches(withText(containsString("Canvas"))))
        Log.d("HomeFragmentTest", "  Initial: Canvas content displayed")

        // Switch to Image
        onView(withId(R.id.button_image))
            .perform(click())
        onView(withId(R.id.text_content))
            .check(matches(withText(containsString("Image"))))
        Log.d("HomeFragmentTest", "  After click: Image content displayed")

        // Switch back to Canvas
        onView(withId(R.id.button_canvas))
            .perform(click())
        onView(withId(R.id.text_content))
            .check(matches(withText(containsString("Canvas"))))
        Log.d("HomeFragmentTest", "  After click back: Canvas content displayed")

        Log.d("HomeFragmentTest", "✓ Successfully switched between tabs multiple times")
    }
}

