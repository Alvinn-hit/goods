package com.recklessmo.dao.sms;

import com.recklessmo.model.sms.SmsCode;
import org.apache.ibatis.annotations.Param;

/**
 *
 * Created by hpf on 2/9/17.
 */
public interface SmsCodeDAO {

    void addSmsCode(SmsCode smsCode);
    SmsCode getSmsCodeById(@Param("id")long id);

}
