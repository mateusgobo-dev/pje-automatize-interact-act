package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.NotaSessaoJulgamentoDAO;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;
import java.util.Map;

/**
 * Componente de controle negocial da entidade {@link NotaSessaoJulgamentoManager}.
 */
@Name("notaSessaoJulgamentoManager")
public class NotaSessaoJulgamentoManager extends BaseManager<NotaSessaoJulgamento> {
	
	@In
	private NotaSessaoJulgamentoDAO notaSessaoJulgamentoDAO;

	@Override
	protected NotaSessaoJulgamentoDAO getDAO() {
		return notaSessaoJulgamentoDAO;
	}
	
	public List<NotaSessaoJulgamento> recuperaNotas(Sessao sessao, ProcessoTrf processo){
		Search s = new Search(NotaSessaoJulgamento.class);
		s.setDistinct(true);
		s.addOrder("o.dataCadastro", Order.DESC);
		addCriteria(s,
				Criteria.equals("sessao", sessao),
				Criteria.equals("processoTrf", processo),
				Criteria.equals("ativo", Boolean.TRUE));
		return list(s);
	}
	
	public Map<Integer,Integer> contagemNotasPorProcesso(Sessao sessao){
		return notaSessaoJulgamentoDAO.contagemNotasPorProcesso(sessao);
	}

	/**
	 * metodo responsavel por recuperar todas as notas sessao julgamento onde a pessao passada em parametro é
	 * a pessoa que realizou o cadastro da nota.
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<NotaSessaoJulgamento> recuperarNotasSessaoJulgamento(Pessoa pessoa) throws Exception {
		return notaSessaoJulgamentoDAO.recuperarNotasSessaoJulgamento(pessoa);
	}

	public NotaSessaoJulgamento recuperarPorId(Integer _id) {
		return notaSessaoJulgamentoDAO.find(_id);
	}

}
