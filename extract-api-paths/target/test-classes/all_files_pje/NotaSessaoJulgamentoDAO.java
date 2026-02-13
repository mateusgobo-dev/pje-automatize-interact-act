/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;
import java.util.HashMap;
import java.util.Map;

/**
 * Componete de acesso a dados da entidade {@link NotaSessaoJulgamento}.
 * 
 * @author cristof
 *
 */
@Name("notaSessaoJulgamentoDAO")
public class NotaSessaoJulgamentoDAO extends BaseDAO<NotaSessaoJulgamento> {

	@Override
	public Integer getId(NotaSessaoJulgamento e) {
		return e.getIdNotaSessaoJulgamento();
	}

	/**
	 * metodo responsavel por recuperar todas as notas sessao julgamento onde a pessao passada em parametro é
	 * a pessoa que realizou o cadastro da nota.
	 * @param _pessoa
	 * @return
	 * @throws Exception
	 */
	public List<NotaSessaoJulgamento> recuperarNotasSessaoJulgamento(Pessoa _pessoa) throws Exception {
		List<NotaSessaoJulgamento> resultado = null;
		Search search = new Search(NotaSessaoJulgamento.class);
		try {
			search.addCriteria(Criteria.equals("usuarioCadastro.idUsuario", _pessoa.getIdPessoa()));			
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		try {
			resultado = list(search);
		} catch (EntityNotFoundException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ocorreu um erro ao tentar recuperar as notas em sessao julgamento da pessoa ");
			sb.append(_pessoa.getNome());
			sb.append(". Por favor, contacte o suporte do tribunal.");
			
			throw new Exception(sb.toString());
		}
		return resultado;
	}

	/**
	 * Dada uma Sessão, recupera a contagem de anotações para os processos que 
	 * contém ao menos uma anotação..
	 * @param idSessao A sessão que se quer recuperar a contagem.
	 * @return Um mapa contendo o idProcessoTrf e sua respectiva contagem de 
	 * anotações.
	 */
	public Map<Integer, Integer> contagemNotasPorProcesso(Sessao sessao) {
		return contagemNotasPorProcesso(sessao.getIdSessao());
	}
	
	/**
	 * Dada uma Sessão, recupera a contagem de anotações para os processos que 
	 * contém ao menos uma anotação..
	 * @param idSessao A sessão que se quer recuperar a contagem.
	 * @return Um mapa contendo o idProcessoTrf e sua respectiva contagem de 
	 * anotações.
	 */
	public Map<Integer, Integer> contagemNotasPorProcesso(Integer idSessao) {
		List list = getEntityManager().createQuery(
				"SELECT n.processoTrf.idProcessoTrf, COUNT(n) " 
				+ " FROM NotaSessaoJulgamento n"
				+ " WHERE n.sessao.idSessao = :idSessao"
				+ " GROUP BY n.processoTrf.idProcessoTrf"
		)
				.setParameter("idSessao", idSessao)
				.getResultList();
		
		Map<Integer,Integer> map = new HashMap<Integer,Integer>(list.size());
		for (Object objRow: list) {
			Object[] row = (Object[])objRow;
			map.put((Integer)row[0], ((Number)row[1]).intValue());
		}
		return map;
	}

	/**
	 * Dada uma Sessão, recupera a contagem de anotações para cada processo que 
	 * a compõe.
	 * @param idSessao A sessão que se quer recuperar a contagem.
	 * @return Um mapa contendo o idProcessoTrf e sua respectiva contagem de 
	 * anotações.
	 */
	public Map<Integer, Long> contagemNotasPorCadaProcesso(Integer idSessao) {
		List list = getEntityManager().createQuery(
				"SELECT s.processoTrf.idProcessoTrf, COUNT(n) " 
				+ " FROM SessaoPautaProcessoTrf s, "
				+ "      NotaSessaoJulgamento n"
				+ " WHERE ((n IS NULL) OR (n.processoTrf==s.processoTrf) AND (n.sessao = s.sessao))"
				+ "   AND (s.sessao.idSessao = :idSessao)"
				+ " GROUP BY n.processoTrf.idProcessoTrf"
		)
				.setParameter("idSessao", idSessao)
				.getResultList();
		
		Map<Integer,Long> map = new HashMap<Integer,Long>(list.size());
		for (Object objRow: list) {
			Object[] row = (Object[])objRow;
			map.put((Integer)row[0], (Long)row[1]);
		}
		return map;
	}

}
