package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.QuadroAvisoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.QuadroAviso;

@Name(QuadroAvisoManager.NAME)
public class QuadroAvisoManager extends BaseManager<QuadroAviso>{
	
	public static final String NAME = "quadroAvisoManager";
	
	@In
	private QuadroAvisoDAO quadroAvisoDAO;

	@Override
	protected QuadroAvisoDAO getDAO() {
		return quadroAvisoDAO;
	}

	/**
	 * metodo responsavel por recuperar todos os avisos postados no quadro de aviso, 
	 * pela pessoa passada em parametro
	 * @param pessoa
	 * @return
	 * @throws Exception 
	 */
	public List<QuadroAviso> recuperarAvisosQuadroAviso(Pessoa pessoa) throws Exception {
		return quadroAvisoDAO.recuperarAvisosQuadroAviso(pessoa);
	}

	public QuadroAviso recuperarPorId(Integer idQuadroAviso) {
		return quadroAvisoDAO.find(idQuadroAviso);
	}

}