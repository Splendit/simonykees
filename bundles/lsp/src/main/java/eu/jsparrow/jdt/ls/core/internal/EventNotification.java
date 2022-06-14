package eu.jsparrow.jdt.ls.core.internal;

import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EventNotification {

    /**
     * Event type
     */
	@SerializedName("eventType")
	@Expose
	private EventType type;

	/**
	 * Optional data
	 */
	@SerializedName("data")
	@Expose
	private Object data;
        
    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

	public EventNotification withType(EventType type) {
		this.type = type;
		return this;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(Object data) {
		this.data = data;
	}

	public EventNotification withData(Object data) {
		this.data = data;
		return this;
    }

	@Override
	public String toString() {
		return MessageJsonHandler.toString(this);
	}
}
