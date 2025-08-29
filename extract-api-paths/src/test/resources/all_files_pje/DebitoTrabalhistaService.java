package br.jus.csjt.pje.business.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.visao.beans.DebitoTrabalhistaBean;
import br.jus.pje.jt.entidades.DebitoTrabalhista;
import br.jus.pje.jt.entidades.DebitoTrabalhistaHistorico;
import br.jus.pje.jt.entidades.MotivoAlteracaoDebitoTrabalhista;
import br.jus.pje.jt.entidades.SituacaoDebitoTrabalhista;
import br.jus.pje.jt.entidades.TipoOperacaoEnum;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Classe responsável por gravar os dados dos débitos trabalhistas
 * 
 * @author Kelly Leal
 * @since 1.4.3-JT
 * @category PJE-JT
 * 
 */

@Name(DebitoTrabalhistaService.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class DebitoTrabalhistaService extends AbstractHome<DebitoTrabalhista> {

	@Logger
	private Log log;

	private static final long serialVersionUID = 1L;
	public static final String NAME = "debitoTrabalhistaService";

	public DebitoTrabalhista getDebitoTrabalhista(ProcessoTrf processoTrf) {

		Query q = getEntityManager().createQuery(
				"from DebitoTrabalhista rs where rs.processoParte.processoTrf = :processoTrf");
		q.setParameter("processoTrf", processoTrf);

		try {
			return (DebitoTrabalhista) q.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Método utilizado para envio de Debito Trabalhista On-Line Gera o registro
	 * que deve ser enviado, monta o XML, envia ao webservice e atualiza a data
	 * de envio.
	 * 
	 */
	public String enviarXMLDebitoTrabalhistaOnLine(DebitoTrabalhistaHistorico debitoTrabalhistaHistoricoSerEnviado) {

		String resultado = "";
		String resultadoEnvio = "";

		Map<Pessoa, List<DebitoTrabalhistaHistorico>> pessoas = new HashMap<Pessoa, List<DebitoTrabalhistaHistorico>>();

		if (pessoas.get(debitoTrabalhistaHistoricoSerEnviado.getProcessoParte().getPessoa()) == null) {
			pessoas.put(debitoTrabalhistaHistoricoSerEnviado.getProcessoParte().getPessoa(),
					new ArrayList<DebitoTrabalhistaHistorico>());
		}
		pessoas.get(debitoTrabalhistaHistoricoSerEnviado.getProcessoParte().getPessoa()).add(
				debitoTrabalhistaHistoricoSerEnviado);

		log.info("Envio On-Line: {0}", pessoas.size());

		if (pessoas.size() > 0) {

			log.info("Monta o XML para envio");
			String xml = montarXML(pessoas);
			log.info("XML para envio On-Line: {0}", xml.trim());

			log.info("Envia XML ao webservice");
			// Envia ao webservice
			resultado = this.enviarXML(xml.trim());
			log.info("Resultado do envio: {0}", resultado);

			if (resultado != null && resultado.indexOf("<sucesso>true</sucesso>") >= 0) {

				// Sucesso ao enviar
				log.info("Altera a data (de envio) do histórico de '{0}'", debitoTrabalhistaHistoricoSerEnviado
						.getProcessoParte().getPessoa().getNome());
				// Altera a data de envio no historico
				debitoTrabalhistaHistoricoSerEnviado.setDataEnvio(new Date());
				debitoTrabalhistaHistoricoSerEnviado.setRespostaEnvio(null);

				log.info("Envio On-line efetuado com sucesso.");

				return "";

			} else {

				String nomeParte = debitoTrabalhistaHistoricoSerEnviado.getProcessoParte().getPessoa().getNome();

				if (resultado != null && resultado.indexOf("<descricaoErro>") >= 0)
					resultadoEnvio = "Erro ao enviar débito para a parte "
							+ nomeParte
							+ ":\n"
							+ resultado.substring(resultado.indexOf("<descricaoErro>") + "<descricaoErro>".length(),
									resultado.indexOf("</descricaoErro>")) + "\n";

				else if (resultado != null && resultado.indexOf("This request requires HTTP authentication") >= 0)
					resultadoEnvio = "Erro ao enviar débito para a parte " + nomeParte + ":\n"
							+ "Falha de autenticação ao enviar Débito Trabalhista para o BNDT.\n";

				else if (resultado != null && resultado.indexOf("Erro ao chamar o webservice do BNDT") >= 0)
					resultadoEnvio = "Erro ao enviar débito para a parte " + nomeParte + ":\n"
							+ "Erro ao chamar o webservice do BNDT.\n";
				else
					resultadoEnvio = "Erro ao enviar débito para a parte " + nomeParte + ":\n"
							+ "Falha ao enviar Débito Trabalhista para o BNDT.\n";

				log.info("Grava erro do histórico de '{0}'", nomeParte);
				debitoTrabalhistaHistoricoSerEnviado.setRespostaEnvio(resultadoEnvio);

				return resultadoEnvio;
			}
		}

		return "";
	}

	/**
	 * Método chamado pelo Job do Cadastro Nacional de Débito Trabalhista Ele
	 * busca os registros que devem ser enviados, monta o XML, envia ao
	 * webservice e atualiza a data de envio.
	 * 
	 */
	public void enviarXMLDebitoTrabalhista() {
		String resultado = "";

		log.info("Busca as informações de débitos trabalhistas para envio.");
		Map<Pessoa, List<DebitoTrabalhistaHistorico>> pessoas = obterDebitosTrabalhistasParaEnvio();

		log.info("Quantidade de registros encontrados para envio: {0}", pessoas.size());
		if (pessoas.size() > 0) {

			log.info("Monta o XML para envio");
			String xml = montarXML(pessoas);
			log.info("XML para envio: {0}", xml.trim());

			log.info("Envia XML ao webservice");
			// Envia ao webservice
			resultado = this.enviarXML(xml.trim());
			log.info("Resultado do envio: {0}", resultado);

			if (resultado != null && resultado.indexOf("<sucesso>true</sucesso>") >= 0) {

				for (Pessoa pessoa : pessoas.keySet()) {

					List<DebitoTrabalhistaHistorico> listaDebitos = pessoas.get(pessoa);

					for (DebitoTrabalhistaHistorico debitoTrabalhistaHistorico : listaDebitos) {

						log.info("Altera a data (de envio) do histórico de '{0}'", debitoTrabalhistaHistorico
								.getProcessoParte().getPessoa().getNome());
						// Altera a data de envio no historico
						debitoTrabalhistaHistorico.setDataEnvio(new Date());
						debitoTrabalhistaHistorico.setRespostaEnvio(null);
						gravarDebitoTrabalhistaHistorico(debitoTrabalhistaHistorico);
					}
				}
				log.info("Job executado com sucesso.");

			} else {

				for (Pessoa pessoa : pessoas.keySet()) {

					List<DebitoTrabalhistaHistorico> listaDebitos = pessoas.get(pessoa);

					for (DebitoTrabalhistaHistorico debitoTrabalhistaHistorico : listaDebitos) {

						log.info("Grava erro do histórico de '{0}'", debitoTrabalhistaHistorico.getProcessoParte()
								.getPessoa().getNome());

						// Altera a data de envio no historico

						if (resultado != null && resultado.indexOf("<descricaoErro>") >= 0) {

							String resultadoEnvio = resultado.substring(resultado.indexOf("<descricaoErro>")
									+ "<descricaoErro>".length(), resultado.indexOf("</descricaoErro>"));

							debitoTrabalhistaHistorico.setRespostaEnvio(resultadoEnvio);
							gravarDebitoTrabalhistaHistorico(debitoTrabalhistaHistorico);

						}
					}

				}
				log.info("Job executado com sucesso.");

			}
		}
	}

	private String montarXML(Map<Pessoa, List<DebitoTrabalhistaHistorico>> pessoas) {
		StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><partes>");
		for (Pessoa pessoa : pessoas.keySet()) {

			List<DebitoTrabalhistaHistorico> dth = pessoas.get(pessoa);

			Collections.sort(dth, new Comparator<DebitoTrabalhistaHistorico>() {
				@Override
				public int compare(DebitoTrabalhistaHistorico debito1, DebitoTrabalhistaHistorico debito2) {
					return debito1.getDataAlteracao().compareTo(debito2.getDataAlteracao());
				}
			});

			for (DebitoTrabalhistaHistorico debitoTrabalhistaHistorico : dth) {

				xml.append("<parte>");
				if (pessoa instanceof PessoaFisica) {
					adicionarCampo(xml, "cpf", pessoa.getDocumentoCpfCnpj().replace(".", "").replace("-", ""));
				}
				if (pessoa instanceof PessoaJuridica) {
					adicionarCampo(xml, "cnpj",
							pessoa.getDocumentoCpfCnpj().replace(".", "").replace("-", "").replace("/", ""));
				}

				xml.append("<processos>");

				ProcessoTrf processoTrf = debitoTrabalhistaHistorico.getProcessoParte().getProcessoTrf();
				xml.append("<processo>");
				adicionarCampo(xml, "numero", padL(processoTrf.getNumeroSequencia().toString(), 7, '0'));
				adicionarCampo(xml, "digito", processoTrf.getNumeroDigitoVerificador().toString());
				adicionarCampo(xml, "ano", processoTrf.getAno().toString());
				adicionarCampo(xml, "orgao", processoTrf.getNumeroOrgaoJustica().toString().substring(0, 1));
				adicionarCampo(xml, "tribunal", processoTrf.getNumeroOrgaoJustica().toString().substring(1));
				adicionarCampo(xml, "vara", padL(processoTrf.getNumeroOrigem().toString(), 4, '0'));
				adicionarCampo(xml, "operacao", debitoTrabalhistaHistorico.getOperacao().getCod());
				adicionarCampo(xml, "codigo_situacao", debitoTrabalhistaHistorico.getSituacaoDebitoTrabalhista()
						.getCodXML());

				if (debitoTrabalhistaHistorico.getUsuarioResponsavel() instanceof Pessoa) {
					Pessoa pessoaResponsavel = debitoTrabalhistaHistorico.getUsuarioResponsavel();
					adicionarCampo(xml, "cpf_resp_info", pessoaResponsavel.getDocumentoCpfCnpj().replace(".", "")
							.replace("-", ""));
				}
				xml.append("</processo>");
				xml.append("</processos></parte>");
			}

		}
		xml.append("</partes>");
		return xml.toString();
	}

	private void adicionarCampo(StringBuilder xml, String campo, String valor) {
		xml.append("<" + campo + ">");
		xml.append(valor);
		xml.append("</" + campo + ">");
	}

	public String padL(String str, int tam, char padChar) {
		if (str.length() > tam) {
			str = str.substring(str.length() - tam);
		} else {
			for (int i = str.length(); i < tam; i++) {
				str = padChar + str;
			}
		}
		return str;
	}

	// Busca os débitos trabalhistas para envio
	@SuppressWarnings("unchecked")
	private Map<Pessoa, List<DebitoTrabalhistaHistorico>> obterDebitosTrabalhistasParaEnvio() {
		Map<Pessoa, List<DebitoTrabalhistaHistorico>> pessoas = new HashMap<Pessoa, List<DebitoTrabalhistaHistorico>>();

		Query q = getEntityManager().createQuery("from DebitoTrabalhistaHistorico d where d.dataEnvio is null");

		List<DebitoTrabalhistaHistorico> debitosTrabalhistas;

		try {
			debitosTrabalhistas = q.getResultList();

			for (DebitoTrabalhistaHistorico debitoTrabalhistaHistorico : debitosTrabalhistas) {

				if (pessoas.get(debitoTrabalhistaHistorico.getProcessoParte().getPessoa()) == null) {
					pessoas.put(debitoTrabalhistaHistorico.getProcessoParte().getPessoa(),
							new ArrayList<DebitoTrabalhistaHistorico>());
				}
				pessoas.get(debitoTrabalhistaHistorico.getProcessoParte().getPessoa()).add(debitoTrabalhistaHistorico);
			}

		} catch (Exception e) {
			log.error("Erro ao tentar buscar os débitos trabalhistas para envio ao BNDT");
			e.printStackTrace();
		}
		return pessoas;
	}

	/**
	 * Método que lança os movimentos referentes ao registro no BNDT (Banco
	 * Nacional de Débitos Trabalhista).
	 * 
	 * @param historico
	 *            Histórico do débito trabalhista.
	 * @author Guilherme Bispo
	 */
	public void lancarMovimentosDebitoTrabalhista(DebitoTrabalhistaHistorico historico) {

		// Código = 50085 - Descrição = Registrada a #{tipo de determinação} de dados de #{nome_da_parte} no BNDT #{complemento do tipo de determinação}
		
		String codMovimentoBNDT = CodigoMovimentoNacional.CODIGO_MOVIMENTO_PROCESSO_BNDT;

		// Preenchendo o complemento "complemento do tipo de determinação"
		String codComplementoTipoDeterminacao = "";
		String desComplementoTipoDeterminacao = "";

		if (historico.getMotivo() != null) {
			codComplementoTipoDeterminacao = historico.getMotivo().getCodComplemento();
		} else {
			codComplementoTipoDeterminacao = historico.getSituacaoDebitoTrabalhista().getCodComplemento();
		}

		if (!codComplementoTipoDeterminacao.isEmpty()) {
			desComplementoTipoDeterminacao = LancadorMovimentosService.instance().getElementoDominioByCodigoCnj(codComplementoTipoDeterminacao).getValor();
		}
		
		MovimentoAutomaticoService.preencherMovimento().deCodigo(codMovimentoBNDT)
								  .associarAoProcesso(historico.getProcessoParte().getProcessoTrf().getProcesso())
								  .comProximoComplementoVazio().preencherComTexto(historico.getOperacao().getLabel().toLowerCase())
								  .comProximoComplementoVazio().doTipoDinamico().preencherComObjeto(historico.getProcessoParte())
								  .comProximoComplementoVazio().preencherComCodigo(codComplementoTipoDeterminacao).preencherComTexto(desComplementoTipoDeterminacao)
								  .lancarMovimento();
	}

	private String enviarXML(String xml) {
		String resultado = "";

		// Definindo Usuário e Senha
		String usuario = ParametroUtil.getFromContext("usuarioWebserviceBNDT", false);
		String senha = ParametroUtil.getFromContext("senhaWebserviceBNDT", false);
		String urlWebserviceBNDT = ParametroUtil.getFromContext("urlWebserviceBNDT", false);

		// Colocando o usuário e senha nas credenciais da chamada http
		Credentials credenciais = new UsernamePasswordCredentials(usuario, senha);
		HttpClient httpclient = new HttpClient();
		httpclient.getState().setCredentials(AuthScope.ANY, credenciais);
		httpclient.getParams().setAuthenticationPreemptive(true);
		ClientExecutor clientExecutor = new ApacheHttpClient4Executor();

		try {
			// Criando a chamada HTTPS com as credenciais definidas
			// anteriormente
			ClientRequestFactory factory = new ClientRequestFactory(clientExecutor, new URI(""));
			System.setProperty("javax.net.ssl.trustStoreType", "JKS");
			// Pacote resources abaixo de onde se encontra esta classe
			System.setProperty("javax.net.ssl.trustStore",
					getClass().getResource("../../../../../../../../WEB-INF/aplicacaojt.keystore").getPath());
			System.getProperty("javax.net.ssl.trustStore");
			ClientRequest request = factory.createRequest(urlWebserviceBNDT);

			// Definindo o tipo do conteúdo(vai no cabeçalho da requisição) e o
			// conteúdo (XML para CNDT)
			request.body("application/xml", xml);

			// obtendo o resultado do WebService via POST
			resultado = request.postTarget(String.class);

		} catch (Exception e) {
			resultado = "Erro ao chamar o webservice do BNDT.";
			log.error(resultado);
			e.printStackTrace();
		}
		return resultado;
	}

	@SuppressWarnings("unchecked")
	public List<DebitoTrabalhistaBean> obterListaDebitoTrabalhistaSemSituacao() {

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		
		List<ProcessoParte> listaTodasPartes = processoTrf.getListaAutor();
		listaTodasPartes.addAll(processoTrf.getListaReu());

		List<ProcessoParte> listaPartesTipoFisicaOuJuridica = new ArrayList<ProcessoParte>();

		for (ProcessoParte processoParte : listaTodasPartes) {

			if (processoParte.getPessoa() != null) {

				if (processoParte.getPessoa().getDocumentoCpfCnpj() != null) {

					listaPartesTipoFisicaOuJuridica.add(processoParte);
				}

			}
		}

		StringBuilder hql = new StringBuilder("select ");
		hql.append("			new br.jus.cnj.pje.visao.beans.DebitoTrabalhistaBean(pp) ");
		hql.append("	  from ");
		hql.append("			ProcessoParte pp ");
		hql.append("	  where pp in (:listaPartes) ");
		hql.append("		and ");
		hql.append("			pp not in (select rsp.processoParte from DebitoTrabalhista rsp)");

		Query q = EntityUtil.createQuery(hql.toString()).setParameter("listaPartes", Util.isEmpty(listaPartesTipoFisicaOuJuridica)?null:listaPartesTipoFisicaOuJuridica);

		return q.getResultList();

	}

	@SuppressWarnings("unchecked")
	public List<DebitoTrabalhista> obterListaDebitoTrabalhistaCadastrados() {

		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();

		Query q = getEntityManager().createQuery(
				"from DebitoTrabalhista rs where rs.processoParte.processoTrf = :processoTrf ");
		q.setParameter("processoTrf", processoTrf);

		try {

			return q.getResultList();

		} catch (NoResultException e) {

			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public List<SituacaoDebitoTrabalhista> obterListaSituacoes() {

		Query q = getEntityManager().createQuery("from SituacaoDebitoTrabalhista ss where ss.operacao = :tipoOperacao");
		q.setParameter("tipoOperacao", TipoOperacaoEnum.I);

		List<SituacaoDebitoTrabalhista> listaSituacaoTrabalhista = q.getResultList();

		return listaSituacaoTrabalhista;

	}

	public void gravarDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {

		getEntityManager().persist(debitoTrabalhista);
		getEntityManager().flush();
	}

	public void gravarDebitoTrabalhistaHistorico(DebitoTrabalhistaHistorico debitoTrabalhistaHistorico) {

		getEntityManager().persist(debitoTrabalhistaHistorico);
		getEntityManager().flush();

	}

	public SituacaoDebitoTrabalhista obterSituacaoDebitoTrabalhistaPorDescricao(String descricao) {

		Query q = getEntityManager().createQuery("from SituacaoDebitoTrabalhista rs where rs.descricao = :descricao ");
		q.setParameter("descricao", descricao);

		try {

			return (SituacaoDebitoTrabalhista) q.getSingleResult();

		} catch (NoResultException e) {

			return null;
		}

	}

	public MotivoAlteracaoDebitoTrabalhista obterMotivoDebitoTrabalhistaPorDescricao(String descricao) {

		Query q = getEntityManager().createQuery(
				"from MotivoAlteracaoDebitoTrabalhista rs where rs.descricao = :descricao ");
		q.setParameter("descricao", descricao);

		try {

			return (MotivoAlteracaoDebitoTrabalhista) q.getSingleResult();

		} catch (NoResultException e) {

			return null;
		}

	}

	@SuppressWarnings("unchecked")
	public List<DebitoTrabalhistaHistorico> obterHistoricoDebitoTrabalhista(ProcessoParte processoParte) {

		Query q = getEntityManager()
				.createQuery(
						"from DebitoTrabalhistaHistorico rs where rs.processoParte = :processoParte order by rs.dataAlteracao desc");
		q.setParameter("processoParte", processoParte);

		try {
			return q.getResultList();

		} catch (NoResultException e) {
			return null;
		}

	}

	public List<MotivoAlteracaoDebitoTrabalhista> oterMotivosAlteracaoDebitoTrabalhista() {

		Query q = getEntityManager().createQuery("from MotivoAlteracaoDebitoTrabalhista");

		@SuppressWarnings("unchecked")
		List<MotivoAlteracaoDebitoTrabalhista> listaMotivos = q
				.getResultList();

		return listaMotivos;
	}

	public void excluirDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {

		getEntityManager().remove(debitoTrabalhista);
		getEntityManager().flush();

	}
}