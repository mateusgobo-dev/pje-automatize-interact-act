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

import br.com.infox.pje.manager.PessoaFisicaManager;
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
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PessoaFisicaRestController.NAME)
@Scope(ScopeType.EVENT)
@Path("/pje-legacy/api/v1")
@Restrict("#{identity.loggedIn}")
public class PessoaFisicaRestController implements Serializable{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "pessoaFisicaRestController"; 
	
	@Logger
	private Log logger;	
	
	@In
	private PessoaFisicaManager pessoaFisicaManager; 
	
	private PessoaConverter pessoaConverter = new PessoaConverter();
	
	private ApiFilterUtil apiFilterUtil;
	
	private List<String> mensagens = new ArrayList<String>();
	
	@Create
	public void init() {
		this.apiFilterUtil = new ApiFilterUtil();
		this.pessoaConverter = new PessoaConverter();
	}	
	
	@GET
	@Path("/pessoas-fisicas/{numeroDocumento:.+}")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<Pessoa> recuperarPessoaFisica(@PathParam("numeroDocumento") String numeroDocumento) {
		PjeResponse<Pessoa> response = new PjeResponse<Pessoa>(PjeResponseStatus.OK, "200", null, new Pessoa());
		
		if(InscricaoMFUtil.verificaCPF(numeroDocumento)) {
			// Recuperar pessoa fisica pelo documento de identificacao
			PessoaFisica pf = this.pessoaFisicaManager.findByCPF(numeroDocumento);
			if(pf != null) {
				PessoaConverter converter = new PessoaConverter();
				Pessoa pessoa = converter.convertFrom(pf);
				response = new PjeResponse<Pessoa>(PjeResponseStatus.OK, "200", null, pessoa);
			}
		}
		
		return response;
	}
	
	@GET
	@Path("/pessoas-fisicas")
	@Produces(MediaType.APPLICATION_JSON)
	public PjeResponse<List<Pessoa>> recuperarPessoasFisicasComFiltro(@QueryParam("filter") String filter) {
		PjeResponse<List<Pessoa>> response = new PjeResponse<List<Pessoa>>(PjeResponseStatus.OK, "200", null, new ArrayList<>());
		
		if(!StringUtil.isEmpty(filter)) {
			try {
				List<Pessoa> listaPessoas = new ArrayList<>();
				JsonNode rootNode = this.getMapper().readTree(filter);
				ApiFilter apiFilter = this.apiFilterUtil.convertToApiFilter(rootNode);
				ApiCriteria criteriaDocumentosIdentificatorios = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.numeroDocumentoPrincipal");
				
				if(criteriaDocumentosIdentificatorios != null) {
					ApiCondition conditionIn = null;
					conditionIn = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatorios, ApiConditionOperatorEnum.IN);
					
					if(conditionIn != null) {
						filterbyMultiplesCpfs(listaPessoas, conditionIn);
					} else {					
						filterByCpfAndNome(listaPessoas, apiFilter, criteriaDocumentosIdentificatorios);
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

	private void filterbyMultiplesCpfs(List<Pessoa> listaPessoas, ApiCondition conditionIn) {
		String[] items = conditionIn.getValue().replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
		if(items.length > 0) {
			List<PessoaFisica> listaPF = this.pessoaFisicaManager.findByMultipleCPF(Arrays.asList(items));
			for(PessoaFisica pf : listaPF) {
				listaPessoas.add(this.pessoaConverter.convertFrom(pf));
			}
		}
	}

	private void filterByCpfAndNome(List<Pessoa> listaPessoas, ApiFilter apiFilter,
			ApiCriteria criteriaDocumentosIdentificatorios) {
		ApiCriteria criteriaDocumentosIdentificatoriosNome = this.apiFilterUtil.findCriteriaByField(apiFilter, "dadosBasicos.nome");	
		if(criteriaDocumentosIdentificatoriosNome != null) {
			ApiCondition conditionContainsCpf = null;
			ApiCondition conditionContainsNome = null;
			conditionContainsCpf = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatorios, ApiConditionOperatorEnum.CONTAINS);
			conditionContainsNome = this.apiFilterUtil.findConditionByOperator(criteriaDocumentosIdentificatoriosNome, ApiConditionOperatorEnum.CONTAINS);
			if (conditionContainsCpf != null && conditionContainsNome != null) {
				String nome = conditionContainsNome.getValue().replaceAll("\"", "");
				String numeroCpf = conditionContainsCpf.getValue().replaceAll("\"", "");
				List<PessoaFisica> listaPF = this.pessoaFisicaManager.findByNomeAndCPF(numeroCpf, nome);
				for (PessoaFisica pf : listaPF) {
					listaPessoas.add(this.pessoaConverter.convertFrom(pf));
				}
			}					
		}
	}
	
	private ObjectMapper getMapper() {
		PjeJSONProvider jsonProvider = new PjeJSONProvider();
		return jsonProvider.getObjectMapper();
	}
}
