package br.com.infox.pje.action;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoProcessoDocumentoVotoManager;
import br.jus.cnj.pje.nucleo.manager.TipoVotoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

public abstract class AbstractInteiroTeorProcesso {

	private List<ModeloDocumento> modeloDocumentoList;

	private ProcessoTrf processoTrf;
	private Sessao sessao;

	private ModeloDocumento modeloDocumento;
	protected List<TipoVoto> tipoVotoList;
	private String certChain;
	private String signature;

	public abstract SessaoProcessoDocumento getSessaoProcessoDocumento();

	public abstract void setSessaoProcessoDocumento(SessaoProcessoDocumento sessaoProcessoDocumento);

	@In
	protected SessaoProcessoDocumentoManager sessaoProcessoDocumentoManager;

	@In
	protected SessaoProcessoDocumentoVotoManager sessaoProcessoDocumentoVotoManager;

	@In
	protected ModeloDocumentoLocalManager modeloDocumentoLocalManager;

	@In
	protected ProcessoDocumentoManager processoDocumentoManager;

	@In
	protected TipoVotoManager tipoVotoManager;
	
	@In
	protected DocumentoJudicialService documentoJudicialService;
	
	@In
	protected ProcessoJudicialService processoJudicialService;

	@In
	protected AssinaturaDocumentoService assinaturaDocumentoService;

	public void update() {
		ProcessoDocumento pd = getSessaoProcessoDocumento().getProcessoDocumento();
		pd.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
		pd.setNomeUsuarioAlteracao(Authenticator.getUsuarioLogado().getNome());
		pd.setDataAlteracao(new Date());
		pd.setPapel(Authenticator.getPapelAtual());
		pd.setLocalizacao(Authenticator.getLocalizacaoAtual());
		try {
			sessaoProcessoDocumentoManager.persist(getSessaoProcessoDocumento());
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar gravar o documento!");
			e.printStackTrace();
		}
		FacesMessages.instance().add(Severity.INFO, "Documento atualizado com sucesso!");
	}

	// protected abstract SessaoProcessoDocumentoManager
	// getSessaoProcessoDocumentoManager();

	public SessaoProcessoDocumento persist() {
		
		SessaoProcessoDocumento spd = getSessaoProcessoDocumento();
		UsuarioLocalizacao local = Authenticator.getUsuarioLocalizacaoAtual();
		String modeloDocumentoBin = null;
		
		if(spd.getProcessoDocumento() != null){
			modeloDocumentoBin = spd.getProcessoDocumento().getProcessoDocumentoBin()
			.getModeloDocumento();
		}
		if(!Strings.isEmpty(modeloDocumentoBin)){
			ProcessoDocumento pd = spd.getProcessoDocumento();
			if(spd instanceof SessaoProcessoDocumentoVoto){
				pd.setProcessoDocumento("voto");
				if(pd.getTipoProcessoDocumento() == null){
					pd.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoVoto());
				}
			}
			pd.setUsuarioInclusao(local.getUsuario());
			pd.setNomeUsuarioInclusao(local.getUsuario().getNome());
			pd.setDataInclusao(new Date());
			pd.setPapel(local.getPapel());
			pd.setLocalizacao(local.getLocalizacaoFisica());
			pd.setProcesso(processoTrf.getProcesso());
			try{
				pd = documentoJudicialService.persist(pd, true);
				spd.setProcessoDocumento(pd);
			}catch(PJeBusinessException e){
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o voto:  " + e.getMessage(), e);
				return null;
			}
		}else{
			spd.setProcessoDocumento(null);
		}

		spd = sessaoProcessoDocumentoManager.persistirSessaoEAgregados(getSessao(), spd, getProcessoTrf(), local, Authenticator.getOrgaoJulgadorAtual());

		FacesMessages.instance().add(Severity.INFO, "Documento inserido com sucesso!");

		return spd;
	}

	/**
	 * Realiza a gravação dos campos e assina o voto
	 * 
	 */
	public void assinarESalvar() {

		// Valida se existe conteúdo no editor
		if (this.getSessaoProcessoDocumento().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() == null
				|| this.getSessaoProcessoDocumento().getProcessoDocumento().getProcessoDocumentoBin()
						.getModeloDocumento().isEmpty()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura é necessário que tenha algum conteúdo no editor");
			return;
		}

//		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
//
//		try {
//			VerificaCertificadoPessoa.verificaCertificadoValidoESePertenceAPessoa(certChain, pessoaLogada);
//		} catch (CertificadoException e) {
//			FacesMessages.instance().add(Severity.ERROR, "Erro ao validar o certificado: " + e.getMessage());
//			e.printStackTrace();
//			return;
//		}
//
		SessaoProcessoDocumento spd = persist();
		ProcessoDocumento pd = spd.getProcessoDocumento();
		pd.getProcessoDocumentoBin().setCertChain(certChain);
		pd.getProcessoDocumentoBin().setSignature(signature);
		ProcessoTrf processoJudicial;
		try {
			processoJudicial = processoJudicialService.findById(pd.getProcesso().getIdProcesso());
			documentoJudicialService.finalizaDocumento(pd, processoJudicial, null, true);
			EntityUtil.flush();
		} catch (PJeDAOException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar gravar o documento no banco de dados: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		} catch (CertificadoException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Erro de certificado ao realizar a assinatura: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a assinatura: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		} 
//		ProcessoDocumentoBin pdb = spd.getProcessoDocumento().getProcessoDocumentoBin();
//		this.processoDocumentoManager.inserirAssinaturaNoProcessoDocumentoBin(pdb, signature, certChain, hoje,
//				pessoaLogada);
//		FacesMessages.instance().clear();
//		FacesMessages.instance().add(Severity.INFO, "Documento assinado com sucesso.");

	}

	public boolean isRelatorENaoAssinado() {
		return sessaoProcessoDocumentoManager.isRelatorENaoAssinado(sessao, processoTrf,
				Authenticator.getOrgaoJulgadorAtual(), getSessaoProcessoDocumento());
	}

	public boolean existeDocumento() {
		return (getSessaoProcessoDocumento() != null && getSessaoProcessoDocumento().getSessao() != null && getSessaoProcessoDocumento().getProcessoDocumento() != null);
	}

	public List<ModeloDocumento> getModeloDocumentoList(TipoProcessoDocumento tipoProcessoDocumento) {

		if (this.modeloDocumentoList == null && this.getSessaoProcessoDocumento() != null) {
			this.modeloDocumentoList = this.modeloDocumentoLocalManager
					.getModeloDocumentoPorTipo(tipoProcessoDocumento);
		}
		return modeloDocumentoList;

	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	protected SessaoProcessoDocumento getSessaoProcessoDocumentoByTipo(TipoProcessoDocumento tipoProcessoDocumento) {
		if (processoTrf == null || sessao == null || tipoProcessoDocumento == null) {
			return null;
		}
		return sessaoProcessoDocumentoManager.getSessaoProcessoDocumentoByTipo(sessao, tipoProcessoDocumento,
				processoTrf.getProcesso());
	}

	protected SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVotoByTipoOj(OrgaoJulgador orgaoJulgador) {
		if (processoTrf == null || sessao == null) {
			return null;
		}
		return sessaoProcessoDocumentoVotoManager.recuperarVoto(sessao, processoTrf, orgaoJulgador);
	}

	public boolean podeEditarConteudo() {
		return getSessaoProcessoDocumento() != null
				&& sessaoProcessoDocumentoManager.podeEditarConteudoDocumento(getSessao(),
						getSessaoProcessoDocumento(), getProcessoTrf(), Authenticator.getOrgaoJulgadorAtual());
	}

	public boolean podeEditarComponente() {
		return getSessaoProcessoDocumento() != null
				&& sessaoProcessoDocumentoManager.podeEditarComponentesVoto(getSessao(), getSessaoProcessoDocumento(),
						getProcessoTrf(), Authenticator.getOrgaoJulgadorAtual());
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public Sessao getSessao() {
		return sessao;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setTipoVotoList(List<TipoVoto> tipoVotoList) {
		this.tipoVotoList = tipoVotoList;
	}

	public abstract TipoVotoManager getTipoVotoManager();

	/**
	 * Popula o texto do documento com o modelo selecionado.
	 */
	public void onSelectModeloDocumento() {
		ProcessoDocumentoBin processoDocumentoBin = getSessaoProcessoDocumento().getProcessoDocumento()
				.getProcessoDocumentoBin();
		if (modeloDocumento == null) {
			processoDocumentoBin.setModeloDocumento(null);
		} else {
			processoDocumentoBin.setModeloDocumento(ProcessoDocumentoHome.processarModelo(modeloDocumento
					.getModeloDocumento()));
		}
	}

	public void newInstance() {
		processoTrf = null;
		sessao = null;
		setSessaoProcessoDocumento(null);
	}

	public void removerAssinatura(ProcessoDocumento pd) {
		try {
			assinaturaDocumentoService.removeAssinatura(pd);
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Assinatura removida com sucesso.");
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a assinatura.");
		}
	}

}
