package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.DimensaoFuncionalDAO;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoFuncional;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(DimensaoFuncionalManager.NAME)
public class DimensaoFuncionalManager extends BaseManager<DimensaoFuncional> {

	public static final String NAME = "dimensaoFuncionalManager";
	
	@In
	private DimensaoFuncionalDAO dimensaoFuncionalDAO;

	@Override
	protected DimensaoFuncionalDAO getDAO() {
		return dimensaoFuncionalDAO;
	}

	public List<DimensaoFuncional> getDimensoesFuncionais(ProcessoTrf proc, List<Competencia> competencias, Jurisdicao jurisdicao) {
		return dimensaoFuncionalDAO.getDimensoesFuncionais(proc, competencias, jurisdicao);
	}


}
