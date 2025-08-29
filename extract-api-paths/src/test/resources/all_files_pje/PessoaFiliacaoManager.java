package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaFiliacaoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFiliacao;

@Name(PessoaFiliacaoManager.NAME)
public class PessoaFiliacaoManager extends BaseManager<PessoaFiliacao>{

	public static final String NAME = "pessoaFiliacaoManager";

	@In
	private PessoaFiliacaoDAO pessoaFiliacaoDAO;
	
	public List<PessoaFiliacao> recuperaFiliacoes(Pessoa pessoa){
		return pessoaFiliacaoDAO.recuperaFiliacoesDaPessoa(pessoa);
	}
	
	@Override
	protected BaseDAO<PessoaFiliacao> getDAO() {
		return this.pessoaFiliacaoDAO;
	}
	
}
