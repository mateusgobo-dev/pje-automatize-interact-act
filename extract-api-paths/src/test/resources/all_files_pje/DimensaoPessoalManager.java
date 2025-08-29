package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.DimensaoPessoalDAO;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(DimensaoPessoalManager.NAME)
public class DimensaoPessoalManager extends BaseManager<DimensaoPessoal> {

	public static final String NAME = "dimensaoPessoalManager";
	
	@In
	private DimensaoPessoalDAO dimensaoPessoalDAO;

	@Override
	protected DimensaoPessoalDAO getDAO() {
		return dimensaoPessoalDAO;
	}

	public List<DimensaoPessoal> getDimensoesPessoais(ProcessoTrf proc, List<Competencia> competencias, Jurisdicao jurisdicao) {
		if(proc.getPessoaPoloAtivoList().size() == 0 || proc.getPessoaPoloPassivoList().size() == 0){
			return Collections.emptyList();
		}else{
			return dimensaoPessoalDAO.getDimensoesPessoais(proc, competencias, jurisdicao);
		}
	}


}
