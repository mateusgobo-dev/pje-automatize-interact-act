package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.DiaSemana;

/**
 * Componente de acesso a dados da entidade {@link DiaSemana}.
 * 
 * @author thiago.vieira
 *
 */
@Name("diaSemanaDAO")
public class DiaSemanaDAO extends BaseDAO<DiaSemana> {

	@Override
	public Integer getId(DiaSemana dia) {
		return dia.getIdDiaSemana();
	}

}