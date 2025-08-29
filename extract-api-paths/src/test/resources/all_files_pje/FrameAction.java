package br.jus.cnj.pje.view.fluxo;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("frameAction")
@Scope(ScopeType.EVENT)
public class FrameAction {

	private String url;

	@In
	private TramitacaoProcessualService tramitacaoProcessualService;

	@Create
	public void load() throws Exception {
		ProcessoTrf processoTrf = this.tramitacaoProcessualService.recuperaProcesso();

		this.url = this.tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.PJE_FLUXO_URL_FRAME_FRONTEND) + this.obtemParametros(processoTrf);
	}

	private String obtemParametros(ProcessoTrf processoTrf) {
		StringBuilder parametros = new StringBuilder();

		parametros
			.append(";numeroProcesso=").append(StringUtil.removeNaoNumericos(processoTrf.getNumeroProcesso()))
			.append(";reus=").append(this.obtemReus(processoTrf));

		return parametros.toString();
	}

	private String obtemReus(ProcessoTrf processoTrf) {
		StringBuilder result = new StringBuilder();

		Pessoa pessoa = null;
		for (ProcessoParte processoParte : processoTrf.getListaPartePrincipal(Boolean.TRUE, Boolean.FALSE, ProcessoParteParticipacaoEnum.P)) {
			pessoa = processoParte.getPessoa();
			if (pessoa.getDocumentoCpfCnpj() != null) {
				result.append(this.convertePara(pessoa)).append(",");
			}
		}

		return result.toString().substring(0, result.length() -1);
	}

	private String convertePara(Pessoa pessoa) {
		return pessoa.getNome() + ":" + StringUtil.removeNaoNumericos(pessoa.getDocumentoCpfCnpj());
	}

	public String getUrl() {
		return this.url;
	}

}
