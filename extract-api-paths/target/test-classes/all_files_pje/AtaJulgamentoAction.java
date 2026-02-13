/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.DocumentoSessaoManager;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.DocumentoSessao;
import br.jus.pje.nucleo.entidades.Sessao;

@Name(AtaJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AtaJulgamentoAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "ataJulgamentoAction";

	/**
	 * Armazena o id da sessao que sera passado via parametro (GET), definido o no arquivo ataJulgamento.page.xml
	 */
	@RequestParameter(value="idSessao")
	private Integer idSessao;

	@In
	private FacesMessages facesMessages;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	/**
	 * Armazena a ata de julgamento que sera editada e assinada
	 */
	private DocumentoSessao ataJulgamento;
	
	@In
	private DocumentoJudicialService documentoJudicialService;

	private Sessao sessao;
	
	@In
	private SessaoManager sessaoManager;
	
	@In
	private DocumentoSessaoManager documentoSessaoManager;
	
	/**
	 * Metodo responsavel por carregar os dados da tela, este metodo sera invocado pelo seam, atraves da definicao no arquivo ataJulgamento.page.xml
	 */
	@Create
	public void iniciar() {
		this.sessao = this.sessaoManager.recuperarPorId(getIdSessao());
		SessaoHome.instance().setInstance(this.sessao);
		this.ataJulgamento = this.documentoSessaoManager.recuperarOuCriarNovaAtaJulgamentoPorSessao(this.sessao);		
	}
		
	@Override
	public String getActionName() {
		return NAME;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		getAtaJulgamento().setCertChain(arquivoAssinadoHash.getCadeiaCertificado());
		getAtaJulgamento().setSignature(arquivoAssinadoHash.getAssinatura());
	}
	
	public String getUrlDocsField() {		
		return this.documentoJudicialService.getDownloadLink(getAtaJulgamento().getModeloDocumentoSessao());
	}
		
	public DocumentoSessao getAtaJulgamento() {
		return ataJulgamento;		
	}

	public void setAtaJulgamento(DocumentoSessao ataJulgamento) {
		this.ataJulgamento = ataJulgamento;
	}
	
	public Integer getIdSessao() {
		return idSessao;
	}

	public void setIdSessao(Integer idSessao) {
		this.idSessao = idSessao;
	}
	
	public void salvarAtaJulgamento() {
		try {
			this.ataJulgamento = this.documentoSessaoManager.salvar(this.ataJulgamento);

			ajaxDataUtil.sucesso();
			
			facesMessages.add(Severity.INFO, "A ata de julgamento foi salva com sucesso!");			
		}
		catch (Exception e) {			
			
			ajaxDataUtil.erro();			
			
			facesMessages.add(Severity.ERROR, "Ocorreu um erro ao salvar a ata de julgamento, mensagem interna: {0}", e.getMessage());		
		}
	}

	public boolean getAtaJulgamentoEstaPersistida() {
		return getAtaJulgamento() != null && getAtaJulgamento().getIdDocumentoSessao() != null;
	}
	
	public boolean getAtaJulgamentoEstaAssinada() {
		return getAtaJulgamento() != null && getAtaJulgamento().getCertChain() != null;
	}
		
	public void concluir() {
		
		try {
			documentoSessaoManager.concluirAssinaturaAtaJulgamento(getAtaJulgamento(), Authenticator.getPessoaLogada());
			
			ajaxDataUtil.sucesso();
			
			facesMessages.add(Severity.INFO, "A ata de julgamento foi salva com sucesso!");
		}
		catch (Exception e) {
						
			ajaxDataUtil.erro();
			
			facesMessages.add(Severity.ERROR, "Erro ao assinar a ata de julgamento, mensagem interna: {0}", e.getLocalizedMessage());		
		}
	}	
}