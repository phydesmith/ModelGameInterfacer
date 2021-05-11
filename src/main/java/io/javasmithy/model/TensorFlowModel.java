package io.javasmithy.model;

import org.tensorflow.*;
import org.tensorflow.ndarray.ByteNdArray;
import org.tensorflow.ndarray.NdArrays;
import org.tensorflow.types.TUint8;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.ndarray.StdArrays;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.ndarray.buffer.ByteDataBuffer;


//import java.awt.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

public class TensorFlowModel {
    SavedModelBundle smb;

    public TensorFlowModel(){
        this.smb = SavedModelBundle.load("src/main/resources/io/javasmithy/model_1/saved_model", "serve");
        /*
        for (Signature s:
                this.smb.signatures()
             ) {
            System.out.println(s.key());
        }
        Iterator iterator = this.smb.graph().operations();
        while(iterator.hasNext()){
            Operation op =  (Operation) iterator.next();
            System.out.println(op.name());
        }
        */

    }

    public ArrayList<DetectedObject>  callModel(BufferedImage image, double scoreThreshold) {
        System.out.println("Starting call...");
        ArrayList<DetectedObject> detectedObjectsInImage = new ArrayList<DetectedObject>();
        List<Tensor> outputsList = null;

        //Map<String, Tensor> output = smb.call(input);

        Map<String, Tensor> outputMap = null;
        try ( Tensor input = processImage(image) ) {
            Map<String, Tensor> inputMap = new HashMap<>();

            inputMap.put("input_tensor", processImage(image));
            System.out.println("TENSOR HASH : " + inputMap.get("input_tensor").toString());
            outputMap = smb.call(inputMap);

            Session sesh = smb.session();
            Session.Runner runner = sesh.runner();

            System.out.println("\n INPUT NAMES: " + smb.signatures().get(1).inputNames());

            outputsList = smb.session()
                            .runner()
                            .feed("[input_tensor]", input)
                            //.feed("input", input)
                            //.feed("input_query", input)
                            //.feed("serving_default", input)
                            .fetch("detection_scores")
                            .fetch("detection_classes")
                            .fetch("detection_boxes")
                            .run();
            input.close();

            inputMap.get("input_tensor").close();
        }

        /*
        try (Tensor scoresT = outputMap.get("detection_scores");
             Tensor classesT = outputMap.get("detection_classes");
             Tensor boxesT = outputMap.get("detection_boxes");
        ) {
        */

        try (Tensor scoresT = outputsList.get(0);
             Tensor classesT = outputsList.get(1);
             Tensor boxesT = outputsList.get(2); ){


            int maxObjects = (int) scoresT.asRawTensor().data().asFloats().size();
            //float[] scores = scoresT.copyTo(new float[1][maxObjects])[0];
            //float[] classes = classesT.copyTo(new float[1][maxObjects])[0];
            //float[][] boxes = boxesT.copyTo(new float[1][maxObjects][4])[0];

            float[] scores = new float[maxObjects];
            float[] classes = new float[maxObjects];
            float[][] boxes = new float[maxObjects][4];
            for (int i = 0; i < scores.length; i++){
                scores[i] = scoresT.asRawTensor().data().asFloats().getFloat(i);
                classes[i] = classesT.asRawTensor().data().asFloats().getFloat(i);
            }

            int c = 0;
            for (int i = 0; i < maxObjects; i++){
                boxes[i][0] = boxesT.asRawTensor().data().asFloats().getFloat(c);
                boxes[i][1] = boxesT.asRawTensor().data().asFloats().getFloat(c+1);
                boxes[i][2] = boxesT.asRawTensor().data().asFloats().getFloat(c+2);
                boxes[i][3] = boxesT.asRawTensor().data().asFloats().getFloat(c+3);
                c+=4;
            }



            //System.out.println(Arrays.toString(scores));
            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] > scoreThreshold) {
                    System.out.println("Added an object");
                    detectedObjectsInImage.add(new DetectedObject(scores[i], classes[i], boxes[i]));
                }
            }
            scoresT.close();
            classesT.close();
            boxesT.close();
        }


        outputMap.forEach( (k,v) -> v = null );
        System.out.println("Ending call...");
        return detectedObjectsInImage;
    }


    // *************************
    // https://github.com/davpapp/PowerMiner/blob/master/src/ObjectDetector.java
    // no longer exists https://github.com/tensorflow/models/blob/master/research/object_detection/
    private Tensor processImage(BufferedImage image){
        BufferedImage formattedImage = convertBufferedImage(image, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] data = ((DataBufferByte) formattedImage.getData().getDataBuffer()).getData();

        Raster raster =  formattedImage.getData();
        //System.out.println("NUMBANKS: " + raster.getDataBuffer().getNumBanks());
        //System.out.println("NUM ELEMENTS: " + raster.getNumDataElements());

        bgr2rgb(data);

        System.out.println("Image data: " + data[0] + data[1]);

        byte[][][][] shapedData = formArray(data);

        final long BATCH_SIZE=1;
        final long CHANNELS=3;
        Shape shape = Shape.of(
                        BATCH_SIZE,
                        formattedImage.getHeight(),
                        formattedImage.getWidth(),
                        CHANNELS);
        return TUint8.tensorOf(shape, values -> StdArrays.copyTo(shapedData, values));
    }

    private byte[][][][] formArray(byte[] data){
        byte[][][] formattedBytes = new byte[640][640][3];

        byte[] b = new byte[409600];
        byte[] g = new byte[409600];
        byte[] r = new byte[409600];

        for (int i = 0; i < 409600; i++){
            b[i] = data[i];
        }
        for (int i = 409600; i < 819200; i++){
            g[i%409600] = data[i];
        }
        for (int i = 819200; i < data.length; i++){
            r[i%819200] = data[i];
        }


        for (int row = 0; row < formattedBytes[0][0].length; row++){
            int startIndex = row*640;
            for (int column = startIndex; column<formattedBytes[0][0].length; column++){
                formattedBytes[row][column][0] = b[column];
                formattedBytes[row][column][1] = g[column];
                formattedBytes[row][column][2] = r[column];
            }
        }

        byte[][][][] lastHope = new byte[1][640][640][3];
        lastHope[0] = formattedBytes;

        return lastHope;
    }

    /*
    private Tensor<UInt8> makeImageTensor(BufferedImage image) throws IOException {
        BufferedImage formattedImage = convertBufferedImage(image, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = ((DataBufferByte) formattedImage.getData().getDataBuffer()).getData();
        System.out.println(formattedImage.getData().getDataBuffer().getNumBanks());
        bgr2rgb(data);
        final long BATCH_SIZE = 1;
        final long CHANNELS = 3;
        long[] shape = new long[] {BATCH_SIZE, formattedImage.getHeight(), formattedImage.getWidth(), CHANNELS};
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        data = null;
        return Tensor.create(UInt8.class, shape, byteBuffer);
    }
    */


    private BufferedImage convertBufferedImage(BufferedImage sourceImage, int bufferedImageType) {
        BufferedImage image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), bufferedImageType);
        Graphics2D g2d = image.createGraphics();
        g2d.drawImage(sourceImage, 0, 0, null);
        g2d.dispose();
        return image;
    }

    private void bgr2rgb(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            byte tmp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = tmp;
        }
    }

    // *************************
}
