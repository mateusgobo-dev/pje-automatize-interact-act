package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.vo.AcordaoCompilacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;

@Name(value=AcordaoModelo.NAME)
@Scope(ScopeType.EVENT)
public class AcordaoModelo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "acordaoModelo";

	private AcordaoCompilacao acordaoCompilacao;
	
	@Logger
	private Log logger;
	
	void inicializaAcordaoCompilacao() {
		SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager = ComponentUtil.getComponent(SessaoPautaProcessoTrfManager.class);
		SessaoPautaProcessoTrf sppt = sessaoPautaProcessoTrfManager.getSessaoPautaProcessoTrfJulgado(ProcessoJbpmUtil.getProcessoTrf());
		if (sppt != null) {
			try {
				sessaoPautaProcessoTrfManager.atualizaSessaoProcessoDocumentos(sppt.getProcessoTrf(), sppt.getSessao());
				 TaskInstance taskInstance = org.jboss.seam.bpm.TaskInstance.instance();
                 if (taskInstance != null){
                	 acordaoCompilacao = sessaoPautaProcessoTrfManager.recuperarAcordaoCompilacao(sppt, taskInstance);

                 }
			 } catch (Exception e) {
                 logger.error(e.getMessage());
			 }
		}
	}

	public AcordaoCompilacao getAcordaoCompilacao() {
		return acordaoCompilacao;
	}

	public void setAcordaoCompilacao(AcordaoCompilacao acordaoCompilacao) {
		this.acordaoCompilacao = acordaoCompilacao;
	}

	public String getProclamacaoDecisao() {
		String resultado = null;
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null && getAcordaoCompilacao().getSessaoPautaProcessoTrf() != null){
			resultado = getAcordaoCompilacao().getSessaoPautaProcessoTrf().getProclamacaoDecisao();
		}
		return (resultado != null)? resultado : "";
	}
	
	public String getEmenta() {
		String resultado = "";
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null){
			resultado = getModeloDocumento(getAcordaoCompilacao().getEmentaRelatorDoAcordao());
		}
		return resultado;
	}
	
	public String getNotasOrais() {		
		String resultado = "";
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null){
			resultado = getModeloDocumento(getAcordaoCompilacao().getNotasOrais());
		}
		return resultado;
	}
	
	private String getModeloDocumento(ProcessoDocumento pd) {
		String resultado = (pd != null && pd.getProcessoDocumentoBin() != null)? pd.getProcessoDocumentoBin().getModeloDocumento() : "";
		return (resultado != null)? resultado : "";
	}
	
	private String getModeloDocumento(SessaoProcessoDocumento spd) {
		String resultado = (spd != null)? getModeloDocumento(spd.getProcessoDocumento()) : "";
		return (resultado != null)? resultado : "";
	}
	
	public String getRelatorio() {
		String resultado = "";
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null){
			resultado = getModeloDocumento(getAcordaoCompilacao().getRelatorio()); 
		}
		return resultado;
	}
	
	/**
	 * Usualmente é o <b>voto vencedor</b>!
	 * @return Texto com o voto vencedor.
	 */
	public String getVotoRelatorParaAcordao() {
		String resultado = "";
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null){
			resultado = getModeloDocumento(getAcordaoCompilacao().getVotoRelatorDoAcordao());
		}
		return resultado;
	}
	
	public List<ProcessoDocumento> getVotosSemVotoRelatorParaAcordao() {
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null) {
			return sessaoProcessoDocumentoParaProcessoDocumento(getAcordaoCompilacao().getVotosSemVotoRelatorParaAcordao());
		}
		return new ArrayList<>(0);
	}
	
	public List<ProcessoDocumento> getVotosSemVotoRelatorOriginalEAcordao() {
		List<ProcessoDocumento> retorno = new ArrayList<>(0);
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null) {
			retorno = sessaoProcessoDocumentoParaProcessoDocumento(getAcordaoCompilacao().getVotosSemVotoRelatorOriginalEAcordao());
		}
		return retorno;
	}
	
	public List<ProcessoDocumento> getVotosQueForamAssinadosSemVotoRelatorParaAcordao() {
		List<ProcessoDocumento> retorno = new ArrayList<>(0);
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null) {
			retorno = sessaoProcessoDocumentoParaProcessoDocumento(getAcordaoCompilacao().getVotosQueForamAssinadosSemVotoRelatorParaAcordao());
		}
		return retorno;
	}
	
	public List<ProcessoDocumento> getVotosQueForamAssinadosSemVotoRelatorOriginalEAcordao() {
		List<ProcessoDocumento> retorno = new ArrayList<>(0);
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null) {
			retorno = sessaoProcessoDocumentoParaProcessoDocumento(getAcordaoCompilacao().getVotosQueForamAssinadosSemVotoRelatorOriginalEAcordao());
		}
		return retorno;
	}
	
	private List<ProcessoDocumento> sessaoProcessoDocumentoParaProcessoDocumento(List<SessaoProcessoDocumentoVoto> sessaoProcessoDocumentos) {
		
		List<ProcessoDocumento> pds = new ArrayList<>();
		
		for (SessaoProcessoDocumentoVoto spd : sessaoProcessoDocumentos) {
			pds.add(spd.getProcessoDocumento());
		}
		
		return pds;		
	}
	
	public ProcessoTrf getProcesso() {
		if(getSessaoPautaProcessoTrf() != null) {
			return getSessaoPautaProcessoTrf().getProcessoTrf();
		}
		return null;
	}
	
	public SessaoPautaProcessoTrf getSessaoPautaProcessoTrf() {
		SessaoPautaProcessoTrf retorno = null;
		if(getAcordaoCompilacao() == null){
			inicializaAcordaoCompilacao();
		}
		if(getAcordaoCompilacao() != null) {
			retorno = getAcordaoCompilacao().getSessaoPautaProcessoTrf();
		}
		return retorno;
	}
	
	public String getComposicaoSessao() {

		StringBuilder resultado = new StringBuilder();

		if(getSessaoPautaProcessoTrf() != null && getSessaoPautaProcessoTrf().getSessao() != null) {
			List<OrgaoJulgador> orgaosJulgadoresPresentes = getSessaoPautaProcessoTrf().getSessao().getOrgaosJulgadoresPresentes();

			for (OrgaoJulgador orgaoJulgador : orgaosJulgadoresPresentes) {
				resultado.append("<br />");
				resultado.append(orgaoJulgador.toString());
			}
		}
		
		return resultado.toString();
	}
	/**
	 * Método responsável por recuperar o  magistrado presidente da sessão de julgamento.
	 * @return pessoaMagistrado
	 */
	public PessoaMagistrado getPessoaMagistradoPresidenteSessao() {
		List<SessaoComposicaoOrdem> ordensDeComposicao = getSessaoPautaProcessoTrf().getSessao().getSessaoComposicaoOrdemList();
		return ComponentUtil.getComponent(PessoaMagistradoManager.class).getMagistradoPresidenteSessao(ordensDeComposicao);
	}
	/**
	 * Método responsável por recuperar os dados do magistrado relator originário do processo especificado. 
	 * @return pessoaFisica
	 */
	public PessoaFisica getPessoaFisicaMagistradoRelatorOriginario() {
		Map<OrgaoJulgador, PessoaMagistrado> mapaSessaoComposicao = getAcordaoCompilacao().getComposicaoSessaoJulgamento();
		return ComponentUtil.getComponent(PessoaFisicaManager.class).getPessoaFisicaMagistradoRelatorOriginario(mapaSessaoComposicao, getProcesso().getOrgaoJulgador());
				
	}
	/**
	 * Método responsável por recuperar os dados do magistrado relator vencedor do julgamento processo especificado.
	 * @return pessoaFisica
	 */
	public PessoaFisica getPessoaFisicaMagistradoRelatorVencedor() {
		Map<OrgaoJulgador, PessoaMagistrado> mapaSessaoComposicao = getAcordaoCompilacao().getComposicaoSessaoJulgamento();
		return ComponentUtil.getComponent(PessoaFisicaManager.class).getPessoaFisicatMagistradoRelatorVencedor(mapaSessaoComposicao, getSessaoPautaProcessoTrf().getOrgaoJulgadorVencedor());
	}
	
}