package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.OficialJusticaCentralMandadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.CentralMandado;
import br.jus.pje.nucleo.entidades.OficialJusticaCentralMandado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;

@Name(OficialJusticaCentralMandadoManager.NAME)
public class OficialJusticaCentralMandadoManager extends BaseManager<OficialJusticaCentralMandado> {

	public static final String NAME = "oficialJusticaCentralMandadoManager";
	
	@In
	private OficialJusticaCentralMandadoDAO oficialJusticaCentralMandadoDAO;
	
	@In
	private UsuarioLocalizacaoManager usuarioLocalizacaoManager;
	
	/**
	 * Verifica se um cadastro específico já existe.
	 * @param central
	 * @param ul
	 * @return
	 */
	public boolean isExisteCadastro(CentralMandado central, List<UsuarioLocalizacao> ul) {
		int id = getDAO().getOficialJusticaCentralMandado(central, ul).getIdOficialJusticaCentralMandado();
		return id != 0 ? true : false;
	}
	
	/**
	 * Verifica se há cadastro com a localizacao/papel especificado.
	 * @param central
	 * @param ul
	 * @return
	 */
	public boolean isExisteCadastro(UsuarioLocalizacao ul) {
		return getDAO().getOficialJusticaCentralMandadoList(ul).isEmpty() ? false : true;
	}
	
	@Override
	protected OficialJusticaCentralMandadoDAO getDAO() {
		return this.oficialJusticaCentralMandadoDAO;
	}
	
	public void remove(UsuarioLocalizacao usuarioLocalizacao) throws PJeBusinessException {
		List<OficialJusticaCentralMandado> oficialJusticaCentralMandadoList = 
				oficialJusticaCentralMandadoDAO.getOficialJusticaCentralMandadoList(usuarioLocalizacao);
		
		for (OficialJusticaCentralMandado oficialJusticaCentralMandado : oficialJusticaCentralMandadoList) {
			remove(oficialJusticaCentralMandado);
		}
	}
	
	@Override
	public void remove(OficialJusticaCentralMandado ojcm) throws PJeBusinessException {
		this.getDAO().remove(ojcm);

		UsuarioLocalizacao usuarioLocalizacao = ojcm.getUsuarioLocalizacao();
		if (this.oficialJusticaCentralMandadoDAO.getOficialJusticaCentralMandadoList(usuarioLocalizacao).size() == 1) {
			usuarioLocalizacaoManager.remove(usuarioLocalizacao);
		}
		
		this.flush();
	}
	
}
