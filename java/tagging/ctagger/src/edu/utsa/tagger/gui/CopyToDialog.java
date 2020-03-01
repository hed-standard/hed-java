package edu.utsa.tagger.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import edu.utsa.tagger.AbstractTagModel;
import edu.utsa.tagger.TaggedEvent;
import edu.utsa.tagger.Tagger;
import edu.utsa.tagger.TaggerSet;
import edu.utsa.tagger.guisupport.*;

/**
 * Dialog for user to select what events to copy the tag to
 *
 * @author Dung Truong
 */
public class CopyToDialog extends JDialog {

    private JLayeredPane tagPanel = new JLayeredPane();
    private Integer eventID;
    private Tagger tagger;
    private ArrayList<TagItemView> topLevelTags = new ArrayList<>();
    private TreeMap<Integer,ArrayList<TagItemView>> groupLevelTags = new TreeMap<>();
    private HashMap<String, Integer> copyToList = new HashMap<>();
    private JList eventList;
    private XButton cancelBtn;
    private XButton okButton;
    private boolean response;

    public CopyToDialog(JFrame frame, Tagger tagger, Integer evtID) {
        super(frame, "Copy Tag", true);
        this.tagger = tagger;
        eventID = evtID;

        ConstraintContainer mainPanel = new ConstraintContainer();
        mainPanel.setBackground(FontsAndColors.DIALOG_BG);
        mainPanel.setOpaque(true);
        JLayeredPane splitPane = new JLayeredPane();
        splitPane.setBackground(FontsAndColors.DIALOG_BG);
        splitPane.setOpaque(true);

        ConstraintContainer splitPaneLeft = new ConstraintContainer();
        JLabel tagTitle = new JLabel("Select tags to copy");
        tagTitle.setFont(FontsAndColors.headerFont);
        tagTitle.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
        splitPaneLeft.add(tagTitle, new Constraint("top:0 left:65 height:30 right:0"));
        tagPanel.setOpaque(true);
        tagPanel.setBackground(FontsAndColors.BLUE_MEDIUM);
        tagPanel.setLayout(new ConstraintLayout());
        addMainTags(tagger.getTaggedEventFromGroupId(eventID).getTagGroups());
        JLayeredPane tagContainer = new JLayeredPane();
        ScrollLayout tagScrollLayout = new ScrollLayout(tagContainer, tagPanel);
        tagContainer.setLayout(tagScrollLayout);
        splitPaneLeft.add(tagContainer, new Constraint("top:35 left:10 right:10 bottom:30"));

        ConstraintContainer splitPaneRight = new ConstraintContainer();
        JLabel eventTitle = new JLabel("Select events to copy tags to");
        eventTitle.setFont(FontsAndColors.headerFont);
        eventTitle.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
        splitPaneRight.add(eventTitle, new Constraint("top:0 left:13 height:30 right:0"));
        getCopyToList();
        eventList = new JList(copyToList.keySet().toArray());
        eventList.setBackground(FontsAndColors.BLUE_VERY_LIGHT);
        eventList.setFont(FontsAndColors.contentFont);
        eventList.setForeground(FontsAndColors.BLUE_DARK);
        eventList.setVisibleRowCount(-1);
        JLayeredPane eventContainer = new JLayeredPane();
        eventList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        ScrollLayout eventScrollLayout = new ScrollLayout(eventContainer, eventList);
        eventContainer.setLayout(eventScrollLayout);
        splitPaneRight.add(eventContainer, new Constraint("top:35 left:10 right:10 bottom:30"));

        VerticalSplitLayout splitLayout = new VerticalSplitLayout(splitPane, splitPaneLeft, splitPaneRight, 300);
        splitPane.setLayout(splitLayout);
        splitPane.setPreferredSize(new Dimension(600,500));
        mainPanel.add(splitPane, new Constraint("top:10 left:10 right:10 bottom:50"));

        cancelBtn = TaggerView.createMenuButton("Cancel");
        cancelBtn.addMouseListener(new CancelButtonListener());
        mainPanel.add(cancelBtn, new Constraint(
                "bottom:10 height:30 right:150 width:100"));

        okButton = TaggerView.createMenuButton("Ok");
        okButton.addMouseListener(new OkButtonListener());
        mainPanel.add(okButton, new Constraint(
                "bottom:10 height:30 right:5 width:100"));

        getContentPane().add(mainPanel);
        mainPanel.setPreferredSize(new Dimension(600,550));
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void getCopyToList() {
        for (TaggedEvent event : tagger.getEventSet()) {
            if (event.getEventLevelId() != eventID) {
                copyToList.put(event.getCode(),event.getEventLevelId());
            }
        }
    }

    public void addMainTags(TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroup) {
        int top = 0;
        JLabel topLevelLabel = new JLabel("Top level tags:");
        topLevelLabel.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
        topLevelLabel.setBackground(FontsAndColors.DIALOG_BG);
        topLevelLabel.setFont(FontsAndColors.contentFont.deriveFont(Font.BOLD));
        tagPanel.add(topLevelLabel, new Constraint("top:" + top + " height:23"));
        top += 23;

        for (AbstractTagModel tag : tagGroup.get(eventID)) {
            // addTagItemView
            TagItemView tagItemView = new TagItemView(tag);
            topLevelTags.add(tagItemView);
            tagPanel.add(tagItemView, new Constraint("top:" + top + " left:10 height:23 right:0"));
            top += 23;
        }

        int i = 1;
        for (Map.Entry<Integer, TaggerSet<AbstractTagModel>> entry : tagGroup.entrySet()) {
            if (entry.getKey() != eventID) {
                groupLevelTags.put(entry.getKey(), new ArrayList<TagItemView>());
                JLabel groupLabel = new JLabel("Group " + i++ + " tags:");
                groupLabel.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
                groupLabel.setBackground(FontsAndColors.DIALOG_BG);
                groupLabel.setFont(FontsAndColors.contentFont.deriveFont(Font.BOLD));
                tagPanel.add(groupLabel, new Constraint("top:" + top + " height:23"));
                top += 23;
                // addTagGroupView
                for (AbstractTagModel tag : entry.getValue()) {
                    TagItemView tagItemView = new TagItemView(tag);
                    groupLevelTags.get(entry.getKey()).add(tagItemView);
                    tagPanel.add(tagItemView, new Constraint("left:10 top:" + top + " height:23 right:0"));
                    top += 23;

                }
            }
        }
    }


    /**
     * Panel of checkbox and tag path
     */
    private class TagItemView extends JCheckBox{
        AbstractTagModel tagModel;

        public TagItemView(AbstractTagModel tag) {
            super(tag.getPath());
            this.tagModel = tag;
            this.setBackground(FontsAndColors.DIALOG_BG);
            this.setForeground(FontsAndColors.DIALOG_MESSAGE_FG);
        }

        public AbstractTagModel getTagModel() {
            return tagModel;
        }
    }

    private class CancelButtonListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            response = false;
            setVisible(false);
            dispose();
        }
    }
    private class OkButtonListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            ArrayList<Integer> selectedEventID = new ArrayList<>();
            for (int selected : eventList.getSelectedIndices()) {
                String eventCode = (String) eventList.getModel().getElementAt(selected);
                Integer evtID = copyToList.get(eventCode);
                selectedEventID.add(evtID);
            }
            // copy top level tags
            for (TagItemView tagView : topLevelTags) {
                if (tagView.isSelected()) {
                    GuiTagModel tagModel = (GuiTagModel) tagView.getTagModel();
                    tagModel.requestToggleTag(new HashSet(selectedEventID));
                }
            }

            // copy group tags
            // create new groups
            for (Map.Entry<Integer, ArrayList<TagItemView>> entry : groupLevelTags.entrySet()) {
                Set<Integer> newGroupIDs = null;
                for (TagItemView tagView : entry.getValue()) {
                    if (tagView.isSelected()) {
                        if (newGroupIDs == null) {
                            newGroupIDs = tagger.addNewGroups(new HashSet(selectedEventID));
                        }
                        GuiTagModel tagModel = (GuiTagModel) tagView.getTagModel();
                        tagModel.requestToggleTag(newGroupIDs);
                    }
                }
            }
            response = true;
            setVisible(false);
            dispose();
        }
    }
}