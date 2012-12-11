package org.ender.wiki;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class HtmlDraw {

    private JTextPane htmlpane;

    public HtmlDraw(String text){
	if(text == null){text = "";}
	text = text.replaceAll("=\"/", "=\"http://salemwiki.info/");
	
	htmlpane = new JTextPane();
	htmlpane.setEditable(false);
	htmlpane.setBackground(new Color(200,200,200,255));
	htmlpane.setOpaque(false);

	// add an html editor kit
	HTMLEditorKit kit = new HTMLEditorKit();
	htmlpane.setEditorKit(kit);

	HTMLDocument doc = (HTMLDocument) kit.createDefaultDocument();
	htmlpane.setDocument(doc);
	htmlpane.setText(text);
	htmlpane.setSize(htmlpane.getPreferredSize());
	layoutComponent(htmlpane);
	htmlpane.addHyperlinkListener(new HyperlinkListener() {

	    @Override
	    public void hyperlinkUpdate(HyperlinkEvent e) {
		System.out.println(e.getURL());
		System.out.println(e.getInputEvent());
	    }
	});


	doClickInRectangle(htmlpane, 172, 84, false, 0);
	doClickInRectangle(htmlpane, 815, 1110, false, 0);
    }

    public void setWidth(int w){
	if(htmlpane == null){return;}
	Dimension d = htmlpane.getSize();
	d.width = w;
	htmlpane.setSize(d);
	//layoutComponent(htmlpane);
	d = htmlpane.getPreferredScrollableViewportSize();
	//d.width = w;
	htmlpane.setSize(w, (int) d.getHeight());
    }

    public int getHeight(){
	return htmlpane.getHeight();
    }

    public BufferedImage getimg(int w, int h) {
	if(img == null){
	    System.out.println("drawing...");
	    img = createImage(htmlpane, new Rectangle(w, h));
	    try {
		writeImage(img, "img.png");
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return img;
    }

    public static void doClickInRectangle(Component component,
	    int x, int y,
	    boolean useRightClick,
	    int keyMods) {
	int modifiers = useRightClick ? MouseEvent.BUTTON3_MASK : MouseEvent.BUTTON1_MASK;
	modifiers |= keyMods;
	final int nbClicks = 1;
	component.dispatchEvent(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, 1, modifiers, x, y, nbClicks, useRightClick));
    }

    public static BufferedImage createImage(JComponent component) {
	Dimension d = component.getSize();

	if (d.width == 0 || d.height == 0) {
	    d = component.getPreferredSize();
	    component.setSize(d);
	}

	Rectangle region = new Rectangle(0, 0, d.width, d.height);
	return createImage(component, region);
    }

    public static BufferedImage createImage(JComponent component, Rectangle region) {
	BufferedImage image = new BufferedImage(region.width, region.height, BufferedImage.TYPE_INT_ARGB);
	return createImage(component, region, image);
    }
    
    public static BufferedImage createImage(JComponent component, Rectangle region, BufferedImage image) {
	// Make sure the component has a size and has been layed out.
	// (necessary check for components not added to a realized frame)

	if (!component.isDisplayable()) {
	    Dimension d = component.getSize();

	    if (d.width == 0 || d.height == 0) {
		d = component.getPreferredSize();
		component.setSize(d);
	    }

	    layoutComponent(component);
	}

	Graphics2D g2d = image.createGraphics();

	// Paint a background for non-opaque components,
	// otherwise the background will be black

	if (!component.isOpaque()) {
	    g2d.setColor(component.getBackground());
	    g2d.fillRect(region.x, region.y, region.width, region.height);
	}

	g2d.translate(-region.x, -region.y);
	component.paint(g2d);
	g2d.dispose();
	return image;
    }

    public void get(BufferedImage image, int w, int h) {
	    img = createImage(htmlpane, new Rectangle(w, h), image);
    }

    static void layoutComponent(Component component) {
	synchronized (component.getTreeLock()) {
	    component.doLayout();

	    if (component instanceof Container) {
		for (Component child : ((Container) component).getComponents()) {
		    layoutComponent(child);
		}
	    }
	}
    }

    /**
     * Write a BufferedImage to a File.
     * 
     * @param image
     *            image to be written
     * @param fileName
     *            name of file to be created
     * @exception IOException
     *                if an error occurs during writing
     */
    private static List<String> types = Arrays.asList(ImageIO.getWriterFileSuffixes());
    private BufferedImage img;
    public static void writeImage(BufferedImage image, String fileName) throws IOException {
	if (fileName == null) return;

	int offset = fileName.lastIndexOf(".");

	if (offset == -1) {
	    String message = "file suffix was not specified";
	    throw new IOException(message);
	}

	String type = fileName.substring(offset + 1);

	if (types.contains(type)) {
	    ImageIO.write(image, type, new File(fileName));
	} else {
	    String message = "unknown writer file suffix (" + type + ")";
	    throw new IOException(message);
	}
    }
}
