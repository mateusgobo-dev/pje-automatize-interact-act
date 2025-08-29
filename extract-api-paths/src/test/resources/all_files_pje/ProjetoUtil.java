package br.com.infox.cliente.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;

import javax.activation.DataHandler;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.util.Strings;

import com.sun.istack.ByteArrayDataSource;

import br.com.infox.performance.ObjectConversationCache;
import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Operator;


@Name(ProjetoUtil.NAME)
@Scope(ScopeType.APPLICATION)
@Install(dependencies =  {
		ParametroUtil.NAME}
	)
@Startup(depends = ParametroUtil.NAME)
public class ProjetoUtil {

    public static final String NAME = "projetoUtil";

	/**
	 * Converte um DataHandler para array de bytes.
	 * @param dh
	 *            DataHandler
	 * @return array de bytes.
	 */
	public static byte[] converterParaBytes(DataHandler dh) {
		byte[] resultado = null;
		
		if (dh != null) {
			ObjectConversationCache cache = getObjectConversationCache();
			
			if (!cache.isCache(dh.hashCode())) {
				try {
					InputStream is = dh.getInputStream();
					resultado = ProjetoUtil.converterParaBytes(is);
					cache.put(dh.hashCode(), resultado);
				} catch (IOException e) {}
			}

			resultado = cache.get(dh.hashCode());
		}
		return resultado;
	}
	
	/**
	 * Converte um InputStream para array de bytes.
	 * @param is
	 *            InputStream
	 * @return array de bytes.
	 */
	public static byte[] converterParaBytes(InputStream is) {
		byte[] resultado = null;
		try {
			resultado = IOUtils.toByteArray(is);
		} catch (IOException e) {
		}

		return resultado;
	}
	
	/**
	 * Converte um array de bytes em um DataHandler.
	 * @param bytes Array de bytes.
	 * @return DataHandler.
	 */
	public static DataHandler converterParaDataHandler(byte[] bytes) {
		MimeUtilChecker checker = new MimeUtilChecker();
		String mimeType = checker.getMimeType(bytes);
		return converterParaDataHandler(bytes, mimeType);
	}
	
	/**
	 * Converte um array de bytes em um DataHandler.
	 * 
	 * @param bytes Array de bytes.
	 * @param mimeType Mime type do tipo do dado.
	 * @return DataHandler.
	 */
	public static DataHandler converterParaDataHandler(byte[] bytes, String mimeType) {
		DataHandler resultado = null;
		
		if (bytes != null && mimeType != null) {
			resultado = new DataHandler(new ByteArrayDataSource(bytes, mimeType));
		}
			
		return resultado;
	}
	
	/**
	 * Retorna a quantidade de bytes do DataHandler.
	 * 
	 * @param dh DataHandler
	 * @return quantidade de bytes.
	 */
	public static int getTamanho(DataHandler dh) {
		int resultado = 0;
		byte[] bytes = converterParaBytes(dh);
		if (bytes != null) {
			resultado = bytes.length;
		}
		return resultado;
	}
	
	/**
	 * @param colecao
	 * @return true se a coleção for vazia.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isVazio(Collection colecao) {
		return (colecao == null || colecao.isEmpty());
	}
	
	/**
	 * Verifica se o argumento colecao não é nulo e possui pelo menos um registro.
	 * 
	 * @param colecao {@link java.util.Collection}
	 * @return Verdadeiro, caso o argumento coleção não seja nulo e possua pelo menos um registro. 
	 * Falso, caso contrário.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isNotVazioSize(Collection colecao) {
		return isNotVazio(colecao) && colecao.size() > 0;
	}
	
	/**
	 * Retorna true se a(s) string(s) não tiver(em) vazia(s).
	 * 
	 * @param strings Strings
	 * @return true se a(s) string(s) tiver(em) vazia(s).
	 */
	public static boolean isNaoVazio(String... strings) {
		boolean res = false;

		if (strings != null) {
			res = true;
			for (int idx = 0; idx < strings.length && (res == true); idx++) {
				String string = strings[idx];
				res = (string != null && !string.trim().equals(""));
			}
		}
		return res;
	}
	
	/**
	 * @param colecao
	 * @return true se a coleção não for vazia.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isNotVazio(Collection colecao) {
		return !isVazio(colecao);
	}
	
	/**
	 * Retorna o primeiro valor não vazio.
	 * 
	 * @param values Strings
	 * @return String
	 */
	public static String primeiroNaoVazio(String... values) {
        if (values != null) {
            for (String val : values) {
                if (StringUtils.isNotBlank(val)) {
                    return val;
                }
            }
        }
        return null;
    }

	
	public static void downloadDocumento(String nome, String contentType, byte[] bytes) throws Exception {
		
		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			response.setContentType(contentType);
			response.setContentLength(bytes.length);
			response.setHeader("Content-disposition", "attachment; filename=\"" + nome + "\"");
			
			ServletOutputStream out = response.getOutputStream();
			
			out.write(bytes);
			out.flush();
			
			facesContext.responseComplete();
		}
		catch (Exception e) {
			throw new Exception("Erro ao gerar o download do arquivo, mensagem interna: " + e.getMessage());
		}
	}
	
	/**
	 * Retorna o tamanho da coleção.
	 * 
	 * @param colecao Coleção.
	 * @return tamanho da coleção.
	 */
	public static Integer getTamanho(Collection<?> colecao) {
		return (colecao != null ? colecao.size(): 0);
	}

	/**
	 * Retorna ObjectConversationCache.
	 * 
	 * @return Novo ObjectConversationCache.
	 */
	private static ObjectConversationCache getObjectConversationCache() {
		ObjectConversationCache cache = null;
		if (Contexts.isConversationContextActive()) {
			cache = ObjectConversationCache.instance();
		} else {
			cache = new ObjectConversationCache();
		}
		
		return cache;
	}
	
	public static String getChaveCriptografica() {
		String chaveCriptografica = ParametroUtil.instance().getChaveCriptografiaSimetrica();
		if (Strings.isEmpty(chaveCriptografica)){
			chaveCriptografica = Constantes.CHAVE_PADRAO_CRIPTOGRAFIA;
		}
		return chaveCriptografica;
	}
	
	/**
	 * Compara dois valores de variáveis.
	 * 
	 * @param given o valor recebido
	 * @param expected o valor esperado
	 * @return true, se os dois objetos forem iguais ({@link Object#equals(Object)}) ou se puderem ser convertidos para booleanos e seus valores forem iguais
	 */
	public static boolean compareObjects(Object given, Object expected, Operator op){
		if(given == null || expected == null){
			return false;
		}
		if((op == null || op.equals(Operator.equals) || op.equals(Operator.lessOrEquals) || op.equals(Operator.greaterOrEquals)) 
			&& (given.equals(expected)
				|| (given instanceof Boolean && expected instanceof String && ((String) expected).equalsIgnoreCase(((Boolean) given).toString()))
				|| (expected instanceof Boolean && given instanceof String && ((String) given).equalsIgnoreCase(((Boolean) expected).toString()))
				)){
			return true;
		}
		if((op.equals(Operator.less) || op.equals(Operator.lessOrEquals)) &&
				((given instanceof Integer && expected instanceof Integer
				&& ((Integer) given).compareTo((Integer) expected) <= 0)
				|| 
				(given instanceof Date && expected instanceof Date
						&& ((Date) given).compareTo((Date) expected) <= 0)
				|| 
				(given instanceof String && !(given instanceof Date) && expected instanceof Date
						&& (DateUtil.stringToDate((String) given, "yyyy-MM-dd")).compareTo((Date) expected) <= 0)
				)){
			return true;
		}
		if((op.equals(Operator.greater) || op.equals(Operator.greaterOrEquals)) &&
				((given instanceof Integer && expected instanceof Integer
				&& ((Integer) given).compareTo((Integer) expected) >= 0)
				|| 
				(given instanceof Date && expected instanceof Date
						&& ((Date) given).compareTo((Date) expected) >= 0)
				|| 
				(given instanceof String && !(given instanceof Date) && expected instanceof Date
						&& (DateUtil.stringToDate((String) given, "yyyy-MM-dd")).compareTo((Date) expected) >= 0)
				)){
			return true;
		}
		return false;
	}
}