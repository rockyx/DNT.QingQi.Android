package dnt.diag.ecu.synerject;

import dnt.diag.channel.ChannelException;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataList;
import dnt.diag.ecu.ActiveState;
import dnt.diag.ecu.ActiveTestFunction;
import dnt.diag.ecu.DiagException;

public class PowertrainActiveTest extends ActiveTestFunction {

	protected static byte[] activateInjectorOn;
	protected static byte[] activateInjectorOff;
	protected static byte[] activateIgnitionCoilOn;
	protected static byte[] activateIgnitionCoilOff;
	protected static byte[] activateTheFuelPumpOn;
	protected static byte[] activateTheFuelPumpOff;

	static {
		activateInjectorOn = null;
		activateInjectorOff = null;
		activateIgnitionCoilOn = null;
		activateIgnitionCoilOff = null;
		activateTheFuelPumpOn = null;
		activateTheFuelPumpOff = null;
	}

	private Powertrain ecu;
	private PowertrainDataStream ds;

	private void initCommands() {
		if (activateInjectorOn == null) {
			activateInjectorOn = getFormat().pack(
					getDB().queryCommand("Activate Injector On", "Synerject"));
		}

		if (activateInjectorOff == null) {
			activateInjectorOff = getFormat().pack(
					getDB().queryCommand("Activate Injector Off", "Synerject"));
		}

		if (activateIgnitionCoilOn == null) {
			activateIgnitionCoilOn = getFormat().pack(
					getDB().queryCommand("Activate Ignition Coil On",
							"Synerject"));
		}

		if (activateIgnitionCoilOff == null) {
			activateIgnitionCoilOff = getFormat().pack(
					getDB().queryCommand("Activate Ignition Coil Off",
							"Synerject"));
		}

		if (activateTheFuelPumpOn == null) {
			activateTheFuelPumpOn = getFormat().pack(
					getDB().queryCommand("Activate The Fuel Pump On",
							"Synerject"));
		}

		if (activateTheFuelPumpOff == null) {
			activateTheFuelPumpOff = getFormat().pack(
					getDB().queryCommand("Activate The Fuel Pump Off",
							"Synerject"));
		}
	}

	public PowertrainActiveTest(Powertrain ecu) {
		super(ecu.getDB(), ecu.getChannel(), ecu.getFormat());
		this.ecu = ecu;
		this.ds = new PowertrainDataStream(ecu);
		
		this.ds.setBeginCalc(null);
		this.ds.setEndCalc(null);
		this.ds.setBeginRead(null);
		this.ds.setEndRead(null);

		initCommands();
	}

	@Override
	public void execute(int mode) {
		try {

			this.ds.setBeginCalc(null);
			this.ds.setEndCalc(null);
			this.ds.setBeginRead(null);
			this.ds.setEndRead(null);
			
			if (mode == PowertrainActiveType.Injector.getValue()) {
				LiveDataList lds = ds.getLiveDataItems();

				for (LiveDataItem item : lds) {
					item.setEnabled(false);
					item.setDisplay(false);
				}

				lds.get("INJ_MODE").setEnabled(true);
				lds.get("INJ_MODE").setDisplay(true);
				lds.makeDisplayItems();
			} else if (mode == PowertrainActiveType.IgnitionCoil.getValue()) {
				LiveDataList lds = ds.getLiveDataItems();

				for (LiveDataItem item : lds) {
					item.setEnabled(false);
					item.setDisplay(false);
				}

				lds.get("CUR_IGC_DIAG_cyl1").setEnabled(true);
				lds.get("CUR_IGC_DIAG_cyl1").setDisplay(true);
				lds.get("IGA_1").setEnabled(true);
				lds.get("IGA_1").setDisplay(true);
				lds.get("TD_1").setEnabled(true);
				lds.get("TD_1").setDisplay(true);
				lds.makeDisplayItems();
			} else if (mode == PowertrainActiveType.FuelPump.getValue()) {
				LiveDataList lds = ds.getLiveDataItems();

				for (LiveDataItem item : lds) {
					item.setEnabled(false);
					item.setDisplay(false);
				}

				lds.get("STATE_EFP").setEnabled(true);
				lds.get("STATE_EFP").setDisplay(true);
				lds.makeDisplayItems();

			}

			ecu.startCommunication.execute();

			changeState(ActiveState.Idle);

			ActiveState state = ActiveState.Idle;

			byte[] cmd = null;
			byte[] rData = new byte[100];

			while (state != ActiveState.Stop) {
				if (state == ActiveState.Idle) {
					ds.startOnce();
				} else if (state == ActiveState.Positive) {
					changeState(ActiveState.Idle);
					if (mode == PowertrainActiveType.Injector.getValue()) {
						cmd = activateInjectorOn;
					} else if (mode == PowertrainActiveType.IgnitionCoil
							.getValue()) {
						cmd = activateIgnitionCoilOn;
					} else if (mode == PowertrainActiveType.FuelPump.getValue()) {
						cmd = activateTheFuelPumpOn;
					}

					try {
						getChannel().sendAndRecv(cmd, 0, cmd.length, rData);
					} catch (ChannelException e) {
//						changeState(ActiveState.Stop);
//						throw new DiagException(e.getMessage());
					}

					if (rData[0] == 0x7F) {
//						changeState(ActiveState.Stop);
//						throw new DiagException(getDB().queryText(
//								"Active Test Fail", "System"));
					}
				} else if (state == ActiveState.Negative) {
					changeState(ActiveState.Idle);
					if (mode == PowertrainActiveType.Injector.getValue()) {
						cmd = activateInjectorOff;
					} else if (mode == PowertrainActiveType.IgnitionCoil
							.getValue()) {
						cmd = activateIgnitionCoilOff;
					} else if (mode == PowertrainActiveType.FuelPump.getValue()) {
						cmd = activateTheFuelPumpOff;
					}

					try {
						getChannel().sendAndRecv(cmd, 0, cmd.length, rData);
					} catch (ChannelException e) {
						changeState(ActiveState.Stop);
						throw new DiagException(e.getMessage());
					}

					if (rData[0] == 0x7F) {
						changeState(ActiveState.Stop);
						throw new DiagException(getDB().queryText(
								"Active Test Fail", "System"));
					}
				}
				
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}

				state = getState();
			}

			ecu.endCommunication.execute();

		} catch (ChannelException e) {
			throw new DiagException(e.getMessage());
		}
	}

}
