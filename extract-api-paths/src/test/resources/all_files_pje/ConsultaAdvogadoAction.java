/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.PessoaAdvogadoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAdvogadoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Compoenente de controle da tela de pesquisa do CRUD de PessoaAdvogado
 * @author Rodrigo Santos Menezes
 *
 */
@Name(ConsultaAdvogadoAction.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaAdvogadoAction extends BaseAction<PessoaAdvogado>{

	public static final String NAME = "consultaAdvogadoAction";
	private static final long serialVersionUID = 1L;
	
	private String nome;
	private Estado estadoOAB;
	private String numeroOAB;
	private String letraOAB;
	private String cpf;
	private Boolean ativo = Boolean.TRUE;
	private Date dataInicioCadastro;
	private Date dataFimCadastro;
	private EntityDataModel<PessoaAdvogado> model;
	
	@In
	private PessoaAdvogadoManager pessoaAdvogadoManager;
	
	@In
	private EstadoManager estadoManager;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private PessoaAdvogadoHome pessoaAdvogadoHome;
	
	@RequestParameter(value="advogadoGridCount")
	private Integer advogadoGridCount;	
	
	@Create
	public void init(){
		pesquisar();
	}
	
	public void pesquisar(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		
		try {			
			model = new EntityDataModel<PessoaAdvogado>(PessoaAdvogado.class, super.facesContext, getRetriever());
			model.setCriterias(criterios);
			model.addOrder("o.nome", Order.ASC);
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao executar a pesquisa: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Este método adiciona os critérios de pesquisa preenchidos na tela.
	 * 
	 * @return Lista de critérios de pesquisa
	 */
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		criterios.add(Criteria.equals("pessoa.unificada", Boolean.FALSE));
		
		if (getAtivo() != null) {
			if(getAtivo() == Boolean.TRUE){
				criterios.add(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.ADV, PessoaFisica.ADV));
			} else {
				criterios.add(Criteria.not(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.ADV, PessoaFisica.ADV)));
			}
		}
		
		if(!Strings.isEmpty(nome)){
			criterios.add(Criteria.contains("nome", nome));
		}
		
		if (estadoOAB != null || !Strings.isEmpty(numeroOAB)){
			if (estadoOAB != null){
				criterios.add(Criteria.equals("ufOAB.idEstado",estadoOAB.getIdEstado()));
			}
			if (!Strings.isEmpty(numeroOAB)){
				criterios.add(Criteria.startsWith("numeroOAB",numeroOAB));
				if(!Strings.isEmpty(letraOAB)){
					criterios.add(Criteria.equals("letraOAB", letraOAB));
				}
			}
		}

		if(!Strings.isEmpty(cpf)){
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","CPF"));
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.ativo",Boolean.TRUE));
			criterios.add(Criteria.startsWith("pessoaDocumentoIdentificacaoList.numeroDocumento", cpf));
		}
		
		if (dataInicioCadastro != null && dataFimCadastro == null){
		 	criterios.add(Criteria.greaterOrEquals("dataCadastro",dataInicioCadastro));
		}else if (dataFimCadastro != null && dataInicioCadastro == null){		 				
			criterios.add(Criteria.lessOrEquals("dataCadastro",dataFimCadastro));
		}else if(dataInicioCadastro != null && dataFimCadastro != null){
		 	Calendar novaDataAutuacaoFim = Calendar.getInstance();	
		 	novaDataAutuacaoFim.setTime(dataFimCadastro);
		 	novaDataAutuacaoFim.add(Calendar.DAY_OF_MONTH, 1);	
		 	criterios.add(Criteria.between("dataCadastro", dataInicioCadastro, novaDataAutuacaoFim.getTime()));
	    }
		
		return criterios;
	}
	
	@Override
	protected DataRetriever<PessoaAdvogado> getRetriever() {
		final PessoaAdvogadoManager manager = (PessoaAdvogadoManager)getManager();
		final Integer tableCount = advogadoGridCount;
		DataRetriever<PessoaAdvogado> retriever = new DataRetriever<PessoaAdvogado>() {
			@Override
			public PessoaAdvogado findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<PessoaAdvogado> list(Search search) {
				return manager.list(search);
			}
			@Override
			public long count(Search search) {
				if (tableCount != null && tableCount >=0){
					return tableCount;
				}

				return manager.count(search);
			}
			@Override
			public Object getId(PessoaAdvogado obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}
	
	public List<Estado> getEstados(){
		return estadoManager.estadoItems();
	}
	
	/**
	 * Este método inativa o advogado informado
	 * @param instance O {@link PessoaAdvogado} informado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String inativa(PessoaAdvogado instance) {		
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaAdvogado.class);
			instance = pessoaFisica.getPessoaAdvogado();
			instance.setAdvogadoAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaAdvogado.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}	
	
	/**
	 * Este método reativa o advogado.
	 * @param pessoaAdvogado {@link PessoaAdvogado}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativarAdvogado(PessoaAdvogado pessoaAdvogado){
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(pessoaAdvogado.getPessoa(), PessoaAdvogado.class);
			pessoaAdvogado = pessoaFisica.getPessoaAdvogado();
			pessoaAdvogado.setAdvogadoAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaAdvogado.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}
	
	public void limparCamposPesquisa(){
		this.nome = null;
		this.estadoOAB = null;
		this.numeroOAB = null;
		this.letraOAB = null;
		this.cpf = null;
		this.dataFimCadastro = null;
		this.dataInicioCadastro = null;
		this.ativo = null;
	}
	
	@Override
	protected BaseManager<PessoaAdvogado> getManager() {
		return pessoaAdvogadoManager;
	}

	@Override
	public EntityDataModel<PessoaAdvogado> getModel() {
		return this.model;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Estado getEstadoOAB() {
		return estadoOAB;
	}

	public void setEstadoOAB(Estado estadoOAB) {
		this.estadoOAB = estadoOAB;
	}

	public String getNumeroOAB() {
		return numeroOAB;
	}

	public void setNumeroOAB(String numeroOAB) {
		this.numeroOAB = numeroOAB;
	}

	public String getLetraOAB() {
		return letraOAB;
	}

	public void setLetraOAB(String letraOAB) {
		this.letraOAB = letraOAB;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Date getDataInicioCadastro() {
		return dataInicioCadastro;
	}

	public void setDataInicioCadastro(Date dataInicioCadastro) {
		this.dataInicioCadastro = dataInicioCadastro;
	}

	public Date getDataFimCadastro() {
		return dataFimCadastro;
	}

	public void setDataFimCadastro(Date dataFimCadastro) {
		this.dataFimCadastro = dataFimCadastro;
	}

	public Integer getAdvogadoGridCount() {
		return advogadoGridCount;
	}
	
	public void setAdvogadoGridCount(Integer advogadoGridCount) {
		this.advogadoGridCount = advogadoGridCount;
	}
	
	/**
	 * Retorna true se os campos obrigatórios do formulário estiverem preenchidos.
	 * 
	 * @return booleano
	 */
	public boolean isPreenchidoCamposObrigatorios(){
		PessoaAdvogado advogado = pessoaAdvogadoHome.getInstance();
		
		return (StringUtils.isNotBlank(advogado.getNumeroCPF()) &&
				StringUtils.isNotBlank(advogado.getNome()) &&
				advogado.getDataNascimento() != null &&
				StringUtils.isNotBlank(advogado.getNomeGenitora()) &&
				StringUtils.isNotBlank(advogado.getNumeroOAB()) &&
				advogado.getUfOAB() != null &&
				advogado.getTipoInscricao() != null && 
				advogado.getSexo() != null &&
				StringUtils.isNotBlank(advogado.getDddCelular()) &&
				StringUtils.isNotBlank(advogado.getNumeroCelular()) &&
				StringUtils.isNotBlank(advogado.getEmail()));
	}

}
