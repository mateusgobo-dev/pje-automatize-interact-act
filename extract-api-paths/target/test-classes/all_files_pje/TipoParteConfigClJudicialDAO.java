package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;

@Name(TipoParteConfigClJudicialDAO.NAME)
public class TipoParteConfigClJudicialDAO extends BaseDAO<TipoParteConfigClJudicial> {
	public static final String NAME = "tipoParteConfigClJudicialDAO";

	@Override
	public Object getId(TipoParteConfigClJudicial e) {
		return e.getIdTipoParteConfigClJudicial();
	}
	
	/**
	 * Método responsável por recuperar a configuração de acordo com a
	 * {@link ClasseJudicial}
	 * 
	 * @param classeJudicial
	 *            parâmetro que se deseja verificar se existe configuração
	 * @return <code>List</code> contendo tipos de partes e suas configurações
	 *         da classe judicial passada por parâmetro
	 */
	@SuppressWarnings("unchecked")
	public List<TipoParteConfigClJudicial> recuperarTipoParteConfiguracao(ClasseJudicial classeJudicial) {
		StringBuilder sb = new StringBuilder("SELECT o FROM TipoParteConfigClJudicial AS o ");
		sb.append(" WHERE o.classeJudicial = :classeJudicial ");
		
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("classeJudicial", classeJudicial);
		
		List<TipoParteConfigClJudicial> resultList = query.getResultList();
		return resultList.isEmpty() ? new ArrayList<TipoParteConfigClJudicial>(0) : resultList;
	}
	
	/**
	 * Método responsável por verificar se uma configuração de um tipo de parte
	 * para uma classe judicial existe
	 * 
	 * @param tipoParteConfigClJudicial
	 *            parâmetro que se deseja verificar se existe
	 * @return <code>Boolean</code> caso a configuração exista
	 */
	public Boolean existeTipoParteConfigClJudicial(TipoParteConfigClJudicial tipoParteConfigClJudicial) {
		StringBuilder sb = new StringBuilder("SELECT COUNT(tpccj) FROM TipoParteConfigClJudicial AS tpccj ");
		sb.append(" WHERE tpccj.classeJudicial = :classeJudicial ");
		
		montarQueryAtributosConfiguracao(tipoParteConfigClJudicial, sb);
		
		Query query = entityManager.createQuery(sb.toString());
		query.setParameter("classeJudicial", tipoParteConfigClJudicial.getClasseJudicial());
		
		montarParametrosConfiguracao(tipoParteConfigClJudicial, query);

		try {
			Long result = (Long) query.getSingleResult();
			return (result > 0);
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Método responsável por montar os parâmetros do método
	 * {@link #existeTipoParteConfigClJudicial(TipoParteConfigClJudicial)}
	 * 
	 * @param tipoParteConfigClJudicial
	 * @param query
	 */
	private void montarParametrosConfiguracao(TipoParteConfigClJudicial tipoParteConfigClJudicial, Query query) {
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null) {
			query.setParameter("tipoParte", tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo() != null) {
			query.setParameter("poloAtivo", tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo() != null) {
			query.setParameter("poloPassivo", tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getOutrosParticipantes() != null) {
			query.setParameter("outros", tipoParteConfigClJudicial.getTipoParteConfiguracao().getOutrosParticipantes());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaFisica() != null) {
			query.setParameter("pessoaFisica", tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaFisica());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaJuridica() != null) {
			query.setParameter("pessoaJuridica", tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaJuridica());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getEnteAutoridade() != null) {
			query.setParameter("enteAutoridade", tipoParteConfigClJudicial.getTipoParteConfiguracao().getEnteAutoridade());
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getOab() != null) {
			query.setParameter("oab", tipoParteConfigClJudicial.getTipoParteConfiguracao().getOab());
		}
	}

	/**
	 * Método responsável por montar a query com os atributos para pesquisa do
	 * método {@link #existeTipoParteConfigClJudicial(TipoParteConfigClJudicial)}
	 * 
	 * @param tipoParteConfiguracao
	 * @param sb
	 */
	private void montarQueryAtributosConfiguracao(TipoParteConfigClJudicial tipoParteConfigClJudicial, StringBuilder sb) {
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoParte() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.tipoParte = :tipoParte ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloAtivo() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.poloAtivo = :poloAtivo ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getPoloPassivo() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.poloPassivo = :poloPassivo ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getOutrosParticipantes() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.outrosParticipantes = :outros ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaFisica() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.tipoPessoaFisica = :pessoaFisica ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getTipoPessoaJuridica() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.tipoPessoaJuridica = :pessoaJuridica ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getEnteAutoridade() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.enteAutoridade = :enteAutoridade ");
		}
		if (tipoParteConfigClJudicial.getTipoParteConfiguracao().getOab() != null) {
			sb.append(" AND tpccj.tipoParteConfiguracao.oab = :oab ");
		}
	}
}
