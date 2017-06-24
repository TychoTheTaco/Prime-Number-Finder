package com.tycho.app.primenumberfinder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tycho Bellers
 *         Date Created: 3/2/2017
 */

public class TreeNode{

    /**
     * Tag used for logging and debugging.
     */
    private static final String TAG = "TreeNode";

    private final List<TreeNode> nodes = new ArrayList<>();

    private long value;

    private int highestLevel = 0;

    public TreeNode(long value){
        this.value = value;
    }

    public TreeNode addNode(TreeNode treeNode){
        nodes.add(treeNode);
        return this;
    }

    public List<TreeNode> getNodes(){
        return nodes;
    }

    public long getValue(){
        return value;
    }

    public int countLevels(){
        return getLevels(0) + 1;
    }

    private int getLevels(int level){
       // Log.e(TAG, "Nodes: " + getNodes().size() + " level: " + level);
        if (level > highestLevel) highestLevel = level;
        //Log.e(TAG, "highestLevel is " + highestLevel);
        for (TreeNode treeNode : getNodes()){
            int lv = treeNode.getLevels(level + 1);
            if (lv > highestLevel) highestLevel = lv;
        }
        return highestLevel;
    }
}
