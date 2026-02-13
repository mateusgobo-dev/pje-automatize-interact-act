/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.entity.log;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.hibernate.validator.Length;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Reflections;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.MeasureTime;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.IEntidade;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.entidades.log.Ignore;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;
import br.jus.pje.nucleo.util.StringUtil;

public class LogUtil implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(LogUtil.class);

	private static final ThreadLocal<SimpleDateFormat> localSimpleDateFormat = new ThreadLocal<SimpleDateFormat>();

	private static final SimpleDateFormat getSimpleDateFormat() {
		SimpleDateFormat format = localSimpleDateFormat.get();
		if (format == null) {
			format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
			localSimpleDateFormat.set(format);
		}
		return format;
	}

	/**
	 * Checa se a classe é um array de bytes.
	 *
	 * @param type
	 * @return
	 */
	public static final boolean isBinario(final Class<?> type) {
		return ((type != null) && type.isArray() && type.getComponentType().equals(Byte.TYPE));
	}

	/**
	 * Checa se um atributo de um objeto é um array de bytes.
	 *
	 * @param entidade
	 * @param nomeAtributo
	 * @return
	 * @throws Exception
	 */
	public static boolean isBinario(Object entidade, String nomeAtributo) throws Exception {
		Class<?> classAtributo = getType(entidade, nomeAtributo);
		return isBinario(classAtributo);
	}

	public static boolean isBinario(Class<?> clazz, String nomeAtributo) throws Exception {
		Class<?> classAtributo = getType(clazz, nomeAtributo);
		return isBinario(classAtributo);
	}

	public static Class<?> getType(Object entidade, String nomeAtributo) {
		if (entidade == null) {
			return null;
		}
		Class<?> classAtributo = Reflections.getField(entidade.getClass(), nomeAtributo).getType();
		return classAtributo;
	}

	public static Class<?> getType(Class<?> clazz, String nomeAtributo) {
		Class<?> classAtributo = Reflections.getField(clazz, nomeAtributo).getType();
		return classAtributo;
	}

	/**
	 * Checa se o atributo de um objeto é uma coleção.
	 *
	 * @param entidade
	 * @param nomeAtributo
	 * @return
	 * @throws Exception
	 */
	public static boolean isCollection(Object entidade, String nomeAtributo) throws Exception {
		Class<?> classAtributo = getType(entidade, nomeAtributo);
		return isCollection(classAtributo);
	}

	public static boolean isCollection(Class<?> clazz, String nomeAtributo) throws Exception {
		Class<?> classAtributo = getType(clazz, nomeAtributo);
		return isCollection(classAtributo);
	}

	public static boolean isCollection(Class<?> classAtributo) {
		return (classAtributo != null) && Collection.class.isAssignableFrom(classAtributo);
	}

	/**
	 * Testa se o atributo de um objeto é considerado de tamanho pequeno para o
	 * armazenamento no log.
	 *
	 * @param entidade
	 * @param nomeAtributo
	 * @return
	 * @throws Exception
	 */
	public static boolean isSmallField(Object entidade, String nomeAtributo) throws Exception {
		Class<?> classAtributo = getType(entidade, nomeAtributo);
		if (String.class.equals(classAtributo)) {
			PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(entidade, nomeAtributo);
			Length lengthAnnotation = pd.getReadMethod().getAnnotation(Length.class);
			return (lengthAnnotation != null) && (lengthAnnotation.max() <= 300);
		}
		return !isBinario(classAtributo);
	}

	/**
	 * Testa se a entidade possui a anotação @Ignore, caso possua não será
	 * logada
	 *
	 * @param entidade
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean isLogable(final Object entity) {
		if (entity == null) {
			return false;
		}
		if (entity instanceof IEntidade) {
			return ((IEntidade) entity).isLoggable();
		}
		return !EntityUtil.isAnnotationPresent(entity, Ignore.class);
	}

	public static final boolean compareObj(final Object o1, final Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		if (o1.equals(o2)) {
			return true;
		}
		if (EntityUtil.isEntity(o1)) {
			final Object id1 = EntityUtil.getEntityIdObject(o1);
			if (id1 == null) {
				return false;
			}
			return id1.equals(EntityUtil.getEntityIdObject(o2));
		}
		return false;
	}

	private static Integer getUsuarioIdLogado() {
		Context context = Contexts.getSessionContext();
		if (context == null) {
			context = Contexts.getEventContext(); 
		}
		if (context==null)
			return null;
		return Authenticator.getIdUsuarioLogado();
	}

	public static String getIpRequest(final HttpServletRequest request) throws LogException {
		// Condição adicionada no caso de uso de rotinas de de agendamento(job).
		// Neste caso não existe uma requisição então o objeto
		// HttpServletRequest será nulo.
		if (request == null) {
			return "";
		}
		String ipRequest = request.getHeader("x-forwarded-for");
		if (StringUtil.isEmpty(ipRequest) || "unknown".equalsIgnoreCase(ipRequest)) {
			ipRequest = request.getHeader("x-real-ip");
		}
		if (StringUtil.isEmpty(ipRequest) || "unknown".equalsIgnoreCase(ipRequest)) {
			ipRequest = request.getRemoteAddr();
		}
		return ipRequest;
	}

	public static String getIpRequest() throws LogException {
		final  HttpServletRequest request = getRequest();
		return getIpRequest(request);
	}

	public static String getUrlRequest(final HttpServletRequest request) throws LogException {
		// Condição adicionada no caso de uso de rotinas de de agendamento(job).
		// Neste caso não existe uma requisição então o objeto
		// HttpServletRequest será nulo.
		if (request == null) {
			return "";
		}
		return request.getRequestURL().toString();
	}

	public static String getUrlRequest() throws LogException {
		final HttpServletRequest request = getRequest();
		return getUrlRequest(request);
	}

	public static String getIdPagina() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		String requestURL = request.getRequestURL().toString();
		String viewId = requestURL.split(request.getContextPath())[1];
		return viewId;
	}
	
	public static HttpServletRequest getRequest() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null) {
			return null;
		}
		HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
		return request;
	}

	public static HttpServletRequest setRequestWebServiceContext(WebServiceContext wsCtxt) {
		MessageContext msgCtxt = wsCtxt.getMessageContext();
		HttpServletRequest request = (HttpServletRequest) msgCtxt.get(MessageContext.SERVLET_REQUEST);
		return request;
	}
	
	public static String toStringForLogWithCatchNullPointer(Object object, String nomePropriedade) throws NullPointerException {
		try {
			return toStringForLog(object);
		} catch (NullPointerException e) {
			log.warn("NullPointerException ao tentar obter texto da propriedade: " + nomePropriedade, e);
			return null;
		}
	}

	public static String toStringForLog(Class<?> clazz, String name, Object value) {
		String valueStr = null;
		if (LogUtil.isValidForLog(clazz, name)) {
			valueStr = LogUtil.toStringForLog(value);
		}
		return valueStr;
	}

	public static String toStringForLog(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof Date) {
			SimpleDateFormat dateFormat = getSimpleDateFormat();
			return dateFormat.format((Date) object);
		}
		if (EntityUtil.isEntity(object)) {
			return EntityUtil.getEntityIdObject(object).toString();
		}
		return object.toString();
	}

	public static boolean isValidForLog(Class<?> clazz, String name) {
		try {
			Class<?> classAtributo = LogUtil.getType(clazz, name);
			return !LogUtil.isCollection(classAtributo) && !LogUtil.isBinario(classAtributo);
		} catch(IllegalArgumentException ex) {
			log.trace("Ignorando campo proxy do hibernate", ex);
		}
		return false;
	}

	public static String toStringFields(Object component) {
		try {
			MeasureTime t = (log.isTraceEnabled()) ? new MeasureTime(true) : null;
			PropertyDescriptor[] props = Introspector.getBeanInfo(component.getClass()).getPropertyDescriptors();
			StringBuilder builder = new StringBuilder();
			for (PropertyDescriptor descriptor : props) {
				if (!isCollection(descriptor.getPropertyType())
						&& descriptor.getReadMethod() != null
						&& !ComponentUtil.hasAnnotation(descriptor, Lob.class)) {

					Object field = descriptor.getReadMethod().invoke(component);
					builder.append(descriptor.getName()).append('=');
					if (field != null && EntityUtil.isEntity(field)) {
						builder.append(toStringForLogWithCatchNullPointer(field, descriptor.getName()));
					} else {
						builder.append(field);
					}
					builder.append("; ");
				}
			}
			if (t != null) {
				log.info("toStringFields(Object component): " + t.getTime());
			}
			return builder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public static Map<String, Object> getFields(Object component) {
		try {
			MeasureTime t = (log.isTraceEnabled()) ? new MeasureTime(true) : null;
			Map<String, Object> map = new HashMap<String, Object>();
			PropertyDescriptor[] props = Introspector.getBeanInfo(component.getClass()).getPropertyDescriptors();
			for (PropertyDescriptor descriptor : props) {
				if (isColumn(descriptor)) {
					Object field = descriptor.getReadMethod().invoke(component);
					map.put(descriptor.getName(), field);
				}
			}
			if (t != null) {
				log.trace("getFields(" + component.getClass().getName() + "): " + t.getTime());
			}
			return map;
		} catch (Exception e) {
			return new HashMap<String, Object>(0);
		}
	}

	private static boolean isColumn(PropertyDescriptor pd) {
		Method rm = pd.getReadMethod();
		return rm != null && (rm.isAnnotationPresent(Column.class) || rm.isAnnotationPresent(JoinColumn.class));
	}

	public static EntityLog getEntityLog(Object component, TipoOperacaoLogEnum operacaoLogEnum) {
		EntityLog entityLog = createEntityLog(component);
		entityLog.setTipoOperacao(operacaoLogEnum);
		Map<String, Object> fields = getFields(component);
		for (Entry<String, Object> entry : fields.entrySet()) {
			EntityLogDetail det = new EntityLogDetail();
			det.setNomeAtributo(entry.getKey());
			String value = entry.getValue() == null ? "null" : entry.getValue().toString();
			if (operacaoLogEnum.equals(TipoOperacaoLogEnum.D)) {
				det.setValorAnterior(value);
			} else {
				det.setValorAtual(value);
			}
			det.setEntityLog(entityLog);
			entityLog.getLogDetalheList().add(det);
		}
		return entityLog;
	}

	public static EntityLog createEntityLog(Object component) {
		Integer idUsuario = getUsuarioIdLogado();
		String urlRequest = null;
		String ip = null;

		try {
			HttpServletRequest request = getRequest();
			ip = getIpRequest(request);
			urlRequest = getUrlRequest(request);
		} catch (LogException e) {
			// Se a requisição for executada por temporizador, não há requisição
			// então não se consegue obter o ip
			ip = "localhost";
		}
		return createEntityLog(component, idUsuario, ip, urlRequest);
	}

	public static EntityLog createEntityLog(Object component, Integer idUsuario, String ip, String urlRequest) {
		Class<? extends Object> clazz = EntityUtil.getEntityClass(component);
		final Object id = EntityUtil.getEntityIdObject(component);
		return createEntityLog(clazz, id, idUsuario, ip, urlRequest);
	}

	public static EntityLog createEntityLog(Class<?> clazz, Object id, Integer idUsuario, String ip, String urlRequest) {
		EntityLog entityLog = new EntityLog();
		entityLog.setIdUsuario(idUsuario);
		entityLog.setDataLog(new Date());
		entityLog.setIp(ip.length() > 15 ? "desconhecido" : ip);
		entityLog.setUrlRequisicao(urlRequest);
		entityLog.setNomeEntidade(clazz.getSimpleName());
		entityLog.setNomePackage(clazz.getPackage().getName());
		entityLog.setIdEntidade(id != null ? id.toString() : "");
		return entityLog;
	}

	public static void removeEntity(Object entity) {
		if (!EntityUtil.isEntity(entity)) {
			throw new IllegalArgumentException("O objeto não é uma entidade");
		}
		StringBuilder sb = new StringBuilder(100);
		sb.append("delete from ").append(entity.getClass().getName());
		sb.append(" o where o.").append(EntityUtil.getId(entity).getName());
		sb.append(" = :id");
		EntityManager em = getEntityManagerLog();
		Query query = em.createQuery(sb.toString());
		query.setParameter("id", EntityUtil.getEntityIdObject(entity));
		if (query.executeUpdate() > 0) {
			EntityLog entityLog = getEntityLog(entity, TipoOperacaoLogEnum.D);
			em.persist(entityLog);
		}
	}
	
	/**
	 * Esse mtodo tem o objetivo de retornar o IP da requisio.
	 * Por experincia, vimos que o mtodo {@link #getIpRequest()} pode retornar Ips IPv4 ou IPv6.
	 * Tambm, caso exista algum proxy ou dependendo da configurao de rede, o mtodo {@link #getIpRequest()} pode retornar mais de um IP.
	 * 
	 * Exemplo: "2001:0DB8:0000:0000:130F:0000:0000:140B,2001:0DB8:0000:0000:130F:0000:0000:140B"
	 *  
	 * Nesse caso, como o tamanho  inesperado, decidimos limitar o tamanho mximo do campo, para no estourar o tamanho mximo previsto no banco.
	 * 
	 * A ideia desse mtodo  no retornar excees para no prejudicar o andamente de determinada execues de cdigo que 
	 * preferem no obter o IP do que impedir determinado andamento do cdigo, como LOGIN.
	 *
	 * 
	 * @param tamanhoMaximo
	 * @return IP
	 */
	public static String getIpRequest(int tamanhoMaximo) {
		try {
			String ip = LogUtil.getIpRequest();
			if (StringUtil.isEmpty(ip)) {
				return null;
			}
			return StringUtil.limitarTamanho(ip, tamanhoMaximo);
		} catch (Exception e) {
			log.warn(e);
		}
		return null;
	}
	

	private static EntityManager getEntityManagerLog() {
		return  (EntityManager) Component.getInstance("entityManagerLog");
	}

	public static boolean isRequisicaoIntercomunicacaoRest() {
		boolean requisicaoIntercomunicacao = false;
		try {
			String urlRequest = getUrlRequest(getRequest());
			requisicaoIntercomunicacao = urlRequest != null && urlRequest.contains("pje-legacy/api/v1/servico-intercomunicacao-2.2");
		} catch (LogException e) {
			log.error(e.getLocalizedMessage());
		}
		return requisicaoIntercomunicacao;
	}

}


