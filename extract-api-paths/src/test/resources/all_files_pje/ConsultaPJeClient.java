package br.jus.cnj.pje.ws.client;

import java.net.URL;
import java.util.List;

import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.ws.AssuntoJudicial;
import br.jus.cnj.pje.ws.ClasseJudicial;
import br.jus.cnj.pje.ws.Competencia;
import br.jus.cnj.pje.ws.ConsultaPJe;
import br.jus.cnj.pje.ws.ConsultaPJeService;
import br.jus.cnj.pje.ws.Jurisdicao;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;

public class ConsultaPJeClient {
	private EnderecoWsdl enderecoWsdl;
	private URL url;
	private ConsultaPJeService service;
	private ConsultaPJe port;

	public static Log log = Logging.getLog(ParametroUtil.class);
	
	public ConsultaPJeClient(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
		iniciar();
	}
	
	ConsultaPJeClient() {
		iniciar();
	}
	
	private void iniciar() {
		try {
			this.url = new URL(this.enderecoWsdl.getWsdlConsulta());
			this.service = new ConsultaPJeService(url);
			this.port = service.getConsultaPJePort();
		} catch (Exception e) {
			String mensagem = FacesUtil.getMessage("entity_messages",
				"ws.consultapje.erro.iniciar",
				(enderecoWsdl != null ? enderecoWsdl.getDescricao() : null), 
    			(enderecoWsdl != null ? enderecoWsdl.getWsdlConsulta() : null)
        	);
			
			throw new AplicationException(mensagem);
		}
	}

	public List<Jurisdicao> consultarJurisdicoes() {
		return port.consultarJurisdicoes();
	}

	public List<ClasseJudicial> consultarClassesJudiciais(Jurisdicao jurisdicao) {
		return port.consultarClassesJudiciais(jurisdicao);
	}

	public List<AssuntoJudicial> consultarAssuntosJudiciais(Jurisdicao jurisdicao, ClasseJudicial classeJudicial) {
		return port.consultarAssuntosJudiciais(jurisdicao, classeJudicial);
	}

	public List<Competencia> consultarCompetencias(Jurisdicao jurisdicao, ClasseJudicial classeJudicial,
			List<AssuntoJudicial> assuntos) {
		return port.consultarCompetencias(jurisdicao, classeJudicial, assuntos);
	}
	
	/**
	 * Metodo responsavel por recuperar as classes judiciais para remessa entre
	 * instancias.
	 * 
	 * @param jurisdicao
	 *            a jurisdicao que se deseja pesquisar
	 * @return <code>List</code> de classes judiciais.
	 */
	public List<ClasseJudicial> consultarClassesJudiciaisRemessa(Jurisdicao jurisdicao) {
		return port.consultarClassesJudiciaisRemessa(jurisdicao);
	}
	
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}
	
}
