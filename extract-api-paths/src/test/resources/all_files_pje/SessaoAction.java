package br.com.jt.pje.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.service.PautaJulgamentoService;
import br.com.infox.view.GenericCrudAction;
import br.com.jt.pje.list.OrgaoJulgadorSessaoList;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.com.jt.pje.manager.SalaManager;
import br.com.jt.pje.manager.SessaoManager;
import br.com.jt.pje.manager.TipoSessaoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorColegiadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name(SessaoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class SessaoAction extends GenericCrudAction<SessaoJT>{

	private static final long serialVersionUID = 7779834046421013677L;

	public static final String NAME = "sessaoAction";
	
	@In
	private TipoSessaoManager tipoSessaoManager;
	@In
	private OrgaoJulgadorColegiadoManager orgaoJulgadorColegiadoManager;
	@In
	private SalaManager salaManager;
	@In
	private SessaoManager sessaoManager;
	@In
	private ComposicaoSessaoManager composicaoSessaoManager;
	@In
	private PessoaMagistradoManager pessoaMagistradoManager;
	
	@In
	private PautaJulgamentoService pautaJulgamentoService;

	private TipoSessao tipoSessao;
	private Sala sala;
	private List<SalaHorario> listaSalaHorario = new ArrayList<SalaHorario>(0);
	private boolean repetir;
	private Date dataInicial;
	private Date dataFinal;
	private List<OrgaoJulgador> listaOJ = new ArrayList<OrgaoJulgador>(0);
	private List<ComposicaoSessao> composicaoSessaoList = new ArrayList<ComposicaoSessao>(0);
	private PessoaProcurador pessoaProcurador;
	
	private OrgaoJulgadorSessaoList orgaoJulgadorSessaoList = new OrgaoJulgadorSessaoList();

	public void setIdSessao(Integer id){
		super.setIdInstance(id);
		setTipoSessao(getInstance().getTipoSessao());
	}
	
	public void marcarSala(SalaHorario obj){
		if (getListaSalaHorario().contains(obj)){
			getListaSalaHorario().remove(obj);
		}else {
			getListaSalaHorario().add(obj);
		}
	}
	
	/**
	 * [PJEII-4077] Método responsável por verificar se a lista de orgão julgador contain o objeto passado, verificad
	 * a existencia, apresenta o checkbox selecionado ou não. 
	 * @param row
	 * @return boolean
	 */
	public boolean contains(OrgaoJulgador row) {
		for (OrgaoJulgador o : getListaOJ()) {
			if (o.equals(row)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * [PJEII-4077] Método responsável por verificar se todos os checkbox estão marcados.
	 * @return boolean
	 */
	public boolean allMarked() {
		if (getComposicaoSessaoList() != null && getListaOJ() != null && !getComposicaoSessaoList().isEmpty() 
				&& !getListaOJ().isEmpty() && getComposicaoSessaoList().size() == getListaOJ().size()) {
			return true;
		}
		return false;
	}
	
	public void addRemoveAll(){
		if(!getListaOJ().isEmpty() && !orgaoJulgadorSessaoList.list().isEmpty()){
			setListaOJ(new ArrayList<OrgaoJulgador>(0));
		} else {
			getListaOJ().clear();
			getListaOJ().addAll(orgaoJulgadorSessaoList.list());
			if (getListaOJ().size() != getComposicaoSessaoList().size()) {
				getListaOJ().clear();
				for (ComposicaoSessao comp: getComposicaoSessaoList()) {
					getListaOJ().add(comp.getOrgaoJulgador());
				}
			}
		}
	}
	
	public void addItem(OrgaoJulgador row){
		if (getListaOJ().contains(row)){
			getListaOJ().remove(row);
		}else{
			getListaOJ().add(row);
		}
	}
	
	public void carregarComposicaoSessao(){
		getListaOJ().clear();
		getComposicaoSessaoList().clear();
		getListaOJ().addAll(composicaoSessaoManager.getOrgaoJulgadorBySessao(getInstance()));
		
		for(OrgaoJulgador oj: getListaOJ()){
			ComposicaoSessao composicaoSessao = composicaoSessaoManager.getComposicaoSessao(getInstance(), oj);
			getComposicaoSessaoList().add(composicaoSessao);
		}
		
		for(OrgaoJulgador oj: orgaoJulgadorSessaoList.list()){
			if(!getListaOJ().contains(oj)){
				ComposicaoSessao composicaoSessao = new ComposicaoSessao();
				composicaoSessao.setSessao(getInstance());
				composicaoSessao.setOrgaoJulgador(oj);
				if(ParametroUtil.instance().isPrimeiroGrau()){
					composicaoSessao.setMagistradoPresente(pessoaMagistradoManager.getMagistradoTitular(oj));  
				} else{  
					composicaoSessao.setMagistradoPresente(pessoaMagistradoManager.getMagistradoTitular(oj, getInstance().getOrgaoJulgadorColegiado()));  
				}  
				getComposicaoSessaoList().add(composicaoSessao);
			}
		}
		
		if(getListaOJ().isEmpty()){
			getListaOJ().addAll(orgaoJulgadorSessaoList.list());
		}
		
	}
	
	public void gravarProcuradorNaSessao(){
		getInstance().setPessoaProcurador(pessoaProcurador);
		update(getInstance());
		pessoaProcurador = null;
	}
	
	public void removerIncluirComposicaoSessao(){
		List<ComposicaoSessao> listComposicao = new ArrayList<ComposicaoSessao>(0);
		for(ComposicaoSessao cs: composicaoSessaoList){
			if(getListaOJ().contains(cs.getOrgaoJulgador())){
				listComposicao.add(cs);
			}
		}
		
		pautaJulgamentoService.atualizarComposicoesSessao(getListaOJ(), getInstance(), listComposicao);
		carregarComposicaoSessao();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro atualizado com sucesso!");
	}
	
	public void persist(){
		SessaoJT sessao = sessaoManager.persist(Authenticator.getOrgaoJulgadorColegiadoAtual(), dataInicial, dataFinal, 
				repetir, listaSalaHorario, tipoSessao, Authenticator.getUsuarioLogado());
		if(sessao != null){
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Registro cadastrado com sucesso.");
			setInstance(sessao);
		}
	}
	
	public void update(){
		getInstance().setTipoSessao(tipoSessao);
		this.update(getInstance());
	}
	
	public void clearSalaHorarioList(){
		setListaSalaHorario(new ArrayList<SalaHorario>(0));
	}
	
	public void newInstance(){
		setDataFinal(null);
		setDataInicial(null);
		setListaOJ(new ArrayList<OrgaoJulgador>(0));
		clearSalaHorarioList();
		setRepetir(false);
		setSala(null);
		setTipoSessao(null);
		composicaoSessaoList = new ArrayList<ComposicaoSessao>(0);
		pessoaProcurador = null;
		super.newInstance();
	}

	/*
	 * inicio dos items
	*/
	
	public SituacaoSessaoEnum[] getSituacaoSessaoEnumValues(){
		return SituacaoSessaoEnum.values();
	}

	public List<TipoSessao> getTipoSessaoItems(){
		return tipoSessaoManager.getTipoSessaoItems();
	}
	
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems(){
		return orgaoJulgadorColegiadoManager.getOrgaoJulgadorColegiadoItems();
	}
	
	public List<Sala> getSalaSessaoItems(){
		if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
			return salaManager.getSalaSessaoItemsByOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
		}else{
			return salaManager.getSalaSessaoItems();
		}
	}
	
	public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(OrgaoJulgador orgaoJulgador){
		if(ParametroUtil.instance().isPrimeiroGrau()){  
			return pessoaMagistradoManager.magistradoSubstitutoSessaoItems(orgaoJulgador, getInstance());  
		} else{  
			return pessoaMagistradoManager.magistradoSubstitutoSessaoItems(orgaoJulgador, getInstance(), getInstance().getOrgaoJulgadorColegiado());  
		} 
	}

	/*
	 * inicio dos gets e sets
	*/
	
	public void setSala(Sala sala) {
		this.sala = sala;
	}

	public Sala getSala() {
		return sala;
	}

	public void setRepetir(Boolean repetir) {
		this.repetir = repetir;
	}

	public Boolean getRepetir() {
		return repetir;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setListaOJ(List<OrgaoJulgador> listaOJ) {
		this.listaOJ = listaOJ;
	}

	public List<OrgaoJulgador> getListaOJ() {
		return listaOJ;
	}

	public void setTipoSessao(TipoSessao tipoSessao) {
		this.tipoSessao = tipoSessao;
	}

	public TipoSessao getTipoSessao() {
		return tipoSessao;
	}


	public void setListaSalaHorario(List<SalaHorario> listaSalaHorario) {
		this.listaSalaHorario = listaSalaHorario;
	}

	public List<SalaHorario> getListaSalaHorario() {
		return listaSalaHorario;
	}

	public OrgaoJulgadorSessaoList getOrgaoJulgadorSessaoList() {
		return orgaoJulgadorSessaoList;
	}

	public void setOrgaoJulgadorSessaoList(OrgaoJulgadorSessaoList orgaoJulgadorSessaoList) {
		this.orgaoJulgadorSessaoList = orgaoJulgadorSessaoList;
	}

	public PessoaProcurador getPessoaProcurador() {
		return pessoaProcurador;
	}

	public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
		this.pessoaProcurador = pessoaProcurador;
	}

	public List<ComposicaoSessao> getComposicaoSessaoList() {
		return composicaoSessaoList;
	}

	public void setComposicaoSessaoList(List<ComposicaoSessao> composicaoSessaoList) {
		this.composicaoSessaoList = composicaoSessaoList;
	}

}
