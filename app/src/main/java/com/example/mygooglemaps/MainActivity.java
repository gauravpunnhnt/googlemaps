package com.example.mygooglemaps;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean mLocationPermissionGranted;
    private static final int LOCATION_REQUEST_CODE = 007;
    private final int PLAY_SERVICES_ERROR_CODE = 006;
    private GoogleMap mMap;
    public static final String TAG = "GoogleMaps";

    private ImageButton btnLocate;
    private EditText etAddress;

    private final double PAONTA_SAHIB_LAT = 30.443989;
    private final double PAONTA_SAHIB_LNG = 77.605999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etAddress = findViewById(R.id.et_address);
        btnLocate = findViewById(R.id.btn_locate);
        btnLocate.setOnClickListener(this::geolocate);

        FloatingActionButton fab = findViewById(R.id.fab);
        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMap != null){
                    mMap.animateCamera(CameraUpdateFactory.zoomBy(5));

                    LatLng latLng = new LatLng(28.6139, 77.2090);

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,14);
                    mMap.animateCamera(cameraUpdate, 3000, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(new LatLng(28.6139, 77.2090));
                            mMap.addMarker(markerOptions);
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    double bottomBoundry = PAONTA_SAHIB_LAT - 0.03;
                    double leftBoudry = PAONTA_SAHIB_LNG - 0.03;
                    double topBoundry = PAONTA_SAHIB_LAT + 0.03;
                    double rightBoundry = PAONTA_SAHIB_LNG + 0.03;

                    LatLngBounds PAONTA_BOUNDS = new LatLngBounds(
                            new LatLng(bottomBoundry,leftBoudry),
                            new LatLng(topBoundry,rightBoundry)
                    );

                    mMap.setLatLngBoundsForCameraTarget(PAONTA_BOUNDS);
                    //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(PAONTA_BOUNDS,1));
                    //showMarker(PAONTA_BOUNDS.getCenter());
                }
            }
        });*/

        initGoogleMap();

        SupportMapFragment supportMapFragment = /*SupportMapFragment.newInstance();*/ (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment_container);

        /*getSupportFragmentManager().beginTransaction()
                .add(R.id.map_fragment_container,supportMapFragment)
                .commit();*/
        supportMapFragment.getMapAsync(this);
    }

    public void geolocate(View view){
        hideSoftKeyboard(view);

        String locationName = etAddress.getText().toString();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(locationName,1);

            if (addressList.size()>0){
                Address address = addressList.get(0);
                gotoLocation(address.getLatitude(),address.getLongitude());
                mMap.addMarker(new MarkerOptions().position(new LatLng(address.getLatitude(),address.getLongitude())));

                Toast.makeText(this, address.getLocality() ,Toast.LENGTH_SHORT).show();

                Log.d(TAG,"geoLocate: Country: "+address.getLocality());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void hideSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);
    }


    private void initGoogleMap() {

        if (isServicesOk()){
            if (checkLocationPermission()){
                Toast.makeText(this,"Ready to Map!",Toast.LENGTH_SHORT);
            }else {
                requestLocationPermission();
            }
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isServicesOk() {

        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();

        int result = googleApi.isGooglePlayServicesAvailable(this);

        if ( result == ConnectionResult.SUCCESS){
            return true;
        }else if (googleApi.isUserResolvableError(result)){
            Dialog dialog = googleApi.getErrorDialog(this,result,PLAY_SERVICES_ERROR_CODE,
                    task->Toast.makeText(this,"Dialog is cancelled by User",Toast.LENGTH_SHORT).show());
            dialog.show();
        }else {
            Toast.makeText(this,"Google Play Servies are needed",Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this,"Permission not granted",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"onMapReady:map is showing on the screen");

        mMap = googleMap;
        gotoLocation(PAONTA_SAHIB_LAT,PAONTA_SAHIB_LNG);

        //MarkerOptions markerOptions = new MarkerOptions()
          //      .position(new LatLng(PAONTA_SAHIB_LAT,PAONTA_SAHIB_LNG));
        //mMap.addMarker(markerOptions);

        /*mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.setIndoorEnabled(false);*/
    }

    private void gotoLocation(double lat,double lng){

        LatLng latLng = new LatLng(lat,lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,14);
        mMap.moveCamera(cameraUpdate);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    public void showMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        mMap.addMarker(markerOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){

            case R.id.maptype_none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;

            case R.id.maptype_normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.maptype_satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.maptype_terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.maptype_hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}