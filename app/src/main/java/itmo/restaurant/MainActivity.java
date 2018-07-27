package itmo.restaurant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import itmo.getset.CustomMarker;
import itmo.util.ConnectionDetector;
import itmo.util.GPSTracker;


public class MainActivity extends FragmentActivity {


    LocationManager locManager;
    Drawable drawable;
    Document document;
    v2GetRouteDirection v2GetRouteDirection;
    LatLng fromPosition;
    LatLng toPosition;
    GoogleMap mGoogleMap;
    MarkerOptions markerOptions;
    Location location;
    String lat, lng, map, nm, ad, id, rate;
    double destlat, destlng, curlst, curlng;
    GPSTracker gps;
    double latitude;
    double longitude;
    Button btn_detail;
    private HashMap<CustomMarker, Marker> markersHashMap;
    private Iterator<Entry<CustomMarker, Marker>> iter;
    private CameraUpdate cu;
    private CustomMarker customMarkerOne, customMarkerTwo;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    View layout12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cd = new ConnectionDetector(getApplicationContext());

        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {

            btn_detail = (Button) findViewById(R.id.btn_detail);
            markersHashMap = new HashMap<CustomMarker, Marker>();
            MapsInitializer.initialize(getApplicationContext());

            gps = new GPSTracker(MainActivity.this);
            // проверка работы gps
            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

            } else {
                // Не работает gps, просим его вкллючить
                gps.showSettingsAlert();
            }
            Intent iv = getIntent();
            lat = iv.getStringExtra("lat");
            lng = iv.getStringExtra("lng");
            map = iv.getStringExtra("map");
            nm = iv.getStringExtra("nm");
            ad = iv.getStringExtra("ad");
            id = iv.getStringExtra("id");
            rate = iv.getStringExtra("rate");

            try {
                destlat = Double.parseDouble(lat);
                destlng = Double.parseDouble(lng);
                // curlst = Double.parseDouble(latitude);
                // curlng = Double.parseDouble(longitude);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            v2GetRouteDirection = new v2GetRouteDirection();
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            supportMapFragment.getMapAsync(googleMap -> {
                googleMap = mGoogleMap;
                // включаем MyLocation в гуглкартах
                mGoogleMap.setMyLocationEnabled(true);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
                mGoogleMap.getUiSettings().setCompassEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
                mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
                mGoogleMap.setTrafficEnabled(true);
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(-5));
                markerOptions = new MarkerOptions();
                fromPosition = new LatLng(latitude, longitude);
                // fromPosition = new LatLng(40.7127, -74.006086);
                // toPosition = new LatLng(destlat, destlng);
                toPosition = new LatLng(21.2049, 72.8406);
                GetRouteTask getRoute = new GetRouteTask();

                // рисуем дорогу от текущего места до назаначения
                getRoute.execute();

        });


    } else

    {
        // Нет интернета
        RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        if (rl_back == null) {
            Log.d("second", "second");
            RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

            layout12 = getLayoutInflater().inflate(
                    R.layout.connectiondialog, rl_dialoguser, false);

            rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(
                    MainActivity.this, R.anim.popup));
            Button btn_yes = (Button) layout12.findViewById(R.id.btn_yes);
            btn_yes.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

}

private class GetRouteTask extends AsyncTask<String, Void, String> {

    private ProgressDialog Dialog;
    String response = "";

    @Override
    protected void onPreExecute() {
        Dialog = new ProgressDialog(MainActivity.this);
        Dialog.setMessage("Загружаем путь...");
        Dialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
        // получаем значения пути
        document = v2GetRouteDirection.getDocument(fromPosition,
                toPosition,
                itmo.restaurant.v2GetRouteDirection.MODE_DRIVING);
        response = "Успешно";
        return response;

    }

    @Override
    protected void onPostExecute(String result) {
        mGoogleMap.clear();
        if (response.equalsIgnoreCase("Успешно")) {
            ArrayList<LatLng> directionPoint = v2GetRouteDirection
                    .getDirection(document);
            PolylineOptions rectLine = new PolylineOptions().width(10)
                    .color(Color.RED);

            for (int i = 0; i < directionPoint.size(); i++) {
                rectLine.add(directionPoint.get(i));
            }
            // добавляем путь на карту
            mGoogleMap.addPolyline(rectLine);
            markerOptions.position(toPosition);
            markerOptions.draggable(true);
            customMarkerOne = new CustomMarker("markerOne", latitude,
                    longitude);
            customMarkerTwo = new CustomMarker("markerOne", 21.2049,
                    72.8406);
            Marker newMark = mGoogleMap.addMarker(markerOptions);
            // mGoogleMap.addMarker(markerOptions);

            addMarkerToHashMap(customMarkerOne, newMark);
            addMarkerToHashMap(customMarkerTwo, newMark);
            zoomToMarkers(btn_detail);
            mGoogleMap
                    .setOnMarkerClickListener(new OnMarkerClickListener() {

                        @Override
                        public boolean onMarkerClick(Marker arg0) {
                            Button btn_detail = (Button) findViewById(R.id.btn_detail);
                            btn_detail.setVisibility(View.VISIBLE);
                            btn_detail.setText("" + arg0.getPosition()
                                    + "\n" + "Название: " + nm + "\n"
                                    + "Адрес: " + ad);
                            return false;
                        }
                    });
        }

        Dialog.dismiss();
    }

}

    public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);

    }

    public void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap<CustomMarker, Marker>();
        }
    }

    public void zoomToMarkers(View v) {
        zoomAnimateLevelToFitMarkers(120);
    }

    public void zoomAnimateLevelToFitMarkers(int padding) {
        iter = markersHashMap.entrySet().iterator();
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        LatLng ll = null;
        while (iter.hasNext()) {
            Map.Entry mEntry = iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();

            ll = new LatLng(key.getCustomMarkerLatitude(),
                    key.getCustomMarkerLongitude());
            b.include(ll);
        }
        try {
            LatLngBounds bounds = b.build();
            Log.d("bounds", "" + bounds);
            cu = CameraUpdateFactory.newLatLngBounds(bounds, 25);
            mGoogleMap.animateCamera(cu);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

}
