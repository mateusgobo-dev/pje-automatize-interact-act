package br.com.infox.access;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.core.Interpolator;

public class MenuItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String label;

	private String url;

	private List<MenuItem> children = new ArrayList<MenuItem>();
	
	
/*
 * PJE-JT: Ricardo Scholz e David Vieira : PJE-1107 - 2012-01-17 Alteracoes feitas pela JT.
 * Inclusao do atributo 'popup', que indica se a URL do item sera aberta em nova janela 
 * (target="_blank"). Comportamento padrao e abrir a URL na mesma janela.
 */
   private Boolean popup = false;	        
/*
 * PJE-JT: Fim.
 */
   

	public MenuItem(String label, String url) {
		this.label = label;
		this.url = url;
	}

	public MenuItem(String label) {
		this.label = label;
	}

	public String getLabel() {
		return Interpolator.instance().interpolate(label);
	}

	public String getLabelExpression() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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

	@Override
	public String toString() {
		return label + ":" + getUrl() + " " + children;
	}

	public List<MenuItem> getChildren() {
		return children;
	}

	public MenuItem add(MenuItem item) {
		int i = children.indexOf(item);
		if (i != -1) {
			item = children.get(i);
		} else {
			children.add(item);
		}
		return item;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((label == null) ? 0 : label.hashCode());
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
		final MenuItem other = (MenuItem) obj;
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		return true;
	}

	
	
	
	
	 /*
	  * PJE-JT: Ricardo Scholz e David Vieira : PJE-1107 - 2012-01-17 Alteracoes feitas pela JT.
	  * Getter e setter do atributo 'popup'. Vide comentario na declaracao do atributo.
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