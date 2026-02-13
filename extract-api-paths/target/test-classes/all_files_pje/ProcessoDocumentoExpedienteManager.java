/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoExpedienteDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

/**
 * @author cristof
 * 
 */
@Name("processoDocumentoExpedienteManager")
public class ProcessoDocumentoExpedienteManager extends BaseManager<ProcessoDocumentoExpediente>{

	@In
	private ProcessoDocumentoExpedienteDAO processoDocumentoExpedienteDAO;

	@Override
	protected ProcessoDocumentoExpedienteDAO getDAO(){
		return processoDocumentoExpedienteDAO;
	}

	/**
	 * @return ProcessoDocumentoExpedienteManager
	 */
	public static ProcessoDocumentoExpedienteManager instance() {
		return ComponentUtil.getComponent(ProcessoDocumentoExpedienteManager.class);
	}
	
	public ProcessoDocumentoExpediente getDocumentoReferido(){
		ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();
		pde.setAnexo(false);
		pde.setDtImpressao(null);
		return pde;
	}

	public ProcessoDocumentoExpediente getDocumentoReferido(ProcessoExpediente pe, ProcessoDocumento pd){
		ProcessoDocumentoExpediente pde = getDocumentoReferido();
		pde.setProcessoDocumento(pd);
		pde.setProcessoExpediente(pe);
		return pde;
	}

	public ProcessoDocumento getProcessoDocumentoAtoByExpediente(ProcessoExpediente pe){
		return processoDocumentoExpedienteDAO.getProcessoDocumentoAtoByExpediente(pe);
	}

	public boolean temExpedienteVinculado(ProcessoDocumento processoDocumento){
		return processoDocumentoExpedienteDAO.existeDocumentoExpedienteComAto(processoDocumento);
	}

	public List<ProcessoDocumento> getListaProcessoDocumentoVinculadoAto(ProcessoDocumento ato){
		return processoDocumentoExpedienteDAO.getListaProcessoDocumentoVinculadoAto(ato);
	}
	/**
	 * Retorna a lista de processo documento de um expediente.  
	 * 
	 *  
	 * @param ProcessoExpediente
	 * @author Eduardo Paulo
	 * @since 10/06/2015
	 * @return Uma lista de ProcessoDocumento
	 */
	public List<ProcessoDocumento> getListaProcessoDocumentoVinculadoExpediente(ProcessoExpediente pe){
		return processoDocumentoExpedienteDAO.getListaProcessoDocumentoVinculadoExpediente(pe);
	}

}
