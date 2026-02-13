package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.utils.Constantes;
import br.jus.cnj.pje.business.dao.AssuntoTrfDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.AssuntoTrfDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(AssuntoTrfManager.NAME)
public class AssuntoTrfManager extends BaseManager<AssuntoTrf>{
	public static final String NAME = "assuntoTrfManager";

	@In
	private AssuntoTrfDAO assuntoTrfDAO;
	@Override
	protected AssuntoTrfDAO getDAO() {
		return assuntoTrfDAO;
	}

	/**
	 * @return AssuntoTrfManager
	 */
	public static AssuntoTrfManager instance() {
		return (AssuntoTrfManager)Component.getInstance(AssuntoTrfManager.NAME);
	}
	
	public List<AssuntoTrf> carregarAssuntosJudiciais(Integer idJurisdicao, Integer idClasseJudicial,
			List<String> codigoAssuntoList) throws PJeBusinessException{
		
		List<AssuntoTrf> assuntoTrfList = assuntoTrfDAO.carregarAssuntosJudiciais(idJurisdicao, idClasseJudicial, codigoAssuntoList);
		
		// Verifica se todos os códigos de assuntos informados existem na base
		for (String codigoAssunto : codigoAssuntoList) {
			boolean encontrado = false;
			for (AssuntoTrf assuntoTrf : assuntoTrfList) {
				if (assuntoTrf.getCodAssuntoTrf().equals(String.valueOf(codigoAssunto))) {
					encontrado = true;
					break;
				}
			}
			if (!encontrado) {
				throw new PJeBusinessException(String.format("Assunto de código %s inválido", codigoAssunto));
			}
		}
		
		return assuntoTrfList;
	}

	public AssuntoTrf findByCodigo(String codigo, Boolean ativos) throws PJeException{
		try{
			return assuntoTrfDAO.findByCodigo(codigo);
		}catch (NoResultException e){
			throw new PJeException("pje.assunto.codigoinexistente", e, codigo);
		}catch (NonUniqueResultException e) {
			throw new PJeException("pje.assunto.multiploscodigo", e, codigo);
		}
	}
	
	public AssuntoTrf findByCodigo(String codigo) throws PJeException{
		return this.findByCodigo(codigo, null);
	}
	
	/**
	 * Consulta o AssuntoTrf pelo código.
	 * 
	 * @param codigo
	 * @return AssuntoTrf
	 * @throws PJeException
	 */
	public AssuntoTrf findByCodigo(Integer codigo) throws PJeException{
		AssuntoTrf resultado = null;
		
		if (codigo != null) {
			resultado = findByCodigo(codigo.toString());
		}
		return resultado;
	}
	
	public AssuntoTrf findByNome(String nome) {
		return this.assuntoTrfDAO.findByNome(nome);
	}
	
    /**
     * Verifica se um dado assunto tem alguns filhos.
     * 
     * @param assunto assunto a ser pesquisado.
     * @return true, se o assunto for pai de algum outro assunto.
     */
    public boolean hasChildren(AssuntoTrf assunto){
    	return assuntoTrfDAO.hasChildren(assunto);
    }
    
    public List<AssuntoTrf> findAssuntosTrfPorProcessoTrf(ProcessoTrf processoJudicial){
    	
    	Search s = new Search(AssuntoTrf.class);
    	
    	try {
			s.addCriteria(Criteria.equals("processoAssuntoList.processoTrf", processoJudicial));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
    	
    	List<AssuntoTrf> result = list(s);
    	
    	return result;
    	
    }   
    
    public List<AssuntoTrfDTO> findAllAssuntoTrfDTO(){
    	return this.assuntoTrfDAO.findAllAssuntoTrfDTO();
    }
    
    public List<AssuntoTrfDTO> findAssuntoTrfFolhaDTO(){
    	return this.assuntoTrfDAO.findAssuntoTrfFolhaDTO();
    }
    
      public List<AssuntoTrfDTO> findAllAssuntoTrfDTOByListCodigo(List<String> listaCodigos){
    	return this.assuntoTrfDAO.findAllAssuntoTrfDTOByListCodigo(listaCodigos);
    }

	public boolean isAreaDiretoConsumo(Integer idAreaDireito) {
		boolean result = false;

		if (idAreaDireito != null) {
			AssuntoTrf assuntoTrf = this.assuntoTrfDAO.find(idAreaDireito);
			if (assuntoTrf != null) {
				result = Constantes.CODIGO_AREA_DIREITO_CONSUMO.equals(assuntoTrf.getCodAssuntoTrf());
			}
		}

		return result;
	}
	
	    public List<AssuntoTrf> findAllAssuntoTrfByListCodigo(List<String> listaCodigos){
    	return this.assuntoTrfDAO.findAllAssuntoTrfByListCodigo(listaCodigos);
    }

	public Integer obtemAreaDireito(AssuntoTrf assuntoTrf) {
		return this.assuntoTrfDAO.obtemAreaDireito(assuntoTrf);
	}

}
