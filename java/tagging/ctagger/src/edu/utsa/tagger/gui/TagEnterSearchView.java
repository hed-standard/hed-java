package edu.utsa.tagger.gui;

import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * View to display tag as a search result
 * from entering tag in tagged event
 */
public class TagEnterSearchView extends TagSearchView {
    EventEnterTagView eventEnterTagView;

    public TagEnterSearchView(EventEnterTagView eventEnterTagView, GuiTagModel model) {
        super(eventEnterTagView.getTagger(), eventEnterTagView.getAppView(), model);
        this.eventEnterTagView = eventEnterTagView;
    }

    @Override
    public void mouseClickedEvent(Tagger tagger, TaggerView appView) {
        addTagToEvent();
    }

    /**
     * Add tag to the event associated with this eventEnterTagView
     * and any selected group that the event contains
     */
    public void addTagToEvent() {
        if (getModel().requestToggleTag(eventEnterTagView.getAppView().getSelected()) == 0) {
            TaggedEvent tgevt = eventEnterTagView.getTagger().getTaggedEventFromGroupId(Collections.max(eventEnterTagView.getAppView().getSelected()));
            eventEnterTagView.getAppView().scrollToEvent(tgevt);
            eventEnterTagView.getAppView().getEventEnterTagView().getjTextArea().transferFocusBackward();
            eventEnterTagView.getAppView().getEventEnterTagView().getjTextArea().setText("Enter tag ...");
            eventEnterTagView.getAppView().getEventEnterTagView().getjTextArea().requestFocusInWindow();
        }
    }
}
