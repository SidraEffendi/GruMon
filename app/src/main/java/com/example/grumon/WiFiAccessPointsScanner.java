package com.example.grumon;

import android.Manifest;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WiFiAccessPointsScanner extends IntentService {

    // Default Constructor
    public WiFiAccessPointsScanner() {
        super("WiFiAccessPointsScanner");
        // TODO Auto-generated constructor stub
        //Toast.makeText(this, "Scan Starting1", Toast.LENGTH_LONG).show();
    }

    WifiManager wifiManager;
    BroadcastReceiver wifiScanReceiver;
    WifiManager.WifiLock wifiLock;


    @Override
    protected void onHandleIntent(Intent intent){

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "Set wifi on for measurements");
        wifiLock.acquire();

        if (!wifiManager.isWifiEnabled()) {
            //Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            Log.d("Scan ", "WiFi is disabled ... We need to enable it");
            wifiManager.setWifiEnabled(true);
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            Log.i("Scan loc", "granted");
        }
        else {
            Log.i("Scan loc", " not granted");
            //requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION_REQUEST_CODE);
            //checkLocationPermission();
        }

        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                List <ScanResult> results = wifiManager.getScanResults();
                Log.i("ScanResult in Receiver", results.toString());
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
//                unregisterReceiver(this);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        getApplicationContext().registerReceiver(wifiScanReceiver,intentFilter);
        registerReceiver(wifiScanReceiver,intentFilter);

        Log.d("Scan Starting", "yes");
        boolean r = wifiManager.isScanAlwaysAvailable();

        if (r){
            Log.i("Scan Available", "True");
        }


        boolean success = wifiManager.startScan();
        if(!success)
        {
            Log.i("Scan result", "scan failed");
            // scan failure handling
            scanFailure();
        }
        else{
            Log.i("Scan result", "scan ongoing");
            scanSuccess(); //this works and I got the info I need.
        }
    }

    private void scanSuccess() {


        List<ScanResult> results = wifiManager.getScanResults();
        Log.i("Scan Inside receiver", "yes");
        Log.i("ScanSuccess", results.toString());

        ArrayList<String> wifi_fingerprint= new ArrayList<>();

        if(results != null && !results.isEmpty()){
            for (ScanResult scan : results) {
                int level = WifiManager.calculateSignalLevel(scan.level, 20);
                String SSID = scan.SSID;
                long timestamp = scan.timestamp;
                String access_point = "SSID: "+SSID+ ", " +"Signal Level: "+ level+ ", "+ "timestamp: "+ timestamp;
                wifi_fingerprint.add(access_point);
                Log.i("ScanPos", access_point);
            } }

        // write the results to a file
        FileHelper.saveToFile(wifi_fingerprint,"grumon.txt");

    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifiManager.getScanResults();
        Log.i("ScanInsidefail receiver", "yes");
        Log.i("ScanFailure", results.toString());
        //... potentially use older scan results ...
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(wifiScanReceiver);
        wifiLock.release();
        Log.i("Scan", "OnDestroyReached");
    }

}
