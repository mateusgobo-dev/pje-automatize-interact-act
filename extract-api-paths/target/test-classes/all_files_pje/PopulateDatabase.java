/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.itx.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.itx.util.domains.Domains;
import br.com.itx.util.domains.DomainsFactory;

public class PopulateDatabase {

	private final Connection connection;
	private final int qtOfRegisters;
	private Domains domains;

	private DatabaseMetaData dbMeta;
	private Set<String> populatedTables = new HashSet<String>();
	private Map<String, List<String>> enumValues = new HashMap<String, List<String>>();
	private ArrayList<Integer> types;
	private ArrayList<String> names;
	private ArrayList<Integer> sizes;

	public PopulateDatabase(Connection connection, int qtOfRegisters) {
		this.connection = connection;
		this.qtOfRegisters = qtOfRegisters;
	}

	public void clearDatabase() throws SQLException {
		dbMeta = connection.getMetaData();
		populatedTables = new HashSet<String>();
		ResultSet rsTabelas = dbMeta.getTables(null, null, "%", new String[] { "TABLE" });
		while (rsTabelas.next()) {
			String tab = rsTabelas.getString(3);
			if (!populatedTables.contains(tab)) {
				clear(tab);
			}
		}
	}

	private void clear(String tab) throws SQLException {
		ResultSet keys = dbMeta.getExportedKeys(null, null, tab);
		while (keys.next()) {
			String table = keys.getString(7);
			if (!table.equals(tab) && !populatedTables.contains(table)) {
				clear(table);
			}
		}
		PreparedStatement ps = connection.prepareStatement("delete from " + tab);
		System.out.println(ps);
		ps.executeUpdate();
	}

	public void populate(boolean checkDomains) throws SQLException {
		if (checkDomains) {
			domains = DomainsFactory.getDomains(connection);
		}
		populatedTables = new HashSet<String>();
		dbMeta = connection.getMetaData();
		ResultSet rsTabelas = dbMeta.getTables(null, null, "%", new String[] { "TABLE" });
		while (rsTabelas.next()) {
			String tab = rsTabelas.getString(3);
			if (!populatedTables.contains(tab)) {
				populate(tab);
			}
		}
	}

	private void populate(String tab) throws SQLException {
		ResultSet keys = dbMeta.getImportedKeys(null, null, tab);
		while (keys.next()) {
			String table = keys.getString(3);
			if (!table.equals(tab) && !populatedTables.contains(table)) {
				populate(table);
			}
		}
		populateTab(tab);
		populatedTables.add(tab);
	}

	private void populateTab(String tab) throws SQLException {
		ResultSet rs = connection.prepareStatement("select count(*) from " + tab).executeQuery();
		rs.next();
		if (rs.getInt(1) > 0) {
			return;
		}

		System.out.println(tab);
		ResultSet columns = dbMeta.getColumns(null, null, tab, "%");
		StringBuilder sb = new StringBuilder();
		types = new ArrayList<Integer>();
		names = new ArrayList<String>();
		sizes = new ArrayList<Integer>();
		Map<String, Integer> domainsMap;
		if (domains != null) {
			domainsMap = domains.getDomains();
		} else {
			domainsMap = new HashMap<String, Integer>();
		}
		while (columns.next()) {
			String fieldName = columns.getString(4);
			names.add(fieldName);
			String key = columns.getString(6);

			// MySQL
			if (key.equals("enum")) {
				enumValues.put(fieldName, getEnumValues(tab, fieldName));
			}

			if (domainsMap.containsKey(key)) {
				types.add(domainsMap.get(key));
				sizes.add(domains.getDomainLength().get(key));
			} else {
				types.add(columns.getInt(5));
				int len = columns.getInt(7);
				sizes.add(len == -1 ? 255 : len);
			}
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("?");
		}
		sb.insert(0, "insert into " + tab + " values (");
		sb.append("); ");

		executeStatement(connection.prepareStatement(sb.toString()));
	}

	private List<String> getEnumValues(String tab, String fieldName) throws SQLException {
		List<String> values = new ArrayList<String>();
		ResultSet sc = connection.prepareStatement("SHOW COLUMNS FROM " + tab + " LIKE '" + fieldName + "'")
				.executeQuery();
		while (sc.next()) {
			Pattern p = Pattern.compile("'(\\p{L}+)'");
			String type = sc.getString("Type");
			Matcher m = p.matcher(type);
			while (m.find()) {
				values.add(m.group(1));
			}
		}
		return values;
	}

	private void executeStatement(PreparedStatement ps) throws SQLException {

		for (int count = 0; count < qtOfRegisters; count++) {
			int i = 1;
			for (Integer type : types) {
				String fieldName = names.get(i - 1);
				if (enumValues.containsKey(fieldName)) {
					List<String> values = enumValues.get(fieldName);
					int k = count % values.size();
					ps.setString(i, values.get(k));
				} else {
					setParameter(ps, count, i, type);
				}
				i++;
			}
			System.out.println(ps.toString());
			ps.execute();
		}
	}

	private void setParameter(PreparedStatement ps, int count, int i, Integer type) throws SQLException {
		switch (type) {
		case Types.CHAR:
			String val = "S";
			if (names.get(i - 1).equals("tp_processo")) {
				val = "V";
			}
			if (names.get(i - 1).equals("in_sexo")) {
				val = "M";
			}
			if (names.get(i - 1).equals("cd_classificacao_parte") || names.get(i - 1).equals("cd_tipo_usuario")
					|| names.get(i - 1).equals("cd_tipo_parte")) {
				val = new Character((char) ('A' + count)) + "";
			}
			ps.setString(i, val);
			break;
		case Types.INTEGER:
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.NUMERIC:
		case Types.REAL:
		case Types.SMALLINT:
		case Types.TINYINT:
			ps.setInt(i, count + 1);
			break;
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			String value = names.get(i - 1) + " " + (count + 1);
			if (value.length() > sizes.get(i - 1)) {
				value = (count + 1) + " " + value;
				value = value.substring(0, sizes.get(i - 1));
			}
			ps.setString(i, value);
			break;
		case Types.TIME:
			ps.setTime(i, new Time(new java.util.Date().getTime()));
			break;
		case Types.TIMESTAMP:
		case Types.DATE:
			ps.setDate(i, new Date(new java.util.Date().getTime()));
			break;
		case Types.BINARY:
		case Types.LONGVARBINARY:
			ps.setBytes(i, "<BINARY>".getBytes());
			break;
		case Types.BIT:
			ps.setBoolean(i, false);
			break;
		default:
			throw new RuntimeException("Tipo nao identificado: " + type);
		}
	}

	public static void main(String[] args) throws Exception {

		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/dbd_epa_gerado", "postgres",
				"postgres");

		// Class.forName("com.mysql.jdbc.Driver");
		// Connection conn =
		// DriverManager.getConnection("jdbc:mysql://localhost/padroes",
		// "root", "root");

		PopulateDatabase pd = new PopulateDatabase(conn, 30);
		pd.clearDatabase();
		pd.populate(true);
	}

}
