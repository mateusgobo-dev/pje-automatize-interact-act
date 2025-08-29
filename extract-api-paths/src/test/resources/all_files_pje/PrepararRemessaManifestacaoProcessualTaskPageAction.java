package br.com.infox.bpm.taskPage.remessacnj;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

@Name(PrepararRemessaManifestacaoProcessualTaskPageAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PrepararRemessaManifestacaoProcessualTaskPageAction extends
		TaskAction implements Serializable {

	@Logger
	private Log log;

	private static final long serialVersionUID = 1L;

	public static final String NAME = "prepararRemessaManifestacaoProcessualTaskPageAction";

	private ManifestacaoProcessualMetaData manifestacaoProcessualMetaData;

	@In(create = true)
	private transient ProcessoTrfHome processoTrfHome;

	@In
	private transient ParametroService parametroService;

	@In
	private transient ClasseJudicialManager classeJudicialManager;

	@In
	private transient AgrupamentoClasseJudicialManager agrupamentoClasseJudicialManager;

	@In(create = true)
	private transient TipoProcessoDocumentoManager tipoProcessoDocumentoManager;

	@In(create = true)
	private ManifestacaoProcessualMetaDataManager manifestacaoProcessualMetaDataManager;

	@Create
	public void init() throws Exception {

		if (!processoTrfHome.isManaged()) {
			return;
		}

		// tramitacaoProcessualService.apagaVariavel(Variaveis.VARIAVEL_REMESSA_MANIFESTACAO_PROCESSUAL);

		manifestacaoProcessualMetaData = manifestacaoProcessualMetaDataManager.get();

		String codigoAgrupamento = parametroService.valueOf(Parametros.CODIGO_AGRUPAMENTO_ASSUNTO_REMESSA_STF);
		if(codigoAgrupamento == null){
			return;
		}
		// a variável não existe, criar nova manifestacao processual
		if (manifestacaoProcessualMetaData == null) {
			manifestacaoProcessualMetaData = manifestacaoProcessualMetaDataManager.create(processoTrfHome.getInstance().getIdProcessoTrf(), null, codigoAgrupamento);
		} else {
			manifestacaoProcessualMetaDataManager.refresh(manifestacaoProcessualMetaData);
		}
	}

	@Out(required=false)
	public ManifestacaoProcessualMetaData getManifestacaoProcessualMetaData() {
		return manifestacaoProcessualMetaData;
	}

	public void setManifestacaoProcessualMetaData(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData) {
		this.manifestacaoProcessualMetaData = manifestacaoProcessualMetaData;
	}

	public String getJurisdicao() {
		return processoTrfHome.getInstance().getJurisdicao().getJurisdicao();
	}

	public String getOrgaoJulgador() {
		return processoTrfHome.getInstance().getOrgaoJulgador()
				.getOrgaoJulgador();
	}

	public String getValorCausa() {
		return processoTrfHome.getInstance().getVlCausa();
	}

	public String getNumeroProcesso() {
		return processoTrfHome.getInstance().getNumeroProcesso();
	}

	public List<ClasseJudicial> getClassesJudiciais() {
		List<ClasseJudicial> classesAgrupamento = classeJudicialManager
				.getClassesAgrupadas(agrupamentoClasseJudicialManager.findByCodigo(parametroService
						.valueOf(Parametros.CODIGO_AGRUPAMENTO_CLASSES_REMESSA_STF)));

		return classesAgrupamento;
	}

	public void inverterPolos() {
		manifestacaoProcessualMetaDataManager
				.invertPolos(manifestacaoProcessualMetaData);
	}

	public void duplicarPolos() throws Exception {
		manifestacaoProcessualMetaDataManager
				.duplicatePolos(manifestacaoProcessualMetaData);
	}

	public void gravar() {
		try {
			manifestacaoProcessualMetaDataManager
					.persist(manifestacaoProcessualMetaData);
			FacesMessages.instance().add(Severity.INFO, "Operação realizada com sucesso!");
			
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode());
		}
	}

	public void reiniciar() throws Exception {
		manifestacaoProcessualMetaDataManager
				.remove(manifestacaoProcessualMetaData);
		init();
	}

	public List<TipoProcessoDocumento> getTiposDocumentoPossiveis(
			String descricaoTipo) {
		List<TipoProcessoDocumento> returnValue = tipoProcessoDocumentoManager
				.findByAplicacaoClasse(Integer.parseInt(parametroService
						.valueOf(Parametros.ID_APLICACAO_CLASSE_ESPECIAL)),
						descricaoTipo);
		if (returnValue == null || returnValue.isEmpty()) {
			returnValue = tipoProcessoDocumentoManager.findByAplicacaoClasse(
					Integer.parseInt(parametroService
							.valueOf(Parametros.ID_APLICACAO_CLASSE_ESPECIAL)),
					"%");
		}

		return returnValue;
	}

}
