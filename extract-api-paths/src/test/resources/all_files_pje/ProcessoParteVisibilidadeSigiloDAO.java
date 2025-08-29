/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoVisibilidadeSegredo;

/**
 * Componente de acesso a dados da entidade {@link ProcessoParteVisibilidadeSigilo}.
 * 
 * @author cristof
 *
 */
@Name("processoParteVisibilidadeSigiloDAO")
public class ProcessoParteVisibilidadeSigiloDAO extends BaseDAO<ProcessoParteVisibilidadeSigilo> {
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	public Integer getId(ProcessoParteVisibilidadeSigilo e) {
		return e.getIdProcessoParteVisibilidadeSigilo();
	};
	
	public boolean visivel(ProcessoParte parte, Pessoa pessoa){
		String query = "SELECT COUNT(v.idProcessoParteVisibilidadeSigilo) FROM ProcessoParteVisibilidadeSigilo AS v " +
				"	WHERE v.processoParte = :parte " +
				"		AND v.pessoa = :pessoa";
		Query q = entityManager.createQuery(query);
		q.setParameter("parte", parte);
		q.setParameter("pessoa", pessoa);
		q.setMaxResults(1);
		Number cont = (Number) q.getSingleResult();
		return cont.intValue() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoParteVisibilidadeSigilo> recuperarVisualizadores(ProcessoParte parte) {
		Query q = entityManager.createQuery("SELECT ppvs FROM ProcessoParteVisibilidadeSigilo AS ppvs WHERE ppvs.processoParte.idProcessoParte = :idProcessoParte");
		q.setParameter("idProcessoParte", parte.getIdProcessoParte());
		return q.getResultList();
	}

}
