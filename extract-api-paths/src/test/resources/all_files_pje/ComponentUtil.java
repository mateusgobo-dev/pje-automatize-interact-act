/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.itx.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Reflections;

import br.com.infox.cliente.home.DiligenciaHome;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.home.SessaoProcessoDocumentoVotoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.list.ProcessoDocumentoElaboracaoAcordaoCkList;
import br.com.infox.pje.list.ProcessoDocumentoElaboracaoAcordaoList;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.pje.service.IntimacaoPartesService;
import br.com.itx.component.AbstractHome;
import br.com.jt.pje.dao.SessaoDAO;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.com.jt.pje.manager.SalaManager;
import br.com.jt.pje.manager.SessaoManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.cnj.pje.business.dao.BlocoComposicaoDAO;
import br.jus.cnj.pje.business.dao.BlocoJulgamentoDAO;
import br.jus.cnj.pje.business.dao.ConsultaProcessoAdiadoVistaDAO;
import br.jus.cnj.pje.business.dao.FluxoDAO;
import br.jus.cnj.pje.business.dao.NotaSessaoBlocoDAO;
import br.jus.cnj.pje.business.dao.OrgaoJulgadorCargoDAO;
import br.jus.cnj.pje.business.dao.OrgaoJulgadorDAO;
import br.jus.cnj.pje.business.dao.ParametroDAO;
import br.jus.cnj.pje.business.dao.ProcessoBlocoDAO;
import br.jus.cnj.pje.business.dao.SessaoPautaProcessoTrfDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoDAO;
import br.jus.cnj.pje.business.dao.SessaoProcessoDocumentoVotoDAO;
import br.jus.cnj.pje.business.dao.VinculacaoDependenciaEleitoralDAO;
import br.jus.cnj.pje.business.dao.VotoBlocoDAO;
import br.jus.cnj.pje.nucleo.manager.BlocoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.BlocoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoAdiadoVistaManager;
import br.jus.cnj.pje.nucleo.manager.ControleVersaoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.HistoricoDeslocamentoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.ManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.MuralService;
import br.jus.cnj.pje.nucleo.manager.NotaSessaoBlocoManager;
import br.jus.cnj.pje.nucleo.manager.NotaSessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PapelManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAutoridadeManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PlacarSessaoManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoBlocoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoAcordaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoLidoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpDocCertidaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoPesoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.RegistroIntimacaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoComposicaoOrdemManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoMultDocsVotoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.TipoParteConfiguracaoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.VotoBlocoManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.OrgaoJulgadorService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualImpl;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.servicos.EditorEstiloService;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.view.ListProcessoCompletoBetaAction;
import br.jus.cnj.pje.view.PainelDoMagistradoNaSessaoAction;
import br.jus.cnj.pje.view.PainelSessaoSecretarioSessaoAction;
import br.jus.cnj.pje.view.PlacarSessaoAction;
import br.jus.cnj.pje.view.PopUpVotoBlocoAction;
import br.jus.cnj.pje.view.fluxo.AcordaoModelo;
import br.jus.cnj.pje.view.fluxo.WinVotoBlocoAction;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.csjt.pje.view.action.LancadorMovimentosAction;

public final class ComponentUtil {

	private static final LogProvider log = Logging.getLogProvider(ComponentUtil.class);

	private ComponentUtil() {
	}

	/**
	 * Busca um componente pelo identificador
	 * 
	 * @param componentId
	 *            identificador do component
	 * @return componente com o nome solicitado ou null, especialmente em testes
	 *         de integração.
	 */
	public static UIComponent getUIComponent(String componentId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			return null;
		}
		UIViewRoot viewRoot = facesContext.getViewRoot();
		if (viewRoot == null) {
			return null;
		}
		return viewRoot.findComponent(componentId);
	}

	/**
	 * Limpa os campos de um componente
	 * 
	 * @param component
	 *            geralmente um UIForm, mas pode ser qualquer tipo de
	 *            UIComponente
	 */
	public static void clearChildren(UIComponent component) {
		if (component == null) {
			return;
		}
		if (component instanceof EditableValueHolder) {
			EditableValueHolder evh = (EditableValueHolder) component;
			evh.setValid(true);
			evh.setValue(null);
		}
		for (UIComponent c : component.getChildren()) {
			clearChildren(c);
		}
	}

	public static List<PropertyDescriptor> getProperties(Object component) {
		return getProperties(component.getClass());
	}

	public static List<PropertyDescriptor> getProperties(Class<?> component) {
		List<PropertyDescriptor> resp = new ArrayList<PropertyDescriptor>();
		try {
			PropertyDescriptor[] pds = getPropertyDescriptors(component);
			for (int i = 0; i < pds.length; i++) {
				PropertyDescriptor pd = pds[i];
				if (!pd.getName().equals("class") && !pd.getName().equals("bytes")) {
					resp.add(pd);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resp;
	}

	/**
	 * Metodo que devolve um array com os PropertyDescriptors de uma classe
	 * 
	 * @param clazz
	 * @return
	 */
	public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) {
		try {
			return Introspector.getBeanInfo(clazz).getPropertyDescriptors();
		} catch (IntrospectionException e) {
		}
		return new PropertyDescriptor[0];
	}

	public static PropertyDescriptor[] getPropertyDescriptors(Object component) {
		return getPropertyDescriptors(component.getClass());
	}

	public static PropertyDescriptor getPropertyDescriptor(Object component, String property) {
		try {
			return PropertyUtils.getPropertyDescriptor(component, property);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static Class<?> getType(Object component, String property) {
		PropertyDescriptor pd = getPropertyDescriptor(component, property);
		if (pd == null) {
			return null;
		}
		return getType(pd);
	}

	public static Class<?> getType(PropertyDescriptor pd) {
		return pd.getPropertyType();
	}

	public static boolean hasAnnotation(PropertyDescriptor pd, Class<? extends Annotation> annotation) {
		Method readMethod = pd.getReadMethod();
		if (readMethod != null) {
			if (readMethod.isAnnotationPresent(annotation)) {
				return true;
			}

			Class<?> declaringClass = readMethod.getDeclaringClass();
			try {
				Field field = declaringClass.getDeclaredField(pd.getName());
				return field.isAnnotationPresent(annotation);
			} catch (NoSuchFieldException ex) {
				return false;
			}

		}
		return false;
	}

	public static Object getValue(Object component, String property) {
		Method getterMethod = Reflections.getGetterMethod(component.getClass(), property);
		if (getterMethod != null) {
			getterMethod.setAccessible(true);
			return Reflections.invokeAndWrap(getterMethod, component, new Object[0]);
		}
		return null;
	}

	public static Object getValue(Object component, PropertyDescriptor pd) {
		return Reflections.invokeAndWrap(pd.getReadMethod(), component, new Object[0]);
	}

	public static void setValue(Object component, String property, Object value) {
		Method setterMethod = Reflections.getSetterMethod(component.getClass(), property);
		if (setterMethod != null) {
			Reflections.invokeAndWrap(setterMethod, component, value);
		}
	}

	public static void setValue(Object component, PropertyDescriptor pd, Object value) {
		Reflections.invokeAndWrap(pd.getWriteMethod(), component, value);
	}

	public static Object getValuePrivateField(Object component, String fieldName) {
		Object returnObj = null;
		try {
			Field f = component.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			returnObj = f.get(component);
			f.setAccessible(false);
		} catch (Exception e) {
			log.warn("Exception ao tentar ler atributo privado", e);
		}
		return returnObj;
	}

	/**
	 * Metodo que recebe o nome de um home e devolve o getInstance() deste home,
	 * fazendo o cast. Retorna uma exceção caso o componente não seja
	 * encontrado.
	 * 
	 * @param <C>
	 * @param homeName
	 * @return
	 */
	public static <C> C getInstance(String homeName) {
		AbstractHome<C> home = getComponent(homeName);
		if (home == null) {
			throw new IllegalArgumentException("O home '" + homeName + "' não foi encontrado.");
		}
		return home.getInstance();
	}

	/**
	 * Metodo que devolve a instancia de um componente usando o
	 * {@link org.jboss.seam.Component#getInstance(String)
	 * Component.getInstance} e fazendo cast para o tipo declarado.
	 * 
	 * @param <C>
	 *            O tipo declarado
	 * @param componentName
	 *            Nome do componte
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> C getComponent(String componentName) {
		return (C) Component.getInstance(componentName);
	}
	
	/**
	 * Metodo que devolve a instancia de um componente usando o
	 * {@link org.jboss.seam.Component#getInstance(Class)
	 * Component.getInstance} e fazendo cast para o tipo declarado.
	 * 
	 * @param <C>
	 *            O tipo declarado
	 * @param componentClass
	 *            Tipo do componte
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> C getComponent(Class<C> componentClass) {
		return (C) getComponent(Seam.getComponentName(componentClass), Seam.getComponentScope(componentClass), true);
	}
	
	/**
	 * Método que devolve a instância de um componente diretamente
	 * do contexto passado como argumento e fazendo cast para o tipo declarado.
	 * <br/>
	 * Se o componente não existir ou não for possível recuperá-lo do contexto argumento,
	 * uma tentativa é realizada em {@link org.jboss.seam.Component#getInstance(Class)}
	 * que varre todos os contextos. 
	 * 
	 * @param nomeComponente - a classe do componente a ser recuperado/criado;
	 * @param escopo - o contexto inicial a ser utilizado para buscar o componente já existente;
	 * @param create - true/false para indicar se o componente deve ser criado, caso não exista;
	 * @return Instância singleton do componente desejado no contexto.
	 */
	@SuppressWarnings("unchecked")
	private static <C> C getComponent(String nomeComponente, ScopeType escopo, boolean create) {
		return (C) Component.getInstance(nomeComponente, escopo, create);
	}	
	
	/**
	 * Metodo que devolve a instancia de um componente usando o
	 * {@link org.jboss.seam.Component#getInstance(Class, boolean)}
	 * e fazendo cast para o tipo declarado.
	 * 
	 * @param <C>
	 *            O tipo declarado
	 * @param componentClass
	 *            Tipo do componte
	 * @param create
	 * 			  Se o componente deve ser criado caso não esteja no contexto
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> C getComponent(Class<C> componentClass, boolean create) {
		return (C) getComponent(Seam.getComponentName(componentClass), Seam.getComponentScope(componentClass), create);
	}	

	/**
	 * Metodo que devolve a instancia de um componente usando o
	 * {@link org.jboss.seam.Component#getInstance(String)
	 * Component.getInstance} e fazendo cast para o tipo declarado.
	 * 
	 * @param <C>
	 *            O tipo declarado
	 * @param componentName
	 *            Nome do componente
	 * @param scopeType
	 *            O Escopo do componente
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> C getComponent(String componentName, ScopeType scopeType) {
		return (C) Component.getInstance(componentName, scopeType);
	}

	/**
	 * Retorna true se algum dos objetos for null
	 * 
	 * @param objects
	 * @return
	 */
	public static boolean containsNullObject(Object... objects) {
		if (objects != null) {
			for (Object object : objects) {
				if (object == null) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Metodo que devolve a instancia de um componente usando o
	 * {@link org.jboss.seam.Component#getInstance(String, boolean)
	 * Component.getInstance} e fazendo cast para o tipo declarado.
	 * 
	 * @param <C>
	 *            O tipo declarado
	 * @param componentName
	 *            Nome do componte
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <C> C getComponent(String componentName, boolean create) {
		return (C) Component.getInstance(componentName, create);
	}

	/**
	 * Retorna a nome do componente atraves da anotação @Name
	 * 
	 * @param clazz
	 * @return
	 */
	public static String getComponentName(Class<?> clazz) {
		Name annotationName = clazz.getAnnotation(Name.class);
		return annotationName.value();
	}

	/**
	 * Testa de um componente está no contexto de conversação
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isOnConversationContext(Class<?> clazz) {
		String componentName = ComponentUtil.getComponentName(clazz);
		if (componentName != null) {
			return isOnConversationContext(componentName);
		} else {
			throw new IllegalArgumentException("Classe não possui @Name");
		}
	}

	/**
	 * Testa de um componente está no contexto de conversação
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isOnConversationContext(String name) {
		Object object = Contexts.getConversationContext().get(name);
		return object != null;
	}
	
	/**
	 * Retorna o componente singleton {@link DocumentoJudicialService} do contexto.
	 * 
	 * @return {@link DocumentoJudicialService}
	 */
	public static DocumentoJudicialService getDocumentoJudicialService() {
		return getComponent(DocumentoJudicialService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ParametroService} do contexto.
	 * 
	 * @return {@link ParametroService}
	 */
	public static ParametroService getParametroService() {
		return getComponent(ParametroService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PessoaMagistradoManager} do contexto.
	 * 
	 * @return {@link PessoaMagistradoManager}
	 */
	public static PessoaMagistradoManager getPessoaMagistradoManager() {
		return getComponent(PessoaMagistradoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoBinPessoaAssinaturaManager} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoBinPessoaAssinaturaManager}
	 */
	public static ProcessoDocumentoBinPessoaAssinaturaManager getProcessoDocumentoBinPessoaAssinaturaManager() {
		return getComponent(ProcessoDocumentoBinPessoaAssinaturaManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoPautaProcessoTrfManager} do contexto.
	 * 
	 * @return {@link SessaoPautaProcessoTrfManager}
	 */
	public static SessaoPautaProcessoTrfManager getSessaoPautaProcessoTrfManager() {
		return getComponent(SessaoPautaProcessoTrfManager.NAME, SessaoPautaProcessoTrfManager.ESCOPO, false);
	}
	
	/**
	 * Retorna o componente singleton {@link ModeloDocumentoManager} do contexto.
	 * 
	 * @return {@link ModeloDocumentoManager}
	 */
	public static ModeloDocumentoManager getModeloDocumentoManager() {
		return getComponent(ModeloDocumentoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoManager} do contexto.
	 * 
	 * @return {@link SessaoManager}
	 */
	public static SessaoManager getSessaoManager() {
		return getComponent(SessaoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoTrfLocalManager} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoTrfLocalManager}
	 */
	public static ProcessoDocumentoTrfLocalManager getProcessoDocumentoTrfLocalManager() {
		return getComponent(ProcessoDocumentoTrfLocalManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link IntimacaoPartesService} do contexto.
	 * 
	 * @return {@link IntimacaoPartesService}
	 */
	public static IntimacaoPartesService getIntimacaoPartesService() {
		return getComponent(IntimacaoPartesService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoDAO} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoDAO}
	 */
	public static SessaoProcessoDocumentoDAO getSessaoProcessoDocumentoDAO() {
		return getComponent(SessaoProcessoDocumentoDAO.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoManager} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoManager}
	 */
	public static SessaoProcessoDocumentoManager getSessaoProcessoDocumentoManager() {
		return getComponent(SessaoProcessoDocumentoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoJulgamentoManager} do contexto.
	 * 
	 * @return {@link SessaoJulgamentoManager}
	 */
	public static SessaoJulgamentoManager getSessaoJulgamentoManager() {
		return getComponent(SessaoJulgamentoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link LancadorMovimentosAction} do contexto.
	 * 
	 * @return {@link LancadorMovimentosAction}
	 */
	public static LancadorMovimentosAction getLancadorMovimentosAction() {
		return getComponent(LancadorMovimentosAction.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoVotoManager} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoVotoManager}
	 */
	public static SessaoProcessoDocumentoVotoManager getSessaoProcessoDocumentoVotoManager() {
		return getComponent(SessaoProcessoDocumentoVotoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link TipoVotoManager} do contexto.
	 * 
	 * @return {@link TipoVotoManager}
	 */
	public static TipoVotoManager getTipoVotoManager() {
		return getComponent(TipoVotoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link DerrubadaVotoManager} do contexto.
	 * 
	 * @return {@link DerrubadaVotoManager}
	 */
	public static DerrubadaVotoManager getDerrubadaVotoManager() {
		return getComponent(DerrubadaVotoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoJudicialManager} do contexto.
	 * 
	 * @return {@link ProcessoJudicialManager}
	 */
	public static ProcessoJudicialManager getProcessoJudicialManager() {
		return getComponent(ProcessoJudicialManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoTrfManager} do contexto.
	 * 
	 * @return {@link ProcessoTrfManager}
	 */
	public static ProcessoTrfManager getProcessoTrfManager() {
		return getComponent(ProcessoTrfManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link OrgaoJulgadorCargoManager} do contexto.
	 * 
	 * @return {@link OrgaoJulgadorCargoManager}
	 */
	public static OrgaoJulgadorCargoManager getOrgaoJulgadorCargoManager() {
		return getComponent(OrgaoJulgadorCargoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoManager} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoManager}
	 */
	public static ProcessoDocumentoManager getProcessoDocumentoManager() {
		return getComponent(ProcessoDocumentoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoTrfHome} do contexto.
	 * 
	 * @return {@link ProcessoTrfHome}
	 */
	public static ProcessoTrfHome getProcessoTrfHome() {
		return getComponent(ProcessoTrfHome.class);
	}
	
	/**
	 * Retorna o componente singleton {@link HistoricoDeslocamentoOrgaoJulgadorManager} do contexto.
	 * 
	 * @return {@link HistoricoDeslocamentoOrgaoJulgadorManager}
	 */
	public static HistoricoDeslocamentoOrgaoJulgadorManager getHistoricoDeslocamentoOrgaoJulgadorManager() {
		return getComponent(HistoricoDeslocamentoOrgaoJulgadorManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link UsuarioService} do contexto.
	 * 
	 * @return {@link UsuarioService}
	 */
	public static UsuarioService getUsuarioService() {
		return getComponent(UsuarioService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link AtoComunicacaoService} do contexto.
	 * 
	 * @return {@link AtoComunicacaoService}
	 */
	public static AtoComunicacaoService getAtoComunicacaoService() {
		return getComponent(AtoComunicacaoService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PrazosProcessuaisService} do contexto.
	 * 
	 * @return {@link PrazosProcessuaisService}
	 */
	public static PrazosProcessuaisService getPrazosProcessuaisService() {
		return getComponent(PrazosProcessuaisServiceImpl.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoParteExpedienteManager} do contexto.
	 * 
	 * @return {@link ProcessoParteExpedienteManager}
	 */
	public static ProcessoParteExpedienteManager getProcessoParteExpedienteManager() {
		return getComponent(ProcessoParteExpedienteManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoJudicialService} do contexto.
	 * 
	 * @return {@link ProcessoJudicialService}
	 */
	public static ProcessoJudicialService getProcessoJudicialService() {
		return getComponent(ProcessoJudicialService.class);
	}	
	
	/**
	 * Retorna o componente singleton {@link ManifestacaoProcessualManager} do contexto.
	 * 
	 * @return {@link ManifestacaoProcessualManager}
	 */
	public static ManifestacaoProcessualManager getManifestacaoProcessualManager() {
		return getComponent(ManifestacaoProcessualManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link TipoProcessoDocumentoManager} do contexto.
	 * 
	 * @return {@link TipoProcessoDocumentoManager}
	 */
	public static TipoProcessoDocumentoManager getTipoProcessoDocumentoManager() {
		return getComponent(TipoProcessoDocumentoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PrioridadeProcessoManager} do contexto.
	 * 
	 * @return {@link PrioridadeProcessoManager}
	 */
	public static PrioridadeProcessoManager getPrioridadeProcessoManager() {
		return getComponent(PrioridadeProcessoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PessoaJuridicaManager} do contexto.
	 * 
	 * @return {@link PessoaJuridicaManager}
	 */
	public static PessoaJuridicaManager getPessoaJuridicaManager() {
		return getComponent(PessoaJuridicaManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoTrfManifestacaoProcessualManager} do contexto.
	 * 
	 * @return {@link ProcessoTrfManifestacaoProcessualManager}
	 */
	public static ProcessoTrfManifestacaoProcessualManager getProcessoTrfManifestacaoProcessualManager() {
		return getComponent(ProcessoTrfManifestacaoProcessualManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link RegistroIntimacaoManager} do contexto.
	 * 
	 * @return {@link RegistroIntimacaoManager}
	 */
	public static RegistroIntimacaoManager getRegistroIntimacaoManager() {
		return getComponent(RegistroIntimacaoManager.class);
	}
		
	/**
	 * Retorna o componente singleton {@link OrgaoJulgadorColegiadoManager} do contexto.
	 * 
	 * @return {@link OrgaoJulgadorColegiadoManager}
	 */
	public static OrgaoJulgadorColegiadoManager getOrgaoJulgadorColegiadoManager() {
		return getComponent(OrgaoJulgadorColegiadoManager.class);
	}	
	
	/**
	 * Retorna o componente singleton {@link ProcessoExpedienteManager} do contexto.
	 * 
	 * @return {@link ProcessoExpedienteManager}
	 */
	public static ProcessoExpedienteManager getProcessoExpedienteManager() {
		return getComponent(ProcessoExpedienteManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PessoaFisicaService} do contexto.
	 * 
	 * @return {@link PessoaFisicaService}
	 */
	public static PessoaFisicaService getPessoaFisicaService() {
		return getComponent(PessoaFisicaService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PessoaFisicaManager} do contexto.
	 * 
	 * @return {@link PessoaFisicaManager}
	 */
	public static PessoaFisicaManager getPessoaFisicaManager() {
		return getComponent(PessoaFisicaManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link PessoaAutoridadeManager} do contexto.
	 * 
	 * @return {@link PessoaAutoridadeManager}
	 */
	public static PessoaAutoridadeManager getPessoaAutoridadeManager() {
		return getComponent(PessoaAutoridadeManager.class);
	}	
	
	/**
	 * Retorna o componente singleton {@link DiligenciaHome} do contexto.
	 * 
	 * @return {@link DiligenciaHome}
	 */
	public static DiligenciaHome getDiligenciaHome() {
		return getComponent(DiligenciaHome.class);
	}

	/**
	 * Retorna o componente singleton {@link MimeUtilChecker} do contexto.
	 * 
	 * @return {@link MimeUtilChecker}
	 */
	public static MimeUtilChecker getMimeUtilChecker() {
		return getComponent(MimeUtilChecker.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoHome} do contexto.
	 * 
	 * @return {@link SessaoHome}
	 */
	public static SessaoHome getSessaoHome() {
		return getComponent(SessaoHome.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoPautaProcessoTrfHome} do contexto.
	 * 
	 * @return {@link SessaoPautaProcessoTrfHome}
	 */
	public static SessaoPautaProcessoTrfHome getSessaoPautaProcessoTrfHome() {
		return getSessaoPautaProcessoTrfHome(false);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoPautaProcessoTrfHome} do contexto.
	 * @param create - indica se o componente deve ser criado no contexto se não existir.
	 * @return
	 */
	public static SessaoPautaProcessoTrfHome getSessaoPautaProcessoTrfHome(boolean create) {
		return getComponent(SessaoPautaProcessoTrfHome.class);
	}	
	
	/**
	 * Retorna o componente singleton {@link ParametroUtil} do contexto.
	 * 
	 * @return {@link ParametroUtil}
	 */
	public static ParametroUtil getParametroUtil() {
		return getComponent(ParametroUtil.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ParametroJtUtil} do contexto.
	 * 
	 * @return {@link ParametroJtUtil}
	 */
	public static ParametroJtUtil getParametroJtUtil() {
		return getComponent(ParametroJtUtil.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoMagistradoManager} do contexto.
	 * 
	 * @return {@link ProcessoMagistradoManager}
	 */
	public static ProcessoMagistradoManager getProcessoMagistradoManager() {
		return getComponent(ProcessoMagistradoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link TramitacaoProcessualService} do contexto.
	 * 
	 * @return {@link TramitacaoProcessualService}
	 */
	public static TramitacaoProcessualService getTramitacaoProcessualService() {
		return getComponent(TramitacaoProcessualImpl.class);
	}
	
	/**
	 * Retorna o componente singleton {@link AcordaoModelo} do contexto.
	 * 
	 * @return {@link AcordaoModelo}
	 */
	public static AcordaoModelo getAcordaoModelo() {
		return getComponent(AcordaoModelo.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ModeloDocumentoLocalManager} do contexto.
	 * 
	 * @return {@link ModeloDocumentoLocalManager}
	 */
	public static ModeloDocumentoLocalManager getModeloDocumentoLocalManager() {
		return getComponent(ModeloDocumentoLocalManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoAcordaoManager} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoAcordaoManager}
	 */
	public static ProcessoDocumentoAcordaoManager getProcessoDocumentoAcordaoManager() {
		return getComponent(ProcessoDocumentoAcordaoManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link TipoProcessoDocumentoPapelService} do contexto.
	 * 
	 * @return {@link TipoProcessoDocumentoPapelService}
	 */
	public static TipoProcessoDocumentoPapelService getTipoProcessoDocumentoPapelService() {
		return getComponent(TipoProcessoDocumentoPapelService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoElaboracaoAcordaoList} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoElaboracaoAcordaoList}
	 */
	public static ProcessoDocumentoElaboracaoAcordaoList getProcessoDocumentoElaboracaoAcordaoList() {
		return getComponent(ProcessoDocumentoElaboracaoAcordaoList.class);
	}
	
	/**
	 * Retorna o componente singleton {@link Identity} do contexto.
	 * 
	 * @return {@link Identity}
	 */
	public static Identity getIdentity() {
		return getComponent(Identity.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoHome} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoHome}
	 */
	public static ProcessoDocumentoHome getProcessoDocumentoHome(boolean create) {
		return getComponent(ProcessoDocumentoHome.class, create);
	}
	
	/**
	 * Retorna o componente singleton {@link AssinaturaDocumentoService} do contexto.
	 * 
	 * @return {@link AssinaturaDocumentoService}
	 */
	public static AssinaturaDocumentoService getAssinaturaDocumentoService() {
		return getComponent(AssinaturaDocumentoService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoHome} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoHome}
	 */
	public static SessaoProcessoDocumentoHome getSessaoProcessoDocumentoHome() {
		return getComponent(SessaoProcessoDocumentoHome.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoBinManager} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoBinManager}
	 */
	public static ProcessoDocumentoBinManager getProcessoDocumentoBinManager() {
		return getComponent(ProcessoDocumentoBinManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link ProcessoAudienciaManager} do contexto.
	 * 
	 * @return {@link ProcessoAudienciaManager}
	 */
	public static ProcessoAudienciaManager getProcessoAudienciaManager() {
		return getComponent(ProcessoAudienciaManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SalaManager} do contexto.
	 * 
	 * @return {@link SalaManager}
	 */
	public static SalaManager getSalaManager() {
		return getComponent(SalaManager.class);
	}
	
	/**
	 * Retorna o componente singleton {@link OrgaoJulgadorService} do contexto.
	 * 
	 * @return {@link OrgaoJulgadorService}
	 */
	public static OrgaoJulgadorService getOrgaoJulgadorService() {
		return getComponent(OrgaoJulgadorService.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoVotoDAO} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoVotoDAO}
	 */
	public static SessaoProcessoDocumentoVotoDAO getSessaoProcessoDocumentoVotoDAO() {
		return getComponent(SessaoProcessoDocumentoVotoDAO.class);
	}

	/**
	 * Retorna o componente singleton {@link TaskInstanceUtil} do contexto.
	 * 
	 * @return {@link TaskInstanceUtil}
	 */
	public static TaskInstanceUtil getTaskInstanceUtil() {
		return getComponent(TaskInstanceUtil.class);
	}

	/**
	 * Retorna o componente singleton {@link FacesMessages} do contexto.
	 * 
	 * @return {@link FacesMessages}
	 */
	public static FacesMessages getFacesMessages() {
		return getComponent(FacesMessages.class);
	}

	/**
	 * Retorna o componente singleton {@link TaskInstanceHome} do contexto.
	 * 
	 * @return {@link TaskInstanceHome}
	 */
	public static TaskInstanceHome getTaskInstanceHome() {
		return getComponent(TaskInstanceHome.class);
	}

	/**
	 * Retorna o componente singleton {@link SessaoProcessoDocumentoVotoHome} do contexto.
	 * 
	 * @return {@link SessaoProcessoDocumentoVotoHome}
	 */
	public static SessaoProcessoDocumentoVotoHome getSessaoProcessoDocumentoVotoHome() {
		return getComponent(SessaoProcessoDocumentoVotoHome.class);
	}
	
	/**
	 * Retorna o componente singleton {@link SessaoDAO} do contexto.
	 * 
	 * @return {@link SessaoDAO}
	 */
	public static SessaoDAO getSessaoDAO() {
		return getComponent(SessaoDAO.class);
	}

	/**
	 * Retorna o componente singleton {@link ParametroDAO} do contexto.
	 * 
	 * @return {@link ParametroDAO}
	 */
	public static ParametroDAO getParametroDAO() {
		return getComponent(ParametroDAO.class);
	}

	/**
	 * Retorna o componente singleton {@link FluxoDAO} do contexto.
	 * 
	 * @return {@link FluxoDAO}
	 */
	public static FluxoDAO getFluxoDAO() {
		return getComponent(FluxoDAO.class);
	}

	/**
	 * Retorna o componente singleton {@link SessaoJulgamentoService} do contexto.
	 * 
	 * @return {@link SessaoJulgamentoService}
	 */
	public static SessaoJulgamentoService getSessaoJulgamentoService() {
		return getComponent(SessaoJulgamentoService.class);
	}

	/**
	 * Retorna o componente singleton {@link SessaoPautaProcessoTrfDAO} do contexto.
	 * 
	 * @return {@link SessaoPautaProcessoTrfDAO}
	 */
	public static SessaoPautaProcessoTrfDAO getSessaoPautaProcessoTrfDAO() {
		return getComponent(SessaoPautaProcessoTrfDAO.class);
	}

	/**
	 * Retorna o componente singleton {@link ControleVersaoDocumentoManager} do contexto.
	 * 
	 * @return {@link ControleVersaoDocumentoManager}
	 */
	public static ControleVersaoDocumentoManager getControleVersaoDocumentoManager() {
		return getComponent(ControleVersaoDocumentoManager.class);
	}

	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoBinHome} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoBinHome}
	 */
	public static ProcessoDocumentoBinHome getProcessoDocumentoBinHome() {
		return getComponent(ProcessoDocumentoBinHome.class);
	}

	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoElaboracaoAcordaoCkList} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoElaboracaoAcordaoCkList}
	 */
	public static ProcessoDocumentoElaboracaoAcordaoCkList getProcessoDocumentoElaboracaoAcordaoCkList() {
		return getComponent(ProcessoDocumentoElaboracaoAcordaoCkList.class);
	}

	/**
	 * Retorna o componente singleton {@link ProcessoDocumentoHome} do contexto.
	 * 
	 * @return {@link ProcessoDocumentoHome}
	 */
	public static ProcessoDocumentoHome getProcessoDocumentoHome() {
		return getComponent(ProcessoDocumentoHome.class);
	}
	
	/**
	 * Retorna o componente singleton {@link Expressions} do contexto.
	 * 
	 * @return {@link Expressions}
	 */
	public static Expressions getExpressions() {
		return getComponent(Expressions.class);
	}
    
    /**
     * Retorna o componente singleton {@link PapelManager} do contexto.
     *
     * @return {@link PapelManager}
     */
    public static PapelManager getPapelManager() {
        return getComponent(PapelManager.class);
    }
    
    public static UsuarioLocalizacaoManager getUsuarioLocalizacaoManager() {
    	return getComponent(UsuarioLocalizacaoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link MuralService} do contexto.
     *
     * @return {@link MuralService}
     */
    public static MuralService getMuralService() {
        return getComponent(MuralService.class);
    }

	/**
     * Retorna o componente singleton {@link PlacarSessaoManager} do contexto.
     *
     * @return {@link PlacarSessaoManager}
     */
    public static PlacarSessaoManager getPlacarSessaoManager() {
        return getComponent(PlacarSessaoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link VotoManager} do contexto.
     *
     * @return {@link VotoManager}
     */
    public static VotoManager getVotoManager() {
        return getComponent(VotoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link NotaSessaoJulgamentoManager} do contexto.
     *
     * @return {@link NotaSessaoJulgamentoManager}
     */
    public static NotaSessaoJulgamentoManager getNotaSessaoJulgamentoManager() {
        return getComponent(NotaSessaoJulgamentoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link SessaoProcessoMultDocsVotoManager} do contexto.
     *
     * @return {@link SessaoProcessoMultDocsVotoManager}
     */
    public static SessaoProcessoMultDocsVotoManager getSessaoProcessoMultDocsVotoManager() {
        return getComponent(SessaoProcessoMultDocsVotoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link ProcessoDocumentoLidoManager} do contexto.
     *
     * @return {@link processoDocumentoLidoManager}
     */
    public static ProcessoDocumentoLidoManager getProcessoDocumentoLidoManager() {
        return getComponent(ProcessoDocumentoLidoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link TipoParteConfiguracaoManager} do contexto.
     *
     * @return {@link tipoParteConfiguracaoManager}
     */
    public static TipoParteConfiguracaoManager getTipoParteConfiguracaoManager() {
        return getComponent(TipoParteConfiguracaoManager.class);
	}
	
    /**
     * Retorna o componente singleton {@link OrgaoJulgadorManager} do contexto.
     *
     * @return {@link OrgaoJulgadorManager}
     */
    public static OrgaoJulgadorManager getOrgaoJulgadorManager() {
        return getComponent(OrgaoJulgadorManager.class);
    }

     /** 
     * Retorna o componente singleton {@link ProcessoExpDocCertidaoManager} do contexto.
     *
     * @return {@link processoExpDocCertidaoManager}
     */
    public static ProcessoExpDocCertidaoManager getProcessoExpDocCertidaoManager() {
        return getComponent(ProcessoExpDocCertidaoManager.class);
    }

	/**
     * Retorna o componente singleton {@link VinculacaoDependenciaEleitoralDAO} do contexto.
     *
     * @return {@link VinculacaoDependenciaEleitoralDAO}
     */
	public static VinculacaoDependenciaEleitoralDAO getVinculacaoDependenciaEleitoralDAO() {
		return getComponent(VinculacaoDependenciaEleitoralDAO.class);
	}
    
	/**
	 * Retorna o componente singleton {@link OrgaoJulgadorCargoDAO} do contexto.
	 * 
	 * @return {@link OrgaoJulgadorCargoDAO}
	 */
	public static OrgaoJulgadorCargoDAO getOrgaoJulgadorCargoDAO() {
		return getComponent(OrgaoJulgadorCargoDAO.class);
	}
	
    /**
     * Retorna o componente singleton {@link BlocoJulgamentoManager} do contexto.
     *
     * @return {@link BlocoJulgamentoManager}
     */
    public static BlocoJulgamentoManager getBlocoJulgamentoManager() {
        return getComponent(BlocoJulgamentoManager.class);
    }
    
    /**
     * Retorna o componente singleton {@link BlocoJulgamentoDAO} do contexto.
     *
     * @return {@link BlocoJulgamentoDAO}
     */
    public static BlocoJulgamentoDAO getBlocoJulgamentoDAO() {
        return getComponent(BlocoJulgamentoDAO.class);
    }

	public static ProcessoBlocoDAO getProcessoBlocoDAO() {
		return getComponent(ProcessoBlocoDAO.class);
	}
	
	public static ProcessoBlocoManager getProcessoBlocoManager() {
		return getComponent(ProcessoBlocoManager.class);
	}
	
	public static NotaSessaoBlocoDAO getNotaSessaoBlocoDAO() {
		return getComponent(NotaSessaoBlocoDAO.class);
	}
	
	public static NotaSessaoBlocoManager getNotaSessaoBlocoManager() {
		return getComponent(NotaSessaoBlocoManager.class);
	}
	
	public static ConsultaProcessoAdiadoVistaDAO getConsultaProcessoAdiadoVistaDAO() {
		return getComponent(ConsultaProcessoAdiadoVistaDAO.class);
	}
	
	public static ConsultaProcessoAdiadoVistaManager getConsultaProcessoAdiadoVistaManager() {
		return getComponent(ConsultaProcessoAdiadoVistaManager.class);
	}

	public static BlocoComposicaoManager getBlocoComposicaoManager() {
		return getComponent(BlocoComposicaoManager.class);
	}
	
	public static BlocoComposicaoDAO getBlocoComposicaoDAO() {
		return getComponent(BlocoComposicaoDAO.class);
	}
	
	public static SessaoComposicaoOrdemManager getSessaoComposicaoOrdemManager() {
		return getComponent(SessaoComposicaoOrdemManager.class);
	}
	
	public static VotoBlocoDAO getVotoBlocoDAO() {
		return getComponent(VotoBlocoDAO.class);
	}

	public static VotoBlocoManager getVotoBlocoManager() {
		return getComponent(VotoBlocoManager.class);
	}
	
	public static PopUpVotoBlocoAction getPopUpVotoBlocoAction() {
		return getComponent(PopUpVotoBlocoAction.class);
	}
	
	public static WinVotoBlocoAction getWinVotoBlocoAction() {
		return getComponent(WinVotoBlocoAction.class);
	}
	
	public static EditorEstiloService getEditorEstiloService() {
		return getComponent(EditorEstiloService.class);
	}
	
	public static ListProcessoCompletoBetaAction getListProcessoCompletoBetaAction() {
		return getComponent(ListProcessoCompletoBetaAction.class);
	}
	
	public static PainelSessaoSecretarioSessaoAction getPainelSessaoSecretarioSessaoAction() {
		return getComponent(PainelSessaoSecretarioSessaoAction.class);
	}

	public static PainelDoMagistradoNaSessaoAction getPainelDoMagistradoNaSessaoAction() {
		return getComponent(PainelDoMagistradoNaSessaoAction.class);
	}
	
	public static OrgaoJulgadorDAO getOrgaoJulgadorDAO() {
		return getComponent(OrgaoJulgadorDAO.class);
	}
	
    public static PlacarSessaoAction getPlacarSessaoAction() {
        return getComponent(PlacarSessaoAction.class);
    }

	/**
     * Retorna o componente singleton {@link SessaoPautaProcessoComposicaoManager} do contexto.
     *
     * @return {@link SessaoPautaProcessoComposicaoManager}
     */
	public static SessaoPautaProcessoComposicaoManager getSessaoPautaProcessoComposicaoManager() {
		return getComponent(SessaoPautaProcessoComposicaoManager.class);
	}

	/**
     * Retorna o componente singleton {@link ProcessoParteManager} do contexto.
     *
     * @return {@link ProcessoParteManager}
     */
	public static ProcessoParteManager getProcessoParteManager() {
		return getComponent(ProcessoParteManager.class);
	}

	/**
     * Retorna o componente singleton {@link PessoaProcuradoriaManager} do contexto.
     *
     * @return {@link PessoaProcuradoriaManager}
     */
	public static PessoaProcuradoriaManager getPessoaProcuradoriaManager() {
		return getComponent(PessoaProcuradoriaManager.class);
	}

	/**
	 * Retorna o componente singleton {@link ProcessoPesoParteManager} do contexto.
	 * 
	 * @return {@link ProcessoPesoParteManager}
	 */
	public static ProcessoPesoParteManager getProcessoPesoParteManager() {
		return getComponent(ProcessoPesoParteManager.class);
	}
    
	/**
	 * Retorna o componente singleton {@link CompetenciaManager} do contexto.
	 * 
	 * @return {@link CompetenciaManager}
	 */
	public static CompetenciaManager getCompetenciaManager() {
		return getComponent(CompetenciaManager.class);
	}
    
	/**
	 * Retorna o componente singleton {@link ProcessoTrfConexaoManager} do contexto.
	 * 
	 * @return {@link ProcessoTrfConexaoManager}
	 */
	public static ProcessoTrfConexaoManager getProcessoTrfConexaoManager() {
		return getComponent(ProcessoTrfConexaoManager.class);
	}			
   
	/**
	 * Retorna o componente singleton {@link JurisdicaoManager} do contexto.
	 * 
	 * @return {@link JurisdicaoManager}
	 */
	public static JurisdicaoManager getJurisdicaoManager() {
		return getComponent(JurisdicaoManager.class);
	}						

	/**
	 * Retorna o componente singleton {@link UsuarioLocalizacaoMagistradoServidorManager} do contexto.
	 * 
	 * @return {@link UsuarioLocalizacaoMagistradoServidorManager}
	 */
	public static UsuarioLocalizacaoMagistradoServidorManager getUsuarioLocalizacaoMagistradoServidorManager() {
		return getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
	}
}