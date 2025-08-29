package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.query.PessoaProcuradoriaEntidadeQuery;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.Procuradoria;

/**
 * Classe com as consultas a entidade de PessoaProcuradoriaEntidade.
 * 
 * @author João Paulo Lacerda
 * 
 */
@Name(PessoaProcuradoriaEntidadeDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaProcuradoriaEntidadeDAO extends BaseDAO<PessoaProcuradoriaEntidade> implements Serializable, PessoaProcuradoriaEntidadeQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaProcuradoriaEntidadeDAO";

	/**
	 * @param pessoa
	 * @return PessoaProcuradoriaEntidade relacionada a Pessoa informada.
	 */
	@Deprecated
	public PessoaProcuradoriaEntidade getPessoaProcuradoriaEntidade(Pessoa pessoa) {
		Query q = getEntityManager().createQuery(PESSOA_PROCURADORIA_ENTIDADE_POR_PESSOA_QUERY);
		q.setParameter(PESSOA_PARAM, pessoa);

		PessoaProcuradoriaEntidade result = EntityUtil.getSingleResult(q);
		return result;
	}

	/**
	 * [PJEII-4244] Método que carrega a lista de PessoaProcuradoriaEntidade de determinada pessoa.
	 * @param pessoa
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaProcuradoriaEntidade> getListaPessoaProcuradoriaEntidade(Pessoa pessoa){
		StringBuilder hql = new StringBuilder();
		hql.append("SELECT ppe ");
		hql.append(" FROM PessoaProcuradoriaEntidade ppe ");
		hql.append(" WHERE ppe.pessoa = :pessoa ");
		hql.append(" ORDER BY ppe.idPessoaProcuradoriaEntidade ASC");
		
		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("pessoa", pessoa);
		
		return (List<PessoaProcuradoriaEntidade>) query.getResultList();
	}
	
	/**
	 * Retorna um lista com os identificadores das pessoas representadas pela {@link Procuradoria}
	 * @param idProcuradoria
	 * @return lista de {@link Integer} com os identificadores das pessoas representadas
	 */
	@SuppressWarnings("unchecked")
	public List<Integer> getIdsPessoasRepresentadasPorProcuradoria(Integer idProcuradoria){
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT o.pessoa.idPessoa FROM PessoaProcuradoriaEntidade o WHERE o.procuradoria.idProcuradoria = :idProcuradoria ");
		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idProcuradoria", idProcuradoria);
		return query.getResultList();
	}

	@Override
	public Object getId(PessoaProcuradoriaEntidade e) {
		return e.getIdPessoaProcuradoriaEntidade();
	}

}