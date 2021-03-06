package org.vitrivr.cineast.core.data.messages.query;

import java.util.Arrays;
import java.util.List;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class Query implements Message {
    /**
     *
     */
    private QueryComponent[] containers;

    /**
     *
     */
    private MediaType[] types;

    /**
     *
     * @param containers
     */
    @JsonCreator
    public Query(@JsonProperty("containers") QueryComponent[] containers, @JsonProperty("types") MediaType[] types) {
        this.containers = containers;
        this.types = types;
    }

    /**
     * Getter for containers.
     *
     * @return
     */
    public List<QueryComponent> getContainers() {
        return Arrays.asList(this.containers);
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.Q_SIM;
    }
}
