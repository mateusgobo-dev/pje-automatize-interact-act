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

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.AgrupamentoPessoas;

/**
 * Componente de acesso a dados da entidade {@link AgrupamentoPessoas}.
 * 
 * @author thiago.vieira
 *
 */
@Name("agrupamentoPessoasDAO")
public class AgrupamentoPessoasDAO  extends BaseDAO<AgrupamentoPessoas>{

	@Override
	public Object getId(AgrupamentoPessoas e) {
		return e.getIdAgrupamento();
	}
}
