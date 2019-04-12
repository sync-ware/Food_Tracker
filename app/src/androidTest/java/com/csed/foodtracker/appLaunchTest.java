package com.csed.foodtracker;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;


@RunWith(AndroidJUnit4.class)
public class appLaunchTest {

    private List<Recipe> expectedList;
    private int expectedListLength;
    private Context getContext;

    @Rule
    public ActivityTestRule<ViewRecipeActivity> recipeActivityActivityTestRule
            = new ActivityTestRule<>(ViewRecipeActivity.class);
    @Rule
    public ActivityTestRule<MainActivity> activityRule
            = new ActivityTestRule<>(MainActivity.class);
    @Rule
    public IntentsTestRule<MainActivity> viewRecipeActivityIntentsTestRule =
            new IntentsTestRule<>(MainActivity.class);

    @Before
    public void initValidData() {
        System.out.println("Hi");
    }


    @Test
    public void changeText_sameActivity() {
        MainActivity main = activityRule.getActivity();
        View viewBy = main.findViewById(R.id.recipe_recyclerview);
        RecyclerView aa = (RecyclerView) viewBy;
        RecyclerView.Adapter adapter = aa.getAdapter();
        assertThat(adapter, notNullValue());
        assertThat("Fail",adapter.getItemCount() > 0);
//        onView(withId(R.id.recipe_recyclerview)).perform(click());
        aa.findViewHolderForAdapterPosition(0).itemView.callOnClick();
        Intents.init();
        intended(hasComponent(ViewRecipeActivity.class.getName()));
/*        onView(withId(2131296438)).perform(RecyclerViewActions.actionOnItemAtPosition(0,click()));
        intended(hasComponent(ViewRecipeActivity.class.getName()));*/
        //        Apparently clicks on an item

    }
}
