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
    private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<gpx xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "        xmlns=\"http://www.topografix.com/GPX/1/0\"\n" +
            "        xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\"\n" +
            "        version=\"1.0\"\n" +
            "        creator=\"KeypadMapper2\">\n" +
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

    @Override
    public int read() throws IOException {
        if (idx >= buffer.length()) {
            if (parser.hasNext()) {
                parser.readNext();
                buffer = String.format("<trkpt lat=\"%s\" lon=\"%s\"><ele>%s</ele><time>%s</time></trkpt>\n",
                        parser.getCurrentLatStr(), parser.getCurrentLonStr(), parser.getCurrentAlt(), parser.getCurrentDateStr());
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