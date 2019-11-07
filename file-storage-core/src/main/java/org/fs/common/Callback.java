package org.fs.common;

/**
 * 回调
 *
 * @param <P> 参数类型
 * @param <R> 结果类型
 */
public interface Callback<P, R> {
    R call(P p);
}
