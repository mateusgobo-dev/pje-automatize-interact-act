package br.com.jus.peticao.inicial.impl;

import java.util.function.Supplier;

public interface PeticaoSupplier {
    Supplier<String> url = () -> "https://sso.stg.cloud.pje.jus.br/auth/realms/pje/protocol/openid-connect/auth?client_id=portalexterno-frontend&redirect_uri=https%3A%2F%2Fportalexterno-tribunais.stg.pdpj.jus.br%2Fhome&state=b3350715-45d0-4879-87b6-2d59e5d0b92e&response_mode=fragment&response_type=code&scope=openid&nonce=3c2094b7-30c2-451b-96d5-a0fab505b67d";
}
