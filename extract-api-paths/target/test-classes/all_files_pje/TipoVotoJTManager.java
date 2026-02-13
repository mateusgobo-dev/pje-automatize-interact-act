package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.TipoVotoJTDAO;
import br.jus.pje.jt.entidades.TipoVotoJT;

@Name(TipoVotoJTManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class TipoVotoJTManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "tipoVotoJTManager";
	
	@In
	private TipoVotoJTDAO tipoVotoJTDAO;
	
	public List<TipoVotoJT> getTipoVotoRelator(){
		return tipoVotoJTDAO.getTipoVotoRelator();
	}
	
	public List<TipoVotoJT> getTipoVotoVogal(){
		return tipoVotoJTDAO.getTipoVotoVogal();
	}
	
}
