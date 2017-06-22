package com.imagehandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.imagehandler.permissions.PermissionHandler;
import com.imagehandler.permissions.PermissionStatusListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public void compressImage() {
        try {
            compressImage(mFilePath, mUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void compressImage(String imagePath, Uri uri) throws FileNotFoundException {
        Bitmap scaledBitmap = null;
        int maxWidth = 1280;
        int maxHeight = 1280;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        Rect rect = new Rect();
        Bitmap bmp = BitmapFactory.decodeStream(mActivity.getContentResolver().openInputStream(uri), rect, options);

        options.inJustDecodeBounds = true;
        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = (float) maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = (float) maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
//        options.inDither = false;
//        options.inPurgeable = true;
//        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        if(bmp!=null)  bmp.recycle();

        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imagePath);

            //write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

}