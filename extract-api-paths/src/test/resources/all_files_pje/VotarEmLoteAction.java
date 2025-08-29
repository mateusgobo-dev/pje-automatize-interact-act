package br.com.infox.pje.action;

import static br.com.itx.util.EntityUtil.find;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.bean.VotoAcompanhadoBean;
import br.com.infox.pje.list.VotoAcompanhadoList;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.infox.pje.service.SessaoJulgamentoService;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;

/**
 * Classe action para a página /PJE2/Painel/painel_usuario/
 * Painel_Usuario_Magistrado_2_Grau/votarEmLotePopUp.xhtml
 * 
 * @author daniel
 * 
 */
@Name(VotarEmLoteAction.NAME)
@Scope(ScopeType.PAGE)
public class VotarEmLoteAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1300034737091767408L;
	public static final String NAME = "votarEmLoteAction";
	private static String CHECK_DOCUMENTOS_INSERIDOS_VAR_NAME = "checkDocumentosInseridosVarName";
	private String idProcessoTrfList;
	private List<ProcessoTrf> processoTrfList;
	private Sessao sessao;
	private Integer idSessao;
	private TipoVoto tipoVoto;
	private Integer idTipoVoto;
	private String descricaoDocumento;
	private String voto;
	private TipoProcessoDocumento tipoProcessoDocumentoVoto;
	private List<ModeloDocumento> modeloDocumentoList;
	private ModeloDocumento modeloDocumento;
	private boolean destaqueSessao;
	private boolean liberaVotoAntecipado;
	private boolean impedimentoSuspeicao;
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto = new SessaoProcessoDocumentoVoto();
	private VotoAcompanhadoList votoAcompanhadoList = new VotoAcompanhadoList();
	private List<VotoAcompanhadoBean> votoAcompanhadoBeanList;

	@In
	private SessaoJulgamentoService sessaoJulgamentoService;
	@In
	private ModeloDocumentoLocalManager modeloDocumentoLocalManager;
	@In
	private transient ProcessoDocumentoManager processoDocumentoManager;
	@In
	private GenericManager genericManager;
	@In
	private transient ProcessoDocumentoBinManager processoDocumentoBinManager;

	private String certChain;
	private String signature;

	/**
	 * Realiza a votação em lote dos processos selecionados no painel do
	 * magistrado na sessão.
	 */
	public void votarEmLote() {
		Date hoje = new Date();
		inativarVotosAnteriores();
		inserirDocumentosNosProcessos(hoje);
		setVotoRealizado();
	}

	/**
	 * Realiza a votação em lote dos processos selecionados no painel do
	 * magistrado na sessão e grava a assinatura dos documentos.
	 */
	public void votarEmLoteEAssinar() {

		if (Strings.isEmpty(voto)) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR,
					"Para assinatura do voto, é necessário que tenha algum conteúdo no editor.");
			return;
		}

		Pessoa pessoaLogada = Authenticator.getPessoaLogada();
		try {
			VerificaCertificadoPessoa.verificaCertificadoValidoESePertenceAPessoa(certChain, pessoaLogada);
		} catch (CertificadoException e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao validar o certificado: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		Date hoje = new Date();
		inativarVotosAnteriores();
		List<ProcessoDocumento> documentos = inserirDocumentosNosProcessos(hoje);
		for (ProcessoDocumento processoDocumento : documentos) {
			processoDocumentoManager.inserirAssinaturaNoProcessoDocumentoBin(
					processoDocumento.getProcessoDocumentoBin(), signature, certChain, hoje, pessoaLogada);
		}
		setVotoRealizado();
	}

	private List<ProcessoDocumento> inserirDocumentosNosProcessos(Date hoje) {
		List<ProcessoDocumento> list = new ArrayList<ProcessoDocumento>();
		try{
			for (ProcessoTrf procTrf : processoTrfList) {
				ProcessoDocumentoBin pdb = processoDocumentoBinManager.inserirProcessoDocumentoBin(hoje, voto);
				ProcessoDocumento pd = new ProcessoDocumento();
				list.add(pd);
				pd.setProcessoDocumento(descricaoDocumento);
				pd.setDataInclusao(hoje);
				pd.setProcessoDocumentoBin(pdb);
				pd.setTipoProcessoDocumento(getTipoProcessoDocumentoVoto());
				pd = processoDocumentoManager.inserirProcessoDocumento(pd, procTrf, pdb);
				SessaoProcessoDocumentoVoto spdv = new SessaoProcessoDocumentoVoto();
				spdv.setSessao(getSessao());
				spdv.setProcessoTrf(procTrf);
				spdv.setProcessoDocumento(pd);
				spdv.setOrgaoJulgador(getOrgaoJulgadorUsuarioLogado());
				spdv.setLiberacao(liberaVotoAntecipado);
				//spdv.setIdSessaoProcessoDocumento(pd.getIdProcessoDocumento());
				spdv.setOrgaoJulgador(getOrgaoJulgadorUsuarioLogado());
				spdv.setTipoVoto(tipoVoto);
				spdv.setImpedimentoSuspeicao(impedimentoSuspeicao);
				spdv.setDestaqueSessao(destaqueSessao);
				this.sessaoJulgamentoService.setOjAcompanhado(procTrf, spdv, votoAcompanhadoBeanList,
						Authenticator.getOrgaoJulgadorAtual());
				genericManager.persist(spdv);
			}
			FacesMessages.instance().add("Voto realizado com sucesso.");
		} catch (PJeBusinessException e) {
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar o Documento: {0}", e.getMessage());
 			e.printStackTrace();
		}
		return list;
	}

	private void setVotoRealizado() {
		new Util().setToPageContext(CHECK_DOCUMENTOS_INSERIDOS_VAR_NAME, true);
	}

	public boolean isVotoRealizado() {
		Object var = new Util().getFromPageContext(CHECK_DOCUMENTOS_INSERIDOS_VAR_NAME);
		return var == null ? false : (Boolean) var;
	}

	private OrgaoJulgador getOrgaoJulgadorUsuarioLogado() {
		return Authenticator.getOrgaoJulgadorAtual();
	}

	private void inativarVotosAnteriores() {
		List<Processo> processos = new ArrayList<Processo>();
		for (ProcessoTrf processoTrf : processoTrfList) {
			processos.add(processoTrf.getProcesso());
		}
		List<SessaoProcessoDocumento> listSessaoProcessoDocumentoAtivo = sessaoJulgamentoService
				.listSessaoProcessoDocumentoAtivo(getSessao(), getTipoProcessoDocumentoVoto(),
						getOrgaoJulgadorUsuarioLogado(), processos);
		sessaoJulgamentoService.excluirSessaoProcessoDocumentoList(listSessaoProcessoDocumentoAtivo,
				Authenticator.getUsuarioLogado());
	}

	/**
	 * Obtem a lista de votos acompanhados, porém os valores são atribuidos a um
	 * Bean para possibilitar a seleção dos registros na tabela.
	 * 
	 * @param maxResults
	 *            maximo de resultados.
	 * @return lista de VotoAcompanhadoBean.java
	 */
	public List<VotoAcompanhadoBean> votoAcompanhadoBeanList(int maxResults) {
		if (votoAcompanhadoBeanList == null) {
			votoAcompanhadoBeanList = new ArrayList<VotoAcompanhadoBean>();
			for (SessaoProcessoDocumentoVoto spdv : votoAcompanhadoList.list(maxResults)) {
				VotoAcompanhadoBean vab = new VotoAcompanhadoBean(spdv, false);
				votoAcompanhadoBeanList.add(vab);
			}
		}
		return votoAcompanhadoBeanList;
	}

	/**
	 * Sessão corrente, em execução.
	 */
	public void setIdSessao(Integer idSessao) {
		if (idSessao != null && this.idSessao == null) {
			setSessao(find(Sessao.class, idSessao));
			if (getSessao() == null) {
				throw new AplicationException("Sessão não encontrada: " + idSessao);
			}
			this.idSessao = idSessao;
		}
	}

	public Integer getIdSessao() {
		return this.idSessao;
	}

	/**
	 * Tipo de Voto selecionado no painel do magistrado na sessão.
	 */
	public void setIdTipoVoto(Integer idTipoVoto) {
		if (idTipoVoto != null && this.idTipoVoto == null) {
			setTipoVoto(find(TipoVoto.class, idTipoVoto));
			if (getTipoVoto() == null) {
				throw new AplicationException("Sessão não encontrada: " + idTipoVoto);
			}
			this.idTipoVoto = idTipoVoto;
		}
	}

	public Integer getIdTipoVoto() {
		return this.idTipoVoto;
	}

	/**
	 * Processos que devem ser votados em lote.
	 */
	public void setIdProcessoTrfList(String idProcessoTrfList) {
		if (idProcessoTrfList != null && this.idProcessoTrfList == null) {
			processoTrfList = new ArrayList<ProcessoTrf>();
			for (String s : idProcessoTrfList.split(",")) {
				ProcessoTrf processoTrf = find(ProcessoTrf.class, Integer.parseInt(s));
				processoTrfList.add(processoTrf);
			}
			this.idProcessoTrfList = idProcessoTrfList;
		}
	}

	public String getIdProcessoTrfList() {
		return this.idProcessoTrfList;
	}

	/**
	 * Popula o texto do documento com o modelo selecionado.
	 */
	public void onSelectModeloDocumento() {
		if (modeloDocumento == null) {
			voto = null;
		} else {
			voto = ProcessoDocumentoHome.processarModelo(modeloDocumento.getModeloDocumento());
		}
	}

	/**
	 * Obtem o tipo de documento que seja Voto.
	 * 
	 * @return
	 */
	public TipoProcessoDocumento getTipoProcessoDocumentoVoto() {
		if (tipoProcessoDocumentoVoto == null) {
			tipoProcessoDocumentoVoto = ParametroUtil.instance().getTipoProcessoDocumentoVoto();
		}
		return tipoProcessoDocumentoVoto;
	}

	public void setModeloDocumentoList(List<ModeloDocumento> modeloDocumentoList) {
		this.modeloDocumentoList = modeloDocumentoList;
	}

	/**
	 * Obtem a lista de modelos de documento baseado no tipoProcessoDocumento
	 * 
	 * @return
	 */
	public List<ModeloDocumento> getModeloDocumentoList() {
		if (modeloDocumentoList == null) {
			modeloDocumentoList = modeloDocumentoLocalManager.getModeloDocumentoPorTipo(getTipoProcessoDocumentoVoto());
		}
		return modeloDocumentoList;
	}

	public void setModeloDocumento(ModeloDocumento modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}

	public ModeloDocumento getModeloDocumento() {
		return modeloDocumento;
	}

	public void setTipoVoto(TipoVoto tipoVoto) {
		this.tipoVoto = tipoVoto;
	}

	public TipoVoto getTipoVoto() {
		return tipoVoto;
	}

	public void setDescricaoDocumento(String descricaoDocumento) {
		this.descricaoDocumento = descricaoDocumento;
	}

	public String getDescricaoDocumento() {
		return descricaoDocumento;
	}

	public String getVoto() {
		return voto;
	}

	public void setVoto(String voto) {
		this.voto = voto;
	}

	public void setDestaqueSessao(boolean destaqueSessao) {
		this.destaqueSessao = destaqueSessao;
	}

	public boolean isDestaqueSessao() {
		return destaqueSessao;
	}

	public void setLiberaVotoAntecipado(boolean liberaVotoAntecipado) {
		this.liberaVotoAntecipado = liberaVotoAntecipado;
	}

	public boolean isLiberaVotoAntecipado() {
		return liberaVotoAntecipado;
	}

	public void setImpedimentoSuspeicao(boolean impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}

	public boolean isImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}

	public void setVotoAcompanhadoList(VotoAcompanhadoList votoAcompanhadoList) {
		this.votoAcompanhadoList = votoAcompanhadoList;
	}

	public VotoAcompanhadoList getVotoAcompanhadoList() {
		return votoAcompanhadoList;
	}

	public SessaoJulgamentoService getSessaoJulgamentoService() {
		return sessaoJulgamentoService;
	}

	public void setSessaoJulgamentoService(SessaoJulgamentoService sessaoJulgamentoService) {
		this.sessaoJulgamentoService = sessaoJulgamentoService;
	}

	public void setSessao(Sessao sessao) {
		this.sessao = sessao;
	}

	public Sessao getSessao() {
		return sessao;
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

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		return sessaoProcessoDocumentoVoto;
	}

	public void setSessaoProcessoDocumentoVoto(SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto) {
		this.sessaoProcessoDocumentoVoto = sessaoProcessoDocumentoVoto;
	}

}