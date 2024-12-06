package com.batch.android.dispatcher.mixpanel;

import android.os.Build;

import com.batch.android.Batch;
import com.batch.android.BatchMessage;
import com.batch.android.BatchPushPayload;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Test the Mixpanel Event Dispatcher implementation
 */
@Config(sdk = Build.VERSION_CODES.O_MR1)
@RunWith(RobolectricTestRunner.class)
@PrepareForTest(MixpanelAPI.class)
public class MixpanelDispatcherTest {
    private MixpanelAPI mixpanel;
    private MixpanelDispatcher mixpanelDispatcher;

    @Before
    public void setUp() {
        mixpanel = PowerMockito.mock(MixpanelAPI.class);

        mixpanelDispatcher = new MixpanelDispatcher();
        mixpanelDispatcher.mixpanelInstance = mixpanel;
    }

    @Test
    public void testNotificationDeeplinkQueryVars() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?utm_source=batchsdk&utm_medium=push-batch&utm_campaign=yoloswag&utm_content=button1",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "push-batch");
        expected.put("utm_source", "batchsdk");
        expected.put("utm_campaign", "yoloswag");
        expected.put("utm_content", "button1");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_display"), mapEq(expected));
    }

    @Test
    public void testNotificationNoData() {

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "push");
        expected.put("$source", "batch");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_display"), Mockito.eq(expected));
    }

    @Test
    public void testNotificationDeeplinkQueryVarsEncode() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?utm_source=%5Bbatchsdk%5D&utm_medium=push-batch&utm_campaign=yoloswag&utm_content=button1",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "push-batch");
        expected.put("utm_source", "[batchsdk]");
        expected.put("utm_campaign", "yoloswag");
        expected.put("utm_content", "button1");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_display"), mapEq(expected));
    }

    @Test
    public void testNotificationDeeplinkFragmentVars() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com#utm_source=batch-sdk&utm_medium=pushbatch01&utm_campaign=154879548754&utm_content=notif001",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "pushbatch01");
        expected.put("utm_source", "batch-sdk");
        expected.put("utm_campaign", "154879548754");
        expected.put("utm_content", "notif001");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_open"), mapEq(expected));
    }

    @Test
    public void testNotificationDeeplinkFragmentVarsEncode() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com/test#utm_source=%5Bbatch-sdk%5D&utm_medium=pushbatch01&utm_campaign=154879548754&utm_content=notif001",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "pushbatch01");
        expected.put("utm_source", "[batch-sdk]");
        expected.put("utm_campaign", "154879548754");
        expected.put("utm_content", "notif001");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_open"), mapEq(expected));
    }

    @Test
    public void testNotificationCustomPayload() {

        Map<String, String> customPayload = new HashMap<>();
        customPayload.put("utm_medium", "654987");
        customPayload.put("utm_source", "jesuisuntest");
        customPayload.put("utm_campaign", "heinhein");
        customPayload.put("utm_content", "allo118218");
        TestEventPayload payload = new TestEventPayload(null,
                null,
                customPayload);

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "654987");
        expected.put("utm_source", "jesuisuntest");
        expected.put("utm_campaign", "heinhein");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISPLAY, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_display"), mapEq(expected));
    }

    @Test
    public void testNotificationDeeplinkPriority() {
        // priority: Custom Payload > Query vars > Fragment vars
        Map<String, String> customPayload = new HashMap<>();
        customPayload.put("utm_medium", "654987");
        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?utm_source=batchsdk&utm_campaign=yoloswag#utm_source=batch-sdk&utm_medium=pushbatch01&utm_campaign=154879548754&utm_content=notif001",
                customPayload);

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "654987");
        expected.put("utm_source", "batchsdk");
        expected.put("utm_campaign", "yoloswag");
        expected.put("utm_content", "notif001");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_open"), mapEq(expected));
    }

    @Test
    public void testNotificationDeeplinkNonTrimmed() {
        Map<String, String> customPayload = new HashMap<>();
        TestEventPayload payload = new TestEventPayload(null,
                "   \n     https://batch.com?utm_source=batchsdk&utm_campaign=yoloswag     \n ",
                customPayload);

        Map<String, Object> expected = new HashMap<>();
        expected.put("$source", "batch");
        expected.put("utm_medium", "push");
        expected.put("utm_source", "batchsdk");
        expected.put("utm_campaign", "yoloswag");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_OPEN, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_open"), mapEq(expected));
    }

    @Test
    public void testNotificationDismissCampaign() {

        Map<String, String> customPayload = new HashMap<>();
        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?utm_campaign=yoloswag",
                customPayload);

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "push");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "yoloswag");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.NOTIFICATION_DISMISS, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_notification_dismiss"), mapEq(expected));
    }

    @Test
    public void testInAppNoData() {

        TestEventPayload payload = new TestEventPayload(null,
                null,
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", null);
        expected.put("batch_tracking_id", null);

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_show"), mapEq(expected));
    }

    @Test
    public void testInAppShowUppercaseQueryVars() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?uTm_ConTENT=jesuisuncontent",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("batch_tracking_id", null);
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", null);
        expected.put("utm_content", "jesuisuncontent");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_show"), mapEq(expected));
    }

    @Test
    public void testInAppShowUppercaseFragmentVars() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com#UtM_CoNtEnT=jesuisuncontent",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("batch_tracking_id", null);
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", null);
        expected.put("utm_content", "jesuisuncontent");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_show"), mapEq(expected));
    }

    @Test
    public void testInAppTrackingId() {

        TestEventPayload payload = new TestEventPayload("jesuisunid",
                null,
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "jesuisunid");
        expected.put("batch_tracking_id", "jesuisunid");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_click"), mapEq(expected));
    }

    @Test
    public void testInAppDeeplinkContentQueryVars() {

        TestEventPayload payload = new TestEventPayload("jesuisunid",
                "https://batch.com?utm_content=jesuisuncontent",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "jesuisunid");
        expected.put("batch_tracking_id", "jesuisunid");
        expected.put("utm_content", "jesuisuncontent");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLOSE, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_close"), mapEq(expected));
    }

    @Test
    public void testInAppDeeplinkFragmentQueryVars() {

        TestEventPayload payload = new TestEventPayload("jesuisunid",
                "https://batch.com#utm_content=jesuisuncontent00587",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "jesuisunid");
        expected.put("batch_tracking_id", "jesuisunid");
        expected.put("utm_content", "jesuisuncontent00587");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_SHOW, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_show"), mapEq(expected));
    }

    @Test
    public void testInAppCloseError() {

        TestEventPayload payload = new TestEventPayload("jesuisunid",
                null,
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "jesuisunid");
        expected.put("batch_tracking_id", "jesuisunid");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLOSE_ERROR, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_close_error"), mapEq(expected));
    }

    @Test
    public void testInAppWebView() {

        TestEventPayload payload = new TestEventPayload(null,
                "jesuisunbouton",
                null,
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", null);
        expected.put("batch_tracking_id", null);
        expected.put("batch_webview_analytics_id", "jesuisunbouton");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_WEBVIEW_CLICK, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_webview_click"), mapEq(expected));
    }

    @Test
    public void testInAppDeeplinkContentPriority() {

        TestEventPayload payload = new TestEventPayload("jesuisunid",
                "https://batch.com?utm_content=jesuisuncontent002#utm_content=jesuisuncontent015",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", "jesuisunid");
        expected.put("batch_tracking_id", "jesuisunid");
        expected.put("utm_content", "jesuisuncontent002");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_AUTO_CLOSE, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_auto_close"), mapEq(expected));
    }

    @Test
    public void testInAppDeeplinkContentNoId() {

        TestEventPayload payload = new TestEventPayload(null,
                "https://batch.com?utm_content=jesuisuncontent",
                new HashMap<>());

        Map<String, Object> expected = new HashMap<>();
        expected.put("utm_medium", "in-app");
        expected.put("$source", "batch");
        expected.put("utm_campaign", null);
        expected.put("batch_tracking_id", null);
        expected.put("utm_content", "jesuisuncontent");

        mixpanelDispatcher.dispatchEvent(Batch.EventDispatcher.Type.MESSAGING_CLICK, payload);
        Mockito.verify(mixpanel).trackMap(Mockito.eq("batch_in_app_click"), mapEq(expected));
    }

    private static class TestEventPayload implements Batch.EventDispatcher.Payload {

        private String trackingId;
        private String deeplink;
        private String webViewAnalyticsID;
        private Map<String, String> customPayload;

        TestEventPayload(String trackingId,
                         String deeplink,
                         Map<String, String> customPayload)
        {
            this(trackingId, null, deeplink, customPayload);
        }

        TestEventPayload(String trackingId,
                         String webViewAnalyticsID,
                         String deeplink,
                         Map<String, String> customPayload)
        {
            this.trackingId = trackingId;
            this.webViewAnalyticsID = webViewAnalyticsID;
            this.deeplink = deeplink;
            this.customPayload = customPayload;
        }

        @Nullable
        @Override
        public String getTrackingId()
        {
            return trackingId;
        }

        @Nullable
        @Override
        public String getWebViewAnalyticsID() {
            return webViewAnalyticsID;
        }

        @Nullable
        @Override
        public String getDeeplink()
        {
            return deeplink;
        }

        @Nullable
        @Override
        public String getCustomValue(@NonNull String key)
        {
            if (customPayload == null) {
                return null;
            }
            return customPayload.get(key);
        }

        @Override
        public boolean isPositiveAction() {
            return false;
        }

        @Nullable
        @Override
        public BatchMessage getMessagingPayload()
        {
            return null;
        }

        @Nullable
        @Override
        public BatchPushPayload getPushPayload()
        {
            return null;
        }
    }

    public static Map<String, Object> mapEq(Map<String, Object> expected) {
        return Mockito.argThat(new StringMapObjectMatcher(expected));
    }

    private static class StringMapObjectMatcher implements ArgumentMatcher<Map<String, Object>>
    {
        Map<String, Object> expected;

        private StringMapObjectMatcher(Map<String, Object> expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Map<String, Object> map) {
            return equalMaps(map, expected);
        }

        private boolean equalMaps(Map<String, Object> one, Map<String, Object> two) {
            if (one.size() != two.size()) {
                return false;
            }

            Set<String> setOne = new HashSet<>(one.keySet());
            setOne.addAll(two.keySet());
            Object valueOne;
            Object valueTwo;

            for (String key : setOne) {
                if (!one.containsKey(key) || !two.containsKey(key)) {
                    return false;
                }

                valueOne = one.get(key);
                valueTwo = two.get(key);
                // This only works with Maps that are String/Object
                if (valueOne instanceof Map && valueTwo instanceof Map &&
                        !equalMaps((Map<String, Object>) valueOne, (Map<String, Object>) valueTwo)) {
                    return false;
                } else if (valueOne == null) {
                    if (valueTwo != null) {
                        return false;
                    }
                } else if (!valueOne.equals(valueTwo)) {
                    return false;
                }
            }
            return true;
        }
    }
}
