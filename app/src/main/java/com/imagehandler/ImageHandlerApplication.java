package com.imagehandler;

import android.app.Application;
import android.widget.WrapperListAdapter;

import com.imagehandler.permissions.PermissionHandler;

/**
 * Created by arun on 8/6/17.
 */

public class ImageHandlerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PermissionHandler.permissionInitialize();
    }
}
