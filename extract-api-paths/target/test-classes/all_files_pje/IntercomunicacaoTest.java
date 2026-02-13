/**
 * IntercomunicacaoTest.java
 * 
 * Data: 28/10/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.util.CodificacaoCertificado;
import br.com.itx.util.ReflectionsUtil;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.intercomunicacao.v222.beans.Assinatura;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoLocal;
import br.jus.cnj.intercomunicacao.v222.beans.AssuntoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.AvisoComunicacaoPendente;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.ComunicacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.DataHora;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Endereco;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeDocumentoIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeGeneroPessoa;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeRepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadeVinculacaoProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadesRelacionamentoPessoal;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoLocal;
import br.jus.cnj.intercomunicacao.v222.beans.MovimentoNacional;
import br.jus.cnj.intercomunicacao.v222.beans.NumeroUnico;
import br.jus.cnj.intercomunicacao.v222.beans.OrgaoJulgador;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.Pessoa;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ProcessoJudicial;
import br.jus.cnj.intercomunicacao.v222.beans.RelacionamentoPessoal;
import br.jus.cnj.intercomunicacao.v222.beans.RepresentanteProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RequisicaoConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaAvisosPendentes;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultarTeorComunicacao;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.TipoQualificacaoPessoa;
import br.jus.cnj.intercomunicacao.v222.beans.VinculacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.servico.Intercomunicacao;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.signerapplet.keystore.ProviderUtil;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe de teste da interface do MNI (Intercomunicacao).
 * 
 * @author adriano.pamplona
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@SuppressWarnings("all")
public abstract class IntercomunicacaoTest {
	
	private static final String P7S = "teste-documento.p7s";
	private static final String HTML = "teste-documento.html";
	private static final String ANEXO1 = "teste-anexo1.pdf";
	private static final String ANEXO2 = "teste-anexo2.pdf";
	private static final String ANEXO3 = "teste-anexo3.pdf";
	private static final String ANEXO4 = "teste-anexo4.pdf";
	
	private static Intercomunicacao endpoint = null;

	protected static final Logger logger = Logger.getLogger(IntercomunicacaoTest.class);
	
	private static final String MNI_IDS_PROCESSO_PARTE_EXPEDIENTE = "mni:idsProcessoParteExpediente";
	private static final String MIME_APPLICATION_PDF = "application/pdf";
	private static final String MIME_APPLICATION_PKCS7 = "application/pkcs7";
	
	//Usuários disponíveis na aplicação do endpoint.
	//TJPB 1g
	private static String USUARIO_LOGIN = "TJPB;09283185000163/120725;admin123";
	private static String USUARIO_POLO_ATIVO = "DAYVISON SOUZA DOS SANTOS;10777525470;admin123";
	private static String USUARIO_POLO_PASSIVO = "LUCIANO JOSE COSTA DA SILVA;02650534478;admin123";
	private static String USUARIO_ADVOGADO_POLO_ATIVO = "ABINOAN FERREIRA DA SILVA;06501709466;admin123";
	private static String USUARIO_ADVOGADO_POLO_PASSIVO = null;
	private static String USUARIO_MAGISTRADO = "Eriberto Calcutá;562.201.777-70";
	private static String USUARIO_DIRETOR_SECRETARIA = "Pegasi Geminorum;376.488.515-71";
	private static String USUARIO_NAO_PRESENTE = "GLEDE;11111111111";
	private static String USUARIO_PROCURADOR = "Procurador Padrão MPGO;mpm;mpm@2310";
	private static String USUARIO_SEM_ENDERECO = "GILBERTO SABINO DA SILVA;524.360.471-20";
	
	//Dados para entrega de manifestação.
	private static final String MANIFESTACAO_LOCALIDADE = "8109";
	private static final Integer MANIFESTACAO_CLASSE = 436;
	private static final Integer MANIFESTACAO_ASSUNTO = 6062;
	private static final Integer MANIFESTACAO_COMPETENCIA = 4;
	private static final String MANIFESTACAO_TIPO_PETICAO_INICIAL = "58";
	private static final String MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER = "8150006";
	
	private static String MANIFESTACAO_LOCALIDADE_INATIVA = "4";
		
	static {
		Logger.getRootLogger().setLevel(Level.ERROR);
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(new ConsoleAppender(new SimpleLayout()));
		logger.removeAllAppenders();
		logger.setLevel(Level.ALL);
		logger.info("Logger configurado com sucesso!");
		
		HttpsURLConnection.setDefaultHostnameVerifier(
			(hostname, sslSession) -> hostname.equals("localhost")
		);
	}
	
	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test1EntregarManifestacaoProcessual() {
		logger.info("test1EntregarManifestacaoProcessual: -------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}

	/**
	 * Teste da consulta de processo.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test2ConsultarProcesso() {
		logger.info("test2ConsultarProcesso: --------------------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		imprimir(respostaMP, false);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(respostaMP.getProtocoloRecebimento());
		
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}

	/**
	 * Teste da consulta de avisos pendentes do advogado.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test3ConsultarAvisosPendentesDoAdvogado() {
		logger.info("test3ConsultarAvisosPendentesDoAdvogado: ---------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		imprimir(respostaMP, false);
		
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));

		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		validarResposta(resposta);
		imprimir(resposta, true);
	}
	
	/**
	 * Teste da consulta de teor de comunicação. Será consultado o primeiro aviso do advogado.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test4ConsultarTeorComunicacaoDoAdvogado() {
		logger.info("test4ConsultarTeorComunicacaoDoAdvogado: ---------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		
		
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));
		
		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		validarResposta(resposta);
		imprimir(resposta, true);
		
		List<AvisoComunicacaoPendente> avisos = resposta.getAviso();
		AvisoComunicacaoPendente aviso = obterExpedienteDoProcesso(avisos, respostaMP.getProtocoloRecebimento());
		
		if (aviso != null) {
			RequisicaoConsultarTeorComunicacao requisicaoCTC = novaRequisicaoConsultarTeorComunicacao(USUARIO_ADVOGADO_POLO_ATIVO);
			requisicaoCTC.setIdentificadorAviso(aviso.getIdAviso());
			
			RespostaConsultarTeorComunicacao respostaCTC = mniConsultarTeorComunicacao(requisicaoCTC);
			validarResposta(respostaCTC);
			imprimir(respostaCTC, true);
		}
	}
	
	/**
	 * Entrega de manifestação como resposta a um expediente. É necessário passar um número de 
	 * processo válido para o teste não falhar.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test5EntregarManifestacaoProcessualComoRespostaDeExpediente() {
		logger.info("test5EntregarManifestacaoProcessualComoRespostaDeExpediente: -------");
		
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		
		Assert.assertNotNull(respostaMP.getRecibo());
		
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));
		
		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		validarResposta(resposta);
		
		List<AvisoComunicacaoPendente> avisos = resposta.getAviso();
		Assert.assertFalse(avisos.isEmpty());
		logger.info(String.format("Há %d avisos.", avisos.size()));
		
		AvisoComunicacaoPendente aviso = obterExpedienteDoProcesso(avisos, respostaMP.getProtocoloRecebimento());
		
		if (aviso != null) {
			RequisicaoConsultarTeorComunicacao requisicaoCTC = novaRequisicaoConsultarTeorComunicacao(USUARIO_ADVOGADO_POLO_ATIVO);
			requisicaoCTC.setIdentificadorAviso(aviso.getIdAviso());
			
			RespostaConsultarTeorComunicacao respostaCTC = mniConsultarTeorComunicacao(requisicaoCTC);
			validarResposta(respostaCTC);
			imprimir(respostaCTC, true);
			
			NumeroUnico nu = new NumeroUnico();
			nu.setValue(respostaMP.getProtocoloRecebimento());
			
			ManifestacaoProcessual manifestacaoResposta = new ManifestacaoProcessual();
			manifestacaoResposta.setIdManifestante(retirarMascaraCpf(
					obterPessoaAdvogadoPoloAtivo().getNumeroDocumentoPrincipal().getValue()));
			manifestacaoResposta.setSenhaManifestante(retirarMascaraCpf(
					obterPessoaAdvogadoPoloAtivo().getNumeroDocumentoPrincipal().getValue()));
			
			manifestacaoResposta.setNumeroProcesso(nu);
			
			DocumentoProcessual documentoResposta = new DocumentoProcessual();
			byte[] p7sBytes = ProjetoUtil.converterParaBytes(obterStream(P7S));
			DataHandler p7sDataHandler = ProjetoUtil.converterParaDataHandler(p7sBytes, MIME_APPLICATION_PKCS7);
			documentoResposta.setConteudo(p7sDataHandler);
			documentoResposta.setMimetype(MIME_APPLICATION_PKCS7);
			documentoResposta.setDataHora(converterParaDataHora(new Date()));
			documentoResposta.setHash(Crypto.encodeSHA256(p7sBytes));
			documentoResposta.setNivelSigilo(0);
			documentoResposta.setTipoDocumento(MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER); //informações
			
			manifestacaoResposta.getDocumento().add(documentoResposta);
			
			Parametro parametro = new Parametro();
			parametro.setNome(MNI_IDS_PROCESSO_PARTE_EXPEDIENTE);
			parametro.setValor(aviso.getIdAviso().getValue());//Separe os id's com ';' caso deseje passar mais de um.
			manifestacaoResposta.getParametros().add(parametro);
			
			RespostaManifestacaoProcessual respostaEXP = mniEntregarManifestacaoProcessual(manifestacaoResposta);
			validarResposta(respostaEXP);
			imprimir(respostaEXP, false);
		}
	}

	/**
	 * Teste da consulta de processo sobre um número de processo específico.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test6ConsultarProcessoEspecifico() {
		logger.info("test6ConsultarProcessoEspecífico: ----------------------------------");
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue("08000039720228150731");
		
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setIdConsultante("82749655153");
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirCabecalho(Boolean.TRUE);
		requisicaoCP.setIncluirDocumentos(Boolean.TRUE);
		requisicaoCP.setMovimentos(Boolean.FALSE);
		
		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}

	/**
	 * Teste para o MPMG: consulta de avisos pendentes, consultar teor (dar ciência) e entrega de manifestação (responder expediente).
	 * 1) verificar se o documento foi anexado ao processo.
	 * 2) o sistema não emite o número do protocolo.
	 * 3) altera a etapa de 'Processo com prazo em curso' para 'Certificar resposta'.
	 * 
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test7TesteGeralUsuarioProcurador() {
		logger.info("test7TesteGeral_UsuarioProcurador: ---------------------------------");

		String usuarioProcurador = USUARIO_PROCURADOR.split(";")[1];
		String senhaProcurador = USUARIO_PROCURADOR.split(";")[2];
		
		Pessoa autoridade = novaPessoa("MINISTÉRIO PÚBLICO DO ESTADO DE MINAS GERAIS", "20971057000145", TipoQualificacaoPessoa.AUTORIDADE);
		
		// CRIANDO UM PROCESSO
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				null,
				autoridade, 
				obterPessoaPoloPassivo(), 
				null, 
				null,
				P7S, 
				1680, 
				10303, 
				"2", 
				2, 
				0, 
				2.0,
				null,
				null,
				0, 
				"58");
		manifestacao.setIdManifestante(usuarioProcurador);
		manifestacao.setSenhaManifestante(senhaProcurador);
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		imprimir(respostaMP, false);
		
		//CONSULTANDO OS EXPEDIENTES PENDENTES
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(USUARIO_PROCURADOR);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));
		
		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		validarResposta(resposta);
		imprimir(resposta, true);
		
		List<AvisoComunicacaoPendente> avisos = resposta.getAviso();
		AvisoComunicacaoPendente aviso = obterExpedienteDoProcesso(avisos, respostaMP.getProtocoloRecebimento());
		
		if (aviso != null) {
			//PEGANDO O PRIMEIRO EXPEDIENTE E CONSULTANDO TEOR PARA REGISTRAR CIÊNCIA
			logger.info(String.format("Teor do aviso %s", aviso.getIdAviso().getValue()));
			
			RequisicaoConsultarTeorComunicacao requisicaoCTC = novaRequisicaoConsultarTeorComunicacao(USUARIO_PROCURADOR);
			requisicaoCTC.setIdentificadorAviso(aviso.getIdAviso());
			
			RespostaConsultarTeorComunicacao respostaCTC = mniConsultarTeorComunicacao(requisicaoCTC);
			validarResposta(respostaCTC);
			imprimir(respostaCTC, true);
	
			//RESPONDENDO O EXPEDIENTE.
			NumeroUnico nu = new NumeroUnico();
			nu.setValue(respostaMP.getProtocoloRecebimento());
			
			ManifestacaoProcessual manifestacaoResposta = novaManifestacaoProcessual(USUARIO_PROCURADOR);
			manifestacaoResposta.setNumeroProcesso(nu);
			
			DocumentoProcessual documentoResposta = new DocumentoProcessual();
			byte[] p7sBytes = ProjetoUtil.converterParaBytes(obterStream(P7S));
			DataHandler p7sDataHandler = ProjetoUtil.converterParaDataHandler(p7sBytes, MIME_APPLICATION_PKCS7);
			documentoResposta.setConteudo(p7sDataHandler);
			documentoResposta.setMimetype(MIME_APPLICATION_PKCS7);
			documentoResposta.setDataHora(converterParaDataHora(new Date()));
			documentoResposta.setHash("aaaaaaaaaa");
			documentoResposta.setNivelSigilo(0);
			documentoResposta.setTipoDocumento("58");
			
			manifestacaoResposta.getDocumento().add(documentoResposta);
			
			Parametro parametro = new Parametro();
			parametro.setNome(MNI_IDS_PROCESSO_PARTE_EXPEDIENTE);
			parametro.setValor(aviso.getIdAviso().getValue());
			manifestacaoResposta.getParametros().add(parametro);
			
			RespostaManifestacaoProcessual respostaExpediente = mniEntregarManifestacaoProcessual(manifestacaoResposta);
			validarResposta(respostaExpediente);
			imprimir(respostaExpediente, false);
		}
	}
	
	/**
	 * Teste de todas as operações para a AGU.
	 * 
	 * @throws RuntimeException
	 */
	@Test
	@Ignore
	public void test8TesteGeralAGU() {
		logger.info("test8TesteGeral_AGU: -----------------------------------------------");
		
		
		final String EDUARDO_LANG = "Procurador Padrão AGU;53568196336;agu@2310";
		final String POLO_PASSIVO = "Coronae Lyrae;008.186.029-37";
		
		logger.info("Entrega de Manifestação ---------------------------------------------");
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(EDUARDO_LANG),
				novaPessoa("INSTITUTO NACIONAL DO SEGURO SOCIAL", "29979036000140", TipoQualificacaoPessoa.AUTORIDADE), 
				novaPessoa(POLO_PASSIVO.split(";")[1], POLO_PASSIVO.split(";")[1], TipoQualificacaoPessoa.FISICA), 
				null, 
				null,
				HTML, 
				11892, 
				11952, 
				"2", 
				2, 
				0, 
				2.0,
				null,
				null,
				0, 
				"58");
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		
		logger.info("Processo: "+ respostaMP.getProtocoloRecebimento());
		logger.info("FIM Entrega de Manifestação -----------------------------------------");
		
		logger.info("Consulta de avisos pendentes ----------------------------------------");
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(EDUARDO_LANG);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));
		
		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		validarResposta(resposta);
		
		List<AvisoComunicacaoPendente> avisos = resposta.getAviso();
		Assert.assertFalse(avisos.isEmpty());
		AvisoComunicacaoPendente aviso = obterExpedienteDoProcesso(avisos, respostaMP.getProtocoloRecebimento());
		
		if (aviso != null) {
			logger.info(String.format("Há %d avisos.", avisos.size()));
			logger.info(String.format("Selecionando aviso: %s", aviso.getIdAviso().getValue()));
			logger.info(" - tipo da comunicação: "+ aviso.getTipoComunicacao().getValue());
			
			logger.info("FIM Consulta de avisos pendentes ------------------------------------");
			
			logger.info("Consulta teor da comunicação ----------------------------------------");
			RequisicaoConsultarTeorComunicacao requisicaoCTC = novaRequisicaoConsultarTeorComunicacao(EDUARDO_LANG);
			requisicaoCTC.setIdentificadorAviso(aviso.getIdAviso());
			
			RespostaConsultarTeorComunicacao respostaCTC = mniConsultarTeorComunicacao(requisicaoCTC);
			validarResposta(respostaCTC);
	
			logger.info("Registrando ciência no aviso: "+ aviso.getIdAviso().getValue());
			for (ComunicacaoProcessual comunicacao : respostaCTC.getComunicacao()) {
				logger.info("-----> ID: " + comunicacao.getId().getValue());
				logger.info(comunicacao.getProcesso());
				logger.info(comunicacao.getTipoComunicacao().getValue());
				logger.info("Documentos: " + comunicacao.getDocumento().size());
				for (DocumentoProcessual documento : comunicacao.getDocumento()) {
					logger.info("- documento: "+ documento.getIdDocumento());
				}
			}
			logger.info("FIM Consulta teor da comunicação ------------------------------------");
			
			logger.info("Responder expediente ------------------------------------------------");
			NumeroUnico nu = new NumeroUnico();
			nu.setValue(respostaMP.getProtocoloRecebimento());
			
			ManifestacaoProcessual manifestacaoResposta = novaManifestacaoProcessual(EDUARDO_LANG);
			manifestacaoResposta.setNumeroProcesso(nu);
			
			DocumentoProcessual documentoResposta = new DocumentoProcessual();
			byte[] p7sBytes = ProjetoUtil.converterParaBytes(obterStream(P7S));
			DataHandler p7sDataHandler = ProjetoUtil.converterParaDataHandler(p7sBytes, MIME_APPLICATION_PKCS7);
			documentoResposta.setConteudo(p7sDataHandler);
			documentoResposta.setMimetype(MIME_APPLICATION_PKCS7);
			documentoResposta.setDataHora(converterParaDataHora(new Date()));
			documentoResposta.setHash(Crypto.encodeSHA256(p7sBytes));
			documentoResposta.setNivelSigilo(0);
			documentoResposta.setTipoDocumento("58");
			
			manifestacaoResposta.getDocumento().add(documentoResposta);
			
			Parametro parametro = new Parametro();
			parametro.setNome(MNI_IDS_PROCESSO_PARTE_EXPEDIENTE);
			parametro.setValor(aviso.getIdAviso().getValue());
			manifestacaoResposta.getParametros().add(parametro);
			
			RespostaManifestacaoProcessual respostaEXP = mniEntregarManifestacaoProcessual(manifestacaoResposta);
			validarResposta(respostaEXP);
			
			logger.info("Resposta de expediente OK, expediente: "+ aviso.getIdAviso().getValue());
			logger.info("FIM Responder expediente --------------------------------------------");
		}
	}
	
	/**
	 * Teste da entrega de manifestação processual avulsa.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test9EntregarManifestacaoProcessualAvulsa() {
		logger.info("test9EntregarManifestacaoProcessualAvulsa: -------------------------");

		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		
		ManifestacaoProcessual manifestacaoAvulsa = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				"11111111111",
				"22222222222",
				0, 
				MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER); //informações
		manifestacaoAvulsa.setDadosBasicos(null);
		manifestacaoAvulsa.setNumeroProcesso(nu);
		RespostaManifestacaoProcessual respostaAvulsa = mniEntregarManifestacaoProcessual(manifestacaoAvulsa);
		validarResposta(respostaAvulsa);
		imprimir(respostaAvulsa, false);
				
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}

	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test10EntregarManifestacaoProcessualComAnexos() {
		logger.info("test10EntregarManifestacaoProcessualComAnexos: ---------------------");
			
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		DocumentoProcessual documento = novoDocumentoProcessual(
				P7S, 
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL, 
				null, 
				null);
		manifestacao.getDocumento().add(documento);
		
		DocumentoProcessual peticao1 = manifestacao.getDocumento().get(0);
		peticao1.getDocumentoVinculado().add(novoDocumentoProcessual(ANEXO1, 0, MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER, null, null));
		peticao1.getDocumentoVinculado().add(novoDocumentoProcessual(ANEXO2, 0, MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER, null, null));
		
		DocumentoProcessual peticao2 = manifestacao.getDocumento().get(1);
		peticao2.getDocumentoVinculado().add(novoDocumentoProcessual(ANEXO3, 0, MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER, null, null));
		peticao2.getDocumentoVinculado().add(novoDocumentoProcessual(ANEXO4, 0, MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER, null, null));
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(Boolean.TRUE);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}

	/**
	 * Faz uma entrega de manifestação sem documentos, deverá ser lançado um erro.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test11EntregarManifestacaoProcessualSemDocumentos() {
		logger.info("test11EntregarManifestacaoProcessualSemDocumentos: -----------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.getDocumento().clear();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals("Documentação não informada.", resposta.getMensagem());
	}

	/**
	 * Teste da consulta de avisos pendentes do advogado, deverá ser executado com sucesso, 
	 * porém sem avisos.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test12ConsultarAvisosPendentesDoAdvogadoRetornandoListaVazia() {
		logger.info("test12ConsultarAvisosPendentesDoAdvogadoRetornandoListaVazia: ------");
		
		final String USUARIO = "Coronae Lyrae;00818602937";
		RequisicaoConsultaAvisosPendentes requisicaoCAP = novaRequisicaoConsultaAvisosPendentes(USUARIO);
		requisicaoCAP.setDataReferencia(converterParaDataHora(novaData(1, 1, 2000)));

		RespostaConsultaAvisosPendentes resposta = mniConsultarAvisosPendentes(requisicaoCAP);
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getAviso().isEmpty());
		Assert.assertTrue(resposta.getMensagem().equals("Não foram encontrados expedientes pendentes de ciência."));
	}

	/**
	 * Faz uma entrega de manifestação sem petição inicial, deverá ser lançado um erro.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test13EntregarManifestacaoProcessualSemPeticao() {
		logger.info("test13EntregarManifestacaoProcessualSemPeticao: --------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		DocumentoProcessual documento = manifestacao.getDocumento().get(0);
		documento.setTipoDocumento("2000015");//expediente
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals("Erro ao autuar processo: Não há petição inicial anexada ao processo.", 
				resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual com polos duplicados.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test14EntregarManifestacaoProcessualComPolosDuplicados() {
		logger.info("test14EntregarManifestacaoProcessualComPolosDuplicados: ------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloAtivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				1680, 
				10303,
				"2", 
				2, 
				0, 
				2.0,
				"11111111111",
				"22222222222",
				0, 
				"58");
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Duplicação de polos não é permitida: o documento %s está "
				+ "registrado para as pessoas '%s' e '%s'.";
		mensagem = String.format(
				mensagem, 
				obterPessoaPoloAtivo().getNumeroDocumentoPrincipal().getValue(),
				obterPessoaPoloAtivo().getNome(),
				obterPessoaPoloAtivo().getNome());
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual com tipo de documento não permitido para o 
	 * usuário da requisição.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test15EntregarManifestacaoProcessualComTipoDocumentoNaoPermitido() {
		logger.info("test15EntregarManifestacaoProcessualComTipoDocumentoNaoPermitido: --");
		
		//MANIFESTAÇÃO INICIAL + PETIÇÃO INICIAL = OK
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		Assert.assertNotNull(resposta.getRecibo());
		
		//MANIFESTAÇÃO AVULSA COM PETIÇÃO INICIAL = ERRO
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.setDadosBasicos(null);
		manifestacao.setNumeroProcesso(nu);
		
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		String mensagem = "Tipo de documento inválido para esta manifestação:";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
		
		//PETIÇÃO INICIAL + ANEXO PETIÇÃO INICIAL = ERRO
		manifestacao = novaManifestacaoProcessualPadrao();
		DocumentoProcessual anexo = novoDocumentoProcessual(
				P7S, 
				0, 
				"58", 
				null, 
				null);
		manifestacao.getDocumento().get(0).getDocumentoVinculado().add(anexo);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		mensagem = "Tipo de documento inválido para esta manifestação:";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}

	/**
	 * Teste da entrega de manifestação processual com pessoa relacionada.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test16EntregarManifestacaoProcessualComPessoaRelacionada() {
		logger.info("test16EntregarManifestacaoProcessualComPessoaRelacionada: ----------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		Pessoa pessoaRelacionamento = new Pessoa();
		pessoaRelacionamento.setNome("Fulano de Tal");
		pessoaRelacionamento.setTipoPessoa(TipoQualificacaoPessoa.FISICA);
		pessoaRelacionamento.setSexo(ModalidadeGeneroPessoa.M);

		RelacionamentoPessoal relacionamentoPessoal = new RelacionamentoPessoal();
		relacionamentoPessoal.setPessoa(pessoaRelacionamento);
		relacionamentoPessoal.setModalidadeRelacionamento(ModalidadesRelacionamentoPessoal.T); //tutor --> Essa relação não existe no BD.
		
		PoloProcessual poloAtivo = manifestacao.getDadosBasicos().getPolo().get(0);
		Parte parteAtivo = poloAtivo.getParte().get(0);
		Pessoa pessoa = parteAtivo.getPessoa();
		pessoa.getPessoaRelacionada().add(relacionamentoPessoal);
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Tipo de relação pessoal \"TUT\" não cadastrado para o tipo de pessoa Física";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
		
		//execução com sucesso
		relacionamentoPessoal.setModalidadeRelacionamento(ModalidadesRelacionamentoPessoal.C); //curador --> Essa relação existe no BD.
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual avulsa por pessoa não associada ao processo.
	 * A requisição deverá ser executada com sucesso e um registro a mais deverá ser inserido na
	 * tabela tb_proc_doc_ptcao_nao_lida.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test17EntregarManifestacaoProcessualDePessoaNaoAssociada() {
		logger.info("test17EntregarManifestacaoProcessualDePessoaNaoAssociada: ----------");

		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		
		ManifestacaoProcessual manifestacaoAvulsa = novaManifestacaoProcessualPadrao();
		
		manifestacaoAvulsa.setDadosBasicos(null);
		manifestacaoAvulsa.setNumeroProcesso(nu);
		RespostaManifestacaoProcessual respostaAvulsa = mniEntregarManifestacaoProcessual(manifestacaoAvulsa);
		validarResposta(respostaAvulsa);
		imprimir(respostaAvulsa, false);

		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual com jurisdição inativa.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test18EntregarManifestacaoProcessualComJurisdicaoInativa() {
		logger.info("test18EntregarManifestacaoProcessualComJurisdicaoInativa: ----------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.getDadosBasicos().setCodigoLocalidade(MANIFESTACAO_LOCALIDADE_INATIVA);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue((
				resposta.getMensagem().startsWith("Jurisdição") && 
				resposta.getMensagem().endsWith("inativa")));
	}
	
	/**
	 * Teste da entrega de manifestação processual com classe que exige processo de referência.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test19EntregarManifestacaoProcessualComClasseQueExigeProcessoDeRef() {
		logger.info("test19EntregarManifestacaoProcessualComClasseQueExigeProcessoDeRef: ");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				46, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "A classe judicial escolhida exige número do processo referência/originário";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}
	
	/**
	 * Teste da entrega de manifestação processual com classe que exige numeração própria.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test20EntregarManifestacaoProcessualComClasseQueExigeNumeracaoPropria() {
		logger.info("test20EntregarManifestacaoProcessualComClasseQueExigeNumeracaoPropria:");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				1301, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Para esta manifestação (classe judicial exige numeração própria), deve-se informar o número do Processo Judicial";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}

	/**
	 * Teste da entrega de manifestação processual com classe que não permite a protocolação por
	 * jus postulandi.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test21EntregarManifestacaoProcessualPorJusPostulandi() {
		logger.info("test21EntregarManifestacaoProcessualPorJusPostulandi: --------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				null, 
				null,
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "A classe judicial escolhida não permite Jus Postulandi como manifestante sem os dados do representante processual.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual por jus postulandi onde o mesmo não se encontra 
	 * no polo ativo.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test22EntregarManifestacaoProcessualPorJusPostulandiOndeNaoEhPoloAtivo() {
		logger.info("test22EntregarManifestacaoProcessualPorJusPostulandiOndeNaoEhPoloAtivo: ");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_POLO_ATIVO),
				obterPessoaPoloPassivo(), 
				obterPessoaPoloAtivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "O jus postulandi \"%s\" deve ser o único integrante do polo ativo";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(
				String.format(mensagem, obterPessoaPoloAtivo().getNome()), 
				resposta.getMensagem());
		
		//Duas pessoas no polo ativo.
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_POLO_ATIVO),
				obterPessoaPoloPassivo(), 
				obterPessoaPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				null, 
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		manifestacao.getDadosBasicos().getPolo().add(
				novoPoloProcessual(obterPessoaAdvogadoPoloAtivo(), null, ModalidadePoloProcessual.AT));
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		mensagem = "O jus postulandi \"%s\" deve ser o único integrante do polo ativo";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(
				String.format(mensagem, obterPessoaPoloAtivo().getNome()), 
				resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual sem polo ativo.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test23EntregarManifestacaoProcessualSemPoloAtivo() {
		logger.info("test23EntregarManifestacaoProcessualSemPoloAtivo: ------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				null, 
				obterPessoaPoloPassivo(), 
				null, 
				null,
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Erro ao autuar processo: Deve haver ao menos uma parte no polo ativo vinculada ao processo.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual com endereço desconhecido no polo ativo.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test24EntregarManifestacaoProcessualComEnderecoDesconhecido() {
		logger.info("test24EntregarManifestacaoProcessualComEnderecoDesconhecido: -------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaSemEndereco(), //obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(),
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Selecione ao menos um endereço para a pessoa '%s'.";
		mensagem = String.format(
				mensagem, 
				obterPessoaSemEndereco().getNome());
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação processual com complemento de classe obrigatório.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test25EntregarManifestacaoProcessualComComplementoClasseObrigatorio() {
		logger.info("test25EntregarManifestacaoProcessualComComplementoClasseObrigatorio:");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				238, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "O complemento 'nr_teste' da classe 'AVOCATÓRIA' exige um valor obrigatório.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
		
		manifestacao.getParametros().add(novoParametro("nr_teste", "teste"));
		manifestacao.getParametros().add(novoParametro("data_teste", "18/02/2015 15:47:01"));
		manifestacao.getParametros().add(novoParametro("cpf_teste", "52003183191"));
		manifestacao.getParametros().add(novoParametro("cnpj_teste", "04.126.371/0001-75"));
		manifestacao.getParametros().add(novoParametro("xpto", "xxx"));
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}

	/**
	 * Teste da entrega de manifestação processual com documento sem assinatura.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test26EntregarManifestacaoProcessualComDocumentoSemAssinatura() {
		logger.info("test26EntregarManifestacaoProcessualComDocumentoSemAssinatura: -----");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), //obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(),
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				HTML, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		manifestacao.getDocumento().get(0).unsetAssinatura();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Documento '58-Petição inicial' sem assinatura, não é possível juntar documento sem assinatura.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
	}

	/**
	 * Teste da entrega de manifestação para validar as regras 'Permite ente ou autoridade'.
	 * 
	 * <pre>
	 * SE usuário interno ENTÃO
	 * 	SE 'exige ente ou autoridade' E 'não tem autoridade' ENTÃO
	 * 		erro "Erro ao protocolar processo: a classe judicial escolhida 'Exige ente ou autoridade' no polo ativo ou passivo."
	 * 	FIM SE
	 * SENÃO
	 * 	SE 'existe autoridade polo ativo' ENTÃO
	 * 		erro "Erro ao protocolar processo: a classe judicial escolhida não 'Permite ente ou autoridade' no polo ativo."
	 * 	SENÃO SE 'existe autoridade polo passivo' E (NÃO 'permite ente ou autoridade' E NÃO 'exige ente ou autoridade') ENTÃO
	 * 		erro "Erro ao protocolar processo: a classe judicial escolhida não 'Permite ente ou autoridade' no polo ativo ou passivo."
	 * 	FIM SE
	 * FIM SE
	 * </pre>
	 * 
	 * @throws RuntimeException
	 */
	@Test
	public void test27EntregarManifestacaoProcessualComEnteOuAutoridade() {
		logger.info("test27EntregarManifestacaoProcessualComEnteOuAutoridade: -----------");

		Pessoa autoridade = novaPessoa("AUTORIDADE(1) TEST CASE - 27", "", TipoQualificacaoPessoa.AUTORIDADE);
		Pessoa pessoaVinculada = novaPessoa("MPDFT", "08212332000141", TipoQualificacaoPessoa.JURIDICA);
		autoridade.getEndereco().add(novoEndereco(0));
		autoridade.setPessoaVinculada(pessoaVinculada);

		// USUÁRIO INTERNO + CLASSE QUE EXIGE AUTORIDADE + SEM AUTORIDADE = ERRO
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_DIRETOR_SECRETARIA), 
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				null, 
				null,
				P7S, 
				92, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);

		String mensagem = "Erro ao protocolar processo: a classe judicial escolhida 'Exige ente ou autoridade' no polo ativo ou passivo.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());

		// USUÁRIO EXTERNO + AUTORIDADE NO POLO ATIVO = ERRO
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO), 
				autoridade, 
				obterPessoaPoloPassivo(), 
				null, 
				null,
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);

		mensagem = "Erro ao protocolar processo: a classe judicial escolhida não 'Permite ente ou autoridade' no polo ativo.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());

		// USUÁRIO EXTERNO + AUTORIDADE NO POLO PASSIVO + CLASSE QUE NÃO PERMITE OU EXIGE AUTORIDADE = ERRO
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO), 
				obterPessoaPoloAtivo(), 
				autoridade, 
				null, 
				null,
				P7S, 
				92, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		mensagem = "Erro ao protocolar processo: a classe judicial escolhida não 'Permite ente ou autoridade' no polo ativo ou passivo.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());

		// USUÁRIO EXTERNO (PROCURADOR QUE HERDA ADVOGADO) + AUTORIDADE NO POLO PASSIVO + CLASSE QUE NÃO PERMITE OU EXIGE AUTORIDADE = ERRO
		manifestacao = novaManifestacaoProcessual(
				null, 
				obterPessoaPoloAtivo(), 
				autoridade, 
				null,
				null,
				P7S, 
				1680, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		manifestacao.setIdManifestante(USUARIO_PROCURADOR.split(";")[1]);
		manifestacao.setSenhaManifestante(USUARIO_PROCURADOR.split(";")[2]);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		mensagem = "Erro ao protocolar processo: a classe judicial escolhida não 'Permite ente ou autoridade' no polo ativo ou passivo.";
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());

		// USUÁRIO INTERNO + AUTORIDADE NO POLO PASSIVO = OK
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_DIRETOR_SECRETARIA), 
				obterPessoaPoloAtivo(), 
				autoridade, 
				null, 
				null,
				P7S, 
				MANIFESTACAO_CLASSE, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA, 
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		
		Assert.assertNotNull(resposta.getRecibo());
		
	}

	/**
	 * Teste da consulta de processo com processo vinculado (Associado).
	 * 
	 * @throws Exception
	 */
	@Test
	public void test28ConsultarProcessoComProcessoVinculado() {
		logger.info("test28ConsultarProcessoPorNumero: --------------------------------------------");
		
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		
		manifestacao = novaManifestacaoProcessualPadrao();
		VinculacaoProcessual vinculacao = new VinculacaoProcessual();
		vinculacao.setNumeroProcesso(nu);
		vinculacao.setVinculo(ModalidadeVinculacaoProcesso.AR);
		
		manifestacao.getDadosBasicos().getProcessoVinculado().add(vinculacao);
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		
		nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());

		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test29EntregarManifestacaoProcessualComVariasModalidadesDocumentoIdentificacao() {
		logger.info("test29EntregarManifestacaoProcessualComVariasModalidadesDocumentoIdentificacao:");
		
		//Montando o polo ativo com alguns tipos de documento
		Pessoa pessoaPoloAtivo = obterPessoaPoloAtivo();
		
		DocumentoIdentificacao cn = new DocumentoIdentificacao();
		cn.setNome(pessoaPoloAtivo.getNome());
		cn.setTipoDocumento(ModalidadeDocumentoIdentificador.CN);
		cn.setCodigoDocumento("CN_8596");
		cn.setEmissorDocumento("Cartorio XPTO");		
		pessoaPoloAtivo.getDocumento().add(cn);

		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				pessoaPoloAtivo, 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual com documento fora do tamanho permitido.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test30EntregarManifestacaoProcessualComDocumentoDeTamanhoInvalido() {
		logger.info("test30EntregarManifestacaoProcessualComDocumentoDeTamanhoInvalido:");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				"teste-documento-tamanho-nok.pdf", 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		logger.info(resposta.getMensagem());
		Assert.assertTrue(resposta.getMensagem().endsWith("bytes."));
	}
	
	/**
	 * Teste da entrega de manifestação processual com mimetype não permitido.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test31EntregarManifestacaoProcessualComDocumentoDeMimetypeInvalido() {
		logger.info("test31EntregarManifestacaoProcessualComDocumentoDeMimetypeInvalido:");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				"documento-teste-tamanho-tipo-nok.docx", 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		Assert.assertTrue(resposta.getMensagem().endsWith("não é permitido."));
	}
	
	/**
	 * Teste da consulta de processo retornando todos os documentos com binários.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test32ConsultarProcessoRetornandoTodosBinarios() {
		logger.info("test32ConsultarProcessoRetornandoTodosBinarios: --------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.getDocumento().add(novoDocumentoProcessual(ANEXO1, 0, "2000003", null, null));
		manifestacao.getDocumento().add(novoDocumentoProcessual(ANEXO2, 0, "2000003", null, null));
		
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(respostaMP.getProtocoloRecebimento());
		
		//CONSULTANDO SOMENTE OS METADADOS
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(Boolean.TRUE);
		requisicaoCP.setMovimentos(Boolean.TRUE);
		
		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
		
		for (DocumentoProcessual documento : respostaCP.getProcesso().getDocumento()) {
			Assert.assertNotNull(documento);
			
			logger.info("- documento: "+ documento.getIdDocumento());
			
			byte[] bytes = ProjetoUtil.converterParaBytes(documento.getConteudo());
			boolean temBinario = (bytes != null && bytes.length > 0);
			Assert.assertFalse(temBinario);
			logger.info("  - tem binário: "+ temBinario);
		}
		
		//CONSULTANDO TODOS OS DOCUMENTOS COM BINÁRIOS
		requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(Boolean.TRUE);
		requisicaoCP.setMovimentos(Boolean.TRUE);
		//Atributo responsável em retornar o binário de todos os documentos.
		requisicaoCP.getDocumento().add("*");
				
		respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, true);
	}
	
	/**
	 * Teste de entrega de manifestação com documento de código alfanumérico.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test33EntregarManifestacaoProcessualComTipoDeDocumentoAlfanumerico() {
		logger.info("test33EntregarManifestacaoProcessualComTipoDeDocumentoAlfanumerico: --------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.getDocumento().add(novoDocumentoProcessual(ANEXO1, 0, "200000x", null, null));
		
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
	}
	
	/**
	 * Teste de entrega de manifestação com documento de código alfanumérico.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test34EntregarManifestacaoPoloSemDocumento() {
		logger.info("test34EntregarManifestacaoPoloSemDocumento: ------------------------");
		
		Pessoa poloAtivo = obterPessoaPoloAtivo();
		poloAtivo.getDocumento().clear();
		poloAtivo.setNumeroDocumentoPrincipal(null);
		
		//teste com polo sem documento de identificação.
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				poloAtivo, 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "Documento de identificação não informado para parte 'Camelopardalis Mebsuta.'";
		Assert.assertTrue(respostaMP.isSucesso());
		Assert.assertEquals(mensagem, respostaMP.getMensagem());
		
		//teste com o documento principal não batendo com um documento da lista.
		CadastroIdentificador identificador = new CadastroIdentificador();
		identificador.setValue("00000");

		poloAtivo = obterPessoaPoloAtivo();
		poloAtivo.setNumeroDocumentoPrincipal(identificador);
		
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				poloAtivo, 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		
		mensagem = "Documento de identificação da parte 'Camelopardalis Mebsuta' não confere com o código do documento principal. É preciso preencher o atributo 'pessoa.numeroDocumentoPrincipal' com um valor existente na lista de atributos de 'pessoa.documento'.";
		Assert.assertTrue(respostaMP.isSucesso());
		Assert.assertEquals(mensagem, respostaMP.getMensagem());
		
		//teste com a pessoa somente com numeroDocumentoPrincipal
		
		poloAtivo = obterPessoaPoloAtivo();
		poloAtivo.setNumeroDocumentoPrincipal(null);
		
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				poloAtivo, 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		
		mensagem = "Documento de identificação da parte 'Camelopardalis Mebsuta' não confere com o código do documento principal. É preciso preencher o atributo 'pessoa.numeroDocumentoPrincipal' com um valor existente na lista de atributos de 'pessoa.documento'.";
		Assert.assertTrue(respostaMP.isSucesso());
		Assert.assertEquals(mensagem, respostaMP.getMensagem());
	}

	/**
	 * Consulta de processo para validar os acessos ao processo e aos documentos.
	 * Pré requisitos:
	 * 1) Processo sem segredo e com 2 documentos
	 * 2) Processo sem segredo, 2 documentos sendo que 1 é sigiloso
	 * 3) Processo com segredo, 3 documentos sendo que 1 é sigiloso
	 * @throws Exception
	 */
	@Test
	public void test35AcessosNaConsultaDeProcesso() {
		logger.info("test35AcessosNaConsultaDeProcesso: ---------------------------------");
		
		final String ADVOGADO_POLO_ATIVO = "ADRIANO PAMPLONA;82749655153;admin123";
		final String PROCURADOR = "CLAUDIA PAIVA CARVALHO;01959072137;admin123";
		final String USUARIO_NAO_RELACIONADO = "LARISSA BONI VALIERIS;36912153860;admin123";
		
		//processo sem segredo e com 2 documentos
		final String PROCESSO_SEM_SEGREDO_JUSTICA = "00028148720152000000";
		//processo sem segredo, 2 documentos sendo que 1 é sigiloso
		final String PROCESSO_SEM_SEGREDO_JUSTICA_COM_DOC_SIGILOSO = "00028130520152000000";
		//processo com segredo, 4 documentos sendo que 1 é sigiloso para XXXXX
		final String PROCESSO_COM_SEGREDO_JUSTICA = "00028157220152000000";
		
		//----- TESTE 1
		//Processo SEM segredo de justiça
		//Usuário sem relação com o processo
		//Deve retornar normalmente
		logger.info("teste 1");
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(PROCESSO_SEM_SEGREDO_JUSTICA);
		
		RequisicaoConsultaProcesso requisicao = novaRequisicaoConsultaProcesso(USUARIO_NAO_RELACIONADO);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		
		//----- TESTE 2
		//Processo SEM segredo de justiça, com 2 documentos sendo que 1 é sigiloso
		//Usuário sem relação com o processo
		//Deve retornar o processo e 1 documento
		logger.info("teste 2");
		nu.setValue(PROCESSO_SEM_SEGREDO_JUSTICA_COM_DOC_SIGILOSO);
		requisicao = novaRequisicaoConsultaProcesso(USUARIO_NAO_RELACIONADO);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		Assert.assertEquals(1, resposta.getProcesso().getDocumento().size());
		
		//----- TESTE 3
		//Processo SEM segredo de justiça, com 2 documentos sendo que 1 é sigiloso
		//Usuário sem relação com o processo
		//Deve retornar o processo e 1 documento
		logger.info("teste 3");
		nu.setValue(PROCESSO_SEM_SEGREDO_JUSTICA_COM_DOC_SIGILOSO);
		requisicao = novaRequisicaoConsultaProcesso(USUARIO_NAO_RELACIONADO);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		Assert.assertEquals(1, resposta.getProcesso().getDocumento().size());
		
		//----- TESTE 4
		//Processo SEM segredo de justiça, com 2 documentos sendo que 1 é sigiloso
		//Usuário procurador sem relação com o processo.
		//Não deve retornar o processo com 2 documento.
		logger.info("teste 4");
		nu.setValue(PROCESSO_SEM_SEGREDO_JUSTICA_COM_DOC_SIGILOSO);
		requisicao = novaRequisicaoConsultaProcesso(PROCURADOR);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		Assert.assertEquals(1, resposta.getProcesso().getDocumento().size());
		
		//----- TESTE 5
		//Processo COM segredo de justiça, com 3 documentos sendo que 1 é sigiloso
		//Usuário sem relação com o processo
		//Não deve retornar o processo.
		logger.info("teste 5");
		nu.setValue(PROCESSO_COM_SEGREDO_JUSTICA);
		requisicao = novaRequisicaoConsultaProcesso(USUARIO_NAO_RELACIONADO);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		String mensagem = String.format("Processo de número %s não encontrado!", PROCESSO_COM_SEGREDO_JUSTICA);
		Assert.assertTrue(resposta.isSucesso());
		Assert.assertEquals(mensagem, resposta.getMensagem());
		
		//----- TESTE 6
		//Processo COM segredo de justiça, com 3 documentos sendo que 1 é sigiloso
		//Usuário advogado da parte ativa
		//Não deve retornar o processo e os 3 documentos.
		logger.info("teste 6");
		nu.setValue(PROCESSO_COM_SEGREDO_JUSTICA);
		requisicao = novaRequisicaoConsultaProcesso(ADVOGADO_POLO_ATIVO);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		Assert.assertEquals(4, resposta.getProcesso().getDocumento().size());
		
		//----- TESTE 7
		//Processo COM segredo de justiça, com 4 documentos sendo que 1 é sigiloso
		//Usuário procurador não relacionado com o processo, porém foi emitido uma intimação para a 
		//	entidade representada pela procuradoria do procurador do login.
		//Não deve retornar o processo e os 3 documentos.
		logger.info("teste 7");
		nu.setValue(PROCESSO_COM_SEGREDO_JUSTICA);
		requisicao = novaRequisicaoConsultaProcesso(PROCURADOR);
		requisicao.setNumeroProcesso(nu);
		requisicao.setIncluirCabecalho(Boolean.TRUE);
		requisicao.setIncluirDocumentos(Boolean.TRUE);
		requisicao.setMovimentos(Boolean.TRUE);
		
		resposta = mniConsultarProcesso(requisicao);
		validarResposta(resposta);
		imprimir(resposta, false);
		Assert.assertEquals(3, resposta.getProcesso().getDocumento().size());
	}
	
	/**
	 * Teste da entrega de manifestação processual com classe inativa.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test36EntregarManifestacaoProcessualComClasseInativa() {
		logger.info("test36EntregarManifestacaoProcessualComClasseInativa: ");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "A classe judicial escolhida está inativa na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.";
		Assert.assertFalse(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}
	
	/**
	 * Teste da entrega de manifestação processual com classe sem competência.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test37EntregarManifestacaoProcessualComClasseSemCompetencia() {
		logger.info("test37EntregarManifestacaoProcessualComClasseSemCompetencia: ");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin("Ney Robson Pereira Medeiros;53607104468"),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		manifestacao.setSenhaManifestante("admin123");
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "A classe judicial escolhida não pertence a nenhuma competência na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.";
		Assert.assertFalse(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}
	
	/**
	 * Teste da entrega de manifestação processual com classe sem fluxo associado.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test38EntregarManifestacaoProcessualComClasseSemFluxoAssociado() {
		logger.info("test38EntregarManifestacaoProcessualComClasseSemFluxoAssociado: ");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				1231, 
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		
		String mensagem = "A classe judicial escolhida não pertence a nenhum rito processual na jurisdição/localidade escolhida e não pode ser utilizada para protocolo.";
		Assert.assertFalse(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}
	
	/**
	 * Teste da entrega de manifestação processual com endereço.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test39EntregarManifestacaoProcessualComEndereco() {
		logger.info("test39EntregarManifestacaoProcessualComEndereco: -------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();

		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(resposta.getProtocoloRecebimento());
		
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}

	/**
	 * Teste da entrega de manifestação processual com documento inexistente.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test40EntregarManifestacaoProcessualComTipoDocumentoInexistente() {
		logger.info("test40EntregarManifestacaoProcessualComTipoDocumentoInexistente: ---");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		DocumentoProcessual peticao = manifestacao.getDocumento().get(0);
		peticao.getDocumentoVinculado().add(novoDocumentoProcessual(P7S, 0, "500", null, null));
		peticao.getDocumentoVinculado().add(novoDocumentoProcessual(P7S, 0, "500", null, null));
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		String mensagem = "O Tipo de documento 500 não existe ou está inativo na instalação do PJe de destino, erro: null.";
		Assert.assertFalse(resposta.isSucesso());
		Assert.assertTrue(resposta.getMensagem().startsWith(mensagem));
	}

	/**
	 * Teste da entrega de manifestação processual incidental.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test41EntregarManifestacaoProcessualIncidental() {
		logger.info("test41EntregarManifestacaoProcessualIncidental: -------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		manifestacao.getParametros().add(novoParametro(MNIParametro.isProcessoIncidental(), "true"));
		manifestacao.getParametros().add(novoParametro(MNIParametro.getNumeroUnicoProcessoOriginario(), "0000030-06.2016.2.00.0000"));

		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}

	/**
	 * Teste da entrega de manifestação com autoridade no polo passivo.
	 * 
	 * @throws RuntimeException
	 */
	@Test
	public void test42EntregarManifestacaoProcessualPorProcurador() {
		logger.info("test42EntregarManifestacaoProcessualPorProcurador: -----------------");

		String cpfProcurador = USUARIO_PROCURADOR.split(";")[1];
		String senhaProcurador = USUARIO_PROCURADOR.split(";")[2];
		
		Pessoa autoridade = novaPessoa("ASSOJAF", "06.012.238/0001-13", TipoQualificacaoPessoa.AUTORIDADE);
		autoridade.setPessoaVinculada(novaPessoa(
				"ASSOCIAÇÃO DOS OFICIAIS DE JUSTIÇA AVALIADORES FEDERAIS DA JUSTIÇA DO TRABALHO DA 15ª REGIÃO - ASSOJAF - 15", 
				"06.012.238/0001-13", 
				TipoQualificacaoPessoa.JURIDICA));
		Pessoa poloAtivo = obterPessoaPoloAtivo();
		poloAtivo.getEndereco().add(novoEndereco(0));
		
		// CRIANDO UM PROCESSO
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_PROCURADOR),
				poloAtivo, 
				autoridade,
				null,//novoRepresentanteProcessual(nomeProcurador, cpfProcurador, ModalidadeRepresentanteProcessual.P),
				null,
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		manifestacao.setIdManifestante(retirarMascaraCpf(cpfProcurador)+"/23748");//localização do procurador
		manifestacao.setSenhaManifestante(senhaProcurador);
		manifestacao.getParametros().add(novoParametro(MNIParametro.getIdOrgaoRepresentacao(), "03.507.415/0001-44"));
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		imprimir(respostaMP, false);
	}
	
	/**
	 * Teste de entrega de manifestação processual e consulta com terceiro interessado.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test43EntregarManifestacaoProcessualComTerceiroInteressado() {
		logger.info("test43EntregarManifestacaoProcessualComTerceiroInteressado: --------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		Pessoa terceiro = novaPessoa("MARIA CRISTINA IRIGOYEN PEDUZZI", "144.418.291-91", TipoQualificacaoPessoa.FISICA);
		PoloProcessual polo = novoPoloProcessual(terceiro, null, ModalidadePoloProcessual.VI);
		manifestacao.getDadosBasicos().getPolo().add(polo);
		RespostaManifestacaoProcessual respostaMP = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(respostaMP);
		imprimir(respostaMP, false);
		
		NumeroUnico nu = new NumeroUnico();
		nu.setValue(respostaMP.getProtocoloRecebimento());
		
		RequisicaoConsultaProcesso requisicaoCP = novaRequisicaoConsultaProcesso(USUARIO_ADVOGADO_POLO_ATIVO);
		requisicaoCP.setNumeroProcesso(nu);
		requisicaoCP.setIncluirDocumentos(true);
		requisicaoCP.setMovimentos(Boolean.TRUE);

		RespostaConsultaProcesso respostaCP = mniConsultarProcesso(requisicaoCP);
		validarResposta(respostaCP);
		imprimir(respostaCP, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual com assunto nacional e local.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test44EntregarManifestacaoProcessualComAssuntoLocal() {
		logger.info("test44EntregarManifestacaoProcessualComAssuntoLocal: ---------------");
		
		//Teste com 'codigoNacional'
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				10303,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		
		//Teste com 'codigoLocal'
		manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_ADVOGADO_POLO_ATIVO),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(), 
				obterAdvogadoPoloPassivo(),
				P7S, 
				MANIFESTACAO_CLASSE,
				null,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		
		AssuntoLocal assuntoLocal = new AssuntoLocal();
		assuntoLocal.setCodigoAssunto(10303);
		
		AssuntoProcessual assuntoProcessual = new AssuntoProcessual();
		assuntoProcessual.setAssuntoLocal(assuntoLocal);
	
		manifestacao.getDadosBasicos().getAssunto().clear();
		manifestacao.getDadosBasicos().getAssunto().add(assuntoProcessual);
		
		resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
	}

	/**
	 * Teste da entrega de manifestação processual com anexos no formato ISO-8859-1 e UTF-8.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test45EntregarManifestacaoProcessualComAnexosUTF8() {
		logger.info("test45EntregarManifestacaoProcessualComAnexosUTF8: -----------------");
		
		RepresentanteProcessual advogado = obterAdvogadoPoloAtivo();
		advogado.getEndereco().add(novoEndereco(0));
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		DocumentoProcessual peticao = manifestacao.getDocumento().get(0);
		peticao.getDocumentoVinculado().add(novoDocumentoProcessual(ANEXO1, 0, MANIFESTACAO_TIPO_DOCUMENTO_QUALQUER, null, null));
		peticao.getDocumentoVinculado().add(novoDocumentoProcessual("teste-documento-iso88591.html", 0, "2000011", null, null));
		peticao.getDocumentoVinculado().add(novoDocumentoProcessual("teste-documento-utf8.html", 0, "2000011", null, null));
		
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual com duas autoridades nos polos.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test46EntregarManifestacaoProcessualComAutoridades() {
		logger.info("test46EntregarManifestacaoProcessualComAutoridades: -------------------------------");

		final String PESSOA = "UNIAO FEDERAL";
		final String CNPJ = "09580252000292";
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		
		Pessoa autoridade1 = novaPessoa("Autoridade de Teste 10", CNPJ, TipoQualificacaoPessoa.AUTORIDADE);
		autoridade1.getEndereco().clear();
		autoridade1.getEndereco().add(novoEndereco(0));
		Pessoa juridica1 = novaPessoa(PESSOA, CNPJ, TipoQualificacaoPessoa.JURIDICA);
		juridica1.getEndereco().clear();
		juridica1.getEndereco().add(novoEndereco(1));
		autoridade1.setPessoaVinculada(juridica1);
		
		Pessoa autoridade2 = novaPessoa("Autoridade de Teste 20", CNPJ, TipoQualificacaoPessoa.AUTORIDADE);
		autoridade2.getEndereco().clear();
		autoridade2.getEndereco().add(novoEndereco(0));
		Pessoa juridica2 = novaPessoa(PESSOA, CNPJ, TipoQualificacaoPessoa.JURIDICA);
		juridica2.getEndereco().clear();
		juridica2.getEndereco().add(novoEndereco(1));
		autoridade2.setPessoaVinculada(juridica2);
		
		Pessoa autoridade3 = novaPessoa("Autoridade de Teste 30", CNPJ, TipoQualificacaoPessoa.AUTORIDADE);
		autoridade3.getEndereco().clear();
		autoridade3.getEndereco().add(novoEndereco(0));
		Pessoa juridica3 = novaPessoa(PESSOA, CNPJ, TipoQualificacaoPessoa.JURIDICA);
		juridica3.getEndereco().clear();
		juridica3.getEndereco().add(novoEndereco(1));
		autoridade3.setPessoaVinculada(juridica3);
		
		
		Parte parte = new Parte();
		parte.setPessoa(autoridade2);
		
		
		PoloProcessual poloAtivo = novoPoloProcessual(autoridade1, null, ModalidadePoloProcessual.AT);
		poloAtivo.getParte().add(parte);
		PoloProcessual poloPassivo = novoPoloProcessual(autoridade3, null, ModalidadePoloProcessual.PA);
		
		manifestacao.getDadosBasicos().getPolo().set(0, poloAtivo);
		manifestacao.getDadosBasicos().getPolo().set(1, poloPassivo);
		manifestacao.setIdManifestante(USUARIO_DIRETOR_SECRETARIA.split(";")[1]);
		manifestacao.setSenhaManifestante(USUARIO_DIRETOR_SECRETARIA.split(";")[2]);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test47EntregarManifestacaoProcessualDePJComDocumentoRJC() {
		logger.info("test1EntregarManifestacaoProcessual: -------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		
		Pessoa juridica = novaPessoa("ROGERIO PINHEIRO DA COSTA - ME", "11112", TipoQualificacaoPessoa.JURIDICA);
		juridica.getDocumento().get(0).setTipoDocumento(ModalidadeDocumentoIdentificador.RJC);
		
		manifestacao.getDadosBasicos().getPolo().get(1).getParte().get(0).setPessoa(juridica);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test48EntregarManifestacaoProcessualDePessoaJuridica() {
		logger.info("test48EntregarManifestacaoProcessualDePessoaJuridica: -------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessual(
				novoIntercomunicacaoLogin(USUARIO_LOGIN),
				obterPessoaPoloAtivo(), 
				obterPessoaPoloPassivo(), 
				obterAdvogadoPoloAtivo(),
				obterAdvogadoPoloPassivo(), 
				P7S, 
				MANIFESTACAO_CLASSE,
				MANIFESTACAO_ASSUNTO,
				MANIFESTACAO_LOCALIDADE,
				MANIFESTACAO_COMPETENCIA,
				0, 
				2.0,
				null,
				null,
				0, 
				MANIFESTACAO_TIPO_PETICAO_INICIAL);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	/**
	 * Teste da entrega de manifestação processual.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test48EntregarManifestacaoProcessualComOrgaoJulgador() {
		logger.info("test48EntregarManifestacaoProcessualComOrgaoJulgador: -------------------------------");
		
		ManifestacaoProcessual manifestacao = novaManifestacaoProcessualPadrao();
		OrgaoJulgador oj = new OrgaoJulgador();
		oj.setCodigoOrgao("13");
		manifestacao.getDadosBasicos().setOrgaoJulgador(oj);
		RespostaManifestacaoProcessual resposta = mniEntregarManifestacaoProcessual(manifestacao);
		validarResposta(resposta);
		imprimir(resposta, false);
	}
	
	protected abstract RespostaManifestacaoProcessual mniEntregarManifestacaoProcessual(ManifestacaoProcessual manifestacao);

	protected abstract RespostaConsultaProcesso mniConsultarProcesso(RequisicaoConsultaProcesso parametro);
	
	protected abstract RespostaConsultaAvisosPendentes mniConsultarAvisosPendentes(RequisicaoConsultaAvisosPendentes parametro);
	
	protected abstract RespostaConsultarTeorComunicacao mniConsultarTeorComunicacao(RequisicaoConsultarTeorComunicacao parametro);

	/**
	 * @return Manifestação padrão.
	 */
	protected ManifestacaoProcessual novaManifestacaoProcessualPadrao() {
		return novaManifestacaoProcessual(
			novoIntercomunicacaoLogin(USUARIO_LOGIN),
			obterPessoaPoloAtivo(), 
			obterPessoaPoloPassivo(), 
			obterAdvogadoPoloAtivo(),
			obterAdvogadoPoloPassivo(), 
			P7S, 
			MANIFESTACAO_CLASSE,
			MANIFESTACAO_ASSUNTO,
			MANIFESTACAO_LOCALIDADE,
			MANIFESTACAO_COMPETENCIA,
			0, 
			2.0,
			null,
			null,
			0, 
			MANIFESTACAO_TIPO_PETICAO_INICIAL);
	}
	
	/**
	 * Nova manifestação processual. O manifestante é o advogado.
	 * 
	 * @param pessoaLogin
	 * @param poloAtivo
	 * @param poloPassivo
	 * @param advogadoPoloAtivo
	 * @param advogadoPoloPassivo
	 * @param peticaoP7S
	 * @param classe
	 * @param assunto
	 * @param localidade
	 * @param competencia
	 * @param nivelSigilo
	 * @param valorCausa
	 * @param cpfDevedorPrincipal
	 * @param cpfDevedorAlternativo
	 * @param nivelSigiloDocumento
	 * @param tipoDocumento
	 * @return nova manifestação processual.
	 */
	protected ManifestacaoProcessual novaManifestacaoProcessual(IntercomunicacaoLogin pessoaLogin, 
			Pessoa poloAtivo, Pessoa poloPassivo, RepresentanteProcessual advogadoPoloAtivo,
			RepresentanteProcessual advogadoPoloPassivo,
			String peticao, 
			Integer classe, Integer assunto, String localidade, Integer competencia, 
			Integer nivelSigilo, Double valorCausa, 
			String cpfDevedorPrincipal, String cpfDevedorAlternativo, 
			Integer nivelSigiloDocumento, String tipoDocumento) {
		ManifestacaoProcessual manifestacao = new ManifestacaoProcessual();
		manifestacao.setDataEnvio(converterParaDataHora(new Date()));
		
		if (pessoaLogin != null) {
			manifestacao.setIdManifestante(pessoaLogin.getCpf());
			manifestacao.setSenhaManifestante(pessoaLogin.getSenha());
		}
		AssuntoProcessual assuntoProcessual = new AssuntoProcessual();
		assuntoProcessual.setCodigoNacional(assunto);
		assuntoProcessual.setPrincipal(true);
	
		CabecalhoProcessual dadosBasicos = new CabecalhoProcessual();
		dadosBasicos.getAssunto().add(assuntoProcessual);
		dadosBasicos.setClasseProcessual(classe);
		dadosBasicos.setCodigoLocalidade(localidade);
		dadosBasicos.setCompetencia(competencia);
		dadosBasicos.setNivelSigilo(nivelSigilo);
		dadosBasicos.setValorCausa(valorCausa);
	
		
		if (poloAtivo != null) {
			if (poloAtivo.getEndereco().isEmpty()) {
				poloAtivo.getEndereco().add(novoEndereco(0));
			}
			dadosBasicos.getPolo().add(
					novoPoloProcessual(poloAtivo, advogadoPoloAtivo, ModalidadePoloProcessual.AT));
		}
		if (poloPassivo != null) {
			if (poloPassivo.getEndereco().isEmpty()) {
				poloPassivo.getEndereco().add(novoEndereco(1));
			}
			dadosBasicos.getPolo().add(
					novoPoloProcessual(poloPassivo, advogadoPoloPassivo, ModalidadePoloProcessual.PA));
		}
	
		manifestacao.setDadosBasicos(dadosBasicos);
	
		if (peticao != null) {
			DocumentoProcessual documento = novoDocumentoProcessual(
					peticao, 
					nivelSigiloDocumento, 
					tipoDocumento, 
					cpfDevedorPrincipal, 
					cpfDevedorAlternativo);
			manifestacao.getDocumento().add(documento);
		}
		
		return manifestacao;
	}

	/**
	 * @return pessoa do polo ativo.
	 */
	protected Pessoa obterPessoaPoloAtivo() {
		String nome = USUARIO_POLO_ATIVO.split(";")[0];
		String cpf = USUARIO_POLO_ATIVO.split(";")[1];
		TipoQualificacaoPessoa tipo = TipoQualificacaoPessoa.FISICA;
		if (StringUtils.length(cpf) > 11) {
			tipo = TipoQualificacaoPessoa.JURIDICA;
		}
		return novaPessoa(nome, cpf, tipo);
	}

	/**
	 * @return pessoa do polo passivo.
	 */
	protected Pessoa obterPessoaPoloPassivo() {
		String nome = USUARIO_POLO_PASSIVO.split(";")[0];
		String cpf = USUARIO_POLO_PASSIVO.split(";")[1];
		TipoQualificacaoPessoa tipo = TipoQualificacaoPessoa.FISICA;
		if (StringUtils.length(cpf) > 11) {
			tipo = TipoQualificacaoPessoa.JURIDICA;
		}
		return novaPessoa(nome, cpf, tipo);
	}

	/**
	 * @return pessoa advogado do polo ativo.
	 */
	protected Pessoa obterPessoaAdvogadoPoloAtivo() {
		String nome = USUARIO_ADVOGADO_POLO_ATIVO.split(";")[0];
		String cpf = USUARIO_ADVOGADO_POLO_ATIVO.split(";")[1];
		return novaPessoa(nome, cpf, TipoQualificacaoPessoa.FISICA);
	}
	
	/**
	 * @return representante processual do advogado do polo ativo.
	 */
	protected RepresentanteProcessual obterAdvogadoPoloAtivo() {
		String nome = USUARIO_ADVOGADO_POLO_ATIVO.split(";")[0];
		String cpf = USUARIO_ADVOGADO_POLO_ATIVO.split(";")[1];
		return novoRepresentanteProcessual(nome, cpf, ModalidadeRepresentanteProcessual.A);
	}
	
	/**
	 * @return representante processual do advogado do polo passivo.
	 */
	protected RepresentanteProcessual obterAdvogadoPoloPassivo() {
		RepresentanteProcessual resultado = null;
		if (USUARIO_ADVOGADO_POLO_PASSIVO != null) {
			String nome = USUARIO_ADVOGADO_POLO_PASSIVO.split(";")[0];
			String cpf = USUARIO_ADVOGADO_POLO_PASSIVO.split(";")[1];
			resultado = novoRepresentanteProcessual(nome, cpf, ModalidadeRepresentanteProcessual.A);
		}
		return resultado;
	}

	/**
	 * @return pessoa do magistrado.
	 */
	protected Pessoa obterMagistrado() {
		String nome = USUARIO_MAGISTRADO.split(";")[0];
		String cpf = USUARIO_MAGISTRADO.split(";")[1];
		return novaPessoa(nome, cpf, TipoQualificacaoPessoa.AUTORIDADE);
	}

	/**
	 * @return pessoa do diretor de secretaria.
	 */
	
	protected Pessoa obterDiretorSecretaria() {
		String nome = USUARIO_DIRETOR_SECRETARIA.split(";")[0];
		String cpf = USUARIO_DIRETOR_SECRETARIA.split(";")[1];
		return novaPessoa(nome, cpf, TipoQualificacaoPessoa.AUTORIDADE);
	}

	/**
	 * @return pessoa não presente no processo.
	 */
	protected Pessoa obterPessoaNaoPresenteNoProcesso() {
		String nome = USUARIO_NAO_PRESENTE.split(";")[0];
		String cpf = USUARIO_NAO_PRESENTE.split(";")[1];
		return novaPessoa(nome, cpf, TipoQualificacaoPessoa.FISICA);
	}

	/**
	 * @return pessoa sem endereço.
	 */
	protected Pessoa obterPessoaSemEndereco() {
		String nome = USUARIO_SEM_ENDERECO.split(";")[0];
		String cpf = USUARIO_SEM_ENDERECO.split(";")[1];
		return novaPessoa(nome, cpf, TipoQualificacaoPessoa.FISICA);
	}

	/**
	 * Novo representante processual.
	 * 
	 * @param nome
	 * @param cpf
	 * @param tipo
	 * @return representante processual.
	 */
	protected RepresentanteProcessual novoRepresentanteProcessual(String nome, String cpf, ModalidadeRepresentanteProcessual tipo) {
		Map<String, Endereco> mapa = new HashMap<>();
		mapa.put(USUARIO_ADVOGADO_POLO_ATIVO.split(";")[0], novoEndereco(2));
		if (USUARIO_ADVOGADO_POLO_PASSIVO != null) {
			mapa.put(USUARIO_ADVOGADO_POLO_PASSIVO.split(";")[0], novoEndereco(3));
		}
		Endereco endereco = mapa.get(nome);
		if (endereco == null) {
			endereco = novoEndereco(2);
		}
		RepresentanteProcessual advogado = new RepresentanteProcessual();
		advogado.setNome(nome);
		advogado.setNumeroDocumentoPrincipal(cpf);
		advogado.setTipoRepresentante(tipo);
		advogado.getEndereco().add(endereco);
		
		return advogado;
	}
	
	
	/**
	 * Nova pessoa.
	 * 
	 * @param nome
	 * @param cpf
	 * @param tipo
	 * @return pessoa.
	 */
	protected Pessoa novaPessoa(String nome, String cpf, TipoQualificacaoPessoa tipo) {
		Map<String, Endereco> mapa = new HashMap<>();
		mapa.put(USUARIO_POLO_ATIVO.split(";")[0], novoEndereco(0));
		mapa.put(USUARIO_POLO_PASSIVO.split(";")[0], novoEndereco(1));
		Endereco endereco = mapa.get(nome);
		if (endereco == null) {
			endereco = novoEndereco(0);
		}
		
		Pessoa pessoa = new Pessoa();
		pessoa.setNome(nome);
		pessoa.setTipoPessoa(tipo);
		pessoa.getEndereco().add(endereco);
		
		CadastroIdentificador ci = new CadastroIdentificador();
		ci.setValue(cpf);
		pessoa.setNumeroDocumentoPrincipal(ci);

		DocumentoIdentificacao documento = new DocumentoIdentificacao();
		documento.setNome(pessoa.getNome());
		documento.setTipoDocumento(ModalidadeDocumentoIdentificador.CMF);
		documento.setCodigoDocumento(ci.getValue());
		documento.setEmissorDocumento("SSP");
		pessoa.getDocumento().add(documento);
		return pessoa;
	}
	
	/**
	 * Novo polo processual.
	 * 
	 * @param pessoa
	 * @param representante
	 * @param modalidade
	 * @return polo processual.
	 */
	protected PoloProcessual novoPoloProcessual(Pessoa pessoa, RepresentanteProcessual representante, ModalidadePoloProcessual modalidade) {
		PoloProcessual poloProcessual = new PoloProcessual();
		poloProcessual.setPolo(modalidade);
		
		Parte parte = new Parte();
		parte.setPessoa(pessoa);

		if (representante != null) {
			parte.getAdvogado().add(representante);
		}
		
		poloProcessual.getParte().add(parte);
		return poloProcessual;
	}

	/**
	 * Converte Date para DataHora.
	 * 
	 * @param data
	 * @return DataHora.
	 */
	protected DataHora converterParaDataHora(Date data) {
		DataHora dataHora = new DataHora();
		dataHora.setValue(new SimpleDateFormat(MNIParametro.PARAM_FORMATO_DATA_HORA).format(data));
		return dataHora;
	}
	
	/**
	 * Nova data.
	 * 
	 * @param dia
	 * @param mes
	 * @param ano
	 * @return data.
	 */
	protected Date novaData(int dia, int mes, int ano) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, dia);
		calendar.set(Calendar.MONTH, (mes - 1));
		calendar.set(Calendar.YEAR, ano);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTime();
	}
	
	/**
	 * Novo DocumentoProcessual.
	 * 
	 * @param pathDocumento
	 * @param nivelSigilo
	 * @param tipoDocumento
	 * @param cpfDevedorPrincipal
	 * @param cpfDevedorAlternativo
	 * @return DocumentoProcessual
	 */
	protected DocumentoProcessual novoDocumentoProcessual(String pathDocumento, int nivelSigilo, String tipoDocumento, String cpfDevedorPrincipal, String cpfDevedorAlternativo) {
		
		InputStream peticaoStream = obterStream(pathDocumento);
		byte[] peticaoBytes = ProjetoUtil.converterParaBytes(peticaoStream);
		
		String extensao = pathDocumento.substring(pathDocumento.lastIndexOf('.')+1);
		
		DocumentoProcessual documentoProcessual = new DocumentoProcessual();
		DataHora dataHora = converterParaDataHora(new Date());
		documentoProcessual.setDataHora(dataHora);
		documentoProcessual.setHash(Crypto.encodeSHA256(peticaoBytes));
		documentoProcessual.setNivelSigilo(nivelSigilo);
		documentoProcessual.setTipoDocumento(tipoDocumento); //58 = petição inicial.
		documentoProcessual.setDescricao(pathDocumento);
		
		if (extensao.equalsIgnoreCase("p7s")) {
			DataHandler p7sDataHandler = ProjetoUtil.converterParaDataHandler(peticaoBytes, MIME_APPLICATION_PKCS7);
			documentoProcessual.setConteudo(p7sDataHandler);
			documentoProcessual.setMimetype(MIME_APPLICATION_PKCS7);
		} else if (extensao.equalsIgnoreCase("pdf")) {
			DataHandler htmlDataHandler = ProjetoUtil.converterParaDataHandler(peticaoBytes, MIME_APPLICATION_PDF);
			documentoProcessual.setConteudo(htmlDataHandler);
			documentoProcessual.setMimetype(MIME_APPLICATION_PDF);
			
			Assinatura assinatura = assinarDocumento(peticaoBytes);
			documentoProcessual.getAssinatura().add(assinatura);
		} else {
			String mime = getMimeType(peticaoBytes);
			DataHandler htmlDataHandler = ProjetoUtil.converterParaDataHandler(peticaoBytes, mime);
			documentoProcessual.setConteudo(htmlDataHandler);
			documentoProcessual.setMimetype(mime);

			Assinatura assinatura = assinarDocumento(peticaoBytes);
			documentoProcessual.getAssinatura().add(assinatura);
		}
		
		return documentoProcessual;
	}
	
	/**
	 * Novo DocumentoProcessual assinado no modo teste.
	 * 
	 */
	protected DocumentoProcessual novoDocumentoProcessualAssinadoModoTeste() {
		byte[] peticaoBytes = ProjetoUtil.converterParaBytes(obterStream(ANEXO1));
		DataHandler handler = ProjetoUtil.converterParaDataHandler(peticaoBytes);
		
		DocumentoProcessual documentoProcessual = new DocumentoProcessual();
		DataHora dataHora = converterParaDataHora(new Date());
		documentoProcessual.setConteudo(handler);
		documentoProcessual.setDataHora(dataHora);
		documentoProcessual.setHash(Crypto.encodeSHA256(peticaoBytes));
		documentoProcessual.setNivelSigilo(0);
		documentoProcessual.setTipoDocumento(MANIFESTACAO_TIPO_PETICAO_INICIAL); //58 = petição inicial.
		documentoProcessual.setDescricao(ANEXO1);
		documentoProcessual.setMimetype(MIME_APPLICATION_PDF);
		
		String cadeiaPEM = "MIIHMDCCBRigAwIBAgIIKO6lfDYpBNgwDQYJKoZIhvcNAQENBQAwbjELMAkGA1UE BhMCQlIxEzARBgNVBAoTCklDUC1CcmFzaWwxNDAyBgNVBAsTK0F1dG9yaWRhZGUg Q2VydGlmaWNhZG9yYSBSYWl6IEJyYXNpbGVpcmEgdjIxFDASBgNVBAMTC0FDIENB SVhBIHYyMB4XDTExMTIyMzEzNTI1OFoXDTE5MTIyMTEzNTI1OFowXTELMAkGA1UE BhMCQlIxEzARBgNVBAoMCklDUC1CcmFzaWwxIDAeBgNVBAsMF0NhaXhhIEVjb25v bWljYSBGZWRlcmFsMRcwFQYDVQQDDA5BQyBDQUlYQSBQRiB2MjCCAiIwDQYJKoZI hvcNAQEBBQADggIPADCCAgoCggIBANWvsvNnqWNg+rR82rG/WpAs6NKhKpgXcfRg 1G8onArhQ9MSaLnGYTMgkWsbCfOrrCAtE5TVUDJG60+swtwAsIPkZLl7LwhQ6AAQ TX9qknKMPV7sAZlW3SJO+f5uurT894QpqzBW22zT6dgSlhED5HHVqRbsUHoYDH/d nTQCvxkHyDELwowjHffg8/80VOE9kUAjDAWLY4ZTvW+2KRJXFzYyDScA89f5aM1R lLUhAW2hq/KmnunfMsCVUNqQ2LVwNCFjlfn0MHdiE/OooIsL/fE9gUuddCw1h+g1 Icgji4dqCPCoju4/XlDeTF9Z29qCrLuuSKlIdTdUU2aPzLGkzz04/UavAapgOWIe +5DirtLcBST4lTv9TcXleFNtygBCFFNbEcpa2iqYqdw9EndC3k7qYaeijgZgrRBH 4R89k0jbMZG0bKIttCIizOCcHzJJhGx+nQNuoVvPeLyBcIxSX9rvNTzzIIuyH2jV lhrqgAJnDsasTW34FJTB9BVqMnM1k4+IO2ac+zKgfrgTO3lzyqJcTyN2UCbqVw2r SnLxB7ZZTuu3rn8joXQAQ3ABk6phTnzZ08RfHK4Zi+dxdFWxwCZjfRn7KSvgYLMj MmNKqbvWtr41FN2zaO5oc46CKKMIgFShJkWL7fvaUHmxc9x80YZsOamraU5gviXR nehfyN3bAgMBAAGjggHhMIIB3TAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQE AwIBBjAdBgNVHQ4EFgQUnirWQVcAr1vtB/jQXI7zbeblDBowHwYDVR0jBBgwFoAU D1AkMeS6vLGZSSY17Q7Qdf6cn1UwgcUGA1UdIASBvTCBujBbBgZgTAECAQgwUTBP BggrBgEFBQcCARZDaHR0cDovL2NlcnRpZmljYWRvZGlnaXRhbC5jYWl4YS5nb3Yu YnIvZG9jdW1lbnRvcy9kcGNhYy1jYWl4YXBmLnBkZjBbBgZgTAECAwgwUTBPBggr BgEFBQcCARZDaHR0cDovL2NlcnRpZmljYWRvZGlnaXRhbC5jYWl4YS5nb3YuYnIv ZG9jdW1lbnRvcy9kcGNhYy1jYWl4YXBmLnBkZjCBsQYDVR0fBIGpMIGmMCugKaAn hiVodHRwOi8vbGNyLmNhaXhhLmdvdi5ici9hY2NhaXhhdjIuY3JsMCygKqAohiZo dHRwOi8vbGNyMi5jYWl4YS5nb3YuYnIvYWNjYWl4YXYyLmNybDBJoEegRYZDaHR0 cDovL3JlcG9zaXRvcmlvLmljcGJyYXNpbC5nb3YuYnIvbGNyL0NBSVhBL0FDQ0FJ WEEvYWNjYWl4YXYyLmNybDANBgkqhkiG9w0BAQ0FAAOCAgEAg5dz7NCYlQi1O/WI OHr2VPWEaJXLP6ciVVW21uHaop78VndwOT9NbhTANLC92maSTCK3QeJaLtL5lAjL Uo3mA0y976nkaXlQW2jFR3eMIr7vU7xSX/eL5144e6IUbY+YS74EwH8Wn/jP2AOR 5r89CTNQ+CqMy8LHFab7tHcwCmUnalbTt7t6zANN8kJG87nrNu3tLhhT2kaGe2O7 UUV3Xi17NoUV92i8T0u0eQ8Nsv4yqtsgSUCebjnlgTaJskIUow0UMgRzZWRaO99L F4U8BhvPF82UZWmDzMm+Ktswwy+nWGEmSzTOlaLv9UYzun1kDMC6pqWziyLjmz7v eM9eaTKwUBTrqAe/5U8FYSufeh4j9p8KGKLkwTjwAkbQjjRi/vKXZFqw0v1AxoC4 9NZ0tvOuJPcprXMc6idhjgvaz1Ye0uXpMyT4bp5f1/ufkMProiLUo/z8YtPZ/wzp yvVtle+4Ri3Z7qWRAwNZ2Nd70jtKjfG1GIi3blTdMWL1gr6+tMLB6OnyZTh8X2aD CtdQy/S55JjD+t2MxtW22IaS+KOWF2IGWZm4L0b/rGwvk0ZN0djJEyrac7Y41zyM lzJjPlsetJXV+eXPBkkk/RqJnoHB+QOGzK1+ssJ4cq+0SRH6H6MuQLdkPcXRx1g1 ax6m9jdWLtwKLLp3+SXt01ZZVBM=";
		
		Assinatura resultado = new Assinatura();
		resultado.setAlgoritmoHash(SignatureAlgorithm.SHA256withRSA.name());
		resultado.setAssinatura("assinaturadeteste");
		resultado.setCadeiaCertificado(cadeiaPEM);
		resultado.setCodificacaoCertificado(CodificacaoCertificado.PEM.getValor());
		resultado.setDataAssinatura(converterParaDataHora(new Date()));
		
		documentoProcessual.getAssinatura().add(resultado);
		
		return documentoProcessual;
	}

	/**
	 * @param nome
	 * @param valor
	 * @return novo Parametro
	 */
	protected Parametro novoParametro(String nome, String valor) {
		Parametro parametro = new Parametro();
		parametro.setNome(nome);
		parametro.setValor(valor);
		return parametro;
	}

	/**
	 * @param arquivo
	 * @return Stream do arquivo passado por parâmetro.
	 */
	protected InputStream obterStream(String arquivo) {
		
		InputStream resultado = null;
		
		if (arquivo.contains("/") || arquivo.contains("\\")) {
			try {
				resultado = new FileInputStream(new File(arquivo));
			} catch (FileNotFoundException e) {
				throw new IntercomunicacaoException(e.getMessage());
			}
		} else {
			resultado = IntercomunicacaoTest.class.getResourceAsStream(arquivo);
		}
		
		return resultado;
	}

	/**
	 * Gera um pdf do array de bytes.
	 * 
	 * @param bytes
	 * @param arquivo
	 * @throws RuntimeException
	 */
	protected void gerarPDF(byte[] bytes, String arquivo) {
		if (bytes != null) {
			
			try (FileOutputStream fos = new FileOutputStream(arquivo)){
				arquivo = System.getProperty("java.io.tmpdir") + File.separator + arquivo;
				fos.write(bytes);
				fos.flush();
			} catch (Exception e) {
				String mensagem = String.format("Erro ao gerar o recibo, erro: %s",
						e.getMessage());
				throw new IntercomunicacaoException(mensagem);
			}
		}
	}

	/**
	 * @param resposta
	 * @return
	 * @throws RuntimeException
	 */
	protected void validarResposta(Object resposta) {

		if (resposta != null) {
			boolean sucesso = (Boolean) ReflectionsUtil.getValue(resposta,
					"sucesso");
			String mensagem = ReflectionsUtil.getStringValue(resposta,
					"mensagem");

			logger.info(mensagem);
			if (!sucesso) {
				throw new IntercomunicacaoException(mensagem);
			}
		} else {
			throw new IntercomunicacaoException("Resposta está nulo!");
		}
	}
	
	/**
	 * Remove a máscara do CPF.
	 * @param cpf
	 * @return string sem máscara.
	 */
	protected String retirarMascaraCpf(String cpf) {
		if (cpf != null) {
			cpf = cpf.replaceAll("[^0-9]*", "");
		}
		return cpf;
	}

	/**
	 * Retorna o expediente do processo informado.
	 * @param avisos Lista de expedientes.
	 * @param numeroProcesso Processo.
	 * @return Expediente do processo informado.
	 */
	protected AvisoComunicacaoPendente obterExpedienteDoProcesso(
			List<AvisoComunicacaoPendente> avisos, String numeroProcesso) {
		AvisoComunicacaoPendente resultado = null;
		
		for (int indice = 0; indice < avisos.size() && resultado == null; indice++) {
			AvisoComunicacaoPendente aviso = avisos.get(indice);
			if (aviso.getProcesso().getNumero().getValue().equalsIgnoreCase(numeroProcesso)) {
				resultado = aviso;
			}
		}
		return resultado;
	}
	
	/**
	 * Assina o documento passado por parâmetro.
	 * 
	 * @param documento Documento que será assinado.
	 * @return Assinatura do documento.
	 */
	protected Assinatura assinarDocumento(byte[] documento) {
		Assinatura resultado = new Assinatura();
		
		try {
			int[] array = {99, 101, 106, 110, 112};
			
			// Certificado pje.pfx
//			KeyStore ks = KeyStore.getInstance("PKCS12");
//			InputStream is = CertificadoDigitalService.class.getClassLoader().getResourceAsStream("pje.pfx");
//			ks.load(is, new char[]{(char)array[4], (char)array[2], (char)array[1], (char)array[0], (char)array[3], (char)array[2], (char)array[4], (char)array[2], (char)array[1]});
//			String alias = obterAlias(ks);
//			PrivateKey privateKey = (PrivateKey) ks.getKey(alias, new char[]{(char)array[4], (char)array[2], (char)array[1], (char)array[0], (char)array[3], (char)array[2], (char)array[4], (char)array[2], (char)array[1]});
			
			// Certificado do token
			KeyStore ks = obterKeyStore();
			String alias = obterAlias(ks);
			PrivateKey privateKey = obterPrivateKey(ks, alias);
			Certificate[] certificates = ks.getCertificateChain(alias);
			
			byte[] assinatura = assinarDocumento(privateKey, documento);
			String cadeiaPEM = obterCadeiaPEM(certificates);
			String assinaturaBase64 = new String(SigningUtilities.base64Encode(assinatura));
			
			resultado.setAlgoritmoHash(SignatureAlgorithm.SHA256withRSA.name());
			resultado.setAssinatura(assinaturaBase64);
			resultado.setCadeiaCertificado(cadeiaPEM);
			resultado.setCodificacaoCertificado(CodificacaoCertificado.PEM.getValor());
			resultado.setDataAssinatura(converterParaDataHora(new Date()));
		} catch (Exception e) {
			throw new IntercomunicacaoException(e.getMessage());
		}

		return resultado;
	}
	
	/**
	 * Assina o documento e retorna o array de bytes da assinatura.
	 * 
	 * @param privateKey
	 * @param documento
	 * @return assinatura
	 * @throws InvalidKeyException 
	 */
	protected byte[] assinarDocumento(PrivateKey privateKey, byte[] documento) throws InvalidKeyException {
		byte[][] assinatura = Signer.sign(privateKey, SignatureAlgorithm.SHA256withRSA, documento);
		
		return assinatura[0];
	}
	
	/**
	 * Retorna o certificado no formato PEM.
	 * 
	 * @param certificates
	 * @return String PEM
	 * @throws CertificateEncodingException 
	 */
	protected String obterCadeiaPEM(Certificate[] certificates) throws CertificateEncodingException {
		StringBuilder resultado = new StringBuilder();
		for (Certificate certificate : certificates) {
			byte[] bytes = certificate.getEncoded();
			resultado.append("-----BEGIN CERTIFICATE-----");
			resultado.append("\n");
			resultado.append(new Base64(64).encodeToString(bytes));
			resultado.append("-----END CERTIFICATE-----");
			resultado.append("\n");
		}
		resultado.deleteCharAt(resultado.length()-1);
		return resultado.toString();
	}
	
	/**
	 * Retorna a chave privada do certificado.
	 * 
	 * @param ks
	 * @param alias
	 * @return Chave privada.
	 * @throws Exception
	 */
	protected PrivateKey obterPrivateKey(KeyStore ks, String alias) throws Exception {
		return (PrivateKey) ks.getKey(alias, null);
	}
	
	/**
	 * Retorna o primeiro alias do keystore.
	 * 
	 * @param ks
	 * @return Alias.
	 * @throws KeyStoreException 
	 */
	protected String obterAlias(KeyStore ks) throws KeyStoreException {
		List<String> aliasesList = Collections.list(ks.aliases());
		return aliasesList.get(0);
	}
	
	/**
	 * Retorna o keystore do token.
	 * 
	 * @return Keystore.
	 * @throws Exception
	 */
	protected KeyStore obterKeyStore() throws Exception {
		InputStream providers = ClassLoader.getSystemResourceAsStream("br/jus/cnj/pje/intercomunicacao/servico/providers.csv");
		
		//Se ocorrer um erro java.lang.NoClassDefFoundError: sun/security/pkcs11/SunPKCS11 é
		//necessário colocar o jar sunpkcs11.jar no classpath, o arquivo em questão não está 
		//disponível na versão de 64 bits do java.
		ProviderUtil provider = new ProviderUtil(providers, "ISO-8859-1", null);
		provider.reload();
		return provider.getKeyStoreMap().values().iterator().next().getKeyStore();
	}
	
	/**
	 * @param data Documento.
	 * @return String mimetype do array de bytes.
	 */
	protected String getMimeType(byte[] data){
		String mimetype = "unknown";
		try {
			TikaInputStream tis = TikaInputStream.get(data);
			Tika tika = new Tika();
			mimetype = tika.detect(tis);
		} catch (IOException e) {
			logger.info("Erro ao tentar identificar o tipo de arquivo a partir de seus bytes: "+ e.getLocalizedMessage());
		}
		return mimetype;
	}
	
	/**
	 * Imprimir os dados de resposta da consulta de processo.
	 * O pre-requisito para a impressão da resposta é que tenha sido executada com sucesso.
	 * 
	 * @param resposta RespostaConsultaProcesso
	 * @param validacaoDocumentoTemBinario True se for para fazer a validação se existe o binário 
	 * dos documentos.
	 */
	protected void imprimir(RespostaConsultaProcesso resposta, Boolean validacaoDocumentoTemBinario) {
		Assert.assertNotNull(resposta);
		Assert.assertNotNull(resposta.getProcesso());
		
		ProcessoJudicial processo = resposta.getProcesso();
		List<DocumentoProcessual> documentos = processo.getDocumento();
		List<MovimentacaoProcessual> movimentos = processo.getMovimento();
		CabecalhoProcessual dadosBasicos = processo.getDadosBasicos();
		
		if (dadosBasicos != null) {
			List<Parametro> parametros = dadosBasicos.getOutroParametro();
			
			OrgaoJulgador oj = dadosBasicos.getOrgaoJulgador();
			Parametro situacao = MNIParametroUtil.obter(parametros, MNIParametro.getSituacaoProcesso());
			logger.info("Processo.......: "+ dadosBasicos.getNumero().getValue());
			logger.info("Jurisdição.....: "+ dadosBasicos.getCodigoLocalidade());
			logger.info("Situação.......: "+ (situacao != null ? situacao.getValor() : "null"));
			logger.info("Tamanho........: "+ dadosBasicos.getTamanhoProcesso());
			logger.info("Orgão Julgador.: "+ oj.getNomeOrgao());
			logger.info("    Instância..: "+ oj.getInstancia());
			
			List<CadastroIdentificador> magistrados = dadosBasicos.getMagistradoAtuante();
			for (CadastroIdentificador magistrado : magistrados) {
				logger.info("Magistrado: "+ magistrado.getValue());
			}
			
			List<PoloProcessual> polos = dadosBasicos.getPolo();
			for (PoloProcessual polo : polos) {
				logger.info("Polo...........: "+ polo.getPolo().value());
				List<Parte> partes = polo.getParte();
				for (Parte parte : partes) {
					Pessoa pessoa = parte.getPessoa();
					CadastroIdentificador numeroDocumentoPrincipal = pessoa.getNumeroDocumentoPrincipal();
					List<Endereco> enderecos = pessoa.getEndereco();
					logger.info("    Parte......: "+ pessoa.getNome());
					if (numeroDocumentoPrincipal != null) {
						logger.info("    CPF/CNPJ...: "+ numeroDocumentoPrincipal.getValue());
					}
					for (Endereco endereco : enderecos) {
						logger.info("        End....: "+ endereco.getCep() +" - "+ endereco.getLogradouro() +" - "+ endereco.getNumero());
					}
					List<RepresentanteProcessual> representantes = parte.getAdvogado();
					for (RepresentanteProcessual representante : representantes) {
						logger.info("        - Advogado: "+ representante.getNome());
						
					}
				}
			}
			if (parametros != null) {
				for (Parametro parametro : parametros) {
					logger.info("Parametros:");
					logger.info(String.format("%s: %s", parametro.getNome(), parametro.getValor()));
				}
			}
			logger.info("\n");
		} else {
			logger.info("Cabeçalho não recuperado.");
		}
		
		if (!documentos.isEmpty()) {
			logger.info("Quantidade de documentos: "+ documentos.size());
			logger.info("Documentos...");
			for (DocumentoProcessual documento : documentos) {
				byte[] bytes = ProjetoUtil.converterParaBytes(documento.getConteudo());
				if (BooleanUtils.isTrue(validacaoDocumentoTemBinario)) {
					Assert.assertTrue((bytes != null && bytes.length > 0));
				}
				
				logger.info("-- id...: "+ documento.getIdDocumento());
				logger.info(" - bytes: "+ (bytes != null ? "sim": "não"));
				logger.info(" - tipo.: "+ documento.getTipoDocumento());

				List<DocumentoProcessual> vinculados = documento.getDocumentoVinculado();
				logger.info("\tQuantidade de vinculados: "+ vinculados.size());
				logger.info("\tVinculados...");
				for (DocumentoProcessual vinculado : vinculados) {
					byte[] bytesVinculado = ProjetoUtil.converterParaBytes(vinculado.getConteudo());
					if (BooleanUtils.isTrue(validacaoDocumentoTemBinario)) {
						Assert.assertTrue((bytesVinculado != null && bytesVinculado.length > 0));
					}
					
					logger.info("\t-- id...: "+ vinculado.getIdDocumento());
					logger.info("\t - bytes: "+ (bytesVinculado != null ? "sim": "não"));
					logger.info("\t - tipo.: "+ vinculado.getTipoDocumento());
				}
			}
			DocumentoProcessual primeiroDocumento = documentos.get(0);
			Object extensao = primeiroDocumento.getAny();
			if (extensao != null) {
			}
		} else {
			logger.info("Documentos não recuperados.");
		}
		
		if (!movimentos.isEmpty()) {
			logger.info("Quantidade de movimentos: "+ movimentos.size());
			logger.info("Movimentos...");
			for (MovimentacaoProcessual movimento : movimentos) {
				MovimentoLocal ml = movimento.getMovimentoLocal();
				MovimentoNacional mn = movimento.getMovimentoNacional();
				
				logger.info("- id...........: "+ movimento.getIdentificadorMovimento());
				logger.info("- desc local...: "+ (ml != null ? ml.getDescricao() : null));
				logger.info("- cod nacional.: "+ (mn != null ? mn.getCodigoNacional() : null));
			}
		} else {
			logger.info("Movimentos não recuperados.");
		}
	}

	/**
	 * Imprimir os dados de resposta da entrega de manifestação.
	 * O pre-requisito para a impressão da resposta é que tenha sido executada com sucesso.
	 * 
	 * @param resposta RespostaManifestacaoProcessual
	 * @param gerarRecibo True se for para gerar o pdf do recibo na pasta temp.
	 */
	protected void imprimir(RespostaManifestacaoProcessual resposta, Boolean gerarRecibo) {
		Assert.assertNotNull(resposta);
		Assert.assertNotNull(resposta.getProtocoloRecebimento());
		Assert.assertNotNull(resposta.getDataOperacao());
		
		logger.info("Processo...: "+ resposta.getProtocoloRecebimento());
		logger.info("Data.......: "+ resposta.getDataOperacao().getValue());
		if (BooleanUtils.isTrue(gerarRecibo)) {
			byte[] bytes = ProjetoUtil.converterParaBytes(resposta.getRecibo());
			gerarPDF(bytes, "recibo-"+ resposta.getProtocoloRecebimento() +".pdf");
			logger.info("- Recibo 'recibo-"+ resposta.getProtocoloRecebimento() +".pdf' gerado com sucesso.");
		}
	}

	/**
	 * Imprimir os dados de resposta da consulta de avisos.
	 * O pre-requisito para a impressão da resposta é que tenha sido executada com sucesso.
	 * 
	 * @param resposta RespostaConsultaAvisosPendentes
	 * @param validacaoExisteAvisos True se for para validar a existência de avisos.
	 */
	protected void imprimir(RespostaConsultaAvisosPendentes resposta, Boolean validacaoExisteAvisos) {
		Assert.assertNotNull(resposta);
		
		List<AvisoComunicacaoPendente> avisos = resposta.getAviso();
		if (BooleanUtils.isTrue(validacaoExisteAvisos)) {
			Assert.assertFalse(avisos.isEmpty());
		}
		
		for (AvisoComunicacaoPendente aviso : avisos) {
			Parte destinatario = aviso.getDestinatario();
			Pessoa pessoaDestinatario = (destinatario != null ? destinatario.getPessoa() : null);
			
			logger.info("-- Id aviso....: "+ aviso.getIdAviso().getValue());
			logger.info(" - Data........: "+ aviso.getDataDisponibilizacao().getValue());
			logger.info(" - Destinatário: "+ (pessoaDestinatario != null ? pessoaDestinatario.getNome() : null));
			
			if (aviso.getProcesso() != null) {
				List<Parametro> parametros = aviso.getProcesso().getOutroParametro();
				for (Parametro parametro : parametros) {
					logger.info(" 	- Parâmetro ("+ parametro.getNome() +"): "+ parametro.getValor());
				}
			}
		}
	}

	/**
	 * Imprimir os dados de resposta da consulta de teor.
	 * O pre-requisito para a impressão da resposta é que tenha sido executada com sucesso.
	 * 
	 * @param resposta RespostaConsultaAvisosPendentes
	 * @param validacaoExisteAvisos True se for para validar a existência de avisos.
	 */
	protected void imprimir(RespostaConsultarTeorComunicacao resposta, Boolean validacaoExisteComunicacao) {
		Assert.assertNotNull(resposta);
		
		List<ComunicacaoProcessual> comunicacoes = resposta.getComunicacao();
		if (BooleanUtils.isTrue(validacaoExisteComunicacao)) {
			Assert.assertFalse(comunicacoes.isEmpty());
		}
		
		for (ComunicacaoProcessual comunicacao : comunicacoes) {
			logger.info("-- Id comunicacao.: "+ comunicacao.getId().getValue());
			logger.info(" - Processo.......: "+ comunicacao.getProcesso());
			logger.info(" - Tipo...........: "+ comunicacao.getTipoComunicacao().getValue());
			logger.info(" - Qtd documentos.: " + comunicacao.getDocumento().size());
			for (DocumentoProcessual documento : comunicacao.getDocumento()) {
				byte[] bytes = ProjetoUtil.converterParaBytes(documento.getConteudo());
				
				logger.info("   - documento....: "+ documento.getIdDocumento());
				logger.info("   - bytes........: "+ (bytes != null ? "sim": "não"));
				logger.info("   - mimetype.....: "+ documento.getMimetype());
				logger.info("   - tipo do doc..: "+ documento.getTipoDocumento());
			}
		}
	}
	
	/**
	 * Retorna nova ManifestacaoProcessual com usuário e senha.
	 * 
	 * @param usuarioLogin
	 * @return nova ManifestacaoProcessual com usuário e senha.
	 */
	protected ManifestacaoProcessual novaManifestacaoProcessual(String usuarioLogin) {
		IntercomunicacaoLogin login = novoIntercomunicacaoLogin(usuarioLogin);
		
		ManifestacaoProcessual requisicao = new ManifestacaoProcessual();
		requisicao.setIdManifestante(login.getCpf());
		requisicao.setSenhaManifestante(login.getSenha());
		return requisicao;
	}
	
	/**
	 * Retorna nova RequisicaoConsultaAvisosPendentes com usuário e senha.
	 * 
	 * @param usuarioLogin
	 * @return nova RequisicaoConsultaAvisosPendentes com usuário e senha.
	 */
	protected RequisicaoConsultaAvisosPendentes novaRequisicaoConsultaAvisosPendentes(String usuarioLogin) {
		IntercomunicacaoLogin login = novoIntercomunicacaoLogin(usuarioLogin);
		
		RequisicaoConsultaAvisosPendentes requisicao = new RequisicaoConsultaAvisosPendentes();
		requisicao.setIdConsultante(login.getCpf());
		requisicao.setSenhaConsultante(login.getSenha());
		return requisicao;
	}
	
	/**
	 * Retorna nova RequisicaoConsultaProcesso com usuário e senha.
	 * 
	 * @param usuarioLogin
	 * @return nova RequisicaoConsultaProcesso com usuário e senha.
	 */
	protected RequisicaoConsultaProcesso novaRequisicaoConsultaProcesso(String usuarioLogin) {
		IntercomunicacaoLogin login = novoIntercomunicacaoLogin(usuarioLogin);
		
		RequisicaoConsultaProcesso requisicao = new RequisicaoConsultaProcesso();
		requisicao.setIdConsultante(login.getCpf());
		requisicao.setSenhaConsultante(login.getSenha());
		return requisicao;
	}

	/**
	 * Retorna nova RequisicaoConsultarTeorComunicacao com usuário e senha.
	 * 
	 * @param usuarioLogin
	 * @return nova RequisicaoConsultarTeorComunicacao com usuário e senha.
	 */
	protected RequisicaoConsultarTeorComunicacao novaRequisicaoConsultarTeorComunicacao(String usuarioLogin) {
		IntercomunicacaoLogin login = novoIntercomunicacaoLogin(usuarioLogin);
		
		RequisicaoConsultarTeorComunicacao requisicao = new RequisicaoConsultarTeorComunicacao();
		requisicao.setIdConsultante(login.getCpf());
		requisicao.setSenhaConsultante(login.getSenha());
		return requisicao;
	}
	
	/**
	 * Retorna IntercomunicacaoLogin com as informações de login da requisição.
	 * 
	 * @param usuario String no formato NOME;CPF;SENHA
	 * @return IntercomunicacaoLogin
	 */
	protected IntercomunicacaoLogin novoIntercomunicacaoLogin(String usuario) {
		String cpf = ArrayUtil.get(usuario, ";", 1);
		String senha = ArrayUtil.get(usuario, ";", 2);
		
		IntercomunicacaoLogin login = new IntercomunicacaoLogin();
		login.setCpf(cpf);
		login.setSenha((senha != null ? senha : cpf));
		return login;
	}
	
	/**
	 * @return Novo endereço padrão.
	 */
	protected Endereco novoEndereco(Integer indice) {
		final String CIDADE = "Brasília";
		final String BAIRRO = "Taguatinga Sul";
		final String ESTADO = "DF";
		
		Endereco endereco1 = new Endereco();
		endereco1.setCep("72015-010");
		endereco1.setCidade(CIDADE);
		endereco1.setEstado(ESTADO);
		endereco1.setBairro(BAIRRO);
		endereco1.setLogradouro("QSA 1, casa");
		endereco1.setNumero("1");
		
		Endereco endereco2 = new Endereco();
		endereco2.setCep("72015-520");
		endereco2.setCidade(CIDADE);
		endereco2.setEstado(ESTADO);
		endereco2.setBairro(BAIRRO);
		endereco2.setLogradouro("QSB 2, casa");
		endereco2.setNumero("2");
		
		Endereco endereco3 = new Endereco();
		endereco3.setCep("72016-030");
		endereco3.setCidade(CIDADE);
		endereco3.setEstado(ESTADO);
		endereco3.setBairro(BAIRRO);
		endereco3.setLogradouro("QSC 3, casa");
		endereco3.setNumero("3");
		
		Endereco endereco4 = new Endereco();
		endereco4.setCep("72020-040");
		endereco4.setCidade(CIDADE);
		endereco4.setEstado(ESTADO);
		endereco4.setBairro(BAIRRO);
		endereco4.setLogradouro("QSD 4, casa");
		endereco4.setNumero("4");

		List<Endereco> enderecos = new ArrayList<>();
		enderecos.add(endereco1);
		enderecos.add(endereco2);
		enderecos.add(endereco3);
		enderecos.add(endereco4);
		
		return enderecos.get(indice);
	}
	
	/**
	 * Login da requisição ao IntercomunicacaoService.
	 * 
	 * @author Adriano Pamplona
	 */
	class IntercomunicacaoLogin {
		private String cpf;
		private String senha;
		
		/**
		 * @return Retorna cpf.
		 */
		public String getCpf() {
			return cpf;
		}
		
		/**
		 * @param cpf Atribui cpf.
		 */
		public void setCpf(String cpf) {
			this.cpf = cpf;
		}
		
		/**
		 * @return Retorna senha.
		 */
		public String getSenha() {
			return senha;
		}
		
		/**
		 * @param senha Atribui senha.
		 */
		public void setSenha(String senha) {
			this.senha = senha;
		}
	}
}