package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaAutoridadeDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;

@Name(PessoaAutoridadeManager.NAME)
public class PessoaAutoridadeManager extends AbstractUsuarioManager<PessoaAutoridade, PessoaAutoridadeDAO> {
	
	public static final String NAME = "pessoaAutoridadeManager";

	@In
	private PessoaAutoridadeDAO pessoaAutoridadeDAO;
	
	@Override
	protected BaseDAO<PessoaAutoridade> getDAO() {
		return pessoaAutoridadeDAO;
	}
	
	/**
	 * Recupera as autoridades vinculadas ao orgaoVinculacao
	 * @param orgaoVinculacao
	 * @return
	 * @throws Exception
	 */
	public List<PessoaAutoridade> findByOrgaoVinculacao(Pessoa orgaoVinculacao) {
		return pessoaAutoridadeDAO.findByOrgaoVinculacao(orgaoVinculacao);
	}
	
	/**
	 * Recupera as autoridades vinculadas ao orgaoVinculacao e com mesmo nome
	 * @param orgaoVinculacao, nome
	 * @return
	 * @throws Exception
	 */
	public PessoaAutoridade findByOrgaoVinculacaoENome(Pessoa orgaoVinculacao, String nomeAutoridade) {
		PessoaAutoridade autoridadeVinculada = null;
		
		for(PessoaAutoridade autoridade : this.findByOrgaoVinculacao(orgaoVinculacao)){
			if(autoridade.getNome().equals(nomeAutoridade)){
				autoridadeVinculada = autoridade; 
				break;
			}
		}
		
		return autoridadeVinculada;
	}

	/**
	 * metodo responsavel por buscar no banco de dados a PessoaAutoridade da pessoa passada em parametro.
	 * nao dispara exceçao caso nao encontre o objeto, retornando null.
	 * @param pessoa
	 * @return PessoaFisica / null
	 */
	public PessoaAutoridade encontraPessoaAutoridadePorPessoa(Pessoa pessoa) {
		return pessoaAutoridadeDAO.recuperaPessoaAutoridadePelaID(pessoa.getIdPessoa());
	}
	
	/**
 	 * metodo que verifica se existe uma instancia da pessoa passada em parametro no banco de dados da respectiva tabela
 	 * @param pessoa
 	 * @return true / false
 	 */
 	public boolean verficarPessoaExisteTabelaEnteAutoridade(Pessoa pessoa) {
 		return (encontraPessoaAutoridadePorPessoa(pessoa) != null ? Boolean.TRUE : Boolean.FALSE);
 	}

	/**
	 * Filtra os PessoaAutoridade de acordo com o nome informado.
	 * 
	 * @param nome String nome a ser filtrado.
	 * @return List<PessoaAutoridade> lista com os PessoaAutoridade.
	 */
	public List<PessoaAutoridade> filtrarPessoaAutoridade(String nome) {
		return pessoaAutoridadeDAO.filtrarPessoaAutoridade(nome);
	}
}