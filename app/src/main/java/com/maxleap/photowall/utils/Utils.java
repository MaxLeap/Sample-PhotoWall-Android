package com.maxleap.photowall.utils;

import android.content.Context;
import android.widget.Toast;
import com.maxleap.LocationCallback;
import com.maxleap.MLGeoPoint;
import com.maxleap.MLLocationManager;
import com.maxleap.MLLog;
import com.maxleap.exception.MLException;

public class Utils {

    public static final String TAG = Utils.class.getName();

    private static MLGeoPoint sCurrentLocation;

    private static MLGeoPoint sMockLocation;

    static {
        sMockLocation = new MLGeoPoint(31.154274, 121.430803);
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void getCurrentLocation(long timeout, final LocationCallback doneCallback) {
        MLLocationManager.getCurrentLocationInBackground(timeout, new LocationCallback() {
            @Override
            public void done(final MLGeoPoint geoPoint, final MLException e) {
                if (e != null) {
                    MLLog.e(TAG, e);
                    if (doneCallback != null) doneCallback.done(null, e);
                    return;
                }

                sCurrentLocation = geoPoint;

                if (doneCallback != null) doneCallback.done(getPreviousLocation(), null);
                MLLog.d(TAG, "Current location is " + geoPoint.toString());
            }
        });
    }

    public static MLGeoPoint getPreviousLocation() {
        if (sCurrentLocation == null)
            return new MLGeoPoint(sMockLocation.getLatitude(), sMockLocation.getLongitude());
        return new MLGeoPoint(sCurrentLocation.getLatitude(), sCurrentLocation.getLongitude());
    }
}
