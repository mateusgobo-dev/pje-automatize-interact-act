/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * DAO LogHistoricoMovimentacaoDAO
 * 
 * @author Carlos Lisboa
 * @since 1.7.1
 */
@Name("logHistoricoMovimentacaoDAO")
public class LogHistoricoMovimentacaoDAO extends BaseDAO<LogHistoricoMovimentacao> {
	

	@Override
	public Object getId(LogHistoricoMovimentacao log) {
		// TODO Auto-generated method stub
		return log.getIdLog();
	}

	/**
	 * metodo responsavel por recuperar todos os logs da pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<LogHistoricoMovimentacao> recuperarLogs(Pessoa _pessoa) throws Exception {
		List<LogHistoricoMovimentacao> resultado = null;
		Search search = new Search(LogHistoricoMovimentacao.class);
		try {
			search.addCriteria(Criteria.equals("usuario.idUsuario", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar os logs históricos de movimentação de processo da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}


}
