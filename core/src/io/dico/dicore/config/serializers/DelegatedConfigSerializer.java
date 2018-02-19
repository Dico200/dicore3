package io.dico.dicore.config.serializers;

public abstract class DelegatedConfigSerializer<TIn, TOut, TDel extends IConfigSerializer<TIn>> extends BaseConfigSerializer<TOut> {
    protected final TDel delegate;
    
    DelegatedConfigSerializer(TDel delegate) {
        this.delegate = delegate;
    }
    
    public TDel getDelegate() {
        return delegate;
    }
    
    @Override
    public String inputTypeName() {
        return delegate.inputTypeName();
    }
    
}
