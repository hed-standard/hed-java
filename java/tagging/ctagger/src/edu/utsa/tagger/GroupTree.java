package edu.utsa.tagger;

import java.util.ArrayList;
import java.util.List;

public class GroupTree {
    GroupNode root;
    public GroupTree(int eventId) {
        root = new GroupNode(eventId);
    }

    public GroupNode getRoot() {
        return root;
    }
    public boolean add(int parent, int children) {
        GroupNode nodeToAdd = search(parent);
        if (nodeToAdd == null) {
            return false;
        }
        else {
            nodeToAdd.add(children);
            return true;
        }
    }

    public GroupNode search(int groupId) {
        return root.search(groupId);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public class GroupNode implements Comparable{
        int groupId;
        List<GroupNode> children;
        public GroupNode(int id) {
            groupId = id;
            children = new ArrayList<>();
        }
        public int getGroupId() {
            return groupId;
        }
        public List<GroupNode> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }
        public GroupNode search(int target) {
            if (groupId == target) {
                return this;
            }
            else {
                for (GroupNode child : children) {
                    GroupNode found = child.search(target);
                    if (found != null)
                        return found;
                }
                return null;
            }
        }

        public void add(int childId) {
            GroupNode childNode = new GroupNode(childId);
            children.add(childNode);
        }

        @Override
        public int compareTo(Object o) {
            if (((GroupNode)o).getGroupId() == groupId)
                return 0;
            else
                return -1;
        }

        @Override
        public String toString() {
            return toStringPrefix("");
        }
        private String toStringPrefix(String prefix) {
            String str = prefix + groupId;
            for (GroupNode child : children) {
                str += "\n" + child.toStringPrefix(prefix + "\t");
            }
            return str;

        }
    }
}
