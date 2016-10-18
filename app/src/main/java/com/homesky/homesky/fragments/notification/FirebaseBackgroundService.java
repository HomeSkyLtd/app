package com.homesky.homesky.fragments.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.homesky.homecloud_lib.model.notification.ActionResultNotification;
import com.homesky.homecloud_lib.model.notification.DetectedNodeNotification;
import com.homesky.homecloud_lib.model.notification.LearntRulesNotification;
import com.homesky.homecloud_lib.model.notification.Notification;
import com.homesky.homesky.MessageService;
import com.homesky.homesky.R;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.homecloud.HomecloudHolder;

import java.util.HashMap;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by henrique on 10/18/16.
 */

public class FirebaseBackgroundService extends Service {

    private static final String TAG = "FirebaseService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate:");
        super.onCreate();
        HomeSkyBroadcastReceiver receiver = new HomeSkyBroadcastReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver((receiver), new IntentFilter(MessageService.NOTIF_RESULT));
    }

    public static class HomeSkyBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "HomeSkyReceiver";
        private String ACTION = "action", NODE = "node", RULE = "rule";
        private static HashMap<String, String> sNotificationsString;

        public HomeSkyBroadcastReceiver() {
            reset();
        }

        public static void reset() {
            sNotificationsString = new HashMap<>(3);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, FirebaseBackgroundService.class));
            notify(context, intent);
        }

        private void notify(Context context, Intent intent) {
            Notification n = (Notification) intent.getSerializableExtra(MessageService.NOTIF_MESSAGE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            Log.d(TAG, "onReceive: " + n.toString());

            builder
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_home_white_24dp)
                    .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                    .setColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, context.getTheme()))
                    .setContentTitle("HomeSky")
                    .setContentText("Touch to see what's new in your house");

            if (n instanceof ActionResultNotification) {
                ActionResultNotification an = (ActionResultNotification) n;
                String str = "Sent action: value " + an.getAction().getValue() + " to node " + an.getAction().getNodeId();
                sNotificationsString.put(ACTION, str);

            } else if (n instanceof DetectedNodeNotification) {
                DetectedNodeNotification dn = (DetectedNodeNotification) n;
                String str = "New nodes: " + dn.getNumberOfNodes();
                sNotificationsString.put(NODE, str);

            } else if (n instanceof LearntRulesNotification) {
                LearntRulesNotification ln = (LearntRulesNotification) n;
                String str = "New rules: " + ln.getNumberOfRules();
                sNotificationsString.put(RULE, str);

            }

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                    .setSummaryText(HomecloudHolder.getInstance().getUsername());
            for (String s : sNotificationsString.values()) {
                style.addLine(s);
            }

            builder.setStyle(style);

            Intent resultIntent = new Intent(context, MenuFragmentsActivity.class);
            resultIntent.putExtra(MenuFragmentsActivity.EXTRA_STARTING_FRAGMENT, 4);
            resultIntent.putExtra(MenuFragmentsActivity.EXTRA_NOTIFICATION_MAP, sNotificationsString);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MenuFragmentsActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


            notificationManager.notify(0, builder.build());
        }
    }
}
