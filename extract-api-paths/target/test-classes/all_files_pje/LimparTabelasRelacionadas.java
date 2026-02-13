package br.com.infox.cliente;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import br.com.itx.util.FileUtil;


/**
 * Classe que recebe um arquivo e abre conexão para executar o script que está no arquivo
 * 
 * 
 * @author wilson
 * 
 */
public class LimparTabelasRelacionadas {
	
	private Connection connection;
	
	public LimparTabelasRelacionadas(Connection connection) {
		this.connection = connection;
	}

	
	public String executeScript(String file) throws IOException {
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;
		try {
			String fileName = LimparBaseDados.class.getResource(file).getFile();
			reader = new BufferedReader(new FileReader(fileName));
			String text = null;
			PreparedStatement ps = null;
			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
//				contents.append(text).append(System.getProperty("line.separator"));
				try {
					ps = connection.prepareStatement(text);
					ps.executeUpdate();
				} catch (Exception e) {
//					e.printStackTrace();
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}catch (IOException e) {
			System.out.println(e);  
		}finally {
			FileUtil.close(reader);
		}
		System.out.println(contents.toString());
		System.out.println("Processando...");
		return contents.toString();
	}
}