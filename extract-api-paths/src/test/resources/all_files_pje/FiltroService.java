package br.jus.cnj.pje.nucleo.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CriterioFiltroManager;
import br.jus.cnj.pje.nucleo.manager.FiltroManager;
import br.jus.cnj.pje.nucleo.manager.TagManager;
import br.jus.pje.nucleo.entidades.Filtro;
import br.jus.pje.nucleo.entidades.Tag;

@Name(FiltroService.NAME)
public class FiltroService extends BaseService{

	public static final String NAME = "filtroService";
	
	@In
	private FiltroManager filtroManager;
	
	@In
	private CriterioFiltroManager criterioFiltroManager;
	
	@In
	private TagManager tagManager;
	
	public Filtro criarFiltro(Filtro filtro) throws PJeBusinessException {
		this.filtroManager.persist(filtro);
		this.filtroManager.flush();
		filtro = this.filtroManager.findById(filtro.getId());
		
		return filtro;
	}


	public void excluirFiltro(Integer idFiltro) {
		criterioFiltroManager.excluirPorIdFiltro(idFiltro);
		filtroManager.excluirPorId(idFiltro);
		
	}

	public void aplicarFiltros(Integer idEtiqueta, List<Integer> idsFiltros) throws PJeBusinessException {
		Tag tag = tagManager.findById(idEtiqueta);
		for (Integer idFiltro: idsFiltros) {
			Filtro filtro = filtroManager.recuperarPorIdCompleto(idFiltro);
			filtro.getTags().add(tag);
			filtroManager.persist(filtro);
		}
	}	
}
