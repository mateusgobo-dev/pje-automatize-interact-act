/**
 * pje
 * Copyright (C) 2010-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.Constants;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.search.Search;

/**
 * Componente abstrato básico para a criação de controladores de tela.
 * O escopo básico destes componentes será {@link ScopeType#CONVERSATION}, mas
 * isso pode e eventualmente deve ser modificado na implementação concreta do controlador,
 * a depender do caso específico.
 * 
 * @author Daniel Miranda
 * @author Paulo Cristovão de Araújo Silva Filho
 *
 * @param <T> o tipo principal a ser tratado neste controlador
 */
@Scope(ScopeType.CONVERSATION)
public abstract class BaseAction<T> implements Serializable {

	private static final long serialVersionUID = -6091139977326355752L;
	
	/**
	 * O objeto de log.
	 */
	@Logger
	protected Log logger;
	
	/**
	 * O objeto de envio de mensagens para a tela.
	 */
	@In
	protected FacesMessages facesMessages;
	
	/**
	 * O objeto que contém o contexto faces da tela.
	 */
	@In(required = false)
	protected FacesContext facesContext;
	
	/**
	 * A instância concreta do objeto atualmente tratado.
	 */
	protected T instance;
	
	/**
	 * Recupera o gerenciador negocial da entidade principal a ser tratado.
	 * 
	 * @return o manager da entidade negocial principal
	 */
	protected abstract BaseManager<T> getManager();
	
	/**
	 * Recupera o {@link EntityDataModel} responsável pela recuperação e 
	 * paginação das entidades eventualmente pesquisadas no controlador.
	 * 
	 * @return o modelo paginável das entidades
	 */
	public abstract EntityDataModel<T> getModel();
	
	/**
	 * Constrói um objeto de recuperação de dados que poderá ser
	 * utilizado na construção do modelo de dados de recuperação deste objeto,
	 * utilizando uma implementação básica que faz uso de métodos específicos
	 * da classe abstrata {@link BaseManager}.
	 * 
	 * @return o recuperador de dados.
	 * @see BaseManager#findById(Object)
	 * @see BaseManager#list(Search)
	 * @see BaseManager#count(Search)
	 * @see BaseManager#getId(Object)
	 */
	protected DataRetriever<T> getRetriever(){
		final BaseManager<T> manager = getManager();
		DataRetriever<T> retriever = new DataRetriever<T>() {
			@Override
			public T findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			@Override
			public List<T> list(Search search) {
				try{
					return manager.list(search);
				}catch (IllegalArgumentException e){
					return Collections.emptyList();
				}
			}
			@Override
			public long count(Search search) {
				return manager.count(search);
			}
			@Override
			public Object getId(T obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}

	/**
	 * Persiste a entidade mantendo sua instância
	 */
	public void persist(){
		persist(false);
	}
	
	/**
	 * Atualiza a entidade contida na instância atual do controlador.
	 */
	public void mergeAndFlush(){
		try{
			getManager().merge(instance);
			getManager().flush();
			facesMessages.addFromResourceBundle(Severity.INFO, "alerta.dadosGravadosComSucesso");
		} catch (PJeBusinessException e){
			reportMessage(e);
			logger.error("Erro ao atualizar " + instance.getClass().getName(), e);
		}
	}
	

	/**
	 * Persiste a entidade contida na instância atual do controlador.
	 * 
	 * @param newInstance booleano indicativo de que, ao final bem
	 * sucedido da persistência, deve ser criado um novo objeto.
	 */
	public void persist(boolean newInstance){
		try{
			getManager().persist(instance);
			if (newInstance)
				newInstance();
			facesMessages.addFromResourceBundle(Severity.INFO, "alerta.dadosGravadosComSucesso");
		} catch (PJeBusinessException e){
			reportMessage(e);
			logger.error("Erro ao persistir " + instance.getClass().getName(), e);
		}
	}

	/**
	 * Persiste a entidade contida na instância atual do controlador, assegurando
	 * que os dados sejam concretamente gravadas no serviço de acesso a dados
	 * quando o modo de flush for manual.
	 * 
	 */
	public void persistAndFlush(){
		persistAndFlush(false);
	}

	/**
	 * Persiste a entidade contida na instância atual do controlador, assegurando
	 * que os dados sejam concretamente gravados no serviço de acesso a dados
	 * quando o modo de flush for manual.
	 * 
	 * @param newInstance booleano indicativo de que, ao final bem
	 * sucedido da persistência, deve ser criado um novo objeto.
	 */
	public void persistAndFlush(boolean newInstance){
		try{
			getManager().persistAndFlush(instance);
			if (newInstance)
				newInstance();
			facesMessages.addFromResourceBundle(Severity.INFO, "alerta.dadosGravadosComSucesso");
		} catch (PJeException e){
			reportMessage(e);
			logger.error("Erro ao persistir " + instance.getClass().getName(), e);
		}

	}

	/**
	 * Remove a entidade atualmente gerenciada pelo controlador.
	 */
	public void remove(){
		try{
			getManager().remove(getInstance());
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		} catch (Exception e){
			reportMessage(e);
			logger.error("Erro ao remover " + instance.getClass().getName(), e);
		}
	}

	/**
	 * Remove a entidade atualmente gerenciada pelo controlador, assegurando
	 * que os dados sejam concretamente gravados no serviço de acesso a dados
	 * quando o modo de flush for manual.
	 */
	public void removeAndFlush(){
		try{
			getManager().remove(getInstance());
			getManager().flush();
			facesMessages.addFromResourceBundle(Severity.INFO, "Alerta_deleted");
		} catch (Exception e){
			reportMessage(e);
			logger.error("Erro ao remover " + instance.getClass().getName(), e);
		}
	}

	/**
	 * Criar e retornar uma nova instâcia da entidade gerenciada, que passa a ser a
	 * apontada pela propriedade {@link #instance}.
	 */
	@SuppressWarnings("unchecked")
	public T newInstance(){
		try{
			instance = (T) getGenericClass(0).newInstance();
			return instance;
		} catch (InstantiationException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Atribui uma instância da entidade como sendo a gerenciada pelo controlador.
	 * 
	 * @param instance a instância a ser atribuída
	 */
	public void setInstance(T instance){
		this.instance = instance;
	}

	/**
	 * Recupera a instância da entidade atualmente gerenciada pelo controlador.
	 * 
	 * @return a instância atualmente gerenciada
	 */
	public T getInstance(){
		return instance;
	}

	/**
	 * Recupera o tipo de entidade da classe genérica atual, dada a posição.
	 *  
	 * @param index a posição, iniciada por 0, do tipo que se pretende identificar.
	 * @return a classe do tipo na inexésima posição da classe genérica
	 */
	private Class<?> getGenericClass(int index){
		Type type = ((ParameterizedType) (getClass().getGenericSuperclass())).getActualTypeArguments()[index];
		return (Class<?>) type;
	}

	/**
	 * Recupera todas as entidades do tipo atual.
	 * 
	 * @return a lista de entidades
	 */
	public List<T> findAll(){
		try{
			return getManager().findAll();
		} catch (PJeBusinessException e){
			reportMessage(e);
			logger.error("Erro no findAll " + instance.getClass().getName(), e);
		}
		return null;
	}

	/**
	 * Recupera a entidade por seu identificador unívoco.
	 * 
	 * @param id o identificador da entidade
	 * @return a entidade, se existente, ou null.
	 */
	public T findById(Object id){
		try{
			return (T) getManager().findById(id);
		} catch (PJeBusinessException e){
			reportMessage(e);
			logger.error("Erro no findById " + instance.getClass().getName(), e);
		}
		return null;
	}

	/**
	 * Limpa o formulário cujo identificador é o dado.
	 * 
	 * @param formName o identificador do fomulário a ser limpo.
	 */
	public void clearForm(String formName){
		if (formName != null && !formName.trim().isEmpty())
			facesContext.getViewRoot().findComponent(formName).getChildren().clear();
	}

	/**
	 * Registra uma mensagem, traduzindo o código pertinente pelo correspondente
	 * texto nos resource bundles da aplicação.
	 * 
	 * @param code o código da mensagem
	 * @param params os parâmetros de complementação da mensagem.
	 */
	public void reportMessage(String code, Object... params){
		if (code.contains(Constants.PREFIXO_ERROR)){
			logger.error(code, params);
			facesMessages.addFromResourceBundle(Severity.ERROR, code, params);
		} else if (code.contains(Constants.PREFIXO_INFO)){
			logger.info(code, params);
			facesMessages.addFromResourceBundle(Severity.INFO, code, params);
		} else if (code.contains(Constants.PREFIXO_WARN)){
			logger.warn(code, params);
			facesMessages.addFromResourceBundle(Severity.WARN, code, params);
		} else if (code.contains(Constants.PREFIXO_FATAL)){
			logger.fatal(code, params);
			facesMessages.addFromResourceBundle(Severity.FATAL, code, params);
		} else{
			logger.warn(code, params);
			facesMessages.addFromResourceBundle(Severity.WARN, code, params);
		}
	}

	/**
	 * Registra uma mensagem decorrente de uma exceção não previamente tratada.
	 * 
	 * @param e a exceção.
	 */
	public void reportMessage(Exception e){
		e.printStackTrace();
		if (e instanceof PJeException){
			reportMessage(((PJeException) e).getCode(), ((PJeException) e).getParams());
		}else{
			reportMessage(Constants.PJE_DEFAULT_ERROR_MSG, e);
		}
	}

	/**
	 * Indica se a instância já está definida neste controlador.
	 * 
	 * @return true, se a propriedade {@link #instance} já estiver definida
	 */
	public boolean isManaged(){
		return (instance != null);
	}
  	
  	/**
  	 * Método responsável pela ação ao clicar na aba Pesquisa
  	 */
  	public void onClickSearchTab() {
  		newInstance();
  	}
  	
  	/**
  	 * Método responsável pela ação ao clicar na aba Formulário
  	 */
  	public void onClickFormTab() {
  		
  	}
}
