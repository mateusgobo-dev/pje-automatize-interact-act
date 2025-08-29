package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProtocoloExternoMniDAO;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.ProtocoloExternoMni;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(ProtocoloExternoMniManager.NAME)
public class ProtocoloExternoMniManager extends BaseManager<ProtocoloExternoMni>{
	
	public static final String NAME = "protocoloExternoMniManager";

	@In
	private ProtocoloExternoMniDAO protocoloExternoMniDAO;
	
	@Override
	protected ProtocoloExternoMniDAO getDAO() {
		return protocoloExternoMniDAO;
	}
	
	public static ProtocoloExternoMniManager getInstance() {
		return ComponentUtil.getComponent(ProtocoloExternoMniManager.class);
	}
	
	public ProtocoloExternoMni buscarPorIdentificadorSistemaExternoEhProcuradoria(String numeroProtocoloSistemaExterno, Procuradoria procuradoria) {
		return protocoloExternoMniDAO.findByNumeroIdentificadorSistemaExternoAndProcuradoria(numeroProtocoloSistemaExterno, procuradoria);
	}

	public ProtocoloExternoMni buscarPorIdentificadorSistemaExternoEhUsuario(String identificadorSitemaExterno, Usuario usuario) { 
	   return protocoloExternoMniDAO.findByNumeroIdentificadorSistemaExternoAndUsuario(identificadorSitemaExterno, usuario); 
	} 
}
