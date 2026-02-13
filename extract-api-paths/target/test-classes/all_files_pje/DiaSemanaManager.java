package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.DiaSemanaDAO;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link DiaSemana}.
 * @author thiago.vieira
 */
@Name(DiaSemanaManager.NAME)
public class DiaSemanaManager extends BaseManager<DiaSemana> {
	
	public static final String NAME = "diaSemanaManager";
	
	@In
	private DiaSemanaDAO diaSemanaDAO;

   
	@Override
	protected DiaSemanaDAO getDAO() {
		return diaSemanaDAO;
	}
	
	public List<String> getDiaSemanaList() {
		Search s = new Search(DiaSemana.class);
		s.setRetrieveField("diaSemana");
		return list(s);
	}

	public static Integer diaSemanaInt(DiaSemana obj) {
		if (obj.getDiaSemana().equals("Domingo")) {
			return 0;
		}
		if (obj.getDiaSemana().equals("Segunda")) {
			return 1;
		}
		if (obj.getDiaSemana().equals("Terça")) {
			return 2;
		}
		if (obj.getDiaSemana().equals("Quarta")) {
			return 3;
		}
		if (obj.getDiaSemana().equals("Quinta")) {
			return 4;
		}
		if (obj.getDiaSemana().equals("Sexta")) {
			return 5;
		}
		if (obj.getDiaSemana().equals("Sábado")) {
			return 6;
		}
		if (obj.getDiaSemana().equals("Todos os dias")){
			return 7;
		}
		return null;
	}
	
}
