package blobs.client.generate.utils.expression;

import blobs.client.generate.utils.JSForm;
import blobs.client.generate.utils.binop.*;
import blobs.client.generate.utils.expression.literal.BooleanLiteral;
import blobs.client.generate.utils.expression.literal.NumberLiteral;
import blobs.client.generate.utils.expression.literal.StringLiteral;
import blobs.client.generate.utils.term.FuncIf;
import blobs.client.generate.utils.term.FuncTermReturn;
import blobs.client.generate.utils.term.FuncTermStatement;
import blobs.client.generate.utils.term.IntoFuncTermStatement;
import blobs.client.generate.utils.unop.IntoNum;
import blobs.client.generate.utils.unop.Not;

import java.io.InputStream;

public interface Expression extends JSForm, IntoFuncTermStatement {
    InputStream inputStream(int indentation);
    @Override
    default FuncTermStatement termination() {
        return returned();
    }
    default GetProperty get(Identifier property) {
        return GetProperty.of(this, property);
    }
    default Invocation invoke() {
        return Invocation.of(this);
    }
    default Addition add(Expression operand) {
        return Addition.of(this, operand);
    }
    default Subtraction subtract(Expression operand) {
        return Subtraction.of(this, operand);
    }
    default Multiplication multiply(Expression operand) {
        return Multiplication.of(this, operand);
    }
    default Division divide(Expression operand) {
        return Division.of(this, operand);
    }
    default Equation equals(Expression operand) {
        return Equation.of(this, operand);
    }
    default LessThen lessThen(Expression operand) {
        return LessThen.of(this, operand);
    }
    default GreaterThen greaterThen(Expression operand) {
        return GreaterThen.of(this, operand);
    }
    default IntoNum intoNum() {
        return IntoNum.of(this);
    }
    default Not not() {
        return Not.of(this);
    }
    default FuncIf.ExpectThen ifStatement() {
        return FuncIf.of(this);
    }
    default FuncTermReturn returned() {
        return FuncTermReturn.of(this);
    }
    default Construction construct() {
        return Construction.of(this);
    }
    static BooleanLiteral literal(boolean bool) {
        return BooleanLiteral.of(bool);
    }
    static NumberLiteral literal(Number number) {
        return NumberLiteral.of(number);
    }
    static StringLiteral literal(String string) {
        return StringLiteral.of(string);
    }
    static Identifier identifier(String identifier) {
        return Identifier.of(identifier);
    }
}
