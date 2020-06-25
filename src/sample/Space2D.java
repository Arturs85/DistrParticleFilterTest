package sample;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class Space2D {

    ArrayList<javafx.scene.shape.Rectangle> obstacles;
    int width;
    int height;
    int maxSize = 100;

    public static int cellsOffset = 10;
    int startPositionCount = 0;
    int terminalCount = 0;
    Canvas canvas;

    Space2D(int width, int height) {
        this.width = width;
        this.height = height;
        this.canvas = Main.canvas;
        obstacles = new ArrayList<>();

    }

    public void setWidth(int width) {
        if(width<maxSize && width>1)
            this.width = width;
    }

    public void setHeight(int height) {
        if(height<maxSize && height>1)
            this.height = height;
    }



    void draw() {
        draw(canvas);
    }

    void draw(Canvas canvas) {
        //background
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);  // fill with white background
        g.fillRect(0+cellsOffset, 0+cellsOffset,width, height);
        g.strokeRect(0+cellsOffset, 0+cellsOffset, width, height);
        // draw obst
        g.setFill(Color.GRAY);  // fill with white background

        for (javafx.scene.shape.Rectangle r:obstacles) {
            g.fillRect(r.getX()* PublicPartOfAgent.scale+cellsOffset,r.getY()+cellsOffset,r.getWidth(),r.getHeight());
        }
    }











}
