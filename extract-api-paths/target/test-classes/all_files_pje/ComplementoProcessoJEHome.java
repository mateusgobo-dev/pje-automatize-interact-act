package br.jus.je.pje.home;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.component.AbstractHome;
import br.jus.cnj.pje.servicos.DistribuicaoService;
import br.jus.je.pje.suggest.MunicipioEleicaoSuggestBean;
import br.jus.pje.je.entidades.ComplementoProcessoJE;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ComplementoProcessoJEHome.NAME)
@BypassInterceptors
public class ComplementoProcessoJEHome extends AbstractHome<ComplementoProcessoJE> {

	private static final long	serialVersionUID	= 1L;

	public static final String	NAME				= "complementoProcessoJEHome";

	private Estado				estado;
	private Eleicao            eleicao;
	private Municipio		 municipio;

	@Override
	protected ComplementoProcessoJE createInstance() {
		return new ComplementoProcessoJE();
	}

	@Override
	public void newInstance() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (processoTrf.getComplementoJE() != null) {
			ComplementoProcessoJE complemento = processoTrf.getComplementoJE();
			MunicipioEleicaoSuggestBean bean = MunicipioEleicaoSuggestBean.instance();
			bean.setInstance(complemento.getMunicipioEleicao());
			
			this.setEstado(complemento.getEstadoEleicao());
			this.setMunicipio(complemento.getMunicipioEleicao());
			this.setEleicao(complemento.getEleicao());
			
			this.setInstance(complemento);
		} else {
			ComplementoProcessoJE complemento = new ComplementoProcessoJE();
			complemento.setProcessoTrf(processoTrf);
			this.setInstance(complemento);
		}
	}

	public String gravarComplemento() {
		try {
			MunicipioEleicaoSuggestBean bean = MunicipioEleicaoSuggestBean.instance();
			this.municipio = bean.getInstance();

			if ((this.estado == null) || (this.estado.getIdEstado() == 0)) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O estado deve ser selecionado");
				return null;
			}
			
			if (this.municipio == null) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O município deve ser selecionado");
				return null;
			}			

			if (this.eleicao == null) {
				// Se o processo se enquadra na prevenção prevista no art. 260 do Código Eleitoral
				if (DistribuicaoService.instance().verificarEnquadramentoPEO(this.getInstance().getProcessoTrf())) {
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O ano da eleição deve ser selecionado, "
							+ "pois este processo poderá ser enquadrado na prevenção do art. 260 do Código Eleitoral.");
					return null;
				}
			}
			
			boolean dadosForamAlterados = dadosForamAlterados();
			
			getInstance().setEleicao(this.eleicao);
			getInstance().setEstadoEleicao(this.estado);
			getInstance().setMunicipioEleicao(this.municipio);
			if (getInstance().getDtAtualizacao() == null)
			{
			 	getInstance().setDtAtualizacao(new Date());
			}
			if (getInstance().getProcessoTrf() == null)
			{
				getInstance().setProcessoTrf(ProcessoTrfHome.instance().getInstance());
			}
			getInstance().getProcessoTrf().setComplementoJE(getInstance());
			
			if (getInstance().getProcessoTrf().getNumeroSequencia()  != null && dadosForamAlterados)
			{
			 	// [PJEII-3651] [PJEII-10539]
			 	// Verifica se o processo possui dados para entrar em uma cadeia de prevenção 260, e se está dentro de alguma cadeia (retificação)
				boolean retirarDaCadeia260 = DistribuicaoService.instance().verificarPrevencaoArt260DadosEleitoriais(getInstance().getProcessoTrf());
				if (retirarDaCadeia260)
				{
					getInstance().setVinculacaoDependenciaEleitoral(null);
					getInstance().setDtAtualizacao(new Date());
				}
			}
			 
			String persist = persist();
			
			FacesMessages.instance().clear();
			
			if (StringUtils.isNotBlank(persist)){
				FacesMessages.instance().add(StatusMessage.Severity.INFO, "Dados eleitorais alterados corretamente");
			}else{
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao vincular dados eleitorais");
			}
			return persist;
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao vincular dados eleitorais");
			return null;
		}
	}
	
	private boolean dadosForamAlterados() {
		Eleicao eleicao = getInstance().getEleicao();

		if (eleicao != null && !eleicao.equals(this.eleicao)) {
			return true;
		}

		if (eleicao != null && eleicao.isGeral()) {
			if (getInstance().getEstadoEleicao() != null  && !getInstance().getEstadoEleicao().equals(this.estado)) {
				return true;
			}
		} else {
			if (getInstance().getMunicipioEleicao() != null && !getInstance().getMunicipioEleicao().equals(this.municipio)) {
				return true;
			}
		}

		return false;
	}

	public Estado getEstado() {
		return this.estado;
	}

	public void setEstado(Estado estado) {
		if (this.estado != null) {
			if ((estado == null) || (!this.estado.getEstado().equals(estado.getEstado()))) {
				getMunicipioEleicaoSuggest().setInstance(null);
			}
		}
		this.estado = estado;
	}
	
	private MunicipioEleicaoSuggestBean getMunicipioEleicaoSuggest() {
		MunicipioEleicaoSuggestBean municipioEleicaoSuggest = (MunicipioEleicaoSuggestBean) Component
				.getInstance("municipioEleicaoSuggest");
		return municipioEleicaoSuggest;
	}
	
	public Eleicao getEleicao() {
		return this.eleicao;
	}
	
	public void setEleicao(Eleicao eleicao) {
		this.eleicao = eleicao;
	}
	
	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}
	
	public static ComplementoProcessoJEHome instance() {
		return (ComplementoProcessoJEHome) Component.getInstance(ComplementoProcessoJEHome.NAME);
	}

	
}
