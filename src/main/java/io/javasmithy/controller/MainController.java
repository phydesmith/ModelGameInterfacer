package io.javasmithy.controller;

import io.javasmithy.auto.CaptureTask;
import io.javasmithy.model.DetectedObject;
import io.javasmithy.model.TensorFlowModel;
import io.javasmithy.utils.CoordConverter;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import org.tensorflow.RawTensor;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    private TensorFlowModel tfm;
    Random random;
    private Stage stage;
    CaptureTask captureTask;
    AnimationTimer animationTimer;
    Boolean capturing;

    @FXML
    Canvas captureCanvas;
    @FXML
    Button startButton, topButton, singleCaptureButton, continuousCaptureButton, exitButton;
    @FXML
    Label socketLabel;
    @FXML
    TextArea imageWriteInfoTextArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.captureTask = new CaptureTask("testCaptures");
        this.capturing = false;
        this.random = new Random();
        this.tfm = new TensorFlowModel();

    }



    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void exit(){
        Platform.exit();
        System.exit(0);
    }

    @FXML
    private void startCaptureTask(){
        this.captureTask = new CaptureTask("testCaptures");
        this.captureTask.start();
        this.startButton.setDisable(true);

    }
    @FXML
    private void stopCaptureTask(){
        this.captureTask.setTakeCaptures(false);
        this.startButton.setDisable(false);
    }

    @FXML
    private void toggleContinuous(){
        if (animationTimer == null){
            this.animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    drawCapture();
                }
            };
        }

        if (capturing){
            this.capturing = false;
            this.animationTimer.stop();
        } else {
            this.capturing = true;
            this.animationTimer.start();
        }

    }

    @FXML
    private void drawCapture(){

        captureCanvas.getGraphicsContext2D().drawImage(this.captureTask.getImage(), 0, 0);
        List<DetectedObject> results =  tfm.callModel(this.captureTask.getBufferedImage(), .07);

        System.out.println("Drawing Rectangles..");
        //  TO DRAW RECTANGLES
        for (DetectedObject detectedObject: results) {
            double[] coords = detectedObject.getCoords();
            System.out.println("Drawing Rectangle: " + Arrays.toString(coords));
            captureCanvas.getGraphicsContext2D().setStroke(Color.RED);
            captureCanvas.getGraphicsContext2D().strokeRect(
                    coords[0],
                    coords[1],
                    coords[2],
                    coords[3]
            );
        }
        System.out.println("Finished Drawing Rectangles!");

    }

}