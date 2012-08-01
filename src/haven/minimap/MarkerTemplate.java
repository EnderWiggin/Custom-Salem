package haven.minimap;

import haven.minimap.Marker.Shape;

import java.awt.*;

public class MarkerTemplate {
    
    public final Color color;
    public final Shape shape;
    public final boolean visible;
    public final String tooltip;
    public final boolean showtooltip;

    public MarkerTemplate(Color color, boolean visible, String tooltip, boolean showtooltip, Shape shape) {
        this.color = color;
        this.visible = visible;
        this.tooltip = tooltip;
        this.showtooltip = showtooltip;
        this.shape = shape;
    }
}
