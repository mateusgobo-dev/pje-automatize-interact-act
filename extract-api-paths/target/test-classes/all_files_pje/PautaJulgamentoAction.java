package br.com.jt.pje.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.view.GenericAction;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PautaJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PautaJulgamentoAction extends GenericAction implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "pautaJulgamentoAction";
	
	private Integer idSessao;
	private SessaoJT sessao;

	@In
	private SessaoManager sessaoManager;
	@In
	private ComposicaoSessaoManager composicaoSessaoManager; 
	@In
	private AbaAptosPautaAction abaAptosPautaAction; 
	@In
	private AbaRemanescentesAction abaRemanescentesAction;
	@In
	private AbaEmMesaAction abaEmMesaAction;
	@In
	private AbaVotacaoAntecipadaAction abaVotacaoAntecipadaAction;
	@In
	private AbaPautaJulgamentoAction abaPautaJulgamentoAction;
	
	public boolean disabilitarAbaAptos(){
		return !sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.A);
	}
	
	public boolean desabilitarAbasInclusao(){
		return disabilitarAbaAptos() && !sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.S);
	}
	
	public void carregarAbas(){
		setSessao(sessaoManager.find(SessaoJT.class, idSessao));
		
		setTab("pautaJulgamento");
		
		abaAptosPautaAction.newInstance();
		abaAptosPautaAction.setSessao(sessao);
		
		abaRemanescentesAction.newInstance();
		abaRemanescentesAction.setSessao(sessao);
		
		abaEmMesaAction.newInstance();
		abaEmMesaAction.setSessao(sessao);
		
		abaPautaJulgamentoAction.newInstance();
		abaPautaJulgamentoAction.setSessao(sessao);
		
		abaVotacaoAntecipadaAction.newInstance();
		abaVotacaoAntecipadaAction.setSessao(sessao);
	}
	
	public String titlePage(){
		StringBuilder sb = new StringBuilder();
		sb.append(Authenticator.getOrgaoJulgadorColegiadoAtual());
		sb.append(" - ");
		sb.append(sessao.getTipoSessao());
		sb.append(" - ");
		sb.append(DateUtil.getDataFormatada(sessao.getDataSessao(), "dd/MM/yyyy"));
		sb.append(" ");
		sb.append(DateUtil.getDataFormatada(new Date(sessao.getSalaHorario().getHoraInicial().getTime()), "HH:mm"));
		return sb.toString();
	}
	
	public boolean sessaoFechada(){
		return getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.F);
	}
	
	/*
	 * inicio dos items
	*/
	
	public List<OrgaoJulgador> getOrgaoJulgadorItems(){
		return composicaoSessaoManager.getOrgaoJulgadorBySessao(sessao);
	}
	
	/*
	 * inicio dos gets e sets
	*/
	
	public SessaoJT getSessao() {
		return sessao;
	}

	public void setSessao(SessaoJT sessao) {
		this.sessao = sessao;
	}

	public AbaAptosPautaAction getAbaAptosPautaAction() {
		return abaAptosPautaAction;
	}

	public void setAbaAptosPautaAction(AbaAptosPautaAction abaAptosPautaAction) {
		this.abaAptosPautaAction = abaAptosPautaAction;
	}
	
	public void setAbaRemanescentesAction(AbaRemanescentesAction abaRemanescentesAction) {
		this.abaRemanescentesAction = abaRemanescentesAction;
	}

	public AbaRemanescentesAction getAbaRemanescentesAction() {
		return abaRemanescentesAction;
	}

	public void setAbaEmMesaAction(AbaEmMesaAction abaEmMesaAction) {
		this.abaEmMesaAction = abaEmMesaAction;
	}

	public AbaEmMesaAction getAbaEmMesaAction() {
		return abaEmMesaAction;
	}
	
	public void setAbaVotacaoAntecipadaAction(AbaVotacaoAntecipadaAction abaVotacaoAntecipadaAction) {
		this.abaVotacaoAntecipadaAction = abaVotacaoAntecipadaAction;
	}

	public AbaVotacaoAntecipadaAction getAbaVotacaoAntecipadaAction() {
		return abaVotacaoAntecipadaAction;
	}

	public AbaPautaJulgamentoAction getAbaPautaJulgamentoAction() {
		return abaPautaJulgamentoAction;
	}

	public void setAbaPautaJulgamentoAction(
			AbaPautaJulgamentoAction abaPautaJulgamentoAction) {
		this.abaPautaJulgamentoAction = abaPautaJulgamentoAction;
	}

	public Integer getIdSessao(){
		return idSessao;
	}
	
	public void setIdSessao(Integer idSessao){
		this.idSessao = idSessao;
	}

}