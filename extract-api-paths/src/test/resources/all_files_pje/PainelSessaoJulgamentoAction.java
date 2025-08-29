package br.com.jt.pje.action;

import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.faces.event.ValueChangeEvent;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.agenda.AgendaSessao;
import br.com.infox.component.agenda.AgendaSessaoJT;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.view.GenericAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;

@Name(PainelSessaoJulgamentoAction.NAME)
@Scope(ScopeType.PAGE)
public class PainelSessaoJulgamentoAction extends GenericAction{
	
	private static final long serialVersionUID = 8894039768939473378L;

	public static final String NAME = "painelSessaoJulgamentoAction";
	
	private SessaoJT sessao;
	
	@In
	private PautaSessaoManager pautaSessaoManager;
	@In
	private SessaoManager sessaoManager;

	public Map<String, String> getLegendaSituacao() {
		SortedMap<String, String> map = new TreeMap<String, String>();
		SituacaoSessaoEnum[] values = SituacaoSessaoEnum.values();
		for (int i = 0; i < values.length; i++) {
			map.put(values[i].toString(), values[i].getLabel());
		}
		return map;
	}
	
	public void refreshAgenda(){
		Contexts.removeFromAllContexts("agendaSessao");
		EntityUtil.getEntityManager().clear();
	}
	
	public Boolean existemVariasSessoes(){
		return sessaoManager.existemVariasSessoes(Authenticator.getOrgaoJulgadorAtual(), Authenticator.getOrgaoJulgadorColegiadoAtual());
	}
	
	public Integer quantidadeProcessosEmPauta(Object sessao) {
	    return pautaSessaoManager.quantidadeProcessosEmPauta(sessao);
	}
	
	public int getIdSessaoDoDia() {
		return sessaoManager.getIdSessaoDoDia(Authenticator.getOrgaoJulgadorColegiadoAtual());
	}

	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}

	public Date currentDate(){
		if("JT".equalsIgnoreCase(ParametroUtil.instance().getTipoJustica())){
			return ((AgendaSessaoJT)ComponentUtil.getComponent("agendaSessaoJT")).getCurrentDate();
		}
		return ((AgendaSessao)ComponentUtil.getComponent("agendaSessao")).getCurrentDate();
	}
	
	public void selectDay(ValueChangeEvent event){
		if("JT".equalsIgnoreCase(ParametroUtil.instance().getTipoJustica())){
			((AgendaSessaoJT)ComponentUtil.getComponent("agendaSessaoJT")).selectDay(event);
		}
		((AgendaSessao)ComponentUtil.getComponent("agendaSessao")).selectDay(event);
	}
	
	public Object getDataModel(){
		if("JT".equalsIgnoreCase(ParametroUtil.instance().getTipoJustica())){
			return ((AgendaSessaoJT)ComponentUtil.getComponent("agendaSessaoJT"));
		}
		return ((AgendaSessao)ComponentUtil.getComponent("agendaSessao"));
	}
}