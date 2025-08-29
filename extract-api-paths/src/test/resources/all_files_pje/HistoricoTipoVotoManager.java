package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.HistoricoTipoVotoDAO;
import br.jus.pje.jt.entidades.HistoricoTipoVoto;
import br.jus.pje.jt.entidades.Voto;

@Name(HistoricoTipoVotoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class HistoricoTipoVotoManager extends GenericManager{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "historicoTipoVotoManager";
	
	@In
	private HistoricoTipoVotoDAO historicoTipoVotoDAO;
	
	public List<HistoricoTipoVoto> getAllHistoricoTipoVoto(Voto voto){
		return historicoTipoVotoDAO.getAllHistoricoTipoVoto(voto);
	}
	
}
