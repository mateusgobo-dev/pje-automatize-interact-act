/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.PapelDAO;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

/**
 * @author cristof
 * 
 */
@Name("papelService")
@Scope(ScopeType.EVENT)
@Transactional
public class PapelService {

	@In(create = true)
	private PapelDAO papelDAO;
	
	@In(value=Parametros.USUARIO_LOCALIZACAO_ATUAL, required=false, scope=ScopeType.SESSION)
	private UsuarioLocalizacao usuarioLocalizacao;
	
	@Logger
	private Log logger;

	public Papel findByName(String name) {
		return this.papelDAO.findByName(name);
	}

	public Papel findByCodeName(String codeName) {
		return this.papelDAO.findByCodeName(codeName);
	}
	
	public Papel findById(String id){
		if(id == null){
			throw new IllegalArgumentException("Argumento nulo repassado para recuperação do identificador.");
		}
		try {
			Integer papelId = new Integer(id);
			return findById(papelId);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("O argumento de pesquisa não pode ser convertido para um inteiro: " + id);
		}
	}
	
	public Papel findById(int id){
		return papelDAO.find(id);
	}
	
	public Papel getPapelAtual(){
		if(usuarioLocalizacao != null){
			if(usuarioLocalizacao.getPapel() != null){
				return papelDAO.find(usuarioLocalizacao.getPapel().getIdPapel());
			}else{
				logger.warn("A informação sobre a localização atual do usuário não contém papel válido.");
				return null;
			}
		}else{
			logger.warn("A informação sobre a localização atual do usuário não existe na sessão.");
			return null;
		}
	}

}
