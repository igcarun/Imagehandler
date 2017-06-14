package com.imagehandler;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by arun on 8/6/17.
 */

public class FilePickUtils {

    private String mDirectoryName;
    private String mSubDirectoryName;
    private String mFileName;
    private String mImageExtensionName = "jpg";
    private String mFilePath;

    public FilePickUtils setDirectoryName(String directoryName) {
        mDirectoryName = directoryName;
        return this;
    }

    public FilePickUtils setSubDirectoryName(String subDirectoryName) {
        mSubDirectoryName = subDirectoryName;
        return this;
    }

    public FilePickUtils setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }

    public FilePickUtils setExtensionName(String extensionName) {
        mImageExtensionName = extensionName;
        return this;
    }

    public String build() {

        mFilePath = new File(Environment.getExternalStorageDirectory()+"").getAbsolutePath();

        if (mDirectoryName != null && mDirectoryName.length() > 0) {
            mFilePath += "/"+mDirectoryName;

            if (!isDirectoryExists()) {
                File file = new File(mFilePath);
                file.mkdirs();
            }
        }

        if (mSubDirectoryName != null && mSubDirectoryName.length() > 0) {
            mFilePath += "/"+mSubDirectoryName;
            if (!isSubDirectoryExists()) {
                File file = new File(mFilePath);
                file.mkdirs();
            }
        }

        if (mFileName != null && mFileName.length() > 0) {
            mFilePath += "/"+mFileName+"."+mImageExtensionName;
            File file = new File(mFilePath);
            if (isFileNameExists()) {
                file.delete();
            }
        } else {
            mFileName = (new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())).
                                                     format(new Date()) + "." + mImageExtensionName;
            File file = new File(mFilePath,mFileName);
            if (isFileNameExists()) {
                file.delete();
            }
        }
        return mFilePath;
    }


    private boolean isDirectoryExists() {
        File file = new File(mFilePath);
        return file.isDirectory() && file.exists();
    }

    private boolean isSubDirectoryExists() {
        File file = new File(mFilePath);
        return file.isDirectory() && file.exists();
    }

    private boolean isFileNameExists() {
        File file = new File(mFilePath);
        return file.isDirectory() && file.exists();
    }

}