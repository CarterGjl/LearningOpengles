package com.example.expanded;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExpandedItem<K, V> {

    private boolean expanded;

    private final K group;

    private final List<V> children;

    public ExpandedItem(@NonNull K group, @Nullable List<V> children) {
        this.group = group;
        if (children == null) {
            this.children = new ArrayList<>();
        } else {
            this.children = children;
        }
    }


    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @NonNull
    public List<V> getChildren() {
        return children;
    }

    @NonNull
    public K getGroup() {
        return group;
    }

}
