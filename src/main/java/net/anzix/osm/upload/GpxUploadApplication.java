package net.anzix.osm.upload;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import net.anzix.osm.upload.data.DaoMaster;
import net.anzix.osm.upload.data.DaoSession;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;
import net.anzix.osm.upload.source.DirSource;
import net.anzix.osm.upload.source.SourceHandler;
import net.anzix.osm.upload.source.SygicSource;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class GpxUploadApplication extends Application {

    DaoMaster daoMaster;

    DaoSession daoSession;

    private Map<String, SourceHandler> sourceHandlers = new HashMap<String, SourceHandler>();

    private List<Source> sources = new ArrayList<Source>();

    public boolean syncNeeded = false;

    static String sdCardPath;

    @Override
    public void onCreate() {
        super.onCreate();
        SQLiteDatabase db = new DaoMaster.DevOpenHelper(this, "notes-db", null).getWritableDatabase();
        addSourceHandler(new SygicSource());
        addSourceHandler(new DirSource());

        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        File f = new File("/sdcard");
        if (f.exists()) {
            sdCardPath = getNormalPath(f);
        }
    }

    private void addSourceHandler(SourceHandler s) {
        sourceHandlers.put(s.getKey(), s);
    }

    public static String getNormalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            Log.d("OSM", "Can't get canonical path", e);
            return file.getAbsolutePath();
        }
    }

    public static String getDisplayPath(String path) {
        if (sdCardPath != null) {
            return path.replaceAll(sdCardPath, "").substring(1);
        }
        return path;
    }

    public void sync() {
        sources.clear();
        sources = daoSession.getSourceDao().queryBuilder().list();

        Map<String, Gpx> gpxes = new HashMap<String, Gpx>();
        for (Gpx gpx : daoSession.getGpxDao().queryBuilder().list()) {
            gpxes.put(gpx.getLocation(), gpx);
        }

        for (Source source : sources) {
            SourceHandler handler = sourceHandlers.get(source.getType());
            for (Gpx gpx : handler.getGpxFiles(source)) {
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

    public Collection<SourceHandler> getSourceHandlers() {
        return sourceHandlers.values();
    }

    public SourceHandler getSourceHandle(String type) {
        return sourceHandlers.get(type);
    }
}
