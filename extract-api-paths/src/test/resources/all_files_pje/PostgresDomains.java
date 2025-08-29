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
package br.com.itx.util.domains;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class PostgresDomains implements Domains {

	private final Connection connection;

	private Map<String, Integer> domainLength;

	public PostgresDomains(Connection connection) {
		this.connection = connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.com.itx.util.domains.Domains#getDomains()
	 */
	@Override
	public Map<String, Integer> getDomains() throws SQLException {
		String sql = "select t1.typname, " + "(case t2.typname when 'varchar' then t1.typtypmod - 4 "
				+ "else -1 end) as length , t2.typname, t3.consrc "
				+ "from pg_type t1 left join pg_constraint t3 on t1.oid = t3.contypid" + ", pg_type t2 "
				+ "where t1.typbasetype = t2.oid and t1.typtype='d' " + "and t1.typnamespace=2200";

		ResultSet rs = connection.createStatement().executeQuery(sql);
		Map<String, Integer> domains = new HashMap<String, Integer>();
		domainLength = new HashMap<String, Integer>();
		while (rs.next()) {
			String key = rs.getString(1);
			Integer length = rs.getInt(2);
			String value = rs.getString(3);
			// String constraint = rs.getString(4);
			// TODO Verificar como pegar os valores da constraint e colocar
			// igual ao enumValues
			Integer i = null;
			if (value.equals("bpchar")) {
				i = Types.CHAR;
			} else if (value.equals("numeric")) {
				i = Types.FLOAT;
			} else if (value.equals("varchar")) {
				i = Types.VARCHAR;
				domainLength.put(key, length);
			} else if (value.startsWith("int")) {
				i = Types.INTEGER;
			} else if (value.equals("timestamp")) {
				i = Types.TIMESTAMP;
			} else {
				throw new RuntimeException("Nao identificado: " + key + " = " + value);
			}
			domains.put(key, i);
		}
		return domains;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.com.itx.util.domains.Domains#getDomainLength()
	 */
	@Override
	public Map<String, Integer> getDomainLength() {
		if (domainLength == null) {
			try {
				getDomains();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return domainLength;
	}

	public static void main(String[] args) throws Exception {

		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://172.20.1.110:5432/desbd_epa", "postgres",
				"postgres");

		DomainsFactory.getDomains(conn);

		// Class.forName("com.mysql.jdbc.Driver");
		// Connection conn =
		// DriverManager.getConnection("jdbc:mysql://localhost/padroes",
		// "root", "root");

		Domains pd = new PostgresDomains(conn);
		// pd.clearDatabase();
		// pd.populate(true);
		System.out.println(pd.getDomains());
		System.out.println(pd.getDomainLength());
	}

}
