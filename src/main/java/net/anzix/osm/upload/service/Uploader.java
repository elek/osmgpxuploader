package net.anzix.osm.upload.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;
import net.anzix.osm.upload.Constants;
import net.anzix.osm.upload.GpxUploadApplication;
import net.anzix.osm.upload.Preferences;
import net.anzix.osm.upload.R;
import net.anzix.osm.upload.data.DaoSession;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.GpxDao;
import net.anzix.osm.upload.source.SourceHandler;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Service to upload tracks in the background.
 */
public class Uploader extends IntentService {

    public static final String FILE_PATH = "PATH";
    public static final String TYPE = "TYPE";
    public static final String NAME = "NAME";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String TAGS = "TAGS";
    public static final String VISIBILITY = "VISIBILITY";
    public static final String GPX_ID = "GPX_ID";


    private GpxUploadApplication app;
    private SharedPreferences preferences;
    private NotificationManager notificationManager;
    private Notification myNotification;

    public Uploader() {
        super("Uploader service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = (GpxUploadApplication) getApplication();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String result = null;
        myNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("GPX uploader")
                .setContentText("Uploading gpx track...")
                .setTicker("Uploading gpx...")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setSmallIcon(R.drawable.av_upload)
                .build();
        notificationManager.notify(0, myNotification);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Gpx gpx = new Gpx();
        gpx.setLocation(intent.getStringExtra(FILE_PATH));
        gpx.setType(intent.getStringExtra(TYPE));
        gpx.setId(intent.getLongExtra(GPX_ID, 0));
        try {


            String description = intent.getStringExtra(DESCRIPTION);
            String tags = intent.getStringExtra(TAGS);
            String visibility = intent.getStringExtra(VISIBILITY);


            SourceHandler sh = app.getSourceHandle(gpx.getType());
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost postRequest = new HttpPost("http://api.openstreetmap.org/api/0.6/gpx/create");
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            InputStream s = sh.createStream(gpx);
            reqEntity.addPart("file", new InputStreamBody(s, gpx.getName()));
            reqEntity.addPart("description", new StringBody(description));
            reqEntity.addPart("tags", new StringBody(tags));

            reqEntity.addPart("visibility", new StringBody("" + visibility));

            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
                    preferences.getString(Preferences.OSM_USER_NAME, ""),
                    preferences.getString(Preferences.OSM_PASSWORD, ""));

            postRequest.addHeader(BasicScheme.authenticate(creds, "US_ASCII", false));
            postRequest.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(postRequest);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            String sResponse;
            StringBuilder str = new StringBuilder();

            while ((sResponse = reader.readLine()) != null) {
                str.append(sResponse);
            }
            if (response.getStatusLine().getStatusCode() == 200) {
                GpxUploadApplication app = (GpxUploadApplication) getApplication();
                DaoSession session = app.getDaoSession();
                String path = gpx.getLocation();

                Gpx ngpx = session.getGpxDao().queryBuilder().where(GpxDao.Properties.Location.eq(path)).build().unique();
                if (ngpx != null) {
                    ngpx.setUploaded(new Date());
                    session.update(ngpx);
                    gpx = ngpx;
                } else {
                    ngpx = new Gpx();
                    ngpx.setType("dir");
                    ngpx.setLocation(path);
                    ngpx.setCreated(new Date(new File(path).lastModified()));
                    ngpx.setUploaded(new Date());
                    session.insert(ngpx);
                    gpx = ngpx;
                }
                app.syncNeeded = true;
                result = "File uploaded sucessfully";
            } else {
                result = "ERROR: " + str + " " + response.getStatusLine().getStatusCode();
            }
        } catch (Exception e) {
            Log.e(e.getClass().getName(), e.getMessage(), e);
            result = "ERROR: " + e.getMessage();

        }


        myNotification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("GPX uploader")
                .setContentText(result)
                .setTicker("GPX is uploaded")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.av_upload)
                .build();

        notificationManager.notify(0, myNotification);
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(Constants.BROADCAST_INSERTED);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra("GPX_ID", gpx.getId());
        sendBroadcast(intentUpdate);

    }
}
