package io.renren.modules.app.service;

import io.renren.common.utils.R;
import io.renren.modules.app.entity.loginUserEntity;

public interface UserSecurityService {

    R login(loginUserEntity loginUserEntity);

    R logout();
}
