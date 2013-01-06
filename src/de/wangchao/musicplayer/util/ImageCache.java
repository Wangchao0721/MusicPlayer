
package de.wangchao.musicplayer.util;

import de.wangchao.musicplayer.OnlineMusicApi;
import de.wangchao.musicplayer.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;

/**
 * @author yingyixu
 */
public class ImageCache {
    private static final String TAG = "OnlineMusic";
    private static final boolean DEBUG = OnlineMusicApi.DEBUG;

    private static final String CACHE_DIR = Environment.getExternalStorageDirectory()
            + "/onlinemusic/imagecache";
    private static final String TEMP_SUFFIX = ".tmp";

    private HashMap<String, WeakReference<Drawable>> memoryCache;

    private ImageCache() {

        memoryCache = new HashMap<String, WeakReference<Drawable>>();
    }

    private static ImageCache instance;

    public static ImageCache getInstance() {

        if (instance == null) {
            instance = new ImageCache();
        }

        return instance;
    }

    public void getDrawable(final String imageUrl, final ImageView imageView) {

        if (TextUtils.isEmpty(imageUrl) || imageView == null) {
            return;
        }

        new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {

                Drawable drawable = loadImageFromUrl(imageUrl);
                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable result) {

                if (result != null) {
                    imageView.setImageDrawable(result);
                }
            }
        }.execute();

    }

    public void getDrawable(final String imageUrl, final int width, final int height,
            final ImageCallback imageCallBack) {

        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }

        if (width <= 0 || height <= 0) {
            return;
        }

        final String imageName = getImageName(imageUrl);
        if (memoryCache.containsKey(imageName)) {
            WeakReference<Drawable> weakReference = memoryCache.get(imageName);
            Drawable drawable = weakReference.get();
            if (drawable != null && imageCallBack != null) {
                imageCallBack.imageLoaded(drawable, imageUrl);

                if (DEBUG) {
                    Log.i(TAG, "image hitted in memory");
                }
                return;
            }
        }

        new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {

                Drawable drawable = loadImageFromUrl(imageUrl, width, height);
                if (drawable != null) {
                    memoryCache.put(imageName, new WeakReference<Drawable>(drawable));
                }

                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable result) {

                super.onPostExecute(result);

                if (result != null && imageCallBack != null) {
                    imageCallBack.imageLoaded(result, imageUrl);
                }
            }
        }.execute();

    }

    public interface ImageCallback {
        public void imageLoaded(Drawable imageDrawable, String imageUrl);
    }

    // TODO must be modified if the file name can not uniquely describe a image
    public static String getImageName(String imageUrl) {

        String imageName = "";

        if (imageUrl != null && imageUrl.length() != 0) {
            imageName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        }

        return imageName;
    }

    private String fetchCacheFile(String imageUrl) {

        if (imageUrl == null || imageUrl.trim().equalsIgnoreCase("")) {
            return null;
        }

        if (Tools.checkFolderAvailable(CACHE_DIR)) {
            String imageName = getImageName(imageUrl);
            File imageFile = new File(CACHE_DIR, imageName);
            File imageTempFile = new File(CACHE_DIR, imageName + TEMP_SUFFIX);

            if (!imageFile.exists() || imageFile.isDirectory()) {
                try {
                    imageTempFile.createNewFile();
                    FileOutputStream fileOutStream = new FileOutputStream(imageTempFile);
                    InputStream urlInStream = new URL(imageUrl).openStream();

                    int buffer = urlInStream.read();
                    while (buffer != -1) {
                        fileOutStream.write(buffer);
                        buffer = urlInStream.read();
                    }
                    fileOutStream.close();
                    urlInStream.close();

                    imageTempFile.renameTo(imageFile);
                    if (DEBUG) {
                        Log.i(TAG, "image not hitted, load from network");
                    }
                } catch (IOException e) {
                    if (DEBUG) {
                        Log.e(TAG, "image " + e.getMessage());
                    }
                }
            } else {
                if (DEBUG) {
                    Log.i(TAG, "image hitted in sdcard");
                }
            }

            if (imageFile.length() > Runtime.getRuntime().freeMemory() / 2) {
                if (DEBUG) {
                    Log.i(TAG, "image file too large, skip parse");
                }
                if (memoryCache != null) {
                    memoryCache.clear();
                }
                return null;
            }

            return imageFile.getPath();
        }
        return null;
    }

    private Drawable loadImageFromUrl(String imageUrl) {

        Drawable drawable = null;
        String filePath = fetchCacheFile(imageUrl);
        if (!TextUtils.isEmpty(filePath)) {
            drawable = Drawable.createFromPath(filePath);
        }
        return drawable;
    }

    private Drawable loadImageFromUrl(String imageUrl, int reqWidth, int reqHeight) {

        Drawable drawable = null;
        String filePath = fetchCacheFile(imageUrl);
        if (!TextUtils.isEmpty(filePath)) {
            Bitmap bitmap = decodeSampledBitmapFromResource(filePath, reqWidth, reqHeight);
            if (bitmap != null) {
                drawable = new BitmapDrawable(bitmap);
            }
        }
        return drawable;
    }

    public static Bitmap decodeSampledBitmapFromResource(String pathName, int reqWidth,
            int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
            int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public void clearAllCache() {

        if (memoryCache != null) {
            memoryCache.clear();
        }
        Tools.deleteAllFile(CACHE_DIR);
    }
}
