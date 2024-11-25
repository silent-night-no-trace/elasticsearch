/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */
package fixture.s3;

import com.sun.net.httpserver.HttpHandler;

import org.elasticsearch.rest.RestStatus;

import static fixture.s3.S3HttpHandler.sendError;

public class S3HttpFixtureWithSessionToken extends S3HttpFixture {

    protected final String sessionToken;

    public S3HttpFixtureWithSessionToken(String bucket, String basePath, String accessKey, String sessionToken) {
        super(true, bucket, basePath, accessKey);
        this.sessionToken = sessionToken;
    }

    @Override
    protected HttpHandler createHandler() {
        final HttpHandler delegate = super.createHandler();
        return exchange -> {
            final String securityToken = exchange.getRequestHeaders().getFirst("x-amz-security-token");
            if (securityToken == null) {
                sendError(exchange, RestStatus.FORBIDDEN, "AccessDenied", "No session token");
                return;
            }
            if (securityToken.equals(sessionToken) == false) {
                sendError(exchange, RestStatus.FORBIDDEN, "AccessDenied", "Bad session token");
                return;
            }
            delegate.handle(exchange);
        };
    }
}
