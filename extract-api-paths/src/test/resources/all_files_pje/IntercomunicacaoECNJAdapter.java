package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ExpedienteProcessualECNJ;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessualECNJ;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.PessoaExpediente;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.converter.PessoaParaIntercomunicacaoPessoaConverter;
import br.jus.cnj.pje.nucleo.manager.EventoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaDocumentoIdentificacaoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

@Name(IntercomunicacaoECNJAdapter.NAME)
public class IntercomunicacaoECNJAdapter implements
		ManifestacaoProcessualHandler {
	
	private static final String NOME_PARAMETRO_EXPEDIENTE = "expediente";

	private static final String CD_TIPO_DOCUMENTO_INTIMACAO = "2000017";

	@Logger
	private Log log;

	private static final String CODIGO_MOVIMENTO_LEGADO = "999999999";

	public static final String NAME = "v222.intercomunicacaoECNJAdapter";

	// utilizado para buscar outros parametros MNI
	public static final String NOME_PARAMETRO_MOVIMENTACAO = "movimento";

	// utilizado como para carregar pessoas juridicas
	private static final String CEP_GENERICO = "00000000";
	private static final String CNPJ_UNIAO = "07.457.108/0001-57";

	// TODO alterar os CNPJ. Só esta com o de pernambuco
	public enum EstadosBrasileirosEnum {
		AC("Acre", "10.572.014/0001-33"), AL("Alagoas", "10.572.014/0001-33"), AP(
				"Amapá", "10.572.014/0001-33"), AM("Amazonas",
				"10.572.014/0001-33"), BA("Bahia", "10.572.014/0001-33"), CE(
				"Ceará", "10.572.014/0001-33"), DF("Distrito Federal",
				"10.572.014/0001-33"), ES("Espírito Santo",
				"10.572.014/0001-33"), GO("Goiás", "10.572.014/0001-33"), MA(
				"Maranhão", "10.572.014/0001-33"), MT("Mato Grosso",
				"10.572.014/0001-33"), MS("Mato Grosso do Sul",
				"10.572.014/0001-33"), MG("Minas Gerais", "10.572.014/0001-33"), PA(
				"Pará", "10.572.014/0001-33"), PB("Paraíba",
				"10.572.014/0001-33"), PR("Paraná", "10.572.014/0001-33"), PE(
				"Pernambuco", "10.572.014/0001-33"), PI("Piauí",
				"10.572.014/0001-33"), RJ("Rio de Janeiro",
				"10.572.014/0001-33"), RN("Rio Grande do Norte",
				"10.572.014/0001-33"), RS("Rio Grande do Sul",
				"10.572.014/0001-33"), RO("Rondônia", "10.572.014/0001-33"), RR(
				"Roraima", "10.572.014/0001-33"), SC("Santa Catarina",
				"10.572.014/0001-33"), SP("São Paulo", "10.572.014/0001-33"), SE(
				"Sergipe", "10.572.014/0001-33"), TO("Tocantins",
				"10.572.014/0001-33");
		private String label;
		private String cnpj;

		EstadosBrasileirosEnum(String label, String cnpj) {
			this.label = label;
			this.cnpj = cnpj;
		}

		public String getLabel() {
			return this.label;
		}

		public String getCnpj() {
			return cnpj;
		}
	}
 
	@In
	private PessoaDocumentoIdentificacaoManager pessoaDocumentoIdentificacaoManager;

	@In
	private ProcessoJudicialManager processoJudicialManager;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In("eventoManager")
	private EventoManager eventoProcessualManager;

	@In
	private ProcessoTrfManager processoTrfManager;
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	
	@In
	private ProcessoExpedienteManager processoExpedienteManager;

	@Override
	public void onBeforeEntregarManifestacaoProcessual(
			ManifestacaoProcessual manifestacaoProcessual) {

		List<DocumentoProcessual> documentosParaIgnorar = new ArrayList<DocumentoProcessual>();

		for (DocumentoProcessual documento : manifestacaoProcessual
				.getDocumento()) {

			if (!documento.isSetDataHora()) {
				documentosParaIgnorar.add(documento);
			}

			// tratar array de assinaturas -- documentos do ecnj não possuem
			// assinatura
			if (documento.getAssinatura().size() == 1
					&& documento.getAssinatura().get(0) == null) {
				documento.getAssinatura().clear();
				documento.setHash("GERAR_POSTERIORMENTE");
			}
		}

		manifestacaoProcessual.getDocumento().removeAll(documentosParaIgnorar);

		// array de processos vinculados -- ecnj não possui processos vinculados
		if (manifestacaoProcessual.getDadosBasicos().getProcessoVinculado()
				.size() == 1
				&& manifestacaoProcessual.getDadosBasicos()
						.getProcessoVinculado().get(0) == null) {
			manifestacaoProcessual.getDadosBasicos().getProcessoVinculado()
					.clear();
		}

		// Criar pessoa vinculada
		Pessoa pessoaVinculada = null;
		for (PoloProcessual polo : manifestacaoProcessual.getDadosBasicos()
				.getPolo()) {
			for (Parte parte : polo.getParte()) {
				// se a pessoa AUTORIDADE não tiver documento,
				// adicionar pessoa vinculada o estado que representa a UF do
				// sistema

				for (br.jus.cnj.intercomunicacao.v222.beans.Endereco endereco : parte
						.getPessoa().getEndereco()) {
					if (parte.getPessoa().getTipoPessoa() == TipoQualificacaoPessoa.AUTORIDADE) {
						if (parte.getPessoa().getDocumento().isEmpty()
								|| parte.getPessoa().getDocumento().get(0) == null) {
							if (endereco.isSetEstado()
									&& !endereco.getEstado().trim().isEmpty()) {
								EstadosBrasileirosEnum uf = EstadosBrasileirosEnum
										.valueOf(endereco.getEstado());
								pessoaVinculada = pessoaDocumentoIdentificacaoManager
										.findByNumeroDocumento(uf.getCnpj())
										.get(0).getPessoa();

							}
							// carregar união
							else {
								pessoaVinculada = pessoaDocumentoIdentificacaoManager
										.findByNumeroDocumento(CNPJ_UNIAO)
										.get(0).getPessoa();
							}

							parte.getPessoa().setPessoaVinculada(
									converterPessoaParaIntercomunicacaoPessoa(pessoaVinculada));

							if (!parte.getPessoa().getOutroNome().isEmpty()
									&& parte.getPessoa().getOutroNome().get(0) == null) {
								parte.getPessoa().getOutroNome().clear();
							}
						}
					}
					// tratar endereços sem cep
					if (endereco.getCep() == null
							|| endereco.getCep().trim().isEmpty()) {
						endereco.setCep(CEP_GENERICO);
					}

					// Carrega as relações pessoais
					if (!parte.getPessoa().getPessoaRelacionada().isEmpty()
							&& parte.getPessoa().getPessoaRelacionada().get(0) == null) {
						parte.getPessoa().getPessoaRelacionada().clear();
					}

				}

				// se autoridade, tratar pessoa vinculada
				if (parte.getPessoa().getTipoPessoa() == TipoQualificacaoPessoa.AUTORIDADE
						&& parte.getPessoa().getPessoaVinculada() == null) {
					if (parte.getPessoa().getEndereco() == null
							|| parte.getPessoa().getEndereco().isEmpty()) {
						for (br.jus.cnj.intercomunicacao.v222.beans.Endereco endereco : parte
								.getPessoa().getEndereco()) {
							if (parte.getPessoa().getDocumento().isEmpty()
									|| parte.getPessoa().getDocumento().get(0) == null) {
								if (endereco.isSetEstado()) {
									EstadosBrasileirosEnum uf = EstadosBrasileirosEnum
											.valueOf(endereco.getEstado());
									pessoaVinculada = pessoaDocumentoIdentificacaoManager
											.findByNumeroDocumento(uf.getCnpj())
											.get(0).getPessoa();

								}

							}
						}
					}
					// carregar união
					else {
						pessoaVinculada = pessoaDocumentoIdentificacaoManager
								.findByNumeroDocumento(CNPJ_UNIAO).get(0)
								.getPessoa();
					}

					parte.getPessoa().setPessoaVinculada(
							converterPessoaParaIntercomunicacaoPessoa(pessoaVinculada));
				}

				for (RepresentanteProcessual rp : parte.getAdvogado()) {
					for (br.jus.cnj.intercomunicacao.v222.beans.Endereco endereco : rp
							.getEndereco()) {
						// tratar endereços sem cep
						if (endereco.getCep() == null
								|| endereco.getCep().trim().isEmpty()) {
							endereco.setCep(CEP_GENERICO);
						}
					}
				}
			}
		}
	}

	@Override
	public void onAfterEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacaoProcessual, ProcessoTrf processoTrf, ProcessoDocumento documentoPrincipal) {
		try {
			if(log.isDebugEnabled()){
				log.debug("Salvando Movimentacoes... ");
			}
			processoTrf = processoTrfManager.find(ProcessoTrf.class,
					processoTrf.getIdProcessoTrf());
			// Salvar as movimentações
			for (Parametro parametro : manifestacaoProcessual.getParametros()) {
				if (parametro.getNome().equals(NOME_PARAMETRO_MOVIMENTACAO)) {
					// TODO verificar se um parser xml é mais performatico que
					// manipular o xml como string
					String xml = parametro.getValor()
							.replaceAll("<movimentar>", "")
							.replaceAll("</movimentar>", "");
					String[] xmls = xml.split("</movimentacaoProcessualECNJ>");
					for (String item : xmls) {
						MovimentacaoProcessualECNJ movimentoECNJ = parse(item
								+ "</movimentacaoProcessualECNJ>",
								MovimentacaoProcessualECNJ.class);

						salvarMovimentacao(manifestacaoProcessual,
								movimentoECNJ, processoTrf);
					}
				}
				
				if (parametro.getNome().equals(NOME_PARAMETRO_EXPEDIENTE)) {
					// TODO verificar se um parser xml é mais performatico que
					// manipular o xml como string
					String xml = parametro.getValor()
							.replaceAll("<expedientes>", "")
							.replaceAll("</expedientes>", "");
					String[] xmls = xml.split("</expedienteProcessualECNJ>");
					for (String item : xmls) {
						ExpedienteProcessualECNJ movimentoECNJ = parse(item
								+ "</expedienteProcessualECNJ>",
								ExpedienteProcessualECNJ.class);

						salvarExpediente(manifestacaoProcessual,
								movimentoECNJ, processoTrf);
					}
				}
			}
			if(log.isDebugEnabled()){
				log.debug("Persist movimentações...");
			}
			
			processoTrfManager.persist(processoTrf);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private <T> T parse(String xml, Class<T> clazz) throws Exception {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			StringReader reader = new StringReader(xml);
			T returnValue = (T) jaxbUnmarshaller.unmarshal(reader);

			reader.close();
			return returnValue;
		} catch (JAXBException e) {
			throw new Exception("Erro ao decodificar o xml: " + xml, e);
		}
	}

	private void salvarMovimentacao(
			ManifestacaoProcessual manifestacaoProcessual,
			MovimentacaoProcessualECNJ movimentacaoProcessualECNJ,
			ProcessoTrf processoTrf) throws Exception {

		ProcessoEvento processoEvento = new ProcessoEvento();

		processoEvento.setDataAtualizacao(new SimpleDateFormat("yyyyMMddHms")
				.parse(movimentacaoProcessualECNJ.getDataHora().getValue()));
		processoEvento.setDescricaoEvento(movimentacaoProcessualECNJ
				.getMovimentoLocal());
		/*
		 * String codigoNacional =
		 * String.valueOf(movimentacaoProcessualECNJ.getMovimentoNacional
		 * ().getCodigoNacional()); Evento evento =
		 * eventoProcessualManager.findByIds(codigoNacional).get(0);
		 */
		Evento evento = eventoProcessualManager.findByIds(
				CODIGO_MOVIMENTO_LEGADO).get(0);
		processoEvento.setEvento(evento);
		// movimentoProcesso.setIdJbpmTask(processoTrf.getProcesso().getIdJbpm());
		processoEvento.setProcessado(true);
		processoEvento.setProcesso(processoTrf.getProcesso());
		processoEvento.setDescricaoEvento("");
		for (String complemento : movimentacaoProcessualECNJ.getComplemento()) {
			processoEvento.setDescricaoEvento(processoEvento
					.getDescricaoEvento() + "/" + complemento);
		}

		if (processoEvento.getDescricaoEvento() != null) {
			processoEvento.setDescricaoEvento(processoEvento
					.getDescricaoEvento().replaceFirst("/", ""));
			processoEvento.setTextoFinalExterno(processoEvento
					.getDescricaoEvento());
			processoEvento.setTextoFinalInterno(processoEvento
					.getDescricaoEvento());
		}

		processoEvento
				.setObservacao(movimentacaoProcessualECNJ.getObservacao());

		/*
		 * for (ProcessoDocumento processoDocumento : processoTrf.getProcesso()
		 * .getProcessoDocumentoList()) { for (DocumentoProcessual documentoMNI
		 * : manifestacaoProcessual .getDocumento()) { String numeroDocumento =
		 * documentoMNI.getIdDocumento(); if
		 * (numeroDocumento.equals(processoDocumento .getNumeroDocumento())) {
		 * for (Parametro outroParametro : documentoMNI .getOutroParametro()) {
		 * if (outroParametro.getNome().equals(
		 * NOME_PARAMETRO_RESPONSAVEL_MOVIMENTO)) {
		 * atualizaProcessoDocumento(processoDocumento,
		 * movimentacaoProcessualECNJ.getAtivo(), outroParametro.getValue()); }
		 * } } } }
		 */

		// vinculacao do evento com o documento
		if (movimentacaoProcessualECNJ.getIdDocumentoVinculado() == null
				|| !movimentacaoProcessualECNJ.getIdDocumentoVinculado()
						.isEmpty()) {
			if (movimentacaoProcessualECNJ.getIdDocumentoVinculado().get(0) != null
					&& !movimentacaoProcessualECNJ.getIdDocumentoVinculado()
							.get(0).trim().isEmpty()) {

				List<ProcessoDocumento> processoDocumentoList = processoDocumentoManager
						.getDocumentosPorNumero(processoTrf.getIdProcessoTrf(),
								movimentacaoProcessualECNJ
										.getIdDocumentoVinculado().get(0));

				if (processoDocumentoList != null
						&& !processoDocumentoList.isEmpty()) {
					ProcessoDocumento documentoPrincipal = processoDocumentoList
							.get(0);
					processoEvento.setProcessoDocumento(documentoPrincipal);

					// atualizar os documentos vinculados
					for (int i = 1; i < movimentacaoProcessualECNJ
							.getIdDocumentoVinculado().size(); i++) {
						List<ProcessoDocumento> processoDocumentoVinculadoList = processoDocumentoManager
								.getDocumentosPorNumero(
										processoTrf.getIdProcessoTrf(),
										movimentacaoProcessualECNJ
												.getIdDocumentoVinculado().get(
														i));
						if (processoDocumentoVinculadoList != null
								&& !processoDocumentoVinculadoList.isEmpty()) {
							ProcessoDocumento documentoVinculado = processoDocumentoVinculadoList
									.get(0);
							if (documentoVinculado != null
									&& !documentoVinculado
											.equals(documentoPrincipal)) {
								documentoVinculado
										.setDocumentoPrincipal(documentoPrincipal);
							}
						} else {
							log.warn("documento "
									+ movimentacaoProcessualECNJ
											.getIdDocumentoVinculado().get(i)
									+ " não encontrado. Vinculado ao movimento "
									+ movimentacaoProcessualECNJ
											.getIdentificadorMovimento());
						}

					}
				}

			}
		}
		processoEvento.setUsuario(ParametroUtil.instance().getUsuarioSistema());
		processoEvento.setVerificadoProcessado(true);
		processoEvento.setVisibilidadeExterna(true);

		processoTrf.getProcesso().getProcessoEventoList().add(processoEvento);
	}
	
	private void salvarExpediente(ManifestacaoProcessual manifestacaoProcessual,
			ExpedienteProcessualECNJ expedienteProcessualECNJ,
			ProcessoTrf processoTrf) throws Exception{

		ProcessoExpediente processoExpediente = new ProcessoExpediente();
		
		processoExpediente.setProcessoTrf(processoTrf);
		processoExpediente.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.valueOf(expedienteProcessualECNJ.getMeioExpedicao().name()));
		processoExpediente.setUrgencia(false);
		processoExpediente.setTipoProcessoDocumento(tipoProcessoDocumentoManager.findByCodigoDocumento(CD_TIPO_DOCUMENTO_INTIMACAO, null));

		//TODO default false processoExpediente.setDocumentoExistente(false);
		//TODO default true processoExpediente.setInTemporario(true);
		//TODO nullable processoExpediente.setDtCriacao(dtCriacao)
		
		SimpleDateFormat sdf = new SimpleDateFormat(MNIParametro.PARAM_FORMATO_DATA_HORA);
		
		for(PessoaExpediente item : expedienteProcessualECNJ.getPessoasExpediente()){
			ProcessoParteExpediente processoParteExpediente = new ProcessoParteExpediente();
			Date dataInicio = sdf.parse(item.getDataInicio().getValue());
			
			Date dataFim = null;
			if(item.getDataFim() != null){
				dataFim = new SimpleDateFormat(MNIParametro.PARAM_FORMATO_DATA_HORA).parse(item.getDataFim().getValue());
			}

			processoParteExpediente.setDtPrazoProcessual(dataInicio);
			processoParteExpediente.setDtPrazoLegal(dataFim);
			processoParteExpediente.setPrazoLegal(item.getPrazo());
			processoParteExpediente.setPessoaCiencia(recuperarPessoaPorNome(processoTrf, item.getPessoa().getNome()));
			processoParteExpediente.setNomePessoaCiencia(item.getPessoa().getNome());
			
			
			//TODO processoParteExpediente.setCienciaSistema(cienciaSistema);
			//TODO processoParteExpediente.setPendenteManifestacao(pendenteManifestacao);
			
			//resposta verificar se somente serão migrados expedientes em aberto
			
			processoParteExpediente.setFechado(dataFim != null);
			processoParteExpediente.setTipoPrazo(TipoPrazoEnum.valueOf(item.getTipoPrazoExpediente().name()));
			
			
			
			processoExpediente.getProcessoParteExpedienteList().add(processoParteExpediente);			
		}
		
		processoExpedienteManager.persist(processoExpediente);

		if (DomicilioEletronicoService.instance().isIntegracaoHabilitada()
				&& ExpedicaoExpedienteEnum.E.equals(processoExpediente.getMeioExpedicaoExpediente())) {
			DomicilioEletronicoService.instance().enviarExpedientesAsync(Arrays.asList(processoExpediente));
		}
	}
	
	private Pessoa recuperarPessoaPorNome(ProcessoTrf processoTrf, String nome){
		
		for(ProcessoParte processoParte : processoTrf.getProcessoParteList()){
			if(processoParte.getPessoa().getNome().equals(nome)){
				return processoParte.getPessoa();
			}
		}
		
		return null;
		
	}

	/**
	 * Converte um objeto do tipo pessoa para o tipo intercomunicaca pessoa.
	 * 
	 * @param pessoa
	 * @return Intercomunicacao pessoa.
	 */
	private br.jus.cnj.intercomunicacao.v222.beans.Pessoa converterPessoaParaIntercomunicacaoPessoa(Pessoa pessoa) {
		PessoaParaIntercomunicacaoPessoaConverter converter = new PessoaParaIntercomunicacaoPessoaConverter();
		return converter.converter(pessoa);
	}
}
