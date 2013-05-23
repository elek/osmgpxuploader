package net.anzix.osm.upload;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.anzix.osm.upload.data.DaoMaster;
import net.anzix.osm.upload.data.DaoSession;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GpxUploadApplication extends Application {

    DaoMaster daoMaster;

    DaoSession daoSession;

    private List<DirSource> sources = new ArrayList<DirSource>();

    public boolean syncNeeded = false;

    String sdCardPath;

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = new DaoMaster.DevOpenHelper(this, "notes-db", null).getWritableDatabase();

        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        File f = new File("/sdcard");
        if (f.exists()) {
            sdCardPath = getNormalPath(f);
        }
    }

    public String getNormalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            Log.d("OSM", "Can't get canonical path", e);
            return file.getAbsolutePath();
        }
    }

    public String getDisplayPath(String path) {
        if (sdCardPath != null) {
            return path.replaceAll(sdCardPath, "");
        }
        return path;
    }

    public void sync() {
        sources.clear();
        for (Source source : daoSession.getSourceDao().queryBuilder().list()) {
            sources.add(new DirSource(source));
        }

        Map<String, Gpx> gpxes = new HashMap<String, Gpx>();
        for (Gpx gpx : daoSession.getGpxDao().queryBuilder().list()) {
            gpxes.put(gpx.getLocation(), gpx);
        }

        for (DirSource source : sources) {
            for (Gpx gpx : source.getGpxFiles()) {
                if (!gpxes.containsKey(gpx.getLocation())) {
                    daoSession.getGpxDao().insert(gpx);
                } else {
                    gpxes.remove(gpx.getLocation());
                }
            }
        }

        for (String key : gpxes.keySet()) {
            Gpx gpx = gpxes.get(key);
            if (gpx.getUploaded() == null) {
                daoSession.getGpxDao().delete(gpx);
            }
        }
    }

    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public void setDaoMaster(DaoMaster daoMaster) {
        this.daoMaster = daoMaster;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }
}
