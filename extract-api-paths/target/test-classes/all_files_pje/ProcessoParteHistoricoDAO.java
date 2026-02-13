/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteHistorico;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de acesso a dados da entidade {@link ProcessoParteHistorico}.
 * 
 * @author cristof
 *
 */
@Name("processoParteHistoricoDAO")
public class ProcessoParteHistoricoDAO extends BaseDAO<ProcessoParteHistorico> {

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.business.dao.BaseDAO#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(ProcessoParteHistorico e) {
		return e.getIdProcessoParteHistorico();
	}

	/**
	 * recupera todos os processosParteHistoricos da pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<ProcessoParteHistorico> recuperaProcessosParteHistoricos(Pessoa _pessoa) throws Exception{
		List<ProcessoParteHistorico> resultado = null;
		Search search = new Search(ProcessoParteHistorico.class);
		try {
			search.addCriteria(Criteria.equals("usuarioLogin.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os processos parte históricos da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(".");
			sb.append(" Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}

}
