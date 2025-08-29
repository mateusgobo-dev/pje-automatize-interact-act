/**
 * 
 */
package br.jus.cnj.pje.view;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.core.certificado.DadosAssinatura;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.AssinaturaUtil;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.LocalizacaoManager;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;

/**
 * @author cristof
 * 
 */
@Name("verificaDocumentoAction")
@Scope(ScopeType.EVENT)
public class VerificaDocumentoAction {

	@RequestParameter
	private Integer idProcessoDocumento;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@In
	private FacesMessages facesMessages;
	
	@In
	private LocalizacaoManager localizacaoManager;

	private List<DadosAssinatura> dadosAssinaturas;

	private ProcessoDocumento documento;

	@Create
	public void init() {
		if (idProcessoDocumento != null) {
			try {
				documento = documentoJudicialService.getDocumento(idProcessoDocumento);
				// carregaDadosCertificados();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Não foi possível recuperar o documento {0}: {1}", idProcessoDocumento, e.getLocalizedMessage());
			}
		}
	}

	private void carregaDadosCertificados() {
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = documentoJudicialService.validaAssinaturasDocumento(documento, false);
		dadosAssinaturas = new ArrayList<DadosAssinatura>(assinaturas.size());
		if (assinaturas.size() == 0) {
			facesMessages.add(Severity.INFO, "Não há assinaturas válidas no documento {0}", idProcessoDocumento);
			return;
		}
		for (ProcessoDocumentoBinPessoaAssinatura assinatura : assinaturas) {
			if (AssinaturaUtil.isModoTeste(assinatura.getAssinatura())) {
				DadosAssinatura da = new DadosAssinatura();
				da.nome = assinatura.getNomePessoa();
				da.commonName = "Assinatura de teste";
				da.dataAssinatura = assinatura.getDataAssinatura();
				da.assinatura = assinatura.getAssinatura();
				da.certChain = assinatura.getCertChain();
				da.issuer = "PJe em teste";
				dadosAssinaturas.add(da);
			} else {
				try {
					Certificate[] cert = SigningUtilities.getCertChain(assinatura.getCertChain());
					DadosAssinatura da = new DadosAssinatura();
					da.nome = assinatura.getNomePessoa();
					da.commonName = ((X509Certificate) cert[0]).getSubjectX500Principal().getName();
					da.dataAssinatura = assinatura.getDataAssinatura();
					da.assinatura = assinatura.getAssinatura();
					da.certChain = assinatura.getCertChain();
					da.certificate = (X509Certificate) cert[0];
					da.issuer = da.certificate.getIssuerX500Principal().getName();
					dadosAssinaturas.add(da);
				} catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Não foi possível recuperar a assintura vinculada ao certificado de [{0}].", assinatura.getNomePessoa());
				}
			}
		}
	}

	public List<DadosAssinatura> getDadosAssinaturas() {
		if (dadosAssinaturas == null || dadosAssinaturas.size() == 0) {
			carregaDadosCertificados();
		}
		return dadosAssinaturas;
	}

	public void setDadosAssinaturas(List<DadosAssinatura> dadosAssinaturas) {
		this.dadosAssinaturas = dadosAssinaturas;
	}

	public ProcessoDocumento getDocumento() {
		return documento;
	}

	public void setDocumento(ProcessoDocumento documento) {
		this.documento = documento;
	}
	
	/**
 	 * Verifica se a localização do usuário logado é descendente da localização do usuário criador do documento.
 	 * 
 	 * @return Boolean: verdadeiro se for localização descendente. Falso, caso contrário.
 	 */
 	public boolean isLocalizacaoDescendente(){
 		if(getDocumento().getLocalizacao() != null){
 			return localizacaoManager.isLocalizacaoDescendente(Authenticator.getLocalizacaoAtual(), getDocumento().getLocalizacao()); 
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Verifica se o usuário logado é o criador do documento.
 	 * 
 	 * @return Boolean: verdadeiro se o usuário logado for o criador do documento. Falso, caso contrário.
 	 */
 	public boolean isUsuarioCriadorDocumento(){
 		return Authenticator.getIdUsuarioLogado().equals(getDocumento().getUsuarioInclusao().getIdUsuario());
 	}
}