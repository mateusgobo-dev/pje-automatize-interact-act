package br.jus.cnj.pje.business.dao;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaNomeAlternativo;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.TipoNomeAlternativoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaNomeAlternativoDAO.NAME)
public class PessoaNomeAlternativoDAO extends BaseDAO<PessoaNomeAlternativo> {
	
	public static final String NAME = "pessoaNomeAlternativoDAO";

	@Override
	public Object getId(PessoaNomeAlternativo e) {
		return e.getIdPessoaNomeAlternativo();
	}

	/**
 	 * Recupera nome alternativo
 	 * @param nomeAlternativo
 	 * @return List<PessoaNomeAlternativo> / lista vazia
 	 */
 	@SuppressWarnings("unchecked")
 	public List<PessoaNomeAlternativo> recuperaNomesAlternativos(String nomeAlternativo){
 		List<PessoaNomeAlternativo> resultados = new ArrayList<PessoaNomeAlternativo>(0);
 		StringBuilder sb = new StringBuilder();
 		sb.append("SELECT na FROM PessoaNomeAlternativo AS na ");
 		sb.append("WHERE pessoaNomeAlternativo = :nomeAlternativo ");
 		
 		Query q = entityManager.createQuery(sb.toString());
 		q.setParameter("nomeAlternativo", nomeAlternativo);
 		resultados = q.getResultList();
 	
 		return resultados;
 	}

 	/**
 	 * recupera todos os nomes alternativos cadastrados pela pessoa passada em parametro.
 	 * @param _pessoa
 	 * @return
 	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosCadastrados(Pessoa _pessoa) {
		List<PessoaNomeAlternativo> resultado = null;
		Search search = new Search(PessoaNomeAlternativo.class);
		try {
			search.addCriteria(Criteria.equals("usuarioCadastrador.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}

	/**
	 * recupera todos os nomes alternativos onde a pessoa passada em parametro é a proprietaria
	 * @param _pessoa
	 * @return
	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosProprietarios(Pessoa _pessoa) {
		List<PessoaNomeAlternativo> resultado = null;
		Search search = new Search(PessoaNomeAlternativo.class);
		try {
			search.addCriteria(Criteria.equals("pessoa.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
	
	/**
	 * recupera todos os nomes alternativos onde a pessoa passada em parametro é a proprietaria levanto em consideração o tipo do nome alternativo
	 * @param _pessoa
	 * @param tipo
	 * @return
	 */
	public List<PessoaNomeAlternativo> recuperaNomesAlternativosProprietarios(Pessoa _pessoa, TipoNomeAlternativoEnum tipo) {
		List<PessoaNomeAlternativo> resultado = null;
		Search search = new Search(PessoaNomeAlternativo.class);
		try {
			search.addCriteria(Criteria.equals("pessoa.idUsuario", _pessoa.getIdPessoa()));
			search.addCriteria(Criteria.equals("tipoNomeAlternativo", tipo));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
	
	/**
	 * Verifica se o pessoaNomeAlternativo está sendo usdado em alguma parte em algum processo
	 * @param pessoaNomeAlternativo
	 * @return
	 */
	public Boolean isNomeAlternativoEstaSendoUsado(PessoaNomeAlternativo pessoaNomeAlternativo) {
		Search search = new Search(ProcessoParte.class);
		try {
			search.addCriteria(Criteria.equals("pessoaNomeAlternativo", pessoaNomeAlternativo));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		search.setMax(1);
		return count(search) > 0 ? Boolean.TRUE : Boolean.FALSE;
	}
	
	/**
	 * Verifica se o pessoaNomeAlternativo está sendo usdado em alguma parte em algum processo
	 * @param pessoaNomeAlternativo
	 * @return
	 */
	public Boolean isNomeAlternativoEstaSendoUsado(Integer idPessoaNomeAlternativo) {
		Search search = new Search(ProcessoParte.class);
		try {
			search.addCriteria(Criteria.equals("pessoaNomeAlternativo.idPessoaNomeAlternativo", idPessoaNomeAlternativo));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		search.setMax(1);
		return count(search) > 0 ? Boolean.TRUE : Boolean.FALSE;
	}
	
	
	
}