package zapsolutions.zap.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import zapsolutions.zap.R;
import zapsolutions.zap.connection.establishConnectionToLnd.LndConnection;

public class TorUtil {

    private final static String ORBOT_PACKAGE_NAME = "org.torproject.android";
    private final static String ORBOT_MARKET_URI = "market://details?id=" + ORBOT_PACKAGE_NAME;
    private final static String FDROID_DOWNLOAD = "https://f-droid.org/";
    private final static String FDROID_PACKAGE_NAME = "org.fdroid.fdroid";
    private final static String PLAY_PACKAGE_NAME = "com.android.vending";
    private final static String GUARDIAN_REPO = "https://guardianproject.info/fdroid/repo?fingerprint=B7C2EEFD8DAC7806AF67DFCD92EB18126BC08312A7F2D6F3862E46013C7A6135";

    public static boolean isCurrentConnectionTor() {
        if (LndConnection.getInstance().getConnectionConfig().isLocal()) {
            return false;
        } else {
            return LndConnection.getInstance().getConnectionConfig().getHost().contains(".onion");
        }
    }

    public static int getTorTimeoutMultiplier(){
        if (isCurrentConnectionTor()) {
            return RefConstants.TOR_TIMEOUT_MULTIPLIER;
        } else {
            return 1;
        }
    }

    public static void askToInstallOrbotIfMissing(Activity activity) {
        if (!isOrbotInstalled(activity)) {
            new AlertDialog.Builder(activity)
                    .setMessage(R.string.tor_install_orbot)
                    .setCancelable(true)
                    .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent intent = getOrbotInstallIntent(activity);

                            // Download F-Droid if no package manager is available.
                            if (intent.getBooleanExtra("noAppStore", false)) {
                                new AlertDialog.Builder(activity)
                                        .setMessage(R.string.tor_download_fdroid)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                activity.startActivity(intent);
                                            }
                                        }).show();
                            }

                            // Notify to add Guardian Project Repo
                            else if (intent.getBooleanExtra("fdroid", false)) {
                                new AlertDialog.Builder(activity)
                                        .setMessage(R.string.tor_fdroid_info)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                activity.startActivity(intent);
                                            }
                                        }).setNegativeButton(R.string.tor_add_repo, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        final Intent addRepoIntent = new Intent(Intent.ACTION_VIEW);
                                        addRepoIntent.setData(Uri.parse(GUARDIAN_REPO));
                                        addRepoIntent.setPackage(FDROID_PACKAGE_NAME);
                                        activity.startActivity(addRepoIntent);
                                    }
                                }).show();
                            } else {
                                activity.startActivity(intent);
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
        }
    }

    public static boolean isOrbotInstalled(Context context) {
        return isAppInstalled(context, ORBOT_PACKAGE_NAME);
    }

    private static boolean isAppInstalled(Context context, String uri) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static Intent getOrbotInstallIntent(Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ORBOT_MARKET_URI));

        PackageManager pm = context.getPackageManager();
        // Retrieve all activities that can be performed for the given intent.
        List<ResolveInfo> resInfos = pm.queryIntentActivities(intent, 0);

        String foundPackageName = null;
        for (ResolveInfo r : resInfos) {
            Log.i("OrbotHelper", "market: " + r.activityInfo.packageName);
            if (TextUtils.equals(r.activityInfo.packageName, FDROID_PACKAGE_NAME)
                    || TextUtils.equals(r.activityInfo.packageName, PLAY_PACKAGE_NAME)) {
                foundPackageName = r.activityInfo.packageName;
                break;
            }
        }

        if (foundPackageName == null) {
            // we neither have Play Store nor F-Droid
            intent.setData(Uri.parse(FDROID_DOWNLOAD));
            intent.putExtra("noAppStore", true);
        } else {
            if (foundPackageName.equals(FDROID_PACKAGE_NAME)) {
                intent.putExtra("fdroid", true);
            }
            intent.setPackage(foundPackageName);
        }
        return intent;
    }
}


