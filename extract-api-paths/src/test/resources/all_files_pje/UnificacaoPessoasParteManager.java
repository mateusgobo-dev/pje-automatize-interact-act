package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.UnificacaoPessoasParteDAO;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.UnificacaoPessoasParte;

@Name(UnificacaoPessoasParteManager.NAME)
public class UnificacaoPessoasParteManager extends BaseManager<UnificacaoPessoasParte>{

	public static final String NAME = "unificacaoPessoasParteManager";
	
	@In
	private UnificacaoPessoasParteDAO unificacaoPessoasParteDAO;

	@Override
	protected UnificacaoPessoasParteDAO getDAO() {
		return unificacaoPessoasParteDAO;
	}
	
	/**
	 * Recupera lista de partes alteradas em uma unificação.
	 * 
	 * @param processoparte {@link ProcessoParte}.
	 * @return Lista de partes no processo alterado.
	 */
	public List<UnificacaoPessoasParte> recuperarUnificacaoPessoasParte(ProcessoParte pp) {
		return getDAO().recuperarUnificacaoPessoasParte(pp);
	}
}