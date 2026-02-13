package br.com.infox.ibpm.home;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.manager.TipoComplementoManager;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoComDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoDinamico;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoLivre;
import br.jus.pje.nucleo.enums.TipoComplementoEnum;

@Name(TipoComplementoHome.NAME)
@BypassInterceptors
public class TipoComplementoHome extends AbstractHome<TipoComplemento>{

	public static final String NAME = "tipoComplementoHome";
	private static final long serialVersionUID = 1L;
	
	@Logger
	private Log log;
	
	private Long id;
	private String nome;
	private String codigo;
	private String descricaoGlossario;
	private String orgaoCriador;
	private String validacao;
	private String label;
	private String mensagemErro;
	private Boolean ativo = true;
	private TipoComplementoEnum tipoComplemento = TipoComplementoEnum.L;
	private String expressaoBusca;
	private List<AplicacaoDominio> aplicacaoDominioList;
	private TipoComplementoManager tipoComplementoManager;
	private Boolean temMascara = false;
	private String mascara;
	
	public TipoComplementoEnum[] getTipoComplementoEnumValues(){
		return TipoComplementoEnum.values();
	}
	
	@Override
	public void newInstance() {
		super.newInstance();
		limpaValores();		
	}
	
	@Override
	public String persist() {
		String ret = new String();
		if(this.tipoComplemento != null  && !existeComplementoMesmoNome()) {
			if (this.tipoComplemento.equals(TipoComplementoEnum.D)) {
				TipoComplementoComDominio tipoComplementoComDominio = new TipoComplementoComDominio();			
				preencheTipoComplemento(tipoComplementoComDominio);
				tipoComplementoComDominio.setAplicacaoDominioList(this.aplicacaoDominioList);
				
				ret = persistir(tipoComplementoComDominio);
			} else if(this.tipoComplemento.equals(TipoComplementoEnum.I)) {
				TipoComplementoDinamico tipoComplementoDinamico = new TipoComplementoDinamico();
				preencheTipoComplemento(tipoComplementoDinamico);
				tipoComplementoDinamico.setExpressaoBusca(this.expressaoBusca);
				
				ret = persistir(tipoComplementoDinamico);
			} else if(this.tipoComplemento.equals(TipoComplementoEnum.L)) {
				TipoComplementoLivre tipoComplementoLivre = new TipoComplementoLivre();
				preencheTipoComplemento(tipoComplementoLivre);
				tipoComplementoLivre.setTemMascara(this.temMascara);
				tipoComplementoLivre.setMascara(this.mascara);
				
				ret = persistir(tipoComplementoLivre);
			}
		}
		return ret;
	}
	
	private String persistir(TipoComplemento tipoComplemento) {
		getEntityManager().persist(tipoComplemento);
		getEntityManager().flush();
		
		setInstance(tipoComplemento);
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "tipoComplemento_created"));
		
		return "persisted";
	}

	@Override
	public void setId(Object id) {		
		super.setId(id);
		preencheCampos(getInstance());
		if (getInstance() instanceof TipoComplementoDinamico) {
			this.expressaoBusca = ((TipoComplementoDinamico)getInstance()).getExpressaoBusca();
		} else if (getInstance() instanceof TipoComplementoLivre) {
			this.temMascara = ((TipoComplementoLivre)getInstance()).getTemMascara();
			this.mascara = ((TipoComplementoLivre)getInstance()).getMascara();
		}
	}
	
	@Override
	public String update() {
		String ret = new String();
		if (this.tipoComplemento != null && !existeComplementoMesmoNome()) {
			if (this.tipoComplemento.equals(TipoComplementoEnum.D)) {
				removeExpressaoBusca(getInstance());
				removeMascara(getInstance());
				
				ret = atualizaTipoComplemento(TipoComplementoEnum.D, getInstance(), TipoComplementoComDominio.class);
			} else if(this.tipoComplemento.equals(TipoComplementoEnum.I)) {
				removeAplicacaoDominio(getInstance());
				removeMascara(getInstance());
				
				ret = atualizaTipoComplemento(TipoComplementoEnum.I, getInstance(), TipoComplementoDinamico.class);
			} else if(this.tipoComplemento.equals(TipoComplementoEnum.L)) {
				removeAplicacaoDominio(getInstance());
				removeExpressaoBusca(getInstance());
				
				ret = atualizaTipoComplemento(TipoComplementoEnum.L, getInstance(), TipoComplementoLivre.class);
			}
			exibirMensagemUpdate();
		}
		return ret;
	}

	private boolean existeComplementoMesmoNome() {
		boolean result = false;
		
		if (!this.nome.equals(this.getInstance().getNome())) {
			this.tipoComplementoManager = ComponentUtil.getComponent(TipoComplementoManager.NAME);
			if (this.tipoComplementoManager.recuperarTipoComplemento(this.nome) != null) {
				FacesMessages.instance().add(Severity.ERROR, "Já existe um tipo de complemento com o nome informado.");
				result = true;
			}	
		}
		return result;
	}

	private <T extends TipoComplemento> String atualizaTipoComplemento(
			TipoComplementoEnum tipoComplementoEnum, TipoComplemento instancia, Class<T> tipoComplementoClass) {
		
		preencheTipoComplemento(instancia);
		getEntityManager().flush();
		
		if (!this.tipoComplemento.equals(instancia.getTipoComplemento())) {
			EntityUtil.createNativeQuery(getEntityManager(), 
				"UPDATE tb_tipo_complemento SET tp_tipo_complemento = :tipoComplemento WHERE id_tipo_complemento = :idTipoComplemento", "tb_tipo_complemento")
					.setParameter("tipoComplemento", tipoComplementoEnum.name())
					.setParameter("idTipoComplemento", instancia.getIdTipoComplemento())
					.executeUpdate();
			
			getEntityManager().detach(instancia);
			instancia = getEntityManager().find(tipoComplementoClass, getId());
		}
		
		if (instancia instanceof TipoComplementoLivre) {
			((TipoComplementoLivre)instancia).setTemMascara(this.temMascara);
			((TipoComplementoLivre)instancia).setMascara(this.mascara);
		} else if (instancia instanceof TipoComplementoDinamico) {
			((TipoComplementoDinamico)instancia).setExpressaoBusca(this.expressaoBusca);
		}
		
		getEntityManager().flush();
		
		return "persisted";
	}

	private void exibirMensagemUpdate() {
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "tipoComplemento_updated"));
	}

	private void removeExpressaoBusca(TipoComplemento tipoComplemento) {
		if(tipoComplemento instanceof TipoComplementoDinamico) {
			((TipoComplementoDinamico) tipoComplemento).setExpressaoBusca(null);
		}
	}

	private void removeAplicacaoDominio(TipoComplemento  tipoComplemento) {
		if (tipoComplemento instanceof TipoComplementoComDominio) {
			TipoComplementoComDominio tipoComplementoComDominio = (TipoComplementoComDominio) tipoComplemento;
			if (CollectionUtils.isNotEmpty(tipoComplementoComDominio.getAplicacaoDominioList())) {
				tipoComplementoComDominio.setAplicacaoDominioList(null);
			}
		}
	}
	
	private void removeMascara(TipoComplemento  tipoComplemento) {
		if(tipoComplemento instanceof TipoComplementoLivre) {
			TipoComplementoLivre tipoComplementoLivre = (TipoComplementoLivre) tipoComplemento;
			tipoComplementoLivre.setTemMascara(Boolean.FALSE);
			tipoComplementoLivre.setMascara(null);
		}
	}

	private void preencheTipoComplemento(TipoComplemento tipoComplemento) {		
		if(this.id != null) {
			tipoComplemento.setIdTipoComplemento(this.id);
		}
		tipoComplemento.setNome(this.nome);
		tipoComplemento.setCodigo(this.codigo);
		tipoComplemento.setDescricaoGlossario(this.descricaoGlossario);
		tipoComplemento.setOrgaoCriador(this.orgaoCriador);
		tipoComplemento.setValidacao(this.validacao);
		tipoComplemento.setLabel(this.label);
		tipoComplemento.setMensagemErro(this.mensagemErro);
		tipoComplemento.setAtivo(this.ativo);
	}
	
	private void preencheCampos(TipoComplemento tipoComplemento){		
		this.id = tipoComplemento.getIdTipoComplemento();
		this.nome = tipoComplemento.getNome();
		this.codigo = tipoComplemento.getCodigo();
		this.descricaoGlossario = tipoComplemento.getDescricaoGlossario();
		this.orgaoCriador = tipoComplemento.getOrgaoCriador();
		this.validacao = tipoComplemento.getValidacao();
		this.label = tipoComplemento.getLabel();
		this.mensagemErro = tipoComplemento.getMensagemErro();
		this.ativo = tipoComplemento.getAtivo();
		this.tipoComplemento = tipoComplemento.getTipoComplemento();
	}
	
	protected void limpaValores() {
		this.id = null;
		this.nome = null;
		this.codigo = null;
		this.descricaoGlossario = null;
		this.orgaoCriador = null;
		this.validacao = null;
		this.label = null;
		this.mensagemErro = null;
		this.ativo = true;
		this.tipoComplemento = TipoComplementoEnum.L;
		this.expressaoBusca = null;
		this.temMascara = false;
		this.mascara = null;
	}

	public void setTipoComplementoId(Long id) {
		if (id != null && !id.equals(getId())) {
			setId(id);
		}
	}

	public Long getTipoComplementoId() {
		return (Long) getId();
	}	
	
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescricaoGlossario() {
		return this.descricaoGlossario;
	}

	public void setDescricaoGlossario(String descricaoGlossario) {
		this.descricaoGlossario = descricaoGlossario;
	}

	public String getOrgaoCriador() {
		return this.orgaoCriador;
	}

	public void setOrgaoCriador(String orgaoCriador) {
		this.orgaoCriador = orgaoCriador;
	}

	public String getValidacao() {
		return this.validacao;
	}

	public void setValidacao(String validacao) {
		this.validacao = validacao;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public Boolean getAtivo() {
		return this.ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public TipoComplementoEnum getTipoComplemento() {
		return tipoComplemento;
	}

	public void setTipoComplemento(TipoComplementoEnum tipoComplemento) {
		this.tipoComplemento = tipoComplemento;
	}

	public String getExpressaoBusca() {
		return expressaoBusca;
	}

	public void setExpressaoBusca(String expressaoBusca) {
		this.expressaoBusca = expressaoBusca;
	}

	public List<AplicacaoDominio> getAplicacaoDominioList() {
		return aplicacaoDominioList;
	}

	public void setAplicacaoDominioList(List<AplicacaoDominio> aplicacaoDominioList) {
		this.aplicacaoDominioList = aplicacaoDominioList;
	}
	
	public Boolean getTemMascara() {
		return temMascara;
	}

	public void setTemMascara(Boolean temMascara) {
		this.temMascara = temMascara;
	}
	
	public String getMascara() {
		return mascara;
	}

	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

}
