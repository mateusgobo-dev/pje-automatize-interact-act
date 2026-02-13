/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoVisibilidadeSegredoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoVisibilidadeSegredo;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * @author cristof
 * 
 */
@Name("processoDocumentoVisibilidadeSegredoManager")
public class ProcessoDocumentoVisibilidadeSegredoManager extends BaseManager<ProcessoDocumentoVisibilidadeSegredo>{

	private static final long serialVersionUID = 1L;
	@In
	private ProcessoDocumentoVisibilidadeSegredoDAO processoDocumentoVisibilidadeSegredoDAO;

	@Override
	public ProcessoDocumentoVisibilidadeSegredoDAO getDAO(){
		return processoDocumentoVisibilidadeSegredoDAO;
	}

	public boolean visivel(ProcessoDocumento pd, Usuario u){
		return processoDocumentoVisibilidadeSegredoDAO.visivel(pd, u);
	}
	
	public boolean visivel(ProcessoDocumento pd, Usuario u, Procuradoria procuradoria){
		return processoDocumentoVisibilidadeSegredoDAO.visivel(pd, u, procuradoria);
	}

	public boolean acrescentaVisualizador(ProcessoDocumento pd, Pessoa pessoa, boolean flush) throws PJeBusinessException {
		return acrescentaVisualizador(pd, pessoa,null, flush);
	}
	
	public boolean acrescentaVisualizador(ProcessoDocumento pd, Pessoa pessoa, Procuradoria procuradoria, boolean flush) throws PJeBusinessException {
		if(!visivel(pd, pessoa)){
			ProcessoDocumentoVisibilidadeSegredo pdvs = criarNovaInstancia(pd, pessoa, procuradoria);
			
			if(flush){
				persistAndFlush(pdvs);
			}else{
				persist(pdvs);
			}
			return true;
		}
		return false;
	}
	
	public ProcessoDocumentoVisibilidadeSegredo criarNovaInstancia(ProcessoDocumento pd, Pessoa pessoa, Procuradoria procuradoria) {
		ProcessoDocumentoVisibilidadeSegredo pdvs = new ProcessoDocumentoVisibilidadeSegredo();
		
		pdvs.setPessoa(pessoa);
		pdvs.setProcessoDocumento(pd);
		pdvs.setProcuradoria(procuradoria);		
		return pdvs;
	}	

}
