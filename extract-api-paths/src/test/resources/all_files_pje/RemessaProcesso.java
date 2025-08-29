package br.com.infox.cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import br.com.infox.ibpm.home.Authenticator;

public class RemessaProcesso extends AbstractRemessaProcesso {

	public RemessaProcesso() throws SQLException {
	}

	public void validacoes(Integer idProcesso) throws SQLException {
		String sql = "select id_processo, nr_processo from tb_processo p where 1=1 {0} and nr_processo is not null and trim(nr_processo)<>'' and "
				+ "exists (SELECT d.id_processo_documento "
				+ "from tb_processo_documento d "
				+ "inner join tb_proc_doc_bin_pess_assin dba on (d.id_processo_documento_bin=dba.id_processo_documento_bin) "
				+ "left join tb_tipo_processo_documento td on (d.id_tipo_processo_documento=td.id_tipo_processo_documento) "
				+ "where to_ascii(trim(lower(td.ds_tipo_processo_documento))) in ('sentenca', 'despacho') and d.id_processo=p.id_processo) "
				+ "and "
				+ "((select max(ipe.dt_atualizacao) from tb_processo_evento ipe inner join tb_evento_processual ie on (ipe.id_evento=ie.id_evento_processual) where ipe.id_processo=p.id_processo and ie.cd_evento='123A') is null or "
				+ "((select max(ipe.dt_atualizacao) from tb_processo_evento ipe inner join tb_evento_processual ie on (ipe.id_evento=ie.id_evento_processual) where ipe.id_processo=p.id_processo and ie.cd_evento='123A') < "
				+ "(select max(ipe.dt_atualizacao) from tb_processo_evento ipe inner join tb_evento_processual ie on (ipe.id_evento=ie.id_evento_processual) where ipe.id_processo=p.id_processo and ie.cd_evento='123'))) "
				+ "order by p.id_processo;";
		System.out.println("--Iniciando as conexões ....");
		ResultSet rs = null;
		initConnections();
		if (idProcesso != null) {
			rs = createStatementWithValues(getConnFrom(), MessageFormat.format(sql, "and p.id_processo=?"), idProcesso);
		} else {
			rs = getConnFrom().prepareStatement(MessageFormat.format(sql, "")).executeQuery();
		}
		while (rs.next()) {
			rs.getString("nr_processo");
			System.out.println("--####################################################################");
			System.out.println("--Varrendo dados das tabelas do processo[" + rs.getString("nr_processo") + "]["
					+ rs.getString("id_processo") + "]"
					+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS").format(new Date()));
			if (createStatement(getConnTo(), "core", "tb_processo", "nr_processo", rs.getString("nr_processo")).next()) {
				System.out.println("Processo já cadastrado no destino! Não será utilizado no teste!");
			} else {
				RemessaProcessoValidacao remessaProcessoValidacao = new RemessaProcessoValidacao();
				remessaProcessoValidacao.validar(rs.getInt("id_processo"), Authenticator.getUsuarioLogado()
						.getIdUsuario());
				System.out.println(remessaProcessoValidacao.getChecklist());
				/*
				 * try { rollbackTrasaction();
				 * migrarDados(rs.getInt("id_processo")); closeConnections();
				 * initConnections(); } catch (SQLException e) {
				 * e.printStackTrace(); rollbackTrasaction();
				 * closeConnections(); initConnections(); }
				 */
			}
		}
	}

	@Override
	protected void updateIdPkExportedUsuario(Object id) throws SQLException {
		String sqlDoc = "select u.ds_nome, u.ds_login, d.nr_documento_identificacao, pf.id_pessoa_fisica, pj.id_pessoa_juridica "
				+ "from tb_pess_doc_identificacao d "
				+ "left join tb_usuario_login u on (d.id_pessoa=u.id_usuario) "
				+ "left join tb_pessoa_fisica pf on (d.id_pessoa=pf.id_pessoa_fisica) "
				+ "left join tb_pessoa_juridica pj on (d.id_pessoa=pj.id_pessoa_juridica) "
				+ "where cd_tp_documento_identificacao=? and d.in_ativo=true and d.in_usado_falsamente=false and d.id_pessoa=?;";
		String hasLogin = "select 1 from tb_usuario_login u where not exists (select 1 from tb_remessa_proc_usuario ru where ru.id_sessao_origem=? and ru.id_usuario_origem=?) and trim(lower(to_ascii(ds_login)))=trim(lower(to_ascii(?)));";
		String updLogin = "update tb_usuario_login set ds_login=?, ds_senha=? where id_usuario=?;";

		String sqlHasUsuTo = "select d.nr_documento_identificacao, count(*) as quantidade "
				+ "from tb_pess_doc_identificacao d "
				+ "where d.in_ativo=true and d.in_usado_falsamente=false and d.cd_tp_documento_identificacao=? and d.nr_documento_identificacao=? "
				+ "group by d.nr_documento_identificacao";
		String sqlHasUsuToWithName = "select d.nr_documento_identificacao, count(*) as quantidade "
				+ "from tb_pess_doc_identificacao d "
				+ "left join tb_usuario_login u on (d.id_pessoa=u.id_usuario) "
				+ "where d.in_ativo=true and d.in_usado_falsamente=false and d.cd_tp_documento_identificacao=? and d.nr_documento_identificacao=? and trim(lower(to_ascii(u.ds_nome)))=trim(lower(to_ascii(?))) "
				+ "group by d.nr_documento_identificacao";

		String sqlHasUsu = "select * from tb_remessa_proc_usuario where id_usuario_origem=? and id_sessao_origem=?;";

		String sqlInsUsu = "insert into tb_remessa_proc_usuario(id_usuario_origem,id_sessao_origem,id_usuario)values(?,?,"
				+ "(select id_pessoa from tb_pess_doc_identificacao where trim(lower(to_ascii(cd_tp_documento_identificacao)))=trim(lower(to_ascii(?))) and in_ativo=true and in_usado_falsamente=false and nr_documento_identificacao=?));";
		String sqlInsUsuWithName = "insert into tb_remessa_proc_usuario(id_usuario_origem,id_sessao_origem,id_usuario)values(?,?,"
				+ "(select d.id_pessoa from tb_pess_doc_identificacao d "
				+ "left join tb_usuario_login u on (d.id_pessoa=u.id_usuario) "
				+ "where trim(lower(to_ascii(d.cd_tp_documento_identificacao)))=trim(lower(to_ascii(?))) "
				+ "and d.in_ativo=true and d.in_usado_falsamente=false and d.nr_documento_identificacao=? "
				+ "and trim(lower(to_ascii(u.ds_nome)))=trim(lower(to_ascii(?)))))";

		if (createStatementWithValues(getConnTo(), sqlHasUsu, id, getIdUfAtual()).next()) {
			return;
		}

		int qtd = 0;
		boolean duplicado = true;
		ResultSet rsDocCPF = createStatementWithValues(getConnFrom(), sqlDoc, "CPF", id);
		if (rsDocCPF.next()) {
			try {
				/*
				 * ResultSet createStatementWithValues =
				 * createStatementWithValues(getConnTo(), sqlHasUsuTo, "CPF",
				 * rsDocCPF.getObject("nr_documento_identificacao"));
				 * if(createStatementWithValues.next() &&
				 * (qtd=createStatementWithValues.getInt("quantidade"))==1){
				 * PreparedStatement ps =
				 * getConnTo().prepareStatement(sqlInsUsu); ps.setObject(1, id);
				 * ps.setObject(2, getIdUfAtual()); ps.setObject(3, "CPF");
				 * ps.setObject(4,
				 * rsDocCPF.getObject("nr_documento_identificacao"));
				 * ps.execute(); duplicado = false; if(ps.getUpdateCount()>0){
				 * logDataSecaoAtual(ps); } }else { }
				 */
				ResultSet createStatementWithValues2 = createStatementWithValues(getConnTo(), sqlHasUsuToWithName,
						"CPF", rsDocCPF.getObject("nr_documento_identificacao"), rsDocCPF.getObject("ds_nome"));
				if (createStatementWithValues2.next() && (qtd = createStatementWithValues2.getInt("quantidade")) == 1) {
					PreparedStatement ps = getConnTo().prepareStatement(sqlInsUsuWithName);
					ps.setObject(1, id);
					ps.setObject(2, getIdUfAtual());
					ps.setObject(3, "CPF");
					ps.setObject(4, rsDocCPF.getObject("nr_documento_identificacao"));
					ps.setObject(5, rsDocCPF.getObject("ds_nome"));
					ps.execute();
					duplicado = false;
					if (ps.getUpdateCount() > 0) {
						logDataSecaoAtual(ps);
					}
				}
				if (qtd > 1 && duplicado) {
					throw new SQLException("CPF DUPLICADO -> " + rsDocCPF.getObject("nr_documento_identificacao"));
				}
			} catch (SQLException ex) {
				throw ex;
			}
		} else {
			ResultSet rsDocCNPJ = createStatementWithValues(getConnFrom(), sqlDoc, "CPJ", id);
			if (rsDocCNPJ.next()) {
				try {
					/*
					 * ResultSet createStatementWithValues =
					 * createStatementWithValues(getConnTo(), sqlHasUsuTo,
					 * "CPJ",
					 * rsDocCNPJ.getObject("nr_documento_identificacao"));
					 * if(createStatementWithValues.next() &&
					 * (qtd=createStatementWithValues.getInt("quantidade"))==1){
					 * PreparedStatement ps =
					 * getConnTo().prepareStatement(sqlInsUsu); ps.setObject(1,
					 * id); ps.setObject(2, getIdUfAtual()); ps.setObject(3,
					 * "CPJ"); ps.setObject(4,
					 * rsDocCNPJ.getObject("nr_documento_identificacao"));
					 * ps.execute(); duplicado = false;
					 * if(ps.getUpdateCount()>0){ logDataSecaoAtual(ps); } }else
					 * { }
					 */
					ResultSet createStatementWithValues2 = createStatementWithValues(getConnTo(), sqlHasUsuToWithName,
							"CPJ", rsDocCNPJ.getObject("nr_documento_identificacao"), rsDocCNPJ.getObject("ds_nome"));
					if (createStatementWithValues2.next()
							&& (qtd = createStatementWithValues2.getInt("quantidade")) == 1) {
						PreparedStatement ps = getConnTo().prepareStatement(sqlInsUsuWithName);
						ps.setObject(1, id);
						ps.setObject(2, getIdUfAtual());
						ps.setObject(3, "CPJ");
						ps.setObject(4, rsDocCNPJ.getObject("nr_documento_identificacao"));
						ps.setObject(5, rsDocCNPJ.getObject("ds_nome"));
						ps.execute();
						duplicado = false;
						if (ps.getUpdateCount() > 0) {
							logDataSecaoAtual(ps);
						}
					}
					if (qtd > 1 && duplicado) {
						throw new SQLException("CNPJ DUPLICADO -> " + rsDocCNPJ.getObject("nr_documento_identificacao"));
					}
				} catch (SQLException ex) {
					throw ex;
				}
			}
		}
		if (!duplicado) {
			containsPopulatedTable("acl", "tb_usuario_login", id);
		}
	}

	@Override
	protected void cadastraDocumentosPessoa(Object idPessoa) throws SQLException {
		ResultSet rsDocId = createStatement(getConnFrom(), "client", "tb_pess_doc_identificacao", "id_pessoa",
				idPessoa);
		while (rsDocId.next()) {
			migrarTabela("client", "tb_pess_doc_identificacao",
					rsDocId.getObject(getIdColumn("client", "tb_pess_doc_identificacao")));
		}
	}

}
