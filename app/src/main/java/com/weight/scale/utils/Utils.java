package com.weight.scale.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Sergio Cordedda on 30/08/2022
 */
public class Utils {


    public Utils() {
        //Empty constructor
    }

    public final static String FILE_PATH = "configCodeScale4.txt";

    public String readFromFile(Context context) {

        String ret = "";

        try {
            File file = new File(context.getFilesDir(),FILE_PATH);
            if(file.exists()) {
                //FileInputStream inputStream = context.openFileInput(file.getPath());
                FileInputStream fis = new FileInputStream(file.getPath());

                if (fis != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null) {
                        stringBuilder.append("\n").append(receiveString);
                    }

                    inputStreamReader.close();
                    ret = stringBuilder.toString();
                }
            }
            else
            {
                return null;
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
