/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.UsuarioLocalizacaoMagistradoServidorDAO;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

/**
 * @author cristof
 * 
 */
@Name("usuarioLocalizacaoMagistradoServidorService")
public class UsuarioLocalizacaoMagistradoServidorService{

	@In
	private UsuarioLocalizacaoMagistradoServidorDAO usuarioLocalizacaoMagistradoServidorDAO;

	public UsuarioLocalizacaoMagistradoServidor persist(UsuarioLocalizacaoMagistradoServidor ulms){
		try{
			return usuarioLocalizacaoMagistradoServidorDAO.persist(ulms);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
