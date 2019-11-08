/*
 * Clock displays a resizeable digital clock with millisecond resolution
 * Copyright (C) 2018 - 2019  John Murphy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package info.johnmurphyastro.clock;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * Displays the seconds component of the system time to millisecond accuracy
 * @author John Murphy
 */
public class Clock extends JComponent {
    private static final int CANVAS_MIN_HEIGHT = 110;
    private static final int CANVAS_MIN_WIDTH = 800;
    private final Dimension canvasSize = new Dimension(CANVAS_MIN_WIDTH, CANVAS_MIN_HEIGHT);
    
    private Font font = new Font("Monospaced", Font.PLAIN, 12);
    private static final double FONT_ASCENT_CORRECTION = 0.8;
    private int fontX = 15;
    private int fontY = 5;
    private int MARGIN = 2;
    private Rectangle clipRect = new Rectangle(MARGIN, MARGIN, 800, 110);
    
    private static final String EXAMPLE_TIME = "24:58:58.888";
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS"); 
    
    /* Log file */
    private int count = 0;
    private final int maxLogLines;
    private boolean writeLogFlag;
    private BufferedWriter logFile;
    
    /**
     * Draw the system time seconds and milliseconds to the Clock Canvas.
     * Format 00.000
     * @param maxLogLines Number of time updates to log to "ClockLog.txt"
     */
    public Clock(int maxLogLines) {
        this.setOpaque(true);
        this.setDoubleBuffered(false);
        this.maxLogLines = maxLogLines;
        logFile = null;
        if (maxLogLines > 0) {
            try {
                final File fout = new File("ClockLog.txt");
                FileOutputStream fos = new FileOutputStream(fout);
                logFile = new BufferedWriter(new OutputStreamWriter(fos));
            } catch (FileNotFoundException ex) {                
                Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        writeLogFlag = (logFile != null);
        
        addResizeListener();
        this.setBackground(Color.black);
    }
    
    /**
     * Update the current time
     * This uses the base class to repaint the background. The base class will
     * then call paintComponent, which will get the system time and draw it to
     * the Clock canvas.
     */
    public void updateTime(){
        repaint(clipRect);
    }
    
    @Override
    public void paintComponent(Graphics graphics){
        Graphics2D g = (Graphics2D) graphics;
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setColor(Color.white);
        g.setFont(font);
        String time = sdf.format(new Date());
        g.drawString(time, fontX, fontY);
        if (writeLogFlag)
            writeLogFile(time);
    }

    private void writeLogFile(String time) {
        try {
            if (count < maxLogLines) {
                count++;
                logFile.write(time);
                logFile.newLine();
            } else if (count == maxLogLines) {
                writeLogFlag = false;
                logFile.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Clock.class.getName()).log(Level.SEVERE, null, ex);
            writeLogFlag = false;
        }
    }
    
    @Override
    public Dimension getPreferredSize(){
        return new Dimension(CANVAS_MIN_WIDTH, CANVAS_MIN_HEIGHT);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(CANVAS_MIN_WIDTH, CANVAS_MIN_HEIGHT);
    }  

    private void addResizeListener() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent){
                setTimeStringParams();
                repaint();
            }
        });
    }
    
    private void setTimeStringParams() {
        for (int fontSize=12; fontSize<500; fontSize++){
            Font f = new Font("Monospaced", Font.PLAIN, fontSize);
            if (fontTooLarge(f)){
                font = new Font("Monospaced", Font.PLAIN, fontSize - 1);
                break;
            }
        }
        
        Graphics2D g = (Graphics2D)getGraphics();
        FontMetrics fontMetrics = g.getFontMetrics(font);
        fontX = MARGIN;
        fontY = (int)(fontMetrics.getAscent() * FONT_ASCENT_CORRECTION);
        int width = fontMetrics.stringWidth(EXAMPLE_TIME);
        clipRect.setBounds(fontX, 0, width, fontY + 10);
    }

    private boolean fontTooLarge(Font f) {
        getSize(canvasSize);
        
        Graphics2D g = (Graphics2D)getGraphics();
        FontMetrics fontMetrics = g.getFontMetrics(f);
        int width = fontMetrics.stringWidth(EXAMPLE_TIME);
        int ascent = fontMetrics.getAscent();
        return (ascent > canvasSize.height || width > canvasSize.width - MARGIN * 2);
    }
}
