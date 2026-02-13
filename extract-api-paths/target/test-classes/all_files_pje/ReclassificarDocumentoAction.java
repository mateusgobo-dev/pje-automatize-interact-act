/**
 *  pje-1.x
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;

/**
 * Componente de controle de tela da view /Processo/Fluxo/documento/reclassificar.xhtml
 * 
 * @author cristof
 *
 */
@Name("reclassificarDocumentoAction")
@Scope(ScopeType.CONVERSATION)
public class ReclassificarDocumentoAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8003070338572941606L;
	
	private String transicaoPadrao;
	
	private List<ProcessoDocumento> documentos;
	
	private List<TipoProcessoDocumento> tipos;
	
	private ProcessoTrf processo;
	
	private boolean autorizado = true;
	
	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private Identity identity;
	
	@In
	private TaskInstanceHome taskInstanceHome;
	
	@Create
	public void init(){
		if(!identity.hasRole(Papeis.PODE_RECLASSIFICAR_DOCUMENTO)){
			autorizado = false;
			return;
		}
		try {
			processo = tramitacaoProcessualService.recuperaProcesso();
			tipos = documentoJudicialService.getTiposAtivos();
			documentos = documentoJudicialService.getDocumentos(processo);
			transicaoPadrao = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao inicializar o componente: {0}.", e.getLocalizedMessage());
		}
	}
	
	/**
	 * Descarta as alterações realizadas em tela.
	 * 
	 */
	public void descartar(){
		try {
			tipos = documentoJudicialService.getTiposAtivos();
			documentos = documentoJudicialService.getDocumentos(processo);
		}
		catch(Exception e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recarregar os documentos do processo e seus tipos.");
		}
	}
	
	public void finalizar(){
		try {
			documentoJudicialService.flush();
			if(transicaoPadrao != null && !transicaoPadrao.isEmpty()){
				try{
					taskInstanceHome.end(transicaoPadrao);
				}catch(Exception e){
					logger.error("Houve um erro ao realizar a transição: {0}", e.getLocalizedMessage());
					facesMessages.add(Severity.ERROR, "Não foi possível realizar a transição. Por favor, encaminhe o processo manualmente.");
				}
			}
		} catch (PJeBusinessException e) {
			logger.error("Houve um erro ao realizar a transição: {0}", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar gravar as alterações.");
		}
	}

	/**
	 * @return the documentos
	 */
	public List<ProcessoDocumento> getDocumentos() {
		return documentos;
	}

	/**
	 * @param documentos the documentos to set
	 */
	public void setDocumentos(List<ProcessoDocumento> documentos) {
		this.documentos = documentos;
	}

	/**
	 * @return the tipos
	 */
	public List<TipoProcessoDocumento> getTipos() {
		return tipos;
	}
	
	public boolean isAutorizado() {
		return autorizado;
	}
	
}
