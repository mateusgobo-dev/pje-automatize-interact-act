package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jbpm.graph.exe.ProcessInstance;

import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.webservice.bean.bacen.Executado;

@Name("bacenAction")
@Scope(ScopeType.CONVERSATION)
public class BacenAction {

	private List<Executado> executados = new ArrayList<>();

	@In
	private transient TaskInstanceUtil taskInstanceUtil;

	@In
	private transient ProcessoJudicialManager processoJudicialManager;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@Create
	public void load() throws Exception {
		ProcessInstance processInstance = this.taskInstanceUtil.getProcessInstance();

		Integer procId = (Integer) processInstance.getContextInstance().getVariable(Variaveis.VARIAVEL_PROCESSO);
		ProcessoTrf processoTrf = this.processoJudicialManager.findById(procId);

		Pessoa pessoa = null;
		for (ProcessoParte processoParte : processoTrf.getListaPartePrincipal(Boolean.TRUE, Boolean.FALSE, ProcessoParteParticipacaoEnum.P)) {
			pessoa = processoParte.getPessoa();
			if (pessoa.getDocumentoCpfCnpj() != null) {
				this.executados.add(new Executado(StringUtil.removeNaoNumericos(pessoa.getDocumentoCpfCnpj()), pessoa.getNome(), processoTrf.getValorCausa()));
			}
		}

		@SuppressWarnings("unchecked")
		List<Executado> executados = (List<Executado>)this.tramitacaoProcessualService.recuperaVariavel("pje:fluxo:sisbajud:executados");
		if (executados == null) {
			this.atualizaVariavelFluxoExecutados();
		} else {
			List<Executado> aux = new ArrayList<>();

			for (Executado executado : this.executados) {
				if (executados.contains(executado)) {
					aux.add(executados.get(executados.indexOf(executado)));
				} else {
					aux.add(executado);
				}
			}

			this.executados = aux;
			this.atualizaVariavelFluxoExecutados();
		}

	}

	public List<Executado> getExecutados() {
		return this.executados;
	}

	public void atualizaVariavelFluxoExecutados() {
		this.tramitacaoProcessualService.gravaVariavel("pje:fluxo:sisbajud:executados", this.executados);
	}

}
