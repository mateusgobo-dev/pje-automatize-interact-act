package br.jus.csjt.pje.commons.util;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import br.jus.cnj.pje.servicos.PrazosProcessuaisServiceImpl;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.pje.jt.entidades.OrgaoJulgadorJt;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

/**
 * Classe que engloba alguns métodos utilizados por OrgaoJulgadorJt.
 * 
 * @author Rodrigo Cartaxo / Haroldo Arouca
 * @since versão 1.2.0
 * @see [PJE-328]
 * @category PJE-JT
 */
public class OrgaoJulgadorJtUtil {

	/**
	 * Retorna as informações do orgão julgador específicad da JT.
	 * 
	 * @author Rodrigo Cartaxo / Haroldo Arouca
	 * @since versão 1.2.0
	 * @see [PJE-328]
	 * @category PJE-JT
	 * @param entityManager
	 * @param idOrgaoJulgador
	 * @return
	 */
	public static OrgaoJulgadorJt getOrgaoJulgadorJt(EntityManager entityManager, Object idOrgaoJulgador) {

		OrgaoJulgadorJt orgaoJulgadorJt = null;

		try {
			orgaoJulgadorJt = ((OrgaoJulgadorJt) entityManager.createQuery(
					"" + "FROM " + OrgaoJulgadorJt.class.getSimpleName() + " WHERE orgaoJulgador = " + idOrgaoJulgador)
					.getSingleResult());

		} catch (NoResultException e) {
			System.out.println("OrgaoJulgador JT não localizado para o OrgaoJulgador " + idOrgaoJulgador);
		}

		return orgaoJulgadorJt;
	}

	/**
	 * Retorna o interstício em dias corridos, levando em consideração feriados
	 * e finais de semana.
	 * 
	 * @author Rodrigo Cartaxo / Haroldo Arouca
	 * @since versão 1.2.0
	 * @see [PJE-328]
	 * @category PJE-JT
	 * @param intersticio
	 * @param id
	 * @return
	 */
	public static Integer getIntersticioDiasCorridos(Integer intersticio, OrgaoJulgador orgaoJulgador) {

		PrazosProcessuaisServiceImpl prazosProcessuaisService = new PrazosProcessuaisServiceImpl();
		Date hoje = new Date();
		Date dataFinal = new Date();

		Calendario calendario = prazosProcessuaisService.obtemCalendario(orgaoJulgador);

		for (int i = 0; i < intersticio; i++) {
			dataFinal = prazosProcessuaisService.obtemDiaUtilSeguinte(dataFinal, calendario, false);
		}

		long diferenca = dataFinal.getTime() - hoje.getTime();
		Integer diasCorridos = (int) (diferenca / (24 * 60 * 60 * 1000)) + 1;

		return diasCorridos;
	}

}
