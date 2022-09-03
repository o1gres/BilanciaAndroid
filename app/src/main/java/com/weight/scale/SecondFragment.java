package com.weight.scale;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.gson.Gson;
import com.weight.scale.databinding.FragmentSecondBinding;
import com.weight.scale.gson.GasData;
import com.weight.scale.utils.Utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    final String serverUri = "tcp://broker.hivemq.com:1883";

    MqttAndroidClient mqttAndroidClient;

    Utils utils     = new Utils();
    GasData gasData = new GasData();
    Gson gson       = new Gson();

    TextView testTextView;
    TextView weightSize;
    TextView percentageSize;
    TextView gasSize;

    ImageView minus;
    ImageView plus;


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
        Log.i("AAA","item: "+item.getTitle());
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(getActivity(), "called " + item.getItemId(), Toast.LENGTH_SHORT).show();
            utils.deleteFile(getContext(),getActivity());
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
        testTextView    = (TextView) view.findViewById(R.id.textview_second);
        weightSize      = (TextView) view.findViewById(R.id.WeightSize);
        percentageSize  = (TextView) view.findViewById(R.id.PercentageSize);
        gasSize         = (TextView) view.findViewById(R.id.readGasSize);

        minus        = (ImageView) view.findViewById(R.id.minus);
        plus        = (ImageView) view.findViewById(R.id.plus);
        String appCode = utils.readFromFile(getContext());
        Log.i("APPCODE","appcode: "+appCode);
        testTextView.setText(appCode.trim());

        //MQTT CONNECTION
        mqttAndroidClient = new MqttAndroidClient(getContext(), serverUri, appCode.trim());

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try
        {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener()
            {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("Main","Connected correctly");
                    subscribeTopic (appCode.trim());
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    exception.printStackTrace();
                }
            });
        }
        catch (MqttException e)
        {
            Log.d("ERR", "Errore MQTT connect:" +e);
        }


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

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void receiveMqttMessages()
    {
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {

                    GasData aaa = new GasData();
                    aaa.setPercentage(20);
                    aaa.setSettedSize(21);
                    aaa.setWeight(22);
                    aaa.setTime("01:02:03");

                    Log.i("CCC","CCC: "+gson.toJson(aaa));

                    String arrivedMessage = message.toString();
                    Log.i("BBB", "received message: " + arrivedMessage);
                    gasData = gson.fromJson(arrivedMessage.trim(), GasData.class);
                    weightSize.setText(gasData.getWeight().toString());
                    percentageSize.setText(gasData.getPercentage().toString());
                    gasSize.setText(gasData.getSettedSize().toString());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    public void minusButton(View v) {

        String previousGasSize = gasSize.getText().toString();
        Integer previousGasSizeInt = Integer.parseInt(previousGasSize);
        if (previousGasSizeInt >5) {
            String newGasSize = String.valueOf(previousGasSizeInt - 5);
            gasSize.setText(newGasSize);
        }
    }

    public void plusButton(View v) {
        String previousGasSize = gasSize.getText().toString();
        Integer previousGasSizeInt = Integer.parseInt(previousGasSize);
        if (previousGasSizeInt < 20) {
            String newGasSize = String.valueOf(previousGasSizeInt + 5);
            gasSize.setText(newGasSize);
        }
    }

}