package br.com.itx.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.FactoryFinder;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.FacesMessagesFix;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.web.Parameters;

import br.jus.pje.nucleo.util.ArrayUtil;

import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;

/**
 * Classe genérica para acesso ao container do myfaces.
 */
public final class FacesUtil {

	public static final String BUNDLE_MENSAGENS = "messages";

	private FacesUtil() {
	}

	/**
	 * Recupera um ServltContext do builder.
	 * 
	 * @param webapp
	 *            define o contexto a ser recuperado.
	 */
	public static ServletContext getServletContext(String webapp) {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		ServletContext wiSc = (ServletContext) ec.getContext();
		if (webapp == null) {
			return wiSc;
		}
		return wiSc.getContext(webapp);
	}

	/**
	 * Recupera uma mensagem.
	 * 
	 * @param bundle
	 *            define o arquivo de mensagens a ser utilizado.
	 * @param key
	 *            define a chave a ser utilizada.
	 * @param params
	 *            define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public static String getMessage(String bundle, String key, Object... params) {
		FacesContext fc = FacesContext.getCurrentInstance();
		String message = null;
		if ( fc!=null ) {
			Locale loc = fc.getViewRoot().getLocale();
			ResourceBundle rb = ResourceBundle.getBundle(bundle, loc);
			if(params != null && params.length > 0) {
				message = MessageFormat.format(rb.getString(key), params);
			}
			else {
				message = rb.getString(key);
			}
		}
		return message;
	}
	
	/**
	 * Recupera uma mensagem do arquivo de mensagens entity_messages.properties.
	 * 
	 * @param key
	 *            define a chave a ser utilizada.
	 * @param params
	 *            define os parâmetros que serão carregados na mensagem do bundle, se existentes.
	 */
	public static String getMessage(String key, Object... params) {
		return getMessage("entity_messages", key, params);
	}
	

	/**
	 * Recupera o outputstream já com o mime definido.
	 * 
	 * @param mime
	 *            define o mime a ser enviado.
	 * @param filename
	 *            define o nome ao salvar o arquivo.
	 */
	public static OutputStream getOutputStream(boolean nocache, String mime, String name) {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		HttpServletResponse response = (HttpServletResponse) ec.getResponse();
		if (nocache) {
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "must-revalidate, no-store");
			response.setDateHeader("Expires", 0);
		} else {
			response.setHeader("Cache-Control", "max-age=60");
		}
		if (name != null && !name.equals("")) {
			String disposition = "inline; filename=\"" + name + "\"";
			response.setHeader("Content-disposition", disposition);
		}
		response.setContentType(mime);
		OutputStream out = null;
		try {
			out = response.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
		return out;
	}

	/**
	 * Fecha o outputstream.
	 */
	public static void closeOutputStream(OutputStream out) {
		try {
			if (out != null) {
				out.flush();
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Armazena uma mensagem de erro.
	 * 
	 * @param message
	 *            define a mensagem.
	 */
	public static void setErrorMessage(String message) {
		try {
			String encmsg = URLEncoder.encode(message, "iso-8859-1");
			encmsg = encmsg.replace('+', ' ');
			Contexts.getEventContext().set("errorMessage", encmsg);
		} catch (UnsupportedEncodingException e) {
		}
	}

	/**
	 * Clona um objeto.
	 */
	public static <T extends Object> T cloneBean(T obj) {
		Object resp = null;
		try {
			byte[] bytes = null;
			// Serialize to a byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(baos);
			out.writeObject(obj);
			out.close();
			bytes = baos.toByteArray();
			// Deserialize from a byte array
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(bais);
			resp = in.readObject();
			in.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return (T) resp;
	}

	/**
	 * Coloca a FacesMessage adicionada na fase Render Response no FacesContext
	 */
	public static void refreshFacesMessages() {
		if (FacesLifecycle.getPhaseId() != null && FacesLifecycle.getPhaseId().equals(PhaseId.RENDER_RESPONSE)) {
			FacesMessages.afterPhase();
			FacesMessages.instance().beforeRenderResponse();
		}
	}
	
	/**
	 * @param nome Nome do atributo.
	 * @return Objeto do atributo passado por parâmetro.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getApplicationAttribute(String nome) {
		return (T) getApplicationMap().get(nome);
	}
	
	/**
	 * Adiciona um objeto no escopo de aplicação.
	 * 
	 * @param nome Nome do atributo.
	 * @param valor Objeto.
	 */
	public static void setApplicationAttribute(String nome, Object valor) {
		getApplicationMap().put(nome, valor);
	}
	
	/**
	 * Remove um objeto do escopo de aplicação.
	 * 
	 * @param nome Nome do atributo.
	 */
	public static void removeApplicationAttribute(String nome) {
		getApplicationMap().remove(nome);
	}
	
	/**
	 * @return Mapa de atributos do escopo Application.
	 */
	public static Map<String, Object> getApplicationMap() {
		return getExternalContext().getApplicationMap();
	}

	/**
	 * Adiciona uma mensagem no contexto do FacesMessage.
	 * 
	 * @param limpar True se for para limpar as mensagens existentes.
	 * @param excecao Exceção.
	 */
	public static void adicionarMensagemInfo(Boolean limpar, Exception excecao) {
		String msg = excecao.getMessage() + (
				excecao.getCause() == null ? 
						"" : 
							excecao.getCause().getMessage());
		excecao.printStackTrace();
		adicionarMensagemInfo(limpar, msg);
	}
	
	/**
	 * Adiciona uma mensagem no contexto do FacesMessage.
	 * 
	 * @param limpar True se for para limpar as mensagens existentes.
	 * @param mensagem Mensagem que será registrada.
	 */
	public static void adicionarMensagemInfo(Boolean limpar, String mensagem) {
		FacesMessages facesMessages = FacesMessages.instance();
		if (limpar) { 
			facesMessages.clear();
		}
		facesMessages.add(StatusMessage.Severity.INFO, mensagem);
	}

	/**
	 * Adiciona uma mensagem no contexto do FacesMessage.
	 * 
	 * @param limpar True se for para limpar as mensagens existentes.
	 * @param excecao Exceção.
	 */
	public static void adicionarMensagemError(Boolean limpar, Exception excecao) {
		String msg = excecao.getMessage() + (
				excecao.getCause() == null ? 
						"" : 
							excecao.getCause().getMessage());
		excecao.printStackTrace();
		adicionarMensagemError(limpar, msg);
	}
	
	/**
	 * Adiciona uma mensagem no contexto do FacesMessage.
	 * 
	 * @param limpar True se for para limpar as mensagens existentes.
	 * @param mensagem Mensagem que será registrada.
	 */
	public static void adicionarMensagemError(Boolean limpar, String mensagem) {
		FacesMessages facesMessages = FacesMessages.instance();
		if (limpar) { 
			facesMessages.clear();
		}
		facesMessages.add(StatusMessage.Severity.ERROR, mensagem);
	}
	
	/**
	 * @return Primeira mensagem do tipo 'Error'.
	 */
	public static StatusMessage getErrorMessage() {
		FacesMessagesFix fm = (FacesMessagesFix) FacesMessages.instance();
		return fm.getErrorMessage();
	}
	
	/**
	 * @return ExternalContext
	 */
	private static ExternalContext getExternalContext() {
		FacesContext fc = FacesContext.getCurrentInstance();
		return fc.getExternalContext();
	}
	
	public static List<FacesMessage> getCurrentMessages(){
		FacesMessages facesMessages = FacesMessages.instance();
		return facesMessages.getCurrentMessages();
	}
	
	/**
	 * Cria um novo FacesContext caso não exista. O método é usado basicamente nas chamadas aos WebServices.
	 * 
	 * @param request HttpServletRequest.
	 * @param response HttpServletResponse.
	 * @return FacesContext.
	 */
	public static FacesContext novoFacesContext(HttpServletRequest request, HttpServletResponse response) {
		FacesContext fc = FacesContext.getCurrentInstance();
		
		if (fc ==  null) {
			HttpSession session = request.getSession();
			ServletContext servletContext = session.getServletContext();
			
			
			if (servletContext != null && request != null && response != null) {
				LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			    javax.faces.lifecycle.Lifecycle lifecycle = factory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
			    
			    ExternalContext extContext = new ExternalContextImpl(
			    		servletContext,
			    		request,
			    		response);
			    fc = new FacesContextImpl(extContext, lifecycle);
			}
		}
		
		return fc;
	}
	
	/**
	 * @param key
	 * @return Valor do parâmetro do request.
	 */
	public static String getRequestParameter(String key) {
		String result = null;
		
		if (StringUtils.isNotBlank(key)) {
			Map<String, String[]> parameters = Parameters.instance().getRequestParameters();
			result = ArrayUtil.get(parameters.get(key), 0);
		}
		
		return result;
	}
}