package br.com.infox.ibpm.home;

import java.io.Serializable;
import java.util.Comparator;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

/**
 * Comparator usado para ordenação da Lista de Localizações do usuário interno pelos criterios: 1º Administrador 2º Se for primeiro grau numero da
 * Vara 3º toString do UsuarioLocalizacaoMagistradoServidor
 * 
 * @author rodrigo
 * 
 */
public class UsuarioLocalizacaoMagistradoServidorComparator implements Comparator<UsuarioLocalizacaoMagistradoServidor>, Serializable{

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(UsuarioLocalizacaoMagistradoServidor o1, UsuarioLocalizacaoMagistradoServidor o2){
		if (isAdmin(o1)){
			return -1;
		}
		else if (isAdmin(o2)){
			return 1;
		}

		if (ParametroUtil.instance().isPrimeiroGrau()){
			Integer vara1 = getVara(o1);
			Integer vara2 = getVara(o2);
			if (vara1 != null && vara2 != null){
				return vara1.compareTo(vara2);
			}
		}

		return o1.toString().compareTo(o2.toString());
	}

	private boolean isAdmin(UsuarioLocalizacaoMagistradoServidor ul){
		String papel = ul.getUsuarioLocalizacao().getPapel().getIdentificador();
		return papel.equalsIgnoreCase("admin") || papel.equalsIgnoreCase("administrador");
	}

	private Integer getVara(UsuarioLocalizacaoMagistradoServidor ul){
		return ul.getOrgaoJulgador() != null ? ul.getOrgaoJulgador().getNumeroVara() : null;
	}

}