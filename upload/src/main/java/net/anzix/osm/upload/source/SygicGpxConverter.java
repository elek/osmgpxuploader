package net.anzix.osm.upload.source;

/**
 * Input stream with on demand GPX file generation based on a Sygic parser.
 */

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public class SygicGpxConverter extends InputStream {
    private SygicParser parser;
    private String buffer = "";
    private int idx = 0;
    private boolean ended = false;
    private long prevLat;
    private long prevLon;
    private long prevTime;
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
            "        xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\"\n" +
            "        version=\"1.0\"\n" +
            "        creator=\"OsmGpxUploader\">\n" +
            "\n" +
            "        <trk>\n" +
            "                <trkseg>\n";

    private static final String FOOTER = "</trkseg>\n" +
            "        </trk>\n" +
            "</gpx>\n";

    public SygicGpxConverter(SygicParser parser) throws IOException, ParseException {
        this.parser = parser;
        parser.readHeader();
        buffer = HEADER;
    }

    private final static double R = 6367.0;

    private final static double RADIANSPERDEGREE = 0.017453293;

    private double computeDistance(double lat1, double lon1, double lat2, double lon2) {
        // all values are of type "double"; lat1,lon1,lat2 and lon2 are the
        // coordinates we are using in the calculation, in RADIANS.
        // You will have to convert from degrees to radians using the
        // conversion factor, RADIANSPERDEGREE
        lat2 = lat2 * RADIANSPERDEGREE;
        lon2 = lon2 * RADIANSPERDEGREE;
        lat1 = lat1 * RADIANSPERDEGREE;
        lon1 = lon1 * RADIANSPERDEGREE;

        double dlon = lon2 - lon1; // difference in longitude
        double dlat = lat2 - lat1; // difference in latitude

        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


//        double a = (Math.sin(dlat / 2.0) * Math.sin(dlat / 2.0)) + (Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2.0) * Math.sin(dlon / 2.0));
//
//        double c = 2.0 * Math.asin(Math.sqrt(a));
        return (R * c);
    }

    @Override
    public int read() throws IOException {
        double speed = 0;
        double dist = 0;
        if (idx >= buffer.length()) {
            if (parser.hasNext()) {
                parser.readNext();
                if (prevTime > 0) {
                    dist = computeDistance(prevLat / 100000d, prevLon / 100000d, parser.getCurrentLat() / 100000d, parser.getCurrentLon() / 100000d);
                    speed = (dist * 1000 * 60 * 60) / (parser.getCurrentTime() - prevTime);
                }
                if (speed > 800 || dist > 0.1) {
                    buffer = "</trkseg><trkseg>";
                } else {
                    buffer = "";
                }
                buffer += String.format("<trkpt lat=\"%s\" lon=\"%s\"><ele>%s</ele><time>%s</time></trkpt>\n",
                        parser.getCurrentLatStr(), parser.getCurrentLonStr(), parser.getCurrentAlt(), parser.getCurrentDateStr());
                prevLat = parser.getCurrentLat();
                prevLon = parser.getCurrentLon();
                prevTime = parser.getCurrentTime();

                idx = 0;
            } else {
                if (ended) {
                    return -1;
                } else {
                    buffer = FOOTER;
                    idx = 0;
                    ended = true;
                }
            }
        }
        int c = buffer.charAt(idx);
        idx++;
        return c;
    }
}