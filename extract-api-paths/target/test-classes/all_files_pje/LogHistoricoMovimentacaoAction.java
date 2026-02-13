package br.jus.cnj.pje.view;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.LogHistoricoMovimentacaoManager;
import br.jus.pje.nucleo.entidades.LogHistoricoMovimentacao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

/**
 * PJEII-18820
 * Componente de controle da tela acessada por usuários externos, tais como advogados, procuradores etc.
 * Esta classe é a controladora da página Painel/Painel_Usuario/LogHistoricoMovimentacaoAction.xhtml
 * 
 * @author Carlos Lisboa
 *
 */
@Name(LogHistoricoMovimentacaoAction.NAME)
@Scope(ScopeType.EVENT)
public class LogHistoricoMovimentacaoAction extends BaseAction<LogHistoricoMovimentacao> {
	
	private static final long serialVersionUID = -6592182243493983776L;
	public static final String NAME = "logHistoricoMovimentacaoAction";
	
	@In
	private LogHistoricoMovimentacaoManager logHistoricoMovimentacaoManager;
	
	private EntityDataModel<LogHistoricoMovimentacao> logHistoricoMovimentacao;

	private boolean open = false;
	
	private boolean semRegistro = false;
	
	@RequestParameter(value="logHistoricoMovimentacaoGridCount")
	private Integer logHistoricoMovimentacaoGridCount;
	
	@RequestParameter(value="idProcSel")
	private Integer idProcSel;
		
	private Integer idProcessoSelecionado;

	@RequestParameter
	private Integer idProcessoParteExpediente;

	@Create
	public void init(){
		if(idProcessoParteExpediente == null) {
			if(idProcSel != null){
				idProcessoSelecionado = idProcSel;
				pesquisar();
			}			
		}else {
			pesquisar();			
		}
	}
	
	public void pesquisar(){
		try {
			logHistoricoMovimentacao = new EntityDataModel<LogHistoricoMovimentacao>(LogHistoricoMovimentacao.class, super.facesContext, getRetriever());
			if(idProcessoParteExpediente != null){
				getModel().setCriterias(Criteria.equals("processoParteExpediente.idProcessoParteExpediente", idProcessoParteExpediente),
						Criteria.equals("caixa.localizacao", Authenticator.getLocalizacaoAtual()));
			} else {
				getModel().setCriterias(Criteria.equals("processoTrf.idProcessoTrf", idProcessoSelecionado), 
						Criteria.isNull("processoParteExpediente"),
						Criteria.equals("caixa.localizacao", Authenticator.getLocalizacaoAtual()));
			}
			getModel().addOrder("dataLog", Order.DESC);
			
			if(getModel().getRowCount() == 0){
				setSemRegistro(true);
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected BaseManager<LogHistoricoMovimentacao> getManager() {
		return logHistoricoMovimentacaoManager;
	}

	@Override
	public EntityDataModel<LogHistoricoMovimentacao> getModel() {
		return logHistoricoMovimentacao;
	}
	
	public boolean isOpen() {
		return open;
	}

	public boolean isSemRegistro() {
		return semRegistro;
	}

	public void setSemRegistro(boolean semRegistro) {
		this.semRegistro = semRegistro;
	}
	
	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}
	
	public void setIdProcessoSelecionado(Integer idProcessoSelecionado) {
		this.idProcessoSelecionado = idProcessoSelecionado;
	}
	
	public Integer getIdProcessoParteExpediente() {
		return idProcessoParteExpediente;
	}
	
	public void setIdProcessoParteExpediente(Integer idProcessoParteExpediente) {
		this.idProcessoParteExpediente = idProcessoParteExpediente;
	}
	
}
