package dnt.diag.data;

import java.util.Arrays;

public final class LiveDataItem {
	private String shortName;
	private String content;
	private String unit;
	private String defaultValue;
	private String description;
	private String minValue;
	private String maxValue;
	private String cmdName;
	private String cmdClass;
	private String value;
	private byte[] command;
	private byte[] formattedCommand;
	private LiveDataBuffer ecuResponseBuff;
	private int indexForSort;
	private int position;
	private boolean isEnabled;
	private boolean isDisplay;
	private boolean isOutOfRange;
	private LiveDataItemCalc calc;
	private LiveDataValueChanged valueChanged;

	public LiveDataItem() {
		shortName = "";
		content = "";
		unit = "";
		defaultValue = "";
		description = "";
		minValue = "";
		maxValue = "";
		value = "";
		command = null;
		formattedCommand = null;
		ecuResponseBuff = null;
		indexForSort = -1;
		position = -1;
		isEnabled = false;
		isDisplay = false;
		isOutOfRange = false;
		calc = null;
		valueChanged = null;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		String[] minMax = defaultValue.split("~");
		if (minMax != null && minMax.length == 2) {
			this.minValue = minMax[0];
			this.maxValue = minMax[1];
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCmdName() {
		return cmdName;
	}

	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	public String getCmdClass() {
		return cmdClass;
	}

	public void setCmdClass(String cmdClass) {
		this.cmdClass = cmdClass;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (!this.value.equals(value)) {
			this.value = value;
			if (valueChanged != null)
				valueChanged.OnValueChanged(this);
		}
	}

	public byte[] getCommand() {
		return command;
	}

	public void setCommand(byte[] command) {
		this.command = command;
	}

	public byte[] getFormattedCommand() {
		return formattedCommand;
	}

	public void setFormattedCommand(byte[] formattedCommand) {
		this.formattedCommand = formattedCommand;
	}

	public LiveDataBuffer getEcuResponseBuff() {
		return ecuResponseBuff;
	}

	public void setEcuResponseBuff(LiveDataBuffer ecuResponseBuff) {
		this.ecuResponseBuff = ecuResponseBuff;
	}

	public int getIndexForSort() {
		return indexForSort;
	}

	public void setIndexForSort(int indexForSort) {
		this.indexForSort = indexForSort;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isDisplay() {
		return isDisplay;
	}

	public void setDisplay(boolean isDisplay) {
		this.isDisplay = isDisplay;
	}

	public boolean isOutOfRange() {
		return isOutOfRange;
	}

	public void setOutOfRange(boolean isOutOfRange) {
		this.isOutOfRange = isOutOfRange;
	}

	public String getMinValue() {
		return minValue;
	}

	public String getMaxValue() {
		return maxValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cmdClass == null) ? 0 : cmdClass.hashCode());
		result = prime * result + ((cmdName == null) ? 0 : cmdName.hashCode());
		result = prime * result + Arrays.hashCode(command);
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((ecuResponseBuff == null) ? 0 : ecuResponseBuff.hashCode());
		result = prime * result + Arrays.hashCode(formattedCommand);
		result = prime * result + indexForSort;
		result = prime * result + (isDisplay ? 1231 : 1237);
		result = prime * result + (isEnabled ? 1231 : 1237);
		result = prime * result + (isOutOfRange ? 1231 : 1237);
		result = prime * result
				+ ((maxValue == null) ? 0 : maxValue.hashCode());
		result = prime * result
				+ ((minValue == null) ? 0 : minValue.hashCode());
		result = prime * result
				+ ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiveDataItem other = (LiveDataItem) obj;
		if (cmdClass == null) {
			if (other.cmdClass != null)
				return false;
		} else if (!cmdClass.equals(other.cmdClass))
			return false;
		if (cmdName == null) {
			if (other.cmdName != null)
				return false;
		} else if (!cmdName.equals(other.cmdName))
			return false;
		if (!Arrays.equals(command, other.command))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (ecuResponseBuff == null) {
			if (other.ecuResponseBuff != null)
				return false;
		} else if (!ecuResponseBuff.equals(other.ecuResponseBuff))
			return false;
		if (!Arrays.equals(formattedCommand, other.formattedCommand))
			return false;
		if (indexForSort != other.indexForSort)
			return false;
		if (isDisplay != other.isDisplay)
			return false;
		if (isEnabled != other.isEnabled)
			return false;
		if (isOutOfRange != other.isOutOfRange)
			return false;
		if (maxValue == null) {
			if (other.maxValue != null)
				return false;
		} else if (!maxValue.equals(other.maxValue))
			return false;
		if (minValue == null) {
			if (other.minValue != null)
				return false;
		} else if (!minValue.equals(other.minValue))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public void setCalc(LiveDataItemCalc calc) {
		this.calc = calc;
	}

	public void calcValue() {
		if (calc != null)
			setValue(calc.calc());
	}

	public void setOnValueChanged(LiveDataValueChanged changed) {
		valueChanged = changed;
	}

	public void setPosition(int value) {
		position = value;
	}

	public int getPosition() {
		return position;
	}
}
