package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.OrgaoJulgadorColegiadoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.OrgaoJulgadorColegiadoDTO;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(OrgaoJulgadorColegiadoManager.NAME)
public class OrgaoJulgadorColegiadoManager extends BaseManager<OrgaoJulgadorColegiado>{

	public static final String NAME = "orgaoJulgadorColegiadoManager";

	@In
	private OrgaoJulgadorColegiadoDAO orgaoJulgadorColegiadoDAO;
	
	@Override
	protected OrgaoJulgadorColegiadoDAO getDAO() {
		return orgaoJulgadorColegiadoDAO;
	}
	
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems(){
		return orgaoJulgadorColegiadoDAO.getOrgaoJulgadorColegiadoItems();
	}
	
	/**
	 * Recupera a lista de órgãos colegiados que têm entre seus gabinetes o órgão julgador dado
	 * e que tenha a competência informada no momento da chamaeda.
	 * 
	 * @param competencia a competência que restringirá a busca
	 * @param orgaoJulgador o gabinete que deve estar vinculado ao colegiado
	 * @return a lista de colegiados a que pertente o gabinete e que têm a competência informada.
	 */
	public List<OrgaoJulgadorColegiado> getColegiadosCompetentes(Competencia competencia, OrgaoJulgador orgaoJulgador) throws PJeBusinessException{
		return orgaoJulgadorColegiadoDAO.getColegiadosCompetentes(competencia, orgaoJulgador);
	}
	
	public Long numeroOrgaosJulgadores(OrgaoJulgadorColegiado ojc){
		return numeroOrgaosJulgadores(ojc, new Date());
	}
	
	public Long numeroOrgaosJulgadores(OrgaoJulgadorColegiado ojc, Date dataReferencia){
		Search s = new Search(OrgaoJulgadorColegiadoOrgaoJulgador.class);
		addCriteria(s, 
				Criteria.equals("orgaoJulgadorColegiado", ojc),
				Criteria.or(
						Criteria.isNull("dataInicial"),
						Criteria.lessOrEquals("dataInicial", dataReferencia)),
				Criteria.or(Criteria.isNull("dataFinal"),
						Criteria.greaterOrEquals("dataFinal", dataReferencia)),
				Criteria.equals("orgaoJulgador.ativo", true));
		return count(s);
	}

	/**
	 * Recupera uma lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 * 
	 * @param jurisdicao A jurisdição que restringirá a busca.
	 * @return Lista de {@link OrgaoJulgadorColegiado} ativos, de competência ativa e vinculados à uma Jurisdicao.
	 */
	public List<OrgaoJulgadorColegiado> getColegiadosByJurisdicao(Jurisdicao jurisdicao){
		return orgaoJulgadorColegiadoDAO.getColegiadosByJurisdicao(jurisdicao);
	}

	public List<OrgaoJulgadorColegiado> getColegiadosByCompetencia(Competencia competencia){
		return orgaoJulgadorColegiadoDAO.getColegiadosByCompetencia(competencia);
	}

	public List<OrgaoJulgadorColegiado> obterAtivos(Jurisdicao jurisdicao, Competencia competencia){
		return orgaoJulgadorColegiadoDAO.getColegiadosAtivos(jurisdicao, competencia);
	}

	public List<OrgaoJulgadorColegiado> getColegiadosByClasseAssunto(ClasseJudicial classeJudicial, List<AssuntoTrf> assuntoTrfList) {
		return orgaoJulgadorColegiadoDAO.getColegiadosByClasseAssunto(classeJudicial, assuntoTrfList);
	}

	/**
	 * Retorna os Orgaos Julgador Colegiado
	 * Caso o usuario logado nao seja admin e tenha Orgao Julgador Colegiado,
	 * return o seu proprio OrgaosJulgadorColegiado
	 * 
	 * @return List<OrgaoJulgadorColegiado>
	 */
	public List<OrgaoJulgadorColegiado> obterOJColegiadosAtivosPorPerfilLogado() {
		OrgaoJulgadorColegiado ojc = Authenticator.getOrgaoJulgadorColegiadoAtual();

		if( Authenticator.isPapelAdministrador() || ojc == null) {
			return this.orgaoJulgadorColegiadoDAO.getOrgaoJulgadorColegiadoItems();
		}
		
		List<OrgaoJulgadorColegiado> ojcs = new ArrayList<OrgaoJulgadorColegiado>();
		ojcs.add(ojc);
		
		return ojcs;
	}
	
	public List<OrgaoJulgadorColegiadoOrgaoJulgador> obterComposicaoAtiva(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		return getDAO().obterComposicaoAtiva(orgaoJulgadorColegiado);
	}
	
	/**
	 * Retorna os colegiados ativos, pela localizacao
	 * 
	 * @param localizacao
	 * @return
	 */
 	public List<OrgaoJulgadorColegiado> getColegiadosByLocalizacao(Localizacao localizacao){
 		return orgaoJulgadorColegiadoDAO.getColegiadosByLocalizacao(localizacao);
	}
 	
	/**
	 * Retorna os colegiados ativos, pela localizacao exata
	 * 
	 * @param localizacao
	 * @return
	 */
 	public List<OrgaoJulgadorColegiado> getColegiadosByLocalizacaoExata(Localizacao localizacao){
 		return orgaoJulgadorColegiadoDAO.getColegiadosByLocalizacaoExata(localizacao);
	}
 	
 	public List<OrgaoJulgadorColegiadoDTO> findAllDTO(){
 		return orgaoJulgadorColegiadoDAO.findAllDTO();
 	}
}