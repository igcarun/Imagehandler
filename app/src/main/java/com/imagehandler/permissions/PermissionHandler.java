package com.imagehandler.permissions;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arun on 24/5/17.
 */

public class PermissionHandler implements PermissionsListener {

    private static PermissionHandler sPermissionHandler;
    private PermissionStatusListener mPermissionStatusListener;
    private Activity mActivity;
    private int mReqCode;
    private String[] mPermissions;
    private Uri mUri;

    public static void permissionInitialize() {
        if (sPermissionHandler == null) {
            sPermissionHandler = new PermissionHandler();
        }
    }

    public static PermissionHandler getInstance() {
        return sPermissionHandler;
    }

    public PermissionHandler requestPermission(Activity activity, PermissionStatusListener permissionStatusListeners) {
        mPermissionStatusListener = permissionStatusListeners;
        mActivity = activity;
        return sPermissionHandler;
    }

    public PermissionHandler setRequestPermissions(String[] permissions) {
        mPermissions = permissions;
        return sPermissionHandler;
    }

    public PermissionHandler setUri(Uri uri) {
        mUri = uri;
        return sPermissionHandler;
    }

    public PermissionHandler setRequestCode(int reqCode) {
        mReqCode = reqCode;
        return sPermissionHandler;
    }

    public void builder() {
        getPermissions(mPermissions);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean isTempDenied = false;
        boolean isPermDenied = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                isTempDenied = true;
            } else {
                if (ActivityCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED) {
                    Log.e("allowed", permission);
                } else {
                    //set to never ask again
                    isPermDenied = true;
                    Log.e("set to never ask again", permission);
                    //do something here.
                }
            }
        }

        if (isTempDenied) {
            mPermissionStatusListener.showStatusMsg(isTempDenied, mReqCode);
        } else if (isPermDenied) {
            mPermissionStatusListener.showStatusMsg(isTempDenied, mReqCode);
        } else {
            Log.i("Permission misbehave", "Permissionhandler 1");
            mPermissionStatusListener.onSuccessStatusMsg(mReqCode, mUri);
        }
    }

    private void getPermissions(String[] permissions) {
        boolean isPermissionsSuccess = true;
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionSuccess(permission)) {
                isPermissionsSuccess = false;
                permissionList.add(permission);
            }
        }

        if (isPermissionsSuccess) {
            Log.i("Permission misbehave", "Permissionhandler 2");
            mPermissionStatusListener.onSuccessStatusMsg(mReqCode, mUri);
        } else {
            ActivityCompat.requestPermissions(mActivity, permissionList.toArray(new String[0]), mReqCode);
        }
    }

    private boolean isPermissionSuccess(String permission) {
        return ActivityCompat.checkSelfPermission(mActivity, permission) == 0;
    }


}