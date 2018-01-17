package io.dico.dicore;

@FunctionalInterface
public interface ChainedRunnable extends InterfaceChain<Runnable, ChainedRunnable>, Runnable {
    
    @Override
    default ChainedRunnable getEmptyInstance() {
        return EMPTY;
    }
    
    default ChainedRunnable andThen(Runnable other) {
        if (other == null) {
            return this;
        }

        int count = getElementCount() + 1;
        return new ChainedRunnable() {
            @Override
            public void run() {
                try {
                    ChainedRunnable.this.run();
                } finally {
                    other.run();
                }
            }

            @Override
            public ChainedRunnable withoutLastNode() {
                return ChainedRunnable.this;
            }

            @Override
            public Runnable getDelegateOfLastNode() {
                return other;
            }

            @Override
            public int getElementCount() {
                return count;
            }
        };
    }
    
    ChainedRunnable EMPTY = new ChainedRunnable() {
        @Override
        public void run() {
            
        }
        
        @Override
        public ChainedRunnable andThen(Runnable other) {
            return ChainedRunnable.singleton(other);
        }
        
        @Override
        public int getElementCount() {
            return 0;
        }
    
        @Override
        public Runnable getDelegateOfLastNode() {
            return null;
        }
    };
    
    static ChainedRunnable singleton(Runnable n) {
        if (n instanceof ChainedRunnable) {
            return (ChainedRunnable) n;
        }
        if (n == null) {
            return EMPTY;
        }
        return new ChainedRunnable() {
            @Override
            public void run() {
                n.run();
            }

            @Override
            public Runnable getDelegateOfLastNode() {
                return n;
            }
        };
    }
    
}
