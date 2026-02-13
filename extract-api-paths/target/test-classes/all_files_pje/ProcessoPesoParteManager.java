package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoPesoParteDAO;
import br.jus.pje.nucleo.entidades.ProcessoPesoParte;

@Name(ProcessoPesoParteManager.NAME)
public class ProcessoPesoParteManager extends BaseManager<ProcessoPesoParte>{

	public static final String NAME = "processoPesoParteManager";
	
	@In
	private ProcessoPesoParteDAO processoPesoParteDAO;
	
	@Override
	protected ProcessoPesoParteDAO getDAO() {
		return processoPesoParteDAO;
	}
	
	public List<ProcessoPesoParte> buscarPesosPartes() {
		return getDAO().buscarPesosPartes();
	}

}
