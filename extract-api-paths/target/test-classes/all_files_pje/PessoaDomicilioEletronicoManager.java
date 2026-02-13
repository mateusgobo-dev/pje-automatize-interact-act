/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.PessoaDomicilioEletronicoDAO;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.pje.nucleo.entidades.PessoaDomicilioEletronico;

@Name(PessoaDomicilioEletronicoManager.NAME)
public class PessoaDomicilioEletronicoManager extends BaseManager<PessoaDomicilioEletronico> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "pessoaDomicilioEletronicoManager";

	@In
	private PessoaDomicilioEletronicoDAO pessoaDomicilioEletronicoDAO;

	@Override
	protected PessoaDomicilioEletronicoDAO getDAO() {
		return pessoaDomicilioEletronicoDAO;
	}
	
	/**
     * @return Instância da classe.
     */
    public static PessoaDomicilioEletronicoManager instance() {
        return ComponentUtil.getComponent(PessoaDomicilioEletronicoManager.class);
    }

	/**
	 * ATENÇÃO: ESTE MÉTODO NÃO DEVE SER UTILIZADO DIRETAMENTE! Utilizar
	 * {@link DomicilioEletronicoService#getPessoa(String)}, já que o
	 * método do Service leva em consideração se o cache local está ativo ou não.
	 * 
	 * Recupera do banco de dados uma pessoa por número de documento.
	 * 
	 * @param numeroDocumento número do documento da pessoa.
	 * @return entidade que representa a pessoa com esse documento no banco de
	 *         dados.
	 */
	public PessoaDomicilioEletronico findByNumeroDocumento(String numeroDocumento) {
		return getDAO().findByNumeroDocumento(InscricaoMFUtil.acrescentaMascaraMF(numeroDocumento));
	}

}
