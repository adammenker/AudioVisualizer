/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioviz;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author Professor Wergeles 
 * 
 * Music: 
 * http://www.bensound.com/royalty-free-music
 * http://www.audiocheck.net/testtones_sinesweep20-20k.php
 * 
 * 
 * References: 
 * http://stackoverflow.com/questions/11994366/how-to-reference-primarystage
 */
public class PlayerController implements Initializable {

    @FXML
    private AnchorPane vizPane;

    @FXML
    private MediaView mediaView;

    @FXML
    private Text filePathText;

    @FXML
    private Text lengthText;

    @FXML
    private Text currentText;

    @FXML
    private Text bandsText;

    @FXML
    private Text visualizerNameText;

    @FXML
    private Text errorText;

    @FXML
    private Menu visualizersMenu;

    @FXML
    private Menu bandsMenu;

    @FXML
    private Slider timeSlider;

    @FXML
    private Button playPause;
    
    @FXML
    private Slider volumeSlider;
    
    @FXML
    private Text currentVol;
    
    @FXML
    private ProgressBar lengthBar;

    private Media media;
    private MediaPlayer mediaPlayer;

    private Integer numOfBands = 40;
    private final Double updateInterval = 0.05;

    private ArrayList<Visualizer> visualizers;
    private Visualizer currentVisualizer;
    private final Integer[] bandsList = {4, 8, 16, 40, 60, 100};

    private int currentStatus = 0;
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bandsText.setText(Integer.toString((numOfBands/4) * 3));
        bandsText.setFill(Paint.valueOf("#E6E5F8"));

        visualizers = new ArrayList<>();
        visualizers.add(new Amdy6SuperVisual1(320.0));
        visualizers.add(new Amdy6SuperVisual2(320.0));
        
        
        for (Visualizer visualizer : visualizers) {
            MenuItem menuItem = new MenuItem(visualizer.getName());
            menuItem.setUserData(visualizer);
            menuItem.setOnAction((ActionEvent event) -> {
                selectVisualizer(event);
            });
            visualizersMenu.getItems().add(menuItem);
        }
        
        currentVisualizer = visualizers.get(0);
        visualizerNameText.setText(currentVisualizer.getName());
        visualizerNameText.setFill(Paint.valueOf("#E6E5F8"));

        for (Integer bands : bandsList) {
            MenuItem menuItem = new MenuItem(Integer.toString((bands / 4) * 3));
            menuItem.setUserData(bands);
            menuItem.setOnAction((ActionEvent event) -> {
                selectBands(event);
            });
            bandsMenu.getItems().add(menuItem);
        }
    }

    private void selectVisualizer(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        Visualizer visualizer = (Visualizer) menuItem.getUserData();
        changeVisualizer(visualizer);
    }

    private void selectBands(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        numOfBands = (Integer) menuItem.getUserData();
        if (currentVisualizer != null) {
            currentVisualizer.start(numOfBands, vizPane);
        }
        if (mediaPlayer != null) {
            mediaPlayer.setAudioSpectrumNumBands(numOfBands);
        }
        bandsText.setText(Integer.toString((numOfBands/4) * 3));
        bandsText.setFill(Paint.valueOf("#E6E5F8"));
    }

    private void changeVisualizer(Visualizer visualizer) {
        if (currentVisualizer != null) {
            currentVisualizer.end();
        }
        currentVisualizer = visualizer;
        currentVisualizer.start(numOfBands, vizPane);
        visualizerNameText.setText(currentVisualizer.getName());
        visualizerNameText.setFill(Paint.valueOf("#E6E5F8"));
    }

    private void openMedia(File file) {
        filePathText.setText("");
        filePathText.setFill(Paint.valueOf("#E6E5F8"));
        errorText.setText("");

        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        try {
            media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnReady(() -> {
                handleReady();
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                handleEndOfMedia();
            });
            mediaPlayer.setAudioSpectrumNumBands(numOfBands);
            mediaPlayer.setAudioSpectrumInterval(updateInterval);
            mediaPlayer.setAudioSpectrumListener((double timestamp, double duration, float[] magnitudes, float[] phases) -> {
                handleVisualize(timestamp, duration, magnitudes, phases);
            });
            mediaPlayer.setAutoPlay(false); // sets whether choosing a song will start the functionality
            filePathText.setText(file.getPath());
            currentStatus = 1;
            mediaPlayer.play();
            playPause.setText("Pause");
        } catch (Exception ex) {
            errorText.setText(ex.toString());
        }
    }

    private void handleReady() {
        Duration duration = mediaPlayer.getTotalDuration();
        
        String durationMinutes = String.format ("%.2f", duration.toMinutes());
        durationMinutes = durationMinutes.replace(".", ":");
        durationMinutes = durationMinutes + "s";
                
        lengthText.setText(durationMinutes);
        lengthText.setFill(Paint.valueOf("#E6E5F8"));
        Duration ct = mediaPlayer.getCurrentTime();
        currentText.setText(ct.toString());
        currentText.setFill(Paint.valueOf("#E6E5F8"));
        currentVisualizer.start(numOfBands, vizPane);
        timeSlider.setMin(0);
        timeSlider.setMax(duration.toMillis());
        
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(1);
        currentVol.setText(String.valueOf((int)(volumeSlider.getValue() * 100)  + "%"));
        currentVol.setFill(Paint.valueOf("#E6E5F8"));
    }

    private void handleEndOfMedia() {
        mediaPlayer.stop();
        mediaPlayer.seek(Duration.ZERO);
        timeSlider.setValue(0);
        lengthBar.setProgress(0);
        volumeSlider.setValue(0);
    }

    private void handleVisualize(double timestamp, double duration, float[] magnitudes, float[] phases) {
        Duration ct = mediaPlayer.getCurrentTime();
        Duration durationTime = mediaPlayer.getTotalDuration();
        int sec = (int)ct.toSeconds();
        int min = (int)ct.toMinutes();
        double ms = ct.toMillis();
        sec = sec % 60;
        if(sec >= 60){
            sec = 0;
        }
                
        if(sec < 10){
            currentText.setText(String.format("%d:0%d s", min, sec));
        }else{
           currentText.setText(String.format("%d:%d s", min, sec)); 
        }
        Double lengthBarProgress = (ct.toMillis() / durationTime.toMillis()); 
        lengthBar.setProgress(lengthBarProgress + 0.015);
        timeSlider.setValue(ms);
        currentVisualizer.draw(timestamp, duration, magnitudes, phases);
    }

    @FXML
    private void handleOpen(Event event) {
        Stage primaryStage = (Stage) vizPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            openMedia(file);
        }
    }

    @FXML
    private void handlePlayPause(ActionEvent event) {
        if (mediaPlayer != null) {
            if (currentStatus == 0) {
                currentStatus = 1;
                mediaPlayer.play();
                volumeSlider.setValue(mediaPlayer.getVolume());
                playPause.setText("Pause");
            } else {
                currentStatus = 0;
                mediaPlayer.pause();
                playPause.setText("Play");
            }
        }
    }
    
    @FXML
    private void handleSliderMousePressed(Event event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    private void handleSliderMouseReleased(Event event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(timeSlider.getValue()));
            currentVisualizer.start(numOfBands, vizPane);
            mediaPlayer.play();
            if(playPause.getText().equals("Pause")){
               playPause.setText("Pause"); 
            } else if(playPause.getText().equals("Play")){
               playPause.setText("Pause"); 
            }
            
        }
    }

    @FXML
    private void handleVolumeSliderMouseReleased(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volumeSlider.getValue());
//            currentVol.setText(String.valueOf((volumeSlider.getValue())));
            currentVol.setText(String.valueOf((int)(volumeSlider.getValue() * 100) + "%"));
            currentVol.setFill(Paint.valueOf("#E6E5F8"));
        }
    }
}
