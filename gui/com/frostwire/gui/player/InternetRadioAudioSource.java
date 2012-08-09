package com.frostwire.gui.player;

import com.frostwire.alexandria.InternetRadioStation;

public class InternetRadioAudioSource extends AudioSource {

    private InternetRadioStation station;

	public InternetRadioAudioSource(String url, InternetRadioStation station) {
        super(url);
        this.station = station;
    }

	public InternetRadioStation getInternetRadioStation() {
		return station;
	}
}
