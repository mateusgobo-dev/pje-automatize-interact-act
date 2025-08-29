package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.component.AbstractHome;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.AlvaraSoltura;
import br.jus.pje.nucleo.entidades.ContraMandado;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;
import br.jus.pje.nucleo.enums.TipoExpedienteCriminalEnum;

@Scope(ScopeType.CONVERSATION)
@Name("pesquisarExpedientesCriminaisHome")
public class PesquisarExpedientesCriminaisHome extends AbstractHome<ProcessoExpedienteCriminal> {

	private static final long serialVersionUID = 1L;

	@In(create = true)
	private MandadoPrisaoManager mandadoPrisaoManager;

	private TipoExpedienteCriminalEnum tipoExpediente;
	private SituacaoExpedienteCriminalEnum situacaoExpediente;
	private String numeroProcesso;
	private String numeroDocumento;
	private String nomePessoa;
	private Date dtInicio;
	private Date dtTermino;
	private String formularioCumprimento;	
	private TipoExpedienteCriminalEnum[] tiposExpedientes = TipoExpedienteCriminalEnum.values();
	private SituacaoExpedienteCriminalEnum[] situacoesExpedientes = SituacaoExpedienteCriminalEnum.values();
	private List<ProcessoExpedienteCriminal> expedientesCriminais = new ArrayList<ProcessoExpedienteCriminal>();
	
	@Override
	public void create(){	
		super.create();
		formularioCumprimento = "abaCumprirMandadoPrisao.xhtml";
		setTab("searchTab");
	}

	public void pesquisarExpedientesCriminais() {
		expedientesCriminais = mandadoPrisaoManager.pesquisarExpedientesCriminais(tipoExpediente, numeroProcesso,
				nomePessoa, numeroDocumento, dtInicio, dtTermino, situacaoExpediente);
	}

	public void editarExpedienteCriminal(ProcessoExpedienteCriminal expediente) {
		setTab("formulario");		
		
		if(expediente instanceof MandadoPrisao){
			CumprirMandadoPrisaoAction c = getComponent("cumprirMandadoPrisaoAction");
			c.setProcessoExpedienteCriminalEdit((MandadoPrisao)expediente);
			c.prepararAssuntos();
			c.setPasso(2);
			formularioCumprimento = "abaCumprirMandadoPrisao.xhtml";			
		}else if(expediente instanceof AlvaraSoltura){
			CumprirAlvaraSolturaAction c = getComponent("cumprirAlvaraSolturaAction");
			c.setProcessoExpedienteCriminalEdit((AlvaraSoltura)expediente);
			c.prepararAssuntos();
			c.setPasso(2);
			formularioCumprimento = "abaCumprirAlvaraSoltura.xhtml";			
		}else if(expediente instanceof ContraMandado){
			CumprirContraMandadoAction c = getComponent("cumprirContraMandadoAction");
			c.setProcessoExpedienteCriminalEdit((ContraMandado)expediente);
			c.setPasso(1);
			formularioCumprimento = "abaCumprirContraMandado.xhtml";
		}
	}
	
	@Override
	public void onClickFormTab() {
		/*
		 * Deve permancer na tab de pesquisa,
		 * pois existe a possibilidade
		 * de criar um registro do zero.
		 * Só deve ir p/ o formulário
		 * se selecionar um registro da pesquisa
		 */
		setTab("searchTab");
	}
	
	public void limparCamposPesquisa(){
		tipoExpediente = null;
		situacaoExpediente = null;
		numeroProcesso = null;
		nomePessoa = null;
		numeroDocumento = null;
		dtInicio = null;
		dtTermino = null;
	}
	
	@End
	public void onClickSearchTab() {
		super.onClickSearchTab();
	}

	public TipoExpedienteCriminalEnum[] getTiposExpedientes() {
		return tiposExpedientes;
	}

	public void setTiposExpedientes(TipoExpedienteCriminalEnum[] tiposExpedientes) {
		this.tiposExpedientes = tiposExpedientes;
	}

	public SituacaoExpedienteCriminalEnum[] getSituacoesExpedientes() {
		return situacoesExpedientes;
	}

	public void setSituacoesExpedientes(SituacaoExpedienteCriminalEnum[] situacoesExpedientes) {
		this.situacoesExpedientes = situacoesExpedientes;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public Date getDtInicio() {
		return dtInicio;
	}

	public void setDtInicio(Date dtInicio) {
		this.dtInicio = dtInicio;
	}

	public Date getDtTermino() {
		return dtTermino;
	}

	public void setDtTermino(Date dtTermino) {
		this.dtTermino = dtTermino;
	}

	public List<ProcessoExpedienteCriminal> getExpedientesCriminais() {
		return expedientesCriminais;
	}

	public void setExpedientesCriminais(List<ProcessoExpedienteCriminal> expedientesCriminais) {
		this.expedientesCriminais = expedientesCriminais;
	}

	public SituacaoExpedienteCriminalEnum getSituacaoExpediente() {
		return situacaoExpediente;
	}

	public void setSituacaoExpediente(SituacaoExpedienteCriminalEnum situacaoExpediente) {
		this.situacaoExpediente = situacaoExpediente;
	}

	public TipoExpedienteCriminalEnum getTipoExpediente() {
		return tipoExpediente;
	}

	public void setTipoExpediente(TipoExpedienteCriminalEnum tipoExpediente) {
		this.tipoExpediente = tipoExpediente;
	}
	
	public String getFormularioCumprimento(){
		return formularioCumprimento;
	}
	
	public void setFormularioCumprimento(String formularioCumprimento){
		this.formularioCumprimento = formularioCumprimento;
	}
}
