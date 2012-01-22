package haven.minimap;

import java.awt.*;

public class MarkerTemplate {
    public final Color color;
    public final boolean visible;
    public final String tooltip;
    public final boolean showtooltip;

    public MarkerTemplate(Color color, boolean visible, String tooltip, boolean showtooltip) {
        this.color = color;
        this.visible = visible;
        this.tooltip = tooltip;
        this.showtooltip = showtooltip;
    }
}
