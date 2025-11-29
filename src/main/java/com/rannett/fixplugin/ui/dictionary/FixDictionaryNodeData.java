package com.rannett.fixplugin.ui.dictionary;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Metadata attached to each tree node in the FIX dictionary browser.
 */
public class FixDictionaryNodeData {

    public enum NodeType {
        ROOT,
        SECTION,
        MESSAGE,
        COMPONENT,
        GROUP,
        FIELD
    }

    private final NodeType type;
    private final String displayName;
    private final String name;
    private final String tagNumber;
    private final String fixType;
    private final String messageType;
    private final boolean required;
    private final String description;
    private final String source;
    private final Map<String, String> enums;

    private FixDictionaryNodeData(Builder builder) {
        this.type = builder.type;
        this.displayName = builder.displayName;
        this.name = builder.name;
        this.tagNumber = builder.tagNumber;
        this.fixType = builder.fixType;
        this.messageType = builder.messageType;
        this.required = builder.required;
        this.description = builder.description;
        this.source = builder.source;
        this.enums = builder.enums;
    }

    /**
     * Returns the node type classification.
     *
     * @return node category
     */
    public NodeType getType() {
        return type;
    }

    /**
     * Node label displayed in the tree.
     *
     * @return display label
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Raw name value from the dictionary entry.
     *
     * @return dictionary name
     */
    public String getName() {
        return name;
    }

    /**
     * Tag number for field or group entries.
     *
     * @return numeric tag as a string
     */
    public String getTagNumber() {
        return tagNumber;
    }

    /**
     * FIX data type for a field.
     *
     * @return field type
     */
    public String getFixType() {
        return fixType;
    }

    /**
     * Message type code when applicable.
     *
     * @return message type value
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Indicates if the dictionary element is required.
     *
     * @return {@code true} when required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Detailed description of the entry, if available.
     *
     * @return description text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Path or logical source of the dictionary entry.
     *
     * @return source label
     */
    public String getSource() {
        return source;
    }

    /**
     * Enumerated values for a field keyed by enum value.
     *
     * @return enumeration map
     */
    public Map<String, String> getEnums() {
        return enums;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Builder for {@link FixDictionaryNodeData} instances.
     */
    public static class Builder {
        private NodeType type = NodeType.ROOT;
        private String displayName = "";
        private String name = "";
        private String tagNumber;
        private String fixType;
        private String messageType;
        private boolean required;
        private String description;
        private String source;
        private Map<String, String> enums = new LinkedHashMap<>();

        /**
         * Sets the node type.
         *
         * @param type node category
         * @return builder instance
         */
        public Builder withType(NodeType type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the tree display label.
         *
         * @param displayName label to render
         * @return builder instance
         */
        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        /**
         * Sets the underlying dictionary name.
         *
         * @param name dictionary name
         * @return builder instance
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the FIX tag number.
         *
         * @param tagNumber numeric tag
         * @return builder instance
         */
        public Builder withTagNumber(String tagNumber) {
            this.tagNumber = tagNumber;
            return this;
        }

        /**
         * Sets the FIX data type.
         *
         * @param fixType field type
         * @return builder instance
         */
        public Builder withFixType(String fixType) {
            this.fixType = fixType;
            return this;
        }

        /**
         * Sets the message type code.
         *
         * @param messageType message type value
         * @return builder instance
         */
        public Builder withMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        /**
         * Sets whether the entry is required.
         *
         * @param required required flag
         * @return builder instance
         */
        public Builder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Sets the description text.
         *
         * @param description description value
         * @return builder instance
         */
        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the dictionary source label.
         *
         * @param source source description
         * @return builder instance
         */
        public Builder withSource(String source) {
            this.source = source;
            return this;
        }

        /**
         * Sets the enumerated values.
         *
         * @param enums enumeration map
         * @return builder instance
         */
        public Builder withEnums(Map<String, String> enums) {
            this.enums = enums;
            return this;
        }

        /**
         * Builds the immutable {@link FixDictionaryNodeData} instance.
         *
         * @return constructed node metadata
         */
        public FixDictionaryNodeData build() {
            return new FixDictionaryNodeData(this);
        }
    }
}
