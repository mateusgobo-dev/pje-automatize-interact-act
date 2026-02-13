package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.visao.beans.DebitoTrabalhistaBean;
import br.jus.csjt.pje.business.service.DebitoTrabalhistaService;
import br.jus.pje.jt.entidades.DebitoTrabalhista;
import br.jus.pje.jt.entidades.DebitoTrabalhistaHistorico;
import br.jus.pje.jt.entidades.MotivoAlteracaoDebitoTrabalhista;
import br.jus.pje.jt.entidades.SituacaoDebitoTrabalhista;
import br.jus.pje.jt.entidades.TipoOperacaoEnum;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;

/**
 * Action responsável pela edição de débitos trabalhistas
 * 
 * @author Estevão Mognatto
 */
@Name(DebitoTrabalhistaEdicaoAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class DebitoTrabalhistaEdicaoAction implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6389524939281132632L;

	public static final String NAME = "debitoTrabalhistaEdicaoAction";

	private List<DebitoTrabalhistaHistorico> historicoDebitoTrabalhista;
	private List<MotivoAlteracaoDebitoTrabalhista> listaMotivoAlteracaoDebitoTrabalhista;

	private DebitoTrabalhista debitoTrabalhista;
	private String motivoSelecionado;

	private boolean ocorreuErro = false;

	/**
	 * Método que salva o debito trabalhista a ser alterado e obtem a lista de
	 * motivos de alteração do banco de dados, salvando estas informações para
	 * serem utilizadas na Interface de Alteração de debitos trabalhistas(um
	 * modal).
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public void alterarDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		this.debitoTrabalhista = debitoTrabalhista;

		if (listaMotivoAlteracaoDebitoTrabalhista == null) {

			listaMotivoAlteracaoDebitoTrabalhista = debitoTrabalhistaService.oterMotivosAlteracaoDebitoTrabalhista();
		}

	}

	/**
	 * Exclui um débito trabalhista e grava a operação correspondente no banco
	 * de dados.
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public void excluirDebitoTrabalhista() {

		excluirDebitoTrabalhista(debitoTrabalhista);

	}

	/**
	 * Exclui um débito trabalhista e grava a operação correspondente no banco
	 * de dados.
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public void excluirDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		DebitoTrabalhistaAction debitoTrabalhistaAction = ComponentUtil.getComponent(DebitoTrabalhistaAction.NAME);

		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();

		SituacaoDebitoTrabalhista situacaoDebitoTrabalhistaNegativa = debitoTrabalhistaService
				.obterSituacaoDebitoTrabalhistaPorDescricao("Negativa");

		// Adiciona entrada no historico
		DebitoTrabalhistaHistorico debitoTrabalhistaHistorico = new DebitoTrabalhistaHistorico();
		ProcessoParte processoParte = debitoTrabalhista.getProcessoParte();

		debitoTrabalhistaHistorico.setDataAlteracao(new Date());
		debitoTrabalhistaHistorico.setOperacao(TipoOperacaoEnum.E);
		debitoTrabalhistaHistorico.setProcessoParte(processoParte);
		debitoTrabalhistaHistorico.setSituacaoDebitoTrabalhista(situacaoDebitoTrabalhistaNegativa);
		debitoTrabalhistaHistorico.setUsuarioResponsavel(pessoaLogada);

		// Envia para BNDT
		String retornoEnvioBndt = debitoTrabalhistaService.enviarXMLDebitoTrabalhistaOnLine(debitoTrabalhistaHistorico);

		if (retornoEnvioBndt.equals("")) {

			// Grava no Debito Trabalhista Histórico
			debitoTrabalhistaService.gravarDebitoTrabalhistaHistorico(debitoTrabalhistaHistorico);

			// Exclui o debito propriamente dito
			debitoTrabalhistaService.excluirDebitoTrabalhista(debitoTrabalhista);
			// Atualiza as listas de débitos trabalhistas			
			debitoTrabalhistaAction.getListaDebitoTrabalhistaCadastrado().remove(debitoTrabalhista);
			DebitoTrabalhistaBean dtb = new DebitoTrabalhistaBean(processoParte);
			debitoTrabalhistaAction.getListaDebitoTrabalhistaSemSituacao().add(dtb);
			//Atualiza o checkBox de seleção de todos os débitos trabalhistas
			debitoTrabalhistaAction.setCheckAllDebitoTrabalhista(Boolean.FALSE);

			// Lança o moviemnto correspondente
			debitoTrabalhistaService.lancarMovimentosDebitoTrabalhista(debitoTrabalhistaHistorico);

		} else {

			ocorreuErro = true;
			FacesMessages.instance().add(Severity.ERROR, retornoEnvioBndt);
		}

		if (!ocorreuErro) {

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Débito Trabalhista excluído com sucesso!");

		}

	}

	/**
	 * Obtem o histórico de débitos trabalhistas para uma parte (encapsulada em
	 * um objeto DebitoTrabalhista)
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public void obterHistoricoDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		this.debitoTrabalhista = debitoTrabalhista;

		historicoDebitoTrabalhista = debitoTrabalhistaService.obterHistoricoDebitoTrabalhista(debitoTrabalhista
				.getProcessoParte());

	}

	public void limparGrid() {

		listaMotivoAlteracaoDebitoTrabalhista = null;
		motivoSelecionado = null;

	}

	/**
	 * Obtem os motivos cadastrados no banco de dados como uma string separadas
	 * por vírgulas.
	 * 
	 * @param debitoTrabalhista
	 * 
	 * @author Estevão Mognatto
	 */
	public String getListaMotivoAlteracaoDebitoTrabalhistaString() {

		StringBuilder listaMotivoString = new StringBuilder();

		for (MotivoAlteracaoDebitoTrabalhista motivoAlteracaoDebitoTrabalhista : listaMotivoAlteracaoDebitoTrabalhista) {

			if (motivoAlteracaoDebitoTrabalhista.getSituacao().getIdSituacaoDebitoTrabalhista() != debitoTrabalhista
					.getSituacaoDebitoTrabalhista().getIdSituacaoDebitoTrabalhista()) {
				listaMotivoString.append(motivoAlteracaoDebitoTrabalhista.getDescricao());
				listaMotivoString.append(",");
			}
		}

		String subStringSemUltimaVirgula = listaMotivoString.substring(0, listaMotivoString.length() - 1);

		return subStringSemUltimaVirgula;

	}

	/**
	 * Método utilizado na funcionalidade alterar debito trabalhista, verifica
	 * se a situação do motivo selecionado coresponde com a situação atual para
	 * uma parte, se a mesma não corresponder, altera a situação atual e insere
	 * o aperação realizada no histórico.
	 * 
	 * @author Estevão Mognatto
	 */
	public void atualizarDebitoTrabalhista() {

		ocorreuErro = false;

		DebitoTrabalhistaService debitoTrabalhistaService = ComponentUtil.getComponent(DebitoTrabalhistaService.NAME);

		MotivoAlteracaoDebitoTrabalhista motivoDebitoTrabalhistaSelecionado = debitoTrabalhistaService
				.obterMotivoDebitoTrabalhistaPorDescricao(motivoSelecionado);

		Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();

		// Tem que inserir uma entrada na tabela de historico

		DebitoTrabalhistaHistorico debitoTrabalhistaHistorico = new DebitoTrabalhistaHistorico();
		debitoTrabalhistaHistorico.setDataAlteracao(new Date());
		debitoTrabalhistaHistorico.setMotivo(motivoDebitoTrabalhistaSelecionado);
		debitoTrabalhistaHistorico.setOperacao(TipoOperacaoEnum.A);
		debitoTrabalhistaHistorico.setProcessoParte(debitoTrabalhista.getProcessoParte());
		debitoTrabalhistaHistorico.setSituacaoDebitoTrabalhista(motivoDebitoTrabalhistaSelecionado.getSituacao());
		debitoTrabalhistaHistorico.setUsuarioResponsavel(pessoaLogada);

		// Envia para BNDT
		String retornoEnvioBndt = debitoTrabalhistaService.enviarXMLDebitoTrabalhistaOnLine(debitoTrabalhistaHistorico);

		if (retornoEnvioBndt.equals("")) {

			// Verifica se a situação do motivo selecionado coresponde com a
			// situação atual
			if (motivoDebitoTrabalhistaSelecionado.getSituacao().getIdSituacaoDebitoTrabalhista() != debitoTrabalhista
					.getSituacaoDebitoTrabalhista().getIdSituacaoDebitoTrabalhista()) {

				// Tem que atualizar a situação na tabela DebitoTrabalhista
				SituacaoDebitoTrabalhista novaSituacaoDebitoTrabalhista = motivoDebitoTrabalhistaSelecionado
						.getSituacao();
				debitoTrabalhista.setSituacaoDebitoTrabalhista(novaSituacaoDebitoTrabalhista);

				// Atualiza o DebitoTrabalhista com a nova situação
				debitoTrabalhistaService.gravarDebitoTrabalhista(debitoTrabalhista);

			}

			// Insere o debito trabalhuista historico
			debitoTrabalhistaService.gravarDebitoTrabalhistaHistorico(debitoTrabalhistaHistorico);

			// Lança o moviemnto correspondente
			debitoTrabalhistaService.lancarMovimentosDebitoTrabalhista(debitoTrabalhistaHistorico);

		} else {

			ocorreuErro = true;
			FacesMessages.instance().add(Severity.ERROR, retornoEnvioBndt);
		}

		// Limpa o motivo selecionado
		motivoSelecionado = null;

		if (!ocorreuErro) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Débito Trabalhista alterado com sucesso!");
		}
	}

	public void setHistoricoDebitoTrabalhista(List<DebitoTrabalhistaHistorico> historicoDebitoTrabalhista) {
		this.historicoDebitoTrabalhista = historicoDebitoTrabalhista;
	}

	public List<DebitoTrabalhistaHistorico> getHistoricoDebitoTrabalhista() {
		return historicoDebitoTrabalhista;
	}

	public void setMotivoSelecionado(String motivoSelecionado) {
		this.motivoSelecionado = motivoSelecionado;
	}

	public String getMotivoSelecionado() {
		return motivoSelecionado;
	}

	public void setListaMotivoAlteracaoDebitoTrabalhista(
			List<MotivoAlteracaoDebitoTrabalhista> listaMotivoAlteracaoDebitoTrabalhista) {
		this.listaMotivoAlteracaoDebitoTrabalhista = listaMotivoAlteracaoDebitoTrabalhista;
	}

	public List<MotivoAlteracaoDebitoTrabalhista> getListaMotivoAlteracaoDebitoTrabalhista() {
		return listaMotivoAlteracaoDebitoTrabalhista;
	}

	public void setDebitoTrabalhista(DebitoTrabalhista debitoTrabalhista) {
		this.debitoTrabalhista = debitoTrabalhista;
	}

	public DebitoTrabalhista getDebitoTrabalhista() {
		return debitoTrabalhista;
	}

	public void setOcorreuErro(boolean ocorreuErro) {
		this.ocorreuErro = ocorreuErro;
	}

	public boolean isOcorreuErro() {
		return ocorreuErro;
	}

}
