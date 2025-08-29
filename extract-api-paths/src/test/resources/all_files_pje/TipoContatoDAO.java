package br.com.infox.pje.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;
import br.com.infox.pje.query.TipoContatoQuery;
import br.jus.pje.nucleo.entidades.TipoContato;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(TipoContatoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class TipoContatoDAO extends GenericDAO implements Serializable, TipoContatoQuery {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoContatoDAO";

	@SuppressWarnings("unchecked")
	public List<TipoContato> tipoContatoItems(TipoPessoaEnum tipoPessoa) {
		Query q = getEntityManager().createQuery(TIPO_CONTATO_POR_TIPO_PESSOA_QUERY);
		q.setParameter(TIPO_PESSOA, tipoPessoa);

		return q.getResultList();
	}

}