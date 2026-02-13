/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.SessaoProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ModeloDocumentoLocalManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(CertidaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CertidaoJulgamentoAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "certidaoJulgamentoAction";

	/**
	 * Define a lista de arquivos assinados pelo assinador, os arquivos assinados e enviados pelo assinador
	 * sera armazenados nesta lista para posterior validacao e persistencia.
	 */
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	
	@In
	private FacesMessages facesMessages;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
	
	@In
	private ModeloDocumentoLocalManager modeloDocumentoLocalManager;

	@Override
	public String getActionName() {
		return NAME;
	}
	
	/**
 	 * Metodo que verifica se  obrigatrio a assinatura pelo tipo processo documento 
 	 * e papel do usurio logado
 	 * @return boolean
 	 */
	public boolean isApresentaBotaoAssinar(){ 
		boolean retorno = false; 
		if(ParametroUtil.instance().getModeloCertidaoJulgamento() != null) { 
			int idModeloDocumento = (ParametroUtil.instance().getModeloCertidaoJulgamento().getIdModeloDocumento()); 
			ModeloDocumentoLocal modelo = modeloDocumentoLocalManager.findById(idModeloDocumento); 
			TipoProcessoDocumento tipoCertidao = modelo.getTipoProcessoDocumento(); 
			retorno = !tipoProcessoDocumentoPapelService.verificarExigibilidadeNaoAssina(Authenticator.getPapelAtual(), tipoCertidao); 
		} 
		return retorno; 
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}
	
	public String getUrlDocsField() {
		
		List<ProcessoDocumento> documentos = getSessaoProcessoDocumentoHome().getDocumentosAssinar();
		
		return this.documentoJudicialService.getDownloadLinks(documentos);
	}
	
	/**
	 * Recupera o objeto sessaoProcessoDocumentoHome.
	 * O ideal seria realizar um refactoring para extrair todos os metodos e atributos utilizados
	 * pela tela certidaoJulgamento.xhtml para esta action, tirando esta responsabilidade da classe home. 
	 */
	public SessaoProcessoDocumentoHome getSessaoProcessoDocumentoHome() {
		return ComponentUtil.getComponent(SessaoProcessoDocumentoHome.NAME);
	}
	
	public void concluirComMovimentacao() {

		try {
			List<ProcessoDocumento> documentosParaAssinatura = getSessaoProcessoDocumentoHome().getDocumentosAssinar();
			
			this.documentoJudicialService.juntarEhGravarAssinaturaDeProcessosDocumentosNaoSigilososComMovimentacao(this.arquivosAssinados, documentosParaAssinatura);
					
			getSessaoProcessoDocumentoHome().limparDocumentosAssinar();
			
			this.facesMessages.add(Severity.INFO, "As certidões de julgamento foram assinadas com sucesso!");
		}
		catch (Exception e) {
			this.facesMessages.add(Severity.ERROR, "Erro ao assinar as certidões de julgamento, mensagem interna: {0}", e.getMessage());
		}
	}
		
	public void alterarCertidaoJulgamento() {
		try {
			getSessaoProcessoDocumentoHome().updateCertidao();
			
			this.ajaxDataUtil.sucesso();
		}
		catch (Exception e) {			
			
			this.ajaxDataUtil.erro();

			this.facesMessages.add(Severity.ERROR, "Erro ao alterar a certidão de julgamento, mensagem interna: {0}","teste");			
		}
	}
}