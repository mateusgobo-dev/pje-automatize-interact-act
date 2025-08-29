package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.List;
import org.compass.core.util.CollectionUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.entidades.vo.ImpedimentoSuspeicaoVO;
import br.jus.cnj.pje.nucleo.service.VisualizarImpedimentoSuspeicaoService;
import br.jus.pje.nucleo.entidades.ImpedimentoSuspeicao;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(VisualizarImpedimentoSuspeicaoAction.NAME)
@Scope(ScopeType.SESSION)
public class VisualizarImpedimentoSuspeicaoAction implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1009452071728722555L;
	
	public static final String NAME = "visualizarImpedimentoSuspeicaoAction";
	
	private List<ImpedimentoSuspeicao> listaResultadoPesquisa;
	
	private List<ImpedimentoSuspeicaoVO> listaResultadoPesquisaVO;
	
	private List<ProcessoTrf> listaProcesso;
	
	private Boolean sessaoPautaRelacaoJulgamento;
	
	private Boolean aptoInclusaoEmPauta;
	
	private Boolean aptoInclusaoEmMesa;
	
	private Boolean composicaoJulgamento;
	
	private ImpedimentoSuspeicao impedimentoSuspeicao;
	
	private ImpedimentoSuspeicaoVO impedimentoSuspeicaoVO;
	
	public static final int TAMANHO_TEXTO_EXIBICAO = 130;
	
	@Logger
	private transient Log logger;
	
	/**
	 * Recupera a lista de impedimento/suspeicao de acordo com o magistrado do processo selecionado na tarefa.
	 */
	public void pesquisar() {
		VisualizarImpedimentoSuspeicaoService service = ComponentUtil.getComponent(VisualizarImpedimentoSuspeicaoService.class);
		if (CollectionUtils.isEmpty(listaProcesso) && !aptoInclusaoEmPauta && !aptoInclusaoEmMesa && !sessaoPautaRelacaoJulgamento && !composicaoJulgamento) {
			listaResultadoPesquisa = service.pesquisar();
		} 
		if (!CollectionUtils.isEmpty(listaProcesso) && (aptoInclusaoEmPauta || aptoInclusaoEmMesa))  {
			listaResultadoPesquisaVO = service.pesquisar(listaProcesso);
		} 
		if (sessaoPautaRelacaoJulgamento)  {
			listaResultadoPesquisaVO = service.pesquisarRelacaoJulgamento();
		} 
		if (composicaoJulgamento) {
			listaResultadoPesquisaVO = service.pesquisarComposicaoJulgamento();
		}
	}
	
	/**
	 * @return the listaResultadoPesquisa
	 */
	public List<ImpedimentoSuspeicao> getListaResultadoPesquisa() {
		return listaResultadoPesquisa;
	}
	
	/**
	 * @param listaResultadoPesquisa the listaResultadoPesquisa to set
	 */
	public void setListaResultadoPesquisa(List<ImpedimentoSuspeicao> listaResultadoPesquisa) {
		this.listaResultadoPesquisa = listaResultadoPesquisa;
	}
	
	/**
	 * @return the listaProcesso
	 */
	public List<ProcessoTrf> getListaProcesso() {
		return listaProcesso;
	}
	
	/**
	 * @param listaProcesso the listaProcesso to set
	 */
	public void setListaProcesso(List<ProcessoTrf> listaProcesso) {
		this.listaProcesso = listaProcesso;
	}
	
	/**
	 * @return the listaResultadoPesquisaVO
	 */
	public List<ImpedimentoSuspeicaoVO> getListaResultadoPesquisaVO() {
		return listaResultadoPesquisaVO;
	}
	
	/**
	 * @param listaResultadoPesquisaVO the listaResultadoPesquisaVO to set
	 */
	public void setListaResultadoPesquisaVO(List<ImpedimentoSuspeicaoVO> listaResultadoPesquisaVO) {
		this.listaResultadoPesquisaVO = listaResultadoPesquisaVO;
	}
	
	/**
	 * @return the sessaoPautaRelacaoJulgamento
	 */
	public Boolean getSessaoPautaRelacaoJulgamento() {
		return sessaoPautaRelacaoJulgamento;
	}
	
	/**
	 * @param sessaoPautaRelacaoJulgamento the sessaoPautaRelacaoJulgamento to set
	 */
	public void setSessaoPautaRelacaoJulgamento(Boolean sessaoPautaRelacaoJulgamento) {
		this.sessaoPautaRelacaoJulgamento = sessaoPautaRelacaoJulgamento;
	}
	
	/**
	 * @return the aptoInclusaoEmPauta
	 */
	public Boolean getAptoInclusaoEmPauta() {
		return aptoInclusaoEmPauta;
	}
	
	/**
	 * @param aptoInclusaoEmPauta the aptoInclusaoEmPauta to set
	 */
	public void setAptoInclusaoEmPauta(Boolean aptoInclusaoEmPauta) {
		this.aptoInclusaoEmPauta = aptoInclusaoEmPauta;
	}
	
	/**
	 * @return the aptoInclusaoEmMesa
	 */
	public Boolean getAptoInclusaoEmMesa() {
		return aptoInclusaoEmMesa;
	}
	
	/**
	 * @param aptoInclusaoEmMesa the aptoInclusaoEmMesa to set
	 */
	public void setAptoInclusaoEmMesa(Boolean aptoInclusaoEmMesa) {
		this.aptoInclusaoEmMesa = aptoInclusaoEmMesa;
	}
	
	/**
	 * @return the composicaoJulgamento
	 */
	public Boolean getComposicaoJulgamento() {
		return composicaoJulgamento;
	}
	
	/**
	 * @param composicaoJulgamento the composicaoJulgamento to set
	 */
	public void setComposicaoJulgamento(Boolean composicaoJulgamento) {
		this.composicaoJulgamento = composicaoJulgamento;
	}
	
	/**
	 * @return the logger
	 */
	public Log getLogger() {
		return logger;
	}
	
	/**
	 * @param logger the logger to set
	 */
	public void setLogger(Log logger) {
		this.logger = logger;
	}
	
	/**
	 * @return the impedimentoSuspeicao
	 */
	public ImpedimentoSuspeicao getImpedimentoSuspeicao() {
		return impedimentoSuspeicao;
	}
	
	/**
	 * @param impedimentoSuspeicao the impedimentoSuspeicao to set
	 */
	public void setImpedimentoSuspeicao(ImpedimentoSuspeicao impedimentoSuspeicao) {
		this.impedimentoSuspeicao = impedimentoSuspeicao;
	}
	
	/**
	 * @return the impedimentoSuspeicaoVO
	 */
	public ImpedimentoSuspeicaoVO getImpedimentoSuspeicaoVO() {
		return impedimentoSuspeicaoVO;
	}
	
	/**
	 * @param impedimentoSuspeicaoVO the impedimentoSuspeicaoVO to set
	 */
	public void setImpedimentoSuspeicaoVO(ImpedimentoSuspeicaoVO impedimentoSuspeicaoVO) {
		this.impedimentoSuspeicaoVO = impedimentoSuspeicaoVO;
	}
	
	public static String retornarTextoReduzido(String texto) {
		String retorno = "";
		if( texto != null ) {
			retorno = texto.length() > TAMANHO_TEXTO_EXIBICAO ? texto.substring(0,TAMANHO_TEXTO_EXIBICAO).concat("...") : texto;
		}
		return retorno;
	}
}