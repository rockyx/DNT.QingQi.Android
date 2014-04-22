package dnt.diag.db;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dnt.diag.Settings;

final class DBCrypto {
	static final byte[] AES_CBC_KEY = { (byte) 0xFA, (byte) 0xC2, (byte) 0xCC,
			(byte) 0x82, (byte) 0x8C, (byte) 0xFD, (byte) 0x42, (byte) 0x17,
			(byte) 0xA0, (byte) 0xB2, (byte) 0x97, (byte) 0x4D, (byte) 0x19,
			(byte) 0xC8, (byte) 0xA4, (byte) 0xB1, (byte) 0xF5, (byte) 0x73,
			(byte) 0x23, (byte) 0x7C, (byte) 0xB1, (byte) 0xC4, (byte) 0xC0,
			(byte) 0x38, (byte) 0xC9, (byte) 0x80, (byte) 0xB9, (byte) 0xF7,
			(byte) 0xC3, (byte) 0x3E, (byte) 0xC9, (byte) 0x12 };
	static final byte[] AES_CBC_IV = { (byte) 0x7C, (byte) 0xF4, (byte) 0xF0,
			(byte) 0x7D, (byte) 0x3B, (byte) 0x0D, (byte) 0xA1, (byte) 0xC6,
			(byte) 0x35, (byte) 0x74, (byte) 0x18, (byte) 0xB3, (byte) 0x51,
			(byte) 0xA3, (byte) 0x87, (byte) 0x8E };

	static SecretKey aesKey;
	static IvParameterSpec ips;
	static Cipher decryptCipher;
	static Cipher encryptCipher;

	static {
		try {
			aesKey = new SecretKeySpec(AES_CBC_KEY, "AES");

			ips = new IvParameterSpec(AES_CBC_IV);

			decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			decryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ips);

			encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey, ips);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}

	protected static byte[] decryptToBytes(byte[] cipher)
			throws CryptoException {
		if ((cipher == null) || (cipher.length <= 0))
			throw new IllegalArgumentException("Cipher Text");
		try {
			byte[] plainBytes = decryptCipher.doFinal(cipher);
			return plainBytes;
		} catch (Exception e) {
//			e.printStackTrace();
			throw new CryptoException(e.getMessage());
		}
	}

	protected static String decryptToString(byte[] cipher)
			throws CryptoException {
		try {
			byte[] plainBytes = decryptToBytes(cipher);
			if (plainBytes == null)
				return null;
			return new String(plainBytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
			throw new CryptoException(e.getMessage());
		}
	}

	protected static byte[] encrypt(byte[] plain) throws CryptoException {
		if (plain == null || plain.length <= 0)
			throw new IllegalArgumentException("Plain Bytes");

		try {
			byte[] cipherBytes = encryptCipher.doFinal(plain);
			return cipherBytes;
		} catch (Exception e) {
//			e.printStackTrace();
			throw new CryptoException(e.getMessage());
		}
	}

	protected static byte[] encrypt(String plain) throws CryptoException {
		if (plain == null || plain.length() <= 0)
			throw new IllegalArgumentException("Plain Text");

		try {
			byte[] plainBytes = plain.getBytes("UTF-8");
			return encrypt(plainBytes);

		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
			throw new CryptoException(e.getMessage());
		}
	}
}
