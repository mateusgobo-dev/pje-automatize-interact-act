package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.SolicitacaoNoDesvioDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.SolicitacaoNoDesvio;

@Name(SolicitacaoNoDesvioManager.NAME)
public class SolicitacaoNoDesvioManager extends BaseManager<SolicitacaoNoDesvio>{
	
	public static final String NAME = "solicitacaoNoDesvioManager";
	
	@In
	private SolicitacaoNoDesvioDAO solicitacaoNoDesvioDAO;

	@Override
	protected SolicitacaoNoDesvioDAO getDAO() {
		return solicitacaoNoDesvioDAO;
	}

	/**
	 * metodo responsavel por recuperar todas as solicitacoes para o no de desvio da pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<SolicitacaoNoDesvio> recuperarSolicitacoesNoDesvio(Pessoa _pessoa) throws Exception {
		return solicitacaoNoDesvioDAO.recuperarSolicitacoesNoDesvio(_pessoa);
	}

	public SolicitacaoNoDesvio recuperaSolicitacaoNoDesvio(Integer idSolicitacaoNoDesvio) {
		return solicitacaoNoDesvioDAO.find(idSolicitacaoNoDesvio);
	}
}