package br.com.infox.pje.manager;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.EstatisticaProcessoJusticaFederalDAO;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

/**
 * Classe com métodos referentes a regra de negócio da entidade de
 * EstatisticaProcessoJusticaFederal
 * 
 * @author Luiz Carlos Menezes
 * 
 */
@Name(EstatisticaProcessoJusticaFederalManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class EstatisticaProcessoJusticaFederalManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "estatisticaProcessoJusticaFederalManager";

	@In
	private EstatisticaProcessoJusticaFederalDAO estatisticaProcessoJusticaFederalDAO;

	public List<OrgaoJulgador> buscaListaOrgaoJulgador(String secao) {
		if (secao != null && secao.isEmpty()) {
			return Collections.emptyList();
		}
		return estatisticaProcessoJusticaFederalDAO.listOrgaoJulgadoresSecao(secao);
	}

	public List<Competencia> buscaListaCompetenciaOrgaoJulgador(OrgaoJulgador oj) {
		return estatisticaProcessoJusticaFederalDAO.listCompetenciasOrgaoJulgador(oj);
	}

	public long qtdProcessosVara(String sj, OrgaoJulgador oj, Pessoa p) {
		return estatisticaProcessoJusticaFederalDAO.qtdProcessosVara(sj, oj, p);
	}

	public Processo buscaProcessoSegredoJustica(ProcessoTrf processo, int idUsuario) {
		return estatisticaProcessoJusticaFederalDAO.buscaProcessoSegredoJustica(processo, idUsuario);
	}

	public Processo buscaProcessoTextoSigiloso(ProcessoTrf processo, int idUsuario) {
		return estatisticaProcessoJusticaFederalDAO.buscaProcessoTextoSigiloso(processo, idUsuario);
	}

	public List<Usuario> juizesPorOJ(OrgaoJulgador orgaoJulgador) {
		return estatisticaProcessoJusticaFederalDAO.listJuizesPorOJ(orgaoJulgador);
	}

	public List<UsuarioLogin> buscaUsuariosVisibilidadeSegredo(ProcessoTrf processo) {
		return estatisticaProcessoJusticaFederalDAO.listUsuariosVisibilidadeSegredo(processo);
	}

	public int totalAcordosHomologados() {
		return 1;
	}

	public int valorAcordosHomologados() {
		return 1;
	}
}