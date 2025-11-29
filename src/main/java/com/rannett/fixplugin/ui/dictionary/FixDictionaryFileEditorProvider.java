package com.rannett.fixplugin.ui.dictionary;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

    /**
     * File editor provider that replaces XML dictionaries with the structured dictionary viewer.
     */
    public class FixDictionaryFileEditorProvider implements FileEditorProvider, DumbAware {

        @Override
        /**
         * {@inheritDoc}
         */
        public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
            return FixDictionaryDetector.isLikelyFixDictionary(file);
        }

        @Override
        /**
         * {@inheritDoc}
         */
        public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
            return new FixDictionaryFileEditor(project, file);
        }

        @Override
        /**
         * {@inheritDoc}
         */
        public @NotNull String getEditorTypeId() {
            return "fix-dictionary-viewer";
        }

        @Override
        /**
         * {@inheritDoc}
         */
        public @NotNull FileEditorPolicy getPolicy() {
            return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR;
        }
    }
