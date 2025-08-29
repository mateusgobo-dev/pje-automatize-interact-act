package br.jus.cnj.pje.business.dao;

import java.io.Serializable;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.FormularioExterno;

@Name(FormularioExternoDAO.NAME)
public class FormularioExternoDAO extends BaseDAO<FormularioExterno> implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "formularioExternoDAO";

	@Override
	public Object getId(FormularioExterno e) {
		return e.getId();
	}

}
