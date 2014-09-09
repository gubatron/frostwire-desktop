package com.frostwire.bittorrent;

public enum BTDownloadState {

    QUEUED_FOR_CHECKING,
    CHECKING_FILES,
    DOWNLOADING_METADATA,
    DOWNLOADING,
    FINISHED,
    SEEDING,
    ALLOCATING,
    CHECKING_RESUME_DATA,
    PAUSED
}
