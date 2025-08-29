package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaSuggestBean;
import br.com.infox.cliente.component.suggest.TipoPessoaSuggestBean;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DimensaoPessoalManager;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoPessoal;
import br.jus.pje.nucleo.entidades.DimensaoPessoalPessoa;
import br.jus.pje.nucleo.entidades.DimensaoPessoalTipoPessoa;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@SuppressWarnings("serial")
@Name("dimensaoPessoalHome")
public class DimensaoPessoalHome extends AbstractHome<DimensaoPessoal> {

	private AssociacaoDimensaoPessoalEnum tipoAssociacaoItem;

	private ProcessoParteParticipacaoEnum poloItem;
	
	@In(create=true)
	private DimensaoPessoalManager dimensaoPessoalManager;

	public void setDimensaoPessoalId(Integer id) {
		setId(id);
	}

	public Integer getDimensaoPessoalId() {
		return (Integer) getId();
	}

	public List<AssociacaoDimensaoPessoalEnum> getTipoAssociacaoItens() {
		List<AssociacaoDimensaoPessoalEnum> tipoAssociacaoItens = new ArrayList<AssociacaoDimensaoPessoalEnum>();

		for (AssociacaoDimensaoPessoalEnum associacaoDimensaoPessoalItem : AssociacaoDimensaoPessoalEnum.values()) {
			tipoAssociacaoItens.add(associacaoDimensaoPessoalItem);
		}
		return tipoAssociacaoItens;
	}

	public List<ProcessoParteParticipacaoEnum> getPoloItens() {
		List<ProcessoParteParticipacaoEnum> poloItens = new ArrayList<ProcessoParteParticipacaoEnum>();

		for (ProcessoParteParticipacaoEnum poloItem : ProcessoParteParticipacaoEnum.values()) {
			poloItens.add(poloItem);
		}
		return poloItens;
	}

	public void adicionarPessoa() {
		DimensaoPessoalPessoa dimensaoPessoalPessoa = new DimensaoPessoalPessoa();

		dimensaoPessoalPessoa.setDimensaoPessoal(instance);
		dimensaoPessoalPessoa.setPessoa(((PessoaSuggestBean) getComponent("pessoaSuggest")).getInstance());
		dimensaoPessoalPessoa.setPolo(poloItem);
		dimensaoPessoalPessoa.setTipoAssociacao(tipoAssociacaoItem);
		instance.getPessoasAfetadasList().add(dimensaoPessoalPessoa);

		getEntityManager().flush();
		limparCombos();
		Contexts.removeFromAllContexts("pessoaSuggest");
		dimensaoPessoalPessoa = null;
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_created"));
	}

	public void adicionarTipoPessoa() {
		DimensaoPessoalTipoPessoa dimensaoPessoalTipoPessoa = new DimensaoPessoalTipoPessoa();

		dimensaoPessoalTipoPessoa.setDimensaoPessoal(instance);
		dimensaoPessoalTipoPessoa.setTipoPessoa(((TipoPessoaSuggestBean) getComponent("tipoPessoaSuggest"))
				.getInstance());
		dimensaoPessoalTipoPessoa.setPolo(poloItem);
		dimensaoPessoalTipoPessoa.setTipoAssociacao(tipoAssociacaoItem);

		instance.getTiposDePessoasAfetadosList().add(dimensaoPessoalTipoPessoa);
		getEntityManager().flush();
		limparCombos();
		Contexts.removeFromAllContexts("tipoPessoaSuggest");
		dimensaoPessoalTipoPessoa = null;
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_created"));
	}

	private void limparCombos() {
		tipoAssociacaoItem = null;
		poloItem = null;
	}

	public AssociacaoDimensaoPessoalEnum getTipoAssociacaoItem() {
		return tipoAssociacaoItem;
	}

	public void setTipoAssociacaoItem(AssociacaoDimensaoPessoalEnum tipoAssociacaoItem) {
		this.tipoAssociacaoItem = tipoAssociacaoItem;
	}

	public ProcessoParteParticipacaoEnum getPoloItem() {
		return poloItem;
	}

	public void setPoloItem(ProcessoParteParticipacaoEnum poloItem) {
		this.poloItem = poloItem;
	}

	public void addDimensaoPessoalToCompetencia() {
		CompetenciaHome competenciaHome = (CompetenciaHome) getComponent("competenciaHome");

		Competencia competencia = competenciaHome.getInstance();

		if (!competencia.getDimensaoPessoalList().contains(instance)) {
			competencia.getDimensaoPessoalList().add(instance);
			competenciaHome.persist();
		}
		refreshGrid("dimensaoPessoalGrid");
		newInstance();
	}

	@Override
	public String persist() {
		getEntityManager().persist(instance);
		getEntityManager().flush();
		refreshGrid("dimensaoPessoalGrid");
		FacesMessages.instance().clear();		
		FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_created"));
		return "";
	}

	@Override
	public String remove(DimensaoPessoal dimensaoPessoal) {
		CompetenciaHome competenciaHome = (CompetenciaHome) getComponent("competenciaHome");

		Competencia competencia = competenciaHome.getInstance();
		competencia.getDimensaoPessoalList().iterator();
		competencia.getDimensaoPessoalList().remove(dimensaoPessoal);

		competenciaHome.persist();

		refreshGrid("dimensaoPessoalGrid");
		super.newInstance();
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_deleted"));

		return "";
	}

	public void remove(DimensaoPessoalPessoa dimensaoPessoalPessoa) {
		instance.getPessoasAfetadasList().remove(dimensaoPessoalPessoa);
		getEntityManager().flush();
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage( "competenciaDimensaoPessoal_deleted"));
	}

	public void remove(DimensaoPessoalTipoPessoa dimensaoPessoalTipoPessoa) {
		instance.getTiposDePessoasAfetadosList().remove(dimensaoPessoalTipoPessoa);
		getEntityManager().flush();
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_deleted"));
	}

	@SuppressWarnings("unchecked")
	public List<DimensaoPessoal> getDimensaoPessoalItens() {
		Competencia competencia = CompetenciaHome.instance().getInstance();
		String query = "select o from DimensaoPessoal o "
				+ "where o not in (select d from Competencia c join c.dimensaoPessoalList d "
				+ "where c = :competencia)";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("competencia", competencia);
		return q.getResultList();

	}

	@Override
	public String inactive(DimensaoPessoal instance) {
		setInstance(instance);
		getInstance().setAtivo(Boolean.FALSE);
		super.inactive(getInstance());
		return "";
	}

	@Override
	public void newInstance() {
		super.newInstance();

		tipoAssociacaoItem = null;

		poloItem = null;

		((TipoPessoaSuggestBean) getComponent("tipoPessoaSuggest")).setInstance(null);
		((PessoaSuggestBean) getComponent("pessoaSuggest")).setInstance(null);
		refreshGrid("dimensaoPessoalCrudGrid");
	}
	
	/**
	 * @author Antonio Francisco Osorio JR/TJDFT
	 * Sobrescrita do método update para fragmentar as mensagens na tela.	 * 
	 * PJEII-7221
	 */
	public String update() {
	
		String resultado = super.update();
		 try {
			DimensaoPessoal dimensaoPessoal = dimensaoPessoalManager.findById(getInstance().getIdDimensaoPessoal());
			limparMensagens();		
			if(!getInstance().getAtivo() && getInstance().getDimensaoPessoal().equals(dimensaoPessoal.getDimensaoPessoal())){

				FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_inativado"));
				return resultado;
			}
			FacesMessages.instance().add(FacesUtil.getMessage("competenciaDimensaoPessoal_updated"));
		} catch (PJeBusinessException e) {			
			FacesMessages.instance().add(Severity.ERROR,"Erro ao gravar/alterar.");

		}
		return resultado;
		
	
	}

}
