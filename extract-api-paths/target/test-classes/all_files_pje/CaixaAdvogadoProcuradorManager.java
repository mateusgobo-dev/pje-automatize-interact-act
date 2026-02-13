/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;

import br.com.infox.cliente.actions.CaixaAdvogadoProcuradorAction;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.CaixaAdvogadoProcuradorDAO;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.je.pje.entity.vo.CaixaAdvogadoProcuradorVO;
import br.jus.je.pje.entity.vo.JurisdicaoVO;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoUsuarioExternoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de gerenciamento da entidade {@link CaixaAdvogadoProcurador}.
 * 
 * @author cristof
 *
 */
@Name(CaixaAdvogadoProcuradorManager.NAME)
public class CaixaAdvogadoProcuradorManager extends BaseManager<CaixaAdvogadoProcurador> {
	public static final String NAME = "caixaAdvogadoProcuradorManager";
	
	private Integer idUsuarioAtual;
	private Integer idLocalizacaoAtual;
	private TipoUsuarioExternoEnum tipoUsuarioExternoAtual;
	private Integer idProcuradoriaAtual;
	private boolean isProcuradorGestor;
	
	@In
	private CaixaAdvogadoProcuradorDAO caixaAdvogadoProcuradorDAO;

	@Override
	protected CaixaAdvogadoProcuradorDAO getDAO() {
		return caixaAdvogadoProcuradorDAO;
	}
	
	@Observer(Eventos.EVENTO_ATUALIZAR_CAIXAS_PROCURADORES)
	public void distribuirUtilizandoFiltro(ProcessoTrf processo) {
		try {
            if(processo != null && processo.getJurisdicao()!=null) {
            	List<CaixaAdvogadoProcuradorVO> caixas = null;
                caixas = ComponentUtil.getProcessoJudicialManager().getCaixasAcervoJurisdicao(processo.getJurisdicao().getIdJurisdicao());
                if(caixas.size() > 0) {
	                CaixaAdvogadoProcuradorAction caixaAction = (CaixaAdvogadoProcuradorAction) Component.getInstance(CaixaAdvogadoProcuradorAction.class);
	                ConsultaProcessoVO criterios = null;
	                CaixaAdvogadoProcurador caixaDestino = null;
	                for(CaixaAdvogadoProcuradorVO caixaDestinoVO : caixas) {
	                    if(!caixaDestinoVO.getPadrao()) {
	                    	caixaDestino = ComponentUtil.getComponent(CaixaAdvogadoProcuradorManager.class).findById(caixaDestinoVO.getId());
                        	criterios = caixaAction.getCriteriosPesquisaProcessos(caixaDestino);
                        	if (criterios!=null) {
                        		criterios.setJurisdicao(processo.getJurisdicao());
                        		criterios.setApenasSemCaixa(false);
                        		ComponentUtil.getProcessoJudicialManager().copiarProcessoParaCaixa(processo,caixaDestino,criterios);
                        	}
	                    }
	                }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
	}
	
	public static CaixaAdvogadoProcuradorManager instance() {
		return (CaixaAdvogadoProcuradorManager)Component.getInstance(CaixaAdvogadoProcuradorManager.NAME);
	}

	public List<CaixaAdvogadoProcurador> list(Jurisdicao jurisdicao, Localizacao localizacao) {
		return list(jurisdicao.getIdJurisdicao(), localizacao.getIdLocalizacao());
	}
	
	public List<CaixaAdvogadoProcurador> list(Integer idJurisdicao, Integer idLocalizacao) {
		return list(idJurisdicao, idLocalizacao, null);
	}

	public List<CaixaAdvogadoProcurador> list(Integer idJurisdicao, Integer idLocalizacao, Integer idPessoaFisica) {
		return list(idJurisdicao, idLocalizacao, idPessoaFisica, false);
	}
	
	public List<CaixaAdvogadoProcurador> list(Integer idJurisdicao, Integer idLocalizacao, Integer idPessoaFisica, boolean ativas) {
		return getDAO().list(idJurisdicao, idLocalizacao, idPessoaFisica, ativas);
	}

	public CaixaAdvogadoProcuradorVO criarNovaCaixa(String nome, JurisdicaoVO jurisdicaoVO) throws PJeBusinessException {
		inicializaDadosUsuario();
		Localizacao localizacao = LocalizacaoManager.instance().findById(this.idLocalizacaoAtual);
		Jurisdicao jurisdicao = JurisdicaoManager.instance().findById(jurisdicaoVO.getId());
		
		if (StringUtils.isBlank(nome)) {
			throw new PJeBusinessException("Nome da caixa inválido: [" + nome + "].");
		}
		if (jurisdicao == null || localizacao == null) {
			throw new PJeBusinessException("Não é possível criar uma caixa com a jurisdição ou a localização nulas.");
		}
		if (caixaExistente(nome, jurisdicao, localizacao)) {
			throw new PJeBusinessException("Caixa já existe.");
		}
		CaixaAdvogadoProcurador cx = new CaixaAdvogadoProcurador();
		cx.setNomeCaixaAdvogadoProcurador(nome);
		cx.setJurisdicao(jurisdicao);
		cx.setLocalizacao(localizacao);
		persistAndFlush(cx);
		
		return new CaixaAdvogadoProcuradorVO(cx.getIdCaixaAdvogadoProcurador(), cx.getNomeCaixaAdvogadoProcurador(), 
			cx.getDsCaixaAdvogadoProcurador(), jurisdicao.getIdJurisdicao(), jurisdicao.getJurisdicao(), Boolean.TRUE, Boolean.TRUE, BigInteger.ZERO);
	}
	
	public List<CaixaAdvogadoProcurador> recuperaCaixasAdvogadoProcurador(Integer idJurisdicao, Integer idLocalizacao, Integer idPessoaFisica) {
		return caixaAdvogadoProcuradorDAO.list(idJurisdicao, idLocalizacao, idPessoaFisica);
	}
	
	public List<CaixaAdvogadoProcurador> findByIdLocalizacao(Integer idLocalizacao) {
		return caixaAdvogadoProcuradorDAO.findByIdLocalizacao(idLocalizacao);
	}
		
	private boolean caixaExistente(String nome, Jurisdicao jurisdicao, Localizacao localizacao){
		Search s = new Search(CaixaAdvogadoProcurador.class);
		addCriteria(s, Criteria.equals("jurisdicao", jurisdicao));
		addCriteria(s, Criteria.equals("nomeCaixaAdvogadoProcurador", nome));
		addCriteria(s, Criteria.equals("localizacao", localizacao));
		return count(s) > 0;
	}

	public Map<Jurisdicao,List<CaixaAdvogadoProcurador>> getCaixas(Integer idLocalizacao, Integer idProcuradoria, Integer idPessoa, List<Integer> idsJurisdicoes, RepresentanteProcessualTipoAtuacaoEnum atuacaoProcurador, TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		if(idsJurisdicoes == null || idsJurisdicoes.size() == 0){
			return new HashMap<Jurisdicao,List<CaixaAdvogadoProcurador>>();
		}
		return caixaAdvogadoProcuradorDAO.getCaixas(idLocalizacao, idProcuradoria, idPessoa, idsJurisdicoes, atuacaoProcurador, tipoSituacaoExpediente);
	}
	
	public boolean isCaixaAtiva(Integer idCaixa) {
		return caixaAdvogadoProcuradorDAO.isCaixaAtiva(idCaixa);
	}

	public void inicializaDadosUsuario() throws PJeBusinessException{
		this.idUsuarioAtual = Authenticator.getIdUsuarioLogado();
		this.idLocalizacaoAtual = Authenticator.getIdLocalizacaoAtual();
		this.tipoUsuarioExternoAtual = Authenticator.getTipoUsuarioExternoAtual();
		this.idProcuradoriaAtual = Authenticator.getIdProcuradoriaAtualUsuarioLogado();
		this.isProcuradorGestor = Authenticator.isRepresentanteGestor();
	}
	
	/**
	 * Retorna todas as caixas dada uma jurisdiçao que o usuário tem permissão de acessar e que batam com os parâmetros de pesquisa, mesmo que estas estejam vazias 
	 * 
	 * @param idJurisdicao
	 * @param criteriosPesquisa
	 * @return
	 * @throws PJeBusinessException
	 */
	public List<CaixaAdvogadoProcuradorVO> obterCaixasExpedientesJurisdicao(Integer idJurisdicao, PesquisaExpedientesVO criteriosPesquisa) throws PJeBusinessException{
		this.inicializaDadosUsuario();
		
		return caixaAdvogadoProcuradorDAO.obterCaixasExpedientesJurisdicao(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisa);
	}
	
	public List<CaixaAdvogadoProcuradorVO> obterCaixasAcervoJurisdicao(Integer idJurisdicao, ConsultaProcessoVO criteriosPesquisa) throws PJeBusinessException{
		this.inicializaDadosUsuario();
		
		return caixaAdvogadoProcuradorDAO.obterCaixasAcervoJurisdicao(idUsuarioAtual, idLocalizacaoAtual, 
				tipoUsuarioExternoAtual, idProcuradoriaAtual, isProcuradorGestor, idJurisdicao, criteriosPesquisa);
	}
}
