package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.hibernate.AssertionFailure;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.PessoaMagistradoSuggestBean;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.component.tree.LocalizacaoModeloTreeHandler;
import br.com.infox.ibpm.component.tree.PapelUsuarioLocalizacaoPJETree;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.UsuarioLocalizacaoHome;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.UsuarioLocalizacaoMagistradoServidorManager;
import br.jus.cnj.pje.nucleo.manager.VinculacaoUsuarioManager;
import br.jus.cnj.pje.view.CadastroLocalizacaoServidorAction;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoVisibilidade;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.enums.TipoVinculacaoUsuarioEnum;

@Name("usuarioLocalizacaoMagistradoServidorHome")
@BypassInterceptors
public class UsuarioLocalizacaoMagistradoServidorHome
    extends AbstractUsuarioLocalizacaoMagistradoServidorHome<UsuarioLocalizacaoMagistradoServidor> {
    private static final long serialVersionUID = 1L;
    private Usuario usuario;
    private Integer idUsuarioLocalizacaoMagistradoServidor;
    private OrgaoJulgadorColegiado orgaoJulgadorColegiado;
    private OrgaoJulgador orgaoJulgador;
    private Localizacao localizacaoFisica;
    private Localizacao localizacaoModelo;
    private Papel papel;
    
    private Boolean titularidade = Boolean.FALSE;
    private PessoaMagistrado magistrado;

    private char tipo;
    private boolean cadastroValidado = Boolean.FALSE;

    private boolean formularioOrgaoJulgador = false; // se o formulário é do ÓrgãoJulgador, então estão sendo cadastrados magistrados, caso contrário, são servidores de forma geral
    
    @In
    private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;
    
    @In
	private CadastroLocalizacaoServidorAction cadastroLocalizacaoServidorAction;
    
    private UsuarioLocalizacaoMagistradoServidorManager getManager() {
    	if(this.usuarioLocalizacaoMagistradoServidorManager == null) {
    		this.usuarioLocalizacaoMagistradoServidorManager = ComponentUtil.getComponent(UsuarioLocalizacaoMagistradoServidorManager.class);
    	}
    	return this.usuarioLocalizacaoMagistradoServidorManager;
    }
    
    private CadastroLocalizacaoServidorAction getLocalizacaoServidorAction() {
    	if(this.cadastroLocalizacaoServidorAction == null) {
    		this.cadastroLocalizacaoServidorAction = ComponentUtil.getComponent(CadastroLocalizacaoServidorAction.class);
    		this.cadastroLocalizacaoServidorAction.init();
    	}
    	return this.cadastroLocalizacaoServidorAction;
    }
    
    private void limpaValoresSelecionados() {
    	orgaoJulgadorColegiado = null;
    	orgaoJulgador = null;
        localizacaoFisica = null;
        localizacaoModelo = null;
        papel = null;
        usuario = null;
        magistrado = null;
    }

    @Override
    public void newInstance() {
    	super.newInstance();
    	this.limpaValoresSelecionados();
        idUsuarioLocalizacaoMagistradoServidor = null;
        
        UsuarioLocalizacaoMagistradoServidorHome.instance().getInstance().setOrgaoJulgador(null);
        UsuarioLocalizacaoMagistradoServidorHome.instance().getInstance().setOrgaoJulgadorColegiado(null);
        
        LocalizacaoModeloTreeHandler localizacaoModeloTreeHandler = ComponentUtil.getComponent("localizacaoModeloTreeHandler");
        if(localizacaoModeloTreeHandler != null) {
        	localizacaoModeloTreeHandler.clearTree();
		}
                
        PapelUsuarioLocalizacaoPJETree papelUsuarioLocalizacaoPJETree = ComponentUtil.getComponent("papelUsuarioLocalizacaoPJETree");
		if(papelUsuarioLocalizacaoPJETree != null) {
			papelUsuarioLocalizacaoPJETree.clearTree();
		}
		
		UsuarioLocalizacaoHome.instance().newInstance();
		
	    if(OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgador() != null || OrgaoJulgadorColegiadoHome.instance().isManaged()) {
	    	formularioOrgaoJulgador = true;
	    }
	    if(!this.formularioOrgaoJulgador) {
	    	getLocalizacaoServidorAction().limparFormulario();
	    	getLocalizacaoServidorAction().init();
	    }
        this.identificaUsuarioAtual();
    }

    public static UsuarioLocalizacaoMagistradoServidorHome instance() {
        return ComponentUtil.getComponent(
            "usuarioLocalizacaoMagistradoServidorHome");
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public Papel getPapel() {
        return papel;
    }

    @Override
    public void setId(Object id) {
    	if(id == null) {
    		super.setId(id);
    		return;
    	}
        this.limpaValoresSelecionados();
        idUsuarioLocalizacaoMagistradoServidor = null;
        this.getId();
        super.setId(id);

        identificaUsuarioAtual();

    	if(isManaged()) {
    		idUsuarioLocalizacaoMagistradoServidor = getInstance().getIdUsuarioLocalizacaoMagistradoServidor();
    		
    		if(getInstance() != null) {
    			this.orgaoJulgadorColegiado = getInstance().getOrgaoJulgadorColegiado();
    			this.orgaoJulgador = getInstance().getOrgaoJulgador();
    			if (getInstance().getUsuarioLocalizacao() != null) {
    				this.localizacaoFisica = getInstance().getUsuarioLocalizacao().getLocalizacaoFisica();
    				this.localizacaoModelo = getInstance().getUsuarioLocalizacao().getLocalizacaoModelo();
    				this.papel = getInstance().getUsuarioLocalizacao().getPapel();

	            	this.magistrado = EntityUtil.find(PessoaMagistrado.class,
	            			  getInstance().getUsuarioLocalizacao().getUsuario().getIdUsuario());
    			}
    		}
    		if(this.isFormularioOrgaoJulgador()) {
    			getPessoaMagistradoSuggest().setInstance(magistrado);
    		}else {
    	        getLocalizacaoServidorAction().init();
    			getLocalizacaoServidorAction().carregaValoresPreenchidos(this.idUsuarioLocalizacaoMagistradoServidor, 
    					this.orgaoJulgadorColegiado, this.orgaoJulgador, 
    					this.localizacaoFisica, this.localizacaoModelo, this.papel);
    		}
    	}
    }

    private Boolean possuiLocalizacao() {
        OrgaoJulgador oj = null;
        OrgaoJulgadorColegiado ojc = null;
    	if(instance.getOrgaoJulgador() != null) {
    		oj = instance.getOrgaoJulgador();
    	}
    	if (instance.getOrgaoJulgadorColegiado() != null) {
    		ojc = instance.getOrgaoJulgadorColegiado();
    	}
        
        Localizacao localizacaoFisica = null;
        Localizacao localizacaoModelo = null;
        if(oj != null) {
        	localizacaoFisica = oj.getLocalizacao();
        }else if(ojc != null) {
        	localizacaoFisica = ojc.getLocalizacao();
        }else if(instance().getLocalizacaoFisica() != null) {
        	localizacaoFisica = instance().getLocalizacaoFisica();
        }
        // só pode haver localização modelo relacionada a uma localização física pré-selecionada
        if(localizacaoFisica != null && instance().getLocalizacaoModelo() != null) {
        	localizacaoModelo = instance().getLocalizacaoModelo();
        }

        Papel papel = null;
        if(this.isFormularioOrgaoJulgador()) {
        	papel = ParametroUtil.instance().getPapelMagistrado();
        }else {
        	papel = instance().getPapel();
        }
                
        return this.getManager().verificaLocalizacaoInformadaJaExiste(this.usuario, ojc, oj, localizacaoFisica, localizacaoModelo, 
        		papel, instance.getDtInicio(), (isManaged() ? instance.getIdUsuarioLocalizacaoMagistradoServidor() : null));
    }
    
    private boolean isFormularioOrgaoJulgador() {
    	return this.formularioOrgaoJulgador;
    }
    
    private void identificaUsuarioAtual() {
        if(this.isFormularioOrgaoJulgador()) {
        	if(getPessoaMagistradoSuggest().getInstance() != null) {
        		this.usuario = getPessoaMagistradoSuggest().getInstance().getPessoa();
        	}
        }else {
        	if(PessoaServidorHome.instance() != null && PessoaServidorHome.instance().getInstance() != null) {
        		this.usuario = PessoaServidorHome.instance().getInstance().getPessoa();
        	}
        	getLocalizacaoServidorAction().setUsuario(this.usuario);
        }
    }

    private void gravaLocalizacaoVisibilidade() {
        // inserir o cargo como visibilidade caso ele receba distribuição
        if ((instance.getOrgaoJulgadorCargo() != null) &&
                (instance.getOrgaoJulgadorCargo().getRecebeDistribuicao() == true)) {
            UsuarioLocalizacaoVisibilidade ulv = new UsuarioLocalizacaoVisibilidade();
            ulv.setUsuarioLocalizacaoMagistradoServidor(instance);
            ulv.setDtInicio(instance.getDtInicio());
            ulv.setDtFinal(instance.getDtFinal());
            ulv.setOrgaoJulgadorCargo(instance.getOrgaoJulgadorCargo());

            try {
                getEntityManager().persist(ulv);
                EntityUtil.flush();
            } catch (Exception e) {
                FacesMessages.instance().add(Severity.ERROR, "Erro ao gravar a visibilidade");
            }
        }
    }

    private Boolean verificaData() {
        if (getInstance().getDtFinal() != null) {
            if (getInstance().getDtInicio().after(getInstance().getDtFinal())) {
                FacesMessages.instance().clear();
                FacesMessages.instance()
                             .add(StatusMessage.Severity.ERROR,
                    "Data Final não pode ser menor que a Data Inicial.");

                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }

    private String gravaLogUsuarioLocalizacaoMagistrado() {
        PessoaLocalizacaoMagistradoHome plmh = PessoaLocalizacaoMagistradoHome.instance();
        plmh.newInstance();

        // TODO verificar como vai ficar isso, pois tem de existir
        // OrgaoJulgadorColegiadoCargo
        if (getInstance().getOrgaoJulgadorCargo() != null) {
            plmh.getInstance()
                .setCargo(getInstance().getOrgaoJulgadorCargo().getCargo()
                              .getCargo());
        }

        plmh.getInstance().setDataInicial(getInstance().getDtInicio());
        plmh.getInstance().setDataFinal(getInstance().getDtFinal());

        if (getPessoaMagistradoSuggest().getInstance() != null) {
            plmh.getInstance()
                .setMagistrado(getPessoaMagistradoSuggest().getInstance());
        } else {
            PessoaMagistrado pm = EntityUtil.find(PessoaMagistrado.class,
                    instance.getUsuarioLocalizacao().getUsuario().getIdUsuario());
            plmh.getInstance().setMagistrado(pm);
        }

        plmh.getInstance().setNorma(getInstance().getNorma());
        plmh.getInstance().setOrgaoJulgador(getInstance().getOrgaoJulgador());
        plmh.getInstance()
            .setOrgaoJulgadorColegiado(getInstance().getOrgaoJulgadorColegiado());
        plmh.getInstance().setPapel(Authenticator.getPapelAtual().getNome());

        if (tipo == 'e') {
            plmh.getInstance().setDataExclusao(new Date());
        }

        if (tipo == 'i') {
            plmh.getInstance().setDataCriacao(new Date());
        }

        refreshGrid("historicoOrgaoJulgadorGrid");
        refreshGrid("pessoaMagistradoLocalizacaoGrid");
        FacesMessages.instance().clear();

        return plmh.persist();
    }

    private boolean antesPersistOrUpdate() {
        if ((OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgador() != null) &&
        		!OrgaoJulgadorHome.instance().getInstance().getAplicacaoClasse().getCodigoAplicacaoClasse().equals(Constantes.COD_APLICACAO_CLASSE.PRIMEIRO_GRAU)
                && !ParametroUtil.instance().isPrimeiroGrau()) {
        	
            StringBuilder sb = new StringBuilder();
            sb.append("select o from OrgaoJulgadorCargo o ");
            sb.append("where o.orgaoJulgador = :oj ");
            sb.append("and o.recebeDistribuicao = true ");

            Query q = getEntityManager().createQuery(sb.toString());
            q.setParameter("oj", OrgaoJulgadorHome.instance().getInstance());

            if (q.getResultList().size() > 1) {
                FacesMessages.instance()
                             .add(Severity.ERROR,
                    "Existe mais de um Cargo recebendo Distribuição.");

                return Boolean.FALSE;
            }

            if (q.getResultList().size() == 1) {
                OrgaoJulgadorCargo ojc = (OrgaoJulgadorCargo) q.getResultList()
                                                               .get(0);

                if (getInstance().getOrgaoJulgadorCargo() == ojc) {
                    StringBuilder sql = new StringBuilder();
                    sql.append(
                        "select o from UsuarioLocalizacaoMagistradoServidor o ");
                    sql.append(
                        "where o.usuarioLocalizacao.usuario = :magistrado ");
                    sql.append(
                        "and o.orgaoJulgadorCargo.idOrgaoJulgadorCargo = :cargo ");
                    sql.append("and o.orgaoJulgadorColegiado = :ojcolegiado ");

                    if (isManaged()) {
                        sql.append(
                            "and o.idUsuarioLocalizacaoMagistradoServidor != :id");
                    }

                    Query query = getEntityManager().createQuery(sql.toString());
                    query.setParameter("magistrado", this.magistrado.getPessoa());
                    query.setParameter("cargo", ojc.getIdOrgaoJulgadorCargo());
                    query.setParameter("ojcolegiado",
                        instance.getOrgaoJulgadorColegiado());

                    if (isManaged()) {
                        query.setParameter("id",
                            instance.getIdUsuarioLocalizacaoMagistradoServidor());
                    }

                    if (query.getResultList().size() == 1) {
                        FacesMessages.instance()
                                     .add(Severity.ERROR,
                            "Já existe um Magistrado cadastrado com o Cargo que recebe distribuição.");

                        return Boolean.FALSE;
                    }
                }
            }
            
            // Validando se já existe um titular para o cargo, ojc e localização
            if(instance.getMagistradoTitular() != null && instance.getMagistradoTitular() == true) {
            	StringBuilder sql = new StringBuilder();
                sql.append("select o from UsuarioLocalizacaoMagistradoServidor o ");
                sql.append("where o.orgaoJulgador.idOrgaoJulgador = :orgaojulgador ");
                sql.append("and o.orgaoJulgadorCargo.idOrgaoJulgadorCargo = :ojcargo ");
                sql.append("and o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = :ojcolegiado ");
                sql.append("and o.usuarioLocalizacao.localizacaoFisica.idLocalizacao = :ul ");
                
                if (isManaged()) {
                    sql.append("and o.idUsuarioLocalizacaoMagistradoServidor != :id ");
                }
                
                sql.append("and o.magistradoTitular = :titular ");
                
                Query query = getEntityManager().createQuery(sql.toString());
                query.setParameter("orgaojulgador", instance.getOrgaoJulgador().getIdOrgaoJulgador());
                query.setParameter("ojcargo", instance.getOrgaoJulgadorCargo().getIdOrgaoJulgadorCargo());
                query.setParameter("ojcolegiado", instance.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado());
                query.setParameter("ul", instance.getUsuarioLocalizacao().getLocalizacaoFisica().getIdLocalizacao());
                
                if (isManaged()) {
                    query.setParameter("id", instance.getIdUsuarioLocalizacaoMagistradoServidor());
                }
                
                query.setParameter("titular", true);

                if (query.getResultList().size() >= 1) {
                    FacesMessages.instance().add(Severity.ERROR, "Já existe um Magistrado titular para este cargo, Órgão julgador colegiado e Localização.");

                    return Boolean.FALSE;
                }
            }
        }

        return Boolean.TRUE;
    }

    public void gravarLocalizacaoMagistrado(OrgaoJulgador orgaoJulgador) {
        gravarLocalizacaoMagistrado(orgaoJulgador, null);
        newInstance();
    }

    public void gravarLocalizacaoMagistrado(
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        gravarLocalizacaoMagistrado(null, orgaoJulgadorColegiado);
    }

    private void gravarLocalizacaoMagistrado(OrgaoJulgador orgaoJulgador,
        OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
        // verifica se a data final é menor que a final
        if (!verificaData()) {
            return;
        }

        // gravaLocalizacaoVisibilidade();
        magistrado = getPessoaMagistradoSuggest().getInstance();

        UsuarioLocalizacao usuarioLocalizacao = getInstance().getUsuarioLocalizacao();

        if (usuarioLocalizacao == null) {
            usuarioLocalizacao = new UsuarioLocalizacao();
        }

        usuarioLocalizacao.setPapel(ParametroUtil.instance().getPapelMagistrado());
        usuarioLocalizacao.setUsuario(magistrado.getPessoa());
        usuarioLocalizacao.setResponsavelLocalizacao(Boolean.FALSE);

        try {
            if (orgaoJulgador != null) {
                getInstance().setOrgaoJulgador(orgaoJulgador);
                usuarioLocalizacao.setLocalizacaoFisica(orgaoJulgador.getLocalizacao());
            } else if (orgaoJulgadorColegiado != null) {
                getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
                usuarioLocalizacao.setLocalizacaoFisica(orgaoJulgadorColegiado.getLocalizacao());
            }

            if (possuiLocalizacao()) {
                FacesMessages.instance().add(Severity.ERROR,"Registro já cadastrado!");

                return;
            }

            if (antesPersistOrUpdate()) {
                getEntityManager().persist(usuarioLocalizacao);
                getEntityManager().flush();
                getInstance().setUsuarioLocalizacao(usuarioLocalizacao);
                getInstance().setIdUsuarioLocalizacaoMagistradoServidor(usuarioLocalizacao.getIdUsuarioLocalizacao());
                persist();
                getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance(), TipoVinculacaoUsuarioEnum.EGA);
                tipo = 'i';
                gravaLogUsuarioLocalizacaoMagistrado();
                refreshGrid("historicoOrgaoJulgadorGrid");
                refreshGrid("pessoaMagistradoLocalizacaoGrid");
            }
        } catch (AssertionFailure e) {
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException) {
                FacesMessages.instance().clear();
                FacesMessages.instance().add(Severity.ERROR,"Registro já cadastrado!");
            } else {
                FacesMessages.instance()
                             .add(Severity.ERROR,
                    "Erro: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void alterarLocalizacaoMagistrado() {    	
    	if (verificaData()) {
        	magistrado = getPessoaMagistradoSuggest().getInstance();
            getInstance().getUsuarioLocalizacao().setUsuario(magistrado.getPessoa());

            if (possuiLocalizacao()) {
                FacesMessages.instance().add(Severity.ERROR,"Registro já cadastrado!");
            } else {
                if (antesPersistOrUpdate()) {
                    super.update();
                	getVinculacaoUsuarioManager().sincronizarLotacoes(getInstance(), TipoVinculacaoUsuarioEnum.EGA);
                    tipo = 'a';
                    gravaLogUsuarioLocalizacaoMagistrado();
                    
                    FacesMessages.instance().clear();
                  	FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso!");
                }
            }
        }

        refreshGrid("pessoaMagistradoLocalizacaoGrid");
        refreshGrid("historicoOrgaoJulgadorGrid");
        getEntityManager().refresh(getInstance());
    }

    @Override
    public String remove(UsuarioLocalizacaoMagistradoServidor obj) {    	
        // o new instance serve para tirar o registro de edição caso o usuário
        // tenha editado antes de remover
        newInstance();

        UsuarioLocalizacao usuarioLocalizacao = obj.getUsuarioLocalizacao();
        papel = obj.getUsuarioLocalizacao().getPapel();

        if (verificaProcessosMagistradoAuxiliar(obj)) {
            FacesMessages.instance().clear();
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Antes de remover o magistrado, remova os processos vinculados.");
            return null;
        } else {
            obj.setUsuarioLocalizacao(null);
            try {
            	Usuario usuarioMagistrado = usuarioLocalizacao.getUsuario();
             	OrgaoJulgador orgaoJulgador = obj.getOrgaoJulgador();
             	OrgaoJulgadorColegiado orgaoJulgadorColegiado = obj.getOrgaoJulgadorColegiado();            	
             	
                getEntityManager().remove(obj);
                getEntityManager().remove(usuarioLocalizacao);
                getEntityManager().flush();
                
                getVinculacaoUsuarioManager().sincronizarExclusaoLotacaoUsuario(usuarioMagistrado, orgaoJulgador, orgaoJulgadorColegiado, TipoVinculacaoUsuarioEnum.EGA);                 
                
                FacesMessages.instance().add("Registro removido com sucesso!");
            } catch (Exception e) {
                FacesMessages.instance().clear();
                FacesMessages.instance().add(Severity.WARN,"Antes de remover o magistrado remova sua visibilidade");
                return null;
            }
            setInstance(obj);
            getInstance().setUsuarioLocalizacao(usuarioLocalizacao);
            tipo = 'e';
            gravaLogUsuarioLocalizacaoMagistrado();

            FacesMessages.instance().clear();
            FacesMessages.instance().add("Registro excluido com sucesso");
            newInstance();
            refreshGrid("historicoOrgaoJulgadorGrid");
            refreshGrid("pessoaMagistradoLocalizacaoGrid");

            return "removed";
        }
    }
    
	public void atualizaDadosServidor() {
		this.limpaValoresSelecionados();
		this.setCadastroValidado(getLocalizacaoServidorAction().isCadastroValidado() && getLocalizacaoServidorAction().isValoresIniciaisAlterados());
		if(this.isCadastroValidado()) {
			this.orgaoJulgadorColegiado = getLocalizacaoServidorAction().getOrgaoJulgadorColegiado();
			this.orgaoJulgador = getLocalizacaoServidorAction().getOrgaoJulgador();
			this.localizacaoFisica = getLocalizacaoServidorAction().getLocalizacaoFisica();
			this.localizacaoModelo = getLocalizacaoServidorAction().getLocalizacaoModelo();
			this.papel = getLocalizacaoServidorAction().getPapel();
			this.idUsuarioLocalizacaoMagistradoServidor = getLocalizacaoServidorAction().getIdUsuarioLocalizacaoMagistradoServidor();
			this.usuario = getLocalizacaoServidorAction().getUsuario();
		}
	}

	/**
	 * Persiste a informação indicada para o servidor nas tabelas:
	 * - usuarioLocalizacao
	 * - usuarioLocalizacaoMagistradoServidor
	 * - usuarioLocalizacaoVisibilidade
	 * @return
	 */
    public void gravarServidor() {
    	if(!this.isCadastroValidado()) {
    		return;
    	}
    	UsuarioLocalizacao usuarioLocalizacao = null;
        if(this.idUsuarioLocalizacaoMagistradoServidor != null) {
        	super.setId(this.idUsuarioLocalizacaoMagistradoServidor);
        	usuarioLocalizacao = instance.getUsuarioLocalizacao();
        	usuarioLocalizacao.setIdUsuarioLocalizacao(this.idUsuarioLocalizacaoMagistradoServidor);
            validarAlteracaoOrgaoJulgador();
        }else {
        	usuarioLocalizacao = new UsuarioLocalizacao();
        	usuarioLocalizacao.setResponsavelLocalizacao(Boolean.FALSE);
        }

        usuarioLocalizacao.setUsuario(this.usuario);
        usuarioLocalizacao.setLocalizacaoFisica(this.localizacaoFisica);
        usuarioLocalizacao.setLocalizacaoModelo(this.localizacaoModelo);
        usuarioLocalizacao.setPapel(this.papel);

        try {
            getEntityManager().persist(usuarioLocalizacao);
            getEntityManager().flush();
        } catch (AssertionFailure e) {
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException) {
                FacesMessages.instance().clear();
                FacesMessages.instance().add(Severity.ERROR,"Registro já cadastrado!");

                return;
            }
        }
        
    	instance.setOrgaoJulgadorColegiado(this.orgaoJulgadorColegiado);
		instance.setOrgaoJulgador(this.orgaoJulgador);
        instance.setUsuarioLocalizacao(usuarioLocalizacao);
        instance.setIdUsuarioLocalizacaoMagistradoServidor(usuarioLocalizacao.getIdUsuarioLocalizacao());
        if(instance.getDtInicio() == null){
        	instance.setDtInicio(new Date());
        }

        try {
            getEntityManager().persist(instance);
            getEntityManager().flush();
        } catch (AssertionFailure e) {
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof ConstraintViolationException) {
                FacesMessages.instance().clear();
                FacesMessages.instance().add(Severity.ERROR,"Registro de órgãos julgadores já cadastrado!");

                return;
            }
        }


        this.gravaLocalizacaoVisibilidade();

        FacesMessages.instance().clear();
      	FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso!");
      	newInstance();
      	this.setId(usuarioLocalizacao.getIdUsuarioLocalizacao());
      	Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO, usuarioLocalizacao.getUsuario().getIdUsuario());
    }

    private void validarAlteracaoOrgaoJulgador() {
        if (!getInstance().getOrgaoJulgador().equals(this.orgaoJulgador)) {
            getManager().removerVisualizadorProcessosSigilosos(
                    getInstance().getUsuarioLocalizacao().getUsuario().getIdUsuario(),
                    getInstance().getOrgaoJulgador().getIdOrgaoJulgador(),
                    getInstance().getIdUsuarioLocalizacaoMagistradoServidor());
        }
    }

    /**
     * Método responsável por remover a localização do servidor, na aba Localização
	 */
	public String removerServidor(UsuarioLocalizacaoMagistradoServidor obj) {
		UsuarioLocalizacao ul = obj.getUsuarioLocalizacao();
		String query = 
				"select count(o) from UsuarioLocalizacaoVisibilidade o "
				+ "where o.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :idUsuLocMagistrado";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("idUsuLocMagistrado", obj.getIdUsuarioLocalizacaoMagistradoServidor());
		Long retorno = 0L;
		
		try {
			retorno = (Long) q.getSingleResult();
		} catch (NoResultException no) {
		}
		
		if (Authenticator.getPapelAtual().toString().equals(ul.getPapel().toString())
				&& Authenticator.getIdUsuarioLogado().equals(ul.getUsuario().getIdUsuario())) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"Não é possível remover/inativar o perfil do próprio usuário.");
			
			return null;
		}
		
		if (retorno > 0) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR,
					"A Localização só poderá ser excluída quando não houver nenhuma Visibilidade relacionada à mesma.");
			return null;
		}
		
		Usuario usuario = obj.getUsuarioLocalizacao().getUsuario();
		
		if (usuario.getUsuarioLocalizacaoInicial() != null && usuario.getUsuarioLocalizacaoInicial().equals(obj.getUsuarioLocalizacao())){
			usuario.setUsuarioLocalizacaoInicial(null);
			getEntityManager().persist(usuario);
		}
		
		obj.setUsuarioLocalizacao(null);

		try {
			getEntityManager().remove(obj);
			getEntityManager().remove(ul);
            this.getManager().removerVisualizadorProcessosSigilosos(ul.getUsuario().getIdUsuario(), obj.getOrgaoJulgador().getIdOrgaoJulgador(), obj.getIdUsuarioLocalizacaoMagistradoServidor());
			EntityUtil.flush();
			VisualizadoresSigiloHome.instance().inativarVisualizadorLocalizacao(usuario,ul.getUsuarioLocalizacaoMagistradoServidor().getOrgaoJulgador());			
			Events.instance().raiseAsynchronousEvent(Eventos.EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO, ul.getUsuario().getIdUsuario());
			Authenticator.deslogar(ul.getUsuario(), "perfil.atualizar");
		} catch (Exception e) {
			e.printStackTrace();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível excluir!");
			return null;
		}

		newInstance();

		return "removed";
	}

    public void setTitularidade(Boolean titularidade) {
        this.titularidade = titularidade;
    }

    public Boolean getTitularidade() {
        return titularidade;
    }

    public void setMagistrado(PessoaMagistrado magistrado) {
        this.magistrado = magistrado;
    }

    public PessoaMagistrado getMagistrado() {
        return magistrado;
    }

    private PessoaMagistradoSuggestBean getPessoaMagistradoSuggest() {
        PessoaMagistradoSuggestBean pessoaMagistradoSuggest = (PessoaMagistradoSuggestBean) Component.getInstance(
                "pessoaMagistradoSuggest");

        return pessoaMagistradoSuggest;
    }

    public Boolean verificaProcessosMagistradoAuxiliar(
        UsuarioLocalizacaoMagistradoServidor obj) {
        String query = "select count(o) from ProcessoTrfUsuarioLocalizacaoMagistradoServidor o " +
            "where o.usuarioLocalizacaoMagistradoServidor.idUsuarioLocalizacaoMagistradoServidor = :idUsuLocMagistrado";
        Query q = getEntityManager().createQuery(query);
        q.setParameter("idUsuLocMagistrado",
            obj.getIdUsuarioLocalizacaoMagistradoServidor());

        Long retorno = 0L;

        try {
            retorno = (Long) q.getSingleResult();
        } catch (NoResultException no) {
            return Boolean.FALSE;
        }

        return retorno > 0;
    }

    public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
        this.localizacaoFisica = localizacaoFisica;
    }

    public Localizacao getLocalizacaoFisica() {
        return localizacaoFisica;
    }

    public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Integer getIdUsuarioLocalizacaoMagistradoServidor() {
		return idUsuarioLocalizacaoMagistradoServidor;
	}

	public void setIdUsuarioLocalizacaoMagistradoServidor(Integer idUsuarioLocalizacaoMagistradoServidor) {
		this.idUsuarioLocalizacaoMagistradoServidor = idUsuarioLocalizacaoMagistradoServidor;
	}

    @SuppressWarnings("unchecked")
    public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems() {
    	StringBuilder sb = new StringBuilder();
        sb.append(
            "select o.orgaoJulgadorColegiado from OrgaoJulgadorColegiadoOrgaoJulgador o ");
        sb.append("where o.orgaoJulgador = :orgaoJulgador");

        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter("orgaoJulgador",
            OrgaoJulgadorHome.instance().getInstance());

        return q.getResultList();
    }

    /**
	 * retorna a lista de cargos que um magistrado pode usar para realizar a 
	 * vinculação regimental. 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorCargo> getOrgaoJulgadorCargoVinculacaoItems() {

			StringBuilder sb = new StringBuilder();
			sb.append("select o ");
			sb.append("from OrgaoJulgadorCargo o ");			
			sb.append("where o.orgaoJulgador = :orgaoJulgador ");
			sb.append("and o.ativo is true ");
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", OrgaoJulgadorHome.instance().getInstance());
			
			return q.getResultList();
	}

    public boolean existeMagistradoTitular() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
        sb.append("where o.orgaoJulgador = :orgaoJulgador ");
        sb.append("and o.usuarioLocalizacao.papel = :papel ");
        sb.append("and (o.dtFinal is null or o.dtFinal >= :dtInicio) ");
        sb.append("and o.magistradoTitular = true ");
        if(ParametroUtil.instance().isPrimeiroGrau()){
        	sb.append("and o.idUsuarioLocalizacaoMagistradoServidor !=  :idUsuarioLocalizacaoMagistradoServidor ");
        }else if (getInstance().getOrgaoJulgadorColegiado() != null){
        	sb.append("and o.orgaoJulgadorColegiado = :orgaoJulgadorColegiado");
        }        

        Query q = getEntityManager().createQuery(sb.toString());
        q.setParameter("orgaoJulgador",OrgaoJulgadorHome.instance().getInstance());
        q.setParameter("papel", ParametroUtil.instance().getPapelMagistrado());
        q.setParameter("dtInicio", getInstance().getDtInicio());
        if(ParametroUtil.instance().isPrimeiroGrau()){
        	q.setParameter("idUsuarioLocalizacaoMagistradoServidor", getInstance().getIdUsuarioLocalizacaoMagistradoServidor());
        }else if (getInstance().getOrgaoJulgadorColegiado() != null){
        	q.setParameter("orgaoJulgadorColegiado", getInstance().getOrgaoJulgadorColegiado());
        }
        
        Long numeroDeMagistradosTitulares = (Long) q.getSingleResult();
        return numeroDeMagistradosTitulares > 0;
    }
    
    private VinculacaoUsuarioManager getVinculacaoUsuarioManager(){
 		return ComponentUtil.getComponent(VinculacaoUsuarioManager.NAME);
 	}

	public Localizacao getLocalizacaoModelo() {
		return localizacaoModelo;
	}

	public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
		this.localizacaoModelo = localizacaoModelo;
	}

	public boolean isCadastroValidado() {
		return cadastroValidado;
	}

	public void setCadastroValidado(boolean cadastroValidado) {
		this.cadastroValidado = cadastroValidado;
	}
	
	public Localizacao getLocalizacaoModeloRoot() {
		Localizacao localizacaoFisicaReferencia = null;
		if(this.isFormularioOrgaoJulgador()) {
			OrgaoJulgador orgaoJulgador = OrgaoJulgadorHome.instance().getInstance();
			localizacaoFisicaReferencia = orgaoJulgador.getLocalizacao();
		}else if(this.instance != null) {
			if(this.instance.getLocalizacaoFisica() != null) {
				localizacaoFisicaReferencia = this.instance.getLocalizacaoFisica();
			}else if(this.instance.getOrgaoJulgador() != null && this.instance.getOrgaoJulgador().getLocalizacao() != null) {
				localizacaoFisicaReferencia = this.instance.getOrgaoJulgador().getLocalizacao();
			}else if(this.instance.getOrgaoJulgadorColegiado() != null && this.instance.getOrgaoJulgadorColegiado().getLocalizacao() != null) {
				localizacaoFisicaReferencia = this.instance.getOrgaoJulgadorColegiado().getLocalizacao();
			}
		}
		if(localizacaoFisicaReferencia != null) {
			return localizacaoFisicaReferencia.getEstruturaFilho();
		}
		return null;
	}
}
