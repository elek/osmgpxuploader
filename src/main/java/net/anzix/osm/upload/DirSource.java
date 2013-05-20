package net.anzix.osm.upload;

import android.util.Log;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: eszti
 */
public class DirSource {

    private Source source;

    public DirSource(Source source) {
        this.source = source;
    }

    public List<Gpx> getGpxFiles() {
        List<Gpx> result = new ArrayList<Gpx>();
        for (File f : new File(source.getLocation()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".gpx") || s.endsWith(".GPX");
            }
        })) {
            Gpx g = new Gpx();
            g.setType(source.getType());
            try {
                g.setLocation(f.getCanonicalPath());
            } catch (IOException e) {
                Log.e("OSM", "Can't get canonical path ", e);
                g.setLocation(f.getAbsolutePath());
            }
            result.add(g);
        }
        return result;
    }
}
