package ru.kkuzmichev.simpleappforespresso;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasData;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertEquals;

import android.Manifest;
import android.content.Context;
import android.os.Environment;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import io.qameta.allure.android.runners.AllureAndroidJUnit4;
import io.qameta.allure.kotlin.Allure;
import ru.kkuzmichev.simpleappforespresso.matchers.ItemVisibleMatcher;
import ru.kkuzmichev.simpleappforespresso.matchers.SizeMatcher;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AllureAndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> activityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResources() {
        IdlingRegistry.getInstance().register(EspressoIdlingResources.idlingResource);
    }

    @After
    public void unregisterIdlingResources() { //Отключаемся от “счетчика”
        IdlingRegistry.getInstance().unregister(EspressoIdlingResources.idlingResource);
    }

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void failed(Throwable e, org.junit.runner.Description description) {
            String className = description.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            String methodName = description.getMethodName();
            takeScreenshot(className + "#" + methodName);
        }
    };

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule
            .grant(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            );

    @Test
    public void useAppContext() {
        Allure.feature("useAppContext");
        // Context of the app under test.
        Context appContext = getInstrumentation().getTargetContext();
        assertEquals("ru.kkuzmichev.simpleappforespresso", appContext.getPackageName());
    }

    @Test
    public void checkHomeFragmentText() {
        Allure.feature("HomeFragmentText");
        // failed test
        ViewInteraction mainText = onView(
                withId(R.id.text_home)
        );

        mainText.check(
                matches(
                        withText("This is home text")
                )
        );
    }

    @Test
    public void checkButtonIsDisplayed() {
        Allure.feature("ButtonDisplayed");
        // successful test
        ViewInteraction button = onView(
                withId(R.id.fab)
        );

        button.perform(click()).check(matches(isDisplayed()));
    }

    @Test
    public void testIntent() {
        Allure.feature("testIntent");
        Intents.init();
        try {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        } catch (Exception ignored) {
        }
        onView(anyOf(withText(R.string.action_settings), withId(R.id.action_settings))).perform(click());
        Intents.intended(hasData("https://google.com"));
        Intents.release();
    }

    @Test
    public void testGalleryList() {
        Allure.feature("testGalleryList");
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_gallery)).perform(click());
        onView(withId(R.id.recycle_view)).check(ViewAssertions.matches(SizeMatcher.of(10)));
    }

    @Test
    public void testItemElementDisplayed() {
        Allure.feature("testItemElementDisplayed");
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
        onView(withId(R.id.nav_gallery)).perform(click());
        ViewInteraction recyclerView = onView(withId(R.id.recycle_view));
        recyclerView.check(matches(new ItemVisibleMatcher(RecyclerView.class, 0)));
    }

    private void takeScreenshot(String name) {
        File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/screenshots/");
        if (!path.exists()) {
            path.mkdirs();
        }
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        String filename = name + ".png";
        device.takeScreenshot(new File(path, filename));
        try {
            Allure.attachment(filename, new FileInputStream(new File(path, filename)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}