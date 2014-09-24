package com.frostwire.alexandria.db;

import java.util.ArrayList;
import java.util.List;

import com.frostwire.alexandria.InternetRadioStation;

public class InternetRadioStationDB {

    private InternetRadioStationDB() {} // don't allow instantiation of this class

    public static void fill(LibraryDatabase db, InternetRadioStation obj) {
        List<List<Object>> result = db.query("SELECT internetRadioStationId, name, description, url, bitrate, type, website, genre, pls, bookmarked FROM InternetRadioStations WHERE internetRadioStationId = ?", obj.getId());
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public static void fill(List<Object> row, InternetRadioStation obj) {
        int id = (Integer) row.get(0);
        String name = (String) row.get(1);
        String description = (String) row.get(2);
        String url = (String) row.get(3);
        String bitrate = (String) row.get(4);
        String type = (String) row.get(5);
        String website = (String) row.get(6);
        String genre = (String) row.get(7);
        String pls = (String) row.get(8);
        boolean bookmarked = (Boolean) row.get(9);

        obj.setId(id);
        obj.setName(name);
        obj.setDescription(description);
        obj.setUrl(url);
        obj.setBitrate(bitrate);
        obj.setType(type);
        obj.setWebsite(website);
        obj.setGenre(genre);
        obj.setPls(pls);
        obj.setBookmarked(bookmarked);
    }

    public static void save(LibraryDatabase db, InternetRadioStation obj) {
        if (obj.getId() == LibraryDatabase.OBJECT_INVALID_ID) {
            return;
        }

        if (obj.getId() == LibraryDatabase.OBJECT_NOT_SAVED_ID) {
            Object[] statementObjects = createInternetRadioStationInsertStatement(obj);
            int id = db.insert((String) statementObjects[0], (Object[]) statementObjects[1]);
            obj.setId(id);
        } else {
            Object[] statementObjects = createInternetRadioStationUpdateStatement(obj);
            db.update((String) statementObjects[0], (Object[]) statementObjects[1]);
        }
    }

    public static void delete(LibraryDatabase db, InternetRadioStation obj) {
        db.update("DELETE FROM InternetRadioStations WHERE internetRadioStationId = ?", obj.getId());
    }
    
    public static List<InternetRadioStation> getInternetRadioStations(LibraryDatabase db) {
        List<List<Object>> result = db.query("SELECT internetRadioStationId, name, description, url, bitrate, type, website, genre, pls, bookmarked FROM InternetRadioStations");

        List<InternetRadioStation> internetRadioStations = new ArrayList<InternetRadioStation>(result.size());

        for (List<Object> row : result) {
            InternetRadioStation internetRadioStation = new InternetRadioStation(db);
            InternetRadioStationDB.fill(row, internetRadioStation);
            internetRadioStations.add(internetRadioStation);
        }

        return internetRadioStations;
    }

    

    public static long getTotalRadioStations(LibraryDatabase db) {
        List<List<Object>> query = db.query("SELECT COUNT(*) FROM InternetRadioStations");
        return query.size() > 0 ? (Long) query.get(0).get(0) : 0;
    }

    public static void restoreDefaultRadioStations(LibraryDatabase db) {
        List<InternetRadioStation> internetRadioStations = getInternetRadioStations(db);
        
        InternetRadioStationsData data = new InternetRadioStationsData();
        
        for (InternetRadioStation station : internetRadioStations) {
            data.add(station.getName(), station.getDescription(), station.getUrl(), station.getBitrate(), station.getType(), station.getWebsite(), station.getGenre(), station.getPls());
        }
        
        db.update("DELETE FROM InternetRadioStations");

        for (List<Object> row : data.getData()) {
            db.update("INSERT INTO InternetRadioStations (name, description, url, bitrate, type, website, genre, pls, bookmarked) VALUES (LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100), LEFT(?, 100), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100000), false)", row.toArray());
        }
    }

    private static Object[] createInternetRadioStationInsertStatement(InternetRadioStation obj) {
        String sql = "INSERT INTO InternetRadioStations (name, description, url, bitrate, type, website, genre, pls, bookmarked) VALUES (LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100), LEFT(?, 100), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100000), ?)";
        Object[] values = new Object[] { obj.getName(), obj.getDescription(), obj.getUrl(), obj.getBitrate(), obj.getType(), obj.getWebsite(), obj.getGenre(), obj.getPls(), obj.isBookmarked() };
        return new Object[] { sql, values };
    }

    private static Object[] createInternetRadioStationUpdateStatement(InternetRadioStation obj) {
        String sql = "UPDATE InternetRadioStations SET name = LEFT(?, 500), description = LEFT(?, 10000), url = LEFT(?, 10000), bitrate = LEFT(?, 100), type = LEFT(?, 100), website = LEFT(?, 10000), genre = LEFT(?, 10000), pls = LEFT(?, 100000), bookmarked = ? WHERE internetRadioStationId = ?";
        Object[] values = new Object[] { obj.getName(), obj.getDescription(), obj.getUrl(), obj.getBitrate(), obj.getType(), obj.getWebsite(), obj.getGenre(), obj.getPls(), obj.isBookmarked(), obj.getId() };
        return new Object[] { sql, values };
    }
}
