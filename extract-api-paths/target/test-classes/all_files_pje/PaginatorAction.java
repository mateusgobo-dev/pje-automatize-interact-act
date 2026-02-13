package br.com.infox.cliente.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.Identity;
import org.richfaces.component.html.HtmlDataTable;
import org.richfaces.function.RichFunction;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(PaginatorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PaginatorAction extends ProcessoDocumentoBinAction implements Serializable{

	private static final long serialVersionUID = 1L;

	public final static String NAME = "paginatorAction";

	private boolean acessoTerceiros = false;
	private int pagina = 1;
	private int inicio = 1;
	private int inicioAssociados = 1;
	private Map<ProcessoDocumento, List<ProcessoDocumento>> documentosVinculados = new HashMap<ProcessoDocumento, List<ProcessoDocumento>>();
	private ProcessoDocumento processoDocumentoAtual;
	
	List<ProcessoDocumento> documentosSemPermissaoVisualizacao;
	private List<ProcessoDocumento> documentosSemPermissaoVisualizacaoComAnexos;
	
	public List<ProcessoDocumento> filtrarDocumentosSemPermissaoVisualizacao(List<ProcessoDocumento> processoDocumentoList){
		if (documentosSemPermissaoVisualizacao == null) {
			documentosSemPermissaoVisualizacao = new ArrayList<ProcessoDocumento>();
			documentosSemPermissaoVisualizacaoComAnexos = new ArrayList<ProcessoDocumento>();
			for (ProcessoDocumento processoDocumento : processoDocumentoList){
				if (podeVisualizar(processoDocumento) && !(ProcessoDocumentoHome.isUsuarioExterno() && ProcessoDocumentoHome.instance().existePendenciaCienciaSemCache(processoDocumento))){
					if(processoDocumento.getDocumentoPrincipal() == null || 
							processoDocumento.getDocumentoPrincipal().equals(processoDocumento)){
						documentosSemPermissaoVisualizacaoComAnexos.add(processoDocumento);
					}
					
					if(processoDocumento.getDocumentoPrincipal() == null){		
						documentosSemPermissaoVisualizacao.add(processoDocumento);
					}
				}
			}
		}
		return documentosSemPermissaoVisualizacao;
	}
	public List<ProcessoDocumento> filtrarDocumentosAnexos(ProcessoDocumento processoDocumento){
		List<ProcessoDocumento> ret = new ArrayList<ProcessoDocumento>();
		if(processoDocumento == null){
			return ret;
		}
		
		if(documentosVinculados.containsKey(processoDocumento)){
			return documentosVinculados.get(processoDocumento);
		}
		
		for(ProcessoDocumento pd : processoDocumento.getDocumentosVinculados()){
			if(pd.getDataJuntada() == null){
				continue;
			}
			if(pd.getDocumentoSigiloso()){
				if(Authenticator.isMagistrado()
					|| Authenticator.isVisualizaSigiloso()
					|| pd.getUsuarioInclusao().getIdUsuario().equals(Authenticator.getUsuarioLogado().getIdUsuario())){
					ret.add(pd);
				}
				else if(pd.getVisualizadores().size() > 0){
					Pessoa logada = Authenticator.getPessoaLogada();
					for(ProcessoDocumentoVisibilidadeSegredo segredo : pd.getVisualizadores()){
						if(segredo.getPessoa().equals(logada)){
							ret.add(pd);
							break;
						}
					}
				}
			}else{
				ret.add(pd);
			}
		}
		documentosVinculados.put(processoDocumento, ret);
		return ret;
	}

	public boolean podeVisualizar(ProcessoDocumento processoDocumento){
		Map<String, Boolean> mapPermissoes = getRenderedMap(processoDocumento);
		Boolean permissao = this.acessoTerceiros ? 
				mapPermissoes.get(PODE_VISUALIZAR_DOCUMENTO_TERCEIROS) : mapPermissoes.get(PODE_VISUALIZAR_DOCUMENTO);
				
		return permissao == null ? false : permissao.booleanValue();
	}

	public void setAcessoTerceiros(boolean isAcessoTerceiros){
		this.acessoTerceiros = isAcessoTerceiros;
	}

	public boolean isAcessoTerceiros(){
		return acessoTerceiros;
	}

	public List<ProcessoDocumento> getDocumentosSemPermissaoVisualizacao() {
		return documentosSemPermissaoVisualizacao;
	}

	public void setDocumentosSemPermissaoVisualizacao(
			List<ProcessoDocumento> documentosSemPermissaoVisualizacao) {
		this.documentosSemPermissaoVisualizacao = documentosSemPermissaoVisualizacao;
	}
	
	public void inverterOrdenacao(){
		Collections.reverse(documentosSemPermissaoVisualizacao);
		Collections.reverse(documentosSemPermissaoVisualizacaoComAnexos);
	}

	public List<ProcessoDocumento> getDocumentosSemPermissaoVisualizacaoComAnexos() {
		return documentosSemPermissaoVisualizacaoComAnexos;
	}

	public void setDocumentosSemPermissaoVisualizacaoComAnexos(
			List<ProcessoDocumento> documentosSemPermissaoVisualizacaoComAnexos) {
		this.documentosSemPermissaoVisualizacaoComAnexos = documentosSemPermissaoVisualizacaoComAnexos;
	}

	/**
	 * Método responsável por exibir o documento selecionado pelo usuário na área "documento" do paginador
	 * @param idProcessoDocumentoBin Identificador do documento selecionado pelo usuário
	 */
	public void exibirDocumentoNoPaginador(int idProcessoDocumentoBin) {
		HtmlDataTable dataTable = (HtmlDataTable) RichFunction.findComponent("documentosGrid");
		

		if (dataTable == null) {
			dataTable = (HtmlDataTable) RichFunction.findComponent("documentosAssistenteGrid");
		}
		
		if (dataTable != null) {
			int indiceOriginal = dataTable.getRowIndex();
			
			for (int i = 0; i < dataTable.getRowCount(); i++) {
				dataTable.setRowIndex(i);
				ProcessoDocumento processoDocumento = (ProcessoDocumento) dataTable.getRowData();
				
				if (processoDocumento.getProcessoDocumentoBin().getIdProcessoDocumentoBin() == idProcessoDocumentoBin) {
					// Definir o registro a ser exibido
					dataTable.setRowIndex(i);
					setPagina(i + 1);
					return;
				}
			}
			
			dataTable.setRowIndex(indiceOriginal);
		}
	}
	public int getPagina() {
		return pagina;
	}
	public void setPagina(int pagina) {
		this.pagina = pagina;
	}
	public int getInicio() {
		return inicio;
	}
	public void setInicio(int inicio) {
		this.inicio = inicio;
	}
	public int getInicioAssociados() {
		return inicioAssociados;
	}
	public void setInicioAssociados(int inicioAssociados) {
		this.inicioAssociados = inicioAssociados;
	}
	public Map<ProcessoDocumento, List<ProcessoDocumento>> getDocumentosVinculados() {
		return documentosVinculados;
	}
	public void setDocumentosVinculados(Map<ProcessoDocumento, List<ProcessoDocumento>> documentosVinculados) {
		this.documentosVinculados = documentosVinculados;
	}
	
	/**
	 * Consulta os documentos para serem exibidos no paginador.
	 * 
	 * @return Coleção de documentos.
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> consultarColecaoProcessoDocumento() {
		GridQuery gridQuery = (GridQuery) ComponentUtil.getComponent("processoTrfDocumentoPaginatorGrid");
		List<ProcessoDocumento> documentos = gridQuery.getFullList();
		return filtrarDocumentosSemPermissaoVisualizacao(documentos);
	}
	
	/**
	 * Consulta os documentos do tipo 'Comunicação entre Instâncias' para serem exibidos no paginador.
	 * 
	 * @return Coleção de documentos.
	 */
	public List<ProcessoDocumento> consultarColecaoProcessoDocumentoComunicacao(Boolean isJuntado) {
		
		consultarColecaoProcessoDocumento();
		TipoProcessoDocumento tipoComunicacao = ParametroUtil.instance().getTipoProcessoDocumentoComunicacaoEntreInstancias();
		
		if (tipoComunicacao != null) {
			List<ProcessoDocumento> documentosSemPermissaoVisualizacaoNovo = new ArrayList<ProcessoDocumento>();
			List<ProcessoDocumento> documentosSemPermissaoVisualizacaoComAnexoNovo = new ArrayList<ProcessoDocumento>();
			
			for (ProcessoDocumento documento : getDocumentosSemPermissaoVisualizacao()) {
				if (((isJuntado == null) || (documento.getDataJuntada() != null) == isJuntado) &&
					tipoComunicacao.getCodigoDocumento().equals(documento.getTipoProcessoDocumento().getCodigoDocumento())) {
					documentosSemPermissaoVisualizacaoNovo.add(documento);
				}
			}
			for (ProcessoDocumento documento : getDocumentosSemPermissaoVisualizacaoComAnexos()) {
				if (((isJuntado == null) || (documento.getDataJuntada() != null) == isJuntado) &&
					tipoComunicacao.getCodigoDocumento().equals(documento.getTipoProcessoDocumento().getCodigoDocumento())) {
					documentosSemPermissaoVisualizacaoComAnexoNovo.add(documento);
				}
			}
			documentosSemPermissaoVisualizacao = documentosSemPermissaoVisualizacaoNovo;
			documentosSemPermissaoVisualizacaoComAnexos = documentosSemPermissaoVisualizacaoComAnexoNovo;
		}
		return documentosSemPermissaoVisualizacao;
	}
	
	/**
	 * @return processoDocumentoAtual.
	 */
	public ProcessoDocumento getProcessoDocumentoAtual() {
		return processoDocumentoAtual;
	}

	/**
	 * @param processoDocumentoAtual processoDocumentoAtual.
	 */
	public void setProcessoDocumentoAtual(ProcessoDocumento processoDocumentoAtual) {
		this.processoDocumentoAtual = processoDocumentoAtual;
	}
}