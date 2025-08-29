/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.view.ICkEditorController;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.dto.BlocoJulgamentoDTO;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.TipoEditorEnum;

@Name(CertidaoJulgamentoBlocoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CertidaoJulgamentoBlocoAction implements Serializable, ArquivoAssinadoUploader, ICkEditorController {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "certidaoJulgamentoBlocoAction";
	private List<BlocoJulgamento> blocos;
	private BlocoJulgamento bloco;
	private List<BlocoJulgamentoDTO> blocosLote;
	private List<BlocoJulgamentoDTO> blocosLoteSelecionados;
	private String modeloDocumento;
	private boolean editando;
	private boolean expandirBloco;
	private boolean marcouTudo;
	private List<ArquivoAssinadoHash> arquivosAssinados = new ArrayList<ArquivoAssinadoHash>();
	
	@Override
	public String getActionName() {
		return NAME;
	}
	
  	public void marcarTudo() {
	  	for (BlocoJulgamentoDTO b : blocosLote){
			b.setCheck(marcouTudo);
		}
  		if(marcouTudo) {
	  		this.blocosLoteSelecionados = this.blocosLote;
  		} else {
  			this.blocosLoteSelecionados = null;
  			blocosLoteSelecionados = new ArrayList<BlocoJulgamentoDTO>(blocosLote.size());
  		}
  	}
  	
  	public boolean selecionarBloco(BlocoJulgamentoDTO bloco) {
  		boolean retorno = true;
  		if(bloco.isCheck()) {
  			retorno = blocosLoteSelecionados.add(bloco);
  		} else {
  			if(blocosLoteSelecionados.contains(bloco)) {
  				retorno = blocosLoteSelecionados.remove(bloco);
  			}
  		}
  		return retorno;
  	}

	public boolean apresentarBotaoAssinar(){
		boolean retorno = false;
		if(blocosLoteSelecionados.size() > 0 && ComponentUtil.getBlocoJulgamentoManager().verificarAptidaoAssinaturaCertidoes(blocosLoteSelecionados)) {
			retorno = !ComponentUtil.getTipoProcessoDocumentoPapelService().verificarExigibilidadeNaoAssina(Authenticator.getPapelAtual(), ParametroUtil.instance().getTipoProcessoDocumentoCertidaoJulgamento());
		}
		return retorno;
	}
	
	public boolean apresentarBotaoGerarCertidoes() {
		boolean retorno = false;
		if(blocosLoteSelecionados.size() > 0 && !(ComponentUtil.getBlocoJulgamentoManager().verificarAssinaturaCertidoes(blocosLoteSelecionados))) {
			retorno = true;
		}
		return retorno;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.arquivosAssinados.add(arquivoAssinadoHash);
	}
	
	public String getUrlDocsField() {
		return ComponentUtil.getDocumentoJudicialService().getDownloadLinks(ComponentUtil.getBlocoJulgamentoManager().recuperarDocumentosParaAssinatura(blocosLoteSelecionados));
	}
	
	public void concluir() {
		try {
			List<ProcessoDocumento> documentosParaAssinatura = ComponentUtil.getBlocoJulgamentoManager().recuperarDocumentosParaAssinatura(blocosLoteSelecionados);
			ComponentUtil.getDocumentoJudicialService().juntarEhGravarAssinaturaDeProcessosDocumentosNaoSigilosos(this.arquivosAssinados, documentosParaAssinatura);
			ComponentUtil.getBlocoJulgamentoManager().registrarAssinaturaCertidao(blocosLoteSelecionados);
			FacesMessages.instance().add(Severity.INFO, "As certidões de julgamento foram assinadas com sucesso!");
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar assinar as certidões " + e.getLocalizedMessage());
		}
	}
		
  	public List<BlocoJulgamentoDTO> recuperarBlocosLote(Sessao sessao) {
  		blocos = ComponentUtil.getBlocoJulgamentoManager().recuperarBlocosComProcessos(sessao, false, false);
  		blocosLote = new ArrayList<BlocoJulgamentoDTO>(blocos.size());
  		setBlocosLoteSelecionados(new ArrayList<BlocoJulgamentoDTO>(blocosLote.size()));
  		for (BlocoJulgamento b : blocos){
  			blocosLote.add(new BlocoJulgamentoDTO(b,false));
		}
  		setBlocosLoteSelecionados(new ArrayList<BlocoJulgamentoDTO>(blocosLote.size()));
  		return blocosLote;  
	}
  	
  	public void gravarCertidoes() {
  		try {
  			if(getModeloDocumento() != null) { 
				ComponentUtil.getBlocoJulgamentoManager().registrarCertidoes(getModeloDocumentoObjeto(),blocosLoteSelecionados);
				FacesMessages.instance().add(Severity.INFO, "As certidões foram geradas com sucesso!");
  			} else {
  				FacesMessages.instance().add(Severity.ERROR, "Não foi possível recuperar o modelo de certidão de julgamento");
  			}
		} catch (Exception e) {
			if(e instanceof PJeBusinessException) {
				if((((PJeBusinessException)e).getCode()).contains("Erro de interpretação na linha")) {
					FacesMessages.instance().add(Severity.ERROR, "Erro no modelo de documento de certidão de julgamento ao tentar gerar certidões, mensagem interna: " + ((PJeBusinessException)e).getCode());
				} else {
					FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar gerar certidões, mensagem interna: " + ((PJeBusinessException)e).getCode());
				}
			} else {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar gerar certidões, mensagem interna: " + e.getLocalizedMessage());
			}
		}
  	}
  	
  	public List<ProcessoBloco> recuperarProcessos() {
  		List<ProcessoBloco> processosBloco = null;
  		if(bloco != null) {
			try {
				processosBloco = ComponentUtil.getProcessoBlocoManager().recuperarProcessos(bloco);
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}  		
  		}
  		return processosBloco;
		
	}
  	
  	public void exibirDetalhesBloco(BlocoJulgamento blocoParametro) {
  		setExpandirBloco(true);
  		setBloco(blocoParametro);
  	}

	@Override
	public void salvar(String conteudo) {
		try {
			if(bloco != null) {
				if(isCk()) {
					bloco.setCertidaoJulgamento(conteudo);
				} else {
					bloco.setCertidaoJulgamento(getModeloDocumento());
				}
				ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
				if(isCk()) {
					modeloDocumento = conteudo;
				}
				FacesMessages.instance().add(Severity.INFO, "O modelo de certidão para o bloco foi alterado com sucesso!");
			} else {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar o modelo de certidão de julgamento para o bloco: não há bloco selecionado");				
			}
		}
		catch (Exception e) {			
			FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar o modelo de certidão de julgamento para o bloco: " + e.getLocalizedMessage());			
		}
	}

	@Override
	public String getEstilosFormatacao() {
		return ComponentUtil.getEditorEstiloService().recuperarEstilosJSON();
	}

	@Override
	public boolean isFormularioPreenchido() {
		boolean retorno = false;
		if(bloco != null && modeloDocumento != null) {
			retorno = true;
		}
		return retorno;
	}

	public List<BlocoJulgamento> getBlocos() {
		if(blocos == null) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocos;
	}

	public void setBlocos(List<BlocoJulgamento> blocos) {
		this.blocos = blocos;
	}


	public List<BlocoJulgamentoDTO> getBlocosLote() {
		if(blocosLote == null) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosLote;
	}

	public void setBlocosLote(List<BlocoJulgamentoDTO> blocosLote) {
		this.blocosLote = blocosLote;
	}

  	public List<BlocoJulgamentoDTO> getBlocosLoteSelecionados() {
		if(blocosLoteSelecionados == null) {
			blocosLote = recuperarBlocosLote(SessaoHome.instance().getInstance());
		}
		return blocosLoteSelecionados;
	}

	public void setBlocosLoteSelecionados(List<BlocoJulgamentoDTO> blocosLoteSelecionados) {
		this.blocosLoteSelecionados = blocosLoteSelecionados;
	}

	public String getModeloDocumento() {
		if(modeloDocumento == null) {
			if(ParametroUtil.instance().getModeloCertidaoJulgamento() != null) { 
  				ModeloDocumento modelo = ParametroUtil.instance().getModeloCertidaoJulgamento();
  				if( modelo != null ) {
  					modeloDocumento = modelo.getModeloDocumento();
  				}
			}
		}
		return modeloDocumento;
	}
	
	private ModeloDocumento getModeloDocumentoObjeto() {
		return ParametroUtil.instance().getModeloCertidaoJulgamento();
	}

	private ModeloDocumentoLocal getModeloDocumentoLocal() {
		ModeloDocumentoLocal retorno = null; 
		if(ParametroUtil.instance().getModeloCertidaoJulgamento() != null) {
			retorno = ComponentUtil.getModeloDocumentoLocalManager().findById(ParametroUtil.instance().getModeloCertidaoJulgamento().getIdModeloDocumento());
		}
		return retorno;
	}

	public void setModeloDocumento(String modeloDocumento) {
		this.modeloDocumento = modeloDocumento;
	}
	
	public void editarCertidao(BlocoJulgamento bloco) {
		this.modeloDocumento = bloco.getCertidaoJulgamento();
		setEditando(true);
		this.bloco = bloco;
	}
	
	public String isCertidaoSelecionada(BlocoJulgamento bloco) {
		String styleClass = "";
		
		if(this.bloco != null && this.bloco.getIdBlocoJulgamento() == bloco.getIdBlocoJulgamento()) {
			styleClass = "info";
		}
			
		return styleClass;
	}

	public boolean isEditando() {
		return editando;
	}

	public void setEditando(boolean editando) {
		this.editando = editando;
	}

	public boolean isMarcouTudo() {
		return marcouTudo;
	}

	public void setMarcouTudo(boolean marcouTudo) {
		this.marcouTudo = marcouTudo;
	}
	
	public BlocoJulgamento getBloco() {
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	public boolean isExpandirBloco() {
		return expandirBloco;
	}

	public void setExpandirBloco(boolean expandirBloco) {
		this.expandirBloco = expandirBloco;
	}

	public boolean isCk() {
		return getModeloDocumentoLocal() != null && TipoEditorEnum.C.equals(getModeloDocumentoLocal().getTipoEditor()); 
	}
}