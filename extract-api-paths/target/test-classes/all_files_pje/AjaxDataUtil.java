package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Classe responsavel por renderizar dados de respostas de solicitacoes ajax.
 *  
 * @author pablo-moreira
 *
 */
@Name(value=AjaxDataUtil.NAME)
@Scope(ScopeType.EVENT)
public class AjaxDataUtil {
		
	public static final String NAME = "ajaxDataUtil";

	private String data;

	public void setData(String data) {
		this.data = data;
	}
	
	public String getData() {
		return data;
	}

	public void sucesso() {
		setData("sucesso");
	}
	
	public void erro() {
		setData("erro");
	}
}
