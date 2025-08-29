package br.com.infox.cliente.util;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.trf.webservice.ConsultaClienteOAB;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.ws.externo.cna.entidades.DadosAdvogadoOAB;

public class PushUtil {
	public static String formatarCpfCnpj(String cpfCnpj) {
		String result = "";
		if(cpfCnpj != null && !cpfCnpj.equals("")){
			result = cpfCnpj;
			result = result.replace(".", "");
			result = result.replace("-", "");
			result = result.replace("/", "");
			if(result.length() != 14){
				while (result.startsWith("0")) {
					result = result.substring(1);
				}
			}
		}
		return result;
	}
	
	public static String mascararCpfCnpj(String cpfCnpj, boolean valueIsCpf){
		String result = cpfCnpj;
		if(valueIsCpf){
			while(result.length()<11){
				result = "0"+result;
			}
			result = result.substring(0, 3)+"."+result.substring(3, 6)+"."+result.substring(6,9)+"-"+result.substring(9);
		}else{
			while(result.length()<14){
				result = "0"+result;
			}
			result = result.substring(0, 2) + "." + result.substring(2, 5) + "." + result.substring(5, 8)
					+ "/" + result.substring(8, 12) + "-" + result.substring(12);
		}
		return result;
	}
	
	public static boolean consultaDadosOABWebService(String cpf) {
		String cpfConsulta = StringUtil.removeNaoNumericos(cpf);
		try {
			ConsultaClienteOAB consultaClienteOAB = new ConsultaClienteOAB();
			boolean novaConsulta = consultaClienteOAB.consultaDadosBase(cpfConsulta) == null || consultaClienteOAB.consultaDadosBase(cpfConsulta).size()==0;
			consultaClienteOAB.consultaDados(cpfConsulta, novaConsulta);
			List<DadosAdvogadoOAB> dadosAdvogadoList = consultaClienteOAB.getDadosAdvogadoList();
			if (dadosAdvogadoList == null || dadosAdvogadoList.size() == 0) {
				FacesMessages.instance().add(Severity.ERROR, "A OAB não retornou dados para este advogado.");
				return false;
			} else {
				FacesMessages.instance().add(Severity.INFO, "Dados atualizados com sucesso.");
				return true;
			}
		} catch (Exception e) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao consultar a OAB: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public static String gerarHash(String arg0) {
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String dataHora = format.format(new Date());
		String text = String.valueOf(arg0).concat(dataHora);

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
			byte[] sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception ex) {
			ex.printStackTrace();
			return StringUtils.EMPTY;
		}
	}

	private static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int twoHalfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}
	
	public static boolean validarSenha(String senha, String nome){
		if (StringUtils.isNotBlank(senha) && StringUtils.isNotBlank(nome)) {
			return senha.length() >= 6 && senha.length() <= 15 && 
				!senhaContemNome(senha, nome) && !senhaContemRepeticao(senha) && senhaContemOsCriterios(senha);
		}
		return false;
	}
	
	private static boolean senhaContemNome(String senha, String nome){
		senha = senha.toUpperCase();
		nome = nome.toUpperCase();
		if(nome.contains(senha) || senha.contains(nome)){
			return true;
		}
		while(nome.contains(" ")){
			String parteDoNome = nome.substring(0,nome.indexOf(' ')).trim();
			nome = nome.substring(nome.indexOf(' ')).trim();
			if(parteDoNome.length() > 3 && senha.contains(parteDoNome)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean senhaContemRepeticao(String senha){
		return senha.equals("123456789".substring(0,senha.length() > 9 ? 9 : senha.length())) ||
			   senha.toUpperCase().equals("ABCDEFGHIJLMNOPQRSTUVXZ".substring(0, senha.length() > 23 ? 23 : senha.length())) ||
			   senha.equals("112233445566778899".substring(0, senha.length() > 18 ? 18 : senha.length())) ||
			   senha.toUpperCase().equals("AABBCCDDEEFFGGHHIIJJLLMMNNOOPPQRRSSTTUUVVXXZZ".substring(0, senha.length() > 46 ? 46 : senha.length()));
	}
	
	private static boolean senhaContemOsCriterios(String senha){
		int count=0;
		String letrasMaiusculas = "QWERTYUIOPASDFGHJKLZXCVBNM";
		String letrasMinusculas = letrasMaiusculas.toLowerCase();
		String numeros = "1234567890";
		for (int i = 0; i < senha.length(); i++) {
			if(letrasMaiusculas.contains(senha.charAt(i)+"")){
				count ++;
				break;
			}
		}
		for (int i = 0; i < senha.length(); i++) {
			if(letrasMinusculas.contains(senha.charAt(i)+"")){
				count ++;
				break;
			}
		}
		for (int i = 0; i < senha.length(); i++) {
			if(numeros.contains(senha.charAt(i)+"")){
				count ++;
				break;
			}
		}
		for (int i = 0; i < senha.length(); i++) {
			if(!numeros.contains(senha.charAt(i)+"") &&
			   !letrasMinusculas.contains(senha.charAt(i)+"") &&
			   !letrasMaiusculas.contains(senha.charAt(i)+"")){
				count ++;
				break;
			}
		}
		return count >= 3;
	}
}
