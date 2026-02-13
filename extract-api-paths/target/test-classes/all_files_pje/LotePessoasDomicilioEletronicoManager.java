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

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.LotePessoasDomicilioEletronicoDAO;
import br.jus.pje.nucleo.entidades.LotePessoasDomicilioEletronico;

@Name(LotePessoasDomicilioEletronicoManager.NAME)
public class LotePessoasDomicilioEletronicoManager extends BaseManager<LotePessoasDomicilioEletronico> {

	private static final long serialVersionUID = -2645946485363799022L;

	public static final String NAME = "lotePessoasDomicilioEletronicoManager";

	@In(create = true)
	private LotePessoasDomicilioEletronicoDAO lotePessoasDomicilioEletronicoDAO;

	@Override
	protected LotePessoasDomicilioEletronicoDAO getDAO() {
		return lotePessoasDomicilioEletronicoDAO;
	}

	public List<String> findAllNomesLotesProcessados() {
		return getDAO().findAllNomesLotesProcessados();
	}
	
	public LotePessoasDomicilioEletronico findLote(String nomeArquivo) {
		return getDAO().findLote(nomeArquivo);
	}

	public boolean isLoteJaProcessado(String nomeArquivo) {
		return getDAO().isLoteJaProcessado(nomeArquivo);
	}

}
