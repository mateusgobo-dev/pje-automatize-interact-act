package br.com.jt.pje.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(EmMesaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class EmMesaList extends FiltrosPautaJulgamentoList<ProcessoTrf> {

	public static final String NAME = "emMesaList";
	
	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_EJBQL = FiltrosPautaJulgamentoList.DEFAULT_EJBQL +
												"where o.orgaoJulgador in (select cs.orgaoJulgador from ComposicaoSessao cs " +
																			"where cs.sessao = #{pautaJulgamentoAction.sessao}) " +
																			"and exists (select sp from SituacaoProcesso sp where sp.processoTrf = o " + 
																						"and sp.idTarefa = #{parametroUtil.getIdTarefaInclusaoPauta()}) " +
																			"and o.selecionadoJulgamento = true " +
																			"and (not exists (select p from PautaSessao p where " +
																						"p.processoTrf = o) " +
																			"or exists (select ps from PautaSessao ps where " +
																						"ps.processoTrf = o and (ps.sessao.situacaoSessao in ('F') ) and " +
																						"ps.tipoSituacaoPauta.classificacao = 'J' and " +
																						"ps.idPautaSessao in (select max(a.idPautaSessao) from PautaSessao a " +
																						"where a.processoTrf = o))) ";

	private Integer tamanho;
	
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("processo", "processo.numeroProcesso");
		map.put("orgaoJulgador", "orgaoJulgador.orgaoJulgador");
		map.put("classeJudicial", "classeJudicial.classeJudicial");
		return map;
	}

	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder(DEFAULT_EJBQL);
		sb.append("and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("and o.orgaoJulgador = #{orgaoJulgadorAtual} ");
		}
		return sb.toString();
	}

	@Override
	public void setMaxResults(Integer maxResults){
		super.setMaxResults(10);
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
