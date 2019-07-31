package com.example.grumon;

import android.os.Environment;
import android.util.Log;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.example.grumon.MainActivity.LATITUDE;
import static com.example.grumon.MainActivity.LEVEL;
import static com.example.grumon.MainActivity.LONGITUDE;
import static com.example.grumon.MainActivity.SCAN_ACQUIRED;
import static com.example.grumon.MainActivity.app_preferences;
import static com.example.grumon.MainActivity.preferencesEditor;
import static java.lang.Boolean.TRUE;

public class FileHelper {
    final static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/grumon/wifi_fingerprint/" ;
    final static String TAG = FileHelper.class.getName();
    static ArrayList<String> wifiAP = new ArrayList<>();

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean scan_over(ArrayList<String> data){
        SCAN_ACQUIRED = app_preferences.getBoolean("SCAN_ACQUIRED", TRUE);
        preferencesEditor.putBoolean("SCAN_ACQUIRED", TRUE);
        preferencesEditor.commit();
        wifiAP = data;
        return true;
    }


    //here we take the time stamp as file name.
    static boolean saveToFile(){

        String ts = GetTimeStamp();
        String fileName = getMacAddr()+ "_"+ ts + ".txt";
        String location = "{\n\t" +"Latitude: "+ LATITUDE+ "\n\tLongitude"+LONGITUDE+"}";
        String data_json = "{\n" + "timestamp:"+ ts +"\nLocation: "+ location+"\nFloor:"+LEVEL+","+"\nWifi_fingerprint:" +wifiAP+"}";
        try {
            new File(path).mkdirs();
            File file = new File(path+ fileName);
            if (!file.exists()) {
//                Log.e("File", "not created");
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data_json + System.getProperty("line.separator")).getBytes());

            return true;
        }  catch(FileNotFoundException ex) {
//            Log.d(TAG, ex.getMessage());
        }  catch(IOException ex) {
//            Log.d(TAG, ex.getMessage());
        }
        return  false;

    }

    public static String GetTimeStamp()
    {
        Date date= new Date();

        long time = date.getTime();
        //Log.i("Time in Milliseconds: " + time);

        Timestamp ts = new Timestamp(time);
        return ts.toString();

    }

    public static String getMacAddr() {
        // this function gets the mac address of a device.
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

}
