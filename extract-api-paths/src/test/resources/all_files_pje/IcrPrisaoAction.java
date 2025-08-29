package br.com.infox.cliente.home.icrrefactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;

@Name("icrPrisaoAction")
@Scope(ScopeType.CONVERSATION)
public class IcrPrisaoAction extends IcrAssociarEstabelecimentosPrisionaisAction<IcrPrisao, IcrPrisaoManager>{

	private static final long serialVersionUID = 6616955309367838671L;
	private boolean estaPreso = false;
	private String nomeReu = "";
	private ProcessoParte reuProcessoParte;
	private boolean mostrarFormEncerramento = false;
	private Boolean showConfirmacao;
	private IcrPrisao prisaoCandidata;
	private MotivoEncerramentoPrisaoEnum[] motivos = MotivoEncerramentoPrisaoEnum.values();
	private Map<ProcessoParte, Boolean> reusSelecionadosMap = new HashMap<ProcessoParte, Boolean>();

	@Override
	public void init(){
		limparTela();
		super.init();
	}

	/*
	 * TODO verificar qual o motivo da criação deste método pois quando a conversation é encerrada pela home todos os campos são automaticamente
	 * limpos
	 */
	private void limparTela(){
		setUf(null);
		setCidade(null);
		getPrisoesEncerrar().clear();
		setPrisaoCandidata(null);
		setShowConfirmacao(null);
		setMostrarFormEncerramento(false);
		getHome().getReusSelecionados().clear();
	}

	@Override
	public void insert(){
		recarregaReusSelecionados();
		super.insert();
	}

	private void recarregaReusSelecionados(){
		getHome().getReusSelecionados().clear();
		for (ProcessoParte pp : getReusSelecionadosMap().keySet()){
			if (getReusSelecionadosMap().get(pp) == true)
				getHome().getReusSelecionados().add(pp);
		}
	}

	public void desmarcarUltimoReu(){
		if (getUltimoReuSelecionado() != null){
			getReusSelecionadosMap().put(getUltimoReuSelecionado(), false);
			setUltimoReuSelecionado(null);
		}
	}

	public IcrPrisao encontraPrisaoEmAberto(ProcessoParte processoParte){
		// montando a lista de prisões que serão encerradas
		if (processoParte.getIcrPrisoesAtivas() != null){
			for (IcrPrisao icrPrisao : processoParte.getIcrPrisoesAtivas()){
				if (icrPrisao.getDtEncerramentoPrisao() == null){
					return icrPrisao;
				}
			}
		}
		return null;
	}

	public void selecionarReu(ProcessoParte processoParte){
		setPrisaoCandidata(null);
		setShowConfirmacao(false);
		setMostrarFormEncerramento(false);
		setUltimoReuSelecionado(processoParte);
		if (getUltimoReuSelecionado() != null){
			if (getReusSelecionadosMap().get(getUltimoReuSelecionado()) == true){
				IcrPrisao prisao = encontraPrisaoEmAberto(getUltimoReuSelecionado());
				if (prisao != null){
					setPrisaoCandidata(prisao);
					getPrisaoCandidata().setMotivoEncerramentoPrisao(MotivoEncerramentoPrisaoEnum.CP);
					setShowConfirmacao(true);
					setMostrarFormEncerramento(true);
				}
			}
			else{
				removerDaLista(getUltimoReuSelecionado());
			}
		}
	}

	public void confirmarEncerramento(){
		setShowConfirmacao(false);
		setMostrarFormEncerramento(true);
	}

	public void colocarPrisoesMemoria(){
		if (getPrisaoCandidata() != null){
			try{
				if (!getPrisoesEncerrar().contains(getPrisaoCandidata())){
					getManager().validarEncerramentoPrisao(getPrisaoCandidata());
					getPrisoesEncerrar().add(getPrisaoCandidata());
					setPrisaoCandidata(null);
					setMostrarFormEncerramento(false);
					setShowConfirmacao(false);
				}
			} catch (Exception e){
				getPrisaoCandidata().setDtEncerramentoPrisao(null);
				addMessage(Severity.ERROR, e.getMessage(), e);
			}
		}
	}

	private void reverterEncerramento(IcrPrisao prisao){
		if (prisao == null)
			return;
		prisao.setDtEncerramentoPrisao(null);
		prisao.setDsMotivoEncerramento(null);
		prisao.setMotivoEncerramentoPrisao(null);
		prisao.setPrisaoEncerrada(null);
	}

	private void removerDaLista(ProcessoParte pp){
		for (IcrPrisao prisao : getPrisoesEncerrar()){
			if (prisao.getProcessoParte().equals(pp)){
				reverterEncerramento(prisao);
				getPrisoesEncerrar().remove(prisao);
				break;
			}
		}
	}

	public void fecharModal(){
		desmarcarUltimoReu();
		reverterEncerramento(getPrisaoCandidata());
		setPrisaoCandidata(null);
		setMostrarFormEncerramento(false);
		setShowConfirmacao(false);
	}

	@Override
	public boolean canEdit(){
		if (getInstance() == null)
			return true;
		if (getInstance().getDtEncerramentoPrisao() != null)
			return false;
		if (getInstance().getAtivo() != null && getInstance().getAtivo() == false)
			return false;
		return true;
	}

	/*
	 * ************************************************************* GETTER'S AND SETTER'S ****************************************************
	 */
	@Override
	protected EstabelecimentoPrisional getEstabelecimentoPrisional(IcrPrisao entity){
		return entity.getEstabelecimentoPrisional();
	}

	@Override
	protected void setDtPublicacao(Date dtPublicacao){
		//
	}

	public List<TipoPrisaoEnum> getTipoPrisaoValues(){
		List<TipoPrisaoEnum> values = Arrays.asList(TipoPrisaoEnum.values());
		Collections.sort(values, new Comparator<TipoPrisaoEnum>(){

			@Override
			public int compare(TipoPrisaoEnum o1, TipoPrisaoEnum o2){
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		return values;
	}

	public boolean isEstaPreso(){
		return estaPreso;
	}

	public void setEstaPreso(boolean estaPreso){
		this.estaPreso = estaPreso;
	}

	public String getNomeReu(){
		return nomeReu;
	}

	public void setNomeReu(String nomeReu){
		this.nomeReu = nomeReu;
	}

	public ProcessoParte getReuProcessoParte(){
		return reuProcessoParte;
	}

	public void setReuProcessoParte(ProcessoParte reuProcessoParte){
		this.reuProcessoParte = reuProcessoParte;
	}

	public boolean getMostrarFormEncerramento(){
		return mostrarFormEncerramento;
	}

	public void setMostrarFormEncerramento(boolean mostrarFormEncerramento){
		this.mostrarFormEncerramento = mostrarFormEncerramento;
	}

	public MotivoEncerramentoPrisaoEnum[] getMotivos(){
		return motivos;
	}

	public void setMotivos(MotivoEncerramentoPrisaoEnum[] motivos){
		this.motivos = motivos;
	}

	public Boolean getShowConfirmacao(){
		return showConfirmacao;
	}

	public void setShowConfirmacao(Boolean showConfirmacao){
		this.showConfirmacao = showConfirmacao;
	}

	public void setPrisaoCandidata(IcrPrisao prisaoCandidata){
		this.prisaoCandidata = prisaoCandidata;
	}

	public IcrPrisao getPrisaoCandidata(){
		return this.prisaoCandidata;
	}

	private ProcessoParte ultimoReuSelecionado = null;

	public void setUltimoReuSelecionado(ProcessoParte reuSelecionado){
		this.ultimoReuSelecionado = reuSelecionado;
	}

	public ProcessoParte getUltimoReuSelecionado(){
		return ultimoReuSelecionado;
	}

	public List<IcrPrisao> getPrisoesEncerrar(){
		return getManager().getPrisoesEncerrar();
	}

	public void setPrisoesEncerrar(List<IcrPrisao> prisoesEncerrar){
		getManager().setPrisoesEncerrar(prisoesEncerrar);
	}

	public Map<ProcessoParte, Boolean> getReusSelecionadosMap(){
		return reusSelecionadosMap;
	}

	public void setReusSelecionadosMap(Map<ProcessoParte, Boolean> reusSelecionadosMap){
		this.reusSelecionadosMap = reusSelecionadosMap;
	}

	public List<ProcessoParte> getListaReus(){
		List<ProcessoParte> pps = getHome().getProcessoTrf().getListaPartePrincipalPassivo();
		if (getReusSelecionadosMap().isEmpty()){
			for (ProcessoParte pp : pps){
				getReusSelecionadosMap().put(pp, false);
			}
		}
		return pps;
	}

}
