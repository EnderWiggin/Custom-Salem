package haven.minimap;

import java.awt.*;

public class ConfigMarker {
    public Color color;
    public String match;
    public boolean ispattern;
    // indicates whether this marker should be displayed on the map
    public boolean show;
    public String text;
    public boolean tooltip;

    public boolean hastext() {
        return text != null && text.length() != 0;
    }
}
