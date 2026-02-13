/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.AgrupamentoClasseJudicialDAO;
import br.jus.pje.nucleo.entidades.AgrupamentoClasseJudicial;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;

/**
 * @author cristof
 *
 */
@Name(AgrupamentoClasseJudicialManager.NAME)
public class AgrupamentoClasseJudicialManager extends BaseManager<AgrupamentoClasseJudicial> {
	
	public static final String NAME = "agrupamentoClasseJudicialManager";
	
	@In
	private AgrupamentoClasseJudicialDAO agrupamentoClasseJudicialDAO;

	@Override
	protected AgrupamentoClasseJudicialDAO getDAO() {
		return agrupamentoClasseJudicialDAO;
	}
	
	/**
	 * Recupera um agrupamento de classes pelo seu código identificador.
	 * 
	 * @param codigoAgrupamento o código do agrupamento a ser recuperado
	 * @return o agrupamento com o código específico
	 */
	public AgrupamentoClasseJudicial findByCodigo(String codigo){
		return agrupamentoClasseJudicialDAO.findByCodigo(codigo);
	}
	
	/**
	 * Indica se uma determinada classe faz parte do(s) agrupamento(s) indicado(s).
	 * 
	 * @param classe a classe a ser avaliada
	 * @param codigosAgrupamentos os códigos identificadores dos agrupamentos
	 * @return true, se a classe fizer parte de pelo menos um dos agrupamentos indicados
	 */
	public boolean pertence(ClasseJudicial classe, String...codigosAgrupamentos){
		if(codigosAgrupamentos == null || codigosAgrupamentos.length == 0){
			return false;
		}
		return agrupamentoClasseJudicialDAO.pertence(classe, codigosAgrupamentos);
	}
	
	/**
	 * Indica se um determinado assunto faz parte do(s) agrupamento(s) indicado(s).
	 * 
	 * @param assunto o assunto a ser avaliado
	 * @param codigosAgrupamentos os códigos identificadores dos agrupamentos
	 * @return true, se o assunto fizer parte de pelo menos um dos agrupamentos indicados
	 */
	public boolean pertence(AssuntoTrf assunto, String... codigosAgrupamentos){
		if(codigosAgrupamentos == null || codigosAgrupamentos.length == 0){
			return false;
		}
		return agrupamentoClasseJudicialDAO.pertence(assunto, codigosAgrupamentos);
	}

}
