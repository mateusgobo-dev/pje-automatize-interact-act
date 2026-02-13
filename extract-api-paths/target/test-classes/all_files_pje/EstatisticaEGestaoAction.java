package br.jus.jt.estatistica.action;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.cnj.pje.list.OrgaoJulgadorList;
import br.jus.csjt.pje.business.pdf.HtmlParaPdf;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.jt.estatistica.list.QuadroList;
import br.jus.jt.estatistica.list.RelatorioList;
import br.jus.pje.jt.entidades.estatistica.Periodo;
import br.jus.pje.jt.entidades.estatistica.Quadro;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Usuario;

@Name(value = EstatisticaEGestaoAction.NAME) 
@Scope(ScopeType.CONVERSATION)
//@BypassInterceptors
public class EstatisticaEGestaoAction implements Serializable {

	private static final long serialVersionUID = 468189708667664833L;

	public static final String NAME = "estatisticaEGestaoAction";
	
	@In(create=true)
	RelatorioList relatorioList;

	@In(create=true)
	OrgaoJulgadorList orgaoJulgadorList ;
	
	@In(create=true)
	QuadroList quadroList ;
	
	@Logger
	private Log log;
	
	@In
	private EntityManager entityManager;
	
	// atributos da seleção da tela
	private String relatorioSelecionado ;
	private Quadro quadro ;
	private String mes ;
	private String ano ;
	private Periodo periodo = new Periodo(null,null);
	private OrgaoJulgador orgaoJulgador;

	private InstanciaBoletim instanciaBoletim = new InstanciaBoletim();

	public void inicia() {
		// TODO: tirar o teste
		//System.out.println("Iniciou a action");
	}
	
	public void regerar() {
		if (mes.isEmpty() || ano.isEmpty() || Integer.parseInt(mes) < 1 || Integer.parseInt(mes) > 12) {
			FacesMessages.instance().add(Severity.ERROR, "Informe o mês/ano corretamente antes de gerar.");
		} else {

			Usuario u = ParametroUtil.instance().getUsuarioSistema();
	
			// Gerar boletim (Chamar funcao)
			try {			
				if (orgaoJulgador == null) {
					entityManager
					.createNativeQuery(
							"SELECT jt.boletim_gerar_boletim_estatistico(" +
									mes + ", " +
									ano +", " +
									u.getIdUsuario() +
									")"
							)
							.getSingleResult();
				} else { 				
					entityManager
					.createNativeQuery(
							"SELECT CAST(jt.boletim_calcular_boletim_estatistico(" +
									mes + ", " +
									ano +", " +
									orgaoJulgador.getIdOrgaoJulgador() +", " +
									u.getIdUsuario() +
									") AS TEXT)"
							)
							.getSingleResult();
				}
				entityManager.flush();
				log.info("Boletim gerado com sucesso!");
				FacesMessages.instance().add(Severity.INFO, "Boletim gerado com sucesso !.");
				
			} catch (Exception e) {
				FacesMessages.instance().add(Severity.ERROR, "Erro ao regerar boletim !");
				log.error("Erro ao gerar boletim", e);
			}
		}
	}

	public void gerarPDF() {

		if (mes.isEmpty() || ano.isEmpty() || Integer.parseInt(mes) < 1 || Integer.parseInt(mes) > 12) {
			FacesMessages.instance().add(Severity.ERROR, "Informe o mês/ano corretamente antes de gerar.");
		} else {
			instanciaBoletim.getRelatorios().clear();
			periodo.setPeriodo(Integer.valueOf(mes), Integer.valueOf(ano));
			String conteudoHtml = instanciaBoletim.relatorioHtml(relatorioList);
			
			if (conteudoHtml.isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Nenhum dado encontrado para o mês/ano e orgão julgador informados.");
			} else {
				
				byte[] conteudoPdf = null;
				
				// TODO: Gerar PDF em função do html:
				try {
					conteudoPdf = HtmlParaPdf.converte(conteudoHtml);
				} catch (PdfException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// TODO: Fazer download do PDF:
				if( instanciaBoletim.getRelatorios().size() > 0 ) {
					
					FacesContext facesContext = FacesContext.getCurrentInstance();			
					HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();  
					try {
						download(nomeArquivo("pdf"), conteudoPdf, response);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					facesContext.responseComplete();
					
				}
			}
			
		}
		
			
	}

	public void gerarXML() {
		if (mes.isEmpty() || ano.isEmpty() || Integer.parseInt(mes) < 1 || Integer.parseInt(mes) > 12) {
			FacesMessages.instance().add(Severity.ERROR, "Informe o mês/ano corretamente antes de gerar.");
		} else {

			instanciaBoletim.getRelatorios().clear();
			periodo.setPeriodo(Integer.valueOf(mes), Integer.valueOf(ano));
			String xml = instanciaBoletim.relatorioXML(relatorioList);
			
			if (xml.isEmpty()) {
				FacesMessages.instance().add(Severity.ERROR, "Nenhum dado encontrado para o mês/ano e orgão julgador informados.");
			} else {
					
				if( instanciaBoletim.getRelatorios().size() > 0 ) {
					
					FacesContext facesContext = FacesContext.getCurrentInstance();
					HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();  
					try {
						download(nomeArquivo("xml"), xml.getBytes(), response);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					facesContext.responseComplete();
					
				}
			}
		}
		
	}
	
	public String nomeArquivo(String extensao) {		
		if( instanciaBoletim.getRelatorios().size() > 0 ) {			
			OrgaoJulgador oj = instanciaBoletim.getRelatorios().get(0).getOrgaoJulgador();
			String sigla = oj.getSigla();
			String mesano = instanciaBoletim.getRelatorios().get(0).getPeriodo().toString();
			String nomeArquivo = "relatorio_"+sigla+"_"+mesano+"."+extensao;

			return nomeArquivo;			
		}
		return "";
	}
	
	public static void download( String fileName, byte[] content, HttpServletResponse response )
            throws IOException {
       
        response.addHeader( "Content-Disposition",
                "attachment; filename=" + fileName );
       
        response.setContentType( "application/octet-stream" );
        ServletOutputStream outStream = response.getOutputStream();
       
        try {
            outStream.write(content);
            outStream.flush();
        } finally {
            outStream.close();
        }
 
    }	

	public void limparCampos() {
		
	}
	
	public void limparLista() {
		
	}
	
	public void gravarLogRelatorio() {
		
	}
	
	public String getRelatorioSelecionado() {
		return relatorioSelecionado;
	}

	public void setRelatorioSelecionado(String relatorioSelecionado) {
		this.relatorioSelecionado = relatorioSelecionado;
	}

	public Quadro getQuadro() {
		return quadro;
	}

	public void setQuadro(Quadro quadro) {
		this.quadro = quadro;
	}

	public String getMes() {
		return mes;
	}

	public void setMes(String mes) {
		try {
			periodo.setMes(Integer.valueOf(mes));
			this.mes = mes;
		} catch (Exception e) {
			this.mes = "";
		}
	}

	public String getAno() {
		return ano;
	}

	public void setAno(String ano) {
		try {
			periodo.setAno(Integer.valueOf(ano));
			this.ano = ano;
		} catch (Exception e) {
			this.ano = "";
		}
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgadorList getOrgaoJulgadorList() {
		return orgaoJulgadorList;
	}

	public void setOrgaoJulgadorList(OrgaoJulgadorList orgaoJulgadorList) {
		this.orgaoJulgadorList = orgaoJulgadorList;
	}

	public QuadroList getQuadroList() {
		return quadroList;
	}

	public void setQuadroList(QuadroList quadroList) {
		this.quadroList = quadroList;
	}
	
	public RelatorioList getRelatorioList() {
		return relatorioList;
	}

	public void setRelatorioList(RelatorioList relatorioList) {
		this.relatorioList = relatorioList;
	}

	public Periodo getPeriodo() {
		return periodo;
	}

	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
	}
	
}