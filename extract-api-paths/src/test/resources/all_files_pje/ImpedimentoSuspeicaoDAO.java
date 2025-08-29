package br.jus.cnj.pje.business.dao;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.enums.RegraImpedimentoSuspeicaoEnum;
import br.jus.pje.search.Search;
/**
 * Classe para acesso aos dados da entidade ImpedimentoSuspeicao.
 */
@Name(ImpedimentoSuspeicaoDAO.NAME)
@Scope(ScopeType.EVENT)
@SuppressWarnings("unchecked")
public class ImpedimentoSuspeicaoDAO extends BaseDAO<ImpedimentoSuspeicao> {
	
	public static final String NAME = "impedimentoSuspeicaoDAO";
	
	/**
	 * De acordo com o filtro informado e feita a pesquisa.
	 *  
	 * @param filtroRegra RegraImpedimentoSuspeicaoEnum regra selecionada.
	 * @param filtroPessoaMagistrado PessoaMagistrado filtro do magistrado.
	 * @param search Search objeto com os dados da paginacao.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento/suspeicao.
	 */
	public List<ImpedimentoSuspeicao> pesquisar(RegraImpedimentoSuspeicaoEnum filtroRegra, PessoaMagistrado filtroPessoaMagistrado, Search search) {
		StringBuilder sb = new StringBuilder();
		sb.append("from ImpedimentoSuspeicao");
		sb.append(" where 1 = 1");
		if (filtroRegra != null) {
			sb.append(" and regraImpedimentoSuspeicaoEnum = :regraImpedimentoSuspeicao ");
		}
		if (filtroPessoaMagistrado != null) {
			sb.append(" and pessoaMagistrado.idUsuario = :idPessoaMagistrado ");
		}
		sb.append(" order by id asc");
		Query query = getEntityManager().createQuery(sb.toString());
		if (filtroRegra != null) {
			query.setParameter("regraImpedimentoSuspeicao", filtroRegra);
		}
		if (filtroPessoaMagistrado != null) {
			query.setParameter("idPessoaMagistrado", filtroPessoaMagistrado.getIdUsuario());
		}
		if(search.getFirst() != null && search.getFirst().intValue() > 0) {
			query.setFirstResult(search.getFirst());
		}
		if(search.getMax() != null && search.getMax().intValue() > 0) {
			query.setMaxResults(search.getMax());
		}
		return (List<ImpedimentoSuspeicao>)query.getResultList();
	}
	/**
	 * Recupera o impedimentoSuspeicao pela regra e motivo informados.
	 * 
	 * @param regraImpedimentoSuspeicaoEnum regra.
	 * @param descricaoMotivo String descricao do motivo.
	 * @param idPessoaMagistrado Integer id do magistrado em que esta sendo inserido o impedimento/suspeicao.  
	 */
	public ImpedimentoSuspeicao recuperarImpedimentoSuspeicaoPeloMotivo(RegraImpedimentoSuspeicaoEnum regraImpedimentoSuspeicaoEnum, String descricaoMotivo, Integer idPessoaMagistrado) {
		ImpedimentoSuspeicao retorno = null;
		StringBuilder sb = new StringBuilder();
		sb.append("from ImpedimentoSuspeicao where ");
		sb.append(" regraImpedimentoSuspeicaoEnum = :regraImpedimentoSuspeicao and ");
		sb.append(" lower(descricaoMotivo) = lower(:descricaoMotivo)");
		sb.append(" and pessoaMagistrado.idUsuario = :idPessoaMagistrado");
		Query query = getEntityManager().createQuery(sb.toString(), ImpedimentoSuspeicao.class);
		query.setParameter("regraImpedimentoSuspeicao", regraImpedimentoSuspeicaoEnum);
		query.setParameter("descricaoMotivo", descricaoMotivo);
		query.setParameter("idPessoaMagistrado", idPessoaMagistrado);
		try {
			List<ImpedimentoSuspeicao> lista = query.getResultList();
			if (CollectionUtils.isNotEmpty(lista)) {
				retorno = lista.get(0);
			}
		} catch(NoResultException nre) {
			retorno = null;
		}
		return retorno;
	}
	/**
	 * Recupera a lista de impedimento/suspeicao de acordo com o relator do processo informado.
	 * 
	 * @param idUsuario Id do Usuario magistrado ou integrante da composicao do processo.
	 * @param search Search objeto da paginacao.
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento/suspeicao.
	 */
	public List<ImpedimentoSuspeicao> pesquisarPorUsuario(List<Integer> idUsuario) {
		StringBuilder sb = new StringBuilder();
		sb.append("from ImpedimentoSuspeicao ");
		if (idUsuario != null) {
			sb.append(" where pessoaMagistrado.idUsuario in :idUsuario ");
		}
		sb.append(" order by id asc");
		Query query = EntityUtil.createQuery(sb.toString());
		if (idUsuario != null) {
			query.setParameter("idUsuario", idUsuario);
		}
		return (List<ImpedimentoSuspeicao>)query.getResultList();
	}
	@Override
	public Object getId(ImpedimentoSuspeicao e) {
		return e.getId();
	}
	
}