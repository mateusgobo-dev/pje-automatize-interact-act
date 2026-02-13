package br.com.infox.bpm.taskPage.FGPJE;

import java.sql.SQLException;
import java.util.Date;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.RemessaProcesso;
import br.com.infox.cliente.RemessaProcessoValidacao;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RemessaProcessoHistorico;
import br.jus.pje.nucleo.entidades.RemessaProcessoHistoricoLog;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.RemessaStatusEnum;

@Name("remessaTaskPageAction")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class RemessaTaskPageAction extends TaskAction {

	// TODO depois que essa classe for pra produção apagar o frame
	// enviarProcessoFrame

	private static final String TASK_NAME_PROCESSO_REMETIDO_AO_2GRAU = ParametroUtil.instance().getNomeTarefaRemessa2Grau();

	private static final long serialVersionUID = 1L;
	private RemessaProcessoHistoricoLog remessaProcessoHistoricoLog;
	private RemessaProcessoHistorico remessaProcessoHistorico;
	private RemessaProcesso remessaProcesso;
	private Boolean remessaRecente = Boolean.FALSE; // Indica se alguma remessa
													// foi realizada
	private boolean inicializado = false;

	/**
	 * Metodo de inializacao da pagina de tarefa
	 */
	public void initPage() {
		if (!inicializado) {
			buscaRemessaEmAbertoOuRemetivel();
		}
		inicializado = true;
	}

	/**
	 * Metodo que busca Remessas que estejam em Andamento (Aberto) ou que foram
	 * finalizadas mas o processo não foi enviado ainda para a tarefa 'Processo
	 * Remetido ao TRF'.
	 */
	private void buscaRemessaEmAbertoOuRemetivel() {
		String hql = "select o from RemessaProcessoHistorico o where "
				+ "o.processoTrf.idProcessoTrf = :idProcesso and (o.remessaStatusEnum = :statusAberto or"
				+ " (o.remetido = false and o.remessaStatusEnum = :statusFinalizado))";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("idProcesso", ProcessoHome.instance().getInstance().getIdProcesso());
		query.setParameter("statusAberto", RemessaStatusEnum.A);
		query.setParameter("statusFinalizado", RemessaStatusEnum.F);
		remessaProcessoHistorico = EntityUtil.getSingleResult(query);
	}

	/**
	 * Metodo que busca o último historico da remessa do processo atual.
	 */
	private void buscaHistoricoRemessa() {
		String hql = "select o from RemessaProcessoHistoricoLog o where "
				+ "o.remessaProcessoHistorico.processoTrf.idProcessoTrf = :idProcesso "
				+ "order by o.idRemessaProcessoHistoricoLog desc";
		Query query = EntityUtil.createQuery(hql);
		query.setMaxResults(1);
		query.setParameter("idProcesso", ProcessoHome.instance().getInstance().getIdProcesso());
		remessaProcessoHistoricoLog = EntityUtil.getSingleResult(query);
		if (remessaProcessoHistoricoLog != null) {
			remessaProcessoHistorico = remessaProcessoHistoricoLog.getRemessaProcessoHistorico();
		}
	}

	public Boolean verificarConexaoRemessa() {
		try {
			RemessaProcessoValidacao checker = new RemessaProcessoValidacao();
			return checker.verificaConexao();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String verificarEnvioProcesso() {
		try {
			RemessaProcessoValidacao checker = new RemessaProcessoValidacao();
			Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
			if (ProcessoTrfHome.instance().isRemetidoSegundoGrau()) {
				return "Processo já remetido ao TRF.";
			}
			if (!checker.verificaConexao()) {
				return "A conexão encontra-se inativa.";
			} else {
				Usuario usuarioLogado = Authenticator.getUsuarioLogado();
				checker.validar(idProcesso, usuarioLogado.getIdUsuario());
				String out = checker.getChecklist();
				if (out == null || out.equals("")) {
					return "Processo apto para remessa.";
				} else {
					return "Foram encontrados os seguintes problemas: <br/><p>".concat(out).concat("</p>");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return "Erro ao tentar verificar a situação da remessa: " + e.getMessage();
		}
	}

	public void enviarProcesso() {
		verificarEnvioProcesso();

		if (remessaProcesso == null) {
			try {
				remessaProcesso = new RemessaProcesso();
			} catch (Exception e) {
				System.out.println("Erro ao instanciar remessaProcesso");
				e.printStackTrace();
			}
		}

		Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
		if (!remessaProcesso.verificaConexao()) {
			FacesMessages.instance().add(Severity.ERROR, "A conexão se encontra inativa.");
		} else {
			buscaHistoricoRemessa();
			if (remessaProcessoHistorico != null) {
				remessaProcessoHistorico.setRemessaStatusEnum(RemessaStatusEnum.A);
				getEntityManager().merge(remessaProcessoHistorico);
				getEntityManager().flush();
			} else {
				remessaProcessoHistorico = new RemessaProcessoHistorico();
				remessaProcessoHistorico.setProcessoTrf(getProcessoTrf());
				remessaProcessoHistorico.setDataCadastro(new Date());
				remessaProcessoHistorico.setDestino(0);
				remessaProcessoHistorico.setRemetido(false);
				remessaProcessoHistorico.setRemessaStatusEnum(RemessaStatusEnum.A);
				getEntityManager().persist(remessaProcessoHistorico);
				getEntityManager().flush();
			}
			try {
				 remessaProcesso.setIdProcesso(idProcesso); 
				 remessaProcesso.start(); 
				FacesMessages.instance().add(Severity.INFO, "Remessa de Processo em andamento");
			} catch (Exception e) {
				e.printStackTrace();
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar enviar o processo");
			}
			remessaRecente = true;
		}
	}

	private ProcessoTrf getProcessoTrf() {
		return ProcessoTrfHome.instance().getInstance();
	}

	public void refresh() {
		if (remessaProcessoHistorico != null) {
			getEntityManager().refresh(remessaProcessoHistorico);
		}
		if (remessaProcessoHistoricoLog != null) {
			getEntityManager().refresh(remessaProcessoHistoricoLog);
		}
	}

	public RemessaProcessoHistoricoLog buscaHistoricoRemessaLog(RemessaProcessoHistorico remessaProcessoHistorico) {
		String hql = "select o from RemessaProcessoHistoricoLog o where "
				+ "o.remessaProcessoHistorico.idRemessaProcessoHistorico = :idHistorico order by o.idRemessaProcessoHistoricoLog desc limit 1";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("idHistorico", remessaProcessoHistorico.getIdRemessaProcessoHistorico());
		return EntityUtil.getSingleResult(query);
	}

	public long getProgress() {
		buscaHistoricoRemessa();
		if (remessaProcessoHistoricoLog != null) {
			EntityUtil.getEntityManager().refresh(remessaProcessoHistoricoLog);
			RemessaProcessoHistoricoLog log = remessaProcessoHistoricoLog;
			if (log != null && log.getTotalOperacoes() != null && log.getTotalOperacoes() > 0
					&& log.getQuantidadeOperacoes() != null) {
				return log.getQuantidadeOperacoes() * 100 / log.getTotalOperacoes();
			}
		}
		// if (remessaProcessoHistorico != null &&
		// !remessaProcessoHistorico.getRemessaProcessoHistoricoLogList().isEmpty())
		// {
		// Integer ultimaRemessa =
		// remessaProcessoHistorico.getRemessaProcessoHistoricoLogList().size()-1;
		// Integer total =
		// remessaProcessoHistorico.getRemessaProcessoHistoricoLogList().get(ultimaRemessa).getTotalOperacoes();
		// Integer atual =
		// remessaProcessoHistorico.getRemessaProcessoHistoricoLogList().get(ultimaRemessa).getQuantidadeOperacoes();
		// if(total != null && total > 0 && atual != null){
		// return atual*100/total;
		// }else{
		// return 0;
		// }
		// }
		return 0;
	}

	public boolean isPoolEnabled() {
		return isEmAndamento() || isRemetivel() || isComErro();
	}

	public boolean isEmAndamento() {
		return remessaProcessoHistorico != null
				&& remessaProcessoHistorico.getRemessaStatusEnum() == RemessaStatusEnum.A;
	}

	public boolean isComErro() {
		buscaHistoricoRemessa();
		return remessaProcessoHistorico != null
				&& remessaProcessoHistorico.getRemessaStatusEnum() == RemessaStatusEnum.E;
	}

	/**
	 * Remessa foi finalizada, mas o processo ainda não foi para tarefa
	 * TASK_NAME_PROCESSO_REMETIDO_AO_TRF
	 * 
	 * @return
	 */
	public boolean isRemetivel() {
		return remessaProcessoHistorico != null
				&& remessaProcessoHistorico.getRemessaStatusEnum() == RemessaStatusEnum.F
				&& !remessaProcessoHistorico.isRemetido();
	}

	public boolean isRemetido() {
		return remessaProcessoHistorico != null && remessaProcessoHistorico.isRemetido();
	}

	public void irProximaTarefa() {
		if (!isRemetivel()) {
			return;
		}
		end(TASK_NAME_PROCESSO_REMETIDO_AO_2GRAU);
		remessaProcessoHistorico.setRemetido(true);
		getEntityManager().merge(remessaProcessoHistorico);
		getEntityManager().flush();
	}

	public String getTarefaDestino() {
		return TASK_NAME_PROCESSO_REMETIDO_AO_2GRAU;
	}

	public Boolean getRemessaRecente() {
		return remessaRecente;
	}

	public void setRemessaRecente(Boolean remessaRecente) {
		this.remessaRecente = remessaRecente;
	}

}
