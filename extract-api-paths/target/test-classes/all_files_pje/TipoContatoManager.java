package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.TipoContatoDAO;
import br.jus.pje.nucleo.entidades.TipoContato;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name(TipoContatoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class TipoContatoManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoContatoManager";

	@In
	private TipoContatoDAO tipoContatoDAO;

	public List<TipoContato> tipoContatoItems(TipoPessoaEnum tipoPessoa) {
		return tipoContatoDAO.tipoContatoItems(tipoPessoa);
	}

}