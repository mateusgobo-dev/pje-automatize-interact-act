package br.com.jt.pje.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.bean.NumeracaoDocumentoBean;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.editor.NumeracaoDocumento;

@Name(NumeracaoDocumentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class NumeracaoDocumentoAction implements Serializable {

	private static final long serialVersionUID = 8727930771871709504L;

	public static final String NAME = "numeracaoDocumentoAction";

	private List<NumeracaoDocumentoBean> numeracaoDocumentoList;
	
	private String visualizacao;

	public void criarNivel() {
		getNumeracaoDocumentoList().add(new NumeracaoDocumentoBean(getNumeracaoDocumentoList().size() + 1, null, null));
	}

	public void remover(NumeracaoDocumentoBean numeracaoDocumentoBean) {
		getNumeracaoDocumentoList().remove(numeracaoDocumentoBean);
	}

	public void gravar() {
		String delete = "delete from jt.tb_numeracao_documento";
		EntityUtil.createNativeQuery(delete, "tb_numeracao_documento").executeUpdate();
		EntityUtil.getEntityManager().flush();

		for (NumeracaoDocumentoBean bean : getNumeracaoDocumentoList()) {
			int contadorLetras = 1;
			int aux = 1;
			for (int i = 1; i <= 78; i++) {
				NumeracaoDocumento nd = new NumeracaoDocumento();
				nd.setNivel(bean.getNivel());
				nd.setOrdem(i);
				nd.setSeparador(bean.getSeparador());
				nd.setTipoNumeracao(bean.getTipoNumeracao());

				switch (bean.getTipoNumeracao()) {
				case TIPO1:
					nd.setTipo(String.valueOf(i));
					break;
				case TIPO2:
					nd.setTipo(numeralParaRomanoMaiusculo(i));
					break;
				case TIPO3:
					nd.setTipo(numeralParaRomanoMinusculo(i));
					break;
				case TIPO4:
					char letraMinuscula = 0;
					if(('a'-1 + aux) <= 'z'){
						letraMinuscula = (char) ('a'-1 + aux);
					}
					StringBuilder tipoMinusculo = new StringBuilder(String.valueOf(letraMinuscula));
					for(int j = 1; j<contadorLetras; j++){
						tipoMinusculo.append(String.valueOf(letraMinuscula));
					}
					nd.setTipo(tipoMinusculo.toString());
					if(letraMinuscula == 'z'){
						contadorLetras++;
						aux = 1;
					}else{
						aux++;
					}
					break;
				case TIPO5:
					char letraMaiuscula = 0;
					if(('A'-1 + aux) <= 'Z'){
						letraMaiuscula = (char) ('A'-1 + aux);
					}
					StringBuilder tipoMaiusculo = new StringBuilder(String.valueOf(letraMaiuscula));
					for(int j = 1; j<contadorLetras; j++){
						tipoMaiusculo.append(String.valueOf(letraMaiuscula));
					}
					nd.setTipo(tipoMaiusculo.toString());
					if(letraMaiuscula == 'Z'){
						contadorLetras++;
						aux = 1;
					}else{
						aux++;
					}
					break;
				case TIPO6:
					nd.setTipo(gerarValorPorExtenso(i));
					break;
				case TIPO7:
					nd.setTipo(gerarOrdinalPorExtenso(i));
					break;
				}
				EntityUtil.getEntityManager().persist(nd);
			}
		}
		EntityUtil.getEntityManager().flush();
		FacesMessages.instance().add(Severity.INFO, "Numeração salva com sucesso.");
	}

	@SuppressWarnings("unchecked")
	public List<NumeracaoDocumentoBean> getNumeracaoDocumentoList() {
		if (numeracaoDocumentoList == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select new br.com.infox.editor.bean.NumeracaoDocumentoBean(o.nivel,o.tipoNumeracao,o.separador) from NumeracaoDocumento o ");
			sb.append("group by o.nivel,o.tipoNumeracao,o.separador ");
			sb.append("order by o.nivel ");
			numeracaoDocumentoList = EntityUtil.createQuery(sb.toString()).getResultList();
		}
		return numeracaoDocumentoList;
	}

	public void setNumeracaoDocumentoList(List<NumeracaoDocumentoBean> numeracaoDocumentoList) {
		this.numeracaoDocumentoList = numeracaoDocumentoList;
	}

	private String gerarValorPorExtenso(long l){
		String s = "";
		String as[] = {
				"", " um", " dois", " tr\352s", " quatro", " cinco", " seis", " sete", " oito", " nove",
				" dez", " onze", " doze", " treze", " catorze", " quinze", " dezesseis", " dezessete", " dezoito", " dezenove"
		};
		String as1[] = {
				"", "", " vinte", " trinta", " quarenta", " cinq\374enta", " sessenta", " setenta", " oitenta", " noventa"
		};
		String as2[] = {
				"", " cento", " duzentos", " trezentos", " quatrocentos", " quinhentos", " seiscentos", " setecentos", " oitocentos", " novecentos"
		};
		String as3[] = {
				"", " mil", " milh\365es", " bilh\365es", " trilh\365es", " quatrilh\365es", " quintilh\365es", " sextilh\365es"
		};
		String as4[] = {
				"", " mil", " milh\343o", " bilh\343o", " trilh\343o", " quatrilh\343o", " quintilh\343o", " sextilh\343o"
		};
		if(l == 0L)
			s = "";
		for(int j1 = 0; l > 0L; j1++)
		{
			int i1 = (int)(l % 1000L);
			l /= 1000L;
			if(i1 != 0)
			{
				String s1;
				if(i1 < 1000)
				{
					int i;
					int j;
					if((i = i1 % 100) < 20)
					{
						j = 0;
					} else
					{
						i = i1 % 10;
						j = (i1 % 100) / 10;
					}
					int k = i1 / 100;
					if(k == 1 && j == 0 && i == 0)
					{
						s1 = " cem";
					} else
					{
						s1 = as2[k];
						if(j != 0)
							if(j != 1)
								s1 = s1 + " e" + as1[j];
							else
								s1 = s1 + " e" + as[i];
						if(i != 0)
							s1 = s1 + " e" + as[i];
					}
				} else
				{
					s1 = gerarValorPorExtenso(i1);
				}
				if(i1 == 1)
					s = s1 + as4[j1] + "," + s;
				else
					s = s1 + as3[j1] + "," + s;
			}
		}

		s = s.trim();
		if(!s.equals("") || s == null)
		{
			if(s.charAt(0) == 'e')
				s = s.substring(2).trim();
			s = s.substring(0, s.length() - 1).trim();
			s = Character.toUpperCase(s.charAt(0)) + s.substring(1).trim();
			for(int k1 = s.length() - 1; k1 > -1; k1--)
			{
				if(s.charAt(k1) != ',')
					continue;
				if(s.charAt(k1 + 2) == 'e')
					s = s.substring(0, k1) + s.substring(k1 + 1, s.length());
				else
					s = s.substring(0, k1) + " e" + s.substring(k1 + 1, s.length());
				break;
			}

			for(int l1 = 0; l1 < s.length() - 1; l1++)
				if(s.charAt(l1) == ',' && s.charAt(l1 + 2) == 'e')
					s = s.substring(0, l1 + 1) + s.substring(l1 + 3, s.length());

			return s;
		} else
		{
			return null;
		}
	}

	private String gerarOrdinalPorExtenso(long l){
		String s = "";
		String as[] = {
				"", " primeiro", " segundo", " terceiro", " quarto", " quinto", " sexto", " sétimo", " oitavo", " nono",
				" décimo", " décimo primeiro", " décimo segundo", " décimo terceiro", " décimo quarto", " décimo quinto", " décimo sexto", " décimo sétimo", " décimo oitavo", " décimo nono"
		};
		String as1[] = {
				"", "", " vigésimo", " trigésimo", " quadragésimo", " quinquagésimo", " sexagésimo", " septuagésimo", " octogésimo", " nonagésimo"
		};
		String as2[] = {
				"", " cento", " duzentos", " trezentos", " quatrocentos", " quinhentos", " seiscentos", " setecentos", " oitocentos", " novecentos"
		};
		String as3[] = {
				"", " mil", " milh\365es", " bilh\365es", " trilh\365es", " quatrilh\365es", " quintilh\365es", " sextilh\365es"
		};
		String as4[] = {
				"", " mil", " milh\343o", " bilh\343o", " trilh\343o", " quatrilh\343o", " quintilh\343o", " sextilh\343o"
		};
		if(l == 0L)
			s = "";
		for(int j1 = 0; l > 0L; j1++)
		{
			int i1 = (int)(l % 1000L);
			l /= 1000L;
			if(i1 != 0)
			{
				String s1;
				if(i1 < 1000)
				{
					int i;
					int j;
					if((i = i1 % 100) < 20)
					{
						j = 0;
					} else
					{
						i = i1 % 10;
						j = (i1 % 100) / 10;
					}
					int k = i1 / 100;
					if(k == 1 && j == 0 && i == 0)
					{
						s1 = " centésimo";
					} else
					{
						s1 = as2[k];
						if(j != 0)
							if(j != 1)
								s1 = s1  + as1[j];
							else
								s1 = s1  + as[i];
						if(i != 0)
							s1 = s1  + as[i];
					}
				} else
				{
					s1 = gerarValorPorExtenso(i1);
				}
				if(i1 == 1)
					s = s1 + as4[j1] + "," + s;
				else
					s = s1 + as3[j1] + "," + s;
			}
		}

		s = s.trim();
		if(!s.equals("") || s == null)
		{
			if(s.charAt(0) == ' ')
				s = s.substring(2).trim();
			s = s.substring(0, s.length() - 1).trim();
			s = Character.toUpperCase(s.charAt(0)) + s.substring(1).trim();
			for(int k1 = s.length() - 1; k1 > -1; k1--)
			{
				if(s.charAt(k1) != ',')
					continue;
				if(s.charAt(k1 + 2) == ' ')
					s = s.substring(0, k1) + s.substring(k1 + 1, s.length());
				else
					s = s.substring(0, k1) + " " + s.substring(k1 + 1, s.length());
				break;
			}

			for(int l1 = 0; l1 < s.length() - 1; l1++)
				if(s.charAt(l1) == ',' && s.charAt(l1 + 2) == ' ')
					s = s.substring(0, l1 + 1) + s.substring(l1 + 3, s.length());

			return s;
		} else
		{
			return null;
		}
	}

	private String numeralParaRomanoMaiusculo(int num) {
		int[]    numbers = { 1000,  900,  500,  400,  100,   90,  
			50,   40,   10,    9,    5,    4,    1 };

		String[] letters = { "M",  "CM",  "D",  "CD", "C",  "XC",
			"L",  "XL",  "X",  "IX", "V",  "IV", "I" };
		StringBuilder roman = new StringBuilder(); 
		int N = num;
		for (int i = 0; i < numbers.length; i++) {
			while (N >= numbers[i]) {
				roman.append(letters[i]);
				N -= numbers[i];
			}
		}
		return roman.toString();
	}
	
	private String numeralParaRomanoMinusculo(int num) {
		int[]    numbers = { 1000,  900,  500,  400,  100,   90,  
			50,   40,   10,    9,    5,    4,    1 };

		String[] letters = { "m",  "cm",  "d",  "cd", "c",  "xc",
			"l",  "xl",  "x",  "ix", "v",  "iv", "i" };
		StringBuilder roman = new StringBuilder(); 
		int N = num;
		for (int i = 0; i < numbers.length; i++) {
			while (N >= numbers[i]) {
				roman.append(letters[i]);
				N -= numbers[i];
			}
		}
		return roman.toString();
	}

	public String getVisualizacao() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<getNumeracaoDocumentoList().size();i++) {
			NumeracaoDocumentoBean bean = getNumeracaoDocumentoList().get(i);
			for(int j=0; j<i; j++){
				sb.append("&nbsp;&nbsp;&nbsp;&nbsp;");
			}
			switch (bean.getTipoNumeracao()) {
			case TIPO1:
				sb.append("1").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO2:
				sb.append("I").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO3:
				sb.append("i").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO4:
				sb.append("a").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO5:
				sb.append("A").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO6:
				sb.append("Um").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			case TIPO7:
				sb.append("Primeiro").append(bean.getSeparador()).append(" ").append("Nível ").append(String.valueOf(bean.getNivel())).append("<br/>");
				break;
			}
		}
		visualizacao = sb.toString();
		return visualizacao;
	}

	public void setVisualizacao(String visualizacao) {
		this.visualizacao = visualizacao;
	}
}
