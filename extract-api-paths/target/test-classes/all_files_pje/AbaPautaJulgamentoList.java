package br.com.jt.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.jt.entidades.PautaSessao;

@Name(AbaPautaJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AbaPautaJulgamentoList extends FiltrosPautaVotacaoAntecipadaList<PautaSessao> {
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "abaPautaJulgamentoList";
	
	protected static final String R14 = "o.sustentacaoOral = #{!abaPautaJulgamentoAction.sustentacaoOral ? null : abaPautaJulgamentoAction.sustentacaoOral}";
	protected static final String R15 = "o.preferencia = #{!abaPautaJulgamentoAction.preferencia ? null : abaPautaJulgamentoAction.preferencia}";
	protected static final String R16 = "o.processoTrf.orgaoJulgador in (#{abaPautaJulgamentoAction.orgaoJulgadorCheckedList})";
	protected static final String R17 = "o.resultadoVotacao in (#{abaPautaJulgamentoAction.resultadoVotacaoCheckedList})";
	
	@Override
	protected void addSearchFields() {
		super.addSearchFields();
		addSearchField("sustentacaoOral", SearchCriteria.igual, R14);
		addSearchField("preferencia", SearchCriteria.igual, R15);
		addSearchField("orgaoJulgador", SearchCriteria.contendo, R16);
		addSearchField("resultadoVotacao", SearchCriteria.contendo, R17);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder(FiltrosPautaVotacaoAntecipadaList.DEFAULT_EJBQL);
		sb.append("where o.sessao.idSessao = #{pautaJulgamentoAction.sessao.idSessao} ");
		sb.append("and o.processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("and o.processoTrf.orgaoJulgador = #{orgaoJulgadorAtual} ");
		}
		return sb.toString();
	}
	
}