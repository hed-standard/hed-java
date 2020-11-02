package edu.utsa.tagger;

import edu.utsa.tagger.app.TestUtilities;
import edu.utsa.tagger.gui.GuiModelFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class launches the Tagger automatically to be used as a stand-alone
 * tool.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
public class TaggerLauncher {
	public static void main(String[] args) throws IOException {
		String hedXML = "";
		String url = "https://raw.githubusercontent.com/hed-standard/hed-specification/HED-restructure/hedxml-reduced/HEDLatest-reduced.xml";
		try {
			BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
			hedXML = TestUtilities.getResourceAsString(in);
		}
		catch (Exception e) {
			hedXML = TestUtilities.getResourceAsString(TestUtilities.HedFileName);
		}

		TaggerLoader.load(hedXML, "", 44, "CTagger", 3, new GuiModelFactory()); //44

	}
}
