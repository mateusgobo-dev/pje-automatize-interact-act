package br.jus.cnj.pje.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;

public class AcordaoCompilacao {

	private TaskInstance taskInstance;
	private SessaoPautaProcessoTrf sessaoPautaProcessoTrf;
	private List<SessaoProcessoDocumentoVoto> votos = new ArrayList<SessaoProcessoDocumentoVoto>();
	private SessaoProcessoDocumento relatorio;
	private ProcessoDocumento acordao;
	private SessaoProcessoDocumento notasOrais;
	private SessaoProcessoDocumento ementaRelatorDoAcordao;		
	private boolean assinadoEmenta;
	private boolean assinadoRelatorio;
	private boolean assinadoVotoRelatorProcesso;
	private boolean assinadoVotoRelatorAcordao;
	private boolean assinadoNotasOrais;
	private Map<OrgaoJulgador, PessoaMagistrado> composicaoSessaoJulgamento;

	/**
	 * Se o Voto Vencedor (Relator para acordao) for o relator do processo (Relator Originario)
	 * 	- Assinar Acórdão
	 *  - Assinar Ementa
	 *  - Assinar Voto
	 *  - Assinar Relatorio se nao estive assinado, se ja assinado realizar juntada
	 *
	 * Se o Voto Vencedor (Relator para acórdão) for outro órgão julgador do colegiado
	 *  - Assinar Acórdão
	 *  - Assinar Ementa caso tenha feito
	 *  - Assinar Voto
	 *  - Assinar Relatório se nao estiver assinado, se ja assinado realizar juntada
	 *
	 * @return
	 */
	public List<ProcessoDocumento> getProcessoDocumentosParaAssinatura() {

		List<ProcessoDocumento> documentos = new ArrayList<ProcessoDocumento>();

		// Acordao
		if (Util.isDocumentoPreenchido(getAcordao())) {
			documentos.add(getAcordao());
		}

		//Ementa
		if (getEmentaRelatorDoAcordao() != null && Util.isDocumentoPreenchido(getEmentaRelatorDoAcordao().getProcessoDocumento())) {
			if(this.getSessaoPautaProcessoTrf() != null && this.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor() != null && getEmentaRelatorDoAcordao().getOrgaoJulgador().equals(this.getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor())) {
				documentos.add(getEmentaRelatorDoAcordao().getProcessoDocumento());
			}
		}

		// Voto
        SessaoProcessoDocumentoVoto voto = getVotoVencedor();
        if(voto == null){
            voto = getVotoRelatorDoAcordao();
        }
                
		if (voto != null && Util.isDocumentoPreenchido(voto.getProcessoDocumento())) {
			documentos.add(voto.getProcessoDocumento());
		}

		// Relatorio
		// Se o relatorio nao estiver assinado assinar ele
		if (getRelatorio() != null && !isAssinadoRelatorio() && Util.isDocumentoPreenchido(getRelatorio().getProcessoDocumento())) {
			documentos.add(getRelatorio().getProcessoDocumento());
		}

		// Notas orais
		if (getNotasOrais() != null && Util.isDocumentoPreenchido(getNotasOrais().getProcessoDocumento())) {
			documentos.add(getNotasOrais().getProcessoDocumento());
		}

		List<ProcessoDocumento> outrosVotos = getProcessoDocumentoVotosSemVotoRelatorParaAcordao();
		if (outrosVotos != null && !outrosVotos.isEmpty()) {
			documentos.addAll(outrosVotos);
		}

		return documentos;
	}

	public TaskInstance getTaskInstance() {
		return taskInstance;
	}

	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		return sessaoPautaProcessoTrf;
	}

	public SessaoProcessoDocumentoVoto getVotoRelatorDoProcesso() {
		OrgaoJulgador relator = null;

		if (getSessaoPautaProcessoTrf() == null) {
			relator = getAcordao().getProcessoTrf() != null ? getAcordao().getProcessoTrf().getOrgaoJulgador() : ProcessoJbpmUtil.getProcessoTrf().getOrgaoJulgador();
		} else {
			relator = getSessaoPautaProcessoTrf().getOrgaoJulgadorRelator() != null ? getSessaoPautaProcessoTrf().getOrgaoJulgadorRelator() :
				getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador();
		}

		return recuperarVotoPorOrgaoJulgador(relator);
	}

	/**
	 * Método responsável por retornar o voto vencedor do acórdão
	 * 
	 * @return SessaoProcessoDocumentoVoto com o voto vencedor
	 */
	public SessaoProcessoDocumentoVoto getVotoRelatorDoAcordao() {
		return recuperarVotoPorOrgaoJulgador(getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
	}

	private SessaoProcessoDocumentoVoto recuperarVotoPorOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		
		for (SessaoProcessoDocumentoVoto voto : getVotos()) {
			// Caso o voto seja do relator do acordao
			if (voto.getOrgaoJulgador().equals(orgaoJulgador)) {
				return voto;
			}
		}
		
		return null;		
	}

	/**
	 * Método responsável por retornar o voto relator do processo
	 * 
	 * @return SessaoProcessoDocumentoVoto com o voto relator
	 */
	public SessaoProcessoDocumentoVoto getVotoRelator() {
		return recuperarVotoPorOrgaoJulgador(ProcessoJbpmUtil.getProcessoTrf().getOrgaoJulgador());
	}

	/**
	 * Método responsável por retornar o voto vencedor do processo
	 * @return
	 */
	public SessaoProcessoDocumentoVoto getVotoVencedor() {
		if (getSessaoPautaProcessoTrf() != null) {
			return recuperarVotoPorOrgaoJulgador(getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
		}
		return null;
	}

	public List<SessaoProcessoDocumentoVoto> getVotos() {
		return votos;
	}

	public List<SessaoProcessoDocumentoVoto> getVotosSemVotoRelatorParaAcordao() {

		List<SessaoProcessoDocumentoVoto> votos = new ArrayList<SessaoProcessoDocumentoVoto>(getVotos());

		votos.remove(getVotoVencedor() == null ? getVotoRelator() : getVotoVencedor());

		return votos;
	}

	public List<SessaoProcessoDocumentoVoto> getVotosSemVotoRelatorOriginalEAcordao() {

		List<SessaoProcessoDocumentoVoto> votos = new ArrayList<SessaoProcessoDocumentoVoto>(getVotos());

		votos.remove(getVotoRelator());
		votos.remove(getVotoRelatorDoProcesso());

		return votos;
	}

	public List<SessaoProcessoDocumentoVoto> getVotosQueForamAssinadosSemVotoRelatorParaAcordao() {

		List<SessaoProcessoDocumentoVoto> resultado = new ArrayList<SessaoProcessoDocumentoVoto>();

		for (SessaoProcessoDocumentoVoto voto : getVotosSemVotoRelatorParaAcordao()) {
			if (voto.getProcessoDocumento() != null && voto.getProcessoDocumento().getProcessoDocumentoBin().isAssinado()) {
				resultado.add(voto);
			}
		}

		return resultado;
	}

	public List<SessaoProcessoDocumentoVoto> getVotosQueForamAssinadosSemVotoRelatorOriginalEAcordao() {

		List<SessaoProcessoDocumentoVoto> resultado = new ArrayList<SessaoProcessoDocumentoVoto>();

		for (SessaoProcessoDocumentoVoto voto : getVotosSemVotoRelatorOriginalEAcordao()) {
			if (voto.getProcessoDocumento() != null && voto.getProcessoDocumento().getProcessoDocumentoBin().isAssinado()) {
				resultado.add(voto);
			}
		}

		return resultado;
	}

	public SessaoProcessoDocumento getRelatorio() {
		return relatorio;
	}

	public ProcessoDocumento getAcordao() {
		return acordao;
	}

	public SessaoProcessoDocumento getNotasOrais() {
		return notasOrais;
	}

	public SessaoProcessoDocumento getEmentaRelatorDoAcordao() {
		return ementaRelatorDoAcordao;
	}

	public void setTaskInstance(TaskInstance taskInstance) {
		this.taskInstance = taskInstance;
	}

	public void setSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		this.sessaoPautaProcessoTrf = sessaoPautaProcessoTrf;
	}

	public void setRelatorio(SessaoProcessoDocumento relatorio) {
		this.relatorio = relatorio;
	}

	public void setAcordao(ProcessoDocumento acordao) {
		this.acordao = acordao;
	}

	public void setVotos(List<SessaoProcessoDocumentoVoto> votos) {
		this.votos = votos;
	}

	public void setEmentaRelatorDoAcordao(SessaoProcessoDocumento ementaRelatorDoAcordao) {
		this.ementaRelatorDoAcordao = ementaRelatorDoAcordao;
	}

	/**
	 * Se o Voto Vencedor (Relator para acordao) for o relator do processo (Relator Originario)
	 * 	- Assinar Acórdão
	 *  - Assinar Ementa
	 *  - Sera obrigatorio a criacao e assinatura do seu voto
	 *  - Assinar Relatorio se nao estive assinado, se ja assinado realizar juntada
	 *
	 * Se o Voto Vencedor (Relator para acórdão) for outro órgão julgador do colegiado
	 *  - Assinar Acórdão
	 *  - Assinar Ementa caso tenha feito, se nao fez nao assina
	 *  - Sera obrigatorio a criacao e assinatura do seu voto
	 *  - Assinar Relatório se nao estiver assinado, se ja assinado realizar juntada
	 *
	 * @return
	 */
	public List<String> verificarRestricoesParaParaAssinatura() {

		List<String> restricoes = new ArrayList<String>();

		if (isNullOrEmpty(getRelatorio())) {
			restricoes.add("O relatório não foi elaborado!");
		}

		if (isRelatorParaAcordaoDiferenteRelatorOriginario()) {
			if (isNullOrEmpty(getVotoRelator())) {
				restricoes.add("O voto não foi elaborado!");
			}
		}
		else {
			if (isNullOrEmpty(getEmentaRelatorDoAcordao())) {
				restricoes.add("A ementa não foi elaborada!");
			}

			if (isNullOrEmpty(getVotoRelator())) {
				restricoes.add("O voto não foi elaborado!");
			}
		}

		return restricoes;
	}

	private boolean isNullOrEmpty(SessaoProcessoDocumento spd) {
		return spd == null || isNullOrEmpty(spd.getProcessoDocumento());

	}

	private boolean isNullOrEmpty(ProcessoDocumento pd) {
		return pd == null
				|| pd.getProcessoDocumentoBin() == null
				|| pd.getProcessoDocumentoBin().getModeloDocumento() == null
				|| StringUtils.isEmpty(pd.getProcessoDocumentoBin().getModeloDocumento().trim());
	}

	public boolean isRelatorParaAcordaoDiferenteRelatorOriginario() {
		OrgaoJulgador relator = getSessaoPautaProcessoTrf().getOrgaoJulgadorRelator() != null ? getSessaoPautaProcessoTrf().getOrgaoJulgadorRelator() :
			getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador();
		return !getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor().equals(relator);
	}

	public boolean isExigeAssinaturaEmenta() {
		if (isRelatorParaAcordaoDiferenteRelatorOriginario()) {
			return !isNullOrEmpty(getEmentaRelatorDoAcordao());
		}
		else {
			return true;
		}
	}

	public boolean getPermiteCompilacao() {
		return verificarRestricoesParaParaAssinatura().isEmpty();
	}

	public List<ProcessoDocumento> getProcessoDocumentoVotosSemVotoRelatorParaAcordao() {

		List<ProcessoDocumento> votos = new ArrayList<ProcessoDocumento>();

		for (SessaoProcessoDocumento spd : getVotosSemVotoRelatorParaAcordao()) {
			votos.add(spd.getProcessoDocumento());
		}

		return votos;
	}

	public void setNotasOrais(SessaoProcessoDocumento notasOrais) {
		this.notasOrais = notasOrais;
	}

	public void setAssinadoRelatorio(boolean assinadoRelatorio) {
		this.assinadoRelatorio = assinadoRelatorio;
	}

	public boolean isAssinadoRelatorio() {
		return assinadoRelatorio;
	}
	
	public boolean isAssinadoVotoRelatorAcordao() {
		return assinadoVotoRelatorAcordao;
	}

	public void setAssinadoVotoRelatorAcordao(boolean assinadoVotoRelatorAcordao) {
		this.assinadoVotoRelatorAcordao = assinadoVotoRelatorAcordao;
	}

	public boolean isAssinadoVotoRelatorProcesso() {
		return assinadoVotoRelatorProcesso;
	}

	public void setAssinadoVotoRelatorProcesso(boolean assinadoVotoRelatorProcesso) {
		this.assinadoVotoRelatorProcesso = assinadoVotoRelatorProcesso;
	}

	public boolean isAssinadoEmenta() {
		return assinadoEmenta;
	}

	public void setAssinadoEmenta(boolean assinadoEmenta) {
		this.assinadoEmenta = assinadoEmenta;
	}
	
	/**
	 * Método responsável por definir um mapa contendo os órgãos julgadores e respectivos magistrados presentes na sessão de julgamento do processo para o qual o acórdão está sendo gerado.
	 * @param composicaoSessaoJulgamento
	 */
	public void setComposicaoSessaoJulgamento(
			Map<OrgaoJulgador, PessoaMagistrado> composicaoSessaoJulgamento) {
		this.composicaoSessaoJulgamento = composicaoSessaoJulgamento;
	}

	/**
	 * Método responsável por recuperar um mapa contendo os órgãos julgadores e
	 * respectivos magistrados presentes na sessão de julgamento do processo para o qual o acórdão está sendo gerado.
	 * @return
	 */
	public Map<OrgaoJulgador, PessoaMagistrado> getComposicaoSessaoJulgamento() {
		if (composicaoSessaoJulgamento == null) {
			composicaoSessaoJulgamento = new HashMap<OrgaoJulgador, PessoaMagistrado>(0);
		}
		return composicaoSessaoJulgamento;
	}

	public boolean isAssinadoNotasOrais() {
		return assinadoNotasOrais;
	}

	public void setAssinadoNotasOrais(boolean assinadoNotasOrais) {
		this.assinadoNotasOrais = assinadoNotasOrais;
	}
}