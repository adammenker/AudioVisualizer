/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioviz;

import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author adammenker
 */
public class Amdy6SuperVisual2 implements Visualizer{
    private static final String NAME = "Bar Visualizer";
    
    private Integer numberOfBands;
    private AnchorPane vizPane;
    private Double width = 0.0;
    private Double height = 0.0;
    private Double bandWidth = 0.0;
    private Double bandHeight = 0.0;
    private final Double colorTheme;
    
    private Rectangle[] rectangles;
    private Circle[] circles;
    
    public Amdy6SuperVisual2(Double colorTheme) {
        this.colorTheme = colorTheme;
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public void start(Integer numBands, AnchorPane vizPane) {
        end();
        
        // cuts off inactive bands
        this.numberOfBands = (numBands / 4) * 3;
        this.vizPane = vizPane;
        
        // gets height & width of vizPane
        height = vizPane.getHeight();
        width = vizPane.getWidth();
        
        // moves vizPane more towards center of the window
        vizPane.setLayoutY(height / 1.5);

        bandWidth = width / numberOfBands;
        bandHeight = height * 0.015;
        rectangles = new Rectangle[numberOfBands];
        circles = new Circle[numberOfBands];
        
        for (int i = 0; i < numberOfBands; i++) {
            Rectangle rectangle = new Rectangle();
            Circle circle = new Circle();
            
            rectangle.setX(bandWidth * i);
            rectangle.setWidth(bandWidth);
            
            circle.setCenterX(bandWidth * i + bandWidth / 2);
            circle.setCenterY(rectangle.getY() + 100);
            circle.setRadius(bandWidth/2 - bandWidth/5);
            
            vizPane.getChildren().add(rectangle);
            vizPane.getChildren().add(circle);
            rectangles[i] = rectangle;
            circles[i] = circle;
        }
        setOpacitiesToZero(rectangles, circles);
    }
    
    // I implemented this the same way as the example, because it is the intuitive way
    // trying to implement this function another way would be changing things for the 
    // sake of changing things, so I saw no reason to not implement it the same way
    @Override
    public void end() {
        if (rectangles != null) {
            for (int i = 0; i < numberOfBands; i++) {
                vizPane.getChildren().remove(rectangles[i]);
            }
            rectangles = null;
        }
        if (circles != null) {
            for (int i = 0; i < numberOfBands; i++) {
                vizPane.getChildren().remove(circles[i]);
            }
            circles = null;
        }
    }
    
    @Override
    public void draw(double timestamp, double length, float[] magnitudes, float[] phases) {
        if (rectangles == null || circles == null) {
            return;
        }
        
        for (int i = 0; i < numberOfBands; i++) {
            double verticalLocation = (-rectangles[i].getHeight() / 2);
            rectangles[i].setY(verticalLocation);
        }

        for (int i = 0; i < numberOfBands; i++) {
            rectangles[i].setHeight((magnitudes[i] + 70) * (bandHeight / 2));
            rectangles[i].setFill(Color.hsb((colorTheme + magnitudes[i]) * 1.1, 1.0, 1.0, 1.0));
            
            double circleOpacity = ((((magnitudes[i] + 60) / 60) % 1) * 3);
            if(circleOpacity > 1){
                circleOpacity = 1;
            }else if(circleOpacity < 0){
                circleOpacity = 0;
            }
            
            circles[i].setFill(Color.hsb(colorTheme, 1.0, 1.0, circleOpacity));
        }
    }

    public void setOpacitiesToZero(Rectangle[] rectangles, Circle[] circles){
        for (int i = 0; i < numberOfBands; i++) {
            circles[i].setFill(Color.hsb(colorTheme, 1.0, 1.0, 0));
            rectangles[i].setFill(Color.hsb(colorTheme, 1.0, 1.0, 0));
        }
    }
}
