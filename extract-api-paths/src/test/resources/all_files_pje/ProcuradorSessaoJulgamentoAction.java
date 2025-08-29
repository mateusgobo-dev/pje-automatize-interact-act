package br.com.jt.pje.action;

import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.ItensLegendas;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;

@Name(ProcuradorSessaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class ProcuradorSessaoJulgamentoAction extends AbstractSessaoJulgamentoAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5215689230573801354L;
	//TODO extrair metodos comuns entre esse action e o do magistrado
	public static final String NAME = "procuradorSessaoJulgamentoAction";
	
	@Override
	public void iniciarLegenda() {
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[0], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[1], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[2], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[3], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[4], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[5], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[6], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[7], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[8], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[9], false);
		getMapLegenda().put(ItensLegendas.SIGLAS_PROCURADOR_LEGENDAS[10], false);
	}
	
	@Override
	protected char getSiglaPainel() {
		return 'P';
	}
	
	@Override
	public void assinarEmLote(){
		super.assinarEmLote();
		FacesMessages.instance().add(Severity.INFO, "Acórdão(s) assinado(s) com sucesso.");
	}
	
	public void addAllPodeAssinarAcordaoProcurador(){
		getListPautaSessao().clear();
		
		for(PautaSessao pautaSessao: getElaboracaoVotoList().getResultList()){
			if(podeAssinarAcordaoProcurador(pautaSessao, votoProcessoPauta(pautaSessao)) && !isAssinadoAcordaoProcurador(pautaSessao)){
				getListPautaSessao().add(pautaSessao);
				pautaSessao.setCheckBoxSelecionado(true);
			}
		}
		
		if(getListPautaSessao().size() == 0){
			FacesMessages.instance().add(Severity.INFO, "Não existem documentos a serem selecionados no momento.");
		}
		
	}
	
	public void assinar(){
		assinarProcessoDocumento();
		FacesMessages.instance().add(Severity.INFO, "Acórdão assinado com sucesso.");
	}

	
	protected void assinarProcessoDocumento() {
		ProcessoDocumento processoDocumentoJaExistente = processoDocumentoManager.getProcessoDocumento(TIPO_PROCESSO_DOCUMENTO_ACORDAO,
				getProcessoTrf().getProcesso());
		
		if(processoDocumentoJaExistente != null){
			processoDocumentoManager.inserirAssinaturaNoProcessoDocumentoBin(processoDocumentoJaExistente.getProcessoDocumentoBin(), 
					getCertChain(), getSignature(), new Date(), Authenticator.getPessoaLogada());
		}
		
	}
	
	public boolean isAssinadoAcordaoProcurador(PautaSessao pautaSessao){
		return pautaJulgamentoService.isAssinadoAcordaoProcurador(pautaSessao,Authenticator.getPessoaLogada(),TIPO_PROCESSO_DOCUMENTO_ACORDAO);
	}
	
	public boolean isAssinadoAcordaoProcurador(){
		return pautaJulgamentoService.isAssinadoAcordaoProcurador(getPautaSessao(),Authenticator.getPessoaLogada(),TIPO_PROCESSO_DOCUMENTO_ACORDAO);
	}

	@Factory(value="countProcessosPodeAssinarProcurador",scope=ScopeType.EVENT)
	public Integer countProcessosPodeAssinarProcurador(){
		
		Integer countProcessosPodeAssinarProcurador = 0;
		
		for(PautaSessao pautaSessao: getElaboracaoVotoList().getResultList()){
			if(podeAssinarAcordaoProcurador(pautaSessao, votoManager.getVotoProcessoByOrgaoJulgadorSessao(pautaSessao.getProcessoTrf(),
					pautaSessao.getProcessoTrf().getOrgaoJulgador(), pautaSessao.getSessao())) 
				&& !isAssinadoAcordaoProcurador(pautaSessao)){
				countProcessosPodeAssinarProcurador++;
			}
		}
		
		return countProcessosPodeAssinarProcurador;
	}
	
	public boolean podeAssinarAcordaoProcurador(PautaSessao pautaSesssao, Voto voto){
		if(pautaSesssao != null){
			return pautaJulgamentoService
			.podeAssinarAcordaoProcurador(pautaSesssao, voto);
		}
		return false;
	}
	
	public boolean podeAssinarAcordaoProcurador(){
		if(getPautaSessao() != null){
			return pautaJulgamentoService
			.podeAssinarAcordaoProcurador(getPautaSessao(), getVoto());
		}
		return false;
	}
	
	public boolean documentoJaAssinadoProcurador(PautaSessao pautaSesssao){
		
		return false;
	}
	
	public String[][] getItemsLegenda() {
		if(sessaoEncerrada() || sessaoFechada()){
			return ItensLegendas.LEGENDAS_PROCURADOR_SESSAO_ENCERRADA_ARRAY;
		}
		return ItensLegendas.LEGENDAS_PROCURADOR_ARRAY;
	}
	
	public void atualizarProcessoApregoado() {
		PautaSessao pautaProcessoApregoado = pautaSessaoManager.getPautaProcessoApregoadoBySessao(getSessao());
		if(pautaProcessoApregoado != null) {
			setIdProcesso(pautaProcessoApregoado.getProcessoTrf().getIdProcessoTrf());
			inicializar();
			carregarQuantidadeVotos();
		}
	}
}