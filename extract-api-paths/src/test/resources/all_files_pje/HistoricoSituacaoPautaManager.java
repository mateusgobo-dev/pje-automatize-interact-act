package br.com.jt.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.HistoricoSituacaoPautaDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.jt.entidades.HistoricoSituacaoPauta;
import br.jus.pje.jt.entidades.PautaSessao;

@Name(HistoricoSituacaoPautaManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class HistoricoSituacaoPautaManager extends BaseManager<HistoricoSituacaoPauta>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "historicoSituacaoPautaManager";
	
	@In
	private HistoricoSituacaoPautaDAO historicoSituacaoPautaDAO;
	
	@Override
	protected BaseDAO<HistoricoSituacaoPauta> getDAO(){
		return historicoSituacaoPautaDAO;
	}
	
	public void gravarHistorico(PautaSessao pautaSessao){
		HistoricoSituacaoPauta hist = new HistoricoSituacaoPauta();
		hist.setDataSituacaoPauta(pautaSessao.getDataSituacaoPauta());
		hist.setPautaSessao(pautaSessao);
		hist.setTipoSituacaoPauta(pautaSessao.getTipoSituacaoPauta());
		hist.setUsuarioSituacaoPauta(pautaSessao.getUsuarioSituacaoPauta());
		// verificar se já existe HistoricoSituacaoPauta equivalente
		HistoricoSituacaoPauta historicoSituacaoPauta = getHistoricoComMesmaDataESessao(pautaSessao);
		if (historicoSituacaoPauta != null) {
			hist.setIdHistoricoSituacaoPauta(historicoSituacaoPauta.getIdHistoricoSituacaoPauta());
			historicoSituacaoPautaDAO.update(hist);
		} else {
			historicoSituacaoPautaDAO.persist(hist);
		}
	}

	private HistoricoSituacaoPauta getHistoricoComMesmaDataESessao(PautaSessao pautaSessao){
		return historicoSituacaoPautaDAO.getHistoricoComMesmaDataESessao(pautaSessao);
	}

}