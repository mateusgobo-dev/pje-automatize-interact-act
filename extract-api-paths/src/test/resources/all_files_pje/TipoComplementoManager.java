package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.TipoComplementoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;

/**
 * Componente de tratamento negocial da entidade {@link TipoComplemento}.
 * 
 * @author cristof
 *
 */
@Name(TipoComplementoManager.NAME)
public class TipoComplementoManager extends BaseManager<TipoComplemento> {
	
	public static final String NAME = "tipoComplementoManager";
	
	@In
	private TipoComplementoDAO tipoComplementoDAO;
	
	@Override
	protected TipoComplementoDAO getDAO() {
		return tipoComplementoDAO;
	}

	/**
	 * Recupera o tipo de complemento por seu código identificador.
	 * 
	 * @param codigo o código identificador do tipo de complemento
	 * @return o tipo de complemento, ou null, se ele inexistir
	 * @throws PJeBusinessException se houver algum erro ao tentar recuperar o tipo de complemento
	 */
	public TipoComplemento findByCodigo(String codigo) throws PJeBusinessException{
		return tipoComplementoDAO.findByCodigo(codigo);
	}
	
	/**
	 * Método responsável por recuperar um tipo de complemento pelo nome.
	 * @param nome Nome do tipo de complemento.
	 * @return {@link TipoComplemento}.
	 */
	public TipoComplemento recuperarTipoComplemento(String nome) {
		return tipoComplementoDAO.recuperarTipoComplemento(nome);
	}

}
