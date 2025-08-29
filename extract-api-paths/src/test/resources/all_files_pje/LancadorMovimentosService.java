package br.jus.csjt.pje.business.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.convert.ConverterException;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.BusinessProcess;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.remoting.wrapper.ConversionException;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ValorComplementoSegmentado;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.component.tree.ComplementoBean;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.MovimentoBean;
import br.com.infox.ibpm.component.tree.ValorComplementoBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.amqp.model.dto.ProcessoEventoCloudEvent;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.AMQPEventManager;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.service.AutomacaoTagService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService.MovimentoBuilder.ComplementoBuilder;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;
import br.jus.pje.nucleo.entidades.lancadormovimento.ComplementoSegmentado;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoComDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoDinamico;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoLivre;
import br.jus.pje.nucleo.enums.SujeitoAtivoEnum;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Gravar o lançamento de movimentos com seus complementos.
 */
@Name(LancadorMovimentosService.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class LancadorMovimentosService extends AbstractHome<ProcessoEvento> {
	public static final String NAME = "lancadorMovimentosService";

	public static final String COMPLEMENTO_DESTINO = "destino";
	public static final String COMPLEMENTO_MOTIVO_REMESSA = "motivo_da_remessa";
	
	public static final String EL_DEFAULT_MOVIMENTO_RETIFICACAO_CLASSE = "#{preencherMovimento.deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_RETIFICACAO_CLASSE).comComplementoDeNome('classe_anterior').preencherComTexto(classeAntiga).comComplementoDeNome('classe_nova').preencherComTexto(classeNova).lancarMovimento()}";

	private static final long serialVersionUID = 1L;

	public static final String SEPARADOR_INICIO = "#{";
	public static final String SEPARADOR_FIM = "}";

	public static final String SUJEITO_ATIVO = SujeitoAtivoEnum.M.getLabel();

	/**
	 * 
	 * Método utilizado para lançamento de um determinado movimento 
	 * 
	 * @param evento
	 *            Movimento a ser lançado
	 * @param complementoSegmentadoList
	 *            Lista de valores de complementos a serem associados ao
	 *            movimento
	 * @param processoDocumento
	 *            Documento ao qual o movimento deve ser associado
	 * @param processo
	 *            Processo ao qual o movimento deve ser associado
	 * @param tarefa
	 *            Tarefa à qual o movimento deve ser associado
	 * @param idJbpmTask
	 *            Identificador da Tarefa do Fluxo
	 * @param idProcessInstance
	 *            Identificador da instância de processo
	 * @param usuario 
	 * 			  Usuario que lancará o movimento
	 * @return ProcessoEvento gerado, representando o movimento lançado
	 * @deprecated Utilizar o MovimentoAutomaticoService para lancamento automatico de movimentos
	 */
	@Deprecated
	public ProcessoEvento lancarMovimento(Evento evento,
			List<ComplementoSegmentado> complementoSegmentadoList,
			ProcessoDocumento processoDocumento, Processo processo,
			Tarefa tarefa, Long idJbpmTask, Long idProcessInstance) {
		
		/*
		 * Alterado para fazer a busca no entity manager passando a entidade
		 * Usuário como parametro, pois ao usar Authenticator.getUsuarioLogado()
		 * o sistema estava lançando uma exception por estar sendo retornado um proxy
		 * do contexto (tela de assinar atas de audiência
		 */
		Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		Usuario usuarioSistema = null;
		if (usuario != null) {
			usuarioSistema = EntityUtil.getEntityManager().find(Usuario.class, usuario.getIdUsuario());
		} else {
			//Caso o lançador seja executado por algum job
			usuarioSistema = ParametroUtil.instance().getUsuarioSistema();
		}
		
		return lancarMovimento(evento,
				complementoSegmentadoList,
				processoDocumento, processo,
				tarefa, idJbpmTask, idProcessInstance,usuarioSistema);
	}
	
	private Usuario getUsuarioLogadoOuSistema() {
		Usuario usuario = Authenticator.getUsuarioLogado();
		if(usuario == null) {
			usuario = Authenticator.getUsuarioSistema();
		}
		return usuario;
	}
	
	/**
	 * Método utilizado para lançamento de um determinado movimento quando utilizado em JOBS.
	 * Necessário passar o usuário que sera usado no movimento
	 * 
	 * @param evento
	 *            Movimento a ser lançado
	 * @param complementoSegmentadoList
	 *            Lista de valores de complementos a serem associados ao
	 *            movimento
	 * @param processoDocumento
	 *            Documento ao qual o movimento deve ser associado
	 * @param processo
	 *            Processo ao qual o movimento deve ser associado
	 * @param tarefa
	 *            Tarefa à qual o movimento deve ser associado
	 * @param idJbpmTask
	 *            Identificador da Tarefa do Fluxo
	 * @param idProcessInstance
	 *            Identificador da instância de processo
	 * @param usuario 
	 * 			  Usuario que lancará o movimento
	 * @return ProcessoEvento gerado, representando o movimento lançado
	 * @deprecated Utilizar o MovimentoAutomaticoService para lancamento automatico de movimentos
	 */
	@Deprecated
	public ProcessoEvento lancarMovimento(Evento evento,
			List<ComplementoSegmentado> complementoSegmentadoList,
			ProcessoDocumento processoDocumento, Processo processo,
			Tarefa tarefa, Long idJbpmTask, Long idProcessInstance, Usuario usuario) {
		
		ProcessoEvento movimentoProcesso = criarMovimentoProcesso(evento, complementoSegmentadoList, processoDocumento, processo, tarefa, idJbpmTask, idProcessInstance, usuario);

		// 1. buscar o objeto AplicacaoMovimento desejado
		String orgaoJusticaContexto = ParametroUtil.instance().getTipoJustica();
		String codigoAplicacaoClasse = ParametroUtil.instance().getCodigoInstanciaAtual();
		boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();
		
		StringBuilder textoFinalInterno = new StringBuilder("");
		StringBuilder textoFinalExterno = new StringBuilder("");
		
		getEntityManager().refresh(evento);
		if(processoDocumento != null) getEntityManager().refresh(processoDocumento.getProcessoTrf());

		// verificar OrgaoJustica, AplicacaoClasse e SujeitoAtivo
		Query q = getEntityManager().createQuery(getHqlAplicacaoMovimento())
				.setParameter("idEvento", evento.getIdEvento())
				.setParameter("tipoJustica", orgaoJusticaContexto)
				.setParameter("codigoAplicacaoSistema", codigoAplicacaoClasse)
				.setParameter("isPrimeiroGrau", isPrimeiroGrau)
				.setParameter("sujeitoAtivo", SUJEITO_ATIVO);
		
		AplicacaoMovimento aplicacaoMovimento = (AplicacaoMovimento) EntityUtil.getSingleResult(q);

		movimentoProcesso.setTextoParametrizado(aplicacaoMovimento.getTextoParametrizado());
		textoFinalInterno.append(aplicacaoMovimento.getTextoParametrizado());
		textoFinalExterno.append(aplicacaoMovimento.getTextoParametrizado());

		for (AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimento
				.getAplicacaoComplementoList()) {
			complementarTextoFinal(textoFinalInterno, 
					textoFinalExterno, complementoSegmentadoList, 
					aplicacaoComplemento.getTipoComplemento(),
					aplicacaoComplemento.getMultivalorado(),
					aplicacaoComplemento.getVisibilidadeExterna());
		}

		movimentoProcesso.setTextoFinalExterno(textoFinalExterno.toString());
		movimentoProcesso.setTextoFinalInterno(textoFinalInterno.toString());

		if (complementoSegmentadoList != null) {
			for (ComplementoSegmentado complementoSegmentado : complementoSegmentadoList) {
				complementoSegmentado.setMovimentoProcesso(movimentoProcesso);
			}
		}

		getEntityManager().persist(movimentoProcesso);
		getEntityManager().flush();
		return movimentoProcesso;
	}
	
	/**
	 * Método utilizado para lançamento de um movimento quando utilizado {@link MovimentoAutomaticoService}.
	 * 
	 * @param movimentoBuilder
	 * @return ProcessoEvento gerado, representando o movimento lançado
	 */
	protected ProcessoEvento lancarMovimento(MovimentoAutomaticoService.MovimentoBuilder movimentoBuilder) {
		return this.lancarMovimento(movimentoBuilder, true);
	}
	
	/**
	 * Método utilizado para lançamento de um movimento quando utilizado {@link MovimentoAutomaticoService}.
	 * 
	 * @param movimentoBuilder
	 * @return ProcessoEvento gerado, representando o movimento lançado
	 */
	protected ProcessoEvento lancarMovimento(MovimentoAutomaticoService.MovimentoBuilder movimentoBuilder, boolean autoFlush) {
		return this.lancarMovimento(this.converteEmProcessoEvento(movimentoBuilder), autoFlush);
	}

	protected ProcessoEvento lancarMovimento(EventoBean eventoBean, boolean autoFlush) {
		org.jbpm.graph.exe.ProcessInstance processInstance = org.jboss.seam.bpm.ProcessInstance.instance();
		ProcessoEvento movimentoProcesso = this.converteEmProcessoEvento(processInstance, eventoBean);
		
		return this.lancarMovimento(movimentoProcesso, autoFlush);
	}
	
	public void lancarMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance) {
		this.lancarMovimentosTemporariosAssociandoAoDocumento(processInstance, null);
	}

	public void lancarMovimentosTemporariosAssociandoAoDocumento(org.jbpm.graph.exe.ProcessInstance processInstance, ProcessoDocumento processoDocumento) {
		List<ProcessoEvento> listaProcessoEventosTemporarios = this.getProcessoEventoListTemporario(processInstance);
		if(CollectionUtilsPje.isNotEmpty(listaProcessoEventosTemporarios)) {
			for (ProcessoEvento processoEvento : listaProcessoEventosTemporarios) {
				this.lancarMovimento(processoEvento, processoDocumento, true);
			}
			
			this.apagarMovimentosTemporarios(processInstance);
		}
	}
	
	protected ProcessoEvento lancarMovimento(ProcessoEvento movimentoProcesso, boolean autoFlush) {
		return this.lancarMovimento(movimentoProcesso, null, autoFlush);
	}
	
	/**
	 * Lança o evento, se indicado um processoDocumento, sobrescreve a informação do movimento com a informação do {@link ProcessoDocumento}, 
	 * se indicado também fará o flush imediato, após  persistir o movimento no processo
	 * 
	 * @param movimentoProcesso
	 * @param processoDocumento
	 * @param autoFlush
	 * @return
	 */
	protected ProcessoEvento lancarMovimento(ProcessoEvento movimentoProcesso, ProcessoDocumento processoDocumento, boolean autoFlush) {
		if(movimentoProcesso != null) {
			// os movimentos não devem ser lançados retroativamente
			movimentoProcesso.setDataAtualizacao(new Date());
			if(processoDocumento != null) {
				movimentoProcesso.setProcessoDocumento(processoDocumento);
			}
			getEntityManager().persist(movimentoProcesso);
			if (autoFlush)
				getEntityManager().flush();
			
			enviarMensagem(movimentoProcesso);
			Events.instance().raiseTransactionSuccessEvent(AutomacaoTagService.EVENTO_AUTOMACAO_TAG, movimentoProcesso.getProcesso().getIdProcesso());
		}	
		return movimentoProcesso;			
	}

	/**
	 * Envia uma mensagem para o serviço de mensageria.
	 * 
	 * @param processoEvento ProcessoEvento
	 */
	protected void enviarMensagem(ProcessoEvento processoEvento) {
		AMQPEventManager amqpManager = AMQPEventManager.instance();
		amqpManager.enviarMensagem(processoEvento, ProcessoEventoCloudEvent.class);
	}

	private ProcessoEvento converteEmProcessoEvento(MovimentoAutomaticoService.MovimentoBuilder movimentoBuilder) {
		List<String> errosPreRequisitos = this.getErrosPreRequisitosLancamento(movimentoBuilder);
		if (errosPreRequisitos.size() > 0) {
			LogProvider log = Logging.getLogProvider(LancadorMovimentosService.class);
			for (String erro : errosPreRequisitos){
				log.error(erro);
			}
			return null;
		}
		
		return this.getMovimentoProcesso(movimentoBuilder);
	}
	
	private List<EventoBean> converteEmEventoBean(List<ProcessoEvento> processoEventoList){
		List<EventoBean> eventoBeanList = new ArrayList<EventoBean>();
		for (ProcessoEvento processoEvento : processoEventoList) {
			EventoBean eventoBean = new EventoBean();
			eventoBean.setIdEvento(processoEvento.getEvento().getIdEvento());
			eventoBean.setCodEvento(processoEvento.getEvento().getCodEvento());
			eventoBean.setIdJbpmTask(processoEvento.getIdJbpmTask());
			eventoBean.setDescricaoMovimento(processoEvento.getDescricao());
			eventoBean.setDescricaoCompletaMovimento(processoEvento.getTextoFinalInterno());
			String caminhoCompleto = processoEvento.getEvento().getCaminhoCompleto();
			if(StringUtil.isEmpty(caminhoCompleto)) {
				caminhoCompleto = processoEvento.getEvento().getPathDescription();
				if(StringUtil.isEmpty(caminhoCompleto)) {
					caminhoCompleto = processoEvento.getEvento().getEvento();
				}
			}
			eventoBean.setDescricaoCaminhoCompletoMovimento(caminhoCompleto);
			eventoBean.setGlossario(processoEvento.getEvento().getGlossario());
			
			eventoBean.setQuantidade(1);
			eventoBean.setMultiplo(true);
			eventoBean.setExcluir(true);

			if(processoEvento.getProcessoDocumento() != null) {
				eventoBean.setIdProcessoDocumento(processoEvento.getProcessoDocumento().getIdProcessoDocumento());
				eventoBean.setIdTipoProcessoDocumento(processoEvento.getProcessoDocumento().getTipoProcessoDocumento().getIdTipoProcessoDocumento());
			}

			if (!processoEvento.getComplementoSegmentadoList().isEmpty()) {
				// Movimento com complemento
				eventoBean.setTemComplemento(Boolean.TRUE);
				eventoBean.setMovimentoBeanList(this.converteEmMovimentoBeanList(
						processoEvento.getComplementoSegmentadoList()));

				eventoBean.setValido(processoEvento.getComplementoSegmentadoList().stream().parallel()
						.filter(p -> StringUtils.isNotEmpty(p.getValorComplemento())).findAny().isPresent());

			} else {
				// Movimento sem complemento
				eventoBean.setTemComplemento(Boolean.FALSE);
				eventoBean.setMovimentoBeanList(new ArrayList<>());

				// Valido por padrão, não necessita usuário intervir
				eventoBean.setValido(Boolean.TRUE);
			}

			eventoBeanList.add(eventoBean);
		}
		
		return eventoBeanList;
	}
	
	private ProcessoEvento converteEmProcessoEvento(org.jbpm.graph.exe.ProcessInstance processInstance, EventoBean eventoBean) {
		Processo processo = ProcessoHome.instance().getInstance();
		Long idProcessInstance = processInstance.getId();
		ProcessoDocumento processoDocumento = null;
		Usuario usuario = null;
		Long idJbpmTask = eventoBean.getIdJbpmTask();
		if (idJbpmTask == null) {
			idJbpmTask = BusinessProcess.instance().getTaskId();
		}
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = ManagedJbpmContext.instance().getTaskInstance(idJbpmTask);
		
		Tarefa tarefa = null;
		
		if (processo == null){
			throw new ConverterException("Não foi possível encontrar o processo relacionado.");
		}
		Integer idMinuta = JbpmUtil.instance().recuperarIdMinutaEmElaboracao(taskInstance);

		processoDocumento = EntityUtil.find(ProcessoDocumento.class, idMinuta);
		usuario = this.getUsuarioLogadoOuSistema();
		
		tarefa = JbpmUtil.getTarefa(taskInstance.getName(), taskInstance.getProcessInstance().getProcessDefinition().getName());

		List<String> errosPreRequisitos = this.getErrosPreRequisitosLancamento(eventoBean, processo);
		if (errosPreRequisitos.size() > 0) {
			LogProvider log = Logging.getLogProvider(LancadorMovimentosService.class);
			for (String erro : errosPreRequisitos){
				log.error(erro);
			}
			return null;
		}
		
		return this.getProcessoEvento(eventoBean, processo, processoDocumento, tarefa, idJbpmTask, idProcessInstance, usuario);
	}
	
	private List<ComplementoSegmentado> converteEmComplementoSegmentado(MovimentoBean movimentoBean, ParserTextoMovimento parserTextoMovimento) {
		List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();
		
		for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
			TipoComplemento tp = EntityUtil.find(TipoComplemento.class,complementoBean.getIdTipoComplemento());
			ComplementoSegmentado complementoSegmentado = new ComplementoSegmentado();
			complementoSegmentado.setOrdem(0);
			complementoSegmentado.setTipoComplemento(tp);
			complementoSegmentado.setTexto(complementoBean.getLabel());
			
			complementoSegmentado.setValorComplemento(montarValorComplementoMultivalorado(complementoBean.getValorComplementoBeanList()));
			Boolean visibilidadeExterna = false;
			Boolean multivalorado = false;
			
			AplicacaoComplemento aplicacaoComplemento = this.geraAplicacaoComplemento(
					parserTextoMovimento, 
					tp.getCodigo(),
					tp.getNome(),
					complementoSegmentado.getValorComplemento());

			if(aplicacaoComplemento != null) {
				visibilidadeExterna = aplicacaoComplemento.getVisibilidadeExterna();
				multivalorado = aplicacaoComplemento.getMultivalorado();
			}
			complementoSegmentado.setVisibilidadeExterna(visibilidadeExterna);
			complementoSegmentado.setMultivalorado(multivalorado);
			complementoSegmentado.setMovimentoProcesso(null);
			complementoSegmentadoList.add(complementoSegmentado);
		}
		return complementoSegmentadoList;
	}

	private String montarValorComplementoMultivalorado(List<ValorComplementoBean> valorComplementoBeanList) {
		StringBuilder valorComplemento = new StringBuilder("");
		
		if (valorComplementoBeanList.size() > 0) {
			valorComplemento.append(valorComplementoBeanList.get(0).getValor());

			// pega os complementos considerando a ordem
			for (int i = 1; i < valorComplementoBeanList.size() - 1; i++) {
				valorComplemento.append(", " + valorComplementoBeanList.get(i).getValor());
			}

			if (valorComplementoBeanList.size() > 1) {
				valorComplemento.append(" e " + valorComplementoBeanList
						.get(valorComplementoBeanList.size() - 1).getValor());
			}
		}
		
		return valorComplemento.toString();
	}

	private List<MovimentoBean> converteEmMovimentoBeanList(List<ComplementoSegmentado> complementoSegmentadoList) {
		MovimentoBean movimentoBean = new MovimentoBean();
		List<ComplementoBean> complementoBeanList = new ArrayList<ComplementoBean>();

		for (ComplementoSegmentado complementoSegmentado : complementoSegmentadoList) {

			TipoComplemento tp = EntityUtil.find(TipoComplemento.class,complementoSegmentado.getTipoComplemento().getIdTipoComplemento());

			ComplementoBean complementoBean = new ComplementoBean();
			ValorComplementoBean vcb = new ValorComplementoBean();
			vcb.setValor(complementoSegmentado.getValorComplemento());
			vcb.setCodigo(complementoSegmentado.getTexto());
			
			complementoBean.setValorComplementoBeanList(Arrays.asList(vcb));
			complementoBean.setLabel(tp.getLabel());
			complementoBean.setMensagemErro(tp.getMensagemErro());
			complementoBean.setValidacao(tp.getValidacao());
			complementoBean.setGlossario(tp.getDescricaoGlossario());
			complementoBean.setIdTipoComplemento(tp.getIdTipoComplemento());

			Boolean multivalorado = false;
			if(!tp.getAplicacaoComplementoList().isEmpty()) {
				multivalorado = tp.getAplicacaoComplementoList().get(0).getMultivalorado();
			}
			complementoBean.setMultiplo(multivalorado);
			
			if (tp instanceof TipoComplementoLivre) {
				this.preencherTipoComplementoLivre(tp, complementoBean);
			} else if (tp instanceof TipoComplementoDinamico) {
				try {
					this.preencherTipoComplementoDinamico(tp, complementoBean);
				} catch (Exception e) {
					throw new AplicationException("Erro na carga para o movimento " + complementoSegmentado.getMovimentoProcesso().getEvento().getEvento()
							+ ", EL do complemento inválida.");
				}
			} else if (tp instanceof TipoComplementoComDominio) {
				try {
					this.preencherTipoComplementoComDominio(tp, complementoBean);
				} catch (NonUniqueResultException e) {
					throw new AplicationException("Mais de um AplicacaoDominio encontrado para o evento "
							+ complementoSegmentado.getMovimentoProcesso().getEvento().getEvento() + ".");
				} catch (NoResultException e) {
					throw new AplicationException("Nenhum AplicacaoDominio encontrado para o evento "
							+ complementoSegmentado.getMovimentoProcesso().getEvento().getEvento() + ".");
				}
			}
			
			complementoBeanList.add(complementoBean);			
		}
		
		movimentoBean.setComplementoBeanList(complementoBeanList);
		return Arrays.asList(movimentoBean);
	}
	
	/**
	 * Retorna uma lista de ValorComplementoBean para preenchimento do comboBox
	 * dos complementos Dinâmicos.
	 * 
	 * A propriedade 'codigo' de cada elemento da lista de retorno será
	 * preenchida com o atributo anotado @Id na lista de entrada.
	 * 
	 * A propriedade 'valor' de cada elemento da lista de retorno será
	 * preenchida com o toString de cada objeto da lista de entrada.
	 * 
	 * @param evalList
	 *            Lista de objetos retornados pela EL de consulta.
	 * @return Retorna uma lista de ValorComplementoBean para preenchimento do
	 *         comboBox dos complementos Dinâmicos.
	 */
	public List<ValorComplementoBean> converterValorComplementoBean(List<Object> evalList) {
		List<ValorComplementoBean> retorno = new ArrayList<ValorComplementoBean>();
		for (Object o : evalList) {
			ValorComplementoBean vcb = new ValorComplementoBean();
			vcb.setValor(o.toString());
			try {
				vcb.setCodigo(HibernateUtil.getIdAsString(o));
			} catch (Exception e) {
				vcb.setCodigo(o.toString());
			}
			retorno.add(vcb);
		}
		this.ordenarListaComplementos(retorno);

		return retorno;
	}
	
	@SuppressWarnings("unchecked")
	public void preencherTipoComplementoDinamico(TipoComplemento tipoComplemento, ComplementoBean cb) {
		TipoComplementoDinamico tcd = (TipoComplementoDinamico) tipoComplemento;
		cb.setTipoComplementoEnum(ComplementoBean.TipoComplementoEnum.DINAMICO);
		Object retornoAvaliacaoEL = new Util().eval(tcd.getExpressaoBusca());
		List<Object> itens = null;
		if(retornoAvaliacaoEL != null) {
			if (!(retornoAvaliacaoEL instanceof List<?>)) {
				itens = new ArrayList<Object>();
				itens.add(retornoAvaliacaoEL);
			}else {
				itens = (List<Object>) retornoAvaliacaoEL;
				
			}			
		}
		if(itens == null) {
			itens = new ArrayList<Object>(0);
		}
		cb.setElementosComboboxList(this.converterValorComplementoBean(itens));
	}

	public void preencherTipoComplementoLivre(TipoComplemento tipoComplemento, ComplementoBean cb) {
		if (tipoComplemento instanceof TipoComplementoLivre) {
			cb.setTemMascara(((TipoComplementoLivre)tipoComplemento).getTemMascara());
			cb.setMascara(((TipoComplementoLivre)tipoComplemento).getMascara());	
		}
		cb.setTipoComplementoEnum(ComplementoBean.TipoComplementoEnum.LIVRE);
	}
	
	public void preencherTipoComplementoComDominio(TipoComplemento tipoComplemento, ComplementoBean cb) {
		TipoComplementoComDominio tccd = (TipoComplementoComDominio) tipoComplemento;
		cb.setTipoComplementoEnum(ComplementoBean.TipoComplementoEnum.COM_DOMINIO);

		AplicacaoDominio aplicacaoDominio = this.getAplicacaoDominio(tccd);

		List<ValorComplementoBean> vcbList = new ArrayList<ValorComplementoBean>();
		for (ElementoDominio elementoDominio : aplicacaoDominio.getDominio().getElementoDominioList()) {
			if (!elementoDominio.getAtivo()) {
				continue;
			}
			ValorComplementoBean vcbComDominio = new ValorComplementoBean();
			vcbComDominio.setCodigo(elementoDominio.getCodigoGlossario());
			vcbComDominio.setValor(elementoDominio.getValor());

			vcbList.add(vcbComDominio);
		}
		
		this.ordenarListaComplementos(vcbList);
		cb.setElementosComboboxList(vcbList);
	}

    private void ordenarListaComplementos(List<ValorComplementoBean> listaComplementos){
    	Collections.sort(listaComplementos, new Comparator<ValorComplementoBean>() {
            @Override
			public int compare(ValorComplementoBean c1, ValorComplementoBean c2) {
            	return c1.getValor().compareTo(c2.getValor());
            }
        });        
    }
	
	private ProcessoEvento getProcessoEvento(EventoBean eventoBean, Processo processo, ProcessoDocumento processoDocumento,
			Tarefa tarefa, Long idJbpmTask, Long idProcessInstance, Usuario usuario) {

		Evento evento = EntityUtil.find(Evento.class,eventoBean.getIdEvento());
		AplicacaoMovimento aplicacaoMovimento = this.getAplicacaoMovimentoByEvento(evento);
		ParserTextoMovimento parserTextoMovimento = null;
		if(aplicacaoMovimento != null) {
			parserTextoMovimento = new ParserTextoMovimento(aplicacaoMovimento);
		}
		
		List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();
		for (MovimentoBean movimentoBean : eventoBean.getMovimentoBeanList()) {
			complementoSegmentadoList.addAll(this.converteEmComplementoSegmentado(movimentoBean, parserTextoMovimento));
		}

		ProcessoEvento movimentoProcesso = this.criarMovimentoProcesso(evento, complementoSegmentadoList, processoDocumento, processo, tarefa, idJbpmTask, idProcessInstance, usuario);

		if(aplicacaoMovimento != null && parserTextoMovimento != null){
			movimentoProcesso.setTextoParametrizado(aplicacaoMovimento.getTextoParametrizado());
			movimentoProcesso.setTextoFinalExterno(parserTextoMovimento.getTextoFinalExterno());
			movimentoProcesso.setTextoFinalInterno(parserTextoMovimento.getTextoFinalInterno());
			
			if(CollectionUtilsPje.isNotEmpty(movimentoProcesso.getComplementoSegmentadoList())) {
				for (ComplementoSegmentado complementoSegmentado : movimentoProcesso.getComplementoSegmentadoList()) {
					complementoSegmentado.setMovimentoProcesso(movimentoProcesso);
				}
			}
		}
		
		return movimentoProcesso;
	}

	private ProcessoEvento getMovimentoProcesso(MovimentoBuilder movimentoBuilder){
		ProcessoDocumento processoDocumento = null;
		Usuario usuario = null;
		Processo processo = null;
		Evento evento = null;
		AplicacaoMovimento aplicacaoMovimento = null;
		List<ComplementoSegmentado> complementoSegmentados = new ArrayList<ComplementoSegmentado>();
		
		if (movimentoBuilder.idDocumento != null) {
			processoDocumento = ((GenericManager) ComponentUtil.getComponent("genericManager")).find(ProcessoDocumento.class, movimentoBuilder.idDocumento);
		}
		if (movimentoBuilder.idUsuario != null) {
			usuario = ((GenericManager) ComponentUtil.getComponent("genericManager")).find(Usuario.class, movimentoBuilder.idUsuario);
		}
		if (movimentoBuilder.idProcesso != null) {
			processo = ((GenericManager) ComponentUtil.getComponent("genericManager")).find(Processo.class, movimentoBuilder.idProcesso);
		}
		if (movimentoBuilder.codigoDoMovimento != null) {
			evento = this.getEventoProcessualByCodigoCnj(movimentoBuilder.codigoDoMovimento);
		}
		if (evento != null) {
			aplicacaoMovimento = this.getAplicacaoMovimentoByEvento(evento);
		}else{
			throw new IllegalArgumentException("Não há tipo de movimentação disponível.");
		}
		
		ProcessoEvento movimentoProcesso = criarMovimentoProcesso(evento, null, processoDocumento, processo, null, null, null, usuario);
		ParserTextoMovimento parserTextoMovimento = new ParserTextoMovimento(aplicacaoMovimento);
		
		// Para cada complemento que foi preenchido no MovimentoBuilder
		for (ComplementoBuilder complementoBuilder : movimentoBuilder.complementoBuilders){
			// Ex: 'movimento de #{complemento_a} para #{complemento_b}'
			//     -> 
			//     'movimento de preenchido complemento a para #{complemento_b}'
			AplicacaoComplemento aplicacaoComplementoPreenchido = this.geraAplicacaoComplemento(
					parserTextoMovimento, complementoBuilder.codigoDoComplemento, complementoBuilder.nomeDoComplemento, complementoBuilder.texto);
			
			// Criar complemento segmentado para cada complemento preenchido
			if (aplicacaoComplementoPreenchido != null) {
				ComplementoSegmentado complementoSegmentado = new ComplementoSegmentado();
				
				complementoSegmentado.setTexto(complementoBuilder.codigo);
				complementoSegmentado.setValorComplemento(complementoBuilder.texto);
				complementoSegmentado.setMultivalorado(false);
				complementoSegmentado.setOrdem(0);
				
				complementoSegmentado.setVisibilidadeExterna(aplicacaoComplementoPreenchido.getVisibilidadeExterna());
				complementoSegmentado.setTipoComplemento(aplicacaoComplementoPreenchido.getTipoComplemento());
				
				complementoSegmentado.setMovimentoProcesso(movimentoProcesso);
				complementoSegmentados.add(complementoSegmentado);
			}
		}
		
		movimentoProcesso.setComplementoSegmentadoList(complementoSegmentados);
		
		movimentoProcesso.setTextoParametrizado(aplicacaoMovimento.getTextoParametrizado());
		movimentoProcesso.setTextoFinalExterno(parserTextoMovimento.getTextoFinalExterno());
		movimentoProcesso.setTextoFinalInterno(parserTextoMovimento.getTextoFinalInterno());
		
		return movimentoProcesso;
	}
	
	/**
	 * Recebe e atualiza o parserTextoMovimento e retorna uma nova aplicacaoComplemento
	 * 
	 * @param parserTextoMovimento
	 * @param codigoDoComplemento
	 * @param nomeDoComplemento
	 * @param texto
	 * @return
	 */
	private AplicacaoComplemento geraAplicacaoComplemento(ParserTextoMovimento parserTextoMovimento, String codigoDoComplemento, String nomeDoComplemento, String texto) {
		AplicacaoComplemento aplicacaoComplemento = null;
		if (codigoDoComplemento == null && nomeDoComplemento == null){
			aplicacaoComplemento = parserTextoMovimento.preencherProximoComplementoVazio(texto);
		}
		else if (nomeDoComplemento != null){
			aplicacaoComplemento = parserTextoMovimento.preencherProximoComplementoDeNome(nomeDoComplemento, texto);
		}
		else if (codigoDoComplemento != null){
			aplicacaoComplemento = parserTextoMovimento.preencherProximoComplementoDeCodigo(codigoDoComplemento, texto);
		}

		return aplicacaoComplemento;
	}

	private List<String> getErrosPreRequisitosLancamento(EventoBean eventoBean, Processo processo){
		List<String> errosLst = null;
		Evento evento = EntityUtil.find(Evento.class, eventoBean.getIdEvento());
		if(evento != null && !evento.getCodEvento().isEmpty()) {
			if (processo != null){
				errosLst = this.getErrosPreRequisitosLancamento(processo.getIdProcesso(), evento.getCodEvento());
			}
		}
		return errosLst;
	}
	
	private List<String> getErrosPreRequisitosLancamento(MovimentoAutomaticoService.MovimentoBuilder movimentoBuilder){
		return this.getErrosPreRequisitosLancamento(movimentoBuilder.idProcesso, movimentoBuilder.codigoDoMovimento);
	}
	
	private List<String> getErrosPreRequisitosLancamento(Integer idProcesso, String codigoDoMovimento){
		Processo processo = null;
		Evento evento = null;
		AplicacaoMovimento aplicacaoMovimento = null;
		
		if (idProcesso != null) {
			processo = ((GenericManager) ComponentUtil.getComponent("genericManager")).find(Processo.class, idProcesso);
		}
		if (codigoDoMovimento != null) {
			evento = this.getEventoProcessualByCodigoCnj(codigoDoMovimento);
		}
		if (evento != null) {
			aplicacaoMovimento = getAplicacaoMovimentoByEvento(evento);
		}
		
		List<String> erros = new ArrayList<String>();
		LogProvider log = Logging.getLogProvider(LancadorMovimentosService.class);
		
		if (processo == null) {
			erros.add("Ao lançar o movimento automático, não foi encontrado Processo de identificador " + idProcesso + ".");
		}
		if (evento == null) {
			erros.add("Ao lançar o movimento automático, não foi encontrado Evento Processual de código " + codigoDoMovimento + ".");
		}
		if (aplicacaoMovimento == null) {
			erros.add("Ao lançar o movimento automático, foi encontrado nenhum ou mais de um AplicacaoMovimento para o evento de código " + codigoDoMovimento + ".");
		}
		if (erros.size() > 0) {
			for (String erro : erros){
				log.error(erro);
			}
		}
		return erros;
	}
	
	private ProcessoEvento criarMovimentoProcesso(Evento evento, List<ComplementoSegmentado> complementoSegmentadoList,
			ProcessoDocumento processoDocumento, Processo processo,
			Tarefa tarefa, Long idJbpmTask, Long idProcessInstance, Usuario usuario)
	{
		ProcessoEvento movimentoProcesso = new ProcessoEvento();
		movimentoProcesso.setEvento(evento);
		movimentoProcesso.setProcessoDocumento(processoDocumento);
		movimentoProcesso.setProcesso(processo);
		movimentoProcesso.setUsuario(usuario);
		movimentoProcesso.setTarefa(tarefa);
		movimentoProcesso.setDataAtualizacao(new Date());
		movimentoProcesso.setComplementoSegmentadoList(complementoSegmentadoList);
		movimentoProcesso.setIdJbpmTask(idJbpmTask);
		movimentoProcesso.setIdProcessInstance(idProcessInstance);
		movimentoProcesso.setVisibilidadeExterna(evento.getVisibilidadeExterna());
		
		return movimentoProcesso;
	}
	

	/**
	 * Retorna se o tipoComplemento é o complemento do movimento de exclusão
	 * @param tipoComplemento TipoComplemento que será validado
	 * @return Se o tipoComplemento é o complemento do movimento de exclusão
	 */
	public boolean isTipoComplementoMovimentoExcluido(TipoComplemento tipoComplemento) {
		return tipoComplemento.getCodigo().equals("5021");
	}
	
	/**
	 * Retorna se o tipoComplemento é o complemento da data/hora do movimento excluído
	 * @param tipoComplemento TipoComplemento que será validado
	 * @return Se o tipoComplemento é o complemento do data/hora do movimento excluído
	 */
	public boolean isTipoComplementoDataHoraExcluido(TipoComplemento tipoComplemento) {
		return tipoComplemento.getCodigo().equals("5004");
	}

	/**
	 * Método para inclusão dos valores dos complementos no texto parametrizado
	 * de um movimento
	 * 
	 * @param textoFinalInterno
	 *            texto parametrizado utilizado para armazenar o texto final
	 *            para visualização interna
	 * @param textoFinalExterno
	 *            texto parametrizado utilizado para armazenar o texto final
	 *            para visualização externa
	 * @param complementoSegmentadoList
	 *            lista de complementos segmentados
	 * @param aplicacaoComplemento
	 *            utilizado para verificação se visibilidade externa e de
	 *            multiplicidade de valores de complementos
	 */
	public void complementarTextoFinal(StringBuilder textoFinalInterno,
			StringBuilder textoFinalExterno,
			List<ComplementoSegmentado> complementoSegmentadoList,
			TipoComplemento tipoComplemento, Boolean multivalorado,
			Boolean visibilidadeExterna) {
		
		if (complementoSegmentadoList == null) {
			return;
		}
		
		StringBuilder valorComplementoInterno = new StringBuilder("");
		StringBuilder valorComplementoExterno = new StringBuilder("");

		if (multivalorado) {
			// varrer complementos levando a Ordem em consideração
			ArrayList<ComplementoSegmentado> valoresComplementosInternos = new ArrayList<ComplementoSegmentado>();
			ArrayList<ComplementoSegmentado> valoresComplementosExternos = new ArrayList<ComplementoSegmentado>();

			for (ComplementoSegmentado complementoSegmentado : complementoSegmentadoList) {
				if (complementoSegmentado.getTipoComplemento()
						.equals(tipoComplemento)) {
					valoresComplementosInternos.add(complementoSegmentado);
					
					if (complementoSegmentado.getVisibilidadeExterna() == null) {
						// lançamento de novo evento; visibilidadeExterna = aplicacaoComplemento.getVisibilidadeExterna()
						complementoSegmentado.setVisibilidadeExterna(visibilidadeExterna);
						complementoSegmentado.setMultivalorado(multivalorado);
					}
					
					if (complementoSegmentado.getVisibilidadeExterna()) {
						valoresComplementosExternos.add(complementoSegmentado);
					}
				}
			}

			// Caso os tipos dos complementos nao sejam igual o tipo passado
			if (valoresComplementosInternos.isEmpty()) {
				return;
			}

			Comparator<ComplementoSegmentado> comparator = new Comparator<ComplementoSegmentado>() {
				@Override
				public int compare(
						ComplementoSegmentado complementoSegmentado1,
						ComplementoSegmentado complementoSegmentado2) {
					return complementoSegmentado1.getOrdem().compareTo(
							complementoSegmentado2.getOrdem());
				}
			};
			
			valorComplementoInterno = montarValorComplementoMultivalorado(valoresComplementosInternos, comparator);
			valorComplementoExterno = montarValorComplementoMultivalorado(valoresComplementosExternos, comparator);
		} else {
			// varrer lista de complementos segmentados em busca do objeto
			// relacionado ao TipoComplemento em questão
			for (ComplementoSegmentado complementoSegmentado : complementoSegmentadoList) {
				if (complementoSegmentado.getTipoComplemento().equals(
						tipoComplemento)) {
					// substituir o valor correspondente no texto parametrizado
					valorComplementoInterno.append(complementoSegmentado
							.getValorComplemento());

					if (complementoSegmentado.getVisibilidadeExterna() == null) {
						// lançamento de novo evento; visibilidadeExterna = aplicacaoComplemento.getVisibilidadeExterna()
						complementoSegmentado.setVisibilidadeExterna(visibilidadeExterna);
						complementoSegmentado.setMultivalorado(multivalorado);
					}
					
					if (complementoSegmentado.getVisibilidadeExterna())
						valorComplementoExterno.append(complementoSegmentado.getValorComplemento());
				}
			}
		}

		// conforme visibilidade, alterar os textos finais internos e externos
		inserirValorNoTextoFinal(textoFinalInterno,
				tipoComplemento, valorComplementoInterno);
		
		if (!multivalorado) {
			inserirValorNoTextoFinal(textoFinalExterno, tipoComplemento,
					((visibilidadeExterna ? 
							valorComplementoExterno : new StringBuilder(""))));
		}
		else {
			inserirValorNoTextoFinal(textoFinalExterno, tipoComplemento, valorComplementoExterno);
		}
	}
	
	private StringBuilder montarValorComplementoMultivalorado(List<ComplementoSegmentado> valoresComplementos, Comparator<ComplementoSegmentado> comparator)
	{
		StringBuilder valorComplemento = new StringBuilder("");
		Collections.sort(valoresComplementos, comparator);
		
		if (valoresComplementos.size() > 0) {
			valorComplemento.append(valoresComplementos.get(0).getValorComplemento());

			// pega os complementos considerando a ordem
			for (int i = 1; i < valoresComplementos.size() - 1; i++) {
				valorComplemento.append(", " + valoresComplementos.get(i).getValorComplemento());
			}

			if (valoresComplementos.size() > 1) {
				valorComplemento.append(" e " + valoresComplementos
						.get(valoresComplementos.size() - 1).getValorComplemento());
			}
		}
		
		return valorComplemento;
	}

	/**
	 * Método que efetivamente efetua a inclusão de um determinado valor de um
	 * determinado complemento no texto parametrizado
	 * 
	 * @param textoFinal
	 *            texto parametrizado para substituição
	 * @param tipoComplemento
	 *            complemento cujo valor será atribuído no texto parametrizado
	 * @param valorComplemento
	 *            valor do complemento a ser incluso no texto parametrizado
	 */
	private void inserirValorNoTextoFinal(StringBuilder textoFinal,
			TipoComplemento tipoComplemento, StringBuilder valorComplemento) {
		String nomeComplemento = SEPARADOR_INICIO + tipoComplemento.getNome()
				+ SEPARADOR_FIM;
		int posicao = textoFinal.indexOf(nomeComplemento);

		if (posicao >= 0)
			textoFinal.replace(posicao, nomeComplemento.length()
							+ posicao, valorComplemento.toString());
	}
	
	/**
	 * Método para exclusão de mais de um movimento de um processo.
	 * @param processoEventoList Lista de movimentos a serem excluídos.
	 */
	public void excluirMovimento(List<ProcessoEvento> processoEventoList, boolean ignorarValidacaoDeMovimentoExcluivel)
	{
		if (processoEventoList != null)
		{
			for (ProcessoEvento processoEvento : processoEventoList) {
				excluirMovimento(processoEvento, ignorarValidacaoDeMovimentoExcluivel);
			}
		}
	}
	
	
	/**
	 * Método responsável por excluir o último movimento.
	 * @param processoTrf Processo que deverá ser excluído o movimento.
	 * @param codMovimento Código do movimento a ser excluído.
	 * @author Guilherme Bispo
	 */
	public void excluirUltimoMovimento(String codMovimento) throws AplicationException {
		
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		
		ProcessoEvento processoEventoEncontrado = null; 
		
		// Percorre todos os movimentos do processo
		for (ProcessoEvento processoEvento : processoTrf.getProcesso().getProcessoEventoList()) {
			
			// Verifica se o movimento corrente não foi excluído
			if ((HibernateUtil.deproxy(processoEvento.getEvento(), Evento.class) instanceof Evento)
					&& (processoEvento.getProcessoEventoExcludente() == null)) {
	
				Evento eventoProcessual = (Evento) HibernateUtil.deproxy(processoEvento.getEvento(),Evento.class);
				
				// Verifica se o movimento possui o código passado por parâmetro
				if (eventoProcessual.getCodEvento().equals(codMovimento)){
										
					// Atualiza a variavel processoEventoEncontrado com o movimento mais atual
					if (processoEventoEncontrado == null
							|| processoEvento.getDataAtualizacao().after(
									processoEventoEncontrado.getDataAtualizacao())) {
						processoEventoEncontrado = processoEvento;
					}
				}
			}
		}
		
		if (processoEventoEncontrado != null){			
			try{
				excluirMovimento(processoEventoEncontrado, true);
			}
			catch(AplicationException ae){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Movimento '" + processoEventoEncontrado.getTextoFinal(Boolean.FALSE) + "' não pode ser excluído.");
				throw new AplicationException();
			}
		}
	}
	
	/**
	 * Método para exclusão de um movimento de um processo.
	 * @param movimento Movimento a ser excluído
	 * @return O movimento de exclusão ou null caso o movimento a ser excluído não seja excluível.
	 */
	public ProcessoEvento excluirMovimento(ProcessoEvento movimento) throws AplicationException {
		return excluirMovimento(movimento, false);
	}
	
	/**
	 * Método para exclusão de um movimento de um processo.
	 * 
	 * @param movimento Movimento a ser excluído.
	 * @param ignorarValidacaoDeMovimentoExcluivel Determina se levará em consideração o atributo "permiteExclusao" do movimento.
	 * @return O movimento de exclusão ou null caso o movimento a ser excluído não seja excluível.
	 */
	public ProcessoEvento excluirMovimento(ProcessoEvento movimento, boolean ignorarValidacaoDeMovimentoExcluivel) throws AplicationException {
		if (movimento == null) {
			throw new AplicationException("Movimento a ser excluído não pode ser nulo.");
		}

		if (movimento.getAtivo().equals(Boolean.FALSE) || movimento.getProcessoEventoExcludente() != null) {
			throw new AplicationException(String.format(
				"O movimento '%s' já foi previamente excluído e não pode ser excluído novamente.", movimento.getTextoFinal(Boolean.FALSE)));
		}

		if (!ignorarValidacaoDeMovimentoExcluivel) {
			AplicacaoMovimento aplicacaoMovimento = this.getAplicacaoMovimentoByEvento(movimento.getEvento());
			if (aplicacaoMovimento == null || aplicacaoMovimento.getPermiteExclusao() == null || !aplicacaoMovimento.getPermiteExclusao()) {
				throw new AplicationException(String.format("O movimento '%s' não pode ser excluído.",  movimento.getTextoFinal(Boolean.FALSE)));
			}
		}
		
		ProcessoEvento movimentoExcludente = null;
		Evento movimentoExclusao = ComponentUtil.getComponent(EventoManager.class).recuperarMovimentoExclusao();
		if (movimentoExclusao != null) {
			// Descrição: Cancelada a movimentação processual #{movimento-excluido}
			movimentoExcludente = MovimentoAutomaticoService.preencherMovimento()
					.deCodigo(movimentoExclusao.getCodEvento())
					.associarAoProcesso(movimento.getProcesso())
					.comProximoComplementoVazio()
					.preencherComCodigo(Integer.toString(movimento.getIdProcessoEvento()))
					.preencherComTexto(movimento.getTextoFinal(Boolean.TRUE))
					.lancarMovimento();
			
			movimento.setProcessoEventoExcludente(movimentoExcludente);
			if (movimento.getVisibilidadeExterna() != movimentoExcludente.getVisibilidadeExterna()) {
				movimentoExcludente.setVisibilidadeExterna(movimento.getVisibilidadeExterna());
			}
			getEntityManager().persist(movimentoExcludente);
		}
		movimento.setAtivo(Boolean.FALSE);
		
		getEntityManager().flush();

		return movimentoExcludente;
	}

	/**
	 * Retorna 1(um) AplicacaoMovimento para o evento em questão, somente se 
	 * houver aplicabilidade compatível. 
	 * 
	 * @param evento Evento processual donde será buscado o AplicacaoMovimento.
	 * @return AplicacaoMovimento AplicacaoMovimento correspondente ao Evento.
	 */
	public AplicacaoMovimento getAplicacaoMovimentoByEvento(Evento evento) {
		String tipoJustica = ParametroUtil.instance().getTipoJustica();
		String codigoInstancia = ParametroUtil.instance().getCodigoInstanciaAtual();
		boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();

		Query q = getEntityManager().createQuery(getHqlAplicacaoMovimento())
			.setParameter("idEvento", evento.getIdEvento())
			.setParameter("tipoJustica", tipoJustica)
			.setParameter("codigoAplicacaoSistema", codigoInstancia)
			.setParameter("isPrimeiroGrau", isPrimeiroGrau)
			.setParameter("sujeitoAtivo", SUJEITO_ATIVO);

		return (AplicacaoMovimento) EntityUtil.getSingleResult(q);
	}
	
	public static String getHqlAplicacaoMovimento() {
		StringBuilder hql = new StringBuilder();

		hql.append("SELECT am FROM ");
		hql.append("	AplicacaoMovimento am ");
		hql.append("	, AplicabilidadeView ap ");
		hql.append("WHERE ");
		hql.append("	am.eventoProcessual.idEvento = :idEvento");
		hql.append("	AND am.ativo = true ");
		
		hql.append("	AND am.aplicabilidade.idAplicabilidade IN (ap.idAplicabilidade) ");
		hql.append("	AND ap.orgaoJustica = :tipoJustica");
		hql.append("	AND ap.codigoAplicacaoClasse = :codigoAplicacaoSistema");
		hql.append("	AND (:isPrimeiroGrau = false OR ap.sujeitoAtivo = :sujeitoAtivo)");

		return hql.toString();
	}
	
	public AplicacaoDominio getAplicacaoDominio(TipoComplementoComDominio tccd) {
		String tipoJustica = ParametroUtil.instance().getTipoJustica();
		String codigoInstancia = ParametroUtil.instance().getCodigoInstanciaAtual();
		boolean isPrimeiroGrau = ParametroUtil.instance().isPrimeiroGrau();

		Query q = getEntityManager().createQuery(getHqlAplicacaoDominio())
				.setParameter("aplicacaoDominioList", Util.isEmpty(tccd.getAplicacaoDominioList())?null:tccd.getAplicacaoDominioList())
				.setParameter("tipoJustica", tipoJustica)
				.setParameter("codigoAplicacaoSistema", codigoInstancia)
				.setParameter("isPrimeiroGrau", isPrimeiroGrau)
				.setParameter("sujeitoAtivo", LancadorMovimentosService.SUJEITO_ATIVO);
		
		return (AplicacaoDominio) EntityUtil.getSingleResult(q);
	}
	
	private static String getHqlAplicacaoDominio() {
		StringBuilder hql = new StringBuilder();

		hql.append("SELECT ad FROM ");
		hql.append("	AplicacaoDominio ad ");
		hql.append("	, AplicabilidadeView ap ");
		hql.append("WHERE ");
		hql.append("	ad in (:aplicacaoDominioList) ");
		hql.append("	AND ad.dominio.ativo = true ");
		
		hql.append("	AND ad.aplicabilidade.idAplicabilidade IN (ap.idAplicabilidade) ");
		hql.append("	AND ap.orgaoJustica = :tipoJustica");
		hql.append("	AND ap.codigoAplicacaoClasse = :codigoAplicacaoSistema");
		hql.append("	AND (:isPrimeiroGrau = false OR ap.sujeitoAtivo = :sujeitoAtivo)");

		return hql.toString();
	}

	
	/**
	 * Altera a visibilidade externa do movimento no banco de dados.
	 * 
	 * @param evento contendo o atributo visibilidade externa que deve ser 
	 * 		alterada no banco.
	 */
	public void alteraVisibilidade(ProcessoEvento evento) {
		
		getEntityManager().persist(evento);
		getEntityManager().flush();
	}

	/**
	 * Altera a visibilidade externa do complemento de um movimento no banco de dados.
	 * 
	 * @param movimentoProcesso movimento que está tendo a visibilidade de seus complementos alterada.
	 */
	public void alteraVisibilidadeComplementoSegmentado(ProcessoEvento movimentoProcesso) {
		StringBuilder textoFinalInterno = new StringBuilder("");
		StringBuilder textoFinalExterno = new StringBuilder("");
		
		textoFinalExterno.append(movimentoProcesso.getTextoParametrizado());
		
		for (ComplementoSegmentado complementoSegmentado : movimentoProcesso.getComplementoSegmentadoList()) {
			
			if (complementoSegmentado.getOrdem() == 0) {
				
				complementarTextoFinal(textoFinalInterno, textoFinalExterno, 
					movimentoProcesso.getComplementoSegmentadoList(), 
					complementoSegmentado.getTipoComplemento(), complementoSegmentado.getMultivalorado(),
					complementoSegmentado.getVisibilidadeExterna());
				
				getEntityManager().persist(complementoSegmentado);
				getEntityManager().flush();
			}
		}

		movimentoProcesso.setTextoFinalExterno(textoFinalExterno.toString());
		
		getEntityManager().persist(movimentoProcesso);
		getEntityManager().flush();
	}
	
	/**
	 * Método utilizado para lançamento de um determinado movimento
	 * sem associação direta com o fluxo
	 * 
	 * @author Rodrigo Cartaxo / Rafael Barros
	 * 
	 * @param evento
	 *            Movimento a ser lançado
	 * @param complementoSegmentadoList
	 *            Lista de valores de complementos a serem associados ao
	 *            movimento
	 * @param processoDocumento
	 *            Documento ao qual o movimento deve ser associado
	 * @param processo
	 *            Processo ao qual o movimento deve ser associado
	 * @return ProcessoEvento gerado, representando o movimento lançado
	 * @deprecated Utilizar o MovimentoAutomaticoService para lancamento automatico de movimentos
	 */
	@Deprecated
	public ProcessoEvento lancarMovimentoSemFluxo(Evento evento,
			List<ComplementoSegmentado> complementoSegmentadoList,
			ProcessoDocumento processoDocumento, Processo processo) {
		
		if (complementoSegmentadoList == null) {
			complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();
		}
		
		return lancarMovimento(evento, complementoSegmentadoList, processoDocumento, processo, null, null, null);
	}
	

	public Evento getEventoProcessualByCodigoCnj(String codEvento){
		try {
			Query q = EntityUtil.createQuery("from Evento o where o.codEvento= :codEvento")
			.setParameter("codEvento", codEvento);
			return (Evento) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		} 
	}

	public Evento getEventoProcessualById(Integer idEventoProcessual) {
		return EntityUtil.find(Evento.class, idEventoProcessual);
	}
	
	public List<ComplementoSegmentado> getComplementoSegmentadoList(Evento eventoProcessual, HashMap<String, ValorComplementoSegmentado> valoresComplementos){
		
		//Localiza AplicacaoMovimento
		AplicacaoMovimento aplicacaoMovimento = getAplicacaoMovimentoByEvento(eventoProcessual);
		
		//Preenche lista de complementos segmentados
		List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();
		if (aplicacaoMovimento != null) {
			for(AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimento.getAplicacaoComplementoList()){
				
				String tipoComplemento = aplicacaoComplemento.getTipoComplemento().getNome(); 
				ValorComplementoSegmentado valorComplemento;
				
				if (valoresComplementos.containsKey(tipoComplemento)){
					valorComplemento = valoresComplementos.get(tipoComplemento);
					
					ComplementoSegmentado complementoSegmentado = new ComplementoSegmentado();				

					complementoSegmentado.setOrdem(0);
					complementoSegmentado.setTipoComplemento(aplicacaoComplemento.getTipoComplemento());
					complementoSegmentado.setMultivalorado(aplicacaoComplemento.getMultivalorado());
					complementoSegmentado.setVisibilidadeExterna(aplicacaoComplemento.getVisibilidadeExterna());
					complementoSegmentado.setTexto(valorComplemento.getTexto());
					complementoSegmentado.setValorComplemento(valorComplemento.getValorComplemento());

					complementoSegmentadoList.add(complementoSegmentado);

				}
			}
		}
		return complementoSegmentadoList;
	}	

	public ElementoDominio getElementoDominioByCodigoCnj(String codigoCnj){
		Query q = EntityUtil.createQuery("from ElementoDominio o where o.codigoGlossario= :codigoCnj")
		.setParameter("codigoCnj", codigoCnj);
		return (ElementoDominio) q.getResultList().get(0);
	}	
	
	public ProcessoEvento converteEmProcessoEventoByCodigoCNJ(ProcessoTrf processo, ProcessoDocumento doc, String codigoCNJ) {
		ProcessoEvento pe = null;
		if(codigoCNJ != null && StringUtil.isNotEmpty(codigoCNJ)) {
			EventoManager eventoManager = ComponentUtil.getComponent(EventoManager.NAME);
			Evento evento = eventoManager.findByCodigoCNJ(codigoCNJ);
			if(evento != null) {
				ProcessoEventoManager processoEventoManager = ComponentUtil.getComponent(ProcessoEventoManager.NAME);
				pe = processoEventoManager.getMovimentacao(processo, evento, doc);
			}
		}
		return pe;
	}
	
	public void setCondicaoLancamentoMovimentosTemporarioNoFluxo(String condicao) {
		this.setCondicaoLancamentoMovimentosTemporarioNoFluxo(ProcessInstance.instance(), condicao);
	}
	
	public void setCondicaoLancamentoMovimentosTemporarioNoFluxo(org.jbpm.graph.exe.ProcessInstance processInstance, String condicao) {
		processInstance.getContextInstance().setVariable(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO, condicao);
	}
	
	/**
	 * Método que identifica se os movimentos devem ser lançados diretamente ou lançados em variáveis de temporárias de fluxo
	 * 
	 * @return
	 */
	public boolean deveGravarTemporariamente() {
		Boolean gravarTemporariamente = Boolean.FALSE;
		if (TaskInstance.instance() != null) {
			String condicao = (String) ProcessInstance.instance().getContextInstance().getVariable(Variaveis.VARIABLE_CONDICAO_LANCAMENTO_MOVIMENTOS_TEMPORARIO);
			if (condicao != null) {
				try {
					gravarTemporariamente =  (Boolean) Expressions.instance().createValueExpression(condicao).getValue();
				} catch (Exception e) {
					if (condicao.equalsIgnoreCase("true")) {
						gravarTemporariamente = Boolean.TRUE;
					}
				}
			}
		}
		
		return BooleanUtils.isTrue(gravarTemporariamente);
	}
	
	
	/**
	 * Método que valida a configuração no fluxo refente à obrigatoriedade de
	 * seleção do movimento.
	 */
	public boolean possuiCondicaoLancamentoMovimentoObrigatorio(){
		boolean condicao = Boolean.FALSE;
		if (TaskInstance.instance() != null) {
			Object condicaoObj = TaskInstance.instance().getVariableLocally(Variaveis.CONDICAO_LANCAMENTO_MOVIMENTOS_OBRIGATORIO);
			if (condicaoObj != null) {
				if (condicaoObj instanceof String) {
					String condicaoStr = (String)condicaoObj;
					if (condicaoStr.trim().toLowerCase().contains("true")) {
						condicao = Boolean.TRUE;
					}
				} else if (condicaoObj instanceof Boolean) {
					return (Boolean) condicaoObj;
				}
			}
		} 	
		return condicao;
	}

	/**
	 * Método que valida a configuração no fluxo refente à obrigatoriedade de
	 * seleção do movimento.
	 */
	public boolean possuiCondicaoLancamentoMovimentoObrigatorio(org.jbpm.taskmgmt.exe.TaskInstance taskInstance) {
		boolean condicao = Boolean.FALSE;
		if (taskInstance != null) {
			Object condicaoObj = taskInstance.getVariableLocally(Variaveis.CONDICAO_LANCAMENTO_MOVIMENTOS_OBRIGATORIO);
			if (condicaoObj != null) {
				if (condicaoObj instanceof String) {
					String condicaoStr = (String) condicaoObj;
					if (condicaoStr.trim().toLowerCase().contains("true")) {
						condicao = Boolean.TRUE;
					}
				} else if (condicaoObj instanceof Boolean) {
					return (Boolean) condicaoObj;
				}
			}
		}
		return condicao;
	}

	public static LancadorMovimentosService instance() {
		return (ComponentUtil.getComponent(LancadorMovimentosService.NAME));
	}

	/**
	 * Método utilizado para lançamento definitivo de movimentos configurados em outro
	 * nó, através de lançamento temporário de movimentos.
	 */
	public void homologarMovimentosTemporarios(){
		this.homologarMovimentosTemporarios(ProcessInstance.instance());
	}

	/**
	 * Método utilizado para lançamento definitivo de movimentos configurados em outro
	 * nó, através de lançamento temporário de movimentos.
	 * 
	 * @param processInstance Instância do processo JBPM.
	 */
	public void homologarMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance){
		Integer agrupamentosTemporarios = this.getAgrupamentoDeMovimentosTemporarios(processInstance);
		if(agrupamentosTemporarios != null && agrupamentosTemporarios > 0) {
			this.lancarMovimentosTemporarios(processInstance);
		}
	}
	
	/**
	 * Método responsável por apagar as variáveis de fluxo dos movimentos temporários
	 */
	public void apagarMovimentosTemporarios(){
		this.apagarMovimentosTemporarios(ProcessInstance.instance());
	}

	/**
	 * Limpa a variável de fluxo de movimentos selecionados temporariamente, limpa a variavel com o agrupamento possível selecinado
	 * @param processInstance
	 */
	public void apagarMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance) {
		if (processInstance != null && processInstance.getContextInstance() != null) {
			processInstance.getContextInstance().deleteVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE);
			processInstance.getContextInstance().deleteVariable(Variaveis.VARIABLE_MOVIMENTOS_LANCADOS_TEMPORARIAMENTE);
		}
	}

	public List<EventoBean> getMovimentosTemporarios(){
		return this.getMovimentosTemporarios(org.jboss.seam.bpm.ProcessInstance.instance());
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoEvento> getProcessoEventoListTemporario(org.jbpm.graph.exe.ProcessInstance processInstance){
		List<ProcessoEvento> listaMovimentosTemporarios = new ArrayList<ProcessoEvento>();
		if (processInstance != null && processInstance.getContextInstance() != null){
			Object movimentosTemporariosObj = processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_MOVIMENTOS_LANCADOS_TEMPORARIAMENTE);
			
			if(movimentosTemporariosObj != null) {				
				Boolean conseguiuConverter = false;
				if(movimentosTemporariosObj instanceof List<?> && ((List<?>) movimentosTemporariosObj).size() > 0) {
					if(((List<?>) movimentosTemporariosObj).get(0) instanceof EventoBean) {
						List<EventoBean> eventoBeanList = (List<EventoBean>) movimentosTemporariosObj;
						for (EventoBean eventoBean : eventoBeanList) {
							ProcessoEvento pe = this.converteEmProcessoEvento(processInstance, eventoBean);
							listaMovimentosTemporarios.add(pe);
						}
						conseguiuConverter = true;
					}else if(((List<?>) movimentosTemporariosObj).get(0) instanceof ProcessoEvento) {
						listaMovimentosTemporarios = (List<ProcessoEvento>) movimentosTemporariosObj;
						conseguiuConverter = true;
					}
				}
				if(!conseguiuConverter){
					try {
						throw new ConversionException("Não foi possível obter os movimentos temporários cadastrados.");
					} catch (ConversionException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return listaMovimentosTemporarios;		
	}
	
	@SuppressWarnings("unchecked")
	public List<EventoBean> getMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance){
		List<EventoBean> listaMovimentosTemporarios = new ArrayList<EventoBean>();
		if (processInstance != null && processInstance.getContextInstance() != null){
			Object movimentosTemporariosObj = processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_MOVIMENTOS_LANCADOS_TEMPORARIAMENTE);
			
			if(movimentosTemporariosObj != null && movimentosTemporariosObj instanceof List<?> && 
				((List<?>) movimentosTemporariosObj).size() > 0 && ((List<?>)movimentosTemporariosObj).get(0) != null) {
				
				boolean conseguiuConverter = Boolean.FALSE;
				
				List<Object> movimentosTemporariosObjList = (List<Object>) movimentosTemporariosObj;
				if (movimentosTemporariosObjList.get(0) instanceof EventoBean) {
					listaMovimentosTemporarios = (List<EventoBean>) movimentosTemporariosObj;
					conseguiuConverter = Boolean.TRUE;
				} else if (movimentosTemporariosObjList.get(0) instanceof ProcessoEvento) {
					List<ProcessoEvento> processoEventoTempList = (List<ProcessoEvento>) movimentosTemporariosObj;
					listaMovimentosTemporarios = this.converteEmEventoBean(processoEventoTempList);
					conseguiuConverter = Boolean.TRUE;
				}
				
				if (!conseguiuConverter) {
					try {
						throw new ConversionException("Não foi possível obter os movimentos temporários cadastrados.");
					} catch (ConversionException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return listaMovimentosTemporarios;
	}
	
	/**
	 * O padrão é armazenar na variável temporária os movimentos no formato de lista de ProcessoEvento - com este formato basta depois dar um persist para lancar o movimento
	 * 
	 * @param processInstance
	 * @param movimentoBuilder
	 */
	public void setMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance, MovimentoBuilder movimentoBuilder) {
		if (processInstance != null && processInstance.getContextInstance() != null && movimentoBuilder != null){
			List<ProcessoEvento> processoEventoList = new ArrayList<ProcessoEvento>();
			ProcessoEvento movimentoProcesso = this.converteEmProcessoEvento(movimentoBuilder);
			processoEventoList.add(movimentoProcesso);
			this.setMovimentosTemporariosProcessoEvento(processInstance, processoEventoList);
		}
	}

	/**
	 * O padrão é armazenar na variável temporária os movimentos no formato de lista de ProcessoEvento - com este formato basta depois dar um persist para lancar o movimento
	 * 
	 * @param processInstance
	 * @param eventoBeanList
	 */
	public void setMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance, List<EventoBean> eventoBeanList) {
		List<ProcessoEvento> processoEventoList = new ArrayList<ProcessoEvento>();
		if (processInstance != null && processInstance.getContextInstance() != null) {
			if(!eventoBeanList.isEmpty()){
				for (EventoBean eventoBean : eventoBeanList) {
					ProcessoEvento movimentosProcesso = this.converteEmProcessoEvento(processInstance, eventoBean);
					processoEventoList.add(movimentosProcesso);
				}
			}
		}
		this.setMovimentosTemporariosProcessoEvento(processInstance, processoEventoList);
	}

	/**
	 * O padrão é armazenar na variável temporária os movimentos no formato de lista de ProcessoEvento - com este formato basta depois dar um persist para lancar o movimento
	 * 
	 * @param processInstance
	 * @param eventoBeanList
	 */
	public void setMovimentosTemporariosProcessoEvento(org.jbpm.graph.exe.ProcessInstance processInstance, List<ProcessoEvento> processoEventoList) {
		if (processInstance != null && processInstance.getContextInstance() != null) {
			if(!processoEventoList.isEmpty()){
				processInstance.getContextInstance().setVariable(Variaveis.VARIABLE_MOVIMENTOS_LANCADOS_TEMPORARIAMENTE, processoEventoList);
			}else {
				processInstance.getContextInstance().deleteVariable(Variaveis.VARIABLE_MOVIMENTOS_LANCADOS_TEMPORARIAMENTE);
			}
		}
	}

	public Integer getAgrupamentoDeMovimentosTemporarios(){
		return this.getAgrupamentoDeMovimentosTemporarios(org.jboss.seam.bpm.ProcessInstance.instance());
	}
	
	/**
	 * Recupera o agrupador de movimentos temporário selecionado, o legado pode estar como string, por isso é necessário fazer a conversão para inteiro
	 * 
	 * @param processInstance
	 * @return
	 */
	public Integer getAgrupamentoDeMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance){
		Integer retorno = null;
		if (processInstance != null && processInstance.getContextInstance() != null){
			if(processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE) instanceof Integer) {
				retorno = (Integer) processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE);
			}else if(processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE) instanceof String) {
				retorno = Integer.valueOf((String) processInstance.getContextInstance().getVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE));
			}
		}
		
		return retorno;
	}
	
	/**
	 * Identifica o agrupador de movimentos que deve ser utilizado para a seleção de movimentos temporários
	 * Caso já haja um agrupador diferente no fluxo, apaga esse agrupador e os movimentos que poderiam já estar selecionados 
	 * 
	 * @param processInstance
	 * @param idAgrupamento
	 */
	public void setAgrupamentoDeMovimentosTemporarios(org.jbpm.graph.exe.ProcessInstance processInstance, Integer idAgrupamento) {
		if (processInstance != null && processInstance.getContextInstance() != null && idAgrupamento != null && idAgrupamento > 0){
			if(this.getAgrupamentoDeMovimentosTemporarios(processInstance) != idAgrupamento) {
				this.apagarMovimentosTemporarios(processInstance);
			}
			processInstance.getContextInstance().setVariable(Variaveis.VARIABLE_AGRUPAMENTOS_LANCADOS_TEMPORARIAMENTE, idAgrupamento);
		}
	}
}
