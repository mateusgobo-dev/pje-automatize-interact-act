package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.ws.rs.NotFoundException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.json.JSONObject;

import br.com.infox.cliente.util.HttpUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name("validaProcessoMigracaoService")
public class ValidaProcessoMigracaoService implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String PROCESSO_ORIGINARIO_SEGUNDA_INSTANCIA = "Processo não pode ser migrado, pois é referenciado como originário na segunda instância.";
	private static final String PROCESSO_TRAMITANDO_SEGUNDA_INSTANCIA = "Processo não pode ser migrado, pois está tramitando em segunda instância.";
	private static final String PROCESSO_TRAMITANDO_OUTRA_INSTANCIA = "Processo não pode ser migrado, pois está tramitando em outra instância.";
	private static final String PROCESSO_CONCLUSAO_ABERTA = "Processo não pode ser migrado, pois está com conclusão aberta.";
	private static final String PROCESSO_REMESSA_INTERNA = "Processo não pode ser migrado, pois possui remessa para outros órgãos.";
	private static final String PROCESSO_COMPENTENCIA_INVALIDA = "Processo não pode ser migrado, pois competência não está habilitada para migração.";
	private static final String PROCESSO_EXPEDIENTE_ABERTO = "Processo não pode ser migrado, pois possui prazos em aberto.";
	private static final String PROCESSO_CONEXAO = "Processo associado %s não atende aos requisitos para migração. %s";
	private static final String PROCESSO_MIGRADO = "Processo bloqueado devido migração.";
	private static final String PROCESSO_NAO_ENCONTRADO = "Processo não encontrado.";
	private static final String PROCESSO_AUDIENCIA_ABERTA = "Processo não pode ser migrado, pois está com audiência marcada.";
	private static final String PROCESSO_EXPEDIENTE_BAIXADO_ARQUIVADO = "Processo não pode ser migrado, pois está baixado ou arquivado";
	private static final String PROCESSO_EXPEDIENTE_CPF_CPJ_INEXISTENTE = "Processo não pode ser migrado, pois possui a parte %s não possui CPF ou CNPJ cadastrado.";
	private static final String PROCESSO_EXPEDIENTE_CPF_CPJ_INVALIDO = "Processo não pode ser migrado, pois possui parte %s possui CPF ou CNPJ inválido.";

	private static final String MENSAGEM_ORIGINARIO_EJUD = "Processo referenciado como originário na segunda instância";
	private static final String MENSAGEM_TRAMITANDO_EJUD = "Processo correndo na segunda instância";

	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String AUTH_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private static final String CPF = "CPF";
	private static final String CPJ = "CPJ";

	private String token;

	private Boolean controleApenso = Boolean.TRUE;

	@In
	private ProcessoEventoManager processoEventoManager;

	@In
	private ProcessoTrfConexaoManager processoTrfConexaoManager;

	@In
	private ProcessoTrfManager processoTrfManager;

	@In
	private ParametroService parametroService;

	@In
	private ProcessoAudienciaManager processoAudienciaManager;

	private ProcessoJudicialManager judicialManager = (ProcessoJudicialManager) Component
			.getInstance(ProcessoJudicialManager.class);;

	public void validarSinalizacaoMigracao(String numeroProcesso, Boolean validaConexao)
			throws NegocioException, Exception {

		try {

			ProcessoTrf processoTrf = buscarProcesso(numeroProcesso);

			if (validarProcessoMigrado(processoTrf)) {
				throw new NegocioException(PROCESSO_MIGRADO);
			}
			
			if (validarCompetencia(processoTrf)) {
				throw new NegocioException(PROCESSO_COMPENTENCIA_INVALIDA);
			}

			if (validarProcessoTramiteOutraIntancia(processoTrf)) {
				throw new NegocioException(PROCESSO_TRAMITANDO_OUTRA_INSTANCIA);
			}

			if (validarConclusao(processoTrf)) {
				throw new NegocioException(PROCESSO_CONCLUSAO_ABERTA);
			}

			if (validarRemessaInterna(processoTrf)) {
				throw new NegocioException(PROCESSO_REMESSA_INTERNA);
			}

			if (validarExpedienteAberto(processoTrf)) {
				throw new NegocioException(PROCESSO_EXPEDIENTE_ABERTO);
			}

			if (validarPautaAudiencia(processoTrf)) {
				throw new NegocioException(PROCESSO_AUDIENCIA_ABERTA);
			}

			if (validarBaixaOuArquivamento(processoTrf)) {
				throw new NegocioException(PROCESSO_EXPEDIENTE_BAIXADO_ARQUIVADO);
			}
			
			if (!Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_DESABILITA_PROCESSO_CPF_CNPJ_INVALIDO)
					.getValorVariavel())) {
				validarCPFCPJ(processoTrf);;
			}

			if (!Boolean.parseBoolean(
					parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_EJUD).getValorVariavel())) {
				validarProcessoEJUD(processoTrf);
			}

			if (!Boolean.parseBoolean(
					parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_CONEXAO).getValorVariavel())) {
				validarConexaoProcesso(processoTrf, validaConexao);
			}

		} catch (NegocioException e) {
			throw e;
		}catch (NotFoundException e) {
			throw e;
		}catch (Exception e) {
			throw e;
		}	
	}

	private boolean validarProcessoMigrado(ProcessoTrf processoTrf) {

		return processoTrf.getInBloqueioMigracao();
	}

	public Boolean validarProcessoTramiteOutraIntancia(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_OUTRA_INSTANCIA)
				.getValorVariavel())) {
			return Boolean.FALSE;
		}

		return processoTrf.getInOutraInstancia();
	}

	public Boolean validarCompetencia(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(
				parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_COMPETENCIA).getValorVariavel())) {
			return Boolean.FALSE;
		}

		String idCompetencia = parametroService.findByName(Parametros.MIGRACAO_PROCESSO_LISTA_COMPETENCIA)
				.getValorVariavel();

		List<Integer> listIds = Arrays.stream(idCompetencia.split(",")).map(Integer::parseInt)
				.collect(Collectors.toList());

		return listIds.stream().anyMatch(id -> id.equals(processoTrf.getCompetencia().getIdCompetencia())) == false;

	}

	public void validarConexaoProcesso(ProcessoTrf processoTrf, Boolean validaConexao)
			throws PJeBusinessException, Exception {

		if (!validaConexao) {
			return;
		}

		Set<ProcessoTrf> list = preencherListaApensados(processoTrf);

		list.stream().forEach(associado -> {

			String numeroProcessoAssociado = associado.getNumeroProcesso();

			try {

				ProcessoTrf obj = buscarProcesso(numeroProcessoAssociado);

				if (!obj.getInBloqueioMigracao()) {
					validarSinalizacaoMigracao(associado.getNumeroProcesso(), false);
				}
			} catch (Exception e) {
				throw new NegocioException(String.format(PROCESSO_CONEXAO, numeroProcessoAssociado, e.getMessage()));
			}
		});
	}

	public Boolean validarConclusao(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(
				parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_CONCLUSAO).getValorVariavel())) {
			return Boolean.FALSE;
		}

		return processoEventoManager.existeConclusaoAberta(processoTrf);
	}

	public Boolean validarExpedienteAberto(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(parametroService
				.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_EXPENDIENTE_ABERTO).getValorVariavel())) {
			return Boolean.FALSE;
		}

		return ComponentUtil.getComponent(ProcessoParteExpedienteManager.class).existeExpedienteAberto(processoTrf);
	}

	public Boolean validarRemessaInterna(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_REMESSA_INTERNA)
				.getValorVariavel())) {
			return Boolean.FALSE;
		}

		Query q = EntityUtil.getEntityManager().createNativeQuery(parametroService
				.findByName(Parametros.MIGRACAO_PROCESSO_QUERIE_VERIFICA_REMESSA_INTERNA).getValorVariavel());

		q.setParameter("idProcesso", processoTrf.getIdProcessoTrf());
		
		Integer result = (Integer) q.getSingleResult();

		return result >= 1;
	}

	private void validarProcessoEJUD(ProcessoTrf processoTrf) throws Exception {

		try {

			String retorno = consultarProcessoEJUD(getToken(), processoTrf.getNumeroProcesso());

			if (retorno.equals(MENSAGEM_ORIGINARIO_EJUD)) {
				throw new NegocioException(PROCESSO_ORIGINARIO_SEGUNDA_INSTANCIA);
			}

			if (retorno.equals(MENSAGEM_TRAMITANDO_EJUD)) {
				throw new NegocioException(PROCESSO_TRAMITANDO_SEGUNDA_INSTANCIA);
			}

		} catch (Exception e) {
			throw e;
		}
	}

	private Boolean validarPautaAudiencia(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_DESABILITA_VALIDACAO_AUDIENCIA_ABERTA)
				.getValorVariavel())) {
			return Boolean.FALSE;
		}

		List<ProcessoAudiencia> audienciasAbertas = processoAudienciaManager
				.procurarAudienciasAbertasPorProcesso(processoTrf, null);

		return audienciasAbertas.size() > 0;
	}

	private String gerarToken() throws Exception {

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpPost httpPost = new HttpPost(
					parametroService.findByName(Parametros.URL_AUTENTICACAO_CONSULTA_PROCESSO_EJUD).getValorVariavel());
			httpPost.setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON);

			StringEntity entity = new StringEntity(criarJson().toString());
			httpPost.setEntity(entity);

			try (CloseableHttpResponse response = httpClient.execute(httpPost)) {

				verificarCodigoRetorno(response.getStatusLine().getStatusCode());
				return formatarResposta(EntityUtils.toString(response.getEntity()));
			}
		}
	}

	private String consultarProcessoEJUD(String token, String numeroProcesso) throws Exception {

		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

			HttpGet httpGet = new HttpGet(
					parametroService.findByName(Parametros.URL_BASE_CONSULTA_PROCESSO_EJUD).getValorVariavel()
							+ numeroProcesso);
			httpGet.setHeader(AUTH_HEADER, BEARER_PREFIX + token);

			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

				verificarCodigoRetorno(response.getStatusLine().getStatusCode());
				return formatarResposta(EntityUtils.toString(response.getEntity()));
			}
		}
	}

	private ProcessoTrf buscarProcesso(String numeroProcesso) {

		try {

			return judicialManager.findByNU(numeroProcesso).get(0);

		} catch (Exception e) {
			throw new NotFoundException(PROCESSO_NAO_ENCONTRADO);
		}
	}

	private String formatarResposta(String token) {

		if (token.startsWith("\"") && token.endsWith("\"")) {
			token = token.trim().substring(1, token.length() - 1);
		}
		return token;
	}

	private void verificarCodigoRetorno(int responseCode) {
		if (!HttpUtil.isStatus2xx(responseCode)) {
			throw new RuntimeException("Falha na requisição ao EJUD. Código de resposta: " + responseCode);
		}
	}

	public String getToken() throws Exception {

		if (token == null) {
			token = gerarToken();
		}

		return token;
	}

	private JSONObject criarJson() {

		JSONObject jsonInput = new JSONObject();
		jsonInput.put("login",
				ParametroUtil.getFromContext(Parametros.USUARIO_AUTENTICACAO_CONSULTA_PROCESSO_EJUD, true));
		jsonInput.put("senha",
				ParametroUtil.getFromContext(Parametros.SENHA_AUTENTICACAO_CONSULTA_PROCESSO_EJUD, true));

		return jsonInput;
	}

	public Boolean getControleApenso() {
		return controleApenso;
	}

	public void setControleApenso(Boolean controleApenso) {
		this.controleApenso = controleApenso;
	}

	private Set<ProcessoTrf> preencherListaApensados(ProcessoTrf processoTrf) throws Exception, PJeBusinessException {

		Set<ProcessoTrf> list = new HashSet<>(buscaConexao(processoTrf));

		while (getControleApenso()) {
			list.addAll(buscarProcessosVinculados(list));
		}

		list.remove(processoTrf);

		return list;
	}

	private Set<ProcessoTrf> buscaConexao(ProcessoTrf processoTrf) {

		List<ProcessoTrfConexao> listConexao = processoTrfConexaoManager
				.getListProcessosAssociados(processoTrf.getIdProcessoTrf(), true);

		return listConexao.stream().map(conexao -> conexao.getProcessoTrf()).collect(Collectors.toSet());
	}

	private Set<ProcessoTrf> buscarProcessosVinculados(Set<ProcessoTrf> listaUnica)
			throws PJeBusinessException, Exception {

		Integer controle = listaUnica.size();

		List<ProcessoTrf> list = new ArrayList<>(listaUnica);

		for (ProcessoTrf p : list) {

			listaUnica.addAll(processoTrfManager.recuperarProcessosApensados(p));
		}

		if (controle == listaUnica.size()) {
			setControleApenso(Boolean.FALSE);
		}

		return listaUnica;
	}

	private Boolean validarBaixaOuArquivamento(ProcessoTrf processoTrf) {

		if (Boolean.parseBoolean(parametroService.findByName(Parametros.MIGRACAO_DESABILITA_PROCESSO_BAIXADO_ARQUIVADO)
				.getValorVariavel())) {
			return Boolean.FALSE;
		}

		ProcessoEvento obj = processoEventoManager.recuperaUltimaMovimentacao(processoTrf);

		String cdEvento = parametroService.findByName(Parametros.MIGRACAO_PROCESSO_LISTA_EVENTOS_ARQUIVAMENTO)
				.getValorVariavel();

		List<String> listCdEvento = Arrays.stream(cdEvento.split(",")).collect(Collectors.toList());

		return listCdEvento.stream().anyMatch(cd -> cd.equals(obj.getEvento().getCodEvento())) == true;

	}

	private void validarCPFCPJ(ProcessoTrf processoTrf) {

		try {
	        List<ProcessoParte> listaPartes = montarListaPartesProcesso(processoTrf);

	        listaPartes.stream()
	            .filter(parte -> parte.getPessoa().getPessoaDocumentoIdentificacaoList().stream()
	                .noneMatch(doc -> CPF.equals(doc.getTipoDocumento().getCodTipo()) || CPJ.equals(doc.getTipoDocumento().getCodTipo())))
	            .map(parte -> parte.getPessoa().getNome())
	            .findFirst()
	            .ifPresent(nomeParte -> {
	                throw new NegocioException(String.format(PROCESSO_EXPEDIENTE_CPF_CPJ_INEXISTENTE, nomeParte));
	            });

	        listaPartes.stream()
	            .filter(parte -> parte.getPessoa().getPessoaDocumentoIdentificacaoList().stream()
	                .anyMatch(doc -> {
	                    TipoDocumentoIdentificacao tipo = doc.getTipoDocumento();
	                    String codigoTipo = tipo.getCodTipo();
	                    String numeroDocumento = doc.getNumeroDocumento();

	                    return (TipoPessoaEnum.F.equals(tipo.getTipoPessoa()) && CPF.equals(codigoTipo) && !validaCPF(numeroDocumento))
	                        || (TipoPessoaEnum.J.equals(tipo.getTipoPessoa()) && CPJ.equals(codigoTipo) && !validateCPJ(numeroDocumento));
	                }))
	            .map(parte -> parte.getPessoa().getNome())
	            .findFirst()
	            .ifPresent(nomeParte -> {
	                throw new NegocioException(String.format(PROCESSO_EXPEDIENTE_CPF_CPJ_INVALIDO, nomeParte));
	            });

	    } catch (NegocioException e) {
	        throw e;
	    } 
	}

	private List<ProcessoParte> montarListaPartesProcesso(ProcessoTrf processoTrf) {
		
		List<ProcessoParte> listaPartes = new ArrayList<>();
		listaPartes.addAll(processoTrf.getListaParteAtivo());

		listaPartes.removeIf(parte -> parte.getIsAtivo().equals(Boolean.FALSE));
		listaPartes.removeIf(parte -> parte.getPessoa().getInTipoPessoa().equals(TipoPessoaEnum.A));

		return listaPartes;
	}

	public static String removeCarecteres(String documento) {
		if (documento != null) {
			documento = documento.replaceAll("-", "");
			documento = documento.replaceAll("\\.", "");
			documento = documento.replaceAll(",", "");
			documento = documento.replaceAll("/", "");
			documento = documento.replaceAll(" ", "");
			documento = documento.replaceAll("\\(", "");
			documento = documento.replaceAll("\\)", "");
			documento = documento.replaceAll("\\_", "");
		}

		return documento;
	}

	private Boolean validaCPF(String cpf) {
		if (cpf == null) {
			return false;
		}

		cpf = removeCarecteres(cpf);

		if (cpf.equals("00000000000") || cpf.equals("11111111111") || cpf.equals("22222222222")
				|| cpf.equals("33333333333") || cpf.equals("44444444444") || cpf.equals("55555555555")
				|| cpf.equals("66666666666") || cpf.equals("77777777777") || cpf.equals("88888888888")
				|| cpf.equals("99999999999") || (cpf.length() != 11)) {
			return (false);
		}

		char dig10, dig11;
		int soma, i, r, numero, peso;

		try {
			soma = 0;
			peso = 10;
			for (i = 0; i < 9; i++) {
				numero = cpf.charAt(i) - 48;
				soma = soma + (numero * peso);
				peso = peso - 1;
			}

			r = 11 - (soma % 11);
			if ((r == 10) || (r == 11)) {
				dig10 = '0';
			} else {
				dig10 = (char) (r + 48);
			}

			soma = 0;
			peso = 11;
			for (i = 0; i < 10; i++) {
				numero = cpf.charAt(i) - 48;
				soma = soma + (numero * peso);
				peso = peso - 1;
			}

			r = 11 - (soma % 11);
			if ((r == 10) || (r == 11)) {
				dig11 = '0';
			} else {
				dig11 = (char) (r + 48);
			}

			if ((dig10 == cpf.charAt(9)) && (dig11 == cpf.charAt(10))) {
				return (true);
			} else {
				return (false);
			}
		} catch (final Exception erro) {
			return (false);
		}
	}

	private Boolean validateCPJ(String cnpj) {
		if (cnpj == null) {
			return false;
		}
		
		cnpj = removeCarecteres(cnpj);

		if (cnpj.equals("00000000000000") || cnpj.equals("11111111111111") || cnpj.equals("22222222222222")
				|| cnpj.equals("33333333333333") || cnpj.equals("44444444444444") || cnpj.equals("55555555555555")
				|| cnpj.equals("66666666666666") || cnpj.equals("77777777777777") || cnpj.equals("88888888888888")
				|| cnpj.equals("99999999999999") || (cnpj.length() != 14)) {
			return (false);
		}

		int soma = 0, digito;

		if (cnpj.length() != 14) {
			return false;
		}

		String calcCpj = cnpj.substring(0, 12);

		final char[] charCpj = cnpj.toCharArray();

		for (int i = 0; i < 4; i++) {
			if (charCpj[i] - 48 >= 0 && charCpj[i] - 48 <= 9) {
				soma += (charCpj[i] - 48) * (6 - (i + 1));
			}
		}

		for (int i = 0; i < 8; i++) {
			if (charCpj[i + 4] - 48 >= 0 && charCpj[i + 4] - 48 <= 9) {
				soma += (charCpj[i + 4] - 48) * (10 - (i + 1));
			}
		}

		digito = 11 - (soma % 11);

		calcCpj += (digito == 10 || digito == 11) ? "0" : Integer.toString(digito);

		soma = 0;
		for (int i = 0; i < 5; i++) {
			if (charCpj[i] - 48 >= 0 && charCpj[i] - 48 <= 9) {
				soma += (charCpj[i] - 48) * (7 - (i + 1));
			}
		}

		for (int i = 0; i < 8; i++) {
			if (charCpj[i + 5] - 48 >= 0 && charCpj[i + 5] - 48 <= 9) {
				soma += (charCpj[i + 5] - 48) * (10 - (i + 1));
			}
		}

		digito = 11 - (soma % 11);
		calcCpj += (digito == 10 || digito == 11) ? "0" : Integer.toString(digito);

		return cnpj.equals(calcCpj);
	}
}