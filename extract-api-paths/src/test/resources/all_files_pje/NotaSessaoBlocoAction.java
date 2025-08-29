package br.jus.cnj.pje.view;

import java.util.Date;
import java.util.List;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.BlocoComposicao;
import br.jus.pje.nucleo.entidades.BlocoJulgamento;
import br.jus.pje.nucleo.entidades.NotaSessaoBloco;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("notaSessaoBlocoAction")
@Scope(ScopeType.CONVERSATION)
public class NotaSessaoBlocoAction {

	
	private Boolean mostrarNota;
	private Boolean situacao;
	private BlocoJulgamento bloco;
	private NotaSessaoBloco notaSessaoBloco;
	private String nota;
	private OrgaoJulgador orgaoJulgador;
	private List<BlocoComposicao> listComposicaoBloco;
	private List<OrgaoJulgador> listComposicaoOrgaoJulgador;

	public void ativarModal(BlocoJulgamento bloco) {
		setBloco(bloco);
		setMostrarNota(true);
	}
	
	
	public void desativarModal() {
		bloco = null;
		setMostrarNota(false);
		setOrgaoJulgador(null);
		this.nota = null;
		this.notaSessaoBloco = null;
	}

	
	public void selecionarNota(NotaSessaoBloco notaSessaoBloco) {
		this.setNota(notaSessaoBloco.getNotaSessaoBloco());
		this.setNotaSessaoBloco(notaSessaoBloco);
		this.setOrgaoJulgador(notaSessaoBloco.getOrgaoJulgador());
		this.setSituacao(notaSessaoBloco.getAtivo());
	}
	
	public void incluirNota() {
		notaSessaoBloco = new NotaSessaoBloco();
		notaSessaoBloco.setAtivo(situacao);
		notaSessaoBloco.setBloco(bloco);
		notaSessaoBloco.setDataCadastro(new Date());
		notaSessaoBloco.setNotaSessaoBloco(nota);
		notaSessaoBloco.setOrgaoJulgador(orgaoJulgador);
		notaSessaoBloco.setUsuarioCadastro(Authenticator.getUsuarioLogado());
		try {
			ComponentUtil.getNotaSessaoBlocoManager().persistAndFlush(notaSessaoBloco);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
		this.setNota("");
		this.setNotaSessaoBloco(null);
		this.setOrgaoJulgador(null);
		this.setSituacao(true);
	}
	
	public void inativar(NotaSessaoBloco notaSessaoBloco) {
		notaSessaoBloco.setAtivo(false);
		try {
			ComponentUtil.getNotaSessaoBlocoManager().persistAndFlush(notaSessaoBloco);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}
	
	public void alterar() {
		notaSessaoBloco.setNotaSessaoBloco(nota);
		notaSessaoBloco.setAtivo(situacao);
		notaSessaoBloco.setOrgaoJulgador(orgaoJulgador);
		try {
			ComponentUtil.getNotaSessaoBlocoManager().persistAndFlush(notaSessaoBloco);
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
		}
	}

	public void setMostrarNota(Boolean mostrarNota) {
		this.mostrarNota = mostrarNota;
	}

	public Boolean getMostrarNota() {
		return mostrarNota;
	}
	
	public BlocoJulgamento getBloco() {
		return bloco;
	}

	public void setBloco(BlocoJulgamento bloco) {
		this.bloco = bloco;
	}

	public String getNota() {
		return nota;
	}

	public void setNota(String nota) {
		this.nota = nota;
	}
	
	public NotaSessaoBloco getNotaSessaoBloco() {
		return notaSessaoBloco;
	}

	public void setNotaSessaoBloco(NotaSessaoBloco notaSessaoBloco) {
		this.notaSessaoBloco = notaSessaoBloco;
	}


	public List<NotaSessaoBloco> getAnotacoes(){
		return ComponentUtil.getNotaSessaoBlocoManager().recuperar(bloco);
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}


	public Boolean getSituacao() {
		return situacao;
	}


	public void setSituacao(Boolean situacao) {
		this.situacao = situacao;
	}

	public List<BlocoComposicao> getListComposicaoBloco() {
		if(listComposicaoBloco == null && getBloco() != null ) { 
			listComposicaoBloco	= ComponentUtil.getBlocoComposicaoManager().findByBlocoPresentes(getBloco());
		}
		if(listComposicaoBloco == null || listComposicaoBloco.size() == 0) {
			try {
				ComponentUtil.getBlocoComposicaoManager().gerarComposicaoBloco(getBloco());
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

	public void setListComposicaoOrgaoJulgador(List<OrgaoJulgador> listComposicaoOrgaoJulgador) {
		this.listComposicaoOrgaoJulgador = listComposicaoOrgaoJulgador;
	}
	
	public void incluirNotaOrgaoJulgador(int idBloco, String nota) {
		BlocoJulgamento bloco = ComponentUtil.getBlocoJulgamentoDAO().find(idBloco);
		if(bloco != null && ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual() != null) {
			notaSessaoBloco = new NotaSessaoBloco();
			notaSessaoBloco.setAtivo(true);
			notaSessaoBloco.setBloco(bloco);
			notaSessaoBloco.setDataCadastro(new Date());
			notaSessaoBloco.setNotaSessaoBloco(nota);
			notaSessaoBloco.setOrgaoJulgador(ComponentUtil.getPainelDoMagistradoNaSessaoAction().getOrgaoAtual());
			notaSessaoBloco.setUsuarioCadastro(Authenticator.getUsuarioLogado());
			try {
				ComponentUtil.getNotaSessaoBlocoManager().persistAndFlush(notaSessaoBloco);
				FacesMessages.instance().add(Severity.INFO, "Anotação registrada com sucesso");
			} catch (PJeBusinessException e) {
				FacesMessages.instance().add(Severity.ERROR, e.getLocalizedMessage());
			}
			this.setNota("");
			this.setNotaSessaoBloco(null);
			this.setOrgaoJulgador(null);
			this.setSituacao(true);
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Não foi possível recuperar os dados para vincular à anotação" );
		}
	}
}