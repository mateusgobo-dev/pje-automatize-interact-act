package br.com.infox.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.component.tree.TarefaTree;
import br.com.infox.ibpm.component.ControlePaginaInicialUsuario;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.FluxoManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.pje.nucleo.dto.VisaoGeralTarefasDTO;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name(VisaoGeralTarefasAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class VisaoGeralTarefasAction {

	public static final String NAME = "visaoGeralTarefasAction";
	
	@In
	private FluxoManager fluxoManager;
	
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	private String nomeTarefaBase64;
	
	private List<VisaoGeralTarefasDTO> listaVisaoGeralTarefas;

	@Factory("listaVisaoGeralTarefas")
	public List<VisaoGeralTarefasDTO> obterVisaoGeralTarefas() {
		return fluxoManager.obterVisaoGeralTarefasUsuario();
	}
	
	public List<VisaoGeralTarefasDTO> getListaVisaoGeralTarefas() {
		if (listaVisaoGeralTarefas == null){
			listaVisaoGeralTarefas = fluxoManager.obterVisaoGeralTarefasUsuario();
		}
		
		return listaVisaoGeralTarefas;
	}

	public void setListaVisaoGeralTarefas(List<VisaoGeralTarefasDTO> listaVisaoGeralTarefas) {
		this.listaVisaoGeralTarefas = listaVisaoGeralTarefas;
	}

	public void trocarPerfilUsuario	(Integer idUsuarioLocalizacaoMagistradoServidor) throws PJeBusinessException{
		UsuarioLocalizacaoMagistradoServidor perfilDestino = usuarioLocalizacaoMagistradoServidorManager.findById(idUsuarioLocalizacaoMagistradoServidor);
		try{
			ControlePaginaInicialUsuario.instance().setTarefaInicial(nomeTarefaBase64);
			Authenticator.instance().setLocalizacaoAtualCombo(perfilDestino);
			TarefaTree tarefaTree = ComponentUtil.getComponent(TarefaTree.NAME);
			tarefaTree.clearTree();
		}finally{
			ControlePaginaInicialUsuario.instance().setTarefaInicial("");
		}
	}

	public String getNomeTarefaBase64() {
		return nomeTarefaBase64;
	}

	public void setNomeTarefaBase64(String nomeTarefaBase64) {
		this.nomeTarefaBase64 = nomeTarefaBase64;
	}

}