import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

// 
// Decompiled by Procyon v0.5.30
// 

public class DataPath extends JPanel
{
    private BufferedImage image;
    private BufferedImage editedImage;
    private Machine mac;
    private ControlSignals signals = null;
    
    public Point PCMuxCTLLocation = new Point(145, 487);
    public Point rsMuxCTLLocation = new Point(373, 129);
    public Point rtMuxCTLLocation = new Point(373, 199);
    public Point rdMuxCTLLocation = new Point(373, 266);
    public Point regFileWELocation = new Point(458, 99);
    public Point regInputMuxCTLLocation = new Point(1067, 157);
    public Point ArithCTLLocation = new Point(707, 62);
    public Point ArithMuxCTLLocation = new Point(675, 97);
    public Point LOGICCTLLocation = new Point(707, 161);
    public Point LogicMuxCTLLocation = new Point(675, 196);
    public Point SHIFTCTLLocation = new Point(707, 259);
    public Point CONSTCTLLocation = new Point(707, 343);
    public Point CMPCTLLocation = new Point(707, 436);
    public Point ALUMuxCTLLocation = new Point(820, 111);
    public Point NZPWELocation = new Point(326, 437);
    public Point DATAWELocation = new Point(943, 241);
    
    public DataPath(final Machine mac, final JFrame frame) {
        this.mac = mac;
//        this.image = new BufferedImage(256, 248, BufferedImage.TYPE_INT_ARGB);
//        final Graphics2D graphics = this.image.createGraphics();
//        graphics.setColor(Color.black);
//        graphics.fillRect(0, 0, 256, 248);
        try {
        	URL url = PennSim.class.getResource("/resources/LC4_DataPath.jpg");
        	this.image = ImageIO.read(url);
         	this.editedImage = ImageIO.read(url);
        	//this.image = ImageIO.read(new File("LC4_DataPath.jpg"));
        	//this.editedImage = ImageIO.read(new File("LC4_DataPath.jpg"));
        } catch (IOException ex) {
        	System.out.println(ex);
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Graphics2D graphics = this.image.createGraphics();
        graphics.drawImage(image, 0, 0, width /2, height/2, this);
        final Dimension maximumSize = new Dimension(width/2, height/2);
        this.setPreferredSize(maximumSize);
        this.setMaximumSize(maximumSize);
        frame.setMaximumSize(maximumSize);
    }
    
    public void UpdateSignals(ControlSignals signals) {
    	this.signals = signals;
    	this.repaint();
    }
    
    public void reset() {
        final Graphics2D graphics = this.image.createGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, 256, 248);
        this.repaint();
    }
    
    
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        final int width = this.getWidth();
        final int height = this.getHeight();
        this.editedImage = (BufferedImage)this.createImage(width, height);
        final Graphics2D graphics2 = this.editedImage.createGraphics();
        graphics.drawImage(this.image, 0, 0, null);
    	if(this.signals != null) {
    		if(signals.PCMuxCTL != 15) {
    			graphics.setFont(new Font("Osaka", Font.BOLD, 15));
				graphics.setColor(Color.RED);
				graphics.drawString(": " + signals.PCMuxCTL, PCMuxCTLLocation.x, PCMuxCTLLocation.y);
    		}
    		if(signals.rsMuxCTL != 15) {
				graphics.setColor(Color.BLACK);
				graphics.drawString(": " + signals.rsMuxCTL, rsMuxCTLLocation.x, rsMuxCTLLocation.y);
    		}
    		if(signals.rtMuxCTL != 15) {
				graphics.setColor(Color.BLUE);
				graphics.drawString(": " + signals.rtMuxCTL, rtMuxCTLLocation.x, rtMuxCTLLocation.y);
    		}
    		if(signals.rdMuxCTL != 15) {
				graphics.setColor(Color.CYAN);
				graphics.drawString(": " + signals.rdMuxCTL, rdMuxCTLLocation.x, rdMuxCTLLocation.y);
    		}
    		if(signals.regFileWE != 15) {
				graphics.setColor(Color.DARK_GRAY);
				graphics.drawString(": " + signals.regFileWE, regFileWELocation.x, regFileWELocation.y);
    		}
    		if(signals.regInputMuxCTL != 15) {
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.drawString(": " + signals.regInputMuxCTL, regInputMuxCTLLocation.x, regInputMuxCTLLocation.y);
    		}
    		if(signals.ArithCTL != 15) {
				graphics.setColor(Color.GREEN);
				graphics.drawString(": " + signals.ArithCTL, ArithCTLLocation.x, ArithCTLLocation.y);
    		}
    		if(signals.ArithMuxCTL != 15) {
				graphics.setColor(Color.MAGENTA);
				graphics.drawString(": " + signals.ArithMuxCTL, ArithMuxCTLLocation.x, ArithMuxCTLLocation.y);
    		}
    		if(signals.LOGICCTL != 15) {
				graphics.setColor(Color.ORANGE);
				graphics.drawString(": " + signals.LOGICCTL, LOGICCTLLocation.x, LOGICCTLLocation.y);
    		}
    		if(signals.LogicMuxCTL != 15) {
				graphics.setColor(Color.PINK);
				graphics.drawString(": " + signals.LogicMuxCTL, LogicMuxCTLLocation.x, LogicMuxCTLLocation.y);
    		}
    		if(signals.SHIFTCTL != 15) {
				graphics.setColor(Color.DARK_GRAY);
				graphics.drawString(": " + signals.SHIFTCTL, SHIFTCTLLocation.x, SHIFTCTLLocation.y);
    		}
    		if(signals.CONSTCTL != 15) {
				graphics.setColor(Color.CYAN);
				graphics.drawString(": " + signals.CONSTCTL, CONSTCTLLocation.x, CONSTCTLLocation.y);
    		}
    		if(signals.CMPCTL != 15) {
				graphics.setColor(Color.GREEN);
				graphics.drawString(": " + signals.CMPCTL, CMPCTLLocation.x, CMPCTLLocation.y);
    		}
    		if(signals.ALUMuxCTL != 15) {
				graphics.setColor(Color.RED);
				graphics.drawString(": " + signals.ALUMuxCTL, ALUMuxCTLLocation.x, ALUMuxCTLLocation.y);
    		}
    		if(signals.NZPWE != 15) {
				graphics.setColor(Color.BLUE);
				graphics.drawString(": " + signals.NZPWE, NZPWELocation.x, NZPWELocation.y);
    		}
    		if(signals.DATAWE != 15) {
				graphics.setColor(Color.MAGENTA);
				graphics.drawString(": " + signals.DATAWE, DATAWELocation.x, DATAWELocation.y);
    		}
    		graphics.setColor(Color.BLACK);
    	}
    }
}
