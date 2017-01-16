package com.recklessmo.dao.wechat;

import com.recklessmo.model.wechat.WechatCallbackMsg;
import com.recklessmo.model.wechat.WechatMessage;
import com.recklessmo.web.webmodel.page.WechatMsgPage;

import java.util.List;

/**
 * Created by hpf on 1/12/17.
 */
public interface WechatMessageDAO {

    List<WechatMessage> getListByOrgIdAndOpenId(WechatMsgPage page);
    int getCountByOrgIdAndOpenId(WechatMsgPage page);


    void insertMessage(WechatMessage wechatMessage);

}