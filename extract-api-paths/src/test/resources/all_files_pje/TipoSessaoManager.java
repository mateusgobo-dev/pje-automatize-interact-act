package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.jt.pje.dao.TipoSessaoDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.TipoSessaoDTO;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name(TipoSessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class TipoSessaoManager {

	public static final String NAME = "tipoSessaoManager";
	
	@In
	private TipoSessaoDAO tipoSessaoDAO;
	
	public List<TipoSessao> getTipoSessaoItems(){
		return tipoSessaoDAO.getTipoSessaoItems();
	}
	
	public List<TipoSessaoDTO> getTipoSessaoDTOItems(){
		return this.tipoSessaoDAO.getTipoSessaoDTOItems();
	}
}
