package edu.utsa.tagger.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.utsa.tagger.*;
import edu.utsa.tagger.guisupport.ITagDisplay;
import edu.utsa.tagger.guisupport.MessageConstants;

/**
 * This class represents a tag, including information used by the GUI.
 * 
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins
 */
public class GuiTagModel extends AbstractTagModel {

	// Highlight color to display for tag
	public enum Highlight {
		//NONE, HIGHLIGHT_MATCH, HIGHLIGHT_CLOSE_MATCH, HIGHLIGHT_TAKES_VALUE, GREY_VERY_VERY_LIGHT, GREY_VERY_LIGHT, GREY_LIGHT, GREY_VERY_VERY_MEDIUM, GREY_VERY_MEDIUM, GREY_MEDIUM, GREY_DARK, GREY_VERY_DARK, GREY_VERY_VERY_DARK;
		NONE, HIGHLIGHT_MATCH, HIGHLIGHT_CLOSE_MATCH, HIGHLIGHT_TAKES_VALUE, BLUE_VERY_LIGHT, BLUE_1, BLUE_2, BLUE_3, BLUE_4, BLUE_5, BLUE_6, BLUE_7, BLUE_8;
	};

	private final Tagger tagger;
	private TaggerView appView;
	private TagView tagView;
	private TagEditView tagEditView;
    private AddValueView addValueView;
	private TagChooserView tagChooserView;

	private boolean inEdit;
	private boolean firstEdit;
	private boolean inAddValue;
	private boolean collapsable;
	private boolean collapsed;
	private boolean missing = false;
	private Highlight highlight = Highlight.NONE;

	public int selectionState = SELECTION_STATE_NONE;

	public static final int SELECTION_STATE_NONE = 0;
	public static final int SELECTION_STATE_MIXED = 1;
	public static final int SELECTION_STATE_ALL = 2;
	private ArrayList<AbstractTagModel> attributes;

	public GuiTagModel(final Tagger tagger) {
		this.tagger = tagger;
	}

	/**
	 * Gets the view for adding a value, used if this tag takes a value.
	 * 
	 * @return
	 */
	public AddValueView getAddValueView() {
        if (this.addValueView == null) {
            this.addValueView = new AddValueView(this.tagger, this.appView, this);
        }

        return this.addValueView;
	}

	/**
	 * Gets a view for adding a value, used if this tag takes a value. Uses an
	 * alternate tag display.
	 * 
	 * @param alternateView
	 *            A tag display other than the main appView.
	 * @return
	 */
	public AddValueView getAlternateAddValueView(ITagDisplay alternateView) {
		return new AddValueView(tagger, appView, alternateView, this);
	}

	public Highlight getHighlight() {
		return highlight;
	}

	/**
	 * Returns a tag view for using the tag chooser dialog.
	 * 
	 * @param baseDepth
	 *            The depth of the tag at the base of the sub-hierarchy
	 * @return
	 */
	public TagChooserView getTagChooserView(int baseDepth) {
		if (tagChooserView == null) {
			tagChooserView = new TagChooserView(tagger, this);
		}
		tagChooserView.setDepth(getDepth() - baseDepth);
		return tagChooserView;
	}

	/**
	 * Gets the view for editing the tag in the GUI.
	 * 
	 * @return
	 */
	public TagEditView getTagEditView() {
		if (tagEditView == null) {
			tagEditView = new TagEditView(tagger, appView, this);
		}
		return tagEditView;
	}

	public RRTagView getRRTagView(TaggedEvent taggedEvent) {
		return new RRTagView(tagger, appView, taggedEvent, this);
	}

    public TagEventEditView getTagEventEditView(TaggedEvent taggedEvent) {
        return new TagEventEditView(this.tagger, taggedEvent, this);
	}

	/**
	 * Create a new TagEventView
	 * @param groupId
	 * @return
	 */
    public TagEventView getTagEventView(int groupId) {
        return new TagEventView(this.tagger, this.appView, groupId, this, false);
	}

	/**
	 * Returns the tag view to be used when searching tags.
	 * 
	 * @return
	 */
	public TagSearchView getTagSearchView() {
		return new TagSearchView(tagger, appView, this);
	}

	/**
	 * Gets the basic tag view for the GUI.
	 * 
	 * @return
	 */
	public TagView getTagView() {
		if (tagView == null) {
			tagView = new TagView(tagger, appView, this);
		}
		return tagView;
	}

	public TaggerView getAppView() {return appView;}

	public Tagger getTagger() {return tagger;}

	public ArrayList<AbstractTagModel> getAttributes() {
		return attributes;
	}

	/**
	 * Get all units associated with the unit classes of this tag
	 * @return	an array of all units associated with this tag
	 */
	public String[] getUnits() {
		// Get list of unit classes associated with this tag
		String[] unitClassArray = Tagger.trimStringArray(getUnitClass().split(","));
		String[] unitsArray = {}; // all units from all unit classes applied to the tag
		// Retrieve units of each unit classes and put them into unitsArray
		// content of unitComboBox will be populated using unitsArray
		for (int i = 0; i < unitClassArray.length; i++) {
			if (!unitClassArray[i].isEmpty() && tagger.unitClasses.get(unitClassArray[i]) != null) {
				// get units of the class and put them into a String array (unitStrings) of unit names
				List<UnitXmlModel> units = tagger.unitClasses.get(unitClassArray[i]);
				String[] unitStrings = new String[units.size()];
				int j = 0;
				for (UnitXmlModel unit : units) {
					unitStrings[j++] = unit.getName();
				}
				unitsArray = Tagger.concat(unitsArray, unitStrings);
			}
		}
		return unitsArray;
	}

	public boolean isCollapsable() {
		return collapsable;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public boolean isInAddValue() {
		return inAddValue;
	}

	public boolean isInEdit() {
		return inEdit;
	}

	public boolean isFirstEdit() {
		return firstEdit;
	}

	public boolean isMissing() {
		return missing;
	}

	/**
	 * Attempts to toggle this tag with the groups with the groups currently
	 * selected for tagging.
	 */
	public int requestToggleTag() {
        return this.requestToggleTag(this.appView.getSelected());
	}

	/**
	 * Attempts to toggle this tag with the groups with the given IDs.
	 * 
	 * @param groupIds
	 *
	 * @return 0 if sucess, -1 if failed
	 */
	public int requestToggleTag(Set<Integer> groupIds) {
		if (groupIds.isEmpty()) {
			appView.showTaggerMessageDialog(MessageConstants.NO_EVENT_SELECTED,
					"Okay", null, null);
			return -1;
		}
		if (isChildRequired()) {
			appView.showTaggerMessageDialog(
					MessageConstants.SELECT_CHILD_ERROR, "Ok", null, null);
			return -1;
		}
		ToggleTagMessage message = tagger.toggleTag(this, groupIds);
		if (message != null) {
			if (message.rrError) {
				appView.showTaggerMessageDialog(
						MessageConstants.ASSOCIATE_RR_ERROR, "Ok", null, null);
			} else if (message.descendants.size() > 0) {
				appView.showDescendantDialog(message);
			} else if (message.uniqueValues.size() > 0) {
				appView.showUniqueDialog(message);
			} else {
				appView.showAncestorDialog(message);
			}
		}
		appView.updateEventsPanel();
		return 0;
	}

	public void setAppView(TaggerView appView) {
		this.appView = appView;
	}

	public void setCollapsable(boolean collapsable) {
		this.collapsable = collapsable;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public void setHighlight(Highlight highlight) {
		this.highlight = highlight;
	}

	public void setInAddValue(boolean addTransient) {
		this.inAddValue = addTransient;
	}

	public void setInEdit(boolean inEdit) {
		this.inEdit = inEdit;
	}

	public void setFirstEdit(boolean firstEdit) {
		this.firstEdit = firstEdit;
	}

	public void setMissing(boolean missing) {
		this.missing = missing;
	}

	public void setAttributes(ArrayList<AbstractTagModel> list) {
		attributes = list;
	}

	/**
	 * Updates whether the tag is missing from the hierarchy.
	 */
	public void updateMissing() {
        this.tagger.updateMissing(this);
    }


}
