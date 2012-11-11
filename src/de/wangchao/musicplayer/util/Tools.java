
package de.wangchao.musicplayer.util;

import de.wangchao.musicplayer.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class Tools {
    static boolean DEBUG = true;

    public static boolean checkMediaAvailable() {

        boolean mExternalStorageAvailable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = false;
        }
        return mExternalStorageAvailable;
    }

    public static boolean checkMediaWriteable() {

        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageWriteable = false;
        }
        return mExternalStorageWriteable;
    }

    public static void debugLog(String tag, String msg) {

        if (DEBUG) {
            Log.v(tag, msg + "");
        }
    }

    /*
     * Try to use String.format() as little as possible, because it creates a
     * new Formatter every time you call it, which is very inefficient. Reusing
     * an existing Formatter more than tripled the speed of makeTimeString().
     * This Formatter/StringBuilder are also used by makeAlbumSongsLabel()
     */
    private static StringBuilder sFormatBuilder = new StringBuilder();
    private static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    public static String makeTimeString(Context context, long secs) {

        String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort
                : R.string.durationformatlong);

        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;

        return sFormatter.format(durationformat, timeArgs).toString();
    }

    /**
     * delete folder
     * 
     * @param filePath folder path
     * @return boolean
     */
    public static boolean deleteFolder(String folderPath) {

        if (!checkMediaWriteable()) {
            return false;
        }

        if (!deleteAllFile(folderPath)) {
            return false;
        }
        File folderFile = new File(folderPath);
        return folderFile.delete();
    }

    /**
     * Delete all files in a folder, remain the folder
     * 
     * @param folder path
     */
    public static boolean deleteAllFile(String path) {

        if (!checkMediaWriteable()) {
            return false;
        }

        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }
        String[] fileList = file.list();
        File tempFile = null;
        for (int i = 0; i < fileList.length; i++) {
            if (path.endsWith(File.separator)) {
                tempFile = new File(path + fileList[i]);
            } else {
                tempFile = new File(path + File.separator + fileList[i]);
            }
            if (tempFile.isFile()) {
                tempFile.delete();
            } else if (tempFile.isDirectory()) {
                if (!deleteFolder(path + "/" + fileList[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check whether the folder is available and create it if it does not exist.
     * 
     * @param filePath
     * @return
     */
    public static boolean checkFolderAvailable(String filePath) {

        if (!Tools.checkMediaAvailable()) {
            return false;
        }
        if (!Tools.checkMediaWriteable()) {
            return false;
        }
        File folderFile = new File(filePath);
        if (!folderFile.exists() || !folderFile.isDirectory()) {
            folderFile.mkdirs();
        }
        return true;
    }

    /**
     * Get the 32bits MD5 string
     * 
     * @param str
     * @return
     */
    public static String getMD5Str(String str) {

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString();
    }

    public static String getMD5Password(String password) {

        String firstStr = getMD5Str(password);
        Tools.debugLog("MD5 First Password", firstStr);
        String secondStr = getMD5Str(firstStr.substring(1, 9));
        Tools.debugLog("MD5 Second Password", secondStr);
        return secondStr;
    }

    public static long getAvailableStorage() {

        String storageDirectory = null;
        storageDirectory = Environment.getExternalStorageDirectory().toString();

        try {
            StatFs stat = new StatFs(storageDirectory);
            long avaliableSize = ((long) stat.getAvailableBlocks() * (long) stat.getBlockSize());
            return avaliableSize;
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED
                            || info[i].getState() == NetworkInfo.State.CONNECTING) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isTextEmpty(String text) {

        if (text == null || text.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void exitDialog(final Activity a) {

        AlertDialog.Builder builder = new Builder(a);
        builder.setMessage("确定要退出吗?");
        builder.setTitle("提示");

        builder.setPositiveButton("确认", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                a.finish();
            }
        });
        builder.setNegativeButton("取消", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
