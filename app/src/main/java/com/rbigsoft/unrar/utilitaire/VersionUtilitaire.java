package com.rbigsoft.unrar.utilitaire;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.rbigsoft.sn.unzip.R;


public class VersionUtilitaire {
    public static boolean isLite(Context context) {
        if (!(context == null || context.getApplicationInfo() == null)) {
            if ("com.sn.unziparchiver.lite".equals(context.getApplicationInfo().packageName)) {
                return false;
            }
        }
        return true;
    }

    public static String getProviderName(Context context) {
        return isLite(context) ? "com.sn.unzip.unzar.provider_paths" : "com.sn.unzip.unzar.provider_paths";
    }
    public static void existdialogMessage(final Activity activity) {
        AlertDialog create = new Builder(activity).create();
        create.setTitle(activity.getString(R.string.exit_title));
        create.setMessage(activity.getString(R.string.exit_message));
        create.setButton(activity.getString(R.string.exit_yes), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.finish();
            }
        });
        create.setButton2(activity.getString(R.string.exit_no), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        create.show();
    }
}
