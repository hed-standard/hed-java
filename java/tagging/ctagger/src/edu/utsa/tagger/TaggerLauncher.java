package edu.utsa.tagger;

import edu.utsa.tagger.app.TestUtilities;
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
		TaggerLoader loader = TaggerLoader.load(1, "CTagger", 3, new GuiModelFactory()); //44
        boolean done = false;
		while (!done) {
			try {
				Thread.sleep(500);
			}
			catch(InterruptedException e) {
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace());
			}
			done = loader.isNotified();
		}
		if (done) {
			try {
				System.out.println(loader.getXMLAndEvents()[1]);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace());
			}
		}
	}
}
