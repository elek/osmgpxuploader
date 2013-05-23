package net.anzix.osm.upload.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Input stream with on the fly sygic to gpx conversion.
 */
public class SygicInputStream extends FileInputStream {

    public SygicInputStream(File file) throws FileNotFoundException {
        super(file);
    }

    public String readRawString(int w) throws IOException {
        byte[] buff = new byte[w];
        read(buff);
        return new String(buff);
    }

    public int readInt() throws IOException {
        return read() + read() * 256;
    }


    public long readLong() throws IOException {
        return read() + 256 * read() + 65536 * read() + 16777216l * read();
//return 16777216l * read() + 65536 * read() + 256 * read() + read();
    }

    public int readByte() throws IOException {
        return read();
    }

    public String readWString() throws IOException {
        String s = "";
        int size = readInt();
        for (int p = 0; p < size; p++) {
            char c = (char) readInt();
            s += c;
        }
        return s;
    }
}
