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
package br.com.itx.component;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.persistence.EntityExistsException;

import org.hibernate.AssertionFailure;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.SeamResourceBundle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.PessoaAssistenteAdvogadoHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.interceptor.IgnoreFacesTransactionMessageEvent;
import br.jus.cnj.pje.nucleo.Constants;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.TipoDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@SuppressWarnings("unchecked")
public abstract class AbstractHome<T> extends EntityHome<T> {

	private static final String MSG_INACTIVE_SUCCESS = "Registro inativado com sucesso.";

	private static final String MSG_REMOVE_ERROR = "Não foi possível excluir.";

	private static final String MSG_REGISTRO_CADASTRADO = "Registro já cadastrado!";

	private static final LogProvider log = Logging.getLogProvider(AbstractHome.class);

	private static final long serialVersionUID = 1L;

	private String tab = null;
	private String goBackUrl = null;
	private String goBackId = null;
	private String goBackTab = null;
	private T oldEntity;
	private UIData dataTable;
	
	private Boolean perfilAtivoInicial;
	private Boolean usuarioAtivoInicial;

	protected String getInactiveSuccess() {
		return MSG_INACTIVE_SUCCESS;
	}

	protected String getRemoveError() {
		return MSG_REMOVE_ERROR;
	}

	protected String getEntityExistsExceptionMessage() {
		return MSG_REGISTRO_CADASTRADO;
	}

	protected String getNonUniqueObjectExceptionMessage() {
		return MSG_REGISTRO_CADASTRADO;
	}

	protected String getConstraintViolationExceptionMessage() {
		return MSG_REGISTRO_CADASTRADO;
	}

	/**
	 * Lista dos campos que não devem ser limpados ao realizar inclusão no
	 * formulario
	 */
	private List<String> lockedFields = new ArrayList<String>();

	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	public String getGoBackUrl() {
		return goBackUrl;
	}

	public void setGoBackUrl(String goBackUrl) {
		this.goBackUrl = goBackUrl;
	}

	public void setGoBackId(String goBackId) {
		this.goBackId = goBackId;
	}

	public String getGoBackId() {
		return goBackId;
	}

	public String getGoBackTab() {
		return goBackTab;
	}

	public void setGoBackTab(String goBackTab) {
		this.goBackTab = goBackTab;
	}

	public T getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	/**
	 * Cria uma instancia nova da entidade tipada, mantendo a instância antiga gerenciada.
	 * 
	 * @see AbstractHome#clearInstance(boolean)
	 */
	public void newInstance() {
		newInstance(null);
	}

	/**
	 * Cria uma instancia nova da entidade tipada, mantendo a instância antiga gerenciada.
	 * A instância antiga é atualizada de forma otimizada utilizando-se o seu ID 
	 * 
	 * @param oldObjectId Chave primária da instancia antiga.
	 */
	public void newInstance(Object oldObjectId) {
		clearInstance(false, oldObjectId);
	}
	
	/**
	 * Cria uma instância nova da entidade tipada, permitindo que o desenvolvedor
	 * solicite que a entidade antiga seja desligada no contexto de gerenciamento JPA.
	 *  
	 * @param detach true, para desligar a entidade antiga do contexto de gerenciamento JPA.
	 */
	public void clearInstance(boolean detach){
		clearInstance(detach, null);
	}
	
	/**
	 * Cria uma instância nova da entidade tipada, permitindo que o desenvolvedor
	 * solicite que a entidade antiga seja desligada no contexto de gerenciamento JPA.
	 * Caso a instância antiga continue no contexto, o seus campos são atualizados.
	 * O oldObjectIdde é utilizado para otimizar a atualização. 
	 *  
	 * @param detach true, para desligar a entidade antiga do contexto de gerenciamento JPA.
	 * @param oldObjectId chave primária da instancia antiga
	 */
	public void clearInstance(boolean detach, Object oldObjectId){
		oldEntity = null;

		if (super.isManaged()) {
			try {
				/**
				 *  Faz com que o hibernate pare de gerenciar o objeto, mantendo suas propriedades para reaproveitamento nos locks.
				 */
				if(detach){
					((Session) getEntityManager().getDelegate()).evict(instance);
				}else{
					if(oldObjectId != null){
						((Session) getEntityManager().getDelegate()).evict(instance);
						instance = getEntityManager().find(getEntityClass(), oldObjectId);
					}else{
						getEntityManager().refresh(instance);
					}
				}
			} catch (Exception e) {
				/**
				 *  Ignora a possível exceção lançada, por exemplo, caso a
				 *  entidade não seja encontrada.
				 */
				log.error("Erro ao limpar a instância atual: [" + e.getLocalizedMessage() + "].", e);
			}
		}
		if (lockedFields.size() > 0) {
			try {
				clearUnlocked();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setId(null);
			clearForm();
			instance = createInstance();
		}
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			try {
				updateOldInstance();
				//TODO Definir a estratégia de log independentemente da utilização de classes home.
				/**
				 * Da forma como foi feito, somente serão logados eventos de SELECT quando:
				 * 1) For utilizada classes home;
				 * 2) For chamado, explicitamente, o método setId() da classe home
				 * 3) O setId() da classe home concreta chamar o super.setId() da AbstractHome()
				 * Events.instance().raiseEvent("logLoadEventNow", instance);
				 */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Remove o componente atual de todos os contextos pelo nome definido no @Name
	 */
	public void removeFromContext() {
		String componentName = Component.getComponentName(this.getClass());
		Contexts.removeFromAllContexts(componentName);
	}

	private void updateOldInstance() {
		updateOldInstance(getInstance());
	}

	private void updateOldInstance(T instance) {
		try {
			oldEntity = (T) EntityUtil.cloneObject(instance, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String remove() {
		String ret = null;
		try {
			ret = super.remove();
			newInstance();
			raiseEventHome("afterRemove");
		} catch (AssertionFailure af) {
			/**
			 *  Bug do Hibernate, esperamos a versão 3.5
			 *  */
		} catch (RuntimeException e) {
			FacesMessages fm = FacesMessages.instance();
			fm.add(StatusMessage.Severity.ERROR, getRemoveError());
			e.printStackTrace();
		}
		return ret;
	}

	public String remove(T obj) {
		setInstance(obj);
		return remove();
	}

	public boolean isEditable() {
		return true;
	}

	/**
	 * Chama eventos antes e depois de persistir a entidade. Caso ocorra um
	 * Exception utiliza um metodo para colocar null no id da entidade
	 */
	@Override
	public String persist() {
		String ret = null;
		String msg = ".persist() (" + getInstanceClassName() + ")";
		try {
			if (beforePersistOrUpdate()) {
				ret = super.persist();
				updateOldInstance();
				afterPersistOrUpdate(ret);
				raiseEventHome("afterPersist");
			}
		} catch (AssertionFailure e) {
			/**
			 *  Resolver o bug do AssertionFailure onde o hibernate consegue
			 *  persistir com sucesso,
			 *  mas lança um erro.
			 */
			log.warn(".persist() (" + getInstanceClassName() + "): " + e.getMessage());
			ret = "persisted";
			updateOldInstance();
			raiseEventHome("afterPersist");
		} catch (EntityExistsException e) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			instance().add(StatusMessage.Severity.ERROR, getEntityExistsExceptionMessage());
			log.error(msg, e);
		} catch (NonUniqueObjectException e) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			instance().add(StatusMessage.Severity.ERROR, getNonUniqueObjectExceptionMessage());
			log.error(msg, e);
		} catch (InvalidStateException e){
		    for(InvalidValue value : e.getInvalidValues()){
                log.info("Classe da instância do bean: " + value.getBeanClass());
                log.info("Possui uma propriedade inválida: " + value.getPropertyName());
                log.info("Com mensagem: " + value.getMessage());
            }
        } catch (AplicationException e) {
			throw new AplicationException("Erro: " + e.getMessage(), e);
		} catch (Exception e) {
			if(e.getCause() instanceof ConstraintViolationException){
				Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
				instance().add(StatusMessage.Severity.ERROR, getConstraintViolationExceptionMessage());
				log.error(msg, e.getCause());
			}else{
				instance().add(StatusMessage.Severity.ERROR, "Erro ao gravar: " + e.getMessage(), e);
				log.error(msg, e);
			}
		}
		if (ret == null) {
			/**
			 *  Caso ocorra algum erro, é criada uma copia do instance sem O Id e
			 *  os List
			 */
			try {
				setInstance(EntityUtil.cloneEntity(getInstance(), false));
			} catch (Exception e) {
				log.warn(".persist() (" + getInstanceClassName() + "): " + Strings.toString(getInstance()), e);
				newInstance();
			}
		}
		return ret;
	}

	/**
	 * Caso o instance não seja null, possua Id não esteja managed, é dado um
	 * find na entidade para que ela fique managed novamente.
	 */
	@Override
	public boolean isManaged() {
		if (getInstance() != null && isIdDefined() && !super.isManaged()) {
			setInstance(getEntityManager().find(getEntityClass(), getId()));
		}
		return super.isManaged();
	}

	/**
	 * Chama eventos antes e depois de atualizar a entidade
	 */
	@Override
	public String update() {
		String ret = null;
		String msg = ".update() (" + getInstanceClassName() + ")";
		try {
			if (beforePersistOrUpdate()) {
				ret = super.update();
				ret = afterPersistOrUpdate(ret);
			}
		} catch (AssertionFailure e) {
			/**
			 *  Resolver o bug do AssertionFailure onde o hibernate consegue
			 *  persistir com sucesso,
			 *  mas lança um erro.
			 */
			log.warn(".update() (" + getInstanceClassName() + "): " + e.getMessage());
			ret = "persisted";
		} catch (EntityExistsException e) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			instance().add(StatusMessage.Severity.ERROR, getEntityExistsExceptionMessage());
			log.error(msg, e);
		} catch (NonUniqueObjectException e) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			instance().add(StatusMessage.Severity.ERROR, getNonUniqueObjectExceptionMessage());
			log.error(msg, e);
		} catch (ConstraintViolationException e) {
			Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
			instance().add(StatusMessage.Severity.ERROR, getConstraintViolationExceptionMessage());
			log.warn(msg, e);
		} catch (Exception e) {
			if(e.getCause() instanceof ConstraintViolationException){
				Events.instance().raiseEvent(IgnoreFacesTransactionMessageEvent.IGNORE_MESSAGE);
				instance().add(StatusMessage.Severity.ERROR, getConstraintViolationExceptionMessage());
				log.error(msg, e.getCause());
			}else{
				instance().add(StatusMessage.Severity.ERROR, "Erro ao gravar: " + e.getMessage(), e);
				log.error(msg, e);
			}
		}
		String name = getEntityClass().getName() + "." + "afterUpdate";
		super.raiseEvent(name, getInstance(), oldEntity);
		if (ret != null) {
			updateOldInstance();
		}
		return ret;
	}

	private void raiseEventHome(String type) {
		raiseEventHome(type, null);
	}

	private void raiseEventHome(String type, T anterior) {
		String name = getEntityClass().getName() + "." + type;
		if (anterior != null) {
			super.raiseEvent(name, getInstance(), anterior);
		} else {
			super.raiseEvent(name, getInstance());
		}
	}

	/**
	 * Método chamado antes de persistir ou atualizar a entidade
	 * 
	 * @return true se a entidade pode ser persistida ou atualizada
	 */
	protected boolean beforePersistOrUpdate() {
		return true;
	}

	/**
	 * Método chamado depois de persistir ou atualizar a entidade
	 * 
	 * @param ret
	 *            é o retorno da operação de persistência
	 */
	protected String afterPersistOrUpdate(String ret) {
		return ret;
	}

	/**
	 * Busca o componente definido por name, se nao achar, cria
	 * 
	 * @param name
	 *            é o nome do componente
	 * @return retorna o componente já no tipo esperado
	 */
	public <C> C getComponent(String name) {
		return (C) Component.getInstance(name);
	}

	/**
	 * Busca o componente definido por name, se nao achar, cria
	 * 
	 * @param name
	 *            é o nome do componente
	 * @param scopeType
	 *            é o escopo em que o componente se encontra
	 * @return retorna o componente já no tipo esperado
	 */
	public <C> C getComponent(String name, ScopeType scopeType) {
		return (C) Component.getInstance(name, scopeType);
	}

	/**
	 * Busca o componente definido por name
	 * 
	 * @param name
	 *            é o nome do componente
	 * @param create
	 *            se true, cria o componente, senão retorna null
	 * @return retorna o componente já no tipo esperado
	 */
	public <C> C getComponent(String name, boolean create) {
		return (C) Component.getInstance(name, create);
	}

	/**
	 * Evento acionado quando o usuário entra na aba de pesquisa.
	 */
	public void onClickSearchTab() {
		newInstance();
	}

	public void onClickFormTab() {
	}

	/**
	 * Metodo para limpar o formulario com o mesmo nome do Home, caso houver
	 * algum Chamado pelo newInstance
	 */
	public void clearForm() {
		StringBuilder formName = new StringBuilder(this.getClass().getSimpleName());
		formName.replace(0, 1, formName.substring(0, 1).toLowerCase());
		formName.replace(formName.length() - 4, formName.length(), "");
		formName.append("Form");
		UIComponent form = ComponentUtil.getUIComponent(formName.toString());
		ComponentUtil.clearChildren(form);
	}

	public void refreshGrid(String gridId) {
		GridQuery g = getComponent(gridId, false);
		if (g != null) {
			g.refresh();
		}
	}

	public String getHomeName() {
		String name = null;
		Name nameAnnotation = this.getClass().getAnnotation(Name.class);
		if (nameAnnotation != null) {
			name = nameAnnotation.value();
		}
		return name;
	}

	public String inactive(T instance) {
		ComponentUtil.setValue(instance, "ativo", false);
		getEntityManager().merge(instance);
		getEntityManager().flush();
		instance().add(StatusMessage.Severity.ERROR, getInactiveSuccess());
		return "update";
	}

	private String getInstanceClassName() {
		return getInstance() != null ? getInstance().getClass().getName() : "";
	}

	/**
	 * Verifica se o registro está na lista para controlar o ícone do cadeado.
	 * 
	 * @param idField
	 *            - Nome do atributo da Entity referente ao campo
	 * @param homeRef
	 *            - Home da Entity do atributo informado
	 */
	public void toggleFields(String idField, AbstractHome<?> homeRef) {
		if (homeRef.getLockedFields().contains(idField)) {
			homeRef.getLockedFields().remove(idField);
		} else {
			homeRef.getLockedFields().add(idField);
		}
	}

	/**
	 * Limpa todos os campos que não foram marcados.
	 * 
	 * @throws Exception
	 */
	public void clearUnlocked() throws Exception {
		PropertyDescriptor[] pds = ComponentUtil.getPropertyDescriptors(getInstance());
		T t = (T) getInstance().getClass().newInstance();
		for (PropertyDescriptor pd : pds) {
			for (String lockedField : lockedFields) {
				/*
				 * Alguns componentes alteram o id do campo e adicionam a String
				 * "field" e/ou "combo" na frente deste id, portanto é
				 * necessário fazer essa verificação e remover as Strings para
				 * que o valor possa ser copiado corretamente.
				 */
				if (lockedField.startsWith("field")) {
					lockedField = lockedField.replaceFirst("field", "");
				}

				if (lockedField.contains("Combo")) {
					lockedField = lockedField.replaceFirst("Combo", "");
				}

				if (lockedField.equals(pd.getName())) {
					ComponentUtil.setValue(t, pd.getName(), pd.getReadMethod().invoke(getInstance()));
				}
			}
		}
		setId(null);
		clearForm();
		instance = t;
	}

	/**
	 * Retorna a lista dos campos que não devem ser limpados.
	 * 
	 * @return
	 */
	public List<String> getLockedFields() {
		return lockedFields;
	}

	/**
	 * Seta a lista dos campos que não devem ser limpados.
	 * 
	 * @param lockedFields
	 *            - Lista dos campos que não devem ser limpados
	 */
	public void setLockedFields(List<String> lockedFields) {
		this.lockedFields = lockedFields;
	}
	
	public Boolean getPerfilAtivoInicial() {
		return perfilAtivoInicial;
	}

	public void setPerfilAtivoInicial(Boolean perfilAtivoInicial) {
		this.perfilAtivoInicial = perfilAtivoInicial;
	}

	public Boolean getUsuarioAtivoInicial() {
		return usuarioAtivoInicial;
	}

	public void setUsuarioAtivoInicial(Boolean usuarioAtivoInicial) {
		this.usuarioAtivoInicial = usuarioAtivoInicial;
	}

	public void reportMessage(String code, Object... params){

		ResourceBundle bundle = SeamResourceBundle.getBundle();
		String message = null;
		try{
			message = bundle.getString(code);
		} catch (MissingResourceException e){
			e.printStackTrace();
			message = code;
		}

		if (!FacesMessages.instance().getCurrentMessages().contains(message)
			&& !FacesMessages.instance().getCurrentGlobalMessages()
					.contains(message)){
			if (code.contains(Constants.PREFIXO_ERROR)){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, code, params);
			}
			else if (code.contains(Constants.PREFIXO_INFO)){
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, code, params);
			}
			else if (code.contains(Constants.PREFIXO_WARN)){
				FacesMessages.instance().addFromResourceBundle(Severity.WARN, code, params);
			}
			else if (code.contains(Constants.PREFIXO_FATAL)){
				FacesMessages.instance().addFromResourceBundle(Severity.FATAL, code, params);
			}
			else{
				FacesMessages.instance().addFromResourceBundle(Severity.WARN, code, params);
			}
		}
	}
	
	public void reportMessage(Exception e){

		e.printStackTrace();

		if (e instanceof PJeException){
			reportMessage(((PJeException) e).getCode(), ((PJeException) e).getParams());
		}
		else{
			reportMessage(Constants.PJE_DEFAULT_ERROR_MSG, e);
		}
	}
	
	/**
	  * Função auxiliar para retornar um objeto Tipo de Documento Identificação
	  * conforme parâmetros do códico do tipo de documento ("CPF", "TIT") e do tipo 
	  * de pessoa (TipoPessoaEnum.F - Pessoa Física, TipoPessoaEnum.J - Pessoa
	  * Jurídica)
	  * @param String codigo, TipoPessoaEnum tipoPessoa
	  * @return TipoDocumentoIdentificacao
	  * @see PessoaAssistenteAdvogadoHome PessoaAssistenteProcuradoriaHome
	  */
	 public TipoDocumentoIdentificacao getTipoDocumentoIdentificacao(String codigo, TipoPessoaEnum tipoPessoa){
	  TipoDocumentoIdentificacaoManager doc = (TipoDocumentoIdentificacaoManager) Component.getInstance("tipoDocumentoIdentificacaoManager");
	  return doc.carregarTipoDocumentoIdentificacao(codigo, tipoPessoa);
	 }
	 

	/**
	 * Método responsável por atualizar a especialização da instância, se a
	 * situação do perfil for marcada como ativo ou desespecializar se a
	 * situação do perfil for marcada como inativo.
	 * 
	 * @param perfilAtivo
	 *            responsável pela verificação se o perfil será especializado ou
	 *            desespecializado
	 * @param pessoa
	 *            a pessoa a ser feita a operação
	 * @param clazz
	 *            as classes especializadas a serem respeitadas
	 * @throws PJeBusinessException
	 *             exceção lançada para ser tradada no método
	 *             <code>update()</code> da classe Home.
	 */
	@SuppressWarnings("hiding")
	public <T extends PessoaFisicaEspecializada> void atualizarEspecializacao(Boolean perfilAtivo, PessoaFisica pessoa, Class<T> clazz) throws PJeBusinessException {
		PessoaService pessoaService = ComponentUtil.<PessoaService>getComponent("pessoaService");
		if (perfilAtivo) {
			pessoaService.especializa(pessoa, clazz);
		} else if (!perfilAtivo) {
			pessoaService.desespecializa(pessoa, clazz);
		}
	}

	/**
	 * Método responsável por identificar se no cadastro houve mudança nos
	 * campos de situação do perfil e/ou cadastro geral do usuário. Ambas opções
	 * afetam diretamente o perfil e a localização.
	 * 
	 * @param perfilAtivoInicial
	 *            valor inicial do campo <i>Situação deste perfil</i>.
	 * @param usuarioAtivoInicial
	 *            valor inicial do campo <i>Situação do cadastro geral do
	 *            usuário</i>.
	 * @return <code>Boolean</code>, <code>true</code> se for verificado que o
	 *         campo mudou o valor do seu estado inicial.
	 */
	public Boolean isCadastroAlterado() {
		if (instance instanceof PessoaFisicaEspecializada) {
			PessoaFisicaEspecializada pessoa = (PessoaFisicaEspecializada) instance;
			return (!pessoa.isPerfilAtivo().equals(this.perfilAtivoInicial) || !pessoa.getAtivo().equals(this.usuarioAtivoInicial));
		} else {
			PessoaFisica pessoa = (PessoaFisica) instance;
			return (!pessoa.getAtivo().equals(this.usuarioAtivoInicial));
		}
	}
	
	protected void limparMensagens() {
		StatusMessages.instance().clearGlobalMessages();
		FacesMessages.instance().clearGlobalMessages();
	}

	public UIData getDataTable() {
		return dataTable;
	}

	public void setDataTable(UIData dataTable) {
		this.dataTable = dataTable;
	}

}
