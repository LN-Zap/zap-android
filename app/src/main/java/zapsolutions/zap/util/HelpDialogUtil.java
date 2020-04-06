package zapsolutions.zap.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import zapsolutions.zap.R;

public class HelpDialogUtil {

    public static void showDialog(Context context, int StringResource) {
        LayoutInflater adbInflater = LayoutInflater.from(context);
        View titleView = adbInflater.inflate(R.layout.help_dialog_title, null);

        new AlertDialog.Builder(context)
                .setCustomTitle(titleView)
                .setMessage(StringResource)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }
}


