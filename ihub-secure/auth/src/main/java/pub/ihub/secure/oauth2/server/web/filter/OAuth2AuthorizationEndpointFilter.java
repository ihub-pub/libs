/*
 * Copyright (c) 2021 Henry 李恒 (henry.box@outlook.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pub.ihub.secure.oauth2.server.web.filter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.MultiValueMap;
import pub.ihub.core.ObjectBuilder;
import pub.ihub.secure.oauth2.server.OAuth2Authorization;
import pub.ihub.secure.oauth2.server.OAuth2AuthorizationService;
import pub.ihub.secure.oauth2.server.RegisteredClientRepository;
import pub.ihub.secure.oauth2.server.TokenType;
import pub.ihub.secure.oauth2.server.client.RegisteredClient;
import pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode;
import pub.ihub.secure.oauth2.server.token.OAuth2Tokens;
import pub.ihub.secure.oauth2.server.web.OAuth2Filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static cn.hutool.core.lang.Assert.isTrue;
import static cn.hutool.core.lang.Assert.notBlank;
import static cn.hutool.core.lang.Assert.notNull;
import static cn.hutool.core.text.CharSequenceUtil.blankToDefault;
import static cn.hutool.core.text.CharSequenceUtil.isBlank;
import static cn.hutool.core.text.CharSequenceUtil.isNotBlank;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Base64.getUrlEncoder;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.ACCESS_DENIED;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_SCOPE;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNAUTHORIZED_CLIENT;
import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.UNSUPPORTED_RESPONSE_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CODE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_DESCRIPTION;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ERROR_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.RESPONSE_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.STATE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_CHALLENGE;
import static org.springframework.security.oauth2.core.endpoint.PkceParameterNames.CODE_CHALLENGE_METHOD;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.OPENID;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZATION_REQUEST;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZED_SCOPES;
import static pub.ihub.secure.oauth2.server.token.OAuth2AuthorizationCode.generateAuthCode;

/**
 * OAuth2.0授权令牌认证过滤器
 * TODO 整理页面
 *
 * @author henry
 */
public class OAuth2AuthorizationEndpointFilter extends OAuth2Filter {

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final RequestMatcher authorizationRequestMatcher;
	private final RequestMatcher userConsentMatcher;
	private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(getUrlEncoder().withoutPadding(), 96);
	private final StringKeyGenerator stateGenerator = new Base64StringKeyGenerator(getUrlEncoder());
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public OAuth2AuthorizationEndpointFilter(RegisteredClientRepository registeredClientRepository,
											 OAuth2AuthorizationService authorizationService,
											 String authorizationEndpointUri) {
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;

		RequestMatcher authorizationRequestGetMatcher = requestMatcher(authorizationEndpointUri, GET);
		RequestMatcher authorizationRequestPostMatcher = requestMatcher(authorizationEndpointUri, POST);
		RequestMatcher openidScopeMatcher = request -> {
			String scope = request.getParameter(SCOPE);
			return isNotBlank(scope) && scope.contains(OPENID);
		};
		RequestMatcher consentActionMatcher = request ->
			request.getParameter(UserConsentPage.CONSENT_ACTION_PARAMETER_NAME) != null;
		authorizationRequestMatcher = new OrRequestMatcher(authorizationRequestGetMatcher,
			new AndRequestMatcher(authorizationRequestPostMatcher, openidScopeMatcher,
				new NegatedRequestMatcher(consentActionMatcher)));
		userConsentMatcher = new AndRequestMatcher(authorizationRequestPostMatcher, consentActionMatcher);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			if (authorizationRequestMatcher.matches(request)) {
				processAuthorizationRequest(request, response, filterChain);
				return;
			} else if (userConsentMatcher.matches(request)) {
				processUserConsent(request, response);
				return;
			}
		} catch (OAuth2AuthenticationException e) {
			OAuth2Error error = e.getError();
			if (isNotBlank(error.getUri())) {
				sendErrorResponse(request, response, error.getUri(), error, request.getParameter(STATE));
			} else {
				sendErrorResponse(response, error);
			}
			return;
		}
		filterChain.doFilter(request, response);
	}

	private void processAuthorizationRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		OAuth2AuthorizationRequestContext authorizationRequestContext = new OAuth2AuthorizationRequestContext(
			request.getRequestURL().toString(), getParameters(request));

		authorizationRequestContext.setRegisteredClient(registeredClientRepository.findByClientId(
			authorizationRequestContext.getClientId()));

		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		if (!isPrincipalAuthenticated(principal)) {
			filterChain.doFilter(request, response);
			return;
		}

		RegisteredClient registeredClient = authorizationRequestContext.getRegisteredClient();
		OAuth2AuthorizationRequest authorizationRequest = authorizationRequestContext.buildAuthorizationRequest();
		ObjectBuilder<OAuth2Authorization> builder = ObjectBuilder.builder(OAuth2Authorization::new)
			.set(OAuth2Authorization::setRegisteredClientId, registeredClient.getId())
			.set(OAuth2Authorization::setPrincipalName, principal.getName())
			.set(OAuth2Authorization::setAttributes, new HashMap<>(2) {
				{
					put(AUTHORIZATION_REQUEST, authorizationRequest);
				}
			});

		if (registeredClient.isRequireUserConsent()) {
			String state = this.stateGenerator.generateKey();
			OAuth2Authorization authorization = builder
				.put(OAuth2Authorization::getAttributes, OAuth2Authorization.STATE, state)
				.build();
			this.authorizationService.save(authorization);

			// TODO Need to remove 'in-flight' authorization if consent step is not completed (e.g. approved or cancelled)

			// TODO 替换为templates页面
			UserConsentPage.displayConsent(request, response, registeredClient, authorization);
		} else {
			OAuth2AuthorizationCode authorizationCode = generateAuthCode(codeGenerator.generateKey(),
				registeredClient.getAccessTokenTimeToLive());
			this.authorizationService.save(builder
				.set(OAuth2Authorization::setTokens,
					ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::token, authorizationCode).build())
				.put(OAuth2Authorization::getAttributes, AUTHORIZED_SCOPES, authorizationRequest.getScopes())
				.build());

//			TODO security checks for code parameter
//			The authorization code MUST expire shortly after it is issued to mitigate the risk of leaks.
//			A maximum authorization code lifetime of 10 minutes is RECOMMENDED.
//			The client MUST NOT use the authorization code more than once.
//			If an authorization code is used more than once, the authorization server MUST deny the request
//			and SHOULD revoke (when possible) all tokens previously issued based on that authorization code.
//			The authorization code is bound to the client identifier and redirection URI.

			sendAuthorizationResponse(request, response,
				authorizationRequestContext.resolveRedirectUri(), authorizationCode, authorizationRequest.getState());
		}
	}

	private void processUserConsent(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		UserConsentRequestContext userConsentRequestContext = new UserConsentRequestContext(
			request.getRequestURL().toString(), getParameters(request));

		userConsentRequestContext.setAuthorization(this.authorizationService.findByToken(
			userConsentRequestContext.getState(), new TokenType(OAuth2Authorization.STATE)));
		userConsentRequestContext.setRegisteredClient(this.registeredClientRepository.findByClientId(
			userConsentRequestContext.getClientId()));

		if (!UserConsentPage.isConsentApproved(request)) {
			this.authorizationService.remove(userConsentRequestContext.getAuthorization());
			throw exceptionSupplier(ACCESS_DENIED, CLIENT_ID,
				userConsentRequestContext.getAuthorizationRequest().getState()).get();
		}

		OAuth2AuthorizationCode authorizationCode = generateAuthCode(codeGenerator.generateKey(),
			userConsentRequestContext.getRegisteredClient().getAccessTokenTimeToLive());
		OAuth2Authorization authorization = OAuth2Authorization.from(userConsentRequestContext.getAuthorization())
			.set(OAuth2Authorization::setTokens,
				ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::token, authorizationCode).build())
			.setSub(OAuth2Authorization::getAttributes, attributes -> {
				attributes.remove(OAuth2Authorization.STATE);
				attributes.put(AUTHORIZED_SCOPES, userConsentRequestContext.getScopes());
			})
			.build();
		this.authorizationService.save(authorization);

		sendAuthorizationResponse(request, response, userConsentRequestContext.resolveRedirectUri(),
			authorizationCode, userConsentRequestContext.getAuthorizationRequest().getState());
	}

	private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response, String redirectUri,
										   OAuth2AuthorizationCode authorizationCode, String state) throws IOException {
		redirectStrategy.sendRedirect(request, response, fromUriString(redirectUri)
			.queryParam(CODE, authorizationCode.getTokenValue())
			.queryParamIfPresent(STATE, Optional.of(blankToDefault(state, null))).toUriString());
	}

	private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
								   String redirectUri, OAuth2Error error, String state) throws IOException {
		redirectStrategy.sendRedirect(request, response, fromUriString(redirectUri)
			.queryParam(ERROR, error.getErrorCode())
			.queryParamIfPresent(ERROR_DESCRIPTION, Optional.of(blankToDefault(error.getDescription(), null)))
			.queryParamIfPresent(ERROR_URI, Optional.of(blankToDefault(error.getUri(), null)))
			.queryParamIfPresent(STATE, Optional.of(blankToDefault(state, null))).toUriString());
	}

	private void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		// TODO Send default html error response
		response.sendError(BAD_REQUEST.value(), error.toString());
	}

	private static boolean isPrincipalAuthenticated(Authentication principal) {
		return principal != null &&
			!AnonymousAuthenticationToken.class.isAssignableFrom(principal.getClass()) &&
			principal.isAuthenticated();
	}

	@Getter(PROTECTED)
	private static class OAuth2AuthorizationRequestContext extends AbstractRequestContext {

		private final String responseType;
		private final String redirectUri;

		private OAuth2AuthorizationRequestContext(String authorizationUri, MultiValueMap<String, String> parameters) {
			super(authorizationUri, parameters);
			responseType = getParameterValue(parameters, RESPONSE_TYPE);
			isTrue(responseType.equals(CODE), exceptionSupplier(UNSUPPORTED_RESPONSE_TYPE, RESPONSE_TYPE));
			redirectUri = getParameterValue(parameters, REDIRECT_URI, true);
		}

		@Override
		public void setRegisteredClient(RegisteredClient registeredClient) {
			notNull(registeredClient, exceptionSupplier(CLIENT_ID));
			isTrue(registeredClient.getAuthorizationGrantTypes().contains(AUTHORIZATION_CODE),
				exceptionSupplier(UNAUTHORIZED_CLIENT, CLIENT_ID));
			super.setRegisteredClient(registeredClient);
			// TODO 考虑是否保留多回调地址
			isTrue(isNotBlank(redirectUri) ? registeredClient.getRedirectUris().contains(redirectUri) :
					!getScopes().contains(OPENID) || registeredClient.getRedirectUris().size() == 1,
				exceptionSupplier(REDIRECT_URI));
			isTrue(getScopes().isEmpty() || registeredClient.getScopes().containsAll(getScopes()),
				exceptionSupplier(INVALID_SCOPE, SCOPE, resolveRedirectUri()));
			// (REQUIRED for public clients) - RFC 7636 (PKCE)
			String codeChallenge = getParameterValue(getParameters(), CODE_CHALLENGE, true);
			if (isNotBlank(codeChallenge)) {
				String codeChallengeMethod = getParameterValue(getParameters(), CODE_CHALLENGE_METHOD, true);
				isTrue(isBlank(codeChallengeMethod) ||
						"S256".equals(codeChallengeMethod) || "plain".equals(codeChallengeMethod),
					exceptionSupplier(INVALID_REQUEST, CODE_CHALLENGE_METHOD, resolveRedirectUri()));
			} else {
				isTrue(!registeredClient.isRequireProofKey(),
					exceptionSupplier(INVALID_REQUEST, CODE_CHALLENGE, resolveRedirectUri()));
			}
		}

		private OAuth2AuthorizationRequest buildAuthorizationRequest() {
			return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri(getAuthorizationUri())
				.clientId(getClientId())
				.redirectUri(getRedirectUri())
				.scopes(getScopes())
				.state(getState())
				.additionalParameters(additionalParameters -> additionalParameters
					.putAll(filterParameters(getParameters(), RESPONSE_TYPE, CLIENT_ID, REDIRECT_URI, SCOPE, STATE)))
				.build();
		}
	}

	@Getter(PRIVATE)
	@Setter(PRIVATE)
	private static class UserConsentRequestContext extends AbstractRequestContext {

		private OAuth2Authorization authorization;

		private UserConsentRequestContext(String authorizationUri, MultiValueMap<String, String> parameters) {
			super(authorizationUri, parameters);
			notBlank(getState(), exceptionSupplier(STATE));
		}

		@Override
		protected String getRedirectUri() {
			return getAuthorizationRequest().getRedirectUri();
		}

		@Override
		public void setRegisteredClient(RegisteredClient registeredClient) {
			notNull(registeredClient, exceptionSupplier(CLIENT_ID));
			isTrue(registeredClient.getId().equals(authorization.getRegisteredClientId()), exceptionSupplier(CLIENT_ID));
			super.setRegisteredClient(registeredClient);
			isTrue(getScopes().isEmpty() || getAuthorizationRequest().getScopes().containsAll(getScopes()),
				exceptionSupplier(INVALID_SCOPE, SCOPE, resolveRedirectUri()));
		}

		private OAuth2AuthorizationRequest getAuthorizationRequest() {
			return getAuthorization().getAttribute(AUTHORIZATION_REQUEST);
		}

		public void setAuthorization(OAuth2Authorization authorization) {
			this.authorization = notNull(authorization, exceptionSupplier(STATE));
			Authentication principal = SecurityContextHolder.getContext().getAuthentication();
			isTrue(isPrincipalAuthenticated(principal) &&
				principal.getName().equals(authorization.getPrincipalName()), exceptionSupplier(STATE));
		}

	}

	@Getter(PROTECTED)
	@Setter(PROTECTED)
	private abstract static class AbstractRequestContext {

		private final String authorizationUri;
		private final MultiValueMap<String, String> parameters;
		private final String clientId;
		private final String state;
		private final Set<String> scopes;
		private RegisteredClient registeredClient;
		private OAuth2Error error;
		private boolean redirectOnError;

		protected AbstractRequestContext(String authorizationUri, MultiValueMap<String, String> parameters) {
			this.authorizationUri = authorizationUri;
			this.parameters = parameters;
			this.clientId = getParameterValue(parameters, CLIENT_ID);
			this.state = getParameterValue(parameters, STATE, true);
			this.scopes = extractScopes(parameters);
		}

		protected String resolveRedirectUri() {
			return isNotBlank(getRedirectUri()) ? getRedirectUri() :
				getRegisteredClient().getRedirectUris().iterator().next();
		}

		protected abstract String getRedirectUri();

	}

	private static class UserConsentPage {

		private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", UTF_8);
		private static final String CONSENT_ACTION_PARAMETER_NAME = "consent_action";
		private static final String CONSENT_ACTION_APPROVE = "approve";
		private static final String CONSENT_ACTION_CANCEL = "cancel";

		private static void displayConsent(HttpServletRequest request, HttpServletResponse response,
										   RegisteredClient registeredClient, OAuth2Authorization authorization) throws IOException {

			String consentPage = generateConsentPage(request, registeredClient, authorization);
			response.setContentType(TEXT_HTML_UTF8.toString());
			response.setContentLength(consentPage.getBytes(UTF_8).length);
			response.getWriter().write(consentPage);
		}

		private static boolean isConsentApproved(HttpServletRequest request) {
			return CONSENT_ACTION_APPROVE.equalsIgnoreCase(request.getParameter(CONSENT_ACTION_PARAMETER_NAME));
		}

		private static boolean isConsentCancelled(HttpServletRequest request) {
			return CONSENT_ACTION_CANCEL.equalsIgnoreCase(request.getParameter(CONSENT_ACTION_PARAMETER_NAME));
		}

		private static String generateConsentPage(HttpServletRequest request,
												  RegisteredClient registeredClient, OAuth2Authorization authorization) {

			OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(
				AUTHORIZATION_REQUEST);
			String state = authorization.getAttribute(OAuth2Authorization.STATE);

			StringBuilder builder = new StringBuilder();

			builder.append("<!DOCTYPE html>");
			builder.append("<html lang=\"en\">");
			builder.append("<head>");
			builder.append("    <meta charset=\"utf-8\">");
			builder.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">");
			builder.append("    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\" integrity=\"sha384-JcKb8q3iqJ61gNV9KGb8thSsNjpSL0n8PARn9HuZOnIxN0hoP+VmmDGMN5t9UJ0Z\" crossorigin=\"anonymous\">");
			builder.append("    <title>Consent required</title>");
			builder.append("</head>");
			builder.append("<body>");
			builder.append("<div class=\"container\">");
			builder.append("    <div class=\"py-5\">");
			builder.append("        <h1 class=\"text-center\">Consent required</h1>");
			builder.append("    </div>");
			builder.append("    <div class=\"row\">");
			builder.append("        <div class=\"col text-center\">");
			builder.append("            <p><span class=\"font-weight-bold text-primary\">" + registeredClient.getClientId() + "</span> wants to access your account <span class=\"font-weight-bold\">" + authorization.getPrincipalName() + "</span></p>");
			builder.append("        </div>");
			builder.append("    </div>");
			builder.append("    <div class=\"row pb-3\">");
			builder.append("        <div class=\"col text-center\">");
			builder.append("            <p>The following permissions are requested by the above app.<br/>Please review these and consent if you approve.</p>");
			builder.append("        </div>");
			builder.append("    </div>");
			builder.append("    <div class=\"row\">");
			builder.append("        <div class=\"col text-center\">");
			builder.append("            <form method=\"post\" action=\"" + request.getRequestURI() + "\">");
			builder.append("                <input type=\"hidden\" name=\"client_id\" value=\"" + registeredClient.getClientId() + "\">");
			builder.append("                <input type=\"hidden\" name=\"state\" value=\"" + state + "\">");

			for (String scope : authorizationRequest.getScopes()) {
				builder.append("                <div class=\"form-group form-check py-1\">");
				builder.append("                    <input class=\"form-check-input\" type=\"checkbox\" name=\"scope\" value=\"" + scope + "\" id=\"" + scope + "\" checked>");
				builder.append("                    <label class=\"form-check-label\" for=\"" + scope + "\">" + scope + "</label>");
				builder.append("                </div>");
			}

			builder.append("                <div class=\"form-group pt-3\">");
			builder.append("                    <button class=\"btn btn-primary btn-lg\" type=\"submit\" name=\"consent_action\" value=\"approve\">Submit Consent</button>");
			builder.append("                </div>");
			builder.append("                <div class=\"form-group\">");
			builder.append("                    <button class=\"btn btn-link regular\" type=\"submit\" name=\"consent_action\" value=\"cancel\">Cancel</button>");
			builder.append("                </div>");
			builder.append("            </form>");
			builder.append("        </div>");
			builder.append("    </div>");
			builder.append("    <div class=\"row pt-4\">");
			builder.append("        <div class=\"col text-center\">");
			builder.append("            <p><small>Your consent to provide access is required.<br/>If you do not approve, click Cancel, in which case no information will be shared with the app.</small></p>");
			builder.append("        </div>");
			builder.append("    </div>");
			builder.append("</div>");
			builder.append("</body>");
			builder.append("</html>");

			return builder.toString();
		}
	}

}
