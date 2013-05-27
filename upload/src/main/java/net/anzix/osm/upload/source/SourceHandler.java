package net.anzix.osm.upload.source;

import net.anzix.osm.upload.data.Gpx;
import net.anzix.osm.upload.data.Source;

import java.io.InputStream;
import java.util.List;

/**
 * Interface to handle sources of track files.
 */
public interface SourceHandler {

    public String getKey();

    public String getName();

    public String getDescription();

    public List<Gpx> getGpxFiles(Source source);

    public InputStream createStream(Gpx gpx);

    public String getDisplayableLocation(Gpx gpx);
}
