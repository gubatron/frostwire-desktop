package com.apple.eawt;

import java.util.EventObject;

public abstract class AppEvent extends EventObject {
    AppEvent() {
        super(Application.getApplication());
    }

    public static class AboutEvent extends AppEvent {
        AboutEvent() {
        }
    }

    public static class QuitEvent extends AppEvent {
        QuitEvent() {
        }
    }

    public static class PreferencesEvent extends AppEvent {
        PreferencesEvent() {
        }
    }
}
