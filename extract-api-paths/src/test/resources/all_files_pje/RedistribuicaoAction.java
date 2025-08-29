/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.ApplicationContext;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.VinculacaoDependenciaEleitoralManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.VinculacaoDependenciaEleitoral;
import br.jus.pje.nucleo.enums.TipoDistribuicaoEnum;
import br.jus.pje.nucleo.enums.TipoRedistribuicaoEnum;

/**
 * Componente de controle de tela para a redistribuição. A implementação inicial atenderá apenas
 * ao caso de redistribuição de que trata issue [PJEII-5681].
 * 
 * @author eduardo.pereira
 *
 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-5681">PJEII-5681</a>
 */
@Name("redistribuicaoAction")
@Scope(ScopeType.CONVERSATION)
public class RedistribuicaoAction implements Serializable {
	
	private static final long serialVersionUID = 7742198191077258759L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private transient TramitacaoProcessualService tramitacaoProcessualService;
	
	@In(create=true)
	private transient DistribuicaoService distribuicaoService;
	
	@In
	private Conversation conversation;
	
	@In
	private ApplicationContext applicationContext;
	
	// FIXME: após realizar o refactoring para que esta classe se torne a
	// controller de toda a view de redistribuição (que esta
	// extremamente fragmentada e utilizando "Home's"), retirar esta injeção
	@In
	private transient ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome;
	
	// FIXME: após realizar o refactoring para que esta classe se torne a
	// controller de toda a view de redistribuição (que esta
	// extremamente fragmentada e utilizando "Home's"), retirar esta injeção
	@In
	private transient ProcessoTrfHome processoTrfHome;
	
	@In
	private VinculacaoDependenciaEleitoralManager vinculacaoDependenciaEleitoralManager;
	
	private boolean concluido = false;
	
	private ProcessoTrf processoJudicial;
	
	private TipoDistribuicaoEnum tipoDistribuicao;
	
	private TipoRedistribuicaoEnum tipoRedistribuicao;
	
	@Create
	public void init(){
		// FIXME: após realizar o refactoring para que esta classe se torne a
		// controller de toda a view de redistribuição (que esta
		// extremamente fragmentada e utilizando "Home's"), retirar este set
		setTipoRedistribuicao(processoTrfRedistribuicaoHome.getInTipoRedistribuicao());
		processoJudicial = tramitacaoProcessualService.recuperaProcesso();
	}
	
	public void confirmarRedistribuicao(){
		switch (tipoDistribuicao) {
		case EN:
			redistribuirPorEncaminhamento();
			break;
		case PP:
			redistribuirPorPrevencaoArt260();
			break;
		case S:
			redistribuirPorSorteio();
			break;
		default:
			throw new UnsupportedOperationException("Método ainda não implementado");
		}
	}

	/**
	 * Redistribui o processo pelo motivo de "encaminhamento". Caso o método
	 * seja chamado no âmbito da Justiça eleitoral, será averiguado se o
	 * processo se enquadra em prevenção do art.260 do CE, caso enquadre, será
	 * criada uma nova cadeia de processos baseada neste tipo de prevenção para
	 * o Órgão Julgador selecionado no formulário de encaminhamento. Se o
	 * processo não se enquadrar nas regras do art.260 do CE, a redistribuição
	 * realizada segue normalmente como é para todas as justiças.
	 */
	private void redistribuirPorEncaminhamento(){
		try {
			boolean jEeParametroPrevencaoAtivo = distribuicaoService.isJEeParametroPrevencaoAtivo();
			
			if(jEeParametroPrevencaoAtivo){
				boolean processoEnquadra260 = distribuicaoService.verificarEnquadramentoPEO(this.processoJudicial);
				if(!processoEnquadra260 || (processoEnquadra260 && !isCadeiaProcessosArt260CEExistente())){
					processoTrfRedistribuicaoHome.gravarRedistribuicaoTipoEncaminhamento();
				}
			} 
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Erro ao verificar enquadramento na prevenção do art.260 do CE!");
		}
	}
	
	/**
	 * Retorna se os parametros necessarios para trabalhar com o artigo 260 estao cadastrados e validos.
	 * @return True se habilita prevencao 260.
	 */
	public boolean isHabilitaBotaoRedistribuirJE(){
		return distribuicaoService.isJEeParametroPrevencaoAtivo();
	}
	
	/**
	 * Verifica a existência de cadeia de processos preventos criada para o
	 * processo em redistribuição. Em caso de redistribuição por encaminhamento,
	 * exibe na tela mensagem informativa ao usuário da impossibilidade de
	 * redistribuição por este Tipo/Motivo.
	 * 
	 * @return true se existente e false caso contrario
	 * @throws PJeBusinessException
	 */
	public boolean isCadeiaProcessosArt260CEExistente() throws PJeBusinessException{
		boolean resultado = Boolean.FALSE;
		VinculacaoDependenciaEleitoral vinculacao = processoJudicial.getComplementoJE().getVinculacaoDependenciaEleitoral();
		
		if(vinculacao != null && !vinculacao.getCargoJudicial().getOrgaoJulgador().equals(processoTrfRedistribuicaoHome.getOrgaoJulgadorRedistribuicao())){
			resultado = Boolean.TRUE;
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.ERROR, "erro.redistribuicao.encaminhamento.cadeia.260.existente", 
						vinculacao.getCargoJudicial().getOrgaoJulgador().getOrgaoJulgador(), vinculacao.getEleicao().getDescricao(), vinculacao.getEstadoMunicipio());
		}
		if(vinculacao != null && vinculacao.getCargoJudicial().getOrgaoJulgador().equals(processoTrfRedistribuicaoHome.getOrgaoJulgadorRedistribuicao())){
			resultado = Boolean.TRUE;
			facesMessages.clear();
			facesMessages.addFromResourceBundle(Severity.ERROR, "erro.redistribuicao.encaminhamento.cadeia.260.orgao.julgador.invalido", 
						vinculacao.getCargoJudicial().getOrgaoJulgador().getOrgaoJulgador());
		}
		return resultado;
	}
	
	private void redistribuirPorPrevencaoArt260(){
		long contagemRedistribuidos;
		try {
			logger.debug("Iniciando a redistribuição por prevenção do art. 260 do Código Eleitoral em relação ao processo [{0}] e seu(s) conexo(s)..", processoJudicial);
			contagemRedistribuidos = distribuicaoService.redistribuirPrevencaoRecursalEleitoral(processoJudicial, true,getTipoDistribuicao(),tipoRedistribuicao);
			concluido = true;
			if(conversation.isLongRunning()){
				conversation.end();
			}
			String orgaoJulgadorSorteado = (String) applicationContext.get("pje:redistribuicao:lote:orgaoJulgadorSorteado");  
			logger.debug("Redistribuição por prevenção do art. 260 do Código Eleitoral em relação ao processo [{0}] finalizada afetando [{1}] processos.", processoJudicial, contagemRedistribuidos);
			facesMessages.add(Severity.INFO, "Foi(ram) agendada(s) a(s) redistribuição(ões) para {0} processo(s). Orgão Julgador Sorteado: {1}", contagemRedistribuidos,orgaoJulgadorSorteado);
		} catch (PJeBusinessException e) {
			logger.error("Erro ao tentar realizar a redistribuição por prevenção eleitoral: {0}", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar redistribuir. Tente novamente mais tarde ou contate o administrador.");
		}catch (Exception e) {
			logger.error("Erro ao tentar realizar a redistribuição por prevenção eleitoral: {0}", e.getLocalizedMessage());
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar redistribuir. Tente novamente mais tarde ou contate o administrador.");
		}finally{
			applicationContext.remove("pje:redistribuicao:lote:orgaoJulgadorSorteado");
		}
	}
	
	//FIXME Este método delega suas ações para a classe ProcessoTrfRedistribuicaoHome, quando esta classe for o controller definitivo da redistribuição retirar esta delegação 
	private void redistribuirPorSorteio() {
		switch(getTipoRedistribuicao()){
		case I:
			processoTrfRedistribuicaoHome.gravarRedistribuicaoImpedimento();
			break;
		case J:
			processoTrfRedistribuicaoHome.gravarRedistribuicaoDeterminacaoJudicial();
			break;
		case O:
			processoTrfRedistribuicaoHome.gravarRedistribuicaoSuspeicaoRelator();
			break;
		default:
			facesMessages.add(Severity.ERROR, "Tipo de redistribuição invalido!");
			break;
		}
	}
	
	public TipoDistribuicaoEnum getTipoDistribuicao() {
		return tipoDistribuicao;
	}


	public void setTipoDistribuicao(TipoDistribuicaoEnum tipoDistribuicao) {
		this.tipoDistribuicao = tipoDistribuicao;
	}

	public TipoRedistribuicaoEnum getTipoRedistribuicao() {
		return tipoRedistribuicao;
	}

	public void setTipoRedistribuicao(TipoRedistribuicaoEnum tipoRedistribuicao) {
		this.tipoRedistribuicao = tipoRedistribuicao;
	}

	public boolean isConcluido() {
		return concluido;
	}
}
