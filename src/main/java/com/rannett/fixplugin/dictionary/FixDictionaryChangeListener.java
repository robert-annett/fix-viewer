package com.rannett.fixplugin.dictionary;

import com.intellij.util.messages.Topic;

/**
 * Listener notified when FIX dictionary mappings change for a project.
 */
public interface FixDictionaryChangeListener {

    /**
     * Topic used to broadcast dictionary change events to interested components.
     */
    Topic<FixDictionaryChangeListener> TOPIC = Topic.create(
            "Fix Dictionary Changes",
            FixDictionaryChangeListener.class
    );

    /**
     * Invoked when the active FIX dictionary mappings for the project are updated.
     */
    void onDictionariesChanged();
}
