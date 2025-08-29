/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.util;

import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.lang.StringUtils;

import  static br.jus.pje.nucleo.util.StringUtil.*;


/**
 * Classe utilizada para criptogratia de textos.
 * 
 * @author Geraldo Moraes
 * @version $Revision: 1.3 $
 * 
 * @since 3.0
 */
public class Crypto {
	private SecretKey desKey;

	/**
	 * Cria um novo Crypto.
	 * 
	 * @param key
	 *            a chave que será usada no DES.
	 */
	public Crypto(String key) {
		String strKey = (key == null ? "" : key);
		try {
			KeyGenerator kg = KeyGenerator.getInstance(SignatureAlgorithm.DES.getCodigo(), "SunJCE");
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			sr.setSeed(strKey.getBytes());
			kg.init(sr);
			desKey = kg.generateKey();
		} catch (Exception err) {
			err.printStackTrace(System.err);
			// Nunca deve ocorrer.
		}
	}

	/**
	 * Codifica um texto para o formato DES.
	 * 
	 * @param text
	 *            o texto a ser codificado.
	 * 
	 * @return o texto no formato DES.
	 */
	public String encodeDES(String text) {
		StringBuffer resp = new StringBuffer();
		if (text != null) {
			try {
				byte[] enc = encodeDES(getBytesFromStr(text));
				for (int i = 0; i < enc.length; i++) {
					if ((enc[i] & 0xff) < 0x10) {
						resp.append("0");
					}
					resp.append(Long.toString(enc[i] & 0xff, 16));
				}
			} catch (Exception err) {
				err.printStackTrace(System.err);
				// Nunca deve ocorrer.
			}
		}
		return resp.toString();
	}
	
	public byte[] encodeDES(byte[] msg) {
		try {
			Cipher cipher = Cipher.getInstance(SignatureAlgorithm.DES.getCodigo(), "SunJCE");
			cipher.init(Cipher.ENCRYPT_MODE, desKey);
			return cipher.doFinal(msg);
		} catch (Exception err) {
			err.printStackTrace(System.err);
			// Nunca deve ocorrer.
		}
		
		return msg;
	}

	/**
	 * Decodifica um texto no formato DES.
	 * 
	 * @param text
	 *            o texto no formato DES.
	 * 
	 * @return o texto decodificado.
	 */
	public String decodeDES(String text) {
		if (text == null) {
			return "";
		}
		try {
			int size = text.length() / 2;
			byte[] msg = new byte[size];
			for (int i = 0; i < (size * 2); i = i + 2) {
				String hex = text.substring(i, i + 2);
				msg[i / 2] = (byte) (Integer.parseInt(hex, 16));
			}
			byte[] dec = decodeDES(msg);
			return new String(dec).trim();
		} catch (Exception err) {
			System.err.println(err.getMessage() + " in password \"" + text + "\"");
		}
		return "";
	}
	
	public byte[] decodeDES(byte[] msg) {
		try {
			Cipher cipher = Cipher.getInstance(SignatureAlgorithm.DES.getCodigo(), "SunJCE");
			AlgorithmParameters algParams = cipher.getParameters();
			cipher.init(Cipher.DECRYPT_MODE, desKey, algParams);
			return cipher.doFinal(msg);
		} catch (Exception err) {
			System.err.println(err.getMessage());
		}
		return msg;
	}

	/**
	 * Codifica um texto para o formato MD5.
	 * 
	 * @param text
	 *            o texto a ser codificado.
	 * 
	 * @return o texto no formato MD5.
	 */
	public static String encodeMD5(String text) {
		return encodeMD5(getBytesFromStr(text));
	}

	/**
	 * Codifica um texto para o formato MD5.
	 *
	 * 
	 * @return o texto no formato MD5.
	 */
	public static String encodeMD5(byte[] bytes) {
        return encode(bytes, Type.MD5);
    }
	

	/**
	 * Codifica um texto para o formato SHA-256.
	 * 
	 * @param text
	 *            o texto a ser codificado.
	 * 
	 * @return o texto no formato SHA-256.
	 */
	public static String encodeSHA256(String text) {
		return encodeSHA256(getBytesFromStr(text));
	}
	
	/**
	 * Codifica um texto para o formato SHA-256.
	 *
	 * 
	 * @return o texto no formato SHA-256.
	 */
	public static String encodeSHA256(byte[] bytes) {
		return encode(bytes, Type.SHA_256);
	}

	public static String encodeSHA1(byte[] bytes) {
		return encode(bytes, Type.SHA1);
	}

	public static String encode(byte[] bytes, String typeStr) {
		Type type = Type.valueOf(typeStr);
		return encode(bytes, type);
	}
	
    public static String encode(String string, Type type) {
    	return encode(getBytesFromStr(string), type);
    }
    
	public static String encode(byte[] bytes, Type type) {
		StringBuilder resp = new StringBuilder();
		if (bytes != null) {
			try {
				MessageDigest digest = MessageDigest.getInstance(type.getCodigo());
				byte[] hash = digest.digest(bytes);
				for (int i = 0; i < hash.length; i++) {
					if ((hash[i] & 0xff) < 0x10) {
						resp.append("0");
					}
					resp.append(Long.toString(hash[i] & 0xff, 16));
				}
			} catch (NoSuchAlgorithmException err) {
				err.printStackTrace(System.err);
				// Nunca deve ocorrer.
			}
		}
		return resp.toString();
	}
	
	public static String encodeMD5(String text, Boolean validarNulo){
    	if(validarNulo){
    		if(text == null){
    			text = "";
    		}
    	}
    	return encodeMD5(text);
    }
	
	public enum Type {
		MD5("MD5"), SHA1("SHA1"), SHA_256("SHA-256"), SHA_512("SHA-512");
		
		Type(String codigo) {
			this.codigo = codigo;
		}
		
		private String codigo;
		
		public String getCodigo() {
			return codigo;
		}
	}
	
	public enum SignatureAlgorithm {
		DES("DES"), RSA("RSA");
		
		SignatureAlgorithm(String codigo) {
			this.codigo = codigo;
		}
		
		private String codigo;
		
		public String getCodigo() {
			return codigo;
		}
	}
	
	/**
	 * Retorna true se o hash MD5 passado por parâmetro é o mesmo do hash MD5 gerado através do 
	 * array de bytes.
	 * 
	 * @param bytes
	 * @param md5
	 * @return booleano
	 */
	public static boolean isMD5Valido(byte[] bytes, String md5) {
		boolean isvalid = false;
		if (bytes==null){
			isvalid = true;
		}else if(StringUtils.isBlank(md5)){
			isvalid = false;
		}else if(StringUtils.equals(encodeMD5(bytes), md5)){
			isvalid = true;
		}
		return  isvalid;
	}
}