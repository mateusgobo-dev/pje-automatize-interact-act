package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.FormularioExternoDAO;
import br.jus.pje.nucleo.entidades.FormularioExterno;

@Name(FormularioExternoManager.NAME)
public class FormularioExternoManager extends BaseManager<FormularioExterno> implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "formularioExternoManager";
	
	@In
	private FormularioExternoDAO formularioExternoDAO;
	
	@Override
	protected BaseDAO<FormularioExterno> getDAO() {
		return this.formularioExternoDAO;
	}

}
