package io.javasmithy.auto;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;


public class CaptureTask extends RobotTask {
    long batchTime;
    String picName;
    Image image;
    Boolean takeCaptures;
    BufferedImage bufferedImage;

    public CaptureTask(String picName){
        super();
        this.picName = picName;
        this.batchTime = System.nanoTime();
        this.takeCaptures = true;
        createFolder();
    }

    @Override
    public void run(){
        while(takeCaptures){
            this.robot.delay(10);

            this.bufferedImage = this.robot.createScreenCapture(new Rectangle(640, 220, 640, 640));
            this.image = SwingFXUtils.toFXImage(
                    this.bufferedImage,
                    null
            );
            try {
                this.batchTime = System.nanoTime();
                //ImageIO.write(buffImage, "png", new File("captures/"+this.picName+"/capture_"+ this.batchTime + "_" + this.picName +".png"));
                ImageIO.write(this.bufferedImage, "png", new File("captures/"+this.picName+"/currentCapture" +".png"));
                System.out.print("\r File written " + this.batchTime);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void createFolder(){
        System.out.println(this.picName);
        File file = new File("captures/"+this.picName+"/");
        System.out.println("Directory created: " + file.mkdir());
    }

    public Image getImage(){
        if (this.image == null) System.out.println("Image is null");
        return this.image;
    }
    public BufferedImage getBufferedImage(){
        if (this.bufferedImage == null) System.out.println("BufferedImage is null");
        return this.bufferedImage;
    }

    public void setTakeCaptures(Boolean status){
        this.takeCaptures = status;
    }

}
