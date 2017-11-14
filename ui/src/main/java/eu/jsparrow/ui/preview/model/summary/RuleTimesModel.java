package eu.jsparrow.ui.preview.model.summary;

import org.eclipse.core.databinding.observable.value.WritableValue;

import eu.jsparrow.ui.preview.model.BaseModel;

public class RuleTimesModel extends BaseModel {

	private String name;

	private Integer times;

	private WritableValue<String> timeSaved = new WritableValue<>();

	public String getName() {
		return name;
	}

	public Integer getTimes() {
		return times;
	}

	public String getTimeSaved() {
		return this.timeSaved.getValue();
	}

	public void setTimeSaved(String timeSaved) {
		this.timeSaved.setValue(timeSaved);
	}

	public RuleTimesModel(String name, Integer times, String timeSaved) {
		this.name = name;
		this.times = times;
		setTimeSaved(timeSaved);
	}
}
