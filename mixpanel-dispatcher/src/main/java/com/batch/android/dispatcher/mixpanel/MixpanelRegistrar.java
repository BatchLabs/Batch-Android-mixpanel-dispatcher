package com.batch.android.dispatcher.mixpanel;

import android.content.Context;

import com.batch.android.BatchEventDispatcher;
import com.batch.android.eventdispatcher.DispatcherRegistrar;

/**
 * Mixpanel Registrar
 * The class will instantiate from the SDK using reflection
 * See the library {@link android.Manifest} for more information
 */
public class MixpanelRegistrar implements DispatcherRegistrar
{
    /**
     * Singleton instance
     */
    private static MixpanelDispatcher instance = null;

    /**
     * Singleton accessor
     * @param unused Context used to initialize the dispatcher
     * @return Dispatcher instance
     */
    static MixpanelDispatcher getInstance(Context unused)
    {
        if (instance == null) {
            instance = new MixpanelDispatcher();
        }
        return instance;
    }

    /**
     * Singleton accessor
     * @param context Context used to initialize the dispatcher
     * @return Dispatcher instance
     */
    @Override
    public BatchEventDispatcher getDispatcher(Context context)
    {
        return getInstance(context);
    }
}
