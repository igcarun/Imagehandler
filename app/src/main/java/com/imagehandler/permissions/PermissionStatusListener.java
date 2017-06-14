package com.imagehandler.permissions;

import android.net.Uri;

/**
 * Created by arun on 22/5/17.
 */

public interface PermissionStatusListener {

    void showStatusMsg(boolean isTempDenied, int reqCode);

    void onSuccessStatusMsg(int reqCode, Uri uri);
}
