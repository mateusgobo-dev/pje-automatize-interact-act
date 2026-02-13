package br.jus.csjt.pje.business.manager;

import java.util.Date;

import org.jboss.seam.annotations.Name;

import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.csjt.pje.persistence.dao.PeriodoDeInatividadeDaSalaList;
import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;
import br.jus.pje.jt.util.JTDateUtil;
import br.jus.pje.nucleo.entidades.Sala;

/**
 * Classe de regras de negocio para uma instancia de Periodo de inatividade dda
 * sala
 * 
 * @category PJE-JT
 * @since versao 1.2.0
 * @author Rafael Carvalho
 */
@Name("periodoDeInatividadeDaSalaManager")
public class PeriodoDeInatividadeDaSalaManager {

	/**
	 * Verifica se o periodo de inicio esta dentro de um periodo de inabilitacao
	 * ja existente.
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param inicio
	 * @param sala
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 09/08/2011
	 */
	private void validarPeriodoDeInativacao(Date inicio, Date termino, Sala sala) {
		boolean inativaInicio = sala.verificaSeASalaEstaInativa(inicio, true, false);
		boolean inativaTermino = false;
		if (termino != null) {
			inativaTermino = sala.verificaSeASalaEstaInativa(termino, false, true);
		}
		if (inativaInicio || inativaTermino) {
			throw new BusinessException("periodoDeInatividadeDaSala.error.jaExiste");
		}
	}

	/**
	 * Verifica se a data e anterior a hoje
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param inicio
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 09/08/2011
	 */
	private void validarInicioDoPeriodoDeInativacao(Date inicio) {
		Date hoje = JTDateUtil.getDate();
		if (JTDateUtil.before(inicio, hoje)) {
			throw new BusinessException("periodoDeInatividadeDaSala.error.dataInicioMenorHoje");
		}
	}

	/**
	 * Faz validacao de um periode de inatividade para uma instancia da Sala
	 * 
	 * @author Rafael Carvalho <rafael.carvalho@tst.jus.br>
	 * 
	 * @param instance
	 * @return
	 * @category PJE-JT
	 * @since 1.2.0
	 * @created 09/08/2011
	 */
	public boolean validarPeriodo(PeriodoDeInatividadeDaSala instance) {
		try {
			if (instance.getCanEditInicio()) {
				validarInicioDoPeriodoDeInativacao(instance.getInicio());
			}
			validarPeriodoDeInativacao(instance.getInicio(), instance.getTermino(), instance.getSala());
			return true;
		} catch (BusinessException e) {
			new PeriodoDeInatividadeDaSalaList().refresh();
			return false;
		}
	}

}
