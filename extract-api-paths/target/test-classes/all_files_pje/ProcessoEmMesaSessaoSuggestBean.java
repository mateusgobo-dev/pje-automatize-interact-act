package br.com.infox.cliente.component.suggest;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.com.infox.ibpm.home.Authenticator;

@Name("processoEmMesaSessaoSuggest")
@BypassInterceptors
public class ProcessoEmMesaSessaoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;
	
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where ");
		sb.append("lower(TO_ASCII(o.processo.numeroProcesso)) like lower(concat('%',TO_ASCII(:");
		sb.append(INPUT_PARAMETER);
		sb.append("), '%')) and ");
		sb.append("o.orgaoJulgador in (select cs.orgaoJulgador from ComposicaoSessao cs ");
		sb.append("where cs.sessao = #{secretarioSessaoJulgamentoAction.sessao}) and ");
		
		sb.append("o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} and ");  
	 	if(Authenticator.getOrgaoJulgadorAtual() != null){  
	 		sb.append("o.orgaoJulgador = #{orgaoJulgadorAtual} and ");  
	 	}  
	 	sb.append("o.orgaoJulgador in (select cs.orgaoJulgador from ComposicaoSessao cs ");  
	 	sb.append("where cs.sessao = #{secretarioSessaoJulgamentoAction.sessao}) and");
	 	
		/**
		 * 04/03/2012 - PJE-JT - Antonio Lucas PJEII-5830
		 * apenas os processos que estiverem na tarefa "Aguardando inclusão em pauta ou sessão" 
		 * deverão ser listados como incluíveis durante a sessão de julgamento. 
		 */
		sb.append(" exists (select sp from SituacaoProcesso sp where sp.processoTrf = o "); 
		sb.append("			and sp.idTarefa = #{parametroUtil.getIdTarefaInclusaoPauta()}) and ");		

		//remanescentes
		sb.append("(exists (select ps from PautaSessao ps where ");
		sb.append("ps.processoTrf = o and ps.sessao.situacaoSessao = 'F' and ");
		sb.append("ps.tipoSituacaoPauta.classificacao in ('D', 'R') and ");
		sb.append("ps.dataSituacaoPauta >= (current_date - o.orgaoJulgadorColegiado.diaRetiradaAdiada) and ");
		sb.append("ps.idPautaSessao in (select max(a.idPautaSessao) from PautaSessao a ");
		sb.append("						where a.processoTrf = o)) or ");
		//em mesa
		sb.append("(o.selecionadoJulgamento = true and ");
		sb.append("(not exists (select p from PautaSessao p where ");
		sb.append("p.processoTrf = o) ");
		sb.append("or exists (select ps from PautaSessao ps where ");
		sb.append("ps.processoTrf = o and ps.sessao.situacaoSessao = 'F' and ");
		sb.append("ps.tipoSituacaoPauta.classificacao = 'J' and ");
		sb.append("ps.idPautaSessao in (select max(a.idPautaSessao) from PautaSessao a ");
		sb.append("where a.processoTrf = o))))) ");
		sb.append("order by o.processo.numeroProcesso");
		return sb.toString();
	}

}
