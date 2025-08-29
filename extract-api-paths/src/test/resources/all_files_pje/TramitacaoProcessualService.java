package br.jus.cnj.pje.nucleo.service;

import java.util.Date;

import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.jus.cnj.pje.entidades.vo.ProcessoProcessInstanceVO;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * 
 * Interface de tratamento da tramitação de processos judiciais em fluxos.
 *  
 * @author cristof
 *
 */
public interface TramitacaoProcessualService {
	
	/**
	 * Recupera o valor de uma variável dada pretensamente existente no fluxo atual de tramitação do processo.
	 * 
	 * @param nome o nome da variável que se pretende recuperar
	 * @return o valor da variável, se existente, ou null
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual ou seu fluxo de tramitação atual
	 */
	public Object recuperaVariavel(String nome);
	
	public Object recuperaVariavel(ProcessInstance processInstance, String nome) ;
	
	/**
	 * Grava um determinado valor da variável com o nome dado no fluxo atual de tramitação, sobrescrevendo seu 
	 * valor, se já existente.
	 * 
	 * @param nome o nome da variável a ser gravada
	 * @param value o valor a ser gravado
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual ou seu fluxo de tramitação atual
	 */
	public void gravaVariavel(String nome, Object value);
	
	/**
	 * Recupera o valor de uma variável existente da tarefa atual do fluxo de tramitação do processo.
	 * 
	 * @param nome o nome da variável que se pretende recuperar
	 * @return o valor da variável, se existente na tarefa, ou null
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual, seu fluxo de tramitação atual ou sua tarefa atual
	 */
	public Object recuperaVariavelTarefa(String nome);
	
	public Object recuperaVariavelTarefa(TaskInstance ti, String nome);
	
	/**
	 * Grava um determinado valor da variável com o nome dado na tarefa atual, sobrescrevendo seu 
	 * valor, se já existente.
	 * 
	 * @param nome o nome da variável a ser gravada
	 * @param value o valor a ser gravado
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual, seu fluxo de tramitação ou a tarefa à qual a variável será vinculada 
	 */
	public void gravaVariavelTarefa(String nome, Object value);
	
	/**
	 * Apaga a variável de fluxo com o nome dado.
	 * 
	 * @param nome o nome da variável a ser apagada do fluxo
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual ou seu fluxo de tramitação atual
	 */
	public void apagaVariavel(String nome);
	
	/**
	 * Apaga a variável de tarefa com o nome dado.
	 * 
	 * @param nome o nome da variável a ser apagada
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual, seu fluxo de tramitação ou a tarefa da qual a variável deve ser apagada 
	 */
	public void apagaVariavelTarefa(String nome);
	
	/**
	 * Recupera o processo judicial que tem o identificador dado.
	 * 
	 * @param idProcesso o identificador interno do processo judicial a ser recuperado.
	 * @return o processo judicial que tem o identificador dado, ou null, se ele não existir
	 */
	public ProcessoTrf recuperaProcesso(Integer idProcesso);
	
	/**
	 * Recupera o objeto que encapsulo o numero do processo e o idProcessInstance
	 * 
	 * @param idProcesso o identificador interno do processo judicial a ser recuperado.
	 * @param idProcessInstance o identificador do processInstance para ser encapsulado
	 * @return o objeto ProcessoBean que contém o número do processo e o idProcessInstance
	 */
	public ProcessoProcessInstanceVO recuperaProcessoProcessInstanceVO(Integer idProcesso, Long idProcessInstance);
	
	/**
	 * Recupera o processo judicial da instância de fluxo atual.
	 * 
	 * @return o processo judicial que tem o identificador dado, ou null, se ele não existir
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 */
	public ProcessoTrf recuperaProcesso();
	
	/**
	 * Indica se um processo judicial atual teve pedido de apreciação urgente solicitado e ainda não apreciado.
	 * 
	 * @return true, se há pedido de apreciação urgente solicitado e ainda não apreciado
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 */
	public boolean temUrgencia();
	
	
	/**
	 * Indica se um processo judicial com o identificador dado teve pedido de apreciação urgente 
	 * solicitado e ainda não apreciado.
	 * 
	 * @param idProcesso o identificador interno do processo judicial
	 * @return true, se há pedido de apreciação urgente solicitado e ainda não apreciado
	 * 
	 *  @throws IllegalArgumentException, caso o identificador dado não pertença a um processo judicial
	 *  existente na instalação
	 */
	public boolean temUrgencia(Integer idProcesso);
	
	/**
	 * Indica se um processo judicial atual está marcado como sigiloso no sistema.
	 *  
	 * @return true, se o processo judicial está marcado como sigiloso
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 */
	public boolean sigiloso();

	/**
	 * Indica se um processo judicial com o identificador dado está marcado como sigiloso no sistema.
	 *  
	 * @param idProcesso o identificador interno do processo judicial
	 * @return true, se o processo judicial está marcado como sigiloso
	 * 
	 *  @throws IllegalArgumentException, caso o identificador dado não pertença a um processo judicial
	 *  existente na instalação
	 */
	public boolean sigiloso(Integer idProcesso);
	
	/**
	 * Indica se o processo judicial atual tem entre seus assuntos ativos, aquele cujo código é o 
	 * indicado.
	 *   
	 * @param codigoAssunto o código do assunto na instalação, que, ordinariamente, será o código nacional
	 * do assunto no SGT, conforme Resolução CNJ 65
	 * @return true, se o processo tem, entre seus assuntos, aquele com o código indicado.
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 */
	public boolean temAssunto(Integer codigoAssunto);

	/**
	 * Indica se o processo com o identificador dado tem, entre seus assuntos ativos, aquele cujo código é o 
	 * indicado.
	 *   
	 * @param idProcesso o identificador do processo judicial a ser pesquisado  
	 * @param codigoAssunto o código do assunto na instalação, que, ordinariamente, será o código nacional
	 * do assunto no SGT, conforme Resolução CNJ 65
	 * @return true, se o processo tem, entre seus assuntos, aquele com o código indicado.
	 */
	public boolean temAssunto(Integer idProcesso, Integer codigoAssunto);
	
	/**
	 * Indica se o processo tem, entre seus assuntos, algum dos assuntos contidos no grupo com o identificador
	 * dado.
	 * 
	 * @param idGrupo o identificador do grupo
	 * @return true, se o processo tem, entre seus assuntos, ao menos um que faz parte do grupo indicado
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 */
	public boolean temAssuntoDoGrupo(String idGrupo);
	
	/**
	 * Indica se o processo indicado tem, entre seus assuntos, algum dos assuntos contidos no grupo com o identificador
	 * dado.
	 * 
	 * @param idProcesso o identificador do processo
	 * @param idGrupo o identificador do grupo
	 * @return true, se o processo tem, entre seus assuntos, ao menos um que faz parte do grupo indicado
	 * 
	 *  @throws IllegalArgumentException, caso o identificador dado não pertença a um processo judicial
	 *  existente na instalação
	 */
	public boolean temAssuntoDoGrupo(Integer idProcesso, String idGrupo);
	
	/**
	 * Indica se o processo atual tem, entre seus movimentos, o movimento do código indicado. 
	 * 
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @return true, se o movimento tiver sido lançado no processo
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 *  @throws IllegalArgumentException, caso o código do movimento não exista na instalação
	 */
	public boolean temMovimento(String codigoMovimento);
	
	/**
	 * Indica se o processo indicado tem, entre seus movimentos, o movimento do código indicado. 
	 * 
	 * @param idProcesso o identificador do processo judicial
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @return true, se o movimento tiver sido lançado no processo
	 *  @throws IllegalArgumentException, caso o processo judicial ou o código do movimento não exista na instalação
	 */
	public boolean temMovimento(Integer idProcesso, String codigoMovimento);

	/**
	 * Indica se o processo atual tem, entre seus movimentos, o movimento do código indicado. 
	 * A pesquisa será feita até o número de movimentos indicado no parâmetro "limitePesquisa", 
	 * que poderá ser 0 (zero) para o caso de se pretender avaliar todas as movimentações já ocorridas 
	 * no processo.
	 * 
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @return true, se o movimento tiver sido lançado no processo nas últimas (limitePesquisa) movimentações
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 *  @throws IllegalArgumentException, caso o código do movimento não exista na instalação
	 */
	public boolean temMovimento(String codigoMovimento, Date dataLimite);
	
	/**
	 * Indica se o processo indicado tem, entre seus movimentos, o movimento do código indicado. 
	 * A pesquisa será feita até o número de movimentos indicado no parâmetro "limitePesquisa", 
	 * que poderá ser 0 (zero) para o caso de se pretender avaliar todas as movimentações já ocorridas 
	 * no processo.
	 * 
	 * @param idProcesso o identificador do processo judicial
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @return true, se o movimento tiver sido lançado no processo indicados nas últimas (limitePesquisa) movimentações
	 *  @throws IllegalArgumentException, caso o processo judicial ou o código do movimento não exista na instalação
	 */
	public boolean temMovimento(Integer idProcesso, String codigoMovimento, Date dataLimite);
	
	/**
	 * Indica se o processo atual tem, entre seus movimentos, o movimento do código indicado. 
	 * A pesquisa será feita até o número de movimentos indicado no parâmetro "limitePesquisa", 
	 * que poderá ser 0 (zero) para o caso de se pretender avaliar todas as movimentações já ocorridas 
	 * no processo.
	 * A pesquisa também poderá reclamar que a movimentação pesquisada seja especializada pelo
	 * complemento da movimentação. A lista de complementos exigida deverá ser indicada por meio de 
	 * Strings no formato codigoComplemento:valorComplemento. 
	 * 
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @param complementos lista de complementos, no formato "codigoComplemento:valorComplemento" que 
	 * devem necessariamente estar presentes para que o resultado seja verdadeiro
	 * @return true, se o movimento tiver sido lançado no processo e com os complementos indicados nas últimas 
	 * (limitePesquisa) movimentações
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 *  @throws IllegalArgumentException, caso o código do movimento não exista na instalação
	 */
	public boolean temMovimento(String codigoMovimento, Date dataLimite, String...complementos);
	
	/**
	 * Indica se o processo indicado tem, entre seus movimentos, o movimento do código indicado. 
	 * A pesquisa será feita até o número de movimentos indicado no parâmetro "limitePesquisa", 
	 * que poderá ser 0 (zero) para o caso de se pretender avaliar todas as movimentações já ocorridas 
	 * no processo.
	 * A pesquisa também poderá reclamar que a movimentação pesquisada seja especializada pelo
	 * complemento da movimentação. A lista de complementos exigida deverá ser indicada por meio de 
	 * Strings no formato codigoComplemento:valorComplemento. 
	 * 
	 * @param idProcesso o identificador do processo judicial
	 * @param codigoMovimento o código do movimento a ser pesquisado
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @param complementos lista de complementos, no formato "codigoComplemento(int):valorComplemento" que 
	 * devem necessariamente estar presentes para que o resultado seja verdadeiro
	 * @return true, se o movimento tiver sido lançado no processo e com os complementos indicados nas últimas 
	 * (limitePesquisa) movimentações
	 *  @throws IllegalArgumentException, caso o processo judicial ou o código do movimento não exista na instalação
	 */
	public boolean temMovimento(Integer idProcesso, String codigoMovimento, Date dataLimite, String...complementos);
	
	/**
	 * Indica se o processo atual teve lançado algum movimento do grupo identificado.
	 * 
	 * @param idGrupo o identificador do grupo de movimentações
	 * @return true, se o processo tem algum dos movimentos do grupo
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 *  @throws IllegalArgumentException, caso o código do grupo não exista na instalação
	 */
	public boolean temMovimentoDoGrupo(String idGrupo);
	
	/**
	 * Indica se o processo atual teve lançado algum movimento do grupo identificado.
	 *
	 * @param idProcesso o identificador do processo
	 * @param idGrupo o identificador do grupo de movimentações
	 * @return true, se o processo tem algum dos movimentos do grupo
	 *  @throws IllegalArgumentException, caso o processo judicial ou o código do grupo não exista na instalação
	 */
	public boolean temMovimentoDoGrupo(Integer idProcesso, String idGrupo);
	
	/**
	 * Indica se o processo atual teve, entre as (limitePesquisa) últimas movimentações, 
	 * algum dos movimentos pertencentes ao grupo identificado.
	 * 
	 * @param idGrupo o identificador do grupo de movimentações
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @return true, se algum dos últimos movimentos pertence ao grupo indicado
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual 
	 *  @throws IllegalArgumentException, caso o código do grupo não exista na instalação
	 */
	public boolean temMovimentoDoGrupo(String idGrupo, Date dataLimite);
	
	/**
	 * Indica se o processo atual teve, entre as (limitePesquisa) últimas movimentações, 
	 * algum dos movimentos pertencentes ao grupo identificado.
	 * 
	 * @param idProcesso o identificador do processo judicial
	 * @param idGrupo o identificador do grupo de movimentações
	 * @param dataLimite data passada que limita a pesquisa àqueles movimentos ocorridos após a data
	 * @return true, se algum dos últimos movimentos pertence ao grupo indicado
	 *  @throws IllegalArgumentException, caso o processo judicial ou o código do grupo não exista na instalação
	 */
	public boolean temMovimentoDoGrupo(Integer idProcesso, String idGrupo, Date dataLimite);
	
	/**
	 * PJEII-5900 - Retorna a quantidade de processos preventos
	 * @return a quantidade de processos preventos
	 */
	public int contagemPreventoPendentes();
	
	/**
	 * PJEII-5900 - Retorna a quantidade de processos preventos
	 * @return a quantidade de processos preventos
	 */
	public int contagemPreventoPendentes(Integer idProcesso);
	
	/**
	 * Desloca um dado processo judicial para execução de tarefas em outro órgão judicial sem
	 * que isso implique em redistribuição.
	 * 
	 * @return true, se o deslocamento foi bem sucedido
	 */
	public boolean deslocarFluxoParaOrgaoDiverso();

	/**
	 * Desloca um dado processo judicial para execução de tarefas em outro órgão
	 * que pediu vista sem que isso implique em redistribuição.
	 * 
	 * @return true, se o deslocamento foi bem sucedido
	 */
	public boolean deslocarFluxoParaOrgaoVista(); 

	
	/**
	 * Registra em variáveis de fluxo os valores dos ids dos órgão julgador e cargo do vencedor do processo na sessão
	 * 
	 * @return true se o registro foi bem sucedido
	 */
	public boolean registrarOrgaoVencedor(); 

	/**
	 * Desloca um dado processo judicial para execução de tarefas em outro órgão judicial sem
	 * que isso implique em redistribuição.
	 * 
 	 * @param idProcessoJudicial o identificador do processo judicial
	 * @param idOrgao
	 * @param idCargoJudicial
	 * @param idColegiado
	 * @return true, se o deslocamento foi bem sucedido
	 */
	public boolean deslocarFluxoParaOrgaoDiverso(Integer idProcessoJudicial, Integer idOrgao, Integer idCargoJudicial, Integer idColegiado);
	
	/**
	 * Desloca um dado processo judicial para execução de tarefas em outro órgão judicial sem
	 * que isso implique em redistribuição.
	 * 
	 * @param idProcesso o identificador do processo judicial
	 * @return true, se o deslocamento foi bem sucedido
	 */
	public boolean deslocarFluxoParaOrgaoDiverso(Integer idProcesso);
	
    /**
     * Acrescenta ao processo atual uma situação do código dado.
     * 
     * @param codigoTipoSituacao o código do tipo de situação a ser incluído
     */
    public void acrescentarSituacao(String codigoTipoSituacao);
    
    /**
     * Acrescenta ao processo com o código dado uma situação com o tipo do código dado.
     * 
     * @param idProcesso o identificador do processo
     * @param codigoTipoSituacao o código do tipo de situação a ser incluído
     */
    public void acrescentarSituacao(Integer idProcesso, String codigoTipoSituacao);
    
    /**
     * Acrescenta ao processo uma situação com o tipo do código dado.
     * 
     * @param processo o processo que terá acrescida a situação
     * @param codigoTipoSituacao o código do tipo de situação a ser criado
     */
    public void acrescentarSituacao(ProcessoTrf processo, String codigoTipoSituacao);
    
    /**
     * Remove do processo atual a situação do código informado. A remoção significa a marca
     * de que ela se encerrou e não está mais ativa.
     * 
     * @param codigoTipoSituacao o código da situação a ser removido
     */
    public void removerSituacao(String codigoTipoSituacao);
    
    /**
     * Remove do processo com o identificador informado a situação do código dado. A remoção significa a marca
     * de que ela se encerrou e não está mais ativa.
     * 
     * @param idProcesso o identificador do processo
     * @param codigoTipoSituacao o código da situação a ser removido
     */
    public void removerSituacao(Integer idProcesso, String codigoTipoSituacao);
    
    /**
     * Remove do processo dado a situação do código informado. A remoção significa a marca
     * de que ela se encerrou e não está mais ativa.
     * 
     * @param processo o processo a ter removida a situação
     * @param codigoTipoSituacao o código da situação a ser removido
     */
    public void removerSituacao(ProcessoTrf processo, String codigoTipoSituacao);
    
    /**
     * Indica se o o processo atual tem situação ativa atual e válida cujo tipo é do código dado   
     * 
     * @param codigoSituacao o código da situação a pesquisar
     * @return true, se houver situação na situação informada
     */
    public boolean temSituacao(String codigoSituacao);
    
    /**
     * Indica se o o processo com o identificador dado tem situação ativa atual e válida cujo tipo é do código dado   
     * 
     * @param idProcesso o identificador do processo a ser pesquisado
     * @param codigoSituacao o código da situação a pesquisar
     * @return true, se houver situação na situação informada
     */
    public boolean temSituacao(Integer idProcesso, String codigoSituacao);
    
    /**
     * Indica se o o processo tem situação ativa atual e válida cujo tipo é do código dado   
     * 
     * @param processo o processo a ser pesquisado
     * @param codigoSituacao o código da situação a pesquisar
     * @return true, se houver situação na situação informada
     */
    public boolean temSituacao(ProcessoTrf processo, String codigoSituacao);
    
    /**
     * Indica se o o processo tinha a situação válida cujo tipo é do código dado na data de referência    
     * 
     * @param processo o processo a ser pesquisado
     * @param codigoSituacao o código da situação a pesquisar
     * @param dataReferencia a data de referência, caso seja nula, será considerado o momento da consulta
     * @return true, se havia a situação válida na data de referência
     */
    public boolean temSituacao(ProcessoTrf processo, String codigoSituacao, Date dataReferencia);

	
    /**
     * Recupera a transicao padrao da tarefa fornecida atraves da variavel da tarefa frameDefaultLeavingTransition
     * @param taskInstance
     * @return A transicao padrao
     */
    public Transition recuperarTransicaoPadrao(TaskInstance taskInstance);
    
    /**
     * Verifica se o órgao julgador vencedor é o mesmo do relator do processo.
     * @return verdadeiro se o órgão julgador vencedor for igual ao órgão julgador Relator.
     */
    public boolean magistradoRelatorVencedor();
    
    /***
     * Verifica se o voto do relator do processo foi assinado.
     * @return	verdadeiro se o voto existe e foi assinado.
     */
    public boolean votoRelatorAssinado();

	/*
	Verifica se a injecao da processInstance ocorreu a contento
	 */
	public Boolean isProcessInstanceNula();
	
	/**
	 * Indica se a transição argumento está configurada no fluxo como 'dispensaRequeridos'.
	 * 
	 * @param transition
	 * 
	 * @return true/false
	 */
	public Boolean isTransicaoDispensaRequeridos(String transition);
	
	public void setTaskInstance(TaskInstance taskInstance);

    /**
     * Verifica se o objeto TaskInstance é null.
     * @return	verdadeiro se o TaskInstance for null. Falso, caso contrário.
     */
    public boolean isNullTaskInstance();

	public boolean contemVariavel(String nome);

    /**
     * Movimenta o processo para a transição definida
     * 
     * @param transition tarefa destino
     */
	public void movimentarProcessoJudicial(String transition);

	/**
	 * Indica se o ultimo julgamento do processo foi de acordo com os parametros:
	 * 'M' - de Mérito
	 * 'P' - de Preliminar
	 *
	 * @param ProcessoTrf - processoTrf
	 * @param String - letra 'M' ou 'P'
	 * @return true, se o ultimo julgamento for do tipo da letra informado
	 *
	 *  @throws IllegalArgumentException, caso o identificador dado não seja as letras M ou P
	 */
	public boolean ultimoJulgamentoTipo(ProcessoTrf processoTrf, String letra);

	/**
	 * Indica se o ultimo julgamento do processo foi de acordo com os parametros:
	 * 'M' - de Mérito
	 * 'P' - de Preliminar
	 *
	 * @param String - letra 'M' ou 'P'
	 * @return true, se o ultimo julgamento for do tipo da letra informado
	 *
	 *  @throws IllegalArgumentException, caso o identificador dado não seja as letras M ou P
	 */
	public boolean ultimoJulgamentoTipo(String letra);
	
	/**
	 * Recupera uma variável do root se existente.
	 * 
	 * @param nome o nome da variável que se pretende recuperar
	 * @return o valor da variável, se existente na tarefa, ou null
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual, seu fluxo de tramitação atual ou sua tarefa atual
	 */
	public Object recuperaVariavelDoFluxoRaiz(String nome);
	
	/**
	 * Grava uma variável no root, sobrescrevendo seu valor, se já existente.
	 * 
	 * @param nome o nome da variável a ser gravada
	 * @param value o valor a ser gravado
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual, seu fluxo de tramitação ou a tarefa à qual a variável será vinculada 
	 */
	public void gravaVariavelNoFluxoRaiz(String nome, Object value);
	
	/**
	 * Apaga a variável do root com o nome dado.
	 * 
	 * @param nome o nome da variável a ser apagada do root
	 * 
	 * @throws IllegalStateException caso a chamada tenha sido feita sem que, contextualmente, seja possível 
	 * identificar o processo judicial atual ou seu fluxo de tramitação atual
	 */
	public void apagaVariavelDoFluxoRaiz(String nome);
	
}