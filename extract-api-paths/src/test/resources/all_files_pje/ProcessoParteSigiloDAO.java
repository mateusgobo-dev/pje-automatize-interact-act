/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de acesso a dados da entidade {@link ProcessoParteSigilo}.
 * 
 * @author cristof
 *
 */
@Name("processoParteSigiloDAO")
public class ProcessoParteSigiloDAO extends BaseDAO<ProcessoParteSigilo> {
	
	@Override
	public Integer getId(ProcessoParteSigilo e) {
		return e.getIdProcessoParteSigilo();
	}

	/**
	 * metodo responsavel por recuperar todos os @ProcessoParteSigilo da pessoa passada em parametro
	 * @param _pessoa
	 * @return
	 */
	public List<ProcessoParteSigilo> recuperaProcessoParteSigilo(Pessoa _pessoa) {
		List<ProcessoParteSigilo> resultado = null;
		Search search = new Search(ProcessoParteSigilo.class);
		try {
			search.addCriteria(Criteria.equals("usuarioCadastro.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}

}
