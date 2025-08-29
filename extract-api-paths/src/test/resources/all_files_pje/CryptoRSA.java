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

import static br.jus.pje.nucleo.util.StringUtil.getBytesFromStr;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

import br.jus.pje.nucleo.util.Crypto.SignatureAlgorithm;

public class CryptoRSA {
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public CryptoRSA() {
		super();
	}

	public void initPrivateKey(String privateKeyStr) {
		byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance(SignatureAlgorithm.RSA.toString());
			privateKey = kf.generatePrivate(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	public void initPublicKey(String publicKeyStr) {
		byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance(SignatureAlgorithm.RSA.toString());
			publicKey = kf.generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	public String sign(String text) {
		return transformText(text, Cipher.ENCRYPT_MODE);
	}

	public byte[] sign(byte[] msg) {
		return transformMessage(msg, Cipher.ENCRYPT_MODE);
	}
	
	public String unsign(String text) {
		return transformText(text, Cipher.DECRYPT_MODE);
	}

	public byte[] unsign(byte[] msg) {
		return transformMessage(msg, Cipher.DECRYPT_MODE);
	}
	
	private String transformText(String text, int opmode) {
		StringBuffer resp = new StringBuffer();
		if (text != null) {
			try {
				byte[] enc = transformMessage(getBytesFromStr(text), opmode);
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
	
	public byte[] transformMessage(byte[] msg, int opmode) {
		try {
			Cipher cipher = Cipher.getInstance(SignatureAlgorithm.RSA.toString());
			Key key = opmode == Cipher.ENCRYPT_MODE ? privateKey : publicKey;
			cipher.init(opmode, key);
			return cipher.doFinal(msg);
		} catch (Exception err) {
			err.printStackTrace(System.err);
			// Nunca deve ocorrer.
		}
		
		return msg;
	}
}