package com.frostwire.alexandria.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.frostwire.alexandria.InternetRadioStation;
import com.frostwire.alexandria.Library;
import com.frostwire.alexandria.Playlist;
import com.frostwire.alexandria.PlaylistItem;

public class LibraryDB extends ObjectDB<Library> {

    public LibraryDB(LibraryDatabase db) {
        super(db);
    }

    public void fill(Library obj) {
        // this is a special case, since we will have only one library per database
        List<List<Object>> result = db.query("SELECT libraryId, name, version FROM Library");
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            fill(row, obj);
        }
    }

    public void fill(List<Object> row, Library obj) {
        int id = (Integer) row.get(0);
        String name = (String) row.get(1);
        int version = (Integer) row.get(2);

        obj.setId(id);
        obj.setName(name);
        obj.setVersion(version);
    }

    public void save(Library obj) {
        // nothing
    }

    public void delete(Library obj) {
        // nothing
    }

    public List<Playlist> getPlaylists(Library library) {
        List<List<Object>> result = db.query("SELECT playlistId, name, description FROM Playlists");

        List<Playlist> playlists = new ArrayList<Playlist>(result.size());

        for (List<Object> row : result) {
            Playlist playlist = new Playlist(library);
            playlist.getDB().fill(row, playlist);
            playlists.add(playlist);
        }

        return playlists;
    }

    public Playlist getPlaylist(Library library, String name) {
        List<List<Object>> result = db.query("SELECT playlistId, name, description FROM Playlists WHERE name = ?", name);
        Playlist playlist = null;
        if (result.size() > 0) {
            List<Object> row = result.get(0);
            playlist = new Playlist(library);
            playlist.getDB().fill(row, playlist);
        }
        return playlist;
    }
    
    public List<InternetRadioStation> getInternetRadioStations(Library library) {
        List<List<Object>> result = db.query("SELECT internetRadioStationId, name, description, url, bitrate, type, website, genre, pls, bookmarked FROM InternetRadioStations");

        List<InternetRadioStation> internetRadioStations = new ArrayList<InternetRadioStation>(result.size());

        for (List<Object> row : result) {
            InternetRadioStation internetRadioStation = new InternetRadioStation(library);
            internetRadioStation.getDB().fill(row, internetRadioStation);
            internetRadioStations.add(internetRadioStation);
        }

        return internetRadioStations;
    }

    public Playlist getStarredPlaylist(Library library) {
        String query = "SELECT playlistItemId, filePath, fileName, fileSize, fileExtension, trackTitle, trackDurationInSecs, trackArtist, trackAlbum, coverArtPath, trackBitrate, trackComment, trackGenre, trackNumber, trackYear, starred " + "FROM PlaylistItems WHERE starred = ?";

        List<List<Object>> result = db.query(query, true);

        Playlist playlist = new Playlist(library, LibraryDatabase.STARRED_PLAYLIST_ID, "starred", "starred");

        List<PlaylistItem> items = new ArrayList<PlaylistItem>(result.size());
        Set<String> paths = new HashSet<String>();

        for (List<Object> row : result) {
            PlaylistItem item = new PlaylistItem(playlist);
            item.getDB().fill(row, item);
            if (!paths.contains(item.getFilePath())) {
                items.add(item);
                paths.add(item.getFilePath());
            }
        }

        playlist.getItems().addAll(items);

        return playlist;
    }

    public void updatePlaylistItemProperties(String filePath, String title, String artist, String album, String comment, String genre, String track, String year) {
        Object[] sqlAndValues = createPlaylistItemPropertiesUpdate(filePath, title, artist, album, comment, genre, track, year);
        db.update((String) sqlAndValues[0], (Object[]) sqlAndValues[1]);
    }

    private Object[] createPlaylistItemPropertiesUpdate(String filePath, String title, String artist, String album, String comment, String genre, String track, String year) {
        String sql = "UPDATE PlaylistItems SET trackTitle = LEFT(?, 500), trackArtist = LEFT(?, 500), trackAlbum = LEFT(?, 500), trackComment = LEFT(?, 500), trackGenre = LEFT(?, 20), trackNumber = LEFT(?, 6), trackYear = LEFT(?, 6) WHERE filePath = LEFT(?, 10000)";

        Object[] values = new Object[] { title, artist, album, comment, genre, track, year, filePath };

        return new Object[] { sql, values };
    }

    public long getTotalRadioStations(Library library) {
        List<List<Object>> query = db.query("SELECT COUNT(*) FROM InternetRadioStations");
        return query.size() > 0 ? (Long) query.get(0).get(0) : 0;
    }

    public void restoreDefaultRadioStations(Library library) {
        List<InternetRadioStation> internetRadioStations = getInternetRadioStations(library);
        
        InternetRadioStationsData data = new InternetRadioStationsData();
        
        for (InternetRadioStation station : internetRadioStations) {
            data.add(station.getName(), station.getDescription(), station.getUrl(), station.getBitrate(), station.getType(), station.getWebsite(), station.getGenre(), station.getPls());
        }
        
        db.update("DELETE FROM InternetRadioStations");

        for (List<Object> row : data.getData()) {
            db.update("INSERT INTO InternetRadioStations (name, description, url, bitrate, type, website, genre, pls, bookmarked) VALUES (LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100), LEFT(?, 100), LEFT(?, 10000), LEFT(?, 10000), LEFT(?, 100000), false)", row.toArray());
        }
    }
}
