package com.singhla.lakshay.mapsuser;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback ,AdapterView.OnItemSelectedListener{

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    List<String> categories = new ArrayList<String>();
    Spinner spinner;
    String number,check;
    Marker marker[] = new Marker[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        categories.add("879A  from Shahbad Dairy to Janakpuri");
        categories.add("879A from Janakpuri to Shahbad Dairy");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if(i==0){
            number = "879A from Janakpuri to Shahbad Dairy";
        }
        else if(i==1){
            number = "879A  from Shahbad Dairy to Janakpuri";
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int index=1;
                while(true) {
                    check = dataSnapshot.child(number).child("bus" + index).child("check").getValue(String.class);
                    if (check == null)
                        break;
                    else if (check.equals("false")) {
                        Toast.makeText(MapsActivity.this,number +" "+index,Toast.LENGTH_SHORT).show();
                        String s = dataSnapshot.child(number).child("bus" + index).child("lat").getValue(String.class);
                        String s1 = dataSnapshot.child(number).child("bus" + index).child("long").getValue(String.class);

                        if (marker[index-1] != null) {
                            marker[index-1].remove();
                        }
                        LatLng sydney = new LatLng(Double.parseDouble(s), Double.parseDouble(s1));
                        marker[index-1] = mMap.addMarker(new MarkerOptions().position(sydney));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                        mMap.setMinZoomPreference(10.0f);
                        Toast.makeText(MapsActivity.this, "Lat:" + s + "Long: " + s1, Toast.LENGTH_SHORT).show();
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

    }
}
