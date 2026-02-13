package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.CentralMandadoLocalizacao;

public class CentralMandadoLocalizacaoDAO extends BaseDAO<CentralMandadoLocalizacao>{

	@Override
	public Object getId(CentralMandadoLocalizacao e) {
		return e.getIdCentralMandadoLocalizacao();
	}
	
	/**
	 * Busca as localizações cadastradas conforme a central de mandado
	 * @param centralMandado
	 * @return lista de {@linkplain CentralMandadoLocalizacao}
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandadoLocalizacao> findByCentralMandado(CentralMandado centralMandado) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from CentralMandadoLocalizacao o where ");
		sb.append("o.centralMandado.idCentralMandado = :centralMandado ");
		sb.append("order by o.centralMandado");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("centralMandado", centralMandado.getIdCentralMandado());
		return query.getResultList();
	}
}
