package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;


public class Timer extends BorderPane {
    private int min = 0;
    private int sec = 0;
    private Timeline timeline;

    Label time = new Label("00:00");

    public Timer() {

        time.setFont(Font.font(30));
        time.setTextFill(Color.AQUAMARINE);

        setRight(time);
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            sec++;
            if (sec == 60) {
                sec = 0;
                min++;
            }
            time.setText(String.format("%02d:%02d", min, sec));
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    public String getValue() {
        return time.getText();
    }

    public void stop() {
        timeline.stop();
    }

    public void setValue(String str) {
        time.setText(str);
    }

}
