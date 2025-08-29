/**
 * MimetypeUtil.java.
 *
 * Data de criação: 27/08/2014
 */
package br.com.infox.cliente.util;

/**
 * Classe utilitária responsável em prover os recursos necessários para se
 * trabalhar com mimetype's.
 * 
 * @author Adriano Pamplona
 * 
 */
public final class MimetypeUtil {

	public static final String MIME_TYPE_KML = "application/vnd.google-earth.kml+xml";
	/**
	 * Retorna true se o mime for application/pkcs7.
	 * 
	 * @param mime
	 * @return booleano
	 */
	public static Boolean isMimetypePkcs7(String mime) {
		return getMimetypePkcs7().equalsIgnoreCase(mime);
	}

	/**
	 * Retorna true se o mime for application/pdf.
	 * 
	 * @param mime
	 * @return booleano
	 */
	public static Boolean isMimetypePdf(String mime) {
		return getMimetypePdf().equalsIgnoreCase(mime);
	}
	
	/**
	 * Retorna true se o mime for text/html.
	 * 
	 * @param mime
	 * @return booleano
	 */
	public static Boolean isMimetypeHtml(String mime) {
		return getMimetypeHtml().equalsIgnoreCase(mime);
	}

	/**
	 * @return application/pkcs7
	 */
	public static String getMimetypePkcs7() {
		return "application/pkcs7";
	}

	/**
	 * @return application/pdf
	 */
	public static String getMimetypePdf() {
		return "application/pdf";
	}
	
	/**
	 * @return text/html
	 */
	public static String getMimetypeHtml() {
		return "text/html";
	}
	
}
