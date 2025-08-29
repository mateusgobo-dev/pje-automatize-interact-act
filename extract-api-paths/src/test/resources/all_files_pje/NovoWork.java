package br.com.infox.cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;

import br.jus.pje.nucleo.util.Crypto;

class NovoWork implements Work {
	private String connUrl = "";

	@Override
	public void execute(Connection conn) throws SQLException {
		// connUrl =
		// conn.getMetaData().getURL().replaceAll("\\?loginTimeout=0&prepareThreshold=0",
		// "");
		PreparedStatement psConn = conn
				.prepareStatement("select * from tb_remessa_processo_host where id_sessao_destino=( "
						+ "select " + "case vl_variavel " + "when 'TRF5' then 0 " + "when 'AL' then 1 "
						+ "when 'CE' then 2 " + "when 'PB' then 3 " + "when 'PE' then 4 " + "when 'RN' then 5 "
						+ "when 'SE' then 6 " + "end "
						//+ "from tb_parametro where nm_variavel ilike 'cdUfRemessaProcesso');");
						+ "from tb_parametro where lower(nm_variavel) like lower('cdUfRemessaProcesso'));");
		ResultSet rs = psConn.executeQuery();
		Crypto crypto = new Crypto("PJE_remessa_2001#@1!");
		String resultado = null;
		if (rs.next()) {
			resultado = rs.getString("ds_url") + "?loginTimeout=10" + "&user=" + rs.getString("ds_login")
					+ "&password=" + crypto.decodeDES(rs.getString("ds_senha"));
		}
		psConn.close();
		this.connUrl = resultado;
	}

	public String getConnUrl() throws SQLException {
		return this.connUrl;
	}
}