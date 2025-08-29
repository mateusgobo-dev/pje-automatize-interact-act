package br.com.infox.ibpm.home;

import java.util.Comparator;

import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * Comparator usado para ordenação da Lista de Localizações do usuário interno
 * pelos criterios: 1º Administradores - 2º Servidores - 
 * 3º toString do UsuarioLocalizacaoMagistradoServidor
 * 
 * @author rodrigo
 * 
 */
public class UsuarioLocalizacaoComparator implements Comparator<UsuarioLocalizacao> {

	@Override
	public int compare(UsuarioLocalizacao o1, UsuarioLocalizacao o2) {
		String nameO1 = o1.toString().toUpperCase();
		String nameO2 = o2.toString().toUpperCase();
		
		if(isAdmin(o1) && !isAdmin(o2)) {
			return -1;
		}else if(!isAdmin(o1) && isAdmin(o2)) {
			return 1;
		}
		if(isServidor(o1) && !isServidor(o2)) {
			return -1;
		}else if(!isServidor(o1) && isServidor(o2)) {
			return 1;
		}
		
		return nameO1.compareTo(nameO2);
	}

	private boolean isServidor(UsuarioLocalizacao ul) {
		Papel papel = ul.getPapel();
		Authenticator.instance();
		return (!Authenticator.isUsuarioExterno(papel));
	}
	
	private boolean isAdmin(UsuarioLocalizacao ul) {
		Papel papel = ul.getPapel();
		Authenticator.instance();
		return (Authenticator.isPapelAdministrador(papel));
	}
}