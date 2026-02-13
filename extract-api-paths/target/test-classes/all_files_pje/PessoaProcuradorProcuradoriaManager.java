package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.PessoaProcuradorProcuradoriaDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcuradorProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

/**
 * Classe com métodos referentes a regra de negócio da entidade de
 * PessoaProcuradorProcuradoria
 * 
 * @author Joao Paulo Lacerda
 * 
 */
@Name(PessoaProcuradorProcuradoriaManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PessoaProcuradorProcuradoriaManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaProcuradorProcuradoriaManager";

	@In
	private PessoaProcuradorProcuradoriaDAO pessoaProcuradorProcuradoriaDAO;
	@In
	private PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager;

	public List<PessoaProcuradorProcuradoria> getPessoaProcuradorProcuradoriaList(
			PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade) {
		return pessoaProcuradorProcuradoriaDAO.getPessoaProcuradorProcuradoriaList(pessoaProcuradoriaEntidade);
	}

	public List<PessoaProcuradorProcuradoria> getPessoaProcuradorProcuradoriaList(Pessoa pessoa) {
		PessoaProcuradoriaEntidade pessoaProcuradoriaEntidade = pessoaProcuradoriaEntidadeManager
				.getPessoaProcuradoriaEntidade(pessoa);
		if (pessoaProcuradoriaEntidade != null) {
			return getPessoaProcuradorProcuradoriaList(pessoaProcuradoriaEntidade);
		}
		return Collections.emptyList();
	}

	/**
	 * Este método recebe como argumento uma pessoa e valida se existe
	 * procurador vinculado com situação <i>Ativa</i> e que possua certificado
	 * cadastrado no sistema.</li>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param pessoa
	 *            pessoa a ser validada.
	 * @return <code>True</code> caso seja um procurador certificado ou
	 *         <code>False</code> caso não seja.
	 */
	public boolean isProcuradorCertificado(Pessoa pessoa) {
		List<PessoaProcuradorProcuradoria> pessoaProcuradorProcuradoriaList = getPessoaProcuradorProcuradoriaList(pessoa);
		if (pessoaProcuradorProcuradoriaList != null) {
			for (PessoaProcuradorProcuradoria ppp : pessoaProcuradorProcuradoriaList) {
				if (ppp.getPessoaProcurador().getAtivo() && ppp.getPessoaProcurador().getCertChain() != null
						&& ppp.getPessoaProcurador().getAssinatura() != null) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean existeProcuradorEntidade(int idPessoa) {
		return pessoaProcuradorProcuradoriaDAO.existeProcuradorEntidade(idPessoa);
	}

}