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

package pub.ihub.demo.client.web;

import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;
import static pub.ihub.secure.core.Constant.CLIENT_ID_CLIENT_CREDENTIALS;
import static pub.ihub.secure.core.Constant.CLIENT_ID_INTERNAL;
import static pub.ihub.secure.core.Constant.RESOURCE_SCOPES_ENDPOINT_URI;

/**
 * @author liheng
 */
@RestController
@AllArgsConstructor
public class DefaultController {

	private final WebClient webClient;
	private final String messagesBaseUri = "http://localhost:8090/messages";

	@GetMapping({"/", "/index"})
	public String index(@RegisteredOAuth2AuthorizedClient(CLIENT_ID_CLIENT_CREDENTIALS)
							OAuth2AuthorizedClient authorizedClient) {
		String messages = webClient
			.get()
			.uri(this.messagesBaseUri)
			.attributes(oauth2AuthorizedClient(authorizedClient))
			.retrieve()
			.bodyToMono(String.class)
			.block();

		return messages;
	}

	@GetMapping("/scopes")
	public Set<?> scopes() {
		return webClient
			.get()
			.uri("http://localhost:8090" + RESOURCE_SCOPES_ENDPOINT_URI)
			.attributes(clientRegistrationId(CLIENT_ID_INTERNAL))
			.retrieve()
			.bodyToMono(Set.class)
			.block();
	}

}
