package dev.skidfuscator.obfuscator.util.cfg;

import dev.skidfuscator.obfuscator.skidasm.cfg.SkidBlock;
import dev.skidfuscator.obfuscator.skidasm.expr.SkidVarExpr;
import dev.skidfuscator.obfuscator.skidasm.stmt.SkidCopyVarStmt;
import dev.skidfuscator.obfuscator.util.RandomUtil;
import lombok.experimental.UtilityClass;
import org.mapleir.ir.cfg.BasicBlock;
import org.mapleir.ir.cfg.ControlFlowGraph;
import org.mapleir.ir.code.Expr;
import org.mapleir.ir.code.Stmt;
import org.mapleir.ir.code.expr.AllocObjectExpr;
import org.mapleir.ir.code.expr.ConstantExpr;
import org.mapleir.ir.code.expr.VarExpr;
import org.mapleir.ir.code.expr.invoke.InvocationExpr;
import org.mapleir.ir.code.expr.invoke.VirtualInvocationExpr;
import org.mapleir.ir.code.stmt.PopStmt;
import org.mapleir.ir.code.stmt.ThrowStmt;
import org.mapleir.ir.locals.dynamic.DynamicLocal;
import org.objectweb.asm.Type;

import java.io.IOException;

@UtilityClass
public class Blocks {
    // A list of random exceptions
    private final Class<?>[] exceptionClasses = new Class<?>[] {
            IllegalAccessException.class,
            IOException.class,
            RuntimeException.class,
            ArrayStoreException.class
    };

    public BasicBlock exception(final ControlFlowGraph cfg) {
        return exception(cfg, null);
    }

    public BasicBlock exception(final ControlFlowGraph cfg, final String notice) {
        // Temporary fix for this
        final Type exception = Type.getType(exceptionClasses[RandomUtil.nextInt(exceptionClasses.length - 1)]);

        final BasicBlock fuckup = new SkidBlock(cfg);
        final Expr alloc_exception = new AllocObjectExpr(exception);
        final DynamicLocal local = cfg.getDynamicLocals().newLocal(exception);

        final SkidVarExpr dup_save = new SkidVarExpr(cfg, local, exception);
        final Stmt dup_stmt = new SkidCopyVarStmt(cfg, dup_save, alloc_exception, true);
        fuckup.add(dup_stmt);

        final VarExpr fuck = new VarExpr(local, exception);
        final Expr init_alloc;

        if (notice == null) {
            init_alloc = new VirtualInvocationExpr(
                    InvocationExpr.CallType.SPECIAL,
                    new Expr[]{fuck},
                    exception.getClassName().replace(".", "/"),
                    "<init>",
                    "()V"
            );
        } else {
            init_alloc = new VirtualInvocationExpr(
                    InvocationExpr.CallType.SPECIAL,
                    new Expr[]{fuck, new ConstantExpr(notice)},
                    exception.getClassName().replace(".", "/"),
                    "<init>",
                    "(Ljava/lang/String;)V"
            );
        }

        final PopStmt popStmt = new PopStmt(init_alloc);
        fuckup.add(popStmt);

        final SkidVarExpr returnFuck = new SkidVarExpr(cfg, local, exception);
        final Stmt exception_stmt = new ThrowStmt(returnFuck);
        fuckup.add(exception_stmt);

        cfg.addVertex(fuckup);

        return fuckup;
    }
}
