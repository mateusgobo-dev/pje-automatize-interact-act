package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ManifestacaoProcessualDAO.NAME)
public class ManifestacaoProcessualDAO extends BaseDAO<ManifestacaoProcessual>{
	
	public static final String NAME = "manifestacaoProcessualDAO";

	@Override
	public Object getId(ManifestacaoProcessual e) {
		return e.getIdManifestacaoProcessual();
	}
	
	public ManifestacaoProcessual buscaSemWsdl(ProcessoTrf processoTrf) {
		String hql = "select mp from ManifestacaoProcessual mp where mp.processoTrf = :p and mp.wsdlOrigemEnvio is null";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("p", processoTrf);
		
		return EntityUtil.getSingleResult(q);
	}

	public ManifestacaoProcessual buscaUltimoEntregue(ProcessoTrf processoTrf) {
		StringBuilder hql = new StringBuilder("select mp from ManifestacaoProcessual mp where mp.processoTrf = :p");
		hql.append(" and mp.wsdlOrigemEnvio is not null ");
		hql.append(" and mp.dataRecebimento = ( ");
		hql.append(" 								select max(dataRecebimento) from ManifestacaoProcessual mp2 ");
		hql.append(" 								  where mp2.processoTrf = :p and mp2.wsdlOrigemEnvio is not null ");
		hql.append(" 						  ) ");
		
		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("p", processoTrf);
		
		return EntityUtil.getSingleResult(q);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ManifestacaoProcessual> findByProcessoTrf(ProcessoTrf processoTrf) {
		String hql = "select mp from ManifestacaoProcessual mp where mp.processoTrf.idProcessoTrf = :p";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("p", processoTrf.getIdProcessoTrf());
		
		return q.getResultList();
	}
}
