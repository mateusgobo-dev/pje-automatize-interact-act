package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.jt.entidades.PautaSessao;

@Name(PautaSessaoIntimacoesPrazoEsgotado10DiasJTList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PautaSessaoIntimacoesPrazoEsgotado10DiasJTList extends AbstractPautaSessaoJTList<PautaSessao> {
	
	public static final String NAME = "pautaSessaoIntimacoesPrazoEsgotado10DiasJTList";

	private static final long serialVersionUID = 1L;
	
	@Override
	public List<PautaSessao> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	/* 
	 * PJE-JT: Ricardo Scholz : PJE-1398 - 2012-07-11 Alteracoes feitas pela JT.
	 * Correção do HQL, de forma a incluir não apenas as tuplas em que o usuário logado 
	 * é parte, mas também as tuplas nas quais ele é representante ou procurador de uma parte.
	 */
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PautaSessao o ");
		sb.append("where o.sessao.dataFechamentoPauta is not null ");
		if(super.getRelatorio() != null && super.getRelatorio()){
			sb.append("and o.processoTrf.idProcessoTrf in (select c.processo.idProcesso from ProcessoDocumento c ");
			sb.append(									  "where c.processo.idProcesso = o.processoTrf.idProcessoTrf ");
			sb.append(									  "and c.tipoProcessoDocumento.tipoProcessoDocumento like 'Relatório') ");
		}else if(super.getRelatorio() != null){
			sb.append("and o.processoTrf.idProcessoTrf not in (select c.processo.idProcesso from ProcessoDocumento c ");
			sb.append(										  "where c.processo.idProcesso = o.processoTrf.idProcessoTrf ");
			sb.append(										  "and c.tipoProcessoDocumento.tipoProcessoDocumento like 'Relatório') ");
		}
		sb.append("and o.processoTrf in (select ppe.processoJudicial from ProcessoParteExpediente ppe ");
		sb.append(						"where ppe.processoJudicial = o.processoTrf ");
		sb.append(						"and ( ");
		sb.append(							"ppe.pessoaParte = #{usuarioLogado} ");
		sb.append(							"or ppe.pessoaParte in ( ");
		sb.append(								"select ppp.pessoaProcuradoriaEntidade.pessoa from PessoaProcuradorProcuradoria ppp ");
		sb.append(								"where ppp.pessoaProcurador = #{usuarioLogado}) ");
		sb.append(							"or ppe.pessoaParte in ( ");
		sb.append(								"select ppr.processoParte.pessoa from ProcessoParteRepresentante ppr ");
		sb.append(								"where ppr.representante = #{usuarioLogado})) ");
		sb.append(						"and cast(current_date as date) > cast(ppe.dtPrazoLegal as date) ");
		sb.append(						"and cast(current_date as date) <= cast(ppe.dtPrazoLegal as date)+10 ");
		sb.append(						"and ppe.prazoLegal > 0) ");
		return sb.toString();
	}
	/*
	 * PJE-JT: Fim.
	 */
}