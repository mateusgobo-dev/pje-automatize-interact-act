package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaExpedienteDocIdentificacaoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaExpedienteDocIdentificacao;

@Name(PessoaExpedienteDocIdentificacaoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteDocIdentificacaoManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaExpedienteDocIdentificacaoManager";

	@In
	private PessoaExpedienteDocIdentificacaoDAO pessoaExpedienteDocIdentificacaoDAO;

	public List<PessoaExpedienteDocIdentificacao> pessoaExpedienteDocIdentificacaoList(Pessoa pessoa) {
		return pessoaExpedienteDocIdentificacaoDAO.pessoaExpedienteDocIdentificacaoList(pessoa);
	}

}