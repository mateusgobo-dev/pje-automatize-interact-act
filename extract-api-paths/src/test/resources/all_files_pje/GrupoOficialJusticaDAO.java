package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;
/**
 * Classe responsavel por manipular consultas e regras para a entidade de GrupoOficialJustica
 */
@Name(GrupoOficialJusticaDAO.NAME)
public class GrupoOficialJusticaDAO extends BaseDAO<GrupoOficialJustica> {
	public static final String NAME = "grupoOficialJusticaDAO";

	public static GrupoOficialJusticaDAO instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	@Override
	public Object getId(GrupoOficialJustica grupoOficialJustica) {
		return grupoOficialJustica.getIdGrupoOficialJustica();
	}

	/**
	 * Metodo que retorna os Grupos de Oficiais que estao vinculados a determinadas centrais de mandado e/ou 
	 *     vinculados ao usuario
	 * 
	 * @param centralMandado Central de Mandado selecionada, caso seja null nao e usada na consulta
	 * @param idUsuario Id do usuario logado, caso seja null não e usado na consulta
	 * @param ativo GrupoOficalJustica esta ativo, caso seja null não é usado na consulta
	 * @return List de Grupos de Oficiais de Justica
	 */
	@SuppressWarnings("unchecked")
	public List<GrupoOficialJustica> obter(CentralMandado centralMandado, Integer idUsuario, Boolean ativo) {
		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append(" SELECT DISTINCT grupoOficial FROM GrupoOficialJustica grupoOficial ");
		hqlQuery.append(" 	 JOIN grupoOficial.pessoaGrupoOficialJusticaList pessoaGrupoOficial ");
		hqlQuery.append(" WHERE grupoOficial.idGrupoOficialJustica IS NOT NULL ");
		if(ativo != null){
			hqlQuery.append(" AND grupoOficial.ativo = :paramAtivo");
		}
		if(centralMandado != null){
			hqlQuery.append(" AND grupoOficial.centralMandado.idCentralMandado = :paramIdCentral");
		}
		if(idUsuario != null){
			hqlQuery.append(" AND pessoaGrupoOficial.pessoa.id = :paramIdUsuario");
		}
		hqlQuery.append(" ORDER BY grupoOficial.grupoOficialJustica");
		
		Query query = entityManager.createQuery(hqlQuery.toString());
		if(ativo != null){
			query.setParameter("paramAtivo", ativo);
		}
		if(centralMandado != null){
			query.setParameter("paramIdCentral", centralMandado.getIdCentralMandado());
		}
		if(idUsuario != null){
			query.setParameter("paramIdUsuario", idUsuario);
		}
		return query.getResultList();
	}
	
	/**
	 * Metodo que retorna os Grupos de Oficiais que estao vinculados as centrais de mandado.
	 * 
	 * @param centraisMandado Centrais de Mandado
	 * @param ativo GrupoOficalJustica esta ativo, caso seja null nao e usado na consulta
	 * @return List de Grupos de Oficiais de Justica das centrais de mandado
	 */
	@SuppressWarnings("unchecked")
	public List<GrupoOficialJustica> obter(List<CentralMandado> centraisMandado, Boolean ativo) {
		StringBuilder hqlQuery = new StringBuilder();
		hqlQuery.append(" SELECT grupoOficial FROM GrupoOficialJustica grupoOficial ");
		hqlQuery.append(" WHERE grupoOficial.centralMandado IN (:centrais)");
		if(ativo != null){
			hqlQuery.append(" AND grupoOficial.ativo = :paramAtivo");
		}
		hqlQuery.append(" ORDER BY grupoOficial.grupoOficialJustica");

		Query query = entityManager.createQuery(hqlQuery.toString());
		query.setParameter("centrais", centraisMandado);
		if(ativo != null){
			query.setParameter("paramAtivo", ativo);
		}
		return query.getResultList();
	}
}