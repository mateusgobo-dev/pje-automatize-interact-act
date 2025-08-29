package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoExpDocCertidaoDAO;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpDocCertidao;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
@Name(ProcessoExpDocCertidaoManager.NAME)
public class ProcessoExpDocCertidaoManager extends BaseManager<ProcessoExpDocCertidao>{

	public static final String NAME = "processoExpDocCertidaoManager";

	@In
	private ProcessoExpDocCertidaoDAO processoExpDocCertidaoDAO;
	
	@Override
	protected BaseDAO<ProcessoExpDocCertidao> getDAO() {
		// TODO Auto-generated method stub
		return this.processoExpDocCertidaoDAO;
	}
	
	public ProcessoDocumento retornaCertidao(ProcessoParteExpediente ppe) {	
		return processoExpDocCertidaoDAO.retornaCertidao(ppe);
	}
	
	public ProcessoExpDocCertidao retornaPorProcessoDocumentoEhProcessoParteExpediente(ProcessoDocumento pd, ProcessoParteExpediente ppe) throws NoSuchFieldException{
		List<ProcessoExpDocCertidao> pdList =  new ArrayList<ProcessoExpDocCertidao>();
		
		Search s = new Search(ProcessoExpDocCertidao.class);
		
		s.addCriteria(Criteria.equals("processoDocumentoCertidao", pd));
		s.addCriteria(Criteria.equals("processoParteExpediente", ppe));
		pdList = list(s);
		
		if(pdList != null && !pdList.isEmpty()){
			return pdList.get(0);
		} else {
			return null;
		}
	}
	
}
