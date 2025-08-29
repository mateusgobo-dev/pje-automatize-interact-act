package br.com.infox.cliente;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Session;
import org.jboss.seam.security.management.PasswordHash;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.jt.enums.DestinoRemessaEnum;
import br.jus.pje.jt.enums.MotivoRecebimentoEnum;
import br.jus.pje.jt.enums.MotivoRemessaEnum;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe para remessa de processos
 * 
 * Os detalhes da conexão estão no arquivo remessa_processo.properties
 * 
 * @author ruiz/marcone
 * 
 */
public abstract class AbstractRemessaProcesso extends Thread {
	private Integer idUfAtual;
	private Integer idUfDestino;
	private String nrProcessoAtual;
	private Integer idProcessoAtual;
	private Connection connFrom, connTo, connFrom_bin, connTo_bin, connFromHistLog;
	private DatabaseMetaData dbMetaFrom;
	private Set<String> populatedTables = new HashSet<String>();
	private Set<String> logTablesExported = new HashSet<String>();
	private static Properties properties;
	private final String urlFrom;
	private final String urlFrom_bin;
	private Integer idProcesso;
	private int idHistAtual;
	private int idHistLogAtual;
	private StringBuilder logErroGeral = new StringBuilder();
	private StringBuilder logGeral = new StringBuilder();
	private StringBuilder logAviso = new StringBuilder();
	private int qtdOperacoes = 0;
	private Map<String, String> sqlInsertTable = new HashMap<String, String>();
	private Map<String, String[]> qualifiedFK = new HashMap<String, String[]>();
	private Map<Integer, Set<String>> usuarioDuplicadoList = new HashMap<Integer, Set<String>>();
	private final String quebra = System.getProperty("line.separator");

	public AbstractRemessaProcesso() throws SQLException {
		InputStream inStream;
		properties = new Properties();
		inStream = AbstractRemessaProcesso.class.getResourceAsStream("/remessa_processo.properties");
		try {
			properties.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setIdUfDestino(getPrefixUF(getUfToProperty()));
		this.urlFrom = pegaConexaoLocal();
		this.urlFrom_bin = this.urlFrom.substring(0, this.urlFrom.indexOf("?")) + "_bin"
				+ this.urlFrom.substring(this.urlFrom.indexOf("?"));
	}

	private String pegaConexaoLocal() throws SQLException {
		if (this.urlFrom != null) {
			return this.urlFrom;
		}
		String resultado = "";
		NovoWork novoWork = new NovoWork();
		((Session) EntityUtil.getEntityManager().getDelegate()).doWork(novoWork);
		resultado = novoWork.getConnUrl();
		return resultado;
	}

	public Boolean verificaConexao() {
		try {
			initConnections();
		} catch (Exception e) {
			return Boolean.FALSE;
		}
		if ((connFrom != null) && (connFrom_bin != null) && (connTo != null) && (connTo_bin != null)) {
			try {
				closeConnections();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	protected void initConnections() throws SQLException {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new SQLException("JDBC não foi encontrado no CP");
		}

		// --------------------
		connFromHistLog = DriverManager.getConnection("jdbc:postgresql://" + this.urlFrom);
		connFromHistLog.setAutoCommit(false);
		// --------------------
		connFrom = DriverManager.getConnection("jdbc:postgresql://" + this.urlFrom);
		connFrom.setAutoCommit(false);
		connFrom_bin = DriverManager.getConnection("jdbc:postgresql://" + this.urlFrom_bin);
		connFrom_bin.setAutoCommit(false);

		ResultSet rsUfFrom = connFrom.prepareStatement(
				"select vl_variavel from core.tb_parametro where nm_variavel='cdUfRemessaProcesso';").executeQuery();
		if (rsUfFrom.next()) {
			setIdUfAtual(getPrefixUF(rsUfFrom.getString("vl_variavel")));
		} else {
			throw new SQLException("Parâmetro cdUfRemessaProcesso não definido!");
		}
		setIdUfDestino(getPrefixUF(getUfToProperty()));

		if (getIdUfDestino().equals(getIdUfAtual())) {
			throw new SQLException("Origem e destino iguais");
		}

		// Pega a URL destino baseado no UF destino da base atual(Origem)
		StringBuilder builder = new StringBuilder();
		builder.append("select ");
		builder.append(" case when ((select vl_variavel from core.tb_parametro where nm_variavel='inRemessaProcessoProducao') :: boolean) then h.ds_url ");
		builder.append(" else h.ds_url_homologacao end as ds_url_destino, ds_login, ds_senha ");
		builder.append(" from client.tb_remessa_processo_host h where h.id_sessao_destino=?;");
		PreparedStatement psDestino = connFrom.prepareStatement(builder.toString());
		psDestino.setInt(1, getIdUfDestino());
		ResultSet rsDestino = psDestino.executeQuery();
		if (rsDestino.next()) {
			Crypto crypto = new Crypto("PJE_remessa_2001#@1!");
			String dsSenha = crypto.decodeDES(rsDestino.getString("ds_senha"));
			String dsLogin = rsDestino.getString("ds_login");
			String urlTo = rsDestino.getString("ds_url_destino");
			dbMetaFrom = connFrom.getMetaData();
			connTo = DriverManager.getConnection("jdbc:postgresql://" + urlTo + "?loginTimeout=10", dsLogin, dsSenha);
			connTo.setAutoCommit(false);
			connTo_bin = DriverManager.getConnection("jdbc:postgresql://" + urlTo + "_bin?loginTimeout=10", dsLogin,
					dsSenha);
			connTo_bin.setAutoCommit(false);
		} else {
			throw new SQLException(
					"Destino não cadastrado na tabela tb_remessa_processo_host ou parâmetro inRemessaProcessoProducao não foi definido.");
		}
	}

	public void rollbackTrasaction() throws SQLException {
		connTo.rollback();
		connTo_bin.rollback();
		connFrom.rollback();
		connFrom_bin.rollback();
		connTo.setAutoCommit(false);
		connTo_bin.setAutoCommit(false);
		connFrom.setAutoCommit(false);
		connFrom_bin.setAutoCommit(false);
		// --------------------
		// connFromHistLog.rollback();
		// connFromHistLog.setAutoCommit(false);

		String rollback = "--rollback;(" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + ")";
		escreverNoLog(rollback);
	}

	public void closeConnections() throws SQLException {
		try {

			escreverNoLog(" - INICIO - COMMIT - DESTINO BIN");
			connTo_bin.commit();
			escreverNoLog(" - FIM - COMMIT - DESTINO BIN");

			escreverNoLog(" - INICIO - COMMIT - DESTINO");
			connTo.commit();
			escreverNoLog(" - FIM - COMMIT - DESTINO");

			escreverNoLog(" - INICIO - FECHANDO_CONEXÃO - DESTINO");
			connTo.close();
			escreverNoLog(" - FIM - FECHANDO_CONEXÃO - DESTINO");

			escreverNoLog(" - INICIO - FECHANDO_CONEXÃO - DESTINO BIN");
			connTo_bin.close();
			escreverNoLog(" - FIM - FECHANDO_CONEXÃO - DESTINO BIN");

			escreverNoLog(" - INICIO - COMMIT - ORIGEM");
			connFrom.commit();
			escreverNoLog(" - FIM - COMMIT - ORIGEM");

			escreverNoLog(" - INICIO - FECHANDO_CONEXÃO - ORIGEM");
			connFrom.close();
			escreverNoLog(" - FIM - FECHANDO_CONEXÃO - ORIGEM");

			escreverNoLog(" - INICIO - FECHANDO_CONEXÃO - ORIGEM BIN");
			connFrom_bin.close();
			escreverNoLog(" - FIM - FECHANDO_CONEXÃO - ORIGEM BIN");

		} catch (SQLException e) {
			escreverNoErroLog("Erro ao fechar as conexões");
			escreverNoErroLog(e.toString());
			throw e;
		}
	}

	public void closeLogConnection() throws SQLException {
		try {
			escreverNoLog(" - FECHANDO_CONEXÃO... - LOG ORIGEM");
			connFromHistLog.close();
		} catch (SQLException e) {
			System.out.println("Erro ao fechar a conexão de log");
			System.out.println(e.toString());
			throw e;
		}
	}

	@Override
	public void run() {
		if (this.idProcesso != null && this.idProcesso > 0) {
			try {
				migrarDados(this.idProcesso);
			} catch (Exception e) {
				try {
					e.printStackTrace();
					escreverNoErroLog("Erro dentro da thread: " + e.getMessage());
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Método utilizado para migrar o processo e os dados associados a ele.
	 * 
	 * @param idProcesso
	 * @throws SQLException
	 */
	public void migrarDados(Integer idProcesso) throws SQLException {
		try {
			setIdProcessoAtual(idProcesso);
			this.logGeral.append("--Iniciando as conexões ....");
			initConnections();
			escreverNoLog("--Atualizando Processos Conexos ....");
			popularProcessoTrfConexao(idProcesso);
			escreverNoLog("--Atualizando variáveis ....");
			atualizaVariaveis(idProcesso);
			escreverNoLog("--Migrando dados das tabelas ....");
			migrarProcesso(idProcesso);
			escreverNoLog("--Migrando documentos binários ....");
			migrarDocumentosBinarios(idProcesso);
			// escreverNoLog("--Ajustes finais ....");
			// ajustesFinais(idProcesso);
			//Eventos desnecessários no Ambiente da JT
//			escreverNoLog("--Lançando eventos ....");
//			lancarEventos(idProcesso);
			escreverNoLog("--Lançando movimentos do CSJT ....");
			lancarMovimentosCSJT(idProcesso);
			escreverNoLog("--Setando informações em ProcessoDocumentoTrf ....");
			setarInstanciaOrigem(idProcesso);
			escreverNoLog("--Agendando remessa para criar seu fluxo....");
			agendarProcesso(idProcesso);
			escreverNoLog("--Log de tabelas envolvidas na replicação");
			escreverNoLog("--".concat(this.logTablesExported.toString()));
			escreverNoLog("--Concluído");
			escreverNoLog("--Concluído com sucesso");
			atualizaStatusRemessa("F");
		} catch (Throwable e) {
			e.printStackTrace();
			rollbackTrasaction();
			escreverNoErroLog(e.toString());
			atualizaStatusRemessa("E");
			throw new SQLException(e);
		} finally {
			escreverNoLog("--Fechando conexões ....");
			try {
				// updateHistLog(idProcesso);
				// atualizarLog();
				closeConnections();
			} catch (SQLException e) {
				escreverNoErroLog(e.toString());
				throw e;
			} finally {
				closeLogConnection();
			}
		}
	}

	private void popularFKs() throws SQLException {
		ResultSet rsFK = connFrom
				.prepareStatement(
						"SELECT nl.nspname as o_schema, tbl.relname as o_table, al.attname as o_column, nr.nspname as fk_schema, tbr.relname as fk_table, ar.attname as fk_column "
								+ "FROM pg_constraint c "
								+ "LEFT JOIN pg_class tbl ON (c.conrelid = tbl.oid) "
								+ "LEFT JOIN pg_class tbr ON (c.confrelid = tbr.oid) "
								+ "LEFT JOIN pg_namespace nl ON (tbl.relnamespace = nl.oid) "
								+ "LEFT JOIN pg_namespace nr ON (tbr.relnamespace = nr.oid) "
								+ "LEFT JOIN pg_attribute al ON (c.conrelid = al.attrelid and c.conkey[1] = al.attnum and al.attnum > 0) "
								+ "LEFT JOIN pg_attribute ar ON (c.confrelid = ar.attrelid and c.confkey[1] = ar.attnum and ar.attnum > 0) "
								+ "WHERE c.contype = 'f' AND "
								+ "nl.nspname in ('acl', 'core', 'client') "
								+ "order by 1").executeQuery();

		while (rsFK.next()) {
			if (!checkInBlackList(rsFK.getString("o_schema"), rsFK.getString("o_table"), rsFK.getString("o_column"))) {
				String[] resultado = new String[3];
				resultado[0] = rsFK.getString("fk_schema");
				resultado[1] = rsFK.getString("fk_table");
				resultado[2] = rsFK.getString("fk_column");
				addQualifiedFK(rsFK.getString("o_schema"), rsFK.getString("o_table"), rsFK.getString("o_column"),
						resultado);
			}
		}
	}

	private void ajustesFinais(Integer idProcesso) throws SQLException {
		// AJUSTE REFERENTE AO PARÂMETRO in_tutela_liminar QUE NÃO EXISTE NO
		// 2ºGRAU
		ResultSet rsProcessoOrigem = createStatement(connFrom, "client", "tb_processo_trf", idProcesso);
		if (rsProcessoOrigem.next()) {
			PreparedStatement psProcessoTrf = connTo
					.prepareStatement("update client.tb_processo_trf p SET "
							+ "in_pedido_liminar=?, in_antecipacao_tutela=? "
							+ "where id_processo_trf=(select ip.id_processo from core.tb_processo ip where ip.id_pk_tb_processo_pg=? and ip.id_sessao_pg=?);");
			psProcessoTrf.setObject(1, rsProcessoOrigem.getObject("in_tutela_liminar"));
			psProcessoTrf.setObject(2, rsProcessoOrigem.getObject("in_tutela_liminar"));
			psProcessoTrf.setInt(3, idProcesso);
			psProcessoTrf.setInt(4, getIdUfAtual());
			psProcessoTrf.execute();
			logDataSecaoAtual(psProcessoTrf);
		}
	}

	private void atualizaVariaveis(Integer idProcesso) throws SQLException {
		PreparedStatement psHisLog = connFromHistLog
				.prepareStatement("select id_remessa_processo_hist_log, id_remessa_processo_historico "
						+ "from client.tb_remessa_proc_hist_log rl "
						+ "where rl.id_remessa_processo_historico=(select id_remessa_processo_historico from client.tb_remessa_proc_historico where id_processo_trf=? and id_sessao_destino=? and in_remetido=false and in_status<>'F' order by dt_cadastro desc limit 1) "
						+ "order by rl.dt_cadastro desc limit 1");
		psHisLog.setInt(1, idProcesso);
		psHisLog.setInt(2, getIdUfDestino());
		ResultSet rsHistLog = psHisLog.executeQuery();
		if (rsHistLog.next()) {
			setIdHistLogAtual(rsHistLog.getInt("id_remessa_processo_hist_log"));
			setIdHistAtual(rsHistLog.getInt("id_remessa_processo_historico"));
		}
		popularFKs();
	}

	/*
	 * private void updateHistLog(Integer idProcesso) throws SQLException {
	 * String sqlUpdHistLogRemessa =
	 * "UPDATE tb_remessa_processo_hist_log " +
	 * "SET ds_log_remessa=?, ds_log_erro=? " +
	 * "WHERE id_remessa_processo_historico=?;"; try { PreparedStatement
	 * psUpdHistLogRemessa; psUpdHistLogRemessa =
	 * connFromHistLog.prepareStatement(sqlUpdHistLogRemessa);
	 * psUpdHistLogRemessa.setObject(1, this.logGeral.toString());
	 * psUpdHistLogRemessa.setObject(2, this.logErroGeral.toString());
	 * psUpdHistLogRemessa.setInt(3, getIdHistLogAtual());
	 * psUpdHistLogRemessa.execute(); } catch (SQLException e) {
	 * escreverNoErroLog("Erro no update da tabela de log");
	 * escreverNoErroLog(e.toString()); throw e; } }
	 */

	/**
	 * Função que retorna uma lista de avisos referente a remessa de processo;
	 * 
	 * @param idProcesso
	 * @return
	 * @throws SQLException
	 */
	public String getCheckList(Integer idProcesso) throws SQLException {
		String sql = "select "
				+ "(select max(ipe.dt_atualizacao) from core.tb_processo_evento ipe inner join client.tb_evento_processual ie on (ipe.id_evento=ie.id_evento_processual) where ipe.id_processo=? and ie.cd_evento='192') as dt_recebido, "
				+ "(select max(ipe.dt_atualizacao) from core.tb_processo_evento ipe inner join client.tb_evento_processual ie on (ipe.id_evento=ie.id_evento_processual) where ipe.id_processo=? and ie.cd_evento='123A') as dt_remessa";

		String sqlPjOrigem = "select pj.nr_cnpj, u.id_usuario, u.ds_nome from client.tb_pessoa_juridica pj left join core.tb_usuario_login u on (pj.id_pessoa_juridica=u.id_usuario) "
				+ "inner join ( "
				+ "select distinct * from "
				+ "(select pp.id_pessoa, pp.id_processo_trf from tb_processo_parte pp where id_pessoa is not null "
				+ "union all "
				+ "select ppe.id_pessoa_parte, pe.id_processo_trf from tb_processo_expediente pe left join tb_proc_parte_expediente ppe on (pe.id_processo_expediente=ppe.id_processo_expediente) where ppe.id_pessoa_parte is not null "
				+ "union all "
				+ "select ppe.id_pessoa_parte, pe.id_processo_trf from tb_processo_expediente pe left join tb_proc_parte_expediente ppe on (pe.id_processo_expediente=ppe.id_processo_expediente) where ppe.id_pessoa_ciencia is not null) pe "
				+ ") ppe on (pj.id_pessoa_juridica=ppe.id_pessoa and ppe.id_processo_trf=?)";

		String sqlHasPj = "select id_pessoa from tb_pess_doc_identificacao where trim(lower(to_ascii(nr_documento_identificacao)))=trim(lower(to_ascii(?))) and in_ativo=true and in_usado_falsamente=false;";

		StringBuilder resultado = new StringBuilder();
		ResultSet rsProc = createStatement(connFrom, "core", "tb_processo", idProcesso);
		if (rsProc.next()) {
			if (!rsProc.getString("nr_processo").isEmpty()) {
				ResultSet rsProcDest = createStatement(connTo, "core", "tb_processo", "nr_processo",
						rsProc.getString("nr_processo"));
				if (rsProcDest.next()) {
					resultado.append("Processo já existe no destino;\n");
					ResultSet rsEventos = createStatementWithValues(connFrom, sql, idProcesso, idProcesso);
					if (rsEventos.next()) {
						if (rsEventos.getDate("dt_recebido") != null
								&& (rsEventos.getDate("dt_recebido").before(rsEventos.getDate("dt_remessa")))) {
							resultado.append("Processo não pode ser enviado. Ele não retornou ainda.\n");
						}
					}
				} else {
					ResultSet rsPjOrigem = createStatementWithValues(connFrom, sqlPjOrigem, idProcesso);
					while (rsPjOrigem.next()) {
						if (!createStatementWithValues(connTo, sqlHasPj, rsPjOrigem.getString("nr_cnpj")).next()) {
							resultado.append("PJ não cadastrado no destino: " + rsPjOrigem.getObject("id_usuario")
									+ ":" + rsPjOrigem.getObject("nr_cnpj") + " - " + rsPjOrigem.getObject("ds_nome")
									+ ";\n");
						}
					}
				}
			} else {
				resultado.append("Processo sem número;\n");
			}
		} else {
			resultado.append("Processo inexistente na origem");
		}
		return resultado.toString();
	}

	/**
	 * Método para setar a instância original do processo na tabela ProcessoTrf
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void setarInstanciaOrigem(int id) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select nr_processo from tb_processo where id_processo = ? ");
		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		psFrom.setObject(1, id);
		ResultSet nrProcessoSet = psFrom.executeQuery();
		if (nrProcessoSet.next()) {
			escreverNoLog("--> numero do processo " + nrProcessoSet.getString("nr_processo"));
			sql = new StringBuilder();
			sql.append("Insert into tb_processo_documento_trf ");
			sql.append("		(id_processo_documento_trf,in_decisao_terminativa,cd_instancia_origem) ");
			sql.append("Select 	doc.id_processo_documento,'N','1' ");
			sql.append("		From tb_processo_documento as doc ");
			sql.append("		Inner Join tb_processo as proc on doc.id_processo = proc.id_processo ");
			sql.append("		Where proc.nr_processo = ?");
			PreparedStatement psTo = connTo.prepareStatement(sql.toString());
			psTo.setObject(1, nrProcessoSet.getString("nr_processo"));
			psTo.execute();
			logDataSecaoAtual(psTo);
		} else {
			escreverNoErroLog("Erro ao tentar encontrar o Processo");
		}
	}
	
	
	/**
	 * Método para lançar os movimentos e respectivos complementos do CSJT.
	 * Atentar para o fato de que caso esses movimentos  ou algum de
	 * seus complementos for alterado o código deve ser revisado, pois como ele 
	 * gera comandos sql ao invés de pesquisar valores em bancos, os dados podem
	 * ficar desatualizados. 
	 * @param idProcesso identificador do processo que será remetido ao 2º grau
	 * @throws SQLException
	 */
	private void lancarMovimentosCSJT(int idProcesso) throws SQLException{
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		Timestamp now = new Timestamp(cal.getTimeInMillis());
		
		StringBuilder sql = new StringBuilder();
		sql.append("select nr_processo from tb_processo where id_processo = ? ");
		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		PreparedStatement psTo = connTo.prepareStatement(sql.toString());
		psFrom.setObject(1, idProcesso);
		ResultSet nrProcessoSet = psFrom.executeQuery();
		
		if(nrProcessoSet.next()){
		
			//Insere o movimento do CSJT de remessa de processo e respectivos complementos na base de 1º grau
			inserirEventoMovimentoRemessa(now, nrProcessoSet, psFrom, connFrom);
			//Insere o movimento do CSJT de remessa de processo e respectivos complementos na base de 2º grau
			inserirEventoMovimentoRemessa(now, nrProcessoSet, psTo, connTo);
			
			logDataSecaoAtual(psTo);
		}
		
	}

	private void inserirEventoMovimentoRemessa(Timestamp now,
			ResultSet nrProcessoSet, PreparedStatement ps,Connection con) throws SQLException {
		// Estava fixo o usuário com id de usuario '1' na inserção de movimentos, corrigido para que atenda
		// quando não houver o usuário '1', nesse caso vai usar o menor id de usuario
		PreparedStatement psIdUsuario = connTo.prepareStatement("select min(id_usuario) from tb_usuario_login where id_usuario = 1 or not exists (select id_usuario from acl.tb_usuario_login where id_usuario = 1) ");
		ResultSet nrUsuario = psIdUsuario.executeQuery();
		nrUsuario.next();
		int idUsuario = nrUsuario.getInt(1);
				
		StringBuilder sql;
		
		sql = new StringBuilder();
		sql.append("INSERT INTO tb_processo_evento(");
		sql.append("		id_processo, id_usuario, dt_atualizacao,"); 
		sql.append("        tp_processo_evento, ds_texto_final_externo, ds_texto_final_interno, ");
		sql.append("        in_visibilidade_externa, ds_texto_parametrizado, id_evento) ");
		sql.append("VALUES ((SELECT ID_PROCESSO FROM TB_PROCESSO P WHERE P.NR_PROCESSO = ?), ?, ?, ?, ?, ?, ?, ?, " +
				"(select id_evento_processual from tb_evento_processual where cd_evento = " +
					CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_REMESSA
				+"))");
		
		ps = con.prepareStatement(sql.toString());
		ps.setObject(1, nrProcessoSet.getString("nr_processo"));
		ps.setObject(2, idUsuario);
		ps.setObject(3, now);
		//Tipo da entidade MovimentoProcesso
		ps.setObject(4, "M");
		ps.setObject(5, "Remetidos os autos para "+ DestinoRemessaEnum.D.getLabel() + " " +MotivoRemessaEnum.PPREME.getLabel());
		ps.setObject(6, "Remetidos os autos para "+ DestinoRemessaEnum.D.getLabel() + " " +MotivoRemessaEnum.PPREME.getLabel());
		ps.setObject(7, true);
		ps.setObject(8, "Remetidos os autos para #{destino} #{motivo da remessa}");
		
		ps.execute();
		logDataSecaoAtual(ps);
		
		
		sql = new StringBuilder();
		
		sql.append("INSERT INTO tb_complemento_segmentado(");
		sql.append("        vl_ordem, ds_texto, ds_valor_complemento, ");
		sql.append("        in_visibilidade_externa, ");
		sql.append("        in_multivalorado, id_tipo_complemento, id_movimento_processo)");
		sql.append(" VALUES (?, ?, ?, ?, ?, " +
				   "(select id_tipo_complemento from tb_tipo_complemento where ds_label='"+LancadorMovimentosService.COMPLEMENTO_DESTINO+"'), " +
				   "(select MAX(id_processo_evento) from tb_processo_evento));");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setObject(1, 0);
		ps.setObject(2, DestinoRemessaEnum.D.getCodigo());
		ps.setObject(3, DestinoRemessaEnum.D.getLabel());
		ps.setObject(4, true);
		ps.setObject(5, false);
		
		ps.execute();
		logDataSecaoAtual(ps);
		
		
		sql = new StringBuilder();
		
		sql.append("INSERT INTO tb_complemento_segmentado(");
		sql.append("        vl_ordem, ds_texto, ds_valor_complemento, ");
		sql.append("        in_visibilidade_externa, ");
		sql.append("        in_multivalorado, id_tipo_complemento, id_movimento_processo)");
		sql.append(" VALUES (?, ?, ?, ?, ?, " +
				   "(select id_tipo_complemento from tb_tipo_complemento where ds_label like '"+LancadorMovimentosService.COMPLEMENTO_MOTIVO_REMESSA+"'), " +
				   "(select MAX(id_processo_evento) from tb_processo_evento));");
		
		ps = con.prepareStatement(sql.toString());
		
		ps.setObject(1, 0);
		ps.setObject(2, MotivoRemessaEnum.PPREME.getCodigo());
		ps.setObject(3, MotivoRemessaEnum.PPREME.getLabel());
		ps.setObject(4, true);
		ps.setObject(5, false);
		
		ps.execute();
		logDataSecaoAtual(ps);
	}

	/**
	 * Método para criar os eventos de remessa na origem e no destino
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void lancarEventos(int id) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select nr_processo from tb_processo where id_processo = ? ");
		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		psFrom.setObject(1, id);
		ResultSet nrProcessoSet = psFrom.executeQuery();
		sql = new StringBuilder();
		if (nrProcessoSet.next()) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			Timestamp now = new Timestamp(cal.getTimeInMillis());

			escreverNoLog("--> numero do processo " + nrProcessoSet.getString("nr_processo"));
			sql = new StringBuilder();
			sql.append(" INSERT INTO TB_PROCESSO_EVENTO ");
			sql.append(" (ID_PROCESSO, ID_EVENTO, ID_USUARIO, DT_ATUALIZACAO) VALUES ");
			sql.append(" ((SELECT ID_PROCESSO FROM TB_PROCESSO P WHERE P.NR_PROCESSO = ?),");
			sql.append(" (SELECT ep.id_evento_processual FROM TB_EVENTO_PROCESSUAL EP WHERE EP.CD_EVENTO = '123A'), ");
			sql.append(" 1, ?)");
			PreparedStatement psTo = connTo.prepareStatement(sql.toString());
			psTo.setObject(1, nrProcessoSet.getString("nr_processo"));
			psTo.setObject(2, now);
			psTo.execute();
			logDataSecaoAtual(psTo);

			sql = new StringBuilder();
			sql.append(" INSERT INTO TB_PROCESSO_EVENTO ");
			sql.append(" (ID_PROCESSO, ID_EVENTO, ID_USUARIO, DT_ATUALIZACAO) VALUES ");
			sql.append(" ((SELECT ID_PROCESSO FROM TB_PROCESSO P WHERE P.NR_PROCESSO = ?),");
			sql.append(" (SELECT ep.id_evento_processual FROM TB_EVENTO_PROCESSUAL EP WHERE EP.CD_EVENTO = '123A'), ");
			sql.append(" 1, ?)");
			psFrom = connFrom.prepareStatement(sql.toString());
			psFrom.setObject(1, nrProcessoSet.getString("nr_processo"));
			psFrom.setObject(2, now);
			psFrom.execute();
			logDataSecaoAtual(psFrom);

			// Adiciona 1s
			cal.add(Calendar.SECOND, 1);
			now = new Timestamp(cal.getTimeInMillis());

			sql = new StringBuilder();
			sql.append(" INSERT INTO TB_PROCESSO_EVENTO ");
			sql.append(" (ID_PROCESSO, ID_EVENTO, ID_USUARIO, DT_ATUALIZACAO) VALUES ");
			sql.append(" ((SELECT ID_PROCESSO FROM TB_PROCESSO P WHERE P.NR_PROCESSO = ?),");
			sql.append(" (SELECT ep.id_evento_processual FROM TB_EVENTO_PROCESSUAL EP WHERE EP.CD_EVENTO = '981'), ");
			sql.append(" 1, ?)");
			psTo = connTo.prepareStatement(sql.toString());
			psTo.setObject(1, nrProcessoSet.getString("nr_processo"));
			psTo.setObject(2, now);
			psTo.execute();
			logDataSecaoAtual(psTo);

		} else {
			escreverNoLog("Erro ao tentar encontrar o Processo");
		}
	}

	/**
	 * Método para popular os campos necessários para remessa na tabela
	 * tb_processo_trf_conexao
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void popularProcessoTrfConexao(int id) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE TB_PROCESSO_TRF_CONEXAO CON ");
		sql.append("SET DS_ORGAO_JULGADOR = ");
		sql.append("	(SELECT OJ.DS_ORGAO_JULGADOR FROM TB_ORGAO_JULGADOR AS OJ ");
		sql.append("		INNER JOIN TB_PROCESSO_TRF AS PR ON PR.ID_ORGAO_JULGADOR = OJ.ID_ORGAO_JULGADOR ");
		sql.append("		WHERE PR.ID_PROCESSO_TRF = CON.ID_PROCESSO_TRF_CONEXO), ");
		sql.append("NR_PROCESSO = ");
		sql.append("	(SELECT PR.NR_PROCESSO FROM TB_PROCESSO AS PR ");
		sql.append("		WHERE PR.ID_PROCESSO = CON.ID_PROCESSO_TRF_CONEXO), ");
		sql.append("DS_POLO_ATIVO = ");
		sql.append("	array_to_string(array( ");
		sql.append("		SELECT UL.DS_NOME FROM TB_USUARIO_LOGIN AS UL ");
		sql.append("			INNER JOIN TB_PROCESSO_PARTE AS PP ON PP.ID_PESSOA = UL.ID_USUARIO ");
		sql.append("			WHERE PP.ID_PROCESSO_TRF = CON.ID_PROCESSO_TRF_CONEXO AND PP.IN_PARTICIPACAO = 'A' ");
		sql.append("		),','), ");
		sql.append("DS_POLO_PASSIVO = ");
		sql.append("	array_to_string(array( ");
		sql.append("		SELECT UL.DS_NOME FROM TB_USUARIO_LOGIN AS UL ");
		sql.append("			INNER JOIN TB_PROCESSO_PARTE AS PP ON PP.ID_PESSOA = UL.ID_USUARIO ");
		sql.append("			WHERE PP.ID_PROCESSO_TRF = CON.ID_PROCESSO_TRF_CONEXO AND PP.IN_PARTICIPACAO = 'P' ");
		sql.append("		),',') ");
		sql.append("WHERE CON.ID_PROCESSO_TRF = ? AND CON.ID_PROCESSO_TRF_CONEXO IS NOT NULL ");

		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		psFrom.setObject(1, id);
		psFrom.execute();
		logDataSecaoAtual(psFrom);
	}
	
	private String getParametroCodigoFluxo() throws SQLException{
		StringBuilder sql = new StringBuilder();
		sql.append("select vl_variavel from tb_parametro where nm_variavel = 'codigoFluxoInicioProcessoRemetido' ");
		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		ResultSet rsParametroCodigoFluxo = psFrom.executeQuery();
		if(rsParametroCodigoFluxo.next()){
			return rsParametroCodigoFluxo.getString("vl_variavel");
		}
		else{
			escreverNoErroLog("Parametro codigoFluxoInicioProcessoRemetido não configurado ");
			throw new SQLException();
		}
	}

	/**
	 * Método para agendar um Processo enviado ao segundo grau de forma que ele
	 * receba o fluxo
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void agendarProcesso(int id) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("select nr_processo from tb_processo where id_processo = ? ");
		PreparedStatement psFrom = connFrom.prepareStatement(sql.toString());
		psFrom.setObject(1, id);
		ResultSet nrProcessoSet = psFrom.executeQuery();
		sql = new StringBuilder();
		sql.append("select id_fluxo from tb_fluxo where cd_fluxo = ? ");
		PreparedStatement psFluxo = connTo.prepareStatement(sql.toString());
		psFluxo.setString(1, getParametroCodigoFluxo());
		ResultSet fluxoSet = psFluxo.executeQuery();
		if (nrProcessoSet.next() && fluxoSet.next()) {
			escreverNoLog("--> numero do processo " + nrProcessoSet.getString("nr_processo"));
			sql = new StringBuilder();
			sql.append("insert into tb_agendamento_remessa (id_processo,id_fluxo,in_processado) values ");
			sql.append("((select id_processo from tb_processo where nr_processo = ?),?,'N') ");
			PreparedStatement psTo = connTo.prepareStatement(sql.toString());
			psTo.setObject(1, nrProcessoSet.getString("nr_processo"));
			psTo.setObject(2, fluxoSet.getInt("id_fluxo"));
			psTo.execute();
			logDataSecaoAtual(psTo);
		} else {
			escreverNoErroLog("Erro ao tentar encontrar o Processo ou o Fluxo");
		}
	}

	/**
	 * Método que migra o processo e todas as tabelas que fazem referência a ele
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void migrarProcesso(Integer id) throws SQLException {
		ResultSet rs = createStatement(connFrom, "core", "tb_processo", id);
		if (!rs.next()) {
			throw new SQLException("Processo não encontrado");
		}
		if (createStatementWithValues(connTo,
				"select id_processo from tb_processo where id_sessao_pg=? and id_pk_tb_processo_pg=?",
				getIdUfAtual(), id).next()) {
			throw new SQLException("Processo já cadastrado no destino. Remessa não está preparada para isso!");
		}
		setNrProcessoAtual(rs.getString("nr_processo"));
		escreverNoLog("--Início da migração [" + getNrProcessoAtual() + "][" + getIdProcessoAtual() + "]....");
		String schema = "client";
		String tab = "tb_processo_trf";
		migrarTabela(schema, tab, id);
		escreverNoLog("(Tabelas que referenciam processo)---------");
		ResultSet keys = dbMetaFrom.getExportedKeys(null, schema, tab);
		while (keys.next()) {
			String sch = keys.getString(6);
			String table = keys.getString(7);
			String column = keys.getString(8);
			if (!table.equals(tab) && !table.equals("tb_processo") && !checkInBlackList(sch, table, column)) {
				ResultSet values = createStatement(connFrom, sch, table, column, id);
				while (values.next()) {
					Object key = values.getObject(getIdColumn(sch, table));
					if (key != null) {
						migrarTabela(sch, table, key);
					}
				}
			}
		}
	}

	/**
	 * Método recursivo que migra a tabela passada como parâmetro e as tabelas
	 * que ela depende. As tabelas contidas na propriedade
	 * <b>app.full_table_export</b> do arquivo de propriedades
	 * <b>preferences.properties</b>, serão exportadas as tabelas que a
	 * referenciam.
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @throws SQLException
	 * @see {@link #checkFullTableExport(String, String)}
	 */
	protected void migrarTabela(String schema, String table, Object id) throws SQLException {
		if (checkInBlackList(schema, table) || checkInserted(schema, table, id)
				|| containsPopulatedTable(schema, table, id)) {
			return;
		}

		incrementarQtdOperacoes();

		escreverNoLog("--" + schema + "." + table + ":" + id + "["
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + "] - BEGIN{");
		ResultSet keys = dbMetaFrom.getImportedKeys(null, schema, table);
		while (keys.next()) {
			String fkSchema = keys.getString(2);
			String fkTable = keys.getString(3);
			String fkColumn = keys.getString(8);
			if (!fkTable.equals(table) && !checkInBlackList(schema, table, fkColumn)) {
				ResultSet values = createStatement(connFrom, schema, table, id);
				if (values.next()) {
					Object key = values.getObject(fkColumn);
					if (key != null) {
						migrarTabela(fkSchema, fkTable, key);
					}
				}
			}
		}
		// addPopulatedTable(schema, table, id);
		migraDados(schema, table, id);
		String[] rootTableProperty = getRootTableProperty(schema, table);
		if (rootTableProperty != null) {
			for (String tableRoot : rootTableProperty) {
				String[] tableNameArray = getTableNameArray(tableRoot);
				if (createStatement(connFrom, tableNameArray[0], tableNameArray[1], id).next()) {
					migrarTabela(tableNameArray[0], tableNameArray[1], id);
				}
			}
		}
		if (checkFullTableExport(schema, table)) {
			keys = dbMetaFrom.getExportedKeys(null, schema, table);
			while (keys.next()) {
				String expSchema = keys.getString(6);
				String expTable = keys.getString(7);
				String expColumn = keys.getString(8);
				if (!table.equals(expTable) && !checkInBlackList(expSchema, expTable, expColumn)) {
					ResultSet values = createStatement(connFrom, expSchema, expTable, expColumn, id);
					while (values.next()) {
						Object key = values.getObject(getIdColumn(expSchema, expTable));
						if (key != null) {
							migrarTabela(expSchema, expTable, key);
						}
					}
				}
			}
		}
		if (schema.equals("client") && table.equals("tb_pessoa")) {
			cadastraDocumentosPessoa(id);
		}
		escreverNoLog("--}" + schema + "." + table + ":" + id + "["
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + "] - END");
	}

	protected abstract void cadastraDocumentosPessoa(Object idPessoa) throws SQLException;

	/**
	 * Método onde os dados são migrados para o destino
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @throws SQLException
	 */
	private void migraDados(String schema, String table, Object id) throws SQLException {
		// System.out.println("\n-->" + schema + "." + table);

		String colunaId = getIdColumn(schema, table);
		String select = MessageFormat.format("select * from {0}.{1} where {2}=?", schema, table, colunaId);
		PreparedStatement psSelect = connFrom.prepareStatement(select);
		psSelect.setObject(1, id);
		ResultSet rs = psSelect.executeQuery();
		if (rs.next()) {

			if (checkLockedTables(schema, table)) {
				String unqColumn = getUniqueColumnProperty(schema, table);
				escreverAvisoLog("Inserindo dados na tabela [" + schema + "." + table + ":" + id + "] " + unqColumn
						+ ":" + rs.getObject(unqColumn));
			}

			StringBuilder sql = new StringBuilder();
			String temp_sql = null;
			ResultSetMetaData metaData = rs.getMetaData();
			Integer columnCount = metaData.getColumnCount();
			if ((temp_sql = getSqlInsertTable(schema, table)) != null) {
				sql.append(temp_sql);
			} else {
				sql.append("insert into ").append(schema + "." + table).append("(");
				StringBuilder values = new StringBuilder(" values (");
				for (int i = 1; i <= columnCount; i++) {
					String column = metaData.getColumnName(i);
					String[] qualifiedFk = getQualifiedFk(schema, table, metaData.getColumnName(i));
					if (!checkInBlackList(schema, table, column)
							&& (qualifiedFk != null || !getIdColumn(schema, table).equals(metaData.getColumnName(i)))) { // Não
																															// leva
																															// PK
						if (!values.toString().equals(" values (")) {
							values.append(", ");
							sql.append(", ");
						}
						sql.append(metaData.getColumnName(i));
						if (qualifiedFk != null) {
							values.append(getUniqueExpression(qualifiedFk[0], qualifiedFk[1], column, true));
						} else {
							values.append(getUniqueExpression(schema, table, column, false));
						}
					}
				}
				if (checkPkTableExport(schema, table)) {
					sql.append(", id_sessao_pg, " + getExportedIdColumn(table));
					values.append(", ?, ?");
				}
				values.append(")");
				sql.append(")").append(values);
				addSqlInsertTable(schema + "." + table, sql.toString());
			}

			PreparedStatement ps = connTo.prepareStatement(sql.toString());
			// System.out.println("(A)"+ps.toString());
			try {
				int y = 1;
				Set<Integer> listHiddenColuns = new HashSet<Integer>();
				String tmpPass = "";
				for (int i = 1; i <= columnCount; i++) {

					String column = metaData.getColumnName(i);
					String[] qualifiedFk = getQualifiedFk(schema, table, column);
					if (!checkInBlackList(schema, table, column)
							&& (qualifiedFk != null || !getIdColumn(schema, table).equals(column))) { // Não
																										// leva
																										// PK
						Object o = rs.getObject(column);
						if (checkInHiddenColumns(schema, table, column)) {
							listHiddenColuns.add(y);
						}
						if (qualifiedFk != null) {
							Object[] valores = getValuesForSteatement(qualifiedFk[0], qualifiedFk[1], o);
							for (Object valor : valores) {
								if (valor == null) {
									int parameterType = ps.getParameterMetaData().getParameterType(y);
									ps.setNull(y++, parameterType);
								} else {
									ps.setObject(y++, valor);
								}
							}
							String[] unique = getUniqueProperties(schema, table);
							if (unique == null || !unique[0].equals("tb_usuario_login")) {
								String[] uniqueProperties = getUniqueProperties(qualifiedFk[0], qualifiedFk[1]);
								if (uniqueProperties != null && uniqueProperties[0].equals("tb_usuario_login")
										&& this.usuarioDuplicadoList.get(o) != null) {
									if (this.usuarioDuplicadoList.get(o).add(schema + "." + table)) {
										ResultSet rsDocs = createStatementWithValues(
												connFrom,
												"select * from tb_pess_doc_identificacao where id_pessoa=? and in_ativo=true;",
												o);
										StringBuilder sb = new StringBuilder();
										while (rsDocs.next()) {
											sb.append(rsDocs.getString("cd_tp_documento_identificacao") + ": "
													+ rsDocs.getString("nr_documento_identificacao") + "; \n");
										}

										ResultSet rsUserTo = createStatementWithValues(
												connTo,
												"select u.ds_nome, r.id_usuario from tb_remessa_proc_usuario r left join tb_usuario_login u on (r.id_usuario=u.id_usuario) where r.id_usuario_origem=? and r.id_sessao_origem=?;",
												o, getIdUfAtual());
										if (rsUserTo.next()) {
											escreverAvisoLog("Cadastro em [" + schema + "." + table
													+ "] com possível cadastro do usuário("
													+ rsUserTo.getInt("id_usuario") + ":"
													+ rsUserTo.getString("ds_nome") + ") duplicado(" + quebra
													+ sb.toString() + quebra + ");");
										}
									}
								}
							}
						} else {
							if (o == null) {
								int parameterType = ps.getParameterMetaData().getParameterType(y);
								ps.setNull(y++, parameterType);
							} else {
								if (schema.equals("acl") && table.equals("tb_usuario_login")) {
									if (column.equals("ds_login")) {
										String novoLogin = "rp" + getIdUfAtual() + o;
										if (novoLogin.length() > 100) {
											novoLogin = novoLogin.substring(0, 99);
										}
										escreverAvisoLog("Alterou login de " + rs.getString("id_usuario") + ":"
												+ rs.getString("ds_nome") + "(" + o.toString() + " -> " + novoLogin
												+ ");");
										tmpPass = new PasswordHash().generateSaltedHash(novoLogin, novoLogin, "SHA");
										o = novoLogin;
									} else if (column.equals("ds_senha")) {
										o = tmpPass;
									}
								}
								ps.setObject(y++, o);
							}
						}
					}
				}

				if (checkPkTableExport(schema, table)) {
					ps.setObject(y++, getIdUfAtual());
					ps.setObject(y++, id);
				}
				ps.execute();
				for (Integer i : listHiddenColuns) {
					ps.setObject(i, "[hidden_column]");
				}
				logDataSecaoAtual(ps);
				if (schema.equals("acl") && table.equals("tb_usuario_login")) {
					PreparedStatement psInsUsuInx = connTo
							.prepareStatement("insert into tb_remessa_proc_usuario(id_usuario,id_usuario_origem,id_sessao_origem)values(CURRVAL('sq_tb_usuario_login'),?,?);");
					psInsUsuInx.setObject(1, id);
					psInsUsuInx.setObject(2, getIdUfAtual());
					psInsUsuInx.execute();
					logDataSecaoAtual(psInsUsuInx);
				} else if (schema.equals("client") && table.equals("tb_pess_doc_identificacao")) {
					PreparedStatement psInsUsuDoc = connTo
							.prepareStatement("insert into tb_rmessa_proc_doc_usuario(id_pessoa_doc_identificacao,id_pess_doc_identfcacao_origem,id_sessao_origem)values(CURRVAL('sq_tb_pess_doc_identificacao'),?,?);");
					psInsUsuDoc.setObject(1, id);
					psInsUsuDoc.setObject(2, getIdUfAtual());
					psInsUsuDoc.execute();
					logDataSecaoAtual(psInsUsuDoc);
				}
			} catch (SQLException e) {
				// System.err.println(sql);
				// System.err.println(Arrays.toString(pValues));
				// connTo.rollback();
				// connFrom.rollback();
				String erro = "--\n\nErro!----------------\n" + "select * from " + schema + "." + table + " where "
						+ getIdColumn(schema, table) + "=" + id + ";" + "\n" + ps.toString();
				escreverNoErroLog(erro);
				throw e;
				// break;
			} finally {
				ps.close();
			}
		}
		rs.close();
	}

	/**
	 * Verifica se já existe o registro no TRF5
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @throws SQLException
	 */
	private boolean checkInserted(String schema, String table, Object id) throws SQLException {
		if (this.populatedTables.contains(schema + "." + table + ":" + id)) {
			return true;
		}

		String[] uniqueProperties = getUniqueProperties(schema, table);
		if (uniqueProperties != null) {
			if (uniqueProperties[0].equals("tb_usuario_login")
					&& !this.populatedTables.contains("tb_usuario_login" + ":" + id)) {
				try {
					updateIdPkExportedUsuario(id);
				} catch (SQLException e) {
					this.usuarioDuplicadoList.put((Integer) id, new HashSet<String>());
					return false;
				}
			}
		}
		if (schema.equals("client") && table.equals("tb_pess_doc_identificacao")) {
			updateIdPkExportedDocUsuario(id);
		}

		if (checkStatementExportedQualifiedPkTo(schema, table, id)) {
			return true;
		} else if (checkPkTableExport(schema, table)) {
			if (checkStatementExportedPkTo(schema, table, id)) {
				return true;
			}
		} else if (uniqueProperties != null) {
			String[] tableNameArray = getTableNameArray(uniqueProperties[0]);
			if (getQualifiedPkTableExportProperty(tableNameArray[0], tableNameArray[1]) != null) {
				if (checkStatementExportedQualifiedPkTo(tableNameArray[0], tableNameArray[1], schema, table, id)) {
					return true;
				}
			} else if (checkPkTableExport(tableNameArray[0], tableNameArray[1])) {
				if (checkStatementExportedPkTo(tableNameArray[0], tableNameArray[1], schema, table, id)) {
					return true;
				}
			} else {
				ResultSet rsValuesQueryUniqueFrom = createStatement(connFrom, tableNameArray[0], tableNameArray[1], id);
				if (rsValuesQueryUniqueFrom.next()) {
					if (!tableNameArray[1].equals(table)) {
						return checkStatementTo(schema, table, tableNameArray[0], tableNameArray[1],
								uniqueProperties[1], rsValuesQueryUniqueFrom);
					}
					ResultSet rsValuesQueryUniqueTo = createCheckStatementTo(tableNameArray[0], tableNameArray[1],
							uniqueProperties[1], rsValuesQueryUniqueFrom);
					if (rsValuesQueryUniqueTo.next()) {
						escreverNoLog("--(Já cadastrado)[" + schema + "." + table + ":"
								+ rsValuesQueryUniqueTo.getObject(uniqueProperties[1]) + "]");
						return true;
					}
				}
			}
		}
		return false;
	}

	protected abstract void updateIdPkExportedUsuario(Object id) throws SQLException;

	private void updateIdPkExportedDocUsuario(Object id) throws SQLException {
		String sqlDocPessoa = "select d.* from tb_pess_doc_identificacao d " + "where d.id_pessoa=?;";
		String sqlHasDocUsuTo = "select id_pessoa_doc_identificacao from tb_pess_doc_identificacao where 1=1 "
				+ "and id_pessoa=(select id_usuario from tb_remessa_proc_usuario where id_usuario_origem=? and id_sessao_origem=?) "
				+ "and trim(lower(to_ascii(cd_tp_documento_identificacao)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(in_ativo)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(in_usado_falsamente)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(nr_documento_identificacao)))=trim(lower(to_ascii(?)));";
		String sqlHasDocUsu = "select * from tb_rmessa_proc_doc_usuario where id_pess_doc_identfcacao_origem=? and id_sessao_origem=?;";
		String sqlInsDocUsu = "insert into tb_rmessa_proc_doc_usuario(id_pess_doc_identfcacao_origem,id_sessao_origem,id_pessoa_doc_identificacao)values(?,?,"
				+ "(select id_pessoa_doc_identificacao from tb_pess_doc_identificacao where 1=1 "
				+ "	and id_pessoa=(select id_usuario from tb_remessa_proc_usuario where id_usuario_origem=? and id_sessao_origem=?) "
				+ "	and trim(lower(to_ascii(cd_tp_documento_identificacao)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(in_ativo)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(in_usado_falsamente)))=trim(lower(to_ascii(?))) and trim(lower(to_ascii(nr_documento_identificacao)))=trim(lower(to_ascii(?)))));";
		ResultSet rsDocPessoa = createStatementWithValues(connFrom, sqlDocPessoa, id);
		while (rsDocPessoa.next()) {
			if (createStatementWithValues(connTo, sqlHasDocUsuTo, id, getIdUfAtual(),
					rsDocPessoa.getObject("cd_tp_documento_identificacao"), rsDocPessoa.getObject("in_ativo"),
					rsDocPessoa.getObject("in_usado_falsamente"), rsDocPessoa.getObject("nr_documento_identificacao"))
					.next()) {
				if (!containsPopulatedTable("client", "tb_pess_doc_identificacao",
						rsDocPessoa.getObject("id_pessoa_doc_identificacao"))) {
					if (!createStatementWithValues(connTo, sqlHasDocUsu,
							rsDocPessoa.getObject("id_pessoa_doc_identificacao"), getIdUfAtual()).next()) {
						PreparedStatement psDocUsu = connTo.prepareStatement(sqlInsDocUsu);
						psDocUsu.setObject(1, rsDocPessoa.getObject("id_pessoa_doc_identificacao"));
						psDocUsu.setObject(2, getIdUfAtual());
						psDocUsu.setObject(3, id);
						psDocUsu.setObject(4, getIdUfAtual());
						psDocUsu.setObject(5, rsDocPessoa.getObject("cd_tp_documento_identificacao"));
						psDocUsu.setObject(6, rsDocPessoa.getObject("in_ativo"));
						psDocUsu.setObject(7, rsDocPessoa.getObject("in_usado_falsamente"));
						psDocUsu.setObject(8, rsDocPessoa.getObject("nr_documento_identificacao"));
						psDocUsu.execute();
						if (psDocUsu.getUpdateCount() > 0) {
							logDataSecaoAtual(psDocUsu);
						}
					}
				}
			}
		}
	}

	/**
	 * Método que seleciona a expressão utilizada para retornar o valor que será
	 * inserido. Abaixo as opções disponíveis caso o campo for uma FK:
	 * <ul>
	 * <li>Consulta baseada na PK exportada;</li>
	 * <li>Na coluna única da tabela(ou tabela pai);</li>
	 * </ul>
	 * 
	 * @param schema
	 * @param table
	 * @param column
	 * @param qualifiedFk
	 * @return
	 * @throws SQLException
	 */
	private String getUniqueExpression(String schema, String table, String column, boolean isFk) throws SQLException {
		String[] uniqueProperties = getUniqueProperties(schema, table);
		if (uniqueProperties != null) {
			String[] tableNameArray = getTableNameArray(uniqueProperties[0]);
			// Set<String> uniqueCols = new HashSet<String>(Arrays.asList(uniqueProperties[1].split(",")));
			String qualifiedPkTableExportProperty = getQualifiedPkTableExportProperty(tableNameArray[0],
					tableNameArray[1]);
			if (qualifiedPkTableExportProperty != null && isFk) {
				String[] qualifiedTableNameArray = getTableNameArray(qualifiedPkTableExportProperty);
				return MessageFormat.format("(select {0} from {1}.{2} where {3}=? and {4}=?)",
						getIdColumn(tableNameArray[0], tableNameArray[1]), qualifiedTableNameArray[0],
						qualifiedTableNameArray[1], "id_sessao_origem",
						getIdColumn(tableNameArray[0], tableNameArray[1]) + "_origem");
			} else if (checkPkTableExport(tableNameArray[0], tableNameArray[1]) && isFk) {
				return MessageFormat.format("(select {0} from {1}.{2} where {3}=? and {4}=?)",
						getIdColumn(tableNameArray[0], tableNameArray[1]), tableNameArray[0], tableNameArray[1],
						"id_sessao_pg", getExportedIdColumn(tableNameArray[1]));
			} else if (isFk) {
				return MessageFormat.format(
						"(select {0} from {1}.{2} where trim(lower(to_ascii({3})))=trim(lower(to_ascii(?))))",
						getIdColumn(tableNameArray[0], tableNameArray[1]), tableNameArray[0], tableNameArray[1],
						uniqueProperties[1]);
			}
		} else {
			String qualifiedPkTableExportProperty = getQualifiedPkTableExportProperty(schema, table);
			if (qualifiedPkTableExportProperty != null && isFk) {
				String[] qualifiedTableNameArray = getTableNameArray(qualifiedPkTableExportProperty);
				return MessageFormat.format("(select {0} from {1}.{2} where {3}=? and {4}=?)",
						getIdColumn(schema, table), qualifiedTableNameArray[0], qualifiedTableNameArray[1],
						"id_sessao_origem", getIdColumn(schema, table) + "_origem");
			} else if (checkPkTableExport(schema, table) && isFk) {
				return MessageFormat.format("(select {0} from {1}.{2} where {3}=? and {4}=?)",
						getIdColumn(schema, table), schema, table, "id_sessao_pg", getExportedIdColumn(table));
			}
		}
		return "?";
	}

	/**
	 * Método que seleciona a expressão utilizada para retornar o valor que será
	 * inserido. Abaixo as opções disponíveis caso o campo for uma FK:
	 * <ul>
	 * <li>Consulta baseada na PK exportada;</li>
	 * <li>Na coluna única da tabela ou tabela pai;</li>
	 * </ul>
	 * 
	 * @param schema
	 * @param table
	 * @param qualifiedFk
	 * @return
	 * @throws SQLException
	 */
	private Object[] getValuesForSteatement(String schema, String table, Object id) throws SQLException {
		List<Object> resultado = new ArrayList<Object>();
		if (id == null) {
			String[] uniqueProperties = getUniqueProperties(schema, table);
			if (uniqueProperties != null) {
				String[] tableNameArray = getTableNameArray(uniqueProperties[0]);
				if (checkPkTableExport(tableNameArray[0], tableNameArray[1])
						|| getQualifiedPkTableExportProperty(tableNameArray[0], tableNameArray[1]) != null) {
					resultado.add(null);
					resultado.add(null);
					return resultado.toArray();
				} else {
					Set<String> uniqueCols = new HashSet<String>(Arrays.asList(uniqueProperties[1].split(",")));
					for (String unqCol : uniqueCols) {
						resultado.add(null);
					}
					return resultado.toArray();
				}
			} else {
				if (checkPkTableExport(schema, table) || getQualifiedPkTableExportProperty(schema, table) != null) {
					resultado.add(null);
					resultado.add(null);
					return resultado.toArray();
				}
			}
		}

		String[] uniqueProperties = getUniqueProperties(schema, table);
		if (uniqueProperties != null) {
			String[] tableNameArray = getTableNameArray(uniqueProperties[0]);
			ResultSet rsValues = createStatement(connFrom, tableNameArray[0], tableNameArray[1], id);
			if (rsValues.next()) {
				if (checkPkTableExport(tableNameArray[0], tableNameArray[1])
						|| getQualifiedPkTableExportProperty(tableNameArray[0], tableNameArray[1]) != null) {
					resultado.add(getIdUfAtual());
					resultado.add(rsValues.getObject(getIdColumn(tableNameArray[0], tableNameArray[1])));
				} else {
					Set<String> uniqueCols = new HashSet<String>(Arrays.asList(uniqueProperties[1].split(",")));
					for (String unqCol : uniqueCols) {
						resultado.add(rsValues.getObject(unqCol));
					}
				}
			}
		} else {
			ResultSet rsValues = createStatement(connFrom, schema, table, id);
			if (rsValues.next()) {
				if (checkPkTableExport(schema, table) || getQualifiedPkTableExportProperty(schema, table) != null) {
					resultado.add(getIdUfAtual());
					resultado.add(rsValues.getObject(getIdColumn(schema, table)));
				}
			}
		}
		return resultado.toArray();
	}

	/**
	 * Funcão que retorna o schema, tabela e coluna da FK passada no parâmetro.
	 * 
	 * @param schema
	 * @param table
	 * @param fkColumn
	 * @return {@link String[]}
	 * @throws SQLException
	 */
	private String[] getQualifiedFk(String schema, String table, String fkColumn) throws SQLException {
		String[] resultado = null;
		if ((resultado = getQualifiedFK(schema, table, fkColumn)) != null) {
			return resultado;
		}
		/*
		 * resultado = new String[3]; ResultSet keys =
		 * dbMetaFrom.getImportedKeys(null, schema, table); while(keys.next()){
		 * if(keys.getString(8).equals(fkColumn)){
		 * resultado[0]=keys.getString(2); resultado[1]=keys.getString(3);
		 * resultado[2]=keys.getString(4); return resultado; } }
		 */
		return null;
	}

	private ResultSet createCheckStatementTo(String schema, String tab, String idColumns, ResultSet rsIdsFrom)
			throws SQLException {
		if (idColumns.isEmpty()) {
			escreverNoLog(tab + "------------------------");
		}
		String[] idColumnArray = idColumns.split(",");
		String pattern = "select * from {0}.{1} where 1=1";
		String codiction = "";
		for (String idColumn : idColumnArray) {
			codiction += " and trim(lower(to_ascii(" + idColumn + ")))=trim(lower(to_ascii(?)))";
		}
		String sql = MessageFormat.format(pattern, schema, tab, idColumns) + codiction;
		PreparedStatement ps = connTo.prepareStatement(sql);
		int i = 1;
		for (String idColumn : idColumnArray) {
			ps.setObject(i++, rsIdsFrom.getObject(idColumn));
		}
		return ps.executeQuery();
	}

	private boolean checkStatementTo(String schema, String table, String schemaUnq, String tableUnq, String idColumns,
			ResultSet rsIdsFrom) throws SQLException {
		if (idColumns.isEmpty()) {
			escreverNoLog(table + "------------------------");
		}
		String[] idColumnArray = idColumns.split(",");
		String pattern = "select * from {0}.{1} where {2}=(select {3} from {4}.{5} where 1=1 ";
		String codiction = "";
		for (String idColumn : idColumnArray) {
			codiction += " and trim(lower(to_ascii(" + idColumn + ")))=trim(lower(to_ascii(?)))";
		}
		String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table),
				getIdColumn(schemaUnq, tableUnq), schemaUnq, tableUnq, idColumns)
				+ codiction + ")";
		PreparedStatement ps = connTo.prepareStatement(sql);
		int i = 1;
		for (String idColumn : idColumnArray) {
			ps.setObject(i++, rsIdsFrom.getObject(idColumn));
		}
		return ps.executeQuery().next();
	}

	private boolean checkStatementExportedPkTo(String schema, String table, Object id) throws SQLException {
		String pattern = "select * from {0}.{1} where {2}=? and {3}=?";
		String sql = MessageFormat.format(pattern, schema, table, "id_sessao_pg", getExportedIdColumn(table));
		PreparedStatement ps = connTo.prepareStatement(sql);
		ps.setObject(1, getIdUfAtual());
		ps.setObject(2, id);
		return ps.executeQuery().next();
	}

	private boolean checkStatementExportedPkTo(String schemaUniq, String tableUniq, String schema, String table,
			Object id) throws SQLException {
		String pattern = "select 1 from {0}.{1} where {2}=(select {3} from {4}.{5} where id_sessao_pg=? and {6}=?)";
		String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table),
				getIdColumn(schemaUniq, tableUniq), schemaUniq, tableUniq, getExportedIdColumn(tableUniq));
		PreparedStatement ps = connTo.prepareStatement(sql);
		ps.setObject(1, getIdUfAtual());
		ps.setObject(2, id);
		return ps.executeQuery().next();
	}

	private boolean checkStatementExportedQualifiedPkTo(String schema, String table, Object id) throws SQLException {
		String qualifiedPkTableExportProperty = getQualifiedPkTableExportProperty(schema, table);
		if (qualifiedPkTableExportProperty != null) {
			String[] tableNameArray = getTableNameArray(qualifiedPkTableExportProperty);
			String pattern = "select * from {0}.{1} where {2}=? and {3}=?";
			String sql = MessageFormat.format(pattern, tableNameArray[0], tableNameArray[1], "id_sessao_origem",
					getIdColumn(schema, table) + "_origem");
			PreparedStatement ps = connTo.prepareStatement(sql);
			ps.setObject(1, getIdUfAtual());
			ps.setObject(2, id);
			return ps.executeQuery().next();
		}
		return false;
	}

	private boolean checkStatementExportedQualifiedPkTo(String schemaUniq, String tableUniq, String schema,
			String table, Object id) throws SQLException {
		String qualifiedPkTableExportProperty = getQualifiedPkTableExportProperty(schemaUniq, tableUniq);
		if (qualifiedPkTableExportProperty != null) {
			String[] tableNameArray = getTableNameArray(qualifiedPkTableExportProperty);
			String pattern = "select 1 from {0}.{1} where {2}=(select {3} from {4}.{5} where id_sessao_origem=? and {6}=?)";
			String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table),
					getIdColumn(schemaUniq, tableUniq), tableNameArray[0], tableNameArray[1],
					getIdColumn(schemaUniq, tableUniq) + "_origem");
			PreparedStatement ps = connTo.prepareStatement(sql);
			ps.setObject(1, getIdUfAtual());
			ps.setObject(2, id);
			return ps.executeQuery().next();
		}
		return false;
	}

	private ResultSet createStatement(Connection connection, String schema, String table, Object id)
			throws SQLException {
		String pattern = "select * from {0}.{1} where {2}=?";
		String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table));
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setObject(1, id);
		return ps.executeQuery();
	}

	protected ResultSet createStatement(Connection connection, String schema, String table, String column, Object id)
			throws SQLException {
		String pattern = "select * from {0}.{1} where {2}=?";
		String sql = MessageFormat.format(pattern, schema, table, column);
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setObject(1, id);
		return ps.executeQuery();
	}

	protected ResultSet createStatementWithValues(Connection connection, String query, Object... valores)
			throws SQLException {
		PreparedStatement ps = connection.prepareStatement(query);
		int i = 1;
		for (Object valor : valores) {
			ps.setObject(i++, valor);
		}
		return ps.executeQuery();
	}

	/**
	 * Retorna o nome da coluna que é a PK da tabela
	 * 
	 * @param schema
	 * @param table
	 * @return Retorna null caso não encontre
	 * @throws SQLException
	 */
	protected String getIdColumn(String schema, String table) throws SQLException {
		ResultSet keys = dbMetaFrom.getPrimaryKeys(null, schema, table);
		if (keys.next()) {
			return keys.getString(4);
		}
		return null;
	}

	/**
	 * Retorna o nome da coluna que conterá a PK exportada.
	 * 
	 * @param table
	 * @return
	 */
	private String getExportedIdColumn(String table) {
		if ("tb_proc_doc_bin_pess_assin".equalsIgnoreCase(table)) {
			return "ID_PK_TB_PR_D_BIN_PES_AS_PG";
		} else if ("tb_processo_documento_bin".equalsIgnoreCase(table)) {
			return "ID_PK_TB_PRO_DOCUM_BIN_PG";
		} else if ("tb_processo_expediente".equalsIgnoreCase(table)) {
			return "ID_PK_TB_PROCE_EXPEDIENTE_PG";
		} else {
			return "id_pk_" + table + "_pg";
		}
	}

	/**
	 * Retorna um array com o nome do schema e da tabela
	 * 
	 * @param quilifiedTableName
	 * @return
	 */
	private String[] getTableNameArray(String quilifiedTableName) {
		if (quilifiedTableName != null) {
			return quilifiedTableName.split("\\.");
		}
		return null;
	}

	/**
	 * Verifica se é uma tabela terá sua PK exportada;
	 * 
	 * @param schema
	 * @param table
	 */
	private boolean checkPkTableExport(String schema, String table) {
		String property = properties.getProperty("app.table_pk_export");
		if (property != null) {
			Set<String> properties = new HashSet<String>(Arrays.asList(property.split(";")));
			if (properties.contains(schema + "." + table)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se é uma tabela será exportada completamente(tabelas das chaves
	 * importadas e exportadas);
	 * 
	 * @param schema
	 * @param table
	 */
	private boolean checkFullTableExport(String schema, String table) {
		String property = properties.getProperty("app.full_table_export");
		if (property != null) {
			Set<String> properties = new HashSet<String>(Arrays.asList(property.split(",")));
			if (properties.contains(schema + "." + table)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se a tabela está na lista negra, mas antes, a lista branca é
	 * consultada, se existir, retorna verdadeiro e a lista negra não é
	 * consultada, senão, a lista negra é consultada.
	 * 
	 * @param schema
	 * @param table
	 * @throws SQLException
	 */
	private boolean checkInBlackList(String schema, String table) {
		String propertyWl = getWhiteListProperty(schema, table);
		if (propertyWl != null) {
			return false;
			/*
			 * Set<String> propertieWhiteList = new
			 * HashSet<String>(Arrays.asList(propertyWl.split(","))); if
			 * (propertieWhiteList.contains("") ||
			 * propertieWhiteList.contains("all")){ }
			 */
		}
		return true;
	}

	/**
	 * Verifica se a coluna está na lista negra, mas antes, a lista branca é
	 * consultada, se existir, retorna verdadeiro e a lista negra não é
	 * consultada, senão, a lista negra é consultada.
	 * 
	 * @param schema
	 * @param table
	 * @throws SQLException
	 */
	private boolean checkInBlackList(String schema, String table, String column) throws SQLException {
		if (checkPkTableExport(schema, table)) {
			if (column.equals(getExportedIdColumn(table)) || column.equals("id_sessao_pg")) {
				return true;
			}
		}
		String propertyWl = getWhiteListProperty(schema, table);
		if (propertyWl != null) {
			Set<String> propertieWhiteList = new HashSet<String>(Arrays.asList(propertyWl.split(",")));
			if (propertieWhiteList.contains("") || propertieWhiteList.contains("all")
					|| propertieWhiteList.contains(column)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Verifica se a coluna será mostrada no log de execução
	 * 
	 * @param schema
	 * @param table
	 * @param column
	 * @return
	 */
	private boolean checkInHiddenColumns(String schema, String table, String column) {
		String propertyHC = getHiddenColumnProperty(schema, table);
		if (propertyHC != null) {
			Set<String> propertiesHC = new HashSet<String>(Arrays.asList(propertyHC.split(",")));
			return propertiesHC.contains(column);
		}
		return false;
	}

	/**
	 * 
	 * @param schema
	 * @param table
	 * @return Retorna null caso não exista a propriedade, senão,
	 *         [coluna,nome.esquema]
	 */
	private String[] getUniqueProperties(String schema, String table) {
		String[] resultado = new String[2];
		String property = getUniqueColumnProperty(schema, table);
		if (property != null) {
			String propertyPai = getUniqueColumnProperty(property);
			if (propertyPai != null) {
				resultado[0] = property;
				resultado[1] = propertyPai;
				return resultado;
			} else {
				resultado[0] = schema + "." + table;
				resultado[1] = property;
				return resultado;
			}
		}
		return null;
	}

	/**
	 * Retorna o id da UF em que a classe está sendo executada Préfixo das
	 * seções AL=1, CE=2, PB=3, PE=4, RN=5, SE=6
	 * 
	 * @return
	 */
	public Integer getPrefixUF(String valor) throws SQLException {
		String uf = valor;
		if (uf != null) {
			uf = uf.toLowerCase().trim();
		}
		if (uf.equals("trf5")) {
			return 0;
		} else if (uf.equals("al")) {
			return 1;
		} else if (uf.equals("ce")) {
			return 2;
		} else if (uf.equals("pb")) {
			return 3;
		} else if (uf.equals("pe")) {
			return 4;
		} else if (uf.equals("rn")) {
			return 5;
		} else if (uf.equals("se")) {
			return 6;
		} else {
			throw new SQLException("Código do estado não foi transformado!");
		}
	}

	/**
	 * Verifica se a tabela foi migrada, se não foi, e cadastra como migrada;
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @return
	 */
	protected boolean containsPopulatedTable(String schema, String table, Object id) {
		this.logTablesExported.add(schema + "." + table);
		boolean resultado = !this.populatedTables.add(schema + "." + table + ":" + id.toString());
		return resultado;
	}

	private void migrarDocumentosBinarios(Integer idProcesso) throws SQLException {
		try {
			String sqlInsertBin = "insert into tb_processo_documento_bin (id_processo_documento_bin, ob_processo_documento) values (?,?);";
			String sqlBinTo = "select id_processo_documento_bin from tb_processo_documento_bin where id_sessao_pg=? and ID_PK_TB_PRO_DOCUM_BIN_PG=?;";
			ResultSet rsBins = createStatement(connFrom, "core", "tb_processo_documento", "id_processo", idProcesso);
			escreverNoLog("--BIN.core.tb_processo_documento_bin:id_processo=" + idProcesso + "["
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + "] - BEGIN{");
			while (rsBins.next()) {
				qtdOperacoes++;
				PreparedStatement psBinTo = connTo.prepareStatement(sqlBinTo);
				psBinTo.setObject(1, getIdUfAtual());
				Object idOrigem = rsBins.getObject("id_processo_documento_bin");
				psBinTo.setObject(2, idOrigem);
				ResultSet rsIdBinDestino = psBinTo.executeQuery();
				if (rsIdBinDestino.next()) {
					ResultSet rsBin = createStatement(connFrom_bin, "core", "tb_processo_documento_bin", idOrigem);
					if (rsBin.next()) {
						byte[] bin = rsBin.getBytes("ob_processo_documento");
						if (bin == null) {
							escreverNoLog("--BIN.core.tb_processo_documento_bin binário não encontrado. id = "
									+ idOrigem);
						} else {
							PreparedStatement psInsert = connTo_bin.prepareStatement(sqlInsertBin);
							psInsert.setObject(1, rsIdBinDestino.getObject("id_processo_documento_bin"));
							psInsert.setObject(2, bin);
							psInsert.execute();
							incrementarQtdOperacoes();
							logDataSecaoAtual(psInsert);
						}
					}
				}
			}
		} catch (SQLException e) {
			escreverNoErroLog("Erro nos inserts de documentos binários");
			throw e;
		}
		escreverNoLog("--}BIN.core.tb_processo_documento_bin:id_processo=" + idProcesso + "["
				+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) + "] - END");
	}

	/**
	 * Verifica se é uma tabela terá sua PK exportada de forma unica;
	 * 
	 * @param schema
	 * @param table
	 */
	private String getQualifiedPkTableExportProperty(String schema, String table) {
		return properties.getProperty("qualifiedpk." + schema + "." + table);
	}

	private String getBlackListProperty(String schema, String table) {
		return properties.getProperty("blacklist." + schema + "." + table);
	}

	private String getWhiteListProperty(String schema, String table) {
		return properties.getProperty("whitelist." + schema + "." + table);
	}

	private String getHiddenColumnProperty(String schema, String table) {
		return properties.getProperty("hidden." + schema + "." + table);
	}

	private String getUniqueColumnProperty(String schema, String table) {
		return properties.getProperty("unique." + schema + "." + table);
	}

	private String getUniqueColumnProperty(String qualifiedTableName) {
		return properties.getProperty("unique." + qualifiedTableName);
	}

	private String[] getRootTableProperty(String schema, String table) {
		String propertyRoot = properties.getProperty("root." + schema + "." + table);
		if (propertyRoot != null) {
			return propertyRoot.split(";");
		}
		return null;
	}

	private boolean checkLockedTables(String schema, String table) {
		String property = properties.getProperty("app.locked_tables");
		if (property != null) {
			Set<String> properties = new HashSet<String>(Arrays.asList(property.split(",")));
			if (properties.contains(schema + "." + table)) {
				return true;
			}
		}
		return false;
	}

	private String getUfToProperty() {
		return properties.getProperty("to.uf");
	}

	public Integer getIdUfAtual() {
		return idUfAtual;
	}

	public void setIdUfAtual(Integer idUfAtual) {
		this.idUfAtual = idUfAtual;
	}

	public Integer getIdUfDestino() {
		return idUfDestino;
	}

	public void setIdUfDestino(Integer idUfDestino) {
		this.idUfDestino = idUfDestino;
	}

	protected void logDataSecaoAtual(PreparedStatement ps) throws SQLException {
		String resultado = "";
		try {
			resultado = MessageFormat.format("{0}--<log destino=\"{1}\" data=\"{2}\"/>", ps.toString(), ps
					.getConnection().getMetaData().getURL().replaceAll("jdbc:postgresql://", ""), new SimpleDateFormat(
					"dd/MM/yyyy HH:mm:ss:SSS").format(new Date()));

		} catch (SQLException e) {
			e.printStackTrace();
		}
		escreverNoLog(resultado);
	}

	protected Connection getConnFrom() {
		return connFrom;
	}

	protected void setConnFrom(Connection connFrom) {
		this.connFrom = connFrom;
	}

	protected Connection getConnTo() {
		return connTo;
	}

	protected void setConnTo(Connection connTo) {
		this.connTo = connTo;
	}

	public String getNrProcessoAtual() {
		return nrProcessoAtual;
	}

	public void setNrProcessoAtual(String nrProcessoAtual) {
		this.nrProcessoAtual = nrProcessoAtual;
	}

	protected Integer getIdProcessoAtual() {
		return idProcessoAtual;
	}

	protected void setIdProcessoAtual(Integer idProcessoAtual) {
		this.idProcessoAtual = idProcessoAtual;
	}

	private void escreverNoLog(String texto) throws SQLException {
		logGeral.append(texto + quebra);
		System.out.println(texto);

		try {
			PreparedStatement psUpdHistLog = connFromHistLog
					.prepareStatement("update tb_remessa_proc_hist_log set ds_log_remessa=? where id_remessa_processo_hist_log=?;");
			psUpdHistLog.setString(1, this.logGeral.toString());
			psUpdHistLog.setInt(2, getIdHistLogAtual());
			psUpdHistLog.execute();
			connFromHistLog.commit();
		} catch (SQLException e) {
			escreverNoErroLog("Erro no update no log de remessa da tabela de log");
			throw e;
		}
	}

	private void escreverAvisoLog(String texto) throws SQLException {
		logAviso.append(texto + quebra);
		System.out.println(texto);
		try {
			PreparedStatement psUpdHistLog = connFromHistLog
					.prepareStatement("update tb_remessa_proc_hist_log set ds_log_erro=? where id_remessa_processo_hist_log=?;");
			psUpdHistLog.setString(1, this.logAviso.toString());
			psUpdHistLog.setInt(2, getIdHistLogAtual());
			psUpdHistLog.execute();
			connFromHistLog.commit();
		} catch (SQLException e) {
			escreverNoErroLog("Erro no update no aviso log da tabela de log");
			throw e;
		}
	}

	/*
	 * private void atualizarLog() throws SQLException{ try{ PreparedStatement
	 * psUpdHistLog = connFromHistLog.prepareStatement(
	 * "update tb_remessa_processo_hist_log set ds_log_remessa=? where id_remessa_processo_hist_log=?;"
	 * ); psUpdHistLog.setString(1, this.logGeral.toString());
	 * psUpdHistLog.setInt(2, getIdHistLogAtual()); psUpdHistLog.execute();
	 * connFromHistLog.commit(); }catch(SQLException e){
	 * escreverNoErroLog("Erro no update da tabela de log");
	 * escreverNoErroLog(e.toString()); throw e; }
	 * 
	 * }
	 */
	private void atualizaStatusRemessa(String status) throws SQLException {
		try {
			PreparedStatement psUpdHistLog = connFromHistLog
					.prepareStatement("update tb_remessa_proc_historico set in_status=? where id_remessa_processo_historico=?;");
			psUpdHistLog.setString(1, status);
			psUpdHistLog.setInt(2, getIdHistAtual());
			psUpdHistLog.execute();
			connFromHistLog.commit();
			logDataSecaoAtual(psUpdHistLog);
			psUpdHistLog.close();
		} catch (SQLException e) {
			escreverNoErroLog("Erro no update da atualização de status do histório da remessa");
			escreverNoErroLog(e.toString());
			throw e;
		}
	}

	protected void escreverNoErroLog(String texto) throws SQLException {
		String resultado = MessageFormat.format("{0}--<processo=\"{1}\" data=\"{2}\"/>", texto, getNrProcessoAtual()
				+ " - " + getIdProcessoAtual(), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()));
		logErroGeral.append(resultado + quebra);
		System.out.println(resultado);

		try {
			PreparedStatement psUpdHistLog = connFromHistLog
					.prepareStatement("update tb_remessa_proc_hist_log set ds_log_erro=? where id_remessa_processo_hist_log=?;");
			psUpdHistLog.setObject(1, this.logErroGeral.toString());
			psUpdHistLog.setInt(2, getIdHistLogAtual());
			psUpdHistLog.execute();
			connFromHistLog.commit();
			psUpdHistLog.close();
		} catch (SQLException e) {
			System.out.println("Erro no update no log de erro da tabela de log");
			System.out.println(e.toString());
			throw e;
		}
		// Util.escreverNoErroLog(getIdProcessoAtual(), resultado);
	}

	private void incrementarQtdOperacoes() throws SQLException {
		this.qtdOperacoes++;
		PreparedStatement psUpdHistLog = connFromHistLog
				.prepareStatement("update tb_remessa_proc_hist_log set qt_operacoes=? where id_remessa_processo_hist_log=?;");
		psUpdHistLog.setInt(1, qtdOperacoes);
		psUpdHistLog.setInt(2, getIdHistLogAtual());
		psUpdHistLog.execute();
		connFromHistLog.commit();
	}

	public Integer getQtdOperacoes() {
		return qtdOperacoes;
	}

	public void setQtdOperacoes(Integer quantidade) {
		this.qtdOperacoes = quantidade;
	}

	public Integer getIdProcesso() {
		return idProcesso;
	}

	public void setIdProcesso(Integer idProcesso) {
		this.idProcesso = idProcesso;
	}

	private void setIdHistAtual(int int1) {
		this.idHistAtual = int1;
	}

	public int getIdHistAtual() {
		return idHistAtual;
	}

	private void setIdHistLogAtual(int int1) {
		this.idHistLogAtual = int1;
	}

	public int getIdHistLogAtual() {
		return idHistLogAtual;
	}

	public Boolean addSqlInsertTable(String qualifiedTableName, String sqlInsert) {
		return this.sqlInsertTable.put(qualifiedTableName, sqlInsert) == null ? Boolean.TRUE : Boolean.FALSE;
	}

	public String getSqlInsertTable(String schema, String table) {
		return this.sqlInsertTable.get(schema + "." + table);
	}

	public Boolean addQualifiedFK(String schema, String table, String columnFk, String[] qualifiedFk) {
		return this.qualifiedFK.put(schema + "." + table + "." + columnFk, qualifiedFk) == null ? Boolean.TRUE
				: Boolean.FALSE;
	}

	public String[] getQualifiedFK(String schema, String table, String columnFk) {
		return this.qualifiedFK.get(schema + "." + table + "." + columnFk);
	}

}