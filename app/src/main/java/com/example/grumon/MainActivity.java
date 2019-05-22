package com.example.grumon;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static java.lang.Boolean.TRUE;



public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {


    Button Button1,Button2, Done;

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    Marker mCurrLocationMarker;
    LocationCallback mLocationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        // The workflow is based on the activity status of the user.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //take input from the user about its current activity
        Button1=findViewById(R.id.Button1);
        Button1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){


                Toast.makeText(getApplicationContext(),"Yes clicked",Toast.LENGTH_SHORT).show();
                //start scanning the for WiFi APs. If it fails suspend the app process and inform user of failure.
                Intent myIntent = new Intent(MainActivity.this,WiFiAccessPointsScanner.class);
                startService(myIntent);


                // ask user to mark pin location on map and enter the level they are at.
                showMap();
    //                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION_REQUEST_CODE);


            }
        });


        Button2=findViewById(R.id.Button2);
        Button2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){


                Toast.makeText(getApplicationContext(),"No clicked",Toast.LENGTH_SHORT).show();
                //close the app


            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };
    }




    public void showMap(){
        //Displays the google map in a fragment

        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapFrag=(SupportMapFragment)getSupportFragmentManager()
        .findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);

        Toast.makeText(getApplicationContext(),"Map ready",Toast.LENGTH_SHORT).show();

        Done = findViewById(R.id.Done);
        Done.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){


                Toast.makeText(getApplicationContext(),"Done clicked",Toast.LENGTH_SHORT).show();
                takeLevelInput();


            }
        });
    }

    public void takeLevelInput(){
        Toast.makeText(getApplicationContext(),"Level Input",Toast.LENGTH_SHORT).show();
        // creating the EditText widget programatically
        final EditText editText = new EditText(MainActivity.this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setRawInputType(Configuration.KEYBOARD_12KEY);

        // create the AlertDialog as final
        final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                //.setMessage("Enter the floor level you are at.")
                .setTitle("Enter the floor level you are at.")
                .setView(editText)

                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getApplicationContext(),editText.getText(),Toast.LENGTH_SHORT).show();
                        setContentView(R.layout.activity_main);

                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // removes the AlertDialog in the screen
                    }
                })
                .create();

        // set the focus change listener of the EditText
        // this part will make the soft keyboard automatically visible
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        dialog.show();

    }


    @Override
    public void onPause(){
        super.onPause();

        //stop location updates when Activity is no longer active
//        if(mGoogleApiClient!=null){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        //}
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if(android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
                //Location Permission already granted
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.setOnMarkerDragListener(this);

                requestLocationUpdates();
            }
            else{
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else{
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setOnMarkerDragListener(this);

            requestLocationUpdates();
        }
    }

    public void requestLocationUpdates(){

        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback,Looper.myLooper());
        }
    }
    public void updateLocationUI(){
        //mLastLocation=location;
        if(mCurrLocationMarker!=null){
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng=new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        markerOptions.draggable(TRUE);
        mCurrLocationMarker=mGoogleMap.addMarker(markerOptions);

        double num = 0.001;
        //CircleOptions addCircle=new CircleOptions().center(latLng).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
       // mCircle=mGoogleMap.addCircle(addCircle);
        LatLngBounds POS = new LatLngBounds(
                new LatLng(mCurrentLocation.getLatitude() - num, mCurrentLocation.getLongitude() - num),
                new LatLng(mCurrentLocation.getLatitude() + num, mCurrentLocation.getLongitude()+ num));

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mGoogleMap.setLatLngBoundsForCameraTarget(POS);
//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(POS.getCenter(), 10));
         mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(20));
//        mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
//        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(POS,0));

        //stop location updates
//        if(mGoogleApiClient!=null){
//            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        //tvLocInfo.setText("Marker " + marker.getId() + " Drag@" + marker.getPosition());
        Toast.makeText(this,"dragging",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
       //tvLocInfo.setText("Marker " + marker.getId() + " DragEnd");
        Toast.makeText(this,"drag end",Toast.LENGTH_LONG).show();
        LatLng latLng=new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(20));

        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);

    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        //tvLocInfo.setText("Marker " + marker.getId() + " DragStart");
        Toast.makeText(this,"drag start",Toast.LENGTH_LONG).show();

    }





    public static final int MY_PERMISSIONS_REQUEST_LOCATION=99;
    private void checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED){

            // Should we show an explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                .setTitle("Location Permission Needed")
                .setMessage("This app needs the Location permission, please accept to use location functionality")
                .setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .create()
                .show();

            }
            else{
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],int[]grantResults){
        switch(requestCode){
            case MY_PERMISSIONS_REQUEST_LOCATION:{
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                }
                else{

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"permission denied",Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
        //updateLocationUI();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}

//have a text view with a question and two buttons with yes and no instead of a dialogue box.



//@Override
//public void onRequestPermissionsResult(int requestCode,
//        String permissions[],int[]grantResults){
//
//        switch(requestCode){
//        case LOCATION_REQUEST_CODE:{
//
//        if(grantResults.length==0
//        ||grantResults[0]!=
//        PackageManager.PERMISSION_GRANTED){
//        Toast.makeText(this,
//        "Unable to show location - permission required",
//        Toast.LENGTH_LONG).show();
//        }else{
//
//        SupportMapFragment mapFragment=
//        (SupportMapFragment)getSupportFragmentManager()
//        .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//        }
//        }
//        }
//

//
//    /**
//     * Manipulates the map when it's available.
//     * The API invokes this callback when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user receives a prompt to install
//     * Play services inside the SupportMapFragment. The API invokes this method after the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        // Add a marker in Sydney, Australia,
//        // and move the map's camera to the same location.
////        LatLng sydney = new LatLng(-33.852, 151.211);
////        googleMap.addMarker(new MarkerOptions().position(sydney)
////                .title("Marker in Sydney"));
////        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        Toast.makeText(getApplicationContext(), "Mark your Position", Toast.LENGTH_SHORT).show();
//
//        if (googleMap != null) {
//            int permission = ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.ACCESS_FINE_LOCATION);
//
//            if (permission == PackageManager.PERMISSION_GRANTED) {
//                googleMap.setMyLocationEnabled(true);
//
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//
//                Marker pos_Marker =  googleMap.addMarker(new MarkerOptions().position(starting).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_laumcher)).title("Starting Location").draggable(false));
//
//                pos_Marker.showInfoWindow();
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_locationpoint, 10));
//                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15),2000, null);
//            } else {
//                requestPermission(
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        LOCATION_REQUEST_CODE);
//            }
//        }
//    }

//


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }