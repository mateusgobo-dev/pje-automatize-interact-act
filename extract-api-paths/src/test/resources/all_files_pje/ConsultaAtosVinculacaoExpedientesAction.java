package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.list.ConsultaAtosVinculacaoExpedientesList;
import br.com.infox.pje.manager.ProcessoDocumentoTrfLocalManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.LiberacaoConsultaPublicaEnum;
import br.jus.pje.nucleo.enums.SituacaoAtoEnum;

@Name(ConsultaAtosVinculacaoExpedientesAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ConsultaAtosVinculacaoExpedientesAction implements Serializable {

	private static final long serialVersionUID = -3055138484064127915L;

	public static final String NAME = "consultaAtosVinculacaoExpedientesAction";

	private boolean marcarDesmarcarChecks;
	private List<ProcessoDocumentoTrf> atoMagistradoMarcadoList = new ArrayList<ProcessoDocumentoTrf>(0);
	private List<TipoProcessoDocumento> tipoDocumentoAtoMagistradoList;
	private Map<Integer, Boolean> atoMagistradoLiberadoConsultaPublica = new HashMap<Integer, Boolean>();
	private Map<Integer, Boolean> atoComExpedienteVinculado = new HashMap<Integer, Boolean>();

	public boolean temExpedienteVinculado(ProcessoDocumento pd) {
		Boolean temExpediente = atoComExpedienteVinculado.get(pd.getIdProcessoDocumento());
		if (temExpediente == null) {
			temExpediente = ComponentUtil.getComponent(ProcessoDocumentoExpedienteManager.class).temExpedienteVinculado(pd);
			atoComExpedienteVinculado.put(pd.getIdProcessoDocumento(), temExpediente);
		}
		return temExpediente;
	}

	public String getDescricaoTipoDocumentoAtoMagistrado(ProcessoExpediente processoExpediente) {
		ProcessoDocumento processoDocumentoAto = ComponentUtil.getComponent(ProcessoExpedienteManager.class).getProcessoDocumentoAto(processoExpediente);
		return processoDocumentoAto != null ? processoDocumentoAto.getTipoProcessoDocumento()
				.getTipoProcessoDocumento() : "-";
	}

	public List<TipoProcessoDocumento> getTipoDocumentoAtoMagistradoList() {
		if (tipoDocumentoAtoMagistradoList == null) {
			tipoDocumentoAtoMagistradoList = ComponentUtil.getComponent(TipoProcessoDocumentoManager.class).getTipoDocumentoAtoMagistradoList();
		}
		return tipoDocumentoAtoMagistradoList;
	}

	public void liberarAtosConsultaPublica() {
		ProcessoDocumentoTrfLocalManager processoDocumentoTrfLocalManager = ComponentUtil.getComponent(ProcessoDocumentoTrfLocalManager.class);
		for (ProcessoDocumentoTrf processoDocumentoTrf : atoMagistradoMarcadoList) {
			processoDocumentoTrfLocalManager.criarDocumentoLiberadoConsultaPublica(
					processoDocumentoTrf.getProcessoDocumento(), LiberacaoConsultaPublicaEnum.D);
			atoMagistradoLiberadoConsultaPublica.put(processoDocumentoTrf.getIdProcessoDocumento(), true);
		}
		atoMagistradoMarcadoList.clear();
	}

	public String getNomePartePoloAtivo(ProcessoTrf processoTrf) {
		return montarNomePartes(ComponentUtil.getComponent(ProcessoParteManager.class).getPessoasPoloAtivoList(processoTrf));
	}

	public String getNomePartePoloPassivo(ProcessoTrf processoTrf) {
		return montarNomePartes(ComponentUtil.getComponent(ProcessoParteManager.class).getPessoasPoloPassivoList(processoTrf));
	}

	private String montarNomePartes(List<Pessoa> partes) {
		StringBuilder sb = new StringBuilder();
		if (partes.size() > 0) {
			sb.append(partes.get(0));
			if (partes.size() > 1) {
				sb.append(" e outros");
			}
		}
		return sb.toString();
	}

	public SituacaoAtoEnum[] situacaoAtoValues() {
		return SituacaoAtoEnum.values();
	}

	public Boolean isLiberadoConsultaPublica(ProcessoDocumentoTrf processoDocumentoTrf) {
		Boolean liberado = atoMagistradoLiberadoConsultaPublica.get(processoDocumentoTrf.getIdProcessoDocumento());
		if (liberado == null) {
			liberado = ComponentUtil.getComponent(ProcessoDocumentoManager.class).isLiberadoConsultaPublica(processoDocumentoTrf.getProcessoDocumento());
			atoMagistradoLiberadoConsultaPublica.put(processoDocumentoTrf.getIdProcessoDocumento(), liberado);
		}
		return liberado;
	}

	public void setMarcarDesmarcarChecks(boolean marcarDesmarcarChecks) {
		this.marcarDesmarcarChecks = marcarDesmarcarChecks;

		addAtosMagistradoLiberados();
	}

	public boolean isMarcarDesmarcarChecks() {
		return marcarDesmarcarChecks;
	}

	private void addAtosMagistradoLiberados() {
		atoMagistradoMarcadoList.clear();
		if (marcarDesmarcarChecks) {
			for (ProcessoDocumentoTrf processoDocumentoTrf : ConsultaAtosVinculacaoExpedientesList.instance().list()) {
				if (!isLiberadoConsultaPublica(processoDocumentoTrf)) {
					atoMagistradoMarcadoList.add(processoDocumentoTrf);
				}
			}
		}
	}

	public void setAtoMagistradoMarcadoList(List<ProcessoDocumentoTrf> atoMagistradoMarcadoList) {
		this.atoMagistradoMarcadoList = atoMagistradoMarcadoList;
	}

	public List<ProcessoDocumentoTrf> getAtoMagistradoMarcadoList() {
		return atoMagistradoMarcadoList;
	}

	public void addAtoMagistradoMarcado(ProcessoDocumentoTrf processoDocumentoTrf) {
		atoMagistradoMarcadoList.add(processoDocumentoTrf);
	}

	public void removeAtoMagistradoMarcado(ProcessoDocumentoTrf processoDocumentoTrf) {
		atoMagistradoMarcadoList.remove(processoDocumentoTrf);
	}
	
	public List<ProcessoParte> getPartesPoloAtivo(ProcessoTrf processoTrf) {
		return ComponentUtil.getComponent(ProcessoParteManager.class).getPartesPoloAtivo(processoTrf);
	}

	public List<ProcessoParte> getPartesPoloPassivo(ProcessoTrf processoTrf) {
		return ComponentUtil.getComponent(ProcessoParteManager.class).getPartesPoloPassivo(processoTrf);
	}

}