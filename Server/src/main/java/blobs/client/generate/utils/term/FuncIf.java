package blobs.client.generate.utils.term;

import blobs.client.generate.utils.expression.Expression;
import blobs.client.utils.InputStreams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FuncIf {
    public static ExpectThen of(Expression condition) {
        return new ExpectThen(condition);
    }

    public static class ExpectThen implements FuncOngoStatement {
        private final Expression condition;
        private final List<FuncOngoStatement> ongoing;
        private FuncTermStatement termination;

        private ExpectThen(Expression condition) {
            this.condition = condition;
            ongoing = new LinkedList<>();
        }

        public ExpectThen thenDo(IntoFuncOngoStatement statement) {
            if (this.termination != null) {
                throw new RuntimeException("then clause was already terminated");
            }
            this.ongoing.add(statement.statement());
            return this;
        }

        public ExpectElse thenDo(IntoFuncTermStatement termination) {
            if (this.termination != null) {
                throw new RuntimeException("then clause was already terminated");
            }
            this.termination = termination.termination();
            return new ExpectElse(condition, new FuncTermBlock(ongoing, termination.termination()));
        }

        public ExpectElseOngo elseDo(FuncOngoStatement statement) {
            return new ExpectElseOngo(condition, new FuncOngoBlock(ongoing)).elseDo(statement);
        }

        @Override
        public InputStream inputStream(int indentation) {
            LinkedList<InputStream> inputStreams = new LinkedList<>();
            inputStreams.add(new ByteArrayInputStream("if (".getBytes()));
            inputStreams.add(condition.inputStream(indentation + 1));
            inputStreams.add(InputStreams.of("\n" + indentationUnit.repeat(indentation + 1) + ") "));
            inputStreams.add((termination == null ? new FuncOngoBlock(ongoing)
                                                  : new FuncTermBlock(ongoing, termination)
                             ).inputStream(indentation));
            return new SequenceInputStream(Collections.enumeration(inputStreams));
        }
    }

    public static class ExpectElse implements FuncOngoStatement {
        private final Expression condition;
        private final FuncTermBlock thenClause;
        private final List<FuncOngoStatement> ongoing;

        public ExpectElse(Expression condition, FuncTermBlock thenClause) {
            this.condition = condition;
            this.thenClause = thenClause;
            ongoing = new LinkedList<>();
        }

        public ExpectElse elseDo(FuncOngoStatement statement) {
            ongoing.add(statement);
            return this;
        }

        public Term elseDo(IntoFuncTermStatement termination) {
            return new Term(condition, thenClause, new FuncTermBlock(ongoing, termination.termination()));
        }

        @Override
        public InputStream inputStream(int indentation) {
            return InputStreams.of(InputStreams.of("if ("),
                                   condition.inputStream(indentation + 1),
                                   InputStreams.of("\n" + indentationUnit.repeat(indentation) + ") "),
                                   thenClause.inputStream(indentation),
                                   InputStreams.of(" else "),
                                   new FuncOngoBlock(ongoing).inputStream(indentation));
        }
    }

    public static class ExpectElseOngo implements FuncOngoStatement {
        private final Expression condition;
        private final FuncOngoBlock thenClause;
        private final List<FuncOngoStatement> ongoing;
        private FuncTermStatement termination;

        private ExpectElseOngo(Expression condition, FuncOngoBlock thenClause) {
            this.condition = condition;
            this.thenClause = thenClause;
            ongoing = new LinkedList<>();
        }

        public ExpectElseOngo elseDo(FuncOngoStatement statement) {
            if (termination != null) {
                throw new RuntimeException("else clause was already terminated");
            }
            ongoing.add(statement);
            return this;
        }

        public ExpectElseOngo elseDo(IntoFuncTermStatement termination) {
            if (this.termination != null) {
                throw new RuntimeException("else clause was already terminated");
            }
            this.termination = termination.termination();
            return this;
        }

        @Override
        public InputStream inputStream(int indentation) {
            return InputStreams.of(InputStreams.of("if ("),
                                   condition.inputStream(indentation + 1),
                                   InputStreams.of("\n" + indentationUnit.repeat(indentation) + ") "),
                                   thenClause.inputStream(indentation),
                                   InputStreams.of(" else "),
                                   (termination == null ? new FuncOngoBlock(ongoing)
                                                        : new FuncTermBlock(ongoing, termination)
                                   ).inputStream(indentation));
        }
    }

    public static class Term implements FuncTermStatement {
        private final Expression condition;
        private final FuncTermBlock thenClause;
        private final FuncTermBlock elseClause;

        public Term(Expression condition, FuncTermBlock thenClause, FuncTermBlock elseClause) {
            this.condition = condition;
            this.thenClause = thenClause;
            this.elseClause = elseClause;
        }

        @Override
        public InputStream inputStream(int indentation) {
            return InputStreams.of(InputStreams.of("if ("),
                                   condition.inputStream(indentation + 1),
                                   InputStreams.of("\n" + indentationUnit.repeat(indentation) + ") "),
                                   thenClause.inputStream(indentation),
                                   InputStreams.of(" else "),
                                   elseClause.inputStream(indentation));
        }
    }
}
