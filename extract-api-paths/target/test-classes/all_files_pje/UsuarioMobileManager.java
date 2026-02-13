package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;

import br.com.infox.core.manager.GenericManager;
import br.jus.cnj.pje.business.dao.UsuarioMobileDAO;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioMobile;
import br.jus.pje.nucleo.enums.PlataformaDispositivoEnum;

@AutoCreate
@Scope(ScopeType.CONVERSATION)
@Name(UsuarioMobileManager.NAME)

public class UsuarioMobileManager extends GenericManager implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String NAME = "usuarioMobileManager";
	
	@In
	private UsuarioMobileDAO usuarioMobileDAO;
	
	private TimeBasedOneTimePasswordGenerator otpGenerator;
	private static final Integer tamanhoSenha = 6;
	private Base32 base32 = new Base32();
	
	private TimeBasedOneTimePasswordGenerator getOTPGenerator () throws NoSuchAlgorithmException {
		if ( otpGenerator==null ) {
			otpGenerator = new TimeBasedOneTimePasswordGenerator(30, TimeUnit.SECONDS, tamanhoSenha, "HmacSHA1");
		}
		return otpGenerator;
	}


	public List<UsuarioMobile> listaUsuarioMobile(Usuario usuario) {
		return usuarioMobileDAO.listaUsuarioMobile(usuario);
	}

	public UsuarioMobile recuperarUsuarioMobile(String cpf, String email, String codPareamento){
		return usuarioMobileDAO.recuperarUsuarioMobile(cpf, email, codPareamento);
	}

	public boolean checkCodigoPareamento(String codigoPareamento) {
		return usuarioMobileDAO.checkCodigoPareamento(codigoPareamento);
	}

	public String gerarTokenContador(String codigoPareamento) throws Exception {
		SecretKey key = new SecretKeySpec(base32.decode(codigoPareamento), "SHA1");
		return StringUtils.leftPad(getOTPGenerator().generateOneTimePassword(key, 1)+"", tamanhoSenha, '0');
	}
	
	public String gerarTokenTempo(String codigoPareamento, Date data) throws Exception {
		SecretKey key = new SecretKeySpec(base32.decode(codigoPareamento), "SHA1");
		return StringUtils.leftPad(getOTPGenerator().generateOneTimePassword(key, data)+"", tamanhoSenha, '0');
	}


	public void inativarUsuarioMobile(UsuarioMobile usuarioMobile) {
		usuarioMobileDAO.inativarUsuarioMobile(usuarioMobile);
	}


	public boolean usuarioMobilePareado(UsuarioMobile usuarioMobile) {
		return usuarioMobile!=null && usuarioMobile.getIdUsuarioMobile()!=null && usuarioMobileDAO.usuarioMobilePareado(usuarioMobile);
	}


	public void parearDispositivo(PlataformaDispositivoEnum plataforma, UsuarioMobile usuarioMobile, String versaoPlataforma, String nomeDispositivo) {
		usuarioMobileDAO.parearDispositivo(plataforma, usuarioMobile, versaoPlataforma, nomeDispositivo);
	}

	public static void main (String args[]) throws InvalidKeyException, NoSuchAlgorithmException {
		SecretKey key = new SecretKeySpec(new Base32().decode("J57X3O6A2FZ7IKQ3WYJUTEEHGU4YC3YI"), "SHA1");
		System.out.println( StringUtils.leftPad(new TimeBasedOneTimePasswordGenerator(30, TimeUnit.SECONDS, tamanhoSenha, "HmacSHA1").generateOneTimePassword(key, 1)+"", tamanhoSenha, '0') );
	}


	public UsuarioMobile getUsuarioMobileParaPareamento(String codigoPareamento, String cpf, String email) {
		return usuarioMobileDAO.getUsuarioMobileParaPareamento(codigoPareamento, cpf, email);
	}


	public UsuarioMobile getUsuarioMobilePareado(String codigoPareamento) {
		return usuarioMobileDAO.getUsuarioMobilePareado(codigoPareamento);
	}



}
