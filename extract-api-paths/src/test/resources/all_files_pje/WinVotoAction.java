package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.pje.bean.VotoAcompanhadoBean;
import br.com.infox.pje.list.VotoAcompanhadoSecretarioList;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoComposicao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumentoVoto;
import br.jus.pje.nucleo.entidades.TipoVoto;

@Name(WinVotoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class WinVotoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "winVotoAction";
	
	private boolean iniciado;	
	private SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao;
	private SessaoProcessoDocumentoVoto sessaoProcessoDocumentoVoto;
	private List<TipoVoto> tiposVoto = new ArrayList<TipoVoto>();
    private VotoAcompanhadoSecretarioList votoAcompanhadoList;
    private List<VotoAcompanhadoBean> votoAcompanhadoBeanList;
    private HashMap<VotoAcompanhadoBean, Boolean> checkAcompanhamentoMap = new HashMap<VotoAcompanhadoBean, Boolean>(0);

    private SessaoProcessoDocumentoVoto buscarVoto(SessaoPautaProcessoComposicao sppc) {
		return ComponentUtil.getSessaoProcessoDocumentoVotoManager().recuperarVoto(sppc.getSessaoPautaProcessoTrf().getSessao(), sppc.getSessaoPautaProcessoTrf().getProcessoTrf(), sppc.getOrgaoJulgador());
	}
	
	public void iniciarVotacao(SessaoPautaProcessoComposicao sessaoPautaProcessoComposicao) {
		this.sessaoPautaProcessoComposicao = sessaoPautaProcessoComposicao;
		this.sessaoProcessoDocumentoVoto = buscarVoto(sessaoPautaProcessoComposicao);
		
		if (this.sessaoProcessoDocumentoVoto == null) {
			this.sessaoProcessoDocumentoVoto = new SessaoProcessoDocumentoVoto();
			this.sessaoProcessoDocumentoVoto.setOrgaoJulgador(sessaoPautaProcessoComposicao.getOrgaoJulgador());
			this.sessaoProcessoDocumentoVoto.setProcessoTrf(sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getProcessoTrf());
			this.sessaoProcessoDocumentoVoto.setSessao(sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getSessao());
		}

		votoAcompanhadoList = new VotoAcompanhadoSecretarioList();
				
		carregarTiposVoto();
		onChangeTipoVoto();
		
		iniciado = true;
	}

	private void carregarTiposVoto() {	
		if (isVotoRelator()) {
			this.tiposVoto = ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator();
		}
		else {
	        this.tiposVoto = ComponentUtil.getTipoVotoManager().tiposVotosVogais();
		}
	}

	public SessaoProcessoDocumentoVoto getSessaoProcessoDocumentoVoto() {
		return sessaoProcessoDocumentoVoto;
	}

	public SessaoPautaProcessoComposicao getSessaoPautaProcessoComposicao() {
		return sessaoPautaProcessoComposicao;
	}
	
	public List<TipoVoto> getTiposVoto() {
		return tiposVoto;
	}
	
	public boolean isVotoRelator() {
		OrgaoJulgador ojProcesso = getSessaoPautaProcessoComposicao().getSessaoPautaProcessoTrf().getProcessoTrf().getOrgaoJulgador(); 
		OrgaoJulgador ojVoto = getSessaoPautaProcessoComposicao().getOrgaoJulgador();
		return (ojProcesso.equals(ojVoto));
	}
	
	public void onChangeTipoVoto() {
    	votoAcompanhadoBeanList = new ArrayList<VotoAcompanhadoBean>();
    	List<SessaoProcessoDocumentoVoto> listVotoDivergente = votoAcompanhadoList.list(); 
    	List<SessaoProcessoDocumentoVoto> listVotoDivergenteAux = new ArrayList<SessaoProcessoDocumentoVoto>();
    	
		for (SessaoProcessoDocumentoVoto votoAcompanhado : listVotoDivergente) {
			if (votoAcompanhado.getOrgaoJulgador().equals(votoAcompanhado.getOjAcompanhado())){
				listVotoDivergenteAux.add(votoAcompanhado);
			}
		}
        for (SessaoProcessoDocumentoVoto spdv : listVotoDivergenteAux) {
        	VotoAcompanhadoBean vab = new VotoAcompanhadoBean(spdv, false);
        	if(sessaoProcessoDocumentoVoto != null && spdv.getOrgaoJulgador().equals(sessaoProcessoDocumentoVoto.getOjAcompanhado())){
        		vab.setCheck(true);
        	} else{
        		vab.setCheck(false);
        	}
        	votoAcompanhadoBeanList.add(vab);
        }
        
        carregarCheckAcompanhamentoMapFromBean();
	}
	
	/**
	 * A partir da listagem de bean negocial contendo informações de acompanhamento de órgão julgador,  
	 * carrega map usado para bind de checkbox relacionado ao órgão julgador (ou voto) acompanhado.
	 */
	private void carregarCheckAcompanhamentoMapFromBean() {
		checkAcompanhamentoMap.clear();
		for (VotoAcompanhadoBean acompanhamento : votoAcompanhadoBeanList){
			checkAcompanhamentoMap.put(acompanhamento, acompanhamento.getCheck());
		}
	}
	
	/**
	 * A partir de um map contendo os estados de checkbox relacionado ao órgão julgador (ou voto) acompanhado,
	 * atualiza tal informação de acompanhamento na bean de negócio.
	 */
	private void atualizarVotoAcompanhadoBeanFromMap() {
		for (VotoAcompanhadoBean acompanhamento : votoAcompanhadoBeanList){
			acompanhamento.setCheck(checkAcompanhamentoMap.get(acompanhamento));
		}
	}
	

	public VotoAcompanhadoSecretarioList getVotoAcompanhadoList() {
		return votoAcompanhadoList;
	}

	public List<VotoAcompanhadoBean> getVotoAcompanhadoBeanList() {
		return votoAcompanhadoBeanList;
	}

	public boolean isIniciado() {
		return iniciado;
	}
	
	
	public HashMap<VotoAcompanhadoBean, Boolean> getCheckAcompanhamentoMap() {
		return checkAcompanhamentoMap;
	}

	public void setCheckAcompanhamentoMap(
			HashMap<VotoAcompanhadoBean, Boolean> checkAcompanhamentoMap) {
		this.checkAcompanhamentoMap = checkAcompanhamentoMap;
	}

	public void atualizarVoto() {
		try {
			atualizarVotoAcompanhadoBeanFromMap();
			
			if (sessaoProcessoDocumentoVoto.getProcessoDocumento() == null || sessaoProcessoDocumentoVoto.getProcessoDocumento().getIdProcessoDocumento() == 0) {	
				sessaoProcessoDocumentoVoto.setOrgaoJulgador(getSessaoPautaProcessoComposicao().getOrgaoJulgador());
				sessaoProcessoDocumentoVoto.setSessao(getSessaoPautaProcessoComposicao().getSessaoPautaProcessoTrf().getSessao());
				sessaoProcessoDocumentoVoto.setLiberacao(true);
			}

			if (sessaoProcessoDocumentoVoto.getTipoVoto().getContexto().equals("C")
					|| sessaoProcessoDocumentoVoto.getTipoVoto().getContexto().equals("P")) {
				sessaoProcessoDocumentoVoto.setOjAcompanhado(sessaoProcessoDocumentoVoto.getProcessoTrf().getOrgaoJulgador());
				sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(false);
			} else {
				int qtdCheck = 0;
				
				VotoAcompanhadoBean acompanhado = null;
				
				for (VotoAcompanhadoBean voto : votoAcompanhadoBeanList) {
					if (voto.getCheck()) {
						qtdCheck++;
						acompanhado = voto;
					}
				}
				if (qtdCheck == 1) {
					sessaoProcessoDocumentoVoto.setOjAcompanhado(acompanhado.getSessaoProcessoDocumentoVoto().getOrgaoJulgador());
				} else {
					sessaoProcessoDocumentoVoto.setOjAcompanhado(sessaoProcessoDocumentoVoto.getOrgaoJulgador());
				}
				sessaoProcessoDocumentoVoto.setImpedimentoSuspeicao(false);
			}
			
			if(sessaoProcessoDocumentoVoto.getProcessoDocumento() != null && sessaoProcessoDocumentoVoto.getProcessoDocumento().getIdProcessoDocumento() == 0){
				sessaoProcessoDocumentoVoto.setProcessoDocumento(null);
			}
			
			sessaoProcessoDocumentoVoto.setDtVoto(new Date());

			if (sessaoProcessoDocumentoVoto.getIdSessaoProcessoDocumento() == 0) {			
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().persist(sessaoProcessoDocumentoVoto);
			} else {
				ComponentUtil.getSessaoProcessoDocumentoVotoManager().update(sessaoProcessoDocumentoVoto);
			}

			PopUpVotoSessaoAction popUpVotoSessaoAction = (PopUpVotoSessaoAction)Component.getInstance(PopUpVotoSessaoAction.class);
			popUpVotoSessaoAction.getMapVotos().put(sessaoProcessoDocumentoVoto.getOrgaoJulgador().getIdOrgaoJulgador(),sessaoProcessoDocumentoVoto);
			ComponentUtil.getSessaoProcessoDocumentoVotoManager().flush();
			
			ComponentUtil.getDerrubadaVotoManager().analisarTramitacaoFluxoVotoDerrubado(sessaoProcessoDocumentoVoto);	
			
			setaMaioriaVotacao();
			
		} 
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao atualizar o voto: {0}", e.getLocalizedMessage());
		}		
	}

	public void atualizaMapaOJAcompanhado(VotoAcompanhadoBean selecionado){
		for(VotoAcompanhadoBean bean : checkAcompanhamentoMap.keySet()){
			if(!bean.equals(selecionado)){
				checkAcompanhamentoMap.put(bean,false);
			}
		}
	}
	
	private void setaMaioriaVotacao() {		
		OrgaoJulgador ojMaioria = ComponentUtil.getSessaoProcessoDocumentoVotoManager().contagemMaioriaVotacao(sessaoProcessoDocumentoVoto.getSessao(), sessaoProcessoDocumentoVoto.getProcessoTrf());
		SessaoPautaProcessoTrf sppt = getSessaoPautaProcessoComposicao().getSessaoPautaProcessoTrf();
	
		if (ojMaioria != null) {			
			sppt.setOrgaoJulgadorVencedor(ojMaioria);
		} else {
			sppt.setOrgaoJulgadorVencedor(sessaoProcessoDocumentoVoto.getProcessoTrf().getOrgaoJulgador());
		}
		
		try {
			ComponentUtil.getSessaoPautaProcessoTrfManager().alterar(sppt);
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao atualizar o voto: {0}", e.getLocalizedMessage());
		}
	}
	
	public String obterMagistradoVotanteComposicao(OrgaoJulgador orgaoJulgador){
		String nomeMagistradoVotante = "";
		for(SessaoPautaProcessoComposicao componente : sessaoPautaProcessoComposicao.getSessaoPautaProcessoTrf().getSessaoPautaProcessoComposicaoList()){
			if (componente.getOrgaoJulgador().equals(orgaoJulgador)){
				nomeMagistradoVotante = componente.getMagistradoPresente().getNome();
				break;
			}
		}
		return nomeMagistradoVotante;
	}
}
