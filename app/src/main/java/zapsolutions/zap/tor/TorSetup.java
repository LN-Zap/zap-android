package zapsolutions.zap.tor;

import android.app.Application;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import io.matthewnelson.topl_service.TorServiceController;
import io.matthewnelson.topl_service.lifecycle.BackgroundManager;
import io.matthewnelson.topl_service.notification.ServiceNotification;
import zapsolutions.zap.BuildConfig;
import zapsolutions.zap.R;

public class TorSetup {

    public static TorServiceController.Builder generateTorServiceControllerBuilder(Application application) {
        return new TorServiceController.Builder(
                application,
                generateTorServiceNotificationBuilder(application),
                generateBackgroundManagerPolicy(),
                BuildConfig.VERSION_CODE,
                new ZapTorSettings(),
                "geoip",
                "geoip6"
        )
                .addTimeToDisableNetworkDelay(1000L)
                .addTimeToRestartTorDelay(100L)
                .addTimeToStopServiceDelay(100L)
                .disableStopServiceOnTaskRemoved(false)
                .setEventBroadcaster(new TorServiceEventBroadcaster())
                .setBuildConfigDebug(BuildConfig.DEBUG);
    }

    private static ServiceNotification.Builder generateTorServiceNotificationBuilder(Context context) {
        return new ServiceNotification.Builder(
                "ZapTor",
                "ZapTor",
                "Zap tor service",
                21
        )
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCustomColor(R.color.lightningOrange)
                .enableTorRestartButton()
                .enableTorStopButton()
                .showNotification(true);
    }

    private static BackgroundManager.Builder.Policy generateBackgroundManagerPolicy() {
        return new BackgroundManager.Builder()
                .respectResourcesWhileInBackground(30);
    }

}