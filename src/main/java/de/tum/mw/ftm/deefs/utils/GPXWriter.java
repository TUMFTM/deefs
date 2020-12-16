package de.tum.mw.ftm.deefs.utils;


import com.graphhopper.util.GPXEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Helper Class, providing methods to write GPX files
 *
 * @author Michael Wittmann
 */
public class GPXWriter {

    /**
     * Converts a List of points to a readable GPX-File. This way, tracks can be visualized for example in Google Earth or QGis
     *
     * @param file      Output File
     * @param trackName Track Name
     * @param points    Waypoints of given track
     */
    public static void writeGPX(File file, String trackName, List<GPXEntry> points) {

        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + trackName + "</name><trkseg>\n";

        StringBuilder segments = new StringBuilder();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        for (GPXEntry l : points) {
            segments.append("<trkpt lat=\"").append(l.getLat()).append("\" lon=\"").append(l.getLon()).append("\"><time>").append(df.format(new Date(l.getMillis()))).append("</time></trkpt>\n");
        }

        String footer = "</trkseg></trk></gpx>";

        try {
            FileWriter writer = new FileWriter(file, false);
            writer.append(header);
            writer.append(name);
            writer.append(segments.toString());
            writer.append(footer);
            writer.flush();
            writer.close();
            System.out.println("writing GPXFile was sucessful!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("Error while writing GPX-File");
        }
    }
}
