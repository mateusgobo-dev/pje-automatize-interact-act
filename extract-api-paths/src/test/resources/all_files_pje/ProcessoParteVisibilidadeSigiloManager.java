package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ProcessoParteVisibilidadeSigiloDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteVisibilidadeSigilo;

@Name("processoParteVisibilidadeSigiloManager")
public class ProcessoParteVisibilidadeSigiloManager extends BaseManager<ProcessoParteVisibilidadeSigilo> {

	@In
	private ProcessoParteVisibilidadeSigiloDAO processoParteVisibilidadeSigiloDAO;
	
	@Override
	protected ProcessoParteVisibilidadeSigiloDAO getDAO() {
		return processoParteVisibilidadeSigiloDAO;
	}
	
	public boolean visivel(ProcessoParte parte, Pessoa pessoa){
		return processoParteVisibilidadeSigiloDAO.visivel(parte, pessoa);
	}
	
	public List<ProcessoParteVisibilidadeSigilo> recuperarVisualizadores(ProcessoParte parte) {
		return processoParteVisibilidadeSigiloDAO.recuperarVisualizadores(parte);
	}
	
	public boolean acrescentarVisualizador(ProcessoParte parte, Pessoa pessoa) throws PJeBusinessException {
		if (!parte.getParteSigilosa()) {
			return false;
		}
		if (!visivel(parte, pessoa)) {
			persistAndFlush(criarVisualizador(parte, pessoa));
			return true;
		}
		return false;
	}
	
	public void removerVisualizador(ProcessoParteVisibilidadeSigilo visualizador) throws PJeBusinessException {
		remove(visualizador);
		flush();
	}

	private ProcessoParteVisibilidadeSigilo criarVisualizador(ProcessoParte parte, Pessoa pessoa) {
		ProcessoParteVisibilidadeSigilo visibilidade = new ProcessoParteVisibilidadeSigilo();
		visibilidade.setPessoa(pessoa);
		visibilidade.setProcessoParte(parte);
		return visibilidade;
	}

}
