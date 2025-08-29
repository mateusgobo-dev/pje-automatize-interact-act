package br.jus.cnj.pje.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.faces.event.PhaseId;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.FacesLifecycle;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessualManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.service.ReCaptchaService;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name("consultaPublicaAction")
@Scope(ScopeType.EVENT)
public class ConsultaPublicaAction extends BaseAction<ProcessoTrf>{

	private static final long serialVersionUID = 1L;
	private Integer idProcessoTrf;
	private String numeroProcesso;
	private String nomeParte;
	private String nomeAdvogado;
	private ClasseJudicial classe;
	private String documentoParte;
	private Estado estadoOAB;
	private String numeroOAB;
	private String letraOAB;
	private String chaveAcesso;
	private String numeroProcessoReferencia;
	
	private EntityDataModel<ProcessoTrf> model;
	
	@RequestParameter(value="processosGridCount")
	private Integer processosGridCount;
	
	@RequestParameter(value="idProcessoSelecionado")
	private Integer idProcessoSelecionado;
	
	@In
	private ConsultaProcessualManager consultaProcessualManager;
	
	@Create
	public void Init(){
		//Para não executar a grid ao entrar na página a primeira vez
		if (FacesLifecycle.getPhaseId() != PhaseId.RENDER_RESPONSE){
			model = new EntityDataModel<ProcessoTrf>(ProcessoTrf.class, super.facesContext, getRetriever());
		}
	}
	
	public String gerarChaveAcessoProcesso(Integer idProcesso){
		Crypto c = new Crypto(ProjetoUtil.getChaveCriptografica());
		HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
		String chaveCodificada = c.encodeDES((idProcesso + ":" + session.getId()));
		return chaveCodificada;
	}

	public void pesquisar() {
		try {
			if (ParametroUtil.instance().isReCaptchaAtivo() && 
					!ReCaptchaService.instance().validarResposta((String)Util.getRequestParameter("g-recaptcha-response"))) {
				
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "captcha.invalidCaptcha");
			} else {
				model = new EntityDataModel<ProcessoTrf>(ProcessoTrf.class, super.facesContext, getRetriever());
				
				List<Criteria> criterios = new ArrayList<Criteria>(0);
				criterios.addAll(getCriteriosTelaPesquisa());
				criterios.addAll(getCriteriosNegociais());
				
				HashMap<String, Order> ordenacao = new HashMap<>();
				ordenacao.put("o.ano", Order.ASC);
				ordenacao.put("o.numeroSequencia", Order.ASC);

				model = consultaProcessualManager.pesquisar(criterios, ordenacao, 30);

				if(model.getRowCount() == 0){
					facesMessages.add(Severity.ERROR, "Sua pesquisa não encontrou nenhum processo disponível.");
				}
			}	
		} catch (Exception e) {
			model = null;
			facesMessages.add(Severity.ERROR, e.getMessage());
		}
	}
	
	/**
	 * Metodo responsavel por informar ao xhtml se o numero de registros encontrados
	 * a partir do filtro  da pesquisa eh maior do que o maximo.
	 * @return
	 */
	public boolean isNumeroMaximoDeRegistrosExcedido() {
		return consultaProcessualManager.isNumeroMaximoDeRegistrosExcedido();
	}

	
	private List<Criteria> getCriteriosNegociais() throws Exception{
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.add(Criteria.equals("processoStatus", ProcessoStatusEnum.D));		
		criterios.add(Criteria.equals("segredoJustica",false));
		return criterios;
	}
	
	/**
	 * Adiciona os critérios de pesquisa de acordo com as regras de negócio
	 * 
	 * @return {@link List} de {@link Criteria} com as condições de pesquisa.
	 */
	private List<Criteria> getCriteriosTelaPesquisa() throws Exception{
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		if (StringUtils.isNotBlank(numeroProcesso)) {
			criterios.add(Criteria.equals("processo.numeroProcesso", numeroProcesso));
		}
		
		if (StringUtils.isNotBlank(numeroProcessoReferencia)) {
			criterios.add(Criteria.equals("desProcReferencia", numeroProcessoReferencia));
		}
		 
		ProcessoParteParticipacaoEnum[] tiposPartePesquisa = {ProcessoParteParticipacaoEnum.A,ProcessoParteParticipacaoEnum.P};
		ProcessoParteSituacaoEnum[] situacoesPartePesquisa = {ProcessoParteSituacaoEnum.A,ProcessoParteSituacaoEnum.S};
		
		nomeParte = nomeParte.trim();
		if (StringUtils.isNotBlank(nomeParte) || StringUtils.isNotBlank(documentoParte)) {
			if (StringUtils.isNotBlank(nomeParte) && nomeParte.split(" ").length <= 1) {
				throw new Exception("É necessário informar ao menos dois nomes para realizar a consulta por nome da parte.");
			}
			
			if (StringUtils.isNotBlank(documentoParte) && !InscricaoMFUtil.validarCpfCnpj(documentoParte)) {
				throw new Exception("Documento de identificação inválido");
			}
			//Adciona criterios para pesquisa do nome e documento
			ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
			criterios.addAll(processoTrfManager.getCriteriasPesquisaNomeDocumento(nomeParte.trim(), documentoParte));
			
			criterios.add(Criteria.in("processoParteList.inParticipacao", tiposPartePesquisa));
			criterios.add(Criteria.in("processoParteList.inSituacao", situacoesPartePesquisa));
			PessoaFisicaManager pessoaFisicaManager = ComponentUtil.getComponent(PessoaFisicaManager.class);
			List<Integer> menores = pessoaFisicaManager.getMenores(nomeParte, documentoParte);			
			if(menores != null && !menores.isEmpty()) {
				//exclui os menores de idade da pesquisa
				criterios.add(Criteria.not(Criteria.in("processoParteList.pessoa.idPessoa", menores.toArray())));
			}
		}
		
		if (StringUtils.isNotBlank(numeroOAB) || StringUtils.isNotBlank(nomeAdvogado)) {			
			criterios.add(Criteria.equals("processoParteList.processoParteRepresentanteList.tipoRepresentante.idTipoParte", 
					ParametroUtil.instance().getTipoParteAdvogado().getIdTipoParte()));
			
			criterios.add(Criteria.in("processoParteList.inParticipacao", tiposPartePesquisa));					
			
			criterios.add(Criteria.in("processoParteList.processoParteRepresentanteList.inSituacao", situacoesPartePesquisa));
			
			nomeAdvogado = nomeAdvogado.trim(); 
			if (StringUtils.isNotBlank(nomeAdvogado)) {
				if (nomeAdvogado.split(" ").length <= 1) {
					throw new Exception("É necessário informar ao menos dois nomes para realizar a consulta por nome do advogado.");
				}
				criterios.add(Criteria.contains(
					"processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.nome", nomeAdvogado));
			}

			if (estadoOAB != null || !Strings.isEmpty(numeroOAB)){
				criterios.add(Criteria.equals("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","OAB"));
				if (estadoOAB != null) {
					criterios.add(Criteria.equals("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.estado.idEstado",estadoOAB.getIdEstado()));
				}
				if (StringUtils.isNotBlank(numeroOAB)) {
					String numeroFinal = numeroOAB;
					if (StringUtils.isNotBlank(letraOAB)) {
						numeroFinal += "-" + letraOAB;
					}
					criterios.add(Criteria.equals("processoParteList.processoParteRepresentanteList.representante.pessoaDocumentoIdentificacaoList.numeroDocumento",numeroFinal));
				}
			}
		}

		if (classe != null){
			criterios.add(Criteria.equals("classeJudicial.idClasseJudicial",classe.getIdClasseJudicial()));
		}
		
		if (criterios.size() == 0){
			throw new Exception("Pelo menos um dos critérios de pesquisa deve ser informado.");
		}
		
		return criterios;
	}
	
	public void tratarVerificacaoChaveAcesso() throws PJeBusinessException{
		Integer idProc = SecurityTokenControler.instance().verificaChaveAcessoConsultaPublica();
		Identity identity = ComponentUtil.getComponent(Identity.class);
		if (idProc == 0 && identity.isLoggedIn()) {
			idProc = SecurityTokenControler.instance().verificaChaveAcesso();
		}
		if (idProc != null && idProc != 0) {
			ProcessoTrfHome.instance().setProcessoTrfIdProcessoTrf(idProc);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Estado> getEstados(){
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from Estado o order by estado");
		Query query = em.createQuery(sql.toString());
		return  query.getResultList();
	}
	
	@SuppressWarnings("rawtypes")
	public void selecionarClasse(NodeSelectedEvent event) {
        HtmlTree tree = (HtmlTree) event.getComponent();
        classe = (ClasseJudicial)((br.com.infox.component.tree.EntityNode) tree.getRowData()).getEntity();
    }
	
	public String getDescricaoParaExibicao(Object selected){
		String selecionado = "";
		if (selected == null || selected.toString() == null){
			return selecionado;
		} else {
			if (selected.toString().length() > 25){
				selecionado = selected.toString().substring(0, 25) + "...";
			} else {
				selecionado = selected.toString();
			}
			return selecionado;
		}
	}
	
	public String getDescricaoUltimoEvento(Integer idProcesso) {
		try{
			ProcessoEventoManager processoEventoManager = ComponentUtil.getComponent(ProcessoEventoManager.class);
			ProcessoEvento movimento = processoEventoManager.recuperaUltimaMovimentacaoPublica(idProcesso, new Date());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			if(movimento != null){
				return movimento.getTextoFinalExterno() + " (" + sdf.format(movimento.getDataAtualizacao()) + ")";
			}
		}catch (PJeBusinessException e){
			facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a movimentação mais recente.");
		}
		return null;
	}
	
	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class);
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return this.model;
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
	
	public ClasseJudicial getClasse() {
		return classe;
	}

	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}

	public String getDocumentoParte() {
		return documentoParte;
	}

	public void setDocumentoParte(String documentoParte) {
		this.documentoParte = documentoParte;
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

	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}
	
	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNomeAdvogado() {
		return nomeAdvogado;
	}

	public void setNomeAdvogado(String nomeAdvogado) {
		this.nomeAdvogado = nomeAdvogado;
	}

	public String getChaveAcesso() {
		return chaveAcesso;
	}

	public void setChaveAcesso(String chaveAcesso) {
		this.chaveAcesso = chaveAcesso;
	}

	public String getNumeroProcessoReferencia() {
		return numeroProcessoReferencia;
	}

	public void setNumeroProcessoReferencia(String numeroProcessoReferencia) {
		this.numeroProcessoReferencia = numeroProcessoReferencia;
	}
	
}
