package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.FiltroDTO;
import br.jus.pje.nucleo.entidades.Filtro;

@Name("filtroDAO")
public class FiltroDAO extends BaseDAO<Filtro>{

	@Override
	public Integer getId(Filtro e){
		return e.getId();
	}

	@SuppressWarnings("unchecked")
	public List<Filtro> listarFiltrosParaAutomacao(Integer idLocalizacao){
		StringBuilder hql = new StringBuilder();
		hql.append("select distinct o from Filtro o join fetch o.criterios join fetch o.tags ");
		hql.append("where  o.idLocalizacao = :idLocalizacao ");
		
		Query q = getEntityManager().createQuery(hql.toString());

		q.setParameter("idLocalizacao", idLocalizacao);

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<FiltroDTO> listarFiltrosByTag(Integer idTag){
		StringBuilder hql = new StringBuilder();
		hql.append("select new br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.FiltroDTO(f.id, f.nomeFiltro, f.idLocalizacao, fg.idTagHerdado) ");
		hql.append("from Filtro f, FiltroTag fg ");
		hql.append("where fg.idFiltro=f.id and fg.idTag=:idTag ");
		
		Query q = getEntityManager().createQuery(hql.toString());

		q.setParameter("idTag", idTag);

		return q.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Filtro> listarFiltros(CriterioPesquisa crit, Integer idLocalizacao) {
		Query q = montaHqlListarFiltros(crit, idLocalizacao, false);
		return q.getResultList();
	}

	public Long listarQtdFiltros(CriterioPesquisa crit, Integer idLocalizacao) {
		Query q = montaHqlListarFiltros(crit, idLocalizacao, true);
		return (Long) q.getSingleResult();
	}

	private Query montaHqlListarFiltros(CriterioPesquisa crit, Integer idLocalizacao, boolean count) {
		StringBuilder hql = new StringBuilder();
		if (count) {
			hql.append(" SELECT count(f) FROM Filtro f WHERE 1=1 ");
		} else {
			hql.append(" SELECT f FROM Filtro f WHERE 1=1 ");
		}
		hql.append(" AND f.idLocalizacao = :idLocalizacao ");

		if (crit != null) {
			if (StringUtils.isNotEmpty(crit.getNomeFiltro())) {
				hql.append("and lower(to_ascii(f.nomeFiltro)) like '%' || lower(to_ascii(:nomeFiltro)) || '%' ");
			}
		}

		if (!count) {
			hql.append(" order by f.nomeFiltro");
		}

		Query q = getEntityManager().createQuery(hql.toString());
		q.setParameter("idLocalizacao", idLocalizacao);

		if (crit != null) {
			if (StringUtils.isNotEmpty(crit.getNomeFiltro())) {
				q.setParameter("nomeFiltro", crit.getNomeFiltro());
			}
		}

		if (crit != null && crit.getPage() != null && !count) {
			q.setFirstResult(crit.getPage());
		}
		if (crit != null && crit.getMaxResults() != null && !count) {
			q.setMaxResults(crit.getMaxResults());
		}

		return q;
	}

	public Filtro recuperarPorIdCompleto(Integer id) {
		String hql = "select f from Filtro f join fetch f.criterios left join fetch f.tags where f.id = :id";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("id", id);
		return (Filtro) q.getSingleResult();
	}

	public void excluirCriterios(Filtro filtro) {
		String hql = "delete from CriterioFiltro cf where cf.filtro.id = :idFiltro";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("idFiltro", filtro.getId());
		q.executeUpdate();
	}
	
	public void excluirPorId(Integer idFiltro) {
		String hql = "delete from Filtro o where o.id = :id";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("id",idFiltro);
		q.executeUpdate();
	}

	public void excluirPorIdTag(Integer idTag) {
		String hql = "delete from Filtro f where f.id IN (select ft.idFiltro from FiltroTag ft where ft.idTagHerdado is null and ft.idTag=:idTag)";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("idTag", idTag);
		q.executeUpdate();	
	}	
	
}
