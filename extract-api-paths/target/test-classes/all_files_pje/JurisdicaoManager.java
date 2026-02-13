package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.business.dao.JurisdicaoDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto.JurisdicaoDTO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle negocial da entidade {@link Jurisdicao};
 * 
 * @author cristof
 *
 */
@Name(JurisdicaoManager.NAME)
public class JurisdicaoManager extends BaseManager<Jurisdicao> {
	public static final String NAME = "jurisdicaoManager";
	
	private Integer idUsuarioAtual;
	private Integer idLocalizacaoAtual;
	private TipoUsuarioExternoEnum tipoUsuarioExternoAtual;
	private Integer idProcuradoriaAtual;
	private boolean isProcuradorGestor;

	@In
	private JurisdicaoDAO jurisdicaoDAO;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	@Override
	protected JurisdicaoDAO getDAO() {
		return jurisdicaoDAO;
	}
	
	public static JurisdicaoManager instance() {
		return (JurisdicaoManager)Component.getInstance(JurisdicaoManager.NAME);
	}
	
	/**
	 * Retorna a lista de jurisdições ativas cadastradas no sistema.
	 * 
	 * @return
	 */
	public List<Jurisdicao> getJurisdicoesAtivas(){
		Search s = new Search(Jurisdicao.class);
		try {
			s.addCriteria(Criteria.equals("ativo", true));
			s.addOrder("jurisdicao", Order.ASC);
		} catch (Exception e) {
			return null;
		}
		return list(s);
	}

	/**
	 * Retorna Jurisdição a partir do Id.
	 * 
	 * @return
	 */
	public Jurisdicao findByIdJurisdicao(Integer id){
		Search s = new Search(Jurisdicao.class);
		try {
			s.addCriteria(Criteria.equals("idJurisdicao", id));
		} catch (Exception e) {
			return null;
		}
		return (Jurisdicao)list(s).get(0);
	}	

	public List<Jurisdicao> getJurisdicoesAcervoPorPessoaOuProcuradoria(Integer idPessoa, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador){
		return jurisdicaoDAO.getJurisdicoesAcervoPorPessoaOuProcuradoria(idPessoa, idProcuradoria, atuacaoProcurador);
	}
	
	public List<Jurisdicao> getJurisdicoesExpedientesPorPessoaOuProcuradoria(Integer idPessoa, Integer idProcuradoria, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador){
		return jurisdicaoDAO.getJurisdicoesExpedientesPorPessoaOuProcuradoria(idPessoa, idProcuradoria, atuacaoProcurador);
	}

	public List<Jurisdicao> getJurisdicoes(Pessoa representante, Integer idProcuradoria){
		List<Jurisdicao> jurisdicoes = new ArrayList<Jurisdicao>();
		PessoaProcuradoria pessoaProcuradoria = null;
		RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador;

		if(Authenticator.isProcurador()){
			pessoaProcuradoria = pessoaProcuradoriaManager.getPessoaProcuradoria(representante.getIdPessoa(), idProcuradoria);
			atuacaoProcurador = pessoaProcuradoria.getAtuacaoReal();
			jurisdicoes = this.getJurisdicoesExpedientesPorPessoaOuProcuradoria(representante.getIdPessoa(), idProcuradoria, atuacaoProcurador);
		}else{
			jurisdicoes = this.getJurisdicoesExpedientesPorPessoaOuProcuradoria(representante.getIdPessoa(), null, null);
		}
		
		return jurisdicoes;
	}

	public void inicializaDadosUsuario() throws PJeBusinessException{
		this.idUsuarioAtual = Authenticator.getIdUsuarioLogado();
		this.idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		this.tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
		this.idProcuradoriaAtual = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
		this.isProcuradorGestor = Authenticator.isRepresentanteGestor();
	}

	/**
	 * Método responsável por recuperar as jurisdições nais quais o usuário logado possui expedientes - dado um critério de pesquisa 
	 * 
	 * @param criteriosPesquisa
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<JurisdicaoVO> obterJurisdicoesExpedientes(PesquisaExpedientesVO criteriosPesquisa) throws PJeBusinessException {
		this.inicializaDadosUsuario();

		return jurisdicaoDAO.obterJurisdicoesExpedientes(idUsuarioAtual, idLocalizacaoAtual, 
					tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisa);
	}

	/**
	 * Método responsável por recuperar as jurisdições nais quais o usuário logado possui processos - dado um critério de pesquisa
	 * @param criteriosPesquisa
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<JurisdicaoVO> obterJurisdicoesAcervo(ConsultaProcessoVO criteriosPesquisa) throws PJeBusinessException {		
		this.inicializaDadosUsuario();

		return jurisdicaoDAO.obterJurisdicoesAcervo(idUsuarioAtual, idLocalizacaoAtual, 
					tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, criteriosPesquisa);
	}
	
	/**
	 * Retorna a jurisdição pelo número de origem.
	 * 
	 * @param numeroOrigem Número de origem.
	 * @return Jurisdição.
	 */
	public Jurisdicao obterPorNumeroOrigem(String numeroOrigem){
		Jurisdicao resultado = null;
		
		if (numeroOrigem != null) {
			Integer numeroOrigemInteger = Integer.parseInt(numeroOrigem);
			resultado = getDAO().obterPorNumeroOrigem(numeroOrigemInteger);
		}
		return resultado;
	}

	public List<JurisdicaoDTO> recuperarJurisdicoesDTO(){
		return this.getDAO().findAllJurisdicaoDTO();
	}

	public List<Jurisdicao> recuperarJurisdicoes() {
		return this.recuperarJurisdicoes(null, null);
	}

	public List<Jurisdicao> recuperarJurisdicoes(Municipio municipio) {
		return this.recuperarJurisdicoes(null, municipio);
	}

	public List<Jurisdicao> recuperarJurisdicoes(Integer idAreaDireito, Municipio municipio) {
		return this.jurisdicaoDAO.recuperarJurisdicoes(idAreaDireito, municipio != null ? municipio.getIdMunicipio() : null);
	}

}
