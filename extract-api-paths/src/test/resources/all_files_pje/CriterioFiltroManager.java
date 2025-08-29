package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.CriterioFiltroDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioFiltroDTO;
import br.jus.pje.nucleo.entidades.CriterioFiltro;

@Name(CriterioFiltroManager.NAME)
public class CriterioFiltroManager extends BaseManager<CriterioFiltro>{

	public static final String NAME = "criterioFiltroManager";
	
	@In
	private CriterioFiltroDAO criterioFiltroDAO;
	
	@Override
	protected BaseDAO<CriterioFiltro> getDAO() {
		return this.criterioFiltroDAO;
	}
	
	public void excluirPorIdFiltro(Integer idFiltro){
		this.criterioFiltroDAO.excluirPorIdFiltro(idFiltro);
	}
	
	public List<CriterioFiltroDTO> recuperarCriteriosPorIdFiltro(Integer idFiltro){
		return this.criterioFiltroDAO.recuperarCriteriosPorIdFiltro(idFiltro);
	}
	
	public List<CriterioFiltro> recuperarCriteriosEntitiesPorIdFiltro(Integer idFiltro){
		return this.criterioFiltroDAO.recuperarCriteriosEntitiesPorIdFiltro(idFiltro);
	}	
}
