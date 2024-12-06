package com.batch.android.dispatcher.mixpanel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.batch.android.Batch;
import com.batch.android.BatchEventDispatcher;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mixpanel Event Dispatcher
 * The dispatcher should generate UTM tag from a Batch payload and send them to the Mixpanel SDK
 */
public class MixpanelDispatcher implements BatchEventDispatcher
{
    /**
     * Mixpanel UTM tag keys
     */
    private static final String CAMPAIGN = "utm_campaign";
    private static final String SOURCE = "utm_source";
    private static final String MEDIUM = "utm_medium";
    private static final String CONTENT = "utm_content";
    private static final String INTEGRATION_ID = "$source";

    /**
     * UTM tag keys
     */
    private static final String UTM_CAMPAIGN = "utm_campaign";
    private static final String UTM_SOURCE = "utm_source";
    private static final String UTM_MEDIUM = "utm_medium";
    private static final String UTM_CONTENT = "utm_content";

    /**
     * Key used to dispatch the Batch tracking Id on Mixpanel
     */
    private static final String BATCH_TRACKING_ID = "batch_tracking_id";

    /**
     * Key used to dispatch the webview click analytics ID
     */
    private static final String BATCH_WEBVIEW_ANALYTICS_ID = "batch_webview_analytics_id";

    /**
     * Event name used when logging on Mixpanel
     */
    private static final String NOTIFICATION_DISPLAY_NAME = "batch_notification_display";
    private static final String NOTIFICATION_OPEN_NAME = "batch_notification_open";
    private static final String NOTIFICATION_DISMISS_NAME = "batch_notification_dismiss";
    private static final String MESSAGING_SHOW_NAME = "batch_in_app_show";
    private static final String MESSAGING_CLOSE_NAME = "batch_in_app_close";
    private static final String MESSAGING_AUTO_CLOSE_NAME = "batch_in_app_auto_close";
    private static final String MESSAGING_CLOSE_ERROR_NAME = "batch_in_app_close_error";
    private static final String MESSAGING_CLICK_NAME = "batch_in_app_click";
    private static final String MESSAGING_WEBVIEW_CLICK_NAME = "batch_in_app_webview_click";
    private static final String UNKNOWN_EVENT_NAME = "batch_unknown";

    MixpanelAPI mixpanelInstance = null;

    MixpanelDispatcher()
    {
    }

    /**
     * Set the MixpanelAPI instance that the dispatcher will use.
     *
     * It is recommended to call this method as early as possible, like in your
     * Application's onCreate.
     *
     * Calling this anywhere else, like an Activity's onCreate, might make the dispatcher
     * miss events.
     *
     * @param context Your application context
     * @param instance MixpanelAPI instance
     */
    public static void setMixpanelInstance(@NonNull Context context, @NonNull MixpanelAPI instance)
    {
        MixpanelDispatcher dispatcher = MixpanelRegistrar.getInstance(context);
        dispatcher.mixpanelInstance = instance;
    }

    /**
     * Callback when a new event just happened in the Batch SDK.
     *
     * @param type The type of the event
     * @param payload The payload associated with the event
     */
    @Override
    public void dispatchEvent(@NonNull Batch.EventDispatcher.Type type,
                              @NonNull Batch.EventDispatcher.Payload payload)
    {
        Map<String, Object> mixpanelParams = null;
        if (type.isNotificationEvent()) {
            mixpanelParams = getNotificationParams(payload);
        } else if (type.isMessagingEvent()) {
            mixpanelParams = getInAppParams(payload);
        }
        mixpanelParams.put(INTEGRATION_ID, "batch");

        if (mixpanelInstance != null) {
            mixpanelInstance.trackMap(getMixpanelEventName(type), mixpanelParams);
        } else {
            Log.e("BatchMixpanelDispatcher", "Tried to send a mixpanel event, but no" +
                    "instance was set. Did you call MixpanelDispatcher.setMixpanelInstance()" +
                    "in your Application's onCreate?");
        }
    }

    private static Map<String, Object> getInAppParams(Batch.EventDispatcher.Payload payload)
    {
        Map<String, Object> mixpanelParams = new HashMap<>();
        mixpanelParams.put(CAMPAIGN, payload.getTrackingId());
        mixpanelParams.put(MEDIUM, "in-app");
        mixpanelParams.put(BATCH_TRACKING_ID, payload.getTrackingId());

        String webViewAnalyticsId = payload.getWebViewAnalyticsID();
        if (webViewAnalyticsId != null) {
            mixpanelParams.put(BATCH_WEBVIEW_ANALYTICS_ID, webViewAnalyticsId);
        }

        String deeplink = payload.getDeeplink();
        if (deeplink != null) {
            try {
                deeplink = deeplink.trim();
                Uri uri = Uri.parse(deeplink);
                if (uri.isHierarchical()) {
                    String fragment = uri.getFragment();
                    if (fragment != null && !fragment.isEmpty()) {
                        Map<String, String> fragments = getFragmentMap(fragment);
                        // Copy from fragment part of the deeplink
                        copyValueFromMap(fragments, UTM_CONTENT, mixpanelParams, CONTENT);
                    }
                    // Copy from query parameters of the deeplink
                    copyValueFromQuery(uri, UTM_CONTENT, mixpanelParams, CONTENT);
                }
            } catch (Exception e) {
                Log.e("BatchMixpanelDispatcher", "Something went wrong parsing deeplink: " + e.getLocalizedMessage());
            }
        }
        // Load from custom payload
        copyValueFromPayload(payload, UTM_CAMPAIGN, mixpanelParams, CAMPAIGN);
        copyValueFromPayload(payload, UTM_MEDIUM, mixpanelParams, MEDIUM);
        copyValueFromPayload(payload, UTM_SOURCE, mixpanelParams, SOURCE);
        return mixpanelParams;
    }

    private static Map<String, Object> getNotificationParams(Batch.EventDispatcher.Payload payload)
    {
        Map<String, Object> mixpanelParams = new HashMap();
        mixpanelParams.put(MEDIUM, "push");

        String deeplink = payload.getDeeplink();
        if (deeplink != null) {
            try {
                deeplink = deeplink.trim();
                Uri uri = Uri.parse(deeplink);
                if(uri.isHierarchical()) {
                    String fragment = uri.getFragment();
                    if (fragment != null && !fragment.isEmpty()) {
                        Map<String, String> fragments = getFragmentMap(fragment);
                        // Copy from fragment part of the deeplink
                        copyValueFromMap(fragments, UTM_CAMPAIGN, mixpanelParams, CAMPAIGN);
                        copyValueFromMap(fragments, UTM_MEDIUM, mixpanelParams, MEDIUM);
                        copyValueFromMap(fragments, UTM_SOURCE, mixpanelParams, SOURCE);
                        copyValueFromMap(fragments, UTM_CONTENT, mixpanelParams, CONTENT);
                    }

                    // Copy from query parameters of the deeplink
                    copyValueFromQuery(uri, UTM_CAMPAIGN, mixpanelParams, CAMPAIGN);
                    copyValueFromQuery(uri, UTM_MEDIUM, mixpanelParams, MEDIUM);
                    copyValueFromQuery(uri, UTM_SOURCE, mixpanelParams, SOURCE);
                    copyValueFromQuery(uri, UTM_CONTENT, mixpanelParams, CONTENT);
                }
            } catch (Exception e) {
                Log.e("BatchMixpanelDispatcher", "Something went wrong parsing deeplink: " + e.getLocalizedMessage());
            }
        }
        // Load from custom payload
        copyValueFromPayload(payload, UTM_CAMPAIGN, mixpanelParams, CAMPAIGN);
        copyValueFromPayload(payload, UTM_MEDIUM, mixpanelParams, MEDIUM);
        copyValueFromPayload(payload, UTM_SOURCE, mixpanelParams, SOURCE);
        return mixpanelParams;
    }

    private static Map<String, String> getFragmentMap(String fragment)
    {
        String[] params = fragment.split("&");
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] parts = param.split("=");
            if (parts.length >= 2) {
                map.put(parts[0].toLowerCase(), parts[1]);
            }
        }
        return map;
    }

    private static void copyValueFromMap(Map<String, String> mapFrom,
                                         String keyFrom,
                                         Map<String, Object> mapOut,
                                         String keyOut)
    {
        String value = mapFrom.get(keyFrom);
        if (value != null) {
            mapOut.put(keyOut, value);
        }
    }

    private static void copyValueFromQuery(Uri uri, String keyFrom, Map<String, Object> mapOut, String keyOut)
    {
        Set<String> keys = uri.getQueryParameterNames();
        for (String key : keys) {
            if (keyFrom.equalsIgnoreCase(key)) {
                String value = uri.getQueryParameter(key);
                if (value != null) {
                    mapOut.put(keyOut, value);
                    return;
                }
            }
        }
    }

    private static void copyValueFromPayload(Batch.EventDispatcher.Payload payload,
                                             String keyFrom,
                                             Map<String, Object> mapOut,
                                             String keyOut)
    {
        String value = payload.getCustomValue(keyFrom);
        if (value != null) {
            mapOut.put(keyOut, value);
        }
    }

    private static String getMixpanelEventName(Batch.EventDispatcher.Type type) {
        switch (type) {
            case NOTIFICATION_DISPLAY:
                return NOTIFICATION_DISPLAY_NAME;
            case NOTIFICATION_OPEN:
                return NOTIFICATION_OPEN_NAME;
            case NOTIFICATION_DISMISS:
                return NOTIFICATION_DISMISS_NAME;
            case MESSAGING_SHOW:
                return MESSAGING_SHOW_NAME;
            case MESSAGING_CLOSE:
                return MESSAGING_CLOSE_NAME;
            case MESSAGING_AUTO_CLOSE:
                return MESSAGING_AUTO_CLOSE_NAME;
            case MESSAGING_CLOSE_ERROR:
                return MESSAGING_CLOSE_ERROR_NAME;
            case MESSAGING_CLICK:
                return MESSAGING_CLICK_NAME;
            case MESSAGING_WEBVIEW_CLICK:
                return MESSAGING_WEBVIEW_CLICK_NAME;
        }
        return UNKNOWN_EVENT_NAME;
    }
}
