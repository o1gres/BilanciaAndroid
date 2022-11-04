package com.weight.scale;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.weight.scale.gson.GasData;
import com.weight.scale.utils.Utils;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

/**
 * Created by Sergio Cordedda on 27/10/2022
 */
public class NotificationService extends Service {

    public static  Context context;
    public static NotificationManager notificationManager;

    Gson gson = new Gson();
    Utils utils = new Utils();
    GasData gasData = new GasData();
    //MQTT
    final String serverUri = "tcp://developerhome.ddns.net:1883";
    MqttAndroidClient mqttAndroidClient;
    String appCode;

    public NotificationService()
    {
        //Empty Constructor
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        //Get context
        String ns = Context.NOTIFICATION_SERVICE;
        notificationManager = (NotificationManager) getSystemService(ns);

        //Manage MQTT communication
        //MQTT CONNECTION
        appCode = utils.readFromFile(context);

        if (null != appCode) {
            connectToMqtt(context);
        }

    }

    public void connectToMqtt(Context context)
    {
        if (null == appCode)
        {
            appCode = utils.readFromFile(context);
        }
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, appCode.trim(), Ack.AUTO_ACK);

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(Utils.MQTT_USERNAME);
        mqttConnectOptions.setPassword(Utils.MQTT_PASSWORD.toCharArray());

        IntentFilter filter = new IntentFilter();
        filter.addAction( "publishNewSize" );
        context.registerReceiver(broadcastReceiver, filter);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("Main", "Connected correctly");
                    subscribeTopic(appCode.trim());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.d("ERR", "Errore MQTT connect:" + e);
        }
    }

    public static void generatePushNotification(Float percentage)
    {
        String CHANNEL_ID="bombola_id";
        String channel_name="bombola_channel_name";
        String channel_description="bombola_channel_description";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    channel_name,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(channel_description);
            notificationManager.createNotificationChannel(channel);
        }
        /*
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bombola)
                .setLargeIcon(BitmapFactory. decodeResource (Resources.getSystem() , R.drawable.bombola))
                .setContentTitle("Bombola in esaurimento")
                .setContentText("Attenzione la bombola è al "+percentage+"%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);*/

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bombola16)
                .setContentTitle("Bombola in esaurimento")
                .setContentText("Attenzione la bombola è al "+percentage+"%")
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(BitmapFactory. decodeResource (Resources.getSystem() , R.drawable.bombola)))
                .build();
        notificationManager.notify(0, notification);
    }


    public void subscribeTopic(String topic) {
        try {
            if (mqttAndroidClient.isConnected()) {
                mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i("MQTTSUB", "subscribed succeed");
                        if (mqttAndroidClient.isConnected()) {
                            receiveMqttMessages();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.i("MQTTSUB", "subscribed failed");
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    public void receiveMqttMessages() {
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    String arrivedMessage = message.toString();
                    Log.i("MQTT", "received message: " + arrivedMessage);

                    GasData gasData = gson.fromJson(arrivedMessage.trim(), GasData.class);
                    if (gasData.getPercentage()<=15.0) {
                        NotificationService.generatePushNotification(gasData.getPercentage());
                    }

                    Intent i = new Intent();
                    i.setAction("jsonReceived");
                    i.putExtra("json", arrivedMessage.trim());
                    context.sendBroadcast(i);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    public void publishMessage(String message, String topic) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setId(1);
            mqttMessage.setPayload(message.getBytes(StandardCharsets.UTF_8));
            mqttMessage.setQos(0);
            mqttMessage.setRetained(true);
            mqttAndroidClient.publish("bs" + topic, mqttMessage);
        } catch (Exception e) {
            Log.e("MQTT", "Error publishing message: " + e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("publishNewSize")) {
                String topic = intent.getStringExtra("topic");
                String message = intent.getStringExtra("newSize");
                publishMessage(message, topic);
            }
        }
    };
}
