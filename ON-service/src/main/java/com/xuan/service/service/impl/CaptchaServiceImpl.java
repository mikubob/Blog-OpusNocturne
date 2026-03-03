package com.xuan.service.service.impl;

import com.xuan.common.enums.CaptchaType;
import com.xuan.common.constant.RedisConstant;
import com.xuan.entity.dto.common.CaptchaResponse;
import com.xuan.service.service.ICaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 *
 * @author 玄〤
 * @since 2026-03-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements ICaptchaService {

    private final StringRedisTemplate stringRedisTemplate;
    private final Random random = new Random();

    @Override
    public CaptchaResponse generateCaptcha(CaptchaType type, String identifier, String ip) {
        // 生成数学算式
        String question;
        int answer;

        // 随机选择运算符
        int operator = random.nextInt(4); // 0:+, 1:-, 2:*, 3:/
        int num1 = random.nextInt(20) + 1; // 1-20
        int num2 = random.nextInt(20) + 1; // 1-20

        switch (operator) {
            case 0: // 加法
                question = num1 + " + " + num2 + " = ?";
                answer = num1 + num2;
                break;
            case 1: // 减法
                // 确保结果为正数
                if (num1 < num2) {
                    int temp = num1;
                    num1 = num2;
                    num2 = temp;
                }
                question = num1 + " - " + num2 + " = ?";
                answer = num1 - num2;
                break;
            case 2: // 乘法
                // 限制乘法结果不太大
                num1 = random.nextInt(10) + 1; // 1-10
                num2 = random.nextInt(10) + 1; // 1-10
                question = num1 + " × " + num2 + " = ?";
                answer = num1 * num2;
                break;
            case 3: // 除法
                // 确保能整除
                num2 = random.nextInt(9) + 1; // 1-9
                num1 = num2 * (random.nextInt(10) + 1); // 确保能整除
                question = num1 + " ÷ " + num2 + " = ?";
                answer = num1 / num2;
                break;
            default:
                question = "1 + 1 = ?";
                answer = 2;
        }

        // 构建Redis Key
        String captchaKey;
        if (CaptchaType.LOGIN == type) {
            captchaKey = RedisConstant.CAPTCHA_LOGIN_KEY_PREFIX + ip + ":" + identifier;
        } else {
            captchaKey = RedisConstant.CAPTCHA_COMMENT_KEY_PREFIX + identifier;
        }

        // 存储到Redis
        stringRedisTemplate.opsForValue().set(
                captchaKey,
                String.valueOf(answer),
                RedisConstant.CAPTCHA_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        return new CaptchaResponse(question, captchaKey);
    }

    @Override
    public boolean validateCaptcha(CaptchaType type, String identifier, String answer) {
        String captchaKey;
        if (CaptchaType.LOGIN == type) {
            // LOGIN类型直接使用传入的完整key
            captchaKey = identifier;
        } else {
            // COMMENT类型构建key
            captchaKey = RedisConstant.CAPTCHA_COMMENT_KEY_PREFIX + identifier;
        }

        try {
            // 从Redis获取答案
            String correctAnswer = stringRedisTemplate.opsForValue().get(captchaKey);
            if (correctAnswer == null) {
                return false;
            }

            // 验证答案
            boolean isValid = correctAnswer.equals(answer);

            // 无论对错，立即删除Redis中的key（防重放）
            stringRedisTemplate.delete(captchaKey);

            return isValid;
        } catch (Exception e) {
            log.error("验证码验证失败", e);
            // 异常时也删除key
            stringRedisTemplate.delete(captchaKey);
            return false;
        }
    }

}
