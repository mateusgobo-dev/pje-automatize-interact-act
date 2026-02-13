package br.com.jt.pje.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.list.CalendarioEventoList;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.CalendarioEvento;

@Name(CalendarioEventoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class CalendarioEventoAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String NAME = "calendarioEventoAction";
	private CalendarioEventoList calendarioEventoList;
	List<CalendarioEvento> lista = null;
	
	public CalendarioEventoAction() {
		
	}
	
	public CalendarioEventoList getCalendarioEventoList() {
		if(calendarioEventoList == null){
			calendarioEventoList = ComponentUtil.getComponent(CalendarioEventoList.NAME);
			calendarioEventoList.carregaDadosDaSessao();
			lista = calendarioEventoList.list();
		}
		return calendarioEventoList;
	}
	
	public void setCalendarioEventoList(CalendarioEventoList calendarioEventoList) {
		this.calendarioEventoList = calendarioEventoList;
	}
	
	public void pesquisar(Integer size){
		String msgCampoObrigatorio = "Campo obrigatório!";
		boolean camposObrigatoriosPreenchidos = true;
		if(getCalendarioEventoList().getEstado() == null){
			FacesMessages.instance().addToControl("estadoComboId", Severity.ERROR, msgCampoObrigatorio);
			camposObrigatoriosPreenchidos = false;
		}
		if(getCalendarioEventoList().getJurisdicaoMunicipio() == null){
			FacesMessages.instance().addToControl("municipioCombo", Severity.ERROR, msgCampoObrigatorio);
			camposObrigatoriosPreenchidos = false;
		}
		if(getCalendarioEventoList().getOrgaoJulgador() == null){
			FacesMessages.instance().addToControl("orgaoJugadorComboId", Severity.ERROR, msgCampoObrigatorio);
			camposObrigatoriosPreenchidos = false;
		}
		if(camposObrigatoriosPreenchidos){
			this.lista =  getCalendarioEventoList().list();
		}
	}
	
	public List<CalendarioEvento> getLista() {
		return lista;
	}
	public void setLista(List<CalendarioEvento> lista) {
		this.lista = lista;
	}
	
}
