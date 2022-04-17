package org.opendatakit.activites.VerifyServerSettingsActivity;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.consts.IntentConsts;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.services.R;
import org.opendatakit.services.sync.actions.activities.LoginActivity;
import org.opendatakit.services.sync.actions.activities.VerifyServerSettingsActivity;
import org.opendatakit.services.sync.actions.fragments.UpdateServerSettingsFragment;

import java.util.Collections;
import java.util.Map;

public class LoggedOutStateTest {

    private final String TEST_SERVER_URL = "https://testUrl.com";

    private ActivityScenario<VerifyServerSettingsActivity> activityScenario;

    @Before
    public void setUp() {
        String APP_NAME = "testAppName";

        Intent intent = new Intent(getContext(), VerifyServerSettingsActivity.class);
        intent.putExtra(IntentConsts.INTENT_KEY_APP_NAME, APP_NAME);
        activityScenario = ActivityScenario.launch(intent);

        onView(withId(android.R.id.button2)).inRoot(RootMatchers.isDialog()).perform(ViewActions.click());
        activityScenario.onActivity(activity -> {
            PropertiesSingleton props = activity.getProps();
            assertThat(props).isNotNull();

            Map<String, String> serverProperties = UpdateServerSettingsFragment.getUpdateUrlProperties(TEST_SERVER_URL);
            assertThat(serverProperties).isNotNull();
            props.setProperties(serverProperties);

            activity.updateViewModelWithProps();
        });
    }

    @Test
    public void verifyVisibilityTest() {
        onView(withId(R.id.tvUserHeadingVerifySettings)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.tvUsernameVerify)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tvVerificationStatusVerify)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tvLastSyncTimeVerify)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnStartVerifyUser)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

        onView(withId(R.id.btnDrawerOpen)).perform(ViewActions.click());
        onView(withId(R.id.drawer_resolve_conflict)).check(doesNotExist());
        onView(withId(R.id.drawer_switch_sign_in_type)).check(doesNotExist());
        onView(withId(R.id.drawer_update_credentials)).check(doesNotExist());
    }

    @Test
    public void verifyValuesTest() {
        onView(withId(R.id.tvUserHeadingVerifySettings)).check(matches(withText(getContext().getString(R.string.user_logged_out_label))));
        onView(withId(R.id.btnDrawerLogin)).check(matches(withText(getContext().getString(R.string.drawer_sign_in_button_text))));
    }

    @Test
    public void verifyDrawerSignInButtonClick() {
        Intents.init();
        onView(withId(R.id.btnDrawerOpen)).perform(ViewActions.click());
        onView(withId(R.id.btnDrawerLogin)).perform(ViewActions.click());
        Intents.intended(IntentMatchers.hasComponent(LoginActivity.class.getName()));
        Intents.release();
    }

    @After
    public void clearTestEnvironment() {
        activityScenario.onActivity(activity -> {
            PropertiesSingleton props = activity.getProps();
            assertThat(props).isNotNull();

            Map<String, String> serverProperties = UpdateServerSettingsFragment.getUpdateUrlProperties(
                    activity.getString(org.opendatakit.androidlibrary.R.string.default_sync_server_url)
            );
            assertThat(serverProperties).isNotNull();
            serverProperties.put(CommonToolProperties.KEY_FIRST_LAUNCH,"true");
            props.setProperties(serverProperties);
        });
        activityScenario.close();
    }

    private Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

}
