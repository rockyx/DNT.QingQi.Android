package dnt.diag.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import dnt.diag.Settings;
import dnt.diag.data.LiveDataItem;
import dnt.diag.data.LiveDataList;
import dnt.diag.data.TroubleCodeItem;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public final class VehicleDB {
	static final char[] hexArray="0123456789ABCDEF".toCharArray();
	static final String DB_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/QingQi/";
	static final String DB_NAME = "sys.db";
	static HashMap<String, String> stringMap;

	private Context context;
	private SQLiteDatabase db;
	private char[] hexChars;
	private StringBuilder queryTextBuilder;

	static {
		stringMap = new HashMap<String, String>();
	}

	private void copyDatabase() throws IOException {
		createDirectory();

		InputStream istream = null;
		OutputStream ostream = null;
		try {
			AssetManager am = context.getAssets();
			istream = am.open(DB_NAME);
			ostream = new FileOutputStream(DB_PATH + DB_NAME);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = istream.read(buffer)) > 0) {
				ostream.write(buffer, 0, length);
			}
		} finally {
			try {
				if (istream != null)
					istream.close();
				if (ostream != null)
					ostream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
	}

	private void createDirectory() {
		File file = new File(DB_PATH);
		if (!file.exists())
			file.mkdir();

		file = new File(DB_PATH + DB_NAME);
		if (file.exists())
			file.delete();
	}

	public VehicleDB(Context context) throws IOException {
		this.context = context;
		copyDatabase();

		db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
				SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		if (db == null) {
			throw new DatabaseException(DB_PATH + DB_NAME);
		}
		
		queryTextBuilder = new StringBuilder(1024);
		hexChars = new char[2048];
	}
	
	private String bytesToHex(byte[] bytes) {
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			hexChars[i * 2] = hexArray[v >> 4];
			hexChars[i * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars, 0, bytes.length * 2);
	}

	private void convertBinaryToString(byte[] bin) {
		queryTextBuilder.append("X'");
		queryTextBuilder.append(bytesToHex(bin));
		queryTextBuilder.append("'");
	}

	public String queryText(String name, String cls) {
		String key = String.format("%s_%s_%s", name, cls, Settings.language);

		if (!stringMap.containsKey(key)) {
			byte[] enName = DBCrypto.encrypt(name);
			byte[] enCls = DBCrypto.encrypt(cls);

			queryTextBuilder.setLength(0);
			queryTextBuilder.append("SELECT ");
			queryTextBuilder.append("Content ");
			queryTextBuilder.append("FROM ");
			queryTextBuilder.append("Text ");
			queryTextBuilder.append("WHERE ");
			queryTextBuilder.append("Name=");
			convertBinaryToString(enName);
			queryTextBuilder.append(" AND Language=");
			convertBinaryToString(DBCrypto.getLanguage());
			queryTextBuilder.append(" AND Class=");
			convertBinaryToString(enCls);

			Cursor cursor = null;

			try {
				cursor = db.rawQuery(queryTextBuilder.toString(), null);
				cursor.moveToFirst();
				byte[] cipherBytes = cursor.getBlob(0);
				stringMap.put(key, DBCrypto.decryptToString(cipherBytes));
			} catch (Exception ex) {
				throw new DatabaseException(String.format(
						"Query text fail name = %s, class = %s", name, cls));
			} finally {
				if (cursor != null)
					cursor.close();
			}
		}
		return stringMap.get(key);
	}

	public byte[] queryCommand(String name, String cls) {
		byte[] enName = DBCrypto.encrypt(name);
		byte[] enCls = DBCrypto.encrypt(cls);

		queryTextBuilder.setLength(0);
		queryTextBuilder.append("SELECT ");
		queryTextBuilder.append("Command ");
		queryTextBuilder.append("FROM ");
		queryTextBuilder.append("Command ");
		queryTextBuilder.append("WHERE ");
		queryTextBuilder.append("Name=");
		convertBinaryToString(enName);
		queryTextBuilder.append(" AND Class=");
		convertBinaryToString(enCls);

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(queryTextBuilder.toString(), null);
			cursor.moveToFirst();
			byte[] cipherBytes = cursor.getBlob(0);
			return DBCrypto.decryptToBytes(cipherBytes);
		} catch (Exception ex) {
			throw new DatabaseException(String.format(
					"Query command fail name = %s, class = %s", name, cls));
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	public TroubleCodeItem queryTroubleCode(String code, String cls) {
		byte[] enCode = DBCrypto.encrypt(code);
		byte[] enCls = DBCrypto.encrypt(cls);

		queryTextBuilder.setLength(0);
		queryTextBuilder.append("SELECT ");
		queryTextBuilder.append("Content, ");
		queryTextBuilder.append("Description ");
		queryTextBuilder.append("FROM ");
		queryTextBuilder.append("TroubleCode ");
		queryTextBuilder.append("WHERE ");
		queryTextBuilder.append("Code=");
		convertBinaryToString(enCode);
		queryTextBuilder.append(" AND Language=");
		convertBinaryToString(DBCrypto.getLanguage());
		queryTextBuilder.append(" AND Class=");
		convertBinaryToString(enCls);

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(queryTextBuilder.toString(), null);
			cursor.moveToFirst();
			byte[] cipherContent = cursor.getBlob(0);
			byte[] cipherDescription = cursor.getBlob(1);

			TroubleCodeItem item = new TroubleCodeItem();
			item.setCode(code);
			item.setContent(DBCrypto.decryptToString(cipherContent));
			if (cipherDescription != null)
				item.setDescription(DBCrypto.decryptToString(cipherDescription));
			return item;
		} catch (Exception ex) {
			throw new DatabaseException(String.format(
					"Query trouble code fail code = %s, class = %s", code, cls));
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	public LiveDataList queryLiveData(String cls) {
		byte[] enCls = DBCrypto.encrypt(cls);

		queryTextBuilder.setLength(0);
		queryTextBuilder.append("SELECT ");
		queryTextBuilder.append("ShortName, ");
		queryTextBuilder.append("Content, ");
		queryTextBuilder.append("Unit, ");
		queryTextBuilder.append("DefaultValue, ");
		queryTextBuilder.append("CommandName, ");
		queryTextBuilder.append("CommandClass, ");
		queryTextBuilder.append("Description, ");
		queryTextBuilder.append("[Index] ");
		queryTextBuilder.append("FROM ");
		queryTextBuilder.append("LiveData ");
		queryTextBuilder.append("WHERE ");
		queryTextBuilder.append("Language=");
		convertBinaryToString(DBCrypto.getLanguage());
		queryTextBuilder.append(" AND ");
		queryTextBuilder.append("Class=");
		convertBinaryToString(enCls);

		Cursor cursor = null;

		try {
			cursor = db.rawQuery(queryTextBuilder.toString(), null);
			if (cursor.getCount() == 0)
				throw new Exception();

			LiveDataList ret = new LiveDataList();
			while (cursor.moveToNext()) {
				byte[] cipherShortName = cursor.getBlob(0);
				byte[] cipherContent = cursor.getBlob(1);
				byte[] cipherUnit = cursor.getBlob(2);
				byte[] cipherDefaultValue = cursor.getBlob(3);
				byte[] cipherCommandName = cursor.getBlob(4);
				byte[] cipherCommandClass = cursor.getBlob(5);
				byte[] cipherDescription = cursor.getBlob(6);
				byte[] cipherIndex = cursor.getBlob(7);

				if ((cipherShortName == null) || (cipherContent == null)
						|| (cipherIndex == null))
					throw new Exception();

				LiveDataItem item = new LiveDataItem();
				item.setShortName(DBCrypto.decryptToString(cipherShortName));
				item.setContent(DBCrypto.decryptToString(cipherContent));

				if (cipherUnit != null)
					item.setUnit(DBCrypto.decryptToString(cipherUnit));
				if (cipherDefaultValue != null) {
					item.setDefaultValue(DBCrypto
							.decryptToString(cipherDefaultValue));
				}

				if ((cipherCommandClass != null) && (cipherCommandName != null)) {
					item.setCmdName(cipherCommandName == null ? "" : DBCrypto
							.decryptToString(cipherCommandName));
					item.setCmdClass(cipherCommandClass == null ? "" : DBCrypto
							.decryptToString(cipherCommandClass));
					byte[] command = queryCommand(item.getCmdName(),
							item.getCmdClass());
					item.setCommand(command);
				}

				if (cipherDescription != null)
					item.setDescription(DBCrypto
							.decryptToString(cipherDescription));
				byte[] indexArray = DBCrypto.decryptToBytes(cipherIndex);
				int index = (indexArray[3] & 0xFF) << 24;
				index |= (indexArray[2] & 0xFF) << 16;
				index |= (indexArray[1] & 0xFF) << 8;
				index |= indexArray[0] & 0xFF;

				item.setIndexForSort(index);

				ret.add(item);
			}

			return ret;
		} catch (Exception ex) {
			throw new DatabaseException(String.format(
					"Query live data fail class = %s", cls));
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}
}
