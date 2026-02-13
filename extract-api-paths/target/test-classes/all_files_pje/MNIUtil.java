/**
 * MNIUtil.java
 * 
 * Data de criação: 09/06/2015
 */
package br.jus.cnj.pje.intercomunicacao.v222.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.itx.util.ReflectionsUtil;
import br.jus.cnj.intercomunicacao.v222.beans.AvisoComunicacaoPendente;
import br.jus.cnj.intercomunicacao.v222.beans.CabecalhoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.CadastroIdentificador;
import br.jus.cnj.intercomunicacao.v222.beans.ConfirmacaoRecebimento;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoIdentificacao;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Identificador;
import br.jus.cnj.intercomunicacao.v222.beans.ManifestacaoProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.ModalidadePoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.Parametro;
import br.jus.cnj.intercomunicacao.v222.beans.Parte;
import br.jus.cnj.intercomunicacao.v222.beans.PoloProcessual;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaConsultaProcesso;
import br.jus.cnj.intercomunicacao.v222.beans.RespostaManifestacaoProcessual;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.util.ArrayUtil;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe utilitária para tratar hard code do IntercomunicacaoService e classes
 * da remessa/retorno.
 * 
 * @author Adriano Pamplona
 */
public final class MNIUtil {

	/**
	 * Retorna uma coleção dos polos informados pelo parâmetro 'modalidade'.
	 * 
	 * @param polos
	 *            Lista de polos.
	 * @param modalidade
	 *            Tipo do polo que será recuperado.
	 * @return Lista de polo
	 */
	@SuppressWarnings("unchecked")
	public static List<PoloProcessual> obterColecaoPolo(
			List<PoloProcessual> polos,
			final ModalidadePoloProcessual modalidade) {
		List<PoloProcessual> resultado = new ArrayList<PoloProcessual>();

		if (polos != null && modalidade != null) {
			Predicate filtro = novoFiltroPolo(modalidade);
			resultado = (List<PoloProcessual>) CollectionUtils.select(polos,
					filtro);
		}
		return resultado;
	}

	/**
	 * Retorna uma coleção de polo ativo.
	 * 
	 * @param manifestacao
	 *            ManifestacaoProcessual
	 * @return Lista de polo ativo.
	 */
	public static List<PoloProcessual> obterColecaoPoloAtivo(
			ManifestacaoProcessual manifestacao) {
		List<PoloProcessual> resultado = new ArrayList<PoloProcessual>();

		if (isExisteCabecalhoProcessual(manifestacao)) {
			CabecalhoProcessual cabecalhoProcessual = manifestacao
					.getDadosBasicos();
			List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
			resultado = obterColecaoPolo(polos, ModalidadePoloProcessual.AT);
		}
		return resultado;
	}
	
	/**
	 * Retorna uma coleção de polo passivo.
	 * 
	 * @param manifestacao
	 *            ManifestacaoProcessual
	 * @return Lista de polo passivo.
	 */
	public static List<PoloProcessual> obterColecaoPoloPassivo(
			ManifestacaoProcessual manifestacao) {
		List<PoloProcessual> resultado = new ArrayList<PoloProcessual>();

		if (isExisteCabecalhoProcessual(manifestacao)) {
			CabecalhoProcessual cabecalhoProcessual = manifestacao
					.getDadosBasicos();
			List<PoloProcessual> polos = cabecalhoProcessual.getPolo();
			resultado = obterColecaoPolo(polos, ModalidadePoloProcessual.PA);
		}
		return resultado;
	}

	/**
	 * Retorna a primeira parte do primeiro polo processual.
	 * 
	 * @param polos
	 *            Polo.
	 * @return Parte do PoloProcessual.
	 */
	public static Parte obterParte(List<PoloProcessual> polos) {
		Parte resultado = null;
		if (polos != null && polos.get(0) != null
				&& polos.get(0).getParte().size() > 0) {

			resultado = polos.get(0).getParte().get(0);
		}
		return resultado;
	}

	/**
	 * Retorna true se a Parte e a Pessoa possuirem o mesmo número de documento
	 * principal.
	 * 
	 * @param parte
	 *            Parte.
	 * @param pessoa
	 *            Pessoa.
	 * @return Booleano.
	 */
	public static Boolean isIguais(Parte parte, Pessoa pessoa) {
		Boolean resultado = Boolean.FALSE;

		if (parte != null
				&& parte.getPessoa() != null
				&& parte.getPessoa().getNumeroDocumentoPrincipal() != null
				&& StringUtils.isNotBlank(parte.getPessoa()
						.getNumeroDocumentoPrincipal().getValue())
				&& pessoa != null
				&& StringUtils.isNotBlank(pessoa.getDocumentoCpfCnpj())) {

			String cpfParte = parte.getPessoa().getNumeroDocumentoPrincipal()
					.getValue();
			String cpfPessoa = pessoa.getDocumentoCpfCnpj();

			cpfParte = InscricaoMFUtil.retiraMascara(cpfParte);
			cpfPessoa = InscricaoMFUtil.retiraMascara(cpfPessoa);

			resultado = StringUtils.equals(cpfParte, cpfPessoa);
		}
		return resultado;
	}

	/**
	 * Retorna true se a parte possuir representante.
	 * 
	 * @param parte
	 *            Parte.
	 * @return booleano
	 */
	public static Boolean isPossuiRepresentante(Parte parte) {
		return parte != null
				&& parte.getAdvogado().size() > 0
				&& StringUtils.isNotBlank(parte.getAdvogado().get(0)
						.getNumeroDocumentoPrincipal());
	}

	
	/**
	 * Retorna o mimetype do documento.
	 * 
	 * @param documento
	 * @return Mimetype do documento.
	 */
	public static String obterMimeType(DocumentoProcessual documento) {
		String resultado = null;

		if (documento.getMimetype() != null) {
			resultado = documento.getMimetype();
		} else {
			resultado = (documento.getConteudo() != null ? 
					documento.getConteudo().getContentType() : null);
		}

		if (resultado != null && MimetypeUtil.isMimetypePkcs7(resultado)) {
			resultado = MimetypeUtil.getMimetypePdf();
		}
		return StringUtils.left(resultado, 50);
	}
	
	/**
	 * Retorna true se o documento de identificação estiver preenchido.
	 * 
	 * @param identificacao DocumentoIdentificacao
	 * @return true se o documento de identificação for preenchido.
	 */
	public static Boolean isDocumentoIdentificacaoPreenchido(DocumentoIdentificacao identificacao) {
		return (identificacao != null && 
				StringUtils.isNotBlank(identificacao.getCodigoDocumento()) &&
				StringUtils.isNotBlank(identificacao.getEmissorDocumento()) &&
				identificacao.getTipoDocumento() != null);
	}
	
	/**
	 * Retorna true se houver CabecalhoProcessual na ManifestacaoProcessual.
	 * 
	 * @param manifestacao
	 * @return booleano.
	 */
	private static Boolean isExisteCabecalhoProcessual(
			ManifestacaoProcessual manifestacao) {
		return (manifestacao != null && manifestacao.getDadosBasicos() != null);
	}

	/**
	 * Retorna true se os documentos de identificação forem iguais, os atributos comparados são: 
	 * codigoDocumento, emissorDocumento e o tipoDocumento.
	 * 
	 * @param arg0 DocumentoIdentificacao
	 * @param arg1 DocumentoIdentificacao
	 * @return Booleano se os documentos de identificação forem iguais.
	 */
	public static boolean isDocumentoIdentificacaoIguais(
			DocumentoIdentificacao arg0, DocumentoIdentificacao arg1) {
		
		return 	isDocumentoIdentificacaoPreenchido(arg0) && 
				isDocumentoIdentificacaoPreenchido(arg1) &&
				arg0.getCodigoDocumento().equalsIgnoreCase(arg1.getCodigoDocumento()) &&
				arg0.getEmissorDocumento().equalsIgnoreCase(arg1.getEmissorDocumento()) &&
				arg0.getTipoDocumento() == arg1.getTipoDocumento();
	}
	
	/**
	 * Retorna o CPF/CNPJ da requisição.
	 * O login pode vir com o CPF/CNPJ sozinho ou acompanhado da Localização.
	 * 
	 * Exemplos:
	 * 82749655153 => Somente o CPF
	 * 82749655153/34949 => CPF e o Id da Localização
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @return login da requisição.
	 */
	public static String obterLogin(Object requisicao) {
		String login = obterStringLogin(requisicao);
		return ArrayUtil.get(login, "/", 0);
	}
	
	/**
	 * Retorna a senha da requisição.
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @return senha da requisição.
	 */
	public static String obterSenha(Object requisicao) {
		String senha = null;
		
		if (requisicao instanceof ManifestacaoProcessual) {
			senha = ReflectionsUtil.getStringValue(requisicao, "senhaManifestante");
		} else if (requisicao instanceof ConfirmacaoRecebimento){
			senha = ReflectionsUtil.getStringValue(requisicao, "senhaRecebedor");
		} else {
			senha = ReflectionsUtil.getStringValue(requisicao, "senhaConsultante");
		}
		
		return senha;
	}

	/**
	 * Retorna a localização do usuário da requisição.
	 * A localização pode ser enviada no campo 'idManifestante ou idConsultante' separado por '/'.
	 * Ex: idManifestante = '82749655153/23855'
	 * A localização é enviada como ID que pode ser consultado no ConsultaPJe.consultarPapeis(login)
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @return localização do usuário.
	 */
	public static UsuarioLocalizacao obterLocalizacao(Object requisicao) {
		UsuarioLocalizacao resultado = null;

		String idLocalizacao = obterStringLogin(requisicao);
		idLocalizacao = ArrayUtil.get(idLocalizacao, "/", 1);
		if (StringUtil.isNotEmpty(idLocalizacao)) {
			resultado = new UsuarioLocalizacao();
			resultado.setIdUsuarioLocalizacao(Integer.parseInt(idLocalizacao));
		}
		return resultado;
	}

	/**
	 * Atribui a string login ao atributo de login da requisição. (ManifestacaoProcessual.idManifestante, 
	 * RequisicaoConsultaAvisosPendentes.idConsultante etc)
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @param login CPF/CNPJ
	 */
	public static void atribuirLogin(Object requisicao, String login) {
		if (requisicao != null) {
			if (requisicao instanceof ManifestacaoProcessual) {
				ReflectionsUtil.setValue(requisicao, "idManifestante", login);
			} else if (requisicao instanceof ConfirmacaoRecebimento){
				ReflectionsUtil.setValue(requisicao, "idRecebedor", login);
			} else {
				ReflectionsUtil.setValue(requisicao, "idConsultante", login);
			}
		}
	}

	/**
	 * Retorna o ID do expediente presente na lista de expedientes que não estão presentes na lista de avisos.
	 * 
	 * @param expedientes
	 * @param avisos
	 * @return Coleção de ID's.
	 */
	public static List<String> obterIDExpedientesNaoConvertidos(
			List<ProcessoParteExpediente> expedientes,
			List<AvisoComunicacaoPendente> avisos) {
		List<String> resultado = new ArrayList<String>();
		
		Integer totalExpedientes = ProjetoUtil.getTamanho(expedientes);
		Integer totalAvisos = ProjetoUtil.getTamanho(avisos);
		
		if (totalExpedientes > totalAvisos) {
			for (final ProcessoParteExpediente expediente : expedientes) {
				Predicate filtro = novoFiltroAvisoComunicacaoPendentePeloIdExpediente(expediente);
				if (!CollectionUtils.exists(avisos, filtro)) {
					int idProcessoParteExpediente = expediente.getIdProcessoParteExpediente();
					resultado.add(String.valueOf(idProcessoParteExpediente));
				}
			}
		}
		
		return resultado;
	}

	/**
	 * Retorna true se a resposta da consulta de processo foi executada com sucesso.
	 * 
	 * @param resposta RespostaConsultaProcesso
	 * @return booleano
	 */
	public static Boolean isExecucaoOK(RespostaConsultaProcesso resposta) {
		return (resposta != null && 
				(resposta.isSetSucesso() && resposta.isSucesso()) && 
				StringUtils.equalsIgnoreCase(resposta.getMensagem(), "Processo consultado com sucesso"));
	}
	
	/**
	 * Retorna true se o login da consulta de processos foi feito com sucesso.
	 * 
	 * @param resposta RespostaConsultaProcesso
	 * @return booleano
	 */
	public static Boolean isLoginOK(RespostaConsultaProcesso resposta) {
		
		return !StringUtils.startsWithIgnoreCase(resposta.getMensagem(), "Erro ao realizar login via MNI.");
	}
	
	/**
	 * Retorna novo CadastroIdentificador.
	 * 
	 * @param cpfCnpj CPF/CNPJ
	 * @return Novo CadastroIdentificador
	 */
	public static CadastroIdentificador novoCadastroIdentificador(String cpfCnpj) {
		CadastroIdentificador identificador = new CadastroIdentificador();
		identificador.setValue(InscricaoMFUtil.retiraMascara(cpfCnpj));

		return identificador;
	}

	/**
	 * Retorna true se a resposta da entrega de manifestação foi executada com sucesso.
	 * 
	 * @param resposta RespostaManifestacaoProcessual
	 * @return booleano
	 */
	public static Boolean isExecucaoOK(RespostaManifestacaoProcessual resposta) {
		return (resposta != null && 
				(resposta.isSetSucesso() && resposta.isSucesso()) && 
				StringUtils.equalsIgnoreCase(resposta.getMensagem(), "Manifestação processual recebida com sucesso"));
	}
	
	/**
	 * Retorna a string do login da requisição.
	 * 
	 * @param requisicao
	 *            Objeto da requisição (RequisicaoConsultaAvisosPendentes,
	 *            TipoConsultarTeorComunicacao, RequisicaoConsultaProcesso,
	 *            ManifestacaoProcessual, RequisicaoConsultaAlteracao)
	 * @return login da requisição.
	 */
	private static String obterStringLogin(Object requisicao) {
		String login = null;
		
		if (requisicao instanceof ManifestacaoProcessual) {
			login = ReflectionsUtil.getStringValue(requisicao, "idManifestante");
		} else if (requisicao instanceof ConfirmacaoRecebimento){
			login = ReflectionsUtil.getStringValue(requisicao, "idRecebedor");
		} else { 
			login = ReflectionsUtil.getStringValue(requisicao, "idConsultante");
		}
		
		return login;
	}

	/**
	 * Retorna novo filtro de PoloProcessual.
	 * 
	 * @param modalidade
	 *            Tipo do polo.
	 * @return Filtro de PoloProcessual.
	 */
	private static Predicate novoFiltroPolo(
			final ModalidadePoloProcessual modalidade) {
		return new Predicate() {

			@Override
			public boolean evaluate(Object objeto) {
				PoloProcessual polo = (PoloProcessual) objeto;
				return polo.getPolo() == modalidade;
			}
		};
	}

	/**
	 * Filtro de AvisoComunicacaoPendente pelo Id do expediente.
	 * 
	 * @param expediente
	 * @return Filtro.
	 */
	private static Predicate novoFiltroAvisoComunicacaoPendentePeloIdExpediente(
			final ProcessoParteExpediente expediente) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object object) {
				AvisoComunicacaoPendente aviso = (AvisoComunicacaoPendente) object;
				Identificador identificador = aviso.getIdAviso();
				String idAviso = (identificador != null ? identificador.getValue(): null);
				String idExpediente = String.valueOf(expediente.getIdProcessoParteExpediente());
				return StringUtils.equals(idAviso, idExpediente);
			}
		};
	}
	
	/*
	 * TODO 
	 * 
	 * versão 2.2.3+ MNI
	 * 
	 * Alterar este método na versão 2.2.3 ou superior do MNI para
	 * recuperar o atributo do Órgão Representação do novo schema e
	 * não utilizar parametros.
	 */
	@SuppressWarnings("unchecked")
	public static String obterIdOrgaoRepresentacao(Object requisicao) {
		Collection<Parametro> parametros = (Collection<Parametro>) ReflectionsUtil.getValue(
				requisicao, "parametros");
		return MNIParametroUtil.obterValor(parametros, MNIParametro.getIdOrgaoRepresentacao());
	}
}
