package com.singhla.lakshay.mapsuser;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,AdapterView.OnItemSelectedListener{

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private DatabaseReference databaseReference;
    List<String> categories = new ArrayList<String>();
    Spinner spinner;
    String number,check, NothingSelected ="Select Buses";
    ArrayList <Marker> markerlist = new ArrayList<>();
    LatLng mylocation;
    Marker myMarker=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        addSpinnerItems();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

    }

    void addSpinnerItems(){

        categories.add(NothingSelected);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    categories.add(ds.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        number = adapterView.getSelectedItem().toString() ;
        if(number.equals(NothingSelected)){
            for(Marker m : markerlist){
                m.remove();
            }
            markerlist.clear();
            return;
        }
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=1;
                for(Marker m : markerlist){
                    m.remove();
                }
                markerlist.clear();
                while( number != null ) {
                    check = dataSnapshot.child(number).child("bus" + index).child("check").getValue(String.class);
                    if (check == null)
                        break;
                    else if (check.equals("false")) {
                        String s = dataSnapshot.child(number).child("bus" + index).child("lat").getValue(String.class);
                        String s1 = dataSnapshot.child(number).child("bus" + index).child("long").getValue(String.class);
                        LatLng pos = new LatLng(Double.parseDouble(s), Double.parseDouble(s1));
                        markerlist.add( mMap.addMarker(new MarkerOptions().position(pos)) );
                    }
                    index++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //User's Loaction Tacking
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(4000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        try {
            /*mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                            }
                        }
                    });*/
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Log.i("Lakshay",  "Changed" );
                    for (Location location : locationResult.getLocations()) {

                        mylocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if(myMarker == null){
                            myMarker = mMap.addMarker(new MarkerOptions()
                                    .position(mylocation)
                                    .anchor(0.5f,0.5f)
                                    .zIndex(1)
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location_marker)));
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
                        }
                        myMarker.setPosition(mylocation);
                    }
                }
            };

            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);

        }
        catch (SecurityException e){

        }

        //Bus Location Tracking
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int index=1;
                for(Marker m : markerlist){
                    m.remove();
                }
                markerlist.clear();
                while( number != null ) {
                    check = dataSnapshot.child(number).child("bus" + index).child("check").getValue(String.class);
                    if (check == null)
                        break;
                    else if (check.equals("false")) {
                        //Toast.makeText(MapsActivity.this,number +" "+index,Toast.LENGTH_SHORT).show();
                        String s = dataSnapshot.child(number).child("bus" + index).child("lat").getValue(String.class);
                        String s1 = dataSnapshot.child(number).child("bus" + index).child("long").getValue(String.class);
                        LatLng pos = new LatLng(Double.parseDouble(s), Double.parseDouble(s1));
                        markerlist.add( mMap.addMarker(new MarkerOptions().position(pos)) );
                    }
                    index++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        myMarker.remove();
        myMarker = null;
        super.onStop();
    }
}
