package net.anzix.osm.upload.source;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Stateful parser to read Sygic aura log files.
 *
 *
 */
public class SygicParser {
    private SimpleDateFormat format = new SimpleDateFormat("yyMMdd_HHssmm");
    private String from;
    private String to;
    private long startTime;
    private long currentTime;
    private long currentAlt;
    private long currentLat;
    private long currentLon;
    long index = 0;
    private SygicInputStream fis;
    private String file;
    public static final SimpleDateFormat XML_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public SygicParser(String file) {
        this.file = file;
    }

    public void readHeader() throws IOException, ParseException {
        fis = new SygicInputStream(new File(file));
        System.out.println(fis.readRawString(4));
        System.out.println("favorite " + fis.readLong());
        System.out.println("type " + fis.readByte());
        System.out.println("start " + fis.readLong());
        System.out.println("duration " + fis.readLong());
        System.out.println("length " + fis.readLong());
        System.out.println(fis.readWString());
        System.out.println(fis.readWString());

        String startDate = fis.readWString();

        int pos = startDate.lastIndexOf("_");
        startTime = format.parse(startDate.substring(0, pos - 1)).getTime();
        startTime += 120l * 60 * 1000;
//Integer.parseInt(startDate.substring(pos + 1)) * 60 * 60 * 1000l;
        currentTime = startTime;
        System.out.println(fis.readWString());
        System.out.println("lan " + fis.readLong());
        System.out.println("lot " + fis.readLong());
        index = fis.readLong();
    }

    public boolean hasNext() throws IOException {
        return index > 0;
    }

    public void readNext() throws IOException {
        currentLon = fis.readLong();
        currentLat = fis.readLong();
        currentAlt = fis.readLong();
        currentTime += (fis.readLong() / 1000);
        fis.skip(9);
        index--;

    }

    public String getCurrentLatStr() {
        String latStr = "" + currentLat;
        int pos = latStr.length() - 5;
        return latStr.substring(0, pos) + "." + latStr.substring(pos);
    }

    public String getCurrentLonStr() {
        String lonStr = "" + currentLon;
        int pos = lonStr.length() - 5;
        return lonStr.substring(0, pos) + "." + lonStr.substring(pos);
    }

    private Date getCurrentDate() {
        return new Date(currentTime);
    }


    public long getCurrentAlt() {
        return currentAlt;
    }

    public String getCurrentDateStr() {
        String res = XML_DATE_FORMAT.format(getCurrentDate());
        return new StringBuilder(res).insert(res.length() - 2, ":").toString();
    }
}
