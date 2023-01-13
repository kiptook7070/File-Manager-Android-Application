package com.rbigsoft.unrar.utilitaire;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.util.Log;
import androidx.core.app.NotificationCompat.Builder;

import com.rbigsoft.sn.unzip.R;
import com.rbigsoft.unrar.activity.BrowseSdcardActivity;
import java.io.File;
import java.util.Random;
import net.lingala.zip4j.util.InternalZipConstants;

public class NotificationUtil {
    private static String CHANNEL_ID = "easyunrar_channel";

    public static void sendNotification(Activity activity, String str) {
        if (str != null) {
            try {
                createNotificationChannel(activity);
                NotificationManager notificationManager = (NotificationManager) activity.getSystemService("notification");
                String[] split = str.split(InternalZipConstants.ZIP_FILE_SEPARATOR);
                if (split.length > 0) {
                    String string = activity.getString(R.string.job_done);
                    String str2 = split[split.length - 1];
                    File file = new File(str);
                    Intent intent = new Intent(activity, BrowseSdcardActivity.class);
                    intent.setData(Uri.parse(file.getParent()));
                    PendingIntent activity2 = PendingIntent.getActivity(activity, 0, intent, 134217728);
                    int nextInt = new Random(System.currentTimeMillis()).nextInt(1000000);
                    Builder autoCancel = new Builder(activity, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher).setContentTitle(string).setContentText(str2).setAutoCancel(true);
                    autoCancel.setContentIntent(activity2);
                    notificationManager.notify(nextInt, autoCancel.build());
                }
            } catch (Exception e) {
                Log.e("Nofification", "An error occurred while sending a notification", e);
            }
        }
    }

    private static void createNotificationChannel(Activity activity) {
        if (VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Easy Unrar notification channel", 3);
            notificationChannel.setDescription("Notify the end of the extracting process");
            ((NotificationManager) activity.getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
        }
    }
}
