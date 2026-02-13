package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ComplementoClasse;

@Name("complementoClasseDAO")
public class ComplementoClasseDAO extends BaseDAO<ComplementoClasse>{

	@Override
	public Object getId(ComplementoClasse e) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Método responsável por obter a lista de complemento de classe de acordo com 
	 * a classe judicial
	 * @param classeJudicial
	 * @return List<ComplementoClasse>
	 */
	@SuppressWarnings("unchecked")
	public List<ComplementoClasse> getListComplementoClasse(ClasseJudicial classeJudicial) {
		StringBuilder sb = new StringBuilder("select o from ComplementoClasse o ");
		sb.append("inner join o.classeAplicacao cat ")
		.append("where cat.classeJudicial = :classeJudicial");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("classeJudicial", classeJudicial);
		return q.getResultList();
	}
	
	public List<ComplementoClasse> getListComplementoClasse() {
		StringBuilder sb = new StringBuilder("select o from ComplementoClasse o ");
		sb.append("inner join o.classeAplicacao cat ");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}


}
