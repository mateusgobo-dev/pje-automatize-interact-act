package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jbpm.graph.def.Transition;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.bean.PreCadastroPessoaBean;
import br.com.infox.cliente.home.ProcessoAudienciaHome;
import br.com.infox.cliente.home.ProcessoAudienciaPessoaHome;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(value = OperarAudienciaTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class OperarAudienciaTaskPageAction extends TaskAction implements Serializable {

	private static final String NOME_ABA_MARCAR_AUDIENCIA = "marcaAudiencia";

	private static final long serialVersionUID = 1L;

	public static final String NAME = "operarAudienciaTaskPageAction";

	private static final String NOME_AGRUPAMENTO_AUDIENCIA = "Audiência";

	private static final LogProvider log = Logging.getLogProvider(OperarAudienciaTaskPageAction.class);

	private boolean inicialized = false;

	private boolean esconderAbas = false;

	private boolean mostrarModalTestemunhaAtivo = false;

	private boolean mostrarModalTestemunhaPassivo = false;

	public void persistAudiencia() {
		String ret = ProcessoAudienciaHome.instance().persist();
		if (ret != null) {
			try {
				RegistraEventoAction.instance().registraPorNome(NOME_AGRUPAMENTO_AUDIENCIA);
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar audiência: " + e.getMessage());
				log.error("Erro ao persistir audiência: " + e.getMessage(), e);
			}
		}
	}

	public void inserirAtualizarDoc() {
		if (ProcessoExpedienteHome.instance().inserirAtualizarDoc()) {
			try {
				RegistraEventoAction.instance().registraPorNome(
						DarCienciaPartesTaskPageAction.NOME_AGRUPAMENTO_EXPEDICAO_DE_DOCUMENTOS);
			} catch (Exception e) {
				log.error("Erro ao registrar eventos: " + e.getMessage(), e);
			}
			esconderAbas = false;
		}
	}

	public void initPage() {
		if (!inicialized) {
			inicialized = true;
			ProcessoTrfHome.instance().setarInstancia();

			if (possuiAudiencia(ProcessoTrfHome.instance().getInstance())) {
				ProcessoAudienciaHome.instance().fluxoOperacoesAudiencia();
			} else {
				ProcessoAudienciaHome.instance().fluxoDesignarAudiencia();
			}
		} else if (!ProcessoTrfHome.instance().isManaged()) {
			ProcessoTrfHome.instance().setarInstancia();
		}
		limparTela();
	}

	public boolean possuiAudiencia(ProcessoTrf processoTrf) {
		String hql = "select o from ProcessoAudiencia o where o.processoTrf = :processoTrf";
		Query query = EntityUtil.createQuery(hql).setParameter("processoTrf", processoTrf);
		return EntityUtil.getSingleResult(query) != null;
	}

	public boolean isEsconderAbas() {
		return esconderAbas;
	}

	public boolean isMostrarModalTestemunhaAtivo() {
		return mostrarModalTestemunhaAtivo;
	}

	public void setMostrarModalTestemunhaAtivo(boolean mostrarModalTestemunhaAtivo) {
		this.mostrarModalTestemunhaAtivo = mostrarModalTestemunhaAtivo;
		mostrarModalTestemunhaPassivo = !mostrarModalTestemunhaAtivo;
		PreCadastroPessoaBean.instance().newInstance();
		ProcessoAudienciaPessoaHome.instance().newInstance();
		PreCadastroPessoaBean.instance().initCadastroPessoaFisica();
	}

	public boolean isMostrarModalTestemunhaPassivo() {
		return mostrarModalTestemunhaPassivo;
	}

	public void setMostrarModalTestemunhaPassivo(boolean mostrarModalTestemunhaPassivo) {
		this.mostrarModalTestemunhaPassivo = mostrarModalTestemunhaPassivo;
		mostrarModalTestemunhaAtivo = !mostrarModalTestemunhaPassivo;
		PreCadastroPessoaBean.instance().newInstance();
		ProcessoAudienciaPessoaHome.instance().newInstance();
		PreCadastroPessoaBean.instance().initCadastroPessoaFisica();
	}

	@Override
	public List<Transition> getTransitions() {
		List<Transition> transitions = super.getTransitions();
		if (ProcessoExpedienteHome.instance().getVisualizarAbas()) {
			List<Transition> transitionsTemp = new ArrayList<Transition>();
			for (Transition transition : transitions) {
				if (!TaskNamesPrimeiroGrau.DAR_CIENCIA_AS_PARTES.equals(transition.getName())) {
					transitionsTemp.add(transition);
				}
			}
			transitions = transitionsTemp;
		}
		return transitions;
	}

	/**
	 * Método que retorna uma lista de conciliadores disponíveis para
	 * determinado processo. A lista contém todos os magistrados ativos, e os
	 * servidores com papel de conciliador da mesma localização do processo.
	 * 
	 * @return Lista de conciliadores disponíveis para determinado processo.
	 */
	@SuppressWarnings("unchecked")
	public List<Pessoa> getPessoaConciliadorItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct o from  Pessoa o inner join o.usuarioLocalizacaoList ul ");
		sb.append("where ul.papel.identificador  = 'magistrado' ");
		sb.append("or (ul.papel.identificador = 'conciliador' ");
		sb.append("and ul.localizacaoFisica = :localizacaoAtual) ");
		sb.append("and o.ativo = true ");
		sb.append("order by o.nome");
		Query q = getEntityManager().createQuery(sb.toString());
		ProcessoTrf processoTrf = EntityUtil.find(ProcessoTrf.class, ProcessoHome.instance().getInstance()
				.getIdProcesso());
		q.setParameter("localizacaoAtual", processoTrf.getOrgaoJulgador().getLocalizacao());
		return q.getResultList();

	}

	public void limparTela() {
		ProcessoAudienciaHome.instance().setRendDocumento(false);
		AnexarDocumentos anexarDocumentos = (AnexarDocumentos) Component.getInstance("anexarDocumentos");
		anexarDocumentos.limparTela();

	}
}
