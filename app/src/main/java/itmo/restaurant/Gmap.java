package itmo.restaurant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import itmo.getset.CustomMarker;
import itmo.getset.Restgetset;
import itmo.restaurant.LatLngInterpolator.LinearFixed;
import itmo.util.ConnectionDetector;

public class Gmap extends FragmentActivity {
    // гугл карта
    private GoogleMap googleMap;
    private HashMap<CustomMarker, Marker> markersHashMap;
    private Iterator<Entry<CustomMarker, Marker>> iter;
    private CameraUpdate cu;
    private CustomMarker customMarkerOne, customMarkerTwo, customMarkerThree;
    Button btn_map, btn_detail;
    String lat, lng, map, nm, ad, id, rate, latitude, longitude;
    SharedPreferences sharedPreferences;
    int locationCount = 0;
    PopupWindow popup;
    ArrayList<Restgetset> rest1;
    int start = 0;
    String name, address, idf, ratting;
    ProgressDialog progressDialog;
    String[] separateddata;
    View layout12;
    Boolean isInternetPresent = false;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmap);

        cd = new ConnectionDetector(getApplicationContext());

        isInternetPresent = cd.isConnectingToInternet();
        if (isInternetPresent) {
            initialize();
            getintent();

            try {
                // Loading map
                initilizeMap();
                initializeUiSettings();
                initializeMapLocationSettings();
                initializeMapTraffic();
                initializeMapType();
                initializeMapViewSettings();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // получаем широту и долготу
            new getrestaudetail().execute();
        } else {
            RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
            if (rl_back == null) {
                Log.d("second", "second");
                RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

                layout12 = getLayoutInflater().inflate(
                        R.layout.connectiondialog, rl_dialoguser, false);

                rl_dialoguser.addView(layout12);
                rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(
                        Gmap.this, R.anim.popup));
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

    private void getintent() {
        // TODO Auto-generated method stub
        Intent iv = getIntent();
        lat = iv.getStringExtra("lat");
        lng = iv.getStringExtra("lng");
        map = iv.getStringExtra("map");
        nm = iv.getStringExtra("nm");
        ad = iv.getStringExtra("ad");
        id = iv.getStringExtra("id");
        rate = iv.getStringExtra("rate");
        latitude = iv.getStringExtra("latitude");
        longitude = iv.getStringExtra("longitude");

        Log.d("lat", "" + lat);
        Log.d("lng", "" + lng);
        Log.d("lat1", "" + latitude);
        Log.d("lng1", "" + longitude);

        try {
            MapsInitializer.initialize(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        btn_map = (Button) findViewById(R.id.btn_map);
        if (map.equals("yes")) {
            btn_map.setVisibility(View.VISIBLE);
        } else {
            btn_map.setVisibility(View.INVISIBLE);
        }
        btn_map.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //Intent iv = new Intent(Gmap.this, Home.class);
                //iv.putExtra("map", "map");
                //	startActivity(iv);

                Gmap.this.finish();
                overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
            }
        });
    }

    private void initialize() {
        //rest1 = Home.rest;

        rest1 = new ArrayList<Restgetset>();

        btn_detail = (Button) findViewById(R.id.btn_detail);
        btn_detail.setVisibility(View.INVISIBLE);
    }

    public class getrestaudetail extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            getdetailforNearMe();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            setCustomMarkerOnePosition();

        }

    }

    private void getdetailforNearMe() {
        // rest.clear();
        URL hp = null;
        try {

            hp = new URL(getString(R.string.liveurl) +
                    "restaurantdetail.php");


            Log.d("URL", "" + hp);
            URLConnection hpCon = hp.openConnection();
            hpCon.connect();
            InputStream input = hpCon.getInputStream();
            Log.d("input", "" + input);

            BufferedReader r = new BufferedReader(new InputStreamReader(input));

            String x = "";
            x = r.readLine();
            String total = "";

            while (x != null) {
                total += x;
                x = r.readLine();
            }
            Log.d("URL", "" + total);
            JSONObject jObject = new JSONObject(total);
            JSONArray j = jObject.getJSONArray("Restaurant");
            // JSONArray j = new JSONArray(total);

            Log.d("URL1", "" + j);
            for (int i = 0; i < j.length(); i++) {

                JSONObject Obj;
                Obj = j.getJSONObject(i);
                Restgetset temp = new Restgetset();
                temp.setName(Obj.getString("name"));
                temp.setId(Obj.getString("id"));
                temp.setAddress(Obj.getString("address"));
                temp.setLatitude(Obj.getString("latitude"));
                temp.setLongitude(Obj.getString("longitude"));
                temp.setRatting(Obj.getString("ratting"));
                temp.setZipcode(Obj.getString("zipcode"));
                temp.setThubnailimage(Obj.getString("thumbnailimage"));
                temp.setVegtype(Obj.getString("vegtype"));
                // temp.setFoodtype(Obj.getString("foodtype"));
                rest1.add(temp);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }

    }

    private void initilizeMap() {

        ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment)).getMapAsync(googleMap1 -> {
            googleMap = googleMap1;
            sharedPreferences = getSharedPreferences("location", 0);

            // Количество сохраненных мест
            locationCount = sharedPreferences.getInt("locationCount", 0);
            // проверка удалось ли создать карту
            if (googleMap == null) {
                Toast.makeText(getApplicationContext(),
                        "Ошибка! Не удалось создать карту", Toast.LENGTH_SHORT).show();
            }

            (findViewById(R.id.mapFragment))
                    .getViewTreeObserver()
                    .addOnGlobalLayoutListener(
                            new android.view.ViewTreeObserver.OnGlobalLayoutListener() {

                                @Override
                                public void onGlobalLayout() {
                                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                                        (findViewById(R.id.mapFragment))
                                                .getViewTreeObserver()
                                                .removeOnGlobalLayoutListener(this);
                                    } else {
                                        (findViewById(R.id.mapFragment))
                                                .getViewTreeObserver()
                                                .removeGlobalOnLayoutListener(this);
                                    }

                                }
                            });

        });

    }

    // показываем позицию на карте
    public void setCustomMarkerOnePosition() {

        if (map.equals("yes")) {

            for (int i = 0; i < rest1.size(); i++) {

                String lat1 = rest1.get(i).getLatitude();
                String lng1 = rest1.get(i).getLongitude();
                name = rest1.get(i).getName() + ":" + rest1.get(i).getId() + ":"
                        + rest1.get(i).getRatting();
                address = rest1.get(i).getAddress();
                idf = rest1.get(i).getId();
                ratting = rest1.get(i).getRatting();
                Log.d("lat1", "" + lat1);
                Log.d("lng1", "" + lng1);
                customMarkerOne = new CustomMarker("markerOne",
                        Double.parseDouble(lat1), Double.parseDouble(lng1));

                // addMarker(customMarkerOne);
                MarkerOptions markerOption = new MarkerOptions()
                        .position(

                                new LatLng(customMarkerOne
                                        .getCustomMarkerLatitude(),
                                        customMarkerOne
                                                .getCustomMarkerLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker())
                        .title(rest1.get(i).getName() + ":"
                                + rest1.get(i).getId() + ":"
                                + rest1.get(i).getRatting() + ":"
                                + rest1.get(i).getAddress());

                Marker newMark = googleMap.addMarker(markerOption);

                addMarkerToHashMap(customMarkerOne, newMark);
                zoomToMarkers(btn_detail);

                // по клику на маркер гугл карты
                googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker arg0) {
                        // arg0.hideInfoWindow();
                        if (map.equals("yes")) {

                            btn_detail.setVisibility(View.VISIBLE);
                            String title = arg0.getTitle();
                            separateddata = title.split(":");
                            Log.d("1", "" + separateddata[0]);
                            Log.d("2", "" + separateddata[1]);
                            Log.d("3", "" + separateddata[2]);
                            String addressmap = separateddata[3].toLowerCase();

                            // показать детали маркера
                            btn_detail.setText("" + separateddata[0] + "\n"
                                    + addressmap);

                        } else {

                            btn_detail.setVisibility(View.VISIBLE);
                            btn_detail.setText("" + separateddata[0] + "Name: "
                                    + nm + "\n" + "Address: " + ad);

                        }

                        return true;
                    }
                });

                // клик на детали конкретного ресторана
                btn_detail.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(Gmap.this, Detailpage.class);
                        intent.putExtra("id", "" + separateddata[1]);
                        intent.putExtra("rating", "" + separateddata[2]);
                        startActivity(intent);
                    }
                });
            }

        } else {
            customMarkerOne = new CustomMarker("markerOne",
                    Double.parseDouble(lat), Double.parseDouble(lng));

            addMarker(customMarkerOne);

        }
    }

    public void showDirections(View view) {
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?" + "saddr=" + latitude
                        + "," + longitude + "&daddr=" + lat + "," + lng));
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }

    void setCustomMarkerTwoPosition() {
        if (map.equals("yes")) {
            customMarkerTwo = new CustomMarker("markerTwo", 21.1200, 73.1200);
            addMarker(customMarkerTwo);
        } else {

        }

    }

    public void startAnimation(View v) {
        animateMarker(customMarkerOne, new LatLng(40.0675716, 40.7297251));
        // animateMarker(customMarkerOne, new LatLng(0.0, 0.0));
    }

    public void zoomToMarkers(View v) {
        zoomAnimateLevelToFitMarkers(120);
    }

    public void animateBack(View v) {
        animateMarker(customMarkerOne, new LatLng(32.0675716, 27.7297251));
    }

    public void initializeUiSettings() {
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    public void initializeMapLocationSettings() {
        googleMap.setMyLocationEnabled(true);
    }

    public void initializeMapTraffic() {
        googleMap.setTrafficEnabled(true);
    }

    public void initializeMapType() {
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
    }

    public void initializeMapViewSettings() {
        googleMap.setIndoorEnabled(true);
        //googleMap.setBuildingsEnabled(false);
    }

    public void setUpMarkersHashMap() {
        if (markersHashMap == null) {
            markersHashMap = new HashMap<CustomMarker, Marker>();
        }
    }

    public void addMarkerToHashMap(CustomMarker customMarker, Marker marker) {
        setUpMarkersHashMap();
        markersHashMap.put(customMarker, marker);
    }

    public Marker findMarker(CustomMarker customMarker) {
        iter = markersHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry mEntry = iter.next();
            CustomMarker key = (CustomMarker) mEntry.getKey();
            if (customMarker.getCustomMarkerId()
                    .equals(key.getCustomMarkerId())) {
                Marker value = (Marker) mEntry.getValue();
                return value;
            }
        }
        return null;
    }

    public void addMarker(CustomMarker customMarker) {

        for (int i = 0; i < rest1.size(); i++) {
            Restgetset tempobj = rest1.get(i);
            Log.d("tempobj", "" + tempobj);
            MarkerOptions markerOption = new MarkerOptions()
                    .position(

                            new LatLng(customMarker.getCustomMarkerLatitude(),
                                    customMarker.getCustomMarkerLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .title(tempobj.getName());

            Marker newMark = googleMap.addMarker(markerOption);

            addMarkerToHashMap(customMarker, newMark);
        }

    }

    public void removeMarker(CustomMarker customMarker) {
        if (markersHashMap != null) {
            if (findMarker(customMarker) != null) {
                findMarker(customMarker).remove();
                markersHashMap.remove(customMarker);
            }
        }
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
        LatLngBounds bounds = b.build();
        Log.d("bounds", "" + bounds);

        cu = CameraUpdateFactory.newLatLngBounds(bounds, 200, 400, 17);
        googleMap.animateCamera(cu);

    }

    public void moveMarker(CustomMarker customMarker, LatLng latlng) {
        if (findMarker(customMarker) != null) {
            findMarker(customMarker).setPosition(latlng);
            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);
        }
    }

    public void animateMarker(CustomMarker customMarker, LatLng latlng) {
        if (findMarker(customMarker) != null) {

            LatLngInterpolator latlonInter = new LinearFixed();
            latlonInter.interpolate(20,
                    new LatLng(customMarker.getCustomMarkerLatitude(),
                            customMarker.getCustomMarkerLongitude()), latlng);

            customMarker.setCustomMarkerLatitude(latlng.latitude);
            customMarker.setCustomMarkerLongitude(latlng.longitude);

            if (android.os.Build.VERSION.SDK_INT >= 14) {
                MarkerAnimation.animateMarkerToICS(findMarker(customMarker),
                        new LatLng(customMarker.getCustomMarkerLatitude(),
                                customMarker.getCustomMarkerLongitude()),
                        latlonInter);
            } else if (android.os.Build.VERSION.SDK_INT >= 11) {
                MarkerAnimation.animateMarkerToHC(findMarker(customMarker),
                        new LatLng(customMarker.getCustomMarkerLatitude(),
                                customMarker.getCustomMarkerLongitude()),
                        latlonInter);
            } else {
                MarkerAnimation.animateMarkerToGB(findMarker(customMarker),
                        new LatLng(customMarker.getCustomMarkerLatitude(),
                                customMarker.getCustomMarkerLongitude()),
                        latlonInter);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gmap, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}
