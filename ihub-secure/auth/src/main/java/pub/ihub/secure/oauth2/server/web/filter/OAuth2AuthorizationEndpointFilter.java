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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.endpoint.PkceParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.CLIENT_ID;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REDIRECT_URI;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.RESPONSE_TYPE;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.SCOPE;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZATION_REQUEST;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.AUTHORIZED_SCOPES;
import static pub.ihub.secure.oauth2.server.OAuth2Authorization.STATE;

/**
 * OAuth 2.0授权代码授予的Filter ，用于处理OAuth 2.0授权请求的处理
 * TODO 整理页面
 *
 * @author henry
 */
public class OAuth2AuthorizationEndpointFilter extends OAuth2Filter {

	// TODO
	private static final String PKCE_ERROR_URI = "https://tools.ietf.org/html/rfc7636#section-4.4.1";

	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final RequestMatcher authorizationRequestMatcher;
	private final RequestMatcher userConsentMatcher;
	private final StringKeyGenerator codeGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
	private final StringKeyGenerator stateGenerator = new Base64StringKeyGenerator(Base64.getUrlEncoder());
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public OAuth2AuthorizationEndpointFilter(RegisteredClientRepository registeredClientRepository,
											 OAuth2AuthorizationService authorizationService, String authorizationEndpointUri) {
		Assert.notNull(registeredClientRepository, "registeredClientRepository cannot be null");
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.hasText(authorizationEndpointUri, "authorizationEndpointUri cannot be empty");
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;

		RequestMatcher authorizationRequestGetMatcher = new AntPathRequestMatcher(
			authorizationEndpointUri, HttpMethod.GET.name());
		RequestMatcher authorizationRequestPostMatcher = new AntPathRequestMatcher(
			authorizationEndpointUri, HttpMethod.POST.name());
		RequestMatcher openidScopeMatcher = request -> {
			String scope = request.getParameter(OAuth2ParameterNames.SCOPE);
			return StringUtils.hasText(scope) && scope.contains(OidcScopes.OPENID);
		};
		RequestMatcher consentActionMatcher = request ->
			request.getParameter(UserConsentPage.CONSENT_ACTION_PARAMETER_NAME) != null;
		this.authorizationRequestMatcher = new OrRequestMatcher(
			authorizationRequestGetMatcher,
			new AndRequestMatcher(
				authorizationRequestPostMatcher, openidScopeMatcher,
				new NegatedRequestMatcher(consentActionMatcher)));
		this.userConsentMatcher = new AndRequestMatcher(
			authorizationRequestPostMatcher, consentActionMatcher);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (this.authorizationRequestMatcher.matches(request)) {
			processAuthorizationRequest(request, response, filterChain);
		} else if (this.userConsentMatcher.matches(request)) {
			processUserConsent(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	private void processAuthorizationRequest(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		OAuth2AuthorizationRequestContext authorizationRequestContext =
			new OAuth2AuthorizationRequestContext(
				request.getRequestURL().toString(),
				getParameters(request));

		validateAuthorizationRequest(authorizationRequestContext);

		if (authorizationRequestContext.hasError()) {
			if (authorizationRequestContext.isRedirectOnError()) {
				sendErrorResponse(request, response, authorizationRequestContext.resolveRedirectUri(),
					authorizationRequestContext.getError(), authorizationRequestContext.getState());
			} else {
				sendErrorResponse(response, authorizationRequestContext.getError());
			}
			return;
		}

		// ---------------
		// The request is valid - ensure the resource owner is authenticated
		// ---------------

		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		if (!isPrincipalAuthenticated(principal)) {
			// Pass through the chain with the expectation that the authentication process
			// will commence via AuthenticationEntryPoint
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
				.put(OAuth2Authorization::getAttributes, STATE, state)
				.build();
			this.authorizationService.save(authorization);

			// TODO Need to remove 'in-flight' authorization if consent step is not completed (e.g. approved or cancelled)

			UserConsentPage.displayConsent(request, response, registeredClient, authorization);
		} else {
			Instant issuedAt = Instant.now();
			Instant expiresAt = issuedAt.plus(5, ChronoUnit.MINUTES);        // TODO Allow configuration for authorization code time-to-live
			OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
				this.codeGenerator.generateKey(), issuedAt, expiresAt);
			OAuth2Authorization authorization = builder
				.set(OAuth2Authorization::setTokens,
					ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::token, authorizationCode).build())
				.put(OAuth2Authorization::getAttributes, AUTHORIZED_SCOPES, authorizationRequest.getScopes())
				.build();
			this.authorizationService.save(authorization);

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

		UserConsentRequestContext userConsentRequestContext =
			new UserConsentRequestContext(
				request.getRequestURL().toString(),
				getParameters(request));

		validateUserConsentRequest(userConsentRequestContext);

		if (userConsentRequestContext.hasError()) {
			if (userConsentRequestContext.isRedirectOnError()) {
				sendErrorResponse(request, response, userConsentRequestContext.resolveRedirectUri(),
					userConsentRequestContext.getError(), userConsentRequestContext.getState());
			} else {
				sendErrorResponse(response, userConsentRequestContext.getError());
			}
			return;
		}

		if (!UserConsentPage.isConsentApproved(request)) {
			this.authorizationService.remove(userConsentRequestContext.getAuthorization());
			OAuth2Error error = createError(OAuth2ErrorCodes.ACCESS_DENIED, OAuth2ParameterNames.CLIENT_ID);
			sendErrorResponse(request, response, userConsentRequestContext.resolveRedirectUri(),
				error, userConsentRequestContext.getAuthorizationRequest().getState());
			return;
		}

		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(5, ChronoUnit.MINUTES);        // TODO Allow configuration for authorization code time-to-live
		OAuth2AuthorizationCode authorizationCode = new OAuth2AuthorizationCode(
			this.codeGenerator.generateKey(), issuedAt, expiresAt);
		OAuth2Authorization authorization = OAuth2Authorization.from(userConsentRequestContext.getAuthorization())
			.set(OAuth2Authorization::setTokens,
				ObjectBuilder.builder(OAuth2Tokens::new).set(OAuth2Tokens::token, authorizationCode).build())
			.setSub(OAuth2Authorization::getAttributes, attributes -> {
				attributes.remove(STATE);
				attributes.put(AUTHORIZED_SCOPES, userConsentRequestContext.getScopes());
			})
			.build();
		this.authorizationService.save(authorization);

		sendAuthorizationResponse(request, response, userConsentRequestContext.resolveRedirectUri(),
			authorizationCode, userConsentRequestContext.getAuthorizationRequest().getState());
	}

	private void validateAuthorizationRequest(OAuth2AuthorizationRequestContext authorizationRequestContext) {
		// ---------------
		// Validate the request to ensure all required parameters are present and valid
		// ---------------

		// client_id (REQUIRED)
		if (!StringUtils.hasText(authorizationRequestContext.getClientId()) ||
			authorizationRequestContext.getParameters().get(OAuth2ParameterNames.CLIENT_ID).size() != 1) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CLIENT_ID));
			return;
		}
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(
			authorizationRequestContext.getClientId());
		if (registeredClient == null) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CLIENT_ID));
			return;
		} else if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT, OAuth2ParameterNames.CLIENT_ID));
			return;
		}
		authorizationRequestContext.setRegisteredClient(registeredClient);

		// redirect_uri (OPTIONAL)
		if (StringUtils.hasText(authorizationRequestContext.getRedirectUri())) {
			if (!registeredClient.getRedirectUris().contains(authorizationRequestContext.getRedirectUri()) ||
				authorizationRequestContext.getParameters().get(OAuth2ParameterNames.REDIRECT_URI).size() != 1) {
				authorizationRequestContext.setError(
					createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI));
				return;
			}
		} else if (authorizationRequestContext.isAuthenticationRequest() ||        // redirect_uri is REQUIRED for OpenID Connect
			registeredClient.getRedirectUris().size() != 1) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.REDIRECT_URI));
			return;
		}
		authorizationRequestContext.setRedirectOnError(true);

		// response_type (REQUIRED)
		if (!StringUtils.hasText(authorizationRequestContext.getResponseType()) ||
			authorizationRequestContext.getParameters().get(OAuth2ParameterNames.RESPONSE_TYPE).size() != 1) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.RESPONSE_TYPE));
			return;
		} else if (!authorizationRequestContext.getResponseType().equals(OAuth2AuthorizationResponseType.CODE.getValue())) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.UNSUPPORTED_RESPONSE_TYPE, OAuth2ParameterNames.RESPONSE_TYPE));
			return;
		}

		// scope (OPTIONAL)
		Set<String> requestedScopes = authorizationRequestContext.getScopes();
		Set<String> allowedScopes = registeredClient.getScopes();
		if (!requestedScopes.isEmpty() && !allowedScopes.containsAll(requestedScopes)) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_SCOPE, OAuth2ParameterNames.SCOPE));
			return;
		}

		// code_challenge (REQUIRED for public clients) - RFC 7636 (PKCE)
		String codeChallenge = authorizationRequestContext.getParameters().getFirst(PkceParameterNames.CODE_CHALLENGE);
		if (StringUtils.hasText(codeChallenge)) {
			if (authorizationRequestContext.getParameters().get(PkceParameterNames.CODE_CHALLENGE).size() != 1) {
				authorizationRequestContext.setError(
					createError(OAuth2ErrorCodes.INVALID_REQUEST, PkceParameterNames.CODE_CHALLENGE, PKCE_ERROR_URI));
				return;
			}

			String codeChallengeMethod = authorizationRequestContext.getParameters().getFirst(PkceParameterNames.CODE_CHALLENGE_METHOD);
			if (StringUtils.hasText(codeChallengeMethod)) {
				if (authorizationRequestContext.getParameters().get(PkceParameterNames.CODE_CHALLENGE_METHOD).size() != 1 ||
					(!"S256".equals(codeChallengeMethod) && !"plain".equals(codeChallengeMethod))) {
					authorizationRequestContext.setError(
						createError(OAuth2ErrorCodes.INVALID_REQUEST, PkceParameterNames.CODE_CHALLENGE_METHOD, PKCE_ERROR_URI));
					return;
				}
			}
		} else if (registeredClient.isRequireProofKey()) {
			authorizationRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, PkceParameterNames.CODE_CHALLENGE, PKCE_ERROR_URI));
			return;
		}
	}

	private void validateUserConsentRequest(UserConsentRequestContext userConsentRequestContext) {
		// ---------------
		// Validate the request to ensure all required parameters are present and valid
		// ---------------

		// state (REQUIRED)
		if (!StringUtils.hasText(userConsentRequestContext.getState()) ||
			userConsentRequestContext.getParameters().get(OAuth2ParameterNames.STATE).size() != 1) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.STATE));
			return;
		}
		OAuth2Authorization authorization = this.authorizationService.findByToken(
			userConsentRequestContext.getState(), new TokenType(STATE));
		if (authorization == null) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.STATE));
			return;
		}
		userConsentRequestContext.setAuthorization(authorization);

		// The 'in-flight' authorization must be associated to the current principal
		Authentication principal = SecurityContextHolder.getContext().getAuthentication();
		if (!isPrincipalAuthenticated(principal) || !principal.getName().equals(authorization.getPrincipalName())) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.STATE));
			return;
		}

		// client_id (REQUIRED)
		if (!StringUtils.hasText(userConsentRequestContext.getClientId()) ||
			userConsentRequestContext.getParameters().get(OAuth2ParameterNames.CLIENT_ID).size() != 1) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CLIENT_ID));
			return;
		}
		RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(
			userConsentRequestContext.getClientId());
		if (registeredClient == null || !registeredClient.getId().equals(authorization.getRegisteredClientId())) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_REQUEST, OAuth2ParameterNames.CLIENT_ID));
			return;
		}
		userConsentRequestContext.setRegisteredClient(registeredClient);
		userConsentRequestContext.setRedirectOnError(true);

		// scope (OPTIONAL)
		Set<String> requestedScopes = userConsentRequestContext.getAuthorizationRequest().getScopes();
		Set<String> authorizedScopes = userConsentRequestContext.getScopes();
		if (!authorizedScopes.isEmpty() && !requestedScopes.containsAll(authorizedScopes)) {
			userConsentRequestContext.setError(
				createError(OAuth2ErrorCodes.INVALID_SCOPE, OAuth2ParameterNames.SCOPE));
			return;
		}
	}

	private void sendAuthorizationResponse(HttpServletRequest request, HttpServletResponse response,
										   String redirectUri, OAuth2AuthorizationCode authorizationCode, String state) throws IOException {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
			.fromUriString(redirectUri)
			.queryParam(OAuth2ParameterNames.CODE, authorizationCode.getTokenValue());
		if (StringUtils.hasText(state)) {
			uriBuilder.queryParam(OAuth2ParameterNames.STATE, state);
		}
		this.redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString());
	}

	private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response,
								   String redirectUri, OAuth2Error error, String state) throws IOException {

		UriComponentsBuilder uriBuilder = UriComponentsBuilder
			.fromUriString(redirectUri)
			.queryParam(OAuth2ParameterNames.ERROR, error.getErrorCode());
		if (StringUtils.hasText(error.getDescription())) {
			uriBuilder.queryParam(OAuth2ParameterNames.ERROR_DESCRIPTION, error.getDescription());
		}
		if (StringUtils.hasText(error.getUri())) {
			uriBuilder.queryParam(OAuth2ParameterNames.ERROR_URI, error.getUri());
		}
		if (StringUtils.hasText(state)) {
			uriBuilder.queryParam(OAuth2ParameterNames.STATE, state);
		}
		this.redirectStrategy.sendRedirect(request, response, uriBuilder.toUriString());
	}

	private void sendErrorResponse(HttpServletResponse response, OAuth2Error error) throws IOException {
		// TODO Send default html error response
		response.sendError(HttpStatus.BAD_REQUEST.value(), error.toString());
	}

	private static OAuth2Error createError(String errorCode, String parameterName) {
		return createError(errorCode, parameterName, "https://tools.ietf.org/html/rfc6749#section-4.1.2.1");
	}

	private static OAuth2Error createError(String errorCode, String parameterName, String errorUri) {
		return new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
	}

	private static boolean isPrincipalAuthenticated(Authentication principal) {
		return principal != null &&
			!AnonymousAuthenticationToken.class.isAssignableFrom(principal.getClass()) &&
			principal.isAuthenticated();
	}

	@Getter(PRIVATE)
	private static class OAuth2AuthorizationRequestContext extends AbstractRequestContext {

		private final String responseType;
		private final String redirectUri;

		private OAuth2AuthorizationRequestContext(
			String authorizationUri, MultiValueMap<String, String> parameters) {
			super(authorizationUri, parameters,
				parameters.getFirst(CLIENT_ID),
				parameters.getFirst(OAuth2ParameterNames.STATE),
				extractScopes(parameters));
			this.responseType = parameters.getFirst(RESPONSE_TYPE);
			this.redirectUri = parameters.getFirst(REDIRECT_URI);
		}

		private static Set<String> extractScopes(MultiValueMap<String, String> parameters) {
			String scope = parameters.getFirst(SCOPE);
			return StringUtils.hasText(scope) ?
				new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(scope, " "))) :
				Collections.emptySet();
		}

		private boolean isAuthenticationRequest() {
			return getScopes().contains(OidcScopes.OPENID);
		}

		@Override
		protected String resolveRedirectUri() {
			return StringUtils.hasText(getRedirectUri()) ?
				getRedirectUri() :
				getRegisteredClient().getRedirectUris().iterator().next();
		}

		private OAuth2AuthorizationRequest buildAuthorizationRequest() {
			return OAuth2AuthorizationRequest.authorizationCode()
				.authorizationUri(getAuthorizationUri())
				.clientId(getClientId())
				.redirectUri(getRedirectUri())
				.scopes(getScopes())
				.state(getState())
				.additionalParameters(additionalParameters ->
					getParameters().entrySet().stream()
						.filter(e -> !e.getKey().equals(RESPONSE_TYPE) &&
							!e.getKey().equals(CLIENT_ID) &&
							!e.getKey().equals(REDIRECT_URI) &&
							!e.getKey().equals(SCOPE) &&
							!e.getKey().equals(OAuth2ParameterNames.STATE))
						.forEach(e -> additionalParameters.put(e.getKey(), e.getValue().get(0))))
				.build();
		}
	}

	@Getter(PRIVATE)
	@Setter(PRIVATE)
	private static class UserConsentRequestContext extends AbstractRequestContext {

		private OAuth2Authorization authorization;

		private UserConsentRequestContext(
			String authorizationUri, MultiValueMap<String, String> parameters) {
			super(authorizationUri, parameters,
				parameters.getFirst(CLIENT_ID),
				parameters.getFirst(OAuth2ParameterNames.STATE),
				extractScopes(parameters));
		}

		private static Set<String> extractScopes(MultiValueMap<String, String> parameters) {
			List<String> scope = parameters.get(SCOPE);
			return !CollectionUtils.isEmpty(scope) ? new HashSet<>(scope) : Collections.emptySet();
		}

		@Override
		protected String resolveRedirectUri() {
			OAuth2AuthorizationRequest authorizationRequest = getAuthorizationRequest();
			return StringUtils.hasText(authorizationRequest.getRedirectUri()) ?
				authorizationRequest.getRedirectUri() :
				getRegisteredClient().getRedirectUris().iterator().next();
		}

		private OAuth2AuthorizationRequest getAuthorizationRequest() {
			return getAuthorization().getAttribute(AUTHORIZATION_REQUEST);
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

		protected AbstractRequestContext(String authorizationUri, MultiValueMap<String, String> parameters,
										 String clientId, String state, Set<String> scopes) {
			this.authorizationUri = authorizationUri;
			this.parameters = parameters;
			this.clientId = clientId;
			this.state = state;
			this.scopes = scopes;
		}

		protected boolean hasError() {
			return getError() != null;
		}

		protected abstract String resolveRedirectUri();
	}

	private static class UserConsentPage {

		private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);
		private static final String CONSENT_ACTION_PARAMETER_NAME = "consent_action";
		private static final String CONSENT_ACTION_APPROVE = "approve";
		private static final String CONSENT_ACTION_CANCEL = "cancel";

		private static void displayConsent(HttpServletRequest request, HttpServletResponse response,
										   RegisteredClient registeredClient, OAuth2Authorization authorization) throws IOException {

			String consentPage = generateConsentPage(request, registeredClient, authorization);
			response.setContentType(TEXT_HTML_UTF8.toString());
			response.setContentLength(consentPage.getBytes(StandardCharsets.UTF_8).length);
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
			String state = authorization.getAttribute(
				STATE);

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
