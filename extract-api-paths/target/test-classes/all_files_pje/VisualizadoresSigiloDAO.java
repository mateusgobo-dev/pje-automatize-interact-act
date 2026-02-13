package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.VisualizadoresSigilo;
import br.jus.pje.nucleo.util.DateUtil;
import java.util.Collections;

@Name("visualizadoresSigiloDAO")
public class VisualizadoresSigiloDAO extends BaseDAO<VisualizadoresSigilo> {
	
	private static final String SELECT_VISUALIZADORES_QUERY = "select o from VisualizadoresSigilo o ";

	@Override
	public Object getId(VisualizadoresSigilo vs) {
		return vs.getIdVisualizadoresSigilo();
	}

	/**
	 * Recupera a lista de visualizadores de sigilo de um determinado órgão julgador
	 * 
	 * @param orgaoJulgador
	 * @return Lista dos visualizadores
	 */
	public List<VisualizadoresSigilo> getVisualizadoresSigiloOJ(OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder();
		sb.append(SELECT_VISUALIZADORES_QUERY);
		sb.append("where o.orgaoJulgador = :oj ");	
		sb.append("and o.dtInicio <= :dataAtualInicio ");
		sb.append("and (o.dtFinal is null or o.dtFinal >= :dataAtualFinal) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("oj", orgaoJulgador);
		q.setParameter("dataAtualInicio", DateUtil.getBeginningOfToday());
		q.setParameter("dataAtualFinal", DateUtil.getEndOfToday());
		
		try {
			@SuppressWarnings("unchecked")
			List<VisualizadoresSigilo> resultList = q.getResultList();		
	 		return resultList;
		} catch(Exception ex) {
			return Collections.emptyList();
		}
	}
	
	/**
	 * Recupera o visualizador de sigilo de um determinado servidor
	 * 
	 * @param pessoaServidor
	 * @return Visualizador
	 */
	public List<VisualizadoresSigilo> getVisualizadoresSigiloPorServidor(PessoaServidor pessoaServidor) {
		StringBuilder sb = new StringBuilder();
		sb.append(SELECT_VISUALIZADORES_QUERY);
		sb.append("where o.funcionario = :f ");	
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("f", pessoaServidor);
		
		try {
			@SuppressWarnings("unchecked")
			List<VisualizadoresSigilo> resultList = q.getResultList();	
	 		return resultList;
		}catch(Exception ex) {
	 		return Collections.emptyList();
		}
	 	
	}

	public VisualizadoresSigilo getVisualizadoresSigiloByIdUsuario(Integer idUsuario, OrgaoJulgador orgaoJulgador) {
		StringBuilder sb = new StringBuilder();
		sb.append(SELECT_VISUALIZADORES_QUERY);
		sb.append("where o.funcionario.idUsuario = :id ");	
		sb.append("and o.orgaoJulgador = :oj ");	
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("id", idUsuario);
		q.setParameter("oj", orgaoJulgador);
		
		try {
			Object result = q.getSingleResult();
			return (VisualizadoresSigilo) result;
		} catch(Exception ex) {
			return null;
		}

	}
	

}
