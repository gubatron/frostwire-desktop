package com.frostwire.gui.library;

public class Finger {

    // general data

    public String uuid;

    public String nickname;

    public String frostwireVersion;

    public int totalShared;

    // device data

    public String deviceVersion;

    public String deviceModel;

    public String deviceProduct;

    public String deviceName;

    public String deviceManufacturer;

    public String deviceBrand;

    // shared data

    public int numSharedAudioFiles;

    public int numSharedVideoFiles;

    public int numSharedPictureFiles;

    public int numSharedDocumentFiles;

    public int numSharedApplicationFiles;

    public int numSharedRingtoneFiles;

    // total data

    public int numTotalAudioFiles;

    public int numTotalVideoFiles;

    public int numTotalPictureFiles;

    public int numTotalDocumentFiles;

    public int numTotalApplicationFiles;

    public int numTotalRingtoneFiles;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        sb.append(nickname + ", " + totalShared + ", ");
        sb.append("[");
        sb.append("aud:" + numSharedAudioFiles + "/" + numTotalAudioFiles + ", ");
        sb.append("vid:" + numSharedVideoFiles + "/" + numTotalVideoFiles + ", ");
        sb.append("pic:" + numSharedPictureFiles + "/" + numTotalPictureFiles + ", ");
        sb.append("doc:" + numSharedDocumentFiles + "/" + numTotalDocumentFiles + ", ");
        sb.append("app:" + numSharedApplicationFiles + "/" + numTotalApplicationFiles + ", ");
        sb.append("rng:" + numSharedRingtoneFiles + "/" + numTotalRingtoneFiles);
        sb.append("]");
        sb.append(")");

        return sb.toString();
    }
}
