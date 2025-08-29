package br.jus.cnj.pje.view.fluxo;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.icrrefactory.IcrSentencaCondenatoriaManager;
import br.com.infox.pje.service.AbstractAssinarExpedienteCriminalService;
import br.com.infox.pje.service.AssinarMandadoPrisaoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.MandadoPrisaoManager;
import br.jus.pje.nucleo.entidades.CaracteristicaFisica;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.PenaTotal;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.enums.TipoPrisaoEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Scope(ScopeType.CONVERSATION)
@Name("prepararMandadoPrisaoAction")
public class PrepararMandadoPrisaoAction extends PrepararMandadoAlvaraAction<MandadoPrisao, MandadoPrisaoManager>{

	private static final long serialVersionUID = 7444118110158680978L;

	@In(create = true)
	private MandadoPrisaoManager mandadoPrisaoManager;

	@In(create = true)
	private IcrSentencaCondenatoriaManager icrSCOManager;
	
	@In(create = true)
	private AssinarMandadoPrisaoService assinarMandadoPrisaoService;

	private TipoPrisaoEnum[] tiposPrisao = TipoPrisaoEnum.values();
	private String dadosPenaTotal;
	
	
	
	@Override
	public void init() {
		super.init();
		popularPessoas();
		setMaxPasso(2);
		setPasso(0);
		
		if(isPreparando()){
			pesquisarPartesCandidatas();			
		}else{
			pesquisarExpedientesNaoAssinados();
		}
	}

	@Override
	public void editarProcessoExpedienteCriminal(PessoaFisica pessoa){
		super.editarProcessoExpedienteCriminal(pessoa);		
		getProcessoExpedienteCriminalEdit().setPublicacaoRestrita(getProcessoJudicial().getSegredoJustica());
		dadosPenaTotal = null;
	}

	@Override
	public void editarProcessoExpedienteCriminal(MandadoPrisao expediente){
		super.editarProcessoExpedienteCriminal(expediente);
		dadosPenaTotal = null;
		if (expediente.getTipoPrisao() != null && expediente.getTipoPrisao().equals(TipoPrisaoEnum.DEF)){
			dadosPenaTotal = " <table class='dr-table rich-table' border='0' cellpadding='0'> "
				+ "<colgroup span=\"3\"></colgroup>"
				+ "<thead class=\"dr-table-thead\">"
				+ "     <tr class=\"dr-table-header rich-table-header\">"
				+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
				+ "				Pena"
				+ "			</th>"
				+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
				+ "         	Detalhes da pena original"
				+ "         </th>"
				+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
				+ "             Regime"
				+ "         </th>"
				+ "     </tr>"
				+ "		<tr class=\"dr-table-row rich-table-row dr-table-firstrow rich-table-firstrow\">"
				+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getTipoPena().getGeneroPena().getLabel() + "</td>"
				+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getDescricaoPena() + "</td>"
				+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getRegimePena().getLabel() + "</td>"
				+ "     </tr>" + " </table> ";
		}
	}

	@Override
	public void buscarDemaisMandados(){
		if (getProcessoExpedienteCriminalEdit().getRecaptura()){
			try{
				setMandados(getManager().recuperarDemaisMandadosDoProcesso(getProcessoJudicial(),
						getProcessoExpedienteCriminalEdit().getPessoa(), getProcessoExpedienteCriminalEdit(), true));
			} catch (PJeBusinessException e){
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "pje.error.prepararMandadoPrisaoAction.erroBuscarDemaisMandados");
			}
		}

		if (getMandados() == null || getMandados().isEmpty()){
			getProcessoExpedienteCriminalEdit().setRecaptura(false);
			FacesMessages.instance().add(Severity.ERROR, "pje.error.prepararMandadoPrisaoAction.naoExisteMandadadosCumpridos");
		}
	}

	public void carregarDadosPenaTotal(){
		dadosPenaTotal = null;
		if (getProcessoExpedienteCriminalEdit() != null
			&& getProcessoExpedienteCriminalEdit().getTipoPrisao().equals(TipoPrisaoEnum.DEF)){
			PenaTotal pt = icrSCOManager.buscarUltimaPenaPrivativaLiberdadePessoa(getProcessoJudicial(),
					getProcessoExpedienteCriminalEdit().getPessoa());

			if (pt == null){
				getFacesMessages().addFromResourceBundle(
								Severity.ERROR,
								"pje.error.prepararMandadoPrisaoAction.naoExisteSentencaCondenatoria",
								TipoPrisaoEnum.DEF.getLabel());
				getProcessoExpedienteCriminalEdit().setTipoPrisao(null);
				getProcessoExpedienteCriminalEdit().setTipoPena(null);			
				getProcessoExpedienteCriminalEdit().setRegimePena(null);
				getProcessoExpedienteCriminalEdit().setAnosPena(null);
				getProcessoExpedienteCriminalEdit().setMesesPena(null);
				getProcessoExpedienteCriminalEdit().setDiasPena(null);
				getProcessoExpedienteCriminalEdit().setHorasPena(null);
			}else{
				getProcessoExpedienteCriminalEdit().copiarDadosPenalTotal(pt);
				
				dadosPenaTotal = " <table class='dr-table rich-table' border='0' cellpadding='0'> "
					+ "<colgroup span=\"3\"></colgroup>"
					+ "<thead class=\"dr-table-thead\">"
					+ "     <tr class=\"dr-table-header rich-table-header\">"
					+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
					+ "				Pena"
					+ "			</th>"
					+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
					+ "         	Detalhes da pena original"
					+ "         </th>"
					+ "         <th class=\"dr-table-headercell rich-table-headercell\">"
					+ "             Regime"
					+ "         </th>"
					+ "     </tr>"
					+ "		<tr class=\"dr-table-row rich-table-row dr-table-firstrow rich-table-firstrow\">"
					+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getTipoPena().getGeneroPena().getLabel() + "</td>"
					+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getDescricaoPena() + "</td>"
					+ "         <td class=\"dr-table-cell rich-table-cell\">"+ getProcessoExpedienteCriminalEdit().getRegimePena().getLabel() + "</td>"
					+ "     </tr>" + " </table> ";
			}
		}else{
			getProcessoExpedienteCriminalEdit().setTipoPena(null);			
			getProcessoExpedienteCriminalEdit().setRegimePena(null);
			getProcessoExpedienteCriminalEdit().setAnosPena(null);
			getProcessoExpedienteCriminalEdit().setMesesPena(null);
			getProcessoExpedienteCriminalEdit().setDiasPena(null);
			getProcessoExpedienteCriminalEdit().setHorasPena(null);
		}
	}

	public void selecionarMandadoRecaptura(MandadoPrisao mandado){
		getProcessoExpedienteCriminalEdit().setMandadoPrisaoOrigemRecaptura(mandado);
	}

	@Override
	public MandadoPrisaoManager getManager(){
		return mandadoPrisaoManager;
	}

	public String getDadosPenaTotal(){
		return dadosPenaTotal;
	}

	public void setDadosPenaTotal(String dadosPenaTotal){
		this.dadosPenaTotal = dadosPenaTotal;
	}

	public TipoPrisaoEnum[] getTiposPrisao(){
		return tiposPrisao;
	}

	public void setTiposPrisao(TipoPrisaoEnum[] tiposPrisao){
		this.tiposPrisao = tiposPrisao;
	}
	
	@Override
	public AbstractAssinarExpedienteCriminalService<MandadoPrisao> getAssinarExpedienteCriminalService(){
		return assinarMandadoPrisaoService;
	}
	

	/*
	 * Métodos utilizados no modelo de documento de mandado de prisão que utiliza variáveis do cadastro de variáveis
	 */
	public String getDescricaoDataDelito(){
		if (getProcessoExpedienteCriminalEdit() != null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return getProcessoExpedienteCriminalEdit().getDataDelito() != null ? sdf.format(getProcessoExpedienteCriminalEdit().getDataDelito())
					: "Desconhecida";
		}

		return "Desconhecida";
	}

	public String getDescricaoDataNascimento(){
		if (getProcessoExpedienteCriminalEdit() != null){
			return ((PessoaFisica) getProcessoExpedienteCriminalEdit().getPessoa()).getDataNascimento() != null ? ((PessoaFisica) getProcessoExpedienteCriminalEdit()
					.getPessoa()).getDataNascimentoFormatada()
					: "Desconhecida";

		}

		return "Desconhecida";
	}

	public String getDescricaoRecaptura(){
		if (getProcessoExpedienteCriminalEdit() != null){
			return getProcessoExpedienteCriminalEdit().getRecaptura() ? "(X)" : "( )";

		}

		return "( )";
	}

	public String getDescricaoDataValidade(){
		if (getProcessoExpedienteCriminalEdit() != null){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return getProcessoExpedienteCriminalEdit().getDataValidade() != null ? sdf.format(getProcessoExpedienteCriminalEdit().getDataValidade())
					: "Desconhecida";
		}

		return "Desconhecida";
	}
	
	public String getDescricaoCaracteristicasFisicas(){
		StringBuilder texto = new StringBuilder();
		String retorno = "";
		if (getProcessoExpedienteCriminalEdit() != null){
			for(CaracteristicaFisica aux : ((PessoaFisica)getProcessoExpedienteCriminalEdit().getPessoa()).getCaracteristicasFisicas()){
				texto.append(aux.getCaracteristicaFisica().getLabel());
				texto.append("\n");
			}
			
			if(texto != null && !texto.toString().trim().isEmpty()){
				retorno = texto.substring(0, texto.lastIndexOf("\n"));
			}
		}
		
		return retorno;
	}
	
	@Override
	public void proximoPasso() {
		if(getPasso() == 1){
			if(getProcessoExpedienteCriminalEdit().getDataValidade() != null && DateUtil.isDataMenor(getProcessoExpedienteCriminalEdit().getDataValidade(), new Date())){
				getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.error.prepararMandadoPrisaoAction.dataValidadePrisaoMenorDataAtual");				
			}else{
				super.proximoPasso();
			}
		}else{
			super.proximoPasso();
		}
	}

}
