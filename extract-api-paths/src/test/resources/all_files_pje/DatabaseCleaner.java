package br.com.itx.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

/**
 * Classe que efetua a limpeza da base de dados, respeitando os registros
 * referenciados pelos fluxos válidos do sistema.
 * 
 * Um fluxo é considerado válido se ele estiver associado a uma classe, ou for
 * um subfluxo de um fluxo válido.
 * 
 * @author luiz
 * 
 */
@SuppressWarnings("unchecked")
public class DatabaseCleaner {

	private final Connection connection;

	private Set<Integer> fluxoList = new HashSet<Integer>();
	private static Set<Integer> modeloDocumentoList = new HashSet<Integer>();
	private static Set<Integer> localizacaoList = new HashSet<Integer>();

	/**
	 * Map que tem como chave o nome da tabela e como valor um conjunto dos
	 * registros da tabela que deverão PERMANECER na base.
	 */
	private Map<String, Set<Integer>> registrosMap = new HashMap<String, Set<Integer>>();

	/**
	 * Map que tem como chave o nome da tabela e como valor a coluna da chave
	 * primária.
	 */
	private static Map<String, String> primaryKeys = new HashMap<String, String>();

	/**
	 * Expressão SQL para limpeza da base
	 */
	private StringBuilder sql = new StringBuilder();

	/**
	 * Expressões que fazem referencia a registros da base de dados
	 */
	private static final String[] EXPRESSOES = { "modeloDocumento.set" };

	/**
	 * Classes que irão tratar as expressões, na mesma ordem da lista acima
	 */
	private static final Class[] TRATADORES = { ModeloDocumento.class, VerificaEvento.class, PooledActor.class };

	public interface Tratador {
		void trataExpressao(String expressao, Map<String, Set<Integer>> registrosMap, Connection conn);
	}

	static class ModeloDocumento implements Tratador {

		/**
		 * Registra as tabelas com as chaves, que serão tratadas por essa classe
		 */
		static {
			primaryKeys.put("core.tb_modelo_documento", "id_modelo_documento");
		}

		@Override
		public void trataExpressao(String expressao, Map<String, Set<Integer>> registrosMap, Connection conn) {
			Set<Integer> setModelos = registrosMap.get("core.tb_modelo_documento");
			if (setModelos == null) {
				setModelos = new HashSet<Integer>();
				registrosMap.put("core.tb_modelo_documento", setModelos);
			}
			List<String> params = parseParameters(expressao);
			params.remove(0);
			for (String s : params) {
				setModelos.add(Integer.parseInt(s));
				modeloDocumentoList.add(Integer.parseInt(s));
			}
			try {
				for (Integer idModelos : getModeloDocumentoParametro(conn)) {
					setModelos.add(idModelos);
					modeloDocumentoList.add(idModelos);
				}
				;
			} catch (SQLException e) {
				// e.printStackTrace();
			}
		}
	}

	static class VerificaEvento implements Tratador {
		static {
			primaryKeys.put("core.tb_tarefa", "id_tarefa");
		}

		@Override
		public void trataExpressao(String expressao, Map<String, Set<Integer>> registrosMap, Connection conn) {
			Set<Integer> setModelos = registrosMap.get("core.tb_tarefa");
			if (setModelos == null) {
				setModelos = new HashSet<Integer>();
				registrosMap.put("core.tb_tarefa", setModelos);
			}
			List<String> params = parseParameters(expressao);
			for (String s : params) {
				setModelos.add(Integer.parseInt(s));
			}
		}
	}

	static class PooledActor implements Tratador {

		static {
			primaryKeys.put("acl.tb_papel", "id_papel");
			primaryKeys.put("core.tb_localizacao", "id_localizacao");
		}

		@Override
		public void trataExpressao(String expressao, Map<String, Set<Integer>> registrosMap, Connection conn) {
			Set<Integer> setLocal = registrosMap.get("core.tb_localizacao");
			if (setLocal == null) {
				setLocal = new HashSet<Integer>();
				registrosMap.put("core.tb_localizacao", setLocal);
			}
			Set<Integer> setPapel = registrosMap.get("acl.tb_papel");
			if (setPapel == null) {
				setPapel = new HashSet<Integer>();
				registrosMap.put("acl.tb_papel", setPapel);
			}
			List<String> params = parseParameters(expressao);
			for (String s : params) {
				String local = s;
				if (s.contains(":")) {
					local = s.split(":")[0];
					setPapel.add(Integer.parseInt(s.split(":")[1]));
				}
				setLocal.add(Integer.parseInt(local));
				localizacaoList.add(Integer.parseInt(local));
			}
		}
	}

	public DatabaseCleaner(Connection connection) {
		this.connection = connection;
	}

	public void clearBin() throws SQLException {
		System.out.println("Limpando Bin...");
		PreparedStatement ps = null;
		try {
			String query = "delete from core.tb_processo_documento_bin";
			ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
		System.out.println("Limpeza do Bin efetuada com Sucesso.");
	}

	public void clearDatabase() throws SQLException {
		verificaFluxos();
		// buscaParametros();
		geraSql();

		DatabaseCleanerRelacionadas dcr = new DatabaseCleanerRelacionadas(connection);

		System.out.println("Limpando schema Criminal...");
		dcr.limparSchemaCriminal();
		System.out.println("Schema criminal Concluído");

		System.out.println("Executando SqlPreClasse...");
		dcr.executarSqlPreClasse();
		System.out.println("SqlPreClasse Concluído");

		if (fluxoList.size() != 0) {
			System.out.println("Removendo Tabelas relacionadas com o Fluxo...");
			dcr.limparTabelasRelacionadas(fluxoList, modeloDocumentoList, localizacaoList);
			removerTabelas(sql.toString());
			// dcr.removerTipoModeloDocumento();
			// dcr.removerGrupoModeloDocumento();
			System.out.println("Remoção Concluída");
		}

		System.out.println("Executando SqlPosClasse...");
		dcr.executarSqlPosClasse();
		System.out.println("SqlPosClasse Concluído");

		System.out.println("Executando SqlLimparLocalizacao...");
		// dcr.removerLocalizacao();
		System.out.println("SqlLimparLocalizacao Concluído");

		System.out.println("Executando SqlLimparJbpm...");
		dcr.executarSqlJbpm();
		System.out.println("SqlLimparJbpm Concluído");

	}

	private void removerTabelas(String sql) throws SQLException {
		PreparedStatement ps = null;
		ps = connection.prepareStatement(sql);
		ps.executeUpdate();
	}

	private void geraSql() {
		for (Entry<String, Set<Integer>> e : registrosMap.entrySet()) {
			String tabela = e.getKey();
			String pk = primaryKeys.get(tabela);
			append(MessageFormat.format("delete from {0} where {1} not in ({2})", tabela, pk,
					listToString(e.getValue())));
		}
	}

	private void buscaParametros() throws SQLException {
		PreparedStatement ps = null;
		try {
			// busca o xml dos fluxos
			String lista = listToString(fluxoList);
			if (lista.isEmpty()) {
				lista = "0";
			}
			String query = "select ds_xml from core.tb_fluxo where ds_fluxo in ("
					+ "select name_ from jbpm_processdefinition where id_ in (" + lista + "))";
			ps = connection.prepareStatement(query);
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				String xml = resultSet.getString(1);
				buscaExpressoes(xml);
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private void buscaExpressoes(String xml) {
		StringTokenizer st = new StringTokenizer(xml, "#{}");
		st.nextToken();
		while (st.hasMoreTokens()) {
			String expressao = st.nextToken();
			for (int i = 0; i < EXPRESSOES.length; i++) {
				String s = EXPRESSOES[i];
				if (expressao.startsWith(s)) {
					try {
						Tratador t = (Tratador) TRATADORES[i].newInstance();
						t.trataExpressao(expressao, registrosMap, connection);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			st.nextToken();
		}
	}

	private void verificaFluxos() throws SQLException {
		PreparedStatement ps = null;
		try {
			// busca os fluxos que sao referenciados por classes
			String query = "select max(id_) from jbpm_processdefinition where name_ in ("
					+ "select ds_fluxo from core.tb_fluxo where id_fluxo in ("
					+ "select distinct id_fluxo from client.tb_classe_judicial)) group by name_";
			ps = connection.prepareStatement(query);
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				int idFluxo = resultSet.getInt(1);
				if (fluxoList.add(idFluxo)) {
					verificaSubFluxos(idFluxo);
				}
			}
			String lista = listToString(fluxoList);
			append("delete from core.tb_fluxo where ds_fluxo not in ("
					+ "select name_ from jbpm_processdefinition where id_ in (" + lista + "))");
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	private String listToString(Collection c) {
		return c.toString().replaceAll("\\[", "").replaceAll("\\]", "");
	}

	private void append(String string) {
		if (sql.length() > 0) {
			sql.append(";\r\n");
		}
		sql.append(string);
	}

	private void verificaSubFluxos(int idFluxo) throws SQLException {
		PreparedStatement ps = null;
		try {
			String query = "select max(id_), name_ from jbpm_processdefinition where name_ in ("
					+ "select subprocname_ from jbpm_node where processdefinition_ = "
					+ "? and class_ = 'C')group by name_";
			ps = connection.prepareStatement(query);
			ps.setInt(1, idFluxo);
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				int idSubFluxo = resultSet.getInt(1);
				if (fluxoList.add(idSubFluxo)) {
					verificaSubFluxos(idSubFluxo);
				}
			}
		} finally {
			if (ps != null) {
				ps.close();
			}
		}
	}

	public static List<String> parseParameters(String expression) {
		List<String> ret = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(expression, "(',)");
		st.nextToken();
		while (st.hasMoreTokens()) {
			ret.add(st.nextToken().trim());
		}
		return ret;
	}

	public static void main(String[] args) throws Exception {
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pje_capela_1.2.0.M1",
				"postgres", "@TESTE123");

		DatabaseCleaner dc = new DatabaseCleaner(conn);
		dc.clearDatabase();

		Connection connBin = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pje_capela_bin_1.2.0.M1",
				"postgres", "@TESTE123");

		DatabaseCleaner dcBin = new DatabaseCleaner(connBin);
		dcBin.clearBin();
		System.out.println("Limpeza do Banco Concluida com Sucesso.");
	}

	// Busca os modelos de documento que são referenciados nos parâmetros
	public static List<Integer> getModeloDocumentoParametro(Connection connection) throws SQLException {
		PreparedStatement ps = null;
		String query = "select vl_variavel from core.tb_parametro "
				+ "where ds_esquema_tabela_id like 'core.tb_modelo_documento'";
		ps = connection.prepareStatement(query);
		ResultSet resultSet = ps.executeQuery();
		List<Integer> listaModelos = new ArrayList<Integer>(0);
		while (resultSet.next()) {
			Integer idModelo = resultSet.getInt(1);
			listaModelos.add(idModelo);
		}
		return listaModelos;
	}
}
