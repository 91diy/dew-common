package com.ecfront.dew.common;

import com.ecfront.dew.common.interceptor.DewInterceptContext;
import com.ecfront.dew.common.interceptor.DewInterceptExec;
import com.ecfront.dew.common.interceptor.DewInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拦截器栈执行器
 */
public class InterceptorHelper {

    private static Logger logger = LoggerFactory.getLogger(InterceptorHelper.class);

    private static Map<String, List<DewInterceptor<?, ?>>> CONTAINER = new HashMap<>();

    InterceptorHelper() {
    }

    /**
     * 注册拦截器栈
     *
     * @param category    拦截类型
     * @param interceptor 拦截器
     */
    public void register(String category, DewInterceptor<?, ?> interceptor) {
        if (!CONTAINER.containsKey(category)) {
            CONTAINER.put(category, new ArrayList<>());
        }
        CONTAINER.get(category).add(interceptor);
    }

    /**
     * 注册拦截器栈
     *
     * @param category     拦截类型
     * @param interceptors 拦截器列表
     */
    public void register(String category, List<DewInterceptor<?, ?>> interceptors) {
        CONTAINER.put(category, interceptors);
    }

    /**
     * 拦截器栈处理方法
     *
     * @param category 拦截类型
     * @param input    初始入栈对象
     * @param fun      实际执行方法
     * @tparam E 对象的类型
     */
    public <I, O> Resp<DewInterceptContext<I, O>> process(String category, I input, DewInterceptExec<I, O> fun) {
        return process(category, input, new HashMap<>(), fun);
    }

    /**
     * 拦截器栈处理方法
     *
     * @param category 拦截类型
     * @param input    初始入栈对象
     * @param args     初始入栈参数
     * @param fun      实际执行方法
     * @tparam E 对象的类型
     */
    public <I, O> Resp<DewInterceptContext<I, O>> process(String category, I input, Map<String, Object> args, DewInterceptExec<I, O> fun) {
        DewInterceptContext<I, O> context = new DewInterceptContext<>();
        context.setInput(input);
        context.setArgs(args);
        logger.debug("[DewInterceptorProcessor] Process [{}]", category);
        if (!CONTAINER.containsKey(category)) {
            return fun.exec(context);
        }
        List<DewInterceptor<?, ?>> interceptors = CONTAINER.get(category);
        Resp<DewInterceptContext<I, O>> beforeR = doProcess(context, interceptors, true);
        if (!beforeR.ok()) {
            return beforeR;
        }
        Resp<DewInterceptContext<I, O>> execR = fun.exec(beforeR.getBody());
        if (!execR.ok()) {
            return execR;
        }
        return doProcess(execR.getBody(), interceptors, false);
    }

    private static <I, O> Resp<DewInterceptContext<I, O>> doProcess(DewInterceptContext<I, O> context, List<DewInterceptor<?, ?>> interceptors, boolean isBefore) {
        Resp<DewInterceptContext<I, O>> result = Resp.success(context);
        for (DewInterceptor<?, ?> interceptor : interceptors) {
            logger.trace("[DewInterceptorProcessor] Process interceptor [{}]:{}-{}", interceptor.getCategory(), interceptor.getName(), isBefore ? "before" : "after");
            DewInterceptor<I, O> interceptorE = (DewInterceptor<I, O>) interceptor;
            try {
                if (isBefore) {
                    result = interceptorE.before(context);
                } else {
                    result = interceptorE.after(context);
                }
                if (!result.ok()) {
                    logger.warn("[DewInterceptorProcessor] Process interceptor error [{}]:{}-{},[{}]{}",
                            interceptor.getCategory(), interceptor.getName(), isBefore ? "before" : "after", result.getCode(), result.getMessage());
                    interceptorE.error(context);
                    return result;
                }
            } catch (Throwable e) {
                result = Resp.serverError(e.getMessage());
                logger.error("[DewInterceptorProcessor] Process interceptor error [{}]:{}-{},[{}]{}",
                        interceptor.getCategory(), interceptor.getName(), isBefore ? "before" : "after", result.getCode(), result.getMessage());
                interceptorE.error(context);
                return result;
            }
        }
        return result;
    }

}
