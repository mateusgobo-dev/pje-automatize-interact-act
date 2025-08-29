package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.EscolaridadeDAO;
import br.jus.pje.nucleo.entidades.Escolaridade;

@Name(EscolaridadeManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EscolaridadeManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "escolaridadeManager";

	@In
	private EscolaridadeDAO escolaridadeDAO;

	public List<Escolaridade> escolaridadeItems() {
		return escolaridadeDAO.escolaridadeItems();
	}

}