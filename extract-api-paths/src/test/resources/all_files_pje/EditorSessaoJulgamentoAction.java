/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoSessaoManager;
import br.jus.cnj.pje.nucleo.view.ICkEditorController;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.DocumentoSessao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.TipoDocumentoSessaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(EditorSessaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EditorSessaoJulgamentoAction implements Serializable, ArquivoAssinadoUploader, ICkEditorController {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "editorSessaoJulgamentoAction";
	private DocumentoSessao documento;
	private String modeloDocumento;
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	private boolean exibeMPConfirmacaoAtualizacaoMinuta = false;
 
	@RequestParameter(value="idSessao")
	private Integer idSessao;
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
	@Create
	public void iniciar() {
		Sessao sessao = ComponentUtil.getSessaoManager().recuperarPorId(getIdSessao());
		SessaoHome.instance().setInstance(sessao);
		if(sessao != null && sessao.getIdSessao() > 0) {
			try {
				this.documento = DocumentoSessaoManager.instance().recuperarOuCriarNovo(sessao, TipoDocumentoSessaoEnum.M, ParametroUtil.instance().getModeloEditorSessaoJulgamento());
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar recuperar conteúdo da minuta do pregão: " + e.getLocalizedMessage() + " - " + e.getMessage());
			}
			if(this.documento != null) {
				this.modeloDocumento = documento.getModeloDocumentoSessao();
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "No foi possível recuperar os dados da sessão!");
		}
	}
	
	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}
	
	public String getUrlDocsField() {
		return null;
	}
	
	@Override
	public void salvar(String conteudo) {
		try {
			if(this.documento != null) {
				this.documento.setModeloDocumentoSessao(conteudo);
				DocumentoSessaoManager.instance().mergeAndFlush(documento);
			}
			modeloDocumento = conteudo;
			FacesMessages.instance().add(Severity.INFO, "A minuta do pregão foi alterada com sucesso!");
		}
		catch (Exception e) {			
			FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar a minuta do pregão: " + e.getLocalizedMessage());			
		}
	}
	
	public void atualizar() {
		try {
			String conteudo = "";
			if(this.documento != null) {
				ModeloDocumento modelo = ParametroUtil.instance().getModeloEditorSessaoJulgamento();
				if (modelo != null) {
					ModeloDocumentoLocal modeloLocal = ComponentUtil.getModeloDocumentoLocalManager().findById(modelo.getIdModeloDocumento());
					if(modeloLocal != null) {
						conteudo = ComponentUtil.getModeloDocumentoManager().traduzirModelo(modeloLocal.getTipoEditor(), modelo.getModeloDocumento());
						if( StringUtil.isNotEmpty(conteudo)){ 
							documento.setModeloDocumentoSessao(conteudo);
							DocumentoSessaoManager.instance().mergeAndFlush(documento);
							modeloDocumento = conteudo;
						}
					}
				}
			}
			FacesMessages.instance().add(Severity.INFO, "A minuta do pregão foi alterada com sucesso!");
		}
		catch (Exception e) {			
			FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar a minuta do pregão: " + e.getLocalizedMessage());			
		}
	}
	
	public void confirmarAtualizacaoMinuta() {
		setExibeMPConfirmacaoAtualizacaoMinuta(true);
	}

	@Override
	public String getEstilosFormatacao() {
		return ComponentUtil.getEditorEstiloService().recuperarEstilosJSON();
	}

	@Override
	public boolean isFormularioPreenchido() {
		return true;
	}

	public String getModeloDocumento() {
		return modeloDocumento;
	}
	
	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}
	
	public Integer getIdSessao() {
		return idSessao;
	}

	public void setIdSessao(Integer idSessao) {
		this.idSessao = idSessao;
	}
	
	public boolean isExibeMPConfirmacaoAtualizacaoMinuta() {
		return exibeMPConfirmacaoAtualizacaoMinuta;
	}

	public void setExibeMPConfirmacaoAtualizacaoMinuta(boolean exibeMPConfirmacaoAtualizacaoMinuta) {
		this.exibeMPConfirmacaoAtualizacaoMinuta = exibeMPConfirmacaoAtualizacaoMinuta;
	}

}
