package com.jobtrack.application.port;

import com.jobtrack.domain.user.User;

public interface AccessTokenIssuer {
    String issue(User user);
    long accessTokenSeconds();
}
