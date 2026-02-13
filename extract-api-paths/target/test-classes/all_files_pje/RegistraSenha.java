package br.com.infox.ibpm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.mindrot.jbcrypt.BCrypt;

public class RegistraSenha {

	private static final String DRIVER_CLASS = "org.postgresql.Driver";
	private static final String URL = "jdbc:postgresql://192.168.122.34/pjecomumii";
	private static final String USERNAME = "postgres";
	private static final String PASSWORD = "senha";

	public static void main(String[] args) throws Exception {
		Class.forName(DRIVER_CLASS);
		Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		PreparedStatement st = conn
				.prepareStatement("update tb_usuario_login set ds_senha = ? where id_usuario = ?");
		ResultSet rs = conn.createStatement().executeQuery(
				"select id_usuario, ds_login from tb_usuario_login where ds_senha is not null");
		while (rs.next()) {
			int id = rs.getInt("id_usuario");
			String login = rs.getString("ds_login");
			String hash = BCrypt.hashpw(login, BCrypt.gensalt(12));
			st.setString(1, hash);
			st.setInt(2, id);
			st.execute();
			System.out.println("Login: " + login);
		}
	}
	
	public static void menu(String[] args) throws Exception {
		Class.forName(DRIVER_CLASS);
		Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		sb.append("ds_menu_grupo, ds_menu_item, ");
		sb.append("ds_url from wiacs.tb_wiacs_pagina ");
		sb.append("where ds_menu_grupo is not null and id_sistema = 4079 ");
		sb.append("order by ds_menu_grupo, ds_menu_item");
		ResultSet rs = conn.createStatement().executeQuery(sb.toString());
		while (rs.next()) {
			String grupo = rs.getString(1);
			String item = rs.getString(2);
			String url = rs.getString(3);
			System.out.println("<value>" + grupo + "/" + item + ":" + url + "</value>");
		}
	}

}
