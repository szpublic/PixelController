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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PApplet;
import processing.core.PImage;

import com.neophob.sematrix.effect.Effect.EffectName;
import com.neophob.sematrix.effect.PixelControllerEffect;
import com.neophob.sematrix.generator.Generator.GeneratorName;
import com.neophob.sematrix.glue.Collector;
import com.neophob.sematrix.glue.OutputMapping;
import com.neophob.sematrix.glue.Visual;
import com.neophob.sematrix.input.Sound;
import com.neophob.sematrix.jmx.TimeMeasureItemGlobal;
import com.neophob.sematrix.mixer.Mixer.MixerName;
import com.neophob.sematrix.output.gui.elements.SimpleColorPicker;

import controlP5.Button;
import controlP5.ControlP5;
import controlP5.ControllerGroup;
import controlP5.ControllerInterface;
import controlP5.DropdownList;
import controlP5.RadioButton;
import controlP5.Slider;
import controlP5.Tab;
import controlP5.Toggle;


/**
 * Display the internal Visual buffers in full resolution
 * 
 * @author michu
 */
public class GeneratorGui extends PApplet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2344499301021L;

	private static final int SELECTED_MARKER = 10;

	/** The log. */
	private static final Logger LOG = Logger.getLogger(GeneratorGui.class.getName());

	/** The display horiz. */
	private boolean displayHoriz;

	/** The y. */
	private int x,y;

	/** The p image. */
	private PImage pImage=null;

	private ControlP5 cp5;
	private DropdownList generatorListOne, effectListOne;
	private DropdownList generatorListTwo, effectListTwo;
	private DropdownList mixerList;
	private RadioButton selectedVisualList;
	//private CheckBox selectedOutputs;
	private Button randomSelection, randomPresets;
	private Toggle toggleRandom;
	
	//Effect Tab
	private SimpleColorPicker scp;
	private Slider thresholdSlider;
	
	/** The target y size. */
	private int targetXSize, targetYSize;
	private int p5GuiYOffset;

	/**
	 * Instantiates a new internal buffer.
	 *
	 * @param displayHoriz the display horiz
	 * @param x the x
	 * @param y the y
	 * @param targetXSize the target x size
	 * @param targetYSize the target y size
	 */
	public GeneratorGui(boolean displayHoriz, int x, int y, int targetXSize, int targetYSize) {
		this.displayHoriz = displayHoriz;
		this.x = x;
		this.y = y+SELECTED_MARKER;
		this.targetXSize = targetXSize;
		this.targetYSize = targetYSize;
		this.p5GuiYOffset = targetYSize + 71;		
	}


	@SuppressWarnings("deprecation")
	private void themeDropdownList(DropdownList ddl) {
		// a convenience function to customize a DropdownList
		ddl.setItemHeight(12); //height of a element in the dropdown list
		ddl.setBarHeight(15);  //size of the list
		ddl.actAsPulldownMenu(true); //close menu after a selection was done

		ddl.captionLabel().style().marginTop = 3;
		ddl.captionLabel().style().marginLeft = 3;
		ddl.valueLabel().style().marginTop = 3;
	}

	final String ALWAYS_VISIBLE_TAB = "global";
/**
    cw.tab("default").remove();
    cw.addTab("noise");
    cw.addTab("cliffs");
    cw.activateTab("noise");
 */
	/* (non-Javadoc)
	 * @see processing.core.PApplet#setup()
	 */
	public void setup() {
		LOG.log(Level.INFO, "create internal buffer with size "+x+"/"+y);
		size(x,y+100);
		noSmooth();
		frameRate(Collector.getInstance().getFps());
		background(0,0,0);		
		int i=0;        		

		cp5 = new ControlP5(this);
		cp5.setAutoDraw(false);
		cp5.getTooltip().setDelay(200);
        P5EventListener listener = new P5EventListener(this);

		//selected visual
		int nrOfVisuals = Collector.getInstance().getAllVisuals().size();
		selectedVisualList = cp5.addRadioButton(GuiElement.CURRENT_VISUAL.toString(), 0, p5GuiYOffset-40);
		selectedVisualList.setItemsPerRow(nrOfVisuals);
		selectedVisualList.setNoneSelectedAllowed(false);		
		for (i=0; i<nrOfVisuals; i++) {
			Toggle t = selectedVisualList.addItem("EDIT VISUAL #"+(1+i), i);
			t.setWidth(targetXSize);
			t.setHeight(14);
		}
		selectedVisualList.moveTo(ALWAYS_VISIBLE_TAB);
		

		//select outputs
		/*		int nrOfOutputs = Collector.getInstance().getAllOutputMappings().size();
		selectedOutputs = cp5.addCheckBox(GuiElement.CURRENT_OUTPUT.toString(), 0, p5GuiYOffset+80);
		selectedOutputs.setItemsPerRow(nrOfOutputs);
		selectedOutputs.setSpacingRow(10);
		for (i=0; i<nrOfOutputs; i++) {
			Toggle t = selectedOutputs.addItem("PHYSICAL OUTPUT "+(i+1), i);
			t.setWidth(targetXSize);			
		}
		selectedOutputs.deactivateAll();*/


		final int DROPBOXLIST_LENGTH = 110;
		final int DROPBOX_XOFS = DROPBOXLIST_LENGTH + 23;
		//Generator 
		generatorListOne = cp5.addDropdownList(GuiElement.GENERATOR_ONE_DROPDOWN.toString(), 
				0, p5GuiYOffset, DROPBOXLIST_LENGTH, 140);
		generatorListTwo = cp5.addDropdownList(GuiElement.GENERATOR_TWO_DROPDOWN.toString(), 
				3*DROPBOX_XOFS, p5GuiYOffset, DROPBOXLIST_LENGTH, 140);
		themeDropdownList(generatorListOne);
		themeDropdownList(generatorListTwo);
		i=0;
		for (GeneratorName gn: GeneratorName.values()) {
			generatorListOne.addItem(gn.name(), i);
			generatorListTwo.addItem(gn.name(), i);
			i++;
		}
		generatorListOne.setLabel(generatorListOne.getItem(1).getName());
		generatorListTwo.setLabel(generatorListTwo.getItem(0).getName());
		generatorListOne.moveTo(ALWAYS_VISIBLE_TAB);
		generatorListTwo.moveTo(ALWAYS_VISIBLE_TAB);

		//Effect 
		effectListOne = cp5.addDropdownList(GuiElement.EFFECT_ONE_DROPDOWN.toString(), 
				1*DROPBOX_XOFS, p5GuiYOffset, DROPBOXLIST_LENGTH, 140);
		effectListTwo = cp5.addDropdownList(GuiElement.EFFECT_TWO_DROPDOWN.toString(), 
				4*DROPBOX_XOFS, p5GuiYOffset, DROPBOXLIST_LENGTH, 140);
		themeDropdownList(effectListOne);
		themeDropdownList(effectListTwo);
		i=0;
		for (EffectName gn: EffectName.values()) {
			effectListOne.addItem(gn.name(), i);
			effectListTwo.addItem(gn.name(), i);
			i++;
		}
		effectListOne.setLabel(effectListOne.getItem(0).getName());
		effectListTwo.setLabel(effectListTwo.getItem(0).getName());
		effectListOne.moveTo(ALWAYS_VISIBLE_TAB);
		effectListTwo.moveTo(ALWAYS_VISIBLE_TAB);

		//Mixer 
		mixerList = cp5.addDropdownList(GuiElement.MIXER_DROPDOWN.toString(), 
				2*DROPBOX_XOFS, p5GuiYOffset, DROPBOXLIST_LENGTH, 140);
		themeDropdownList(mixerList);
		i=0;
		for (MixerName gn: MixerName.values()) {
			mixerList.addItem(gn.name(), i);
			i++;
		}
		mixerList.setLabel(mixerList.getItem(0).getName());
		mixerList.moveTo(ALWAYS_VISIBLE_TAB);
		
		//Button
		randomSelection = cp5.addButton(GuiElement.BUTTON_RANDOM_CONFIGURATION.toString(), 0,
				5*DROPBOX_XOFS, p5GuiYOffset-15, 100, 15);
		randomSelection.setCaptionLabel("RANDOMIZE");
		randomSelection.moveTo(ALWAYS_VISIBLE_TAB);
		cp5.getTooltip().register(GuiElement.BUTTON_RANDOM_CONFIGURATION.toString(),"cross your fingers, randomize everything");
		
		randomPresets = cp5.addButton(GuiElement.BUTTON_RANDOM_PRESENT.toString(), 0,
				5*DROPBOX_XOFS, p5GuiYOffset+15, 100, 15);
		randomPresets.setCaptionLabel("RANDOM PRESENT");
		randomPresets.moveTo(ALWAYS_VISIBLE_TAB);
		cp5.getTooltip().register(GuiElement.BUTTON_RANDOM_PRESENT.toString(),"Loads a random preset");
		
		toggleRandom = cp5.addToggle(GuiElement.BUTTON_TOGGLE_RANDOM_MODE.toString(), true,
				5*DROPBOX_XOFS, p5GuiYOffset+45, 100, 15);
		toggleRandom.setCaptionLabel("RANDOM MODE");
		toggleRandom.setState(false);
		toggleRandom.moveTo(ALWAYS_VISIBLE_TAB);
		cp5.getTooltip().register(GuiElement.BUTTON_TOGGLE_RANDOM_MODE.toString(),"Toggle the random mode");		

		//tab ---
		
		cp5.getWindow().setPositionOfTabs(0, 492);
		
		//there a default tab which is present all the time. rename this tab
		Tab t1 = cp5.getTab("default");
		t1.setLabel("EFFECT");		
		Tab t2 = cp5.addTab("GENERATOR");		
		Tab t3 = cp5.addTab("RANDOM MODE");		
		
		t1.setColorForeground(0xffff0000);
		t2.setColorForeground(0xffff0000);		
		t3.setColorForeground(0xffff0000);
		
		//EFFECT tab
		thresholdSlider = cp5.addSlider(GuiElement.THRESHOLD.toString(), 0, 255, 255, 0, 350, 160, 14);
		thresholdSlider.setSliderMode(Slider.FIX);
		thresholdSlider.setGroup(t1);		
		thresholdSlider.setDecimalPrecision(0);
		
		scp = new SimpleColorPicker(cp5, (ControllerGroup)cp5.controlWindow.getTabs().get(1), GuiElement.COLOR_PICKER.toString(), 0, 380, 160, 14);
		cp5.register(null, "SimpleColorPicker", scp);		
		scp.registerProperty("interval");
		scp.addListener(listener);

		Button b3 = cp5.addButton("btn3");
		b3.setGroup(t2);  

		
		//register event listener
		cp5.addListener(listener);
				
		//select first visual
		selectedVisualList.activate(0);		
	}


	/**
	 * draw the whole internal buffer on screen.
	 */
	public void draw() {
		long l = System.currentTimeMillis();

		drawGradientBackground();

		int localX=0, localY=0;
		int[] buffer;
		Collector col = Collector.getInstance();

		//set used to find out if visual is on screen
		Set<Integer> outputId = new HashSet<Integer>();
		for (OutputMapping om: col.getAllOutputMappings()) {
			outputId.add(om.getVisualId());
		}

		//draw output buffer and marker
		int ofs=0;
		for (Visual v: col.getAllVisuals()) {
			//get image
			buffer = col.getMatrix().resizeBufferForDevice(v.getBuffer(), v.getResizeOption(), targetXSize, targetYSize);

			if (pImage==null) {
				//create an image out of the buffer
				pImage = col.getPapplet().createImage(targetXSize, targetYSize, PApplet.RGB );				
			}
			pImage.loadPixels();
			System.arraycopy(buffer, 0, pImage.pixels, 0, targetXSize*targetYSize);
			pImage.updatePixels();

			//draw current output
			if (outputId.contains(ofs)) {
				fill(66,200,66);
			} else {
				fill(55,55,55);
			}	
			rect(localX, localY+targetYSize+2, targetXSize, SELECTED_MARKER);				

			//display the image
			image(pImage, localX, localY);
			if (displayHoriz) {
				localX += pImage.width;
			} else {
				localY += pImage.height;
			}
			ofs++;
		}

		//display frame progress
		int frames = col.getFrames() % targetXSize;
		fill(200,200,200);
		rect(0, localY+targetYSize+SELECTED_MARKER+4, frames, 5);
		fill(55,55,55);
		rect(frames, localY+targetYSize+SELECTED_MARKER+4, targetXSize-frames, 5);

		//beat detection
		displaySoundStats(localY);

		cp5.draw();		
		col.getPixConStat().trackTime(TimeMeasureItemGlobal.DEBUG_WINDOW, System.currentTimeMillis()-l);
	}


	/**
	 * draw nice gradient at the end of the screen
	 */
	private void drawGradientBackground() {
		this.loadPixels();	
		int ofs=this.width*(this.height-255);

		for (int y=0; y<255; y++) {
			int pink = color(y/2, y/2, y/2);
			for (int x=0; x<this.width; x++) {
				this.pixels[ofs+x] = pink;				
			}
			ofs += this.width;
		}
		this.updatePixels();		
	}


	/**
	 * 
	 * @param localY
	 */
	private void displaySoundStats(int localY) {
		Sound snd = Sound.getInstance();

		int xofs = targetXSize+2;
		int xx = targetXSize/3-2;

		colorSelect(snd.isKick());
		rect(xofs, localY+targetYSize+SELECTED_MARKER+4, xx, 5);

		xofs+=xx+2;
		colorSelect(snd.isSnare());
		rect(xofs, localY+targetYSize+SELECTED_MARKER+4, xx, 5);

		xofs+=xx+2;
		colorSelect(snd.isHat());
		rect(xofs, localY+targetYSize+SELECTED_MARKER+4, xx, 5);		
	}


	/**
	 * 
	 * @param b
	 */
	private void colorSelect(boolean b) {
		if (b) {
			fill(200,200,200);	
		} else {
			fill(55,55,55);	
		}		
	}

	/**
	 * update only minimal parts of the gui
	 */
	public Collector callbackRefreshMini() {
		LOG.log(Level.INFO, "Refresh Partitial GUI");
		Collector col = Collector.getInstance();
		
		//get visual status			
		Visual v = col.getVisual(col.getCurrentVisual());

		if (v!=null) {		    
			generatorListOne.setLabel(generatorListOne.getItem(v.getGenerator1Idx()).getName());
			generatorListTwo.setLabel(generatorListTwo.getItem(v.getGenerator2Idx()).getName());
			effectListOne.setLabel(effectListOne.getItem(v.getEffect1Idx()).getName());
			effectListTwo.setLabel(effectListTwo.getItem(v.getEffect2Idx()).getName());
			mixerList.setLabel(mixerList.getItem(v.getMixerIdx()).getName());			
		}

		return col;
		//get output status
		//		OutputMapping om = ioMapping.get(currentOutput); 
		//		ret.add(ValidCommands.CHANGE_OUTPUT_EFFECT+EMPTY_CHAR+om.getEffect().getId());
		//		ret.add(ValidCommands.CHANGE_OUTPUT_FADER+EMPTY_CHAR+om.getFader().getId());
		//		ret.add(ValidCommands.CHANGE_OUTPUT_VISUAL+EMPTY_CHAR+om.getVisualId());
	}

	/**
	 * refresh whole gui
	 */
	public void callbackRefreshWholeGui() {
		LOG.log(Level.INFO, "Refresh Whole GUI");
		Collector col = this.callbackRefreshMini();		
				
		PixelControllerEffect pce = col.getPixelControllerEffect();
		scp.setR(pce.getR());
		scp.setG(pce.getG());
		scp.setB(pce.getB());
		
		thresholdSlider.changeValue(pce.getThresholdValue());
		
	}


	/**
	 * mouse listener, used to close dropdown lists
	 * 
	 */
	public void mousePressed() {
		// print the current mouseoverlist on mouse pressed
		List <GuiElement> clickedOn = new ArrayList<GuiElement>();
		List<ControllerInterface> lci = cp5.getWindow().getMouseOverList();
		for (ControllerInterface ci: lci) {
			GuiElement ge = GuiElement.getGuiElement(ci.getName());
			if (ge!=null) {
				clickedOn.add(ge);				
			}
		}

		if (!clickedOn.contains(GuiElement.GENERATOR_ONE_DROPDOWN)) {
			generatorListOne.setOpen(false);
		}
		if (!clickedOn.contains(GuiElement.GENERATOR_TWO_DROPDOWN)) {
			generatorListTwo.setOpen(false);
		}
		if (!clickedOn.contains(GuiElement.EFFECT_ONE_DROPDOWN)) {
			effectListOne.setOpen(false);
		}
		if (!clickedOn.contains(GuiElement.EFFECT_TWO_DROPDOWN)) {
			effectListTwo.setOpen(false);
		}
		if (!clickedOn.contains(GuiElement.MIXER_DROPDOWN)) {
			mixerList.setOpen(false);
		}
		
	}


	/**
	 * select visual by keypress
	 */
	public void keyPressed() {
		if(key>='1' && key<'9') {
			// convert a key-number (48-52) to an int between 0 and 4
			int n = (int)key-49;
			selectedVisualList.activate(n);
		}
	}
	



}
