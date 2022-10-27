package com.weight.scale;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.weight.scale.databinding.FragmentSecondBinding;
import com.weight.scale.gson.GasData;
import com.weight.scale.utils.Utils;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    final String serverUri = "tcp://developerhome.ddns.net:1883";

    MqttAndroidClient mqttAndroidClient;

    Utils utils = new Utils();
    GasData gasData = new GasData();
    Gson gson = new Gson();

    TextView weightSize;
    TextView percentageSize;
    TextView setSize;
    TextView gasSize;
    TextView updateTime;

    ImageView minus;
    ImageView plus;

    String appCode;

    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy--HH:mm:ss");


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.i("AAA", "item: " + item.getTitle());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getActivity(), "called " + item.getItemId(), Toast.LENGTH_SHORT).show();
            utils.deleteFile(getContext(), getActivity());
            Navigation.findNavController(getView())
                    .navigate(R.id.action_SecondFragment_to_FirstFragment);


            return true;
        }

        Toast.makeText(getActivity(), "called " + item.getItemId(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);

        // return false;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        IntentFilter filter = new IntentFilter();
        filter.addAction( "jsonReceived" );
        getActivity().registerReceiver(broadcastReceiver, filter);


        weightSize = (TextView) view.findViewById(R.id.WeightSize);
        percentageSize = (TextView) view.findViewById(R.id.PercentageSize);
        setSize = (TextView) view.findViewById(R.id.SetSize);
        gasSize = (TextView) view.findViewById(R.id.readGasSize);
        updateTime = (TextView) view.findViewById(R.id.lastUpdateRow);
        minus = (ImageView) view.findViewById(R.id.minus);
        plus = (ImageView) view.findViewById(R.id.plus);


        appCode = utils.readFromFile(getContext());
        Log.i("APPCODE", "appcode: " + appCode);




        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minusButton(view);
            }
        });

        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                plusButton(view);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }




    public void updateGraphicInformation(GasData gasData)
    {

        weightSize.setText(gasData.getWeight().toString());
        percentageSize.setText(gasData.getPercentage().toString());
        //From Epoch to time
        Date receivedDate = new Date(Long.parseLong(gasData.getTime()) * 1000);
        Date correctDate = DateUtils.addHours(receivedDate, -1);
        updateTime.setText(sdf.format(correctDate));
        setSize.setText(gasData.getSettedSize().toString());

    }


    public void minusButton(View v) {

        String previousGasSize = gasSize.getText().toString();
        Integer previousGasSizeInt = Integer.parseInt(previousGasSize);
        if (previousGasSizeInt > 5) {
            String newGasSize = String.valueOf(previousGasSizeInt - 5);
            gasSize.setText(newGasSize);
            publishMessage(newGasSize, appCode.trim());
        }
    }

    public void plusButton(View v) {
        String previousGasSize = gasSize.getText().toString();
        Integer previousGasSizeInt = Integer.parseInt(previousGasSize);
        if (previousGasSizeInt < 20) {
            String newGasSize = String.valueOf(previousGasSizeInt + 5);
            gasSize.setText(newGasSize);
            publishMessage(newGasSize, appCode.trim());
        }
    }


    private void publishMessage(String message, String topic) {
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


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("jsonReceived")) {
                String arrivedMessage = intent.getStringExtra("json");
                GasData gasData = gson.fromJson(arrivedMessage.trim(), GasData.class);
                updateGraphicInformation(gasData);
            }
        }
    };



}