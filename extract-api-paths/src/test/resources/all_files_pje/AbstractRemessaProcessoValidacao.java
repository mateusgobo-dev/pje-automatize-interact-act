package br.com.infox.cliente;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.hibernate.Session;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe de validação da remessa de processos
 * 
 * Os detalhes da conexão estão no arquivo remessa_processo.properties
 * 
 * @author ruiz/marcone
 * 
 */
public abstract class AbstractRemessaProcessoValidacao implements Serializable{

	private static final long serialVersionUID = 1L;

	private Integer idUfAtual;
	private Integer idUfDestino;
	private Connection connFromHistLog, connFrom, connTo, connFrom_bin, connTo_bin;
	private DatabaseMetaData dbMetaFrom;
	private Set<String> populatedTables = new HashSet<String>();
	private Set<String> logTablesExported = new HashSet<String>();
	private StringBuilder checkList = new StringBuilder();
	private String nrProcessoAtual;
	private Integer qtdOperacoes = 0;
	private static Properties properties;
	private static Properties propertiesFrom;
	private final String urlFrom;
	private final String urlFrom_bin;

	// private static Properties propertiesFrom;

	public AbstractRemessaProcessoValidacao() throws SQLException{
		InputStream inStream;
		properties = new Properties();
		inStream = AbstractRemessaProcesso.class.getResourceAsStream("/remessa_processo.properties");
		try{
			properties.load(inStream);
		} catch (IOException e){
			e.printStackTrace();
		}
		setIdUfDestino(getPrefixUF(getUfToProperty()));
		this.urlFrom = pegaConexaoLocal();
		this.urlFrom_bin = this.urlFrom.substring(0, this.urlFrom.indexOf("?")) + "_bin"
				+ this.urlFrom.substring(this.urlFrom.indexOf("?"));
	}

	private String pegaConexaoLocal() throws SQLException{
		if (this.urlFrom != null){
			return this.urlFrom;
		}
		String resultado = "";
		NovoWork novoWork = new NovoWork();
		((Session) EntityUtil.getEntityManager().getDelegate()).doWork(novoWork);
		resultado = novoWork.getConnUrl();
		return resultado;
	}

	public Boolean verificaConexao(){
		try{
			initConnections();
		} catch (Exception e){
			return Boolean.FALSE;
		}
		if ((connFrom != null) && (connFrom_bin != null) && (connTo != null) && (connTo_bin != null)){
			try{
				closeConnections();
			} catch (Exception e){
				// TODO tratar erro ao fechar conexão?
			}
			return Boolean.TRUE;
		}
		else{
			return Boolean.FALSE;
		}
	}

	private String getUrlFrom(){
		String resultado = "";
		/*
		 * EntityManager em = EntityUtil.getEntityManager(); Connection connection = ((Session) em.getDelegate()).connection(); try { resultado =
		 * connection.getMetaData().getURL() .replaceAll("\\?loginTimeout=0&prepareThreshold=0", ""); connFrom =
		 * DriverManager.getConnection(resultado, properties.getProperty("from.userName"), properties.getProperty("from.password")); if (connFrom !=
		 * null) { connFrom.setAutoCommit(false); connFrom.close(); System.out.println("OK"); } } catch (SQLException e) { e.printStackTrace(); }
		 */
		// em.getTransaction().commit();
		return resultado;
	}

	protected void initConnections() throws SQLException{
		try{
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e){
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
		if (rsUfFrom.next()){
			setIdUfAtual(getPrefixUF(rsUfFrom.getString("vl_variavel")));
		}
		else{
			throw new SQLException("Parâmetro cdUfRemessaProcesso não definido!");
		}
		setIdUfDestino(getPrefixUF(getUfToProperty()));

		if (getIdUfDestino().equals(getIdUfAtual())){
			throw new SQLException("Origem e destino iguais");
		}

		// Pega a URL destino baseado no UF destino da base atual(Origem)
		StringBuilder builder = new StringBuilder();
		builder.append("select ");
		builder
				.append(" case when ((select vl_variavel from core.tb_parametro where nm_variavel='inRemessaProcessoProducao') :: boolean) then h.ds_url ");
		builder.append(" else h.ds_url_homologacao end as ds_url_destino, ds_login, ds_senha ");
		builder.append(" from client.tb_remessa_processo_host h where h.id_sessao_destino=?;");
		PreparedStatement psDestino = connFrom.prepareStatement(builder.toString());
		psDestino.setInt(1, getIdUfDestino());
		ResultSet rsDestino = psDestino.executeQuery();
		if (rsDestino.next()){
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
		}
		else{
			throw new SQLException(
					"Destino não cadastrado na tabela client.tb_remessa_processo_host ou parâmetro inRemessaProcessoProducao não foi definido.");
		}
	}

	public void rollbackTrasaction() throws SQLException{
		connTo.rollback();
		connTo_bin.rollback();
		connFrom.rollback();
		connFrom_bin.rollback();
		connTo.setAutoCommit(false);
		connTo_bin.setAutoCommit(false);
		connFrom.setAutoCommit(false);
		connFrom_bin.setAutoCommit(false);
		// System.out.println("--rollback;("+ new
		// SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date())+")");
		// --------------------
		// connFromHistLog.rollback();
		// connFromHistLog.setAutoCommit(false);
	}

	public void closeConnections() throws SQLException{
		// connTo_bin.commit();
		// connTo.commit();
		connTo.close();
		connTo_bin.close();
		// connFrom.commit();
		connFrom.close();
		connFrom_bin.close();
		// --------------------
		connFromHistLog.close();
	}

	/**
	 * Método utilizado para migrar o processo e os dados associados a ele.
	 * 
	 * @param idProcesso
	 * @throws SQLException
	 */
	public void validar(Integer idProcesso, Integer idUsuarioSolicitante) throws SQLException{
		try{
			initConnections();
			migrarProcesso(idProcesso);
			contabilizarDocumentosBinarios(idProcesso);
			createLogRemessa(idProcesso, idUsuarioSolicitante);
		} catch (SQLException e){
			throw e;
		} finally{
			rollbackTrasaction();
			closeConnections();
		}
	}

	private void contabilizarDocumentosBinarios(Integer idProcesso) throws SQLException{
		String sqlBinTo = "select id_processo_documento_bin from core.tb_processo_documento_bin where id_sessao_pg=? and ID_PK_TB_PRO_DOCUM_BIN_PG=?;";
		ResultSet rsBins = createStatement(connFrom, "core", "tb_processo_documento", "id_processo", idProcesso);
		while (rsBins.next()){
			PreparedStatement psBinTo = connTo.prepareStatement(sqlBinTo);
			psBinTo.setObject(1, getIdUfAtual());
			psBinTo.setObject(2, rsBins.getObject("id_processo_documento_bin"));
			ResultSet rsIdBinDestino = psBinTo.executeQuery();
			if (rsIdBinDestino.next()){
				ResultSet rsBin = createStatement(connFrom_bin, "core", "tb_processo_documento_bin",
						rsBins.getObject("id_processo_documento_bin"));
				if (rsBin.next()){
					qtdOperacoes++;
				}
			}
		}
	}

	private void createLogRemessa(Integer idProcesso, Integer idUsuarioSolicitante) throws SQLException{
		String sqlHasHistRemessa = "select id_remessa_processo_historico "
				+ "from client.tb_remessa_proc_historico "
				+ "where id_processo_trf=? and id_sessao_destino=? and in_remetido=false and in_status<>'F' "
				+ "order by dt_cadastro desc limit 1";
		String sqlUpdHistRemessa = "update client.tb_remessa_proc_historico set "
				+ "in_status='A' where id_remessa_processo_historico=?;";
		String sqlInsHistRemessa = "INSERT INTO client.tb_remessa_proc_historico( "
				+ "id_processo_trf, id_sessao_destino) " + "VALUES (?, ?);";
		String sqlInsHistRemessaLog = "INSERT INTO client.tb_remessa_proc_hist_log( "
				+ "id_remessa_processo_historico, " + "ds_analise_preventiva, qt_possiveis_operacoes, id_pessoa) "
				+ "VALUES (" + "(select id_remessa_processo_historico " + "from client.tb_remessa_proc_historico "
				+ "where id_processo_trf=? and id_sessao_destino=? and in_remetido=false and in_status<>'F' "
				+ "order by dt_cadastro desc), " + "?, ?, ?);";
		try{
			PreparedStatement psHasHist = connFromHistLog.prepareStatement(sqlHasHistRemessa);
			psHasHist.setInt(1, idProcesso);
			psHasHist.setInt(2, getIdUfDestino());
			ResultSet rsHasHist = psHasHist.executeQuery();
			if (!rsHasHist.next()){
				PreparedStatement psInsHistRemessa = connFromHistLog.prepareStatement(sqlInsHistRemessa);
				psInsHistRemessa.setInt(1, idProcesso);
				psInsHistRemessa.setInt(2, getIdUfDestino());
				psInsHistRemessa.execute();
			}
			else{
				PreparedStatement psUpdHistRemessa = connFromHistLog.prepareStatement(sqlUpdHistRemessa);
				psUpdHistRemessa.setInt(1, rsHasHist.getInt("id_remessa_processo_historico"));
				psUpdHistRemessa.execute();
			}
			PreparedStatement psInsHistRemessaLog = connFromHistLog.prepareStatement(sqlInsHistRemessaLog);
			psInsHistRemessaLog.setInt(1, idProcesso);
			psInsHistRemessaLog.setInt(2, getIdUfDestino());
			psInsHistRemessaLog.setObject(3, getChecklist());
			psInsHistRemessaLog.setInt(4, getQtdOperacoes());
			psInsHistRemessaLog.setInt(5, idUsuarioSolicitante);
			psInsHistRemessaLog.execute();
			connFromHistLog.commit();
		} catch (SQLException e){
			System.out.println("Erro ao criar registros nas tabelas de log");
			throw e;
		}
	}

	/**
	 * Método que migra o processo e todas as tabelas que fazem referência a ele
	 * 
	 * @param id
	 * @throws SQLException
	 */
	private void migrarProcesso(Integer id) throws SQLException{
		ResultSet rs = createStatement(connFrom, "core", "tb_processo", id);
		if (!rs.next()){
			throw new SQLException("Processo não encontrado");
		}
		setNrProcessoAtual(rs.getString("nr_processo"));
		String schema = "client";
		String tab = "tb_processo_trf";
		migrarTabela(schema, tab, id);
		// System.out.println("--Varrendo dados das tabelas que referenciam o processo["
		// + getNrProcessoAtual() + "]");
		ResultSet keys = dbMetaFrom.getExportedKeys(null, schema, tab);
		while (keys.next()){
			String sch = keys.getString(6);
			String table = keys.getString(7);
			String column = keys.getString(8);
			if (!table.equals(tab) && !table.equals("tb_processo") && !checkInBlackList(sch, table, column)){
				ResultSet values = createStatement(connFrom, sch, table, column, id);
				while (values.next()){
					Object key = values.getObject(getIdColumn(sch, table));
					if (key != null){
						migrarTabela(sch, table, key);
					}
				}
			}
		}
	}

	/**
	 * Método recursivo que migra a tabela passada como parâmetro e as tabelas que ela depende. As tabelas contidas na propriedade
	 * <b>app.full_table_export</b> do arquivo de propriedades <b>preferences.properties</b>, serão exportadas as tabelas que a referenciam.
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @throws SQLException
	 * @see {@link #checkFullTableExport(String, String)}
	 */
	private void migrarTabela(String schema, String table, Object id) throws SQLException{
		if (// checkInBlackList(schema, table) ||
		containsPopulatedTable(schema, table, id) || checkInserted(schema, table, id)){
			return;
		}
		this.qtdOperacoes++;
		if (checkInBlackList(schema, table)){
			this.qtdOperacoes--;
		}
		// System.out.println("--" + schema + "." + table + ":" + id + "[" + new
		// SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()) +
		// "] - BEGIN{");
		ResultSet keys = dbMetaFrom.getImportedKeys(null, schema, table);
		while (keys.next()){
			String fkSchema = keys.getString(2);
			String fkTable = keys.getString(3);
			String fkColumn = keys.getString(8);
			if (!fkTable.equals(table) && !checkInBlackList(schema, table, fkColumn)){
				ResultSet values = createStatement(connFrom, schema, table, id);
				if (values.next()){
					Object key = values.getObject(fkColumn);
					if (key != null){
						migrarTabela(fkSchema, fkTable, key);
					}
				}
			}
		}
		// addPopulatedTable(schema, table, id);
		// migraDados(schema, table, id);
		String[] rootTableProperty = getRootTableProperty(schema, table);
		if (rootTableProperty != null){
			for (String tableRoot : rootTableProperty){
				String[] tableNameArray = getTableNameArray(tableRoot);
				migrarTabela(tableNameArray[0], tableNameArray[1], id);
			}
		}
		if (checkFullTableExport(schema, table)){
			keys = dbMetaFrom.getExportedKeys(null, schema, table);
			while (keys.next()){
				String expSchema = keys.getString(6);
				String expTable = keys.getString(7);
				String expColumn = keys.getString(8);
				if (!table.equals(expTable) && !checkInBlackList(expSchema, expTable, expColumn)){
					ResultSet values = createStatement(connFrom, expSchema, expTable, expColumn, id);
					while (values.next()){
						Object key = values.getObject(getIdColumn(expSchema, expTable));
						if (key != null){
							migrarTabela(expSchema, expTable, key);
						}
					}
				}
			}
		}
	}

	/**
	 * Verifica se já existe o registro no TRF5
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @throws SQLException
	 */
	private boolean checkInserted(String schema, String table, Object id) throws SQLException{
		try{

			String[] uniqueProperties = getUniqueProperties(schema, table);

			if (uniqueProperties != null){
				if (uniqueProperties[0].equals("acl.tb_usuario_login")){
					updateIdPkExportedUsuario(id);
					// updateIdPkExportedUsuario32(id);
				}

				String[] tableNameArray = getTableNameArray(uniqueProperties[0]);
				if (checkPkTableExport(tableNameArray[0], tableNameArray[1])){
					if (!checkStatementExportedPkTo(tableNameArray[0], tableNameArray[1], id)
							&& checkInBlackList(tableNameArray[0], tableNameArray[1])){
						appendCheckList(tableNameArray[0] + "-" + tableNameArray[1]);
					}
				}
				ResultSet rsValuesQueryUniqueFrom = createStatement(connFrom, tableNameArray[0], tableNameArray[1], id);
				if (rsValuesQueryUniqueFrom.next()){
					if (!tableNameArray[1].equals(table)){
						if (!checkStatementTo(schema, table, tableNameArray[0], tableNameArray[1], uniqueProperties[1],
								rsValuesQueryUniqueFrom) && checkInBlackList(tableNameArray[0], tableNameArray[1])){
							String[] colunas = uniqueProperties[1].split(",");
							String resultado = "";
							for (String coluna : colunas){
								resultado += coluna + ":" + rsValuesQueryUniqueFrom.getObject(coluna) + ";";
							}
							appendCheckList(tableNameArray[0] + "." + tableNameArray[1] + "[" + resultado + "]");
						}
					}
					else{
						ResultSet rsValuesQueryUniqueTo = createCheckStatementTo(tableNameArray[0], tableNameArray[1],
								uniqueProperties[1], rsValuesQueryUniqueFrom);
						if (!rsValuesQueryUniqueTo.next() && checkInBlackList(tableNameArray[0], tableNameArray[1])){
							String[] colunas = uniqueProperties[1].split(",");
							String resultado = "";
							for (String coluna : colunas){
								resultado += coluna + ":" + rsValuesQueryUniqueFrom.getObject(coluna) + ";";
							}
							appendCheckList(tableNameArray[0] + "." + tableNameArray[1] + "[" + resultado + "]");
							return true;
						}
					}
				}
			}
		} catch (SQLException ex){
			ex.printStackTrace();
			throw ex;
		}
		return false;

	}

	protected abstract void updateIdPkExportedUsuario(Object id) throws SQLException;

	/**
	 * Funcão que retorna o schema, tabela e coluna da FK passada no parâmetro.
	 * 
	 * @param schema
	 * @param table
	 * @param fkColumn
	 * @return {@link String[]}
	 * @throws SQLException
	 */
	private String[] getQualifiedFk(String schema, String table, String fkColumn) throws SQLException{
		String[] resultado = new String[3];
		ResultSet keys = dbMetaFrom.getImportedKeys(null, schema, table);
		while (keys.next()){
			if (keys.getString(8).equals(fkColumn)){
				resultado[0] = keys.getString(2);
				resultado[1] = keys.getString(3);
				resultado[2] = keys.getString(4);
				return resultado;
			}
		}
		return null;
	}

	private ResultSet createCheckStatementTo(String schema, String tab, String idColumns, ResultSet rsIdsFrom)
			throws SQLException{
		if (idColumns.isEmpty()){
			System.out.println(tab + "------------------------");
		}
		String[] idColumnArray = idColumns.split(",");
		String pattern = "select * from {0}.{1} where 1=1";
		String codiction = "";
		for (String idColumn : idColumnArray){
			codiction += " and trim(lower(to_ascii(" + idColumn + ")))=trim(lower(to_ascii(?)))";
		}
		String sql = MessageFormat.format(pattern, schema, tab, idColumns) + codiction;
		PreparedStatement ps = connTo.prepareStatement(sql);
		int i = 1;
		for (String idColumn : idColumnArray){
			ps.setObject(i++, rsIdsFrom.getObject(idColumn));
		}
		return ps.executeQuery();
	}

	private boolean checkStatementTo(String schema, String table, String schemaUnq, String tableUnq, String idColumns,
			ResultSet rsIdsFrom) throws SQLException{
		if (idColumns.isEmpty()){
			System.out.println(table + "------------------------");
		}
		String[] idColumnArray = idColumns.split(",");
		String pattern = "select * from {0}.{1} where {2}=(select {3} from {4}.{5} where 1=1 ";
		String codiction = "";
		for (String idColumn : idColumnArray){
			codiction += " and trim(lower(to_ascii(" + idColumn + ")))=trim(lower(to_ascii(?)))";
		}
		String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table),
				getIdColumn(schemaUnq, tableUnq), schemaUnq, tableUnq, idColumns)
				+ codiction + ")";
		PreparedStatement ps = connTo.prepareStatement(sql);
		int i = 1;
		for (String idColumn : idColumnArray){
			ps.setObject(i++, rsIdsFrom.getObject(idColumn));
		}
		return ps.executeQuery().next();
	}

	private boolean checkStatementExportedPkTo(String schema, String table, Object id) throws SQLException{
		String pattern = "select * from {0}.{1} where {2}=? and {3}=?";
		String sql = MessageFormat.format(pattern, schema, table, "id_sessao_pg", getExportedIdColumn(table));
		PreparedStatement ps = connTo.prepareStatement(sql);
		ps.setObject(1, getIdUfAtual());
		ps.setObject(2, id);
		return ps.executeQuery().next();
	}

	private boolean checkStatementExportedQualifiedPkTo(String schema, String table, Object id) throws SQLException{
		String qualifiedPkTableExportProperty = getQualifiedPkTableExportProperty(schema, table);
		if (qualifiedPkTableExportProperty != null){
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

	protected ResultSet createStatement(Connection connection, String schema, String table, Object id)
			throws SQLException{
		String pattern = "select * from {0}.{1} where {2}=?";
		String sql = MessageFormat.format(pattern, schema, table, getIdColumn(schema, table));
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setObject(1, id);
		return ps.executeQuery();
	}

	protected ResultSet createStatement(Connection connection, String schema, String table, String column, Object id)
			throws SQLException{
		String pattern = "select * from {0}.{1} where {2}=?";
		String sql = MessageFormat.format(pattern, schema, table, column);
		PreparedStatement ps = connection.prepareStatement(sql);
		ps.setObject(1, id);
		return ps.executeQuery();
	}

	protected ResultSet createStatementWithValues(Connection connection, String query, Object... valores)
			throws SQLException{
		PreparedStatement ps = connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		int i = 1;
		for (Object valor : valores){
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
	private String getIdColumn(String schema, String table) throws SQLException{
		ResultSet keys = dbMetaFrom.getPrimaryKeys(null, schema, table);
		if (keys.next()){
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
	private String getExportedIdColumn(String table){
		return "id_pk_" + table + "_pg";
	}

	/**
	 * Retorna um array com o nome do schema e da tabela
	 * 
	 * @param quilifiedTableName
	 * @return
	 */
	private String[] getTableNameArray(String quilifiedTableName){
		if (quilifiedTableName != null){
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
	private boolean checkPkTableExport(String schema, String table){
		String property = properties.getProperty("app.table_pk_export");
		if (property != null){
			Set<String> properties = new HashSet<String>(Arrays.asList(property.split(";")));
			if (properties.contains(schema + "." + table)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se é uma tabela será exportada completamente(tabelas das chaves importadas e exportadas);
	 * 
	 * @param schema
	 * @param table
	 */
	private boolean checkFullTableExport(String schema, String table){
		String property = properties.getProperty("app.full_table_export");
		if (property != null){
			Set<String> properties = new HashSet<String>(Arrays.asList(property.split(",")));
			if (properties.contains(schema + "." + table)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se a tabela está na lista negra, mas antes, a lista branca é consultada, se existir, retorna verdadeiro e a lista negra não é
	 * consultada, senão, a lista negra é consultada.
	 * 
	 * @param schema
	 * @param table
	 * @throws SQLException
	 */
	private boolean checkInBlackList(String schema, String table){
		String propertyWl = getWhiteListProperty(schema, table);
		if (propertyWl != null){
			return false;
			/*
			 * Set<String> propertieWhiteList = new HashSet<String>(Arrays.asList(propertyWl.split(","))); if (propertieWhiteList.contains("") ||
			 * propertieWhiteList.contains("all")){ }
			 */
		}
		return true;
	}

	/**
	 * Verifica se a coluna está na lista negra, mas antes, a lista branca é consultada, se existir, retorna verdadeiro e a lista negra não é
	 * consultada, senão, a lista negra é consultada.
	 * 
	 * @param schema
	 * @param table
	 * @throws SQLException
	 */
	private boolean checkInBlackList(String schema, String table, String column) throws SQLException{
		if (checkPkTableExport(schema, table)){
			if (column.equals(getExportedIdColumn(table)) || column.equals("id_sessao_pg")){
				return true;
			}
		}
		String propertyWl = getWhiteListProperty(schema, table);
		if (propertyWl != null){
			Set<String> propertieWhiteList = new HashSet<String>(Arrays.asList(propertyWl.split(",")));
			if (propertieWhiteList.contains("") || propertieWhiteList.contains("all")
					|| propertieWhiteList.contains(column)){
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param schema
	 * @param table
	 * @return Retorna null caso não exista a propriedade, senão, [coluna,nome.esquema]
	 */
	private String[] getUniqueProperties(String schema, String table){
		String[] resultado = new String[2];
		String property = getUniqueColumnProperty(schema, table);
		if (property != null){
			String propertyPai = getUniqueColumnProperty(property);
			if (propertyPai != null){
				resultado[0] = property;
				resultado[1] = propertyPai;
				return resultado;
			}
			else{
				resultado[0] = schema + "." + table;
				resultado[1] = property;
				return resultado;
			}
		}
		return null;
	}

	/**
	 * Retorna o id da UF em que a classe está sendo executada Préfixo das seções AL=1, CE=2, PB=3, PE=4, RN=5, SE=6
	 * 
	 * @return
	 */
	public Integer getPrefixUF(String valor) throws SQLException{
		String uf = valor;
		if (uf != null){
			uf = uf.toLowerCase().trim();
		}
		if (uf.equals("trf5")){
			return 0;
		}
		else if (uf.equals("al")){
			return 1;
		}
		else if (uf.equals("ce")){
			return 2;
		}
		else if (uf.equals("pb")){
			return 3;
		}
		else if (uf.equals("pe")){
			return 4;
		}
		else if (uf.equals("rn")){
			return 5;
		}
		else if (uf.equals("se")){
			return 6;
		}
		else{
			throw new SQLException("Código do estado não foi transformado!");
		}
	}

	/**
	 * Verifica se a tabela foi migrada
	 * 
	 * @param schema
	 * @param table
	 * @param id
	 * @return
	 */
	private boolean containsPopulatedTable(String schema, String table, Object id){
		boolean resultado = false;
		String[] uniqueProperties = getUniqueProperties(schema, table);
		if (uniqueProperties != null && uniqueProperties[0].equals("acl.tb_usuario_login")){
			this.logTablesExported.add("acl.tb_usuario_login");
			resultado = !this.populatedTables.add("acl.tb_usuario_login:" + id.toString());
		}
		else{
			this.logTablesExported.add(schema + "." + table);
			resultado = !this.populatedTables.add(schema + "." + table + ":" + id.toString());
		}
		return resultado;
	}

	/**
	 * Verifica se é uma tabela terá sua PK exportada de forma unica;
	 * 
	 * @param schema
	 * @param table
	 */
	private String getQualifiedPkTableExportProperty(String schema, String table){
		return properties.getProperty("qualifiedpk." + schema + "." + table);
	}

	private String getWhiteListProperty(String schema, String table){
		return properties.getProperty("whitelist." + schema + "." + table);
	}

	private String getUniqueColumnProperty(String schema, String table){
		return properties.getProperty("unique." + schema + "." + table);
	}

	private String getUniqueColumnProperty(String qualifiedTableName){
		return properties.getProperty("unique." + qualifiedTableName);
	}

	private String[] getRootTableProperty(String schema, String table){
		String propertyRoot = properties.getProperty("root." + schema + "." + table);
		if (propertyRoot != null){
			return propertyRoot.split(";");
		}
		return null;
	}

	private String getUfToProperty(){
		return properties.getProperty("to.uf");
	}

	public Integer getIdUfAtual(){
		return idUfAtual;
	}

	public void setIdUfAtual(Integer idUfAtual){
		this.idUfAtual = idUfAtual;
	}

	public Integer getIdUfDestino(){
		return idUfDestino;
	}

	public void setIdUfDestino(Integer idUfDestino){
		this.idUfDestino = idUfDestino;
	}

	protected void appendCheckList(String valor){
		this.checkList.append("-> " + valor + ";\n");
	}

	public String getChecklist(){
		return this.checkList.toString();
	}

	public String getNrProcessoAtual(){
		return nrProcessoAtual;
	}

	public void setNrProcessoAtual(String nrProcessoAtual){
		this.nrProcessoAtual = nrProcessoAtual;
	}

	protected Connection getConnFrom(){
		return connFrom;
	}

	protected void setConnFrom(Connection connFrom){
		this.connFrom = connFrom;
	}

	protected Connection getConnTo(){
		return connTo;
	}

	protected void setConnTo(Connection connTo){
		this.connTo = connTo;
	}

	public Integer getQtdOperacoes(){
		return qtdOperacoes;
	}

}