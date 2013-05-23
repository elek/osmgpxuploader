package net.anzix.osm.upload.source;

import android.util.Log;
import net.anzix.osm.upload.GpxUploadApplication;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gpx files in a directory.
 */
public class DirSource implements SourceHandler {

    public DirSource() {
    }

    public List<Gpx> getGpxFiles(Source source) {
        List<Gpx> result = new ArrayList<Gpx>();
        for (File f : new File(source.getLocation()).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".gpx") || s.endsWith(".GPX");
            }
        })) {
            Gpx g = new Gpx();
            g.setType(source.getType());
            g.setCreated(new Date(f.lastModified()));
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

    @Override
    public InputStream createStream(Gpx gpx) {
        try {
            return new FileInputStream(gpx.getLocation());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Can't open the file " + gpx.getLocation());
        }
    }

    @Override
    public String getDisplayableLocation(Gpx gpx) {
        return "Gpx file from the dir " + GpxUploadApplication.getDisplayPath(gpx.getPath());
    }

    @Override
    public String getKey() {
        return "dir";
    }

    @Override
    public String getName() {
        return "Directory reader";
    }

    @Override
    public String getDescription() {
        return "Read GPX files from a directory";
    }


}
