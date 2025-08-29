package br.com.jt.pje.list;

import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AbaAptosPautaJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AbaAptosPautaJulgamentoList extends FiltrosPautaJulgamentoList<ProcessoTrf> {
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "abaAptosPautaJulgamentoList";
	private Integer tamanho;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder("select o from ProcessoTrf o ");
		sb.append("where o.selecionadoPauta = true ");
		sb.append("and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("and o.orgaoJulgador = #{orgaoJulgadorAtual} ");
		}
		sb.append("and o.orgaoJulgador in (select cs.orgaoJulgador from ComposicaoSessao cs where ");
		sb.append("cs.sessao = #{pautaJulgamentoAction.sessao}) ");
		
		sb.append("and exists (select sp from SituacaoProcesso sp where sp.processoTrf = o "); 
		sb.append("	           and sp.idTarefa = #{parametroUtil.getIdTarefaInclusaoPauta()}) ");
		
		sb.append("and (");
		sb.append(		"not exists (select p from PautaSessao p ");
        sb.append("	                where p.processoTrf = o) or");
        sb.append("		 exists (select ps from PautaSessao ps ");
        sb.append("	               where ps.processoTrf = o ");
        sb.append("	               and ps.sessao.situacaoSessao = 'F' ");
        sb.append("	               and ps.tipoSituacaoPauta.classificacao = 'J' ");
        sb.append("	               and ps.idPautaSessao in (select max(a.idPautaSessao) from PautaSessao a ");
        sb.append("	                                   where a.processoTrf = o))  ");
        
        sb.append("		or exists (select ps from PautaSessao ps ");
        sb.append("	                where ps.processoTrf = o ");
        sb.append("			        and ps.sessao.situacaoSessao in ('E','F')");
        sb.append("	                and ps.tipoSituacaoPauta.classificacao in ('D', 'R') ");
        sb.append("	                and ps.dataSituacaoPauta < (current_date - o.orgaoJulgadorColegiado.diaRetiradaAdiada) ");
        sb.append("	                and ps.idPautaSessao in (select max(a.idPautaSessao) from PautaSessao a ");
        sb.append("	          					             where a.processoTrf = o) ");
        sb.append("                 ) ");
        sb.append("    )");
		       
		        
		return sb.toString();
	}

	public void setTamanho(Integer tamanho){
		this.tamanho = tamanho;
	}

	public Integer getTamanho(){
		return tamanho;
	}
	
	@Override
	public List<ProcessoTrf> list(int maxResult){
		List<ProcessoTrf> list = super.list(maxResult);
		tamanho = list.size();
		return list;
	}
}
