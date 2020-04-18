package edu.utsa.tagger;

import java.util.ArrayList;
import java.util.List;

public class GroupTree {
    GroupNode root;
    public GroupTree() {
        root = new GroupNode();
    }
    public GroupTree(int eventId) {
        root = new GroupNode(eventId);
    }
    public void setRootId(int id) {
        root.setGroupId(id);
    }
    public GroupNode getRoot() {
        return root;
    }
    public boolean add(int parent, int children) {
        GroupNode nodeToAdd = find(parent);
        if (nodeToAdd == null) {
            return false;
        }
        else {
            nodeToAdd.add(children);
            return true;
        }
    }
    public int remove(int groupId) {
        GroupNode removed = find(groupId);
        GroupNode parentNode = findParent(groupId, root, null);
        boolean success = parentNode.getChildren().remove(removed);
        return success ? parentNode.getGroupId() : -1;
    }

    public GroupNode findParent(int target, GroupNode curNode, GroupNode parent) {
        if (target != root.getGroupId() && parent != null && curNode.getGroupId() == target) {
            return parent;
        }
        else {
            for (GroupNode child : curNode.getChildren()) {
                GroupNode found = findParent(target, child, curNode);
                if (found != null)
                    return found;
            }
            return null;
        }
    }

    public GroupNode find(int groupId) {
        return root.find(groupId);
    }

    @Override
    public String toString() {
        return root.toString();
    }

    public class GroupNode implements Comparable{
        int groupId;
        List<GroupNode> children;
        public GroupNode() {
            children = new ArrayList<>();
        }
        public GroupNode(int id) {
            groupId = id;
            children = new ArrayList<>();
        }
        public int getGroupId() {
            return groupId;
        }
        public void setGroupId(int id) {
            groupId = id;
        }
        public List<GroupNode> getChildren() {
            return children;
        }
        public boolean hasChildren() {
            return !children.isEmpty();
        }
        public GroupNode find(int target) {
            if (groupId == target) {
                return this;
            }
            else {
                for (GroupNode child : children) {
                    GroupNode found = child.find(target);
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
