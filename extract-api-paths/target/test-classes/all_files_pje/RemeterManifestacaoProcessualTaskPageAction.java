package br.com.infox.bpm.taskPage.remessacnj;

import java.io.OutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.util.encoders.Hex;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.quartz.CronExpression;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.controleprazos.AgendaServicosPeriodicos;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.nucleo.service.RemessaManifestacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

public abstract class RemeterManifestacaoProcessualTaskPageAction extends
		TaskAction implements Serializable {

	/**
	 * 
	 */
	protected static final long serialVersionUID = 1L;

	protected static final SimpleDateFormat dateFormatData = new SimpleDateFormat(
			"yyyyMMdd");
	protected static final SimpleDateFormat dateFormatDataHora = new SimpleDateFormat(
			"yyyyMMddHHmmss");

	@Logger
	protected Log log;

	@In
	protected transient TramitacaoProcessualService tramitacaoProcessualService;

	@In
	protected transient DocumentoJudicialService documentoJudicialService;

	@In
	protected transient ParametroService parametroService;

	@In
	protected transient ProcessoJudicialService processoJudicialService;

	@In
	protected transient RemessaManifestacaoProcessualService remessaManifestacaoProcessualService;

	@In(create = true)
	protected transient ManifestacaoProcessualMetaDataManager manifestacaoProcessualMetaDataManager;

	protected ManifestacaoProcessualMetaData manifestacaoProcessualMetaData;
	protected Map<String, Object> dadosResposta = new HashMap<String, Object>();

	protected abstract String getWsdl();

	protected abstract String getIdManifestante();
	
	protected abstract String getDestino();

	protected abstract String getSenhaManifestante();

	public ManifestacaoProcessualMetaData getManifestacaoProcessualMetaData() {
		return manifestacaoProcessualMetaData;
	}

	public void setManifestacaoProcessualMetaData(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData) {
		this.manifestacaoProcessualMetaData = manifestacaoProcessualMetaData;
	}

	public Map<String, Object> getDadosResposta() {
		return dadosResposta;
	}

	public Date convertDataHora(String dataHoraString) throws Exception {
		return dateFormatDataHora.parse(dataHoraString);
	}

	public Date convertData(String dataString) throws Exception {
		return dateFormatData.parse(dataString);
	}

	@SuppressWarnings("unchecked")
	@Create
	public void init() {
		manifestacaoProcessualMetaData = (ManifestacaoProcessualMetaData) tramitacaoProcessualService
				.recuperaVariavel(Variaveis.VARIAVEL_REMESSA_MANIFESTACAO_PROCESSUAL);

		dadosResposta = (Map<String, Object>) tramitacaoProcessualService
				.recuperaVariavel(Variaveis.VARIAVEL_REMESSA_RESPOSTA_MANIFESTACAO_PROCESSUAL);
	}

	public Date getNextFireTime() throws Exception {
		CronExpression cronExpression = new CronExpression(
				AgendaServicosPeriodicos.CRON_EXPRESSION_REMESSA_MANIFESTACAO_PROCESSUAL);
		return cronExpression.getNextValidTimeAfter(new Date());
	}

	public boolean remeterManifestacaoProcessual() {
		try {
			manifestacaoProcessualMetaDataManager
					.refresh(manifestacaoProcessualMetaData);
			
			ProcessoTrf processoOrigem = processoJudicialService.findById(manifestacaoProcessualMetaData.getIdProcessoTrfOrigem());
			
			ManifestacaoProcessualRespostaDTO resposta = remessaManifestacaoProcessualService
					.remeterManifestacaoProcessual(
							getWsdl(), getDestino(), processoOrigem, 
							montarManifestacaoProcessual(manifestacaoProcessualMetaData));
			
			return resposta.getSucesso();

		} catch (Exception e) {
			log.error("Erro ao enviar manifestação processual", e);
			return false;
		}
	}

	public void downloadRecibo() throws Exception {
		ManifestacaoProcessualRespostaDTO resposta = (ManifestacaoProcessualRespostaDTO) dadosResposta
				.get(RemessaManifestacaoProcessualService.RESPOSTA_MANIFESTACAO);
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) facesContext
				.getExternalContext().getResponse();

		response.setContentType("application/pdf");

		response.setContentLength(resposta.getRecibo().length);
		response.setHeader("Content-disposition",
				"attachment; filename=\"reciboManifestacaoProcessual.pdf\"");
		OutputStream out = response.getOutputStream();
		out.write(resposta.getRecibo());
		out.flush();
		facesContext.responseComplete();
	}

	/**
	 * Montar manifestação processual a partir do pojo montado em
	 * PrepararManifestacaoProcessualTaskPageAction
	 * 
	 * @param manifestacaoProcessualMetaData
	 * @return
	 * @throws Exception
	 */
	protected ManifestacaoProcessualRequisicaoDTO montarManifestacaoProcessual(
			ManifestacaoProcessualMetaData manifestacaoProcessualMetaData)
			throws Exception {

		ProcessoTrf processoTrf = processoJudicialService
				.findById(manifestacaoProcessualMetaData
						.getIdProcessoTrfOrigem());
		String numeroNaoFormatado = processoTrf.getNumeroProcesso().replaceAll(
				"[\\.,-]", "");

		ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual = new ManifestacaoProcessualRequisicaoDTO();
		manifestacaoProcessual.setProcessoTrf(processoTrf);

		return manifestacaoProcessual;
	}
}
