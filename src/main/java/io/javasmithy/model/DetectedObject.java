package io.javasmithy.model;

import io.javasmithy.utils.CoordConverter;
import org.tensorflow.ndarray.buffer.FloatDataBuffer;

public class DetectedObject {

    private float detectionScore;
    private float detectionClass;
    private float[] detectionBox;

    public DetectedObject(float detectionScore, float detectionClass, float[] detectionBox) {
        this.detectionBox = detectionBox;
        this.detectionScore = detectionScore;
        this.detectionClass = detectionClass;
    }

    public float getDetectionScore() {
        return detectionScore;
    }

    public float getDetectionClass() {
        return detectionClass;
    }

    public void setDetectionClass(float detectionClass) {
        this.detectionClass = detectionClass;
    }

    public float[] getDetectionBox() {
        return detectionBox;
    }

    public void setDetectionBox(float[] detectionBox) {
        this.detectionBox = detectionBox;
    }

    public void setDetectionScore(float detectionScore) {
        this.detectionScore = detectionScore;
    }

    public double[] getCoords(){
        double x, y, width, height;
        double top = CoordConverter.deNormalize(this.detectionBox[0], 640);
        double left = CoordConverter.deNormalize(this.detectionBox[1], 640);
        double bottom = CoordConverter.deNormalize(this.detectionBox[2], 640);
        double right = CoordConverter.deNormalize(this.detectionBox[3], 640);

        y = top;
        x = left;
        width = (left+right) - 640;
        height = (top+bottom) -  640;

        return new double[] {x, y, height, width};
    }
}
