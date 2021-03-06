/**
 * Copyright (C) 2011 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.neophob.sematrix.output.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PApplet;
import processing.core.PImage;

import com.neophob.sematrix.generator.Generator;
import com.neophob.sematrix.glue.Collector;

/**
 * Helper class to create a new window
 *
 * @author michu
 */
public class GeneratorGuiCreator extends Frame {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2946906663946781980L;

	/** The log. */
	private static final Logger LOG = Logger.getLogger(GeneratorGuiCreator.class.getName());

	/**
	 * Instantiates a new internal debug window.
	 *
	 * @param displayHoriz the display horiz
	 * @param the maximal x size of the window
	 */
	public GeneratorGuiCreator(boolean displayHoriz, int maximalXSize) {
        super("PixelController Generator Window");        
        int nrOfScreens = Collector.getInstance().getAllVisuals().size();
        Generator g = Collector.getInstance().getPixelControllerGenerator().getGenerator(0);
        
        float aspect = (float)g.getInternalBufferXSize()/(float)g.getInternalBufferYSize();
        int singleVisualXSize,singleVisualYSize;   
        singleVisualXSize=maximalXSize/nrOfScreens;
        singleVisualYSize=(int)(maximalXSize/nrOfScreens/aspect);

        int windowXSize=singleVisualXSize;
        int windowYSize=singleVisualYSize;

        if (displayHoriz) {
        	windowXSize*=nrOfScreens;
        } else {
        	windowYSize*=nrOfScreens;
        }
       
        //boarder stuff
        windowXSize+=20;
        windowYSize+=80+200;

        LOG.log(Level.INFO, "create frame with size "+windowXSize+"/"+windowYSize+", aspect: "+aspect);
        setBounds(0, 0, windowXSize, windowYSize);
        this.setResizable(false);
        this.setSize(windowXSize, windowYSize);

        //connect the new PApplet to our frame
        setLayout(new BorderLayout());
        PApplet embed = new GeneratorGui(displayHoriz, windowXSize, windowYSize, singleVisualXSize, singleVisualYSize);        
        add(embed, BorderLayout.CENTER);

        // important to call this whenever embedding a PApplet.
        // It ensures that the animation thread is started and
        // that other internal variables are properly set.
        embed.init();        
        setVisible(true);
        this.setIconImage(GeneratorGuiCreator.createIcon());
	}
	
	/**
	 * help function to load the pixelinvaders logo
	 * @return
	 */
	public static Image createIcon() {
	    PImage img = Collector.getInstance().getPapplet().loadImage("pics/logo.gif");
	    if (img!=null) {
	        return img.getImage();	        
	    }
	    img = new PImage(8,8);
	    return img.getImage();
	}

}
