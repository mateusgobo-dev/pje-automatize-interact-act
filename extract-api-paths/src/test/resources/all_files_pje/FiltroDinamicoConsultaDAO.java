/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.FiltroDinamicoConsulta;

/**
 * @author Everton nogueira pereira
 *
 */
@Name(FiltroDinamicoConsultaDAO.NAME)
public class FiltroDinamicoConsultaDAO extends BaseDAO<FiltroDinamicoConsulta>{
	public static final String NAME = "filtroDinamicoConsultaDAO";

	@SuppressWarnings("unchecked")
	public List<FiltroDinamicoConsulta> obtemConsultasByFuncionalidade(String funcionalidade) {
		StringBuilder sb = new StringBuilder();
		sb.append("select f from FiltroDinamicoConsulta f ");
		sb.append("where f.funcionalidade = :func ");
		sb.append("order by f.nomeConsulta "); 
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("func", funcionalidade);
		return q.getResultList();
	}

	@Override
	public Object getId(FiltroDinamicoConsulta e) {
		return e.getIdConsulta();
	}

	@SuppressWarnings("unchecked")
	public List<FiltroDinamicoConsulta> obtemTodasConsultas() {
		StringBuilder sb = new StringBuilder();
		sb.append("select f from FiltroDinamicoConsulta f ");
		sb.append("order by f.nomeConsulta "); 
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}
}
