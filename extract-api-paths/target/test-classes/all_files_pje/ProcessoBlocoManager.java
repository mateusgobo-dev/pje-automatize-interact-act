package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcessoBlocoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ModeloDocumentoLocal;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoEditorEnum;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("processoBlocoManager")
public class ProcessoBlocoManager extends BaseManager<ProcessoBloco>{

	@Override
	protected ProcessoBlocoDAO getDAO() {
		return ComponentUtil.getProcessoBlocoDAO();
	}
	
	public List<ProcessoTrf> pesquisarProcessosEmBlocos(Sessao sessao) {
		return ComponentUtil.getProcessoBlocoDAO().pesquisarProcessosEmBlocos(sessao);
	}
	
	public List<ProcessoTrf> pesquisarProcessosPautadosEmBlocos(Sessao sessao) {
		return ComponentUtil.getProcessoBlocoDAO().pesquisarProcessosPautadosEmBlocos(sessao);
	}
	
	public long recuperarQuantidadeProcessos(BlocoJulgamento bloco) {
		return ComponentUtil.getProcessoBlocoDAO().recuperarQuantidadeProcessos(bloco);
	}
	
	public ProcessoBloco recuperarNovoProcessoBloco(BlocoJulgamento bloco, ProcessoTrf processo) {
		ProcessoBloco associado = new ProcessoBloco();
		associado.setAtivo(true);
		associado.setBloco(bloco);
		associado.setProcessoTrf(processo);
		associado.setDataInclusao(DateUtil.getDataAtual());
		return associado;
	}
	
	public List<ProcessoBloco> recuperarProcessos(BlocoJulgamento bloco) {
		return ComponentUtil.getProcessoBlocoDAO().recuperarProcessos(bloco);
	}
	
	public ProcessoBloco recuperarProcessoBloco(BlocoJulgamento bloco, ProcessoTrf processo) {
		return ComponentUtil.getProcessoBlocoDAO().recuperarProcessoBloco(bloco, processo);
	}
	
	public List<ProcessoTrf> recuperaProcessosBlocosNaoPautados(Sessao sessao) {
		return ComponentUtil.getProcessoBlocoDAO().recuperaProcessosBlocosNaoPautados(sessao);
	}
	
	
	public void incluirProcessoBlocoJulgamento(BlocoJulgamento bloco, ProcessoTrf processo, Sessao sessao) throws Exception {
		ProcessoBloco associado = ComponentUtil.getProcessoBlocoManager().recuperarNovoProcessoBloco(bloco, processo);
		ComponentUtil.getProcessoBlocoManager().persistAndFlush(associado);
		ComponentUtil.getSessaoPautaProcessoTrfManager().pautarProcesso(sessao, processo, TipoInclusaoEnum.BL);
	}

	public ProcessoBloco recuperaPrimeiroProcessoBloco(BlocoJulgamento bloco) {
		Search s = new Search(ProcessoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.equals("ativo", true));
		s.setMax(1);
		List<ProcessoBloco> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}
	
	public List<ProcessoDocumento> recuperarCertidoes(BlocoJulgamento bloco) {
		Search s = new Search(ProcessoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco", bloco),
				Criteria.not(Criteria.isNull("certidaoJulgamento")),
				Criteria.equals("ativo", true));
		s.setRetrieveField("certidaoJulgamento");
		List<ProcessoDocumento> ret = list(s);
		return ret;
	}
	
	public void gerarCertidoes(ModeloDocumento modeloDocumento, BlocoJulgamento bloco) throws PJeBusinessException {
		List<ProcessoBloco> processos = this.recuperarProcessos(bloco);
		ModeloDocumentoLocal modeloLocal = ComponentUtil.getModeloDocumentoLocalManager().findById(modeloDocumento.getIdModeloDocumento());
		TipoEditorEnum tipoEditor = modeloLocal.getTipoEditor();
		String conteudoModelo = modeloDocumento.getModeloDocumento();
		if(bloco.getCertidaoJulgamento() != null) {
			conteudoModelo = bloco.getCertidaoJulgamento();
		}
		for (ProcessoBloco processoBloco : processos){
			SessaoProcessoDocumento sessaoDocumento = ComponentUtil.getSessaoProcessoDocumentoManager().recuperarCertidaoJulgamento(tipoEditor, conteudoModelo, bloco.getSessao(), processoBloco.getProcessoTrf());
			if(sessaoDocumento != null) {
				processoBloco.setCertidaoJulgamento(sessaoDocumento.getProcessoDocumento());
				this.mergeAndFlush(processoBloco);
			}
		}
	}
	
	public void adicionarProcessosBlocos(BlocoJulgamento bloco, List<ProcessoTrf> processosSelecionados, boolean validarProcessos) throws PJeBusinessException {
		if(validarProcessos) {
			if(bloco.getAgruparOrgaoJulgador() && !ComponentUtil.getBlocoJulgamentoManager().validarProcessosMesmaRelatoria(processosSelecionados, bloco)) {
				throw new PJeBusinessException("Os processos não são de mesma relatoria!");
			}
		}
		for (ProcessoTrf p : processosSelecionados){
			if(recuperarProcessoBloco(bloco, p) == null) { 
				ProcessoBloco associado = recuperarNovoProcessoBloco(bloco, p); 
				ComponentUtil.getProcessoBlocoManager().persistAndFlush(associado); 
			} 
		}
	}
	
	public boolean verificarTodosProcessosPautados(BlocoJulgamento bloco) {
		boolean retorno = true;
		List<ProcessoBloco> processos = ComponentUtil.getProcessoBlocoDAO().recuperarProcessos(bloco);
		for (ProcessoBloco p : processos){
			if(ComponentUtil.getSessaoPautaProcessoTrfManager().getSessaoPautaProcessoTrf(p.getProcessoTrf(), bloco.getSessao()) == null) {
				retorno = false;
				break;
			}
		}
		return retorno;
	}
	
	public boolean agrupadoEmBloco(Sessao sessao, ProcessoTrf processo) {
		Search s = new Search(ProcessoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco.sessao", sessao),
				Criteria.equals("processoTrf", processo),
				Criteria.equals("bloco.ativo", true),
				Criteria.equals("ativo", true));
		return list(s).isEmpty() ? false : true;
	}
	
	public ProcessoBloco recuperarProcessoBloco(Sessao sessao, ProcessoTrf processo) {
		Search s = new Search(ProcessoBloco.class);
		addCriteria(s,
				Criteria.equals("bloco.sessao", sessao),
				Criteria.equals("processoTrf", processo),
				Criteria.equals("bloco.ativo", true),
				Criteria.equals("ativo", true));
		s.setMax(1);
		List<ProcessoBloco> ret = list(s);
		return ret.isEmpty() ? null : ret.get(0);
	}

}
