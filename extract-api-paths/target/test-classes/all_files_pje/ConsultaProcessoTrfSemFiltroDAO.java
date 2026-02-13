/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ConsultaProcessoTrfSemFiltro;

/**
 * [PJEII-20838] - Alterar a tela de pesquisa por processos para solicitar a habilitação nos autos.
 * Classe criada para atender a Issue acima, visto que a implementação antiga não utilizava camadas e a consulta era feita via xml.
 */
@Name("consultaProcessoTrfSemFiltroDAO")
public class ConsultaProcessoTrfSemFiltroDAO extends BaseDAO<ConsultaProcessoTrfSemFiltro>{
	
	@Override
	public Integer getId(ConsultaProcessoTrfSemFiltro e){
		return e.getIdProcessoTrf();
	}

}
