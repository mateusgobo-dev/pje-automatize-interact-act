package br.jus.cnj.pje.nucleo.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.com.jt.pje.dao.SalaDAO;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

/**
 * [PJEII-663] Camada Service para Sala
 * @author Haroldo Arouca
 * @author Tiago Zanon
 */
@Name("salaService")
@Transactional
public class SalaService {

	@In(create = true)
	private SalaDAO salaDAO;

	public List<Sala> findAll() {
		if (salaDAO == null) {
			salaDAO = new SalaDAO();
		}
		return salaDAO.findAll();
	}
	
	public List<Sala> getSalaListByLocalizacaoAndTipoAudiencia(Localizacao localizacao, TipoAudiencia tipoAudiencia) {
		if (salaDAO == null) {
			salaDAO = new SalaDAO();
		}
		return salaDAO.getSalaListByLocalizacaoAndTipoAudiencia(localizacao, tipoAudiencia);
	}
	
	public List<Sala> getSalaListByOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		if (salaDAO == null) {
			salaDAO = new SalaDAO();
		}
		return salaDAO.getSalaListByOrgaoJulgador(orgaoJulgador);
	}
	
	public List<Sala> getSalaListByOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		if (salaDAO == null) {
			salaDAO = new SalaDAO();
		}
		return salaDAO.getSalaListByOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
	}
	
}
