/**
 * @author thiago.vieira
 */

package br.com.infox.cliente.home;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.exception.AlertaInativacaoInvalidaException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.AlertaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoAlertaManager;
import br.jus.pje.nucleo.entidades.Alerta;
import br.jus.pje.nucleo.entidades.ProcessoAlerta;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;

@Name("alertaHome")
public class AlertaHome extends AbstractHome<Alerta> {

	private static final long serialVersionUID = 1L;
	@In
	private AlertaManager alertaManager;
	
	public void setAlertaIdAlerta(Integer id) {
		setId(id);
	}

	public Integer getAlertaIdAlerta() {
		return (Integer) getId();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setDataAlerta(new Date());
		getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		getInstance().setOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
		getInstance().setLocalizacao(Authenticator.getLocalizacaoAtual());
		try {
			
			//[PJEII-18551] - Só é permitido inativar um alerta se não houver processos cadastrados para ele no estado ativo.
			alertaManager.configurarStatusDoAlerta(getInstance(), getInstance().getAtivo());
			
		} catch (AlertaInativacaoInvalidaException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
			return false;
		}
		return super.beforePersistOrUpdate();
	}

	public static AlertaHome instance() {
		return ComponentUtil.getComponent("alertaHome");
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("alertaGrid");
		return super.afterPersistOrUpdate(ret);
	}

	public CriticidadeAlertaEnum[] getCriticidadeAlertaValues() {
		return CriticidadeAlertaEnum.values();
	}
	
	/** Cria um alerta para um processo.
	 * @author Gabriel Azevedo
	 * @since 1.4.4	 
	 * @param processo ProcessoTrf
	 * @param textoAlerta Texto exibido no alerta
	 * @param criticidade Criticidade do alerta
	 */
	public void inserirAlerta(ProcessoTrf processo, String textoAlerta, CriticidadeAlertaEnum criticidade){
		
		ProcessoAlertaManager procAlertaManager = ComponentUtil.getComponent(ProcessoAlertaManager.class);
		
		procAlertaManager.incluirAlertaAtivo(processo, textoAlerta, criticidade);
		
	}
	
	public void inserirAlertaNoProcesso(){
		
		ProcessoTrfHome processoTrfHome = ComponentUtil.getComponent("processoTrfHome");
		// vinculando o alerta ao processo
		Alerta alerta = instance().getInstance();
		alerta.setAtivo(Boolean.TRUE);
		alerta.setDataAlerta(new Date());
		ProcessoAlerta processoAlerta = new ProcessoAlerta();
		processoAlerta.setAlerta(alerta);
		processoAlerta.setProcessoTrf(processoTrfHome.getInstance());
		processoAlerta.setAtivo(true);
		
		alerta.getProcessoAlertaList().add(processoAlerta);

		this.persist();
	}
	
	@Override
	public String inactive(Alerta alerta) {
		try{
			alertaManager.configurarStatusDoAlerta(alerta, false);	
			return super.inactive(alerta);
		}catch(AlertaInativacaoInvalidaException e){
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
			return null;
		}
	}

}