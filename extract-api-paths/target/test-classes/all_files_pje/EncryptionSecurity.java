package br.com.infox.pje.webservice.consultaoutrasessao;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.jboss.seam.util.Base64;

public final class EncryptionSecurity {
	private static SecretKey skey;
	private static KeySpec ks;
	private static PBEParameterSpec ps;
	private static final String algorithm = "PBEWithMD5AndDES";

	static {
		try {
			SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
			ps = new PBEParameterSpec(new byte[] { 3, 1, 4, 1, 5, 9, 2, 6 }, 20);
			ks = new PBEKeySpec("GEaJf3/m5/WXh".toCharArray());
			skey = skf.generateSecret(ks);
		} catch (java.security.NoSuchAlgorithmException ex) {
			ex.printStackTrace();
		} catch (java.security.spec.InvalidKeySpecException ex) {
			ex.printStackTrace();
		}
	}

	public static final String encrypt(final String text) throws BadPaddingException, NoSuchPaddingException,
			IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {

		final Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, skey, ps);
		return Base64.encodeBytes(cipher.doFinal(text.getBytes()));
	}

	public static final String decrypt(final String text) throws BadPaddingException, NoSuchPaddingException,
			IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException {

		final Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, skey, ps);
		String ret = null;
		try {
			ret = new String(cipher.doFinal(Base64.decode(text)));
		} catch (Exception ex) {
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		String password = "263";
		String encoded = EncryptionSecurity.encrypt(password);
		System.out.println(encoded);
		System.out.println(EncryptionSecurity.decrypt(encoded).equals(password));
		// Vamos alterar um caracter, s¢ para ver o que ocorre
		char[] enc = encoded.toCharArray();
		enc[2] = (char) (enc[2] + 1);
		encoded = new String(enc);
		System.out.println(encoded);
		System.out.println(password.equals(EncryptionSecurity.decrypt(encoded)));
	}
}