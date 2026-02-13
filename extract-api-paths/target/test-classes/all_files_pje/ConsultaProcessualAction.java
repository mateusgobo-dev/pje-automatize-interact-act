package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.event.PhaseId;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.component.suggest.ClasseJudicialComCompetenciaSuggestBean;
import br.com.infox.cliente.component.suggest.MovimentoProcessualSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ComplementoClasseManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorCargoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradorManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.ReCaptchaService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.client.criminal.OrgaoProcedimentoOriginarioRestClient;
import br.jus.cnj.pje.webservice.client.criminal.ProcessoCriminalRestClient;
import br.jus.je.pje.suggest.ConsultaProcessualMunicipioSuggestBean;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.je.enums.TipoArtigo260Enum;
import br.jus.pje.nucleo.dto.OrgaoProcedimentoOriginarioDTO;
import br.jus.pje.nucleo.dto.ProcessoCriminalDTO;
import br.jus.pje.nucleo.dto.ProcessoProcedimentoOrigemDTO;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessualManager;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SituacaoProcesso;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name(ConsultaProcessualAction.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaProcessualAction extends BaseAction<ProcessoTrf>{

	public static final String NAME = "consultaProcessualAction";
	
	private static final long serialVersionUID = 1L;
	
	@In
	private ParametroService parametroService;
	
	private Jurisdicao jurisdicao;
	private Integer idProcessoTrf;
	private String nomeParte;
	private String documentoParte;
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private Integer numeroOrigem;
	private OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOrigemCriminal;
	private String numeroProcedimentoCriminal;
	private Integer anoProcedimentoCriminal;
	private String numeroProtocoloPolicia;
	private String assuntoTrf;
	private String classeJudicial;
	private Estado estadoOAB;
	private String numeroOAB;
	private String letraOAB;
	private OrgaoJulgadorColegiado orgaoColegiado;
	private OrgaoJulgador orgaoJulgador;
	private Date dataAutuacaoInicio;
	private Date dataAutuacaoFim;
	private Boolean hasAvisoAcessoProcesso = false;
	private Eleicao eleicao;
	private Estado estado;
	private Municipio municipio;
	private String ramoJustica;
	private String respectivoTribunal;
	private String numeroDocumento;
	private String objetoProcesso;
	private ComplementoClasse complementoClasse;
	private String dsComponenteValidacao;
	private String dsComponenteValidacaoData;
	private List<ComplementoClasse> complementoClasseList;
	private Double valorCausaInicial;
	private Double valorCausaFinal;
	private String numeroProcessoReferencia;
	private Boolean aplicarMascaraProcessoReferencia = Boolean.TRUE;
	private Evento movimentacaoProcessual;
	private TipoArtigo260Enum tipoArtigo260Enum;
	private String nomeAdvogado;
	private String outrosNomesAlcunha;
	private List<Estado> listEstadosJurisdicoes;

	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoManager processoManager;
	
	@In
	private JurisdicaoManager jurisdicaoManager;
	
	@In
	private ProcuradorManager procuradorManager;
	
	@In
	private LocalizacaoManager localizacaoManager;
	
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	
	@In
	private OrgaoJulgadorCargoManager orgaoJulgadorCargoManager;
	
	
	@In(value=Authenticator.USUARIO_LOCALIZACAO_ATUAL, scope=ScopeType.SESSION)
	private UsuarioLocalizacao localizacaoAtual;
	
	@In(create = true)
	private ComplementoClasseManager complementoClasseManager;
	
	private EntityDataModel<ProcessoTrf> model;
	
	@RequestParameter(value="processosGridCount")
	private Integer processosGridCount;
	
	@RequestParameter(value="idProcessoSelecionado")
	private Integer idProcessoSelecionado;

	@RequestParameter(value="iframe")
	private Boolean iframe;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private ConsultaProcessualManager consultaProcessualManager;

	private List<OrgaoProcedimentoOriginarioDTO> orgaoProcedimentoOriginarioList = new ArrayList<>();

	@Create
	public void Init(){
		if(iframe != null && iframe){
			Contexts.getConversationContext().set("showTopoSistema",false);
		}
		else{
			Contexts.getConversationContext().set("showTopoSistema",true);
		}
		atribuirNumeroOrgaoJustica();
		//Para não executar a grid ao entrar na página a primeira vez
		if (org.jboss.seam.contexts.FacesLifecycle.getPhaseId() != PhaseId.RENDER_RESPONSE){
			model = new EntityDataModel<ProcessoTrf>(ProcessoTrf.class, super.facesContext, getRetriever());
		}

	}

	public void recuperarOrgaosProcedimentoOriginario() {
		OrgaoProcedimentoOriginarioRestClient orgaoProcedimentoOrigemRestClient = ComponentUtil
				.getComponent(OrgaoProcedimentoOriginarioRestClient.NAME);
		try {
			orgaoProcedimentoOriginarioList = orgaoProcedimentoOrigemRestClient.recuperaListagem();
		} catch (Exception e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	public void pesquisarReCaptcha() {
		if (ParametroUtil.instance().isReCaptchaAtivo() && Identity.instance().hasRole(Papeis.EXIGE_RECAPTCHA) &&
				!ReCaptchaService.instance().validarResposta((String)Util.getRequestParameter("g-recaptcha-response"))) {
			
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "captcha.invalidCaptcha");
		} else {
			pesquisar();
		}
	}

	public void pesquisar() {
		List<Integer> idsProcessosCriminais = new ArrayList<>(0);
		Boolean validaFiltroCriminal = Boolean.FALSE;
		validaFiltroCriminal = StringUtil.isNotEmpty(numeroProcedimentoCriminal) || 
				(anoProcedimentoCriminal != null && anoProcedimentoCriminal != 0) || 
				orgaoProcedimentoOrigemCriminal != null || 
				StringUtil.isNotEmpty(numeroProtocoloPolicia);
		
		try {
			if(validaFiltroCriminal) {
				idsProcessosCriminais = filtroProcessoCriminal();
			}
			
					
			if (validaFiltroCriminal && CollectionUtilsPje.isEmpty(idsProcessosCriminais)) {
				return;
			}
			List<Criteria> criterios = processoJudicialManager.getCriteriosConsultarProcessos(null, 
				numeroSequencia, digitoVerificador, ano, numeroOrigem, ramoJustica, 
				respectivoTribunal, nomeParte, documentoParte, estadoOAB, numeroOAB, letraOAB, 
				getAssunto(), getClasse(), complementoClasse, dsComponenteValidacao, 
				dsComponenteValidacaoData, orgaoJulgador, orgaoColegiado, dataAutuacaoInicio, 
				dataAutuacaoFim, eleicao, estado, municipio, numeroDocumento, valorCausaInicial, 
				valorCausaFinal, numeroProcessoReferencia, objetoProcesso, jurisdicao, movimentacaoProcessual, true, nomeAdvogado, tipoArtigo260Enum,null, idsProcessosCriminais,
				outrosNomesAlcunha);
		
			HashMap<String, Order> ordenacao = new HashMap<>();
			ordenacao.put("o.dataAutuacao", Order.DESC);
			
			model = consultaProcessualManager.pesquisar(criterios, ordenacao, null);
			
			setIdProcessoTrf(0);
		} catch (Exception e) {
			model = null;
			facesMessages.add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	private List<Integer> filtroProcessoCriminal() throws PJeException {
		List<Integer> idsProcessosCriminais = null;
		
		ProcessoProcedimentoOrigemDTO processoProcedimentoOrigemDTO = new ProcessoProcedimentoOrigemDTO();
		processoProcedimentoOrigemDTO.setNumero(numeroProcedimentoCriminal);
		processoProcedimentoOrigemDTO.setAno(anoProcedimentoCriminal);
		processoProcedimentoOrigemDTO.setOrgaoProcedimentoOriginario(orgaoProcedimentoOrigemCriminal);
		processoProcedimentoOrigemDTO.setNrProtocoloPolicia(numeroProtocoloPolicia);
		
		ProcessoCriminalDTO processoDTO = new ProcessoCriminalDTO();
		List<ProcessoProcedimentoOrigemDTO> processoProcedimentoOrigemList = new ArrayList<>();
		processoProcedimentoOrigemList.add(processoProcedimentoOrigemDTO);
		
		processoDTO.setProcessoProcedimentoOrigemList(processoProcedimentoOrigemList);
		
		ProcessoCriminalRestClient processoCriminalRestClient = ComponentUtil.getComponent(ProcessoCriminalRestClient.class);
		
		List<String> numerosProcesso = processoCriminalRestClient.consultaNumeroProcessoProcedimentoOrigem(processoProcedimentoOrigemDTO);
		
		if(CollectionUtilsPje.isNotEmpty(numerosProcesso)) {
			idsProcessosCriminais = processoManager.recuperarIdsProcessosPorNumero(numerosProcesso);
		}
		return idsProcessosCriminais;
	}

	public void verificarVisualizacaoProcesso(){
		boolean isAdmin = Authenticator.isPapelAdministrador();
		boolean isAdvogado = Authenticator.isAdvogado();
		boolean isAssistenteAdvogado = Authenticator.isAssistenteAdvogado(); 
		boolean isProcurador = Authenticator.isProcurador() || Authenticator.isAssistenteProcurador();
		boolean isJusPostulandi = Authenticator.isJusPostulandi();
		boolean logarAcessoProcessoTerceiro = false;
		
		if (isAssistenteAdvogado && !isAdmin) {
			int idPapelAdvogado = ParametroUtil.instance().getPapelAdvogado().getIdPapel();
			int idTipoParteAdvogado = ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte();
			int idUsuarioLocalizacaoAtual = Authenticator.getUsuarioLocalizacaoAtual().getIdUsuarioLocalizacao();
			
			boolean isAssistenteAdvogadoProcesso = processoParteManager.isAssistenteAdvogadoProcesso(idPapelAdvogado, getIdProcessoSelecionado(), idTipoParteAdvogado, idUsuarioLocalizacaoAtual);
			logarAcessoProcessoTerceiro = !isAssistenteAdvogadoProcesso;
		} else if (isAdvogado && !isAdmin){
			
			Query q = EntityUtil.getEntityManager().createQuery("select 1 from ProcessoParte o " +
																"where o.pessoa.idUsuario = :idUsuario " +
																"and o.processoTrf.idProcessoTrf = :idProcessoTrf " +
																"and o.tipoParte.idTipoParte = :idTipoParte " +
																"and o.inSituacao = 'A' ");
			q.setParameter("idUsuario", Authenticator.getIdUsuarioLogado());
			q.setParameter("idProcessoTrf", idProcessoSelecionado);
			q.setParameter("idTipoParte", ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte());
			logarAcessoProcessoTerceiro = q.getResultList().size() == 0;
		}
		
		if (isProcurador && !isAdmin){
			
			try {
				Integer idProcuradoria = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
				Integer idPessoa = Authenticator.getIdUsuarioLogado();
				Query q = EntityUtil.getEntityManager().createQuery("select 1 from ProcessoParte o " +
																	"where (o.procuradoria.idProcuradoria = :procuradoria or " +
																	"o.pessoa.idUsuario = :pessoa) " +
																	"and o.processoTrf.idProcessoTrf = :idProcessoTrf");
				q.setParameter("idProcessoTrf", idProcessoSelecionado);
				q.setParameter("procuradoria", idProcuradoria);
				q.setParameter("pessoa", idPessoa);
				logarAcessoProcessoTerceiro = q.getResultList().size() == 0;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(isJusPostulandi && !isAdmin){
			Query queryVerificaParte = EntityUtil.getEntityManager().createQuery(
					"select 1 from ProcessoParte o " +
					"where o.pessoa.idUsuario = :idUsuario " +
					"and o.processoTrf.idProcessoTrf = :idProcessoTrf " +
					"and o.inSituacao = :processoParteSituacaoEnum1");

			queryVerificaParte.setParameter("idUsuario", Authenticator.getIdUsuarioLogado());
			queryVerificaParte.setParameter("idProcessoTrf", idProcessoSelecionado);
			queryVerificaParte.setParameter("processoParteSituacaoEnum1", ProcessoParteSituacaoEnum.A);
			
			boolean isParteProcesso = queryVerificaParte.getResultList().size() > 0;			
			
			Query queryVerificaExpedienteAberto = EntityUtil.getEntityManager().createQuery(
					"select 1 from ProcessoParteExpediente ppe " + 
					"where ppe.processoJudicial.idProcessoTrf = :idProcessoTrf and " + 
					"ppe.pessoaParte.idPessoa = :idUsuario and (ppe.dtCienciaParte is null or ppe.resposta is null)");
			
			queryVerificaExpedienteAberto.setParameter("idUsuario", Authenticator.getUsuarioLogado().getIdUsuario());
			queryVerificaExpedienteAberto.setParameter("idProcessoTrf",idProcessoSelecionado);
			
			boolean possuiExpedienteAberto = queryVerificaExpedienteAberto.getResultList().size() > 0;

			logarAcessoProcessoTerceiro = !(isParteProcesso || possuiExpedienteAberto);
		}
		
		hasAvisoAcessoProcesso = logarAcessoProcessoTerceiro;
		
	}
	
	@SuppressWarnings("unchecked")
	public String getNosAtuaisProcesso(Integer idProcesso){
		
		String returnValue = "";
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from SituacaoProcesso o ");
		sql.append("where o.idProcesso = :idProcesso");
		Query query = em.createQuery(sql.toString());
		query.setParameter("idProcesso", idProcesso);
		List<SituacaoProcesso> lista = query.getResultList();
		for (SituacaoProcesso situacaoProcesso : lista) {
			Integer idTarefa = (situacaoProcesso.getIdTarefa() != null ? situacaoProcesso.getIdTarefa().intValue() : null);
			Tarefa tarefa = em.find(Tarefa.class, idTarefa);
			
			returnValue += tarefa.getTarefa() 
					+ recuperaResponsavelDoNo(situacaoProcesso.getIdLocalizacao(), 
												situacaoProcesso.getIdOrgaoJulgadoColegiado(),
												situacaoProcesso.getIdOrgaoJulgadorCargo()
					) 
					+ ", <BR/>";
		}

		if (returnValue.contains(",")) {
			returnValue = returnValue.substring(0, returnValue.lastIndexOf(","));
		}

		return returnValue;
		
	}
	
	/**
	 * Método responsável por recuperar os
	 * {@link OrgaoJulgador} e {@link OrgaoJulgadorColegiado}
	 * responsáveis pelas tarefa exibidas no campo nós atuais
	 * @param idOj
	 * @param idOjc
	 * @return
	 */
	private String recuperaResponsavelDoNo(Long idLocalizacao, Long idOjc, Integer idOjCargo){
		StringBuilder sb = new StringBuilder();
		Localizacao localizacao = null;
		OrgaoJulgadorColegiado ojc = null;
		OrgaoJulgadorCargo ojCargo = null;
		try {
			if(idLocalizacao != null){
				localizacao = localizacaoManager.findById(idLocalizacao.intValue());
			}
			
			if(idOjc != null){
				ojc = orgaoJulgadorColegiadoManager.findById(idOjc.intValue());
			}
		
			if(idOjCargo != null){
				ojCargo = orgaoJulgadorCargoManager.findById(idOjCargo);
			}

			sb.append(" (");
			boolean haPrimeiroItem = false;
			if(localizacao != null) {
				if(haPrimeiroItem) {
					sb.append("/");
				}
				sb.append(localizacao.getLocalizacao());
				haPrimeiroItem = true;
			}
			if (ojCargo != null){
				if(haPrimeiroItem) {
					sb.append("/");
				}
				sb.append(ojCargo.getDescricao());
				haPrimeiroItem = true;
			}
			if(ojc != null) {
				if(haPrimeiroItem) {
					sb.append("/");
				}
				sb.append(ojc.getOrgaoJulgadorColegiado());
				haPrimeiroItem = true;
			}
			sb.append(") ");
		
			
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public List<Estado> getEstados(){
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from Estado o order by estado");
		Query query = em.createQuery(sql.toString());
		return  query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Eleicao> getEleicaoList() {

		StringBuilder sb = new StringBuilder();
		sb.append("select o from Eleicao o where o.ativo = true ");

		return EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();

	}

	public List<Estado> getListEstadosJurisdicoes() {
		if (this.listEstadosJurisdicoes == null || this.listEstadosJurisdicoes.isEmpty()) {
			this.listEstadosJurisdicoes = ComponentUtil.getComponent(EstadoManager.class).recuperarPorJurisdicaoAtiva();

			if (this.listEstadosJurisdicoes.size() == 1) {
				this.estado = this.listEstadosJurisdicoes.get(0);
			}
		}
		return this.listEstadosJurisdicoes;
	}

	public List<Municipio> getListMunicipioJurisdicoes() {
		return ComponentUtil.getComponent(MunicipioManager.class).recuperarPorEstadoComJurisdicao(this.estado);
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorColegiadoList() {
		
		String papel = Authenticator.getPapelAtual().getIdentificador();
		boolean isAdmin = papel.equalsIgnoreCase("admin") || papel.equalsIgnoreCase("administrador");
		
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o FROM OrgaoJulgadorColegiado o WHERE o.ativo = true ");
		
		if (!isAdmin){
			if (ojc != null) {
				sb.append("AND o.idOrgaoJulgadorColegiado = ");
				sb.append(ojc.getIdOrgaoJulgadorColegiado());
			} else {
				sb.append("AND 1 != 1 ");
			}
		}

		sb.append(" ORDER BY CASE WHEN o.orgaoJulgadorColegiado >= 'A' THEN upper(to_ascii(o.orgaoJulgadorColegiado)) ELSE to_char(to_number(o.orgaoJulgadorColegiado, '999'),'000') END, upper(to_ascii(o.orgaoJulgadorColegiado)) ");
		
		return EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();
	}
	
	/**
	* Método responsável por retornar uma lista de Órgãos Julgadores filtrados por OrgaoJulgadorColegiado
	*  
	* @param ojc Objeto da classe OrgaoJulgadorColegiado. Caso seja nulo, é ignorado.
	* @return Lista de OrgaoJulgador filtrados conforme os valores informados nos parâmetros.
	*/
	public List<OrgaoJulgador> getOrgaoJulgadorListPorOjc(OrgaoJulgadorColegiado ojc) {
		return getOrgaoJulgadorListPorOjcJurisdicao(ojc, null);
	}
	
	/**
	* Método responsável por retornar uma lista com a totalidade Jurisdicoes ativas.
	*  
	* @return Lista de Jurisdicao filtrados com a propriedade "ativo" = true.
	*/
	public List<Jurisdicao> getJurisdicaoList() {
		List<Jurisdicao> result = new ArrayList<>();

		if (this.municipio != null) {
			result = this.jurisdicaoManager.recuperarJurisdicoes(this.municipio);
		} else {
			result = this.jurisdicaoManager.getJurisdicoesAtivas();
		}

		return result;
	}

	/**
	* Método responsável por retornar uma lista de Órgãos Julgadores filtrados por OrgaoJulgadorColegiado e Jurisdicao.
	*  
	* @param  ojc			Objeto da classe OrgaoJulgadorColegiado. Caso seja ojcnulo, é ignorado.
	* @param  jurisdicao	Objeto da classe Jurisdicao. Caso seja nulo, é ignorado.
	* @return Lista de OrgaoJulgador filtrados conforme os valores informados nos parâmetros.
	*/
	public List<OrgaoJulgador> getOrgaoJulgadorListPorOjcJurisdicao(OrgaoJulgadorColegiado ojc, Jurisdicao jurisdicao) {
		return processoJudicialManager.getOrgaoJulgadorListPorOjcJurisdicao(ojc, jurisdicao);
	}
	
	public String getDescricaoParaExibicao(Object selected){
		String selecionado = "";
		if (selected == null || selected.toString() == null){
			return selecionado;
		}
		else{
			if (selected.toString().length() > 25){
				selecionado = selected.toString().substring(0, 25) + "...";
			}
			else{
				selecionado = selected.toString();
			}
			return selecionado;
		}
	}
	
	 /**
  	* Método responsável por limpar os filtros de pesquisa [PJEII-10745]
  	*/
  	public void limparCamposPesquisa() {
		this.nomeParte = null;
		this.documentoParte = null;
		this.estadoOAB = null;
		this.numeroOAB = null;
		this.letraOAB = null;
		this.orgaoColegiado = null;
		this.orgaoJulgador = null;
		this.dataAutuacaoInicio = null;
		this.dataAutuacaoFim = null;
		this.eleicao = null;
		limparMunicipio();
		this.estado = null;
		this.complementoClasse = null;
		this.dsComponenteValidacao = null;
		this.valorCausaInicial = null;
		this.valorCausaFinal = null;
		this.objetoProcesso = null;
		this.numeroDocumento = null;
		this.jurisdicao = null;
		this.nomeAdvogado = null;
		this.tipoArtigo260Enum = null;
		limparNumeroProcesso();
		this.orgaoProcedimentoOrigemCriminal = null;
		this.numeroProcedimentoCriminal = null;
		this.anoProcedimentoCriminal = null;
		this.numeroProtocoloPolicia = null;
		limparNumeroProcessoReferencia();
		limparMovimentoProcessual();
		limparAssuntoTrf();
		limparClasseJudicial();
		this.outrosNomesAlcunha = null;
	}

  	/**
   	 * Limpa o campo suggest de municpio
   	 */
 	private void limparMunicipio() {
 		this.municipio = null;
 		ConsultaProcessualMunicipioSuggestBean.instance().setDefaultValue(null);
 		ConsultaProcessualMunicipioSuggestBean.instance().setInstance(null);
	}
 	
 	private void limparNumeroProcesso() {
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		atribuirNumeroOrgaoJustica();
		this.numeroOrigem = null;
 	}
 	
 	private void atribuirNumeroOrgaoJustica() {
		String numeroOrgaoJustica = parametroService.valueOf("numeroOrgaoJustica");
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
 	}
 	
	public void limparNumeroProcessoReferencia() {
		this.numeroProcessoReferencia = null;
	}
	
	/**
	 * Limpa o campo suggest de movimento processual.
	 */
	private void limparMovimentoProcessual() {
		this.movimentacaoProcessual = null;
		MovimentoProcessualSuggestBean.instance().setDefaultValue(null);
		MovimentoProcessualSuggestBean.instance().setInstance(null);
	}
  	
	public void limpardsComponenteValidacao(){
		setDsComponenteValidacao(null);
		setDsComponenteValidacaoData(null);
	}

	/**
	 * Limpa as informações de assunto do campo suggest box.
	 */
	public void limparAssuntoTrf() {
		assuntoTrf = null;
	}

	/**
	 * Limpa as informações de classe judicial do campo suggest box.
	 */
	public void limparClasseJudicial() {
		classeJudicial = null;
		ClasseJudicialComCompetenciaSuggestBean.instance().setDefaultValue(null);
		ClasseJudicialComCompetenciaSuggestBean.instance().setInstance(null);
	}
	
	/**
	 * Retorna true se for para exibir a mensagem da resolução 121. 
	 * A mensagem é renderizada pelo componente javascriptMensagemResolucacao121.xhtml.
	 * 
	 * @return booleano
	 */
	public Boolean isExibirMensagemResolucao121() {
		return 	(Identity.instance().hasRole("jusPostulandi") == false) &&
				(getHasAvisoAcessoProcesso()) &&
				(getIdProcessoSelecionado() != null);
	}
	
	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return processoJudicialManager;
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return this.model;
	}

	public String getAssunto() {
		return assuntoTrf != null ? assuntoTrf : null;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Integer getIdProcessoTrf() {
		return idProcessoTrf;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.idProcessoTrf = idProcessoTrf;
	}
	
	public String getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public String getClasse() {
		return classeJudicial != null ? classeJudicial : null;
	}

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public String getDocumentoParte() {
		return documentoParte;
	}

	public void setDocumentoParte(String documentoParte) {
		this.documentoParte = documentoParte;
	}

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public Estado getEstadoOAB() {
		return estadoOAB;
	}

	public void setEstadoOAB(Estado estadoOAB) {
		this.estadoOAB = estadoOAB;
	}

	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	public String getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Date getDataAutuacaoInicio() {
		return dataAutuacaoInicio;
	}
	  

	public void setDataAutuacaoInicio(Date dataAutuacaoInicio) {
		this.dataAutuacaoInicio = dataAutuacaoInicio;
	}
	
	public Date getDataAutuacaoFim() {
		return dataAutuacaoFim;
	}

	public void setDataAutuacaoFim(Date dataAutuacaoFim) {
		this.dataAutuacaoFim = dataAutuacaoFim;
	}

	public OrgaoJulgadorColegiado getOrgaoColegiado() {
		return orgaoColegiado;
	}

	public void setOrgaoColegiado(OrgaoJulgadorColegiado orgaoColegiado) {
		this.orgaoColegiado = orgaoColegiado;
	}

	public Boolean getHasAvisoAcessoProcesso() {
		return hasAvisoAcessoProcesso;
	}

	public void setHasAvisoAcessoProcesso(Boolean hasAvisoAcessoProcesso) {
		this.hasAvisoAcessoProcesso = hasAvisoAcessoProcesso;
	}

	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}
	
	public String getRamoJustica() {
		return ramoJustica;
	}
	
	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	public Eleicao getEleicao() {
		return eleicao;
	}

	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}

	public Estado getEstado() {
		return estado;
	}


	/*
	PJEII-10234 - método que é setado ao se trocar o valor de estado na pesquisa.
	Caso exista um municipio deste estado setado e se troque o estado, o campo municipio
	deve ser apagado
	 */	

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				((ConsultaProcessualMunicipioSuggestBean) Component.getInstance("consultaProcessualMunicipioSuggest")).setInstance(null);
			}
		}
		this.estado = estado;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public ComplementoClasse getComplementoClasse() {
		return complementoClasse;
	}

	public void setComplementoClasse(ComplementoClasse complementoClasse) {
		this.complementoClasse = complementoClasse;
	}

	public String getDsComponenteValidacao() {
		return dsComponenteValidacao;
	}

	public void setDsComponenteValidacao(String dsComponenteValidacao) {
		this.dsComponenteValidacao = dsComponenteValidacao;
	}

	public String getDsComponenteValidacaoData() {
		return dsComponenteValidacaoData;
	}

	public void setDsComponenteValidacaoData(String dsComponenteValidacaoData) {
		this.dsComponenteValidacaoData = dsComponenteValidacaoData;
	}

	public List<ComplementoClasse> getComplementoClasseList() {
		return complementoClasseList;
	}
	
	public void setComplementoClasseList(
			List<ComplementoClasse> complementoClasseList) {
		this.complementoClasseList = complementoClasseList;
	}

	public Double getValorCausaInicial() {
		return valorCausaInicial;
	}
	
	public Double getValorCausaInicialTeste() {
		if(valorCausaInicial == null) return 0D;
		return valorCausaInicial;
	}

	public void setValorCausaInicial(Double valorCausaInicial) {
		this.valorCausaInicial = valorCausaInicial;
	}

	public Double getValorCausaFinal() {
		return valorCausaFinal;
	}

	public void setValorCausaFinal(Double valorCausaFinal) {
		this.valorCausaFinal = valorCausaFinal;
	}

	public String getObjetoProcesso() {
		return objetoProcesso;
	}

	public void setObjetoProcesso(String objetoProcesso) {
		this.objetoProcesso = objetoProcesso;
	}

	public String getNumeroProcessoReferencia() {
		return numeroProcessoReferencia;
	}

	public void setNumeroProcessoReferencia(String numeroProcessoReferencia) {
		this.numeroProcessoReferencia = numeroProcessoReferencia;
	}

	public Boolean getAplicarMascaraProcessoReferencia() {
		return aplicarMascaraProcessoReferencia;
	}

	public void setAplicarMascaraProcessoReferencia(
			Boolean aplicarMascaraProcessoReferencia) {
		this.aplicarMascaraProcessoReferencia = aplicarMascaraProcessoReferencia;
	}

	public Jurisdicao getJurisdicao() {
		return jurisdicao;
	}

	public void setJurisdicao(Jurisdicao jurisdicao) {
		this.jurisdicao = jurisdicao;
	}
	
	public Evento getMovimentacaoProcessual() {
		return movimentacaoProcessual;
	}

	public void setMovimentacaoProcessual(Evento movimentacaoProcessual) {
		this.movimentacaoProcessual = movimentacaoProcessual;
	}
	
	/**
	 * Método responsável por definir o título do link na Consulta de Processos.
	 * Se o usuário atual possuir o papel (
	 * {@link Papeis#PROCESSO_OBJETO_VISUALIZADOR}) para visualizar o objeto
	 * processual e o processo contiver o objeto, este será mostrado. Caso tenha
	 * o papel e o processo não tenha objeto, será mostrado o número processual
	 * 
	 * @return <code>String</code>, o objeto do processo ou o número processual,
	 *         a depender do papel do usuário atual.
	 */
	public String tituloLinkProcesso() {
		String retorno = model.current.getNumeroProcesso();
		if (Identity.instance().hasRole(Papeis.PROCESSO_OBJETO_VISUALIZADOR)) {
			String objetoProcesso = model.current.getObjeto();
			if (StringUtil.isNotEmpty(objetoProcesso)) {
				retorno = objetoProcesso;
			}
		}
		return retorno;
	}
	
	public TipoArtigo260Enum[] getTipoArtigo260EnumList() {
 		return TipoArtigo260Enum.values();
	}

	public TipoArtigo260Enum getTipoArtigo260Enum() {
		return tipoArtigo260Enum;
	}

	public void setTipoArtigo260Enum(TipoArtigo260Enum tipoArtigo260Enum) {
		this.tipoArtigo260Enum = tipoArtigo260Enum;
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public boolean getIsVisualizaNoTarefa() {
		return Authenticator.isUsuarioInterno();
	}

	public String getNumeroProcedimentoCriminal() {
		return numeroProcedimentoCriminal;
	}

	public void setNumeroProcedimentoCriminal(String numeroProcedimentoCriminal) {
		this.numeroProcedimentoCriminal = numeroProcedimentoCriminal;
	}

	public Integer getAnoProcedimentoCriminal() {
		return anoProcedimentoCriminal;
	}

	public void setAnoProcedimentoCriminal(Integer anoProcedimentoCriminal) {
		this.anoProcedimentoCriminal = anoProcedimentoCriminal;
	}

	public OrgaoProcedimentoOriginarioDTO getOrgaoProcedimentoOrigemCriminal() {
		return orgaoProcedimentoOrigemCriminal;
	}

	public void setOrgaoProcedimentoOrigemCriminal(OrgaoProcedimentoOriginarioDTO orgaoProcedimentoOrigemCriminal) {
		this.orgaoProcedimentoOrigemCriminal = orgaoProcedimentoOrigemCriminal;
	}
	
	public String getNumeroProtocoloPolicia() {
		return numeroProtocoloPolicia;
	}

	public void setNumeroProtocoloPolicia(String numeroProtocoloPolicia) {
		this.numeroProtocoloPolicia = numeroProtocoloPolicia;
	}

	public List<OrgaoProcedimentoOriginarioDTO> getOrgaoList() {
		return orgaoProcedimentoOriginarioList;
	}

	public void setOrgaoList(List<OrgaoProcedimentoOriginarioDTO> orgaoList) {
		this.orgaoProcedimentoOriginarioList = orgaoList;
	}

	public String getOutrosNomesAlcunha() {
		return outrosNomesAlcunha;
	}

	public void setOutrosNomesAlcunha(String outrosNomesAlcunha) {
		this.outrosNomesAlcunha = outrosNomesAlcunha;
	}
	
}
