package br.com.infox.cliente.home.icrrefactory;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;

/**
 * Abstração das actions de icr
 * 
 * @author RodrigoAR
 * 
 * @param <T> o tipo da InformacaoCriminalRelevante
 */
@SuppressWarnings("unchecked")
public abstract class IcrBaseAction<T extends InformacaoCriminalRelevante, J extends IcrBaseManager<T>> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1632880285299846889L;
	private Log log = Logging.getLog(IcrBaseAction.class);
	private InformacaoCriminalRelevanteHome home = InformacaoCriminalRelevanteHome.getHomeInstance();
	private J manager = (J) Component.getInstance(getManagerClass());
	@Out
	// deve ser limpa toda vez que usada
	private List<T> icrList = new ArrayList<T>();

	/**
	 * Ponte entre as actions e as managers de icr.
	 * 
	 * @return
	 */
	protected J getManager(){
		return manager;
	}

	protected InformacaoCriminalRelevanteHome getHome(){
		return home;
	}

	public Log getLog(){
		return log;
	}

	public T getInstance(){
		return (T) home.getInstance();
	}

	public void setInstance(T instance){
		home.setInstance(instance);
	}
	
	public List<T> getIcrList() {
		return icrList;
	}

	public void setIcrList(List<T> icrList) {
		this.icrList = icrList;
	}


	/**
	 * Não sobrescrever, utilize o <code>init()</code>
	 */
	@Create
	public final void internalInit() throws Exception{
		// instancia a subclass da icr
		if (!home.isManaged()){
			try{
				T newInstance = getEntityClass().newInstance();
				newInstance.setTipo(home.getInstance().getTipo());
				newInstance.setProcessoEventoList(home.getInstance().getProcessoEventoList());
				setInstance(newInstance);
			} catch (Exception e){
				addMessage(Severity.ERROR, "Erro ao instanciar " + this.getClass().getName(), e);
			}
		}
		Contexts.getConversationContext().set("action", this);
		init();
	}

	/**
	 * método para inicialização
	 */
	public void init(){
		if (getInstance().getId() == null){
			getInstance().setData(getDataMovimentacao());
			setDtPublicacao(getDataMovimentacao());
		}
	}

	/**
	 * método para setar a data de publicação (se houver) com a data da movimentação.
	 */
	protected void setDtPublicacao(Date dtPublicacao){
		getManager().setDtPublicacao(getInstance(), dtPublicacao);
	}

	/**
	 * Mensagens jsf
	 * 
	 * @param severity
	 * @param key
	 * @param e
	 */
	protected void addMessage(Severity severity, String key, Throwable e, Object... params){
		FacesMessages.instance().addFromResourceBundle(severity, key, params);
		if (e != null){
			if (e instanceof IcrValidationException){
				if (log.isDebugEnabled()){
					log.debug("Erro de validação da icr", e);
				}
			}
			else{
				log.error(e.getMessage(), e);
			}
		}
	}

	protected Class<T> getEntityClass(){
		Class<?> clazz = this.getClass();
		if (!ParameterizedType.class.isAssignableFrom(clazz.getGenericSuperclass().getClass())){
			clazz = clazz.getSuperclass();
		}
		return (Class<T>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	protected Class<J> getManagerClass(){
		Class<?> clazz = this.getClass();
		if (!ParameterizedType.class.isAssignableFrom(clazz.getGenericSuperclass().getClass())){
			clazz = clazz.getSuperclass();
		}
		return (Class<J>) ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[1];
	}

	/**
	 * action do botão incluir inclusão em lote para cada reu selecionado na lista "Reus do Processo" e inclusão simples
	 */
	public void insert(){
		try{
			inicializaParaGravacao();
			// inclusão em lote
			if (getHome().getReusSelecionados() != null && !getHome().getReusSelecionados().isEmpty()){
				getManager().persistAll(getInstance(), getHome().getReusSelecionados());
			}
			// inclusão simples
			else{
				getManager().persist(getInstance());
			}
			addMessage(Severity.INFO, "InformacaoCriminalRelevante_created", null);
			postInsertNavigation();
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null, e.getParams());
		} catch (Exception e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	/**
	 * Sobrescrever para mudar a navegação padrão(ir para a tab de pesquisa) após operações de insert
	 */
	protected void postInsertNavigation(){
		getHome().showTabPesquisa();
	}

	/**
	 * action do botão gravar somente repassa a instance para a manager
	 */
	public void update(){
		try{
			getManager().persist(getInstance());
			addMessage(Severity.INFO, "InformacaoCriminalRelevante_updated", null);
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null, e.getParams());
		} catch (Exception e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	/**
	 * action do botão Novo
	 */
	public void novo(){
		getHome().clear();
		getHome().init();
		getHome().showTabFormulario();
	}

	/**
	 * action do botão proximo passo para cada reu selecionado na lista "Reus do Processo" cria uma instancia de icr e a adiciona na lista "icrList"
	 * para inclusão no momento do cadastro da tipificação de delito
	 */
	public void next(){
		try{
			inicializaParaGravacao();

			if (getHome().getReusSelecionados() != null){
				List<T> icrList = new ArrayList<T>(0);
				for (ProcessoParte reu : getHome().getReusSelecionados()){
					T newInstance = getEntityClass().newInstance();
					PropertyUtils.copyProperties(newInstance,
							getInstance());
					newInstance.setProcessoParte(reu);
					getManager().validate(newInstance);
					if (!getManager().exists(newInstance)){
						icrList.add(newInstance);
					}
					else{
						throw new IcrValidationException("Registro informado já cadastrado no sistema.");
					}
				}

				getIcrList().clear();
				getIcrList().addAll(icrList);
			}
			abreTabProximoPasso();
		} catch (IcrValidationException e){
			addMessage(Severity.ERROR, e.getMessage(), null, e.getParams());
		} catch (Exception e){
			addMessage(Severity.ERROR, e.getMessage(), e);
		}
	}

	public void abreTabProximoPasso(){
		getHome().showTabTipificacaoDelito();
	}

	protected void inicializaParaGravacao(){
		getInstance().setAtivo(true);
	}

	/**
	 * Sobrescrever para adicionar lógica no botão de edição do form
	 * 
	 * @return
	 */
	public boolean canEdit(){
		return true;
	}

	public boolean isManaged(){
		return getHome().isManaged();
	}

	protected Date getDataMovimentacao(){
		return getInstance().getProcessoEventoList().get(0).getDataAtualizacao();
	}

	public boolean exibirBotaoProximoPasso(){
		if (!isManaged()){
			return getHome().exigeTipificacaoDelito();
		}
		return false;
	}

	public boolean exibirBotaoIncluir(){
		return !isManaged() && !getHome().exigeTipificacaoDelito();
	}

	public boolean exibirBotaoGravar(){
		return isManaged();
	}
}
