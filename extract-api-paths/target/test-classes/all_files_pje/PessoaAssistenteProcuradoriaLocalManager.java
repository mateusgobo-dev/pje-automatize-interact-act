package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.PessoaAssistenteProcuradoriaLocalDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(PessoaAssistenteProcuradoriaLocalManager.NAME)
public class PessoaAssistenteProcuradoriaLocalManager extends BaseManager<PessoaAssistenteProcuradoriaLocal> {

	public static final String NAME = "pessoaAssistenteProcuradoriaLocalManager";
	
	@In
	private PessoaAssistenteProcuradoriaLocalDAO pessoaAssistenteProcuradoriaLocalDAO;
	
	@Override
	protected BaseDAO<PessoaAssistenteProcuradoriaLocal> getDAO() {
		return pessoaAssistenteProcuradoriaLocalDAO;
	}
	
	public PessoaAssistenteProcuradoriaLocal recuperar(Pessoa pessoa, Procuradoria procuradoria) {
		try {
			Search search = new Search(PessoaAssistenteProcuradoriaLocal.class);
			search.addCriteria(Criteria.equals("procuradoria", procuradoria));
			search.addCriteria(Criteria.equals("usuario.idUsuario", pessoa.getIdUsuario()));
			
			List<PessoaAssistenteProcuradoriaLocal> result = list(search);
			return result.isEmpty() ? null : result.get(0);
		} catch (NoSuchFieldException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
