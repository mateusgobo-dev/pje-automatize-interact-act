package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.search.Search;


@Scope(ScopeType.PAGE)
public abstract class ListPaginada<E> {
	
	private int tamanhoPagina;
	
	private int quantidadePaginas;
	
	private int paginaAtual;
	
	private Search search;
	
	public abstract List<E> list(Search search);
	
	public abstract Long count();
	
	private Map<Integer, List<Object>> resultados = new HashedMap();
	
	@SuppressWarnings("rawtypes")
	public List<E> ListPaginada() {
		List resultado = list(getSearch());
		if (search.getFirst() != null && search.getFirst() != 0) {
			paginaAtual = search.getFirst();
		}
		if (search.getMax() != null && search.getMax() != 0) {
			tamanhoPagina = search.getMax();

		}
		return resultado.isEmpty() ? new ArrayList<E>() : resultado;
	}

	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}
	
	public int getPageCount(Long count){
		if(count > 0){
			return new Double(Math.ceil(new Double(count)/new Double(search.getMax()))).intValue();
		}
		else{
			return 0;
		}
	}

}
