package dnt.qingqi;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;

import dnt.diag.commbox.Commbox;
import dnt.diag.db.VehicleDB;
import dnt.diag.ecu.AbstractECU;

public class App extends Application {
	public String QM125T_8H;
	public String QM200J_3L;
	public String QM200GY_F;
	public String QM200_3D;
	public String QM250J_2L;
	public String QM250GY;
	public String QM250T;
	public String QM250J_2LFreeze;
	public String ReadTroubleCode;
	public String ClearTroubleCode;
	public String ReadDataStream;
	public String TPSIdleAdjustment;
	public String ISCLearnValueInitialize;
	public String LongTermLearnValueZoneInitialization;
	public String EcuVersion;
	public String ClearTroubleCodeFinish;
	public String OpenCommboxFail;
	public String ClearingTroubleCode;
	public String ReadingECUVersion;
	public String TPSIdleSettingSuccess;
	public String Communicating;
	public String LongTermLearnValueZoneInitializationSuccess;
	public String ISCLearnValueInitializationSuccess;
	public String ReadCurrentTroubleCode;
	public String ReadHistoryTroubleCode;
	public String DynamicDataStream;
	public String StaticDataStream;
	public String Range;
	public String NoneTroubleCode;
	public String OK;
	public String QM48QT_8;
	public String ActiveTest;
	public String Injector;
	public String IgnitionCoil;
	public String FuelPump;
	public String InjectorOnTest;
	public String InjectorOffTest;
	public String InjectorNotice;
	public String IgnitionCoilOnTest;
	public String IgnitionCoilOffTest;
	public String IgnitionCoilNotice;
	public String FuelPumpOnTest;
	public String FuelPumpOffTest;
	public String FuelPumpNotice;
	public String ReadFreezeFrame;
	public String LoadingPleaseWait;
	public String VehicleSelecting;
	public String DeviceInfo;
	public String TPSIdleLearningValueSetting;
	public String _02F_BLongTermLearningValueReset;
	public String DSVISCLearningValueSetting;
	public String DSVISCLearningValueSettingSuccess;

	private VehicleDB db;
	private Commbox commbox;
	private ProgressDialog status = null;
	private AlertDialog fatal = null;
	private AlertDialog list = null;
	private int which = 0;
	private AbstractECU ecu;

	public void setResources(VehicleDB db, Commbox box) {
		this.db = db;
		commbox = box;

		QM125T_8H = queryText("QM125T-8H", "QingQi");
		QM200J_3L = queryText("QM200J-3L", "QingQi");
		QM200GY_F = queryText("QM200GY-F", "QingQi");
		QM200_3D = queryText("QM200-3D", "QingQi");
		QM250J_2L = queryText("QM250J-2L", "QingQi");
		QM250J_2LFreeze = QM250J_2L + "Freeze";
		QM250GY = queryText("QM250GY", "QingQi");
		QM250T = queryText("QM250T", "QingQi");
		ReadTroubleCode = queryText("Read Trouble Code", "System");
		ClearTroubleCode = queryText("Clear Trouble Code", "System");
		ReadDataStream = queryText("Read Data Stream", "System");
		TPSIdleAdjustment = queryText("TPS Idle Adjustment", "Mikuni");
		ISCLearnValueInitialize = queryText("ISC Learn Value Initialize",
				"Mikuni");
		LongTermLearnValueZoneInitialization = queryText(
				"Long Term Learn Value Zone Initialization", "Mikuni");
		EcuVersion = queryText("ECU Version", "System");
		ClearTroubleCodeFinish = queryText("Clear Trouble Code Finish",
				"System");
		OpenCommboxFail = queryText("Open Commbox Fail", "System");
		ClearingTroubleCode = queryText("Clearing Trouble Code, Please Wait",
				"System");
		ReadingECUVersion = queryText("Reading ECU Version, Please Wait",
				"System");
		TPSIdleSettingSuccess = queryText("TPS Idle Setting Success", "Mikuni");
		Communicating = queryText("Communicating", "System");
		LongTermLearnValueZoneInitializationSuccess = queryText(
				"Long Term Learn Value Zone Initialization Success", "Mikuni");
		ISCLearnValueInitializationSuccess = queryText(
				"ISC Learn Value Initialization Success", "Mikuni");
		ReadCurrentTroubleCode = queryText("Read Current Trouble Code",
				"System");
		ReadHistoryTroubleCode = queryText("Read History Trouble Code",
				"System");
		DynamicDataStream = queryText("Dynamic Data Stream", "System");
		StaticDataStream = queryText("Static Data Stream", "System");
		Range = queryText("Range", "System");
		NoneTroubleCode = queryText("None Trouble Code", "System");
		OK = queryText("OK", "System");
		QM48QT_8 = queryText("QM48QT-8", "QingQi");
		ActiveTest = queryText("Activity Test", "System");
		Injector = queryText("Injector", "System");
		IgnitionCoil = queryText("Ignition Coil", "System");
		FuelPump = queryText("Fuel Pump", "System");
		InjectorOnTest = queryText("Injector On Test", "QingQi");
		InjectorOffTest = queryText("Injector Off Test", "QingQi");
		InjectorNotice = queryText("Injector Notice", "QingQi");
		IgnitionCoilOnTest = queryText("Ignition Coil On Test", "QingQi");
		IgnitionCoilOffTest = queryText("Ignition Coil Off Test", "QingQi");
		IgnitionCoilNotice = queryText("Ignition Coil Notice", "QingQi");
		FuelPumpOnTest = queryText("Fuel Pump On Test", "QingQi");
		FuelPumpOffTest = queryText("Fuel Pump Off Test", "QingQi");
		FuelPumpNotice = queryText("Fuel Pump Notice", "QingQi");
		ReadFreezeFrame = queryText("Read Freeze Frame", "System");
		LoadingPleaseWait = queryText("Loading Please Wait", "System");
		VehicleSelecting = queryText("Vehicle Selecting", "System");
		DeviceInfo = queryText("Device Info", "System");
		TPSIdleLearningValueSetting = queryText(
				"TPS Idle Learning Value Setting", "Mikuni");
		_02F_BLongTermLearningValueReset = queryText(
				"02 F/B Long Term Learning Value Reset", "Mikuni");
		DSVISCLearningValueSetting = queryText(
				"DSV ISC Learning Value Setting", "Mikuni");
		DSVISCLearningValueSettingSuccess = queryText(
				"DSV ISC Learning Value Reset Success", "Mikuni");
	}

	public String queryText(String name, String cls) {
		return db.queryText(name, cls);
	}

	public VehicleDB getDB() {
		return db;
	}

	public void connectCommbox() throws IOException {
		commbox.connect();
	}

	public void disconnectCommbox() throws IOException {
		commbox.disconnect();
	}

	public Commbox getCommbox() {
		return commbox;
	}

	public void setECU(AbstractECU ecu) {
		this.ecu = ecu;
	}

	public AbstractECU getECU() {
		return ecu;
	}

	public void showStatus(Context cxt, String msg) {
		status = new ProgressDialog(cxt);
		status.setMessage(msg);
		status.setCancelable(false);
		status.show();
	}

	public void hideStatus() {
		if (status != null)
			status.dismiss();
	}

	public void showFatal(Context cxt, String msg,
			DialogInterface.OnClickListener listener) {
		fatal = new AlertDialog.Builder(cxt).setMessage(msg)
				.setPositiveButton(OK, listener).create();
		fatal.show();
	}

	public int getWhich() {
		return which;
	}

	public void showList(Context cxt, String[] arrays,
			DialogInterface.OnClickListener listener) {
		which = -1;
		list = new AlertDialog.Builder(new ContextThemeWrapper())
				.setSingleChoiceItems(arrays, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								App.this.which = which;
							}
						}).setPositiveButton(OK, listener).create();
		list.show();
	}
}
