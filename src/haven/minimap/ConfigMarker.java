package haven.minimap;

import haven.minimap.Marker.Shape;

import java.awt.*;

public class ConfigMarker {
    public Color color;
    public String match;
    public boolean ispattern;
    // indicates whether this marker should be displayed on the map
    public boolean show;
    public String text;
    public boolean tooltip;
    public Shape shape;

    public boolean hastext() {
        return text != null && text.length() != 0;
    }
}
