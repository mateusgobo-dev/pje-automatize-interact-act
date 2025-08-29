package br.jus.cnj.pje.monitoramento;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.resteasy.annotations.providers.jaxb.IgnoreMediaTypes;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.com.infox.trf.webservice.ConsultaClienteWebService;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.pje.nucleo.entidades.Parametro;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.ws.externo.srfb.entidades.DadosReceitaPessoaFisica;

@Name("monitoramentoService")
@Path("monitoracao")
public class MonitoramentoServiceImpl implements MonitoramentoService {

	private static final String	PREFIXO_ENTITY_MANAGER		= "entityManager";

	private static final String	SUFIXO_COMPONENTE_SEAM		= ".component";

	private static final String	MONITORACAO_BANCO			= "banco";

	private static final String	MONITORACAO_RECEITA			= "receita";

	private static final String	MONITORACAO_OAB				= "oab";

	private static final String	MONITORACAO_DJE				= "dje";

	private static final String	MONITORACAO_COMPLETO		= "completo";

	private static final String	TIPO_JSON					= "json";

	private static final String	TIPO_XML					= "xml";

	private static final String	PARAMETRO_CPF_MONITORADO	= "numeroCpfMonitoracao";

	private static final String	PARAMETRO_CPF_ADMIN			= "numeroCpfAdmin";

	@Logger
	private Log					log;

	@In
	private ParametroService	parametroService;

	@In
	private PessoaFisicaService	pessoaFisicaService;

	@In(create = true, required = false)
	private PublicadorDJE		publicadorDJE;

	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@IgnoreMediaTypes(value = "text/plain")
	public Response monitorar(final @DefaultValue(MonitoramentoServiceImpl.TIPO_JSON) @QueryParam("tipo") String tipo) {
		return this.monitorar(MonitoramentoServiceImpl.MONITORACAO_COMPLETO, tipo);
	}

	@GET
	@Path("/{recurso}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@IgnoreMediaTypes(value = "text/plain")
	@Override
	public Response monitorar(final @PathParam("recurso") String recurso, final @DefaultValue(MonitoramentoServiceImpl.TIPO_JSON) @QueryParam("tipo") String tipo) {
		long l = System.currentTimeMillis();

		RespostaMonitoracao resposta = new RespostaMonitoracao();
		resposta.setMensagem("OK");
		resposta.setStatus(Boolean.TRUE);

		MediaType mediaType = this.getMediaType(tipo);

		if (MonitoramentoServiceImpl.MONITORACAO_COMPLETO.equalsIgnoreCase(recurso)) {
			this.monitorarBanco(resposta);
			this.monitorarReceita(resposta);
			this.monitorarOAB(resposta);
			// Nao monitorar o DJE, pois caso o orgao nao tenha sempre dara erro !!!
			// this.monitorarDJE(resposta);
		} else if (MonitoramentoServiceImpl.MONITORACAO_BANCO.equalsIgnoreCase(recurso)) {
			this.monitorarBanco(resposta);
		} else if (MonitoramentoServiceImpl.MONITORACAO_RECEITA.equalsIgnoreCase(recurso)) {
			this.monitorarReceita(resposta);
		} else if (MonitoramentoServiceImpl.MONITORACAO_OAB.equalsIgnoreCase(recurso)) {
			this.monitorarOAB(resposta);
		} else if (MonitoramentoServiceImpl.MONITORACAO_DJE.equalsIgnoreCase(recurso)) {
			this.monitorarDJE(resposta);
		}

		int statusCode = Response.Status.OK.getStatusCode();

		if (resposta.getItems().isEmpty()) {
			statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
			resposta.setStatus(Boolean.FALSE);
			resposta.setMensagem("Erro: Nenhum item monitorado");
		} else {
			for (ItemMonitorado itemMonitorado : resposta.getItems()) {
				if (!itemMonitorado.getStatus().booleanValue()) {
					statusCode = Response.Status.SERVICE_UNAVAILABLE.getStatusCode();
					resposta.setStatus(Boolean.FALSE);
					resposta.setMensagem(itemMonitorado.getMensagem());
					break;
				}
			}
		}

		l = System.currentTimeMillis() - l;
		resposta.setTempo(l);

		Response response = Response.ok(resposta, mediaType).status(statusCode).build();
		return response;
	}

	private void monitorarBanco(final RespostaMonitoracao resposta) {
		try {
			String[] names = Contexts.getApplicationContext().getNames();

			for (String name : names) {
				if (name.startsWith(MonitoramentoServiceImpl.PREFIXO_ENTITY_MANAGER)) {
					String realName = name.replace(MonitoramentoServiceImpl.SUFIXO_COMPONENTE_SEAM, "");
					Object obj = Component.getInstance(realName);
					if (obj instanceof EntityManager) {
						final ItemMonitorado item = new ItemMonitorado();
						item.setNome(MonitoramentoServiceImpl.MONITORACAO_BANCO);

						try {
							// Como temos banco em PostgreSQL e Oracle
							// e a consulta basica deles sao diferentes
							// foi necessario pegar a conexao para definir
							// qual a forma da consula que sera feita
							EntityManager em = (EntityManager) obj;
							Object delegate = em.getDelegate();
							if (delegate instanceof Session) {
								Session session = (Session) delegate;
								GetConnectionMetadataWork work = new GetConnectionMetadataWork();
								session.doWork(work);

								String driverName = work.getDriverName();
								String sql = work.getSql();

								if (sql == null) {
									throw new IllegalStateException("Banco nao suportado " + driverName);
								}
								Query query = em.createNativeQuery(sql);
								query.getSingleResult();

								item.setMensagem("OK: EntityManager " + realName + "(" + driverName + ")");
								item.setStatus(Boolean.TRUE);
							}

						} catch (Exception e) {
							this.log.error(e.getMessage(), e);
							item.setMensagem("Erro: [" + realName + "]" + e.getMessage());
							item.setStatus(Boolean.FALSE);
						}

						resposta.getItems().add(item);
					}
				}
			}

		} catch (Exception e) {
			this.log.error(e.getMessage(), e);
			ItemMonitorado item = new ItemMonitorado();
			item.setNome(MonitoramentoServiceImpl.MONITORACAO_BANCO);
			item.setMensagem("Erro: " + e.getMessage());
			item.setStatus(Boolean.FALSE);
			resposta.getItems().add(item);
		}
	}

	private void monitorarReceita(final RespostaMonitoracao resposta) {
		ItemMonitorado item = new ItemMonitorado();
		item.setNome(MonitoramentoServiceImpl.MONITORACAO_RECEITA);
		try {
			ConsultaClienteWebService cliente = ConsultaClienteWebService.instance();
			if (cliente == null) {
				throw new IllegalStateException("Cliente para receita nao encontrado");
			}

			PessoaFisica pessoaFisica = this.pessoaFisicaService.findByCPF(this.recuperarCPFAdmin(), this.recuperarCPFMonitorado());
			if (pessoaFisica == null) {
				throw new IllegalStateException("Usuario do sistema nao encontrado");
			}

			Contexts.getEventContext().set("usuarioSistema", pessoaFisica);
			Contexts.getEventContext().set("usuarioLogado", pessoaFisica);

			String cpf = this.recuperarCPFMonitorado();
			String nome = null;

			DadosReceitaPessoaFisica dado = (DadosReceitaPessoaFisica) cliente.consultaDados(TipoPessoaEnum.F, cpf, false);

			if (dado != null) {
				nome = dado.getNome();
			}

			item.setMensagem("OK: CPF " + cpf + ", Nome " + nome);
			item.setStatus(Boolean.TRUE);
		} catch (Exception e) {
			this.log.error(e.getMessage(), e);
			item.setMensagem("Erro: " + e.getMessage());
			item.setStatus(Boolean.FALSE);
		}
		resposta.getItems().add(item);
	}

	private void monitorarOAB(final RespostaMonitoracao resposta) {
		ItemMonitorado item = new ItemMonitorado();
		item.setNome(MonitoramentoServiceImpl.MONITORACAO_OAB);
		try {
			ConsultaClienteOAB cliente = ConsultaClienteOAB.instance();
			if (cliente == null) {
				throw new IllegalStateException("Cliente para a OAB nao encontrado");
			}
			String cpf = this.recuperarCPFMonitorado();
			String nome = null;

			cliente.consultaDados(cpf, false);
			if ((cliente.getDadosAdvogadoList() != null) && (cliente.getDadosAdvogadoList().size() > 0)) {
				nome = cliente.getDadosAdvogadoList().iterator().next().getNome();
			}

			item.setMensagem("OK: CPF " + cpf + ", Nome " + nome);
			item.setStatus(Boolean.TRUE);
		} catch (Exception e) {
			this.log.error(e.getMessage(), e);
			item.setMensagem("Erro: " + e.getMessage());
			item.setStatus(Boolean.FALSE);
		}
		resposta.getItems().add(item);
	}

	private void monitorarDJE(final RespostaMonitoracao resposta) {
		ItemMonitorado item = new ItemMonitorado();
		item.setNome(MonitoramentoServiceImpl.MONITORACAO_DJE);
		try {
			if (this.publicadorDJE == null) {
				throw new IllegalStateException("Publicador DJE nao encontrado");
			}

			Calendar calendar = Calendar.getInstance();

			List<String> list = this.publicadorDJE.verificaPublicacoesPorData(calendar);

			item.setMensagem("OK: Publicacoes " + list.size());
			item.setStatus(Boolean.TRUE);
		} catch (Exception e) {
			this.log.error(e.getMessage(), e);
			item.setMensagem("Erro: " + e.getMessage());
			item.setStatus(Boolean.FALSE);
		}
		resposta.getItems().add(item);
	}

	private String recuperarCPFAdmin() {
		return this.recuperarCPF(MonitoramentoServiceImpl.PARAMETRO_CPF_ADMIN);
	}

	private String recuperarCPFMonitorado() {
		return this.recuperarCPF(MonitoramentoServiceImpl.PARAMETRO_CPF_MONITORADO);
	}

	private String recuperarCPF(final String nomeParametro) {
		if (this.parametroService == null) {
			throw new IllegalStateException("ParametroService nulo");
		}

		Parametro parametro = this.parametroService.findByName(nomeParametro);

		if (parametro == null) {
			throw new IllegalStateException("Parametro '" + nomeParametro + "' nao cadastrado");
		}

		String cpf = parametro.getValorVariavel();

		if ((cpf == null) || (cpf.trim().length() != 11)) {
			throw new IllegalStateException("Parametro '" + nomeParametro + "' possui um valor invalido '" + cpf + "'");
		}

		return cpf;
	}

	private MediaType getMediaType(final String tipo) {
		MediaType mediaType = null;

		if (MonitoramentoServiceImpl.TIPO_JSON.equals(tipo)) {
			mediaType = MediaType.APPLICATION_JSON_TYPE;
		} else if (MonitoramentoServiceImpl.TIPO_XML.equals(tipo)) {
			mediaType = MediaType.APPLICATION_XML_TYPE;
		} else {
			// Default
			this.log.warn("Tipo de midia invalido '#0'", tipo);
			mediaType = MediaType.APPLICATION_JSON_TYPE;
		}

		return mediaType;
	}

	public static class GetConnectionMetadataWork implements Work {

		private String	driverName;

		private String	sql;

		@Override
		public void execute(Connection connection) throws SQLException {
			this.driverName = connection.getMetaData().getDriverName();

			if (this.driverName.indexOf("PostgreSQL") != -1) {
				this.sql = "SELECT 1";
			} else if (this.driverName.indexOf("Oracle") != -1) {
				this.sql = "SELECT 1 FROM DUAL";
			}
		}

		public String getDriverName() {
			return this.driverName;
		}

		public String getSql() {
			return this.sql;
		}
	}
	
}
