package br.jus.pje.api.controllers.v1;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.PessoaJuridicaManager;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponse;
import br.jus.cnj.pje.pjecommons.model.services.PjeResponseStatus;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Pessoa;
import br.jus.cnj.pje.webservice.PjeJSONProvider;
import br.jus.pje.api.converters.PessoaConverter;
import br.jus.pje.api.filter.ApiCondition;
import br.jus.pje.api.filter.ApiConditionOperatorEnum;
import br.jus.pje.api.filter.ApiCriteria;
import br.jus.pje.api.filter.ApiFilter;
import br.jus.pje.api.filter.ApiFilterUtil;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaJuridicaRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
@Restrict("#{identity.loggedIn}")
public class PessoaJuridicaRestController implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaJuridicaRestController";
	
	@Logger
	private Log logger;
	
	@In
	private PessoaJuridicaManager pessoaJuridicaManager;
	
	private PessoaConverter pessoaConverter = new PessoaConverter();
	
	private ApiFilterUtil apiFilterUtil;
	
	private List<String> mensagens = new ArrayList<String>();
	
	@Create
	public void init() {
		this.apiFilterUtil = new ApiFilterUtil();
		this.pessoaConverter = new PessoaConverter();
	}
	
	@GET
	@Path("/pessoas-juridicas/{numeroDocumento:.+}")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<Pessoa> recuperarPessoaJuridica(@PathParam("numeroDocumento") String numeroDocumento) {
		PjeResponse<Pessoa> response = new PjeResponse<Pessoa>(PjeResponseStatus.OK, "200", null, new Pessoa());
		
		if(InscricaoMFUtil.verificaCNPJ(numeroDocumento)) {
			PessoaJuridica pj = this.pessoaJuridicaManager.findByCNPJ(numeroDocumento);
			if(pj != null) {
				Pessoa pessoa = this.pessoaConverter.convertFrom(pj);
				response = new PjeResponse<Pessoa>(PjeResponseStatus.OK, "200", null, pessoa);
			}
		}
		
		return response;		
	}
	
	@GET
	@Path("/pessoas-juridicas")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<List<Pessoa>> recuperarPessoasJuridicasComFiltro(@QueryParam("filter") String filter) {
		PjeResponse<List<Pessoa>> response = new PjeResponse<List<Pessoa>>(PjeResponseStatus.OK, "200", null, new ArrayList<>());
		
		if(!StringUtil.isEmpty(filter)) {
			try {
				List<Pessoa> listaPessoas = new ArrayList<Pessoa>();
				JsonNode rootNode = this.getMapper().readTree(filter);
				ApiFilter apiFilter = this.apiFilterUtil.convertToApiFilter(rootNode);
				ApiCriteria criteriaDocumentosIdentificatorios = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.numeroDocumentoPrincipal");
				ApiCondition conditionIn = null;
				
				if(criteriaDocumentosIdentificatorios != null) {
					conditionIn = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatorios, ApiConditionOperatorEnum.IN);
					
					if(conditionIn != null) {
						filterByMultiplesCNPJs(listaPessoas, conditionIn);
					} else {
						findByCNPJOuNome(listaPessoas, apiFilter, criteriaDocumentosIdentificatorios);
					}
				}
				
				response = new PjeResponse<List<Pessoa>>(PjeResponseStatus.OK, "200", null, listaPessoas);
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
				mensagens.add("Não foi possível converter o filtro");
				response = new PjeResponse<List<Pessoa>>(PjeResponseStatus.OK, "200", mensagens, new ArrayList<>());
			}
		}
		
		return response;
	}
	
	@GET
	@Path("/pessoas-juridicas/secao-judiciaria")
	@Produces(MediaType.APPLICATION_JSON)	
	public PjeResponse<Pessoa> getSecaoJudiciaria(){
		PjeResponse<Pessoa> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, null);	
		try {
			Integer idOJ = Authenticator.getIdOrgaoJulgadorAtual();
			OrgaoJulgador oJ = EntityUtil.find(OrgaoJulgador.class, idOJ);
			Jurisdicao j = EntityUtil.find(Jurisdicao.class, oJ.getJurisdicao().getIdJurisdicao());
			PessoaJuridica pessoaSecaoJudiciaria = j.getPessoaJuridicaSecao();
			if (pessoaSecaoJudiciaria != null) {
				response.setResult(this.pessoaConverter.convertFrom(pessoaSecaoJudiciaria));
			} else {
				mensagens.add("Seção judiciária não encontrada");
				response.setMessages(mensagens);			
			}			
		}catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			mensagens.add("Erro ao buscar seção judiciária");
		}
		return response;
	}
	
	@GET
	@Path("/pessoas-juridicas/inss")
	@Produces(MediaType.APPLICATION_JSON)	
	public PjeResponse<Pessoa> getPessoaINSS(){
		PjeResponse<Pessoa> response = new PjeResponse<>(PjeResponseStatus.OK, "200", null, null);	
		try {
			PessoaJuridica pessoaINSS = pessoaJuridicaManager.findById(ParametroUtil.instance().getPessoaINSS().getIdPessoa());
			if (pessoaINSS != null) {
				response.setResult(this.pessoaConverter.convertFrom(pessoaINSS));
			} else {
				mensagens.add("Pessoa INSS não encontrada");
				response.setMessages(mensagens);			
			}			
		}catch (Exception e) {
			logger.error(e.getLocalizedMessage());
			mensagens.add("Erro ao buscar seção judiciária");
			response.setStatus(PjeResponseStatus.ERROR);
		}
		return response;
	}


	private void filterByMultiplesCNPJs(List<Pessoa> listaPessoas, ApiCondition conditionIn) {
		String[] items = conditionIn.getValue().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
		if(items.length > 0) {
			List<PessoaJuridica> listaPJ = this.pessoaJuridicaManager.findByMultipleCNPJ(Arrays.asList(items));
			for(PessoaJuridica pj : listaPJ) {
				listaPessoas.add(this.pessoaConverter.convertFrom(pj));
			}
		}
	}
	
	private void findByCNPJOuNome(List<Pessoa> listaPessoas, ApiFilter apiFilter,
			ApiCriteria criteriaDocumentosIdentificatorios) {
		ApiCriteria criteriaDocumentosIdentificatoriosNome = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.nome");	
		if(criteriaDocumentosIdentificatoriosNome != null) {
			ApiCondition conditionContainsCpf = null;
			ApiCondition conditionContainsNome = null;
			conditionContainsCpf = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatorios, ApiConditionOperatorEnum.CONTAINS);
			conditionContainsNome = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatoriosNome, ApiConditionOperatorEnum.CONTAINS);
			if (conditionContainsCpf != null && conditionContainsNome != null) {
				
				String nome = conditionContainsNome.getValue().trim().replaceAll("\"", "");
				String numeroCpf = conditionContainsCpf.getValue().trim().replaceAll("\"", "");
				
				if(numeroCpf.length() == 0)					
					numeroCpf = null;
				
				List<PessoaJuridica> listaPJ = this.pessoaJuridicaManager.findByDocumentoCNPJOuNome(numeroCpf, nome);
				for (PessoaJuridica pj : listaPJ) {
					listaPessoas.add(this.pessoaConverter.convertFrom(pj));
				}
			}					
		}
	}
	
	private ObjectMapper getMapper() {
		PjeJSONProvider jsonProvider = new PjeJSONProvider();		
		return jsonProvider.getObjectMapper();
	}
}
