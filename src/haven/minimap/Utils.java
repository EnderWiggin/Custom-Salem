package haven.minimap;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import haven.*;

public class Utils {
    private static final int markersz = 8;

    public static BufferedImage generateMarkerImage() {

        BufferedImage scaled = new BufferedImage(markersz + 1, markersz + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(Color.WHITE);
        g.fillOval(0, 0, scaled.getWidth() - 1, scaled.getHeight() - 1);
        g.setColor(Color.BLACK);
        g.drawOval(0, 0, scaled.getWidth() - 1, scaled.getHeight() - 1);
        return scaled;
    }
    
    public static Tex generateMarkerTex() {
        return new TexI(generateMarkerImage());
    }
    
    public static Color parseColor(String value) {
        try {
            return Color.decode(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
