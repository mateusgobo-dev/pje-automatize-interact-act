package br.jus.cnj.pje.nucleo.manager;


import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.PessoaNomeAlternativoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;

@Name(PessoaNomeAlternativoManager.NAME)
public class PessoaNomeAlternativoManager extends BaseManager<PessoaNomeAlternativo>{
	
	public static final String NAME = "pessoaNomeAlternativoManager";
	public static final String ERRO_NOME_USADO = "Este nome já está sendo usado em processo, portanto não pode ser editado ou excluído.";
	
	@In
	private PessoaNomeAlternativoDAO pessoaNomeAlternativoDAO;

	@Override
	protected PessoaNomeAlternativoDAO getDAO() {
		return pessoaNomeAlternativoDAO;
	}
	
	/**
 	 * metodo responsavel por recuperar o nome alternativo
 	 * @param nomeAlternativo
 	 * @return
 	 */
 	public List<PessoaNomeAlternativo> recuperaNomesAlternativos(String nomeAlternativo) {
 		return pessoaNomeAlternativoDAO.recuperaNomesAlternativos(nomeAlternativo);
 	}

 	/**
 	 * metodo responsavel por recuperar todos os nomes alternativos cadastrados pela pessoa passada em parametro
 	 * @param _pessoa
 	 * @return
 	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosCadastrados(Pessoa _pessoa) {
		return pessoaNomeAlternativoDAO.recuperaNomesAlternativosCadastrados(_pessoa);
	}

	/**
	 * metodo responsavel por recuperar todos os nomes alteranativos que a pessoa passada em parametro é proprietaria
	 * @param _pessoa
	 * @return
	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosProprietarios(Pessoa _pessoa) {
		return pessoaNomeAlternativoDAO.recuperaNomesAlternativosProprietarios(_pessoa);
	}
	
	
	/**
	 * metodo responsavel por recuperar todos os nomes alteranativos que a pessoa passada em parametro é proprietaria
	 * @param _pessoa
	 * @return
	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosProprietarios(Pessoa pessoa, TipoNomeAlternativoEnum tipo) {
		return pessoaNomeAlternativoDAO.recuperaNomesAlternativosProprietarios(pessoa, tipo);
	}
	
	/**
	 * Verifica se o pessoaNomeAlternativo está sendo usdado em alguma parte em algum processo
	 * @param pessoaNomeAlternativo
	 * @return
	 */
	public Boolean isNomeAlternativoEstaSendoUsado(PessoaNomeAlternativo pessoaNomeAlternativo) {
		return pessoaNomeAlternativoDAO.isNomeAlternativoEstaSendoUsado(pessoaNomeAlternativo);

	}
	
	/**
	 * Verifica se o pessoaNomeAlternativo está sendo usdado em alguma parte em algum processo
	 * @param pessoaNomeAlternativo
	 * @return
	 */
	public Boolean isNomeAlternativoEstaSendoUsado(Integer idPessoaNomeAlternativo) {
		return pessoaNomeAlternativoDAO.isNomeAlternativoEstaSendoUsado(idPessoaNomeAlternativo);

	}
	
}