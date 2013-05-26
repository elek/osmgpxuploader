package net.anzix.osm.upload.source;

import android.util.Log;
import net.anzix.osm.upload.GpxUploadApplication;
import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sygic Aura travelbook handler with on demand gpx conversion.
 */
public class SygicSource implements SourceHandler {


    @Override
    public String getKey() {
        return "sygic";
    }

    @Override
    public String getName() {
        return "Sygic Aura travelbook";
    }

    @Override
    public String getDescription() {
        return "Convert traces from sygic travelbook ";
    }

    @Override
    public List<Gpx> getGpxFiles(Source source) {
        List<Gpx> result = new ArrayList<Gpx>();

        FileInputStream fis = null;
        byte[] sig = new byte[4];
        for (File f : new File("/sdcard/Sygic/Res/travelbook").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".log");
            }
        })) {
            try {
                fis = new FileInputStream(f);
                fis.read(sig);
                if (new String(sig).equals("5FRT")) {
                    Gpx g = new Gpx();
                    g.setType(source.getType());
                    g.setCreated(new Date(f.lastModified()));
                    g.setLocation(GpxUploadApplication.getNormalPath(f));
                    result.add(g);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return result;
    }

    @Override
    public InputStream createStream(Gpx gpx) {
        try {
            return new SygicGpxConverter(new SygicParser(gpx.getLocation()));
        } catch (Exception e) {
            throw new RuntimeException("Can not convert sygic log file: " + gpx.getLocation());
        }
    }

    @Override
    public String getDisplayableLocation(Gpx gpx) {
        return "Sygic file from the travelbook";
    }


}