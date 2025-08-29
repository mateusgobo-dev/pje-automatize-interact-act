package br.jus.cnj.pje.nucleo.service;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;

import br.jus.cnj.pje.business.dao.JurisdicaoDAO;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

/**
 * [PJEII-663] Camada Service para Jurisdicao
 * @author Haroldo Arouca
 * @author Tiago Zanon
 */

@Name("jurisdicaoService")
@Transactional
public class JurisdicaoService {

	@In(create = true)
	private JurisdicaoDAO jurisdicaoDAO;

	public List<Jurisdicao> findAll() {
		if (jurisdicaoDAO == null) {
			jurisdicaoDAO = new JurisdicaoDAO();
		}
		return jurisdicaoDAO.findAll();
	}
	public Jurisdicao getJurisdicaoByOrgaoJulgador(OrgaoJulgador orgaoJulgador){
		if (jurisdicaoDAO == null) {
			jurisdicaoDAO = new JurisdicaoDAO();
		}
		return jurisdicaoDAO.getJurisdicaoByOrgaoJulgador(orgaoJulgador);
	}
	
}
