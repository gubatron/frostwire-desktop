package com.frostwire.alexandria.db;

import java.util.LinkedList;
import java.util.List;

public final class InternetRadioStationsData {

    private List<List<Object>> data;

    public InternetRadioStationsData() {
        data = new LinkedList<List<Object>>();

        add(data, "top 100 station - Germanys No.1 Web Hit Station", "top 100 station - Germanys No.1 Web Hit Station", "http://87.230.101.50:80", "128 kbps", "mp3", "http://www.top100station.de", "Top40");
    }

    public List<List<Object>> getData() {
        return data;
    }

    private void add(List<List<Object>> data, String name, String description, String url, String bitrate, String type, String website, String genre) {
        List<Object> row = new LinkedList<Object>();

        row.add(name);
        row.add(description);
        row.add(url);
        row.add(bitrate);
        row.add(type);
        row.add(website);
        row.add(genre);

        data.add(row);
    }
}
