/*
 * In.java
 *
 * Criado em 16 de Outubro de 2006, 15:00
 */
package br.com.infox.cliente;

import java.io.FileInputStream;

import org.omg.CORBA.UserException;

/**
 * Title: EncodeHex<br>
 * Description: Classe que recebe um texto, e codifica esse texto para o formato
 * base64<br>
 * Copyright: Copyright (c) 2006<br>
 * Company: Infox<br>
 * 
 * @author Rodrigo Menezes
 * @version 1.0
 */
public class EncodeBase64 {

	/**
	 * Construtor default da classe EncodeBase64
	 */
	public EncodeBase64() {
	}

	/**
	 * Método que retorna o texto no formato base64
	 * 
	 * @param args
	 *            - vetor de parametros na seguinte ordem:<br>
	 *            1 - texto a ser codificado<br>
	 */
	public String execute(String[] args) throws UserException {
		String saida = null;
		if (args.length == 1) {
			saida = execute(args[0].getBytes());
		}
		if (args.length == 2) {
			if ("true".equals(args[1])) {
				try {
					FileInputStream fis = new FileInputStream(args[0]);
					byte[] bytes = new byte[fis.available()];
					fis.read(bytes);
					fis.close();
					saida = execute(bytes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return saida;
	}

	public static String execute(byte[] b) {
		byte[] cs = org.apache.commons.codec.binary.Base64.encodeBase64(b);
		return new String(cs);
	}

	public static String execute(String s) {
		byte[] cs = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
		return new String(cs);
	}

	public static void main(String[] args) throws Exception {
		String[] teste = { "s:/cretaunificado/WEB-INF/oficiorpv/oficio.pdf", "true" };
		String s = new EncodeBase64().execute(teste);
		System.out.println("String encodada base64: " + s);
	}
}
