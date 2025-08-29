package br.com.jt.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.itx.exception.AplicationException;
import br.com.jt.pje.dao.RecursoParteDAO;
import br.jus.pje.jt.entidades.RecursoParte;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(RecursoParteManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class RecursoParteManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "recursoParteManager";
	
	@In
	private RecursoParteDAO recursoParteDAO;
	
	public void removeBy(Integer idRecurso, Integer idProcessoParte) {
		if(idRecurso == null || idProcessoParte == null){
			throw new AplicationException("Não foi possível remover a parte do recurso");
		}
		recursoParteDAO.removeBy(idRecurso, idProcessoParte);
	}
	
	public List<ProcessoParte> getPartesBy(Integer idRecurso){
		if(idRecurso == null){
			return null;
		}
		return recursoParteDAO.getPartesBy(idRecurso);
	}
	
	public List<RecursoParte> getRecursoPartesByRecurso(Integer idRecurso){
		if(idRecurso == null){
			return null;
		}
		return recursoParteDAO.getRecursoPartesByRecurso(idRecurso);
	}
	
	public boolean existeRecursoParte(Integer idRecurso, Integer idParte){
		return recursoParteDAO.existeRecursoParte(idRecurso, idParte);
	}
	
}
