package br.com.infox.cliente;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;

import br.com.itx.util.ReflectionsUtil;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.enums.SemanaEnum;

@Name("utils")
@Scope(ScopeType.APPLICATION)
public class Util implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String nomeArquivo = getProperty("app.logprefix") + "_" + getCompleteDate(new Date()) + ".log";
	private static final String nomeArquivoErro = "erro_" + getProperty("app.logprefix") + "_"
			+ getCompleteDate(new Date()) + ".log";
	private static final int DIAS_MES = 31;
	private static final CharsetEncoder ASCIIENCODER = Charset.forName("ISO-8859-1").newEncoder();  
	private static final CharsetEncoder UTF8ENCODER = Charset.forName("UTF-8").newEncoder(); 

	public static void limparArquivosLog() {
		/*
		 * Arquivos de log são limpado a cada mês;
		 */
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(getNomearquivo());
			out.write(new String().getBytes("ISO-8859-1"));
			out.flush();
			out.close();

			out = new FileOutputStream(getNomearquivoerro());
			out.write(new String().getBytes("ISO-8859-1"));
			out.flush();
			out.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void escreverNoLog(Object prefix, String texto) {
		if (!texto.trim().equals("")) {
			try {
				byte[] bytes = texto.getBytes("ISO-8859-1");
				texto = new String(bytes);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			String filePath = getFolderPath(getProperty("app.path_log"))
					+ (prefix != null ? prefix.toString() + "_" : "") + getNomearquivo();
			File file = new File(filePath);
			try {
				FileWriter writer = null;
				writer = new FileWriter(file, true);
				writer.append("\n" + texto);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void escreverNoErroLog(Object prefix, String texto) {
		if (!texto.trim().equals("")) {
			try {
				byte[] bytes = texto.getBytes("ISO-8859-1");
				texto = new String(bytes);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			String filePath = getFolderPath(getProperty("app.path_log_erro"))
					+ (prefix != null ? prefix.toString() + "_" : "") + getNomearquivoerro();
			File file = new File(filePath);
			try {
				FileWriter writer = null;
				writer = new FileWriter(file, true);
				writer.append("\n" + texto);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getNomearquivo() {
		return nomeArquivo;
	}

	public static String getNomearquivoerro() {
		return nomeArquivoErro;
	}

	public static String getCompleteDate(Date date) {
		String resultado = date.toString();

		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		resultado = fmt.format(date);// formata a data em um String seguindo a
										// máscara.

		return resultado;
	}

	public static String getProperty(String name) {
		InputStream inStream;
		Properties properties = new Properties();
		inStream = AbstractRemessaProcesso.class.getResourceAsStream("/remessa_processo.properties");
		try {
			properties.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties.getProperty(name);
	}

	public static String getFolderPath(String path) {
		String resultado = path;
		if (!resultado.endsWith("/")) {
			resultado += "/";
		}
		return resultado;
	}

	public static List<SelectItem> mesItems() {
		List<SelectItem> temp = new ArrayList<SelectItem>();
		temp.add(new SelectItem(1, "Janeiro"));
		temp.add(new SelectItem(2, "Fevereiro"));
		temp.add(new SelectItem(3, "Março"));
		temp.add(new SelectItem(4, "Abril"));
		temp.add(new SelectItem(5, "Maio"));
		temp.add(new SelectItem(6, "Junho"));
		temp.add(new SelectItem(7, "Julho"));
		temp.add(new SelectItem(8, "Agosto"));
		temp.add(new SelectItem(9, "Setembro"));
		temp.add(new SelectItem(10, "Outubro"));
		temp.add(new SelectItem(11, "Novembro"));
		temp.add(new SelectItem(12, "Dezembro"));
		return temp;
	}

	public static List<SelectItem> diasItems() {
		List<SelectItem> temp = new ArrayList<SelectItem>();
		temp.add(new SelectItem(null, " "));
		for (int i = 1; i <= DIAS_MES; i++) {
			temp.add(new SelectItem(i, Integer.toString(i)));
		}
		return temp;
	}

	public static Integer diaSemanaInt(SemanaEnum obj) {
		if (obj.getLabel().equals("Domingo")) {
			return 1;
		}
		if (obj.getLabel().equals("Segunda")) {
			return 2;
		}
		if (obj.getLabel().equals("Terça")) {
			return 3;
		}
		if (obj.getLabel().equals("Quarta")) {
			return 4;
		}
		if (obj.getLabel().equals("Quinta")) {
			return 5;
		}
		if (obj.getLabel().equals("Sexta")) {
			return 6;
		}
		if (obj.getLabel().equals("Sábado")) {
			return 7;
		}
		return null;
	}

	public static void setToEventContext(String var, Object object) {
		Contexts.getEventContext().set(var, object);
	}

	/**
	 * PJE-JT: David Vieira: [PJE-779]
	 * Retorna se há algum FacesMessages com Severity.ERROR
	 */
	public boolean hasErrorMessages() {
		List<FacesMessage> currentGlobalMessages = FacesMessages.instance().getCurrentMessages();
		for (FacesMessage facesMessage : currentGlobalMessages) {
			if (facesMessage.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * PJE-JT: Cristiano Nascimento: [PJE-2143, 2157, 2158 e 2164]
	 * Retorna se há algum caracter unicode em uma String, lembrando que os caracteres acentuados e os numéricos não entram nessa definição.
	 */
	public static boolean isStringSemCaracterUnicode(String string){
		
		if (string != null) {
			return ASCIIENCODER.canEncode(string);
		}
		return false;
	} 
	
	/**
	 * PJEII-3257 : Fernando Barreira (25/10/2012)
	 * Verifica se a string referencia um caractere especial UTF-8, excetuando acentos. 
	 */
	public static boolean isStringSemCaracterEspecial(String string) {
		if (string != null) {
			return UTF8ENCODER.canEncode(string) && (string.contains("acute;") || string.contains("grave;") || string.contains("tilde;") || 
					string.contains("circ;") || string.contains("uml;") || string.contains("cedil;"));
		}
		return false;
	}
	
	
	public static String converterCodigoParaCaracteresEspeciais(String string){
		if(string != null){
			return string .replace("&Ccedil;","Ç")
					      .replace("&ccedil;","ç")
					      .replace("&Aacute;", "Á")
						  .replace("&aacute;", "á")
						  .replace("&Acirc;", "Â")
						  .replace("&acirc;", "â")
						  .replace("&Agrave;", "À")
						  .replace("&agrave;", "à")
					      .replace("&Atilde;","Ã")
					      .replace("&atilde;","ã")
					      .replace("&Eacute;", "É")
						  .replace("&eacute;", "é")
					      .replace("&Ecirc;", "Ê")
						  .replace("&ecirc;", "ê")
						  .replace("&Egrave;", "È")
						  .replace("&egrave;", "è")
						  .replace("&Iacute;", "Í")
						  .replace("&iacute;", "í")
						  .replace("&Igrave;", "Ì")
						  .replace("&igrave;", "ì")
					      .replace("&Oacute;", "Ó")
						  .replace("&oacute;", "ó")
						  .replace("&Ocirc;", "Ô")
						  .replace("&ocirc;", "ô")
						  .replace("&Ograve;", "Ò")
						  .replace("&ograve;", "ò")
					      .replace("&Otilde;","Õ")
					      .replace("&otilde;","õ")
					      .replace("&Uacute;", "Ú") 
						  .replace("&uacute;", "ú");
			
		} else {
			return "";
		}

	}
	
	public static boolean listaContem(String lista, String str){
	    str = Pattern.quote(str);
		String aux = String.format("(\\A%s\\Z|\\A%s\\s*,.*|.*,\\s*%s\\s*,.*|.*,\\s*%s\\Z)", str,str,str,str);
		Pattern p = Pattern.compile(aux);
		return p.matcher(lista).matches();
	}
	
	/**
	 * 		Dada uma lista de números inteiros, retorna uma String com os itens
	 * separados por vírgula ou qualquer outro separador passado por parâmetro.
	 * 		Útil para usar em cláusula SQL "in (1,2,3)")
	 * 
	 * */
	public static String joinList(List<?> list, String separator) {
		String result = "";
		for (Object o : list){
			result += (result.equals("") ? o.toString() : separator + o.toString());			
		}
		return result;
	}
	
	/**
	 * Método responsável por transformar uma cadeia de caracteres no formato <b>99[,99]*</b> em um array de inteiros.
	 * 
	 * @param arg0 Cadeia de caracteres no formato <b>99[,99]*</b>
	 * @return Array de inteiros.
	 */
	public static Integer[] converterStringIdsToIntegerArray(String arg0) {
		final Integer[] NO_IDS = new Integer[0];
		
		if (StringUtils.isNotBlank(arg0)) {
			List<Integer> resultIds = new ArrayList<Integer>(0);
			
			String[] ids = arg0.split(",");
			for (int i = 0; i < ids.length; i++) {
				try {
					resultIds.add(Integer.parseInt(ids[i].trim()));
				} catch (NumberFormatException ex) {
					continue;
				}
			}
			return resultIds.toArray(new Integer[resultIds.size()]);
		}
		return NO_IDS;
	}
	
	/**
     * Verifica se o documento está com valor diferente de nulo ou em branco.
     * @param pd documento que será validado.
     * @return true 
     */
	public static boolean isDocumentoPreenchido(ProcessoDocumento pd) {
		return pd != null && pd.getProcessoDocumentoBin() != null && 
				pd.getProcessoDocumentoBin().getModeloDocumento() != null && 
				StringUtils.isNotBlank(pd.getProcessoDocumentoBin().getModeloDocumento().trim());
	}
	
	/**
	 * verifica se o endereco passado em parametro esta nulo ou se o cep contido 
	 * dentro do endereco esta nulo ou se o numero do cep esta nulo ou vazio.
	 * @param endereco
	 * @return true se endereco é nulo ou se cep é nulo ou se o numero do cep é nulo ou vazio.
	 */
	public static boolean isEnderecoCepNulo(Endereco endereco) {
		return (endereco == null || endereco.getCep() == null || StringUtils.isBlank(endereco.getCep().getNumeroCep()));
	}
	
	/**
	 * Adiciona uma variável de ambiente em Runtime.
	 * 
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public static void setEnv(String key, String value) {

		Map<String, String> map = (Map<String, String>) ReflectionsUtil.getValue(System.getenv(), "m");
		map.put(key, value);
	}
}
