package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.icrrefactory.IcrValidationException;
import br.jus.cnj.pje.business.dao.MandadoAlvaraDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.MandadoAlvaraManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.MandadoAlvara;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

public abstract class AbstractCumprirMandadoAlvaraAction<E extends MandadoAlvara, M extends MandadoAlvaraManager<E, ? extends MandadoAlvaraDAO<E>>> implements Serializable{

	private static final long serialVersionUID = 5286795049068112289L;

	@In(create = false, required = true)
	private FacesMessages facesMessages;

	@In
	private EntityManager entityManager;

	private E processoExpedienteCriminalEdit;
	private Date dataInicio;
	private Date dataFim;
	private int passo;
	private int maxPasso;
	private Boolean cumprirExpediente;
	private String previewDocs;	
	private List<ProcessoEvento> movimentacoes = new ArrayList<ProcessoEvento>(0);
	private List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(0);

	public abstract M getManager();
	public abstract void gravar();

	@Create
	public void init(){
		cumprirExpediente = false;
		dataInicio = null;
		dataFim = null;
		movimentacoes.clear();
		passo = maxPasso;
	}

	public void informarCumprimento(){
		try{
			pesquisarMovimentacao();
		} catch (IcrValidationException e){
			facesMessages.add(Severity.ERROR, "pje.cumprirMandadoPrisao.error.recuperarMovimentacoes");
		}
	}

	protected void limparCamposPesquisaMovimentacao(){
		dataInicio = null;
		dataFim = null;
	}

	public void prepararAssuntos(){
		try{
			assuntos = getManager().recuperarAssuntosUltimaTipificacao(
					(PessoaFisica) getProcessoExpedienteCriminalEdit().getPessoa(),
					getProcessoExpedienteCriminalEdit().getProcessoTrf());

		} catch (PJeBusinessException e){
			e.printStackTrace();
		}

		if (assuntos == null){
			assuntos = getProcessoExpedienteCriminalEdit().getProcessoTrf().getAssuntoTrfList();
		}
	}

	public void vincularMovimentacao(ProcessoEvento processoEvento){
		if (!getProcessoExpedienteCriminalEdit().getProcessoEventoList().contains(processoEvento)){
			getProcessoExpedienteCriminalEdit().getProcessoEventoList().add(processoEvento);
			getMovimentacoes().remove(processoEvento);
		}
	}

	public void desvincularMovimentacao(ProcessoEvento processoEvento){
		getMovimentacoes().add(processoEvento);
		getProcessoExpedienteCriminalEdit().getProcessoEventoList().remove(processoEvento);
	}	
	
	@End
	public void cancelar(){
		getEntityManager().refresh(getProcessoExpedienteCriminalEdit());
		processoExpedienteCriminalEdit = null;
		PesquisarExpedientesCriminaisHome  pesquisarExpedientesCriminaisHome = (PesquisarExpedientesCriminaisHome)Contexts.getConversationContext().get("pesquisarExpedientesCriminaisHome");
		pesquisarExpedientesCriminaisHome.setTab("searchTab");
	}

	public FacesMessages getFacesMessages(){
		return facesMessages;
	}

	public List<ProcessoEvento> getMovimentacoes(){
		return movimentacoes;
	}

	public void setMovimentacoes(List<ProcessoEvento> movimentacoes){
		this.movimentacoes = movimentacoes;
	}

	public int getPasso(){
		return passo;
	}

	public void setPasso(int passo){
		this.passo = passo;
		/*
		 * if(passo == 2){ //visualizarDocumentos(); StringBuilder html = new StringBuilder(); html.append(" <div id='divVi'> "); String style =
		 * "width:100%;border:1px solid #000;background:#FFF; padding:10px 10px 10px 10px;"; html.append("<table style=\"" + style + "\">");
		 * html.append("<tr>"); html.append("<td>");
		 * html.append(processoExpedienteCriminalEdit.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento()); html.append("</td>");
		 * html.append("</tr>"); html.append("</table>"); html.append("<br />"); html.append(" <div> "); previewDocs = html.toString(); }
		 */
	}

	public int getMaxPasso(){
		return maxPasso;
	}

	public void setMaxPasso(int maxPasso){
		this.maxPasso = maxPasso;
	}

	public E getProcessoExpedienteCriminalEdit(){
		return processoExpedienteCriminalEdit;
	}

	public void setProcessoExpedienteCriminalEdit(E processoExpedienteCriminalEdit){
		this.processoExpedienteCriminalEdit = processoExpedienteCriminalEdit;
	}

	public Date getDataInicio(){
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio){
		this.dataInicio = dataInicio;
	}

	public Date getDataFim(){
		return dataFim;
	}

	public void setDataFim(Date dataFim){
		this.dataFim = dataFim;
	}

	public Boolean getCumprirExpediente(){
		return cumprirExpediente;
	}

	public void setCumprirExpediente(Boolean cumprirExpediente){
		this.cumprirExpediente = cumprirExpediente;
	}

	public void pesquisarMovimentacao() throws IcrValidationException{
		setMovimentacoes(getManager().getMovimentacoesNaoVinculadas(getProcessoExpedienteCriminalEdit(),
				getDataInicio(), getDataFim(), null));
	}

	public List<AssuntoTrf> getAssuntos(){
		return assuntos;
	}

	public void setAssuntos(List<AssuntoTrf> assuntos){
		this.assuntos = assuntos;
	}

	public String getPreviewDocs(){
		return previewDocs;
	}

	public void setPreviewDocs(String previewDocs){
		this.previewDocs = previewDocs;
	}

	public EntityManager getEntityManager(){
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager){
		this.entityManager = entityManager;
	}

	public Boolean isEditavel(){
		return (getProcessoExpedienteCriminalEdit() != null
					&& getProcessoExpedienteCriminalEdit().getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.PC 
					&& getProcessoExpedienteCriminalEdit() instanceof MandadoAlvara);
	}
}
