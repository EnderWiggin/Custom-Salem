package org.ender.wiki;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class HtmlDraw {

    private JTextPane htmlpane;

    public HtmlDraw(String text){
	// create jeditorpane
	htmlpane = new JTextPane();

	// make it read-only
	htmlpane.setEditable(false);

	// add an html editor kit
	HTMLEditorKit kit = new HTMLEditorKit();
	StyleSheet ss = kit.getStyleSheet();
	try {
	    ss.setBase(new URL("http://salemwiki.info"));
	} catch (MalformedURLException e1) {
	    e1.printStackTrace();
	}
	text = text.replaceAll("=\"/", "=\"http://salemwiki.info/");
	ss.setBaseFontSize("-1");
	//StyleConstants.setBackground(ss, new Color(0xff888833));
	//ss.addRule("body {background-color: rgba(255,228,228, 0.75)}");
	kit.setStyleSheet(ss);
	htmlpane.setEditorKit(kit);
	htmlpane.setBackground(new Color(200,200,200,255));
	htmlpane.setOpaque(false);
	htmlpane.addPropertyChangeListener(new PropertyChangeListener() {

	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt);
		img = null;
	    }
	});

	// create some simple html as a string
	//String htmlString = "<table style=\"float:left; width:320px; padding:0.1em; border:black 2px solid; border-radius:0.4em; background:#71583b; font-family:Calibri; margin-right:25px\">\r\n<tr>\r\n<th style=\"color:white; font-size:1.2em\"><a href=\"http://salemwiki.info/index.php/File:Spinning_Top_icon.png\" class=\"image\"><img alt=\"Spinning Top icon.png\" src=\"http://salemwiki.info/images/6/68/Spinning_Top_icon.png\" width=\"40\" height=\"40\" /></a> <strong class=\"selflink\">Spinning Top</strong>\r\n</th></tr>\r\n<tr>\r\n<td>\r\n<table style=\"width:100%; border:black 2px solid; background:#efebe5\">\r\n<tr>\r\n<td style=\"width:100px\"><b>Skill(s) required:</b>\r\n</td>\r\n<td colspan=\"2\"> <ul>\r\n\t<li><a href=\"http://salemwiki.info/index.php/File:Whittling_icon.png\" class=\"image\"><img alt=\"Whittling icon.png\" src=\"http://salemwiki.info/images/9/9b/Whittling_icon.png\" width=\"30\" height=\"30\" /></a> <a href=\"http://salemwiki.info/index.php/Whittling\" title=\"Whittling\">Whittling</a></li>\r\n</ul>\r\n</td></tr>\r\n<tr>\r\n<td style=\"vertical-align:top\"> <b>Object(s) required:</b> </td>\r\n<td colspan=\"2\">\r\n<ul><li><a href=\"http://salemwiki.info/index.php/File:Sharpened_Stick_icon.png\" class=\"image\"><img alt=\"Sharpened Stick icon.png\" src=\"http://salemwiki.info/images/0/01/Sharpened_Stick_icon.png\" width=\"40\" height=\"40\" /></a> <a href=\"http://salemwiki.info/index.php/Sharpened_Stick\" title=\"Sharpened Stick\">Sharpened Stick</a> x1\r\n</li><li><a href=\"http://salemwiki.info/index.php/File:Woodblock_icon.png\" class=\"image\"><img alt=\"Woodblock icon.png\" src=\"http://salemwiki.info/images/d/d9/Woodblock_icon.png\" width=\"40\" height=\"40\" /></a> <a href=\"http://salemwiki.info/index.php/Woodblock\" title=\"Woodblock\">Woodblock</a> x1\r\n</li><li><a href=\"http://salemwiki.info/index.php/File:Whittler%27s_Bench_icon.png\" class=\"image\"><img alt=\"Whittler&#39;s Bench icon.png\" src=\"http://salemwiki.info/images/9/9c/Whittler%27s_Bench_icon.png\" width=\"40\" height=\"40\" /></a> <a href=\"http://salemwiki.info/index.php/Whittler%27s_Bench\" title=\"Whittler's Bench\">Whittler's Bench</a> x1\r\n</li></ul>\r\n</td></tr>\r\n<tr>\r\n<td style=\"vertical-align:top\"> <b>Required by:</b>\r\n</td>\r\n<td style=\"vertical-align:top\"> <b>Objects</b>    None\r\n</td>\r\n<td style=\"vertical-align:top\"> <b>Structures</b>    None\r\n</td></tr></table>\r\n</td></tr>\r\n<tr>\r\n<th style=\"color:white; font-size:1.2em\"> <b>Proficiencies:</b>\r\n</th></tr>\r\n<tr>\r\n<td>\r\n<table style=\"width:100%; border:black 2px solid; background:#efebe5; padding=5px; vertical-align:bottom; text-align:center;\">\r\n<tr>\r\n<td style=\"vertical-align:top; width:100px; text-align:left\"><b>Proficiencie(s) gained:</b>\r\n</td>\r\n<td>\r\n<table style=\"text-align:left\">\r\n<tr>\r\n<td>650 <a href=\"http://salemwiki.info/index.php/File:Arts_%26_Crafts_icon.png\" class=\"image\"><img alt=\"Arts &amp; Crafts icon.png\" src=\"http://salemwiki.info/images/4/42/Arts_%26_Crafts_icon.png\" width=\"16\" height=\"16\" /></a> <a href=\"http://salemwiki.info/index.php/Arts_%26_Crafts\" title=\"Arts &amp; Crafts\">Arts &amp; Crafts</a>\r\n</td></tr>\r\n\r\n\r\n\r\n\r\n\r\n\r\n<tr>\r\n<td>850 <a href=\"http://salemwiki.info/index.php/File:Hammer_%26_Nail_icon.png\" class=\"image\"><img alt=\"Hammer &amp; Nail icon.png\" src=\"http://salemwiki.info/images/4/4e/Hammer_%26_Nail_icon.png\" width=\"16\" height=\"16\" /></a> <a href=\"http://salemwiki.info/index.php/Hammer_%26_Nail\" title=\"Hammer &amp; Nail\">Hammer &amp; Nail</a>\r\n</td></tr>\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n\r\n<tr>\r\n<td>900 <a href=\"http://salemwiki.info/index.php/File:Thread_%26_Needle_icon.png\" class=\"image\"><img alt=\"Thread &amp; Needle icon.png\" src=\"http://salemwiki.info/images/d/d5/Thread_%26_Needle_icon.png\" width=\"16\" height=\"16\" /></a> <a href=\"http://salemwiki.info/index.php/Thread_%26_Needle\" title=\"Thread &amp; Needle\">Thread &amp; Needle</a>\r\n</td></tr>\r\n<tr>\r\n<td>250 <a href=\"http://salemwiki.info/index.php/File:Natural_Philosophy_icon.png\" class=\"image\"><img alt=\"Natural Philosophy icon.png\" src=\"http://salemwiki.info/images/9/9b/Natural_Philosophy_icon.png\" width=\"16\" height=\"16\" /></a> <a href=\"http://salemwiki.info/index.php/Natural_Philosophy\" title=\"Natural Philosophy\">Natural Philosophy</a>\r\n</td></tr>\r\n\r\n</table>\r\n</td></tr></table>\r\n</td></tr></table>\r\n<h2><span class=\"editsection\">[<a href=\"http://salemwiki.info/index.php?title=Spinning_Top&amp;action=edit&amp;section=1\" title=\"Edit section: About\">edit</a>]</span> <span class=\"mw-headline\" id=\"About\">About</span></h2>\r\n<p>Round and round it goes, bouncing and spinning, dizzy from head to toe, and bubbling with childish giggle.\r\n</p>\r\n<!-- \r\nNewPP limit report\r\nPreprocessor node count: 401/1000000\r\nPost-expand include size: 4125/2097152 bytes\r\nTemplate argument size: 2065/2097152 bytes\r\nExpensive parser function count: 9/100\r\n-->\r\n";

	// create a document, set it on the jeditorpane, then add the
	// html
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

    public BufferedImage getimg(){
	if(img == null){
	    img = createImage(htmlpane);
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
	// Make sure the component has a size and has been layed out.
	// (necessary check for components not added to a realized frame)
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
	if(img == null){
	    System.out.println("drawing...");
	    img = createImage(htmlpane, new Rectangle(w, h), image);
	    try {
		writeImage(img, "img.png");
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	//return img;
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
