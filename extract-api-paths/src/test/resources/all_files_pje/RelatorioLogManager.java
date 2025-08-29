package br.com.infox.pje.manager;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.manager.GenericManager;
import br.jus.pje.nucleo.entidades.RelatorioLog;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe manager para RelatorioLog
 * 
 * @author Allan
 * 
 */
@Name(RelatorioLogManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class RelatorioLogManager extends GenericManager {

	public static final String NAME = "relatorioLogManager";

	public void persist(String descricao, Usuario usuario) {
		RelatorioLog relatorioLog = new RelatorioLog();
		relatorioLog.setDataSolicitacao(new Date());
		relatorioLog.setDescricao(descricao);
		if (usuario == null) {
			usuario = ParametroUtil.instance().getUsuarioSistema();
		}
		relatorioLog.setIdUsuarioSolicitacao(usuario);
		persist(relatorioLog);
	}

}