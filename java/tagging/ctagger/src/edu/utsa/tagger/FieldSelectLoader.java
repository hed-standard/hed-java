package edu.utsa.tagger;

import edu.utsa.tagger.gui.FieldSelectView;
import edu.utsa.tagger.gui.GuiModelFactory;

/**
 * This class is used to load the FieldSelectView that allows the user to select
 * which fields to exclude or tag.
 * 
 * @author Jeremy Cockfield, Kay Robbins
 */
public class FieldSelectLoader {
    private boolean submitted;
    private boolean notified;
    private String primaryField;
    private String[] taggedFields;
    private String[] ignoredFields;
	FieldSelectView fieldSelectView;

    public FieldSelectLoader(String frameTitle, String[] ignoredFields, String[] taggedFields, String primaryField) {
        this(new GuiModelFactory(), frameTitle, ignoredFields, taggedFields, primaryField);
	}

    public FieldSelectLoader(IFactory factory, String frameTitle, String[] ignoredFields, String[] taggedFields, String primaryField) {
        this.submitted = false;
        this.notified = false;
        this.primaryField = new String();
        this.taggedFields = null;
        this.ignoredFields = null;
        this.fieldSelectView = factory.createFieldSelectView(this, frameTitle, ignoredFields, taggedFields, primaryField);
	}

	/**
	 * Gets the primary field.
	 * 
	 * @return A String containing the primary field.
	 */
	public synchronized String getPrimaryField() {
		return primaryField;
	}

    public synchronized String[] getTaggedFields() {
        return this.taggedFields;
	}

    public synchronized String[] getIgnoredFields() {
        return this.ignoredFields;
	}

	/**
	 * Checks to see if the FieldSelectView is notified.
	 * 
	 * @return True if the FieldSelectView is notified, false if otherwise.
	 */
	public synchronized boolean isNotified() {
		return notified;
	}

	/**
	 * Checks to see if the FieldSelectView is submitted.
	 * 
	 * @return True if the FieldSelectView is submitted, false if otherwise.
	 */
	public synchronized boolean isSubmitted() {
		return submitted;
	}

	public synchronized void setNotified(boolean notified) {
		this.notified = notified;
        this.notify();
	}

	/**
	 * Sets the primary field.
	 * 
	 * @param primaryField
	 *            Sets the primary field from the FieldSelectView.
	 */
	public synchronized void setPrimaryField(String primaryField) {
		this.primaryField = primaryField;
	}

	/**
	 * Sets if the FieldSelectView is submitted.
	 * 
	 * @param submitted
	 *            True if the FieldSelectView is submitted, false if otherwise.
	 */
	public synchronized void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	/**
	 * Sets the tagged fields.
	 * 
	 * @param taggedFields
	 *            A String array containing the tagged fields.
	 */
	public synchronized void setTaggedFields(String[] taggedFields) {
		this.taggedFields = taggedFields;
	}

    public synchronized void setIgnoredFields(String[] ignoredFields) {
        this.ignoredFields = ignoredFields;
	}

	/**
	 * Waits for the FieldSelectView to send a notification.
	 */
	public synchronized void waitForNotified() {
		try {
			while (!notified)
				wait();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}