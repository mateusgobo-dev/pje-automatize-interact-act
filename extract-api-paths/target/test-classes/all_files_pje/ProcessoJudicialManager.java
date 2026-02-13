package br.jus.cnj.pje.nucleo.manager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.JurisdicaoDAO;
import br.jus.cnj.pje.business.dao.LembreteDAO;
import br.jus.cnj.pje.business.dao.ProcessoJudicialDAO;
import br.jus.cnj.pje.business.dao.ProcessoPautadoDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CabecalhoProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.EtiquetaProcesso;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.LembreteDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PagedQueryResult;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TagDTO;
import br.jus.cnj.pje.webservice.json.InformacaoUsuarioSessao;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.je.enums.TipoArtigo260Enum;
import br.jus.pje.nucleo.dto.AutoProcessualDTO;
import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.dto.PaginadorDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CompetenciaAreaDireito;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcessual;
import br.jus.pje.nucleo.entidades.TipoSituacaoProcessual;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.enums.ExigibilidadeAssinaturaEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.StatusAudienciaEnum;
import br.jus.pje.nucleo.enums.TipoNomePessoaEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.OABUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
import br.jus.pje.search.Translate;

@Name(ProcessoJudicialManager.NAME)
public class ProcessoJudicialManager extends BaseManager<ProcessoTrf>{

	public static final String NAME = "processoJudicialManager";
	private static final String SITUACAO_PARTE = "processoParteList.inSituacao";
	public static final String ID_USUARIO = "idUsuario";

	private Integer idUsuarioAtual;
	private Integer idLocalizacaoFisica;
	private TipoUsuarioExternoEnum tipoUsuarioExternoAtual;
	private Integer idProcuradoriaAtual;
	private boolean isProcuradorGestor;

	@In
	private ProcessoJudicialDAO processoJudicialDAO;
	
	@In
	private JurisdicaoDAO jurisdicaoDAO;

	@In
	private ParametroService parametroService;
	
	@In
	private ProcessoEventoManager processoEventoManager;

	@In
	private EventoManager eventoManager;

	@In
	private ProcuradorManager procuradorManager;
	
	@In
	private SituacaoProcessualManager situacaoProcessualManager;
	
	@In
	private PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager;
	
	@In
    private ProcessoPautadoDAO processoPautadoDAO;
	
	@In
	private FluxoService fluxoService;
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;
	
	@In
	private ProcessoTagManager processoTagManager;
		

	@Create
	public void inicializaDadosUsuario() {
		if (Contexts.isSessionContextActive()) {
			this.idUsuarioAtual = Authenticator.getIdUsuarioLogado();
			this.idLocalizacaoFisica = Authenticator.getIdLocalizacaoAtual();
			this.tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
			this.idProcuradoriaAtual = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
			this.isProcuradorGestor = Authenticator.isRepresentanteGestor();
		}
	}
	
	public String recuperarParteFormatada(
			ProcessoTrf processoJudicial, 
			boolean comTabela, 
			boolean mostrarOAB,
			boolean mostrarNumeroProcesso,
			ProcessoParteParticipacaoEnum... participacoes) {
		StringBuilder retorno = new StringBuilder("");
		if( processoJudicial != null ) {
			if (mostrarNumeroProcesso){
				retorno.append(processoJudicial.getClasseJudicialStr())
				.append(" <br/> ")
				.append(processoJudicial.getNumeroProcesso())
				.append(" <br/> ");
			}
			if(comTabela) {
				retorno.append("<table>");
			}
			for(ProcessoParteParticipacaoEnum participacao: participacoes){
				for(ProcessoParte polo: processoJudicial.getListaPartePoloObj(false, participacao)){
					if(polo.getPartePrincipal() && polo.getIsAtivo()){
						if(comTabela) {
							retorno = retorno.append("<tr><td colspan=2>" + polo.getPoloTipoParteStr() + "</td><td>:");
							retorno = retorno.append(polo.getNomeParte() + "</td></tr>");
						} else {
							retorno = retorno.append(polo.getPoloTipoParteStr() + ": ");
							retorno = retorno.append(polo.getNomeParte() + "<br/>");
						}
						for(ProcessoParteRepresentante representante: polo.getProcessoParteRepresentanteList()){
							if(representante.getParteRepresentante().getInSituacao().equals(ProcessoParteSituacaoEnum.A)) {
								if(comTabela) {
									retorno.append("<tr><td></td><td>" + representante.getParteRepresentante().getTipoParte().getTipoParte() + "</td><td>:" + representante.getRepresentante().getNome() );
								} else {
									retorno.append("<span style=\"padding-left:15px\">" + representante.getParteRepresentante().getTipoParte().getTipoParte() + ": " + representante.getRepresentante().getNome() + "</span>");
								}
								if(mostrarOAB && representante.getParteRepresentante().getTipoParte().equals(ParametroUtil.instance().getTipoParteAdvogado())) {
									PessoaAdvogado pessoaAdvogado = ((PessoaFisica) representante.getParteRepresentante().getPessoa()).getPessoaAdvogado();
									if(pessoaAdvogado != null) { 
										retorno.append(" - OAB/" + pessoaAdvogado.getOabFormatado());
									}
								}
								if(comTabela) {
									retorno.append( "</td></tr>");
								} else {
									retorno.append( "<br/>");
								}
							}
						}
					}
				}
			
			}
			if(comTabela) {
				retorno.append("</table>");
			}
		}
		return retorno.toString();
	}

	@Override
	protected ProcessoJudicialDAO getDAO(){
		return this.processoJudicialDAO;
	}
	
	public static ProcessoJudicialManager instance() {
		return (ProcessoJudicialManager)Component.getInstance(ProcessoJudicialManager.NAME);
	}

	public List<ProcessoTrf> findByNU(String nu) throws PJeBusinessException{
		String nuLimpa = this.stripNU(nu);
		if (!NumeroProcessoUtil.numeroProcessoValido(nuLimpa)){
			throw new PJeBusinessException("pje.processoJudicial.warn.numeroProcessoInvalido", null, nuLimpa, nuLimpa.length());
		}
		return this.processoJudicialDAO.findByNU(nuLimpa);
	}

	public List<ProcessoTrf> findByNumeracaoUnicaParcial(String numero) throws PJeBusinessException{
		Set<ProcessoTrf> ret = new HashSet<ProcessoTrf>();
		String nu = this.stripNU(numero);
		int comp = nu.length();
		if (comp >= 20){
			return this.findByNU(nu);
		}
		else if (comp < 20 && comp > 13){
			Integer origem = Integer.parseInt(nu.substring(comp - 4));
			Integer tribunal = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer segmento = Integer.parseInt(nu.substring(comp - 7, comp - 6));
			Integer ano = Integer.parseInt(nu.substring(comp - 11, comp - 7));
			Integer dv = Integer.parseInt(nu.substring(comp - 13, comp - 11));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 13));
			ret.addAll(this.processoJudicialDAO.findByNumeracaoUnica(nnn, dv, ano, segmento, tribunal, origem));
		}
		else if (comp <= 13 && comp >= 7){
			Integer ano = Integer.parseInt(nu.substring(comp - 4));
			Integer dv = Integer.parseInt(nu.substring(comp - 6, comp - 4));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 6));
			ret.addAll(this.processoJudicialDAO.findByNumeracaoUnica(nnn, dv, ano, null, null, null));
			if (comp < 10){
				dv = Integer.parseInt(nu.substring(comp - 2));
				nnn = Integer.parseInt(nu.substring(0, comp - 2));
				ret.addAll(this.processoJudicialDAO.findByNumeracaoUnica(nnn, dv, null, null, null, null));
			}
		}else if(comp >= 3 && comp < 7){
			Integer dv = Integer.parseInt(nu.substring(comp - 2));
			Integer nnn = Integer.parseInt(nu.substring(0, comp - 2));
			ret.addAll(this.processoJudicialDAO.findByNumeracaoUnica(nnn, dv, null, null, null, null));
		}else if (comp < 3){
			throw new PJeBusinessException("pje.processoJudicial.warning.numeroProcessoInsuficiente");
		}
		return new ArrayList<ProcessoTrf>(ret);
	}

	private String stripNU(String nu){
		return NumeroProcessoUtil.retiraMascaraNumeroProcesso(nu);
	}

	public ProcessoTrf findByProcessInstance(ProcessInstance processInstance) throws PJeBusinessException{
		Integer procId = (Integer) processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
		return this.findById(procId);
	}

	public List<ProcessoParteRepresentante> getAdvogados(ProcessoParte pp){
		List<ProcessoParteRepresentante> pprList = pp.getProcessoParteRepresentanteList();
		List<ProcessoParteRepresentante> ret = new ArrayList<ProcessoParteRepresentante>(pprList.size());
		int idTipoParteAdvgado = (new Integer(parametroService.valueOf("idTipoParteAdvogado"))).intValue();
		for (ProcessoParteRepresentante ppr : pprList){
			if (ppr.getTipoRepresentante().getIdTipoParte() == idTipoParteAdvgado){
				ret.add(ppr);
			}
		}
		return ret;
	}
	
	public List<ProcessoParte> getPartesSemAdvogado(ProcessoTrf processoTrf, ProcessoParteParticipacaoEnum processoParteParticipacaoEnum){
		
		int idTipoParteAdvgado = (new Integer(parametroService.valueOf("idTipoParteAdvogado"))).intValue();
		List<ProcessoParte> returnValue = processoTrf.getListaPartePoloObj(processoParteParticipacaoEnum);
		List<ProcessoParte> advogados = new ArrayList<ProcessoParte>(1);
		
		if(returnValue != null){
			for(ProcessoParte processoParte : returnValue){
				if(processoParte.getTipoParte().getIdTipoParte() == idTipoParteAdvgado){
					advogados.add(processoParte);
				}
			}
			returnValue.removeAll(advogados);
		}

		return returnValue;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParteRepresentante> getAdvogados(Pessoa p, ProcessoTrf processoTrf){

		int idTipoParteAdvgado = (new Integer(parametroService.valueOf("idTipoParteAdvogado"))).intValue();

		String hql = "select ppr from ProcessoTrf p " + " inner join p.processoParteList pp "
			+ " inner join ppl.processoParteRepresentanteList ppr "
			+ " where pp.pessoa.idUsuario = :idUsuario and "
			+ " ppr.tipoRepresentante.idTipoParte = :idTipoParte and " + " p.idProcessoTrf = :idProcessoTrf ";

		Query query = EntityUtil.getEntityManager().createQuery(hql);

		query.setParameter(ID_USUARIO, p.getIdUsuario());
		query.setParameter("idTipoParte", idTipoParteAdvgado);
		query.setParameter("idProcessoTrf", processoTrf.getIdProcessoTrf());

		return query.getResultList();
	}

	public ProcessoEvento obtemMovimentacao(String codigoNacional, ProcessoTrf processo) throws PJeDAOException{
		Evento tipoMov = eventoManager.findByCodigoCNJ(codigoNacional);
		ProcessoEvento ret = processoEventoManager.getMovimentacao(processo, tipoMov, null);
		return ret;
	}

	public String obtemDescricaoMovimento(ProcessoEvento evento, Object... beans){
		return processoEventoManager.processaMovimentacao(evento, beans);
	}

	public void insereMovimentacoes(ProcessoTrf processo, ProcessoEvento... movimentacoes)
			throws PJeBusinessException, PJeDAOException{
		for (ProcessoEvento mov : movimentacoes){
			mov.setProcesso(processo.getProcesso());
			processoEventoManager.persist(mov);
		}
	}

	/**
	 * Recupera a lista de identificadores dos fluxos de processo de negócio ativos vinculados
	 * a um processo judicial.
	 * 
	 * @param processoJudicial o processo cujos fluxos se pretende recuperar.
	 * @return a lista de fluxos ativos
	 */
	public List<Long> getBusinessProcessIds(ProcessoTrf processoJudicial){
		return getBusinessProcessIds(processoJudicial, null);
	}
	
	
	/**
	 * Recupera a lista de identificadores dos fluxos de processo de negócio ativos vinculados
	 * a um processo judicial.
	 * 
	 * @param processoJudicial o processo cujos fluxos se pretende recuperar.
	 * @return a lista de fluxos ativos
	 */
	public List<Long> getBusinessProcessIds(ProcessoTrf processoJudicial, List<Integer> idsLocalizacoes){
		return processoJudicialDAO.getBusinessProcessIds(processoJudicial, idsLocalizacoes);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> listById(List<Integer> ids) {
		String hql = "select p from ProcessoTrf p where p.idProcessoTrf in (:ids)";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("ids", Util.isEmpty(ids)?null:ids);
		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoTrf> listByIdComPartes(List<Integer> ids) {
		String hql = "select p from ProcessoTrf p join fetch p.processoParteList where p.idProcessoTrf in (:ids)";
		Query query = EntityUtil.getEntityManager().createQuery(hql);
		query.setParameter("ids", Util.isEmpty(ids)?null:ids);
		
		return query.getResultList();
	}

	/**
	 * Verifica se um determinado processo está vinculado a um assunto.
	 * 
	 * @param processo
	 * @param codigo
	 *            código do assunto processual
	 * @return true se houver o vínculo
	 * @category PJEII-3650
	 */
	public boolean possuiAssunto(ProcessoTrf processo, String codigo) {
		return processoJudicialDAO.possuiAssunto(processo, codigo);
	}
	
	/**
	 * Recupera da lista de assuntos de um processo, apenas aqueles não complementares
	 * @param processo
	 * @return
	 */
	public List<AssuntoTrf> recuperaAssuntosNaoComplementares(ProcessoTrf processo){
		List<AssuntoTrf> assuntoTrfList = new ArrayList<AssuntoTrf>();
		for (AssuntoTrf assunto : processo.getAssuntoTrfList()) {
			if (!assunto.getComplementar()) {
				assuntoTrfList.add(assunto);
			}
		}
		return assuntoTrfList;
	}
	
	/**
	 * Ajusta o orgao julgador do fluxo, pois com a alteracao do substituti eventual
	 * caso o processo seja redistribuido o 'ProcessoInstance' deve ser ajustado
	 * para apontar para o novo orgao julgador
	 * @param idProcessoTrf
	 * @category PJEII-7481
	 */
	public void ajustarFluxo(final Integer idProcessoTrf) {
		this.processoJudicialDAO.ajustarFluxo(idProcessoTrf);
	}

	public void ajustarFluxo(final Integer idProcessoTrf,Integer idLocalizacao, Integer idOrgaoJulgadorCargo, Integer idOrgaoJulgadorColegiado) {
		this.processoJudicialDAO.ajustarFluxo(idProcessoTrf,idLocalizacao,idOrgaoJulgadorCargo,idOrgaoJulgadorColegiado);
	}

	public Usuario getRelator(ProcessoTrf processo) {
		PessoaMagistrado relator = processoMagistradoManager.obterRelator(processo);
		return relator != null ? relator.getPessoa() : null;
	}
	
	private void limitarDistribuidos(Search search) throws PJeBusinessException{
		addCriteria(search, Criteria.equals("processoTrf.processoStatus", ProcessoStatusEnum.D));
	}

	private void limitarSigilosos(Search search, Identity identity, UsuarioLocalizacao localizacaoAtual) throws PJeBusinessException{
		Criteria visibilidadeAtribuida = null;
		Criteria visivelVisualizadorProcesso = null;
		Criteria visualizadorProcuradoriaDefensoria = null;
		Criteria naoSigiloso = Criteria.equals("processoTrf.segredoJustica", false);
		List<Integer> idsLocalizacoesFisicas = null;
		Criteria representanteDeParteVisualizadora = null;
		if(localizacaoAtual == null){
			addCriteria(search, naoSigiloso);
			return;
		}
		if(Authenticator.getUsuarioLogado() != null) {
			idsLocalizacoesFisicas = Authenticator.getIdsLocalizacoesFilhas();
		}
		List<Integer> idsUsuarios = new ArrayList<>(0);

		idsUsuarios = adicionarUsuarioETestarSeAssistenteAdvogado(idsUsuarios);
		visibilidadeAtribuida = Criteria.in("processoTrf.visualizadores.idPessoa", idsUsuarios.toArray());
		visibilidadeAtribuida.setRequired("processoTrf.visualizadores", false);

		if(Authenticator.isVisualizaSigiloso() && Authenticator.isUsuarioInterno()){
			List<Criteria> criteriasLocalizacao = new ArrayList<Criteria>();
			adicionarCriteriaSeServidorExclusivoColegiado(criteriasLocalizacao);

			adicionarCriteriaSeOJCNulo(criteriasLocalizacao);
			adicionarCriteriaSeOJCargoAtualNulo(criteriasLocalizacao,localizacaoAtual);
			criteriasLocalizacao.add(Criteria.lessOrEquals("processoTrf.nivelAcesso", Authenticator.recuperarNivelAcessoUsuarioLogado()));
			Criteria criteriaAndLocalizacao = Criteria.and(criteriasLocalizacao.toArray(new Criteria[criteriasLocalizacao.size()]));
			
			if (idsLocalizacoesFisicas != null) {
				Criteria criterioFluxoDeslocado = Criteria.exists(queryExisteFluxoDeslocadoParaLocalizacaoDoUsuario("o.processoTrf.", StringUtil.listToString(idsLocalizacoesFisicas), Authenticator.recuperarNivelAcessoUsuarioLogado()));
				Criteria criteriaAndLocalizacaoOrFluxoDeslocado = Criteria.or(criteriaAndLocalizacao, criterioFluxoDeslocado);
				addCriteria(search, Criteria.or(naoSigiloso, criteriaAndLocalizacaoOrFluxoDeslocado, visibilidadeAtribuida));
			} else {
				addCriteria(search, Criteria.or(naoSigiloso, criteriaAndLocalizacao, visibilidadeAtribuida));
			}
		
		}else {
			Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
			if(idProcuradoria != null) {
				visualizadorProcuradoriaDefensoria = Criteria.exists(queryVisualizadorProcuradoria("processoTrf.",idProcuradoria));	
				visibilidadeAtribuida = Criteria.or(naoSigiloso,visualizadorProcuradoriaDefensoria,visivelVisualizadorProcesso);			
			}
			else{
				visibilidadeAtribuida = Criteria.or(naoSigiloso, visivelVisualizadorProcesso, representanteDeParteVisualizadora);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> adicionarUsuarioETestarSeAssistenteAdvogado(List<Integer> idsUsuarios){
		if (Authenticator.isAssistenteAdvogado()) {
			StringBuilder hql = new StringBuilder();
			hql.append(" SELECT ul.usuario.idUsuario FROM PessoaAssistenteAdvogadoLocal o, UsuarioLocalizacao ul");
			hql.append(" WHERE o.usuario.idUsuario = :idUsuario AND ul.localizacaoFisica = o.localizacaoFisica");
			Query query = EntityUtil.createQuery(hql.toString());
			query.setParameter(ID_USUARIO, Authenticator.getIdUsuarioLogado());
			idsUsuarios = query.getResultList();
		} else {
			idsUsuarios.add(Authenticator.getIdUsuarioLogado());
		}
		return idsUsuarios;
	}
	
	private void adicionarCriteriaSeServidorExclusivoColegiado(List<Criteria> criteriasLocalizacao){
		if(!Authenticator.isServidorExclusivoColegiado()) {
			criteriasLocalizacao.add(Criteria.in("processoTrf.orgaoJulgador.localizacao.idLocalizacao", Authenticator.getIdsLocalizacoesFilhasAtuaisList().toArray()));
		}
	}
	
	private void adicionarCriteriaSeOJCNulo(List<Criteria> criteriasLocalizacao){
		if(Authenticator.getIdOrgaoJulgadorColegiadoAtual() != null) {
			criteriasLocalizacao.add(Criteria.equals("processoTrf.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", Authenticator.getIdOrgaoJulgadorColegiadoAtual()));
		}
	}
	
	private void adicionarCriteriaSeOJCargoAtualNulo(List<Criteria> criteriasLocalizacao, UsuarioLocalizacao localizacaoAtual){
		if(Authenticator.getIdOrgaoJulgadorCargoAtual() != null){
			List<Integer> idsCargos = new ArrayList<Integer>();
			UsuarioLocalizacaoMagistradoServidor li = localizacaoAtual.getUsuarioLocalizacaoMagistradoServidor();
	    	for(UsuarioLocalizacaoVisibilidade ul: ComponentUtil.getComponent(UsuarioLocalizacaoVisibilidadeManager.class).obterVisibilidadesAtivas(li) ) {
	    		if(ul.getOrgaoJulgadorCargo() == null) {
	    			idsCargos.clear();
	    			break;
	    		} else {
	    			idsCargos.add(ul.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
	    		}
	    	}
	    	if(!idsCargos.isEmpty()) {
	    		criteriasLocalizacao.add(Criteria.in("processoTrf.orgaoJulgadorCargo.idOrgaoJulgadorCargo", idsCargos.toArray()));
	    	}
		}
	}
	
	
	private void limitarProcessosUsuario(Search search) throws PJeBusinessException{

		Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
		Integer idUsuario = Authenticator.getIdUsuarioLogado();
		RepresentanteProcessualTipoAtuacaoEnum tipoAtuacaoProcurador = Authenticator.getTipoAtuacaoProcurador();
		List<Criteria> criterias = new ArrayList<Criteria>();

		criterias.add(Criteria.equals("inSituacao", ProcessoParteSituacaoEnum.A));

		if(idProcuradoria == null) {
			criterias.add(Criteria.equals("pessoa.idPessoa",idUsuario));
		}
		else{
			criterias.add(Criteria.notEquals("tipoParte", ParametroUtil.instance().getTipoParteAdvogado()));

			Criteria critProcuradoria = Criteria.and(
										Criteria.equals("procuradoria.idProcuradoria", idProcuradoria),
										Criteria.equals("procuradoria.pessoaProcuradoriaList.pessoa.idUsuario", idUsuario));

			criterias.add(critProcuradoria);
			if(tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.G)) {
				// chefe de procuradoria
				Criteria critChefeProcuradoria = Criteria.and(Criteria.equals("procuradoria.pessoaProcuradoriaList.chefeProcuradoria", true));
				criterias.add(critChefeProcuradoria);
			}
			else if(tipoAtuacaoProcurador.equals(RepresentanteProcessualTipoAtuacaoEnum.D)) {
				// distribuidor
				Criteria critDistribuidor = Criteria.equals("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList.ativo", true);
				critDistribuidor.setRequired("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList", false);			
				critDistribuidor = Criteria.and(
								   critDistribuidor,
								   Criteria.equals(Criteria.path("procuradoria.pessoaProcuradoriaList.pessoaProcuradoriaJurisdicaoList.jurisdicao.idJurisdicao"),Criteria.path("processoTrf.jurisdicao.idJurisdicao")));
				criterias.add(critDistribuidor);
			}
			else {
				// procurador padrão, processos precisam estar em sua caixa.
				Criteria critProcPadrao = Criteria.equals("processoTrf.caixasRepresentantes.caixaAdvogadoProcurador.caixaRepresentanteList.representante.idUsuario", Authenticator.getIdUsuarioLogado());			
				critProcPadrao.setRequired("processoTrf", true);
				critProcPadrao.setRequired("processoTrf.caixasRepresentantes", false);
				critProcPadrao.setRequired("processoTrf.caixasRepresentantes.caixaAdvogadoProcurador.caixaRepresentanteList", false);
				criterias.add(critProcPadrao);
			}
		}
				
		addCriteria(search, criterias.toArray(new Criteria[]{}));
	}
	
	public <T> List<T> listProcessos(Search search, Identity identity, UsuarioLocalizacao localizacaoAtual, boolean verificarSigilo) throws PJeBusinessException {
		return listProcessosUsuarios(search, identity, localizacaoAtual, false, verificarSigilo);
	}
	
	public <T> List<T> listProcessosUsuarios(Search search, Identity identity, UsuarioLocalizacao localizacaoAtual, boolean verificarSigilo) throws PJeBusinessException{
		return listProcessosUsuarios(search, identity, localizacaoAtual, true, verificarSigilo);
	}
	
	private <T> List<T> listProcessosUsuarios(Search search, Identity identity, UsuarioLocalizacao localizacaoAtual, boolean limitarProcessosProprios, boolean verificarSigilo) throws PJeBusinessException{
		limitarDistribuidos(search);
		if(verificarSigilo) {
			limitarSigilosos(search, identity, localizacaoAtual);
		}
		
		if(limitarProcessosProprios){
			limitarProcessosUsuario(search);
		}
		
		search.close();
		if(search.getMax() != null && search.getMax() == 0){
			return Collections.emptyList();
		}
		return list(search);
	}

	public List<ProcessoTrf> recuperarProcessosUsuariosPorNumeroProcesso(String numeroProcesso) {
		return this.recuperarProcessosUsuariosPorNumeroProcesso(numeroProcesso, false);
	}
	
	/**
	 * Retorna uma lista de processos, aplicando os filtros de processo distribuído e processo sigiloso, por número de processo. 
	 * 
	 * @param String numeroProcesso
	 * @return List<ProcessoTrf>
	 */
public List<ProcessoTrf> recuperarProcessosUsuariosPorNumeroProcesso(String numeroProcesso, boolean isProcessoParadigma){
		
		Identity identity = Identity.instance();
		
		return processoJudicialDAO.recuperarProcessosUsuarios(
				true, 
				Authenticator.isJusPostulandi(), 
				numeroProcesso, 
				Authenticator.getIdUsuarioLogado(), 
				Authenticator.getUsuarioLocalizacaoAtual(), 
				identity.hasRole("magistrado"), 
				identity.hasRole(Papeis.VISUALIZA_SIGILOSO), 
				Authenticator.getIdProcuradoriaAtualUsuarioLogado(),
				false,
				ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte(),
				Authenticator.getTipoAtuacaoProcurador(),
				isProcessoParadigma);
	}
	
	private boolean validarPermissaoUsuario(ProcessoTrf processoTrf) {
		List<ProcessoParte> processoPartes = null;
		Search search = new Search(ProcessoParte.class);
		try {
			if(processoTrf != null) {
				addCriteria(search, Criteria.equals("processoTrf.idProcessoTrf", processoTrf.getIdProcessoTrf()));			
			}
			processoPartes = listProcessosUsuarios(search, Identity.instance(), Authenticator.getUsuarioLocalizacaoAtual(), false, Boolean.TRUE);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}

		return CollectionUtilsPje.isNotEmpty(processoPartes);
	}
	
	/**
	 * Método responsável por sinalizar o processo como apto para julgamento
	 * utilizado como EL no fluxo.
	 * 
	 * @param idProcessoJudicial
	 *            id do processo que se deseja incluir para julgamento
	 * @param incluir
	 *            booleano indicativo para inclusão do processo selecionado em
	 *            julgamento ou pauta
	 * @throws PJeBusinessException
	 */
	public void aptidaoParaJulgamento(Integer idProcessoJudicial, boolean incluir) throws PJeBusinessException{
		aptidaoParaJulgamento(idProcessoJudicial, incluir, false);
	}
	
	/**
	 * Método responsável por sinalizar o processo como apto para julgamento
	 * utilizado como EL no fluxo
	 * 
	 * @param idProcessoJudicial
	 *            id do processo que se deseja incluir para julgamento
	 * @param incluir
	 *            booleano indicativo para inclusão do processo selecionado em
	 *            julgamento ou pauta
	 * @param pautaVirtual
	 *            booleano indicativo para inclusão do processo em pauta virtual
	 * @throws PJeBusinessException
	 */
	public void aptidaoParaJulgamento(Integer idProcessoJudicial, boolean incluir, Boolean pautaVirtual) throws PJeBusinessException{
		ProcessoTrf processo = findById(idProcessoJudicial);		
		Boolean exigePauta = processo.getClasseJudicial().getPauta();
		if(exigePauta == null || !exigePauta){
			processo.setSelecionadoJulgamento(incluir);
		}else{
			processo.setSelecionadoPauta(incluir);
		}
		processo.setPautaVirtual(pautaVirtual);
		processo.setDtSolicitacaoInclusaoPauta(new Date());
		persistAndFlush(processo);			
	}
	
	/**
	 * Método responsável por remover a aptidão de pauta para o processo
	 * 
	 * @param idProcesso
	 *            id do processo que se deseja incluir para julgamento
	 * @throws PJeBusinessException
	 */
	public void removerAptidaoParaJulgamento(Integer idProcesso) throws PJeBusinessException{
		if(idProcesso == null || idProcesso.intValue() == 0) {
			throw new PJeBusinessException("Identificador do processo não informado");
		}
		ProcessoTrf processo = findById(idProcesso);
		processo.setSelecionadoJulgamento(false);
		processo.setSelecionadoPauta(false);
		processo.setPautaVirtual(false);
		persistAndFlush(processo);
	}

	public ProcessoTrf recuperarProcesso(Integer idProcesso, Identity identity, Pessoa pessoa, UsuarioLocalizacao localizacaoAtual) throws PJeBusinessException{
		return this.recuperarProcesso(idProcesso, identity, pessoa, localizacaoAtual, true, null);
	}
	
	public ProcessoTrf recuperarProcesso(Integer idProcesso, Identity identity, Pessoa pessoa, UsuarioLocalizacao localizacaoAtual, Boolean limitarDistribuidos, String numeroProcesso) throws PJeBusinessException{

		if ((idProcesso == null || idProcesso.intValue() == 0) && (numeroProcesso == null || numeroProcesso.isEmpty())) {
			throw new PJeBusinessException("Identificador do processo não informado");
		}
		
		Search search = new Search(ProcessoParte.class);
		search.setRetrieveField("processoTrf");
		
		if(idProcesso != null) {
			addCriteria(search, Criteria.equals("processoTrf.idProcessoTrf", idProcesso));			
		}
		
		if(numeroProcesso != null && !numeroProcesso.isEmpty()) {
			addCriteria(search, Criteria.equals("processoTrf.processo.numeroProcesso", numeroProcesso));
		}
		
		if(limitarDistribuidos){
			limitarDistribuidos(search);
		}
		ProcessoTrf proc = null;
        if(idProcesso != null) {
            proc = findById(idProcesso);
        }
        if(proc != null && ProcessoStatusEnum.D.equals(proc.getProcessoStatus()) || !StringUtil.isEmpty(numeroProcesso)) {
        	limitarSigilosos(search, identity, localizacaoAtual);
        }
		search.setMax(1);
		List<ProcessoTrf> ret = list(search);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public List<Integer> recuperarProcessosPessoaLimitarSigilosos(Identity identity, Pessoa pessoa, UsuarioLocalizacao localizacaoAtual) throws PJeBusinessException{
		Search search = new Search(ProcessoParte.class);
		search.setRetrieveField("processoTrf.idProcessoTrf");
		limitarDistribuidos(search);
		limitarSigilosos(search, identity, localizacaoAtual);
		if(localizacaoAtual != null && localizacaoAtual.getUsuarioLocalizacaoMagistradoServidor() == null){
			limitarProcessosUsuario(search);
		}
		return list(search);
	}
	
	/**
	 * PJEII-2128
	 * Método responsável por recuperar a próxima audiência designada ativa e não cancelada
	 * 
	 * @return A próxima audiência designada
	 * @author Leonardo Inácio
	 * @author thiago.vieira
	 */
	public ProcessoAudiencia getProximaAudienciaDesignada(ProcessoTrf processo){
		Search s = new Search(ProcessoAudiencia.class);
		addCriteria(s, Criteria.equals("processoTrf", processo),
					   Criteria.equals("statusAudiencia", StatusAudienciaEnum.M),
					   Criteria.equals("inAtivo", true),
					   Criteria.greaterOrEquals("dtInicio", DateUtil.getDataAtual()),
					   Criteria.isNull("dtCancelamento"));
		s.addOrder("dtInicio", Order.ASC);
		s.setMax(1);
		List<ProcessoAudiencia> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	/**
	 * Método responsável por recuperar a audiência designada ativa e não cancelada mais antiga.
	 * 
	 * @return Audiência designada mais antiga.
	 */
	public ProcessoAudiencia getAudienciaDesignadaMaisAntiga(ProcessoTrf processo){
		Search s = new Search(ProcessoAudiencia.class);
		addCriteria(s, Criteria.equals("processoTrf", processo),
					   Criteria.equals("statusAudiencia", StatusAudienciaEnum.M),
					   Criteria.equals("inAtivo", true),
					   Criteria.isNull("dtCancelamento"));
		s.addOrder("dtInicio", Order.ASC);
		s.setMax(1);
		List<ProcessoAudiencia> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	/**
	 * Método responsável por recuperar a última movimentação do processo especificado
	 * [PJEII-3616]
	 * @param processo
	 * @return Última movimentação do processo especificado
	 * @throws PJeDAOException
	 */
	public ProcessoEvento recuperarUltimoMovimento(ProcessoTrf processo) {
		return processoEventoManager.recuperaUltimaMovimentacao(processo);
	}
	
	/**
	 * Método responsável por recuperar a última movimentação do processo especificado
	 * utilizando as regras de visualização dos autos. 
	 * @param processo
	 * @return
	 */
	public ProcessoEvento recuperarUltimoMovimentoVisivel(ProcessoTrf processo) {
		Search search = new Search(AutoProcessualDTO.class);
		search.setMax(1);
		search.addOrder("documento.dataJuntada", Order.DESC);
		List<AutoProcessualDTO> autos = ComponentUtil.getComponent(ListProcessoCompletoBetaManager.class)
				.recuperarAutos(processo.getIdProcessoTrf(), false, true, search);
		if (!autos.isEmpty()) {
			return autos.get(0).getMovimento();
		}
		return null;
	}
	
	/**
	 * Consulta os processos quando os filtros estão desabilitados, os critérios 
	 * de acesso serão buscados do usuário logado e acrescentados à consulta.
	 * 
	 * @param numeroProcesso Número do processo.
	 * @return coleção de processos.
	 */
	public List<ProcessoTrf> findByNUComFiltroDesabilitado(String numeroProcesso) {
		try {
			Search search = getSearchConsultarProcessos(numeroProcesso, null, null, null, null, 
					null, null, null, null, null, null, null, null, null, null, null, null, null, 
					null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null);
			
			return list(search);
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			return null;
		}
	}

	/**
     * Recupera situações ainda não encerradas e válidas no processo dado.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @return as situações
     */
	public List<SituacaoProcessual> recuperaSituacoesAtuais(ProcessoTrf processo){
		return situacaoProcessualManager.recuperaSituacoesAtuais(processo);
	}
	
    /**
     * Recupera todas as situações válidas de um dado processo, independentemente do período de vigência dessas situações.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @return as situações do processo
     */
	public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo){
		return situacaoProcessualManager.recuperaSituacoes(processo);
	}
	
    /**
     * Recupera situações válidas e ativas do processo dado cuja data inicial é anterior à data de referência e cuja data final é nula ou posterior à data de referência.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @param dataReferencia a data de referência
     * @return as situações
     */
	public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo, Date dataReferencia){
		return situacaoProcessualManager.recuperaSituacoes(processo, dataReferencia);
	}
	
    /**
     * Recupera as situações do processo dado independentemente do período de vigência da situação, incluindo as inválidas a depender da chave de consulta limitarValidas.
     * 
     * @param processo o processo em relação ao qual se pretende recuperar as situações
     * @param limitarValidas marca indicativa de que se pretende recuperar apenas as situções válidas
     * @return as situações
     */
	public List<SituacaoProcessual> recuperaSituacoes(ProcessoTrf processo, boolean somenteValidas){
		return situacaoProcessualManager.recuperaSituacoes(processo, somenteValidas);
	}

	/**
	 * Indica se o processo informado tem uma situação ativa e válida com o código de tipo dado na data
	 * de referência.
	 *  
	 * @param processo o processo a ser verificado
	 * @param codigoSituacao o código do tipo de situação cuja existência se pretende verificar
	 * @param dataReferencia a data de referência
	 * @return true, se o processo tiver a situação com o tipo encontrado na data de referência
	 */
	public boolean temSituacao(ProcessoTrf processo, String codigoSituacao, Date dataReferencia){
    	Search s = new Search(SituacaoProcessual.class);
    	addCriteria(s, 
    			Criteria.equals("processo", processo),
    			Criteria.equals("tipoSituacaoProcessual.codigo", codigoSituacao),
    			Criteria.equals("valida", true));
    	if(dataReferencia != null){
    		addCriteria(s, 
        			Criteria.or(
        					Criteria.isNull("dataInicial"),
        					Criteria.lessOrEquals("dataInicial", dataReferencia)),
        			Criteria.or(
        					Criteria.isNull("dataFinal"),
        					Criteria.greaterOrEquals("dataFinal", dataReferencia)));
    	}
    	return count(s) > 0 ? true : false;
    }
    
    /**
	 * Indica se o processo informado tem uma situação ativa e válida com o código de tipo dado no momento
	 * da consulta.
	 *  
	 * @param processo o processo a ser verificado
	 * @param codigoSituacao o código do tipo de situação cuja existência se pretende verificar
	 * @return true, se o processo tiver ou tiver tido a situação com o tipo encontrado
     */
    public boolean temSituacao(ProcessoTrf processo, String codigoSituacao){
    	return temSituacao(processo, codigoSituacao, new Date());
    }
    
    /**
     * Recupera o número de processos que têm ou tinham a situação ativa e válida e do 
     * tipo dado na data de referência.
     * 
     * @param tipoSituacao o tipo de situação a se pesquisar
     * @param dataReferencia a data de referência, ou nulo para recuperar também o número de processos que 
     * já tiveram a situação dada 
     * @return o número de processos
     * 
     * @throws IllegalArgumentException caso o tipo de situação esteja nulo.
     */
    public long contagemProcessos(TipoSituacaoProcessual tipoSituacao, Date dataReferencia){
    	if(tipoSituacao == null){
    		throw new IllegalArgumentException("O tipo de situação não pode ser nulo");
    	}
    	return  contagemProcessos(tipoSituacao.getCodigo(), dataReferencia);
    }
    
    /**
     * Recupera o número de processos que têm situação ativa e válida e do tipo dado no momento da chamada.
     * 
     * @param tipoSituacao o tipo de situação a se pesquisar
     * @return o número de processos
     */
    public long contagemProcessos(TipoSituacaoProcessual tipoSituacao){
    	return contagemProcessos(tipoSituacao, new Date());
    }
    
    /**
     * Recupera o número de processos que têm ou tinham a situação válida e do 
     * tipo com o código dado na data de referência.
     * 
     * @param codigoSituacao o código do tipo de situação a se pesquisar
     * @param dataReferencia a data de referência, ou nulo para recuperar também o número de processos que 
     * já tiveram a situação dada 
     * @return o número de processos
     * 
     * @throws IllegalArgumentException caso o tipo de situação esteja nulo.
     */
    public long contagemProcessos(String codigoSituacao, Date dataReferencia){
    	Search s = new Search(SituacaoProcessual.class);
    	addCriteria(s, 
    			Criteria.equals("tipoSituacaoProcessual.codigo", codigoSituacao),
    			Criteria.equals("valida", true));
    	if(dataReferencia != null){
    		addCriteria(s, 
        			Criteria.or(
        					Criteria.isNull("dataInicial"),
        					Criteria.lessOrEquals("dataInicial", dataReferencia)),
        			Criteria.or(
        					Criteria.isNull("dataFinal"),
        					Criteria.greaterOrEquals("dataFinal", dataReferencia)));
    	}
    	s.setRetrieveField("processo");
    	return count(s);
    }
    
    /**
     * Recupera o número de processos que têm situação válida e do tipo com o código dado no momento da chamada.
     * 
     * @param codigoSituacao o tipo com o código de situação a se pesquisar
     * @return o número de processos
     */
    public long contagemProcessos(String codigoSituacao){
    	return contagemProcessos(codigoSituacao, new Date());
    }

    /**
     * Recupera a lista de processos que têm situação válida do tipo dado na data de referência.
     *  
     * @param tipoSituacao o tipo de situação a ser pesquisado
     * @param dataReferencia a data de referência, ou null se a data for irrelevante
     * @return a lista de processos
     * 
     * @throws IllegalArgumentException caso o tipo de situação dado esteja nulo.
     */
    public List<ProcessoTrf> recuperaProcessos(TipoSituacaoProcessual tipoSituacao, Date dataReferencia){
    	if(tipoSituacao == null){
    		throw new IllegalArgumentException("O tipo de situação processual não pode ser nulo");
    	}
    	return recuperaProcessos(tipoSituacao.getCodigo(), dataReferencia);
    }
    
    /**
     * Recupera a lista de processos que têm situação válida do tipo dado.
     *  
     * @param tipoSituacao o tipo de situação a ser pesquisado
     * @return a lista de processos
     * 
     * @throws IllegalArgumentException caso o tipo de situação dado esteja nulo.
     */
    public List<ProcessoTrf> recuperaProcessos(TipoSituacaoProcessual tipoSituacao){
    	return recuperaProcessos(tipoSituacao, new Date());
    }
    
    /**
     * Recupera a lista de processos que têm situação válida do tipo de código dado na data de referência.
     *  
     * @param codigoSituacao o código do tipo de situação a ser pesquisado
     * @param dataReferencia a data de referência, ou null se a data for irrelevante
     * @return a lista de processos
     */
    public List<ProcessoTrf> recuperaProcessos(String codigoSituacao, Date dataReferencia){
    	Search s = new Search(SituacaoProcessual.class);
    	addCriteria(s, 
    			Criteria.equals("tipoSituacaoProcessual.codigo", codigoSituacao),
    			Criteria.equals("valida", true));
    	if(dataReferencia != null){
    		addCriteria(s, 
        			Criteria.or(
        					Criteria.isNull("dataInicial"),
        					Criteria.lessOrEquals("dataInicial", dataReferencia)),
        			Criteria.or(
        					Criteria.isNull("dataFinal"),
        					Criteria.greaterOrEquals("dataFinal", dataReferencia)));
    	}
    	s.setRetrieveField("processo");
    	return list(s);
    }
    
    /**
     * Recupera a lista de processos que têm ou tiveram situação ativa e válida do tipo de código dado.
     *  
     * @param codigoSituacao o código do tipo de situação a ser pesquisado
     * @return a lista de processos
     */
    public List<ProcessoTrf> recuperaProcessos(String codigoSituacao){
    	return recuperaProcessos(codigoSituacao, null);
    }
    
    public boolean temSituacaoIncompativel(ProcessoTrf processo, TipoSituacaoProcessual tipoSituacao){
    	if(tipoSituacao.getTiposSituacoesIncompatives().isEmpty()){
    		return false;
    	}
    	Search s = new Search(SituacaoProcessual.class);
    	addCriteria(s, 
    			Criteria.equals("processo", processo),
    			Criteria.equals("ativo", true),
    			Criteria.in("tipoSituacaoProcessual", 
    					tipoSituacao.getTiposSituacoesIncompatives().toArray(new TipoSituacaoProcessual[0])));
    	return count(s) > 0 ? true : false;
    }

	public List<SituacaoProcessual> recuperaSituacao(ProcessoTrf processo, String codigoTipoSituacao) {
		Search s= new Search(SituacaoProcessual.class);
		addCriteria(s, 
				Criteria.equals("processo", processo),
				Criteria.equals("ativo", true),
				Criteria.equals("tipoSituacaoProcessual.codigo", codigoTipoSituacao));
		return list(s);
	}
    
	public List<Pessoa> getMagistradosAtuantes(ProcessoTrf processo){
		return getDAO().getMagistradosAtuantes(processo);
	}
	
	public Map<Integer,BigInteger> getContadoresPorJurisdicao(Integer idPessoa,Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador){
		return processoJudicialDAO.getContadoresPorJurisdicao(idPessoa, idProcuradoria, atuacaoProcurador);
	}
	

	/**
	* Método responsável por retornar uma lista de Órgãos Julgadores filtrados por OrgaoJulgadorColegiado e Jurisdicao.
	* 
	* @param  ojc		Objeto da classe OrgaoJulgadorColegiado. Caso seja nulo, é ignorado.
	* @param  jurisdicao	Objeto da classe Jurisdicao. Caso seja nulo, é ignorado.
	* @return Lista de OrgaoJulgador filtrados conforme os valores informados nos parâmetros.
	*/
	public List<OrgaoJulgador> getOrgaoJulgadorListPorOjcJurisdicao(
			OrgaoJulgadorColegiado ojc, Jurisdicao jurisdicao) {
		return processoJudicialDAO.getOrgaoJulgadorListPorOjcJurisdicao(ojc, jurisdicao);
	}
		
	public List<ProcessoTrf> getProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, Integer idLocalizacao, Search search) {
		return processoJudicialDAO.getProcessosJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria, atuacaoProcurador, idLocalizacao, search);
	}

	public Long getCountProcessosJurisdicao(Integer idJurisdicao, Integer idCaixa, Integer idPessoa, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, Integer idLocalizacao, Search search) {
		return processoJudicialDAO.getCountProcessosJurisdicao(idJurisdicao, idCaixa, idPessoa, idProcuradoria, atuacaoProcurador, idLocalizacao, search);
	}
	
	public String queryVisualizadorProcuradoria (String prefix,Integer idProcuradoria){
		return getDAO().queryVisualizadorProcuradoria(prefix,idProcuradoria);
	}
	
	public String queryRepresentanteDeVisualizador(String prefix) {
		return getDAO().queryRepresentanteDeVisualizador(prefix);
	}
	
	/**
	 * Recupera o {@link ProcessoTrf} que esteja pautado na sessão
	 * Caso a {@link Pessoa} esteja em um {@link OrgaoJulgador} pertencente
	 * ao {@link OrgaoJulgadorColegiado} do {@link ProcessoTrf}
	 * @param idSessao
	 * @param idProcessoTrf
	 * @param p
	 * @return
	 */
	public ProcessoTrf recuperaProcessoPautaSessaoPessoa(Integer idSessao, ProcessoTrf processoTrf){
		Search s = new Search(SessaoPautaProcessoTrf.class);
		
		s.setRetrieveField("processoTrf");
		s.setMax(1);
		
		try {
			s.addCriteria(Criteria.and(
								Criteria.equals("sessao.idSessao", idSessao),
								Criteria.equals("processoTrf", processoTrf),
								Criteria.equals("sessao.orgaoJulgadorColegiado", processoTrf.getOrgaoJulgadorColegiado()),
								Criteria.equals("sessao.orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual())
								));
			
			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		List<ProcessoTrf> lista = list(s);
		
		if(lista != null && !lista.isEmpty())
			return lista.get(0);
		else 
			return null;
 	}	
		
	public List<ProcessoTrf> findByIds(List<Integer> ids) throws NoSuchFieldException{
		if(ids == null || ids.size() == 0){
			return new ArrayList<ProcessoTrf>(0);
		}
		Search s = new Search(ProcessoTrf.class);
		s.addCriteria(Criteria.in("idProcessoTrf",ids.toArray()));
		return list(s);
	}

	/**
	 * Retorna o Search para a consulta de processos filtrando os parâmetros informados.
	 * 
	 * @param numeroProcessoFormatado
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param ramoJustica
	 * @param respectivoTribunal
	 * @param nomeParte
	 * @param documentoParte
	 * @param estadoOAB
	 * @param numeroOAB
	 * @param letraOAB
	 * @param assunto
	 * @param classe
	 * @param complementoClasse
	 * @param dsComponenteValidacao
	 * @param dsComponenteValidacaoData
	 * @param orgaoJulgador
	 * @param orgaoColegiado
	 * @param dataAutuacaoInicio
	 * @param dataAutuacaoFim
	 * @param eleicao
	 * @param estado
	 * @param municipio
	 * @param numeroDocumentoProcesso
	 * @param valorCausaInicial
	 * @param valorCausaFinal
	 * @param numeroProcessoReferencia
	 * @param objetoProcesso
	 * @param jurisdicao
	 * @param validacaoPeloMenosUmCampoInformado
	 * @return Search para a consulta de processos.
	 */
	public Search getSearchConsultarProcessos(String numeroProcessoFormatado,
			Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal, 
			String nomeParte, 
			String documentoParte, 
			Estado estadoOAB, 
			String numeroOAB,
			String letraOAB,
			String assunto, 
			String classe, 
			ComplementoClasse complementoClasse, 
			String dsComponenteValidacao, 
			String dsComponenteValidacaoData, 
			OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoColegiado, 
			Date dataAutuacaoInicio, 
			Date dataAutuacaoFim,
			Eleicao eleicao,
			Estado estado,
			Municipio municipio,
			String numeroDocumentoProcesso,
			Double valorCausaInicial, 
			Double valorCausaFinal,
			String numeroProcessoReferencia,
			String objetoProcesso,
			Jurisdicao jurisdicao,
			Evento movimentacaoProcessual,
			Boolean validacaoPeloMenosUmCampoInformado,
			String parteNumeroProcesso,
			List<Integer> idsProcessosCriminais,
			String outrosNomesAlcunha) throws PJeBusinessException {
		
		Search search = new Search(ProcessoTrf.class);
		try {
			search.setDistinct(true);
			search.addCriteria(getCriteriosConsultarProcessos(
				numeroProcessoFormatado, numeroSequencia, digitoVerificador, ano, numeroOrigem, 
				ramoJustica, respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB,
				letraOAB, assunto, classe, complementoClasse, dsComponenteValidacao,
				dsComponenteValidacaoData, orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
				dataAutuacaoFim, eleicao, estado, municipio, numeroDocumentoProcesso, 
				valorCausaInicial, valorCausaFinal, numeroProcessoReferencia, objetoProcesso,
				jurisdicao, movimentacaoProcessual, validacaoPeloMenosUmCampoInformado,parteNumeroProcesso, idsProcessosCriminais,
				outrosNomesAlcunha));

		} catch (NoSuchFieldException e) {
			throw new PJeRuntimeException("Erro ao efetuar a consulta do processo.", e);
		}
		
		return search;
	}
	
	/**
	 * Retorna a lista de critérios com base nos parâmetros informados e os acessos do usuário 
	 * logado.
	 * 
	 * @param numeroProcessoFormatado
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param ramoJustica
	 * @param respectivoTribunal
	 * @param nomeParte
	 * @param documentoParte
	 * @param estadoOAB
	 * @param numeroOAB
	 * @param letraOAB
	 * @param assunto
	 * @param classe
	 * @param complementoClasse
	 * @param dsComponenteValidacao
	 * @param dsComponenteValidacaoData
	 * @param orgaoJulgador
	 * @param orgaoColegiado
	 * @param dataAutuacaoInicio
	 * @param dataAutuacaoFim
	 * @param eleicao
	 * @param estado
	 * @param municipio
	 * @param numeroDocumentoProcesso
	 * @param valorCausaInicial
	 * @param valorCausaFinal
	 * @param numeroProcessoReferencia
	 * @param objetoProcesso
	 * @param jurisdicao
	 * @param movimentacaoProcessual
	 * @param validacaoPeloMenosUmCampoInformado
	 * @return Lista de critérios para a consulta de processos.
	 */
	public List<Criteria> getCriteriosConsultarProcessos(String numeroProcessoFormatado,
			Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal, 
			String nomeParte, 
			String documentoParte, 
			Estado estadoOAB, 
			String numeroOAB,
			String letraOAB,
			String assunto, 
			String classe, 
			ComplementoClasse complementoClasse, 
			String dsComponenteValidacao, 
			String dsComponenteValidacaoData, 
			OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoColegiado, 
			Date dataAutuacaoInicio, 
			Date dataAutuacaoFim,
			Eleicao eleicao,
			Estado estado,
			Municipio municipio,
			String numeroDocumentoProcesso,
			Double valorCausaInicial, 
			Double valorCausaFinal,
			String numeroProcessoReferencia,
			String objetoProcesso,
			Jurisdicao jurisdicao,
			Evento movimentacaoProcessual,
			Boolean validacaoPeloMenosUmCampoInformado,
			String parteNumeroProcesso,
			List<Integer> idsProcessosCriminais,
			String outrosNomesAlcunha)  throws PJeBusinessException {
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosPesquisaConsultarProcessos(
				numeroProcessoFormatado, numeroSequencia, digitoVerificador, ano, numeroOrigem, 
				ramoJustica, respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB,
				letraOAB, assunto, classe, complementoClasse, dsComponenteValidacao,
				dsComponenteValidacaoData, orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
				dataAutuacaoFim, eleicao, estado, municipio, numeroDocumentoProcesso, 
				valorCausaInicial, valorCausaFinal, numeroProcessoReferencia, objetoProcesso,
				jurisdicao, movimentacaoProcessual, validacaoPeloMenosUmCampoInformado,parteNumeroProcesso, idsProcessosCriminais,
				outrosNomesAlcunha));
		
		validarPreenchimentoPesquisa(validacaoPeloMenosUmCampoInformado, criterios);
		
		criterios.addAll(getCriteriosNegociaisConsultarProcessos(nomeParte,documentoParte));
		
		return criterios;
	}
	
	/**
	 * Retorna os critérios de pesquisa para a consulta de processos.
	 * 
	 * @param numeroProcessoFormatado
	 * @param numeroSequencia
	 * @param digitoVerificador
	 * @param ano
	 * @param numeroOrigem
	 * @param ramoJustica
	 * @param respectivoTribunal
	 * @param nomeParte
	 * @param documentoParte
	 * @param estadoOAB
	 * @param numeroOAB
	 * @param letraOAB
	 * @param assunto
	 * @param classe
	 * @param complementoClasse
	 * @param dsComponenteValidacao
	 * @param dsComponenteValidacaoData
	 * @param orgaoJulgador
	 * @param orgaoColegiado
	 * @param dataAutuacaoInicio
	 * @param dataAutuacaoFim
	 * @param eleicao
	 * @param estado
	 * @param municipio
	 * @param numeroDocumentoProcesso
	 * @param valorCausaInicial
	 * @param valorCausaFinal
	 * @param numeroProcessoReferencia
	 * @param objetoProcesso
	 * @param jurisdicao
	 * @param movimentacaoProcessual
	 * @param validacaoPeloMenosUmCampoInformado
	 * @return Lista de critérios para consulta de processos.
	 */
	private List<Criteria> getCriteriosPesquisaConsultarProcessos(
			String numeroProcessoFormatado,
			Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal, 
			String nomeParte, 
			String documentoParte, 
			Estado estadoOAB, 
			String numeroOAB,
			String letraOAB,
			String assunto, 
			String classe, 
			ComplementoClasse complementoClasse, 
			String dsComponenteValidacao, 
			String dsComponenteValidacaoData, 
			OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoColegiado, 
			Date dataAutuacaoInicio, 
			Date dataAutuacaoFim,
			Eleicao eleicao,
			Estado estado,
			Municipio municipio,
			String numeroDocumentoProcesso,
			Double valorCausaInicial, 
			Double valorCausaFinal,
			String numeroProcessoReferencia,
			String objetoProcesso,
			Jurisdicao jurisdicao,
			Evento movimentacaoProcessual,
			Boolean validacaoPeloMenosUmCampoInformado,
			String parteNumeroProcesso,
			List<Integer> idsProcessosCriminais,
			String outrosNomesAlcunha)  throws PJeBusinessException {
		
		List<Criteria> criterios = new ArrayList<>(0);

		validarFiltrosNomeParteDocumentoParteOutrosNomesAlcunha(nomeParte, documentoParte, outrosNomesAlcunha, criterios);

		validarFiltroNumeroProcesso(numeroProcessoFormatado, numeroSequencia, digitoVerificador, ano, numeroOrigem, parteNumeroProcesso, criterios);

		validarFiltroRamoJusticaRespectivoTribunal(ramoJustica, respectivoTribunal, criterios);

		validarFiltroOrgaoJulgador(orgaoJulgador, criterios);

		validarFiltroOrgaoColegiado(orgaoColegiado, criterios);

		validarFiltroAssunto(assunto, criterios);

		validarFiltroClasse(classe, criterios);

		validarFiltroComplementoClasse(complementoClasse, dsComponenteValidacao, dsComponenteValidacaoData, criterios);

		validarFiltrosOAB(estadoOAB, numeroOAB, letraOAB, criterios);

		validarFiltrosDataAutuacao(dataAutuacaoInicio, dataAutuacaoFim, criterios);

		validarFiltroEleicao(eleicao, criterios);

		validarFiltroEstado(estado, criterios);

		validarFiltroMunicipio(municipio, criterios);

		validarFiltroNumeroDocumentoProcesso(numeroDocumentoProcesso, criterios);

		validarFiltroValorCausa(valorCausaInicial, valorCausaFinal, criterios);

		validarFiltroObjetoProcesso(objetoProcesso, criterios);

		validarFiltroNumeroProcessoReferencia(numeroProcessoReferencia, criterios);

		validarFiltroJurisdicao(jurisdicao, criterios);

		validarFiltroMovimentacaoProcessual(movimentacaoProcessual, criterios);

		validarFiltroIdsProcessosCriminais(idsProcessosCriminais, criterios);

		return criterios;

	}

	private void validarFiltrosNomeParteDocumentoParteOutrosNomesAlcunha(String nomeParte, String documentoParte, String outrosNomesAlcunha, List<Criteria> criterios) throws PJeBusinessException {
		if (!Strings.isEmpty(nomeParte) || !Strings.isEmpty(documentoParte) || !Strings.isEmpty(outrosNomesAlcunha)) {

			criterios.add(Criteria.equals("processoParteList.partePrincipal", true));

			limitarParteSigilosa(criterios);

			if (!Strings.isEmpty(nomeParte)){
				
				Criteria nomeParteCriteria = Criteria.contains("processoParteList.pessoa.nomesPessoa.nome", nomeParte.replace(' ', '%'));
				criterios.add(nomeParteCriteria);

				Criteria tipoNomeCriteria = Criteria.in("processoParteList.pessoa.nomesPessoa.tipo", new TipoNomePessoaEnum[] {TipoNomePessoaEnum.C, TipoNomePessoaEnum.S} );
				criterios.add(tipoNomeCriteria);
			}

			if (!Strings.isEmpty(documentoParte)){

				if (!InscricaoMFUtil.validarCpfCnpj(documentoParte)){
					throw new PJeBusinessException("Documento de identificação inválido");
				}

				final String campoCodTipoDocIdentificacao = "processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo";

				if (documentoParte.length() == 14){
					criterios.add(Criteria.equals(campoCodTipoDocIdentificacao,"CPF"));
				}else{
					criterios.add(Criteria.equals(campoCodTipoDocIdentificacao,"CPJ"));
				}

				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento", documentoParte));

			}

			if (!Strings.isEmpty(outrosNomesAlcunha)) {
				criterios.add(Criteria.contains("processoParteList.pessoa.pessoaNomeAlternativoList.pessoaNomeAlternativo", outrosNomesAlcunha.replace(' ', '%')));
			}
		}
	}

	private void validarFiltroNumeroProcesso(String numeroProcessoFormatado, Integer numeroSequencia, Integer digitoVerificador, Integer ano, Integer numeroOrigem, String parteNumeroProcesso, List<Criteria> criterios) {
		if (StringUtils.isNotBlank(numeroProcessoFormatado)) {
			numeroProcessoFormatado = NumeroProcessoUtil.retiraMascaraNumeroProcesso(numeroProcessoFormatado);
			numeroProcessoFormatado = NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcessoFormatado);
			criterios.add(Criteria.equals("processo.numeroProcesso", numeroProcessoFormatado));
		}

		if (StringUtils.isNotBlank(parteNumeroProcesso)) {
			criterios.add(Criteria.contains("processo.numeroProcesso", parteNumeroProcesso));
		}

		if (numeroSequencia != null && numeroSequencia > 0){
			criterios.add(Criteria.equals("numeroSequencia", numeroSequencia));
		}

		if (digitoVerificador != null && digitoVerificador > 0){
			criterios.add(Criteria.equals("numeroDigitoVerificador", digitoVerificador));
		}

		if (ano != null && ano > 0){
			criterios.add(Criteria.equals("ano", ano));
		}

		if (numeroOrigem != null){
			criterios.add(Criteria.equals("numeroOrigem", numeroOrigem));
		}
	}

	private void validarFiltroRamoJusticaRespectivoTribunal(String ramoJustica, String respectivoTribunal, List<Criteria> criterios) {
		if(StringUtils.isNotBlank(ramoJustica) && StringUtils.isNotBlank(respectivoTribunal)){
			criterios.add(Criteria.equals("numeroOrgaoJustica",Integer.parseInt(ramoJustica + respectivoTribunal)));
		}
	}

	private void validarFiltroOrgaoJulgador(OrgaoJulgador orgaoJulgador, List<Criteria> criterios) {
		if (orgaoJulgador != null){
			criterios.add(Criteria.equals("orgaoJulgador.idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador()));
		}
	}

	private void validarFiltroOrgaoColegiado(OrgaoJulgadorColegiado orgaoColegiado, List<Criteria> criterios) {
		if (orgaoColegiado != null){
			criterios.add(Criteria.equals("orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", orgaoColegiado.getIdOrgaoJulgadorColegiado()));
		}
	}

	private void validarFiltroAssunto(String assunto, List<Criteria> criterios) {
		if (!Strings.isEmpty(assunto)){
			assunto = assunto.replaceAll(" ","%");
			criterios.add(Criteria.contains("processoAssuntoList.assuntoTrf.assuntoTrf", assunto));
		}
	}
	
	private void validarFiltroClasse(String classe, List<Criteria> criterios) {
		if (!Strings.isEmpty(classe)){
			criterios.add(Criteria.contains("classeJudicial.classeJudicial", classe));
		}
	}

	private void validarFiltroComplementoClasse(ComplementoClasse complementoClasse, String dsComponenteValidacao, String dsComponenteValidacaoData, List<Criteria> criterios) {
		/**
		 * Pesquisar de acordo com os valores informados na combo ComplementoClasse e o valor específico do campo.
		 */
		if(complementoClasse != null && (dsComponenteValidacao != null || dsComponenteValidacaoData != null)){
			criterios.add(Criteria.equals("complementoClasseProcessoTrfList.complementoClasse.idComplementoClasse", complementoClasse.getIdComplementoClasse()));
			if(dsComponenteValidacao != null){
				criterios.add(Criteria.equals("complementoClasseProcessoTrfList.valorComplementoClasseProcessoTrf", dsComponenteValidacao.toString()));
			}else if(dsComponenteValidacaoData != null){
				criterios.add(Criteria.equals("complementoClasseProcessoTrfList.valorComplementoClasseProcessoTrf", dsComponenteValidacaoData.toString()));
			}
		}
	}

	private void validarFiltrosOAB(Estado estadoOAB, String numeroOAB, String letraOAB, List<Criteria> criterios) {
		if (estadoOAB != null || !Strings.isEmpty(numeroOAB)){

			criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","OAB"));
			criterios.add(Criteria.equals("processoParteList.tipoParte.idTipoParte",ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte()));

			if (estadoOAB != null){
				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.estado.idEstado", estadoOAB.getIdEstado()));
			}

			if (!Strings.isEmpty(numeroOAB)){

				String numeroFinal = numeroOAB;

				if (!Strings.isEmpty(letraOAB)){
					numeroFinal += "-" + letraOAB;
				}

				criterios.add(Criteria.equals("processoParteList.pessoa.pessoaDocumentoIdentificacaoList.numeroDocumento",numeroFinal));
			}

		}
	}

	private void validarFiltrosDataAutuacao(Date dataAutuacaoInicio, Date dataAutuacaoFim, List<Criteria> criterios) {
		if (dataAutuacaoInicio != null && dataAutuacaoFim == null){
			criterios.add(Criteria.greaterOrEquals("dataAutuacao", dataAutuacaoInicio));

		}else if (dataAutuacaoFim != null && dataAutuacaoInicio == null){
			criterios.add(Criteria.lessOrEquals("dataAutuacao", dataAutuacaoFim));

		}else if(dataAutuacaoInicio != null && dataAutuacaoFim != null){
			Calendar novaDataAutuacaoFim = Calendar.getInstance();
			novaDataAutuacaoFim.setTime(dataAutuacaoFim);
			//Adicionando um dia para a data final.
			//Isto corrige o problema de não retornar resultados quando as datas de autuações são iguais, relatado na issue 14498.
			novaDataAutuacaoFim.add(Calendar.DAY_OF_MONTH, 1);

			criterios.add(Criteria.between("dataAutuacao", dataAutuacaoInicio, novaDataAutuacaoFim.getTime()));
		}
	}

	private void validarFiltroEleicao(Eleicao eleicao, List<Criteria> criterios) {
		if (eleicao != null) {
			criterios.add(Criteria.equals("complementoJE.eleicao", eleicao));
		}
	}

	private void validarFiltroEstado(Estado estado, List<Criteria> criterios) {
		if (estado != null) {
			criterios.add(Criteria.equals("complementoJE.estadoEleicao", estado));
		}
	}

	private void validarFiltroMunicipio(Municipio municipio, List<Criteria> criterios) {
		if (municipio != null) {
			criterios.add(Criteria.equals("complementoJE.municipioEleicao", municipio));
		}
	}

	private void validarFiltroNumeroDocumentoProcesso(String numeroDocumentoProcesso, List<Criteria> criterios) {
		if(numeroDocumentoProcesso != null && !numeroDocumentoProcesso.isEmpty()) {
			criterios.add(Criteria.equals("processo.processoDocumentoList.numeroDocumento", numeroDocumentoProcesso));
		}
	}

	private void validarFiltroValorCausa(Double valorCausaInicial, Double valorCausaFinal, List<Criteria> criterios) {
		if(valorCausaInicial != null){
			criterios.add(Criteria.greaterOrEquals("valorCausa", valorCausaInicial));
		}

		if(valorCausaFinal != null){
			criterios.add(Criteria.lessOrEquals("valorCausa", valorCausaFinal));
		}
	}

	private void validarFiltroObjetoProcesso(String objetoProcesso, List<Criteria> criterios) {
		if(!StringUtils.isEmpty(objetoProcesso)){
			criterios.add(Criteria.fullText("objeto", objetoProcesso));
		}
	}

	private void validarFiltroNumeroProcessoReferencia(String numeroProcessoReferencia, List<Criteria> criterios) throws PJeBusinessException {
		if (numeroProcessoReferencia != null) {
			if (NumeroProcessoUtil.numeroProcessoValido(numeroProcessoReferencia)) {
				criterios.add(Criteria.equals("desProcReferencia", numeroProcessoReferencia));
			} else {
				if (!stripNU(numeroProcessoReferencia).isEmpty()) {
					criterios.add(Criteria.contains("desProcReferencia", new Translate(".-", ""), stripNU(numeroProcessoReferencia)));
				}else {
					throw new PJeBusinessException("Processo referência inválido.");
				}
			}
		}
	}

	private void validarFiltroJurisdicao(Jurisdicao jurisdicao, List<Criteria> criterios) {
		if (jurisdicao != null) {
			criterios.add(Criteria.equals("orgaoJulgador.jurisdicao.idJurisdicao", jurisdicao.getIdJurisdicao()));
		}
	}

	private void validarFiltroMovimentacaoProcessual(Evento movimentacaoProcessual, List<Criteria> criterios) {
		if(movimentacaoProcessual != null) {
			criterios.add(Criteria.equals("processo.processoEventoList.evento.idEvento", movimentacaoProcessual.getIdEvento()));
		}
	}

	private void validarFiltroIdsProcessosCriminais(List<Integer> idsProcessosCriminais, List<Criteria> criterios) {
		if(CollectionUtilsPje.isNotEmpty(idsProcessosCriminais)) {
			criterios.add(Criteria.or(Criteria.in("idProcessoTrf", idsProcessosCriminais.toArray())));
		}
	}

	private void limitarParteSigilosa(List<Criteria> criterios) {
		UsuarioLocalizacao localizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual();
		if (localizacaoAtual != null) {
			UsuarioLocalizacaoMagistradoServidor li = localizacaoAtual.getUsuarioLocalizacaoMagistradoServidor();
			Criteria orgao = null;
			if (li != null) {
				if (Authenticator.isMagistrado() || Authenticator.isVisualizaSigiloso()) {
					if (li.getOrgaoJulgadorCargo() != null) {
						orgao = Criteria.equals("orgaoJulgadorCargo.idOrgaoJulgadorCargo", li.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
					} else if(li.getOrgaoJulgador() != null) {
						orgao = Criteria.equals("orgaoJulgador.idOrgaoJulgador", li.getOrgaoJulgador().getIdOrgaoJulgador());
					} else if(li.getOrgaoJulgadorColegiado() != null) {
						orgao = Criteria.equals("orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", li.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
					}
				}
			}
			Criteria naoSigiloso = Criteria.equals("processoParteList.parteSigilosa", false);
			Criteria visualizadorParte = Criteria.equals("processoParteList.visualizadores.pessoa.idUsuario", Authenticator.getIdUsuarioLogado());
			visualizadorParte.setRequired("processoParteList.visualizadores", false);
			if (orgao != null) {
				Criteria sigiloso = Criteria.equals("processoParteList.parteSigilosa", true);
				criterios.add(Criteria.or(naoSigiloso, Criteria.and(sigiloso, orgao), visualizadorParte));
			} else {
				criterios.add(Criteria.or(naoSigiloso, visualizadorParte));
			}
		}
	}

	/** 
	 * Retorna os critérios negociais de consulta de processo quando os filtros estão desabilitados.
	 * 
	 * @param nomeParte 
	 * @param documentoParte 
	 * @param numeroProcesso Número do processo.
	 * @return lista de critérios de pesquisa.
	 * @throws PJeException
	 */
	private List<Criteria> getCriteriosNegociaisConsultarProcessos(String nomeParte, String documentoParte) {
		boolean isMagistrado 	= Authenticator.isMagistrado() || Authenticator.isVisualizaSigiloso();
		Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();

		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));
	    if(!Strings.isEmpty(nomeParte) || !Strings.isEmpty(documentoParte)){
		    criterios.add(getCriteriosSituacao());
	    }
		
		//FILTRO Segredo de justiça
	    Criteria naoSigiloso = Criteria.equals("segredoJustica",false);
	    Criteria visibilidade = null;
	    if(idProcuradoria != null) {
	    	visibilidade = getCriteriosVisualizacaoProcuradorDefensor();
	    }
	    else if(isMagistrado) {
	    	List<Criteria> localizacaoUsuario = this.getCriteriosLocalizacaoUsuario();
	    	localizacaoUsuario.add(Criteria.lessOrEquals("nivelAcesso", Authenticator.recuperarNivelAcessoUsuarioLogado()));

	    	visibilidade = Criteria.and(localizacaoUsuario.toArray(new Criteria[localizacaoUsuario.size()]));
	    }
	    Criteria visibilidadeAtribuida = getCriteriosVisibilidadeAtribuida();
	    criterios.add(Criteria.or(naoSigiloso,visibilidade,visibilidadeAtribuida));
		
		return criterios;
	}
		
	/**
	 * Retorna o critério de visualização de procurador para a consulta de processos.
	 * 
	 * @return Critério de pesquisa.
	 */
	private Criteria getCriteriosVisualizacaoProcuradorDefensor(){
		boolean isAdmin = Authenticator.isAdministradorProcuradoriadefensoria();
		
		Criteria visibilidadeProcessosProcuradorDefensor = null;

		Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();

		if (idProcuradoria != null && !isAdmin){
			try {				
				visibilidadeProcessosProcuradorDefensor = Criteria.exists(queryVisualizadorProcuradoria("",idProcuradoria));	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return visibilidadeProcessosProcuradorDefensor;
		
	}
	
	/**
	 * Retorna os critérios de visibilidade da localização do usuário logado.
	 * 
	 * @return Critério de pesquisa.
	 */
	private List<Criteria> getCriteriosLocalizacaoUsuario() {
		List<Criteria> criteriasLocalizacao = new ArrayList<>();

		if (!Authenticator.isServidorExclusivoColegiado()) {
			List<Integer> idsLocalizacoesFisicasList = Authenticator.getIdsLocalizacoesFilhasAtuaisList();
			criteriasLocalizacao.add(Criteria.in("orgaoJulgador.localizacao.idLocalizacao", idsLocalizacoesFisicasList.toArray()));
		}

		Integer idOJC = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		if (idOJC != null) {
			criteriasLocalizacao.add(Criteria.equals("orgaoJulgadorColegiado.idOrgaoJulgadorColegiado", idOJC));
		}

		return criteriasLocalizacao;
	}
	
	/**
	 * Retorna os processos que o usuário possui visibilidade.
	 * A visibilidade leva em consideração se o usuário presente na tabela tb_proc_visibilida_segredo
	 * é um polo com situação 'Ativo'. Usuários com visibilidade que não fazem parte do processo
	 * também são resolvidos pela consulta.
	 * 
	 * @param usuarioLogado Usuario
	 * @return Criteria com os processos que o usuário possui visibilidade.
	 */
	@SuppressWarnings("unchecked")
	private Criteria getCriteriosVisibilidadeAtribuida(){
		StringBuilder hql;
		List<Integer> idsUsuarios = new ArrayList<>(0);
		
		idsUsuarios = adicionarUsuarioETestarSeAssistenteAdvogado(idsUsuarios);
		hql = new StringBuilder();
		hql.append("from ProcessoVisibilidadeSegredo vis "); 
		hql.append("where vis.processo = o ");
		hql.append("and vis.pessoa.idUsuario in ("+StringUtils.join(idsUsuarios,",")+") ");
		return Criteria.exists(hql.toString());
	}	
	
	/**
	 * Método responsável pela regra de exibição do botão
	 * "Solicitar inclusão para julgamento virtual" utilizado no fluxo de
	 * decisão colegiada em gabinete
	 * 
	 * @param idProcessoJudicial
	 *            id do processo que se deseja verificar a condição do botão
	 * @return <code>Boolean</code>, <code>true</code> se o processo não estiver
	 *         selecionado para julgamento ou não estiver selecionado para pauta ou
	 *         não estiver em pauta virtual e a classe judicial permitir sessão
	 *         contínua {@link Sessao#getContinua()}.
	 * @throws PJeBusinessException
	 */
	public boolean exibirBotaoJulgamentoVirtual(Integer idProcessoJudicial) throws PJeBusinessException {
		ProcessoTrf processo = findById(idProcessoJudicial);
		
		if (processo == null || processo.getIdProcessoTrf() == 0) {
			return false;
		}
		
		boolean selecionadoJulgamento = processo.getSelecionadoJulgamento();
		Boolean selecionadoPauta = processo.getSelecionadoPauta();
		Boolean pautaVirtual;
		if( processo.getPautaVirtual() != null ) {
			pautaVirtual = processo.getPautaVirtual();
		} else {
			pautaVirtual = Boolean.FALSE;
		}
		Boolean sessaoContinua = processo.getClasseJudicial().getSessaoContinua();
		
		return !(selecionadoJulgamento || selecionadoPauta || pautaVirtual) && sessaoContinua;
	}
	
	/**
	 * Método responsável por verificar se o processo está pautado numa sessão e
	 * ela já tenha sido fechada sua pauta.
	 * 
	 * @param idProcessoJudicial
	 *            id do processo que se deseja verificar
	 * @return <code>Boolean</code>, <code>true</code> se o processo esteja
	 *         pautado numa sessão cuja sua pauta já esteja fechada
	 * @throws PJeBusinessException
	 */
	public boolean isProcessoPautadoSessao(Integer idProcessoJudicial) throws PJeBusinessException {
		ProcessoTrf processo = findById(idProcessoJudicial);
		
		if (processo == null || processo.getIdProcessoTrf() == 0) {
			return false;
		}
		
		return processoPautadoDAO.isProcessoPautado(idProcessoJudicial);
	}
	
	/**
	 * @deprecated {@link ProcessoMagistradoManager#obterNomeRelator(ProcessoTrf)} 
	 */
	@Deprecated
	public String getNomeRelator(ProcessoTrf processo) {
		return processoMagistradoManager.obterNomeRelator(processo);		
	}
	
	public List<Criteria> getCriteriosConsultarProcessos(String numeroProcessoFormatado,
			Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal, 
			String nomeParte, 
			String documentoParte, 
			Estado estadoOAB, 
			String numeroOAB,
			String letraOAB,
			String assunto, 
			String classe, 
			ComplementoClasse complementoClasse, 
			String dsComponenteValidacao, 
			String dsComponenteValidacaoData, 
			OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoColegiado, 
			Date dataAutuacaoInicio, 
			Date dataAutuacaoFim,
			Eleicao eleicao,
			Estado estado,
			Municipio municipio,
			String numeroDocumentoProcesso,
			Double valorCausaInicial, 
			Double valorCausaFinal,
			String numeroProcessoReferencia,
			String objetoProcesso,
			Jurisdicao jurisdicao,
			Evento movimentacaoProcessual,
			Boolean validacaoPeloMenosUmCampoInformado,
			String nomeAdvogado,
			TipoArtigo260Enum tipoArtigo260Enum,
			String parteNumeroProcesso,
			List<Integer> idsProcessosCriminais,
			String outrosNomesAlcunha)  throws PJeBusinessException {
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosPesquisaConsultarProcessos(
				numeroProcessoFormatado, numeroSequencia, digitoVerificador, ano, numeroOrigem, 
				ramoJustica, respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB,
				letraOAB, assunto, classe, complementoClasse, dsComponenteValidacao,
				dsComponenteValidacaoData, orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
				dataAutuacaoFim, eleicao, estado, municipio, numeroDocumentoProcesso, 
				valorCausaInicial, valorCausaFinal, numeroProcessoReferencia, objetoProcesso,
				jurisdicao, movimentacaoProcessual, validacaoPeloMenosUmCampoInformado,nomeAdvogado,tipoArtigo260Enum,parteNumeroProcesso,idsProcessosCriminais,
				outrosNomesAlcunha));
		
		criterios.addAll(getCriteriosNegociaisConsultarProcessos(nomeParte,documentoParte));
		
		return criterios;
	}

	private Collection<? extends Criteria> getCriteriosPesquisaConsultarProcessos(String numeroProcessoFormatado,
			Integer numeroSequencia, 
			Integer digitoVerificador, 
			Integer ano, 
			Integer numeroOrigem, 
			String ramoJustica, 
			String respectivoTribunal, 
			String nomeParte, 
			String documentoParte, 
			Estado estadoOAB, 
			String numeroOAB,
			String letraOAB,
			String assunto, 
			String classe, 
			ComplementoClasse complementoClasse, 
			String dsComponenteValidacao, 
			String dsComponenteValidacaoData, 
			OrgaoJulgador orgaoJulgador, 
			OrgaoJulgadorColegiado orgaoColegiado, 
			Date dataAutuacaoInicio, 
			Date dataAutuacaoFim,
			Eleicao eleicao,
			Estado estado,
			Municipio municipio,
			String numeroDocumentoProcesso,
			Double valorCausaInicial, 
			Double valorCausaFinal,
			String numeroProcessoReferencia,
			String objetoProcesso,
			Jurisdicao jurisdicao,
			Evento movimentacaoProcessual,
			Boolean validacaoPeloMenosUmCampoInformado, 
			String nomeAdvogado,
			TipoArtigo260Enum tipoArtigo260Enum,
			String parteNumeroProcesso,
			List<Integer> idsProcessosCriminais,
			String outrosNomesAlcunha) throws PJeBusinessException {
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		criterios.addAll(getCriteriosPesquisaConsultarProcessos(
				numeroProcessoFormatado, numeroSequencia, digitoVerificador, ano, numeroOrigem, 
				ramoJustica, respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB,
				letraOAB, assunto, classe, complementoClasse, dsComponenteValidacao,
				dsComponenteValidacaoData, orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
				dataAutuacaoFim, eleicao, estado, municipio, numeroDocumentoProcesso, 
				valorCausaInicial, valorCausaFinal, numeroProcessoReferencia, objetoProcesso,
				jurisdicao, movimentacaoProcessual, validacaoPeloMenosUmCampoInformado,parteNumeroProcesso, idsProcessosCriminais,
				outrosNomesAlcunha));
				
		criterios.addAll(getCriteriosPesquisaConsultarProcessosNomeAdvogadoArtigo260(nomeAdvogado,tipoArtigo260Enum));
		
		validarPreenchimentoPesquisa(validacaoPeloMenosUmCampoInformado, criterios);

		return criterios;
	}

	/**
	 * Metodo responsavel por validar o preenchimento minimo dos campos da pesquisa.
	 * 
	 * @param validacaoPeloMenosUmCampoInformado
	 * @param criterios
	 * @throws PJeBusinessException
	 */
	
	private void validarPreenchimentoPesquisa(Boolean validacaoPeloMenosUmCampoInformado, List<Criteria> criterios) throws PJeBusinessException {
		if (BooleanUtils.isTrue(validacaoPeloMenosUmCampoInformado) && criterios.isEmpty() || 
				(criterios.size() == 1 && criterios.toString().contains("numeroOrgaoJustica"))) {
			throw new PJeBusinessException("Pelo menos um dos critérios de pesquisa deve ser informado.");
		}
	}

	private Collection<? extends Criteria> getCriteriosPesquisaConsultarProcessosNomeAdvogadoArtigo260(
			String nomeAdvogado, TipoArtigo260Enum tipoArtigo260Enum) {
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		if (nomeAdvogado != null && !Strings.isEmpty(nomeAdvogado)){
			
			// Ajuste para que seja pesquisado pelo Nome da Parte e Nome do Representante na Consulta Processual.
			
			StringBuilder sqlNomeAdvogado = new StringBuilder();
			sqlNomeAdvogado.append(" select 1 ");
			sqlNomeAdvogado.append(" from ProcessoParte pp ");
			sqlNomeAdvogado.append(" inner join pp.pessoa pes ");
			sqlNomeAdvogado.append(" inner join pes.nomesPessoa np ");
			sqlNomeAdvogado.append(" where ");
			sqlNomeAdvogado.append("   pp.processoTrf.idProcessoTrf = o.idProcessoTrf ");
			sqlNomeAdvogado.append("   and pp.inSituacao = '" + ProcessoParteSituacaoEnum.A + "' ");
			sqlNomeAdvogado.append("   and pp.partePrincipal = false ");
			sqlNomeAdvogado.append("   and pp.inSituacao in ( '" + ProcessoParteSituacaoEnum.A + "', '" + ProcessoParteSituacaoEnum.B + "','" + ProcessoParteSituacaoEnum.S + "') ");
			sqlNomeAdvogado.append("   and LOWER(to_ascii(np.nome)) LIKE LOWER(to_ascii('%" + nomeAdvogado.trim().replace(' ', '%') + "%'))");
			sqlNomeAdvogado.append("   and np.tipo in ( '" + TipoNomePessoaEnum.C + "', '" + TipoNomePessoaEnum.S + "' ) ");
			
			Criteria nomeadvogadoCriteria = Criteria.exists(sqlNomeAdvogado.toString());
			
			criterios.add(nomeadvogadoCriteria);

 		}
		if (tipoArtigo260Enum!=null){
 			if (tipoArtigo260Enum == TipoArtigo260Enum.ProcessosPreventos){
 				criterios.add(Criteria.greater("complementoJE.vinculacaoDependenciaEleitoral.id", 0));
 			}
 			if (tipoArtigo260Enum == TipoArtigo260Enum.ProcessosQueCriaramCadeiaPrevencao){
 				criterios.add(Criteria.equals("complementoJE.paradigma",true));	
 			}
		}
		return criterios;
	}
	
	private Criteria getCriteriosSituacao() {
		Criteria parteAtiva = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.A);
		Criteria parteBaixada = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.B);
		Criteria parteSuspensa = Criteria.equals("processoParteList.inSituacao", ProcessoParteSituacaoEnum.S);
		return Criteria.or(parteAtiva, parteBaixada, parteSuspensa);
	}

	public Long getCountProcessosJurisdicao(Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		this.inicializaDadosUsuario();
		
		return processoJudicialDAO.getCountProcessosJurisdicao(idUsuarioAtual, idLocalizacaoFisica, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisaGeral, searchLocal);
	}
	
	public Long getCountProcessosJurisdicaoCaixa(Integer idJurisdicao, Integer idCaixa, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		this.inicializaDadosUsuario();

		return processoJudicialDAO.getCountProcessosJurisdicaoCaixa(idUsuarioAtual, idLocalizacaoFisica, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, idCaixa, criteriosPesquisaGeral, searchLocal);
	}
	
	public List<JurisdicaoVO> getJurisdicoesAcervo(ConsultaProcessoVO criteriosPesquisa) {
		return processoJudicialDAO.getJurisdicoesAcervo(idUsuarioAtual, idLocalizacaoFisica, tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisa);
	}
	
	public List<ProcessoTrf> getProcessosJurisdicao(Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		this.inicializaDadosUsuario();

		return processoJudicialDAO.getProcessosJurisdicao(idUsuarioAtual, idLocalizacaoFisica, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisaGeral, searchLocal);
	}

	public List<ProcessoTrf> getProcessosJurisdicaoCaixa(Integer idJurisdicao, Integer idCaixa, ConsultaProcessoVO criteriosPesquisaGeral, Search searchLocal) throws PJeBusinessException{
		this.inicializaDadosUsuario();

		return processoJudicialDAO.getProcessosJurisdicaoCaixa(idUsuarioAtual, idLocalizacaoFisica, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, idCaixa, criteriosPesquisaGeral, searchLocal);
	}

	public List<CaixaAdvogadoProcuradorVO> getCaixasAcervoJurisdicao(Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisa) {
		criteriosPesquisa.setIdJurisdicao(idJurisdicao);
		return processoJudicialDAO.getCaixasAcervoJurisdicao(idUsuarioAtual, idLocalizacaoFisica, tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisa);
	}

	public ProcessoTrf pesquisarProcessoPJE(String numeroPprocesso, boolean verificarSigilo) {
		return this.pesquisarProcessoPJE(numeroPprocesso,verificarSigilo, false);
	}

	public ProcessoTrf pesquisarProcessoPJE(String numeroPprocesso, boolean verificarSigilo, boolean isProcessoParadigma) {
		if (NumeroProcessoUtil.numeroProcessoValido(numeroPprocesso)) {
			List<ProcessoTrf> result = this.recuperarProcessosUsuariosPorNumeroProcesso(NumeroProcessoUtil.mascaraNumeroProcesso(numeroPprocesso), isProcessoParadigma);
			if (CollectionUtilsPje.isNotEmpty(result)) {
				return result.get(0);
			}
		}
		return null;
	}
	
	/***
	 * Dado um processo TRF, o sistema verifica se o usuário logado tem permissão para visualizar seus dados
	 * 
	 * @param processo
	 * @return
	 */
	public boolean verificarPermissaoVisibilidade(ProcessoTrf processo) {
		boolean permissaoVisibilidade = true;
		if(processo != null && processo.getSegredoJustica()) {
			permissaoVisibilidade = validarPermissaoUsuario(processo);
		}
		
		return permissaoVisibilidade;
	}

	public List<ProcessoTrf> pesquisarPautaMesa(FiltroProcessoSessaoDTO filtro) {
		return processoJudicialDAO.pesquisarPautaMesa(filtro);
	}

	public PagedQueryResult<CabecalhoProcesso> recuperarMetadadosProcessoParaAssinatura(InformacaoUsuarioSessao informacaoUsuario,  Integer idTipoDocumento, CriterioPesquisa criteriosPesquisa){
		Long qtd = this.processoJudicialDAO.recuperarQtdProcessosAssinatura(informacaoUsuario, idTipoDocumento, criteriosPesquisa);
		List<CabecalhoProcesso> dados = this.processoJudicialDAO.recuperarListaProcessosAssinatura(informacaoUsuario, idTipoDocumento, null, criteriosPesquisa, criteriosPesquisa.getConferidos());
		processarTags(dados,informacaoUsuario);
		if(dados != null && !dados.isEmpty()) {
			processarLembretes(dados);
			processarModulosFluxo(dados);
		}
		return new PagedQueryResult<CabecalhoProcesso>(qtd,dados);
	}
	
	public PagedQueryResult<CabecalhoProcesso> recuperarMetadadosProcesso(InformacaoUsuarioSessao informacaoUsuario, String tarefa, CriterioPesquisa query) {
		Long qtd = processoJudicialDAO.recuperarQtdProcessosTarefa(informacaoUsuario,tarefa,null,query);
		List<CabecalhoProcesso> dados = new ArrayList<CabecalhoProcesso>();
		if(qtd > 0) {
			dados = processoJudicialDAO.recuperarMetadadosProcesso(informacaoUsuario, tarefa, null,query);
			processarTags(dados,informacaoUsuario);
			processarLembretes(dados);
			processarModulosFluxo(dados);
			dados.get(0).setPodeMovimentarEmLote(fluxoService.podeMovimentarEmLote(dados.get(0).getIdTaskInstance()));
			dados.get(0).setPodeMinutarEmLote(fluxoService.podeMinutarEmLote(dados.get(0).getIdTaskInstance()));
			dados.get(0).setPodeIntimarEmLote(fluxoService.podeIntimarEmLote(dados.get(0).getIdTaskInstance()));
			dados.get(0).setPodeDesignarAudienciaEmLote(fluxoService.podeDesignarAudienciaEmLote(dados.get(0).getIdTaskInstance()));
			dados.get(0).setPodeDesignarPericiaEmLote(fluxoService.podeDesignarPericiaEmLote(dados.get(0).getIdTaskInstance()));
			dados.get(0).setPodeRenajudEmLote(fluxoService.podeRenajudEmLote(dados.get(0).getIdTaskInstance()));
		}
		return new PagedQueryResult<CabecalhoProcesso>(qtd,dados);
	}
	
	private void processarTags(List<CabecalhoProcesso> dados,InformacaoUsuarioSessao informacaoUsuario){
		Set<Long> idsProcessos = new HashSet<Long>(dados.size());
		for(CabecalhoProcesso c : dados){
			idsProcessos.add(c.getIdProcesso());
		}
		
		List<EtiquetaProcesso> etiquetaProcessos = processoTagManager.listarTags(idsProcessos, informacaoUsuario.getIdLocalizacaoFisica(), null);
		for(EtiquetaProcesso e : etiquetaProcessos){
			for(CabecalhoProcesso c : dados){
				if(c.getIdProcesso().equals(e.getIdProcesso())){
					c.getTagsProcessoList().add(e);
				}
			}
		}
	}
	
	private void processarLembretes(List<CabecalhoProcesso> dados) {
		LembreteDAO lembreteDAO = ComponentUtil.getComponent(LembreteDAO.NAME);
		
		Set<Integer> idsProcessos = new HashSet<Integer>(dados.size());
		for(CabecalhoProcesso c : dados){
			idsProcessos.add(c.getIdProcesso().intValue());
		}
		List<LembreteDTO> lembretes = lembreteDAO.recuperarLembretesPorSituacaoPorIdsProcesso(true, idsProcessos);
		for(LembreteDTO lembrete : lembretes){
			for(CabecalhoProcesso c : dados){
				if(c.getIdProcesso().equals(lembrete.getIdProcessoJudicial())){
					if(!c.getLembretes().contains(lembrete))
						c.getLembretes().add(lembrete);
					break;
				}
			}
		}
	}
	
	private void processarModulosFluxo(List<CabecalhoProcesso> dados) {
		for (CabecalhoProcesso c : dados) {
			String moduloAngular = fluxoService.obterModuloAngular(c.getIdTaskInstance());
			if(moduloAngular != null && !moduloAngular.isEmpty()) {
				c.setModulo(moduloAngular);
			}
		}
	}
	
	
	public List<TagDTO> recuperarEtiquetasQuantitativoProcessoTarefaPendente(InformacaoUsuarioSessao usuarioSesssao, String tarefa, CriterioPesquisa crit) {
		return processoJudicialDAO.recuperarEtiquetasQuantitativoProcessoTarefaPendente(usuarioSesssao, tarefa, crit);
	}
	
	public List<TagDTO> recuperarEtiquetasQuantitativoParaAssinatura(InformacaoUsuarioSessao usuarioSesssao, Integer tipoDocumento, CriterioPesquisa crit) {
		return processoJudicialDAO.recuperarEtiquetasQuantitativoParaAssinatura(usuarioSesssao, tipoDocumento, crit);
	}
	
	public PagedQueryResult<CabecalhoProcesso> recuperarProcessoTarefaPorEtiqueta(InformacaoUsuarioSessao informacaoUsuario, String tarefa, Integer idTag, CriterioPesquisa query) {
		Long qtd = processoJudicialDAO.recuperarQtdProcessosTarefa(informacaoUsuario,tarefa,idTag, query);
		List<CabecalhoProcesso> dados = new ArrayList<CabecalhoProcesso>();
		if(qtd > 0) {
			dados = processoJudicialDAO.recuperarMetadadosProcesso(informacaoUsuario, tarefa, idTag, query);
			processarTags(dados,informacaoUsuario);
			processarLembretes(dados);
			processarModulosFluxo(dados);
		}
		return new PagedQueryResult<CabecalhoProcesso>(qtd,dados);
	}
	
	public PagedQueryResult<CabecalhoProcesso> recuperarProcessoTarefaAssinaturaPorEtiqueta(InformacaoUsuarioSessao informacaoUsuario, Integer tipoDocumento, Integer idTag, CriterioPesquisa query) {
		Long qtd = processoJudicialDAO.recuperarQtdProcessosAssinatura(informacaoUsuario, tipoDocumento, query, idTag);
		List<CabecalhoProcesso> dados = new ArrayList<CabecalhoProcesso>();
		if(qtd > 0) {
			dados = processoJudicialDAO.recuperarListaProcessosAssinatura(informacaoUsuario, tipoDocumento, idTag, query, false);
			processarTags(dados,informacaoUsuario);
			processarLembretes(dados);
			processarModulosFluxo(dados);
		}
		return new PagedQueryResult<CabecalhoProcesso>(qtd,dados);
	}

	public List<CabecalhoProcesso> recuperarMetadadosProcessoParaAssinaturaMobile(InformacaoUsuarioSessao informacaoUsuario){
		CriterioPesquisa criterioPesquisa = new CriterioPesquisa();
		criterioPesquisa.setExigibilidadeAssinatura(ExigibilidadeAssinaturaEnum.getListaPermiteAssinar());
		return this.processoJudicialDAO.recuperarListaProcessosAssinatura(informacaoUsuario, null, null, criterioPesquisa, null);
	}
	
	public List<CaixaAdvogadoProcuradorVO> getCaixasAcervoJurisdicao(int idJurisdicao) {
		return this.processoJudicialDAO.recuperarListaCaixasAcervoJurisdicao(idJurisdicao);
	}
	
	public void copiarProcessoParaCaixa(ProcessoTrf processo, CaixaAdvogadoProcurador caixaDestino, ConsultaProcessoVO criterios) {
		this.processoJudicialDAO.copiarProcessoParaCaixa(processo,caixaDestino,criterios);
	}

	public List<Integer> recuperarAssuntos(ProcessoTrf processo) {
		List<Integer> retorno = new ArrayList<>();
		List<AssuntoTrf> listaAssunto = processo.getAssuntoTrfList();
		if (listaAssunto == null || listaAssunto.isEmpty()) {
			listaAssunto = ComponentUtil.getComponent(AssuntoTrfManager.class).findAssuntosTrfPorProcessoTrf(processo);
		}
		for (AssuntoTrf assunto : listaAssunto) {
			retorno.add(Integer.valueOf((assunto.getIdAssuntoTrf())));
		}

		return retorno;
	}
	
	public ProcessoTrf recuperarProcesso(ProcessoDocumento documento) {
		return this.getDAO().recuperarProcesso(documento);
	}
	
	public String queryExisteFluxoDeslocadoParaLocalizacaoDoUsuario(String prefix, String idsLocalizacoesFisicas, Integer nivelAcesso) {
		return getDAO().queryExisteFluxoDeslocadoParaLocalizacaoUsuario(prefix, idsLocalizacoesFisicas, nivelAcesso);
	}

	public boolean existeFluxoDeslocadoParaLocalizacaoDoUsuario(ProcessoTrf processoJudicial, String idsLocalizacoesFisicas) {
		return processoJudicialDAO.existeFluxoDeslocadoParaLocalizacaoUsuario(processoJudicial, idsLocalizacoesFisicas);
	}
	
	public List<CabecalhoProcesso> recuperarProcessosPorEtiqueta(InformacaoUsuarioSessao informacaoUsuario, Integer idTag) {
		List<CabecalhoProcesso> dados = processoJudicialDAO.recuperarMetadadosProcessosPorTag(informacaoUsuario, idTag);
		processarTags(dados, informacaoUsuario);
		processarLembretes(dados);
		processarModulosFluxo(dados);
		return dados;
	}
	
	/**
	 * Retorna os id_localizacao constantes em tb_processo_tarefa a partir do id_processo_trf, ou seja, 
	 * retorna todas as localizações em que há tarefas ativas para o processo informado.
	 * Obs: o id_localizacao nessa tabela reflete a localização atual da tarefa, inclusive na hipótese de 'fluxos deslocados',
	 * onde uma tarefa de um processo aparece no fluxo de um órgão julgador distinto do qual o processo se encontra vinculado.  
	 * @param idProcesso
	 * @return
	 */
	public List<Integer> pegaIdsLocalizacaoProcessoTarefa(Long idProcessoTrf) {
		return this.processoJudicialDAO.pegaIdsLocalizacaoProcessoTarefa(idProcessoTrf);
	}

	public List<CompetenciaAreaDireito> recuperarAreasDireito(Municipio municipio) {
		return this.processoJudicialDAO.recuperarAreasDireito(municipio != null ? municipio.getIdMunicipio() : null);
	}

	public boolean isConcluso(ProcessoTrf processoJudicial, String tipoConclusao) {

		return processoJudicialDAO.isConcluso(processoJudicial, tipoConclusao);

	}

	public PaginadorDTO<Void, ProcessoTrf> paginar(String numeroProcesso, String documentoParte, String oab, PaginadorDTO<Void, ProcessoTrf> paginador) {
				
		PaginadorDTO<Void, ProcessoTrf> resultado = new PaginadorDTO<>();
		
		try {
			String oabInscricao = OABUtil.extrairInscricao(oab);
			String oabUF = OABUtil.extrairUF(oab);
			Estado oabEstado = EstadoManager.instance().findBySigla(oabUF);
			
			Search search = getSearchConsultarProcessos(numeroProcesso, null, null, null, null, 
					null, null, null, documentoParte, oabEstado, oabInscricao, null, null, null, null, null, null, null, 
					null, null, null, null, null, null, null, null, null, null, null, null, null, false, null, null, null);
			Long count = count(search);
			
			search.setFirst(paginador.getFirst());
			search.setMax(paginador.getTamanhoPagina());
			List<ProcessoTrf> list = list(search);
			PaginadorDTO<Void, ProcessoTrf> dto = new PaginadorDTO<>();
			dto.setPagina(paginador.getPagina());
			dto.setTamanhoPagina(paginador.getTamanhoPagina());
			dto.setTotalRegistros(count.intValue());
			dto.setColecao(list);
			resultado = dto;
		} catch (PJeBusinessException e) {
			logger.error(e.getLocalizedMessage());
			return null;
		}
		
		return resultado;
	}
	
	
}
