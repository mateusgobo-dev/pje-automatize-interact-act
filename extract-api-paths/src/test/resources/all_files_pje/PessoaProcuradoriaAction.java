/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.PessoaProcuradorHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaJurisdicao;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.RepresentanteProcessualTipoAtuacaoEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name("pessoaProcuradoriaAction")
@Scope(ScopeType.CONVERSATION)
public class PessoaProcuradoriaAction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;

	private PessoaProcuradoria pessoaProcuradoria = new PessoaProcuradoria();
	
	private UsuarioLocalizacao usuarioLocalizacao = new UsuarioLocalizacao();
	
	private Boolean acompanhaSessao = false;
	
	private TipoProcuradoriaEnum tipoProcuradoria;
	
	private List<String> pessoaProcuradoriaJurisdicoesIds = new ArrayList<String>();

	public PessoaProcuradoria getPessoaProcuradoria() {
		return pessoaProcuradoria;
	}

	public void setPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		this.pessoaProcuradoria = pessoaProcuradoria;
	}
	
	public List<PessoaProcuradoria> getProcuradorias(PessoaProcurador p){
		return pessoaProcuradoriaManager.getProcuradorias(p);	
	}

	public TipoProcuradoriaEnum getTipoProcuradoria() {
		return tipoProcuradoria;
	}

	public void setTipoProcuradoria(TipoProcuradoriaEnum tipoProcuradoria) {
		this.tipoProcuradoria = tipoProcuradoria;
	}	
	
	public void persist(){
		if (pessoaProcuradoriaManager.verificaPessoaProcuradoria(pessoaProcuradoria)){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaProcuradoria.erro.representanteVinculado");
		} else if (pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.D
					&& this.getPessoaProcuradoriaJurisdicoesIds().size() <= 0) {
		 	 FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Nenhuma Jurisdição selecionada.");
		 }else{
			try {
				if (pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList().size() > 0){
					pessoaProcuradoriaManager.removeJurisdicoesPessoaProcuradoria(pessoaProcuradoria);
				}
				if(pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.D){
					 pessoaProcuradoriaManager.atualizaPessoaProcuradoria(pessoaProcuradoria, usuarioLocalizacao, this.pessoaProcuradoriaJurisdicoesIds);
				}else{
					 pessoaProcuradoriaManager.atualizaPessoaProcuradoria(pessoaProcuradoria, usuarioLocalizacao);					
				}
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR,"Erro ao vincular orgão de representação: " + e.getCode());
				e.printStackTrace();
			}
			finally{
				FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.message.updateRecord");
				this.pessoaProcuradoriaJurisdicoesIds.clear();
				pessoaProcuradoria = new PessoaProcuradoria();
			}
		 }
	}
	
	public void novo(){
	  pessoaProcuradoria.setPessoa(PessoaProcuradorHome.instance().getInstance());
	  //verifica se já foi inserido PessoaProcuradoria
	  if (pessoaProcuradoriaManager.verificaPessoaProcuradoria(pessoaProcuradoria)){
		  FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaProcuradoria.erro.representanteVinculado");
	  }else{
		//Se Distribuidor, verifica se selecionou alguma jurisdição
		if (pessoaProcuradoria.getAtuacao() == RepresentanteProcessualTipoAtuacaoEnum.D){
			if (this.getPessoaProcuradoriaJurisdicoesIds().size() <= 0){
			    FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pessoaProcuradoria.erro.nenhumaJurisdicaoSelecionada");
			}else{
				try { 
					  pessoaProcuradoriaManager.inserirNovo(pessoaProcuradoria, this.getPessoaProcuradoriaJurisdicoesIds());
					  FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.message.createRecord");
					
				} catch (PJeBusinessException e) {
					facesMessages.add(Severity.ERROR,"Erro ao vincular orgão de representação: " + e.getCode());
					e.printStackTrace();
				}
				finally{
					pessoaProcuradoria = new PessoaProcuradoria();
				}
			}
		}else{
			try { 
				  pessoaProcuradoriaManager.inserirNovo(pessoaProcuradoria);
				  FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.message.createRecord");
				
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR,"Erro ao vincular orgão de representação: " + e.getCode());
				e.printStackTrace();
			}
			finally{
				pessoaProcuradoria = new PessoaProcuradoria();
			}
		}
	  }
		
	}
	
	public TipoProcuradoriaEnum[] getTipoProcuradoriaValues() {
		Procuradoria procuradoriaUsuarioLogado = Authenticator.getProcuradoriaAtualUsuarioLogado();
		Usuario usuarioLogado = Authenticator.getUsuarioLogado();
		if(procuradoriaUsuarioLogado != null && usuarioLogado != null) {
			PessoaProcuradoria pessoaProcuradoria = pessoaProcuradoriaManager.
					getPessoaProcuradoria(usuarioLogado.getIdUsuario(), procuradoriaUsuarioLogado.getIdProcuradoria());
			if(pessoaProcuradoria != null) {
				return new TipoProcuradoriaEnum[]{pessoaProcuradoria.getProcuradoria().getTipo()};
			}
		}
		return TipoProcuradoriaEnum.values();
	}
	
	public RepresentanteProcessualTipoAtuacaoEnum[] getTipoAtuacaoValues() {
		return RepresentanteProcessualTipoAtuacaoEnum.values();
	}
	
	public List<Procuradoria> getListOrgaosDeRepresentacoes(){
		ProcuradoriaManager procManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
		TipoProcuradoriaEnum tipo = getTipoProcuradoria();
		
		return (tipo != null) ? procManager.getlistProcuradorias(tipo) : null;	
	}

	public Boolean getAcompanhaSessao() {
		return acompanhaSessao;
	}

	public void setAcompanhaSessao(Boolean acompanhaSessao) {
		this.acompanhaSessao = acompanhaSessao;
	}

	public void removePessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		pessoaProcuradoriaManager.remove(pessoaProcuradoria);
		FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pje.message.deleteRecord");
	}

	public void alteraPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		setTipoProcuradoria(pessoaProcuradoria.getProcuradoria().getTipo());
		setPessoaProcuradoria(pessoaProcuradoria);
		setUsuarioLocalizacao(pessoaProcuradoriaManager.getUsuarioLocalizacaoPessoaProcuradoria(pessoaProcuradoria));
		if(pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList().size() > 0){
			for(PessoaProcuradoriaJurisdicao ppj : pessoaProcuradoria.getPessoaProcuradoriaJurisdicaoList()){
				this.pessoaProcuradoriaJurisdicoesIds.add(""+ppj.getJurisdicao().getIdJurisdicao());
			}
		}
		pessoaProcuradoria.setAtuacao(pessoaProcuradoria.getAtuacaoReal());
	}
	
	public void novaPessoaProcuradoria() {
		this.pessoaProcuradoriaJurisdicoesIds.clear();
		setPessoaProcuradoria(new PessoaProcuradoria());
	}

	public List<PessoaProcuradoria> getListOrgaoRepresentacoesByTipo(PessoaProcurador p, TipoProcuradoriaEnum tipo){
		return pessoaProcuradoriaManager.getListOrgaoRepresentacoesByTipo(p, tipo);	
	}

	public boolean isProcuradorGestor() {
		return pessoaProcuradoria != null
				&& pessoaProcuradoria.getPessoa() != null  
				&& pessoaProcuradoria.getProcuradoria() != null
				&& pessoaProcuradoriaManager.isProcuradorGestor(pessoaProcuradoria.getPessoa().getIdUsuario(), 
															pessoaProcuradoria.getProcuradoria().getIdProcuradoria());
	}

	public UsuarioLocalizacao getUsuarioLocalizacao() {
		return usuarioLocalizacao;
	}

	public void setUsuarioLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
		this.usuarioLocalizacao = usuarioLocalizacao;
	}
	
	public List<String> getPessoaProcuradoriaJurisdicoesIds(){
		
		return this.pessoaProcuradoriaJurisdicoesIds;
	}

	public void setPessoaProcuradoriaJurisdicoesIds(List<String> lista) {
		this.pessoaProcuradoriaJurisdicoesIds = lista;
	}
	
	public List getGridAssociacoes(String inTipoProcuradoria, PessoaProcuradorHome pessoa){
		return pessoaProcuradoriaManager.getGridAssociacoes(inTipoProcuradoria, pessoa.getInstance().getIdUsuario().toString());
	}

}
