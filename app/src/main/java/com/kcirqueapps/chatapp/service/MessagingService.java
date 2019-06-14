package com.kcirqueapps.chatapp.service;

import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService {
    public static final String ACTION_RECEIVED_MESSAGE = "ReceivedMessage";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData()!=null){
            Intent intent = new Intent(ACTION_RECEIVED_MESSAGE);
            intent.putExtra("data",remoteMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
