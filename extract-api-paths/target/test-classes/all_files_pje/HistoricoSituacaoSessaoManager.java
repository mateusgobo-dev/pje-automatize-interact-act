package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.jus.pje.jt.entidades.HistoricoSituacaoSessao;
import br.jus.pje.jt.entidades.SessaoJT;

@Name(HistoricoSituacaoSessaoManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class HistoricoSituacaoSessaoManager extends GenericManager{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "historicoSituacaoSessaoManager";
	
	public void gravarHistorico(SessaoJT sessao){
		HistoricoSituacaoSessao hist = new HistoricoSituacaoSessao();
		hist.setDataSituacaoSessao(sessao.getDataSituacaoSessao());
		hist.setSessao(sessao);
		hist.setSituacaoSessao(sessao.getSituacaoSessao());
		hist.setUsuarioSituacaoSessao(sessao.getUsuarioSituacaoSessao());
		persist(hist);
	}
	
}