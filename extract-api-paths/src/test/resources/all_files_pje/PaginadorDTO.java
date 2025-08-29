package br.jus.pje.nucleo.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um resultado de paginação.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("all")
public class PaginadorDTO<E, S> implements Serializable {

	private Integer pagina;
	private Integer tamanhoPagina;
	private Integer totalRegistros;
	private E objeto;
	private List<S> colecao;

	/**
	 * @return pagina.
	 */
	public Integer getPagina() {
		if (pagina == null) {
			pagina = 1;
		}
		return pagina;
	}

	/**
	 * @param pagina
	 *            Atribui pagina.
	 */
	public void setPagina(Integer pagina) {
		this.pagina = pagina;
	}

	/**
	 * @return registros.
	 */
	public Integer getTamanhoPagina() {
		if (tamanhoPagina == null) {
			tamanhoPagina = 20;
		}
		return tamanhoPagina;
	}

	/**
	 * @param registros
	 *            Atribui registros.
	 */
	public void setTamanhoPagina(Integer registros) {
		this.tamanhoPagina = registros;
	}

	/**
	 * @return total.
	 */
	public Integer getTotalRegistros() {
		if (totalRegistros == null) {
			totalRegistros = 0;
		}
		return totalRegistros;
	}

	/**
	 * @param total
	 *            Atribui total.
	 */
	public void setTotalRegistros(Integer total) {
		this.totalRegistros = total;
	}

	/**
	 * @return Retorna objeto.
	 */
	public E getObjeto() {
		return objeto;
	}

	/**
	 * @param objeto Atribui objeto.
	 */
	public void setObjeto(E objeto) {
		this.objeto = objeto;
	}

	/**
	 * @return colecao.
	 */
	public List<S> getColecao() {
		if (colecao == null) {
			colecao = new ArrayList<>();
		}
		return colecao;
	}

	/**
	 * @param colecao
	 *            Atribui colecao.
	 */
	public void setColecao(List<S> colecao) {
		this.colecao = colecao;
	}
	
	public int getFirst() {
		return (getPagina() - 1) * getTamanhoPagina();
	}
}
