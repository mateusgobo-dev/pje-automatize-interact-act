package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.PessoaProcuradorProcuradoriaQuery;
import br.jus.pje.nucleo.entidades.PessoaProcuradorProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

/**
 * Classe com as consultas a entidade de PessoaProcuradorProcuradoria.
 * 
 * @author João Paulo Lacerda
 * 
 */
@Name(PessoaProcuradorProcuradoriaDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaProcuradorProcuradoriaDAO extends GenericDAO implements Serializable,
		PessoaProcuradorProcuradoriaQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaProcuradorProcuradoriaDAO";

	/**
	 * @param pessoaProcuradoriaEntidade
	 * @return PessoaProcuradorProcuradoria relacionada a
	 *         PessoaProcuradoriaEntidade informada.
	 */
	@SuppressWarnings("unchecked")
	public List<PessoaProcuradorProcuradoria> getPessoaProcuradorProcuradoriaList(
			PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade) {
		Query q = getEntityManager().createQuery(PESSOA_PROCURADOR_PROCURADORIA_POR_ENTIDADE_LIST_QUERY);
		q.setParameter(PESSOA_PROCURADORIA_ENTIDADE_PARAM, pessoaProcuradoriaEntidade);

		List<PessoaProcuradorProcuradoria> resultList = q.getResultList();
		return resultList;
	}

	public boolean existeProcuradorEntidade(int idPessoa) {
		String hql = "select count(o) from PessoaProcuradorProcuradoria o where o.pessoaProcuradoriaEntidade.pessoa.idUsuario = :idPessoa ";
		Query query = getEntityManager().createQuery(hql).setParameter("idPessoa", idPessoa);
		Long count = (Long) query.getSingleResult();
		return count > 0;
	}

}