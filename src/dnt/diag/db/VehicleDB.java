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
	static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	static final String DB_NAME = "sys.db";

	HashMap<String, byte[]> encryptMap;
	HashMap<String, String> stringMap;
	HashMap<String, byte[]> commandMap;

	private Context context;
	private SQLiteDatabase db;
	private char[] hexChars;
	private StringBuilder queryTextBuilder;

	private void copyDatabase() throws IOException {
		InputStream istream = null;
		OutputStream ostream = null;
		try {
			AssetManager am = context.getAssets();
			istream = am.open(DB_NAME);
			SQLiteDatabase db = context.openOrCreateDatabase(DB_NAME,
					Context.MODE_PRIVATE, null);
			db.close();
			File path = context.getDatabasePath(DB_NAME);
			ostream = new FileOutputStream(path);
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
			}
		}
	}

	public VehicleDB(Context context) throws IOException {
		this.context = context;
		copyDatabase();

		db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		if (db == null) {
			throw new DatabaseException(DB_NAME);
		}

		encryptMap = new HashMap<String, byte[]>();
		stringMap = new HashMap<String, String>();
		commandMap = new HashMap<String, byte[]>();

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

	private byte[] encrypt(String plain) {
		if (!encryptMap.containsKey(plain)) {
			encryptMap.put(plain, DBCrypto.encrypt(plain));
		}

		return encryptMap.get(plain);
	}

	private byte[] getEncryptLang() {
		return encrypt(Settings.language);
	}

	public String queryText(String name, String cls) {
		String key = String.format("%s_%s_%s", name, cls, Settings.language);

		if (!stringMap.containsKey(key)) {

			queryTextBuilder.setLength(0);
			queryTextBuilder.append("SELECT ");
			queryTextBuilder.append("Content ");
			queryTextBuilder.append("FROM ");
			queryTextBuilder.append("Text ");
			queryTextBuilder.append("WHERE ");
			queryTextBuilder.append("Name=");
			convertBinaryToString(DBCrypto.encrypt(name));
			queryTextBuilder.append(" AND Language=");
			convertBinaryToString(getEncryptLang());
			queryTextBuilder.append(" AND Class=");
			convertBinaryToString(DBCrypto.encrypt(cls));

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

		String key = String.format("%s_%s", name, cls);

		if (!commandMap.containsKey(key)) {
			queryTextBuilder.setLength(0);
			queryTextBuilder.append("SELECT ");
			queryTextBuilder.append("Command ");
			queryTextBuilder.append("FROM ");
			queryTextBuilder.append("Command ");
			queryTextBuilder.append("WHERE ");
			queryTextBuilder.append("Name=");
			convertBinaryToString(DBCrypto.encrypt(name));
			queryTextBuilder.append(" AND Class=");
			convertBinaryToString(DBCrypto.encrypt(cls));

			Cursor cursor = null;
			try {
				cursor = db.rawQuery(queryTextBuilder.toString(), null);
				cursor.moveToFirst();
				byte[] cipherBytes = cursor.getBlob(0);
				commandMap.put(key, DBCrypto.decryptToBytes(cipherBytes));
			} catch (Exception ex) {
				throw new DatabaseException(String.format(
						"Query command fail name = %s, class = %s", name, cls));
			} finally {
				if (cursor != null)
					cursor.close();
			}
		}
		
		return commandMap.get(key);
	}

	public TroubleCodeItem queryTroubleCode(String code, String cls) {

		queryTextBuilder.setLength(0);
		queryTextBuilder.append("SELECT ");
		queryTextBuilder.append("Content, ");
		queryTextBuilder.append("Description ");
		queryTextBuilder.append("FROM ");
		queryTextBuilder.append("TroubleCode ");
		queryTextBuilder.append("WHERE ");
		queryTextBuilder.append("Code=");
		convertBinaryToString(DBCrypto.encrypt(code));
		queryTextBuilder.append(" AND Language=");
		convertBinaryToString(getEncryptLang());
		queryTextBuilder.append(" AND Class=");
		convertBinaryToString(DBCrypto.encrypt(cls));

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
		convertBinaryToString(getEncryptLang());
		queryTextBuilder.append(" AND ");
		queryTextBuilder.append("Class=");
		convertBinaryToString(DBCrypto.encrypt(cls));

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
