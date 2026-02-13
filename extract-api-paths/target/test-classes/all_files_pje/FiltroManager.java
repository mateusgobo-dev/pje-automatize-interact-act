package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.FiltroDAO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.CriterioPesquisa;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.FiltroDTO;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.PagedQueryResult;
import br.jus.pje.nucleo.entidades.Filtro;

@Name(FiltroManager.NAME)
public class FiltroManager extends BaseManager<Filtro> {

	public static final String NAME = "filtroManager";

	@In
	private FiltroDAO filtroDAO;
	
	@Override
	protected FiltroDAO getDAO() {
		return this.filtroDAO;
	}

	public List<Filtro> listarFiltrosParaAutomacao(Integer idLocalizacao){
		return filtroDAO.listarFiltrosParaAutomacao(idLocalizacao);
	}
	
	public PagedQueryResult<Filtro> listarFiltros(CriterioPesquisa crit, Integer idLocalizacao) {
		Long qtd = filtroDAO.listarQtdFiltros(crit,idLocalizacao);
		List<Filtro> filtros = filtroDAO.listarFiltros(crit, idLocalizacao);
		return new PagedQueryResult<Filtro>(qtd,filtros);
	}
	
	public List<Filtro> listarFiltros(Integer idLocalizacao) {
		List<Filtro> filtros = filtroDAO.listarFiltros(null, idLocalizacao);
		return filtros;
	}	
	
	public List<FiltroDTO> listarFiltrosByTag(Integer idTag) {
		List<FiltroDTO> filtros = filtroDAO.listarFiltrosByTag(idTag);
		return filtros;
	}	
	
	public Filtro listarFiltroParaEdicao(Integer idFiltro) {
		return filtroDAO.recuperarPorIdCompleto(idFiltro);
	}
	
	public void excluirCriterios(Filtro filtro) {
		this.filtroDAO.excluirCriterios(filtro);
	}
	
	public void excluirPorId(Integer idFiltro) {
		this.filtroDAO.excluirPorId(idFiltro);
	}
	
	public Filtro recuperarPorIdCompleto(Integer id) {
		return this.filtroDAO.recuperarPorIdCompleto(id);
	}
	
}
