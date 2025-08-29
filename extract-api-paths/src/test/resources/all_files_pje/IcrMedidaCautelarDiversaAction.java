package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.pje.nucleo.entidades.AcompanhamentoMedidaCautelar;
import br.jus.pje.nucleo.entidades.IcrMedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.MedidaCautelarDiversa;
import br.jus.pje.nucleo.entidades.MedidaCautelarPessoaAfastamento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.TipoLocalProibicao;
import br.jus.pje.nucleo.enums.FrequenciaComparecimentoEmJuizo;
import br.jus.pje.nucleo.enums.TipoMedidaCautelarDiversaEnum;

@Name("icrMedidaCautelarDiversaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrMedidaCautelarDiversaAction extends
		InformacaoCriminalRelevanteAction<IcrMedidaCautelarDiversa, IcrMedidaCautelarDiversaManager>{

	private static final long serialVersionUID = 3271907892705542980L;
	List<TipoLocalProibicao> tipoLocalProibicaoList;
	List<TipoMedidaCautelarDiversaEnum> tipoMedidaCautelarDiversaList = new ArrayList<TipoMedidaCautelarDiversaEnum>(0);
	private MedidaCautelarPessoaAfastamento medidaCautelarPessoaAfastamentoTemp;
	private Date dataPrimeiroComparecimento;
	private AcompanhamentoMedidaCautelar acompanhamentoEdit;
	
	private String tab = null;

	public String getTab(){
		return tab;
	}

	public void setTab(String tab){
		this.tab = tab;
	}

	@Transient
	@Temporal(TemporalType.DATE)
	public Date getDataPrimeiroComparecimento(){
		return dataPrimeiroComparecimento;
	}

	public void setDataPrimeiroComparecimento(Date dataPrimeiroComparecimento){
		this.dataPrimeiroComparecimento = dataPrimeiroComparecimento;
	}

	public List<TipoMedidaCautelarDiversaEnum> getTipoMedidaCautelarDiversaList(){
		Collections.sort(tipoMedidaCautelarDiversaList);
		return tipoMedidaCautelarDiversaList;

	}

	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		if (tipoLocalProibicaoList == null || tipoLocalProibicaoList.isEmpty()){
			tipoLocalProibicaoList = getManager().getTipoLocalProibicaoList();
		}
		Collections.sort(tipoLocalProibicaoList);
		return tipoLocalProibicaoList;
	}

	public FrequenciaComparecimentoEmJuizo[] getFrequenciaComparecimentoEmJuizoList(){
		return FrequenciaComparecimentoEmJuizo.values();
	}

	public void setMedidaCautelarPessoaAfastamentoTemp(MedidaCautelarPessoaAfastamento passoaAfastamento){
		this.medidaCautelarPessoaAfastamentoTemp = passoaAfastamento;
	}

	public MedidaCautelarPessoaAfastamento getMedidaCautelarPessoaAfastamentoTemp(){
		if (medidaCautelarPessoaAfastamentoTemp == null)
			medidaCautelarPessoaAfastamentoTemp = new MedidaCautelarPessoaAfastamento();
		return medidaCautelarPessoaAfastamentoTemp;
	}

	public void removerPessoaAfastamento(MedidaCautelarPessoaAfastamento medidaPessoa){
		if (medidaPessoa.getId() == null){
			medidaPessoa.getMedidaCautelarDiversa().getMedidaCautelarPessoaAfastamento().remove(medidaPessoa);
		}
		else{
			medidaPessoa.setAtivo(false);
		}
	}

	public void adicionarPessoaAfastamentoTemp(){
		adicionaPessoaAfastamento(getMedidaCautelarPessoaAfastamentoTemp().getMedidaCautelarDiversa(), getMedidaCautelarPessoaAfastamentoTemp()
				.getPessoaAfastemento());
	}

	public void adicionaPessoaAfastamento(
			MedidaCautelarDiversa medida, Pessoa pessoa){
		if (medida == null
			|| pessoa == null)
			return;
		MedidaCautelarPessoaAfastamento medidaPessoaTemp = new MedidaCautelarPessoaAfastamento(
				medida, pessoa);
		if (!medida
				.getMedidaCautelarPessoaAfastamento().contains(medidaPessoaTemp)){
			medida
					.getMedidaCautelarPessoaAfastamento().add(medidaPessoaTemp);
		}
		else{
			FacesMessages.instance().add(Severity.ERROR,
					"Pessoa já associada: " + pessoa.getNome());
		}

	}

	public void adicionaParte(ProcessoParte pp){
		adicionaPessoaAfastamento(getMedidaCautelarPessoaAfastamentoTemp().getMedidaCautelarDiversa(), pp.getPessoa());
	}

	public void adicionaPartes(List<ProcessoParte> pps){
		for (ProcessoParte pp : pps)
			adicionaParte(pp);
	}

	@Override
	public void init(){
		super.init();
		try{
			if (getInstance() != null && !getInstance().getProcessoEventoList().isEmpty())
				ProcessoTrfHome.instance().setId(getInstance().getProcessoEventoList().get(0).getProcesso().getIdProcesso());
		} catch (Exception e){
		}
		tipoMedidaCautelarDiversaList = getManager().getTipoMedidaCautelarDiversaEnumList();
		for (MedidaCautelarDiversa medida : getInstance().getMedidasCautelaresDiversas()){
			if (medida.getAtivo())
				getTipoMedidaCautelarDiversaList().remove(medida.getTipo());
		}
	}

	public boolean exibirDataPrimeiroComparecimento(){
		for (MedidaCautelarDiversa medida : getInstance().getMedidasCautelaresDiversas()){
			if (medida.getAtivo() && TipoMedidaCautelarDiversaEnum.CPP319I.equals(medida.getTipo()))
				return true;
		}
		return false;
	}
	
	public MedidaCautelarDiversa getMedidaCautelar319I(){
		
		for (MedidaCautelarDiversa medida : getInstance().getMedidasCautelaresDiversas()){
			if (TipoMedidaCautelarDiversaEnum.CPP319I.equals(medida.getTipo())){
				return medida;
			}
				
		}
		return null;
	
	}
	
	@Override
	public void abreTabProximoPasso(){
		getHome().showTabMedidasCautelaresDiversas();
	}

	public void adicionarTipoMedidaCautelar(TipoMedidaCautelarDiversaEnum tipo){
		MedidaCautelarDiversa medida = new MedidaCautelarDiversa(tipo);
		medida.setIcr(getInstance());
		getInstance().getMedidasCautelaresDiversas().add(medida);
		if (getTipoMedidaCautelarDiversaList().contains(tipo))
			getTipoMedidaCautelarDiversaList().remove(tipo);
	}

	public void removerMedidaCautelar(MedidaCautelarDiversa medidaCautelar){
		if (!getTipoMedidaCautelarDiversaList().contains(medidaCautelar.getTipo()))
			getTipoMedidaCautelarDiversaList().add(medidaCautelar.getTipo());
		if (medidaCautelar.getId() == null){
			getInstance().getMedidasCautelaresDiversas().remove(medidaCautelar);
		}
		else{
			medidaCautelar.setAtivo(false);
		}
	}
	
	/*********** OPERAÇÕES DE ACOMPANHAMENTO ***********/
	
	public void adicionarAcompanhamento(MedidaCautelarDiversa medida){
		if (medida != null){
			setTab("acompanharMedidasCautelaresTab");
			getAcompanhamentoEdit().setMedidaCautelarDiversa(medida);
		}
	}
	
	public AcompanhamentoMedidaCautelar getAcompanhamentoEdit(){
		if (acompanhamentoEdit == null){
			novoAcompanhamento();
		}
		return acompanhamentoEdit;
	}
	
	public void setAcompanhamentoEdit(AcompanhamentoMedidaCautelar acompanhamentoEdit){
		this.acompanhamentoEdit = acompanhamentoEdit;
	}

	public void carregarAcompanhamentos(){
		acompanhamentoEdit = new AcompanhamentoMedidaCautelar();
	}
	
	public void adicionarTarefa(){
		if (!acompanhamentoEdit.getMedidaCautelarDiversa().getAcompanhamentos().contains(acompanhamentoEdit)){
			acompanhamentoEdit.getMedidaCautelarDiversa().getAcompanhamentos().add(acompanhamentoEdit);
			novoAcompanhamento();	
		}
	}
	
	public void novoAcompanhamento(){
		Integer numeroSequencia = 1;
		if(acompanhamentoEdit != null){
		MedidaCautelarDiversa medida = acompanhamentoEdit.getMedidaCautelarDiversa();
		List<AcompanhamentoMedidaCautelar> l = medida.getAcompanhamentos();
		Integer max = 0;
		Integer tmp = 0;
		for(AcompanhamentoMedidaCautelar x: l) {
			tmp = x.getNumeroSequencia();
			if (tmp != null && tmp > max) max = tmp;
		}
		
			numeroSequencia = ++max;
		}
		
		acompanhamentoEdit = new AcompanhamentoMedidaCautelar();
		acompanhamentoEdit.setNumeroSequencia(numeroSequencia);
		acompanhamentoEdit.setMedidaCautelarDiversa(getMedidaCautelar319I());
	}
	
	public void editarAcompanhamento(AcompanhamentoMedidaCautelar acompanhamento){
		acompanhamentoEdit = acompanhamento;
	}

	public void removerAcompanhamento(AcompanhamentoMedidaCautelar acompanhamento){
		acompanhamento.getMedidaCautelarDiversa().getAcompanhamentos().remove(acompanhamento);
	}

	
}
