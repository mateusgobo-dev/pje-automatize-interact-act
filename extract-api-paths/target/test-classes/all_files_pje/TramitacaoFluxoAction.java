/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view.fluxo;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jbpm.graph.def.Transition;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Classe abstrata de controle de frames de fluxo.
 * 
 * @author cristof
 * @since 1.6.0
 */
public abstract class TramitacaoFluxoAction {
	
	@Logger
	protected transient Log logger;
	
	@In
	protected transient FacesMessages facesMessages;
	
	@In
	protected transient TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(required=false)
	protected TaskInstance taskInstance;
	
	protected String transicaoPadrao;
	
	protected ProcessoTrf processoJudicial;
	
	protected static final String ARQ_PROPERTIES = "entity_messages";
	
	/**
	 * Inicializa o componente, especialmente o processo judicial a ser gerenciado e a transição padrão
	 * de saída, se definida em fluxo.
	 * Caso seja necessária a sobrecarga, recomenda-se que se inclua, na classe derivada, chamada a 
	 * super.init().
	 * 
	 */
	@Create
	public void init(){
		carregaTransicaoPadrao();
		processoJudicial = tramitacaoProcessualService.recuperaProcesso();
		carregarParametros();
	}
	
	/**
	 * Recupera o processo judicial tratado por este componente de controle.
	 * 
	 * @return o processo judicial
	 * @see #init()
	 */
	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}
	
	/**
	 * Recupera a tarefa tratada, se existente.
	 * 
	 * @return a tarefa
	 * @see #init()
	 */
	public TaskInstance getTaskInstance() {
		return taskInstance;
	}
	
	/**
	 * Recupera a transição padrão que tenha sido definida em fluxo para esta tarefa.
	 * 
	 * @return a transição padrão a ser adotada
	 */
	public String getTransicaoPadrao() {
		return transicaoPadrao;
	}
	
	/**
	 * Recupera o mapa de parâmetros de configuração do componente de controle,
	 * sendo a chave o nome da propriedade do componente de controle no qual o 
	 * valor da variável a ela associada nesse mapa será gravado.
	 * 
	 * @return o mapa de parâmetros de configuração.
	 */
	protected abstract Map<String, String> getParametrosConfiguracao();
	
	/**
	 * Carrega, a partir do fluxo, os parâmetros de configuração definidos na action.
	 * Os parâmetros serão recuperados na propriedade chave do mapa retornado de 
	 * {@link #getParametrosConfiguracao()} a partir do valor da variável associada
	 * a essa chave. O valor da variável será recuperado inicialmente da tarefa e,
	 * caso seja nulo, do fluxo.
	 * 
	 */
	protected void carregarParametros(){
		Map<String, String> parametros = getParametrosConfiguracao();
		if(parametros == null){
			return;
		}
		for(Entry<String, String> param: parametros.entrySet()){
			carregarParametro(param.getKey(), param.getValue());
		}
	}
	
	/**
	 * Carrega na propriedade dada o valor da variável de tarefa ou,
	 * se inexistente a variável de tarefa, da variável de fluxo que tem
	 * o nome dado.
	 * 
	 * A atribuição do valor somente será feita se o valor da variável puder
	 * ser atribuído à propriedade. Em outras palavras, se o objeto recuperado 
	 * da variável for de classe idêntica ou derivada da classe de declaração da
	 * propriedade.
	 * 
	 * @param propriedade a propriedade do componente no qual o valor será gravado
	 * @param variavel o nome da variável de tarefa ou de fluxo do qual o valor será
	 * recuperado para gravação.
	 */
	protected void carregarParametro(String propriedade, String variavel){
		Field f = getField(this.getClass(), propriedade);
		if(f == null || taskInstance == null){
			return;
		}else{
			Object v = tramitacaoProcessualService.recuperaVariavelTarefa(variavel);
			
			if(v == null){
				v = tramitacaoProcessualService.recuperaVariavel(variavel);
			}
			
			if(v == null){
				if(f.getType().isAssignableFrom(Boolean.class) || f.getType().isAssignableFrom(boolean.class)){
					try{
						boolean access = f.isAccessible();
						f.setAccessible(true);
						if(f.get(this) == null){
							f.set(this, false);
						}
						f.setAccessible(access);
					} catch (Throwable e) {
						logger.error("Erro ao determinar valor padrão para a propriedade booleana [{0}]: {1}", propriedade, e.getLocalizedMessage());
					}
				}
			}else if(f.getType().isAssignableFrom(v.getClass()) || f.getType().isAssignableFrom(boolean.class)){
				try {
					if(f.isAccessible()){
						f.set(this, v);
					}else{
						f.setAccessible(true);
						f.set(this, v);
						f.setAccessible(false);
					}
				} catch (Throwable e) {
					logger.error("Erro ao tentar carregar o valor da propriedade [{0}] a partir da variável [{1}]: {2}", propriedade, variavel, e.getLocalizedMessage());
				}
			}
		}
	}
	
	private void carregaTransicaoPadrao(){
		if(taskInstance != null){
			Object aux = tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
			if(aux != null){
				if(aux instanceof String){
					for(Transition t: taskInstance.getAvailableTransitions()){
						if(t.getName().equals((String) aux)){
							transicaoPadrao = (String) aux;
							break;
						}
					}
					if(transicaoPadrao == null){
						logger.warn("O nó [{0}] tem configurada como padrão a transição de nome [{1}], mas não há transição tal disponível.", 
								taskInstance.getName(), aux);
					}
				}else{
					logger.warn("O nó [{0}] tem configurada na variável de transição padrão valor de classe diversa de java.lang.String ([{1}]).", 
							taskInstance.getName(), aux.getClass().getCanonicalName());
				}
			}
		}
	}
	
	/**
	 * Recupera o objeto {@link Field} da propriedade informada
	 * na classe dada, buscando nas classes superiores, se existente.
	 * 
	 * @param clazz a classe cuja propriedade se pretende recuperar
	 * @param propriedade a propriedade a ser recuperada
	 * @return o campo ou null, se inexistente a propriedade na classe e em suas
	 * superclasses.
	 */
	private Field getField(Class<?> clazz, String propriedade){
		if(clazz == null){
			return null;
		}
		try{
			return clazz.getDeclaredField(propriedade);
		}catch(NoSuchFieldException e){
			return getField(clazz.getSuperclass(), propriedade);
		}
	}
	
}
