package br.jus.cnj.pje.view.fluxo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.icrrefactory.IcrPrisaoManager;
import br.com.infox.pje.service.CumprirMandadoPrisaoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.EstabelecimentoPrisional;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;

@Name("cumprirMandadoPrisaoAction")
@Scope(ScopeType.CONVERSATION)
public class CumprirMandadoPrisaoAction extends AbstractCumprirMandadoAlvaraAction<MandadoPrisao, MandadoPrisaoManager>{

	private static final long serialVersionUID = 2699237835816990500L;

	@In
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In
	private CumprirMandadoPrisaoService cumprirMandadoPrisaoService;
	
	@In(create = true)
	private IcrPrisaoManager icrPRIManager;

	private String uf;
	private String cidade;
	private List<String> ufs = new ArrayList<String>(0);
	private List<String> cidades = new ArrayList<String>(0);
	private List<EstabelecimentoPrisional> estabelecimentos = new ArrayList<EstabelecimentoPrisional>(0);
	private TipoPrisaoEnum[] tiposPrisao = TipoPrisaoEnum.values();

	@Override
	public void init(){
		uf = null;
		cidade = null;
		ufs.clear();
		cidades.clear();
		estabelecimentos.clear();
		super.init();
	}

	@Override
	public MandadoPrisaoManager getManager(){
		return mandadoPrisaoManager;
	}

	@Override
	public void informarCumprimento(){
		setPasso(3);
		recuperarEstadosEstabelecimentos();
		if (getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento() != null){
			setUf(getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento().getUf());
			
			recuperarCidadesEstabelecimentos();
			setCidade(getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento().getDsCidade());
			
			recuperarEstabelecimentosPrisionais();			
		}
		super.informarCumprimento();
	}

	public void recuperarEstadosEstabelecimentos(){
		setUf(null);
		setCidade(null);
		setCidades(null);
		setEstabelecimentos(null);
		setUfs(icrPRIManager.recuperarEstadosEstabelecimentosPrisionais());
		setCidade(null);
	}

	public void recuperarCidadesEstabelecimentos(){
		setCidade(null);
		setCidades(null);
		setEstabelecimentos(null);
		if (getUf() != null){
			setCidades(icrPRIManager.recuperarCidadesEstabelecimentosPrisionais(getUf()));
		}
	}

	public void recuperarEstabelecimentosPrisionais(){
		setEstabelecimentos(null);
		if (getCidade() != null){
			setEstabelecimentos(icrPRIManager.recuperarEstabelecimentosPrisionais(getCidade()));
		}
	}

	public String getUf(){
		return uf;
	}

	public void setUf(String uf){
		this.uf = uf;
	}

	public String getCidade(){
		return cidade;
	}

	public void setCidade(String cidade){
		this.cidade = cidade;
	}

	public List<String> getCidades(){
		return cidades;
	}

	public void setCidades(List<String> cidades){
		this.cidades = cidades;
	}

	public List<String> getUfs(){
		return ufs;
	}

	public void setUfs(List<String> ufs){
		this.ufs = ufs;
	}

	public List<EstabelecimentoPrisional> getEstabelecimentos(){
		return estabelecimentos;
	}

	public void setEstabelecimentos(List<EstabelecimentoPrisional> estabelecimentos){
		this.estabelecimentos = estabelecimentos;
	}

	@Override
	public void gravar(){
		try{
			cumprirMandadoPrisaoService.cumprirMandadoPrisao(getProcessoExpedienteCriminalEdit());
			getEntityManager().flush();
		} catch (PJeBusinessException e){
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());
		}
	}
	
	@Override
	public void setProcessoExpedienteCriminalEdit(MandadoPrisao processoExpedienteCriminalEdit){
		super.setProcessoExpedienteCriminalEdit(processoExpedienteCriminalEdit);
		if(getProcessoExpedienteCriminalEdit() != null && getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento() != null){
			recuperarEstadosEstabelecimentos();
			uf = getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento().getUf();
			
			recuperarCidadesEstabelecimentos();
			cidade = getProcessoExpedienteCriminalEdit().getEstabelecimentoPrisionalCumprimento().getDsCidade();
			
			recuperarEstabelecimentosPrisionais();
		}
	}
	
	public TipoPrisaoEnum[] getTiposPrisao(){
		return tiposPrisao;
	}

	public void setTiposPrisao(TipoPrisaoEnum[] tiposPrisao){
		this.tiposPrisao = tiposPrisao;
	}
}
