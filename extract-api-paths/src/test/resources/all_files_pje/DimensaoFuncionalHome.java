package br.com.infox.cliente.home;

import java.util.Arrays;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;

import br.com.infox.cliente.component.suggest.DimensaoFuncionalPessoaAutoridadeSuggestBean;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.AssociacaoDimensaoPessoalEnum;
import br.jus.pje.nucleo.entidades.AutoridadeAfetada;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.DimensaoFuncional;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@SuppressWarnings("serial")
@Name("dimensaoFuncionalHome")
@Scope(ScopeType.CONVERSATION)
public class DimensaoFuncionalHome extends AbstractHome<DimensaoFuncional> {
	
	private AssociacaoDimensaoPessoalEnum tipoAssociacao;

	private ProcessoParteParticipacaoEnum polo;
	
	private List<AssociacaoDimensaoPessoalEnum> tiposAssociacao;
	
	private List<ProcessoParteParticipacaoEnum> polos;
	
	public DimensaoFuncionalHome(){
		tiposAssociacao = Arrays.asList(AssociacaoDimensaoPessoalEnum.values());
		polos = Arrays.asList(ProcessoParteParticipacaoEnum.values());
	}

	public void setIdDimensaoFuncional(Integer id) {
		setId(id);
	}

	public Integer getIdDimensaoFuncional() {
		return (Integer) getId();
	}
	
	public String remove(AutoridadeAfetada... autoridadeAfetada){
		for(AutoridadeAfetada aa: autoridadeAfetada){
			int index = indexOf(aa);
			if(index > -1){
				instance.getAutoridadesAfetadas().remove(index);
			}
		}
		persist();			
		refreshGrid("dimensaoFuncionalGrid");
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage( "dimensaoFuncional_excluida"));
		return "updated";
	}
	
	
	public String remove(AutoridadeAfetada autoridadeAfetada){
		return remove(new AutoridadeAfetada[]{autoridadeAfetada});
	}

	public void addDimensaoFuncionalToCompetencia() {
		CompetenciaHome competenciaHome = (CompetenciaHome) getComponent("competenciaHome");

		Competencia competencia = competenciaHome.getInstance();
		if (!competencia.getDimensaoFuncionalList().contains(instance)) {
			competencia.getDimensaoFuncionalList().add(instance);
			competenciaHome.persist();
		}

		refreshGrid("dimensaoFuncionalGrid");
	}

	@Override
	public String persist() {

		StringBuilder msg = new StringBuilder();
		if (super.isManaged()) {
			super.update();
			if (!getInstance().getAtivo()) {

				msg.append(FacesUtil.getMessage(
						"dimensaoFuncional_inativado"));
			} else {

				msg.append(FacesUtil.getMessage(
						"dimensaoFuncional_atualizacao"));
			}

		} else {
			super.persist();
			msg.append(FacesUtil.getMessage(
					"dimensaoFuncional_inserida"));
		}

		refreshGrid("dimensaoFuncionalCrudGrid");
		limparMensagens();
		FacesMessages.instance().add(msg.toString());
		return "";
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		//newInstance();
		return ret;
	}

	public void adicionarAutoridade() {
		PessoaAutoridade autoridade = ((DimensaoFuncionalPessoaAutoridadeSuggestBean) getComponent("dimensaoFuncionalPessoaAutoridadeSuggest"))
				.getInstance();
		if (autoridade == null){ 
			FacesMessages.instance().add("Campo autoridade deve ser preenchido!");
			return;
		}
		AutoridadeAfetada autAf = new AutoridadeAfetada();
		autAf.setAutoridade(autoridade);
		autAf.setDimensaoFuncional(instance);
		autAf.setPolo(polo);
		autAf.setTipoRestricao(tipoAssociacao);
		
		
		boolean autoridadeJaInclusa = indexOf(autAf) > -1;
		
		
		if(!autoridadeJaInclusa){
			instance.getAutoridadesAfetadas().add(autAf);
		}
		

		getEntityManager().flush();
		limparMensagens();		
		FacesMessages.instance().add(FacesUtil.getMessage("dimensaoFuncional_inserida"));
	}

	private int indexOf(AutoridadeAfetada autoridadeAfetada){
		for(AutoridadeAfetada item : instance.getAutoridadesAfetadas()){
			if(autoridadeAfetada.getAutoridade().equals(item.getAutoridade()) && 
					autoridadeAfetada.getPolo().equals(item.getPolo()) && 
					autoridadeAfetada.getTipoRestricao().equals(item.getTipoRestricao())){
				return instance.getAutoridadesAfetadas().indexOf(item);
			}
			
		}
		
		return -1;
	}
	
	@Override
	public String remove(DimensaoFuncional dimensaoFuncional) {
		CompetenciaHome competenciaHome = (CompetenciaHome) getComponent("competenciaHome");

		Competencia competencia = competenciaHome.getInstance();
		competencia.getDimensaoFuncionalList().remove(dimensaoFuncional);
		competenciaHome.persist();

		refreshGrid("dimensaoFuncionalGrid");

		limparMensagens();
		FacesMessages.instance().add(FacesUtil.getMessage("dimensaoFuncional_excluida"));

		return "";
	}

	@SuppressWarnings("unchecked")
	public List<DimensaoFuncional> getDimensaoFuncionalItens() {
		String query = "SELECT o from DimensaoFuncional AS o ";

		return getEntityManager().createQuery(query).getResultList();

	}

	@Override
	public void newInstance() {
		((DimensaoFuncionalPessoaAutoridadeSuggestBean) getComponent("dimensaoFuncionalPessoaAutoridadeSuggest"))
				.setInstance(null);
		super.newInstance();
		instance.getAutoridadesAfetadas().clear();
	}

	public AssociacaoDimensaoPessoalEnum getTipoAssociacao() {
		return tipoAssociacao;
	}

	public void setTipoAssociacao(AssociacaoDimensaoPessoalEnum tipoAssociacao) {
		this.tipoAssociacao = tipoAssociacao;
	}

	public ProcessoParteParticipacaoEnum getPolo() {
		return polo;
	}

	public void setPolo(ProcessoParteParticipacaoEnum polo) {
		this.polo = polo;
	}

	public List<AssociacaoDimensaoPessoalEnum> getTiposAssociacao() {
		return tiposAssociacao;
	}

	public List<ProcessoParteParticipacaoEnum> getPolos() {
		return polos;
	}

}
