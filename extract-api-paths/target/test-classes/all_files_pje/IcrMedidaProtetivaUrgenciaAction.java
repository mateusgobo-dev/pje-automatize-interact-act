package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Transient;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.jus.pje.nucleo.entidades.IcrFuga;
import br.jus.pje.nucleo.entidades.IcrMedidaProtetivaUrgencia;
import br.jus.pje.nucleo.entidades.IcrPrisao;
import br.jus.pje.nucleo.entidades.MedidaProtetivaPessoasAfastamento;
import br.jus.pje.nucleo.entidades.MedidaProtetivaUrgencia;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoLocalProibicao;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.enums.MotivoEncerramentoPrisaoEnum;
import br.jus.pje.nucleo.enums.TipoLocalMedidaProtetivaEnum;
import br.jus.pje.nucleo.enums.TipoMedidaProtetivaUrgenciaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("icrMedidaProtetivaUrgenciaAction")
@Scope(ScopeType.CONVERSATION)
public class IcrMedidaProtetivaUrgenciaAction
		extends
		InformacaoCriminalRelevanteAction<IcrMedidaProtetivaUrgencia, IcrMedidaProtetivaUrgenciaManager>{

	private static final long serialVersionUID = 4309473630294788545L;
	private ProcessoTrf processoJudicial;
	private Date dataPrimeiroComparecimento;
	private List<TipoLocalProibicao> tipoLocalProibicaoList;

	@Transient
	public Date getDataPrimeiroComparecimento(){
		return dataPrimeiroComparecimento;
	}

	public void setDataPrimeiroComparecimento(Date dataPrimeiroComparecimento){
		this.dataPrimeiroComparecimento = dataPrimeiroComparecimento;
	}

	public List<TipoLocalProibicao> getTipoLocalProibicaoList(){
		if (tipoLocalProibicaoList == null || tipoLocalProibicaoList.isEmpty()){
			tipoLocalProibicaoList = getManager().getTipoLocalProibicaoList();
		}
		Collections.sort(tipoLocalProibicaoList);
		return tipoLocalProibicaoList;
	}

	@Override
	public void init(){
		super.init();
		tipoMedidaProtetivaUrgenciaList = getManager()
				.getTipoMedidaProtetivaUrgenciaEnumList();
		for (MedidaProtetivaUrgencia medida : getInstance()
				.getMedidasProtetivasUrgencia()){
			getTipoMedidaProtetivaUrgenciaList().remove(medida.getTipo());
		}
		this.processoJudicial = getHome().getProcessoTrf();
	}

	public ProcessoTrf getProcessoJudicial(){
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial){
		this.processoJudicial = processoJudicial;
	}

	public TipoLocalMedidaProtetivaEnum[] getTipoLocalList(){
		return TipoLocalMedidaProtetivaEnum.values();
	}

	List<TipoMedidaProtetivaUrgenciaEnum> tipoMedidaProtetivaUrgenciaList = new ArrayList<TipoMedidaProtetivaUrgenciaEnum>(
			0);

	public List<TipoMedidaProtetivaUrgenciaEnum> getTipoMedidaProtetivaUrgenciaList(){
		Collections.sort(tipoMedidaProtetivaUrgenciaList);
		return tipoMedidaProtetivaUrgenciaList;
	}

	public void removerMedidaProtetiva(MedidaProtetivaUrgencia medidaProtetiva){
		if (!getTipoMedidaProtetivaUrgenciaList().contains(
				medidaProtetiva.getTipo()))
			getTipoMedidaProtetivaUrgenciaList().add(medidaProtetiva.getTipo());
		getInstance().getMedidasProtetivasUrgencia().remove(medidaProtetiva);
	}

	public void adicionarTipoMedidaProtetiva(
			TipoMedidaProtetivaUrgenciaEnum tipo){
		try{
			MedidaProtetivaUrgencia medida = MedidaProtetivaUrgencia
					.getInstance(tipo);
			medida.setIcr(getInstance());
			getInstance().getMedidasProtetivasUrgencia().add(medida);
			if (getTipoMedidaProtetivaUrgenciaList().contains(tipo)){
				getTipoMedidaProtetivaUrgenciaList().remove(tipo);
			}
		} catch (InstantiationException e){
			e.printStackTrace();
		} catch (IllegalAccessException e){
			e.printStackTrace();
		}
	}

	public boolean exibirDataPrimeiroComparecimento(){
		return false;
	}

	@Override
	public void abreTabProximoPasso(){
		getHome().showTabMedidasProtetivasUrgencia();
	}

	private MedidaProtetivaPessoasAfastamento medidaProtetivaPessoasAfastamentoTemp;

	public MedidaProtetivaPessoasAfastamento getMedidaProtetivaPessoasAfastamentoTemp(){
		if (medidaProtetivaPessoasAfastamentoTemp == null)
			medidaProtetivaPessoasAfastamentoTemp = new MedidaProtetivaPessoasAfastamento();
		return medidaProtetivaPessoasAfastamentoTemp;
	}

	public void setMedidaProtetivaPessoasAfastamentoTemp(MedidaProtetivaPessoasAfastamento medidaProtetivaPessoasAfastamentoTemp){
		this.medidaProtetivaPessoasAfastamentoTemp = medidaProtetivaPessoasAfastamentoTemp;
	}

	public void adicionarPessoaAfastamentoTemp(){
		adicionaPessoaAfastamento(getMedidaProtetivaPessoasAfastamentoTemp().getMedidaProtetivaUrgencia(), getMedidaProtetivaPessoasAfastamentoTemp()
				.getPessoa());
	}

	public void removerPessoaAfastamento(MedidaProtetivaPessoasAfastamento medidaPessoa){
		if (medidaPessoa.getId() == null){
			medidaPessoa.getMedidaProtetivaUrgencia().getMedidasProtetivasPessoasAfastamento().remove(medidaPessoa);
		}
		else{
			medidaPessoa.setAtivo(false);
		}
	}

	public void adicionaParte(ProcessoParte pp){
		adicionaPessoaAfastamento(getMedidaProtetivaPessoasAfastamentoTemp().getMedidaProtetivaUrgencia(), pp.getPessoa());
	}

	public void adicionaPartes(List<ProcessoParte> pps){
		for (ProcessoParte pp : pps)
			adicionaParte(pp);
	}
	
	@Override
	public void next(){
		if (getInstance().getMedidasProtetivasUrgencia().isEmpty()){
			FacesMessages.instance().add(Severity.ERROR,
				"Deve existir pelo menos uma medida protetiva de urgência que obriga o agressor associada.");
		}else{
			super.next();
		}
	}

	public void adicionaPessoaAfastamento(
			MedidaProtetivaUrgencia medidaProtetiva, Pessoa pessoa){
		if (medidaProtetiva == null
			|| pessoa == null)
			return;
		MedidaProtetivaPessoasAfastamento medidaPessoaTemp = new MedidaProtetivaPessoasAfastamento(
				medidaProtetiva, pessoa);
		if (!medidaProtetiva
				.getMedidasProtetivasPessoasAfastamento().contains(medidaPessoaTemp)){
			medidaProtetiva
					.getMedidasProtetivasPessoasAfastamento().add(medidaPessoaTemp);
		}
		else{
			FacesMessages.instance().add(Severity.ERROR,
					"Pessoa já associada: " + pessoa.getNome());
		}

	}

}
