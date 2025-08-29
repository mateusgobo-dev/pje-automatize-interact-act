package br.com.itx.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.utils.Constantes;
import br.com.infox.utils.Constantes.URL_DETALHE_PROCESSO;
import br.com.infox.utils.Constantes.URL_GERAL;
import br.com.infox.utils.Constantes.URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE;

public class UrlUtil {

	public static InputStream getInputStreamUrl(String fileAddress) throws IOException {
		URLConnection urlConn = null;
		URL fileUrl = new URL(fileAddress);

		urlConn = fileUrl.openConnection();
		return urlConn.getInputStream();
	}

	public static boolean isUrlValida(String url) throws IOException {
		InputStream inputStreamURL = null;
		inputStreamURL = getInputStreamUrl(url);
		return inputStreamURL != null;
	}
	
	public static InputStream getInputStreamUrl(String fileAddress, int timeout) throws IOException {
		URL fileUrl = new URL(fileAddress);

		URLConnection urlConn = null;
		urlConn = fileUrl.openConnection();
		urlConn.setConnectTimeout(timeout);
        urlConn.setReadTimeout(timeout);
        
		return urlConn.getInputStream();
	}

	public static boolean isUrlValida(String url, int timeout) throws IOException {
		InputStream inputStreamURL = null;
		inputStreamURL = getInputStreamUrl(url, timeout);
		return inputStreamURL != null;
	}
	
	/**
	 * Monta o link usando o id do processo com a chave ou somente um dos dois.
	 * 
	 * @param url Vem da classe de Constantes e da interface URL_DETALHE_PROCESSO
	 * @param idProcessoTrf id do Processo a ser detalhado
	 * @param chave chave de acesso
	 * @return Link
	 */
	public static String montarLinkDetalheProcesso(String url, Integer idProcessoTrf, String chave){
		StringBuilder link = new StringBuilder();
		link.append(Util.instance().getUrlProject()).append(url);
		if(idProcessoTrf != null && chave != null){
			link.append(URL_DETALHE_PROCESSO.PARAM_ID).append(idProcessoTrf).append(URL_DETALHE_PROCESSO.PARAM_CHAVE_ACESSO).append(chave);
		} else if (idProcessoTrf != null){
			link.append(URL_DETALHE_PROCESSO.PARAM_ID).append(idProcessoTrf);
		} else {
			link.append(URL_DETALHE_PROCESSO.PARAM_CHAVE_ACESSO).append(chave);
		}
		return link.toString();
	}
	
	/**
	 * Monta o link para a consulta publica usando a chave
	 * 
	 * @param chave chave de acesso
	 * @return Link
	 */
	public static String montarLinkConsultaPublica(Integer idProcessoTrf, String chave){
		StringBuilder link = new StringBuilder();
		link.append(Util.instance().getContextPath()).append(Constantes.URL_DETALHE_PROCESSO.CONSULTA_PUBLICA);
		link.append(URL_DETALHE_PROCESSO.PARAM_ID).append(idProcessoTrf);
		link.append(URL_DETALHE_PROCESSO.PARAM_CHAVE_ACESSO).append(chave);
		return link.toString();
	}
	
	/**
	 * Monta o link usando o id do processo, id do expediente e com a chave para tomar ciência e/ou resposta do expediente. 
	 * @param url string da url da funcionalidade
	 * @param idProcessoTrf id do Processo relacionado ao expediente
	 * @param idProcessoParteExpediente id do expediente a ser respondido e/ou tomar ciencia
	 * @param chave chave de acesso
	 * @return Link referente a funcionalidade tomar ciencia resposta expediente
	 */
	public static String montarLinkTomarCienciaRespostaExpediente(String url, Integer idProcessoTrf, 
			Integer idProcessoParteExpediente, String chave){
		StringBuilder link = new StringBuilder();
		link.append(Util.instance().getUrlProject()).append(url);
		if(idProcessoTrf != null && idProcessoParteExpediente != null){
			link.append(URL_GERAL.PARAM_ID).append(idProcessoTrf)
				.append(URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE.PARAM_PROCESSO_JUDICIAL_ID)
				.append(idProcessoTrf)
				.append(URL_TOMAR_CIENCIA_RESPOSTA_EXPEDIENTE.PARAM_EXPEDIENTE_ID)
				.append(idProcessoParteExpediente);
		} else {
			link.append(URL_GERAL.PARAM_CHAVE_ACESSO).append(chave);
		}
		return link.toString();
	}

	/**
	 * Monta o link padrao para o detalhemento do processo
	 * 
	 * @param idProcessoTrf Id do processoo
	 * @return link
	 */
	public static String montarLinkDetalheProcessoDefault(Integer idProcessoTrf) {
		String chave = SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
		return UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_VISUALIZACAO, idProcessoTrf, chave);
	}
	
	/**
	 * Retorna true se a chamada HTTP/HTTPS foi chamada sem erros. O método é usado
	 * para verificar se o endpoint está online.
	 * 
	 * @param endpoint URL que deseja verificar.
	 * @return (true/false).
	 */
	public static Boolean isHttpOK(String endpoint) {
		Boolean isOnline = Boolean.FALSE;

		try {
			URL url = new URL(endpoint);
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(5000);
			http.setReadTimeout(5000);
			isOnline = (http.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			isOnline = Boolean.FALSE;
		} 

		return isOnline;
	}
}