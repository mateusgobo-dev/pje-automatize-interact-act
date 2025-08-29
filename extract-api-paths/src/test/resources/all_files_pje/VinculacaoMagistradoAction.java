package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

@Name("vinculacaoMagistradoAction")
@Scope(ScopeType.EVENT)
public class VinculacaoMagistradoAction implements Serializable {

	private static final long serialVersionUID = -4583045081929557522L;
	
	private ProcessoTrf processoJudicial;
	private UsuarioLocalizacaoMagistradoServidor magistrado;
	private List<UsuarioLocalizacaoMagistradoServidor> magistrados;
	
	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
	
	@In
	private ProcessoTrfUsuarioLocalizacaoMagistradoServidorManager processoTrfUsuarioLocalizacaoMagistradoServidorManager;
	
	@In
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private ProcessoMagistradoManager processoMagistradoManager;

	@Create
	public void init() {
		if (tramitacaoProcessualService != null) {
			processoJudicial = tramitacaoProcessualService.recuperaProcesso();
			magistrados = recuperarMagistrados();
		}
	}
	
	public void vincular() {
		try {
			if (magistrado != null) {
				processoTrfUsuarioLocalizacaoMagistradoServidorManager.vincularMagistradoProcesso(processoJudicial, magistrado);
				magistrados = recuperarMagistrados();
			}
		} catch(Exception e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private List<UsuarioLocalizacaoMagistradoServidor> recuperarMagistrados() {
		return usuarioLocalizacaoMagistradoServidorManager.getMagistrados(
				processoJudicial.getOrgaoJulgador(), processoMagistradoManager.obterRelator(processoJudicial));
	}
	
	public ProcessoTrf getProcessoJudicial() {
		return processoJudicial;
	}

	public UsuarioLocalizacaoMagistradoServidor getMagistrado() {
		return magistrado;
	}

	public void setMagistrado(UsuarioLocalizacaoMagistradoServidor magistrado) {
		this.magistrado = magistrado;
	}

	public List<UsuarioLocalizacaoMagistradoServidor> getMagistrados() {
		return magistrados;
	}
	
}
