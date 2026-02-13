package br.jus.csjt.pje.business.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.context.def.VariableAccess;
import org.jbpm.taskmgmt.def.TaskController;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ResultadoSentenca;
import br.jus.pje.nucleo.entidades.ResultadoSentencaParte;
import br.jus.pje.nucleo.entidades.SolucaoSentenca;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoSolucaoEnum;

/**
 * Classe responsável por gravar o resultado da sentença do processo e as
 * respectivas soluções para as partes.
 * 
 * @author kelly leal/rafael barros
 * @since 1.2.0
 * @category PJE-JT
 * @created 2011-09-09
 */
@Name(ResultadoSentencaService.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class ResultadoSentencaService extends AbstractHome<ResultadoSentenca> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "resultadoSentencaService";

	private static final String SENTENCA_STRING = "sentença";
	private static final String DECISAO_STRING = "decisão";
	private static final String DESPACHO_STRING = "despacho";
	private static final String JUSTICA_TRABALHO = "JT";
	public static final String TIPO_JUSTICA = ComponentUtil.getComponent("tipoJustica");

	LancadorMovimentosService lancadorMovimentosService = ComponentUtil.getComponent(LancadorMovimentosService.NAME);
	ProcessoHome processoHome = ComponentUtil.getComponent(ProcessoHome.NAME);
	ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");

	/**
	 * Método utilizado para gravar o resultado da sentença
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença a ser gravado
	 * 
	 * @author Kelly Leal, Rafael Barros
	 */
	public void gravarResultadoSentenca(ResultadoSentenca resultadoSentenca) {
		getEntityManager().persist(resultadoSentenca);
		getEntityManager().flush();
	}

	/**
	 * Método utilizado para gravar o resultado da sentença de uma parte
	 * (resultado diferenciado por parte)
	 * 
	 * @param resultadoSentencaParte
	 *            resultado da sentença da parte a ser gravado
	 * 
	 * @author Kelly Leal, Rafael Barros
	 */
	public void gravarResultadoSentencaParteDiferenciado(ResultadoSentencaParte resultadoSentencaParte) {
		if (resultadoSentencaParte.getResultadoSentenca().getHomologado()) {
			throw new AplicationException("Sentença já homologada. Ela não pode ser alterada.");
		}

		getEntityManager().persist(resultadoSentencaParte);
		getEntityManager().flush();
	}

	/**
	 * Método utilizado para gravar o resultado da sentença de uma parte
	 * (resultado único - todas as partes autoras terão o mesmo resultado)
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença
	 * @param solucaoSentenca
	 *            solução da sentença
	 * @param valorCondenacao
	 *            valor da condenação
	 * @param valorCustasDispensadas
	 *            valor custas dispensadas
	 * @param valorCustasArrecadar
	 *            valor custas arrecadr
	 * @param assistenciaJudicialGratuita
	 *            assistência judiciária gratuita
	 * 
	 * @author Kelly Leal, Rafael Barros
	 */
	public void gravarResultadoSentencaParteUnico(ResultadoSentenca resultadoSentenca, SolucaoSentenca solucaoSentenca,
			BigDecimal valorCondenacao, BigDecimal valorCustasDispensadas, BigDecimal valorCustasArrecadar,
			Boolean assistenciaJudicialGratuita) {

		if (resultadoSentenca.getHomologado()) {
			throw new AplicationException("Sentença já homologada. Ela não pode ser alterada.");
		}

		for (ProcessoParte processoParteAutor : resultadoSentenca.getProcessoTrf().getListaAutor()) {

			ResultadoSentencaParte resultadoSentencaParte = new ResultadoSentencaParte();

			resultadoSentencaParte.setAssistenciaJudicialGratuita(assistenciaJudicialGratuita);
			resultadoSentencaParte.setSolucaoSentenca(solucaoSentenca);
			resultadoSentencaParte.setResultadoSentenca(resultadoSentenca);
			resultadoSentencaParte.setProcessoParte(processoParteAutor);

			if (valorCondenacao.compareTo(new BigDecimal(0)) < 0) {
				throw new AplicationException("O valor da condenação não pode ser negativo.");
			}
			resultadoSentencaParte.setValorCondenacao(valorCondenacao);

			if (valorCustasDispensadas.compareTo(new BigDecimal(0)) < 0) {
				throw new AplicationException("O valor das custas dispensadas não pode ser negativo.");
			}
			resultadoSentencaParte.setValorCustasDispensadas(valorCustasDispensadas);

			if (valorCustasArrecadar.compareTo(new BigDecimal(0)) < 0) {
				throw new AplicationException("O valor das custas a arrecadar não pode ser negativo.");
			}
			resultadoSentencaParte.setValorCustasArrecadar(valorCustasArrecadar);

			resultadoSentenca.getResultados().add(resultadoSentencaParte);
		}

		getEntityManager().persist(resultadoSentenca);
		getEntityManager().flush();
	}

	/**
	 * Método utilizado para excluir todo o conjunto de resultados de sentenças
	 * de partes de um processo
	 * 
	 * @param resultadoSentenca
	 *            classe que possui um conjunto de resultados de sentenças da
	 *            parte a serem excluídos
	 * 
	 * @author Kelly Leal, Rafael Barros
	 */
	public void excluirTodosResultadoSentencaParte(ResultadoSentenca resultadoSentenca) {

		List<ResultadoSentencaParte> temp = new ArrayList<ResultadoSentencaParte>();
		temp.addAll(resultadoSentenca.getResultados());

		for (ResultadoSentencaParte resultadoSentencaParte : temp) {
			resultadoSentenca.getResultados().remove(resultadoSentencaParte);
			getEntityManager().remove(resultadoSentencaParte);
		}

		getEntityManager().flush();
	}

	/**
	 * Método utilizado para excluir o resultado da sentença não homologado
	 * 
	 * @author Guilherme Bispo
	 */
	public void excluirResultadoSentencaNaoHomologado() {

		if (existeResultadoSentenca()) {

			ResultadoSentenca resultadoSentenca = getUltimoResultadoSentenca(processoTrfHome
					.getInstance().getProcesso());

			getEntityManager().remove(resultadoSentenca);
			getEntityManager().flush();
		}
	}
	
	/**
	 * Método utilizado para excluir o resultado da sentença de uma parte
	 * (resultado diferenciado por parte)
	 * 
	 * @param resultadoSentencaParte
	 *            resultado da sentença da parte a ser excluído
	 * 
	 * @author Kelly Leal, Rafael Barros
	 */
	public void excluirResultadoSentencaParteDiferenciado(ResultadoSentencaParte resultadoSentencaParte) {
		resultadoSentencaParte.getResultadoSentenca().getResultados().remove(resultadoSentencaParte);
		getEntityManager().remove(resultadoSentencaParte);
		getEntityManager().flush();
	}

	/**
	 * 
	 * @param processoTrf
	 * @return null se não encontrado, ou o ResultadoSentenca atrelado ao
	 *         processoTrf
	 */
	public ResultadoSentenca getResultadoSentenca(ProcessoTrf processoTrf) {
		/**
		 * PJE-JT Antonio Lucas 01/06/2013
		 * lógica duplicada
		 */
		try {
			return (ResultadoSentenca) getUltimoResultadoSentenca(processoTrf.getProcesso());
		} catch (NoResultException e) {
			return null;
		}
	}

	
	public List<SolucaoSentenca> getListSolucaoSentencaUnica() {
		return getListSolucaoSentenca(TipoSolucaoEnum.U);
	}

	public List<SolucaoSentenca> getListSolucaoSentencaDiferenciada() {
		return getListSolucaoSentenca(TipoSolucaoEnum.D);
	}
	
	@SuppressWarnings("unchecked")
	private List<SolucaoSentenca> getListSolucaoSentenca(TipoSolucaoEnum tipo) {
		Query q = getEntityManager().createQuery("from SolucaoSentenca ss where ss.tipoSolucao = :tipoSolucao and ss.ativo = true order by ss.ordenacao, ss.descricao");
		q.setParameter("tipoSolucao", tipo);
		return q.getResultList();
	}

	
	/**
	 * Método utilizado para homologar o resultado da sentença e lançar os
	 * devidos movimentos.
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença a ser homologado
	 * 
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	private void coreHomologarResultadoSentenca() {
		ResultadoSentenca resultadoSentenca = null;
		
		try {
			resultadoSentenca = getUltimoResultadoSentenca(processoHome.getInstance());

		} catch (NonUniqueResultException e) {
			throw new AplicationException("Há duas sentenças não homologadas para este processo.");
		} catch (NoResultException e) {
			return;
		}
		
		
		if (resultadoSentenca.getHomologado()) {
			throw new AplicationException("Sentença já homologada. Ela não pode ser homologada novamente.");
		}

		if (resultadoSentenca.getSolucaoUnica()) {
			lancarMovimentoSolucaoUnica(resultadoSentenca);

		} else {
			lancarMovimentoSolucaoDiferenciada(resultadoSentenca);
		}

		resultadoSentenca.setHomologado(true);
		gravarResultadoSentenca(resultadoSentenca);
		
	}

	/**
	 * Método utilizado para homologar o resultado da sentença e lançar os
	 * devidos movimentos.
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença a ser homologado
	 * 
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	public void homologarResultadoSentenca() {
		if (getUltimoSentencaDecisaoDespacho().getTipoProcessoDocumento().getTipoProcessoDocumento()
				.equalsIgnoreCase(SENTENCA_STRING)
				&& isUltimoSentencaDecisaoDespachoAssinado()) {

			coreHomologarResultadoSentenca();
		}
	}

	/**
	 * Método utilizado para homologar o resultado da sentença registrado a
	 * partir das telas de importação do AUD e lançar os devidos movimentos.
	 * 
	 * @author Daniel S. Rocha
	 */
	public void homologarResultadoSentencaAud() {
		coreHomologarResultadoSentenca();
	}

	/**
	 * Método que lança todos os movimentos de acordo com a solução única da
	 * sentença
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença homologada
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	private void lancarMovimentoSolucaoUnica(ResultadoSentenca resultadoSentenca) {

		Boolean lancou = Boolean.FALSE;
		Boolean lancouMovCustas = Boolean.FALSE;

		for (ResultadoSentencaParte resuSentencaParte : resultadoSentenca.getResultados()) {

			String codEvento = resuSentencaParte.getSolucaoSentenca().getCodEvento();

			// Lançar movimento da solução da sentença
			
			if (codEvento != null) {

				// Localizar Evento da Solução
				Evento eventoProcessual = lancadorMovimentosService.getEventoProcessualByCodigoCnj(codEvento);

				// Preenche lista de complementos segmentados
				AplicacaoMovimento aplicacaoMovimento = lancadorMovimentosService.getAplicacaoMovimentoByEvento(eventoProcessual);

				if (aplicacaoMovimento != null && aplicacaoMovimento.getAplicacaoComplementoList().isEmpty()) {

					if (!lancou) {
						MovimentoAutomaticoService.instance().deCodigo(codEvento)
															 .associarAoProcesso(resultadoSentenca.getProcessoTrf().getProcesso())
															 .lancarMovimento();
						
						// Lançar o movimento da Assistência Judicial Gratuita
						lancarMovimentoAssistenciaJudGratuita(resuSentencaParte);

						// Lançar os movimentos das Custas
						lancarMovimentoCustas(resuSentencaParte);
						lancou = Boolean.TRUE;
					}

				} else {

					String codParte = resuSentencaParte.getProcessoParte().getIdProcessoParte() + "";
					String nomeParte = resuSentencaParte.getProcessoParte().getNomeParte();
					ClasseJudicial classeJudicial = resultadoSentenca.getProcessoTrf().getClasseJudicial();

					MovimentoAutomaticoService.preencherMovimento().deCodigo(codEvento)
														           .associarAoProcesso(resultadoSentenca.getProcessoTrf().getProcesso())
														           .comComplementoDeCodigo(1).preencherComCodigo(codParte).preencherComTexto(nomeParte)
														           .comComplementoDeCodigo(5014).preencherComObjeto(classeJudicial)
														           .comComplementoDeCodigo(5023).preencherComTexto("")
														           .lancarMovimento();
					
					// Lançar o movimento da Assistência Judicial Gratuita
					lancarMovimentoAssistenciaJudGratuita(resuSentencaParte);
					
					if (!lancouMovCustas) {
						// Lançar os movimentos das Custas
						lancarMovimentoCustas(resuSentencaParte);
						lancouMovCustas = Boolean.TRUE;
					}

				}


			}
		}
	}

	/**
	 * Método que lança todos os movimentos de acordo com as soluções
	 * diferenciadas da sentença
	 * 
	 * @param resultadoSentenca
	 *            resultado da sentença homologada
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	private void lancarMovimentoSolucaoDiferenciada(ResultadoSentenca resultadoSentenca) {

		for (ResultadoSentencaParte resuSentencaParte : resultadoSentenca.getResultados()) {

			String codEvento = resuSentencaParte.getSolucaoSentenca().getCodEvento();

			// Lançar movimento da solução da sentença
			if (codEvento != null) {

				String codParte = resuSentencaParte.getProcessoParte().getIdProcessoParte() + "";
				String nomeParte = resuSentencaParte.getProcessoParte().getNomeParte();
				ClasseJudicial classeJudicial = resultadoSentenca.getProcessoTrf().getClasseJudicial();

				MovimentoAutomaticoService.preencherMovimento().deCodigo(codEvento)
										  .associarAoProcesso(resultadoSentenca.getProcessoTrf().getProcesso())
										  .comComplementoDeCodigo(1).preencherComCodigo(codParte).preencherComTexto(nomeParte)
										  .comComplementoDeCodigo(5014).preencherComObjeto(classeJudicial)
										  .comComplementoDeCodigo(5023).preencherComTexto("")
										  .lancarMovimento();
			}
			
			// Lançar o movimento da Assistência Judicial Gratuita
			lancarMovimentoAssistenciaJudGratuita(resuSentencaParte);

			// Lançar os movimentos das Custas
			lancarMovimentoCustas(resuSentencaParte);
		}
	}

	/**
	 * Método que lança o movimento Assistência Judicial Gratuita
	 * 
	 * @param resultadoSentencaParte
	 *            resultado da sentença homologada
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	private void lancarMovimentoAssistenciaJudGratuita(ResultadoSentencaParte resultadoSentencaParte) {
		Boolean assistenciaJudicialGratuita = resultadoSentencaParte.getAssistenciaJudicialGratuita();

		if(assistenciaJudicialGratuita == null) {
			return;
		}
		
		String codMovimentoAssistenciaJudGratuita = null;
		
		if (assistenciaJudicialGratuita) {
			codMovimentoAssistenciaJudGratuita = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_AJG_CONCEDIDA;
		} else {
			codMovimentoAssistenciaJudGratuita = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_AJG_NAO_CONCEDIDA;
		}

		// Código = 334 - Descrição = Não concedida a assistência judiciária gratuita a #{nome_da_parte}
		// **************************************************************************************

		String codParte = resultadoSentencaParte.getProcessoParte().getIdProcessoParte() + "";
		String nomeParte = resultadoSentencaParte.getProcessoParte().getNomeParte();

		MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoAssistenciaJudGratuita)
								  .associarAoProcesso(resultadoSentencaParte.getResultadoSentenca().getProcessoTrf().getProcesso())
								  .comProximoComplementoVazio().preencherComCodigo(codParte).preencherComTexto(nomeParte)
								  .lancarMovimento();
	}

	private void lancarMovimentoCustas(ResultadoSentencaParte resultadoSentencaParte) {

		String codMovimentoCustas = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_CUSTAS;

		// Custas dispensadas
		if (resultadoSentencaParte.getValorCustasDispensadas().compareTo(new BigDecimal(0)) > 0) {
		
			// Código = 50073 - Descrição = Arbitradas e #{situação das custas} as custas processuais no valor de #{valor das custas}
			// **************************************************************************************
			MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoCustas)
									  .associarAoProcesso(resultadoSentencaParte.getResultadoSentenca().getProcessoTrf().getProcesso())
									  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(7183) //dispensadas
									  .comProximoComplementoVazio().doTipoLivre().preencherComTexto(resultadoSentencaParte.getValorCustasDispensadas() + "")
									  .lancarMovimento();
		}
		

		// Custas não dispensadas
		if (resultadoSentencaParte.getValorCustasArrecadar().compareTo(new BigDecimal(0)) > 0) {

			// Código = 50073 - Descrição = Arbitradas e #{situação das custas} as custas processuais no valor de #{valor das custas}
			// **************************************************************************************
			MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoCustas)
									  .associarAoProcesso(resultadoSentencaParte.getResultadoSentenca().getProcessoTrf().getProcesso())
									  .comProximoComplementoVazio().doTipoDominio().preencherComElementoDeCodigo(7185) //não dispensadas
									  .comProximoComplementoVazio().doTipoLivre().preencherComTexto(resultadoSentencaParte.getValorCustasArrecadar() + "")
									  .lancarMovimento();
			
		}
	}

	private ResultadoSentenca getUltimoResultadoSentenca(Processo processo) {
		String hql = "select o from ResultadoSentenca o where o.processoTrf.processo.idProcesso = :processo "
				+ "and o.homologado = false and o.resultadoSentencaExcludente is null";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("processo", processo.getIdProcesso());

		return (ResultadoSentenca) query.getSingleResult();
		
	}

	/**
	 * @deprecated lógica duplicada do método {@link #getPossuiSentencaNaoHomologada()}
	 */
	public Boolean existeResultadoSentenca() {
		/**
		 * PJE-JT Antonio Lucas 01/06/2013
		 * lógica duplicada
		 */
		return getPossuiSentencaNaoHomologada();
	}
	
	/**
	 * Método que verifica se o processo possui uma sentença não homologada.
	 * @author reiser - PJEII-965 (jira CNJ)
	 * 
	 * @return Boolean : true se tiver e false caso contrário.
	 */
	public Boolean getPossuiSentencaNaoHomologada() {
		Boolean retorno = true;

		try {
			getUltimoResultadoSentenca(processoTrfHome.getInstance().getProcesso());

		} catch (NonUniqueResultException e) {
			throw new AplicationException("Há duas sentenças não homologadas para este processo.");

		} catch (NoResultException e) {
			retorno = false;
		}
		return retorno;
	}

	/**
	 * Método responsável pela lógica de renderização do botão de
	 * "registrar resultado de sentença" no nó análise de acessoria para a
	 * justiça do trabalho no momento de criação de uma sentença.
	 * 
	 * @return Boolean : true se for para aparecer o botão e false para não
	 *         aparecer.
	 */
	public Boolean getRenderedRegistrarResultadoSentenca() {
		return isSentencaSolucao() && existeVariavelFluxoPodeExibirBotao();
	}
	
	/**
	 * [PJEII-9246] PJE-JT Antonio Lucas 27/06/2013
	 * Método que verifica se existe uma variável de fluxo 
	 * permitindo ou proibindo a exibição do botão registrar sentença
	 * Aceita valores 0 ou 1 (0 para não exibir e 1 para exibir)
	 * Para preservar o comportamento atual, caso a variável não exista,
	 * o botão vai continuar sendo exibido;
	 * @return 
	 */
	private Boolean existeVariavelFluxoPodeExibirBotao() {
		String podeExibir =  
				(String) Contexts.getBusinessProcessContext().get("exibeBotaoRegistrarSentenca");
		//Se não achou a variável
		if (podeExibir == null){
			//então mantém o comportamento atual que é para sempre exibir
			return true;
		}
		else{
			Boolean podeExibirBoolean = new Boolean(podeExibir);
			return podeExibirBoolean;
		}
	}

	private Boolean isSentencaSolucao() {
		Boolean retorno = false;

		TipoProcessoDocumento tipoProcessoDocumentoSelecionado = processoHome.getTipoProcessoDocumento();

		if (TIPO_JUSTICA.equalsIgnoreCase(JUSTICA_TRABALHO) && tipoProcessoDocumentoSelecionado != null
				&& tipoProcessoDocumentoSelecionado.getTipoProcessoDocumento().equalsIgnoreCase(SENTENCA_STRING)) {
			retorno = true;
		}

		return retorno;
	}

	/**
	 * Método responsável pela lógica de renderização do fragmento de
	 * "próxima ação" no nó análise de acessoria para a justiça do trabalho no
	 * momento de criação de uma sentença.
	 * 
	 * @return Boolean : true se for para aparecer o fragmento e false para não
	 *         aparecer.
	 */
	public Boolean getRenderedProximaAcao() {
		Boolean retorno = true;

		/*
		 * Somente validar SE o documento selecionado for sentenca E SE nó de
		 * tarefa estiver usando editorSentencaJt
		 */
		if (isSentencaSolucao() && isUsandoEditorSentenca()) {

			try {
				ResultadoSentenca resultadoSentenca = getUltimoResultadoSentenca(processoTrfHome.getInstance()
						.getProcesso());
				if (resultadoSentenca.getResultados().size() == 0) {
					retorno = false;
				}

			} catch (NonUniqueResultException e) {
				throw new AplicationException("Há duas sentenças não homologadas para este processo.");

			} catch (NoResultException e) {
				retorno = false;
			}
		}
		return retorno;
	}

	private boolean isUsandoEditorSentenca() {
		TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
		if (taskInstance == null) {
			return false;
		}
		try {
			TaskController taskController = taskInstance.getTask().getTaskController();
			List<VariableAccess> list = null;
			if (taskController != null) {
				list = taskController.getVariableAccesses();
				for (VariableAccess var : list) {
					if (var.isReadable() && var.isWritable()) {
						String[] tokens = var.getMappedName().split(":");
						String type = tokens[0];
						if ("textEditGabineteJT".equals(type)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			// swallow
		}

		return false;
	}

	/**
	 * Método que verifica se todos os autores do processo têm resultado de
	 * sentença cadastrado.
	 * 
	 * @return Boolean : true se todos os autores têm sentenças
	 */
	public Boolean getQuantidadeSentencaAutorCorreta() {
		Boolean retorno = true;

		if (isSentencaSolucao()) {

			try {
				ResultadoSentenca resultadoSentenca = getUltimoResultadoSentenca(processoTrfHome.getInstance()
						.getProcesso());

				if (resultadoSentenca.getSolucaoUnica()) {

					// Quando a solução é única ela somente vale para os
					// autores.
					// Por isso aqui busca apenas a lista de autores
					int quantidadePartes = processoTrfHome.getInstance().getListaAutor().size();
					if (quantidadePartes != resultadoSentenca.getResultados().size()) {
						retorno = false;
					}

				} else {

					// Quando a solução é diferenciada, deve-se verificar se
					// todas as partes autoras têm uma sentença
					int quantidadeSentencasAutores = 0;
					for (ResultadoSentencaParte resultadoSentencaParte : resultadoSentenca.getResultados()) {
						if (resultadoSentencaParte.getProcessoParte().getInParticipacao() == ProcessoParteParticipacaoEnum.A) {
							quantidadeSentencasAutores++;
						}
					}

					if (quantidadeSentencasAutores != resultadoSentenca.getProcessoTrf().getListaAutor().size()) {
						retorno = false;
					}
				}

			} catch (NonUniqueResultException e) {
				throw new AplicationException("Há duas sentenças não homologadas para este processo.");

			} catch (NoResultException e) {
				retorno = false;
			}
		}
		return retorno;
	}

	/**
	 * Método que retorna o último documento do tipo sentença, decisão ou
	 * despacho do processo.
	 * 
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	private ProcessoDocumento getUltimoSentencaDecisaoDespacho() {
		List<ProcessoDocumento> lista = ProcessoHome.instance().getProcessoDocumentoList();
		ProcessoDocumento ultimoSentencaDecisaoDespacho = null;
		for (ProcessoDocumento procDocList : lista) {
			if (procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento().equalsIgnoreCase(SENTENCA_STRING)
					|| procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento()
							.equalsIgnoreCase(DECISAO_STRING)
					|| procDocList.getTipoProcessoDocumento().getTipoProcessoDocumento()
							.equalsIgnoreCase(DESPACHO_STRING)) {
				ultimoSentencaDecisaoDespacho = (ultimoSentencaDecisaoDespacho == null ? procDocList
						: ultimoSentencaDecisaoDespacho);
				ultimoSentencaDecisaoDespacho = (ultimoSentencaDecisaoDespacho.getDataInclusao().before(
						procDocList.getDataInclusao()) ? procDocList : ultimoSentencaDecisaoDespacho);
			}
		}
		return ultimoSentencaDecisaoDespacho;
	}

	/**
	 * Método que verifica se o último documento do tipo sentença, decisão ou
	 * despacho foi assinado.
	 * 
	 * @author Emmanuel S. Magalhães, Guilherme D. Bispo
	 */
	public Boolean isUltimoSentencaDecisaoDespachoAssinado() {
		ProcessoDocumento ultimoSentencaDecisaoDespacho = getUltimoSentencaDecisaoDespacho();
		if (ultimoSentencaDecisaoDespacho == null) {
			return Boolean.TRUE;
		} else {
			return ProcessoHome.instance().isDocumentoAssinado(ultimoSentencaDecisaoDespacho.getIdProcessoDocumento());
		}
	}
	
    /**
     * Método que retorna o resultado de uma setença de um processo em que o réu está excluído da lide
     * @param processoTrf
     * @author U006184 - Thiago Oliveira     * 
     * @return null se não encontrado, ou o ResultadoSentenca atrelado ao processoTrf
     */
    public ResultadoSentenca getResultadoSentencaExcluidoDaLide(ProcessoTrf processoTrf, boolean isCPNJ, String documento) {
        String idExcluidoDaLide = ParametroUtil.getFromContext("codExcluidoDaLide", true);

        // Query para verificar se o réu está excluído da lide
        StringBuilder query = new StringBuilder();
        query.append("select rs from ResultadoSentenca rs ");
        query.append("inner join rs.resultados lrs ");
        query.append("where rs.homologado = true ");
        query.append("and rs.processoTrf = :processoTrf ");
        query.append("and rs.resultadoSentencaExcludente is null ");
        query.append("and lrs.solucaoSentenca.idSolucaoSentenca = " + idExcluidoDaLide + " ");

        if(isCPNJ){ // Se for pessoa jurídica
            query.append("and lrs.processoParte.pessoa in (select pj from PessoaJuridica pj ");
            query.append("inner join  pj.pessoaDocumentoIdentificacaoList pd ");
            query.append("where pd.numeroDocumento =  :doc ");  
            query.append("and pd.tipoDocumento.codTipo = 'CPJ')");
        }else{ // Se for pessoa física
            query.append("and lrs.processoParte.pessoa in (select pf from PessoaFisica pf ");
            query.append("inner join  pf.pessoaDocumentoIdentificacaoList pd ");
            query.append("where pd.numeroDocumento =  :doc ");  
            query.append("and pd.tipoDocumento.codTipo = 'CPF')");
        }
        Query q = getEntityManager().createQuery(query.toString());
        q.setParameter("processoTrf", processoTrf);
        q.setParameter("doc",documento);        

        try {
        	return (ResultadoSentenca) q.getSingleResult();
        } catch (NoResultException e) {
        	return null;
        }
    }
    
	/**
	 * Método que verifica se o processo possui uma sentença líquida de acordo com o registro do resultado da sentença.
	 * @return 
	 */
    public Boolean isPossuiSentencaLiquida() {
		String hql = "select o from ResultadoSentenca o where o.processoTrf = :processoTrf "
				+ "and o.homologado = true and o.resultadoSentencaExcludente is null and o.sentencaLiquida = true";
		Query query = getEntityManager().createQuery(hql);
		query.setParameter("processoTrf", processoTrfHome.getInstance());

		if (query.getResultList() == null || query.getResultList().size() <= 0) {
			return false;
		} else {
			return true;
		}
	}
}