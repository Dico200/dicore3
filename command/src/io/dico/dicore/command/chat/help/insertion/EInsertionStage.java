package io.dico.dicore.command.chat.help.insertion;

import io.dico.dicore.command.ExecutionContext;
import io.dico.dicore.command.ICommandAddress;
import org.bukkit.permissions.Permissible;

public enum EInsertionStage implements IInsertionFunction {
    START {
        @Override
        public int insertionIndex(int componentCount, ICommandAddress target, Permissible viewer, ExecutionContext context) {
            return 0;
        }
    },
    CENTER {
        @Override
        public int insertionIndex(int componentCount, ICommandAddress target, Permissible viewer, ExecutionContext context) {
            return componentCount / 2;
        }
    },
    END {
        @Override
        public int insertionIndex(int componentCount, ICommandAddress target, Permissible viewer, ExecutionContext context) {
            return componentCount;
        }
    }
}
