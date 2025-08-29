package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.EtniaDAO;
import br.jus.pje.nucleo.entidades.Etnia;

@Name(EtniaManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EtniaManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "etniaManager";

	@In
	private EtniaDAO etniaDAO;

	public List<Etnia> etniaItems() {
		return etniaDAO.etniaItems();
	}

}