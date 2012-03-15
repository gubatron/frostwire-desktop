package com.frostwire.youtubetest;

public class GeneralSettings {

    public static int getHttpConnectTimeout() {
        return 10000;
    }

    public static int getHttpReadTimeout() {
        return 30000;
    }

    public static IfFileExistsAction getIfFileExistsAction() {
        return IfFileExistsAction.ASK_FOR_EACH_FILE;
    }
}
