package com.imagehandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;

import com.imagehandler.permissions.PermissionHandler;
import com.imagehandler.permissions.PermissionStatusListener;

import java.io.File;

/**
 * Created by arun on 8/6/17.
 */

public class ImageHandler {

    private static ImageHandler sImageHandler;
    private String[] mPermissons;
    private String mDirectoryName;
    private String mSubDirectoryName;
    private String mImageFileName;
    private String mImageExtension;
    private Activity mActivity;
    private int mPermissionSettingsReqCode;
    private PermissionStatusListener mPermissionStatusListener;
    private String mFilePath;
    private Uri mUri;

    /**
     * Builder class for ImageHandler
     */
    public static ImageHandler getInstance() {
        if (sImageHandler == null) {
            sImageHandler = new ImageHandler();
        }
        return sImageHandler;
    }


    /**
     * @param activity instance of activity class
     * @return
     */
    public ImageHandler with(Activity activity) {
        mActivity = activity;
        return this;
    }


    /**
     * add runtime permissions
     *
     * @param permission list of permissions
     */
    public ImageHandler addPermissions(String[] permission) {
        mPermissons = permission;
        return this;
    }

    /**
     * set Image path in
     *
     * @param directoryName  image directory name
     * @param imageFileName  image file name
     * @param imageExtension image extension like png/jpg
     */
    public ImageHandler setImagePath(String directoryName, String imageFileName, String imageExtension) {
        mDirectoryName = directoryName;
        mImageFileName = imageFileName;
        mImageExtension = imageExtension;
        return this;
    }

    /**
     * set Image path in
     *
     * @param directoryName  image directory name
     * @param subDirectoryName image sub directory name
     * @param imageFileName  image file name
     * @param imageExtension image extension like png/jpg
     */
    public ImageHandler setImagePath(String directoryName, String subDirectoryName, String imageFileName, String imageExtension) {
        mDirectoryName = directoryName;
        mSubDirectoryName = subDirectoryName;
        mImageFileName = imageFileName;
        mImageExtension = imageExtension;
        return this;
    }

    public ImageHandler build() {

        mFilePath = getImagePath();

        File file = new File(mFilePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mUri = FileProvider.getUriForFile(mActivity, "com.example.android.fileprovider", file);
        } else {
            mUri = Uri.fromFile(file);
        }

        if (!isPermissionEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionsList();
        } else {
            Log.i("Permission misbehave", "ImageHandler");
            mPermissionStatusListener.onSuccessStatusMsg(mPermissionSettingsReqCode, mUri);
        }
        return this;
    }

    private String getImagePath() {
        return new FilePickUtils()
                .setDirectoryName(mDirectoryName)
                .setSubDirectoryName(mSubDirectoryName)
                .setFileName(mImageFileName)
                .setExtensionName(mImageExtension)
                .build();
    }

    public void permissionsList() {
        PermissionHandler.getInstance().requestPermission(mActivity, mPermissionStatusListener)
                .setRequestPermissions(mPermissons)
                .setUri(mUri)
                .setRequestCode(mPermissionSettingsReqCode).builder();
    }


    private boolean isPermissionEmpty() {
        return mPermissons == null || mPermissons.length == 0;
    }

    public ImageHandler setPermissionListener(PermissionStatusListener permissionStatusListener, int permissionSeetingsReqCode) {
        mPermissionStatusListener = permissionStatusListener;
        mPermissionSettingsReqCode = permissionSeetingsReqCode;
        return this;
    }
}