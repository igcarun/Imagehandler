package com.imagehandler;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mParentLayout = (RelativeLayout) findViewById(R.id.activity_image_rl);
        mImageView = (ImageView) findViewById(R.id.activity_image_iv);
        if (savedInstanceState == null) {
            imageHandler();
        }

    }

    private void imageHandler() {
        ImageHandler.getInstance().with(this).setPermissionListener(this, PERMISSION_SEETINGS_REQ_CODE)
                .addPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .setImagePath(getApplicationInfo().loadLabel(getPackageManager()).toString(), "profile_picture", "jpg").build();
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
        if (requestCode == PERMISSION_SEETINGS_REQ_CODE){
            imageHandler();
        } else if (requestCode == IMAGE_CAPTURE_REQ_CODE) {
            Picasso.with(this).load(mUri).into(mImageView);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}