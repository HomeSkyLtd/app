package com.homesky.homesky.fragments.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.NotificationCompat;

import com.homesky.homecloud_lib.model.notification.ActionResultNotification;
import com.homesky.homecloud_lib.model.notification.DetectedNodeNotification;
import com.homesky.homecloud_lib.model.notification.LearntRulesNotification;
import com.homesky.homecloud_lib.model.notification.Notification;
import com.homesky.homesky.MessageService;
import com.homesky.homesky.R;
import com.homesky.homesky.activities.MenuFragmentsActivity;
import com.homesky.homesky.homecloud.HomecloudHolder;
import com.homesky.homesky.request.ModelStorage;
import com.homesky.homesky.request.RequestCallback;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;
import static android.support.v4.app.NotificationCompat.DEFAULT_VIBRATE;

/**
 * Created by henrique on 10/18/16.
 */

public class FirebaseBackgroundService extends Service {

    private static final String TAG = "FirebaseService";
    private static HomeSkyBroadcastReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mReceiver = new HomeSkyBroadcastReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver((mReceiver), new IntentFilter(MessageService.NOTIF_RESULT));
    }

    public static HomeSkyBroadcastReceiver getReceiver() {
        return mReceiver;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    public static class HomeSkyBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "HomeSkyReceiver";
        private String ACTION = "action", NODE = "node", RULE = "rule";
        private HashMap<String, Item> sNotificationsString;
        private List<Runnable> mCallbacks;

        private class Item {
            private String description;
            private Integer number;

            Item (String description, Integer number) {
                this.description = description;
                this.number = number;
            }

            Item (String description) {
                this.description = description;
            }

            int getNumber() {
                return number;
            }

            @Override
            public String toString() {
                String str = description;

                if (number != null)
                    str += number;

                return str;
            }
        }

        HomeSkyBroadcastReceiver() {
            reset();
            mCallbacks = new LinkedList<>();
        }

        public void addCallback (Runnable r) {
            mCallbacks.add(r);
        }

        public void reset() {
            sNotificationsString = new HashMap<>(3);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            notify(context, intent);

            for (Runnable r : mCallbacks) {
                r.run();
            }
        }

        private void notify(Context context, Intent intent) {
            Notification n = (Notification) intent.getSerializableExtra(MessageService.NOTIF_MESSAGE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

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
                sNotificationsString.put(ACTION, new Item(str));

            } else if (n instanceof DetectedNodeNotification) {
                DetectedNodeNotification dn = (DetectedNodeNotification) n;
                int number = dn.getNumberOfNodes();
                String str = "New nodes: ";

                if (sNotificationsString.containsKey(NODE)) {
                    number += sNotificationsString.get(NODE).getNumber();
                }

                sNotificationsString.put(NODE, new Item(str, number));

                ModelStorage.getInstance().invalidateNodesCache();
            } else if (n instanceof LearntRulesNotification) {
                LearntRulesNotification ln = (LearntRulesNotification) n;

                int number = ln.getNumberOfRules();
                String str = "New rules: ";

                if (sNotificationsString.containsKey(RULE)) {
                    number += sNotificationsString.get(RULE).getNumber();
                }

                sNotificationsString.put(RULE, new Item(str, number));

                ModelStorage.getInstance().invalidateLearntRulesCache();
            }

            NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle()
                    .setSummaryText(HomecloudHolder.getInstance().getUsername());
            for (Item i : sNotificationsString.values()) {
                style.addLine(i.toString());
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
