package com.ankoma88.audiorecorder2;


import com.ankoma88.audiorecorder2.filter.Distortion;
import com.ankoma88.audiorecorder2.filter.Filter;
import com.ankoma88.audiorecorder2.gui.Gui;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class Recorder extends Thread {

    private static final float SAMPLE_RATE = 44100.0F;
    private static final int SAMPLE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 2;
    private static final boolean BIG_ENDIAN = false;

    private ByteArrayOutputStream audioBAOS;
    private ByteArrayOutputStream audioBAOSWithEffect;
    private TargetDataLine line;
    private AudioFormat format;

    private File file;
    private File fileExtra;

    private boolean isRecording;


    public Recorder(String fileName, String fileNameExtra) {

        try {
            format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    SAMPLE_BITS,
                    CHANNELS,
                    FRAME_SIZE,
                    SAMPLE_RATE,
                    BIG_ENDIAN);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                throw new LineUnavailableException(
                        "Format not supported");
            }
            this.line = (TargetDataLine) AudioSystem.getLine(info);
            this.line.open(format);
            this.file = new File(fileName);
            this.fileExtra = new File(fileNameExtra);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void recordAudio() {
        start();
    }

    public void stopAudio() {
        isRecording = false;
        if (this.line != null) {
            this.line.drain();
            this.line.close();
        }
        interrupt();
    }

    public void run() {
        try {
            this.line.open();
            this.line.start();
            writeAudioBAOS();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

    }

    private void writeAudioBAOS() {
        int bufferSize = 4096;
        byte[] bytes = new byte[bufferSize];
        int bytesRead;
        audioBAOS = new ByteArrayOutputStream();
        audioBAOSWithEffect = new ByteArrayOutputStream();
        isRecording = true;

        while (isRecording) {
            bytesRead = this.line.read(bytes, 0, bytes.length);
            audioBAOS.write(bytes, 0, bytesRead);

            if (Gui.isExtraOn) {
                byte[] bytesWithEffect = bytes.clone();
                addDistortion(bytesWithEffect, bytesRead);
                audioBAOSWithEffect.write(bytesWithEffect, 0, bytesWithEffect.length);
            }


        }
    }

    public void saveToFile() throws IOException {
        byte[] audioData = audioBAOS.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream =
                new AudioInputStream(bais, format,audioData.length / format.getFrameSize());

        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, file);

        audioInputStream.close();
        audioBAOS.close();

        if (Gui.isExtraOn) {
            byte[] audioDataWithEffect = audioBAOSWithEffect.toByteArray();
            System.out.println(audioDataWithEffect.length);
            ByteArrayInputStream baisWithEffect = new ByteArrayInputStream(audioDataWithEffect);
            System.out.println(baisWithEffect.toString());
            AudioInputStream audioInputStreamWithEffect =
                    new AudioInputStream(baisWithEffect, format, audioData.length / format.getFrameSize());
            System.out.println(audioInputStreamWithEffect);
            AudioSystem.write(audioInputStreamWithEffect, AudioFileFormat.Type.WAVE, fileExtra);

            audioInputStreamWithEffect.close();
            audioBAOSWithEffect.close();
        }
    }

    private void addDistortion(byte[] buffer, int bytesRead) {
        int[] intData = intArrayFrom16bits(bytesRead, buffer);
        int numShorts = bytesRead / 16;
        Filter filter = new Distortion();
        filter.filter(numShorts, intData);
        intArrayTo16bits(buffer, intData, numShorts);
    }

    public static int[] intArrayFrom16bits(int numBytesRead, byte[] data) {
        int[] intData = new int[numBytesRead];
        ShortBuffer shortBuffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
        for (int i = 0; i < shortBuffer.limit(); i++) {
            intData[i] = shortBuffer.get(i);
        }
        return intData;
    }

    public static void intArrayTo16bits(byte[] data, int[] intData, int numShorts) {
        for (int i = 0; i < numShorts; i++) {
            short value = (short) intData[i];
            data[i * 2] = (byte) ((value >> 8) & 0xFF);
            data[i * 2 + 1] = (byte) (value & 0xFF);
        }
    }

}
