import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

// 
// Decompiled by Procyon v0.5.30
// 

@SuppressWarnings("serial")
public class VideoConsole extends JPanel implements TableModelListener
{
	private BufferedImage image;
    private static final int NROWS = 124;
    private static final int NCOLS = 128;
    private static final int SCALING = 2;
    private static final int WIDTH = 256;
    private static final int HEIGHT = 248;
    public static final int START = 49152;
    public static final int END = 65024;
    private Machine mac;
    
    public VideoConsole(final Machine mac) {
        final Dimension maximumSize = new Dimension(256, 248);
        this.setPreferredSize(maximumSize);
        this.setMinimumSize(maximumSize);
        this.setMaximumSize(maximumSize);
        this.mac = mac;
        this.image = new BufferedImage(256, 248, 9);
        final Graphics2D graphics = this.image.createGraphics();
        this.image.setAccelerationPriority(1.0f);
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, 256, 248);
    }
    
    public void reset() {
        final Graphics2D graphics = this.image.createGraphics();
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, 256, 248);
        this.repaint();
    }
    
    public void bltMemBuffer() {
        if (!PennSim.isDoubleBufferedVideo()) {
            return;
        }
        for (int i = 0; i < 124; ++i) {
            for (int j = 0; j < 128; ++j) {
                final int convertToRGB = convertToRGB(this.mac.getMemory().read(49152 + i * 128 + j));
                for (int k = 0; k < 2; ++k) {
                    for (int l = 0; l < 2; ++l) {
                        this.image.setRGB(j * 2 + l, i * 2 + k, convertToRGB);
                    }
                }
            }
        }
        this.repaint();
    }
    
    @Override
    public void tableChanged(final TableModelEvent tableModelEvent) {
        if (PennSim.isDoubleBufferedVideo()) {
            return;
        }
        final int firstRow = tableModelEvent.getFirstRow();
        final int lastRow = tableModelEvent.getLastRow();
        if (firstRow == 0 && lastRow == 65535) {
            this.reset();
        }
        else {
            if (firstRow < 49152 || firstRow >= 65024) {
                return;
            }
            final int n = 2;
            final int n2 = firstRow - 49152;
            final int n3 = n2 / 128 * n;
            final int n4 = n2 % 128 * n;
            final int convertToRGB = convertToRGB(this.mac.getMemory().read(firstRow));
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j) {
                    this.image.setRGB(n4 + j, n3 + i, convertToRGB);
                }
            }
            this.repaint(n4, n3, n, n);
        }
    }
    
    public void paintComponent(final Graphics graphics) {
        super.paintComponent(graphics);
        final Graphics2D graphics2D = (Graphics2D)graphics;
        if (this.image == null) {
            final int width = this.getWidth();
            final int height = this.getHeight();
            this.image = (BufferedImage)this.createImage(width, height);
            final Graphics2D graphics2 = this.image.createGraphics();
            graphics2.setColor(Color.white);
            graphics2.fillRect(0, 0, width, height);
        }
        graphics2D.drawImage(this.image, null, 0, 0);
    }
    
    private static int convertToRGB(final Word word) {
        return new Color(word.getZext(14, 10) * 8, word.getZext(9, 5) * 8, word.getZext(4, 0) * 8).getRGB();
    }
}
