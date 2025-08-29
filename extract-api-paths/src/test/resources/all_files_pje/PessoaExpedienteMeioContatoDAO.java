package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.PessoaExpedienteMeioContatoQuery;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaExpedienteMeioContato;

@Name(PessoaExpedienteMeioContatoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteMeioContatoDAO extends GenericDAO implements Serializable,
		PessoaExpedienteMeioContatoQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaExpedienteMeioContatoDAO";

	@SuppressWarnings("unchecked")
	public List<PessoaExpedienteMeioContato> pessoaExpedienteMeioContatoList(Pessoa pessoa) {
		Query q = getEntityManager().createQuery(PESSOA_EXP_MEIO_CONTATO_POR_PESSOA_QUERY);
		q.setParameter(PESSOA_PARAM, pessoa);

		return q.getResultList();
	}

}