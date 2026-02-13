package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoAssuntoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(ProcessoAssuntoManager.NAME)
public class ProcessoAssuntoManager extends BaseManager<ProcessoAssunto>{
	public static final String NAME = "processoAssuntoManager";

	@In
	private ProcessoAssuntoDAO processoAssuntoDAO;
	
	@Override
	protected ProcessoAssuntoDAO getDAO() {
		return processoAssuntoDAO;
	}
	
	public static ProcessoAssuntoManager instance() {
		return ComponentUtil.getComponent(ProcessoAssuntoManager.class);
	}
	
	public boolean temAssunto(ProcessoTrf processo, AssuntoTrf assunto) {
		return processoAssuntoDAO.temAssunto(processo, assunto);
	}
	
	public List<ProcessoAssunto> retornaAssuntos(Integer idProcessoTrf){
		Search s = new Search(ProcessoAssunto.class);
		addCriteria(s, Criteria.equals("processoTrf.idProcessoTrf", idProcessoTrf));
		return list(s);
	}
	
	public ProcessoAssunto criaNovo(AssuntoTrf assuntoTrf, ProcessoTrf processoTrf) throws PJeBusinessException {
		ProcessoAssunto processoAssunto = new ProcessoAssunto();
		processoAssunto.setAssuntoTrf(assuntoTrf);
		processoAssunto.setAssuntoPrincipal(Boolean.TRUE);
		processoAssunto.setProcessoTrf(processoTrf);
		
		return this.persist(processoAssunto);
	}

}
