package br.jus.cnj.pje.view.fluxo;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(VisualizarDecisaoAction.NAME)
@Scope(ScopeType.PAGE)
public class VisualizarDecisaoAction {
	
	public static final String NAME = "visualizarDecisaoAction";
	
	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;
	
	@In(create = true, required = true)
	private transient TaskInstanceUtil taskInstanceUtil;
	
	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	private ProcessoDocumento ultimoAto = null;
	
	@Create
	public void init() throws Exception {
		this.encontraUltimoAtoProferido();
	}
	
	/**
	 * A função retorna o último documento juntado no fluxo cujo tipo esteja marcado como AtoProferido, se não encontrar um documento válido, retorna o últmo documento juntado neste fluxo que seja válido,
	 * caso não encontre, busca o último documento do tipo ato proferito válido juntado ao processo, independente do fluxo
	 * @return
	 * @throws Exception
	 */
	private void encontraUltimoAtoProferido() throws Exception{
		ultimoAto = FluxoEL.instance().getAtoProferido();
		if (ultimoAto == null || ultimoAto.getDataExclusao() != null) {
			ultimoAto = FluxoEL.instance().getUltimoDocumentoJuntadoNesteFluxo();

			if(ultimoAto == null || ultimoAto.getDataExclusao() != null) {
				ProcessInstance pi = taskInstanceUtil.getProcessInstance();
				if(pi != null) {
					ProcessoTrf processoJudicial = processoJudicialManager.findByProcessInstance(pi);
					ultimoAto = documentoJudicialService.getUltimoAtoJudicial(processoJudicial.getProcesso());
				}
			}
		} else {
			ProcessoDocumentoBin bin = ultimoAto.getProcessoDocumentoBin();
			if (bin.isBinario()) {
				BinarioVO binario = new BinarioVO();
				binario.setIdBinario(bin.getIdProcessoDocumentoBin());
				binario.setMimeType(bin.getExtensao());
				binario.setNomeArquivo(bin.getNomeArquivo());
				binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
				Contexts.getSessionContext().set("download-binario", binario);
			}
		}
		
		if(ultimoAto == null || ultimoAto.getDataExclusao() != null) {
			ultimoAto = null;
		}
		
		if(ultimoAto != null) {
			ProcessoDocumentoBin bin = ultimoAto.getProcessoDocumentoBin();
			String extensao = bin.getExtensao();
			String html = null;
			if(bin.isBinario()) {
				BinarioVO binario = new BinarioVO();
				binario.setIdBinario(bin.getIdProcessoDocumentoBin());
				binario.setMimeType(extensao);
				binario.setNomeArquivo(bin.getNomeArquivo());
				binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
				binario.setHtml(html);
				Contexts.getSessionContext().set("download-binario", binario);
			}
			
		}
	}
	
	public ProcessoDocumento getUltimoAto(){
		return this.ultimoAto;
	}
}