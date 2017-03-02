package projektvas;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * A waypoint that also has a color and a label
 *
 * @author Martin Steiger
 */
public class MyWaypoint extends DefaultWaypoint {

    private final String label;
    private final String about;
    private final Color color;

    /**
     * @param label the text
     * @param color the color
     * @param coord the coordinate
     */
    public MyWaypoint(String label, String about, Color color, GeoPosition coord) {
        super(coord);
        this.label = label;
        this.color = color;
        this.about = about;
    }

    /**
     * @return the label text
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    public String getAbout() {
        return about;
    }
    
}
