package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BlocoJulgamentoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.dto.BlocoJulgamentoDTO;
import br.jus.pje.nucleo.dto.DadosBlocoJulgamentoDTO;
import br.jus.pje.nucleo.dto.FiltroProcessoSessaoDTO;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoBloco;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.enums.TipoInclusaoDocumentoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaBlocoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name("blocoJulgamentoManager")
public class BlocoJulgamentoManager extends BaseManager<BlocoJulgamento>{

	@Override
	protected BlocoJulgamentoDAO getDAO() {
		return ComponentUtil.getBlocoJulgamentoDAO();
	}

	public List<BlocoJulgamento> findBySessao(Sessao sessao) {
		return getDAO().findBySessao(sessao);
	}
	
	public List<BlocoJulgamento> recuperarBlocosComProcessos(Sessao sessao, boolean especificarJulgados, boolean julgados) {
		return getDAO().recuperarBlocosComProcessos(sessao, especificarJulgados, julgados);
	}
	
	public BlocoJulgamento findByNome(String nome, Sessao sessao) {
		return getDAO().findByNome(nome, sessao);
	}

	public BlocoJulgamento pesquisar(ProcessoTrf processo, Sessao sessao) {
		return getDAO().pesquisar(processo, sessao);
	}
	
	public List<ProcessoTrf> pesquisarProcessosAptosBloco(FiltroProcessoSessaoDTO filtro){
		List<ProcessoTrf> processosPesquisados;
		processosPesquisados = ComponentUtil.getProcessoJudicialManager().pesquisarPautaMesa(filtro);
		List<ProcessoTrf> processosAdiados = ComponentUtil.getConsultaProcessoAdiadoVistaManager().pesquisarAdiados(filtro);
		if( processosAdiados != null ) {
			processosPesquisados.addAll(processosAdiados);
		}
		List<ProcessoTrf> processosVista = ComponentUtil.getConsultaProcessoAdiadoVistaManager().pesquisarVista(filtro);
		if( processosVista != null ) {
			processosPesquisados.addAll(processosVista);
		}
		processosPesquisados.removeAll(ComponentUtil.getProcessoBlocoManager().pesquisarProcessosPautadosEmBlocos(filtro.getSessao()));
		return processosPesquisados;
	}
	
	public BlocoJulgamento recuperarNovoBlocoJulgamento(String nomeBloco, Sessao sessao) {
		BlocoJulgamento bloconovo = new BlocoJulgamento();
		bloconovo.setAtivo(true);
		bloconovo.setBlocoJulgamento(nomeBloco);
		bloconovo.setDataCriacao(DateUtil.getDataAtual());
		bloconovo.setSessao(sessao);
		return bloconovo;
	}
	
	public BlocoJulgamento getBlocoJulgamentoByID(Integer idBloco) {
	   	return getDAO().find(idBloco);
	}
	
	public void atualizarBlocoJulgamento(BlocoJulgamento bloco, TipoSituacaoPautaBlocoEnum situacao) throws Exception {
		List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager()
				.recuperar(bloco);

		for (SessaoPautaProcessoTrf processoPautado : processosPautados) {
			Boolean isbloqueado = ComponentUtil.getSessaoPautaProcessoTrfManager()
					.verificarBloqueioRegistroJulgamentoSemVoto(processoPautado);
			if (situacao.equals(TipoSituacaoPautaBlocoEnum.JG)
					&& (StringUtils.isEmpty(processoPautado.getProclamacaoDecisao())
							|| processoPautado.getOrgaoJulgadorVencedor() == null || isbloqueado)) {

				if (isbloqueado) {
					throw new PJeBusinessException("Antes de julgar o bloco,os votos devem ser registrados");
				}
				throw new PJeBusinessException(
						"Antes de julgar o bloco, a proclamação de julgamento e o órgão julgador vencedor devem ser informados!");

			} else {
				SessaoPautaProcessoTrf sppt = ComponentUtil.getSessaoPautaProcessoTrfManager()
						.alterarSituacaoJulgamento(TipoSituacaoPautaBlocoEnum.getAcaoBtnLegenda(situacao),
								processoPautado);
				if (sppt != null) {
					sppt.setProclamacaoDecisao(bloco.getProclamacaoJulgamento());
					sppt.setOrgaoJulgadorVencedor(bloco.getOrgaoJulgadorVencedor());
					ComponentUtil.getSessaoPautaProcessoTrfManager().mergeAndFlush(sppt);
				}
			}
		}
		bloco.setSituacaoJulgamento(situacao);
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);

	}

	public void atualizarProclamacao(BlocoJulgamento bloco) throws Exception {
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
		List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(bloco);
		for (SessaoPautaProcessoTrf processoPautado : processosPautados){
			processoPautado.setProclamacaoDecisao(bloco.getProclamacaoJulgamento());
			processoPautado.setJulgamentoEnum(bloco.getJulgamentoEnum());
			ComponentUtil.getSessaoPautaProcessoTrfManager().alterar(processoPautado);
		}
	}
	
	public BlocoJulgamento atualizarVotoRelator(BlocoJulgamento bloco) throws PJeBusinessException {
		bloco.setVotoRelator(ComponentUtil.getVotoBlocoManager().recuperarVotoDoRelator(bloco));
		this.mergeAndFlush(bloco);
		return bloco;
	}
	
	public void atualizarVencedor(BlocoJulgamento bloco, OrgaoJulgador orgaoJulgador) throws Exception {
		bloco.setOrgaoJulgadorVencedor(orgaoJulgador);
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
		List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(bloco);
		for (SessaoPautaProcessoTrf processoPautado : processosPautados){
			ComponentUtil.getSessaoPautaProcessoTrfManager().atualizarVencedor(processoPautado, orgaoJulgador);
		}
	}
	
	public void setaMaioriaVotacao(BlocoJulgamento bloco) {		
		OrgaoJulgador ojMaioria = ComponentUtil.getVotoBlocoManager().contagemMaioriaVotacao(bloco);
		if (ojMaioria != null) {			
			bloco.setOrgaoJulgadorVencedor(ojMaioria);
		} else {
			bloco.setOrgaoJulgadorVencedor(bloco.getOrgaoJulgadorRelator());
		}
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
	}
	
	public List<ProcessoDocumento> recuperarDocumentosParaAssinatura(List<BlocoJulgamentoDTO> blocos) {
		List<ProcessoDocumento> retorno = new ArrayList<ProcessoDocumento>();
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			List<ProcessoDocumento> certidoes = ComponentUtil.getProcessoBlocoManager().recuperarCertidoes(blocoDTO.getBloco());
			retorno.addAll(certidoes);
		}
		return retorno;
	}
	
	public void registrarCertidoes(ModeloDocumento modeloDocumento, List<BlocoJulgamentoDTO> blocos) throws PJeBusinessException {
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			BlocoJulgamento bloco = blocoDTO.getBloco();
			ComponentUtil.getProcessoBlocoManager().gerarCertidoes(modeloDocumento, bloco);
			bloco.setCertidaoJulgamento(modeloDocumento.getModeloDocumento());
			bloco.setCertidaoPresente(true);
			this.mergeAndFlush(bloco);
		}
	}
	
	public void registrarAssinaturaCertidao(List<BlocoJulgamentoDTO> blocos) {
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			BlocoJulgamento bloco = blocoDTO.getBloco();
			bloco.setCertidaoAssinada(true);
			this.mergeAndFlush(bloco);
		}
	}
	
	public void alterarModeloCertidao(String modeloDocumento, List<BlocoJulgamentoDTO> blocos) throws PJeBusinessException {
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			BlocoJulgamento bloco = blocoDTO.getBloco();
			bloco.setCertidaoJulgamento(modeloDocumento);
			this.mergeAndFlush(bloco);
		}
	}
	
	public boolean verificarAptidaoAssinaturaCertidoes(List<BlocoJulgamentoDTO> blocos) {
		boolean retorno = true;
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			if(!blocoDTO.getBloco().isCertidaoPresente() || blocoDTO.getBloco().isCertidaoAssinada()) {
				retorno = false;
				break;
			}
		}
		return retorno;
	}
	
	public boolean verificarAssinaturaCertidoes(List<BlocoJulgamentoDTO> blocos) {
		boolean retorno = false;
		for (BlocoJulgamentoDTO blocoDTO : blocos){
			if(blocoDTO.getBloco().isCertidaoAssinada()) {
				retorno = true;
				break;
			}
		}
		return retorno;
	}
	
	
	public OrgaoJulgador recuperarRelator(List<ProcessoTrf> processos) {
		OrgaoJulgador relator = null;
		for (ProcessoTrf processo : processos){
			if(relator == null) {
				relator = processo.getOrgaoJulgador();
			} else {
				if(!relator.equals(processo.getOrgaoJulgador()) ) {
					relator = null;
					break;
				}
			}
		}
		return relator;
	}
	
	public TipoVoto recuperarTipoVoto(List<ProcessoTrf> processos, Sessao sessao) throws PJeBusinessException {
		TipoVoto tipoVoto = null;
		for (ProcessoTrf processo : processos){
			SessaoProcessoDocumentoVoto voto = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoAntecipado(sessao, processo, processo.getOrgaoJulgador());
			if(voto != null) {
				if(tipoVoto == null) {
					tipoVoto = voto.getTipoVoto();
				} else {
					if(!tipoVoto.equals(voto.getTipoVoto())) {
						tipoVoto = null;
						break;
					}
				}
			} else {
				throw new PJeBusinessException("julgamentoBloco.excecaoSemVoto");
			}
		}
		return tipoVoto;
	}
	
	public boolean validarProcessosMesmaRelatoria(List<ProcessoTrf> processos, BlocoJulgamento bloco ) {
		boolean retorno = false;
		OrgaoJulgador relator = recuperarRelator(processos);
		if(relator != null && relator.equals(bloco.getOrgaoJulgadorRelator())) {
			retorno = true;
		}
		return retorno;
	}
	
	
	public BlocoJulgamento criarBloco(DadosBlocoJulgamentoDTO dto) throws Exception {
		BlocoJulgamento bloconovo = recuperarNovoBlocoJulgamento(dto.getNomeBloco(), dto.getSessao());
		bloconovo.setPropostaVoto(dto.getPropostaVoto());
		bloconovo.setOrgaoJulgadorRelator(dto.getOrgaoJulgadorRelator());
		bloconovo.setAgruparOrgaoJulgador(dto.getAgruparOrgaoJulgador());
		if(dto.getAgruparOrgaoJulgador()) {
			OrgaoJulgador relatorBloco = recuperarRelator(dto.getProcessos());
			if(relatorBloco != null && relatorBloco.equals(dto.getOrgaoJulgadorRelator())) {
				SessaoProcessoDocumentoVoto votoRelatorPrimeiroProcesso = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVotoAntecipado(dto.getSessao(), dto.getProcessos().get(0), dto.getOrgaoJulgadorRelator());
				if(votoRelatorPrimeiroProcesso != null && votoRelatorPrimeiroProcesso.getTipoVoto() != null) {
					bloconovo.setVotoRelator(votoRelatorPrimeiroProcesso.getTipoVoto());
				} else {
					throw new PJeBusinessException("Há processos sem voto do relator na lista!");
				}
			} else {
				throw new PJeBusinessException("Os processos selecionados não são da mesma relatoria!");
			}
		} else {
			bloconovo.setVotoRelator(dto.getTipoVotoRelator());
		}
		this.persistAndFlush(bloconovo);
		ComponentUtil.getProcessoBlocoManager().adicionarProcessosBlocos(bloconovo, dto.getProcessos(), false);
		if(dto.getAgruparOrgaoJulgador() != null && dto.getAgruparOrgaoJulgador()) {
			ComponentUtil.getVotoBlocoManager().registrarVotoBloco(dto.getOrgaoJulgadorRelator(), dto.getOrgaoJulgadorRelator(), bloconovo);
		} 
		return bloconovo;
	}
	
	public void registrarVotacaoUnanime(BlocoJulgamento bloco, List<BlocoComposicao> listComposicaoBloco) throws PJeBusinessException {
		bloco.setVotacaoRegistrada(true);
		ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
		TipoVoto acompanhaRelator = ComponentUtil.getTipoVotoManager().recuperaAcompanhaRelator();
		for(BlocoComposicao votante: listComposicaoBloco){
			List<SessaoPautaProcessoTrf> processosPautados = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperar(bloco);
			for (SessaoPautaProcessoTrf processoPautado : processosPautados){
				if(processoPautado.getProcessoTrf().getOrgaoJulgador().equals(votante.getOrgaoJulgador())) {
					continue;
				} else {
					ComponentUtil.getSessaoProcessoDocumentoVotoManager().registrarVotacao(processoPautado, votante.getOrgaoJulgador(), processoPautado.getProcessoTrf().getOrgaoJulgador(), acompanhaRelator, false);
				}
				processoPautado.setOrgaoJulgadorVencedor(processoPautado.getProcessoTrf().getOrgaoJulgador());
				ComponentUtil.getSessaoPautaProcessoTrfManager().mergeAndFlush(processoPautado);
			}
		}
	}
	
	public void atualizarSessaoProcessoDocumentoVoto(int idTipoVoto, String conteudo, BlocoJulgamento bloco,OrgaoJulgador orgao, TipoProcessoDocumento tipoDoc) throws PJeBusinessException {
		List <ProcessoBloco> processos = ComponentUtil.getProcessoBlocoManager().recuperarProcessos(bloco);
		for(ProcessoBloco processo: processos) {
			SessaoProcessoDocumentoVoto processoVoto = ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(bloco.getSessao(), processo.getProcessoTrf(), orgao);
			TipoVoto tipoVoto = ComponentUtil.getTipoVotoManager().findById(idTipoVoto);
			if(processoVoto != null) {
				processoVoto.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
				processoVoto.setTipoVoto(tipoVoto);
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().mergeAndFlush(processoVoto);
			} else {
				processoVoto = new SessaoProcessoDocumentoVoto();
				processoVoto.setTipoVoto(tipoVoto);
				processoVoto.setLiberacao(true);
				processoVoto.setOrgaoJulgador(orgao);
				processoVoto.setSessao(bloco.getSessao());
				processoVoto.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
				ProcessoDocumento pdNovo = ComponentUtil.getProcessoDocumentoManager().registrarProcessoDocumento(conteudo, "Voto", tipoDoc, processo.getProcessoTrf());
				processoVoto.setProcessoDocumento(pdNovo);
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().persistAndFlush(processoVoto);
			}
		}
	}
	
	public void atualizarSessaoProcessoDocumento(String conteudo, BlocoJulgamento bloco,TipoProcessoDocumento tipoSelecionado, OrgaoJulgador orgao) throws PJeBusinessException {
		List <ProcessoBloco> processos = ComponentUtil.getProcessoBlocoManager().recuperarProcessos(bloco);
		for(ProcessoBloco processo: processos) {
			SessaoProcessoDocumento sessaoDocumento = ComponentUtil.getSessaoProcessoDocumentoManager().recuperar(bloco.getSessao(), processo.getProcessoTrf(), orgao, tipoSelecionado);
			if(sessaoDocumento != null) {
				sessaoDocumento.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(conteudo);
				ComponentUtil.getSessaoProcessoDocumentoManager().mergeAndFlush(sessaoDocumento);
			} else {
				sessaoDocumento = new SessaoProcessoDocumento();
				sessaoDocumento.setLiberacao(true);
				sessaoDocumento.setOrgaoJulgador(orgao);
				sessaoDocumento.setSessao(bloco.getSessao());
				sessaoDocumento.setTipoInclusao(TipoInclusaoDocumentoEnum.S);
				ProcessoDocumento pdNovo = ComponentUtil.getProcessoDocumentoManager().registrarProcessoDocumento(conteudo, "Voto", tipoSelecionado, processo.getProcessoTrf());
				sessaoDocumento.setProcessoDocumento(pdNovo);
				ComponentUtil.getSessaoProcessoDocumentoManager().persistAndFlush(sessaoDocumento);
			}
		}
	}
}