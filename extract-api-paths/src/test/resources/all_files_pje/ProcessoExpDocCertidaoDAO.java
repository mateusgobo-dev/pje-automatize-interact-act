package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoExpDocCertidao;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(ProcessoExpDocCertidaoDAO.NAME)
public class ProcessoExpDocCertidaoDAO extends BaseDAO<ProcessoExpDocCertidao> {
	
	public static final String NAME = "processoExpDocCertidaoDAO";

	@Override
	public Object getId(ProcessoExpDocCertidao e) {
		return e.getIdProcessoExpediente();
	}
	
	public ProcessoDocumento retornaCertidao(ProcessoParteExpediente ppe) {	
		ProcessoExpDocCertidao classe = null;
		try {
		classe = (ProcessoExpDocCertidao) EntityUtil.getEntityManager()
				.createQuery("select p from ProcessoExpDocCertidao p where p.processoDocumentoCertidao.ativo = true and p.processoParteExpediente = :ppe")
				.setParameter("ppe", ppe)
				.setFirstResult(0)
				.setMaxResults(1)
				.getSingleResult();
		} catch (Exception e) {
			// necessario para noresultexception, nao faz nada.
		}
		
		if (classe != null) {
			return classe.getProcessoDocumentoCertidao();
		} else {
			return null;
		}
	}

}
