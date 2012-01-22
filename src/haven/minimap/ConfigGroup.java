package haven.minimap;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigGroup {
    public Color color;
    public String name;
    public boolean show;
    public List<ConfigMarker> markers = new ArrayList<ConfigMarker>();
}
