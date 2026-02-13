package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoVisibilidadeManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoUsuarioManager;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.webservice.criminal.dto.PjeUser;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name("usuarioLocalizacaoVisibilidadeHome")
@BypassInterceptors
public class UsuarioLocalizacaoVisibilidadeHome extends AbstractHome<UsuarioLocalizacaoVisibilidade> {

	private static final long serialVersionUID = 1L;
	private Localizacao localizacao;
	private Integer idOrgaoJulgadorCargo;
	private Boolean update = Boolean.FALSE;
	private char tipo;

	public static UsuarioLocalizacaoVisibilidadeHome instance() {
		return ComponentUtil.getComponent("usuarioLocalizacaoVisibilidadeHome");
	}

	public void setUsuarioLocalizacaoVisibilidadeIdUsuarioLocalizacaoVisibilidade(Integer id) {
		setId(id);
	}

	public Integer getUsuarioLocalizacaoVisibilidadeIdUsuarioLocalizacaoVisibilidade() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		localizacao = null;
		idOrgaoJulgadorCargo = null;
		update = Boolean.FALSE;
		setInstance(null);
		super.newInstance();
	}

	private Boolean verificaData() {
		if (getInstance().getDtFinal() != null) {
			if (getInstance().getDtInicio().after(getInstance().getDtFinal())) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR,
						"A data inicial não pode ser posterior à data final.");
				getInstance().setDtFinal(null);
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	private String gravaLogPessoaLocalizacaoVisibilidade() {
		PessoaLocalizacaoMagistradoHome plmh = PessoaLocalizacaoMagistradoHome.instance();
		plmh.newInstance();
		plmh.getInstance().setDataInicial(getInstance().getDtInicio());
		plmh.getInstance().setDataFinal(getInstance().getDtFinal());
		plmh.getInstance().setMagistrado(
				((PessoaFisica) getInstance().getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao()
						.getUsuario()).getPessoaMagistrado());
		plmh.getInstance().setPapel(
				getInstance().getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getPapel().getNome());
		if (getInstance().getOrgaoJulgadorCargo() != null) {
			plmh.getInstance().setCargoVisibilidade(getInstance().getOrgaoJulgadorCargo().getCargo().getCargo());
			plmh.getInstance().setOrgaoJulgador(getInstance().getOrgaoJulgadorCargo().getOrgaoJulgador());
		}

		if (tipo == 'e')
			plmh.getInstance().setDataExclusao(new Date());

		if (tipo == 'i')
			plmh.getInstance().setDataCriacao(new Date());

		return plmh.persist();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		if (Pessoa.instanceOf(getInstance().getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getUsuario(), PessoaMagistrado.class)) {
			gravaLogPessoaLocalizacaoVisibilidade();
			refreshGrid("historicoOrgaoJulgadorGrid");
			refreshGrid("pessoaMagistradoLocalizacaoGrid");
		}
		return super.afterPersistOrUpdate(ret);
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		tipo = isManaged() ? 'a' : 'i';
		
		if (Pessoa.instanceOf(getInstance().getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getUsuario(), PessoaServidor.class)) {
			UsuarioLocalizacaoMagistradoServidor usuarioLocalizacaoMagistradoServidor = getInstance().getUsuarioLocalizacaoMagistradoServidor();
			
			if(!usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacaoVisibilidadeList().contains(getInstance())) {
				usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacaoVisibilidadeList().add(getInstance());
			}
			
			if(usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().getUsuarioLocalizacaoMagistradoServidor() == null) {
				usuarioLocalizacaoMagistradoServidor.getUsuarioLocalizacao().setUsuarioLocalizacaoMagistradoServidor(usuarioLocalizacaoMagistradoServidor);
			}
		}
		
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		if (!verificaData())
			return null;

		Boolean visibilidade = verificarVisibilidade();
		if (!visibilidade && idOrgaoJulgadorCargo != -1) {
			instance.setOrgaoJulgadorCargo(EntityUtil.find(OrgaoJulgadorCargo.class, idOrgaoJulgadorCargo));
			if (existeULV()) {
				String ret = super.persist();
				getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance().getUsuarioLocalizacaoMagistradoServidor(), TipoVinculacaoUsuarioEnum.EGA);
				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "usuarioLocalizacaoVisibilidade_created"));
				return ret;
			}
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "O usuario já possui essa visibilidade!");
			return null;
		} else {
			instance.setOrgaoJulgadorCargo(null);
			if (visibilidade) {
				if (idOrgaoJulgadorCargo == -1) {
					FacesMessages.instance().add(StatusMessage.Severity.INFO, "O usuario já possui essa visibilidade!");
				} else {
					FacesMessages.instance().add(
							StatusMessage.Severity.INFO,
							"O usuario possui a visibilidade 'Todos', para atribuir outra visibilidade a ele"
									+ " é necessario remover essa visibilidade!");
				}
			} else {
				if (!possuiVisibilidade()) {
					String ret = super.persist();
					getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance().getUsuarioLocalizacaoMagistradoServidor(), TipoVinculacaoUsuarioEnum.EGA);					

					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "usuarioLocalizacaoVisibilidade_created"));
					return ret;
				}
				FacesMessages.instance().clear();
				FacesMessages.instance().add(
						StatusMessage.Severity.INFO,
						"Para atribuir essa visibilidade a esse usuario é necessario"
								+ " remover a(s) outra(s) visibilidade(s) que ele possui!");
			}
			return null;
		}
	}

	private Boolean possuiVisibilidade() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoVisibilidade o ");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :ulms ");
		if (update) {
			sb.append("and o.idUsuarioLocalizacaoVisibilidade <> :id");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ulms", instance.getUsuarioLocalizacaoMagistradoServidor());
		if (update) {
			q.setParameter("id", instance.getIdUsuarioLocalizacaoVisibilidade());
		}
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private Boolean verificarVisibilidade() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoVisibilidade o ");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :ulms and ");
		sb.append("o.orgaoJulgadorCargo is null");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ulms", instance.getUsuarioLocalizacaoMagistradoServidor());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private Boolean existeULV() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoVisibilidade o ");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :ulms and ");
		sb.append("o.orgaoJulgadorCargo = :ojc");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ulms", instance.getUsuarioLocalizacaoMagistradoServidor());
		q.setParameter("ojc", instance.getOrgaoJulgadorCargo());
		try {
			Long retorno = (Long) q.getSingleResult();
			return !(retorno > 0);
		} catch (NoResultException no) {
			return Boolean.TRUE;
		}
	}

	@Override
	public String update() {
		if (!verificaData())
			return null;

		update = Boolean.TRUE;
		if (idOrgaoJulgadorCargo == -1) {
			instance.setOrgaoJulgadorCargo(null);
		} else {
			instance.setOrgaoJulgadorCargo(EntityUtil.find(OrgaoJulgadorCargo.class, idOrgaoJulgadorCargo));
		}
		if (updateVisibilidade()) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "O usuario já possui essa visibilidade!");
			getEntityManager().refresh(instance);
		} else {
			if (idOrgaoJulgadorCargo == -1) {
				if (possuiVisibilidade()) {
					StringBuilder erro = new StringBuilder(
							"Para atribuir essa visibilidade a esse usuario é necessario remover ");
					erro.append("a(s) outra(s) visibilidade(s) que ele possui!");
					FacesMessages.instance().add(StatusMessage.Severity.INFO, erro.toString());
					getEntityManager().refresh(instance);
				} else {
					String ret = super.update();

					getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance().getUsuarioLocalizacaoMagistradoServidor(), TipoVinculacaoUsuarioEnum.EGA);					

					FacesMessages.instance().clear();
					FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "usuarioLocalizacaoVisibilidade_updated"));
					return ret;
				}
			} else {
				String ret = super.update();

				getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance().getUsuarioLocalizacaoMagistradoServidor(), TipoVinculacaoUsuarioEnum.EGA);

				FacesMessages.instance().clear();
				FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "usuarioLocalizacaoVisibilidade_updated"));
				return ret;
			}
		}
		return null;
	}

	private Boolean updateVisibilidade() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacaoVisibilidade o ");
		sb.append("where o.usuarioLocalizacaoMagistradoServidor = :ulms ");
		if (idOrgaoJulgadorCargo != -1) {
			sb.append("and o.orgaoJulgadorCargo = :ojc ");
		} else {
			sb.append("and o.orgaoJulgadorCargo is null ");
		}
		sb.append("and o.idUsuarioLocalizacaoVisibilidade <> :id");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("ulms", instance.getUsuarioLocalizacaoMagistradoServidor());
		if (idOrgaoJulgadorCargo != -1) {
			q.setParameter("ojc", instance.getOrgaoJulgadorCargo());
		}
		q.setParameter("id", instance.getIdUsuarioLocalizacaoVisibilidade());

		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (instance.getUsuarioLocalizacaoMagistradoServidor() != null
					&& PessoaServidorHome.instance().getInstance() != null
					&& PessoaServidorHome.instance().getInstance().getIdUsuario() != null) {
				localizacao = instance.getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao()
						.getLocalizacaoFisica();
			}
			idOrgaoJulgadorCargo = instance.getOrgaoJulgadorCargo() == null ? -1 : instance.getOrgaoJulgadorCargo()
					.getIdOrgaoJulgadorCargo();
		}
		if (id == null) {
			localizacao = null;
		}
	}

	/**
	 * Método responsável por retornar as localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 * 
	 * @return As localizações do servidor, salvo as vinculadas ao papel de magistrado.
	 */
	public List<Localizacao> getLocalizacaoServidorItems() {			
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = Authenticator.getOrgaoJulgadorColegiadoAtual();
		OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();
		Localizacao localizacaoFisica = Authenticator.getLocalizacaoFisicaAtual();
		UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager = ComponentUtil.getComponent(UsuarioLocalizacaoVisibilidadeManager.NAME);		
		return usuarioLocalizacaoVisibilidadeManager.getLocalizacaoServidorItems(PessoaServidorHome.instance().getInstance().getIdUsuario(), orgaoJulgadorColegiado, orgaoJulgador, localizacaoFisica);
	}

	@SuppressWarnings("unchecked")
	public List<UsuarioLocalizacaoMagistradoServidor> getUsuarioLocalizacaoMagistradoServidorItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
		sb.append("where o.usuarioLocalizacao.usuario in (select pm from PessoaMagistrado pm) ");
		sb.append("and o.orgaoJulgador = :orgaoJulgador ");
		sb.append("and o.usuarioLocalizacao.papel.identificador = :magistrado ");
		sb.append("and (o.dtFinal is null or o.dtFinal >= :dataAtual) ");
		sb.append("order by o.idUsuarioLocalizacaoMagistradoServidor desc");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgaoJulgador", OrgaoJulgadorHome.instance().getInstance());
		q.setParameter("magistrado", Papeis.MAGISTRADO);
		q.setParameter("dataAtual", DateUtil.getDataAtual());
		return q.getResultList();
	}

	@SuppressWarnings("unchecked")
	private List<OrgaoJulgadorCargo> getOrgaoJulgadorCargoVisibilidadeItems() {
		List<OrgaoJulgador> orgaosJulgadores = null;
		if (instance.getUsuarioLocalizacaoMagistradoServidor() != null || localizacao != null) {
			Localizacao localizacaoFisica = instance.getUsuarioLocalizacaoMagistradoServidor().getLocalizacaoFisica();
	  		OrgaoJulgadorManager orgaoJulgadorManager = ComponentUtil.getComponent("orgaoJulgadorManager");
	  		orgaosJulgadores = orgaoJulgadorManager.findAllbyLocalizacao(localizacaoFisica);
	  		
	  		if(CollectionUtilsPje.isEmpty(orgaosJulgadores)) {
	  			OrgaoJulgadorColegiado orgaoJulgadorColegiado = instance.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgadorColegiado();
	  			if(orgaoJulgadorColegiado != null) {
	  				orgaosJulgadores = orgaoJulgadorManager.getOrgaoJulgadorListByOjc(orgaoJulgadorColegiado);
	  			}
	  		}
		}

		if (CollectionUtilsPje.isEmpty(orgaosJulgadores)) {
			return Collections.EMPTY_LIST;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCargo o ");
		sb.append("where o.orgaoJulgador in (:orgJulg) ");
		sb.append(" ORDER BY o.orgaoJulgador.orgaoJulgadorOrdemAlfabetica ASC, upper(o.descricao)");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgJulg", orgaosJulgadores);

		return q.getResultList();
	}

	public List<SelectItem> getOrgaoJulgadorCargoVisibilidadeSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		list.add(new SelectItem("", "Selecione"));
		List<Integer> listIds = new ArrayList<Integer>();
		for (OrgaoJulgadorCargo orgao : getOrgaoJulgadorCargoVisibilidadeItems()) {
			if(!listIds.contains(orgao.getIdOrgaoJulgadorCargo())) {
				list.add(new SelectItem(orgao.getIdOrgaoJulgadorCargo(), orgao.toString()));
				listIds.add(orgao.getIdOrgaoJulgadorCargo());
			}
		}
		list.add(new SelectItem(-1, "Todos"));
		return list;
	}

	public void setLocalizacao(Localizacao localizacao) {
		this.localizacao = localizacao;
	}

	public Localizacao getLocalizacao() {
		return localizacao;
	}

	public void setIdOrgaoJulgadorCargo(Integer idOrgaoJulgadorCargo) {
		this.idOrgaoJulgadorCargo = idOrgaoJulgadorCargo;
	}

	public Integer getIdOrgaoJulgadorCargo() {
		return idOrgaoJulgadorCargo;
	}
	
 	/**
 	 * Verifica se  possvel editar uma visibilidade.
 	 * 
 	 * @param visibilidade
 	 * @return <code>true</code> se a data final da localizao da visibilidade  
 	 * estiver vazia ou se for posterior a data atual. <code>false</code> caso contrrio.
 	 */
 	public boolean podeEditar(UsuarioLocalizacaoVisibilidade visibilidade) {
 		Boolean isVisibilidadeSubstituicao = (visibilidade.getSubstituicaoMagistrado() != null);
		Boolean isVisibilidadeAtiva =  ( 
				visibilidade.getUsuarioLocalizacaoMagistradoServidor().getDtFinal() == null ||
				DateUtil.isDataMaiorIgual(visibilidade.getUsuarioLocalizacaoMagistradoServidor().getDtFinal(), DateUtil.getDataAtual())
			);
		
		return (isVisibilidadeAtiva && !isVisibilidadeSubstituicao);
 	}

	@Override
	public String remove(UsuarioLocalizacaoVisibilidade obj) {
		try {
			newInstance();
			setInstance(obj);
			UsuarioLocalizacaoVisibilidade visibilidade = EntityUtil.find(UsuarioLocalizacaoVisibilidade.class,
					obj.getIdUsuarioLocalizacaoVisibilidade());
			UsuarioLocalizacaoMagistradoServidor lotacao = visibilidade.getUsuarioLocalizacaoMagistradoServidor(); 			
			getEntityManager().remove(visibilidade);
			EntityUtil.flush();
			getVinculacaoUsuarioManager().sincronizarLotacoes(lotacao, TipoVinculacaoUsuarioEnum.EGA);

			if (Pessoa.instanceOf(getInstance().getUsuarioLocalizacaoMagistradoServidor().getUsuarioLocalizacao().getUsuario(), PessoaMagistrado.class)) {
				setInstance(obj);
				tipo = 'e';
				gravaLogPessoaLocalizacaoVisibilidade();
				refreshGrid("historicoOrgaoJulgadorGrid");
				refreshGrid("pessoaMagistradoLocalizacaoGrid");
			}
			newInstance();
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "usuarioLocalizacaoVisibilidade_deleted"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private VinculacaoUsuarioManager getVinculacaoUsuarioManager(){
 		return ComponentUtil.getComponent(VinculacaoUsuarioManager.NAME);
 	}	

}
