/**
 * 
 */
package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.home.PessoaProcuradorHome;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaJurisdicaoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaJurisdicao;
import br.jus.pje.nucleo.entidades.Procuradoria;

@Name("pessoaProcuradoriaJurisdicaoAction")
@Scope(ScopeType.CONVERSATION)
public class PessoaProcuradoriaJurisdicaoAction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private PessoaProcuradoriaJurisdicaoManager pessoaProcuradoriaJurisdicaoManager;

	@In
	private JurisdicaoManager jurisdicaoManager;
	
	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	@In
	private CaixaRepresentanteManager caixaRepresentanteManager;
	
	@In
	private PessoaProcuradorHome pessoaProcuradorHome;
	
	private List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicoes;
	
	private List<Integer> idJurisdicoes = new ArrayList<Integer>();
	
	private List<String> idJurisdicoesSelecionados = new ArrayList<String>();

	private List<Jurisdicao> jurisdicoes = new ArrayList<Jurisdicao>();
	
	private Procuradoria procuradoria = new Procuradoria();
	
	private PessoaProcuradoria pessoaProcuradoria;
	
	private List<PessoaProcuradoria> listPessoaProcuradoria = new ArrayList<PessoaProcuradoria>();
	
	public List<Jurisdicao> getJurisdicoes(){
		try {
			jurisdicoes = jurisdicaoManager.findAll();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return jurisdicoes;
	}

	public List<Integer> getIdJurisdicoes(){
		idJurisdicoes = new ArrayList<Integer>();
		if(pessoaProcuradoria != null) {
			List<Jurisdicao> listJurisdicoes = jurisdicaoManager.getJurisdicoesAtivas();
			List<Jurisdicao> listJurisdicoesAtribuidas = pessoaProcuradoriaJurisdicaoManager.getPessoaProcuradoriaJurisdicoesAtivas(pessoaProcuradoria);
			listJurisdicoes.removeAll(listJurisdicoesAtribuidas);
			
			for(Jurisdicao j : listJurisdicoes)
				idJurisdicoes.add(j.getIdJurisdicao());			
		}
		return idJurisdicoes;
	}

	public String getNomeJurisdicao(Integer id) {
		String nome = "";
		try {
			Jurisdicao jurisdicao = jurisdicaoManager.findById(id);
			if(jurisdicao != null)
				nome = jurisdicao.getJurisdicao();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
		return nome;  
	}
	
	public void incluir(){
		pessoaProcuradoriaJurisdicoes = new ArrayList<PessoaProcuradoriaJurisdicao>();
		for(String idJurisdicao : idJurisdicoesSelecionados) {
			try {
				Jurisdicao jurisdicao = jurisdicaoManager.findById(Integer.parseInt(idJurisdicao));
				PessoaProcuradoriaJurisdicao ppj = new PessoaProcuradoriaJurisdicao();
				ppj.setJurisdicao(jurisdicao);
				ppj.setPessoaProcuradoria(pessoaProcuradoria);
				ppj.setAtivo(true);
				pessoaProcuradoriaJurisdicoes.add(ppj);
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
		pessoaProcuradoriaJurisdicaoManager.persistList(pessoaProcuradoriaJurisdicoes);
	}

	public List<PessoaProcuradoriaJurisdicao> getPessoaProcuradoriaJurisdicoes() {
		return pessoaProcuradoriaJurisdicoes;
	}

	public void setPessoaProcuradoriaJurisdicoes(
			List<PessoaProcuradoriaJurisdicao> pessoaProcuradoriaJurisdicoes) {
		this.pessoaProcuradoriaJurisdicoes = pessoaProcuradoriaJurisdicoes;
	}

	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	public List<PessoaProcuradoria> getListOrgaosDeRepresentacoes(){
		PessoaProcuradoriaManager manager = (PessoaProcuradoriaManager) Component.getInstance(PessoaProcuradoriaManager.NAME);
		PessoaProcuradorHome pessoaProcuradorHome = (PessoaProcuradorHome) Component.getInstance(PessoaProcuradorHome.NAME);
		PessoaProcurador procurador = pessoaProcuradorHome.getInstance();
		listPessoaProcuradoria = manager.getProcuradorias(procurador);
		return listPessoaProcuradoria;	
	}

	public PessoaProcuradoriaJurisdicaoManager getPessoaProcuradoriaJurisdicaoManager() {
		return pessoaProcuradoriaJurisdicaoManager;
	}

	public void setPessoaProcuradoriaJurisdicaoManager(
			PessoaProcuradoriaJurisdicaoManager pessoaProcuradoriaJurisdicaoManager) {
		this.pessoaProcuradoriaJurisdicaoManager = pessoaProcuradoriaJurisdicaoManager;
	}

	public JurisdicaoManager getJurisdicaoManager() {
		return jurisdicaoManager;
	}

	public void setJurisdicaoManager(JurisdicaoManager jurisdicaoManager) {
		this.jurisdicaoManager = jurisdicaoManager;
	}

	public PessoaProcuradoria getPessoaProcuradoria() {
		return pessoaProcuradoria;
	}

	public void setPessoaProcuradoria(PessoaProcuradoria pessoaProcuradoria) {
		this.pessoaProcuradoria = pessoaProcuradoria;
	}
	
	public List<String> getIdJurisdicoesSelecionados() {
		//List<Jurisdicao> listJurisdicoesAtribuidas = pessoaProcuradoriaJurisdicaoManager.getPessoaProcuradoriaJurisdicoesAtivas(pessoaProcuradoria);
		//for(Jurisdicao jur : listJurisdicoesAtribuidas) {
		//	idJurisdicoesSelecionados.add(Integer.toString(jur.getIdJurisdicao()));
		//}
		return idJurisdicoesSelecionados;
	}

	public void setIdJurisdicoesSelecionados(List<String> idJurisdicoesSelecionados) {
		this.idJurisdicoesSelecionados = idJurisdicoesSelecionados;
	}	
	
	/*
	 * SelectItem com as jurisdições disponíveis para associação
	 */
	public List<SelectItem> getPessoaProcuradoriaJurisdicaoItens(){
		List<Jurisdicao> listJurisdicoes = jurisdicaoManager.getJurisdicoesAtivas();
		List<Jurisdicao> listJurisdicoesAtribuidas = pessoaProcuradoriaJurisdicaoManager.getPessoaProcuradoriaJurisdicoesAtivas(pessoaProcuradoria);
		listJurisdicoes.removeAll(listJurisdicoesAtribuidas);
		
		List<SelectItem> items = new ArrayList<SelectItem>(listJurisdicoes.size());

		
		for(Jurisdicao value : listJurisdicoes){
			SelectItem novoItem = new SelectItem(value);
			novoItem.setValue(value.getIdJurisdicao());
		    items.add(novoItem);
		}
		return items;
	}

	public List<PessoaProcuradoriaJurisdicao> getListPessoaProcuradoriaJurisdicoes(){
		return pessoaProcuradoriaJurisdicaoManager.getPessoaProcuradoriaJurisdicoes(pessoaProcuradoria);
	}	
	
	public void removePessoaProcuradoriaJurisdicao(PessoaProcuradoriaJurisdicao ppj){
		try {
			pessoaProcuradoriaJurisdicaoManager.remove(ppj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Jurisdicao> getJurisdicoesDeCaixasAdvogadoProcuradorDistinct(Procuradoria procuradoria) {
		List<Jurisdicao> jurisdicoes = getJurisdicoesDeCaixasAdvogadoProcurador(procuradoria);
		Set<Jurisdicao> jurisdicoesDistinct = new HashSet<Jurisdicao>(jurisdicoes);
		
		// retorna os valores para lista para funcionar no datatable
		jurisdicoes = new ArrayList<Jurisdicao>();
		for(Jurisdicao jurisdicao : jurisdicoesDistinct){
			jurisdicoes.add(jurisdicao);	
		}
		
		return jurisdicoes;
	}
	
	//Caixas Representante
	private List<CaixaAdvogadoProcurador> getCaixasAdvogadoProcurador() {
		//Caixas vinculadas ao representante
		List<CaixaRepresentante> caixasRepresentante = caixaRepresentanteManager.getCaixaRepresentanteByRepresentante(pessoaProcuradorHome.getInstance().getIdUsuario());
		//Caixas AdvogadoProcurador do representante
		List<CaixaAdvogadoProcurador> caixasAdvogadoProcurador = new ArrayList<CaixaAdvogadoProcurador>();
		for(CaixaRepresentante caixaRep : caixasRepresentante){
			caixasAdvogadoProcurador.add(caixaRep.getCaixaAdvogadoProcurador());
		}
		
		return caixasAdvogadoProcurador;
	}

	public List<Jurisdicao> getJurisdicoesDeCaixasAdvogadoProcurador(Procuradoria procuradoria){
		List<Jurisdicao> jurisdicoes = new ArrayList<Jurisdicao>();
		List<CaixaAdvogadoProcurador> caixas = getCaixasAdvogadoProcurador();
		
		for(CaixaAdvogadoProcurador caixa : caixas){
		  if (procuradoria.getLocalizacao().getIdLocalizacao() == caixa.getLocalizacao().getIdLocalizacao())
			jurisdicoes.add(caixa.getJurisdicao());
		}
		
		return jurisdicoes;
	}	
	
	public List<CaixaAdvogadoProcurador> getCaixasAdvogadoProcuradorJurisdicao(Procuradoria procuradoria, Jurisdicao jurisdicao){
		List<CaixaAdvogadoProcurador> caixas =  getCaixasAdvogadoProcurador();
		List<CaixaAdvogadoProcurador> caixasProcuradoriaJurisdicao =  new ArrayList<CaixaAdvogadoProcurador>();
		
		for(CaixaAdvogadoProcurador caixa : caixas){
		  if(caixa.getLocalizacao().getIdLocalizacao() == procuradoria.getLocalizacao().getIdLocalizacao()
				  && caixa.getJurisdicao().getIdJurisdicao() == jurisdicao.getIdJurisdicao())
			caixasProcuradoriaJurisdicao.add(caixa);
		}
		
		return caixasProcuradoriaJurisdicao;
	}

	public List<CaixaAdvogadoProcurador> getCaixasAdvogadoProcuradorJurisdicaoNovo(Integer idProcuradoria, Integer idJurisdicao){
		List<CaixaAdvogadoProcurador> caixas =  getCaixasAdvogadoProcurador();
		List<CaixaAdvogadoProcurador> caixasProcuradoriaJurisdicao =  new ArrayList<CaixaAdvogadoProcurador>();
		
		for(CaixaAdvogadoProcurador caixa : caixas){
		  if(caixa.getLocalizacao().getIdLocalizacao() == idProcuradoria
				  && caixa.getJurisdicao().getIdJurisdicao() == idJurisdicao)
			caixasProcuradoriaJurisdicao.add(caixa);
		}
		
		return caixasProcuradoriaJurisdicao;
	}	

	  
}
