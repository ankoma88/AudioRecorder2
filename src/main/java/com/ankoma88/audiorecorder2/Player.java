package com.ankoma88.audiorecorder2;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**Audio player class. Plays recorded sound in clip*/
public class Player implements LineListener {

	private boolean playCompleted;
	private boolean isStopped;
	private AudioFormat format;

	public void play(String filePath) throws UnsupportedAudioFileException,
			IOException, LineUnavailableException {

		File audioFile = new File(filePath);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
		format = audioStream.getFormat();

		DataLine.Info info = new DataLine.Info(Clip.class, format);

		Clip audioClip = (Clip) AudioSystem.getLine(info);
		audioClip.addLineListener(this);
		audioClip.open(audioStream);
		audioClip.start();

		playCompleted = false;

		while (!playCompleted) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException iex) {
				if (isStopped) {
					audioClip.stop();
					break;
				}
			}
		}
		audioClip.close();
	}

	public void stopPlaying() {
		isStopped = true;
	}


	@Override
	public void update(LineEvent event) {
		LineEvent.Type type = event.getType();
		if (type == LineEvent.Type.STOP || type == LineEvent.Type.CLOSE) {
			playCompleted = true;
		}
	}
}