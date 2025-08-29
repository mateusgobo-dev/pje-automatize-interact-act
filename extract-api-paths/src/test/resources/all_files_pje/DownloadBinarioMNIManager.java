/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.jus.cnj.pje.business.dao.DownloadBinarioMNIDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.mni.entidades.DownloadBinario;
import br.jus.pje.mni.entidades.DownloadBinarioArquivo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial de execução de recuperação de documentos binários
 * agendados.
 * 
 * @author cristof
 *
 */
@Name("downloadBinarioMNIManager")
public class DownloadBinarioMNIManager extends BaseManager<DownloadBinario> {
	
	@In
	private DownloadBinarioMNIDAO downloadBinarioMNIDAO;

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.nucleo.manager.BaseManager#getDAO()
	 */
	@Override
	protected DownloadBinarioMNIDAO getDAO() {
		return downloadBinarioMNIDAO;
	}
	
	@Transactional
	public List<Integer> recuperaIdentificadores(){
		Search s = new Search(DownloadBinario.class);
		s.setRetrieveField("id");
		return list(s);
	}
	
	@Transactional
	public List<Integer> recuperaIdentificadoresPorProcesso(String numeroProcesso){
		Search s = new Search(DownloadBinario.class);
		addCriteria(s, 
				Criteria.equals("numeroProcesso", numeroProcesso));		
		s.setRetrieveField("id");
		return list(s);
	}	
	
	public boolean haAgendamentos(ProcessoTrf processo){
		Search s = new Search(DownloadBinario.class);
		addCriteria(s, 
				Criteria.equals("manifestacaoProcessual.processoTrf", processo));
		return count(s) > 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> recuperaIdentificadoresDocumentos(String numeroProcessoOriginario){
		Search s = new Search(DownloadBinarioArquivo.class);
		addCriteria(s, 
				Criteria.equals("downloadBinario.numeroProcesso", numeroProcessoOriginario));
		s.setRetrieveField("idArquivoOrigem");
		List<String> ids = list(s);
		return ids;
	}
	
	public Set<Integer> recuperaIdentificadoresConteudos(ProcessoTrf processo){
		Search s = new Search(DownloadBinarioArquivo.class);
		addCriteria(s, 
				Criteria.equals("downloadBinario.manifestacaoProcessual.processoTrf", processo));
		s.setRetrieveField("idProcessoDocumentoBin");
		List<Integer> list = list(s);
		return new HashSet<Integer>(list);
	}
	
	@Transactional
	public void removerDownloadBinario(DownloadBinario dwb) throws PJeBusinessException{
			DownloadBinario dwbD = downloadBinarioMNIDAO.find(dwb.getId());
			downloadBinarioMNIDAO.remove(dwbD);
			downloadBinarioMNIDAO.flush();
	}

}
