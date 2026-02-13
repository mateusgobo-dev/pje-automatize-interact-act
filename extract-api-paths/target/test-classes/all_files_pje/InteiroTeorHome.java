package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;

@Name(InteiroTeorHome.NAME)
@Scope(ScopeType.PAGE)
public class InteiroTeorHome implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6562751507997227048L;

	public static final String NAME = "inteiroTeorHome";

	@Logger
	private Log log;

	@In
	private SessaoProcessoDocumentoHome sessaoProcessoDocumentoHome;

	@In
	private transient ParametroService parametroService;

	@In
	private transient ModeloDocumentoManager modeloDocumentoManager;

	private ModeloDocumento modeloDocumentoInteiroTeor;

	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private List<SessaoProcessoDocumento> listVotos;

	public static InteiroTeorHome instance() {
		return (InteiroTeorHome) Component.getInstance(InteiroTeorHome.NAME);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	@Create
	public void init() {
		sessaoPautaProcessoTrf = SessaoPautaProcessoTrfHome.instance()
				.getInstance();
		listVotos = sessaoProcessoDocumentoHome
				.getDocumentosVotosAcordao(sessaoPautaProcessoTrf);
	}

	public void setModeloDocumentoInteiroTeor(
			ModeloDocumento modeloDocumentoInteiroTeor) {
		this.modeloDocumentoInteiroTeor = modeloDocumentoInteiroTeor;
	}

	public ModeloDocumento getModeloDocumentoInteiroTeor() {
		if (modeloDocumentoInteiroTeor == null) {
			try {
				String idsModelosInteiroTeor = parametroService
						.valueOf(Parametros.ID_MODELO_DOCUMENTO_INTEIRO_TEOR);
				modeloDocumentoInteiroTeor = modeloDocumentoManager
						.findById(Integer.parseInt(idsModelosInteiroTeor));
			} catch (Exception e) {
				FacesMessages.instance().add(
						Severity.ERROR,
						"Ocorreu um erro ao recuperar os modelos de documento do Inteiro Teor: "
								+ e.getMessage());
				log.error(
						"Ocorreu um erro ao recuperar os modelos de documento do Inteiro Teor: ",
						e);
			}
		}

		return modeloDocumentoInteiroTeor;
	}

	public String getTextoRelatorio() {
		StringBuilder sbDocumento = new StringBuilder("");
		SessaoProcessoDocumento spd = sessaoProcessoDocumentoHome
				.getRelatorioMagistrado(sessaoPautaProcessoTrf);
		sbDocumento.append(spd.getProcessoDocumento().getProcessoDocumentoBin()
				.getModeloDocumento());
		return sbDocumento.toString();
	}

	public String getTextoVotoRelator() {
		StringBuilder sbDocumento = new StringBuilder("");
		for (SessaoProcessoDocumento voto : listVotos) {
			if (voto.getOrgaoJulgador().equals(
					sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador())) {
				sbDocumento.append(voto.getProcessoDocumento()
						.getProcessoDocumentoBin().getModeloDocumento());
			}
		}
		return sbDocumento.toString();
	}

	public String getTextoDemaisVotos() {
		StringBuilder sbDocumento = new StringBuilder("");
		for (SessaoProcessoDocumento voto : listVotos) {
			if (!voto.getOrgaoJulgador().equals(
					sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador())) {
				sbDocumento.append(voto.getProcessoDocumento()
						.getProcessoDocumentoBin().getModeloDocumento());
			}
		}
		return sbDocumento.toString();
	}

	@SuppressWarnings("unchecked")
	public String getTextoEmenta() {
		StringBuilder sbDocumento = new StringBuilder("");
		StringBuilder sb = new StringBuilder();
		sb.append("select o.processoDocumento from SessaoProcessoDocumento o ");
		sb.append("where o.processoDocumento.tipoProcessoDocumento.ativo = true ");
		sb.append("and o.sessao.idSessao = :idSessao ");
		sb.append("and o.processoDocumento.tipoProcessoDocumento = :tipoProcessoDocumento ");
		SessaoPautaProcessoTrf sppt = SessaoPautaProcessoTrfHome.instance()
				.getInstance();
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("idSessao", sppt.getSessao().getIdSessao());
		q.setParameter("tipoProcessoDocumento", ParametroUtil.instance()
				.getTipoProcessoDocumentoEmenta());
		List<ProcessoDocumento> lista = q.getResultList();

		if (lista != null && !lista.isEmpty()) {
			for (ProcessoDocumento ementa : lista) {
				sbDocumento.append(ementa.getProcessoDocumentoBin()
						.getModeloDocumento());
			}
		}
		return sbDocumento.toString();
	}

	public String getTextoAcordao() {
		return sessaoProcessoDocumentoHome.getConteudoEditorAcordao();
	}

	public String getNomesMagistrados() {
		return getNomesMagistrados(false);
	}

	@SuppressWarnings("unchecked")
	public String getNomesMagistrados(boolean somenteRelator) {
		StringBuilder sbDocumento = new StringBuilder("");

		SessaoPautaProcessoTrf sppt = SessaoPautaProcessoTrfHome.instance()
				.getInstance();

		StringBuilder sb = new StringBuilder();
		sb.append("Select o from SessaoComposicaoOrdem o ");
		sb.append("where o.sessao = :sessao ");
		Query query = EntityUtil.createQuery(sb.toString());
		query.setParameter("sessao", sppt.getSessao());
		List<SessaoComposicaoOrdem> scoList = new ArrayList<SessaoComposicaoOrdem>(
				0);
		scoList.addAll(query.getResultList());
		List<String> nomesList = new ArrayList<String>();

		for (SessaoComposicaoOrdem sco : scoList) {
			if (somenteRelator
					&& !(sco.getOrgaoJulgador().equals(sessaoPautaProcessoTrf.getOrgaoJulgadorVencedor()))) {
				continue;
			}
			if (sco.getMagistradoSubstitutoSessao() != null) {
				if(sco.getMagistradoSubstitutoSessao() != null){
					nomesList.add(sco.getMagistradoSubstitutoSessao().getNome());
				}
			} else {
				if(sco.getMagistradoPresenteSessao() != null){
					nomesList.add(sco.getMagistradoPresenteSessao().getNome());
				}
			}
		}

		Collections.sort(nomesList);

		for (String nome : nomesList) {
			sbDocumento.append("<br />");
			sbDocumento.append(nome);
		}

		return sbDocumento.toString();
	}

	public String getNomeRelator() {
		return getNomesMagistrados(true);
	}

	public String getTextoInteiroTeor() {
		if (modeloDocumentoInteiroTeor != null) {
			return ProcessoDocumentoHome
					.processarModelo(modeloDocumentoInteiroTeor
							.getModeloDocumento());
		}
		return null;
	}
}
