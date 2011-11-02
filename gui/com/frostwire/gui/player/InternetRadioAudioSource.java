package com.frostwire.gui.player;

import java.net.URL;

import com.frostwire.alexandria.InternetRadioStation;

public class InternetRadioAudioSource extends AudioSource {

    private InternetRadioStation station;

	public InternetRadioAudioSource(URL url, InternetRadioStation station) {
        super(url);
        this.station = station;
    }

	public InternetRadioStation getInternetRadioStation() {
		return station;
	}
}
