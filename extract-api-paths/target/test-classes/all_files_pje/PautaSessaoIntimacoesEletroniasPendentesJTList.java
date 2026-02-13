package br.com.infox.pje.list;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.jt.entidades.PautaSessao;

@Name(PautaSessaoIntimacoesEletroniasPendentesJTList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PautaSessaoIntimacoesEletroniasPendentesJTList extends AbstractPautaSessaoJTList<PautaSessao> {
	
	public static final String NAME = "pautaSessaoIntimacoesEletroniasPendentesJTList";

	private static final long serialVersionUID = 1L;
	
	@Override
	public List<PautaSessao> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}
	
	/* 
	 * PJE-JT: Ricardo Scholz : PJE-1398 - 2012-07-11 Alteracoes feitas pela JT.
	 * Correção do HQL, de forma a incluir não apenas as tuplas em que o usuário 
	 * logado é parte, mas também as tuplas nas quais ele é representante 
	 * ou procurador de uma parte. Além disso, quando tratando-se da Justiça
	 * do Trabalho, considerar também o tipo do ProcessoDocumento.
	 */
	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PautaSessao o, SessaoJT s, ProcessoParteExpediente ppe, ProcessoExpediente pe ");
		sb.append("where o.sessao.idSessao = s.idSessao and ppe.processoJudicial = o.processoTrf and ppe.processoExpediente.idProcessoExpediente = pe.idProcessoExpediente ");
		sb.append("and o.sessao.dataFechamentoPauta is not null  ");
		if(super.getRelatorio() != null && super.getRelatorio()){
			sb.append("and o.processoTrf.idProcessoTrf in (select c.processo.idProcesso from ProcessoDocumento c ");
			sb.append(									  "where c.processo.idProcesso = o.processoTrf.idProcessoTrf ");
			sb.append(									  "and c.tipoProcessoDocumento.tipoProcessoDocumento like 'Relatório') ");
		}else if(super.getRelatorio() != null){
			sb.append("and o.processoTrf.idProcessoTrf not in (select c.processo.idProcesso from ProcessoDocumento c ");
			sb.append(										  "where c.processo.idProcesso = o.processoTrf.idProcessoTrf ");
			sb.append(										  "and c.tipoProcessoDocumento.tipoProcessoDocumento like 'Relatório') ");
		}
		sb.append("and ppe.dtCienciaParte is null ");
		sb.append("and ( ");
		sb.append(	"ppe.pessoaParte = #{usuarioLogado} ");
		sb.append(	"or ppe.pessoaParte in ( ");
		sb.append(		"select ppp.pessoaProcuradoriaEntidade.pessoa from PessoaProcuradorProcuradoria ppp ");
		sb.append(		"where ppp.pessoaProcurador = #{usuarioLogado}) ");
		sb.append(	"or ppe.pessoaParte in ( ");
		sb.append(		"select ppr.processoParte.pessoa from ProcessoParteRepresentante ppr ");
		sb.append(		"where ppr.representante = #{usuarioLogado})) ");
		sb.append("and ppe.processoExpediente.meioExpedicaoExpediente = 'E' ");
		sb.append("and ppe.processoExpediente.dtCriacao > ppe.processoJudicial.dataAutuacao ");
		if(ParametroJtUtil.instance().justicaTrabalho()){
			sb.append("and pe.tipoProcessoDocumento = ");
			sb.append(ParametroUtil.getParametro("idTipoProcessoDocumentoIntimacaoPauta"));
		}
		return sb.toString();
	}
	/*
	 * PJE-JT: Fim.
	 */

}