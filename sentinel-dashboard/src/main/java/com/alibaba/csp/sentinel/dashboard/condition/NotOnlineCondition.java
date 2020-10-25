package com.alibaba.csp.sentinel.dashboard.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * <h3>sentinel-parent</h3>
 * <h4>com.alibaba.csp.sentinel.dashboard.condition</h4>
 * <p>是否非Online环境</p>
 *
 * @author zora
 * @since 2020.10.20
 */
public class NotOnlineCondition implements Condition {
    private static final String ONLINE = "online";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        for (String profile : conditionContext.getEnvironment().getActiveProfiles()) {
            if (profile.toLowerCase().equals(ONLINE)) {
                return false;
            }
        }
        return true;
    }
}
