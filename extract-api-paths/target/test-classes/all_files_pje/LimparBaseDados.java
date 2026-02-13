package br.com.infox.cliente;

import java.sql.Connection;
import java.sql.DriverManager;


/**
 * Classe que passa o driver do postgres e chama o método executeScript(), 
 * passando o arquivo em que vai ser executado o script como parâmetro.
 * 
 * @author wilson
 * 
 */
public class LimparBaseDados {
	
	public static void main(String[] args) throws Exception {
		Class.forName("org.postgresql.Driver");
		Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/desbd_pje_unificada_trt12_2grau",
				"postgres", "postgres");

		LimparTabelasRelacionadas ltr = new LimparTabelasRelacionadas(conn);		
		ltr.executeScript("/limpar_base_dados.sql");
		ltr.executeScript("/limpar_base_entidades.sql");
		System.out.println("");
		System.out.println("Limpeza dos Schemas efetuada com sucesso.");
	}
}