package com.smartx.core.syncmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.smartx.block.Block;
import com.smartx.crypto.Sha256;
import com.smartx.mine.MineHelper;

public class MerkleTree {
    static public class MerkleTreeNode {
        public String Hash;
        public MerkleTreeNode Parent;
        public MerkleTreeNode LeftChild;
        public MerkleTreeNode RightChild;
        public MerkleTreeNode(String str) {
            Hash = str;
        }
        public boolean IsLeaf() {
            return LeftChild == null && RightChild == null;
        }
        public boolean IsRoot() {
            return Parent == null;
        }
    }
    private MerkleTreeNode root;
    public int Depth;
    public MerkleTree(Block blc) {
        List<String> hashes = new ArrayList<>();
        hashes.add(blc.ToSignString());
        for (int i = 0; i < blc.Flds.size(); i++) {
            hashes.add(blc.Flds.get(i).hash);
        }
        MerkleTreeNode leaves[] = new MerkleTreeNode[hashes.size()];
        for (int i = 0; i < hashes.size(); i++) {
            leaves[i] = new MerkleTreeNode(hashes.get(i));
        }
        this.root = Build(leaves);
        int depth = 1;
        for (MerkleTreeNode i = root; i.LeftChild != null; i = i.LeftChild)
            depth++;
        this.Depth = depth;
    }
    private static MerkleTreeNode Build(MerkleTreeNode[] leaves) {
        if (leaves.length == 0) return null;
        if (leaves.length == 1) return leaves[0];
        MerkleTreeNode parents[] = new MerkleTreeNode[(leaves.length + 1) / 2];
        for (int i = 0; i < parents.length; i++) {
            parents[i] = new MerkleTreeNode("");
            parents[i].LeftChild = leaves[i * 2];
            leaves[i * 2].Parent = parents[i];
            if (i * 2 + 1 == leaves.length) {
                parents[i].RightChild = parents[i].LeftChild;
            } else {
                parents[i].RightChild = leaves[i * 2 + 1];
                leaves[i * 2 + 1].Parent = parents[i];
            }
            parents[i].Hash = Sha256.getH256(parents[i].LeftChild.Hash + parents[i].RightChild.Hash);
        }
        return Build(parents); //TailCall
    }
    public static String ComputeRoot(List<String> hashes) {
        if (hashes.size() == 0) return null;
        if (hashes.size() == 1) return hashes.get(0);
        MerkleTree tree = new MerkleTree(hashes);
        return tree.root.Hash;
    }
    public MerkleTree(List<String> hashes) {
        if (hashes.size() == 0) return;
        MerkleTreeNode leaves[] = new MerkleTreeNode[hashes.size()];
        for (int i = 0; i < hashes.size(); i++) {
            leaves[i] = new MerkleTreeNode(hashes.get(i));
        }
        this.root = Build(leaves);
        int depth = 1;
        for (MerkleTreeNode i = root; i.LeftChild != null; i = i.LeftChild)
            depth++;
        this.Depth = depth;
    }
    public static String ComputeRoot(Block blc) {
        List<String> hashes = new ArrayList<>();
        hashes.add(blc.header.hash);
        for (int i = 0; i < blc.Flds.size(); i++) {
            hashes.add(blc.Flds.get(i).hash);
        }
        if (hashes.size() == 0) return null;
        if (hashes.size() == 1) return hashes.get(0);
        MerkleTree tree = new MerkleTree(hashes);
        return tree.root.Hash;
    }
    private static void DepthFirstSearch(MerkleTreeNode node, List<String> hashes) {
        if (node.LeftChild == null) {
            // if left is null, then right must be null
            hashes.add(node.Hash);
        } else {
            DepthFirstSearch(node.LeftChild, hashes);
            DepthFirstSearch(node.RightChild, hashes);
        }
    }
    public static void sortNumberString(List<String> hashes) {
        Collections.sort(hashes, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (MineHelper.cmpHashDiff(o1, o2)) {
                    return 1;
                }
                return -1;
            }
        });
    }
    public static void sortNumberBlock(List<Block> hashes) {
        Collections.sort(hashes, new Comparator<Block>() {
            @Override
            public int compare(Block o1, Block o2) {
                if (MineHelper.cmpHashDiff(o1.header.hash, o2.header.hash)) {
                    return 1;
                }
                return -1;
            }
        });
    }
    public static void main(String[] args) {
        Logger log = Logger.getLogger("Smartx");
        List<String> hashes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String str1 = Sha256.getH256(String.valueOf(i));
            hashes.add(str1);
        }
        MerkleTree tree = new MerkleTree(hashes);
        log.info("tree.root.Hash: " + tree.root.Hash);
    }
}


