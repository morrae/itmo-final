package itmo.util;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class GPSTracker extends Service implements LocationListener{
	 private final Context mContext;
	    boolean isGPSEnabled = false;
	    boolean isNetworkEnabled = false;
	    boolean canGetLocation = false;
	    Location location;
	    double latitude;
	    double longitude;
	    // минимальное расстояния для обновления
	    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 метров
	    // минимальное время между апдейтами
	    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 минута
	    protected LocationManager locationManager;
	    public GPSTracker(Context context) {
	        this.mContext = context;
	        getLocation();
	    }
	    public Location getLocation() {
	        try {
	            locationManager = (LocationManager) mContext
	                    .getSystemService(LOCATION_SERVICE);

	            // получение статуса gps
	            isGPSEnabled = locationManager
	                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
	            // получения статуса сети
	            isNetworkEnabled = locationManager
	                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	            if (!isGPSEnabled && !isNetworkEnabled) {
	                // нет сети
	            } else {
	                this.canGetLocation = true;
	                if (isNetworkEnabled) {
	                    locationManager.requestLocationUpdates(
	                            LocationManager.NETWORK_PROVIDER,
	                            MIN_TIME_BW_UPDATES,
	                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                    Log.d("Network", "Network");
	                    if (locationManager != null) {
	                        location = locationManager
									.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	                        if (location != null) {
	                            latitude = location.getLatitude();
	                            longitude = location.getLongitude();
	                        }
	                    }
	                }
	                // если gps включен получаем широту и долготу
	                if (isGPSEnabled) {
	                    if (location == null) {
	                        locationManager.requestLocationUpdates(
	                                LocationManager.GPS_PROVIDER,
	                                MIN_TIME_BW_UPDATES,
	                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
	                        Log.d("GPS Enabled", "GPS Enabled");
	                        if (locationManager != null) {
	                            location = locationManager
										.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	                            if (location != null) {
	                                latitude = location.getLatitude();
	                                longitude = location.getLongitude();
	                            }
	                        }
	                    }
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return location;
	    } 
	    // остановка gps
	    public void stopUsingGPS(){
	        if(locationManager != null){
	            locationManager.removeUpdates(GPSTracker.this);
	        }       
	    }  

	    public double getLatitude(){
	        if(location != null){
	            latitude = location.getLatitude();
	        }     
	        return latitude;
	    }   

	    public double getLongitude(){
	        if(location != null){
	            longitude = location.getLongitude();
	        }       
	        return longitude;
	    }   

	    public boolean canGetLocation() {
	        return this.canGetLocation;
	    }   

	    public void showSettingsAlert(){
	        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);       
	        alertDialog.setTitle("Необходимо включить GPS");
	        // Setting Dialog Message
	        alertDialog.setMessage("Хотите перейти в настройки?");
	        // On pressing Settings button
	        alertDialog.setPositiveButton("Настройки", new DialogInterface.OnClickListener() {
	            @Override
				public void onClick(DialogInterface dialog,int which) {
	                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	                mContext.startActivity(intent);
	            }
	        });
	        // on pressing cancel button
	        alertDialog.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
	            @Override
				public void onClick(DialogInterface dialog, int which) {
	            dialog.cancel();
	            }
	        });
	        // Showing Alert Message
	        alertDialog.show();
	    }
	@Override
	public void onLocationChanged(Location location) {
	// TODO Auto-generated method stub
	}
	@Override
	public void onProviderDisabled(String provider) {
	// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub

	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub
	}
	@Override
	public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
	}  
}
