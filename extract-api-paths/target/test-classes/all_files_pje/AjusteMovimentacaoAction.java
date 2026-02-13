package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.cliente.actions.ProcessoDocumentoBinAction;
import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.exception.ExclusaoDocumentoException;
import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoEventoList;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.visao.beans.ProcessoEventoBean;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

/**
 * Componente Action usado para interface entre a tela de Ajuste de Movimentos e
 * o Lancador de Movimentos.
 * 
 * @since 1.4.2
 * @category PJE-JT
 * @created 2011-08-25
 * @author Emmanuel S. Magalhães, Guilherme Bispo
 */

@Name(AjusteMovimentacaoAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class AjusteMovimentacaoAction implements Serializable{

	private static final long serialVersionUID = 1625115108667382887L;

	public static final String NAME = "ajusteMovimentacaoAction";
	ProcessoEventoList processoEventoList = ComponentUtil.getComponent(ProcessoEventoList.NAME);
	private String motivoExclusao = "";
	private Boolean opcaoExcluirArquivos = false;
	

	
	/**
	 * Método utilizado para exclusão de um ou mais movimentos
	 */
	public void excluirMovimentosSelecionados(boolean excluirArquivosDosMovimentos) {

		List<ProcessoEvento> movimentosExcluir = new ArrayList<ProcessoEvento>();
		List<ProcessoEventoBean> processoEventoBeanList = processoEventoList.getProcessoEventoBeanList();

		for (ProcessoEventoBean o : processoEventoBeanList) {

			if (o.getSelected()) {
				movimentosExcluir.add(o.getProcessoEvento());
			}
		}
		try{
			Util.beginTransaction();
			if (!movimentosExcluir.isEmpty()) {
				excluirMovimentos(movimentosExcluir, excluirArquivosDosMovimentos);
				refreshProcessoEventoList();
			} else {
				FacesMessages.instance().add(Severity.WARN, "É necessário informar algum movimento para exclusão!");
			}
			
		}catch (Exception e) {
			// Se for um erro de aplicação, a transação será "rollbackeada" e será apresentado a mensagem de erro ao usuário.
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, e.getMessage());
			Util.rollbackTransaction();
			refreshProcessoEventoList();
		}
	}

	/**
	 * Método utilizado para alteração de visibilidade de movimentos
	 */
	public void alteraVisibilidade() {
		List<ProcessoEventoBean> processoEventoBeanList = processoEventoList.getProcessoEventoBeanList();
		LancadorMovimentosService lancadorMovimentosService = ComponentUtil
				.getComponent(LancadorMovimentosService.NAME);

		for (ProcessoEventoBean processoEventoBean : processoEventoBeanList) {
			if (processoEventoBean.getVisibilidadeAlterada()) {
				processoEventoBean.getProcessoEvento().setVisibilidadeExterna(
						processoEventoBean.getVisibilidadeExterna());
				lancadorMovimentosService.alteraVisibilidade(processoEventoBean.getProcessoEvento());
			}
		}

		FacesMessages.instance().add(Severity.INFO, "Visibilidade(s) dos movimentos alterada(s) com sucesso!");
	}

	/**
	 * Validar se o usuário digitou um número de processo correto/existente e se
	 * o mesmo tem permissões para ajustar as movimentações do processo.
	 * 
	 * @author David/Emmanuel
	 * 
	 */
	public void verificarPermissaoConsultaProcesso() {
		boolean valido = false;

		ProcessoEventoList processoEventoList = ((ProcessoEventoList) ComponentUtil
				.getComponent(ProcessoEventoList.NAME));

		Query qSemFiltro = EntityUtil.createQuery("from ConsultaProcessoTrf o where o.numeroProcesso = :nrProcesso")
				.setParameter("nrProcesso", processoEventoList.getEntity().getProcesso().getNumeroProcesso());

		ControleFiltros.instance().iniciarFiltro();

		Query qComFiltro = EntityUtil.createQuery("from ConsultaProcessoTrf o where o.numeroProcesso = :nrProcesso")
				.setParameter("nrProcesso", processoEventoList.getEntity().getProcesso().getNumeroProcesso());

		if (qSemFiltro.getResultList().size() == 1) {
			if (qComFiltro.getResultList().size() == 1) {
				valido = true;
			} else {
				// nao tem permissao
				FacesMessages.instance().add(Severity.ERROR, "Usuário sem permissão para consultar este processo.");
			}
		} else {
			// nao existe o processo
			FacesMessages.instance().add(Severity.ERROR, "Processo não encontrado ou número inválido.");
		}

		if (!valido) {
			processoEventoList.newInstance();
		}
	}

	/**
	 * Método utilizado para habilitar botão "Excluir Movimentos". Ao menos um
	 * movimento deve estar selecionado para exclusão.
	 */
	public Boolean temSelecionados() {

		List<ProcessoEventoBean> processoEventoBeanList = processoEventoList.getProcessoEventoBeanList();

		for (ProcessoEventoBean o : processoEventoBeanList) {

			if (o.getSelected()) {
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	public String getMotivoExclusao() {
		return motivoExclusao;
	}

	public void setMotivoExclusao(String motivoExclusao) {
		this.motivoExclusao = motivoExclusao;
	}

	public Boolean getOpcaoExcluirArquivos() {
		return opcaoExcluirArquivos;
	}

	public void setOpcaoExcluirArquivos(Boolean opcaoExcluirArquivos) {
		this.opcaoExcluirArquivos = opcaoExcluirArquivos;
	}

	/**
	 * Regra define se o usuário tem permissão de excluir o documento.
	 * @param processoEvento
	 * @return
	 */
	public boolean permiteExcluirDocumento(ProcessoEvento processoEvento) {
		// Se for nulo, já retorna falso.
		if(processoEvento == null){
			return false;
		}
	
		ProcessoDocumentoBinAction processoDocumentoBinAction = ComponentUtil.getComponent("processoDocumentoBinAction");
		ProcessoDocumento processoDocumento = processoEvento.getProcessoDocumento();
		
		// Regra define se o usuário tem permissão de INATIVAR o documento.
		// Além de verificar se o usuário pode visualizar o documento, é verificado se o usuário tem permissão de INATIVAR na tela de visualização de documentos do processo.
		boolean permiteExcluirDocumento = permiteVisualizarDocumento(processoDocumento) && processoDocumentoBinAction.permiteExclusaoLogicaDocumento(processoDocumento);

		return permiteExcluirDocumento;
	}
	
	/**
	 * Regra que define se um arquivo poderá ser visualizado na tela de
	 * alteração dos movimentos.
	 * 
	 * @param processoDocumento
	 * @return permiteVisualizarDocumento
	 */
	public boolean permiteVisualizarDocumento(
			ProcessoDocumento processoDocumento) {
		boolean permiteVisualizarDocumento = (processoDocumento != null)
				&& (processoDocumento.getAtivo()
						&& !processoDocumento.getDocumentoSigiloso() && (processoDocumento
						.getTipoProcessoDocumento().getPublico() || Identity
						.instance().hasRole("servidor")));
		return permiteVisualizarDocumento;
	}
	
	/**
	 * Método responsável por fazer a verificação do usuário logado se o mesmo tem as permissoes
	 * devidas para controle dos movimentos e complementos das movimentações do processo.
	 * 
	 * @param processoTrf
	 * @return true se o usuário tem a permisão de acordo com as regras
	 */
	public boolean permiteVisualizarFuncionalidadeMovimentosProcesso(ProcessoTrf processoTrf){
		boolean permissao = false;

		Localizacao localizacaoFisicaUsuario = Authenticator.getLocalizacaoFisicaAtual();
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoUsuario = Authenticator.getOrgaoJulgadorColegiadoAtual();
		boolean isServidorExclusivoOJC = Authenticator.isServidorExclusivoColegiado();
		ProcessoTrfManager processoTrfManager = ComponentUtil.getComponent(ProcessoTrfManager.NAME);
		
		if((processoTrf != null) && 
			 (Authenticator.isMagistrado() || 
					Identity.instance().hasRole(Papeis.CONTROLE_VISIBILIDADE_MOVIMENTO_PROCESSO)) 
			&& processoTrfManager.isPessoaJuizoProcesso(processoTrf, localizacaoFisicaUsuario, orgaoJulgadorColegiadoUsuario, isServidorExclusivoOJC)){
			
			permissao = true;
		}
		return permissao;
	}
			

	/**
	 * Este método retorna true se pelo menos um dos movimentos selecionados
	 * contém arquivo que possa ser excluído.
	 */
	public boolean existemMovimentosSelecionadosComArquivosQuePossamSerExcluidos() {
		List<ProcessoEventoBean> processoEventoBeanList = processoEventoList
				.getProcessoEventoBeanList();
		for (ProcessoEventoBean o : processoEventoBeanList) {
			if (o.getSelected()) {
				if (permiteExcluirDocumento(o.getProcessoEvento())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Exclui os movimentos selecionados. Se algum movimento tiver algum arquivo
	 * relacionado o usuário pode excluir ou não os respectivos arquivos.
	 */
	public void excluirMovimentosSelecionados() {
		// Se o usuário decidir justificar a exclusão de arquivos, a
		// justificativa deve ser informada.
		if (existemMovimentosSelecionadosComArquivosQuePossamSerExcluidos()
				&& !isCampoPreenchido(getMotivoExclusao())
				&& getOpcaoExcluirArquivos()) {
			FacesMessages.instance().add(Severity.ERROR,
					"Erro: A justificativa da exclusão não informada.");
			return;
		}
		excluirMovimentosSelecionados(getOpcaoExcluirArquivos());
	}

	private boolean isCampoPreenchido(String campo) {
		return campo != null && !campo.trim().isEmpty();
	}

	public void refreshProcessoEventoList() {
		((ProcessoEventoList) ComponentUtil
				.getComponent(ProcessoEventoList.NAME)).refreshList();
	}
	
	/**
	 *  Exclui os movimentos selecionados pelo usuário.
	 *  Exclui os documentos relacionados aos movimentos se o usuário decidiu desta forma (excluirArquivosDosMovimentos = true)
	 * @param movimentosExcluir
	 * @param excluirArquivosDosMovimentos
	 * @throws Exception 
	 */
	private void excluirMovimentos(List<ProcessoEvento> movimentosExcluir, boolean excluirArquivosDosMovimentos) throws Exception {
		LancadorMovimentosService lancadorMovimentosService = ComponentUtil.getComponent(LancadorMovimentosService.NAME);
		// Exclui os movimentos selecionados pelo usuário.
		
		for (ProcessoEvento movimentoExcluir : movimentosExcluir) {
			//Exclui o movimento do usuário.
			lancadorMovimentosService.excluirMovimento(movimentoExcluir);
			if (excluirArquivosDosMovimentos && permiteExcluirDocumento(movimentoExcluir)) {
				excluirArquivoDoMovimento(movimentoExcluir, getMotivoExclusao());
			}
		}
		
		// Limpar dados
		setMotivoExclusao(null);
		setOpcaoExcluirArquivos(false);
		// Apresenta as mensagens de sucesso.
		FacesMessages.instance().clear();
		FacesMessages
				.instance()
				.add(Severity.INFO,
						(movimentosExcluir.size() > 1 ? "Movimentos excluídos com sucesso."
								: "Movimento excluído com sucesso."));
		if (excluirArquivosDosMovimentos) {
			FacesMessages.instance().add(Severity.INFO,
					"Documento(s) relacionado(s) excluído(s) com sucesso.");
		}
	}
	
	private void excluirArquivoDoMovimento(ProcessoEvento processoEvento, String motivoDaExclusao) throws ExclusaoDocumentoException {
		ProcessoDocumentoHome anexarDocumentos = ComponentUtil.getComponent(ProcessoDocumentoHome.NAME);
		ProcessoDocumento processoDocumento = processoEvento.getProcessoDocumento();
		if (processoDocumento != null && processoDocumento.getAtivo()) {
			anexarDocumentos.setInstance(processoDocumento);
			anexarDocumentos.getInstance().setMotivoExclusao(motivoDaExclusao);
			anexarDocumentos.gravarExclusaoDoDocumento();
		}
		// Limpar dados
		anexarDocumentos.clearInstance();
	}
}
