package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaServidorDAO;
import br.jus.cnj.pje.business.dao.VisualizadoresSigiloDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.VisualizadoresSigilo;
import java.util.Collections;

@Name(VisualizadoresSigiloManager.NAME)
public class VisualizadoresSigiloManager extends BaseManager<VisualizadoresSigilo>{

	public static final String NAME = "visualizadoresSigiloManager";
	
	@In
	private VisualizadoresSigiloDAO visualizadoresSigiloDAO;
	
	@In
	private PessoaServidorDAO pessoaServidorDAO;

	@Override
	protected BaseDAO<VisualizadoresSigilo> getDAO() {
		return visualizadoresSigiloDAO;
	}

	public List<VisualizadoresSigilo> getVisualizadoresSigiloOJ(OrgaoJulgador orgaoJulgador) {
		if(orgaoJulgador == null){
			return Collections.emptyList();
		}
		return visualizadoresSigiloDAO.getVisualizadoresSigiloOJ(orgaoJulgador);
	}
	
	public List<PessoaServidor> retornaListaPessoaServidor(OrgaoJulgador orgaoJulgador){
		if(orgaoJulgador == null){
			return Collections.emptyList();
		}
		return pessoaServidorDAO.retornaListaPessoaServidor(orgaoJulgador.getIdOrgaoJulgador(), null, null, true);
	}
	
	public List<VisualizadoresSigilo> getVisualizadoresSigiloPorServidor(PessoaServidor pessoaServidor){
		if(pessoaServidor == null){
			return Collections.emptyList();
		}
		return visualizadoresSigiloDAO.getVisualizadoresSigiloPorServidor(pessoaServidor);
	}

	public VisualizadoresSigilo getVisualizadoresSigiloByIdUsuario(Integer idUsuario, OrgaoJulgador orgaoJulgador) {
		if(idUsuario == null){
			return null;
		}
		return visualizadoresSigiloDAO.getVisualizadoresSigiloByIdUsuario(idUsuario, orgaoJulgador);
	}
}
