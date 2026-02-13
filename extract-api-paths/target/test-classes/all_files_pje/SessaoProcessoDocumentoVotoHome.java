package br.com.infox.cliente.home;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.list.SessaoPautaRelacaoJulgamentoList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.DerrubadaVotoManager;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJulgamentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.ProcessoDocumentoStatusEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;
import br.jus.pje.nucleo.enums.VotoStatusEnum;

@Name(SessaoProcessoDocumentoVotoHome.NAME)
public class SessaoProcessoDocumentoVotoHome extends AbstractHome<SessaoProcessoDocumentoVoto> implements ArquivoAssinadoUploader{
	
	private static final long serialVersionUID = -3508264905072509596L;

	private DocumentoJudicialService documentoJudicialService;
	
	private SessaoJulgamentoManager sessaoJulgamentoManager;
	
	private DerrubadaVotoManager derrubadaVotoManager;
	
	private ParametroService parametroService;
	
	@Logger
	private Log logger;

	@In
	private SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

	public static final String NAME = "sessaoProcessoDocumentoVotoHome";
	private static final LogProvider log = Logging.getLogProvider(SessaoProcessoDocumentoVotoHome.class);
	
	private Map<OrgaoJulgador,Boolean> votosMarcados = new HashMap<OrgaoJulgador, Boolean>();

	private String modeloDocumento;
	private SessaoPautaProcessoTrf sessaoPauta;
	private Sessao sessaoCorrente;
	private boolean votoMagistradoAssinado = false;
	private boolean exibeDivVoto = false;
	private boolean sessaoFinalizada = false;
	private boolean sessaoEncerrada = false;
	private VotoStatusEnum tipoStatusVoto;
	private boolean assinado = false;
	private Boolean divVoto = Boolean.FALSE;
	private Boolean flagVisualLiberaVoto = null;
	private ArquivoAssinadoHash arquivoAssinadoHash; 

	private ProcessoDocumentoStatusEnum tipoStatusEmenta;
	private boolean ementaAssinada;
	private ProcessoTrf processo;
	
	private TipoVoto votoAntigo;
	
	private Map<SessaoPautaProcessoTrf,SessaoProcessoDocumentoVoto> mapaVoto = new HashMap<SessaoPautaProcessoTrf,SessaoProcessoDocumentoVoto>(3); 

	private ModeloProclamacaoJulgamento modeloProclamacaoJulgamento;
	
	public static SessaoProcessoDocumentoVotoHome instance() {
		return (SessaoProcessoDocumentoVotoHome) Component.getInstance(NAME);
	}

	@Override
	public void create() {
		documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
		sessaoJulgamentoManager = ComponentUtil.getSessaoJulgamentoManager();
		derrubadaVotoManager = ComponentUtil.getDerrubadaVotoManager();
		parametroService = ComponentUtil.getParametroService();
		
		super.create();
	}
	
	@Override
	public void newInstance() {
		setAssinado(false);
		setSessaoPauta(null);
		setVotoMagistradoAssinado(false);
		setExibeDivVoto(Boolean.FALSE);
		super.newInstance();
		ProcessoDocumentoHome.instance().newInstance();
		ProcessoDocumentoBinHome.instance().newInstance();
	}
	
	/**
	 * Metodo que verifica o parametro pje:sessao:ocultar:VotacaoAntecipadaRelacaoJulgamento que verifica se apresenta ou nao
	 * a aba de votacao antecipada.
	 * @return True se estiver vazio ou com valor verdadeiro
	 */
	public Boolean isApresentaAbaVotacaoAntecipadaRelacaoJulgamento(){
		String verificacao = parametroService.valueOf(Parametros.PJE_SESSAO_NAO_EXIBE_VOTACAO_ANTECIPADA_RELACAO_JULGAMENTO);
		boolean retorno = true;
		if (!StringUtils.isEmpty(verificacao)){
			retorno = Boolean.parseBoolean(verificacao);
		}
		return retorno;
	}
	

	/**
	 * Preenche o voto no formulario da relação de julgamento
	 * 
	 * @param p
	 *            SessaoPautaProcessoTrf esperado.
	 * @param votacaoAntecipada
	 *            Indica se o voto a ser preenchido veio da aba votação
	 *            antecipada ou relação de julgamento.
	 */
	public void voto(SessaoPautaProcessoTrf p, boolean votacaoAntecipada) {
		newInstance();
		verificaStatusSessao(p);
		setVotoMagistradoAssinado(false);
		modeloDocumento = null;
		sessaoPauta = p;
		sessaoCorrente = p.getSessao();
		SessaoProcessoDocumentoHome.instance().setDivVoto(true);
		SessaoProcessoDocumentoHome.instance().setDivRelatorio(false);
		SessaoProcessoDocumentoHome.instance().setDivEmenta(false);
		/**
		 * Ao selecionar Modelo de Documento ,menu: Audiências e sessões > Relação de julgamento >
		 * SessaoPopUp > aba Relação de julgamento, o sistema gera erro no modelo
		 * carregado no editor. O mesmo ocorre ao alternar entre as opções 
		 * Voto,Ementa e Relatório.
		 */
		Contexts.getPageContext().set("idProcessoTrf", p.getProcessoTrf().getIdProcessoTrf());
		if (votacaoAntecipada) {
			setInstance(getVotoMagistradoVog(p));
		} else {
			setInstance(getVotoMagistrado(p));
		}
		if (instance != null && getInstance().getProcessoDocumento() != null) {
			modeloDocumento = getInstance().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
			verificaAssinaturaMagi(instance);
			ProcessoDocumentoHome.instance().setInstance(
					EntityUtil.find(ProcessoDocumento.class, getInstance().getProcessoDocumento().getIdProcessoDocumento()));
			ProcessoTrfHome.instance().setInstance(p.getProcessoTrf());
		} else {
			ProcessoDocumentoHome.instance().newInstance();
			ProcessoDocumentoBinHome.instance().newInstance();
			ProcessoDocumentoHome.instance().getInstance()
					.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		}
		cacheSessaoVotos.clear();
	}

	public void carregaAbaAptosInclusaoPauta() {
		limpaSessaoPauta();
		if (null != SessaoHome.instance().getInstance().getDataMaxIncProcPauta()) {
			if (!SessaoHome.instance().permiteInclusaoPauta()) {
				DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				String dataFormatada = format.format(SessaoHome.instance().getInstance().getDataMaxIncProcPauta());
				FacesMessages.instance().addToControl(
						"msgs",
						"Atingiu o prazo máximo para inclusão de processos de Pauta de Julgamento. O prazo máximo foi dia "
								+ dataFormatada);
			}
		}
	}

	/**
	 * Limpa o valor da sessaoPauta
	 */
	public void limpaSessaoPauta() {
        SessaoPautaRelacaoJulgamentoList list = (SessaoPautaRelacaoJulgamentoList) Component.getInstance(SessaoPautaRelacaoJulgamentoList.class);
        for(SessaoPautaProcessoTrf processoPauta : list.list()){
            getEntityManager().refresh(processoPauta);
        }

		setSessaoPauta(null);
	}

	/**
	 * Define o orgão julgador acompanhado do voto.
	 * 
	 * @param votacaoAntecipada
	 *            - flag para verificar se a chamada do metodo veio da aba
	 *            votação antecimada.
	 */
	public void defineOjAcompanhado(boolean votacaoAntecipada) {
		// verifica se a chamada do metodo veio de votação antecipada
		if (!votacaoAntecipada) {
			getInstance().setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		} else {
			// verifica se acompanha o relator
			if (verificaConcordaRel()) {
				
				// pega o orgao julgador do voto do magistrado relator que foi
				// setado na sessao pauta
				//OrgaoJulgador ojAcomTemp = getVotoMagistrado(this.sessaoPauta).getOrgaoJulgador();
				getInstance().setOjAcompanhado(this.sessaoPauta.getProcessoTrf().getOrgaoJulgador());
				// se ele não acompanhar o relator
			} else {
				// verifica se teve apenas um item marcado
				if (getMarcados().size() == 1) {
					getInstance().setOjAcompanhado(getMarcados().get(0));
					// se marcaou algo diferente de um na lista de acompanhar
					// votos ele seta o ojAcompanhadao como sendo o dele mesmo
				} else {
					getInstance().setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
				}
			}
		}
		cacheSessaoVotos.clear();
	}

	private List<OrgaoJulgador> getMarcados() {
		return new ArrayList<OrgaoJulgador>(votosMarcados.keySet());
	}

	/**
	 * Pega uma SessaoProcessoDocumentoVoto e verifica se ele foi alterado antes
	 * da data de alteração do SessaoProcessoDocumentoVotoHome passado na
	 * chamada do método.
	 * 
	 * @param rowSPDV
	 *            SessaoProcessoDocumentoVoto esperado para fazer a comparação
	 *            com o SessaoProcessoDocumentoVoto contido no instancede
	 *            SessaoProcessoDocumentoVotoHome.
	 * 
	 * @return Retorna "true" caso a data de alteração do
	 *         SessaoProcessoDocumentoVoto pego no parametro for maior que o
	 *         documento contido no instance de SessaoProcessoDocumentoVotoHome.
	 */
	public boolean pintaLinha(SessaoProcessoDocumentoVoto rowSPDV) {
		boolean ret = false;
		// verifica se o usuario logado já efetuou o proprio voto
		if (getInstance().getProcessoDocumento() != null) {
			// verifica se o SessaoProcessoDocumentoVoto ja foi alterado
			if (rowSPDV.getProcessoDocumento().getDataAlteracao() != null) {
				// verifica se a data da alteração do
				// SessaoProcessoDocumentoVotoHome.intance já tem data de
				// alteração
				if (getInstance().getProcessoDocumento().getDataAlteracao() != null) {
					// verifica se a data da alteração do
					// SessaoProcessoDocumentoVoto é maior que a data de
					// alteração do SessaoProcessoDocumentoVotoHome.intance
					if (rowSPDV.getProcessoDocumento().getDataAlteracao()
							.after(getInstance().getProcessoDocumento().getDataAlteracao())) {
						ret = true;
					}
				} else {
					// verifica se a data da alteração do
					// SessaoProcessoDocumentoVoto é maior que a data de
					// inclusão do SessaoProcessoDocumentoVotoHome.intance
					if (rowSPDV.getProcessoDocumento().getDataAlteracao()
							.after(getInstance().getProcessoDocumento().getDataInclusao())) {
						ret = true;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Cadastra o voto do magistrado no caso de ele ainda não ter elaborado o
	 * voto.
	 */
	public void cadastrarVotoMagistrado(boolean votacaoAntecipada) {

		if (((Pessoa.instanceOf(Authenticator.getUsuarioLogado(), PessoaMagistrado.class)))
				|| Authenticator.isPapelAssessor()
				|| Authenticator.isMagistrado()) {

			String ret = null;

			try {
				if(getModeloDocumento() != null){
					ProcessoDocumento pd = new ProcessoDocumento();
					pd.setProcessoDocumento("Voto do magistrado");
					pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
					pd.setDataInclusao(new Date());
					pd.setProcesso(getSessaoPauta().getProcessoTrf().getProcesso());
					pd.setProcessoTrf(getSessaoPauta().getProcessoTrf());
					pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
					pd.getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
					pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
					getInstance().setProcessoDocumento(pd);
					documentoJudicialService.persist(getInstance().getProcessoDocumento(), true);
				}
				getInstance().setProcessoTrf(getSessaoPauta().getProcessoTrf());
				defineOjAcompanhado(votacaoAntecipada);
				getInstance().setSessao(sessaoCorrente);
				getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());

				ret = persist();
				mapaVoto.remove(getSessaoPauta());

			} catch (Exception e) {
				log.error(e.getMessage());
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o documento.");
				ret = null;
			}
			if (ret != null) {
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, "Voto cadastrado com sucesso.");
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "O usuário atual não é magistrado ou assessor.");
			return;
		}
		cacheSessaoVotos.clear();
	}

	/**
	 * Verifica se o votante concorda com o relator
	 * 
	 * @return
	 */
	public boolean verificaConcordaRel() {
		boolean ret = false;
		if (getInstance().getTipoVoto() != null && getInstance().getTipoVoto().getContexto().equalsIgnoreCase("C")) {
			return true;
		}
		return ret;
	}

	/**
	 * Assina documento voto do magistrado relator na aba relação de julgamento.
	 */
	public void assinarVotoMagistrado(boolean votacaoAntecipada) {
		// verifica se o editor tem conteudo para permitir assinatura
		if (Strings.isEmpty(modeloDocumento)) {
			getInstance().setLiberacao(flagVisualLiberaVoto);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do documento, é necessário que tenha algum conteúdo no editor.");
			return;
		}
		if (getInstance().getProcessoDocumento() != null) {
			try {
				updateVotoMagistrado(true);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} else {
			try {
				cadastrarVotoMagistrado(votacaoAntecipada);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
		if(arquivoAssinadoHash == null){
			getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(ProcessoDocumentoBinHome.instance().getCertChain());
			getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(ProcessoDocumentoBinHome.instance().getSignature());
		} else {
			getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
			getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinadoHash.getAssinatura());
		}
		
		try{
			documentoJudicialService.finalizaDocumento(getInstance().getProcessoDocumento(), getInstance().getProcessoTrf(), null, false, true, false, Authenticator.getPessoaLogada(), false);
			FacesMessages.instance().add(Severity.INFO,"Documento assinado com sucesso");
			EntityUtil.getEntityManager().flush();
			setVotoMagistradoAssinado(true);
			setFlagVisualLiberaVoto(null);
		}
		catch(Exception e){
			FacesMessages.instance().clear();
			reportMessage(e);
			log.error(e.getMessage());
			//assinado = false;
		}
		
		cacheSessaoVotos.clear();
	}

	/**
	 * Retorna a lista de enum do status do voto
	 * 
	 * @return VotoStatusEnum[]
	 */
	public VotoStatusEnum[] getVotoStatusEnumValues() {
		VotoStatusEnum[] votoStatus = new VotoStatusEnum[3];
		votoStatus[0] = VotoStatusEnum.NN;
		votoStatus[1] = VotoStatusEnum.FN;
		votoStatus[2] = VotoStatusEnum.FL;
		return votoStatus;
	}

	/**
	 * Verifica a partir de um SessaoPautaProcessoTrf se a sessão esta encerrada
	 * e ou finalizada
	 */
	public void verificaStatusSessao(SessaoPautaProcessoTrf sppt) {
		// verifica se a sessão está encerrada
		SessaoPautaProcessoTrf spp = EntityUtil.find(SessaoPautaProcessoTrf.class, sppt.getIdSessaoPautaProcessoTrf());
		if (spp.getSessao().getDataRealizacaoSessao() != null && spp.getSessao().getDataFechamentoSessao() == null) {
			this.setSessaoEncerrada(Boolean.TRUE);
		}
		// verifica se a sessão está finalizada
		if (spp.getSessao().getDataFechamentoSessao() != null) {
			this.setSessaoFinalizada(Boolean.TRUE);
		}
	}

	/**
	 * Atualizar voto do magistrado.
	 */
	public void updateVotoMagistrado(){
		updateVotoMagistrado(false);
	}
	
	public void updateVotoMagistrado(boolean votacaoAntecipada) {
		cacheSessaoVotos.clear();
		
		if(getInstance().getProcessoDocumento() == null){
			if(getModeloDocumento() != null){
				ProcessoDocumento pd = new ProcessoDocumento();
				pd.setProcessoDocumento("Voto do magistrado");
				pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
				pd.setDataInclusao(new Date());
				pd.setProcesso(getSessaoPauta().getProcessoTrf().getProcesso());
				pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
				pd.getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
				pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);
				getInstance().setProcessoDocumento(pd);
			}
		} else{
			if(modeloDocumento == null){
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.ERROR, "Voto não pode estar vazio.");
				return;
			}
			this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(modeloDocumento);
		}
		defineOjAcompanhado(votacaoAntecipada);
		update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Voto alterado com sucesso.");
	}
	
	public void deleteVoto() throws PJeBusinessException {
		SessaoProcessoDocumentoVoto spdv = getInstance();
		ProcessoDocumento pd = spdv.getProcessoDocumento();
		
		liberarRelatorioEmenta(Boolean.FALSE);
		
		List<SessaoProcessoDocumentoVoto> votosAcompanhantes = sessaoProcessoDocumentoVotoManager
				.getVotosAcompanhantes(spdv, spdv.getOrgaoJulgador());
		for (SessaoProcessoDocumentoVoto vot : votosAcompanhantes) {
			vot.setOjAcompanhado(vot.getOrgaoJulgador());
			sessaoProcessoDocumentoVotoManager.persist(vot);
		}
		derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(spdv);
		
		sessaoProcessoDocumentoVotoManager.remove(spdv);
		if(pd != null) {
			ProcessoDocumentoBinManager.instance().remove(pd.getProcessoDocumentoBin());
			ProcessoDocumentoManager.instance().remove(pd);
		}
	
		newInstance();
		setInstance(null);
	}
	
	public void deletarVoto() {
		try {
			deleteVoto();
			
			getEntityManager().flush();
			getEntityManager().clear();
			
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Voto removido com sucesso.");
		} catch (PJeBusinessException e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover voto.");
		}
	}

	/**
	 * Veirifica se tem assinatura do magistrado.
	 * 
	 * @param spdv
	 * @return
	 */
	public Boolean verificaAssinaturaMagi(SessaoProcessoDocumentoVoto spdv) {
		if (spdv != null) {
			if (!spdv.getProcessoDocumento().getProcessoDocumentoBin().getSignatarios().isEmpty()) {
				setVotoMagistradoAssinado(true);
				return true;
			} else {
				setVotoMagistradoAssinado(false);
				return false;
			}
		} else {
			setVotoMagistradoAssinado(false);
			return false;
		}
	}

	/**
	 * Remove o voto do magistrado daquele processo
	 */
	public void removerVotoMagistrado() {
		ProcessoDocumento pdVoto = getInstance().getProcessoDocumento();
		try {
			// inativa o processo documento
			ProcessoDocumentoHome.instance().setInstance(pdVoto);
			ProcessoDocumentoHome.instance().inactive();
			// inativa o sessao processo documento voto
			getInstance().getProcessoDocumento().setAtivo(false);
			getInstance().setProcessoDocumento(null);
			getEntityManager().merge(getInstance());
			
			ProcessoDocumentoBinHome.instance().setAssinado(false);
			ProcessoDocumentoBinHome.instance().setSignature("");
			ProcessoDocumentoBinHome.instance().setCertChain("");
			
			derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(getInstance());
			
			getEntityManager().flush();

			newInstance();

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Voto removido com sucesso.");
		} catch (Exception e) {
			log.error(e.getMessage());
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover o processo.");
		}
		cacheSessaoVotos.clear();
	}

	/**
	 * Pegar o modelo do documento a partir do processo documento bin
	 * Seta o id do processoTrf na ProcessoTrfHome
	 */
	public void processarModelo() {
		// É necessário setar o id do processo Trf no processoTrfHome para que expressões regulares de modelos genéricos funcionem. (ex.: vide modelo de documento de voto genérico)
		ProcessoTrfHome.instance().setId(getSessaoPauta().getProcessoTrf().getIdProcessoTrf());
		ProcessoDocumentoHome.instance().processarModelo();
		modeloDocumento = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
	}

	/**
	 * Retorna o voto do relator na sessão.
	 * 
	 * @param sessaoPPT
	 * @return Retorna o voto do relator daquele processo na sessão.
	 */
	public SessaoProcessoDocumentoVoto getVotoMagistrado(SessaoPautaProcessoTrf sessaoPPT) {
		return getVotoMagistrado(sessaoPPT,Authenticator.getOrgaoJulgadorAtual());
	}
	
	public SessaoProcessoDocumentoVoto getVotoMagistrado(SessaoPautaProcessoTrf sessaoPPT,OrgaoJulgador oj) {
		if (sessaoPPT==null){
			return null;
		}

		if(mapaVoto.containsKey(sessaoPPT)){
			return mapaVoto.get(sessaoPPT);
		}
		StringBuilder sb = new StringBuilder("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.sessao = :sessao ");
		sb.append("and o.processoTrf = :processo ");
		sb.append("and o.orgaoJulgador = :oj ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", sessaoPPT.getSessao());
		query.setParameter("processo", sessaoPPT.getProcessoTrf());
		query.setParameter("oj", oj);

		SessaoProcessoDocumentoVoto voto = EntityUtil.getSingleResult(query);
		SessaoProcessoDocumentoManager manager = ComponentUtil.getComponent("sessaoProcessoDocumentoManager");

		if (voto != null
				&& manager.documentoInclusoAposProcessoJulgado(voto.getDtVoto(), sessaoPPT.getProcessoTrf()
						.getProcesso())) {
			mapaVoto.put(sessaoPPT, voto);
			return voto;
		} else {
			mapaVoto.put(sessaoPPT, null);
			return null;
		}
	}

	/**
	 * Retorna o voto do vogal na sessão.
	 * 
	 * @param sessaoPPT
	 * @return Retorna o voto do vogal daquele processo na sessão.
	 */
	public SessaoProcessoDocumentoVoto getVotoMagistradoVog(SessaoPautaProcessoTrf sessaoPPT) {
		StringBuilder sb = new StringBuilder("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.orgaoJulgador = :oj ");
		sb.append("and o.sessao = :sessao ");
		sb.append("and o.processoTrf = :processo ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", sessaoPPT.getSessao());
		query.setParameter("processo", sessaoPPT.getProcessoTrf());
		query.setParameter("oj", Authenticator.getOrgaoJulgadorAtual());
		query.setMaxResults(1);
		try {
			SessaoProcessoDocumentoVoto voto = (SessaoProcessoDocumentoVoto)query.getSingleResult();
			if(voto.getOjAcompanhado() != null && !voto.getOjAcompanhado().equals(voto.getOrgaoJulgador())){
				votosMarcados.put(voto.getOjAcompanhado(), true);
			}
			return voto;  

		} catch (NoResultException no) {
			return null;
		}
	}

	/**
	 * Verifica se existe voto destacado para discussao naquele processo
	 * 
	 * @param sessaoPPT
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean existeVotoDestacadoDiscussao(SessaoPautaProcessoTrf sessaoPPT) {
		StringBuilder sb = new StringBuilder("select o from SessaoProcessoDocumentoVoto o ");
		sb.append("where o.processoDocumento.ativo = true ");
		sb.append("and o.sessao = :sessao ");
		sb.append("and o.processoTrf = :processo ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("sessao", sessaoPPT.getSessao());
		query.setParameter("processo", sessaoPPT.getProcessoTrf());
		List<SessaoProcessoDocumentoVoto> listaVotos = query.getResultList();
		// pega a lista de votos daquele processo naquela sessão e verifica se
		// algum está destacado para discussão
		for (SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto : listaVotos) {
			if (sessaoProcessoDocumentoVoto.getDestaqueSessao()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna os tipos de voto para o prenximento da combo tipo voto.
	 * 
	 * @return Retorna uma lista de TipoVoto.
	 * @param votacaoAntecipada
	 *            indica se o tipo de voto a ser retornado veio da aba votação
	 *            antecipada ou não
	 */
	@SuppressWarnings("unchecked")
	public List<TipoVoto> retornaTipoVotoRelJug(boolean votacaoAntecipada) {
		if (votacaoAntecipada) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from TipoVoto o ");
			sb.append("where o.relator = false ");
			sb.append("and o.ativo = true ");
			Query q = getEntityManager().createQuery(sb.toString());
			return q.getResultList();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from TipoVoto o ");
			sb.append("where o.relator = true ");
			sb.append("and o.ativo = true ");
			Query q = getEntityManager().createQuery(sb.toString());
			return q.getResultList();
		}
	}

	/**
	 * grava um documento antecipado na sessao para um processo do tipo voto
	 */
	public void persistVoto() {


		ProcessoDocumento pd = new ProcessoDocumento();
		pd.setProcessoDocumento("Voto do Magistrado");
		pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		pd.setDataInclusao(new Date());
		pd.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
		pd.setProcessoTrf(ProcessoTrfHome.instance().getInstance());
		pd.setExclusivoAtividadeEspecifica(Boolean.TRUE);

		//Issue PJEII-3775: permitir que seja registrado um voto antecipado sem informar conteúdo no Editor de Texto.  
		String verificaModeloDocumentoBin = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
		if(Strings.isEmpty(verificaModeloDocumentoBin)){
			ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true); 
			if (ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento() == null) {
				ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(" ");
			}
		}
		
		pd.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());
		
		try{
			documentoJudicialService.persist(pd, true);
		} catch(PJeBusinessException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarVoto.erro", e.getMessage());
			log.error(e.getMessage());
		}

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		Sessao sessaoProcDoc = sessaoJulgamentoManager.getSessaoJulgamento(processoTrf);

        getInstance().setProcessoDocumento(pd);
        getInstance().setTipoInclusao(TipoInclusaoDocumentoEnum.A);
        getInstance().setOrgaoJulgador(processoTrf.getOrgaoJulgador());
        getInstance().setOjAcompanhado(processoTrf.getOrgaoJulgador());
        getInstance().setSessao(sessaoProcDoc);
        getInstance().setProcessoTrf(processoTrf);
        FacesMessages.instance().clear();
			
        if (getInstance().getLiberacao()) {
            liberarRelatorioEmenta(Boolean.TRUE);
        }
        super.persist();

		cacheSessaoVotos.clear();
	}

    /**
     * Grava um documento antecipado na sessao para um processo do tipo voto. Caso a sess<E3>o de julgamento ainda n<E3>o esteja registrada, apenas o documento vai ser cadastrado e o v<ED>nculo
     * com a sess<E3>o ser<E1> ignorado
     */
    public void persistVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto, boolean isSessaoExistente) throws PJeBusinessException {
        TipoProcessoDocumento tipoProcVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
        
        ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
        ProcessoDocumento procDocVotoRelator = null;
        if (isSessaoExistente) {
            procDocVotoRelator = documentoJudicialService.recuperarUltimoProcessoDocumento(tipoProcVoto, processoTrf.getProcesso());
        } else {
            procDocVotoRelator = documentoJudicialService.recuperarUltimoProcessoDocumentoNaoJulgado(processoTrf.getIdProcessoTrf(), tipoProcVoto.getIdTipoProcessoDocumento());
        }

        if (procDocVotoRelator == null) {
            procDocVotoRelator = new ProcessoDocumento();
            procDocVotoRelator.setProcessoDocumento(tipoProcVoto.getTipoProcessoDocumento());
            procDocVotoRelator.setTipoProcessoDocumento(tipoProcVoto);
            procDocVotoRelator.setDataInclusao(new Date());
            procDocVotoRelator.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
            procDocVotoRelator.setExclusivoAtividadeEspecifica(Boolean.TRUE);
        }

        procDocVotoRelator.setProcessoDocumentoBin(sessaoProcessoDocumentoVoto.getProcessoDocumento().getProcessoDocumentoBin());

        
        if (procDocVotoRelator.getIdProcessoDocumento() == 0 || 
                sessaoProcessoDocumentoVoto.getProcessoDocumento().getProcessoDocumentoBin().getIdProcessoDocumentoBin() == 0) {
            //Issue PJEII-3775: permitir que seja registrado um voto antecipado sem informar conte<FA>do no Editor de Texto. 
            documentoJudicialService.persist(procDocVotoRelator, true, true);
        }
        
        sessaoProcessoDocumentoVoto.setProcessoDocumento(procDocVotoRelator);
        
        if (sessaoProcessoDocumentoVoto.getIdSessaoProcessoDocumento() == 0) {
            sessaoProcessoDocumentoVoto.setTipoInclusao(TipoInclusaoDocumentoEnum.A);
            sessaoProcessoDocumentoVoto.setOrgaoJulgador(processoTrf.getOrgaoJulgador());
            sessaoProcessoDocumentoVoto.setOjAcompanhado(processoTrf.getOrgaoJulgador());
            sessaoProcessoDocumentoVoto.setTipoVoto(((SessaoProcessoDocumentoVoto) sessaoProcessoDocumentoVoto).getTipoVoto());

            Sessao sessaoProcDoc = sessaoJulgamentoManager.getSessaoJulgamento(processoTrf);
            // [ISSUE 1858] Se nao existir a sessao de julgamento, nao persistir as informacoes
            if (sessaoProcDoc != null) {
                sessaoProcessoDocumentoVoto.setSessao(sessaoProcDoc);
            }
            sessaoProcessoDocumentoVoto.setProcessoTrf(processoTrf);

            ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(sessaoProcessoDocumentoVoto);

        } else {
            ComponentUtil.getSessaoProcessoDocumentoManager().mergeAndFlush(sessaoProcessoDocumentoVoto);
        }

        if (sessaoProcessoDocumentoVoto.getLiberacao()) {
            liberarRelatorioEmenta(Boolean.TRUE);
        }
		cacheSessaoVotos.clear();
    }
			
	/**
	 * Grava um documento antecipado na sessao para um processo do tipo voto. Caso a sessão de julgamento ainda não esteja registrada, apenas o documento vai ser cadastrado e o vínculo
	 * com a sessão será ignorado
	 */
	public void persistVoto(Boolean isSessaoExistente) {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		//[PJEVII-1858] Busca o documento do tipo "Voto Relator"
		ProcessoDocumento procDocVotoRelator = documentoJudicialService.recuperarUltimoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto(), processoTrf.getProcesso());
		if (procDocVotoRelator == null) {
			procDocVotoRelator = new ProcessoDocumento();
			procDocVotoRelator.setProcessoDocumento("Voto Relator");
			procDocVotoRelator.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
			procDocVotoRelator.setDataInclusao(new Date());
			procDocVotoRelator.setProcesso(ProcessoTrfHome.instance().getInstance().getProcesso());
			procDocVotoRelator.setExclusivoAtividadeEspecifica(Boolean.TRUE);
		}
		
		//Issue PJEII-3775: permitir que seja registrado um voto antecipado sem informar conteúdo no Editor de Texto.  
		String verificaModeloDocumentoBin = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
		if(Strings.isEmpty(verificaModeloDocumentoBin)){
			ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true); 
			if (ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento() == null) {
				ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(" ");
			}
		}
		
		procDocVotoRelator.setProcessoDocumentoBin(ProcessoDocumentoBinHome.instance().getInstance());

		if (procDocVotoRelator.getIdProcessoDocumento() == 0) {
			try {
				documentoJudicialService.persist(procDocVotoRelator, true);
			} catch(PJeBusinessException e){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessao.salvarVoto.erro", e.getMessage());
				log.error(e.getMessage());
			}
		}
		
		getInstance().setProcessoDocumento(procDocVotoRelator);
		
		if (getInstance().getIdSessaoProcessoDocumento() == 0) {
			getInstance().setProcessoDocumento(procDocVotoRelator);
			getInstance().setTipoInclusao(TipoInclusaoDocumentoEnum.A);
			getInstance().setOrgaoJulgador(processoTrf.getOrgaoJulgador());
			getInstance().setOjAcompanhado(processoTrf.getOrgaoJulgador());
			getInstance().setTipoVoto(((SessaoProcessoDocumentoVoto) ComponentUtil.getSessaoProcessoDocumentoVotoHome().getInstance()).getTipoVoto());

			Sessao sessaoProcDoc = sessaoJulgamentoManager.getSessaoJulgamento(processoTrf);
			// [ISSUE 1858] Se nao existir a sessao de julgamento, nao persistir as informacoes
			if (sessaoProcDoc != null) {
				getInstance().setSessao(sessaoProcDoc);
			}
			getInstance().setProcessoTrf(processoTrf);
		}

		if (getInstance().getIdSessaoProcessoDocumento() == 0) {
			super.persist();
		} else {
			super.update();
		}
		if (getInstance().getLiberacao()) {
			liberarRelatorioEmenta(Boolean.TRUE);
		}
		
		cacheSessaoVotos.clear();
	}

	/**
	 * Atualiza a flag de destaque de voto 
	 */
	public void destacaVoto() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		//[PJEVII-1858] Busca o documento do tipo "Voto Relator"
		ProcessoDocumento procDocVotoRelator = documentoJudicialService.recuperarUltimoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto(), processoTrf.getProcesso());
		getInstance().setProcessoDocumento(procDocVotoRelator);

		Sessao sessaoProcDoc = sessaoJulgamentoManager.getSessaoJulgamento(processoTrf);
		// [ISSUE 1858] Se não existir a sessão de julgamento, não persistir o destaque do voto
		if (sessaoProcDoc != null && sessaoProcDoc.getIdSessao() > 0) {
			liberarRelatorioEmenta(getInstance().getLiberacao());

			if (getInstance().getIdSessaoProcessoDocumento() > 0) {
				super.update();
			} else {
				getInstance().setTipoInclusao(TipoInclusaoDocumentoEnum.A);
				getInstance().setOrgaoJulgador(processoTrf.getOrgaoJulgador());
				getInstance().setOjAcompanhado(processoTrf.getOrgaoJulgador());
				getInstance().setSessao(sessaoProcDoc);
				getInstance().setProcessoTrf(processoTrf);
				super.persist();
			}
		}

		cacheSessaoVotos.clear();
	}

	/**
	 * grava um documento acórdão na sessao para um processo do tipo voto
	 */
	public void persistVotoAcordao() {
		ProcessoDocumento pd = new ProcessoDocumento();
		boolean relator = SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf().getOrgaoJulgador()
				.equals(Authenticator.getOrgaoJulgadorAtual());
		// verifica se é o relator que esta fazendo o voto no acórdão
		// para gravar o processo documento
		if (relator) {
			pd.setProcessoDocumento("Voto do Magistrado");
		} else {
			pd.setProcessoDocumento("Voto");
		}
		pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
		pd.setDataInclusao(new Date());
		ProcessoHome.instance().setInstance(SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf().getProcesso());
		ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true);
		ProcessoDocumentoHome.instance().setInstance(pd);
		ProcessoDocumentoHome.instance().persist();

		getInstance().setSessao(SessaoPautaProcessoTrfHome.instance().getInstance().getSessao());
		//getInstance().setIdSessaoProcessoDocumento(pd.getIdProcessoDocumento());
		getInstance().setProcessoDocumento(pd);
		getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		getInstance().setOjAcompanhado(Authenticator.getOrgaoJulgadorAtual());
		getInstance().setProcessoTrf(SessaoPautaProcessoTrfHome.instance().getInstance().getProcessoTrf());

		FacesMessages.instance().clear();

		super.persist();

		cacheSessaoVotos.clear();
	}

	public void updateVotoAcordaoComAssinatura() {
		updateVotoAcordao();
		assinarDocumento();
	}

	public void updateVotoAcordao() {
		ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(Boolean.TRUE);
		update();
	}

	/**
	 * grava um documento acórdão na sessao para um processo do tipo voto e
	 * assina o documento
	 */
	public void persistirVotoAcordaoAssinatura() {
		if (!ProcessoDocumentoBinHome.instance().isModeloVazio()) {
			persistVotoAcordao();
			assinarDocumento();
		} else {
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do documento, é necessário que tenha algum conteúdo no editor.");
		}
	}

	public void defineFlagVisualLiberaVoto() {
		if (flagVisualLiberaVoto == null) {
			setFlagVisualLiberaVoto(!getInstance().getLiberacao());
		}
	}

	public void updateVoto() {
		liberarRelatorioEmenta(getInstance().getLiberacao());
		update();
	}

	@Override
	public String update() {
		//Issue PJEII-3775: permitir que seja registrado um voto antecipado sem informar conteúdo no Editor de Texto.
		String verificaModeloDocumentoBin = ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento();
		if(Strings.isEmpty(verificaModeloDocumentoBin)){
			ProcessoDocumentoBinHome.instance().setIgnoraConteudoDocumento(true); 
			if (ProcessoDocumentoBinHome.instance().getInstance().getModeloDocumento() == null) {
				ProcessoDocumentoBinHome.instance().getInstance().setModeloDocumento(" ");
			}
		}
		getInstance().setDtVoto(new Date());
		if(getInstance().getProcessoDocumento() != null){
			try{
				documentoJudicialService.persist(getInstance().getProcessoDocumento(), true);
				FacesMessages.instance().clear();
			}
			catch(PJeBusinessException e){
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o voto:  " + e.getMessage(), e);
				log.error(e.getMessage());
			}
		}		

		String retorno = super.update();
		
		try {
			derrubadaVotoManager.analisarTramitacaoFluxoVotoDerrubado(getInstance());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		
		cacheSessaoVotos.clear();
		
		return retorno;		
	}

	public void liberarRelatorioEmenta(boolean valorBooleano) {
		int idProcesso = getInstance().getProcessoTrf().getIdProcessoTrf();
		List<SessaoProcessoDocumento> documentosAntecipados = SessaoProcessoDocumentoHome.instance()
				.documentosAntecipados(idProcesso, false);
		for (SessaoProcessoDocumento spda : documentosAntecipados) {
			spda.setLiberacao(valorBooleano);
			getEntityManager().merge(spda);
		}
		getEntityManager().flush();
	}

	private void assinarDocumento() {
		
		boolean assinado = true;
		
		try{
			getInstance().setDtVoto(new Date());
			documentoJudicialService.finalizaDocumento(getInstance().getProcessoDocumento(), ProcessoTrfHome.instance().getInstance(), null, false, true, false, Authenticator.getPessoaLogada(), false);
			FacesMessages.instance().add(Severity.INFO,"Documento assinado com sucesso");
			getEntityManager().flush();
		}
		catch(Exception e){
			FacesMessages.instance().clear();
			reportMessage(e);
			log.error(e.getMessage());
			assinado = false;
		}
		setAssinado(assinado);
		cacheSessaoVotos.clear();
	}
	
	public void persistVotoComAssinatura() {
		persistVotoComAssinatura(null);
	}

	/**
	 * grava um documento antecipado na sessao para um processo Grava um
	 * documento antecipado na sessao para um processo do tipo voto e assina o
	 * documento
	 */
	public void persistVotoComAssinatura(ArquivoAssinadoHash arquivoAssinado) {
		if (!ProcessoDocumentoBinHome.instance().isModeloVazio()) {
			persistVoto();
			if (getInstance().getLiberacao()) {
				liberarRelatorioEmenta(Boolean.TRUE);
			}else{
				liberarRelatorioEmenta(Boolean.FALSE);
			}
			if(arquivoAssinado != null){
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setCertChain(arquivoAssinado.getCadeiaCertificado());
				this.getInstance().getProcessoDocumento().getProcessoDocumentoBin().setSignature(arquivoAssinado.getAssinatura());
			}			
			assinarDocumento();
		} else {
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do documento, é necessário que tenha algum conteúdo no editor.");
		}
		
		cacheSessaoVotos.clear();
	}
	
	/**
	 * Grava o texto de antecipação da proclamação de julgamento.
	 */
	public void persistProclamacaoJulgamento() {
		if(isPreenchidoTextoProclamacaoJulgamento()) {
			super.update();
			ComponentUtil.getSessaoPautaProcessoTrfManager().atualizarProclamacao(ComponentUtil.getSessaoJulgamentoManager().getSessaoJulgamento(getInstance().getProcessoTrf()), getInstance().getProcessoTrf(), getInstance().getTextoProclamacaoJulgamento());
		} else {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "sessaoProcessoDocumentoVoto.validaTextoProclamacaoJulgamento");
		}
	}
	
	/**
	 * Verifica se o texto de antecipação da proclamação de julgamento foi preenchido.
	 * @return true se o valor verificado não estiver nulo ou vazio, caso contrário, retorna false.
	 */
	private boolean isPreenchidoTextoProclamacaoJulgamento() {
		return getInstance() != null && StringUtils.isNotBlank(getInstance().getTextoProclamacaoJulgamento());
	}
	
	/**
	 * altera um documento antecipado na sessao para um processo do tipo voto e
	 * assina o documento
	 */
	public void updateVotoComAssinatura() {
		if (!ProcessoDocumentoBinHome.instance().isModeloVazio()) {
			//updateVoto();
			assinarDocumento();
			cacheSessaoVotos.clear();
		}
	}

	/**
	 * Retorna a mensagem de voto alterado, caso tenha voto alterado pelo relator.
	 * Caso não tenha alteracao, ira retonar uma String vazia
	 * @param processoPautado
	 * @return Mensagem de alteracao de voto caso tenha.
	 */
	public String verificaValidadeVotoRelator(SessaoPautaProcessoTrf processoPautado) {
		return derrubadaVotoManager.verificaValidadeVotoRelator(processoPautado);
	}
	
	public ProcessoDocumentoStatusEnum[] getDocumentoStatusEnumValues() {
		return ProcessoDocumentoStatusEnum.values();
	}

	public String getMagistradoAssinou(SessaoProcessoDocumentoVoto voto) {
		return sessaoProcessoDocumentoVotoManager.getMagistradoAssinou(voto);
	}

	public void setModeloDocumento(String modelodocumento) {
		this.modeloDocumento = modelodocumento;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}

	public void setVotoMagistradoAssinado(boolean votoMagistradoAssinado) {
		this.votoMagistradoAssinado = votoMagistradoAssinado;
	}

	public boolean getVotoMagistradoAssinado() {
		return votoMagistradoAssinado;
	}

	public void setSessaoPauta(SessaoPautaProcessoTrf sessaoPauta) {
		this.sessaoPauta = sessaoPauta;
	}

	public SessaoPautaProcessoTrf getSessaoPauta() {
		return sessaoPauta;
	}

	public void setSessaoCorrente(Sessao sessaoCorrente) {
		this.sessaoCorrente = sessaoCorrente;
	}

	public Sessao getSessaoCorrente() {
		return sessaoCorrente;
	}

	public void setAssinado(boolean assinado) {
		this.assinado = assinado;
	}

	public boolean isAssinado() {
		return assinado;
	}

	public void setExibeDivVoto(boolean exibeDivVoto) {
		this.exibeDivVoto = exibeDivVoto;
	}

	public boolean getExibeDivVoto() {
		return exibeDivVoto;
	}

	public boolean getSessaoFinalizada() {
		return sessaoFinalizada;
	}

	public void setSessaoFinalizada(boolean sessaoFinalizada) {
		this.sessaoFinalizada = sessaoFinalizada;
	}

	public boolean getSessaoEncerrada() {
		return sessaoEncerrada;
	}

	public void setSessaoEncerrada(boolean sessaoEncerrada) {
		this.sessaoEncerrada = sessaoEncerrada;
	}

	public void setTipoStatusVoto(VotoStatusEnum tipoStatusVoto) {
		this.tipoStatusVoto = tipoStatusVoto;
	}

	public VotoStatusEnum getTipoStatusVoto() {
		return tipoStatusVoto;
	}

	public Boolean getDivVoto() {
		return divVoto;
	}

	public void setDivVoto(Boolean divVoto) {
		this.divVoto = divVoto;
	}

	public ProcessoDocumentoStatusEnum getTipoStatusEmenta() {
		return tipoStatusEmenta;
	}

	public void setTipoStatusEmenta(ProcessoDocumentoStatusEnum tipoStatusEmenta) {
		this.tipoStatusEmenta = tipoStatusEmenta;
	}

	public boolean isEmentaAssinada() {
		return ementaAssinada;
	}

	public void setEmentaAssinada(boolean ementaAssinada) {
		this.ementaAssinada = ementaAssinada;
	}

	public ProcessoTrf getProcesso() {
		return processo;
	}

	public void setProcesso(ProcessoTrf processo) {
		this.processo = processo;
	}

	public Boolean getFlagVisualLiberaVoto() {
		return flagVisualLiberaVoto;
	}

	public void setFlagVisualLiberaVoto(Boolean flagVisualLiberaVoto) {
		this.flagVisualLiberaVoto = flagVisualLiberaVoto;
	}

	public Map<OrgaoJulgador,Boolean> getVotosMarcados() {
		return votosMarcados;
	}

	public void setVotosMarcados(Map<OrgaoJulgador,Boolean> votosMarcados) {
		this.votosMarcados = votosMarcados;
	}
	
	public void marcaVotoChecado(SessaoProcessoDocumentoVoto voto){
		if(votosMarcados.get(voto.getOrgaoJulgador()) == null){
			votosMarcados.put(voto.getOrgaoJulgador(),Boolean.TRUE);
		}else{
			votosMarcados.remove(voto.getOrgaoJulgador());
		}
	}

	public TipoVoto getVotoAntigo() {
		return votoAntigo;
	}

	public void setVotoAntigo(TipoVoto votoAntigo) {
		this.votoAntigo = votoAntigo;
	}

	public ModeloProclamacaoJulgamento getModeloProclamacaoJulgamento() {
		return modeloProclamacaoJulgamento;
	}

	public void setModeloProclamacaoJulgamento(
			ModeloProclamacaoJulgamento modeloProclamacaoJulgamento) {
		this.modeloProclamacaoJulgamento = modeloProclamacaoJulgamento;
	}
	public boolean getAssinado()
	{
		return assinado;
	}
	
	public ArquivoAssinadoHash getArquivoAssinadoHash() {
		return arquivoAssinadoHash;
	}
	
	public void setArquivoAssinadoHash(ArquivoAssinadoHash arquivoAssinadoHash) {
		this.arquivoAssinadoHash = arquivoAssinadoHash;
	}
	
	public String getDownloadLink() {
		StringBuilder sb = new StringBuilder();
		if(getInstance().getProcessoDocumento() != null){
			sb.append("id=");
			sb.append(String.valueOf(getInstance().getProcessoDocumento().getIdProcessoDocumento()));
			sb.append("&codIni=");
			sb.append(ProcessoDocumentoHome.instance().getCodData(getInstance().getProcessoDocumento()));
			sb.append("&md5=");
			sb.append(getInstance().getProcessoDocumento().getProcessoDocumentoBin().getMd5Documento());
			sb.append("&isBin=");
			sb.append(getInstance().getProcessoDocumento().getProcessoDocumentoBin().getExtensao() != null);
		}
		return sb.toString();
	}	

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash)
			throws Exception {
		setArquivoAssinadoHash(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	private Map<Integer,List<SessaoProcessoDocumentoVoto>> cacheSessaoVotos = new HashMap<>();			
	private List<SessaoProcessoDocumentoVoto> getVotosSessao(Sessao sessao) {
		List<SessaoProcessoDocumentoVoto> lista = cacheSessaoVotos.get(sessao.getIdSessao());
		if (lista==null) {
			lista = sessaoProcessoDocumentoVotoManager.recuperarVotos(sessao);
			cacheSessaoVotos.put(sessao.getIdSessao(), lista);
		}
		return lista;
	}
	
	public String getLabelVoto(Sessao sessao, ProcessoTrf processoTrf, OrgaoJulgador orgaoJulgador){
		
		StringBuilder sb = new StringBuilder();
		
		List<SessaoProcessoDocumentoVoto> votos = getVotosSessao(sessao);		
		Optional<SessaoProcessoDocumentoVoto> spdv = votos.stream().filter(p -> 
				processoTrf.equals(p.getProcessoTrf()) && orgaoJulgador.equals(p.getOrgaoJulgador())).findFirst();
		
		if(!spdv.isPresent() || spdv.get().getTipoVoto() == null || !spdv.get().getLiberacao()){
			sb.append("O relator não proferiu ou não liberou o voto");
		} else {
			sb.append(spdv.get().getTipoVoto().getTipoVoto());
		}
		
		return sb.toString();
	}
}