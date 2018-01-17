package io.dico.dicore;

public interface ChainedListenerHandle extends ListenerHandle {
    
    /**
     * returns a ChainedListenerHandle with the last added node detached.
     *
     * if this ChainedListenerHandle is {@link #ROOT}, {@link #ROOT} is returned.
     * The default implementation also returns {@link #ROOT}.
     *
     * @return a ChainedListenerHandle with the last added node detached
     */
    default ChainedListenerHandle withoutLastNode() {
        return ROOT;
    }
    
    /**
     * returns the delegate operation that was inserted as the last node
     *
     * For instance, calling this method on the result of calling {@link #andThen(ListenerHandle)}
     * would return the given ListenerHandle.
     *
     * @return the delegate operation that was inserted as the last node
     */
    default ListenerHandle delegateOfLastNode() {
        // this implementation is for the purpose of lambda constructors or anonymous classes
        return this;
    }
    
    /**
     * @return The amount of runnables that will be run upon running this ChainedListenerHandle.
     */
    default int count() {
        return 1;
    }
    
    default ChainedListenerHandle andThen(ListenerHandle... others) {
        ChainedListenerHandle result = this;
        for (ListenerHandle handle : others) {
            result = result.andThen(handle);
        }
        return result;
    }
    
    /**
     * @param other A new node to insert at the end of this runnable.
     * @return a new ChainedListenerHandle that includes the given ListenerHandle.
     */
    default ChainedListenerHandle andThen(ListenerHandle other) {
        if (other == null) {
            return this;
        }
        
        int count = count() + 1;
        return new ChainedListenerHandle() {
            @Override
            public void register() {
                try {
                    ChainedListenerHandle.this.register();
                } finally {
                    other.register();
                }
            }
    
            @Override
            public void unregister() {
                try {
                    ChainedListenerHandle.this.unregister();
                } finally {
                    other.unregister();
                }
            }
    
            @Override
            public ChainedListenerHandle withoutLastNode() {
                return ChainedListenerHandle.this;
            }
            
            @Override
            public ListenerHandle delegateOfLastNode() {
                return other;
            }
            
            @Override
            public int count() {
                return count;
            }
        };
    }
    
    /**
     * The empty ChainedListenerHandle. Initialize fields to this instance.
     * Expand it by setting them to the result of calling its {@link #andThen(ListenerHandle)} method.
     */
    ChainedListenerHandle ROOT = new ChainedListenerHandle() {
        @Override
        public void register() {
        
        }
        
        public void unregister() {
        
        }
        
        @Override
        public ChainedListenerHandle andThen(ListenerHandle other) {
            return ChainedListenerHandle.singleton(other);
        }
        
        @Override
        public int count() {
            return 0;
        }
        
        @Override
        public ListenerHandle delegateOfLastNode() {
            return null;
        }
    };
    
    static ChainedListenerHandle singleton(ListenerHandle n) {
        if (n instanceof ChainedListenerHandle) {
            return (ChainedListenerHandle) n;
        }
        if (n == null) {
            return ROOT;
        }
        return new ChainedListenerHandle() {
            @Override
            public void register() {
                n.register();
            }
            
            public void unregister() {
                n.unregister();
            }
            
            @Override
            public ListenerHandle delegateOfLastNode() {
                return n;
            }
        };
    }
    
}
