/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.core.providers;


/**
 * @author gubatron
 * @author aldenml
 * 
 * Code taken from android source code
 *
 */
public final class MediaStore {

    public static final String AUTHORITY = "media";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";

    /**
     * Common fields for most MediaProvider tables
     */

    public interface MediaColumns extends BaseColumns {
        /**
         * The data stream for the file
         * <P>Type: DATA STREAM</P>
         */
        public static final String DATA = "_data";

        /**
         * The size of the file in bytes
         * <P>Type: INTEGER (long)</P>
         */
        public static final String SIZE = "_size";

        /**
         * The display name of the file
         * <P>Type: TEXT</P>
         */
        public static final String DISPLAY_NAME = "_display_name";

        /**
         * The title of the content
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * The time the file was added to the media provider
         * Units are seconds since 1970.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_ADDED = "date_added";

        /**
         * The time the file was last modified
         * Units are seconds since 1970.
         * NOTE: This is for internal use by the media scanner.  Do not modify this field.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String DATE_MODIFIED = "date_modified";

        /**
         * The MIME type of the file
         * <P>Type: TEXT</P>
         */
        public static final String MIME_TYPE = "mime_type";

        /**
         * The MTP object handle of a newly transfered file.
         * Used to pass the new file's object handle through the media scanner
         * from MTP to the media provider
         * For internal use only by MTP, media scanner and media provider.
         * <P>Type: INTEGER</P>
         * @hide
         */
        public static final String MEDIA_SCANNER_NEW_OBJECT_ID = "media_scanner_new_object_id";

        /**
         * Non-zero if the media file is drm-protected
         * <P>Type: INTEGER (boolean)</P>
         * @hide
         */
        public static final String IS_DRM = "is_drm";

        /**
         * The width of the image/video in pixels.
         * @hide
         */
        public static final String WIDTH = "width";

        /**
         * The height of the image/video in pixels.
         * @hide
         */
        public static final String HEIGHT = "height";
    }

    /**
     * Contains meta data for all available images.
     */
    public static final class Images {
        public interface ImageColumns extends MediaColumns {
            /**
             * The description of the image
             * <P>Type: TEXT</P>
             */
            public static final String DESCRIPTION = "description";

            /**
             * The picasa id of the image
             * <P>Type: TEXT</P>
             */
            public static final String PICASA_ID = "picasa_id";

            /**
             * Whether the video should be published as public or private
             * <P>Type: INTEGER</P>
             */
            public static final String IS_PRIVATE = "isprivate";

            /**
             * The latitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            public static final String LATITUDE = "latitude";

            /**
             * The longitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            public static final String LONGITUDE = "longitude";

            /**
             * The date & time that the image was taken in units
             * of milliseconds since jan 1, 1970.
             * <P>Type: INTEGER</P>
             */
            public static final String DATE_TAKEN = "datetaken";

            /**
             * The orientation for the image expressed as degrees.
             * Only degrees 0, 90, 180, 270 will work.
             * <P>Type: INTEGER</P>
             */
            public static final String ORIENTATION = "orientation";

            /**
             * The mini thumb id.
             * <P>Type: INTEGER</P>
             */
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";

            /**
             * The bucket id of the image. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            public static final String BUCKET_ID = "bucket_id";

            /**
             * The bucket display name of the image. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";
        }

        public static final class Media implements ImageColumns {
            /**
             * Get the content:// style URI for the image media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the image media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName + "/images/media");
            }

            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");

            /**
             * The MIME type of of this directory of
             * images.  Note that each entry in this directory will have a standard
             * image MIME type as appropriate -- for example, image/jpeg.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/image";

            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = ImageColumns.BUCKET_DISPLAY_NAME;
        }
    }

    /**
     * Container for all audio content.
     */
    public static final class Audio {
        /**
         * Columns for audio file that show up in multiple tables.
         */
        public interface AudioColumns extends MediaColumns {

            /**
             * A non human readable key calculated from the TITLE, used for
             * searching, sorting and grouping
             * <P>Type: TEXT</P>
             */
            public static final String TITLE_KEY = "title_key";

            /**
             * The duration of the audio file, in ms
             * <P>Type: INTEGER (long)</P>
             */
            public static final String DURATION = "duration";

            /**
             * The position, in ms, playback was at when playback for this file
             * was last stopped.
             * <P>Type: INTEGER (long)</P>
             */
            public static final String BOOKMARK = "bookmark";

            /**
             * The id of the artist who created the audio file, if any
             * <P>Type: INTEGER (long)</P>
             */
            public static final String ARTIST_ID = "artist_id";

            /**
             * The artist who created the audio file, if any
             * <P>Type: TEXT</P>
             */
            public static final String ARTIST = "artist";

            /**
             * The artist credited for the album that contains the audio file
             * <P>Type: TEXT</P>
             * @hide
             */
            public static final String ALBUM_ARTIST = "album_artist";

            /**
             * Whether the song is part of a compilation
             * <P>Type: TEXT</P>
             * @hide
             */
            public static final String COMPILATION = "compilation";

            /**
             * A non human readable key calculated from the ARTIST, used for
             * searching, sorting and grouping
             * <P>Type: TEXT</P>
             */
            public static final String ARTIST_KEY = "artist_key";

            /**
             * The composer of the audio file, if any
             * <P>Type: TEXT</P>
             */
            public static final String COMPOSER = "composer";

            /**
             * The id of the album the audio file is from, if any
             * <P>Type: INTEGER (long)</P>
             */
            public static final String ALBUM_ID = "album_id";

            /**
             * The album the audio file is from, if any
             * <P>Type: TEXT</P>
             */
            public static final String ALBUM = "album";

            /**
             * A non human readable key calculated from the ALBUM, used for
             * searching, sorting and grouping
             * <P>Type: TEXT</P>
             */
            public static final String ALBUM_KEY = "album_key";

            /**
             * The track number of this song on the album, if any.
             * This number encodes both the track number and the
             * disc number. For multi-disc sets, this number will
             * be 1xxx for tracks on the first disc, 2xxx for tracks
             * on the second disc, etc.
             * <P>Type: INTEGER</P>
             */
            public static final String TRACK = "track";

            /**
             * The year the audio file was recorded, if any
             * <P>Type: INTEGER</P>
             */
            public static final String YEAR = "year";

            /**
             * Non-zero if the audio file is music
             * <P>Type: INTEGER (boolean)</P>
             */
            public static final String IS_MUSIC = "is_music";

            /**
             * Non-zero if the audio file is a podcast
             * <P>Type: INTEGER (boolean)</P>
             */
            public static final String IS_PODCAST = "is_podcast";

            /**
             * Non-zero if the audio file may be a ringtone
             * <P>Type: INTEGER (boolean)</P>
             */
            public static final String IS_RINGTONE = "is_ringtone";

            /**
             * Non-zero if the audio file may be an alarm
             * <P>Type: INTEGER (boolean)</P>
             */
            public static final String IS_ALARM = "is_alarm";

            /**
             * Non-zero if the audio file may be a notification sound
             * <P>Type: INTEGER (boolean)</P>
             */
            public static final String IS_NOTIFICATION = "is_notification";

            /**
             * The genre of the audio file, if any
             * <P>Type: TEXT</P>
             * Does not exist in the database - only used by the media scanner for inserts.
             * @hide
             */
            public static final String GENRE = "genre";
        }

        public static final class Media implements AudioColumns {
            /**
             * Get the content:// style URI for the audio media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the audio media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName + "/audio/media");
            }

            //            public static Uri getContentUriForPath(String path) {
            //                return (path.startsWith(Environment.getExternalStorageDirectory().getPath()) ? EXTERNAL_CONTENT_URI : INTERNAL_CONTENT_URI);
            //            }

            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");

            /**
             * The MIME type for this table.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/audio";

            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = TITLE_KEY;

            /**
             * Activity Action: Start SoundRecorder application.
             * <p>Input: nothing.
             * <p>Output: An uri to the recorded sound stored in the Media Library
             * if the recording was successful.
             * May also contain the extra EXTRA_MAX_BYTES.
             * @see #EXTRA_MAX_BYTES
             */
            public static final String RECORD_SOUND_ACTION = "android.provider.MediaStore.RECORD_SOUND";

            /**
             * The name of the Intent-extra used to define a maximum file size for
             * a recording made by the SoundRecorder application.
             *
             * @see #RECORD_SOUND_ACTION
             */
            public static final String EXTRA_MAX_BYTES = "android.provider.MediaStore.extra.MAX_BYTES";
        }
    }

    public static final class Video {
        public interface VideoColumns extends MediaColumns {

            /**
             * The duration of the video file, in ms
             * <P>Type: INTEGER (long)</P>
             */
            public static final String DURATION = "duration";

            /**
             * The artist who created the video file, if any
             * <P>Type: TEXT</P>
             */
            public static final String ARTIST = "artist";

            /**
             * The album the video file is from, if any
             * <P>Type: TEXT</P>
             */
            public static final String ALBUM = "album";

            /**
             * The resolution of the video file, formatted as "XxY"
             * <P>Type: TEXT</P>
             */
            public static final String RESOLUTION = "resolution";

            /**
             * The description of the video recording
             * <P>Type: TEXT</P>
             */
            public static final String DESCRIPTION = "description";

            /**
             * Whether the video should be published as public or private
             * <P>Type: INTEGER</P>
             */
            public static final String IS_PRIVATE = "isprivate";

            /**
             * The user-added tags associated with a video
             * <P>Type: TEXT</P>
             */
            public static final String TAGS = "tags";

            /**
             * The YouTube category of the video
             * <P>Type: TEXT</P>
             */
            public static final String CATEGORY = "category";

            /**
             * The language of the video
             * <P>Type: TEXT</P>
             */
            public static final String LANGUAGE = "language";

            /**
             * The latitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            public static final String LATITUDE = "latitude";

            /**
             * The longitude where the image was captured.
             * <P>Type: DOUBLE</P>
             */
            public static final String LONGITUDE = "longitude";

            /**
             * The date & time that the image was taken in units
             * of milliseconds since jan 1, 1970.
             * <P>Type: INTEGER</P>
             */
            public static final String DATE_TAKEN = "datetaken";

            /**
             * The mini thumb id.
             * <P>Type: INTEGER</P>
             */
            public static final String MINI_THUMB_MAGIC = "mini_thumb_magic";

            /**
             * The bucket id of the video. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            public static final String BUCKET_ID = "bucket_id";

            /**
             * The bucket display name of the video. This is a read-only property that
             * is automatically computed from the DATA column.
             * <P>Type: TEXT</P>
             */
            public static final String BUCKET_DISPLAY_NAME = "bucket_display_name";

            /**
             * The bookmark for the video. Time in ms. Represents the location in the video that the
             * video should start playing at the next time it is opened. If the value is null or
             * out of the range 0..DURATION-1 then the video should start playing from the
             * beginning.
             * <P>Type: INTEGER</P>
             */
            public static final String BOOKMARK = "bookmark";
        }

        public static final class Media implements VideoColumns {
            /**
             * Get the content:// style URI for the video media table on the
             * given volume.
             *
             * @param volumeName the name of the volume to get the URI for
             * @return the URI to the video media table on the given volume
             */
            public static Uri getContentUri(String volumeName) {
                return Uri.parse(CONTENT_AUTHORITY_SLASH + volumeName + "/video/media");
            }

            /**
             * The content:// style URI for the internal storage.
             */
            public static final Uri INTERNAL_CONTENT_URI = getContentUri("internal");

            /**
             * The content:// style URI for the "primary" external storage
             * volume.
             */
            public static final Uri EXTERNAL_CONTENT_URI = getContentUri("external");

            /**
             * The MIME type for this table.
             */
            public static final String CONTENT_TYPE = "vnd.android.cursor.dir/video";

            /**
             * The default sort order for this table
             */
            public static final String DEFAULT_SORT_ORDER = TITLE;
        }
    }
}
