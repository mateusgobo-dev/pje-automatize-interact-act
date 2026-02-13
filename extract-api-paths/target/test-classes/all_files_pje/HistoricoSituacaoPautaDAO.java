package br.jus.cnj.pje.business.dao;

import javax.persistence.NoResultException;
import org.jboss.seam.annotations.Name;

import br.jus.pje.jt.entidades.HistoricoSituacaoPauta;
import br.jus.pje.jt.entidades.PautaSessao;

@Name(HistoricoSituacaoPautaDAO.NAME)
public class HistoricoSituacaoPautaDAO extends BaseDAO<HistoricoSituacaoPauta>{
	public static final String NAME = "historicoSituacaoPautaDAO";

	@Override
	public Object getId(HistoricoSituacaoPauta e) {
		return e.getIdHistoricoSituacaoPauta();
	}

	public HistoricoSituacaoPauta getHistoricoComMesmaDataESessao(PautaSessao pautaSessao){
		try {
			return (HistoricoSituacaoPauta) getEntityManager()
					.createQuery("select o from HistoricoSituacaoPauta o where o.pautaSessao.idPautaSessao = :idPauta and o.dataSituacaoPauta = :data")
					.setParameter("idPauta", pautaSessao.getIdPautaSessao())
					.setParameter("data", pautaSessao.getDataSituacaoPauta())
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public HistoricoSituacaoPauta update(HistoricoSituacaoPauta e) {
		getEntityManager().merge(e);
		getEntityManager().flush();
		return e;
	}

}
