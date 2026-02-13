package br.com.infox.access;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.core.Interpolator;

public class MenuItemJson implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nome;

	private String url = "#";

	private List<MenuItemJson> itens = new ArrayList<MenuItemJson>();

	private Boolean popup = false;

	public MenuItemJson(String label, String url) {
		this.nome = label;
		this.url = url;
	}

	public MenuItemJson(String label) {
		this.nome = label;
	}

	public String getNome() {
		return Interpolator.instance().interpolate(nome);
	}

	public void setLabel(String label) {
		this.nome = label;
	}

	public String getUrl() {
		return url == null ? "" : url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String url() {
		return url;
	}

	public List<MenuItemJson> getItens() {
		return itens;
	}

	public void setItens(List<MenuItemJson> itens) {
		this.itens = itens;
	}

	@Override
	public String toString() {
		return nome + ":" + getUrl() + " " + itens;
	}

	public MenuItemJson add(MenuItemJson item) {
		int i = itens.indexOf(item);
		if (i != -1) {
			item = itens.get(i);
		} else {
			itens.add(item);
		}
		return item;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((nome == null) ? 0 : nome.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final MenuItemJson other = (MenuItemJson) obj;
		if (nome == null) {
			if (other.nome != null) {
				return false;
			}
		} else if (!nome.equals(other.nome)) {
			return false;
		}
		return true;
	}

	/*
	 * PJE-JT: Ricardo Scholz e David Vieira : PJE-1107 - 2012-01-17 Alteracoes
	 * feitas pela JT. Getter e setter do atributo 'popup'. Vide comentario na
	 * declaracao do atributo.
	 */
	public Boolean getPopup() {
		return popup;
	}

	public void setPopup(Boolean popup) {
		this.popup = popup;
	}
	/*
	 * PJE-JT: Fim.
	 */

}