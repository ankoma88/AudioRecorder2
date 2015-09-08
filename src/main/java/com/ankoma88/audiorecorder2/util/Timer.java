package com.ankoma88.audiorecorder2.util;

import com.ankoma88.audiorecorder2.gui.Gui;

import javax.swing.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**Timer class for record/play time */
public class Timer extends Thread {
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	private boolean isCounting = false;
	private boolean isReset = false;
	private long startTime;
	private JLabel labelRecordTime;

	public Timer(JLabel jLabel) {
		this.labelRecordTime = jLabel;
	}

	public void run() {
		isCounting = true;
		
		startTime = System.currentTimeMillis();
		
		while (isCounting) {
			try {
				Thread.sleep(1000);
				labelRecordTime.setText(Gui.LBL_TIME + getTime());
			} catch (InterruptedException ex) {
				ex.printStackTrace();
				if (isReset) {
					labelRecordTime.setText(Gui.LBL_TIME + Gui.TIME_START);
					isCounting = false;
					break;
				}
			}
		}
	}


	public void stopCount() {
		isCounting = false;
	}

	public void reset() {
		isReset = true;
		isCounting = false;
	}

	private String getTime() {
		long now = System.currentTimeMillis();
		Date current = new Date(now - startTime);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(current);
	}

}