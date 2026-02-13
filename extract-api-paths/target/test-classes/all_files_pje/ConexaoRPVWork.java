package br.com.infox.cliente;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.jdbc.Work;

class ConexaoRPVWork implements Work {
	private String connUrl = "";
	private final String PARAMETROS_CONEXAO = "";

	@Override
	public void execute(Connection conn) throws SQLException {
		this.connUrl = conn.getMetaData().getURL();
		this.connUrl = this.connUrl.substring(0, this.connUrl.indexOf("?"));
	}

	public String getConnUrl() {
		return connUrl + this.PARAMETROS_CONEXAO;
	}

	public String getConnBinUrl() {
		return connUrl + "_bin" + this.PARAMETROS_CONEXAO;
	}

}