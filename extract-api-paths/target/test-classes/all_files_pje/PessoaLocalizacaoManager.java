/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.PessoaLocalizacaoDAO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;

/**
 * @author cristof
 *
 */
@Name("pessoaLocalizacaoManager")
public class PessoaLocalizacaoManager extends BaseManager<PessoaLocalizacao> {
	
	@In
	private PessoaLocalizacaoDAO pessoaLocalizacaoDAO;
	
	@Override
	protected PessoaLocalizacaoDAO getDAO() {
		return pessoaLocalizacaoDAO;
	}

	
	/**
	 * Recupera a lista de {@link PessoaLocalizacao} afetadas a uma dada pessoa.
	 * 
	 * @param p a pessoa a respeito da qual se pretende obter a informação
	 * @return a lista de {@link PessoaLocalizacao}
	 */
	public List<PessoaLocalizacao> recuperaPorPessoa(Pessoa p){
		return pessoaLocalizacaoDAO.findByRange(null, null, p, "pessoa");
	}

	/**
	 * Recupera a lista de {@link PessoaLocalizacao} afetadas a uma dada localizacao.
	 * 
	 * @param l a localização a respeito da qual se pretende obter a informação.
	 * @return a lista de {@link PessoaLocalizacao}
	 */
	public List<PessoaLocalizacao> recuperaPorLocalizacao(Localizacao l){
		return pessoaLocalizacaoDAO.findByRange(null, null, l, "localizacao");
	}

	public PessoaLocalizacao recuperaUnivoca(Pessoa p, Localizacao l){
		return pessoaLocalizacaoDAO.recuperaUnivoca(p, l);
	}

}
