package com.example.akabhi.mapactivty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private SupportMapFragment supportMapFragment;
    private TextView currentLocation;
    private GoogleApiClient googleApiClient;
    private LocationRequest mLocationRequest;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Marker mCurrLocationMarker, markerLine1, markerLine2;
    private Circle circle;
    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        currentLocation = findViewById(R.id.currentLocation);
        swipeRefreshLayout = findViewById(R.id.swiperefreshlocation);
        Load_permission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    protected void Load_permission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
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
        if (mMap != null) {
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    LatLng latLng = marker.getPosition();
                    double lat = latLng.latitude;
                    double log = latLng.longitude;
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, log, 1);
                        marker.setTitle(addresses.get(0).getAddressLine(0));
                        marker.showInfoWindow();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View view = getLayoutInflater().inflate(R.layout.markerlayout, null);
                    ImageView imageView = view.findViewById(R.id.markerImage);
                    imageView.setImageResource(R.drawable.ic_placeholder);
                    TextView location = view.findViewById(R.id.location);
                    TextView message = view.findViewById(R.id.message);
                    TextView lat = view.findViewById(R.id.log);
                    TextView log = view.findViewById(R.id.lat);

                    LatLng latLng = marker.getPosition();
                    location.setText(marker.getTitle());
                    message.setText(marker.getSnippet());
                    lat.setText("latitude " + latLng.latitude);
                    log.setText("longitude " + latLng.longitude);

                    return view;
                }
            });
        }
        builderAPIGoogleCLint();
    }

    private void builderAPIGoogleCLint() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest().create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
        } else {
            try {
                swipeRefreshLayout.setRefreshing(false);
                double latiude = location.getLatitude();
                double longitude = location.getLongitude();

                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(latiude, longitude, 1);
                String newAddress = addresses.get(0).getAddressLine(0);

                LatLng latLng = new LatLng(latiude, longitude);
                AddMarker(newAddress, latLng);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void AddMarker(String newAddress, LatLng latLng) {
//        if (mCurrLocationMarker != null) {
//            removeEverThing();
//        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.snippet("current location");
        markerOptions.title(newAddress);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        if (markerLine1 == null) {
            markerLine1 = mMap.addMarker(markerOptions);
        } else if (markerLine2 == null) {
            markerLine2 = mMap.addMarker(markerOptions);
            drawLine();
        } else {
            removeEverThing();
            markerLine1 = mMap.addMarker(markerOptions);
        }

        mCurrLocationMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 25);
        mMap.animateCamera(cameraUpdate);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        currentLocation.setText(newAddress);

        circle = drawCircle(latLng);
    }

    private void drawLine() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(markerLine1.getPosition());
        polylineOptions.add(markerLine2.getPosition());
        polylineOptions.color(R.color.colorPrimary);
        polylineOptions.width(3);

        polyline = mMap.addPolyline(polylineOptions);
    }

    private void removeEverThing() {
        markerLine1.remove();
        markerLine1 = null;
        markerLine2.remove();
        markerLine2 = null;
        polyline.remove();
        polyline = null;
        mCurrLocationMarker.remove();
        mCurrLocationMarker = null;
        circle.remove();
        circle = null;
    }

    private Circle drawCircle(LatLng latLng) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.fillColor(0x33fff000);
        circleOptions.radius(5);
        circleOptions.strokeColor(R.color.colorPrimary);
        circleOptions.strokeWidth(3);

        return mMap.addCircle(circleOptions);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
