package com.weight.scale;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.weight.scale.databinding.FragmentFirstBinding;
import com.weight.scale.utils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    Utils utils = new Utils();

    EditText edittext_scale_code;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.i("BOM","First Fragment onCreateView");

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.i("BOM","First Fragment onViewCreated");
        edittext_scale_code = (EditText) view.findViewById(R.id.edittext_scale_code);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                writeCodeToFile(edittext_scale_code.getText().toString(), getContext());

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });


        if (null != utils.readFromFile(getContext()))
        {
            Log.i("FRAG1","Show frag 2");
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment);
        }

    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("BOM","First Fragment onDestroyView");

        binding = null;
    }

    private void writeCodeToFile(String data, Context context) {
        try {

            File file = new File(context.getFilesDir(),utils.FILE_PATH);

            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(data);

            // Close connection
            bw.close();

            Log.i("Fragment1", "File write correctly ");

        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }




}