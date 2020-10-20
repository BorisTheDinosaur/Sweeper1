package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;




import java.util.ArrayList;


public class Main extends Application {

    private static final int width = 300;
    private static final int height = 200;
    private static final int tileSize = 20;
    private static final int tileWidth = width / tileSize;
    private static final int tileHeight = height / tileSize;
    private final Tile[][] matrix = new Tile[tileWidth][tileHeight];
    private Scene scene;
    private int countTrueFlags;
    private int countBombs;
    private int countFlags;
    private double a = 6.25;
    private final Label num = new Label("0");
    private Timer time;

    private Parent create() {

        double rand;
        if (a == 0.0) {
            rand = 0.1;
        }
        if (a == 12.5) {
            rand = 0.3;
        }
        else {
            rand = 0.2;
        }

        MenuBar menuBar = new MenuBar();

        Menu mainMenu = new Menu("_Menu");
        mainMenu.setMnemonicParsing(true);

        Menu optionsMenu = new Menu("_Options");

        Menu levelMenu = new Menu("Level");
        Slider sliderLevel = new Slider(0.0, 12.5, a);
        sliderLevel.setSnapToTicks(true);
        sliderLevel.setBlockIncrement(6.25);
        sliderLevel.setShowTickMarks(true);
        CustomMenuItem level = new CustomMenuItem(sliderLevel);
        levelMenu.getItems().add(level);

        optionsMenu.getItems().addAll(levelMenu);


        MenuItem newGameItem = new MenuItem("_New Game");
        newGameItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newGameItem.setOnAction(event -> {
            a = sliderLevel.getValue();
            scene.setRoot(create());
        });

        MenuItem exitItem = new MenuItem("_Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        exitItem.setOnAction(event -> Platform.exit());

        mainMenu.getItems().addAll(newGameItem, optionsMenu, new SeparatorMenuItem(), exitItem);

        menuBar.getMenus().addAll(mainMenu);


        BorderPane root = new BorderPane();
        root.setPrefSize(width - 9, height + 55);
        root.setTop(menuBar);
        root.setBackground(new Background(new BackgroundFill(Color.DIMGREY, CornerRadii.EMPTY, Insets.EMPTY)));


        num.setFont(Font.font(30));
        num.setTextFill(Color.AQUAMARINE);

        time = new Timer();

        root.setRight(time);
        root.setLeft(num);


        countBombs = 0;
        countTrueFlags = 0;
        countFlags = 0;
        for (int j = 0; j < tileHeight; j++) {
            for (int i = 0; i < tileWidth; i++) {
                Tile tile = new Tile(i, j, Math.random() < rand);
                matrix[i][j] = tile;
                root.getChildren().add(tile);
            }
        }
        for (int j = 0; j < tileHeight; j++) {
            for (int i = 0; i < tileWidth; i++) {
                Tile tile = matrix[i][j];
                if (tile.bomb) {
                    countBombs++;
                    continue;
                }
                tile.bombAround = (int) listNeighbors(tile).stream().filter(titlee -> titlee.bomb).count();
                tile.value.setText(String.valueOf(tile.bombAround));
            }
        }



        num.setText(String.valueOf(countBombs - countFlags));


        return root;
    }


    private ArrayList<Tile> listNeighbors(Tile tile) {
        ArrayList<Tile> neighbors = new ArrayList<>();

        int[] xDif = new int[]{-1, 0, 1, -1, 1, -1, 0, 1};
        int[] yDif = new int[]{-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < xDif.length; i++) {
            int xNei = tile.x + xDif[i];
            int yNei = tile.y + yDif[i];

            if (xNei >=0 && yNei >=0 && xNei < tileWidth && yNei < tileHeight) neighbors.add(matrix[xNei][yNei]);
        }

        return neighbors;
    }


    private class Tile extends StackPane {
        private final int x;
        private final int y;
        private final boolean bomb;
        private int bombAround = 0;
        private boolean visible = false;
        private boolean flag = false;

        private final Rectangle cube = new Rectangle(tileSize, tileSize);
        private final Text value = new Text();

        public Tile(int x, int y, boolean bomb) {
            this.x = x;
            this.y = y;
            this.bomb = bomb;

            value.setStroke(Color.LIGHTGRAY);
            value.setVisible(false);
            cube.setFill(Color.GRAY);
            cube.setStroke(Color.AQUAMARINE);
            getChildren().addAll(cube, value);
            setTranslateX(10 + x * tileSize);
            setTranslateY(74 + y * tileSize);

            setOnMouseClicked(
                    event -> {
                        if (event.getButton().equals(MouseButton.PRIMARY)) see();
                        if (event.getButton().equals(MouseButton.SECONDARY)) flagged();
                    });
        }




        public void see() {
            if (flag || visible) return;
            if (bomb) {
                cube.setFill(Color.RED);
                time.stop();
                newWindow("You Lost!");
                return;
            }
            visible = true;
            value.setVisible(true);
            cube.setFill(Color.BLACK);
            if (value.getText().equals("0"))
                for (Tile neighbor: listNeighbors(this)) {
                    value.setText("");
                    if (neighbor.flag) neighbor.flagged();
                    neighbor.see();
                }
        }

        public void flagged() {
            if (visible) return;

            if (flag) {
                cube.setFill(Color.GRAY);
                if (this.bomb) countTrueFlags--;
                countFlags--;
            }

            if (countFlags >= countBombs) return;
            flag = !flag;

            if (flag) {
                cube.setFill(Color.ORANGE);
                if (this.bomb) countTrueFlags++;
                countFlags++;
            }

            num.setText(String.valueOf(countBombs - countFlags));

            if (countTrueFlags == countBombs) {
                time.setValue(time.getValue());
                time.stop();
                newWindow("You Won!!!");
            }
        }
    }


    private void newWindow(String str) {
        Stage stage = new Stage();
        stage.setTitle("Canep");
          Image icon = new Image(getClass().getResourceAsStream("/sample/resources/iconImage.png"));
          stage.getIcons().add(icon);

        Pane pane = new Pane();


        Label label = new Label(str);
        Button button1 = new Button("New Game");
        button1.setOnAction(event -> {
            scene.setRoot(create());
            stage.close();
        });

        ImageView cup;
        if (str.equals("You Lost!")) cup = new ImageView("/sample/resources/explosionImage.png");
        else cup = new ImageView("/sample/resources/cupImage.png");
        cup.setFitHeight(170);
        cup.setFitWidth(230);

        Label timeValue = new Label("Your Time: " + time.getValue());


        pane.getChildren().addAll(label, button1, cup, timeValue);
        label.setTranslateX(210);
        label.setTranslateY(25);
        label.setFont(new Font("Arial", 30));
        button1.setTranslateX(200);
        button1.setTranslateY(110);
        timeValue.setTranslateX(200);
        timeValue.setTranslateY(80);
        timeValue.setFont(new Font("Arial", 20));
        cup.setTranslateX(-30);

        Scene sceneNew = new Scene(pane, 400, 170, Color.BLACK);

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(sceneNew);
        stage.showAndWait();
        stage.setResizable(false);


    }


    @Override
    public void start(Stage stage) {
        stage.setTitle("Canep");


        Image icon = new Image(getClass().getResourceAsStream("/sample/resources/iconImage.png"));
        stage.getIcons().add(icon);


        scene = new Scene(create());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}