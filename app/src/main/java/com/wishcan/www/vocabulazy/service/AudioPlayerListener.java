package com.wishcan.www.vocabulazy.service;

import android.media.AudioManager;

public interface AudioPlayerListener extends VLTextToSpeech.OnEngineStatusListener, VLTextToSpeech.OnUtteranceFinishListener, AudioManager.OnAudioFocusChangeListener {

}
