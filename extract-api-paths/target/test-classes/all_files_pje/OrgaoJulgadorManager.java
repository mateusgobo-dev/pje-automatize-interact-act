package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.OrgaoJulgadorDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(OrgaoJulgadorManager.NAME)
public class OrgaoJulgadorManager extends BaseManager<OrgaoJulgador>{
	
	public static final String NAME = "orgaoJulgadorManager";

	@In(create=true)
	private OrgaoJulgadorDAO orgaoJulgadorDAO;

	@Override
	protected OrgaoJulgadorDAO getDAO(){
		if (orgaoJulgadorDAO == null) {
			orgaoJulgadorDAO = new OrgaoJulgadorDAO();
		}
		return orgaoJulgadorDAO;
	}

	public List<OrgaoJulgador> findAll() {
		return orgaoJulgadorDAO.findAll();
	}
	
	/**
	 * Busca o primeiro OJ ativo dada uma localização de forma recursiva
	 * @param localizacao
	 * @return
	 */
	public OrgaoJulgador getOrgaoJulgadorByLocalizacao(Localizacao localizacao){
		return getDAO().getOrgaoJulgadorByLocalizacao(localizacao);
	}

	/**
	 * Busca o primeiro OJ ativo dada uma localização exata
	 * @param localizacao
	 * @return
	 */
	public OrgaoJulgador getOrgaoJulgadorByLocalizacaoExata(Localizacao localizacao){
		return getDAO().getOrgaoJulgadorByLocalizacaoExata(localizacao);
	}

	public List<OrgaoJulgador> findAllbyLocalizacao(Localizacao localizacao){
		return getDAO().obterAtivosPorLocalizacao(localizacao);
	}
	
	public List<OrgaoJulgador> findAllbyJurisdicao(Jurisdicao jurisdicao) {
	    return findAllbyJurisdicao(jurisdicao, false);
	}

    public List<OrgaoJulgador> findAllbyJurisdicao(Jurisdicao jurisdicao, boolean ignorarCompetencia) {
	    return ignorarCompetencia ? getDAO().obterOrgaosJulgadoresIndependenteDeCompetencia(jurisdicao) : getDAO().obterAtivosPorJurisdicao(jurisdicao);
    }

	public List<OrgaoJulgador> findAllbyCompetencia(Competencia competencia) {
		return getDAO().obterAtivosPorCompetencia(competencia);
	}
	
	public List<OrgaoJulgador> findAllbyJurisdicaoCompetencia(Jurisdicao jurisdicao, Competencia competencia) {
		return getDAO().obterAtivosPorJurisdicaoCompetencia(jurisdicao, competencia);
	}

	public List<OrgaoJulgador> findAllbyClasseAssunto(ClasseJudicial classeJudicial, List<AssuntoTrf> assuntoTrfList) {
		return getDAO().obterAtivosClasseAssunto(classeJudicial, assuntoTrfList);
	}

	/**
	 * Traz o Diretor de Secretaria da Vara (OrgaoJulgador)
	 * 
	 * @param orgaoJulgador
	 * @return retorna um Usuario do sistema
	 */
	public Usuario getDiretorVara(OrgaoJulgador orgaoJulgador){
		return getDAO().getDiretorVaraByOrgaoJulgador(orgaoJulgador);
	}

	public Usuario getJuizFederal(OrgaoJulgador orgaoJulgador){
		return getDAO().getJuizFederalByOrgaoJulgador(orgaoJulgador);
	}

	public List<Usuario> buscaJuizesOJ(OrgaoJulgador orgaoJulgador){
		return getDAO().listJuizByOrgaoJulgador(orgaoJulgador);
	}

	public List<OrgaoJulgador> buscaPostosDoOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		return getDAO().buscaPostosDoOrgaoJulgador(orgaoJulgador);
	}
	
	public Integer numeroDePostosDoOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		return getDAO().buscaPostosDoOrgaoJulgador(orgaoJulgador).size();
	}
	
	/**
	 * Verifica se o código de origem já está sendo utilizado por algum outro
	 * órgão julgador diferente do passado como parâmetro
	 * 
	 * @param orgaoJulgador
	 * @return
	 */
	public boolean codigoOrigemJaUtilizadoParaJurisdicao(OrgaoJulgador orgaoJulgador) {
		return getDAO().codigoOrigemJaUtilizadoParaJurisdicao(orgaoJulgador);
	}
	
	/**
	 * Recupera a lista de órgãos julgadores ativos pertencentes a um dado órgão julgador colegiado.
	 * 
	 * @param orgao o órgão julgador colegiado a que devem pertencer os órgãos
	 * @return a lista de órgãos
	 */
	public List<OrgaoJulgador> orgaosPorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		return getDAO().getOrgaoJulgadorListByOjc(orgaoJulgadorColegiado);
	}
	
	/**
	 * Recupera a pessoa (magistrado) atualmente responsável pelo órgão julgador dado, ou seja,
	 * aquele cujo papel no órgão foi definido como papel titular.
	 * 
	 * @param orgao o órgão julgador cujo responsável principal se pretende verificar
	 * @param colegiado o órgão colegiado ao qual está vinculado o órgão julgador, se for o caso.
	 * @return o responsável.
	 */
	public PessoaMagistrado recuperaResponsavel(OrgaoJulgador orgao, OrgaoJulgadorColegiado colegiado){
		return recuperaResponsavel(orgao, colegiado, new Date());
	}
	
	/**
	 * Recupera a pessoa (magistrado) responsável pelo órgão julgador na data informada, ou seja,
	 * aquele cujo papel no órgão foi definido como papel titular.
	 * 
	 * @param orgao o órgão julgador cujo responsável principal se pretende verificar
	 * @param colegiado o órgão colegiado ao qual está vinculado o órgão julgador, se for o caso.
	 * @param data a data de referência
	 * @return o responsável.
	 */
	public PessoaMagistrado recuperaResponsavel(OrgaoJulgador orgao, OrgaoJulgadorColegiado colegiado, Date data){
		Entry<Integer, String> responsavel = orgaoJulgadorDAO.obterResponsavel(orgao, colegiado, data);
		if(responsavel==null){
			return null;
		}else{
			Search sp = new Search(PessoaMagistrado.class);
			addCriteria(sp, Criteria.equals("idUsuario", responsavel.getKey()));
			List<PessoaMagistrado> ret1 = list(sp);
			return ret1.get(0);
		}
	}
		
	/**
	 * Recupera o OrgaoJulgadorCargo do responsável pelo OJ.
	 * 
	 * @see OrgaoJulgadorDAO#obterIdOjCargoResponsavel(OrgaoJulgador, OrgaoJulgadorColegiado, Date)
	 * 
	 * @param orgao o órgão julgador cujo responsável principal se pretende verificar
	 * @param colegiado o órgão colegiado ao qual está vinculado o órgão julgador, se for o caso.
	 * @param data a data de referência
	 * @return o cargo {@link OrgaoJulgadorCargo} do responsável pelo OJ.
	 */
	public OrgaoJulgadorCargo recuperarCargoResponsavel(OrgaoJulgador orgao, OrgaoJulgadorColegiado colegiado, Date data){
		Integer idOjCargoResponsavel = orgaoJulgadorDAO.obterIdOjCargoResponsavel(orgao, colegiado, data);
		OrgaoJulgadorCargo responsavel = null;
		if (idOjCargoResponsavel != null) {
			Search sp = new Search(OrgaoJulgadorCargo.class);
			addCriteria(sp, Criteria.equals("idOrgaoJulgadorCargo", idOjCargoResponsavel));
			List<OrgaoJulgadorCargo> ret1 = list(sp);
			responsavel =ret1.get(0);
		}
		return responsavel;
	}
	
	public List<OrgaoJulgador> recuperarOutrosOrgaosJulgadoresDoColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado, OrgaoJulgador orgaoJulgadorExcluido) {
		List<OrgaoJulgador> orgaosDoColegiado = orgaosPorColegiado(orgaoJulgadorColegiado);
		orgaosDoColegiado.remove(orgaoJulgadorExcluido);
		return orgaosDoColegiado;		
	}

	/**
	 * Retorna os orgaos julgadores de acordo com o colegiado.
	 * Caso seja nulo, retorna todos os orgaos julgaores ativos.
	 * 
	 * @param orgaoJulgadorColegiado
	 * @return List<OrgaoJulgador>
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresPorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		return getDAO().getOrgaoJulgadorListByOjc(orgaoJulgadorColegiado);
	}

	/**
	 * Retorna os órgãos julgadores de acordo com o colegiado e a localização física dada
	 * @param orgaoJulgadorColegiado
	 * @param localizacao
	 * @return
	 */
	public List<OrgaoJulgador> obterOrgaosJulgadoresPorColegiadoEhPorLocalizacao(OrgaoJulgadorColegiado orgaoJulgadorColegiado, Localizacao localizacao) {
		return getDAO().obterAtivos(null, null, orgaoJulgadorColegiado, localizacao, true);
	}
	
	/**
	 * Método responsável por obter todos os órgãos julgadores de 2ª ou de 3ª instância que estejam ativos.
	 * 
	 * @return lista de órgãos julgadores de 2ª e 3ª instância ativos.
	 */
	public List<OrgaoJulgador> obterItensAtivosSegundaOuTerceiraInstancia(){
		return getDAO().obterAtivosSegundoOuTerceiroGraus();
	}
	
	/**
	 * Método responsável por obter os órgãos julgadores  de um Órgão julgador colegiado.
	 * 
	 * @return lista de órgãos julgadores.
	 */
	public List<OrgaoJulgador> getOrgaoJulgadorListByOjc(OrgaoJulgadorColegiado orgaoJulgadorColegiado){
		return getDAO().getOrgaoJulgadorListByOjc(orgaoJulgadorColegiado);
	}
	
	/**
	 * 
	 * @param dataSessao
	 * @param horario
	 * @return
	 */
	public List<OrgaoJulgador> obterAtivosComCompetencia(){
		return getDAO().obterAtivosComCompetencia();
	}
	
	/**
	 * Método responsável por obter os órgãos julgadores de 1ª instância ativos de acordo com a jurisdição informada.
	 * @param idJurisdicao Identificador da jurisdição.
	 * 
	 * @return Os órgãos julgadores de 1ª instância ativos de acordo com a jurisdição informada.
	 */
	public List<OrgaoJulgador> obterItensAtivosPrimeiraInstancia(Integer idJurisdicao){
  		JurisdicaoManager jurisdicaoManager = ComponentUtil.getComponent("jurisdicaoManager");
		Jurisdicao jurisdicao = null;
		try {
			jurisdicao = jurisdicaoManager.findById(idJurisdicao);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return getDAO().obterAtivosPrimeiroGrau(jurisdicao);
	}
	
	/** Retorna a descrição do órgão julgador para onde o processo foi distribuído pela última vez.
	 * 
	 * @param processoTrf
	 * @return DescricaoOrgaoJulgadorUltimaDistribuicao
	 */
	public String obterDescricaoOrgaoJulgadorUltimaDistribuicao(ProcessoTrf processoTrf) {
		return getDAO().obterDescricaoOrgaoJulgadorUltimaDistribuicao(processoTrf);
	}
	
	/**
	 * Método responsável por recuperar uma lista de {@link OrgaoJulgador} ativos.
	 * 
	 * @param jurisdicao {@link Jurisdicao}.
	 * @param orgaoJulgadorColegiado {@link OrgaoJulgadorColegiado}.
	 * @param competencia {@link Competencia}.
	 * @return Lista de {@link OrgaoJulgador}.
	 */
	public List<OrgaoJulgador> obterAtivos(Jurisdicao jurisdicao, OrgaoJulgadorColegiado orgaoJulgadorColegiado, Competencia competencia) {
		return getDAO().obterAtivos(jurisdicao, competencia, orgaoJulgadorColegiado);
	}

	public List<OrgaoJulgador> recuperarSemTitular(List<Integer> idsOrgaoJulgador) {
		return this.getDAO().recuperarSemTitular(idsOrgaoJulgador);
	}
	
	public Boolean existeOrgaoJulgadorPorNomeEInstancia(String nome,String instancia, Integer id) {
	    return this.getDAO().existeOrgaoJulgadorPorNomeEInstancia(nome, instancia,id);
	}
	public List<OrgaoJulgador> findByIds(List<Integer> idsOrgaoJulgador) {
		return this.getDAO().findByIds(idsOrgaoJulgador);
	}

}
