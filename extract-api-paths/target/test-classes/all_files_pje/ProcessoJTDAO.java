package br.com.jt.pje.dao;

import java.util.List;

import javax.persistence.Query;

import br.com.infox.core.dao.GenericDAO;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.estatistica.Periodo;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

public class ProcessoJTDAO extends GenericDAO{
	
	@SuppressWarnings("unchecked")
	public List<ProcessoJT> getProcessoJTListByOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		StringBuilder hql = new StringBuilder("select o from ProcessoJT o ");
		hql.append("where o.processoTrf.orgaoJulgador.idOrgaoJulgador = :id_orgao_julgador");
		
		Query q = EntityUtil.createQuery(hql.toString());
		q.setParameter("id_orgao_julgador", orgaoJulgador.getIdOrgaoJulgador());
		
		List<ProcessoJT> result = q.getResultList();
		
		return result;
	}
	
	// TODO método ainda não implementado
	public void invalidarRelatoriosByPeriodo(Periodo periodo, OrgaoJulgador orgaoJulgador) {
		//StringBuilder hql = new StringBuilder("update Relatorio set valido = false where ")
		throw new UnsupportedOperationException("Método ProcessoJTDAO.invalidarRelatoriosByPeriodo(Periodo, OrgaoJulgador) ainda por fazer.");
	}
}
