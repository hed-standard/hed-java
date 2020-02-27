//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.utsa.tagger;

import edu.utsa.tagger.gui.GuiTagModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaggerHistory {
    private static Tagger tagger;
    private int undoIndex = -1;
    private int redoIndex = -1;
    private int capacity = 20;
    private List<HistoryItem> undoStack = new ArrayList();
    private List<HistoryItem> redoStack = new ArrayList();

    public TaggerHistory(Tagger tagger) {
        TaggerHistory.tagger = tagger;
    }

    public void add(HistoryItem item) {
        this.addToUndo(item);
        this.redoStack.clear();
        this.redoIndex = -1;
    }

    private void addToRedo(HistoryItem item) {
        this.redoStack.add(item);
        ++this.redoIndex;
        if (this.redoStack.size() > this.capacity) {
            this.redoStack.remove(0);
            --this.redoIndex;
        }

    }

    private void addToUndo(HistoryItem item) {
        this.undoStack.add(item);
        ++this.undoIndex;
        if (this.undoStack.size() > this.capacity) {
            this.undoStack.remove(0);
            --this.undoIndex;
        }

    }

    private String getMessage(TaggerHistory.Type type) {
        switch(type.ordinal()) {
        case 1:
            return "clear";
        case 2:
            return "add tag";
        case 3:
            return "delete tag";
        case 4:
            return "add event";
        case 5:
            return "delete event";
        case 6:
            return "add group";
        case 7:
            return "add groups";
        case 8:
            return "remove group";
        case 9:
            return "tag edit";
        case 10:
            return "tag path edit";
        case 11:
            return "event edit";
        case 12:
            return "tag event(s)";
        case 13:
            return "untag event(s)";
        case 14:
                return "clear all tags";
        default:
            return "";
        }
    }

    private HistoryItem getNextRedo() {
        return this.redoIndex == -1 ? null : (HistoryItem)this.redoStack.remove(this.redoIndex--);
    }

    private HistoryItem getNextUndo() {
        return this.undoIndex == -1 ? null : (HistoryItem)this.undoStack.remove(this.undoIndex--);
    }

    public String getRedoMessage() {
        if (this.redoIndex == -1) {
            return "No actions to redo.";
        } else {
            HistoryItem item = (HistoryItem)this.redoStack.get(this.redoIndex);
            String message = "Redo " + this.getMessage(item.type);
            return message;
        }
    }

    public String getUndoMessage() {
        if (this.undoIndex == -1) {
            return "No actions to undo.";
        } else {
            HistoryItem item = (HistoryItem)this.undoStack.get(this.undoIndex);
            String message = "Undo " + this.getMessage(item.type);
            return message;
        }
    }

    public HistoryItem redo() {
        HistoryItem item = this.getNextRedo();
        if (item == null) {
            return null;
        } else {
            switch(item.type.ordinal()) {
            case 0:
                this.redoClear(item);
            case 1:
                this.redoAddTag(item);
                break;
            case 2:
                this.redoRemoveTag(item);
                break;
            case 3:
                this.redoAddEvent(item);
                break;
            case 4:
                this.redoRemoveEvent(item);
                break;
            case 5:
                this.redoAddGroup(item);
                break;
            case 6:
                this.redoAddGroups(item);
                break;
            case 7:
                this.redoRemoveGroup(item);
                break;
            case 8:
                this.redoTagEdited(item);
                break;
            case 9:
                this.redoTagPathEdited(item);
                break;
            case 10:
                this.redoEventEdited(item);
                break;
            case 11:
                this.redoAssociate(item);
                break;
            case 12:
                this.redoUnassociate(item);
            case 13:
                this.redoClearAllTags(item);
            }

            return item;
        }
    }

    private void redoAddEvent(HistoryItem item) {
        if (item.event != null && tagger.addEventBase(item.event)) {
            this.addToUndo(item);
        }

    }

    private void redoAddGroup(HistoryItem item) {
        if (item.groupId != null && item.tags != null && item.event != null && tagger.addGroupBase(item.event, item.groupId, item.tags)) {
            this.addToUndo(item);
        }

    }

    private void redoAddGroups(HistoryItem item) {
        if (item.groupIds != null && item.tags != null && item.events != null) {
            TaggedEvent[] events = (TaggedEvent[])item.events.toArray(new TaggedEvent[item.events.size()]);
            Integer[] groupIds = (Integer[])item.groupIds.toArray(new Integer[item.groupIds.size()]);

            for(int i = 0; i < events.length; ++i) {
                tagger.addGroupBase(events[i], groupIds[i], item.tags);
            }

            this.addToUndo(item);
        }

    }

    private void redoAddTag(HistoryItem item) {
        if (item.tagModel != null) {
            tagger.addTagModelBase(item.tagModel);
            this.addToUndo(item);
        }

    }

    private void redoClear(HistoryItem item) {
        tagger.clearLists();
        this.addToUndo(item);
    }

    private void redoAssociate(HistoryItem item) {
        if (item.tagModel != null && item.groupsIds != null) {
            if ("Event/Label".equals(item.tagModel.getParentPath()) && item.eventModel != null) {
                item.eventModel.setLabel(item.tagModel.getPath());
            }

            tagger.associateBase(item.tagModel, item.groupsIds);
            this.addToUndo(item);
        }

    }

    private void redoEventEdited(HistoryItem item) {
        if (item.eventModel != null && item.eventModelCopy != null) {
            AbstractEventModel copy = tagger.editEventCodeLabelBase(item.eventModel, item.eventModelCopy.getCode(), item.eventModelCopy.getLabel());
            item.eventModelCopy = copy;
            if (item.tagModel != null) {
                item.tagModel.setPath("Event/Label/" + item.eventModel.getLabel());
            }

            this.addToUndo(item);
        }

    }

    private void redoRemoveEvent(HistoryItem item) {
        if (item.event != null && tagger.removeEventBase(item.event)) {
            this.addToUndo(item);
        }

    }

    private void redoRemoveGroup(HistoryItem item) {
        if (item.groupId != null && item.tags != null && item.event != null && tagger.removeGroupBase(item.event, item.groupId) != null) {
            this.addToUndo(item);
        }

    }

    private void redoRemoveTag(HistoryItem item) {
        if (item.tagModel != null && item.tags != null) {
            TaggerSet<AbstractTagModel> deleted = tagger.deleteTagBase(item.tagModel);
            item.tags = deleted;
            this.addToUndo(item);
        }

    }

    private void redoTagEdited(HistoryItem item) {
        if (item.tagModelCopy != null && item.tagModel != null) {
            GuiTagModel original = item.tagModelCopy;
            GuiTagModel copy = tagger.editTagBase((GuiTagModel)item.tagModel, original.getName(), original.getDescription(), original.isExtensionAllowed(), original.isChildRequired(), original.takesValue(), original.isNumeric(), original.isRequired(), original.isRecommended(), original.isUnique(), original.getPosition(), original.getPredicateType(), original.getUnitClass());
            item.tagModelCopy = copy;
            this.addToUndo(item);
        }

    }

    private void redoTagPathEdited(HistoryItem item) {
        GuiTagModel original = item.tagModelCopy;
        if ("Event/Label".equals(item.tagModelCopy.getParentPath())) {
            item.eventModel.setLabel(original.getName());
        }

        GuiTagModel copy = tagger.editTagBase((GuiTagModel)item.tagModel, original.getPath(), item.tagModelCopy.getName(), original.getDescription(), original.isExtensionAllowed(), original.isChildRequired(), original.takesValue(), item.tagModelCopy.isRequired(), original.isRecommended(), original.isUnique(), original.getPosition());
        item.tagModelCopy = copy;
        this.addToUndo(item);
    }

    private void redoUnassociate(HistoryItem item) {
        if (item.tagModel != null && item.groupsIds != null) {
            if ("Event/Label".equals(item.tagModel.getParentPath()) && item.eventModel != null) {
                item.eventModel.setLabel(new String());
            }

            tagger.unassociateBase(item.tagModel, item.groupsIds);
            this.addToUndo(item);
        }
    }

    private void redoClearAllTags(HistoryItem item) {
        tagger.clearAllTags();
        addToUndo(item);
    }

    public HistoryItem undo() {
        HistoryItem item = this.getNextUndo();
        if (item == null) {
            return null;
        } else {
            switch(item.type.ordinal()) {
            case 0:
                this.undoClear(item);
            case 1:
                this.undoAddTag(item);
                break;
            case 2:
                this.undoRemoveTag(item);
                break;
            case 3:
                this.undoAddEvent(item);
                break;
            case 4:
                this.undoRemoveEvent(item);
                break;
            case 5:
                this.undoAddGroup(item);
                break;
            case 6:
                this.undoAddGroups(item);
                break;
            case 7:
                this.undoRemoveGroup(item);
                break;
            case 8:
                this.undoTagEdited(item);
                break;
            case 9:
                this.undoTagPathEdited(item);
                break;
            case 10:
                this.undoEventEdited(item);
                break;
            case 11:
                this.undoAssociate(item);
                break;
            case 12:
                this.undoUnassociate(item);
            case 13:
                this.undoClearAllTags(item);
            }

            return item;
        }
    }

    private void undoAddEvent(HistoryItem item) {
        if (item.event != null && tagger.removeEventBase(item.event)) {
            this.addToRedo(item);
        }

    }

    private void undoAddGroup(HistoryItem item) {
        if (item.groupId != null && item.tags != null && item.event != null && tagger.removeGroupBase(item.event, item.groupId) != null) {
            this.addToRedo(item);
        }

    }

    private void undoAddGroups(HistoryItem item) {
        if (item.groupIds != null && item.events != null) {
            TaggedEvent[] events = (TaggedEvent[])item.events.toArray(new TaggedEvent[item.events.size()]);
            Integer[] groupIds = (Integer[])item.groupIds.toArray(new Integer[item.groupIds.size()]);

            for(int i = 0; i < events.length; ++i) {
                tagger.removeGroupBase(events[i], groupIds[i]);
            }

            this.addToRedo(item);
        }

    }

    private void undoAddTag(HistoryItem item) {
        if (item.tagModel != null) {
            tagger.deleteTagBase(item.tagModel);
            this.addToRedo(item);
        }

    }

    private void undoClear(HistoryItem item) {
        tagger.restoreLists();
        this.addToRedo(item);
    }

    private void undoAssociate(HistoryItem item) {
        if (item.tagModel != null && item.groupsIds != null) {
            if ("Event/Label".equals(item.tagModel.getParentPath()) && item.eventModel != null) {
                item.eventModel.setLabel(item.tagModel.getPath());
            }

            tagger.unassociateBase(item.tagModel, item.groupsIds);
            this.addToRedo(item);
        }

    }

    private void undoEventEdited(HistoryItem item) {
        if (item.eventModel != null && item.eventModelCopy != null) {
            AbstractEventModel copy = tagger.editEventCodeLabelBase(item.eventModel, item.eventModelCopy.getCode(), item.eventModelCopy.getLabel());
            item.eventModelCopy = copy;
            if (item.tagModel != null) {
                item.tagModel.setPath("Event/Label/" + item.eventModel.getLabel());
            }

            this.addToRedo(item);
        }

    }

    private void undoRemoveEvent(HistoryItem item) {
        if (item.event != null) {
            int index = item.eventModelPosition;
            if (tagger.addEventBase(index, item.event)) {
                this.addToRedo(item);
            }
        }

    }

    private void undoRemoveGroup(HistoryItem item) {
        if (item.groupId != null && item.tags != null && item.event != null && tagger.addGroupBase(item.event, item.groupId, item.tags)) {
            this.addToRedo(item);
        }

    }

    private void undoRemoveTag(HistoryItem item) {
        if (item.tags != null && item.tagModel != null) {
            int index = item.tagModelPosition;
            tagger.addTagModelBase(index, item.tagModel);
            Iterator var4 = item.tags.iterator();

            while(var4.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var4.next();
                tagger.addTagModelBase(index++, tag);
            }

            this.addToRedo(item);
        }

    }

    private void undoTagEdited(HistoryItem item) {
        if (item.tagModelCopy != null && item.tagModel != null) {
            GuiTagModel original = item.tagModelCopy;
            GuiTagModel copy = tagger.editTagBase((GuiTagModel)item.tagModel, original.getName(), original.getDescription(), original.isExtensionAllowed(), original.isChildRequired(), original.takesValue(), original.isNumeric(), original.isRequired(), original.isRecommended(), original.isUnique(), original.getPosition(), original.getPredicateType(), original.getUnitClass());
            item.tagModelCopy = copy;
            this.addToRedo(item);
        }

    }

    private void undoTagPathEdited(HistoryItem item) {
        GuiTagModel original = item.tagModelCopy;
        if ("Event/Label".equals(item.tagModelCopy.getParentPath()) && item.eventModel != null) {
            item.eventModel.setLabel(original.getName());
        }

        GuiTagModel copy = tagger.editTagBase((GuiTagModel)item.tagModel, original.getPath(), item.tagModelCopy.getName(), original.getDescription(), original.isExtensionAllowed(), original.isChildRequired(), original.takesValue(), item.tagModelCopy.isRequired(), original.isRecommended(), original.isUnique(), original.getPosition());
        item.tagModelCopy = copy;
        this.addToRedo(item);
    }

    private void undoUnassociate(HistoryItem item) {
        if (item.tagModel != null && item.groupsIds != null) {
            if ("Event/Label".equals(item.tagModel.getParentPath()) && item.eventModel != null) {
                item.eventModel.setLabel(new String());
            }

            tagger.associateBase(item.tagModel, item.groupsIds);
            this.addToRedo(item);
        }

    }

    private void undoClearAllTags(HistoryItem item) {
        if (item.tagMap != null) {
            for (TaggedEvent evt : tagger.getEventSet()) {
                if (item.tagMap.containsKey(evt)) {
                    evt.setTagGroups(item.tagMap.get(evt));
                }
            }
        }

        addToRedo(item);
    }

    public static enum Type {
        CLEAR,
        TAG_ADDED,
        TAG_REMOVED,
        EVENT_ADDED,
        EVENT_REMOVED,
        GROUP_ADDED,
        GROUPS_ADDED,
        GROUP_REMOVED,
        TAG_EDITED,
        TAG_PATH_EDITED,
        EVENT_EDITED,
        ASSOCIATED,
        UNASSOCIATED,
        CLEAR_ALL_TAGS;

        private Type() {
        }
    }
}
