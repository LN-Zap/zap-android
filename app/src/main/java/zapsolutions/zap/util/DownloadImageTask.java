package zapsolutions.zap.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String LOG_TAG = DownloadImageTask.class.getName();

    private WeakReference<ImageView> imageViewReference;

    public DownloadImageTask(WeakReference<ImageView> imageViewReference) {
        this.imageViewReference = imageViewReference;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap image = null;
        try {
            InputStream in = new URL(url).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            ZapLog.d(LOG_TAG, e.getMessage());
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        if (imageViewReference.get() != null && result != null) {
            imageViewReference.get().setImageBitmap(result);
        }
    }
}

