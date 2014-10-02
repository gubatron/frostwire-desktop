package com.frostwire.alexandria;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.frostwire.alexandria.IcyInputStream.Track;
import com.frostwire.alexandria.db.InternetRadioStationsData;

public class RadioImportTool {

    public static void main(String[] args) throws Exception {
        run0(0);
    }

    public static void run0(int start) throws Exception {
        InternetRadioStationsData d = new InternetRadioStationsData();
        FileInputStream fstream = new FileInputStream("pls.txt");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        PrintWriter out = new PrintWriter("output.txt", "UTF-8");

        int n = 0;
        String strLine;
        while ((strLine = br.readLine()) != null) {
            try {
                if (n < start) {
                    n++;
                    continue;
                }
                processPls(n, strLine, out, d.nameSet);
                //Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Error for: " + strLine);
                e.printStackTrace();
            }
            n++;
        }

        in.close();
        out.close();

        System.out.println("Total: " + n);
    }

    public static void run1() throws Exception {
        processPls(0, "http://yp.shoutcast.com/sbin/tunein-station.pls?id=98179", new PrintWriter(System.out), new HashSet<String>());
    }

    public static void run2() throws Exception {
        InternetRadioStationsData d = new InternetRadioStationsData();
        List<List<Object>> rows = d.getData();

        PrintWriter out = new PrintWriter("output.txt", "UTF-8");

        int n = 0;
        for (List<Object> r : rows) {
            String streamUrl = (String) r.get(2);
            try {
                writeData(out, streamUrl, d.nameSet);
                out.println("\"" + "" + "\");");
                //Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(" Error for: " + streamUrl);
                //e.printStackTrace();
            }
            n++;
        }

        out.close();

        System.out.println("Total: " + n);
    }

    private static void processPls(int i, String urlStr, PrintWriter out, Set<String> names) throws Exception {
        URL url = new URL(urlStr);
        System.out.print(i + " - " + urlStr);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(10000);
        InputStream is = conn.getInputStream();
        BufferedReader d = null;
        if (conn.getContentEncoding() != null) {
            d = new BufferedReader(new InputStreamReader(is, conn.getContentEncoding()));
        } else {
            d = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        }

        String pls = "";
        String strLine;
        String dataLine = null;
        while ((strLine = d.readLine()) != null) {
            pls += strLine + "\\n";
            if (strLine.startsWith("File1=")) {
                String streamUrl = strLine.split("=")[1];
                dataLine = writeData(out, streamUrl, names);
            }
        }

        // remove pls data
        pls = "";

        if (dataLine != null) {
            out.println("\"" + pls + "\");");
            //System.out.println("\"" + pls + "\");");
        }

        is.close();
    }

    private static String writeData(PrintWriter out, String streamUrl, Set<String> names) throws Exception {
        String dataLine = processIcyStream(streamUrl, names);
        if (dataLine == null) {
            return null;
        }
        System.out.println(dataLine);
        System.out.println();
        out.print(dataLine);
        out.flush();

        return dataLine;
    }

    private static String clean(String str) {
        return str.trim().replace("\"", "\\\"");
    }

    private static String processIcyStream(String streamUrl, Set<String> names) throws Exception {
        System.out.print(" - " + streamUrl);
        Track t = new Track();
        IcyInputStream.create(streamUrl, t);

        String name = clean(t.name);
        String genre = clean(t.genre);
        String website = clean(t.url);
        String type = "";
        String br = t.bitrate != null ? t.bitrate.trim() + " kbps" : "";

        String contentType = t.contentType;
        if (contentType.equals("audio/aacp")) {
            type = "AAC+";
        } else if (contentType.equals("audio/mpeg")) {
            type = "MP3";
        } else if (contentType.equals("audio/aac")) {
            type = "AAC";
        }

        if (names.contains(name)) {
            return null;
        }

        names.add(name);

        return "        add(data, \"" + name + "\", \"" + name + "\", \"" + streamUrl + "\", \"" + br + "\", \"" + type + "\", \"" + website + "\", \"" + genre + "\", ";
    }
}
