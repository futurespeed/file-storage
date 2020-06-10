package fs.common;

/**
 * 回调
 *
 * @param <P> 参数类型
 * @param <R> 结果类型
 */
public interface Callback<P, R> {
    /**
     * 调用
     * @param p 参数
     * @return 结果
     */
    R call(P p);
}
