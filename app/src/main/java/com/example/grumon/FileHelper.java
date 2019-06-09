package com.example.grumon;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FileHelper {
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grumon/wifi_fingerprint" ;
    final static String TAG = FileHelper.class.getName();


    //here we take the time stamp as file name.
    public static boolean saveToFile(ArrayList<String> data, String fileName){
        try {
            new File(path).mkdir();
            File file = new File(path+ fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        }  catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }  catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return  false;


    }
}
