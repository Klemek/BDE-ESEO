/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.bde_eseo.eseomega.gcmpush;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.bde_eseo.eseomega.Constants;
import fr.bde_eseo.eseomega.MainActivity;
import fr.bde_eseo.eseomega.R;
import fr.bde_eseo.eseomega.utils.ThemeUtils;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    // [END receive_message]

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (data != null) {
            Log.d("debug", "" + data);
            String title = data.getString("title");
            String message = data.getString("message");
            String versionPush = data.getString("versionPush");
            double dVersion = 0.0;
            if (versionPush != null && versionPush.length() > 0) {
                dVersion = Double.valueOf(versionPush.replace(",", "."));
            }
            int subject = 0;
            String topic = data.getString("topic");
            if (topic != null) {
                if (topic.length() > 0) {
                    subject = Integer.parseInt(topic);
                }
            }

            // [START_EXCLUDE]
            /**
             * Production applications would usually process the message here.
             * Eg: - Syncing with server.
             *     - Store message in local database.
             *     - Update UI.
             */

            /**
             * In some cases it may be useful to show a notification indicating to the user
             * that a message was received.
             */
            if (from.equals(this.getResources().getString(R.string.play_api_push))) {
                sendNotification(subject, title, message, dVersion);
            }
        }
        // [END_EXCLUDE]
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(int intentID, String title, String message, double versionPush) {

        // prepare intent
        Intent intent = null;
        if (versionPush > Constants.NOTIF_VERSION) {
            intentID = Constants.NOTIF_UPDATE;
            title = getString(R.string.notif_update_title);
            message = getString(R.string.notif_update_text);
        }
        switch (intentID) {
            case Constants.NOTIF_GENERAL:
            case Constants.NOTIF_NEWS:
            case Constants.NOTIF_EVENTS:
            case Constants.NOTIF_CLUBS:
            case Constants.NOTIF_CAFET:
            case Constants.NOTIF_TIPS:
            case Constants.NOTIF_CONNECT:
            default:
                intent = new Intent(this, MainActivity.class);
                intent.putExtra(Constants.KEY_MAIN_INTENT, intentID);
                if (intentID == Constants.NOTIF_GENERAL) {
                    intent.putExtra(Constants.KEY_MAIN_TITLE, title);
                    intent.putExtra(Constants.KEY_MAIN_MESSAGE, message);
                }
                break;
            case Constants.NOTIF_UPDATE:
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName));
                } catch (android.content.ActivityNotFoundException anfe) {
                    Toast.makeText(MyGcmListenerService.this, R.string.toast_playstore, Toast.LENGTH_SHORT).show();
                }
                break;
        }

        // Attach intent to notification, only if has intent
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_ONE_SHOT);

            long[] pattern = {0, 100, 250, 100, 250, 100};

            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notif) // Only small icon for Lollipop
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setLights(Color.YELLOW, 1000, 3000)
                    .setColor(ThemeUtils.resolveColorFromTheme(getApplicationContext(), R.attr.colorPrimary))
                    .setVibrate(pattern)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            // Big icon for previous version (older than Lollipop)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher));
            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Random notification ID
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }


}
