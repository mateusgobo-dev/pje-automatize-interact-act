package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaExpedienteMeioContatoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaExpedienteMeioContato;

@Name(PessoaExpedienteMeioContatoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteMeioContatoManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaExpedienteMeioContatoManager";

	@In
	private PessoaExpedienteMeioContatoDAO pessoaExpedienteMeioContatoDAO;

	public List<PessoaExpedienteMeioContato> pessoaExpedienteMeioContatoList(Pessoa pessoa) {
		return pessoaExpedienteMeioContatoDAO.pessoaExpedienteMeioContatoList(pessoa);
	}

}