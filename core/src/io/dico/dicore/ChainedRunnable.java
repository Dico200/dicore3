package io.dico.dicore;

@FunctionalInterface
public interface ChainedRunnable extends Runnable {

    /**
     * returns a ChainedRunnable with the last added node detached.
     *
     * if this ChainedRunnable is {@link #ROOT}, {@link #ROOT} is returned.
     * The default implementation also returns {@link #ROOT}.
     *
     * @return a ChainedRunnable with the last added node detached
     */
    default ChainedRunnable withoutLastNode() {
        return ROOT;
    }

    /**
     * returns the delegate operation that was inserted as the last node
     *
     * For instance, calling this method on the result of calling {@link #andThen(Runnable)}
     * would return the given Runnable.
     *
     * @return the delegate operation that was inserted as the last node
     */
    default Runnable delegateOfLastNode() {
        // this implementation is for the purpose of lambda constructors or anonymous classes
        return this;
    }

    /**
     * @return The amount of runnables that will be run upon running this ChainedRunnable.
     */
    default int count() {
        return 1;
    }

    /**
     * @param other A new node to insert at the end of this runnable.
     * @return a new ChainedRunnable that includes the given Runnable.
     */
    default ChainedRunnable andThen(Runnable other) {
        if (other == null) {
            return this;
        }

        int count = count() + 1;
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
            public Runnable delegateOfLastNode() {
                return other;
            }

            @Override
            public int count() {
                return count;
            }
        };
    }

    /**
     * The empty ChainedRunnable. Initialize fields to this instance.
     * Expand it by setting them to the result of calling its {@link #andThen(Runnable)} method.
     */
    ChainedRunnable ROOT = new ChainedRunnable() {
        @Override
        public void run() {
            
        }
        
        @Override
        public ChainedRunnable andThen(Runnable other) {
            return ChainedRunnable.singleton(other);
        }
        
        @Override
        public int count() {
            return 0;
        }
    
        @Override
        public Runnable delegateOfLastNode() {
            return null;
        }
    };
    
    static ChainedRunnable singleton(Runnable n) {
        if (n instanceof ChainedRunnable) {
            return (ChainedRunnable) n;
        }
        if (n == null) {
            return ROOT;
        }
        return new ChainedRunnable() {
            @Override
            public void run() {
                n.run();
            }

            @Override
            public Runnable delegateOfLastNode() {
                return n;
            }
        };
    }
    
}
