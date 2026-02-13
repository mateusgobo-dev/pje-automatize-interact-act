package br.com.infox.pje.action;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.csjt.pje.business.pdf.XhtmlParaPdf;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.util.StringUtil;
import org.jboss.seam.security.Identity;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.util.DateUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import java.util.List;

@Name(RelacaoJulgamentoAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class RelacaoJulgamentoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "relacaoJulgamentoAction";
	
	private static final LogProvider log = Logging.getLogProvider(RelacaoJulgamentoAction.class);

	public boolean existeAcordao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		return ComponentUtil.getSessaoProcessoDocumentoManager().existeAcordaoJuntado(
				sessaoPautaProcessoTrf.getSessao(), sessaoPautaProcessoTrf.getProcessoTrf());
	}
	
	public ProcessoDocumentoBin votoRelatorBySessaoProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		SessaoProcessoDocumento spd = 
			ComponentUtil.getSessaoProcessoDocumentoManager().recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(
				sessaoPautaProcessoTrf.getSessao(), 
				sessaoPautaProcessoTrf.getProcessoTrf(), 
				ParametroUtil.instance().getTipoProcessoDocumentoVoto(), 
				sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		
		if (spd == null || !spd.getLiberacao() || spd.getProcessoDocumento() == null || spd.getProcessoDocumento().getProcessoDocumentoBin() == null) {
			ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
			pdb.setModeloDocumento(StringUtils.EMPTY);
			return pdb;
		} else {
			spd.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(StringUtil.cleanData(spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()));
			return spd.getProcessoDocumento().getProcessoDocumentoBin();
		}
	}
	
	public ProcessoDocumentoBin acordaoBySessaoProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		SessaoProcessoDocumento spd = 
				ComponentUtil.getSessaoProcessoDocumentoManager().recuperarSessaoProcessoDocumentoAtivoPorSessaoEhProcessoEhTipoProcessoDocumento(
					sessaoPautaProcessoTrf.getSessao(), 
					sessaoPautaProcessoTrf.getProcessoTrf(), 
					ParametroUtil.instance().getTipoProcessoDocumentoAcordao(), 
					sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador());
		
		if (spd == null || spd.getProcessoDocumento() == null || spd.getProcessoDocumento().getProcessoDocumentoBin() == null) {
			ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
			pdb.setModeloDocumento(StringUtils.EMPTY);
			return pdb;
		} else {
			spd.getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(StringUtil.cleanData(spd.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()));
			return spd.getProcessoDocumento().getProcessoDocumentoBin();
		}
	}
	
	public String getAdvogados(ProcessoTrf processoJudicial, int idPessoa) {
		PessoaAdvogadoManager pessoaAdvogadoManager = ComponentUtil.getComponent(PessoaAdvogadoManager.NAME);
		PessoaManager pessoaManager = ComponentUtil.getComponent(PessoaManager.NAME);
		TipoParteManager tipoParteManager = ComponentUtil.getComponent(TipoParteManager.NAME);
		
		int idTipoParteAdv = Integer.parseInt(ComponentUtil.getComponent(ParametroService.class).valueOf(Parametros.TIPOPARTEADVOGADO));
		TipoParte tipoAdvogado = tipoParteManager.findById(idTipoParteAdv);
		Pessoa pessoa = pessoaManager.findById(idPessoa);
		StringBuilder descricao = new StringBuilder();
		final String SEPARADOR = " - ";
		final String QUEBRA_LINHA = "\n";
		
		if (pessoa != null) {
			List<Pessoa> representantes = ComponentUtil.getComponent(ProcessoParteRepresentanteManager.class).recuperaRepresentantes(processoJudicial, pessoa, tipoAdvogado);
			if (representantes == null || representantes.isEmpty()) {
				return "Não informado";
			}
			for (Pessoa p : representantes) {
				PessoaAdvogado adv = pessoaAdvogadoManager.getPessoaAdvogado(p.getIdPessoa());
				if (adv != null) {
					descricao.append(p.getNome());
					descricao.append(SEPARADOR);
					String oabFormatado = adv.getOabFormatado();
					if (!Strings.isEmpty(oabFormatado)) {
						descricao.append("(" + oabFormatado + ")");
					}
					descricao.append(QUEBRA_LINHA);
				}
			}
		}
		
		return descricao.toString();
	}
	
	public String recuperarVotoRelator(SessaoPautaProcessoTrf sessaoPautaProcessoTrf){
  		StringBuilder retorno = new StringBuilder();
  		ProcessoDocumentoBin voto = votoRelatorBySessaoProcessoTrf(sessaoPautaProcessoTrf);
		if(voto == null || StringUtils.EMPTY.equals(voto)){
			retorno.append("Não informado");
		} else {
			if(ParametroUtil.instance().isPermissaoAcessoVotoPreSessaoSecretarioDecisorio() || ParametroUtil.instance().isPermissaoAcessoVotoPreSessaoSecretarioTodos()) {
				retorno.append(voto);
			} else {
				retorno.append("Voto não liberado");
			}
		}
		return retorno.toString();
	}


	/**
	 * Metodo responsavel por gerar e disponibilizar um relatorio em PDF
	 * dos processos em pauta de julgamento selecionados.
	 */
	public void gerarRelatorioEmPdf() {
		final FacesContext contexto = FacesContext.getCurrentInstance();
		final String nomeArquivo = "relatorio_" + System.currentTimeMillis() + ".pdf";
		final String caminhoRelatorio = "/Sessao/RelacaoJulgamento/report/relacaoJulgamentoPDF.xhtml";
		final byte[] relatorioEmBytes = XhtmlParaPdf.converterParaBytes(caminhoRelatorio);
		
		Contexts.getConversationContext().remove("org.jboss.seam.document.documentStore");
		HttpServletResponse response = (HttpServletResponse) contexto.getExternalContext().getResponse();
		response.setContentType("application/pdf");
		response.setHeader("Content-disposition", "filename=" + nomeArquivo);
		response.setContentLength(relatorioEmBytes.length);
		
		ServletOutputStream outputStream = null;
		try {
			outputStream = response.getOutputStream();
			outputStream.write(relatorioEmBytes, 0, relatorioEmBytes.length);
			outputStream.flush();
		} catch (Exception e) {
			log.error("Ocorreu erro ao tratar stream em relacaoJulgamentoaction.gerarRelatorioEmPdf(): " + e.getLocalizedMessage());
			throw new RuntimeException(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				log.error("Ocorreu erro ao fechar stream em relacaoJulgamentoaction.gerarRelatorioEmPdf(): " + e.getLocalizedMessage());
			}
		}
		contexto.responseComplete();
	}
	
	public boolean permiteRemoverProcessoPautaFechada(SessaoPautaProcessoTrf sessaoPauta) {
		boolean retorno = false;
		if(sessaoPauta != null) {
			

			
			retorno = (sessaoPauta.getSessao().getDataFechamentoPauta() == null && sessaoPauta.getSessao().getDataRealizacaoSessao() == null && ((sessaoPauta.getOrgaoJulgadorUsuarioInclusao() != null && sessaoPauta.getOrgaoJulgadorUsuarioInclusao().equals(Authenticator.getOrgaoJulgadorAtual())) || Authenticator.isPapelPermissaoSecretarioSessao() )) || 
					(sessaoPauta.getSessao().getDataFechamentoPauta() != null && DateUtil.isDataMaiorIgual(DateUtil.getDataAtual(), sessaoPauta.getSessao().getDataFechamentoPauta())
		   			   && TipoInclusaoEnum.PA.equals(sessaoPauta.getTipoInclusao()) && sessaoPauta.getSessao().getDataRealizacaoSessao() == null &&
		   			   ((sessaoPauta.getOrgaoJulgadorUsuarioInclusao() != null && sessaoPauta.getOrgaoJulgadorUsuarioInclusao().equals(Authenticator.getOrgaoJulgadorAtual()) && !sessaoPauta.getProcessoTrf().getClasseJudicial().getPauta()) ||
		   			   (Identity.instance().hasRole(Papeis.PERMITE_REMOVER_PROCESSO_PAUTA_FECHADA) && sessaoPauta.getSessao().getOrgaoJulgadorColegiado().equals(Authenticator.getOrgaoJulgadorColegiadoAtual()))));
		}
		return retorno;
	}
	
	public boolean exibeRemovidos(Sessao sessao) {
		List <ProcessoTrf> processosRemovidos = ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarRemovidos(sessao);
		return processosRemovidos != null && processosRemovidos.size() > 0;
	}

	public List<ProcessoTrf> recuperarProcessosRemovidos(Sessao sessao) {
		return ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarRemovidos(sessao);
	}
}