package com.andrew2070.Empires.entities.Permissions;

public class Tree<T extends TreeNode> {
    private T root;

    public Tree(T root) {
        this.root = root;
    }

    public T getRoot() {
        return root;
    }
}