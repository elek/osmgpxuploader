package net.anzix.osm.upload.source;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;


public class SygicParserTest {

    @Test
    public void test() throws IOException, ParseException {
        SygicGpxConverter conv = new SygicGpxConverter(new SygicParser("src/test/resources/130520_092450.log"));
        InputStream is = conv;
        int c;
        while ((c = is.read()) != -1){
            System.out.print((char)c);
        }


    }
}
