//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package edu.utsa.tagger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import edu.utsa.tagger.TagXmlModel.PredicateType;
import edu.utsa.tagger.TaggerHistory.Type;
import edu.utsa.tagger.gui.GuiEventModel;
import edu.utsa.tagger.gui.GuiTagModel;
import edu.utsa.tagger.gui.GuiTagModel.Highlight;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.Map.Entry;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Tagger {
    private static int groupIdCounter = 0;
    private static final String LABEL_TAG = "Event/Label/";
    private static final String HED_VERSION_TAG = "HED/";
    private TaggerSet<TaggedEvent> backupEventList = new TaggerSet();
    private TaggerSet<AbstractTagModel> backupTagList = new TaggerSet();
    public Highlight currentHighlightType;
    private boolean extensionsAllowed = false;
    private boolean extendAnywhere = false;
    private TaggerSet<TaggedEvent> eventList = new TaggerSet();
    private TaggerSet<AbstractTagModel> extensionAllowedTags = new TaggerSet();
    private TaggerSet<AbstractTagModel> newTags = new TaggerSet();
    private IFactory factory;
    private boolean hedExtended = false;
    public GuiTagModel highlightTag;
    private TaggerHistory history;
    private boolean isPrimary = true;
    private TaggerLoader loader;
    public Highlight previousHighlightType;
    private TaggerSet<AbstractTagModel> recommendedTags = new TaggerSet();
    private TaggerSet<AbstractTagModel> requiredTags = new TaggerSet();
    private int tagLevel = 0;
    private TaggerSet<AbstractTagModel> tags = new TaggerSet();
    private final String[] tsvHeader = new String[]{"Event code", "Event category", "Event label", "Event long name", "Event description", "Other tags"};
    private TaggerSet<AbstractTagModel> uniqueTags = new TaggerSet();
    public HashMap<String, String> unitClassDefaults = new HashMap();
    public HashMap<String, String> unitClasses = new HashMap();
    private String hedVersion = "";

    public static String[] concat(String[] s1, String[] s2) {
        String[] concat = new String[s1.length + s2.length];
        System.arraycopy(s1, 0, concat, 0, s1.length);
        System.arraycopy(s2, 0, concat, s1.length, s2.length);
        return concat;
    }

    private static List<String> splitPath(String path) {
        String[] pathTokens = path.split("[/]");
        List<String> pathAsList = new ArrayList();
        if (pathTokens.length > 0) {
            for(int i = 1; i < pathTokens.length; ++i) {
                pathAsList.add(pathTokens[i]);
            }
        }

        return pathAsList;
    }

    public static String[] trimStringArray(String[] array) {
        String[] trimmedArray = new String[array.length];

        for(int i = 0; i < array.length; ++i) {
            trimmedArray[i] = array[i].trim();
        }

        return trimmedArray;
    }

    public Tagger(IFactory factory, TaggerLoader loader) {
        this.factory = factory;
        this.loader = loader;
        this.history = new TaggerHistory(this);
        this.isPrimary = loader.checkFlags(32);
        this.extendAnywhere = loader.checkFlags(8);
        this.extensionsAllowed = loader.checkFlags(4);
        this.hedExtended = loader.checkFlags(128);
    }

    public Tagger(String xmlData, IFactory factory, TaggerLoader loader) {
        this.factory = factory;
        this.loader = loader;
        this.history = new TaggerHistory(this);
        this.isPrimary = loader.checkFlags(32);
        this.extendAnywhere = loader.checkFlags(8);
        this.extensionsAllowed = loader.checkFlags(4);
        this.hedExtended = loader.checkFlags(128);
        if (xmlData.isEmpty()) {
            throw new RuntimeException("XML data is empty.");
        } else {
            TaggerDataXmlModel savedDataXmlModel = null;

            try {
                JAXBContext context = JAXBContext.newInstance(TaggerDataXmlModel.class);
                savedDataXmlModel = (TaggerDataXmlModel)context.createUnmarshaller().unmarshal(new StringReader(xmlData));
            } catch (JAXBException var6) {
                throw new RuntimeException("Unable to read XML data: " + var6.getMessage());
            }

            if (savedDataXmlModel == null) {
                throw new RuntimeException("Unable to read XML data");
            } else {
                this.processXmlData(savedDataXmlModel);
            }
        }
    }

    public Tagger(String hedString, String eventString, IFactory factory, TaggerLoader loader) {
        this.factory = factory;
        this.loader = loader;
        this.isPrimary = loader.checkFlags(32);
        this.history = new TaggerHistory(this);
        this.extendAnywhere = loader.checkFlags(8);
        this.extensionsAllowed = loader.checkFlags(4);
        this.hedExtended = loader.checkFlags(128);
        this.tags = new TaggerSet();
        this.eventList = new TaggerSet();

        try {
            HedXmlModel hedXmlModel = this.readHEDString(hedString);
            this.populateTagList(hedXmlModel);
            if (loader.checkFlags(1)) {
                Set<EventJsonModel> eventJsonModels = this.readEventJsonString(eventString);
                this.populateEventsFromJson(eventJsonModels);
            } else {
                BufferedReader egtReader = new BufferedReader(new StringReader(eventString));
                this.populateEventsFromTabDelimitedText(egtReader);
            }
        } catch (Exception var7) {
            System.err.println("Unable to create tagger:\n" + var7.getMessage());
        }

    }

    public boolean addEventBase(int index, TaggedEvent event) {
        return this.eventList.add(index, event);
    }

    public boolean addEventBase(TaggedEvent event) {
        return this.eventList.add(event);
    }

    public boolean addGroupBase(TaggedEvent taggedEvent, Integer groupId, TaggerSet<AbstractTagModel> tags) {
        if (!taggedEvent.addGroup(groupId)) {
            return false;
        } else {
            Iterator var5 = tags.iterator();

            while(var5.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var5.next();
                taggedEvent.addTagToGroup(groupId, tag);
            }

            return true;
        }
    }

    public TaggedEvent addNewEvent(String code, String label) {
        GuiEventModel eventModel = (GuiEventModel)this.factory.createAbstractEventModel(this);
        eventModel.setCode(code);
        eventModel.setLabel(label);
        TaggedEvent taggedEvent = new TaggedEvent(eventModel, this);
        int groupId = groupIdCounter++;
        taggedEvent.setEventLevelId(groupId);
        if (!label.trim().isEmpty()) {
            AbstractTagModel labelTag = this.getTagModel("Event/Label/" + label);
            taggedEvent.addTag(labelTag);
        }

        if (this.addEventBase(taggedEvent)) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.EVENT_ADDED;
            historyItem.event = taggedEvent;
            this.history.add(historyItem);
        }

        return taggedEvent;
    }

    public int addNewGroup(TaggedEvent taggedEvent) {
        int groupId = groupIdCounter++;
        if (taggedEvent.addGroup(groupId)) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.GROUP_ADDED;
            historyItem.event = taggedEvent;
            historyItem.groupId = groupId;
            historyItem.tags = (TaggerSet)taggedEvent.getTagGroups().get(groupId);
            this.history.add(historyItem);
        }

        return groupId;
    }

    public Set<Integer> addNewGroups(Set<Integer> eventIds) {
        TaggerSet<Integer> newEventGroupIds = new TaggerSet();
        TaggerSet<TaggedEvent> selectedEvents = new TaggerSet();
        TaggerSet<AbstractTagModel> tags = new TaggerSet();
        boolean eventSelected = false;
        Iterator var7 = eventIds.iterator();

        while(var7.hasNext()) {
            Integer eventId = (Integer)var7.next();
            Iterator var9 = this.eventList.iterator();

            while(var9.hasNext()) {
                TaggedEvent event = (TaggedEvent)var9.next();
                if (eventId == event.getEventLevelId()) {
                    selectedEvents.add(event);
                    ++groupIdCounter;
                    event.addGroup(groupIdCounter);
                    newEventGroupIds.add(groupIdCounter);
                    eventSelected = true;
                }
            }
        }

        if (eventSelected) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.GROUPS_ADDED;
            historyItem.events = selectedEvents;
            historyItem.groupIds = newEventGroupIds;
            historyItem.tags = tags;
            this.history.add(historyItem);
        }

        return newEventGroupIds;
    }

    public AbstractTagModel addNewTag(AbstractTagModel parent, String name) {
        GuiTagModel newTag = (GuiTagModel)this.factory.createAbstractTagModel(this);
        GuiTagModel parentTag = (GuiTagModel)parent;
        String parentPath = new String();
        Highlight[] highlights = Highlight.values();
        if (parent != null) {
            parentPath = parent.getPath();
            int parentHighlightPosition = this.findHighlightPosition(highlights, parentTag.getHighlight());
            if (parentHighlightPosition >= 0) {
                int childHighlightPosition = parentHighlightPosition + 1;
                newTag.setHighlight(highlights[childHighlightPosition]);
            }
        } else {
            newTag.setHighlight(Highlight.BLUE_VERY_LIGHT);
        }

        newTag.setPath(parentPath + "/" + name);
        newTag.setInEdit(true);
        this.extensionAllowedTags.add(newTag);
        this.newTags.add(newTag);
        if (this.addTagModelBase(newTag)) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.TAG_ADDED;
            historyItem.tagModel = newTag;
            this.history.add(historyItem);
            return newTag;
        } else {
            return null;
        }
    }

    public boolean addTagModelBase(AbstractTagModel newTagModel) {
        if (newTagModel.getParentPath() == null) {
            return this.tags.add(newTagModel);
        } else {
            String parentPath = newTagModel.getParentPath();

            int i;
            AbstractTagModel tagModel;
            for(i = 0; i < this.tags.size(); ++i) {
                tagModel = (AbstractTagModel)this.tags.get(i);
                if (tagModel.getPath().equals(parentPath)) {
                    break;
                }
            }

            while(i < this.tags.size()) {
                tagModel = (AbstractTagModel)this.tags.get(i);
                if (!tagModel.getPath().startsWith(parentPath)) {
                    break;
                }

                ++i;
            }

            if (this.tags.add(i, newTagModel)) {
                this.updateTagLists();
                this.sortRRTags();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean addTagModelBase(int index, AbstractTagModel newTagModel) {
        if (this.tags.add(index, newTagModel)) {
            this.updateTagLists();
            this.sortRRTags();
            return true;
        } else {
            return false;
        }
    }

    public void associate(AbstractTagModel tagModel, int index, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.associateBase(tagModel, index, groupIds);
        if (!affectedGroups.isEmpty()) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.ASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            this.history.add(historyItem);
        }

    }

    public void associate(AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.associateBase(tagModel, groupIds);
        if (!affectedGroups.isEmpty()) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.ASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            this.history.add(historyItem);
        }

    }

    public void associate(HistoryItem historyItem, AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.associateBase(tagModel, groupIds);
        if (!affectedGroups.isEmpty()) {
            historyItem.type = Type.ASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            this.history.add(historyItem);
        }

    }

    public Set<Integer> associateBase(AbstractTagModel tagModel, int index, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = new HashSet();
        Iterator var6 = groupIds.iterator();

        while(var6.hasNext()) {
            Integer groupId = (Integer)var6.next();
            TaggedEvent taggedEvent = this.getTaggedEventFromGroupId(groupId);
            if (taggedEvent.addTagToGroup(groupId, tagModel, index)) {
                affectedGroups.add(groupId);
            }
        }

        return affectedGroups;
    }

    public Set<Integer> associateBase(AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = new HashSet();
        Iterator var5 = groupIds.iterator();

        while(var5.hasNext()) {
            Integer groupId = (Integer)var5.next();
            TaggedEvent taggedEvent = this.getTaggedEventFromGroupId(groupId);
            if (taggedEvent.addTagToGroup(groupId, tagModel)) {
                affectedGroups.add(groupId);
            }
        }

        return affectedGroups;
    }

    private Set<EventJsonModel> buildEventJsonModels() {
        Set<EventJsonModel> result = new LinkedHashSet();
        Iterator var3 = this.eventList.iterator();

        while(var3.hasNext()) {
            TaggedEvent event = (TaggedEvent)var3.next();
            EventJsonModel jsonEvent = new EventJsonModel();
            jsonEvent.setCode(event.getEventModel().getCode());
            List<List<String>> tags = new ArrayList();
            Iterator var7 = event.getTagGroups().entrySet().iterator();

            while(true) {
                while(var7.hasNext()) {
                    Entry<Integer, TaggerSet<AbstractTagModel>> entry = (Entry)var7.next();
                    if ((Integer)entry.getKey() == event.getEventLevelId()) {
                        Iterator var13 = ((TaggerSet)entry.getValue()).iterator();

                        while(var13.hasNext()) {
                            AbstractTagModel tag = (AbstractTagModel)var13.next();
                            ArrayList<String> eventTags = new ArrayList();
                            eventTags.add(tag.getPath());
                            tags.add(eventTags);
                        }
                    } else {
                        ArrayList<String> groupTags = new ArrayList();
                        Iterator var10 = ((TaggerSet)entry.getValue()).iterator();

                        while(var10.hasNext()) {
                            AbstractTagModel tag = (AbstractTagModel)var10.next();
                            groupTags.add(tag.getPath());
                        }

                        tags.add(groupTags);
                    }
                }

                jsonEvent.setTags(tags);
                result.add(jsonEvent);
                break;
            }
        }

        return result;
    }

    private TaggerDataXmlModel buildSavedDataModel() {
        TaggerDataXmlModel savedDataModel = new TaggerDataXmlModel();
        savedDataModel.setEventSetXmlModel(this.eventToXmlModel());
        HedXmlModel hedModel = new HedXmlModel();
        TagXmlModel tagModel = this.tagsToModel();
        hedModel.setTags(tagModel.getTags());
        hedModel.setVersion(this.hedVersion);
        savedDataModel.setHedXmlModel(hedModel);
        savedDataModel.getHedXmlModel().setUnitClasses(this.unitClassesToXmlModel());
        return savedDataModel;
    }

    public boolean getExtensionsAllowed() {
        return this.extensionsAllowed;
    }

    public void clearLists() {
        this.backupEventList.addAll(this.eventList);
        this.backupTagList.addAll(this.tags);
        this.eventList.clear();
        this.tags.clear();
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.CLEAR;
        this.history.add(historyItem);
    }

    private String combineColumns(String delimiter, String[] cols, int[] colNums) {
        String combinedCols = new String();

        for(int i = 0; i < colNums.length; ++i) {
            try {
                if (!cols[colNums[i] - 1].trim().isEmpty()) {
                    combinedCols = combinedCols + delimiter + cols[colNums[i] - 1].trim().replaceAll("~", ",~,");
                }
            } catch (Exception var7) {
            }
        }

        combinedCols = combinedCols.replaceFirst(delimiter, "");
        return combinedCols;
    }

    private TaggedEvent createNewEvent(String code) {
        GuiEventModel eventModel = (GuiEventModel)this.factory.createAbstractEventModel(this);
        eventModel.setCode(code);
        eventModel.setLabel(new String());
        TaggedEvent taggedEvent = new TaggedEvent(eventModel, this);
        int groupId = groupIdCounter++;
        taggedEvent.setEventLevelId(groupId);
        return taggedEvent;
    }

    private void createTagSets(Set<TagXmlModel> tagXmlModels) {
        this.tagLevel = 0;
        this.populateTagSets(new String(), tagXmlModels, -1);
        this.sortRRTags();
    }

    private int populateTagSets(String path, Set<TagXmlModel> tagXmlModels, int level) {
        this.tagLevel = Math.max(this.tagLevel, level);
        ++level;
        Object[] highlights = Arrays.copyOfRange(Highlight.values(), 4, Highlight.values().length);
        AbstractTagModel parentTag = this.tagFound(path);
        Iterator var7 = tagXmlModels.iterator();

        while(var7.hasNext()) {
            TagXmlModel tagXmlModel = (TagXmlModel)var7.next();
            AbstractTagModel tagModel = this.factory.createAbstractTagModel(this);
            if (path.isEmpty()) {
                tagModel.setPath(tagXmlModel.getName());
            } else {
                tagModel.setPath(path + "/" + tagXmlModel.getName());
            }

            tagModel.setDescription(tagXmlModel.getDescription());
            tagModel.setChildRequired(tagXmlModel.isChildRequired());
            tagModel.setTakesValue(tagXmlModel.takesValue());
            tagModel.setExtensionAllowed(tagXmlModel.isExtensionAllowed());
            tagModel.setRecommended(tagXmlModel.isRecommended());
            tagModel.setRequired(tagXmlModel.isRequired());
            tagModel.setUnique(tagXmlModel.isUnique());
            if (parentTag != null && PredicateType.PROPERTYOF.equals(parentTag.getPredicateType())) {
                tagModel.setPredicateType(PredicateType.PROPERTYOF);
            } else {
                tagModel.setPredicateType(PredicateType.valueOf(tagXmlModel.getPredicateType().toUpperCase()));
            }

            tagModel.setPosition(tagXmlModel.getPosition());
            tagModel.setIsNumeric(tagXmlModel.isNumeric());
            tagModel.setUnitClass(tagXmlModel.getUnitClass());
            this.tags.add(tagModel);
            GuiTagModel guiTagModel = (GuiTagModel)tagModel;
            guiTagModel.setHighlight((Highlight)highlights[level]);
            if (tagModel.isRequired()) {
                this.requiredTags.add(tagModel);
            } else if (tagModel.isRecommended()) {
                this.recommendedTags.add(tagModel);
            }

            if (tagModel.isUnique()) {
                this.uniqueTags.add(tagModel);
            }

            if (path.isEmpty()) {
                this.populateTagSets(tagXmlModel.getName(), tagXmlModel.getTags(), level);
            } else {
                this.populateTagSets(path + "/" + tagXmlModel.getName(), tagXmlModel.getTags(), level);
            }
        }

        if (this.isExtensionTag(parentTag, tagXmlModels)) {
            this.extensionAllowedTags.add(parentTag);
        }

        return level;
    }

    public boolean isHEDVersionTag(AbstractTagModel tag) {
        return tag.getPath().startsWith("HED/");
    }

    private boolean isExtensionTag(AbstractTagModel tag, Set<TagXmlModel> tagXmlModels) {
        if (tag != null && this.extensionsAllowed) {
            return !"#".equals(tag.getName()) && (tag.isExtensionAllowed() || tagXmlModels.isEmpty() || this.extendAnywhere);
        } else {
            return false;
        }
    }

    public AbstractTagModel createTransientTagModel(AbstractTagModel valueTag, String value) {
        AbstractTagModel tag = this.factory.createAbstractTagModel(this);
        String valueStr = valueTag.getName().replace("#", value);
        tag.setPath(valueTag.getParentPath() + "/" + valueStr);
        return tag;
    }

    public String createTSVString() throws IOException {
        StringWriter sw = new StringWriter();
        BufferedWriter br = new BufferedWriter(sw);
        this.writeTSVFile(br);

        try {
            br.close();
        } catch (IOException var4) {
            System.err.println("Error writing events to string: " + var4.getMessage());
            return null;
        }

        return sw.toString();
    }

    private void createUnitClassHashMapFromXml(UnitClassesXmlModel unitClassesXmlModels) {
        Iterator var3 = unitClassesXmlModels.getUnitClasses().iterator();

        while(var3.hasNext()) {
            UnitClassXmlModel unitClassXmlModel = (UnitClassXmlModel)var3.next();
            this.unitClasses.put(unitClassXmlModel.getName(), unitClassXmlModel.getUnits());
            this.unitClassDefaults.put(unitClassXmlModel.getName(), unitClassXmlModel.getDefault());
        }

    }

    public void deleteTag(AbstractTagModel tag) {
        int tagPosition = this.tags.indexOf(tag);
        TaggerSet<AbstractTagModel> removedTags = this.deleteTagBase(tag);
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.TAG_REMOVED;
        historyItem.tagModel = tag;
        historyItem.tagModelPosition = tagPosition;
        historyItem.tags = removedTags;
        this.history.add(historyItem);
    }

    public TaggerSet<AbstractTagModel> deleteTagBase(AbstractTagModel tag) {
        TaggerSet<AbstractTagModel> removedTags = new TaggerSet();
        String path = tag.getPath();
        this.tags.remove(tag);
        removedTags.add(tag);
        String prefix = path + "/";
        Iterator it = this.tags.iterator();

        while(it.hasNext()) {
            AbstractTagModel currentTag = (AbstractTagModel)it.next();
            if (currentTag.getPath().startsWith(prefix)) {
                it.remove();
                removedTags.add(currentTag);
            }
        }

        this.updateTagLists();
        return removedTags;
    }

    public void editEventCode(AbstractEventModel event, String code) {
        AbstractEventModel copy = this.editEventCodeBase(event, code);
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.EVENT_EDITED;
        historyItem.eventModel = event;
        historyItem.eventModelCopy = copy;
        this.history.add(historyItem);
    }

    public AbstractEventModel editEventCodeBase(AbstractEventModel event, String code) {
        AbstractEventModel copy = this.factory.createAbstractEventModel(this);
        copy.setCode(event.getCode());
        event.setCode(code);
        return copy;
    }

    public void editEventCodeLabel(TaggedEvent taggedEvent, AbstractTagModel tag, String code, String label) {
        AbstractEventModel copy = this.editEventCodeLabelBase(taggedEvent.getEventModel(), code, label);
        HistoryItem historyItem = new HistoryItem();
        historyItem.event = taggedEvent;
        historyItem.eventModel = taggedEvent.getEventModel();
        historyItem.eventModelCopy = copy;
        if (tag != null && !label.trim().isEmpty()) {
            historyItem.type = Type.EVENT_EDITED;
            historyItem.tagModel = tag;
            tag.setPath("Event/Label/" + label);
            historyItem.tagModelCopy = (GuiTagModel)tag;
            this.history.add(historyItem);
        } else if (tag != null && label.trim().isEmpty()) {
            TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups = taggedEvent.getTagGroups();
            this.unassociate(historyItem, tag, tagGroups.keySet());
        } else if (tag == null && !label.trim().isEmpty()) {
            AbstractTagModel labelTag = this.getTagModel("Event/Label/" + label);
            TreeMap<Integer, TaggerSet<AbstractTagModel>> tagGroups = taggedEvent.getTagGroups();
            if (taggedEvent.isInFirstEdit()) {
                this.associateBase(labelTag, tagGroups.keySet());
            } else {
                this.associate(historyItem, labelTag, tagGroups.keySet());
            }
        }

    }

    public AbstractEventModel editEventCodeLabelBase(AbstractEventModel event, String code, String label) {
        AbstractEventModel copy = this.factory.createAbstractEventModel(this);
        copy.setCode(event.getCode());
        event.setCode(code);
        copy.setLabel(event.getLabel());
        event.setLabel(label);
        return copy;
    }

    public void editEventLabel(AbstractEventModel event, String label) {
        AbstractEventModel copy = this.editEventLabelBase(event, label);
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.EVENT_EDITED;
        historyItem.eventModel = event;
        historyItem.eventModelCopy = copy;
        this.history.add(historyItem);
    }

    public AbstractEventModel editEventLabelBase(AbstractEventModel event, String label) {
        AbstractEventModel copy = this.factory.createAbstractEventModel(this);
        copy.setLabel(event.getLabel());
        event.setLabel(label);
        return copy;
    }

    public void editTag(GuiTagModel tag, String name, String description, boolean extensionAllowed, boolean childRequired, boolean takesValue, boolean isNumeric, boolean required, boolean recommended, boolean unique, Integer position, PredicateType predicateType, String unitClass) {
        GuiTagModel copy = this.editTagBase(tag, name, description, extensionAllowed, childRequired, takesValue, isNumeric, required, recommended, unique, position, predicateType, unitClass);
        if (!tag.isFirstEdit()) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.TAG_EDITED;
            historyItem.tagModelCopy = copy;
            historyItem.tagModel = tag;
            this.history.add(historyItem);
        }

    }

    public GuiTagModel editTagBase(GuiTagModel tag, String name, String description, boolean extensionAllowed, boolean childRequired, boolean takesValue, boolean isNumeric, boolean required, boolean recommended, boolean unique, Integer position, PredicateType predicateType, String unitClass) {
        GuiTagModel copy = (GuiTagModel)this.factory.createAbstractTagModel(this);
        copy.setPath(tag.getPath());
        copy.setDescription(tag.getDescription());
        copy.setChildRequired(tag.isChildRequired());
        copy.setTakesValue(tag.takesValue());
        copy.setExtensionAllowed(tag.isExtensionAllowed());
        copy.setIsNumeric(tag.isNumeric());
        copy.setRequired(tag.isRequired());
        copy.setRecommended(tag.isRecommended());
        copy.setUnique(tag.isUnique());
        copy.setPosition(tag.getPosition());
        copy.setPredicateType(tag.getPredicateType());
        copy.setUnitClass(tag.getUnitClass());
        if (!name.isEmpty()) {
            this.updateTagName(tag, name);
        }

        if (description != null) {
            tag.setDescription(description);
        }

        tag.setChildRequired(childRequired);
        tag.setTakesValue(takesValue);
        tag.setExtensionAllowed(extensionAllowed);
        tag.setIsNumeric(isNumeric);
        tag.setRequired(required);
        tag.setRecommended(recommended);
        tag.setUnique(unique);
        tag.setPosition(position);
        tag.setPredicateType(predicateType);
        tag.setUnitClass(unitClass);
        this.updateTagLists();
        return copy;
    }

    public GuiTagModel editTagBase(GuiTagModel tag, String path, String name, String description, boolean extensionAllowed, boolean childRequired, boolean takesValue, boolean required, boolean recommended, boolean unique, Integer position) {
        GuiTagModel copy = (GuiTagModel)this.factory.createAbstractTagModel(this);
        copy.setPath(tag.getPath());
        copy.setDescription(tag.getDescription());
        copy.setChildRequired(tag.isChildRequired());
        copy.setExtensionAllowed(tag.isExtensionAllowed());
        copy.setTakesValue(tag.takesValue());
        copy.setRequired(tag.isRequired());
        copy.setRecommended(tag.isRecommended());
        copy.setUnique(tag.isUnique());
        copy.setPosition(tag.getPosition());
        tag.setPath(path);
        if (!name.isEmpty()) {
            this.updateTagName(tag, name);
        }

        if (description != null) {
            tag.setDescription(description);
        }

        tag.setExtensionAllowed(extensionAllowed);
        tag.setChildRequired(childRequired);
        tag.setTakesValue(takesValue);
        tag.setRequired(required);
        tag.setRecommended(recommended);
        tag.setUnique(unique);
        tag.setPosition(position);
        this.updateTagLists();
        return copy;
    }

    public void editTagPath(TaggedEvent taggedEvent, GuiTagModel tag, String path) {
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.TAG_PATH_EDITED;
        historyItem.tagModelCopy = this.editTagPathBase(tag, path);
        String[] paths = path.split("/");
        tag.setPath(path);
        historyItem.tagModel = tag;
        if (path.startsWith("Event/Label/")) {
            historyItem.eventModelCopy = taggedEvent.getEventModel();
            if (path.equals("Event/Label/")) {
                taggedEvent.getEventModel().setLabel(new String());
            } else {
                taggedEvent.getEventModel().setLabel(paths[paths.length - 1]);
            }

            historyItem.eventModel = taggedEvent.getEventModel();
        }

        this.history.add(historyItem);
    }

    public GuiTagModel editTagPathBase(GuiTagModel tag, String path) {
        GuiTagModel copy = (GuiTagModel)this.factory.createAbstractTagModel(this);
        copy.setPath(tag.getPath());
        tag.setPath(path);
        return copy;
    }

    private EventSetXmlModel eventToXmlModel() {
        EventSetXmlModel eventSetModel = new EventSetXmlModel();
        EventXmlModel currentEvent = null;
        GroupXmlModel currentGroup = null;
        Iterator var5 = this.eventList.iterator();

        label41:
        while(var5.hasNext()) {
            TaggedEvent currentEventModel = (TaggedEvent)var5.next();
            currentEvent = new EventXmlModel();
            currentEvent.setCode(currentEventModel.getEventModel().getCode());
            eventSetModel.addEvent(currentEvent);
            Iterator var7 = currentEventModel.getTagGroups().entrySet().iterator();

            while(true) {
                while(true) {
                    if (!var7.hasNext()) {
                        continue label41;
                    }

                    Entry<Integer, TaggerSet<AbstractTagModel>> tagGroup = (Entry)var7.next();
                    AbstractTagModel tag;
                    Iterator var9;
                    if ((Integer)tagGroup.getKey() == currentEventModel.getEventLevelId()) {
                        var9 = ((TaggerSet)tagGroup.getValue()).iterator();

                        while(var9.hasNext()) {
                            tag = (AbstractTagModel)var9.next();
                            currentEvent.addTag(tag.getPath());
                        }
                    } else {
                        currentGroup = new GroupXmlModel();
                        var9 = ((TaggerSet)tagGroup.getValue()).iterator();

                        while(var9.hasNext()) {
                            tag = (AbstractTagModel)var9.next();
                            currentGroup.addTag(tag.getPath());
                        }

                        currentEvent.addGroup(currentGroup);
                    }
                }
            }
        }

        return eventSetModel;
    }

    public TaggedEvent findGroupInEvent(Set<Integer> groupIds) {
        TaggedEvent foundEvent = null;
        Iterator var4 = this.eventList.iterator();

        while(var4.hasNext()) {
            TaggedEvent event = (TaggedEvent)var4.next();
            Iterator groupIdIterator = groupIds.iterator();

            while(groupIdIterator.hasNext()) {
                if (event.containsGroup((Integer)groupIdIterator.next())) {
                    foundEvent = event;
                }
            }
        }

        return foundEvent;
    }

    public int findHighlightPosition(Highlight[] highlights, Highlight highlightValue) {
        for(int i = 0; i < highlights.length; ++i) {
            if (highlightValue.equals(highlights[i])) {
                return i;
            }
        }

        return -1;
    }

    public List<EventModel> findMissingRequiredTags() {
        List<EventModel> result = new ArrayList();
        Iterator var3 = this.eventList.iterator();

        while(var3.hasNext()) {
            TaggedEvent event = (TaggedEvent)var3.next();
            Iterator var5 = this.requiredTags.iterator();

            while(var5.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var5.next();
                if (event.getRRValue(tag) == null) {
                    result.add(new EventModel(event, event.getEventLevelId(), tag));
                }
            }
        }

        return result;
    }

    private String[] formatTags(String[] tags) {
        List<String> tagsList = Arrays.asList(tags);

        for(int i = 0; i < tagsList.size(); ++i) {
            if (tagsList.get(i) == null) {
                tagsList.remove(i);
            } else {
                tagsList.set(i, ((String)tagsList.get(i)).trim().replaceAll("^/", ""));
                tagsList.set(i, ((String)tagsList.get(i)).replaceAll("\"", ""));
            }
        }

        return (String[])tagsList.toArray(new String[tagsList.size()]);
    }

    public AbstractTagModel getChildValueTag(AbstractTagModel tagModel) {
        int count = 0;
        AbstractTagModel valueTag = null;
        Iterator var5 = this.tags.iterator();

        while(var5.hasNext()) {
            AbstractTagModel t = (AbstractTagModel)var5.next();
            if (t.getDepth() > tagModel.getDepth() && t.getPath().startsWith(tagModel.getPath() + "/")) {
                ++count;
                if (t.takesValue()) {
                    valueTag = t;
                }
            }
        }

        if (count == 1) {
            return valueTag;
        } else {
            return null;
        }
    }

    public TaggedEvent getEventByGroupId(Integer groupId) {
        Iterator var3 = this.eventList.iterator();

        while(var3.hasNext()) {
            TaggedEvent currentEventModel = (TaggedEvent)var3.next();
            if (currentEventModel.containsGroup(groupId)) {
                return currentEventModel;
            }
        }

        return null;
    }

    private List<AbstractTagModel> getEventCategoryTags(TaggedEvent event) {
        List<AbstractTagModel> categoryTags = new ArrayList();
        Iterator var4 = event.getTagGroups().entrySet().iterator();

        while(true) {
            Entry entry;
            do {
                if (!var4.hasNext()) {
                    return categoryTags;
                }

                entry = (Entry)var4.next();
            } while((Integer)entry.getKey() != event.getEventLevelId());

            Iterator var6 = ((TaggerSet)entry.getValue()).iterator();

            while(var6.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var6.next();
                if (tag.getPath().toLowerCase().startsWith("event/category")) {
                    categoryTags.add(tag);
                }
            }
        }
    }

    private AbstractTagModel getEventDescriptionTag(TaggedEvent event) {
        AbstractTagModel defaultTagModel = this.factory.createAbstractTagModel(this);
        Iterator var4 = event.getTagGroups().entrySet().iterator();

        while(true) {
            Entry entry;
            do {
                if (!var4.hasNext()) {
                    return defaultTagModel;
                }

                entry = (Entry)var4.next();
            } while((Integer)entry.getKey() != event.getEventLevelId());

            Iterator var6 = ((TaggerSet)entry.getValue()).iterator();

            while(var6.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var6.next();
                if (tag.getPath().toLowerCase().startsWith("event/description")) {
                    return tag;
                }
            }
        }
    }

    private AbstractTagModel getEventLabelTag(TaggedEvent event) {
        AbstractTagModel defaultTagModel = this.factory.createAbstractTagModel(this);
        Iterator var4 = event.getTagGroups().entrySet().iterator();

        while(true) {
            Entry entry;
            do {
                if (!var4.hasNext()) {
                    return defaultTagModel;
                }

                entry = (Entry)var4.next();
            } while((Integer)entry.getKey() != event.getEventLevelId());

            Iterator var6 = ((TaggerSet)entry.getValue()).iterator();

            while(var6.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var6.next();
                if (tag.getPath().toLowerCase().startsWith("event/label")) {
                    return tag;
                }
            }
        }
    }

    private AbstractTagModel getEventLongNameTag(TaggedEvent event) {
        AbstractTagModel defaultTagModel = this.factory.createAbstractTagModel(this);
        Iterator var4 = event.getTagGroups().entrySet().iterator();

        while(true) {
            Entry entry;
            do {
                if (!var4.hasNext()) {
                    return defaultTagModel;
                }

                entry = (Entry)var4.next();
            } while((Integer)entry.getKey() != event.getEventLevelId());

            Iterator var6 = ((TaggerSet)entry.getValue()).iterator();

            while(var6.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var6.next();
                if (tag.getPath().toLowerCase().startsWith("event/long name")) {
                    return tag;
                }
            }
        }
    }

    public TaggerSet<TaggedEvent> getEventSet() {
        return this.eventList;
    }

    public AbstractTagModel getExtensionAllowedAncestor(String tagPath) {
        Iterator var3 = this.extensionAllowedTags.iterator();

        AbstractTagModel tag;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            tag = (AbstractTagModel)var3.next();
        } while(tagPath == null || !tagPath.toUpperCase().startsWith(tag.getPath().toUpperCase()));

        return tag;
    }

    /**
     * Get ancestor of given tagPath
     * @param tagPath
     * @return
     */
    public AbstractTagModel getTagAncestor(String tagPath) {
        if (tagPath == null || tagPath.isEmpty()) return null;

        AbstractTagModel ancestor = null;
        for (AbstractTagModel tag : tags) {
            if (tagPath.toUpperCase().startsWith(tag.getPath().toUpperCase()))
                ancestor = tag;
        }
        return ancestor;
    }

    public IFactory getFactory() {
        return this.factory;
    }

    public String getHEDString() {
        StringWriter sw = new StringWriter();
        HedXmlModel hedModel = new HedXmlModel();
        TagXmlModel tagModel = this.tagsToModel();
        hedModel.setTags(tagModel.getTags());

        try {
            JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(hedModel, sw);
        } catch (JAXBException var6) {
            throw new RuntimeException("Unable to marshal HED XML String: " + var6.getMessage());
        }

        return sw.toString();
    }

    public TaggerHistory getHistory() {
        return this.history;
    }

    public String getJSONString() {
        Set<EventJsonModel> eventJsonModels = this.buildEventJsonModels();
        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        try {
            writer.writeValue(sw, eventJsonModels);
        } catch (JsonGenerationException var6) {
            var6.printStackTrace();
            throw new RuntimeException(var6.toString());
        } catch (JsonMappingException var7) {
            var7.printStackTrace();
            throw new RuntimeException(var7.toString());
        } catch (IOException var8) {
            var8.printStackTrace();
            throw new RuntimeException(var8.toString());
        }

        return sw.toString();
    }

    public TaggerSet<AbstractTagModel> getRecommendedTags() {
        return this.recommendedTags;
    }

    public String getRedoMessage() {
        return this.history.getRedoMessage();
    }

    public TaggerSet<AbstractTagModel> getRequiredTags() {
        return this.requiredTags;
    }

    public TaggerSet<GuiTagModel> getSearchTags(String searchTextArg) {
        TaggerSet<GuiTagModel> result = new TaggerSet();
        if (searchTextArg.isEmpty()) {
            return null;
        } else {
            String searchText = searchTextArg.toLowerCase();
            Iterator var5 = this.tags.iterator();

            while(var5.hasNext()) {
                AbstractTagModel tag = (AbstractTagModel)var5.next();
                if (tag.getPath().toLowerCase().indexOf(searchText) != -1) {
                    result.add((GuiTagModel)tag);
                }
            }

            return result;
        }
    }

    public TaggerSet<AbstractTagModel> getSubHierarchy(String baseTagPath) {
        AbstractTagModel atm = this.factory.createAbstractTagModel(this);
        atm.setPath(baseTagPath);
        int startIdx = this.tags.indexOf(atm);
        TaggerSet<AbstractTagModel> result = new TaggerSet();

        for(int i = startIdx; i < this.tags.size(); ++i) {
            AbstractTagModel currentTag = (AbstractTagModel)this.tags.get(i);
            if (!currentTag.getPath().startsWith(baseTagPath)) {
                break;
            }

            result.add(currentTag);
        }

        return result;
    }

    public TaggedEvent getTaggedEventFromGroupId(int groupId) {
        Iterator var3 = this.eventList.iterator();

        while(var3.hasNext()) {
            TaggedEvent tem = (TaggedEvent)var3.next();
            if (tem.containsGroup(groupId)) {
                return tem;
            }
        }

        throw new RuntimeException("Unable to get event from groupid");
    }

    public int getTagLevel() {
        return this.tagLevel;
    }

    public AbstractTagModel getTagModel(String path) {
        AbstractTagModel valueTag = null;
        if (!"~".equals(path)) {
            List<String> pathAsList = splitPath(path);
            if (pathAsList.size() > 0) {
                String parentPath = path.substring(0, path.lastIndexOf(47));
                Iterator var6 = this.tags.iterator();

                while(var6.hasNext()) {
                    AbstractTagModel tagModel = (AbstractTagModel)var6.next();
                    if (tagModel.getPath().equals(path)) {
                        return tagModel;
                    }

                    if (tagModel.takesValue() && tagModel.getParentPath().equals(parentPath) && this.matchTakesValueTag(tagModel.getName(), path.substring(path.lastIndexOf(47)))) {
                        valueTag = tagModel;
                        break;
                    }
                }
            }
        }

        AbstractTagModel extensionAllowedAncestor = this.getExtensionAllowedAncestor(path);
        AbstractTagModel tagModel = this.factory.createAbstractTagModel(this);
        tagModel.setPath(path);
        if (extensionAllowedAncestor == null && valueTag == null) {
            ((GuiTagModel)tagModel).setMissing(true);
        }

        return tagModel;
    }

    public SortedSet<AbstractTagModel> getTagSet() {
        return this.tags;
    }

    public String getUndoMessage() {
        return this.history.getUndoMessage();
    }

    public AbstractTagModel getUniqueKey(AbstractTagModel tag) {
        Iterator var3 = this.uniqueTags.iterator();

        while(var3.hasNext()) {
            AbstractTagModel currentTag = (AbstractTagModel)var3.next();
            String currentPrefix = currentTag.getPath() + "/";
            if (tag.getPath().startsWith(currentPrefix)) {
                return currentTag;
            }
        }

        return null;
    }

    public TaggerSet<AbstractTagModel> getUniqueTags() {
        return this.uniqueTags;
    }

    public String getXmlDataString() {
        StringWriter sw = new StringWriter();
        TaggerDataXmlModel savedDataModel = this.buildSavedDataModel();

        try {
            JAXBContext context = JAXBContext.newInstance(TaggerDataXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(savedDataModel, sw);
        } catch (JAXBException var5) {
            throw new RuntimeException("Unable to marshal XML data: " + var5.getMessage());
        }

        return sw.toString();
    }

    public boolean hasChildTags(AbstractTagModel tagModel) {
        Iterator var3 = this.tags.iterator();

        AbstractTagModel t;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            t = (AbstractTagModel)var3.next();
        } while(t.getDepth() <= tagModel.getDepth() || !t.getPath().startsWith(tagModel.getPath() + "/"));

        return true;
    }

    public boolean hasRRTags() {
        return this.requiredTags.size() > 0 || this.recommendedTags.size() > 0;
    }

    public boolean getHEDExtended() {
        return this.hedExtended;
    }

    public String hedToString() {
        StringWriter sw = new StringWriter();
        HedXmlModel hedModel = new HedXmlModel();
        TagXmlModel tagModel = this.tagsToModel();
        hedModel.setTags(tagModel.getTags());
        hedModel.setUnitClasses(this.unitClassesToXmlModel());
        hedModel.setVersion(this.hedVersion);

        try {
            JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(hedModel, sw);
        } catch (JAXBException var6) {
            System.err.println("Unable to write HED XML data to string:\n" + var6.getMessage());
            return "";
        }

        return sw.toString();
    }

    public boolean isDuplicate(String tagPath, AbstractTagModel tagModel) {
        Iterator var4 = this.tags.iterator();

        AbstractTagModel tag;
        do {
            if (!var4.hasNext()) {
                return false;
            }

            tag = (AbstractTagModel)var4.next();
        } while(!tag.getPath().equals(tagPath) || tag.equals(tagModel));

        return true;
    }

    private boolean isHeaderTag(AbstractTagModel tag) {
        return tag.getPath().toLowerCase().startsWith("event/category") || tag.getPath().toLowerCase().startsWith("event/label") || tag.getPath().toLowerCase().startsWith("event/long name") || tag.getPath().toLowerCase().startsWith("event/description");
    }

    public boolean isPrimary() {
        return this.isPrimary;
    }

    public boolean isRRValue(AbstractTagModel tag) {
        Iterator var3 = this.requiredTags.iterator();

        AbstractTagModel currentTag;
        String currentPrefix;
        while(var3.hasNext()) {
            currentTag = (AbstractTagModel)var3.next();
            currentPrefix = currentTag.getPath() + "/";
            if (tag.getPath().startsWith(currentPrefix)) {
                return true;
            }
        }

        var3 = this.recommendedTags.iterator();

        while(var3.hasNext()) {
            currentTag = (AbstractTagModel)var3.next();
            currentPrefix = currentTag.getPath() + "/";
            if (tag.getPath().startsWith(currentPrefix)) {
                return true;
            }
        }

        return false;
    }

    public boolean isExtensionTag(AbstractTagModel tag) {
        return this.extensionAllowedTags.contains(tag);
    }

    public boolean isNewTag(AbstractTagModel tag) {
        return this.newTags.contains(tag);
    }

    public boolean loadEventsAndHED(File savedData) {
        TaggerDataXmlModel savedDataXmlModel = null;

        try {
            JAXBContext context = JAXBContext.newInstance(TaggerDataXmlModel.class);
            savedDataXmlModel = (TaggerDataXmlModel)context.createUnmarshaller().unmarshal(savedData);
        } catch (Exception var4) {
            System.err.println("Unable to read XML file: " + var4.getMessage());
            return false;
        }

        if (savedDataXmlModel == null) {
            System.err.println("Unable to read XML file - unmarshal returned null");
            return false;
        } else {
            return this.processXmlData(savedDataXmlModel);
        }
    }

    public boolean loadHED(File hedFile) {
        try {
            HedXmlModel hedXmlModel = this.readHEDFile(hedFile);
            this.populateTagList(hedXmlModel);
            return true;
        } catch (Exception var3) {
            System.err.println("Unable to load HED XML:\n" + var3.getMessage());
            return false;
        }
    }

    public boolean loadJSON(File egtFile, File hedFile) {
        this.eventList = new TaggerSet();

        try {
            Set<EventJsonModel> eventJsonModels = this.populateJSONList(egtFile);
            this.populateEventsFromJson(eventJsonModels);
            HedXmlModel hedXmlModel = this.readHEDFile(hedFile);
            this.populateTagList(hedXmlModel);
            return true;
        } catch (Exception var5) {
            System.err.println("Unable to load JSON:\n" + var5.getMessage());
            return false;
        }
    }

    public boolean loadTabDelimited(File egtFile, File hedFile, int header, int[] eventCodeColumn, int[] tagColumns) {
        try {
            BufferedReader egtReader = new BufferedReader(new FileReader(egtFile));
            this.populateEventsFromTabDelimitedText(egtReader, header, eventCodeColumn, tagColumns);
            egtReader.close();
            HedXmlModel hedXmlModel = this.readHEDFile(hedFile);
            this.populateTagList(hedXmlModel);
            return true;
        } catch (Exception var8) {
            System.err.println("Unable to read delimited file: " + egtFile.getPath() + ": " + var8.getMessage());
            return false;
        }
    }

    public boolean loadTabDelimitedEvents(File egtFile, int header, int[] eventCodeColumn, int[] tagColumns) {
        try {
            BufferedReader egtReader = new BufferedReader(new FileReader(egtFile));
            this.populateEventsFromTabDelimitedText(egtReader, header, eventCodeColumn, tagColumns);
            egtReader.close();
            return true;
        } catch (Exception var6) {
            System.err.println("Unable to read delimited file: " + egtFile.getPath() + ": " + var6.getMessage());
            return false;
        }
    }

    private AbstractTagModel matchSubhierarchy(String parentPath, AbstractTagModel tag) {
        TaggerSet<AbstractTagModel> takesValueTags = new TaggerSet();
        Iterator var5 = this.getSubHierarchy(parentPath).iterator();

        AbstractTagModel takesValueTag;
        while(var5.hasNext()) {
            takesValueTag = (AbstractTagModel)var5.next();
            if (takesValueTag.takesValue()) {
                takesValueTags.add(takesValueTag);
            } else if (takesValueTag.getPath().equals(tag.getPath())) {
                return takesValueTag;
            }
        }

        var5 = takesValueTags.iterator();

        while(var5.hasNext()) {
            takesValueTag = (AbstractTagModel)var5.next();
            if (this.matchTakesValueTag(takesValueTag.getName(), tag.getName())) {
                return takesValueTag;
            }
        }

        return null;
    }

    private boolean matchTakesValueTag(String valueString, String tagName) {
        String before = valueString.substring(0, valueString.indexOf(35));
        String after = valueString.substring(valueString.indexOf(35) + 1, valueString.length());
        return tagName.startsWith(before) && tagName.endsWith(after);
    }

    public AbstractTagModel openToClosest(AbstractTagModel tag) {
        List<String> path = splitPath(tag.getPath());
        String currentPath = "";
        AbstractTagModel lastOpened = null;
        int i = 0;

        label48:
        for(int j = 0; i < path.size(); ++j) {
            for(currentPath = currentPath + "/" + (String)path.get(i); j < this.tags.size(); ++j) {
                AbstractTagModel currentTag = (AbstractTagModel)this.tags.get(j);
                if (currentTag.takesValue() && i == path.size() - 1 && currentTag.getParentPath().equals(tag.getParentPath())) {
                    AbstractTagModel match = this.matchSubhierarchy(currentTag.getParentPath(), tag);
                    if (match != null) {
                        ((GuiTagModel)currentTag).setCollapsed(false);
                        lastOpened = match;
                        break label48;
                    }
                }

                if (currentTag.getPath().equals(currentPath)) {
                    if (!currentPath.equals(tag.getPath())) {
                        ((GuiTagModel)currentTag).setCollapsed(false);
                    }

                    lastOpened = currentTag;
                    break;
                }
            }

            ++i;
        }

        this.updateTagHighlights(false);
        this.highlightTag = (GuiTagModel)lastOpened;
        if (this.highlightTag != null) {
            this.previousHighlightType = this.highlightTag.getHighlight();
            if (this.highlightTag.equals(tag)) {
                this.currentHighlightType = Highlight.HIGHLIGHT_MATCH;
            } else if (this.highlightTag.takesValue()) {
                this.currentHighlightType = Highlight.HIGHLIGHT_TAKES_VALUE;
            } else {
                this.currentHighlightType = Highlight.HIGHLIGHT_CLOSE_MATCH;
            }
        }

        return lastOpened;
    }

    private boolean populateEventsFromJson(Set<EventJsonModel> eventJsonModels) {
        TaggerSet<TaggedEvent> taggerSetTemp = new TaggerSet();
        Iterator var4 = eventJsonModels.iterator();

        while(var4.hasNext()) {
            EventJsonModel eventJsonModel = (EventJsonModel)var4.next();
            TaggedEvent taggedEvent = this.createNewEvent(eventJsonModel.getCode());
            if (eventJsonModel.getTags() != null) {
                Iterator var8 = eventJsonModel.getTags().iterator();

                label39:
                while(true) {
                    while(true) {
                        if (!var8.hasNext()) {
                            break label39;
                        }

                        List<String> tagList = (List)var8.next();
                        if (tagList.size() > 1) {
                            int groupId = groupIdCounter++;
                            taggedEvent.addGroup(groupId);
                            Iterator var10 = tagList.iterator();

                            while(var10.hasNext()) {
                                String tag = (String)var10.next();
                                AbstractTagModel tagModel = this.getTagModel(tag);
                                taggedEvent.addTagToGroup(groupId, tagModel);
                            }
                        } else if (tagList.size() == 1) {
                            AbstractTagModel tagModel = this.getTagModel((String)tagList.get(0));
                            taggedEvent.addTag(tagModel);
                        }
                    }
                }
            }

            if (eventJsonModel.getCode() == null || eventJsonModel.getCode().isEmpty()) {
                return false;
            }

            taggerSetTemp.add(taggedEvent);
        }

        this.eventList = taggerSetTemp;
        return true;
    }

    private boolean populateEventsFromTabDelimitedText(BufferedReader egtReader) {
        this.eventList = new TaggerSet();
        String line = null;

        try {
            while(true) {
                do {
                    if ((line = egtReader.readLine()) == null) {
                        return true;
                    }
                } while(line.isEmpty());

                String[] cols = line.split("\\t");
                if (cols.length < 2) {
                    return false;
                }

                TaggedEvent event = this.createNewEvent(cols[0]);
                String[] tags = cols[1].split(",");
                int groupId = event.getEventLevelId();
                String[] var11 = tags;
                int var10 = tags.length;

                for(int var9 = 0; var9 < var10; ++var9) {
                    String tag = var11[var9];
                    if (!tag.isEmpty()) {
                        boolean endGroup = false;
                        tag = tag.trim();
                        if (tag.startsWith("(")) {
                            groupId = groupIdCounter++;
                            event.addGroup(groupId);
                            tag = tag.substring(1);
                        }

                        if (tag.endsWith(")")) {
                            endGroup = true;
                            tag = tag.substring(0, tag.length() - 1);
                        }

                        AbstractTagModel tagModel = this.getTagModel(tag);
                        event.addTagToGroup(groupId, tagModel);
                        if (endGroup) {
                            groupId = event.getEventLevelId();
                        }
                    }
                }

                this.eventList.add(event);
            }
        } catch (IOException var13) {
            return false;
        }
    }

    private boolean populateEventsFromTabDelimitedText(BufferedReader egtReader, int header, int[] eventCodeColumns, int[] tagColumns) {
        TaggerSet<TaggedEvent> taggerSetTemp = new TaggerSet();
        groupIdCounter = 0;
        String line = null;
        int lineCount = 0;

        try {
            while((line = egtReader.readLine()) != null) {
                ++lineCount;
                String[] cols = line.split("\\t");
                if (!line.trim().isEmpty() && lineCount > header) {
                    String eventCode = this.combineColumns(" ", cols, eventCodeColumns).trim();
                    if (!eventCode.isEmpty()) {
                        TaggedEvent event = this.createNewEvent(eventCode);
                        if (tagColumns[0] != 0) {
                            int groupId = event.getEventLevelId();
                            String[] tags = this.formatTags(this.combineColumns(",", cols, tagColumns).split(","));
                            String[] var17 = tags;
                            int var16 = tags.length;

                            for(int var15 = 0; var15 < var16; ++var15) {
                                String tag = var17[var15];
                                if (!tag.trim().isEmpty()) {
                                    boolean endGroup = false;
                                    tag = tag.trim();
                                    if (tag.startsWith("(")) {
                                        groupId = groupIdCounter++;
                                        event.addGroup(groupId);
                                        tag = tag.substring(1);
                                    }

                                    if (tag.endsWith(")")) {
                                        endGroup = true;
                                        tag = tag.substring(0, tag.length() - 1);
                                    }

                                    AbstractTagModel tagModel = this.getTagModel(tag);
                                    event.addTagToGroup(groupId, tagModel);
                                    if (endGroup) {
                                        groupId = event.getEventLevelId();
                                    }
                                }
                            }
                        }

                        taggerSetTemp.add(event);
                    }
                }
            }
        } catch (IOException var19) {
            return false;
        }

        this.eventList = taggerSetTemp;
        return true;
    }

    private boolean populateEventsFromXml(EventSetXmlModel egtSetXmlModel) {
        TaggerSet<TaggedEvent> taggerSetTemp = new TaggerSet();
        groupIdCounter = 0;
        Iterator var4 = egtSetXmlModel.getEventXmlModels().iterator();

        while(var4.hasNext()) {
            EventXmlModel eventXmlModel = (EventXmlModel)var4.next();
            TaggedEvent taggedEvent = this.createNewEvent(eventXmlModel.getCode());
            Iterator var7 = eventXmlModel.getTags().iterator();

            while(var7.hasNext()) {
                String tagPath = (String)var7.next();
                AbstractTagModel tagModel = this.getTagModel(tagPath);
                taggedEvent.addTagToGroup(taggedEvent.getEventLevelId(), tagModel);
            }

            Iterator var14 = eventXmlModel.getGroups().iterator();

            while(var14.hasNext()) {
                GroupXmlModel groupXmlModel = (GroupXmlModel)var14.next();
                int groupId = groupIdCounter++;
                taggedEvent.addGroup(groupId);
                Iterator var10 = groupXmlModel.getTags().iterator();

                while(var10.hasNext()) {
                    String tagPath = (String)var10.next();
                    AbstractTagModel tagModel = this.getTagModel(tagPath);
                    taggedEvent.addTagToGroup(groupId, tagModel);
                }
            }

            if (taggedEvent.getEventModel().getCode() == null || taggedEvent.getEventModel().getCode().isEmpty()) {
                return false;
            }

            taggerSetTemp.add(taggedEvent);
        }

        this.eventList = taggerSetTemp;
        return true;
    }

    private Set<EventJsonModel> populateJSONList(File egtFile) throws Exception {
        Set<EventJsonModel> eventJsonModels = (Set)(new ObjectMapper()).readValue(egtFile, new TypeReference<LinkedHashSet<EventJsonModel>>() {
        });
        return eventJsonModels;
    }

    private void populateTagList(HedXmlModel hedXmlModel) {
        this.requiredTags = new TaggerSet();
        this.recommendedTags = new TaggerSet();
        this.uniqueTags = new TaggerSet();
        this.extensionAllowedTags = new TaggerSet();
        this.tags = new TaggerSet();
        if (!hedXmlModel.getVersion().isEmpty()) {
            this.hedVersion = hedXmlModel.getVersion();
        }

        this.createUnitClassHashMapFromXml(hedXmlModel.getUnitClasses());
        this.createTagSets(hedXmlModel.getTags());
    }

    public String[] getUnitClasses() {
        return (String[])this.unitClasses.keySet().toArray(new String[this.unitClasses.size()]);
    }

    public String getHEDVersion() {
        return this.hedVersion;
    }

    public void setHEDVersion(String hedVersion) {
        this.hedVersion = hedVersion;
    }

    private boolean processXmlData(TaggerDataXmlModel xmlData) {
        boolean succeed = false;
        if (this.populateEventsFromXml(xmlData.getEgtSetXmlModel())) {
            this.tags = new TaggerSet();
            this.requiredTags = new TaggerSet();
            this.recommendedTags = new TaggerSet();
            this.uniqueTags = new TaggerSet();
            this.extensionAllowedTags = new TaggerSet();
            this.createUnitClassHashMapFromXml(xmlData.getHedXmlModel().getUnitClasses());
            this.createTagSets(xmlData.getHedXmlModel().getTags());
            succeed = true;
        }

        return succeed;
    }

    private Set<EventJsonModel> readEventJsonString(String egtString) throws Exception {
        Set<EventJsonModel> eventJsonModels = (Set)(new ObjectMapper()).readValue(egtString, new TypeReference<LinkedHashSet<EventJsonModel>>() {
        });
        return eventJsonModels;
    }

    private HedXmlModel readHEDFile(File hedFile) throws Exception {
        JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
        HedXmlModel hedXmlModel = (HedXmlModel)context.createUnmarshaller().unmarshal(hedFile);
        return hedXmlModel;
    }

    private HedXmlModel readHEDString(String hedXmlString) throws Exception {
        StringReader hedStringReader = new StringReader(hedXmlString);
        JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
        HedXmlModel hedXmlModel = (HedXmlModel)context.createUnmarshaller().unmarshal(hedStringReader);
        return hedXmlModel;
    }

    public HistoryItem redo() {
        return this.history.redo();
    }

    public void removeEvent(TaggedEvent event) {
        int index = this.eventList.indexOf(event);
        if (this.removeEventBase(event)) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.eventModelPosition = index;
            historyItem.type = Type.EVENT_REMOVED;
            historyItem.event = event;
            this.history.add(historyItem);
        }

    }

    public boolean removeEventBase(TaggedEvent eventModel) {
        return this.eventList.remove(eventModel);
    }

    public void removeGroup(int groupId) {
        TaggedEvent taggedEvent = this.getTaggedEventFromGroupId(groupId);
        TaggerSet<AbstractTagModel> tagsRemoved = this.removeGroupBase(taggedEvent, groupId);
        if (tagsRemoved != null) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.GROUP_REMOVED;
            historyItem.event = taggedEvent;
            historyItem.groupId = groupId;
            historyItem.tags = tagsRemoved;
            this.history.add(historyItem);
        }

    }

    public TaggerSet<AbstractTagModel> removeGroupBase(TaggedEvent event, Integer groupId) {
        return event.removeGroup(groupId);
    }

    public void restoreLists() {
        this.eventList.addAll(this.backupEventList);
        this.tags.addAll(this.backupTagList);
        this.backupEventList.clear();
        this.backupTagList.clear();
        HistoryItem historyItem = new HistoryItem();
        historyItem.type = Type.CLEAR;
        this.history.add(historyItem);
    }

    public boolean save(File egtFile, File hedFile, boolean json) {
        if (json) {
            Set<EventJsonModel> eventJsonModels = this.buildEventJsonModels();
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

            try {
                FileWriter fw = new FileWriter(egtFile);
                writer.writeValue(fw, eventJsonModels);
            } catch (Exception var10) {
                System.err.println("Unable to save event JSON data to file " + egtFile.getPath() + ": " + var10.getMessage());
                return false;
            }
        } else {
            try {
                BufferedWriter egtWriter = new BufferedWriter(new FileWriter(egtFile));
                this.writeTSVFile(egtWriter);
                egtWriter.close();
            } catch (IOException var9) {
                System.err.println("Error writing tab-delimited text to file: " + var9.getMessage());
                return false;
            }
        }

        HedXmlModel hedModel = new HedXmlModel();
        TagXmlModel tagModel = this.tagsToModel();
        hedModel.setTags(tagModel.getTags());
        hedModel.setUnitClasses(this.unitClassesToXmlModel());

        try {
            JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(hedModel, hedFile);
            return true;
        } catch (JAXBException var8) {
            System.err.println("Unable to save HED XML data to file " + hedFile.getPath() + ": " + var8.getMessage());
            return false;
        }
    }

    public boolean saveEventsAndHED(File savedData) {
        TaggerDataXmlModel savedDataModel = this.buildSavedDataModel();

        try {
            JAXBContext context = JAXBContext.newInstance(TaggerDataXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(savedDataModel, savedData);
            return true;
        } catch (JAXBException var5) {
            System.err.println("Unable to save to file " + savedData.getPath() + ": " + var5.getMessage());
            return false;
        }
    }

    public boolean saveHED(File hedFile) {
        HedXmlModel hedModel = new HedXmlModel();
        TagXmlModel tagModel = this.tagsToModel();
        hedModel.setTags(tagModel.getTags());
        hedModel.setUnitClasses(this.unitClassesToXmlModel());
        hedModel.setVersion(this.hedVersion);

        try {
            JAXBContext context = JAXBContext.newInstance(HedXmlModel.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", true);
            marshaller.marshal(hedModel, hedFile);
            return true;
        } catch (JAXBException var6) {
            System.err.println("Unable to save HED XML data to file " + hedFile.getPath() + ": " + var6.getMessage());
            return false;
        }
    }

    public boolean saveTSVFile(File tsvFile) {
        try {
            BufferedWriter egtWriter = new BufferedWriter(new FileWriter(tsvFile));
            this.writeTSVFile(egtWriter);
            egtWriter.close();
            return true;
        } catch (IOException var3) {
            System.err.println("Error writing tab-delimited text to file: " + var3.getMessage());
            return false;
        }
    }

    public void setChildToPropertyOf() {
        Iterator var2 = this.tags.iterator();

        while(var2.hasNext()) {
            AbstractTagModel tag = (AbstractTagModel)var2.next();
            AbstractTagModel parentTag = this.tagFound(tag.getParentPath());
            if (parentTag != null && PredicateType.PROPERTYOF.equals(parentTag.getPredicateType())) {
                tag.setPredicateType(PredicateType.PROPERTYOF);
            }
        }

    }

    public void setHedExtended(boolean hedExtended) {
        this.hedExtended = hedExtended;
        this.loader.setHEDExtended(hedExtended);
    }

    public void setIsPrimary(boolean isPrimary) {
        this.isPrimary = isPrimary;
    }

    private void sortRRTags() {
        this.requiredTags.sort(new Comparator<AbstractTagModel>() {
            public int compare(AbstractTagModel tag1, AbstractTagModel tag2) {
                int pos1 = tag1.getPosition();
                if (pos1 == -1) {
                    pos1 = Tagger.this.requiredTags.size() + 1;
                }

                int pos2 = tag2.getPosition();
                if (pos2 == -1) {
                    pos2 = Tagger.this.requiredTags.size() + 1;
                }

                if (pos1 == pos2) {
                    return 0;
                } else {
                    return pos1 < pos2 ? -1 : 1;
                }
            }
        });
        this.recommendedTags.sort(new Comparator<AbstractTagModel>() {
            public int compare(AbstractTagModel tag1, AbstractTagModel tag2) {
                int pos1 = tag1.getPosition();
                if (pos1 == -1) {
                    pos1 = Tagger.this.recommendedTags.size() + 1;
                }

                int pos2 = tag2.getPosition();
                if (pos2 == -1) {
                    pos2 = Tagger.this.recommendedTags.size() + 1;
                }

                if (pos1 == pos2) {
                    return 0;
                } else {
                    return pos1 < pos2 ? -1 : 1;
                }
            }
        });
    }

    public AbstractTagModel tagFound(String tagPath) {
        Iterator var3 = this.tags.iterator();

        AbstractTagModel tag;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            tag = (AbstractTagModel)var3.next();
        } while(tagPath == null || !tag.getPath().toUpperCase().equals(tagPath.toUpperCase()));

        return tag;
    }

    public boolean tagPathFound(String tagPath) {
        Iterator var3 = this.tags.iterator();

        while(var3.hasNext()) {
            AbstractTagModel tag = (AbstractTagModel)var3.next();
            if (tag.getPath().toUpperCase().equals(tagPath.toUpperCase())) {
                return true;
            }
        }

        return false;
    }

    private TagXmlModel tagsToModel() {
        Iterator<AbstractTagModel> iter = this.tags.iterator();
        TagXmlModel tagsModel = new TagXmlModel();
        this.tagsToXmlModelHelper(new String(), tagsModel, iter);
        return tagsModel;
    }

    /**
     * Helper method to build hierarchical tag structure recursively.
     */
    private AbstractTagModel tagsToXmlModelHelper(String prefix, TagXmlModel parent, Iterator<AbstractTagModel> iter) {
        if (!iter.hasNext()) {
            return null;
        } else {
            AbstractTagModel next = iter.next();
            while (next != null && next.getPath().startsWith(prefix)) {
                TagXmlModel child = new TagXmlModel();
                child.setName(next.getName());
                child.setDescription(next.getDescription());
                child.setIsNumeric(next.isNumeric());
                child.setChildRequired(next.isChildRequired());
                child.setExtensionAllowed(next.isExtensionAllowed());
                child.setTakesValue(next.takesValue());
                child.setPredicateType(next.getPredicateType().toString());
                child.setRequired(next.isRequired());
                child.setRecommended(next.isRecommended());
                child.setUnique(next.isUnique());
                child.setPosition(next.getPosition());
                child.setUnitClass(next.getUnitClass());
                parent.addChild(child);

                // Process child node and get potential next child node
                next = tagsToXmlModelHelper(next.getPath(), child, iter);
            }

            return next;
        }
    }

    public ToggleTagMessage toggleTag(AbstractTagModel tagModel, Set<Integer> groupIds) {
        AbstractTagModel uniqueKey = this.getUniqueKey(tagModel);
        if (this.loader.checkFlags(2) && uniqueKey == null && !tagModel.isRecommended() && !tagModel.isRequired()) {
            Iterator var6 = groupIds.iterator();

            boolean found;
            do {
                if (!var6.hasNext()) {
                    this.unassociate(tagModel, groupIds);
                    return null;
                }

                Integer groupId = (Integer)var6.next();
                found = false;
                Iterator var8 = this.eventList.iterator();

                while(var8.hasNext()) {
                    TaggedEvent currentTaggedEvent = (TaggedEvent)var8.next();
                    if (currentTaggedEvent.containsTagInGroup(groupId, tagModel)) {
                        found = true;
                        break;
                    }
                }
            } while(found && !"~".equals(tagModel.getName()));

            this.associate(tagModel, groupIds);
            return null;
        } else {
            return this.toggleTagReplacePrefix(tagModel, groupIds, uniqueKey);
        }
    }

    private ToggleTagMessage toggleTagReplacePrefix(AbstractTagModel tagModel, Set<Integer> groupIds, AbstractTagModel uniqueKey) {
        ToggleTagMessage result = new ToggleTagMessage(tagModel, groupIds);
        boolean missingTag = false;
        boolean rrTag = this.isRRValue(tagModel);
        Iterator var8 = groupIds.iterator();

        label70:
        while(var8.hasNext()) {
            Integer groupId = (Integer)var8.next();
            Iterator var10 = this.eventList.iterator();

            while(true) {
                while(true) {
                    TaggedEvent currentEventModel;
                    do {
                        if (!var10.hasNext()) {
                            continue label70;
                        }

                        currentEventModel = (TaggedEvent)var10.next();
                    } while(!currentEventModel.containsGroup(groupId));

                    if (rrTag && groupId != currentEventModel.getEventLevelId()) {
                        result.rrError = true;
                        return result;
                    }

                    AbstractTagModel tagFound = currentEventModel.findTagSharedPath(groupId, tagModel);
                    AbstractTagModel uniqueFound = null;
                    if (uniqueKey != null) {
                        uniqueFound = currentEventModel.findDescendant(groupId, uniqueKey);
                        result.uniqueKey = uniqueKey;
                    }

                    if (tagFound == null && uniqueFound == null) {
                        missingTag = true;
                    } else {
                        if (tagFound != null) {
                            String tagPathFound = tagFound.getPath();
                            if (tagPathFound.compareTo(tagModel.getPath()) > 0) {
                                result.addDescendant(currentEventModel, groupId, tagFound);
                            } else if (tagPathFound.compareTo(tagModel.getPath()) < 0) {
                                result.addAncestor(currentEventModel, groupId, tagFound);
                            }
                        }

                        if (uniqueFound != null && !uniqueFound.getPath().equals(tagModel.getPath()) && uniqueFound != tagFound) {
                            result.addUniqueValue(currentEventModel, groupId, uniqueFound);
                        }
                    }
                }
            }
        }

        if (result.ancestors.size() <= 0 && result.descendants.size() <= 0 && result.uniqueValues.size() <= 0) {
            if (!missingTag && !"~".equals(tagModel.getName())) {
                this.unassociate(tagModel, groupIds);
                return null;
            } else {
                this.associate(tagModel, groupIds);
                return null;
            }
        } else {
            return result;
        }
    }

    public void unassociate(AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.unassociateBase(tagModel, groupIds);
        if (!affectedGroups.isEmpty()) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.UNASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            this.history.add(historyItem);
        }

    }

    public void unassociate(GuiEventModel eventModel, AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.unassociateBase(tagModel, groupIds);
        if (!affectedGroups.isEmpty()) {
            HistoryItem historyItem = new HistoryItem();
            historyItem.type = Type.UNASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            historyItem.eventModel = eventModel;
            this.history.add(historyItem);
        }

    }

    public void unassociate(HistoryItem historyItem, AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = this.unassociateBase(tagModel, groupIds);
        if (!affectedGroups.isEmpty()) {
            historyItem.type = Type.UNASSOCIATED;
            historyItem.groupsIds = affectedGroups;
            historyItem.tagModel = tagModel;
            this.history.add(historyItem);
        }

    }

    public Set<Integer> unassociateBase(AbstractTagModel tagModel, Set<Integer> groupIds) {
        Set<Integer> affectedGroups = new HashSet();
        Iterator var5 = groupIds.iterator();

        while(var5.hasNext()) {
            Integer groupId = (Integer)var5.next();
            Iterator var7 = this.eventList.iterator();

            while(var7.hasNext()) {
                TaggedEvent currentTaggedEvent = (TaggedEvent)var7.next();
                if (currentTaggedEvent.removeTagFromGroup(groupId, tagModel)) {
                    affectedGroups.add(groupId);
                    tagModel.getPath().startsWith("Event/label");
                }
            }
        }

        return affectedGroups;
    }

    public HistoryItem undo() {
        return this.history.undo();
    }

    private UnitClassesXmlModel unitClassesToXmlModel() {
        UnitClassesXmlModel unitClassesXml = new UnitClassesXmlModel();
        Iterator unitClassKeys = this.unitClasses.keySet().iterator();

        while(unitClassKeys.hasNext()) {
            String key = (String)unitClassKeys.next();
            UnitClassXmlModel unitClassXml = new UnitClassXmlModel();
            unitClassXml.setName(key);
            unitClassXml.setUnits((String)this.unitClasses.get(key));
            unitClassXml.setDefault((String)this.unitClassDefaults.get(key));
            unitClassesXml.addUnitClass(unitClassXml);
        }

        return unitClassesXml;
    }

    public void updateMissing(GuiTagModel tag) {
        if ("~".equals(tag.getName())) {
            tag.setMissing(false);
        } else {
            String searchPath = tag.getParentPath();
            Iterator var4 = this.tags.iterator();

            while(var4.hasNext()) {
                AbstractTagModel currentTag = (AbstractTagModel)var4.next();
                if (currentTag.getPath().equals(tag.getPath())) {
                    tag.setMissing(false);
                    return;
                }

                if (currentTag.getPath().equals(searchPath)) {
                    TaggerSet<AbstractTagModel> childTags = this.getSubHierarchy(searchPath);
                    Iterator var7 = childTags.iterator();

                    while(var7.hasNext()) {
                        AbstractTagModel childTag = (AbstractTagModel)var7.next();
                        if (childTag.getPath().equals(tag.getPath())) {
                            tag.setMissing(false);
                            return;
                        }

                        if (childTag.takesValue() && this.matchTakesValueTag(childTag.getName(), tag.getName())) {
                            tag.setMissing(false);
                            return;
                        }
                    }
                }
            }

            tag.setMissing(true);
        }
    }

    public void updateTagHighlights(boolean current) {
        if (this.highlightTag != null) {
            if (current) {
                this.highlightTag.setHighlight(this.currentHighlightType);
            } else {
                this.highlightTag.setHighlight(this.previousHighlightType);
            }
        }

    }

    public void updateTagLists() {
        this.requiredTags = new TaggerSet();
        this.recommendedTags = new TaggerSet();
        this.uniqueTags = new TaggerSet();
        Iterator var2 = this.tags.iterator();

        while(var2.hasNext()) {
            AbstractTagModel tag = (AbstractTagModel)var2.next();
            if (tag.isRequired()) {
                this.requiredTags.add(tag);
            }

            if (tag.isRecommended()) {
                this.recommendedTags.add(tag);
            }

            if (tag.isUnique()) {
                this.uniqueTags.add(tag);
            }
        }

        this.sortRRTags();
    }

    public void updateTagName(AbstractTagModel tagModel, String name) {
        String prefix = tagModel.getPath() + "/";
        String newPath = tagModel.getParentPath() + "/" + name;
        String newPrefix = newPath + "/";
        tagModel.setPath(newPath);

        for(int i = this.tags.indexOf(tagModel) + 1; i < this.tags.size(); ++i) {
            AbstractTagModel currentTag = (AbstractTagModel)this.tags.get(i);
            String currentPath = currentTag.getPath();
            if (!currentPath.startsWith(prefix)) {
                break;
            }

            String updatedPath = currentPath.replaceFirst(prefix, newPrefix);
            currentTag.setPath(updatedPath);
        }

        Iterator var15 = this.eventList.iterator();

        while(var15.hasNext()) {
            TaggedEvent taggedEvent = (TaggedEvent)var15.next();
            Iterator var17 = taggedEvent.getTagGroups().values().iterator();

            while(var17.hasNext()) {
                TaggerSet<AbstractTagModel> tags = (TaggerSet)var17.next();
                Iterator var11 = tags.iterator();

                while(var11.hasNext()) {
                    AbstractTagModel tag = (AbstractTagModel)var11.next();
                    String currentPath = tag.getPath();
                    if (currentPath.startsWith(prefix)) {
                        String updatedPrefix = currentPath.replaceFirst(prefix, newPrefix);
                        tag.setPath(updatedPrefix);
                    }
                }
            }
        }

    }

    private boolean writeEventCategory(BufferedWriter eventWriter, TaggedEvent event) {
        String tagPath = new String();
        List categoryTags = this.getEventCategoryTags(event);

        try {
            if (!categoryTags.isEmpty()) {
                tagPath = ((AbstractTagModel)categoryTags.get(0)).getPath();
                eventWriter.write(((AbstractTagModel)categoryTags.get(0)).getPath());

                for(int i = 1; i < categoryTags.size(); ++i) {
                    tagPath = ((AbstractTagModel)categoryTags.get(i)).getPath();
                    eventWriter.write("," + ((AbstractTagModel)categoryTags.get(i)).getPath());
                }
            }

            eventWriter.write("\t");
            return true;
        } catch (IOException var6) {
            System.err.println("Error writing event category: " + tagPath + var6.getMessage());
            return false;
        }
    }

    private boolean writeEventCode(BufferedWriter eventWriter, TaggedEvent event) {
        try {
            eventWriter.write(event.getEventModel().getCode() + "\t");
            return true;
        } catch (IOException var4) {
            System.err.println("Error writing event code: " + event.getEventModel().getCode() + var4.getMessage());
            return false;
        }
    }

    private boolean writeEventDescription(BufferedWriter eventWriter, TaggedEvent event) {
        AbstractTagModel descriptionTag = this.getEventDescriptionTag(event);

        try {
            if (descriptionTag.getPath() == null) {
                eventWriter.write("\t");
            } else {
                eventWriter.write(descriptionTag.getPath() + "\t");
            }

            return true;
        } catch (IOException var5) {
            System.err.println("Error writing event description: " + descriptionTag.getPath() + var5.getMessage());
            return false;
        }
    }

    private boolean writeEventLabel(BufferedWriter eventWriter, TaggedEvent event) {
        AbstractTagModel labelTag = this.getEventLabelTag(event);

        try {
            if (labelTag.getPath() == null) {
                eventWriter.write("\t");
            } else {
                eventWriter.write(labelTag.getPath() + "\t");
            }

            return true;
        } catch (IOException var5) {
            System.err.println("Error writing event label: " + labelTag.getPath() + var5.getMessage());
            return false;
        }
    }

    private boolean writeEventLevelTags(BufferedWriter eventWriter, Entry<Integer, TaggerSet<AbstractTagModel>> entry) throws IOException {
        boolean previous = false;
        boolean nonHeaderTags = false;
        if (!((TaggerSet)entry.getValue()).isEmpty()) {
            if (!this.isHeaderTag((AbstractTagModel)((TaggerSet)entry.getValue()).get(0))) {
                eventWriter.write(((AbstractTagModel)((TaggerSet)entry.getValue()).get(0)).getPath());
                previous = true;
                nonHeaderTags = true;
            }

            for(int i = 1; i < ((TaggerSet)entry.getValue()).size(); ++i) {
                if (!this.isHeaderTag((AbstractTagModel)((TaggerSet)entry.getValue()).get(i))) {
                    this.writeEventTag(eventWriter, (AbstractTagModel)((TaggerSet)entry.getValue()).get(i), previous);
                    previous = true;
                    nonHeaderTags = true;
                }
            }
        }

        return nonHeaderTags;
    }

    private boolean writeEventLongName(BufferedWriter eventWriter, TaggedEvent event) {
        AbstractTagModel longNameTag = this.getEventLongNameTag(event);

        try {
            if (longNameTag.getPath() == null) {
                eventWriter.write("\t");
            } else {
                eventWriter.write(longNameTag.getPath() + "\t");
            }

            return true;
        } catch (IOException var5) {
            System.err.println("Error writing event long name: " + longNameTag.getPath() + var5.getMessage());
            return false;
        }
    }

    private void writeEventTag(BufferedWriter eventWriter, AbstractTagModel tag, boolean previous) throws IOException {
        String tagPath = tag.getPath();

        try {
            if (previous) {
                eventWriter.append(',');
            }

            eventWriter.write(tag.getPath());
        } catch (IOException var6) {
            System.err.println("Error writing tag: " + tagPath + var6.getMessage());
        }

    }

    private boolean writeGroupTags(BufferedWriter eventWriter, Entry<Integer, TaggerSet<AbstractTagModel>> entry) throws IOException {
        if (!((TaggerSet)entry.getValue()).isEmpty()) {
            eventWriter.append('(');
            eventWriter.write(((AbstractTagModel)((TaggerSet)entry.getValue()).get(0)).getPath());

            for(int i = 1; i < ((TaggerSet)entry.getValue()).size(); ++i) {
                if (!this.isHeaderTag((AbstractTagModel)((TaggerSet)entry.getValue()).get(i))) {
                    this.writeEventTag(eventWriter, (AbstractTagModel)((TaggerSet)entry.getValue()).get(i), true);
                }
            }

            eventWriter.append(')');
        }

        return true;
    }

    private boolean writeHeader(BufferedWriter eventWriter) {
        try {
            eventWriter.write(this.tsvHeader[0]);

            for(int i = 1; i < this.tsvHeader.length; ++i) {
                eventWriter.write("\t" + this.tsvHeader[i]);
            }

            eventWriter.newLine();
            return true;
        } catch (IOException var3) {
            System.err.println("Error writing tab-delimited text: " + var3.getMessage());
            return false;
        }
    }

    private void writeNewLine(BufferedWriter eventWriter, TaggedEvent event) {
        try {
            eventWriter.newLine();
        } catch (IOException var4) {
            System.err.println("Could not write new line for event: " + event.getEventModel().getCode() + var4.getMessage());
        }

    }

    private boolean writeOtherTags(BufferedWriter eventWriter, TaggedEvent event) throws IOException {
        boolean previous = false;
        Iterator var5 = event.getTagGroups().entrySet().iterator();

        while(var5.hasNext()) {
            Entry<Integer, TaggerSet<AbstractTagModel>> entry = (Entry)var5.next();
            if (previous) {
                eventWriter.append(',');
            }

            if ((Integer)entry.getKey() == event.getEventLevelId()) {
                previous = this.writeEventLevelTags(eventWriter, entry);
            } else {
                previous = this.writeGroupTags(eventWriter, entry);
            }
        }

        this.writeNewLine(eventWriter, event);
        return true;
    }

    private boolean writeTSVFile(BufferedWriter eventWriter) throws IOException {
        this.writeHeader(eventWriter);
        Iterator var3 = this.eventList.iterator();

        while(var3.hasNext()) {
            TaggedEvent event = (TaggedEvent)var3.next();
            this.writeEventCode(eventWriter, event);
            this.writeEventCategory(eventWriter, event);
            this.writeEventLabel(eventWriter, event);
            this.writeEventLongName(eventWriter, event);
            this.writeEventDescription(eventWriter, event);
            this.writeOtherTags(eventWriter, event);
        }

        return true;
    }
}
