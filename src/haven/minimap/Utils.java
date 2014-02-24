package haven.minimap;

import java.awt.*;
import java.awt.image.BufferedImage;

import haven.*;
import haven.minimap.Marker.Shape;

public class Utils {
    private static final int tri_x[] = {0, Shape.TRIANGLE.sz/2, Shape.TRIANGLE.sz-1};
    private static final int tri_y[] = {Shape.TRIANGLE.sz-1,0,Shape.TRIANGLE.sz-1};
    private static final int tri2_y[] = {0,Shape.TRIANGLE.sz-1,0};
    
    private static final int dia_x[] = {0, Shape.DIAMOND.sz/2, Shape.DIAMOND.sz-1, Shape.DIAMOND.sz/2};
    private static final int dia_y[] = {Shape.DIAMOND.sz/2, 0, Shape.DIAMOND.sz/2, Shape.DIAMOND.sz-1};
    
    private static final int pen_x[] = {Shape.PENTAGON.sz/2, Shape.PENTAGON.sz-1, (int) ((Shape.PENTAGON.sz+1)*0.75), (int) ((Shape.PENTAGON.sz-1)*0.25), 0};
    private static final int pen_y[] = {0, Shape.PENTAGON.sz/2, Shape.PENTAGON.sz-1, Shape.PENTAGON.sz-1, Shape.PENTAGON.sz/2};
    
    public static BufferedImage generateMarkerImage(Shape shape) {

        BufferedImage scaled = new BufferedImage(shape.sz, shape.sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        switch(shape){
	    case CIRCLE:
		g.setColor(Color.WHITE);
		g.fillOval(0, 0, shape.sz - 1, shape.sz - 1);
		g.setColor(Color.BLACK);
		g.drawOval(0, 0, shape.sz - 1, shape.sz - 1);
		break;
	    case TRIANGLE:
		g.setColor(Color.WHITE);
		g.fillPolygon(tri_x, tri_y, 3);
		g.setColor(Color.BLACK);
		g.drawPolygon(tri_x, tri_y, 3);
		break;
	    case TRIANGLED:
		g.setColor(Color.WHITE);
		g.fillPolygon(tri_x, tri2_y, 3);
		g.setColor(Color.BLACK);
		g.drawPolygon(tri_x, tri2_y, 3);
		break;
	    case DIAMOND:
		g.setColor(Color.WHITE);
		g.fillPolygon(dia_x, dia_y, 4);
		g.setColor(Color.BLACK);
		g.drawPolygon(dia_x, dia_y, 4);
		break;
	    case SQUARE:
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, shape.sz - 1, shape.sz - 1);
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, shape.sz - 1, shape.sz - 1);
		break;
	    case PENTAGON:
		g.setColor(Color.WHITE);
		g.fillPolygon(pen_x, pen_y, 5);
		g.setColor(Color.BLACK);
		g.drawPolygon(pen_x, pen_y, 5);
		break;
	}
        return scaled;
    }
    
    public static Tex generateMarkerTex(Shape shape) {
        return new TexI(generateMarkerImage(shape));
    }
    
    public static Color parseColor(String value) {
        try {
            return Color.decode(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
