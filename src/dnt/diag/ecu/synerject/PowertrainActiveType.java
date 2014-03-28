package dnt.diag.ecu.synerject;

public enum PowertrainActiveType {
	Injector(0), //
	IgnitionCoil(1), //
	FuelPump(2);
	
	int value;
	
	PowertrainActiveType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
