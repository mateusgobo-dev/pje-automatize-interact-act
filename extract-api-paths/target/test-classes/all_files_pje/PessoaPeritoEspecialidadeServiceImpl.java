package br.jus.cnj.pje.servicos;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.PessoaPeritoEspecialidadeDAO;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoEspecialidade;

@Name(value = "pessoaPeritoEspecialidadeService")
public class PessoaPeritoEspecialidadeServiceImpl implements PessoaPeritoEspecialidadeService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@In(create = true)
	private PessoaPeritoEspecialidadeDAO pessoaPeritoEspecialidadeDAO;
	
	public PessoaPeritoEspecialidade findByPessoaPeritoAndEspecialidade(PessoaPerito pessoaPerito, Especialidade especialidade) {
		return pessoaPeritoEspecialidadeDAO.findByPessoaPeritoAndEspecialidade(pessoaPerito, especialidade);
	}
}