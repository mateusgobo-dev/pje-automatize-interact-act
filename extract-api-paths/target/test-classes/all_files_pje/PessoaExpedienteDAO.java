package br.com.infox.pje.dao;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.dao.GenericDAO;

@Name(PessoaExpedienteDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaExpedienteDAO extends GenericDAO implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaExpedienteDAO";

}