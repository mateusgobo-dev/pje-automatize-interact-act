/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoPeticaoNaoLidaDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoTrf;


@Name("processoDocumentoPeticaoNaoLidaManager")
public class ProcessoDocumentoPeticaoNaoLidaManager extends BaseManager<ProcessoDocumentoPeticaoNaoLida>{

	@In
	private ProcessoDocumentoPeticaoNaoLidaDAO processoDocumentoPeticaoNaoLidaDAO;
	
	@Override
	protected BaseDAO<ProcessoDocumentoPeticaoNaoLida> getDAO(){
		return this.processoDocumentoPeticaoNaoLidaDAO;
	}
	
	public ProcessoDocumentoPeticaoNaoLida obterProcessoDocumentoPeticaoNaoLida(ProcessoDocumento processoDocumento){
		
		return this.processoDocumentoPeticaoNaoLidaDAO.obterProcessoDocumentoPeticaoNaoLida(processoDocumento);
	}

	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLida(){
		
		return this.processoDocumentoPeticaoNaoLidaDAO.obterProcessoDocumentoPeticaoNaoLida();
		
	}
	
	public void retirarDestaque(ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida){
		
		this.processoDocumentoPeticaoNaoLidaDAO.retirarDestaque(processoDocumentoPeticaoNaoLida);
	}

	/**
	 * Resgata os documento petição não lida do processo   
	 * 
	 *  
	 * @param ProcessoTrf que será resgatado os documentos
	 * @return uma <b>Lista</b> de objetos contendo as petições
	 */
	public List<ProcessoDocumentoPeticaoNaoLida> obterProcessoDocumentoPeticaoNaoLida(ProcessoTrf processoTrf) {
		return this.processoDocumentoPeticaoNaoLidaDAO.obterProcessoDocumentoPeticaoNaoLida(processoTrf);
	}
	
	/**
	 * Retorna a petição não lida, diferenciando habilitação nos autos ao petição avulsa  
	 * 
	 *  
	 * @param processoDocumento
	 * @param habilitacaoAutos
	 * @author lucas.raw
	 * @since 09/06/2015
	 * @return processoDocumentoPeticaoNaoLida
	 */
	public ProcessoDocumentoPeticaoNaoLida getProcessoDocumentoPeticaoNaoLida(ProcessoDocumento processoDocumento, Boolean habilitacaoAutos) {
		return this.processoDocumentoPeticaoNaoLidaDAO.getProcessoDocumentoPeticaoNaoLidaByDocumento(processoDocumento, habilitacaoAutos);
	}
	
	/**
	 * Identifica se um dado documento é relacionado a uma petição avulsa
	 * 
	 * @author lucas.raw
	 * @since 09/06/2015
	 * @param processoDocumento
	 * @return Boolean
	 */
	public Boolean isDocumentoDePeticaoAvulsa(ProcessoDocumento processoDocumento) {
		return this.getProcessoDocumentoPeticaoNaoLida(processoDocumento, false) != null;
	}
	
	/**
	 * Identifica se um dado documento é relacionado a uma habilitação nos autos
	 * 
	 * @author lucas.raw
	 * @since 09/06/2015
	 * @param processoDocumento
	 * @return Boolean
	 */
	public Boolean isDocumentoDeHabilitacaoAutos(ProcessoDocumento processoDocumento) {
		return this.getProcessoDocumentoPeticaoNaoLida(processoDocumento, true) != null;
	}
	
}
