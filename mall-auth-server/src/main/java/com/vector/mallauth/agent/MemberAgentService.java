package com.vector.mallauth.agent;


import com.vector.common.to.WBSocialUserTO;
import com.vector.common.utils.R;
import com.vector.mallauth.auth.WBSocialUserVo;
import com.vector.mallauth.feign.MemberFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MemberAgentService {

    @Autowired
    MemberFeignService memberFeignService;

    public R oauthLogin(WBSocialUserVo user) {
        WBSocialUserTO param = new WBSocialUserTO();
        param.setAccessToken(user.getAccess_token());
        param.setExpiresIn(user.getExpires_in());
        param.setRemindIn(user.getRemind_in());
        param.setIsRealName(user.getIsRealName());
        param.setUid(user.getUid());
        return memberFeignService.oauthLogin(param);
    }
}
