package edu.umass.cs.sensors.mhldemo.Check;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionChecker {

    protected Context context;

    public PermissionChecker(Context context) {
        this.context = context;
    }

    public boolean hasPermission(String permission) {
        boolean hasPermission = true;

        if (ActivityCompat.checkSelfPermission(this.context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            hasPermission = false;
        }

        return hasPermission;
    }

    public boolean requestPermission(Activity activity, String permission) {
        boolean hasPermission = false;

        if (!hasPermission(permission)) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, 1);
        }

        return hasPermission(permission);
    }
}
