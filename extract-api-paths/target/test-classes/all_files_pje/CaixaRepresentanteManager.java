/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.Collections;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.CaixaRepresentanteDAO;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(CaixaRepresentanteManager.NAME)
public class CaixaRepresentanteManager extends BaseManager<CaixaRepresentante> {
	
	public static final String NAME = "caixaRepresentanteManager";
	
	@In
	private CaixaRepresentanteDAO caixaRepresentanteDAO;

	@Override
	protected CaixaRepresentanteDAO getDAO() {
		return caixaRepresentanteDAO;
	}
	
	public List<PessoaFisica> findAllRepresentantesByCaixa(CaixaAdvogadoProcurador caixaAdvogadoProcurador){
		Search search = new Search(CaixaRepresentante.class);
		search.setRetrieveField("representante");
		addCriteria(search,Criteria.equals("caixaAdvogadoProcurador", caixaAdvogadoProcurador));
		
		List<PessoaFisica> ret = list(search);
		return ret;
	}	
	
	public List<CaixaRepresentante> getCaixasRepresentantes(Integer idPessoaFisica, Integer idCaixaAdvogadoProcurador) {
		return caixaRepresentanteDAO.getCaixasRepresentantes(idPessoaFisica, idCaixaAdvogadoProcurador);
	}
	
	public List<CaixaRepresentante> findAll() {
		return caixaRepresentanteDAO.findAll();
	}
	
	public List<CaixaRepresentante> getCaixaRepresentanteByRepresentante(Integer idPessoaFisica) {
		return caixaRepresentanteDAO.getCaixasRepresentantes(idPessoaFisica, null);
	}
	
	public List<CaixaRepresentante> getCaixaRepresentanteByCaixa(Integer idCaixaAdvogadoProcurador) {
		return caixaRepresentanteDAO.getCaixasRepresentantes(null, idCaixaAdvogadoProcurador);
	}
	
	/**
	 * Método responsável por recuperar as caixas associadas a um determinado usuário e localização.
	 * 
	 * @param idLocalizacao Identificador da localização.
	 * @param idUsuario Identificador do usuário.
	 * @return As caixas associadas a um determinado usuário e localização.
	 */
	public List<CaixaRepresentante> recuperarCaixasRepresentante(Integer idLocalizacao, Integer idUsuario) {
		try {
			Search search = new Search(CaixaRepresentante.class);
			search.addCriteria(Criteria.equals("caixaAdvogadoProcurador.localizacao.idLocalizacao", idLocalizacao));
			search.addCriteria(Criteria.equals("representante.idUsuario", idUsuario));
			return list(search);
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		}
		return Collections.emptyList();
	}
	
}
