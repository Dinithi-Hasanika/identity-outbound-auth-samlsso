/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authenticator.samlsso.fedIdpInitLogout.response;

import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.HttpIdentityResponseFactory;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authenticator.samlsso.fedIdpInitLogout.exception.SAMLIdentityException;

import org.owasp.encoder.Encode;
import javax.servlet.http.HttpServletResponse;

/**
 * This class  builds a HTTP response instance based on the common
 * IdentityRequest format used by the authentication framework.
 */
public class SAMLLogoutResponseFactory extends HttpIdentityResponseFactory {

    @Override
    public boolean canHandle(IdentityResponse identityResponse) {

        return (identityResponse instanceof SAMLLogoutResponse);
    }

    @Override
    public boolean canHandle(FrameworkException exception) {

        return (exception instanceof SAMLIdentityException);
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder create(IdentityResponse identityResponse) {

        HttpIdentityResponse.HttpIdentityResponseBuilder responseBuilder =
            new HttpIdentityResponse.HttpIdentityResponseBuilder();
        create(responseBuilder, identityResponse);
        return responseBuilder;
    }

    @Override
    public void create(HttpIdentityResponse.HttpIdentityResponseBuilder builder, IdentityResponse identityResponse) {

        SAMLLogoutResponse response = (SAMLLogoutResponse) identityResponse;
        String samlPostPage = generateSamlPostPage(response.getAcsUrl(), response.getResponse(), response.getRelayState());
        builder.setBody(samlPostPage);
        builder.setStatusCode(HttpServletResponse.SC_OK);
        builder.setRedirectURL(response.getAcsUrl());
    }

    @Override
    public HttpIdentityResponse.HttpIdentityResponseBuilder handleException(FrameworkException exception) {

        HttpIdentityResponse.HttpIdentityResponseBuilder errorResponseBuilder =
            new HttpIdentityResponse.HttpIdentityResponseBuilder();
        SAMLIdentityException samlException = (SAMLIdentityException) exception;
        String samlPostPage = generateSamlPostPage(samlException.getAcsUrl(), samlException.getExceptionMessage(),
            samlException.getRelayState());
        errorResponseBuilder.setBody(samlPostPage);
        errorResponseBuilder.setStatusCode(HttpServletResponse.SC_OK);
        errorResponseBuilder.setBody(samlException.getExceptionMessage());
        errorResponseBuilder.setRedirectURL(samlException.getAcsUrl());
        return errorResponseBuilder;
    }

    /**
     *Generate the post page for the logout response.
     *
     * @param acUrl
     * @param samlMessage
     * @param relayState
     * @return
     */
    private String generateSamlPostPage(String acUrl, String samlMessage, String relayState) {

        StringBuilder out = new StringBuilder();
        out.append("<html>");
        out.append("<body>");
        out.append("<p>You are now redirected back to " + Encode.forHtmlContent(acUrl));
        out.append(" If the redirection fails, please click the post button.</p>");
        out.append("<form method='post' action='" + Encode.forHtmlAttribute(acUrl) + "'>");
        out.append("<p>");
        out.append("<input type='hidden' name='SAMLResponse' value='" + Encode.forHtmlAttribute(samlMessage) + "'>");

        if (relayState != null) {
            out.append("<input type='hidden' name='RelayState' value='" + Encode.forHtmlAttribute(relayState) +
                "'>");
        }
        out.append("<button type='submit'>POST</button>");
        out.append("</p>");
        out.append("</form>");
        out.append("<script type='text/javascript'>");
        out.append("document.forms[0].submit();");
        out.append("</script>");
        out.append("</body>");
        out.append("</html>");
        return out.toString();
    }
}
