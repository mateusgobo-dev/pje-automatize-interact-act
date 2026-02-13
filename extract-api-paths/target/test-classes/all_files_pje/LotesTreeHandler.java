package br.com.infox.cliente.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.dao.SituacaoProcessoDAO;

@Name("lotesTree")
@BypassInterceptors
public class LotesTreeHandler extends TarefasTreeHandler {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(max(s.idSituacaoProcesso) as id, ");
		sb.append("s.nomeTarefa as nomeTarefa, ");
		sb.append("max(s.idTarefa) as idTask, ");
		sb.append("count(s.nomeCaixa) as qtdEmCaixa, ");
		sb.append("count(s.idTarefa) as qtd,");
		// sb.append("count(s.idLote) as qtdEmLote, ");
		sb.append("(select count(distinct pl.processoTrf) from ProcessoLote pl where pl.lote.tarefa.idTarefa = (SELECT max(sp_s.idTarefa) FROM SituacaoProcesso sp_s WHERE sp_s.idTarefa = s.idTarefa)) as qtdEmLote,");
		sb.append("'Task' as type,");
		sb.append("'");
		sb.append(isSegredo());
		sb.append("' as segredo,");
		sb.append("'");
		sb.append(getTreeType());
		sb.append("' as tree) ");

		SituacaoProcessoDAO situacaoProcessoDAO = (SituacaoProcessoDAO) Component.getInstance(SituacaoProcessoDAO.class, true);
		sb.append(situacaoProcessoDAO.getQueryFromTarefasPermissoes("s", isSegredo(), Authenticator.isVisualizaSigiloso(), Authenticator.getIdsLocalizacoesFilhasAtuais(), 
				Authenticator.isServidorExclusivoColegiado(), Authenticator.getIdOrgaoJulgadorColegiadoAtual()));
		
		sb.append("group by s.nomeTarefa, s.idTarefa ");
		sb.append("order by 2");
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(l.idLote as idLote,");
		sb.append("l.tarefa.idTarefa as idTarefa, ");
		sb.append("l.lote as nomeLote, ");
		sb.append("'Lote' as type, ");
		sb.append("(select count(p) from l.processoTrfList p) as qtd, ");
		sb.append("'");
		sb.append(getTreeType());
		sb.append("' as tree) ");
		sb.append("from Lote l where l.tarefa.idTarefa = :taskId ");
		sb.append("order by l.lote");
		return sb.toString();
	}

	@Override
	protected String getEventSelected() {
		return "selectedLoteTreeEvent";
	}

	@Override
	protected String getTreeType() {
		return "lote";
	}
}