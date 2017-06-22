package com.imagehandler;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.imagehandler.permissions.PermissionHandler;
import com.imagehandler.permissions.PermissionStatusListener;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity implements PermissionStatusListener {

    private static final int PERMISSION_SEETINGS_REQ_CODE = 45;
    private static final int IMAGE_CAPTURE_REQ_CODE = 46;
    private RelativeLayout mParentLayout;
    private ImageView mImageView;
    private Uri mUri;

    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Button button = (Button) findViewById(R.id.activity_image_b);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageActivity.this, TestActivity.class);
                startActivity(intent);
            }
        });
        mParentLayout = (RelativeLayout) findViewById(R.id.activity_image_rl);
        mImageView = (ImageView) findViewById(R.id.activity_image_iv);
        if (savedInstanceState == null) {
            imageHandler();
        }
        Log.i("ImageActivity", "onCreate called");
    }

    private void imageHandler() {
        ImageHandler.getInstance().with(this).setPermissionListener(this, PERMISSION_SEETINGS_REQ_CODE)
                .addPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .setImagePath(getApplicationInfo().loadLabel(getPackageManager()).toString(), "profile_picture", "webp").build();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHandler.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void showStatusMsg(final boolean isTempDenied, final int reqCode) {
        Snackbar.make(mParentLayout, "Need Permission to continue",
                Snackbar.LENGTH_INDEFINITE).setAction("Continue", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTempDenied) {
                    ImageHandler.getInstance().permissionsList();
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, reqCode);
                }
            }
        }).show();
    }

    @Override
    public void onSuccessStatusMsg(int reqCode, Uri uri) {
        switch (reqCode) {
            case PERMISSION_SEETINGS_REQ_CODE:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mUri = uri;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, IMAGE_CAPTURE_REQ_CODE);
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_SEETINGS_REQ_CODE) {
            imageHandler();
        } else if (requestCode == IMAGE_CAPTURE_REQ_CODE) {
            ImageHandler.getInstance().compressImage();
            Picasso.with(this).load(mUri).into(mImageView);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}