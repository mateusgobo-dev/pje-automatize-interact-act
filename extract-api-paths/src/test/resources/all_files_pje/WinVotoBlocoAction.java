package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoVoto;
import br.jus.pje.nucleo.entidades.VotoBloco;

@Name(WinVotoBlocoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class WinVotoBlocoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "winVotoBlocoAction";
	private boolean mostrarDivergentes;
	private BlocoComposicao blocoComposicao;
	private VotoBloco votoBloco;
	private List<TipoVoto> tiposVoto = new ArrayList<TipoVoto>();
    private List<OrgaoJulgador> votoDivergenteList = new ArrayList<OrgaoJulgador>();
    private OrgaoJulgador orgaoJulgadorAcompanhado;
	
	public void iniciarVotacao(BlocoComposicao composicao) {
		this.setBlocoComposicao(composicao);
		this.setVotoBloco(ComponentUtil.getVotoBlocoManager().recuperarVoto(composicao));
		
		if (this.votoBloco == null) {
			this.votoBloco = new VotoBloco();
			this.votoBloco.setOrgaoJulgador(composicao.getOrgaoJulgador());
			this.votoBloco.setBloco(composicao.getBloco());
		} 
		this.setOrgaoJulgadorAcompanhado(votoBloco.getOjAcompanhado());
		carregarTiposVoto();
	}

	private void carregarTiposVoto() {	
		if (isVotoRelator()) {
			this.tiposVoto = ComponentUtil.getTipoVotoManager().listTipoVotoAtivoComRelator();
		}
		else {
			this.tiposVoto = ComponentUtil.getTipoVotoManager().tiposVotosVogais();
		}
	}

	public List<TipoVoto> getTiposVoto() {
		return tiposVoto;
	}
	
	public boolean isVotoRelator() {
		OrgaoJulgador ojBloco = blocoComposicao.getBloco().getOrgaoJulgadorRelator(); 
		OrgaoJulgador ojVoto = blocoComposicao.getOrgaoJulgador();
		return (ojVoto.equals(ojBloco));
	}
	
	public boolean isMostrarDivergentes() {
		mostrarDivergentes = false;
		if(votoBloco != null && votoBloco.getTipoVoto() != null) {
			mostrarDivergentes = !isVotoRelator() && ComponentUtil.getTipoVotoManager().isDivergencia(votoBloco.getTipoVoto()) && ComponentUtil.getVotoBlocoManager().existeDivergente(blocoComposicao);
		}
		return mostrarDivergentes;
	}
	
	public List<OrgaoJulgador> getVotoDivergenteList() {
		votoDivergenteList = ComponentUtil.getVotoBlocoManager().recuperarOrgaosDivergentes(blocoComposicao.getBloco());
		return votoDivergenteList;
	}

	public void atualizarVoto() {
		if(votoBloco.getTipoVoto() != null ) {
			try {
				if(this.getOrgaoJulgadorAcompanhado() != null) {
					votoBloco.setOjAcompanhado(this.getOrgaoJulgadorAcompanhado());
				} else {
					if(isVotoRelator()) {
						votoBloco.getBloco().setVotoRelator(votoBloco.getTipoVoto());
					}
					if(ComponentUtil.getTipoVotoManager().isDivergencia(votoBloco.getTipoVoto())) {
						votoBloco.setOjAcompanhado(votoBloco.getOrgaoJulgador());
					} else {
						votoBloco.setOjAcompanhado(votoBloco.getBloco().getOrgaoJulgadorRelator());
					}
				}
				ComponentUtil.getBlocoJulgamentoManager().mergeAndFlush(votoBloco.getBloco());
				ComponentUtil.getVotoBlocoManager().registrarVoto(votoBloco);
				ComponentUtil.getPopUpVotoBlocoAction().getMapVotos().put(votoBloco.getOrgaoJulgador().getIdOrgaoJulgador(),votoBloco);
				setaMaioriaVotacao();
				FacesMessages.instance().add(Severity.INFO, "Voto atualizado com sucesso!");
			} 
			catch (Exception e) {
				String mensagem = "";
				if(e.getLocalizedMessage() != null) {
					mensagem = e.getLocalizedMessage();
				} else {
					mensagem = e.getMessage();
				}
				FacesMessages.instance().add(Severity.ERROR, "Erro ao atualizar o voto: {0}", mensagem);
			}
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Selecione o tipo de voto!");
		}
	}

	private void setaMaioriaVotacao() {
		ComponentUtil.getBlocoJulgamentoManager().setaMaioriaVotacao(blocoComposicao.getBloco());
	}
	
	public BlocoComposicao getBlocoComposicao() {
		return blocoComposicao;
	}

	public void setBlocoComposicao(BlocoComposicao blocoComposicao) {
		this.blocoComposicao = blocoComposicao;
	}

	public VotoBloco getVotoBloco() {
		return votoBloco;
	}

	public void setVotoBloco(VotoBloco votoBloco) {
		this.votoBloco = votoBloco;
	}

	public OrgaoJulgador getOrgaoJulgadorAcompanhado() {
		return orgaoJulgadorAcompanhado;
	}

	public void setOrgaoJulgadorAcompanhado(OrgaoJulgador orgaoJulgadorAcompanhado) {
		this.orgaoJulgadorAcompanhado = orgaoJulgadorAcompanhado;
	}
}
