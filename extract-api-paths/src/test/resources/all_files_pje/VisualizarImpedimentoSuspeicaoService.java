package br.jus.cnj.pje.nucleo.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.SessaoPautaProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.list.SessaoPautaRelacaoJulgamentoList;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ImpedimentoSuspeicaoDAO;
import br.jus.cnj.pje.entidades.vo.ImpedimentoSuspeicaoVO;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoComposicaoManager;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Classe respons?vel pela parte negocial em que se refere a aba impedimento/suspei??o no cadastro do processo.
 * 
 */
@Name(VisualizarImpedimentoSuspeicaoService.NAME)
public class VisualizarImpedimentoSuspeicaoService extends BaseService implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 8218568201922227136L;
	public static final String NAME = "visualizarImpedimentoSuspeicaoService";
	
	@In
	private transient ImpedimentoSuspeicaoDAO impedimentoSuspeicaoDAO;
	
	@In
	private ImpedimentoSuspeicaoService impedimentoSuspeicaoService;
	
	@Logger
	private transient Log log;
	
	/**
	 * Lista os impedimento/suspeição de acordo com o relator do processo da tarefa selecionada.
	 * 
	 * @return List<ImpedimentoSuspeicao> lista com os impedimento/suspei??o.
	 */
	public List<ImpedimentoSuspeicao> pesquisar() {
		ProcessoTrf processoTrf = ComponentUtil.getTramitacaoProcessualService().recuperaProcesso();
		List<Integer> ids = new ArrayList<>();
		if(!ParametroUtil.instance().isPrimeiroGrau()) {
			Usuario relator = ComponentUtil.getProcessoJudicialManager().getRelator(processoTrf);
			ids.add(relator.getIdUsuario());
		} else {
			ids = ComponentUtil.getProcessoMagistradoManager().obterResponsaveis(processoTrf);
		}
		List<ImpedimentoSuspeicao> listaRetorno = impedimentoSuspeicaoDAO.pesquisarPorUsuario(ids);
		return impedimentoSuspeicaoService.validarRegras(listaRetorno, processoTrf);
	}
	

	/**
	 * Lista os impedimento/suspei??o de acordo com o relator do processo de cada item na lista.
	 * 
	 * @param listaProcessosSelecionados 
	 * @return List<ImpedimentoSuspeicaoVO> lista com as VO's de impedimento/suspei??o. 
	 */
	public List<ImpedimentoSuspeicaoVO> pesquisar(List<ProcessoTrf> listaProcessosSelecionados) {
		List<ImpedimentoSuspeicaoVO> listaRetorno = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(listaProcessosSelecionados)) {
			PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getPessoaMagistradoManager();
			List<PessoaMagistrado> listaTodosMagistrados = pessoaMagistradoManager.magistradoList();
			for (ProcessoTrf processoTrf : listaProcessosSelecionados) {
				listaRetorno.addAll(validarRegrasTodosMagistrados(processoTrf, listaTodosMagistrados));
			}
		}
		return listaRetorno;
	}
	
	/**
	 * Cria a lista de impedimentoSuspeicaoVO de acordo com os impedimentos enquadrados.
	 * 
	 * @param descricaoProcesso String sigla da classe judicial concatenada com o n?mero do processo.
	 * @param orgaoJulgador String nome do ?rg?o julgador.
	 * @param listaImpedimentoEnquadrados List<ImpedimentoSuspeicao> lista com os impedimento/suspei??o enquadrados.
	 * @return List<ImpedimentoSuspeicaoVO> retorna com a lista de ImpedimentoSuspeicaoVO. 
	 */
	private List<ImpedimentoSuspeicaoVO> preencherListaImpedimentoSuspeicaoVO(String descricaoProcesso, String orgaoJulgador, List<ImpedimentoSuspeicao> listaImpedimentoEnquadrados) {
		List<ImpedimentoSuspeicaoVO> listaRetorno = new ArrayList<>();
		ImpedimentoSuspeicaoVO vo;
		for (ImpedimentoSuspeicao impedimentoSuspeicao : listaImpedimentoEnquadrados) {
			Hibernate.initialize(impedimentoSuspeicao.getPessoaMagistrado());
			vo = new ImpedimentoSuspeicaoVO();
			vo.setNumeroProcesso(descricaoProcesso);
			vo.setImpedimentoSuspeicao(impedimentoSuspeicao);
			vo.setOrgaoJulgador(orgaoJulgador);
			listaRetorno.add(vo);
		}
		return listaRetorno;
	}
	
	/**
	 * Lista os impedimento/suspei??o de acordo com o relator do processo de cada sess?o pauta de cada item na lista.
	 * 
	 * @return List<ImpedimentoSuspeicaoVO> retorna com a lista de ImpedimentoSuspeicaoVO. 
	 */
	public List<ImpedimentoSuspeicaoVO> pesquisarRelacaoJulgamento() {
		List<ImpedimentoSuspeicaoVO> listaRetorno = new ArrayList<>();
		SessaoPautaRelacaoJulgamentoList listaSessaoPautaRelacaoJulgamento = (SessaoPautaRelacaoJulgamentoList) Component.getInstance(SessaoPautaRelacaoJulgamentoList.class);
		if (listaSessaoPautaRelacaoJulgamento != null) {
			PessoaMagistradoManager pessoaMagistradoManager = ComponentUtil.getPessoaMagistradoManager();
			List<PessoaMagistrado> listaTodosMagistrados = pessoaMagistradoManager.magistradoList();
			for(SessaoPautaProcessoTrf sessaoPautaProcessoTrf : listaSessaoPautaRelacaoJulgamento.list()) {
				ProcessoTrf processoTrf = sessaoPautaProcessoTrf.getProcessoTrf();
				listaRetorno.addAll(validarRegrasTodosMagistrados(processoTrf, listaTodosMagistrados));
			}
		}
		return listaRetorno;
	}
	
	/**
	 * Lista os impedimento/suspei??o de acordo com o relator do processo de cada sess?o pauta de cada item na lista.
	 * 
	 * @return List<ImpedimentoSuspeicaoVO> retorna com a lista de ImpedimentoSuspeicaoVO. 
	 */
	public List<ImpedimentoSuspeicaoVO> pesquisarComposicaoJulgamento() {
		List<ImpedimentoSuspeicaoVO> listaRetorno = new ArrayList<>(0);
		
		SessaoPautaProcessoTrf sessaoPautaProcessoTrf = SessaoPautaProcessoTrfHome.instance().getInstance();
		
		List<SessaoPautaProcessoComposicao>	listComposicaoProcesso = ComponentUtil.getComponent(SessaoPautaProcessoComposicaoManager.class).findBySessaoPautaProcessoTrf(sessaoPautaProcessoTrf, true);
		ProcessoTrf processoTrf = sessaoPautaProcessoTrf.getProcessoTrf();
		String descricaoProcesso = ComponentUtil.getProcessoTrfManager().recuperarDescricaoProcesso(processoTrf);
		if (CollectionUtils.isNotEmpty(listComposicaoProcesso)) {
			for (SessaoPautaProcessoComposicao sessao : listComposicaoProcesso) { 
				String orgaoJulgador = sessao.getOrgaoJulgador().getOrgaoJulgador();
				List<Integer> ids = new ArrayList<>();
				ids.add(sessao.getMagistradoPresente().getIdUsuario());
				List<ImpedimentoSuspeicao> listaImpedimentoSuspeicaoRelator = impedimentoSuspeicaoDAO.pesquisarPorUsuario(ids);
				List<ImpedimentoSuspeicao> listaImpedimentoEnquadrados = impedimentoSuspeicaoService.validarRegras(listaImpedimentoSuspeicaoRelator, processoTrf);
				if (CollectionUtils.isNotEmpty(listaImpedimentoEnquadrados)) {
					listaRetorno.addAll(preencherListaImpedimentoSuspeicaoVO(descricaoProcesso, orgaoJulgador, listaImpedimentoEnquadrados));
				}
			}
		}
		return listaRetorno;
	}
	
	/**
	 * Valida as regras para o relator do processo informado.
	 * 
	 * @param processoTrf ProcessoTrf dados do processo.
	 * @return List<ImpedimentoSuspeicaoVO>  lista com os impedimento/suspei??o enquadrados.
	 */
	private List<ImpedimentoSuspeicaoVO> validarRegrasTodosMagistrados(ProcessoTrf processoTrf, List<PessoaMagistrado> listaMagistrados) {
		List<ImpedimentoSuspeicaoVO> listaRetorno = new ArrayList<>();
		String descricaoProcesso = ComponentUtil.getProcessoTrfManager().recuperarDescricaoProcesso(processoTrf);
		String orgaoJulgador = ComponentUtil.getProcessoTrfManager().recuperarOrgaoJulgador(processoTrf);
		if (CollectionUtils.isNotEmpty(listaMagistrados)) {
			List<ImpedimentoSuspeicao> listaImpedimentoSuspeicaoMagistrado = null;
			for (PessoaMagistrado pessoaMagistrado : listaMagistrados) {
				List<Integer> ids = new ArrayList<>();
				ids.add(pessoaMagistrado.getIdUsuario());
				listaImpedimentoSuspeicaoMagistrado = impedimentoSuspeicaoDAO.pesquisarPorUsuario(ids);
				List<ImpedimentoSuspeicao> listaImpedimentoEnquadrados = impedimentoSuspeicaoService.validarRegras(listaImpedimentoSuspeicaoMagistrado, processoTrf);
				if (CollectionUtils.isNotEmpty(listaImpedimentoEnquadrados)) {
					listaRetorno.addAll(preencherListaImpedimentoSuspeicaoVO(descricaoProcesso, orgaoJulgador, listaImpedimentoEnquadrados));
				}
			}
		}
		
		return listaRetorno;
	}
}