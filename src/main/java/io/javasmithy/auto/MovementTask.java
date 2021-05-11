package io.javasmithy.auto;

import java.awt.event.KeyEvent;


public class MovementTask extends RobotTask {

    public MovementTask(){
        super();
    }

    @Override
    public void run(){
        for (int i = 0; i < 10; i++){
            this.robot.keyPress(KeyEvent.VK_W);
            this.robot.delay(3000);
            this.robot.keyRelease(KeyEvent.VK_W);

            this.robot.keyPress(KeyEvent.VK_A);
            this.robot.delay(250);
            this.robot.keyRelease(KeyEvent.VK_A);
        }
    }

}
