package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.view.fluxo.WinVotoBlocoAction;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.VotoBloco;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaBlocoEnum;


@Name(PopUpVotoBlocoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PopUpVotoBlocoAction implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "popUpVotoBlocoAction";
	private Integer idBloco;
	private BlocoJulgamento bloco;
	private HashMap<String, Long> placar;
	private TipoVoto votoRelator;
	private List<VotoBloco> votos;
	private Map<Integer,VotoBloco> mapVotos = new HashMap<Integer, VotoBloco>(0);
	private List<BlocoComposicao> listComposicaoBloco;
	private List<OrgaoJulgador> listComposicaoOrgaoJulgador;

	public void inicializar() {
		if(getBloco() != null && bloco.getAgruparOrgaoJulgador()) {
			placar = getPlacar();
			if(bloco != null) {
				if(bloco.getVotoRelator() == null) {
					try {
						ComponentUtil.getBlocoJulgamentoManager().atualizarVotoRelator(getBloco());
					} catch (PJeBusinessException e) {
						FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
					}
				} 
				votoRelator = bloco.getVotoRelator();
				setVotos(ComponentUtil.getVotoBlocoManager().recuperarVotos(getBloco()));
				for(VotoBloco v : votos){
					if(v.getOrgaoJulgador() != null){
						mapVotos.put(v.getOrgaoJulgador().getIdOrgaoJulgador(),v);
					}
				}
				if (bloco != null && bloco.getOrgaoJulgadorVencedor() == null){
					try {
						ComponentUtil.getBlocoJulgamentoManager().atualizarVencedor(bloco, bloco.getOrgaoJulgadorRelator());
					} catch (Exception e) {
						FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
					}
				}
			}
		}
	}
	
	public HashMap<String, Long> getPlacar() {
		placar = ComponentUtil.getVotoBlocoManager().recuperarPlacar(getBloco());
		return placar;
	}
	
	public List<BlocoComposicao> getListComposicaoBloco() {
		if((listComposicaoBloco == null || listComposicaoBloco.size() == 0) && getBloco() != null ) { 
			listComposicaoBloco	= ComponentUtil.getBlocoComposicaoManager().findByBlocoPresentes(getBloco());
		}
		if(listComposicaoBloco == null || listComposicaoBloco.size() == 0) {
			try {
				ComponentUtil.getBlocoComposicaoManager().gerarComposicaoBloco(getBloco());
				listComposicaoBloco = ComponentUtil.getBlocoComposicaoManager().findByBlocoPresentes(getBloco());
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
		}
		return listComposicaoBloco;
	}
	
	public List<OrgaoJulgador> getListComposicaoOrgaoJulgador() {
		if(listComposicaoOrgaoJulgador == null && getBloco() != null ) {
			if(getListComposicaoBloco() != null) { 
				listComposicaoOrgaoJulgador = ComponentUtil.getBlocoComposicaoManager().recuperarOrgaoJulgadorPorBloco(bloco);
			}
		}
		return listComposicaoOrgaoJulgador;
	}
	
	public VotoBloco buscarVoto(BlocoComposicao composicao) {
		VotoBloco retorno = null;
		if(composicao!= null && composicao.getBloco() != null && composicao.getBloco().getAgruparOrgaoJulgador() && mapVotos.containsKey(composicao.getOrgaoJulgador().getIdOrgaoJulgador())){
			retorno = mapVotos.get(composicao.getOrgaoJulgador().getIdOrgaoJulgador());
		}
		return retorno;

	}
	
	public void definirRelatorParaAcordao(BlocoComposicao relatorParaAcordao) {
		if (relatorParaAcordao != null && bloco != null) {
			bloco.setOrgaoJulgadorVencedor(relatorParaAcordao.getOrgaoJulgador());
			ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(bloco);
		}
	}
	
	public void iniciarVotacao(BlocoComposicao composicao) {
		WinVotoBlocoAction winVotoBlocoAction = ComponentUtil.getComponent("winVotoBlocoAction", true);
		winVotoBlocoAction.iniciarVotacao(composicao);	
	}
	
	public TipoVoto getVotoRelator() {
		if(bloco != null) {
			if(bloco.getVotoRelator() == null) {
				try {
					bloco = ComponentUtil.getBlocoJulgamentoManager().atualizarVotoRelator(getBloco());
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			} 
			votoRelator = bloco.getVotoRelator();
		}
		return votoRelator;
	}
	
	public void alterarSituacao(String descricaoSituacao) {
		try {
			ComponentUtil.getBlocoJulgamentoManager().atualizarBlocoJulgamento(bloco, TipoSituacaoPautaBlocoEnum.getEnum(descricaoSituacao));
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Situação do bloco de julgamento alterada com sucesso");
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar situação: " + e.getLocalizedMessage());
		}
	}

	public void atualizarProclamacao() {
		try {
			ComponentUtil.getBlocoJulgamentoManager().atualizarProclamacao(bloco);
			FacesMessages.instance().add(Severity.INFO, "O bloco foi atualizado com sucesso!");
		}
		catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar atualizar proclamação de julgamento: " + e.getLocalizedMessage());
		}
	}
	
	public Map<Integer,VotoBloco> getMapVotos() {
		return mapVotos;
	}

	public void setMapVotos(Map<Integer,VotoBloco> mapVotos) {
		this.mapVotos = mapVotos;
	}

	public void removerVoto(BlocoComposicao s){
		try {
			ComponentUtil.getVotoBlocoManager().removerVoto(s);
			mapVotos.remove(s.getOrgaoJulgador().getIdOrgaoJulgador());
			placar = ComponentUtil.getVotoBlocoManager().recuperarPlacar(getBloco());
			FacesMessages.instance().add(Severity.INFO, "Voto removido com sucesso!");
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover voto, mensagem interna: " + e.getLocalizedMessage());
		}
	}

	public void unanime(){
		if(getBloco() != null) {
			try {
				if(bloco.getAgruparOrgaoJulgador()) {
					ComponentUtil.getVotoBlocoManager().registrarVotacaoUnanime(bloco, listComposicaoBloco);
					placar = ComponentUtil.getVotoBlocoManager().recuperarPlacar(getBloco());
				} else {
					ComponentUtil.getBlocoJulgamentoManager().registrarVotacaoUnanime(bloco,listComposicaoBloco);
				}
				FacesMessages.instance().add(Severity.INFO, "Votação registrada com sucesso");
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao registrar voto, mensagem interna: " + e.getLocalizedMessage());
			}
		}
	}
	
	public BlocoJulgamento getBloco() {
		if(bloco == null){
			if(idBloco != null){
				try {
					bloco = ComponentUtil.getBlocoJulgamentoManager().findById(idBloco);
				} catch (PJeBusinessException e) {
					FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
				}
			}
		}
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	public List<VotoBloco> getVotos() {
		return votos;
	}

	public void setVotos(List<VotoBloco> votos) {
		this.votos = votos;
	}

	public Integer getIdBloco() {
		return idBloco;
	}

	public void setIdBloco(Integer idBloco) {
		this.idBloco = idBloco;
	}

}
