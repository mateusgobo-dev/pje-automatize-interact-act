package br.jus.cnj.pje.webservice.client.correios;

import javax.xml.ws.WebServiceException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.MunicipioManager;
import br.jus.cnj.pje.nucleo.service.CepService;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * 
 * @author Thiago Vieira
 * 
 */
@Name(ConsultaClienteCorreios.NAME)
@Scope(ScopeType.EVENT)
public class ConsultaClienteCorreios {
	
	public static final String NAME = "consultaClienteCorreios";
	
	@Logger
	private Log logger;
	
	@In
	private ParametroService parametroService;
	
	@In
	private MunicipioManager municipioManager;
	
	@In
	private EstadoManager estadoManager;
	
	public Cep consultaCep(String cep) {
		
		cep = CepService.removeMascaraCep(cep);
		String wsdl = parametroService.valueOf("urlWsdlCorreios");
		if (Strings.isEmpty(wsdl)) {
			String message = "O endereço WSDL do serviço de consulta de CEP junto aos Correios não foi definido.";
			// throw new IllegalStateException();
			logger.error(message);
			return null;
		}
		
		EnderecoERP consultaCEP = null;
		try {
			AtendeClienteService service = new AtendeClienteService();
			AtendeCliente port = service.getAtendeClientePort();
			consultaCEP = port.consultaCEP(cep);
		} catch (SQLException_Exception e) {
			logger.error("Erro na execução de consulta ao serviço dos Correios. " +e.getLocalizedMessage());
			return null;
		} catch (SigepClienteException e) {
			logger.error("Erro na execução de consulta ao serviço dos Correios. " +e.getLocalizedMessage());
			return null;
		}
		catch(WebServiceException e){
			logger.error("Erro na comunicação com o serviço de consulta ao serviço dos Correios. " +e.getLocalizedMessage());
			return null;
		}
		
		if(consultaCEP == null || StringUtil.isEmpty(consultaCEP.getCep())){
			return null;
		}
		
		return processaResposta(consultaCEP);
	}

	private Cep processaResposta(EnderecoERP consultaCEP) {
		
		Municipio municipio = municipioManager.findByUfAndDescricao(consultaCEP.getUf(), consultaCEP.getCidade());
		if(municipio == null){
			Estado estado = estadoManager.findBySigla(consultaCEP.getUf());
			
			municipio = new Municipio();
			municipio.setAtivo(true);
			municipio.setEstado(estado);
			if(consultaCEP.getCidade() != null){
				municipio.setMunicipio(consultaCEP.getCidade().toUpperCase());
			}
			try {
				municipioManager.persistAndFlush(municipio);

			} catch (PJeBusinessException e) {
				logger.error("Erro ao persistir o municipio "+municipio +". " + e.getLocalizedMessage());
				return null;
			}
		
		}
		Cep cep = new Cep();
		cep.setAtivo(true);
		cep.setComplemento(consultaCEP.getComplemento());
		cep.setMunicipio(municipio);
		cep.setNomeBairro(consultaCEP.getBairro());
		cep.setNomeLogradouro(consultaCEP.getEnd());
		cep.setNumeroCep(CepService.acrescentaMascaraCep(consultaCEP.getCep()));
		return cep;
	}

}
