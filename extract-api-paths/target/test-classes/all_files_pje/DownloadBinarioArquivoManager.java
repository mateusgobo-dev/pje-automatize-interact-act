/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.DownloadBinarioArquivoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.mni.entidades.DownloadBinarioArquivo;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link DownloadBinarioArquivo}.
 * 
 * @author cristof
 *
 */
@Name("downloadBinarioArquivoManager")
public class DownloadBinarioArquivoManager extends BaseManager<DownloadBinarioArquivo> {
	
	@In
	private DownloadBinarioArquivoDAO downloadBinarioArquivoDAO;

	@Override
	protected DownloadBinarioArquivoDAO getDAO() {
		return downloadBinarioArquivoDAO;
	}
	
	public DownloadBinarioArquivo recuperaPorIdentificadorOriginario(String identificadorOriginario){
		Search s = new Search(DownloadBinarioArquivo.class);
		addCriteria(s,
				Criteria.equals("idArquivoOrigem", identificadorOriginario));
		s.setMax(1);
		List<DownloadBinarioArquivo> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public boolean pendeRecuperacaoPorIdentificadorBinarioLocal(Integer binarioLocal){
		Search s = new Search(DownloadBinarioArquivo.class);
		addCriteria(s,
				Criteria.equals("idProcessoDocumentoBin", binarioLocal));
		s.setMax(1);
		return count(s) > 0;
	}
	
	
	public synchronized void removeById(int idDownloadBinario) throws PJeBusinessException{
		DownloadBinarioArquivo dba = downloadBinarioArquivoDAO.find(idDownloadBinario);
		downloadBinarioArquivoDAO.remove(dba);
		downloadBinarioArquivoDAO.flush();
		
	}

}
