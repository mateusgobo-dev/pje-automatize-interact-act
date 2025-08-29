package br.com.infox.pje.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.infox.ibpm.service.EmailService;
import br.com.infox.pje.manager.PessoaProcuradorProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

@Name(IntimacaoPartesService.NAME)
@Install(precedence = Install.APPLICATION)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class IntimacaoPartesService extends GenericManager {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "intimacaoPartesService";
	private static final int PRAZO_PROCESSUAL_PARTE = 10;

	@In
	private PessoaProcuradorProcuradoriaManager pessoaProcuradorProcuradoriaManager;
	@In
	private PessoaManager pessoaManager;
	@In
	private ProcessoParteManager processoParteManager;
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;
	@In
	private EmailService emailService;

	private Integer prazoExpedienteDocumentoSentenca;
	private Integer prazoExpedienteDocumentoAcordao;

	/**
	 * @author João Paulo Lacerda
	 * @return O prazo de expediente do documento sentença.
	 */
	public Integer getPrazoExpedienteDocumentoSentenca() {
		if (prazoExpedienteDocumentoSentenca == null) {
			TipoProcessoDocumentoTrf tipoProcessoDocumentoTrf = ParametroUtil.instance()
					.getTipoProcessoDocumentoSentenca();
			prazoExpedienteDocumentoSentenca = tipoProcessoDocumentoTrf.getPrazoExpedienteAutomatico();
		}
		return prazoExpedienteDocumentoSentenca;
	}

	/**
	 * @author João Paulo Lacerda
	 * @return O prazo de expediente do documento acordao.
	 */
	public Integer getPrazoExpedienteDocumentoAcordao() {
		if (prazoExpedienteDocumentoAcordao == null) {
			TipoProcessoDocumentoTrf tipoProcessoDocumentoTrf = ParametroUtil.instance()
					.getTipoProcessoDocumentoAcordao();
			prazoExpedienteDocumentoAcordao = tipoProcessoDocumentoTrf.getPrazoExpedienteAutomatico();
		}
		return prazoExpedienteDocumentoAcordao;
	}

	public boolean intimarPartesAutomaticamente(ProcessoTrf processoTrf, ProcessoDocumento processoDocumentoComModelo,
			ProcessoDocumento processoDocumentoAto) {
		ProcessoExpediente processoExpediente = new ProcessoExpediente();
		processoExpediente.setProcessoTrf(processoTrf);
		processoExpediente.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoIntimacao());
		processoExpediente.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.E);
		processoExpediente.setInTemporario(false);
		processoExpediente.setDtCriacao(new Date());
		persist(processoExpediente);

		List<ProcessoParte> partesSemPendenciaList = associarPartesAoExpediente(processoTrf.getProcessoParteList(),
				processoExpediente);
		criarDocumentoParaIntimacaoAutomatica(processoTrf, processoDocumentoComModelo, processoDocumentoAto,
				processoExpediente);
		try {
			RegistraEventoAction.instance().registraPorNome(
					ParametroUtil.instance().getAgrupamentoExpedicaoDocumento().getAgrupamento());
		} catch (Exception e) {
			e.printStackTrace();
		}
		intimarPartesPorEmail(partesSemPendenciaList);

		if (DomicilioEletronicoService.instance().isIntegracaoHabilitada()) {
			DomicilioEletronicoService.instance().enviarExpedientesAsync(Arrays.asList(processoExpediente));
		}

		return partesSemPendenciaList.size() == processoTrf.getProcessoParteList().size();
	}

	public boolean intimarPartesAutomaticamente(ProcessoTrf processoTrf, ProcessoDocumento processoDocumento) {
		return intimarPartesAutomaticamente(processoTrf, processoDocumento, processoDocumento);
	}

	private void intimarPartesPorEmail(List<ProcessoParte> processoParteList) {
		List<UsuarioLogin> usuariosLogin = new ArrayList<UsuarioLogin>();
		for (ProcessoParte processoParte : processoParteList) {
			for (Pessoa pessoa : processoParteManager.getPartesComEmail(processoParte)) {
				usuariosLogin.add(pessoa);
			}
		}
		String body = ParametroUtil.instance().getModeloDocumentoEmailExpediente().getModeloDocumento();
		String subject = "Aviso de Intimação\\Citação das Partes";
		emailService.enviarEmail(usuariosLogin, subject, body);
	}

	/**
	 * Associa as partes ao expediente se as partes estiverem válidas para a
	 * intimação automática.
	 * 
	 * @author Joao Paulo Lacerda
	 * @param processoParteList
	 *            as partes do processo.
	 * @param processoExpediente
	 *            o processo expediente.
	 * @return <code>True</code> se as partes forem validadas para o envio
	 *         automático ou <code>False</code> caso não tenham sido validadas.
	 */
	public List<ProcessoParte> associarPartesAoExpediente(List<ProcessoParte> processoParteList,
			ProcessoExpediente processoExpediente) {
		List<ProcessoParte> parteSemPendenciaList = new ArrayList<ProcessoParte>();
		for (ProcessoParte processoParte : processoParteList) {
			ProcessoParteExpediente processoParteExpediente = new ProcessoParteExpediente();
			processoParteExpediente.setProcessoExpediente(processoExpediente);
			processoParteExpediente.setPessoaParte(processoParte.getPessoa());
			processoParteExpediente.setPrazoLegal(getPrazoIntimacaoAutomatica(processoParte));
			processoParteExpediente.setPrazoProcessual(PRAZO_PROCESSUAL_PARTE);
			processoParteExpediente.setNomePessoaParte(processoParte.getPessoa().getNome());

			String motivoPendencia = validarParteParaExpediente(processoParte);
			if (motivoPendencia == null) {
				parteSemPendenciaList.add(processoParte);
			} else {
				if (motivoPendencia.equals(ParametroUtil.instance()
						.getDsPendenciaIntimacoesAutomaticasPessoaFisicaJuridica())) {
					if (processoParteManager.isUmaPessoaFisicaComUmAdvogadoCertificado(processoParte)) {
						parteSemPendenciaList.add(processoParte);
					}
				}
				processoParteExpediente.setFechado(true);
				processoParteExpediente.setPendencia(motivoPendencia);
			}
			persist(processoParteExpediente);
		}
		return parteSemPendenciaList;
	}

	private Integer getPrazoIntimacaoAutomatica(ProcessoParte processoParte) {
		if (getPrazoExpedienteTipoDocumento() != null) {
			return getPrazoAutomaticoParaExpediente(processoParte) * getPrazoExpedienteTipoDocumento();
		} else {
			return ParametroUtil.instance().getPrazoIntimacoesAutomaticas();
		}
	}

	private Integer getPrazoExpedienteTipoDocumento() {
		return ParametroUtil.instance().isPrimeiroGrau() ? getPrazoExpedienteDocumentoSentenca()
				: getPrazoExpedienteDocumentoAcordao();
	}

	private Integer getPrazoAutomaticoParaExpediente(ProcessoParte processoParte) {
		Integer prazoAutomaticoParaExpediente = null;
		if (processoParte.getPessoa() instanceof PessoaJuridica) {
			PessoaJuridica pessoaJuridica = find(PessoaJuridica.class, processoParte.getPessoa().getIdUsuario());
			prazoAutomaticoParaExpediente = pessoaJuridica.getPrazoExpedienteAutomatico();
		}
		if (prazoAutomaticoParaExpediente == null) {
			prazoAutomaticoParaExpediente = processoParte.getPessoa().getTipoPessoa().getPrazoExpedienteAutomatico();
		}
		return prazoAutomaticoParaExpediente != null ? prazoAutomaticoParaExpediente : 1;
	}

	/**
	 * Este método valida a parte para intimação automática. Para isto, recebe
	 * como argumento uma parte do processo e valida se:
	 * <ol>
	 * <li>É do tipo <i>Coatora</i></li>
	 * <li>É uma <i>Entidade</i>, ou seja, se atrai competência. Se for
	 * <i>Entidade</i>, verifica se existe procurador vinculado com situação
	 * <i>Ativa</i> e que possua certificado cadastrado no sistema.</li>
	 * <li>É um advogado, neste caso, deverá verificar se a situação do advogado
	 * está <i>Ativa</i> e se possui certificado cadastrado no sistema.</li>
	 * <li>A parte intimada é uma pessoa Física ou Jurídica, que não pertença ao
	 * grupo das entidades. Neste caso, deverá verificar se a parte está Ativa e
	 * se possui certificado cadastrado no Sistema.</li>
	 * </ol>
	 * 
	 * @author Joao Paulo Lacerda
	 * @param processoParte
	 *            parte do processo a ser validada.
	 * @return Caso a parte não seja validade o método irá retorna o motivo.
	 */
	public String validarParteParaExpediente(ProcessoParte processoParte) {
		ParametroUtil parametroUtil = ParametroUtil.instance();
		if (processoParteManager.isAutoridadeCoatora(processoParte)) {
			return parametroUtil.getDsPendenciaIntimacoesAutomaticasPessoaAutoridadeCoatora();
		}

		Boolean isEntidade = processoParte.getPessoa().getAtraiCompetencia();

		if (isEntidade) {
			if (!pessoaProcuradorProcuradoriaManager.isProcuradorCertificado(processoParte.getPessoa())) {
				return parametroUtil.getDsPendenciaIntimacoesAutomaticasEntidade();
			}
		} else {
			if (!pessoaManager.isPessoaFisicaOuJuridicaCertificada(processoParte.getPessoa())) {
				return parametroUtil.getDsPendenciaIntimacoesAutomaticasPessoaFisicaJuridica();
			}
		}
		if (processoParteManager.isAdvogadoNaoCertificado(processoParte)) {
			return parametroUtil.getDsPendenciaIntimacoesAutomaticasPessoaAdvogado();
		}
		return null;
	}

	/**
	 * Cria documento para intimação automática seguindo os seguintes passos.
	 * Cria o documento, vincula o documento da minuta ao expediente e assina o
	 * documento.
	 * 
	 * @param processoTrf
	 * @param processoDocumento
	 * @param processoExpediente
	 */
	public void criarDocumentoParaIntimacaoAutomatica(ProcessoTrf processoTrf,
			ProcessoDocumento processoDocumentoComModelo, ProcessoDocumento processoDocumentoAto,
			ProcessoExpediente processoExpediente) {

		// Criar documento bin de intimacao
		ProcessoDocumentoBin processoDocumentoAutomaticoBin = new ProcessoDocumentoBin();
		processoDocumentoAutomaticoBin.setModeloDocumento(processoDocumentoComModelo.getProcessoDocumentoBin()
				.getModeloDocumento());
		processoDocumentoAutomaticoBin.setUsuario(Authenticator.getUsuarioLogado());
		processoDocumentoAutomaticoBin.setDataInclusao(new Date());
		persist(processoDocumentoAutomaticoBin);

		// Criar Documento
		ProcessoDocumento processoDocumentoAutomatico = new ProcessoDocumento();
		processoDocumentoAutomatico.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumento());
		processoDocumentoAutomatico.setProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoIntimacao()
				.getTipoProcessoDocumento());
		processoDocumentoAutomatico.setProcesso(processoTrf.getProcesso());
		processoDocumentoAutomatico.setAtivo(true);
		processoDocumentoAutomatico.setDocumentoSigiloso(false);
		processoDocumentoAutomatico.setPapel(Authenticator.getPapelAtual());
		processoDocumentoAutomatico.setProcessoDocumentoBin(processoDocumentoAutomaticoBin);
		processoDocumentoAutomatico.setUsuarioInclusao(Authenticator.getUsuarioLogado());
		persist(processoDocumentoAutomatico);

		// Vincular Documento da Minuta ao Expediente
		ProcessoDocumentoExpediente processoDocumentoExpediente = new ProcessoDocumentoExpediente();
		processoDocumentoExpediente.setProcessoExpediente(processoExpediente);
		processoDocumentoExpediente.setProcessoDocumentoAto(processoDocumentoAto);
		processoDocumentoExpediente.setProcessoDocumento(processoDocumentoAutomatico);
		processoDocumentoExpediente.setAnexo(false);
		persist(processoDocumentoExpediente);

		// Assinar documento de intimacao
		assinaturaDocumentoService.assinarDocumento(processoDocumentoAutomaticoBin, processoDocumentoAto
				.getProcessoDocumentoBin().getSignature(), processoDocumentoAto.getProcessoDocumentoBin()
				.getCertChain());
	}

}
