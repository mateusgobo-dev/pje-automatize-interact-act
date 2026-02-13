package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Lifecycle;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.AcessoNegadoException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("visualizarExpedienteSemLoginAction")
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class VisualizarExpedienteSemLoginAction {

	private Boolean podeAcessarExpediente = Boolean.FALSE;

	private ProcessoDocumento processoDocumento;

	public boolean init() throws AcessoNegadoException {
		try {
			if (isChaveAcessoOK()) {
				Lifecycle.beginCall();
				
				String[] params = ArrayUtil.split(SecurityTokenControler.instance().validarChaveAcessoGenerica(getChaveAcesso()), "_");
				Integer idProcessoParteExpediente = ArrayUtil.getInteger(params, 0);
				String destinatario = ArrayUtil.get(params, 1);
				Integer idProcessoDocumentoExpediente = ArrayUtil.getInteger(params, 2);

				if (idProcessoParteExpediente != null) {
					ProcessoParteExpediente ppe = ProcessoParteExpedienteManager.instance().findById(idProcessoParteExpediente);
					if (isDocumentoParteIgualDestinatario(ppe, destinatario)) {
						setPodeAcessarExpediente(true);

						if(ParametroUtil.instance().getDomicilioEletronicoRegistraCienciaLinkInteiroTeor()) {
							DomicilioEletronicoService.instance().registrarCienciaPessoal(ppe);
						}

						// Documento principal do expediente
						ProcessoDocumento pd = ppe.getProcessoDocumento();
						ProcessoDocumentoBin pdb = pd.getProcessoDocumentoBin();
						// Documento do anexo do expediente
						if (idProcessoDocumentoExpediente != null) {
							ProcessoDocumentoExpediente pde = ProcessoDocumentoExpedienteManager.instance().findById(idProcessoDocumentoExpediente);
							pd = pde.getProcessoDocumento();
							pdb = pd.getProcessoDocumentoBin();
						}

						if (pdb.isBinario()) {
							PdfView.instance().gerarResponse(pdb);
						} else {
							setProcessoDocumento(pd);
						}
					}
				} else {
					throw new AcessoNegadoException();
				}
			}
		} catch (Exception e) {
			throw new AcessoNegadoException();
		} 

		return podeAcessarExpediente;
	}

	/**
	 * Junta a certidão de ciência do domicílio.
	 * 
	 * @param ppe ProcessoParteExpediente
	 * @throws PJeBusinessException
	 */
	protected void juntarCertidaoCienciaDomicilio(ProcessoParteExpediente ppe) throws PJeBusinessException {
		ModeloDocumento modeloCertidao = ParametroUtil.instance().getModeloDocumentoCertidaoCienciaDomicilio();
		TipoProcessoDocumento tipoDocumento = ParametroUtil.instance().getTipoProcessoDocumentoCertidao();
		if (modeloCertidao != null) {
			Integer idPd = DocumentoJudicialService.instance().gerarMinuta(ppe.getIdProcessoJudicial(), null, null,
					tipoDocumento.getIdTipoProcessoDocumento(), modeloCertidao.getIdModeloDocumento());
			DocumentoJudicialService.instance().juntarDocumento(idPd, null);
			MovimentoAutomaticoService.preencherMovimento().deCodigo(60).associarAoDocumentoDeId(idPd)
					.comComplementoDeCodigo(4).doTipoDominio().preencherComElementoDeCodigo(107).lancarMovimento();
		}
	}

	/**
	 * @param ppe ProcessoParteExpediente
	 * @param destinatário Documento recuperado da chave de acesso.
	 * @return True se o documento da parte for o mesmo do destinatário da chave de acesso.
	 */
	protected boolean isDocumentoParteIgualDestinatario(ProcessoParteExpediente ppe, String destinatario) {
		String cpfCnpj = StringUtil.removeNaoNumericos(ppe.getPessoaParte().getDocumentoCpfCnpj());
		return (cpfCnpj != null && cpfCnpj.equals(destinatario));
	}

	/**
	 * @return True se a chave de acesso estiver OK
	 */
	protected boolean isChaveAcessoOK() {
		return !StringUtil.isEmpty(SecurityTokenControler.instance().validarChaveAcessoGenerica(getChaveAcesso()));
	}

	/**
	 * @return Request("ca")
	 */
	public String getChaveAcesso() {
		return FacesUtil.getRequestParameter("ca");
	}
	
	public Boolean getPodeAcessarExpediente() {
		return podeAcessarExpediente;
	}

	public void setPodeAcessarExpediente(Boolean podeAcessarExpediente) {
		this.podeAcessarExpediente = podeAcessarExpediente;
	}

	public ProcessoDocumento getProcessoDocumento() {
		return processoDocumento;
	}

	public void setProcessoDocumento(ProcessoDocumento processoDocumento) {
		this.processoDocumento = processoDocumento;
	}
}
