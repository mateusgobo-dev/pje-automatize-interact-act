package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.business.dao.EditorEstiloDAO;
import br.jus.pje.nucleo.entidades.editor.EditorEstilo;

@Name(EditorEstiloService.NAME)
@Scope(ScopeType.EVENT)
public class EditorEstiloService{
	
	public final static String NAME = "editorEstiloService";
	
	@In
	private EditorEstiloDAO editorEstiloDAO;
	
	public List<EditorEstilo> recuperarEstilos(){
		return editorEstiloDAO.findAll();
	}
	
	public String recuperarEstilosJSON(){
		
		ArrayList<EditorEstilo> estilos = (ArrayList<EditorEstilo>) recuperarEstilos();
		StringBuilder resultado = new StringBuilder();
		resultado.append("[");
		for (Iterator<EditorEstilo> iterator = estilos.iterator(); iterator.hasNext();) {
			EditorEstilo editorEstilo = (EditorEstilo) iterator.next();
			resultado.append(editorEstilo.getJson());
			if(iterator.hasNext()){
				resultado.append(",");
			}
		}
		resultado.append("]");

		return resultado.toString();
	}
	
}
