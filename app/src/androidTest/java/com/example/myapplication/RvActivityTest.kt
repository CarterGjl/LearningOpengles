package com.example.myapplication

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.After
import org.junit.Before
import org.junit.Test

class RvActivityTest {

    private lateinit var idlingResource : IdlingResource
    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun startActivity() {
        var launch = ActivityScenario.launch(RvActivity::class.java)
        launch.onActivity {
//            idlingResource = it.getIdlingResource()
        }
//        onView(withId(R.id.et_name)).perform(typeText("wanandroid"), closeSoftKeyboard())
//        onView(withId(R.id.et_pwd)).perform(typeText("123456"), closeSoftKeyboard())
//        onView(withId(R.id.btn_login)).perform(click())

    }
}