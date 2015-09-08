package com.ankoma88.audiorecorder2.gui;


import com.ankoma88.audiorecorder2.Player;
import com.ankoma88.audiorecorder2.Recorder;
import com.ankoma88.audiorecorder2.util.Timer;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Logger;

/**Gui starter class with main method*/
public class Gui extends JFrame implements ActionListener {

    public static final String LBL_TIME = "Time: ",
                               TIME_START = " 00:00:00";

    private static final Logger log =
            Logger.getLogger(Gui.class.getSimpleName());

    private static final String MAIN_FRAME_TITLE = "Audio Recorder",
                                FILE_NAME = "record.wav",
                                FILE_NAME_EXTRA = "record_extra.wav",
                                LBL_STATUS = "Status",
                                LBL_EFFECT = "Effect off",
                                BTN_RECORD = "Rec",
                                BTN_PLAY = "Play",
                                BTN_STOP = "Stop",
                                BTN_EXTRA = "Extra",
                                STATUS_RECORDING = "Status: Recording...",
                                STATUS_SAVED = "Status: Record saved.",
                                STATUS_PLAYING = "Status: Playing...",
                                STATUS_STOPPED = "Status: Stopped.",
                                STATUS_ERROR = "ERROR!";

    private JFrame mainFrame;
    private JPanel recorderPanel, statusPanel, topPanel;
    private JButton recordBtn, playBtn, stopBtn, extraBtn;
    private JLabel statusLabel, timeLabel, effectLabel;
    private ImageIcon iconRecord, iconStop, iconPlay, iconExtra;

    private boolean isRecording;
    private boolean isPlaying;
    public static boolean isExtraOn = false;

    private Recorder recorder;
    private Player player;
    private Thread playThread;
    private Timer timer;

    public Gui() {
        initElements();
        setupRecorderPanel();
        setupStatusPanel();
        setupTopPanel();
        addPanelsToMainFrame();
        setupMainFrame();

        log.info("Recorder GUI created");
    }

    private void setupMainFrame() {
        mainFrame.getContentPane().setPreferredSize(new Dimension(500, 110));
        mainFrame.pack();
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLocationByPlatform(true);
        mainFrame.setVisible(true);
    }

    private void initElements() {
        mainFrame =     new JFrame(MAIN_FRAME_TITLE);
        recorderPanel = new JPanel();
        statusPanel =   new JPanel();
        topPanel =      new JPanel();
        recordBtn =     new JButton(BTN_RECORD);
        playBtn =       new JButton(BTN_PLAY);
        stopBtn =       new JButton(BTN_STOP);
        extraBtn =      new JButton(BTN_EXTRA);
        timeLabel =     new JLabel(LBL_TIME);
        effectLabel =   new JLabel(LBL_EFFECT);
        statusLabel =   new JLabel(LBL_STATUS);
        iconRecord =    new ImageIcon(getClass().getResource("/record.png"));
        iconStop =      new ImageIcon(getClass().getResource("/stop.png"));
        iconPlay =      new ImageIcon(getClass().getResource("/play.png"));
        iconExtra =     new ImageIcon(getClass().getResource("/extra.png"));
    }

    /**Panel showing recording/playing time and effect on/off  indicator*/
    private void setupTopPanel() {
        topPanel.setBackground(new Color(255, 235, 59));
        topPanel.setPreferredSize(new Dimension(mainFrame.getWidth(), 20));
        topPanel.setLayout(new FlowLayout());
        topPanel.add(timeLabel);
        effectLabel.setText(LBL_EFFECT);
        topPanel.add(effectLabel);
    }

    /**Panel with recorder controls*/
    private void setupRecorderPanel() {
        recorderPanel.setBackground(new Color(0, 150, 136));
        recorderPanel.setLayout(new FlowLayout());
        recorderPanel.setPreferredSize(new Dimension(mainFrame.getWidth(), 20));

        recordBtn.setFont(new Font("Sans", Font.BOLD, 16));
        playBtn.setFont(new Font("Sans", Font.BOLD, 16));
        stopBtn.setFont(new Font("Sans", Font.BOLD, 16));
        extraBtn.setFont(new Font("Sans", Font.BOLD, 16));

        recordBtn.setIcon(iconRecord);
        playBtn.setIcon(iconPlay);
        stopBtn.setIcon(iconStop);
        extraBtn.setIcon(iconExtra);

        recorderPanel.add(recordBtn);
        recorderPanel.add(playBtn);
        recorderPanel.add(stopBtn);
        recorderPanel.add(extraBtn);

        recordBtn.addActionListener(this);
        playBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        extraBtn.addActionListener(this);

        playBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        extraBtn.setEnabled(true);
    }

/**Status panel: recording/playing/stopped*/
    private void setupStatusPanel() {
        statusPanel.setBackground(new Color(255, 235, 59));
        statusPanel.setPreferredSize(new Dimension(mainFrame.getWidth(), 20));
        statusPanel.setLayout(new FlowLayout());
        statusPanel.add(statusLabel);
    }

    /**Putting all panels to main gui frame*/
    private void addPanelsToMainFrame() {
        mainFrame.getContentPane().add(topPanel, BorderLayout.NORTH);
        mainFrame.getContentPane().add(recorderPanel, BorderLayout.CENTER);
        mainFrame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
    }

    /**Buttons behavior*/
    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();

        if (button == recordBtn) {
            startRecording();
        } else if (button == stopBtn) {
            if (isRecording) {
                stopRecording();
            } else if (isPlaying) {
                stopPlaying();
            }
        } else if (button == playBtn) {
            startPlaying();
        }
        else if (button == extraBtn) {
            toggleEffect();
        }
    }

    /**Switch effect on and off*/
    private void toggleEffect() {
        if (isExtraOn) {
            isExtraOn = false;
            effectLabel.setText("Effect Off");
        } else {
            isExtraOn = true;
            effectLabel.setText("Effect On");
        }
    }

    private void startRecording() {
        recorder = new Recorder(FILE_NAME, FILE_NAME_EXTRA);
        isRecording = true;
        playBtn.setEnabled(false);
        recordBtn.setEnabled(false);
        recorder.recordAudio();
        launchTimer();

        extraBtn.setEnabled(false);
        playBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        statusLabel.setText(STATUS_RECORDING);
    }

    private void stopRecording() {
        isRecording = false;
        timer.stopCount();
        recorder.stopAudio();
        try {
            recorder.saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText(STATUS_ERROR);
        }
        statusLabel.setText(STATUS_SAVED);

        extraBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        playBtn.setEnabled(true);
        recordBtn.setEnabled(true);
    }

    private void startPlaying() {
        player = new Player();
        isPlaying = true;
        launchTimer();
        playThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!isExtraOn) {
                        player.play(FILE_NAME);
                    } else {
                        player.play(FILE_NAME_EXTRA);
                    }
                    isPlaying = false;
                    timer.stopCount();
                    recordBtn.setEnabled(true);
                    playBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                    extraBtn.setEnabled(true);
                    statusLabel.setText(STATUS_STOPPED);

                } catch (UnsupportedAudioFileException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });

        playThread.start();

        extraBtn.setEnabled(false);
        playBtn.setEnabled(false);
        recordBtn.setEnabled(false);
        stopBtn.setEnabled(true);
        statusLabel.setText(STATUS_PLAYING);
    }

    private void stopPlaying() {
        isPlaying = false;
        timer.reset();
        player.stopPlaying();
        playThread.interrupt();

        extraBtn.setEnabled(true);
        playBtn.setEnabled(true);
        recordBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        statusLabel.setText(STATUS_STOPPED);
    }

    private void launchTimer() {
        timer = new Timer(timeLabel);
        timer.start();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Gui();
            }
        });
    }
}
