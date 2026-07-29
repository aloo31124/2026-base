package com.agentflow.base.service;

public interface LineOAuthClient {
    String buildAuthorizationUrl(String state, String nonce, String codeChallenge);
    Profile exchangeAndVerify(String code, String codeVerifier, String nonce);

    record Profile(String lineUserId, String displayName, String pictureUrl, String email) {}
}
