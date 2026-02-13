package br.com.infox.cliente.home;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.VisualizadoresSigiloList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.VisualizadoresSigiloManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.VisualizadoresSigilo;
import br.jus.pje.nucleo.util.DateUtil;


@Name("visualizadoresSigiloHome")
@BypassInterceptors
public class VisualizadoresSigiloHome  extends AbstractHome<VisualizadoresSigilo>{

	private static final long serialVersionUID = 1L;
	
	private Boolean update = Boolean.FALSE;
	private OrgaoJulgador orgaoJulgador;
	private Integer idOrgaoJulgador;
	private Boolean orgaoJulgadorFixo = Boolean.TRUE;
	
	@Override
	public void newInstance() {
		orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		if (orgaoJulgador == null) {
			setOrgaoJulgadorFixo(Boolean.FALSE);
		}
		Integer visualizadorId = null;
		if(isManaged()) {
			update = Boolean.TRUE;
			visualizadorId = instance.getIdVisualizadoresSigilo();
		} else {
			update = Boolean.FALSE;
		}

		super.newInstance(visualizadorId);
	}
	
	public static VisualizadoresSigiloHome instance() {
		return ComponentUtil.getComponent("visualizadoresSigiloHome");
	}
	
	
	@Override
	public void onClickSearchTab() {
		super.onClickSearchTab();
		VisualizadoresSigiloHome.instance().newInstance();
		VisualizadoresSigiloList.instance().newInstance();
	}
	
	@Override
	public void onClickFormTab() {
		super.onClickFormTab();
		VisualizadoresSigiloHome.instance().newInstance();
		VisualizadoresSigiloList.instance().newInstance();
	}

	/**
	 * Verifica os campos de data para que as datas sejam válidas
	 * 
	 * @return True se as datas estiverem corretas e False se não estiverem.
	 */
	private Boolean verificaData() {
		if (getInstance().getDtFinal() != null &&  (getInstance().getDtInicio().after(getInstance().getDtFinal()))) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A data inicial não pode ser posterior à data final.");
				getInstance().setDtFinal(null);
				return Boolean.FALSE;
			
		}
		return Boolean.TRUE;
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		if(orgaoJulgador == null ) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		return super.afterPersistOrUpdate(ret);
	}

	@Override
	public String persist() {
		if (Boolean.FALSE.equals(verificaData())) {
			return null;
		}
		
		if(orgaoJulgador == null ) {
			orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		}
		
		instance.setOrgaoJulgador(orgaoJulgador);
		Boolean existeVisualizador = verificarVisualizador();
		if (Boolean.FALSE.equals(existeVisualizador)) {			
			instance.setUltimaAlteracao(new Date());
			instance.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
			String ret = super.persist();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "visualizadoresSigilo_created"));
			return ret;	
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "O funcionario já é visualizador desse orgão julgador!");
			return null;
		}
	}
	
	/**
	 * Verifica se já existe o mesmo visualizador cadastrado naquele órgão julgador.
	 * 
	 * 
	 * @return False se não existir e True se já existir.
	 */
	private Boolean verificarVisualizador() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from VisualizadoresSigilo o ");
		sb.append("where o.funcionario = :f and ");
		sb.append("o.orgaoJulgador = :oj ");
		if(Boolean.TRUE.equals(update)) {
			sb.append(" and o.idVisualizadoresSigilo <> :id ");
		}		
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("f", instance.getFuncionario());
		q.setParameter("oj", instance.getOrgaoJulgador());
		if(Boolean.TRUE.equals(update)) {
			q.setParameter("id", instance.getIdVisualizadoresSigilo());
		}
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}
	
	@Override
	public String update() {
		if (Boolean.FALSE.equals(verificaData()))
			return null;

		update = Boolean.TRUE;

		if (Boolean.TRUE.equals(verificarVisualizador())) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "O funcionario já é visualizador desse orgão julgador!");
			getEntityManager().refresh(instance);
		} else {
			instance.setOrgaoJulgador(EntityUtil.find(OrgaoJulgador.class, idOrgaoJulgador));
			instance.setUltimaAlteracao(new Date());
			instance.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
			String ret = super.update();				

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "visualizadoresSigilo_updated"));
			return ret;
		}
		return null;
	}
	
	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			idOrgaoJulgador = instance.getOrgaoJulgador() == null ? -1 : instance.getOrgaoJulgador()
					.getIdOrgaoJulgador();
			orgaoJulgador = instance.getOrgaoJulgador() == null ? null : instance.getOrgaoJulgador();
		}
	}
	
	/**
	 * Verifica se o perfil possui um órgão julgador.
	 * 
	 * @return OrgaoJulgador se possui ou null se não possuir.
	 */
	public OrgaoJulgador exibirListaOrgaoJulgador() {
		return Authenticator.getOrgaoJulgadorAtual();
	}
	
	public void ativarOuInativarVisualizador(PessoaServidor pessoaServidor) {
		VisualizadoresSigiloManager visualizadoresSigiloManager = ComponentUtil.getComponent(VisualizadoresSigiloManager.NAME);
		List<VisualizadoresSigilo> visualizadores = visualizadoresSigiloManager.getVisualizadoresSigiloPorServidor(pessoaServidor);
		if(!visualizadores.isEmpty()) {
			for(VisualizadoresSigilo visualizador : visualizadores) {
				setId(visualizador.getIdVisualizadoresSigilo());		
				if(Boolean.FALSE.equals(pessoaServidor.isPerfilAtivo())) {							
					if(Boolean.TRUE.equals(VisualizadoresSigiloList.instance().isAtivo(visualizador))) {
						instance.setDtFinal(DateUtil.dataMenosDias(new Date(), 1));
						update();
					}			
				} else {			
					if(Boolean.FALSE.equals(VisualizadoresSigiloList.instance().isAtivo(visualizador))) {
						instance.setDtFinal(null);
						update();
					}	
				}
			}			
		}	
	}	
	
	public void inativarVisualizadorLocalizacao(Usuario usuario, OrgaoJulgador orgaoJulgadorLocalizacao) {
		orgaoJulgador = orgaoJulgadorLocalizacao;
		List<PessoaServidor> pessoaServidor = retornaListaPessoaServidor();
		Boolean existeUsuarioNoOJ = false;
		for (PessoaServidor pessoa : pessoaServidor) {
			if(Objects.equals(pessoa.getIdUsuario(), usuario.getIdUsuario())) {
				existeUsuarioNoOJ = true;
			}
		}
		if(Boolean.FALSE.equals(existeUsuarioNoOJ)) {
			VisualizadoresSigilo visualizador = getVisualizadoresSigiloByIdUsuario(usuario.getIdUsuario(), orgaoJulgadorLocalizacao);
			if(visualizador != null) {
				setId(visualizador.getIdVisualizadoresSigilo());
				if(Boolean.TRUE.equals(VisualizadoresSigiloList.instance().isAtivo(visualizador))) {
					instance.setDtFinal(DateUtil.dataMenosDias(new Date(), 1));
					update();
				}	
			}
		}	
	}

	public List<VisualizadoresSigilo> getVisualizadoresSigiloOJ() {			
		VisualizadoresSigiloManager visualizadoresSigiloManager = ComponentUtil.getComponent(VisualizadoresSigiloManager.NAME);		
		return visualizadoresSigiloManager.getVisualizadoresSigiloOJ(orgaoJulgador);
	}
	
	public VisualizadoresSigilo getVisualizadoresSigiloByIdUsuario(Integer idUsuario, OrgaoJulgador orgaoJulgadorLocalizacao) {			
		VisualizadoresSigiloManager visualizadoresSigiloManager = ComponentUtil.getComponent(VisualizadoresSigiloManager.NAME);		
		return visualizadoresSigiloManager.getVisualizadoresSigiloByIdUsuario(idUsuario, orgaoJulgadorLocalizacao);
	}
	
	public List<OrgaoJulgador> getOrgaoJulgadorItens(){
  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
  		return orgaoJulgadorManager.findAll();
	}

	public List<PessoaServidor> retornaListaPessoaServidor(){
		VisualizadoresSigiloManager visualizadoresSigiloManager = ComponentUtil.getComponent(VisualizadoresSigiloManager.NAME);	
		if(orgaoJulgador == null){
			return Collections.emptyList();
		}
		return visualizadoresSigiloManager.retornaListaPessoaServidor(orgaoJulgador);
	}
		
	public Boolean getUpdate() {
		return update;
	}
	public void setUpdate(Boolean update) {
		this.update = update;
	}
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}
	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public Integer getIdOrgaoJulgador() {
		return idOrgaoJulgador;
	}

	public void setIdOrgaoJulgador(Integer idOrgaoJulgador) {
		this.idOrgaoJulgador = idOrgaoJulgador;
	}

	public Boolean getOrgaoJulgadorFixo() {
		return orgaoJulgadorFixo;
	}

	public void setOrgaoJulgadorFixo(Boolean orgaoJulgadorFixo) {
		this.orgaoJulgadorFixo = orgaoJulgadorFixo;
	}

	

	

}
