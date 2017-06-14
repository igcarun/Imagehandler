package com.imagehandler.permissions;

/**
 * Created by arun on 24/5/17.
 */

public interface PermissionsListener {

    void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
}
