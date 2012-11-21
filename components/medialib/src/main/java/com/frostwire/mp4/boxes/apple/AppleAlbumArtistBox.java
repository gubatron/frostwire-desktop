package com.frostwire.mp4.boxes.apple;

/**
 * itunes MetaData comment box.
 */
public class AppleAlbumArtistBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "aART";


    public AppleAlbumArtistBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }


}