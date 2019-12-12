package edu.utsa.tagger;

import edu.utsa.tagger.gui.GuiModelFactory;

/**
 * This class launches the Tagger automatically to be used as a stand-alone
 * tool.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
public class TaggerLauncher {
	public static void main(String[] args) {

		TaggerLoader.load(44, "CTagger", 3, new GuiModelFactory()); //44

	}
}
