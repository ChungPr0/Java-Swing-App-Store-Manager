package Main.HomeManager.Charts;

import java.awt.*;

/**
 * Represents a slice in a pie chart.
 */
public class Slice {
    public String name;
    public double value;
    public Color color;
    public Shape shape;

    /**
     * Constructor for a pie chart slice.
     *
     * @param name  The name of the slice.
     * @param value The value of the slice.
     * @param color The color of the slice.
     */
    public Slice(String name, double value, Color color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }
}
