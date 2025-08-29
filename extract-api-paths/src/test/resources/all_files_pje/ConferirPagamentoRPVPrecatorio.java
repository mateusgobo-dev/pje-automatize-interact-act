/**
 * @author Rodrigo Menezes da Conceição
 * @author Gabriel Morrison Lima Dantas
 */

package br.com.infox.trf.webservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.itx.util.DatabaseUtil;
import br.com.itx.util.DatabaseUtilRpv;

public class ConferirPagamentoRPVPrecatorio {
	private Connection conPje;
	private Connection conOracle;
	private static Object lock = new Object();

	public ConferirPagamentoRPVPrecatorio() {
	}

	public void execute() throws Exception {
		try {
			conOracle = new DatabaseUtilRpv().getConnection();
			conPje = new DatabaseUtil().getConnection("jdbc:postgresql://172.20.1.110:5432/desbd_pje");

			if ((conPje == null) || (conOracle == null)) {
				throw new SQLException("Sem conexão com o banco de dados.");
			}

			synchronized (lock) {
				conPje.setAutoCommit(false);
				conOracle.setAutoCommit(false);
				conferirPagamentos();
				conPje.commit();
				conOracle.commit();
			}

		} catch (Exception e) {
			e.printStackTrace();
			try {
				conPje.rollback();
				conOracle.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			throw new Exception(e.toString(), e);
		} finally {
			if (conPje != null) {
				conPje.close();
			}
			if (conOracle != null) {
				conOracle.close();
			}
		}
	}

	private void conferirPagamentos() throws Exception {
		String sql = "SELECT NUMEROBANCO, AGENCIA, CONTA, DATADEPOSITO, "
				+ "NR_PRECATORIO_RPV, NR_PROCESSO_ORIGINARIO, "
				+ "NOMERECLAMANTE, CPF_CNPJ_RECLAMANTE, VALORDEP_ORIG, PARCELA, NOMERECLAMADO, VARA,  "
				+ "MUNICIPIO, UF, DATACADASTRO, PROCESSADO_ORIGEM " + "FROM ESPARTA2.REPOSITORIOPAGAMENTO "
				+ "WHERE PROCESSADO_ORIGEM = false ";

		Statement st = conOracle.createStatement();
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()) {
			String nrBanco = rs.getString("NUMEROBANCO");
			String nrAgencia = rs.getString("AGENCIA");
			String codUf = rs.getString("UF");
			String dtDeposito = rs.getString("DATADEPOSITO");
			java.sql.Date dtCadastro = rs.getDate("DATACADASTRO");
			String nrConta = rs.getString("CONTA");
			String nrPrecatorioRpv = rs.getString("NR_PRECATORIO_RPV");
			String nrProcessoOriginario = rs.getString("NR_PROCESSO_ORIGINARIO");
			String nrCpfCnpjReclamente = rs.getString("CPF_CNPJ_RECLAMANTE");
			String nrParcela = rs.getString("PARCELA");
			String nmReclamente = rs.getString("NOMERECLAMANTE");
			String nmReclamado = rs.getString("NOMERECLAMADO");
			String nmMunicipio = rs.getString("MUNICIPIO");
			String nmVara = rs.getString("VARA");
			String vlDepositoOrig = rs.getString("VALORDEP_ORIG");

			/*
			 * Criar um save point, para em caso de erro voltar para o estado
			 * anterior no rollback, mantendo assim os registros que não tiveram
			 * erro.
			 */
			Savepoint savepointCreta = conPje.setSavepoint();
			Savepoint savepointOracle = conOracle.setSavepoint();

			try {
				inserirRpvPagamento(nrBanco, nrAgencia, codUf, dtDeposito, dtCadastro, nrConta, nrPrecatorioRpv,
						nrProcessoOriginario, nrCpfCnpjReclamente, nrParcela, nmReclamente, nmReclamado, nmMunicipio,
						nmVara, vlDepositoOrig);
			} catch (Exception e) {
				conPje.rollback(savepointCreta);
				conOracle.rollback(savepointOracle);
				e.printStackTrace();
			}
		}
	}

	private void inserirRpvPagamento(String nrBanco, String nrAgencia, String codUf, String dtDeposito,
			java.sql.Date dtCadastro, String nrConta, String nrRpvPrecatorio, String nrProcessoOriginario,
			String nrCpfCnpjReclamente, String nrParcela, String nmReclamente, String nmReclamado, String nmMunicipio,
			String nmVara, String vlDepositoOrig) throws Exception {

		String sql = "INSERT INTO tb_rpv_pagamento( " + "id_rpv, nr_banco, nr_agencia, "
				+ "cd_estado, dt_deposito, dt_cadastro, nr_conta, nr_rpv_precatorio, "
				+ "nr_processo, nr_cpf_cnpj_reclamante, nr_parcela, nm_reclamante, "
				+ "nm_municipio, nm_vara, vl_deposito_originario) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conPje.prepareStatement(sql);
		int idRpv = obterIdRpv(nrRpvPrecatorio);
		if (idRpv > 0) {
			ps.setInt(1, idRpv);
		} else {
			ps.setNull(1, Types.INTEGER);
		}

		ps.setString(2, nrBanco);
		ps.setString(3, nrAgencia);
		ps.setString(4, codUf);
		ps.setDate(5, parseData(dtDeposito));
		ps.setDate(6, dtCadastro);
		ps.setString(7, nrConta);
		ps.setString(8, nrRpvPrecatorio);
		ps.setString(9, nrProcessoOriginario);
		ps.setString(10, formataCPFCNPJ(nrCpfCnpjReclamente));
		ps.setInt(11, Integer.parseInt(nrParcela));
		ps.setString(12, nmReclamente);
		ps.setString(13, nmMunicipio);
		ps.setString(14, nmVara);
		DecimalFormat format = new DecimalFormat("###,##0.00");
		float valorDeposito = format.parse(vlDepositoOrig).floatValue();
		ps.setFloat(15, valorDeposito);
		ps.executeUpdate();
		ps.close();
		if (idRpv > 0) { // nr_rpv_precatorio da tb_rpv = NR_PRECATORIO_RPV do
							// TRF
			marcarRpvComoPaga(idRpv);
		}
	}

	private int obterIdRpv(String nrRpvPrecatorio) throws Exception {
		String sql = "select id_rpv from tb_rpv where " + "nr_rpv_precatorio = " + nrRpvPrecatorio;
		Statement st = conPje.createStatement();
		ResultSet rs = st.executeQuery(sql);
		int idRpv = 0;
		while (rs.next()){
			idRpv = rs.getInt("id_rpv");
		}
		st.close();
		return idRpv;
	}

	/*
	 * Marca RPV como Paga.
	 */
	private void marcarRpvComoPaga(int idRpv) throws Exception {
		String sql = "update tb_rpv set id_rpv_status = ? where " + "id_rpv = ?";
		PreparedStatement ps = conPje.prepareStatement(sql);
		ps.setInt(1, 3); // Id do status Paga na tb_rpv_status do PJE
		ps.setInt(2, idRpv);
		ps.executeUpdate();
		ps.close();
	}

	private String formataCPFCNPJ(String cpfCnpj) {
		return cpfCnpj.replaceAll("[./-]", "");
	}

	private java.sql.Date parseData(String data) throws ParseException {
		java.sql.Date dataSql = null;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date dataUtil;
		dataUtil = sdf.parse(data);
		dataSql = new java.sql.Date(dataUtil.getTime());
		return dataSql;
	}

	public static void main(String[] args) throws Exception {
		ConferirPagamentoRPVPrecatorio c = new ConferirPagamentoRPVPrecatorio();
		c.execute();
	}
}