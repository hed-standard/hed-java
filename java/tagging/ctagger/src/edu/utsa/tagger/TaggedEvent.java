//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.utsa.tagger;

import edu.utsa.tagger.gui.*;

import java.util.*;

/**
 * This class represents a tagged event, consisting of the event model and the
 * associated groupIds and tag models.
 *
 * @author Lauren Jett, Rebecca Strautman, Thomas Rognon, Jeremy Cockfield, Kay
 *         Robbins, Dung Truong
 */
public class TaggedEvent implements Comparable<TaggedEvent> {
    private static Tagger tagger;
    private static TaggerView appView;
    private GuiEventModel guiEventModel;
    private TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups;
    private int eventLevelId;
    private EventView eventView;
    private HashMap<Integer, GroupView> groupViews;
    private HashMap<AbstractTagModel, TagEventView> tagEgtViews;
    private HashMap<AbstractTagModel, RRTagView> rrTagViews;
    private EventEditView eventEditView;
//    private EventEnterTagView eventEnterTagView;

    public TaggedEvent(GuiEventModel guiEventModel, Tagger tagger) {
        this.guiEventModel = guiEventModel;
        TaggedEvent.tagger = tagger;
        this.tagGroups = new TreeMap();
        this.groupViews = new HashMap();
        this.tagEgtViews = new HashMap();
        this.rrTagViews = new HashMap();
//        this.eventEnterTagView = new EventEnterTagView(tagger, this);
    }

    /**
     * Adds an empty group with the given ID to the event.
     *
     * @param groupId
     * @return True if the group was added successfully, false if the group ID
     *         already existed for this event.
     */
    public boolean addGroup(int groupId) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags == null) {
            tags = new TaggerSet();
            this.tagGroups.put(groupId, tags);
            return true;
        } else {
            return false;
        }
    }

    public void addGroupView(GroupView groupView) {
        this.groupViews.put(groupView.getGroupId(), groupView);
    }

    public GroupView getGroupViewByKey(int groupId) {
        return (GroupView)this.groupViews.get(groupId);
    }

    public void addTagEgtView(AbstractTagModel tagModel, TagEventView tagEgtView) {
        this.tagEgtViews.put(tagModel, tagEgtView);
    }

    public TagEventView getTagEgtViewByKey(AbstractTagModel tagModel) {
        return (TagEventView)this.tagEgtViews.get(tagModel);
    }

    public void addRRTagView(AbstractTagModel tagModel, RRTagView rrTagView) {
        this.rrTagViews.put(tagModel, rrTagView);
    }

    public RRTagView getRRTagViewByKey(AbstractTagModel tagModel) {
        return (RRTagView)this.rrTagViews.get(tagModel);
    }

    public boolean addTag(AbstractTagModel tagModel) {
        return this.addTagToGroup(this.eventLevelId, tagModel);
    }

    public boolean addTagToGroup(int groupId, AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags == null) {
            return false;
        } else {
            if ("~".equals(tagModel.getName())) {
                tags.add(tagModel, true);
            } else {
                tags.add(tagModel);
            }

            if (this.getLabel().length() > 0) {
                this.guiEventModel.setLabel(this.getLabel());
            }

            return true;
        }
    }

    public boolean addTagToGroup(int groupId, AbstractTagModel tagModel, int index) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags == null) {
            return false;
        } else {
            if ("~".equals(tagModel.getName())) {
                tags.add(index, tagModel, true);
            } else {
                tags.add(tagModel);
            }

            if (this.getLabel().length() > 0) {
                this.guiEventModel.setLabel(this.getLabel());
            }

            return true;
        }
    }

    public int compareTo(TaggedEvent o) {
        int result = this.getLabel().compareTo(o.getLabel());
        return result != 0 ? result : this.getEventModel().compareTo(o.getEventModel());
    }

    public boolean containsGroup(int groupId) {
        return this.tagGroups.containsKey(groupId);
    }

    public boolean containsTagInGroup(int groupId, AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        return tags != null ? tags.contains(tagModel) : false;
    }

    public int findTagIndex(int groupId, AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        return tags != null && tags.contains(tagModel) ? tags.indexOf(tagModel) : -1;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            TaggedEvent other = (TaggedEvent)obj;
            if (other.getLabel() == null) {
                return false;
            } else {
                return this.compareTo(other) == 0;
            }
        }
    }

    public AbstractTagModel findDescendant(int groupId, AbstractTagModel uniqueKey) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags == null) {
            return null;
        } else {
            String uniquePrefix = uniqueKey.getPath() + "/";
            Iterator var6 = tags.iterator();

            AbstractTagModel tag;
            String path;
            do {
                if (!var6.hasNext()) {
                    return null;
                }

                tag = (AbstractTagModel)var6.next();
                path = tag.getPath();
            } while(!path.equals(uniqueKey.getPath()) && !path.startsWith(uniquePrefix) && !path.equals(uniqueKey.getPath()));

            return tag;
        }
    }

    public Set<Integer> findTagGroup(String tagName) {
        Set<Integer> groups = new HashSet();
        TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups = this.getTagGroups();
        Set<Integer> tagGroupKeys = tagGroups.keySet();
        Iterator var6 = tagGroupKeys.iterator();

        while(var6.hasNext()) {
            Integer key = (Integer)var6.next();
            Iterator var8 = ((TaggerSet)tagGroups.get(key)).iterator();

            while(var8.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var8.next();
                if (tag.getPath().startsWith(tagName)) {
                    groups.add(key);
                }
            }
        }

        return groups;
    }

    public AbstractTagModel findTagModel(String tagName) {
        TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups = this.getTagGroups();
        Set<Integer> tagGroupKeys = tagGroups.keySet();
        Iterator var5 = tagGroupKeys.iterator();

        while(var5.hasNext()) {
            Integer key = (Integer)var5.next();
            Iterator var7 = ((TaggerSet)tagGroups.get(key)).iterator();

            while(var7.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var7.next();
                if (tag.getPath().startsWith(tagName)) {
                    return tag;
                }
            }
        }

        return null;
    }

    public AbstractTagModel findTagSharedPath(int groupId, AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags != null) {
            Iterator var5 = tags.iterator();

            while(var5.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var5.next();
                String path = tag.getPath();
                if (path.startsWith(tagModel.getPath() + "/") || path.equals(tagModel.getPath()) || tagModel.getPath().startsWith(path + "/")) {
                    return tag;
                }
            }
        }

        return null;
    }

    public int findGroupTildeCount(int groupId) {
        int count = 0;
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags != null) {
            Iterator var5 = tags.iterator();

            while(var5.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var5.next();
                if ("~".equals(tag.getPath())) {
                    ++count;
                }
            }
        }

        return count;
    }

    public EventEditView getEventEditView() {
        if (this.eventEditView == null) {
            this.eventEditView = new EventEditView(tagger, appView, this);
        }

        return this.eventEditView;
    }

    public int getEventLevelId() {
        return this.eventLevelId;
    }

    public GuiEventModel getEventModel() {
        return this.guiEventModel;
    }

    public EventView getEventView() {
        if (this.eventView == null) {
            this.eventView = new EventView(tagger, appView, this);
        }

        return this.eventView;
    }

    public int getGroupId(int groupId) {
        int count = 0;

        for(Iterator var4 = this.tagGroups.keySet().iterator(); var4.hasNext(); ++count) {
            int id = (Integer)var4.next();
            if (id == groupId) {
                return count;
            }
        }

        return -1;
    }

    public String getLabel() {
        String label = new String();
        Iterator var3 = ((TaggerSet)this.tagGroups.get(this.eventLevelId)).iterator();

        while(var3.hasNext()) {
            AbstractTagModel tag = (AbstractTagModel)var3.next();
            if (tag.getPath().startsWith("Event/Label/")) {
                return tag.getName();
            }
        }

        return label;
    }

    public String getCode() {
        return guiEventModel.getCode();
    }

    public int getNumTagsInGroup(int groupId) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        return tags == null ? -1 : tags.size();
    }

    public RRTagView getRRTagView(AbstractTagModel key) {
        return new RRTagView(tagger, appView, this, key);
    }

    public TaggerSet<AbstractTagModel> getRRValue(AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> desc = new TaggerSet();
        TaggerSet<AbstractTagModel> eventTags = (TaggerSet)this.tagGroups.get(this.eventLevelId);

        for (AbstractTagModel tag : eventTags) {
            if (tag.getPath().startsWith(tagModel.getPath() + "/") || tag.getPath().equals(tagModel.getPath())) {
                desc.add(tag);
            }
        }
        if (desc.size() > 0)
            return desc;
        else
            return null;
    }

    public TreeMap<Integer, TaggerSet<AbstractTagModel>> getTagGroups() {
        return this.tagGroups;
    }

    public void setTagGroups(TreeMap<Integer, TaggerSet<AbstractTagModel>> tGroup) {
        tagGroups = tGroup;
    }

//    public EventEnterTagView getEventEnterTagView() {return eventEnterTagView;}

    public int findTagCount() {
        int numTags = 0;

        for(Iterator tagGroupIterator = this.tagGroups.keySet().iterator(); tagGroupIterator.hasNext(); numTags += this.getNumTagsInGroup((Integer)tagGroupIterator.next())) {
        }

        return numTags;
    }

    public boolean isInEdit() {
        return this.guiEventModel.isInEdit();
    }

    public boolean isInFirstEdit() {
        return this.guiEventModel.isInFirstEdit();
    }

    public TaggerSet<AbstractTagModel> removeGroup(int groupId) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        this.tagGroups.remove(groupId);
        return tags;
    }

    public boolean removeTagFromGroup(int groupId, AbstractTagModel tagModel) {
        TaggerSet<AbstractTagModel> tags = (TaggerSet)this.tagGroups.get(groupId);
        if (tags == null) {
            return false;
        } else {
            Iterator it = tags.iterator();

            while(it.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)it.next();
                if (tag.equals(tagModel)) {
                    it.remove();
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isRRTagDescendant(AbstractTagModel tagModel) {
        Iterator rrTags = this.rrTagViews.keySet().iterator();

        while(rrTags.hasNext()) {
            AbstractTagModel rrTag = (AbstractTagModel)rrTags.next();
            if (tagModel.getPath().startsWith(rrTag.getPath())) {
                return true;
            }
        }

        return false;
    }

    public AbstractTagModel findRRParentTag(AbstractTagModel tagModel) {
        Iterator rrTags = this.rrTagViews.keySet().iterator();

        while(rrTags.hasNext()) {
            AbstractTagModel rrTag = (AbstractTagModel)rrTags.next();
            if (tagModel.getPath().startsWith(rrTag.getPath())) {
                return rrTag;
            }
        }

        return null;
    }

    public void setEventLevelId(int groupId) {
        this.eventLevelId = groupId;
        this.addGroup(groupId);
    }

    public void setEventModel(GuiEventModel eventModel) {
        this.guiEventModel = eventModel;
    }

    public void setInEdit(boolean inEdit) {
        this.guiEventModel.setInEdit(inEdit);
    }

    public void setInFirstEdit(boolean inFirstEdit) {
        this.guiEventModel.setInFirstEdit(inFirstEdit);
    }

    public void setShowInfo(boolean showInfo) {
        this.guiEventModel.setShowInfo(showInfo);
    }

    public boolean showInfo() {
        return this.guiEventModel.showInfo();
    }

    public void setAppView(TaggerView appView) {
        TaggedEvent.appView = appView;
    }


    /**
     * Delete all tags associated with this event
     * @return copy of the tag map of this event
     */
    public TreeMap<Integer, TaggerSet<AbstractTagModel>> deleteAllTag() {
        // Create copy of the to be removed tagGroups, for undoing
        TreeMap<Integer, TaggerSet<AbstractTagModel>> removed = new TreeMap<Integer, TaggerSet<AbstractTagModel>>();
        for (Map.Entry<Integer, TaggerSet<AbstractTagModel>> entry : tagGroups.entrySet()) {
            removed.put(entry.getKey(), (TaggerSet<AbstractTagModel>) entry.getValue().clone());
        }
        // clear
        tagGroups.clear();
        tagGroups.put(eventLevelId, new TaggerSet<AbstractTagModel>());
        guiEventModel.setLabel("");
        return removed;
    }

    /**
     * Retrieve all tags associated with this event in string format
     * @return  an array list containing all tags of this event in string
     */
    public ArrayList<String> tagsToString() {
        ArrayList<String> tags = new ArrayList<>();
        for (TaggerSet<AbstractTagModel> tagSet : tagGroups.values()) {
            for (AbstractTagModel tag : tagSet) {
                tags.add(tag.getPath());
            }
        }

        return tags;
    }
}
