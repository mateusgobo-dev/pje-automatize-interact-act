package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaExpedienteEnderecoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaExpedienteEndereco;

@Name(PessoaExpedienteEnderecoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteEnderecoManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaExpedienteEnderecoManager";

	@In
	private PessoaExpedienteEnderecoDAO pessoaExpedienteEnderecoDAO;

	public List<PessoaExpedienteEndereco> pessoaExpedienteEnderecoList(Pessoa pessoa) {
		return pessoaExpedienteEnderecoDAO.pessoaExpedienteEnderecoList(pessoa);
	}

}