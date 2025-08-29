/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO;
import br.jus.cnj.pje.business.dao.ProcessoParteExpedienteDAO.CriterioPesquisa;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.extensao.ConectorECT;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.AvisoRecebimentoECT;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.EnderecoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.je.pje.entity.vo.SituacaoExpedienteVO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.UnificacaoPessoas;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.CategoriaPrazoEnum;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.PJEHolder;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name(ProcessoParteExpedienteManager.NAME)
public class ProcessoParteExpedienteManager extends BaseManager<ProcessoParteExpediente>{

	public static final String NAME = "processoParteExpedienteManager";
	
	private Integer idUsuarioAtual;
	private Integer idLocalizacaoAtual;
	private TipoUsuarioExternoEnum tipoUsuarioExternoAtual;
	private Integer idProcuradoriaAtual;
	private boolean isProcuradorGestor;
	private boolean mni;
	
	@Logger
	private Log logger;
	
	@In(create = true)
	private ProcessoParteExpedienteDAO processoParteExpedienteDAO;

	@In(create = true)
	private PrazosProcessuaisService prazosProcessuaisService;

	@In(create = true, required = false)
	private EnderecoService enderecoService;

	@In(create = true)
	private ProcessoJudicialManager processoJudicialManager;
	
	@In(create = true)
	private ProcuradorManager procuradorManager;
	
	@In(create = true)
	private PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager;

    @In(create = true, required = false)
    private ConectorECT conectorECT;	
	
    @In(create = true, required = false)
	private ParametroService parametroService;
    
    @In
    private JurisdicaoManager jurisdicaoManager;
    
    @In
    private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;

    @In
    PessoaProcuradoriaManager pessoaProcuradoriaManager;

	@In
	private PessoaService pessoaService;

    @In
    private DomicilioEletronicoService domicilioEletronicoService;
    
    @Create
	public void inicializaDadosUsuario() {
		this.idUsuarioAtual = Authenticator.getIdUsuarioLogado();
		this.idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		this.tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
		this.idProcuradoriaAtual = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
		this.isProcuradorGestor = Authenticator.isRepresentanteGestor();
		this.mni = Authenticator.isWS();
	}
    
	@Override
	protected ProcessoParteExpedienteDAO getDAO(){
		return this.processoParteExpedienteDAO;
	}
	
	public static ProcessoParteExpedienteManager instance() {
		return (ProcessoParteExpedienteManager)Component.getInstance(ProcessoParteExpedienteManager.NAME);
	}

	/**
	 * Recupera um ato de comunicação novo que tem como destinatário a pessoa
	 * dada.
	 * 
	 * @param p a pessoa que figurará como destinatário no ato de comunicação
	 * @return um ato de comunicação ({@link ProcessoParteExpediente}) novo que tem como
	 * destinatário a pessoa p. 
	 */
	public ProcessoParteExpediente getExpedientePessoal(Pessoa p){
		return getExpedientePessoal(p, null);
	}
	
	public List<Integer> getAtosComunicacaoExpirados(Date date) {
		Search search = new Search(ProcessoParteExpediente.class);
		search.setRetrieveField("idProcessoParteExpediente");
		addCriteria(search,
				Criteria.equals("fechado", false),
				Criteria.not(Criteria.isNull("dtCienciaParte")),
				Criteria.not(Criteria.equals("tipoPrazo", TipoPrazoEnum.S)),
				Criteria.less("dtPrazoLegal", date),
				Criteria.or(Criteria.equals("tipoPrazo", TipoPrazoEnum.C),Criteria.greater("prazoLegal", 0)));
		return list(search);
	}
	
	/**
	 * Recupera um ato de comunicação novo que tem como destinatário a pessoa
	 * dada.
	 * 
	 * @param p a pessoa que figurará como destinatário no ato de comunicação
	 * @param procuradoriaSelecionada irá selecionar a procuradoria selecionada ou a primeira desde que exista
	 * @return um ato de comunicação ({@link ProcessoParteExpediente}) novo que tem como
	 * destinatário a pessoa p. 
	 */
	public ProcessoParteExpediente getExpedientePessoal(Pessoa p, Procuradoria procuradoriaSelecionada){
		ProcessoParteExpediente ppe = new ProcessoParteExpediente();
		ppe.setCienciaSistema(false);
		ppe.setDtCienciaParte(null);
		ppe.setDtPrazoLegal(null);
		ppe.setDtPrazoProcessual(null);
		ppe.setPessoaParte(p);
		ppe.setNomePessoaParte(p.getNome());
		ppe.setPrazoLegal(5);
		ppe.setTipoPrazo(TipoPrazoEnum.D);
		
		if(procuradoriaSelecionada != null) {
			ppe.setProcuradoria(procuradoriaSelecionada);
		} else {
			ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager)Component.getInstance("procuradoriaManager");
			List<Procuradoria> procuradorias = procuradoriaManager.getlistProcuradorias(p);

			if(procuradorias != null && procuradorias.size() > 0) {
				ppe.setProcuradoria(procuradorias.get(0));
			}
		}
		return ppe;
	}
	
	public ProcessoParteExpediente obtemExpedientePessoal(Pessoa pessoa, ProcessoParte processoParte) {
		ProcessoParteExpediente ppe = null;
		if (processoParte != null) {
			ppe = getExpedientePessoal(pessoa, processoParte.getProcuradoria());
			
			// Obtém o endereço processual da parte
			if (ArrayUtil.isListNotEmpty(processoParte.getEnderecos())) {
				List<ProcessoParteExpedienteEndereco> enderecosProcessuais = new ArrayList<>(0);
				Endereco enderecoProcessual = processoParte.getEnderecos().get(0);
				ProcessoParteExpedienteEndereco ppee = new ProcessoParteExpedienteEndereco();
				ppee.setProcessoParteExpediente(ppe);
				ppee.setEndereco(enderecoProcessual);
				enderecosProcessuais.add(ppee);
				ppe.setProcessoParteExpedienteEnderecoList(enderecosProcessuais);
			}
		} else {
			ppe = getExpedientePessoal(pessoa, null);
		}
		return ppe;
	}        
        
	/**
	 * Recupera um ato de comunicação novo que tem como destinatário a parte parte
	 * dada.
	 * 
	 * @param pp a parte cuja pessoa figurará como destinatário no ato de comunicação
	 * @return um ato de comunicação ({@link ProcessoParteExpediente}) novo que tem como
	 * destinatário a pessoa constante como parte. 
	 */
	public ProcessoParteExpediente getExpedientePessoal(ProcessoParte pp){
		return getExpedientePessoal(pp.getPessoa(), pp.getProcuradoria());
	}

	/**
	 * Prepara o ato de comunicação dado, incluindo a preparação para a contagem automatizada de
	 * início do prazo de graça de que trata a Lei 11.419/2006.
	 * 
	 * @param ppe o ato de comunicação pessoal que será preparado
	 * @param pe o expediente que deu origem ao ato de comunicação preparado 
	 * @param processo o processo judicial ao qual está vinculada a comunicação.
	 * @param calendario o calendario de eventos aplicáveis ao orgao julgador
	 * @throws PJeBusinessException
	 */
	public void preparaComunicacao(ProcessoParteExpediente ppe, ProcessoExpediente pe, ProcessoTrf processo, Calendario calendario) throws PJeBusinessException{
		ppe.setProcessoExpediente(pe);
		ppe.setProcessoJudicial(processo);
		if (ppe.getTipoPrazo() == TipoPrazoEnum.S || ppe.getTipoPrazo() == TipoPrazoEnum.C){
			ppe.setPrazoLegal(null);
			ppe.setPrazoProcessual(null);
		}

		if (!TipoPrazoEnum.C.equals(ppe.getTipoPrazo()) && pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.E) {
			if (ppe.getDtPrazoProcessual() == null) {
				ppe.setDtPrazoProcessual(new Date());
			}
			Date dataLimiteCiencia = prazosProcessuaisService.obtemDataIntimacaoComunicacaoEletronica(ppe.getDtPrazoProcessual(), 
					calendario, ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoCiencia(), ContagemPrazoEnum.C);
			ppe.setDtPrazoLegal(dataLimiteCiencia);
			ppe.setDtPrazoProcessual(pe.getDtCriacao());
		}

		List<ProcessoParteExpedienteEndereco> enderecosFinais = new ArrayList<ProcessoParteExpedienteEndereco>(0);
		for (Endereco e : ppe.getEnderecos()){
			if (e != null && e.getIdEndereco() != 0){
				Endereco aux = enderecoService.getEndereco(e.getIdEndereco());
				ProcessoParteExpedienteEndereco ppee = new ProcessoParteExpedienteEndereco();
				if (aux == null){
					e.setIdEndereco(0);					
					ppee.setProcessoParteExpediente(ppe);
					ppee.setEndereco(e);
					enderecosFinais.add(ppee);
				}
				else{
					ppee.setProcessoParteExpediente(ppe);
					ppee.setEndereco(aux);
					enderecosFinais.add(ppee);
				}
			}
		}
		ppe.getProcessoParteExpedienteEnderecoList().clear();
		ppe.setProcessoParteExpedienteEnderecoList(enderecosFinais);
	}

	/**
	 * Grava um ato de comunicação, fazendo as vinculações necessárias entre ele, o ato que lhes deu origem e o
	 * processo judicial em que foi produzido.
	 * 
	 * @param ppe o ato de comunicação a ser gravado
	 * @param pe o ato que deu origem à comunicação
	 * @param processo o processo judicial em que o ato foi produzido
	 * @param calendario O calendario com uma lista de eventos aplicáveis ao orgao julgador do expediente
	 * @return o ato de comunicação gravado
	 * @throws PJeBusinessException
	 * @throws PJeDAOException
	 */
	public ProcessoParteExpediente persist(ProcessoParteExpediente ppe, ProcessoExpediente pe, ProcessoTrf processo, Calendario calendario)
			throws PJeBusinessException, PJeDAOException{
		preparaComunicacao(ppe, pe, processo, calendario);
		return persist(ppe);
	}

	/**
	 * Registra que foi tomada ciência a ato de comunicação por seu respectivo destinatário.
	 * 
	 * @param ppe o ato de comunicação sobre o qual houve a ciência
	 * @param date a data de ciência do ato
	 * @param force indicativo de que a ciência deve ser atualizada para a data dada ainda que 
	 * ela já tenha sido registrada.
	 * @param calendario o calendario com uma lista de eventos do orgao julgador a ser considerada no registro de ciência
	 */
	public void registraCiencia(ProcessoParteExpediente ppe, Date date, boolean force, Calendario calendario, boolean flush){
		registraCiencia(ppe, date, force, calendario, flush, true);
	}
	
	/**
	 * Registra que foi tomada ciência a ato de comunicação por seu respectivo destinatário.
	 * 
	 * @param ppe o ato de comunicação sobre o qual houve a ciência
	 * @param date a data de ciência do ato
	 * @param force indicativo de que a ciência deve ser atualizada para a data dada ainda que 
	 * ela já tenha sido registrada.
	 * @param calendario o calendario com uma lista de eventos do orgao julgador a ser considerada no registro de ciência
	 * @param isIntegrarDomicilio True indica que a ciência será sinalizada 
	 * também ao Domicílio Eletrônico.
	 */
	public void registraCiencia(ProcessoParteExpediente ppe, Date date, boolean force, Calendario calendario, boolean flush, boolean isIntegrarDomicilio){
		registraCienciaSemTransitar(ppe, date, force, calendario, flush, isIntegrarDomicilio);
		try {
			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_CIENCIA_DADA, ppe.getProcessoJudicial());
		} catch (Throwable e) {
			logger.error("{registraCiencia} Erro ao transitar processo: " + e.getMessage());
			logger.error(e);
		}
	}
	
	public void registraCiencia(ProcessoParteExpediente ppe, Date date, boolean force, Calendario calendario){
		registraCiencia(ppe, date, force, calendario, true);
	}

	public void registraCienciaSemTransitar(ProcessoParteExpediente ppe, Date date, boolean force, Calendario calendario, boolean flush){		
		registraCienciaSemTransitar(ppe, date, force, calendario, flush, true);
	}
	
	public void registraCienciaSemTransitar(ProcessoParteExpediente ppe, Date date, boolean force,
			Calendario calendario, boolean flush, boolean isIntegrarDomicilio) {
		if (ppe.getDtCienciaParte() != null && !force) {
			return;
		}
		ppe = EntityUtil.getEntityManager().merge(ppe);
		ppe.setDtCienciaParte(date);
		TipoPrazoEnum tipoPrazo = ppe.getTipoPrazo();
		if (ppe.getPrazoLegal() != null && ppe.getPrazoLegal() != 0 && tipoPrazo != TipoPrazoEnum.S
				&& tipoPrazo != TipoPrazoEnum.C) {
			CategoriaPrazoEnum categoriaPrazo = (ppe.getProcessoJudicial().getCompetencia() == null)
					? CategoriaPrazoEnum.U
					: ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual();

			Date dataInicialContagemPrazoProcessual = (Date) date.clone();

			if (isIntegrarDomicilio && domicilioEletronicoService.isCitacaoEnviadaDomicilioEletronico(ppe)) {

				dataInicialContagemPrazoProcessual = domicilioEletronicoService
						.calculaReferenciaDataPrazoLegalParaCitacaoEnviadaAoDomicilioEletronico(calendario, date);

			}
			Date dataFinal = prazosProcessuaisService.calculaPrazoProcessual(dataInicialContagemPrazoProcessual,
					ppe.getPrazoLegal(), tipoPrazo, calendario, categoriaPrazo, ContagemPrazoEnum.M);
			ppe.setDtPrazoLegal(dataFinal);
			if (dataFinal.before(new Date())) {
				ppe.setFechado(true);
				Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, ppe.getProcessoJudicial());
			}
		} else if (tipoPrazo == TipoPrazoEnum.S) {
			ppe.setDtPrazoLegal(null);
		}
		processoParteExpedienteDAO.persist(ppe);
		if (flush) {
			processoParteExpedienteDAO.flush();
		}

		// Registrar ciência no Domicílio somente se a requisição atual não for
		// originada pelo próprio Domicílio,
		// pois o próprio Domicílio registra ciência nos seus expedientes.
		if (isIntegrarDomicilio && !PJEHolder.isWebhookAction()) {
			domicilioEletronicoService.registraCiencia(ppe);
		}
	}
	
	public void registraCienciaSemTransitar(ProcessoParteExpediente ppe, Date date, boolean force, Calendario calendario){
		registraCienciaSemTransitar(ppe, date, force, calendario, true);
	}

	public List<ProcessoParteExpediente> getParteExpedienteFromProcesso(ProcessoTrf processo){
		return processoParteExpedienteDAO.getParteExpedienteFromProcesso(processo);
	}

	public List<ProcessoParteExpediente> getAtosComunicacao(Pessoa advogado, ProcessoTrf processoJudicial, int firstRow, int maxRows, CriterioPesquisa criterio, Pessoa...representados){
		return processoParteExpedienteDAO.getAtosComunicacao(advogado, processoJudicial, firstRow, maxRows, criterio, representados);
	}
	
	public long contagemAtos(Pessoa advogado, ProcessoTrf processoJudicial, CriterioPesquisa criterio, Pessoa...representados){
		return processoParteExpedienteDAO.contagemAtos(advogado, processoJudicial, criterio, representados);
	}
	
	/**
	 * @see ProcessoParteExpedienteDAO#contagemExpedientesPendentesCiencia(ProcessoDocumento, Pessoa)
	 */
	public Long contagemExpedientesPendentesCiencia(ProcessoDocumento documento, Pessoa pessoa){
		return processoParteExpedienteDAO.contagemExpedientesPendentesCiencia(documento, pessoa);
	}
	
	public List<Integer> mapaExpedientesPendentes(List<Integer> lista){
		return processoParteExpedienteDAO.mapaExpedientesPendentes(lista);
	}
	
	public Long contagemExpedientesPendentesCienciaProcuradoria(ProcessoDocumento documento, Pessoa pessoa, List<Integer> ids){
		return processoParteExpedienteDAO.contagemExpedientesPendentesCienciaProcuradoria(documento, pessoa, ids);
	}
	
	/**
	 * @see ProcessoParteExpedienteDAO#contagemExpedientesPendentesCiencia(ProcessoDocumento)
	 */
	public Long contagemExpedientesPendentesCiencia(ProcessoDocumento documento){
		return processoParteExpedienteDAO.contagemExpedientesPendentesCiencia(documento);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentes(ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoPendentes(processos);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoEletronicoPendentes(ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoEletronicoPendentes(processos);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesCiencia(ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoPendentesCiencia(processos);
	}
	
	public List<Integer> getAtosComunicacaoPendentesCienciaIds(List<ExpedicaoExpedienteEnum> meios, Date dataLimite, ProcessoTrf...processos) throws PJeBusinessException{
		Search search = getSearchAtosComunicacaoPendentesCiencia(true, meios, dataLimite, processos);
		return list(search);
	}

	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesCienciaList(List<ExpedicaoExpedienteEnum> meios, Date dataLimite, ProcessoTrf...processos) throws PJeBusinessException{
		Search search = getSearchAtosComunicacaoPendentesCiencia(false, meios, dataLimite, processos);
		return list(search);
	}

	public List<Integer> getIdsProcessoParteExpedienteByIdProcessoExpediente(Integer idProcessoExpediente) {
		Search search = new Search(ProcessoParteExpediente.class);

		search.setRetrieveField("idProcessoParteExpediente");

		addCriteria(search, Criteria.equals("processoExpediente.idProcessoExpediente", idProcessoExpediente));

		return list(search);
	}

	private Search getSearchAtosComunicacaoPendentesCiencia(boolean returnJustId, List<ExpedicaoExpedienteEnum> meios, Date dataLimite, ProcessoTrf...processos) {
		Search search = new Search(ProcessoParteExpediente.class);
		if(returnJustId) {
			search.setRetrieveField("idProcessoParteExpediente");
		}
		addCriteria(search, 
				Criteria.isNull("dtCienciaParte"),
				Criteria.in("processoExpediente.meioExpedicaoExpediente", meios.toArray(new ExpedicaoExpedienteEnum[meios.size()])),
				Criteria.equals("fechado", false));
		if(dataLimite != null){
			addCriteria(search, Criteria.or(
												Criteria.isNull("dtPrazoLegal"),
												Criteria.less("dtPrazoLegal", dataLimite)));
		}
		if(processos != null && processos.length > 0){
			addCriteria(search, Criteria.in("processoJudicial", processos));
		}

		search.addOrder("idProcessoParteExpediente", Order.DESC);

		return search;
	}
	
	public List<Integer> getAtosComunicacaoPendentesCienciaIds(List<ExpedicaoExpedienteEnum> meios) throws PJeBusinessException{
		return getAtosComunicacaoPendentesCienciaIds(meios, new Date(), new ProcessoTrf[0]);
	}

	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesCienciaList(List<ExpedicaoExpedienteEnum> meios) throws PJeBusinessException{
		return getAtosComunicacaoPendentesCienciaList(meios, new Date(), new ProcessoTrf[0]);
	}

	public List<Integer> getAtosComunicacaoExpiradosIds(Date date) throws PJeBusinessException{
		return list(getSearchAtosComunicacaoExpirados(true, date));
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoExpiradosList(Date date) throws PJeBusinessException{
		return list(getSearchAtosComunicacaoExpirados(false, date));
	}
	
	private Search getSearchAtosComunicacaoExpirados(boolean returnJustId, Date date) {
		Search search = new Search(ProcessoParteExpediente.class);
		if(returnJustId) {
			search.setRetrieveField("idProcessoParteExpediente");
		}
		addCriteria(search,
				Criteria.equals("fechado", false),
				Criteria.less("dtPrazoLegal", date),
				Criteria.not(Criteria.equals("tipoPrazo", TipoPrazoEnum.S)),
				Criteria.or(Criteria.not(Criteria.isNull("dtCienciaParte")), Criteria.equals("tipoPrazo", TipoPrazoEnum.C)));

		search.addOrder("idProcessoParteExpediente", Order.DESC);

		return search;
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoDecorridos(ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoDecorridos(new Date(), processos);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoDecorridos(Date data){
		return processoParteExpedienteDAO.getAtosComunicacaoDecorridos(data);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoDataCerta(long diasAntes, ProcessoTrf...processos){
		return getAtosComunicacaoDataCerta(diasAntes, new Date(), processos);
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoDataCerta(long diasAntes, Date dataReferencia, ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoDataCerta(diasAntes, dataReferencia, processos);
	}
	
	public List<Integer> getAtosComunicacaoSemPrazoExpiradosIds(long maximoDias, Date data) throws PJeBusinessException{
		Search search = getSearchAtosComunicacaoSemPrazoExpirados(true, maximoDias, data);
		return list(search);
	}

	public List<ProcessoParteExpediente> getAtosComunicacaoSemPrazoExpiradosList(long maximoDias, Date data) throws PJeBusinessException{
		Search search = getSearchAtosComunicacaoSemPrazoExpirados(false, maximoDias, data);
		return list(search);
	}
	
	private Search getSearchAtosComunicacaoSemPrazoExpirados(boolean returnJustId, long maximoDias, Date data){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(data);
		cal.add(Calendar.DAY_OF_YEAR, (int) - maximoDias);
		Search search = new Search(ProcessoParteExpediente.class);
		if(returnJustId) {
			search.setRetrieveField("idProcessoParteExpediente");
		}
		addCriteria(search, 
				Criteria.equals("fechado", false),	// Expedientes não fechados
				Criteria.or(
						Criteria.equals("tipoPrazo", TipoPrazoEnum.S),
						Criteria.isNull("prazoLegal"),
						Criteria.equals("prazoLegal", 0)
						), // Expedientes que sejam expressa ou implicitamente sem prazo
				Criteria.not(Criteria.equals("tipoPrazo", TipoPrazoEnum.C)), // e que não sejam de data certa				
				Criteria.less("processoExpediente.dtCriacao", cal.getTime()) // e cuja data de criação
				);

		search.addOrder("idProcessoParteExpediente", Order.DESC);

		return search;
	}
	
	public List<ProcessoParteExpediente> getAtosComunicacaoSemPrazo(long maximoDias, ProcessoTrf...processos){
		return getAtosComunicacaoSemPrazo(maximoDias, new Date(), processos);
	}

	public List<ProcessoParteExpediente> getAtosComunicacaoSemPrazo(long maximoDias, Date dataReferencia, ProcessoTrf...processos){
		return processoParteExpedienteDAO.getAtosComunicacaoSemPrazo(maximoDias, dataReferencia, processos);
	}

	/**
	 * Registra que foi tomada ciência a ato de comunicação por seu respectivo destinatário.
	 * 
	 * @param ppe o ato de comunicação sobre o qual houve a ciência
	 * @param date a data de ciência do ato
	 * @param calendario O calendario com a lista de eventos do orgao julgador a ser considerada no registro de ciência
	 */
	public void registraCienciaPessoal(ProcessoParteExpediente ppe, Date date, Calendario calendario){
		registraCiencia(ppe, date, false, calendario);
	}

	public Long countPartesNaoCientes(ProcessoExpediente pe){
		return processoParteExpedienteDAO.countPartesNaoCientes(pe);
	}

	public boolean existsPartesIntimacaoAutoPendente(ProcessoTrf p){
		Long result = processoParteExpedienteDAO.countPartesIntimacaoAutomaticaPendente(p);
		return result != null && result > 0;
	}

	public List<ProcessoParteExpediente> listPartesIntimacaoAutoPendente(int p){
		return processoParteExpedienteDAO.listPartesIntimacaoAutoPendente(p);
	}

	public Long countExpedienteEntidadeIntimacao(ProcessoTrf pTrf, Pessoa p){
		return processoParteExpedienteDAO.countExpedienteEntidadeIntimacao(pTrf, p);
	}

	public boolean existeExpedienteEntidadeIntimacao(ProcessoTrf pTrf, Pessoa p){
		return countExpedienteEntidadeIntimacao(pTrf, p) > 0;
	}

	public Long countAdvogadoIntimadoExpediente(ProcessoTrf pTrf, Pessoa p){
		return processoParteExpedienteDAO.countAdvogadoIntimadoExpediente(pTrf, p, ParametroUtil.instance()
				.getTipoParteAdvogado());
	}

	public boolean existeAdvogadoIntimadoExpediente(ProcessoTrf pTrf, Pessoa p){
		return countAdvogadoIntimadoExpediente(pTrf, p) > 0;
	}

	public List<ProcessoParteExpediente> processoParteExpedienteComDocumentoList(ProcessoTrf instance){
		return processoParteExpedienteDAO.processoParteExpedienteComDocumentoList(instance);
	}
	
	public boolean temDocumentoPendenteCiencia(ProcessoParteExpediente expediente){
		return processoParteExpedienteDAO.temDocumentoPendenteCiencia(expediente);
	}

	public boolean todosTomaramCiencia(Integer idProcesso) {
		ProcessoTrf processo = null;
		boolean retorno = false;
		try {
			processo = processoJudicialManager.findById(idProcesso);
			retorno = processoParteExpedienteDAO.todosTomaramCiencia(processo);
		} catch (PJeBusinessException e) {
			retorno = false;
			e.printStackTrace();
		}
		
		return retorno;
	}
	
	/**
	 * @see ProcessoParteExpedienteDAO#getPartesDoExpedienteBy(Integer)
	 */
	public List<ProcessoParteExpediente> getPartesDoExpedienteBy(Integer idDocumento){
		return processoParteExpedienteDAO.getPartesDoExpedienteBy(idDocumento, false);
	}

    /**
    * @see ProcessoParteExpedienteDAO#getPartesDoExpedienteBy(Integer, Boolean)
    */
    public List<ProcessoParteExpediente> getPartesDoExpedienteBy(Integer idDocumento, boolean apenasDocumentoPrincipalExpediente){
                   return processoParteExpedienteDAO.getPartesDoExpedienteBy(idDocumento, apenasDocumentoPrincipalExpediente);
    }

	private void limitarExpedientesProprios(Search search, Usuario usuario) throws PJeBusinessException{
		limitarExpedientesProprios(search,usuario,null);
	}
	
	private void limitarExpedientesProprios(Search search, Usuario usuario,Integer idProcesso) throws PJeBusinessException{
		List<Criteria> criterias = new ArrayList<Criteria>();

		if(idProcesso != null){
	 		criterias.add(Criteria.equals("processoJudicial.idProcessoTrf", idProcesso));
	 	}
		
		Integer idProcuradoriaAtual = null;
		if(Contexts.getSessionContext().get(Authenticator.ID_PROCURADORIA_ATUAL) != null){
			idProcuradoriaAtual = (Integer) Contexts.getSessionContext().get(Authenticator.ID_PROCURADORIA_ATUAL);
		}
		
		Criteria destinatario = null;
		if(idProcuradoriaAtual != null){
			destinatario = Criteria.equals("procuradoria.idProcuradoria", idProcuradoriaAtual);
		}
		else{
			destinatario = Criteria.and(Criteria.equals("destinatarioList.idDestinatario", usuario.getIdUsuario()));
		}
		
		Criteria parte = Criteria.and(Criteria.equals("processoJudicial.processoParteList.inSituacao", ProcessoParteSituacaoEnum.A), destinatario);
		criterias.add(parte);
		addCriteria(search, criterias.toArray(new Criteria[criterias.size()]));
	}
	
	/**
	 * Inclui em uma dada pesquisa critérios que limitam a resposta aos atos de comunicação
	 * que têm definida uma data de ciência, mas que não têm uma resposta associada.
	 * 
	 * @param search a pesquisa à qual serão acrescentados os critérios
	 * @throws PJeBusinessException
	 * @see ProcessoParteExpediente#getDtCienciaParte()
	 * @see ProcessoParteExpediente#getResposta()
	 */
	private void limitarExpedientesPendentesManifestacao(Search search, Boolean pendentesCiencia) throws PJeBusinessException{
		if(pendentesCiencia){
			addCriteria(search, 
					Criteria.isNull("resposta"));
		}else{
			addCriteria(search, 
					Criteria.not(Criteria.isNull("dtCienciaParte")),
					Criteria.isNull("resposta"));
		}
	}
	
	/**
	 * Inclui em uma dada pesquisa critérios que limitam a resposta aos atos de comunicação 
	 * que não têm definidas quer data de ciência, quer resposta.
	 * 
	 * @param search a pesquisa à qual serão acrescentados os critérios
	 * @throws PJeBusinessException
	 * @see ProcessoParteExpediente#getDtCienciaParte()
	 * @see ProcessoParteExpediente#getResposta()
	 */
	private void limitarPendentesCiencia(Search search) throws PJeBusinessException{
		addCriteria(search, 
				Criteria.isNull("dtCienciaParte"),
				Criteria.isNull("resposta"),
				Criteria.equals("fechado", Boolean.FALSE));
	}
	
	/**
	 * Recupera a lista de objetos indicados no search em que os atos de comunicação associados
	 * receberam ciência de seus destinatários, têm prazo de manifestação definido e que ainda 
	 * não foi superado e aos quais ainda não foi dada resposta.
	 * 
	 * @param search a pesquisa a ser realizada
	 * @param usuario o usuário a respeito do qual será feita a pesquisa
	 * @return a lista de objetos definido no search que atedem aos critérios
	 * @throws PJeBusinessException
	 * 
	 * @see #limitarExpedientesProprios(Search, Usuario, List)
	 * @see #limitarExpedientesComPrazo(Search)
	 * @see #limitarExpedientesPendentesManifestacao(Search)
	 */
	public <T> List<T> listPendentesManifestacao(Search search, Usuario usuario) throws PJeBusinessException {
		return listPendentesManifestacao(search, usuario,false);
	}
	
	
	public <T> List<T> listPendentesManifestacao(Search search, Usuario usuario, boolean pendentesCiencia) throws PJeBusinessException {
		validarSearch(search, ProcessoParteExpediente.class);
		if (Identity.instance().hasRole(Papeis.PERMITE_RESPONDER_EXPEDIENTE)){
			pendentesCiencia = true;			
		} else {
			limitarExpedientesProprios(search, usuario);
		}
		limitarExpedientesPendentesManifestacao(search,pendentesCiencia);
		addCriteria(search, Criteria.equals("fechado", false));
		search.close();
		if(search.getMax() != null && search.getMax() == 0){
			return Collections.emptyList();
		}
		return list(search);
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Recupera uma lista de expedientes limitada por expedientes sem registro de ciência ou sem resposta,
	 * caso o parametro tipoSituacaoExpediente seja nulo ou limita a lista dependendo da situação passada 
	 * por parametro.
	 * 
	 * @param search a pesquisa a ser realizada
	 * @param usuario o usuário a respeito do qual será feita a pesquisa
	 * @param tipoSituacaoExpediente o tipo de situação que devem limitar os expedientes
	 * @return a lista de objetos definido no search que atedem aos critérios
	 * @throws PJeBusinessException
	 */
	public <T> List<T> listExpedientes(Search search, Usuario usuario, TipoSituacaoExpedienteEnum tipoSituacaoExpediente) throws PJeBusinessException {

		validarSearch(search, ProcessoParteExpediente.class);

		List<Criteria> criterias = new ArrayList<Criteria>();
		
		//limita os expedientes por processos distribuidos
		limitarExpedientesPorStatusProcesso(criterias, ProcessoStatusEnum.D);
		
		//restringe os expedientes de acordo com as permissões de visualização do usuário logado
		limitarExpedientesPorUsuario(criterias, usuario);
				
		//restringe os expedientes pelo tipo da situação
		limitarExpedientesPorSituacao(criterias, tipoSituacaoExpediente);

		if(criterias.size() > 0) {
			addCriteria(search, criterias.toArray(new Criteria[criterias.size()]));
		}

		search.close();
		if(search.getMax() != null && search.getMax() == 0){
			return Collections.emptyList();
		}
		return list(search);
	}
	
	private void limitarExpedientesPorStatusProcesso(List<Criteria> criterias, ProcessoStatusEnum processoStatus) {
		criterias.add(Criteria.equals("processoJudicial.processoStatus", processoStatus));
	}
	
	private void limitarExpedientesPorUsuario(List<Criteria> criterias, Usuario usuario) {
		
		if(Pessoa.instanceOf(usuario, PessoaProcurador.class) 
				&& (Authenticator.isProcurador() || usuarioHasRole(Papeis.CONSULTA_MNI))) {
			
			Procuradoria procuradoriaAtual = Authenticator.getProcuradoriaAtualUsuarioLogado();
				
			Criteria representante = Criteria.and(
				Criteria.equals("procuradoria.idProcuradoria", procuradoriaAtual.getIdProcuradoria()),
				Criteria.equals("procuradoria.pessoaProcuradoriaList.pessoa.idUsuario", usuario.getIdUsuario())
			);
			
			// chefe de procuradoria
			Criteria critChefeProcuradoria = Criteria.equals("procuradoria.pessoaProcuradoriaList.chefeProcuradoria", true);

			// distribuidor
			Criteria critDistribuidor = Criteria.equals("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList.ativo", true);
			critDistribuidor.setRequired("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList", false);			
			critDistribuidor = Criteria.and(
					critDistribuidor,
					Criteria.equals(Criteria.path("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList.jurisdicao.idJurisdicao"),Criteria.path("processoJudicial.jurisdicao.idJurisdicao")));

			// procurador padrão, processos precisam estar em sua caixa.
			Criteria critProcPadrao = Criteria.equals("caixasRepresentantes.caixaAdvogadoProcurador.caixaRepresentanteList.representante.idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());			
			critProcPadrao.setRequired("caixasRepresentantes", false);
			critProcPadrao.setRequired("caixasRepresentantes.caixaAdvogadoProcurador.caixaRepresentanteList", false);
			
			Criteria critProcuradoria = Criteria.and(representante, 
					Criteria.or(critChefeProcuradoria, critDistribuidor, critProcPadrao));
			
			// Critério que filtra o representante (procurador ou defensor) do processo.
			criterias.add(critProcuradoria);
			
		} else if(Pessoa.instanceOf(usuario, PessoaAdvogado.class) 
				&& (Authenticator.isAdvogado() 
						|| usuarioHasRole(Papeis.CONSULTA_MNI))) {

			Criteria rep = Criteria.equals("processoJudicial.processoParteList.processoParteRepresentanteList.representante.idUsuario", usuario.getIdUsuario());
			Criteria inSituacao = Criteria.equals("processoJudicial.processoParteList.processoParteRepresentanteList.inSituacao", ProcessoParteSituacaoEnum.A);
			Criteria tipoRepresentante = Criteria.equals("processoJudicial.processoParteList.processoParteRepresentanteList.tipoRepresentante", ParametroUtil.instance().getTipoParteAdvogado());
			
			rep.setRequired("processoJudicial.processoParteList.processoParteRepresentanteList", false);
			inSituacao.setRequired("processoJudicial.processoParteList.processoParteRepresentanteList", false);
			tipoRepresentante.setRequired("processoJudicial.processoParteList.processoParteRepresentanteList", false);
			
			Criteria representante = 
				Criteria.or(
					(Criteria.equals("pessoaParte.idUsuario", usuario.getIdUsuario())),
					Criteria.and(
						tipoRepresentante,
						inSituacao,
						Criteria.equals(Criteria.path("pessoaParte.idUsuario"), Criteria.path("processoJudicial.processoParteList.pessoa.idUsuario")),
						Criteria.equals("intimacaoPessoal", Boolean.FALSE),
						rep
					)
			);
			
			// Critério que filtra o representante (advogado) do processo.
			criterias.add(representante);
		} else {
			// Critério que filtra a parte do processo.
			criterias.add(Criteria.equals("pessoaParte.idUsuario", usuario.getIdUsuario()));
		}
		
	}

	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Acrescenta à pesquisa os filtros de acordo com o tipo da situação dos expedientes.
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 * @param tipoSituacaoExpediente o tipo da situação dos expedientes
	 */
	private void limitarExpedientesPorSituacao(List<Criteria> criterias, TipoSituacaoExpedienteEnum tipoSituacaoExpediente){
		if(tipoSituacaoExpediente != null) {
			switch (tipoSituacaoExpediente) {
			case PENDENTES_CIENCIA: //Pendentes de ciência ou de seu registro
				addCriteriaExpedientesPendentesCiencias(criterias);
				break;
			case CIENCIA_DESTINATARIO: // Ciência dada pelo destinatário direto ou indireto e dentro do prazo
				addCriteriaExpedientesCienciaDestinatario(criterias);
				break;
			case CIENCIA_JUDICIARIO: // Ciência dada pelo Judiciário e dentro do prazo
				addCriteriaExpedientesCienciaJudiciario(criterias);
				break;
			case PRAZO: // Cujos prazos expiraram sem resposta nos últimos 10 dias.
				addCriteriaExpedientesPrazoDezDias(criterias);
				break;
			case SEM_PRAZO: // Sem prazo
				addCriteriaExpedientesSemPrazos(criterias);
				break;
			case RESPONDIDOS: // Respondidos nos últimos 10 dias
				addCriteriaExpedientesRespondidos(criterias);
				break;
			case PENDENTES_CIENCIA_RESPOSTA: // Pendentes de ciência e resposta (Pendentes de manifestação)
			default: // Pendentes de ciência e resposta (Pendentes de manifestação)
				addCriteriaExpedientesPendentesCienciaResposta(criterias);
			}
		} else {
			// Pendentes de ciência e resposta (Pendentes de manifestação)
			addCriteriaExpedientesPendentesCienciaResposta(criterias);
		}
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes que estão pendentes de ciencia ou de respostas
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesPendentesCienciaResposta(List<Criteria> criterias){
		criterias.add(Criteria.equals("fechado", false));
		Criteria expSemCienciaOuResposta = Criteria.or(Criteria.isNull("dtCienciaParte"),Criteria.isNull("resposta"));
		criterias.add(expSemCienciaOuResposta);
		criterias.add(Criteria.greater("dtPrazoLegal", new Date()));
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes que estão pendentes de ciencia ou de seu registro
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesPendentesCiencias(List<Criteria> criterias){
		criterias.add(Criteria.equals("fechado", false));
		criterias.add(Criteria.isNull("dtCienciaParte"));
		criterias.add(Criteria.or(Criteria.greaterOrEquals("dtPrazoLegal", new Date()), Criteria.equals("tipoPrazo", TipoPrazoEnum.S)));
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes que tiveram ciência dada pelo destinatário direto ou indireto e dentro do prazo
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesCienciaDestinatario(List<Criteria> criterias){
		criterias.add(Criteria.equals("fechado", false));
		criterias.add(Criteria.greaterOrEquals("dtPrazoLegal", new Date()));
		criterias.add(Criteria.not(Criteria.isNull("dtCienciaParte")));
		criterias.add(Criteria.equals("cienciaSistema", false));
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes que tiveram ciência dada pelo Judiciário e dentro do prazo
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesCienciaJudiciario(List<Criteria> criterias){
		criterias.add(Criteria.equals("fechado", false));
		criterias.add(Criteria.greaterOrEquals("dtPrazoLegal", new Date()));
		criterias.add(Criteria.not(Criteria.isNull("dtCienciaParte")));
		criterias.add(Criteria.equals("cienciaSistema", true));
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes cujo prazo findou nos últimos 10 dias
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesPrazoDezDias(List<Criteria> criterias){
		Date limite = DateUtil.getBeginningOfDay(DateUtil.dataMenosDias(new Date(), 10));
		criterias.add(Criteria.not(Criteria.equals("tipoPrazo",TipoPrazoEnum.S)));
		criterias.add(Criteria.isNull("resposta"));
		criterias.add(Criteria.between("dtPrazoLegal", limite, new Date()));		
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes sem prazo
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesSemPrazos(List<Criteria> criterias){
		criterias.add(Criteria.equals("fechado", false));
		criterias.add(Criteria.equals("tipoPrazo", TipoPrazoEnum.S));
		criterias.add(Criteria.not(Criteria.isNull("dtCienciaParte")));
	}
	
	/**
	 * [PJEII-18559 e PJEII-18891]
	 * Limita a pesquisa de expedientes respondidos nos últimos 10 dias
	 * 
	 * @param criterias a lista de critérios aos quais serão acrescentados os critérios de pesquisa
	 */
	private void addCriteriaExpedientesRespondidos(List<Criteria> criterias){
		Date limite = DateUtil.getBeginningOfDay(DateUtil.dataMenosDias(new Date(), 10));
		criterias.add(Criteria.equals("fechado", true));
		criterias.add(Criteria.not(Criteria.isNull("resposta")));
		criterias.add(Criteria.not(Criteria.isNull("resposta.data")));
		criterias.add(Criteria.greaterOrEquals("resposta.data", limite));
		criterias.add(Criteria.lessOrEquals("resposta.data", new Date()));
	}
	
	/**
	 * Recupera a lista de objetos indicados no search em que os atos de comunicação associados
	 * têm como destinatário o usuário indicado ou seus representados, ainda não receberam ciência, 
	 * têm prazo de manifestação definido e superior a zero e não receberam resposta.
	 * 
	 * @param search a pesquisa
	 * @param usuario o usuário a respeito do qual será feita a pesquisa
	 * @return a lista de objetos definido no search que atedem aos critérios
	 * @throws PJeBusinessException
	 * 
	 * @see #limitarExpedientesProprios(Search, Usuario, List)
	 * @see #limitarPendentesCiencia(Search)
	 * @see ProcessoParteExpediente#getPrazoLegal()
	 */
	public <T> List<T> pendentesCiencia(Search search, Usuario usuario) throws PJeBusinessException {
		validarSearch(search, ProcessoParteExpediente.class);

		limitarExpedientesProprios(search, usuario);
		limitarPendentesCiencia(search);
		search.close();
		if(search.getMax() != null && search.getMax() == 0){
			return Collections.emptyList();
		}
		return list(search);
	}

	/**
	 * Recupera a lista de expedientes em que os atos de comunicação associados
	 * têm como destinatário o usuário indicado ou seus representados, ainda não receberam ciência, 
	 * têm prazo de manifestação definido e superior ao parâmetro dataCriacao e não receberam resposta.
	 * 
	 * @param pessoa o usuário a respeito do qual será feita a pesquisa
	 * @param dataCriacao Data de criação do expediente que será usado como critério 'a partir de'.
	 * @param numeroProcesso Número do processo.
	 * @param idExpediente Id do expediente que será consultado.
	 * @return a lista de objetos definido no search que atedem aos critérios
	 * @throws PJeBusinessException
	 * 
	 * @see #limitarExpedientesProprios(Search, Usuario, List)
	 * @see #limitarPendentesCiencia(Search)
	 * @see ProcessoParteExpediente#getPrazoLegal()
	 */
	public List<ProcessoParteExpediente> pendentesCiencia(Pessoa pessoa, Date dataCriacao, String numeroProcesso, Integer idExpediente) throws PJeBusinessException {
		List<ProcessoParteExpediente> resultado = new ArrayList<ProcessoParteExpediente>();
		
		if (pessoa != null) {
			Search search = new Search(ProcessoParteExpediente.class);
			search.setDistinct(true);
			
			if(numeroProcesso != null) {
				addCriteria(search, Criteria.equals("processoJudicial.processo.numeroProcesso", mascaraNumeroProcesso(numeroProcesso) ));
			}
			if(idExpediente != null) {
				addCriteria(search, Criteria.equals("idProcessoParteExpediente", idExpediente));
			}
			if (dataCriacao != null) {
				addCriteria(search, Criteria.greaterOrEquals("processoExpediente.dtCriacao", dataCriacao));
			}
			
			resultado = pendentesCiencia(search, pessoa);
		}
		return resultado;
	}

	/**
	 * Consulta os expedientes pendentes para a parte a partir da data informada.
	 * 
	 * @param pessoa Parte
	 * @param data Data do prazo legal. (VERIFICAR)
	 * @return coleção de expedientes pendentes para a parte.
	 */
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(Pessoa pessoa, Date data) {
		List<ProcessoParteExpediente> resultado = new ArrayList<ProcessoParteExpediente>();
		
		if (pessoa != null && data != null) {
			resultado = getDAO().consultarExpedientesPendentes(pessoa, data);
		}
		
		return resultado;
	}

	/**
	 * Consulta os expedientes pendentes para a parte a partir da data informada.
	 * 
	 * @param pessoas Partes
	 * @param data Data do prazo legal. (VERIFICAR)
	 * @return coleção de expedientes pendentes para a parte.
	 */
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(List<Pessoa> pessoas, Date data) {
		List<ProcessoParteExpediente> resultado = new ArrayList<ProcessoParteExpediente>();
		if (ProjetoUtil.isNotVazio(pessoas) && data != null) {
			resultado = getDAO().consultarExpedientesPendentes(pessoas, data);
		}
		return resultado;
	}

	/**
	 * Consulta os expedientes pendentes para a parte a partir da data informada.
	 * 	 
	 * @param data Data do prazo legal. 
	 * @return coleção de expedientes pendentes para a parte.
	 */
	public List<ProcessoParteExpediente> consultarExpedientesPendentes(Integer idProcesso, Date data) {
		List<ProcessoParteExpediente> resultado = new ArrayList<ProcessoParteExpediente>();
		
		if (idProcesso != null && data != null) {
			resultado = getDAO().consultarExpedientesPendentes(idProcesso, data);
		}
		
		return resultado;
	}

	public boolean temExpediente(Usuario usuario,Integer idProcesso) throws PJeBusinessException {
		Search s = new Search(ProcessoParteExpediente.class);
		limitarExpedientesProprios(s, usuario,idProcesso);
		long count = count(s);
		return count == 0 ? false : true;
	}
	
	/**
	 * Verifica a entrega de uma expediente enviado atraves dos Correios,
	 * consultando base de dados do ConectorECT. Essa verificacao NAO vai na
	 * base dos Correios.
	 * 
	 * @param ProcessoParteExpediente
	 *            : ProcessoParteExpediente a ser verificado
	 * @return @see AvisoRecebimentoECT
	 * @throws @see PJeBusinessException
	 */
	public AvisoRecebimentoECT verificaEntrega(
			ProcessoParteExpedienteEndereco processoParteExpedienteEndereco)
			throws PJeBusinessException {
		
		if(processoParteExpedienteEndereco == null){
			throw new PJeBusinessException(
					"pje.processoParteExpedienteManager.parametroNaoInformado.erro.msg",
					null, "ProcessoParteExpediente");
		}
		
		if (processoParteExpedienteEndereco.getProcessoParteExpediente()
				.getProcessoExpediente().getMeioExpedicaoExpediente() != ExpedicaoExpedienteEnum.C) {
			throw new PJeBusinessException(
					"pje.processoParteExpedienteManager.verificarRastreioApenasExpedienteCorreios.warn.msg");
		}
		
		try {
			return conectorECT.verificaEntrega(processoParteExpedienteEndereco.getNumeroAr());
		} catch (PontoExtensaoException e) {
			throw new PJeBusinessException("conectorEct.default.error.msg", e, e.getLocalizedMessage());
		}
	}
	
	/**
	 * Recupera o rastreio da correspondencia consultando a base de dados dos
	 * Correios.
	 * 
	 * @param ProcessoParteExpediente
	 *            : ProcessoParteExpediente a ser rastreado
	 * @return byte[]: XML contendo o conteudo do rastreio
	 * @throws @see PJeBusinessException
	 */
	public String recuperaXMLRastreioCorrespondencia(
			ProcessoParteExpedienteEndereco processoParteExpedienteEndereco) {

		return processoParteExpedienteEndereco.getNumeroAr();
		
	}
	
	public List<Integer> recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEIds(String... codigo) {
		return processoParteExpedienteDAO.recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEIds(codigo);
	}

	public List<ProcessoParteExpediente> recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEList(String... codigo) {
		return processoParteExpedienteDAO.recuperaProcessoParteExpedientePeloCodigoDePublicacaoDJEList(codigo);
	}

	public List<Integer> recuperaProcessoParteExpedientePorMateriaPublicadaDJEIds(boolean consultaPeloRecibo, List<Integer> codigos) {
		return processoParteExpedienteDAO.recuperaProcessoParteExpedientePorMateriaPublicadaDJEIds(consultaPeloRecibo, codigos);
	}

	/**
	 * 
	 * @param consultaPeloRecibo - se false: busca por idProcessoExpediente, se true: busca por reciboPublicacaoDJE
	 * @param codigo
	 * @return
	 */
	public List<ProcessoParteExpediente> recuperaProcessoParteExpedientePorMateriaPublicadaDJEList(boolean consultaPeloRecibo, String... codigo) {
		return processoParteExpedienteDAO.recuperaProcessoParteExpedientePorMateriaPublicadaDJEList(consultaPeloRecibo, codigo);
	}

	/**
	 * Método responsável por fechar o expediente passado como parâmetro 
	 * Após a atualização o evento ProcessoJudicialService.EVENTO_EXPEDIENTE_FECHADO é chamado para o processo.
	 *
	 * @param processoParteExpediente processosParteExpediente que deve ser fechado
	 */
	public void fecharExpediente(ProcessoParteExpediente processoParteExpediente)  throws PJeBusinessException {
		if (processoParteExpediente != null) {
			try {
				Usuario usuarioSistema = ParametroUtil.instance().getUsuarioSistema();
				Usuario usuarioLogado = Authenticator.getUsuarioLogado();

				if (usuarioLogado != null && usuarioSistema != null
						&& usuarioSistema.getIdUsuario() != usuarioLogado.getIdUsuario()) {
					Pessoa p = pessoaService.findById(usuarioLogado.getIdUsuario());
					processoParteExpediente.setPessoaEncerramentoManual(p);
					processoParteExpediente.setDtEncerramentoManual(new Date());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			processoParteExpediente.setFechado(true);
			processoParteExpediente.setPendenteManifestacao(false);
			try{
				flush();
				Events.instance().raiseEvent(Eventos.EVENTO_EXPEDIENTE_FECHADO, processoParteExpediente.getProcessoExpediente().getProcessoTrf());
			} catch (PJeBusinessException excecao) {
				String msg = String.format("Erro ao tentar fechar o expediente: %s.", excecao.getLocalizedMessage());
				logger.error(msg);
				throw new PJeBusinessException(msg, excecao);
			}
		}
	}
	
	/**
	 * Método responsável por verificar se o expediente passado como parâmetro não está vencido, ou seja, não tem prazo ou tem prazo, mas ainda não vencido
	 * @param expediente expediente que será avaliado quanto ao vencimento
	 * @return retorna true se o expediente não tiver prazo associado ou não estiver vencido, false caso contrário 
	 */
	public boolean isExpedienteNaoVencido(ProcessoParteExpediente expediente){
		boolean retorno;
		Date now = new Date();
		if( expediente.getDtPrazoLegal() == null || (expediente.getDtPrazoLegal()).before(now)){
			retorno = true;
		} else {
			retorno = false;
		}
		return retorno;
	}

	public List<ProcessoParteExpediente> getExpedientesSemRespostaEnviadoViaSistema(Procuradoria procuradoria){
		Search search = new Search(ProcessoParteExpediente.class);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -30);
		addCriteria(search, Criteria.and(
				Criteria.equals("procuradoria.idProcuradoria", procuradoria.getIdProcuradoria()),
				Criteria.equals("processoExpediente.meioExpedicaoExpediente", ExpedicaoExpedienteEnum.E),
				Criteria.equals("fechado", false),
				Criteria.isNull("resposta"),
				Criteria.greater("processoExpediente.dtCriacao", cal.getTime())));
		return list(search);
	}
	
	/**
	 * Recupera a quantidade de expedientes em aberto do processo
	 * @param processoJudicial
	 * @return
	 */
	public Long getQuantidadeExpedientesAbertosPorProcesso(ProcessoTrf processoJudicial){
		return processoParteExpedienteDAO.countExpedienteNaoFechado(processoJudicial);
	}
	
	public List<ProcessoParteExpediente> consultarExpedientesPorPessoa(Pessoa pessoa) {
		return  processoParteExpedienteDAO.consultarExpedientesPorPessoa(pessoa);
	}
	
	public List<ProcessoParteExpediente> consultarExpedientesUnificados(UnificacaoPessoas ups) {
		return  processoParteExpedienteDAO.consultarExpedientesUnificados(ups);
	}
	
	public String recuperarQuantidadeExpediente() {
		return getProcessoParteExpedienteDAO().recuperaQuantidadeExpediente();
	}
	
	public ProcessoParteExpedienteDAO getProcessoParteExpedienteDAO() {
		if (processoParteExpedienteDAO == null) {
			processoParteExpedienteDAO = new ProcessoParteExpedienteDAO();
		}
		return processoParteExpedienteDAO;
	}

	public void setProcessoParteExpedienteDAO(
			ProcessoParteExpedienteDAO processoParteExpedienteDAO) {
		this.processoParteExpedienteDAO = processoParteExpedienteDAO;
	}
	
	/**
	 * Retorna os Expedientes Pendentes do usuário logado de acordo com o processo informado
	 * @param processo
	 * @return
	 * @throws PJeBusinessException 
	 * @throws NoSuchFieldException 
	 */
	public List<ProcessoParteExpediente> listExpUsuario(ProcessoTrf processo) throws PJeBusinessException{
		Search search = new Search(ProcessoParteExpediente.class);
		List<ProcessoParteExpediente> processosExpediente = new ArrayList<ProcessoParteExpediente>();
		try {
			search.addCriteria(Criteria.equals("processoJudicial", processo));
		} catch (NoSuchFieldException e) {
			String msg = String.format("Erro ao montar os critérios básicos de pesquisa: %s.", e.getLocalizedMessage());
			logger.error(msg);
			throw new IllegalArgumentException(msg, e);
		}
		processosExpediente = listExpedientes(search, Authenticator.getUsuarioLogado(), TipoSituacaoExpedienteEnum.PENDENTES_CIENCIA_RESPOSTA);
		return processosExpediente;
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer,BigInteger> contadorExpedientesPorJurisdicao(Integer idPessoa,Integer idProcuradoria){
		if(idProcuradoria == null){
			idProcuradoria = -1;
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select id_jurisdicao,count(0) from client.tb_proc_parte_expediente ppe ");
		sql.append("inner join client.tb_processo_trf ptf on ppe.id_processo_trf = ptf.id_processo_trf ");
		sql.append("where in_fechado = false ");
		sql.append("and ");
		if(idProcuradoria != -1){
			sql.append(" ppe.id_procuradoria = :idProcuradoria ");
		} else {
			sql.append("exists(select 1 from client.vs_destinatario_expediente parte ");
			sql.append("where parte.id_processo_parte_expediente = ppe.id_processo_parte_expediente and id_destinatario = :idPessoa) ");
		}
		
		sql.append("group by id_jurisdicao ");
		
		Query q = EntityUtil.getEntityManager().createNativeQuery(sql.toString());
		if(idProcuradoria != -1){
			q.setParameter("idProcuradoria", idProcuradoria);
		} else {
			q.setParameter("idPessoa", idPessoa);
		}
		List<Object[]> resultList = q.getResultList();
		Map<Integer,BigInteger> res= new HashMap<Integer,BigInteger>(0);
				for (Object[] borderTypes: resultList) {
					res.put((Integer)borderTypes[0], (BigInteger)borderTypes[1]);
				   }		
		return res;
	}
	
	public List<ProcessoParteExpediente> recuperaExpedientesAbertosPorProcessoParte(ProcessoParte processoParte) {
		return processoParteExpedienteDAO.recuperaExpedientesAbertosPorProcessoParte(processoParte);
	}
	
	/**
	 * Consulta todos os expedientes abertos dos meios de comunicação informados e de um determinado processo.
	 * 
	 * @param meiosComunicacao meios de comunicação que serão filtrados na consulta
	 * @param processoTrf processo que será recuperado os expedientes abertos
	 * @return coleção de expedientes.
	 * */
	public List<ProcessoParteExpediente> recuperaExpedientesAbertosPorMeiosComunicacaoAndProcessoTrf(List<ExpedicaoExpedienteEnum> meiosComunicacao, ProcessoTrf processoTrf) {
		return processoParteExpedienteDAO.recuperaExpedientesAbertosPorMeiosComunicacaoAndProcessoTrf(meiosComunicacao, processoTrf);
	}
	
	/**
	 * Método responsável por verificar se a {@link Pessoa} possui expediente aberto no {@link ProcessoTrf}.
	 * 
	 * @param processoTrf {@link ProcessoTrf}.
	 * @param pessoa {@link Pessoa}.
	 * @return Verdadeiro se a {@link Pessoa} possui expediente aberto no {@link ProcessoTrf}. Falso, caso contrário.
	 */
	public boolean verificarExpedienteAberto(ProcessoTrf processoTrf, Pessoa pessoa) {
		return this.processoParteExpedienteDAO.verificarExpedienteAberto(processoTrf, pessoa);
	}
	
	public Map<Integer,BigInteger> getContadoresPorJurisdicao(Pessoa representante, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente){
		return processoParteExpedienteDAO.getContadoresPorJurisdicao(representante.getIdPessoa(), idProcuradoria, atuacaoProcurador, tipoSituacaoExpediente);
	}

	public List<ProcessoParteExpediente> getExpedientesJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, Integer idProcuradoria, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Search search) throws PJeBusinessException {
		RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoRepresentante = this.getTipoAtuacaoRepresentante(idPessoa, idProcuradoria);
		List<ProcessoParteExpediente> listaRetorno = processoParteExpedienteDAO.getExpedientesJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria, tipoAtuacaoRepresentante, tipoSituacaoExpediente, search);
		getProcessoJudicial(listaRetorno);
		return listaRetorno;
	}

	/**
	 * Recupera o processo judicial de cada expediente para poder ordenar as lista as partes ativo e passivo.
	 * @param lista List<ProcessoParteExpediente> lista com os processoParteExpediente.
	 * @throws PJeBusinessException 
	 */
	public void getProcessoJudicial(List<ProcessoParteExpediente> lista) throws PJeBusinessException {
		if (CollectionUtils.isNotEmpty(lista)) {
			for (ProcessoParteExpediente ppe : lista) {
				ProcessoTrf processoJudicial = ppe.getProcessoJudicial();
				if (processoJudicial != null && processoJudicial.getIdProcessoTrf() > 0) {
					processoJudicial = (ProcessoTrf)processoJudicialManager.findById(processoJudicial.getIdProcessoTrf());
					processoJudicial.getProcessoParteList();
					ppe.setProcessoJudicial(processoJudicial);
				}
			}
		}
	}
	
	public Long getCountProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, Integer idProcuradoria, TipoSituacaoExpedienteEnum tipoSituacaoExpediente, Search search){
		RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoRepresentante = this.getTipoAtuacaoRepresentante(idPessoa, idProcuradoria);
		return processoParteExpedienteDAO.getCountProcessosJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria, tipoAtuacaoRepresentante, tipoSituacaoExpediente, search);
	}
	
	public List<ProcessoParteExpediente> findByIds(List<Integer> ids) throws NoSuchFieldException{
		if(ids == null || ids.size() == 0){
			return new ArrayList<ProcessoParteExpediente>(0);
		}
		Search s = new Search(ProcessoParteExpediente.class);
		s.addCriteria(Criteria.in("idProcessoParteExpediente",ids.toArray()));
		return list(s);
	}
	
	public List<ProcessoParteExpediente> recuperaExpedientesNaoFechados(ProcessoTrf processo){
		return getDAO().recuperaExpedientesNaoFechados(processo);
	}

	/**
	 * Recupera a lista de expedientes em que os atos de comunicação associados
	 * têm como destinatário o usuário indicado ou seus representados e não foram fechados
	 * 
	 * @param pessoa o usuário ou destinatário a respeito do qual será feita a pesquisa
	 * @param dataCriacao Data de criação do expediente que será usado como critério 'a partir de'.
	 * @param numeroProcesso Número do processo.
	 * @param idExpediente Id do expediente que será consultado.
	 * @param isFechado boolean que define se o expediente está fechado
	 * @return a lista de objetos definido no search que atedem aos critérios
	 * @throws PJeBusinessException
	 * 
	 */
	public List<ProcessoParteExpediente> expedientesPorDestinatario(Pessoa pessoa, Date dataCriacao, String numeroProcesso, Integer idExpediente, Boolean isFechado) throws PJeBusinessException {
		List<ProcessoParteExpediente> resultado = new ArrayList<ProcessoParteExpediente>();
		
		if (pessoa != null) {
			Search search = new Search(ProcessoParteExpediente.class);
			search.setDistinct(true);
			addCriteria(search, Criteria.equals("destinatarioList.idDestinatario", pessoa.getIdUsuario()));

			if(isFechado != null){
				addCriteria(search, Criteria.equals("fechado", isFechado));	
			}
			
			
			if(numeroProcesso != null) {
				addCriteria(search, Criteria.equals("processoJudicial.processo.numeroProcesso", mascaraNumeroProcesso(numeroProcesso) ));
			}
			if(idExpediente != null) {
				addCriteria(search, Criteria.equals("idProcessoParteExpediente", idExpediente));
			}
			if (dataCriacao != null) {
				addCriteria(search, Criteria.greaterOrEquals("processoExpediente.dtCriacao", dataCriacao));
			}

			limitarExpedientesProprios(search, pessoa);
			validarSearch(search, ProcessoParteExpediente.class);

			search.close();
			
			if(search.getMax() != null && search.getMax() == 0){
				return Collections.emptyList();
			}

			resultado = list(search);
		}
		return resultado;
	}
	
	public Long getCountExpedientes(PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getCountExpedientes(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisaGeral, searchLocal);
	}	

	/**
	 * Dado um critério de pesquisa, retorna uma lista de situações com os contadores relacionados
	 * @param criteriosPesquisa
	 * @return
	 */
	public List<SituacaoExpedienteVO> getCountSituacoesExpedientes(PesquisaExpedientesVO criteriosPesquisaGeral) throws PJeBusinessException{		
		List <SituacaoExpedienteVO> listaSituacoesExpedientes = new ArrayList<SituacaoExpedienteVO>();
		Long contadorExpedientes = 0L;
		
		for(TipoSituacaoExpedienteEnum situacao : TipoSituacaoExpedienteEnum.values()) {
			criteriosPesquisaGeral.setTipoSituacaoExpediente(situacao);
			contadorExpedientes = this.getCountExpedientes(criteriosPesquisaGeral, null);
			listaSituacoesExpedientes.add(new SituacaoExpedienteVO(situacao, contadorExpedientes));
		}
		
		return listaSituacoesExpedientes;
	}
	
	public Long getCountExpedientesJurisdicao(Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getCountExpedientesJurisdicao(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisaGeral, searchLocal);
	}
	
	public Long getCountExpedientesJurisdicaoCaixa(Integer idJurisdicao, Integer idCaixa, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getCountExpedientesJurisdicaoCaixa(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, idCaixa, criteriosPesquisaGeral, searchLocal);
	}
	
	/**
	 * Busca expedientes do usuário, passado um critério de pesquisa - sem restringir pela jurisdição ou pela caixa
	 * @param criteriosPesquisaGeral
	 * @param searchLocal
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<ProcessoParteExpediente> getExpedientes(PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getExpedientes(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisaGeral, searchLocal, this.mni);
	}
	
	public List<ProcessoParteExpediente> getExpedientesJurisdicao(Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getExpedientesJurisdicao(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisaGeral, searchLocal, this.mni);
	}

	public List<ProcessoParteExpediente> getExpedientesJurisdicaoCaixa(Integer idJurisdicao, Integer idCaixa, PesquisaExpedientesVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		return processoParteExpedienteDAO.getExpedientesJurisdicaoCaixa(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, idCaixa, criteriosPesquisaGeral, searchLocal);
	}
	
	public List<JurisdicaoVO> getJurisdicoesExpedientes(PesquisaExpedientesVO criteriosPesquisa) {
		return processoParteExpedienteDAO.getJurisdicoesExpedientes(idUsuarioAtual, idLocalizacaoAtual, tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisa);
	}	
	
	public List<CaixaAdvogadoProcuradorVO> getCaixasExpedientesJurisdicao(Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa) {
		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		return processoParteExpedienteDAO.getCaixasExpedientesJurisdicao(idUsuarioAtual, idLocalizacaoAtual, tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisa);
	}
	
    private RepresentanteProcessualTipoAtuacaoEnum getTipoAtuacaoRepresentante(Integer idPessoa, Integer idProcuradoria){
        PessoaProcuradoria  pessoaProcuradoria = ComponentUtil.getPessoaProcuradoriaManager().getPessoaProcuradoria(idPessoa, idProcuradoria);

        if (pessoaProcuradoria != null){
            return pessoaProcuradoria.getAtuacaoReal();
        }
        return null;
    }
    
    /**
	 * Retorna true se o meio de envio do expediente for igual a E = Sistema.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @return Boleano
	 */
	public boolean isMeioExpedicaoSistema(ProcessoParteExpediente parteExpediente) {
		return parteExpediente != null 
				&& parteExpediente.getProcessoExpediente() != null 
				&& ExpedicaoExpedienteEnum.E.equals(parteExpediente.getProcessoExpediente().getMeioExpedicaoExpediente());
	}

	/**
	 * Retorna true se existir expedientes abertos e enviados ao Domicílio Eletrônico do processo passado.
	 * 
	 * @param processo ProcessoTrf
	 * @return Booleano
	 */
	public boolean isExisteExpedienteAbertoEnviadoAoDomicilioEletronico(ProcessoTrf processo) {
		return getDAO().isExisteExpedienteAbertoEnviadoAoDomicilioEletronico(processo);
	}
	
	/**
	 * @return Lista de expedientes enviados ao Domicílio Eletrônico.
	 */
	public List<ProcessoParteExpediente> getAtosComunicacaoPendentesDomicilioEletronico(){
		return processoParteExpedienteDAO.getAtosComunicacaoPendentesDomicilioEletronico();
	}
	
	/**
	 * Retorna true se existir expediente parte.
	 * 
	 * @param parteExpediente ProcessoParteExpediente
	 * @return Booleano
	 */
	public boolean isExisteParteExpediente(ProcessoParteExpediente parteExpediente) throws PJeBusinessException{
		Boolean resultado = Boolean.FALSE;

		if (parteExpediente != null) {
			resultado = processoParteExpedienteDAO.isExisteParteExpediente(parteExpediente);
		}
		return resultado;
	}	

	/**
	 * @param ppe ProcessoParteExpediente
	 * @return True se o expediente for pessoal.
	 */
	public boolean isExpedientePessoal(ProcessoParteExpediente ppe) {
		return (ppe != null && BooleanUtils.toBoolean(ppe.getIntimacaoPessoal()));
	}

	public Boolean existeExpedienteAberto(ProcessoTrf processoTrf) {
		return getDAO().existeExpedienteAberto(processoTrf);
	}
}
