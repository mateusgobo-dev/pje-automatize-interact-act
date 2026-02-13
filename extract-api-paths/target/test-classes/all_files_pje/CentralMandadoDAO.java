package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.Localizacao;

/**
 * Classe responsavel por manipular consultas e regras para a entidade de CentralMandado
 */
@Name(CentralMandadoDAO.NAME)
public class CentralMandadoDAO extends BaseDAO<CentralMandado>{
	
	public static final String NAME  = "centralMandadoDAO";
	
	public static CentralMandadoDAO instance() {
		return ComponentUtil.getComponent(NAME);
	}

	/**
	 * Retorna as centrais de mandados de acordo com as localizacoes.
	 * 
	 * @param localizacoes Localizacoes
	 * @return Lista de CentralMandado pertencentes as localizacoes
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandado> obterPorLocalizacao(List<Localizacao> localizacoes){
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT DISTINCT cm ");
		sbQuery.append(" FROM CentralMandadoLocalizacao cml ");
		sbQuery.append(" 		JOIN cml.centralMandado cm ");
		sbQuery.append(" WHERE cml.localizacao IN (:paramIds)");
		sbQuery.append(" 	AND cm.ativo = true");
		sbQuery.append(" order by cm.centralMandado ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("paramIds", localizacoes);

		return query.getResultList();
	}
	
	/**
	 * Retorna as centrais de mandados de acordo com o usuario e que os grupos de oficiais sejam ativos.
	 * 
	 * @param idUsuario id do usuario
	 * @return List de Centrais de Mandado do usuario
	 */
	@SuppressWarnings("unchecked")
	public List<CentralMandado> obterCentraisMandadosPorUsuario(Integer idUsuario) {
		StringBuilder sbQuery = new StringBuilder();
		sbQuery.append(" SELECT cm ");
		sbQuery.append(" 	FROM CentralMandado cm ");
		sbQuery.append(" 		JOIN cm.grupoOficialJusticaList grupoOficial");
		sbQuery.append(" 		JOIN grupoOficial.pessoaGrupoOficialJusticaList pessoaGrupo");
		sbQuery.append(" WHERE pessoaGrupo.pessoa.idUsuario = :paramId");
		sbQuery.append(" 	AND cm.ativo = true");
		sbQuery.append(" 	AND pessoaGrupo.ativo = true");
		sbQuery.append(" order by cm.centralMandado ");
		
		Query query = getEntityManager().createQuery(sbQuery.toString());
		query.setParameter("paramId", idUsuario);
		return query.getResultList();
	}

	/**
	 * Retorna as localizacoes de uma dada central de mdandados, filtrando também pela localização indicada pelo usuário
	 * 
	 * @param centralMandado
	 * @param localizacaoUsuarioList
	 * @return lista de localizacoes
	 */
	@SuppressWarnings("unchecked")
	public List<Localizacao> obterLocalizacoes(CentralMandado centralMandado, List<Localizacao> localizacaoUsuarioList){
		StringBuilder sb = new StringBuilder();
		sb.append("select cml.localizacao ");
		sb.append(" from CentralMandadoLocalizacao cml "); 
		sb.append(" where cml.centralMandado.idCentralMandado = :idCentralMandado ");
		sb.append(" and cml.localizacao.ativo=true");
		if(CollectionUtilsPje.isNotEmpty(localizacaoUsuarioList)) {
			sb.append(" and cml.localizacao IN (:localizacoesList) ");
		}			
		sb.append(" order by cml.localizacao");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idCentralMandado", centralMandado.getIdCentralMandado());
		if(CollectionUtilsPje.isNotEmpty(localizacaoUsuarioList)) {
			query.setParameter("localizacoesList", localizacaoUsuarioList);
		}
		
		return query.getResultList();
	}
	
	@Override
	public Object getId(CentralMandado e) {
		return e.getIdCentralMandado();
	}
	
}
