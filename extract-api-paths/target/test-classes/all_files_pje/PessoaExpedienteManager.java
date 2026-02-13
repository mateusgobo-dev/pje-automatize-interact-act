package br.com.infox.pje.manager;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaExpedienteDAO;

@Name(PessoaExpedienteManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaExpedienteManager";

	@In
	private PessoaExpedienteDAO pessoaExpedienteDAO;

}