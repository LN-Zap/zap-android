package zapsolutions.zap.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * This class helps to easily manage the permission necessary for the Zap app.
 */
public class PermissionsUtil {

    // These codes defined here will be used to respond to user input on the request permission dialogs.
    public static final int CAMERA_PERMISSION_CODE = 0;


    public static boolean hasCameraPermission(Context context) {
        return hasPermission(context, Manifest.permission.CAMERA);
    }

    public static void requestCameraPermission(Context context, boolean forceRequest) {
        requestPermissions(context, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE, forceRequest);
    }


    private static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestPermissions(Context context, String[] permissions, int code, boolean forceRequest) {

        for (int i = 0; i < permissions.length; i++) {
            // Do not request permission if user already denied it.
            // If forceRequest is true, the user will still be asked unless he ticked "don't ask me again".
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissions[i]) & !forceRequest) {
                ZapLog.debug("PermissionsUtil", "User denied this request before, no permission requested");
            } else {
                ActivityCompat.requestPermissions((Activity) context, permissions, code);
                break;
            }
        }

    }
}
