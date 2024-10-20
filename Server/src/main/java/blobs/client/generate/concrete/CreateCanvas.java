package blobs.client.generate.concrete;

import blobs.client.generate.utils.expression.literal.ObjectLiteral;
import blobs.client.generate.utils.statement.ScopeStatement;
import blobs.client.generate.utils.statement.decleration.ImmutableDeclaration;
import blobs.client.generate.utils.expression.*;

import java.io.IOException;
import java.util.function.BiFunction;

import static blobs.client.generate.utils.expression.Expression.literal;

public class CreateCanvas {
    public CreateCanvas() {
        ;
    }

    public static void main(String[] args) throws IOException {
        ImmutableDeclaration canvasDeclaration =
                Expression.identifier("canvas").declareAs(Scope.create()
                                                               .addStatement(Expression.identifier("canvas").declareAs(Expression.identifier("document").get(Expression.identifier("createElement"))
                                                                                                                                 .invoke()
                                                                                                                                 .addArgument(Expression.literal("canvas"))))
                                                               .addStatement(Expression.identifier("document")
                                                                                       .get(Expression.identifier("body"))
                                                                                       .get(Expression.identifier("appendChild")).invoke()
                                                                                       .addArgument(Expression.identifier("canvas")))
                                                               .addStatement(Expression.identifier("size").mutableDeclaration())
                                                               .addStatement(Assignment.of(Expression.identifier("size"), Expression.literal(0)))
                                                               .addStatement(Expression.identifier("adjustSize").declareAs(Function.create()
                                                                                                                                   .addStatement(Assignment.of(Expression.identifier("canvas").get(Expression.identifier("width")),
                                                                                                                                                               Expression.identifier("window").get(Expression.identifier("innerWidth"))))
                                                                                                                                   .addStatement(Assignment.of(Expression.identifier("canvas").get(Expression.identifier("height")),
                                                                                                                                                               Expression.identifier("window").get(Expression.identifier("innerHeight"))))
                                                                                                                                   .addStatement(Assignment.of(Expression.identifier("size"),
                                                                                                                                                               Expression.identifier("Math").get(Expression.identifier("min")).invoke()
                                                                                                                                                                         .addArgument(Expression.identifier("canvas").get(Expression.identifier("width")))
                                                                                                                                                                         .addArgument(Expression.identifier("canvas").get(Expression.identifier("height")))))
                                                                                                                                   .conclude(Expression.identifier("udefined"))))
                                                               .addStatement(Expression.identifier("window").get(Expression.identifier("onresize")).assign(Expression.identifier("adjustSize"))
                                                                                       .statement())
                                                               .addStatement(Expression.identifier("adjustSize").invoke())
                                                               .conclude(Expression.identifier("canvas")));
        Invocation arcInvocation =
                Expression.identifier("context").get(Expression.identifier("arc"))
                          .invoke()
                          .addArgument(Expression.identifier("canvas").get(Expression.identifier("width"))
                                                 .add(Expression.identifier("x").multiply(Expression.identifier("size")))
                                                 .divide(Expression.literal(2)))
                          .addArgument(Expression.identifier("canvas").get(Expression.identifier("height"))
                                                 .add(Expression.identifier("y").multiply(Expression.identifier("size")))
                                                 .divide(Expression.literal(2)))
                          .addArgument(Expression.identifier("r").multiply(Expression.identifier("size").divide(Expression.literal(2))))
                          .addArgument(Expression.literal(0))
                          .addArgument(Expression.identifier("Math").get(Expression.identifier("PI")).multiply(Expression.literal(2)));
        ImmutableDeclaration alphaFunctionDeclaration =
                Expression.identifier("alpha").declareAs(Function.create()
                                                                 .addParameter(Expression.identifier("blob"))
                                                                 .conclude(Expression.identifier("Math").get(Expression.identifier("min"))
                                                                                     .invoke()
                                                                                     .addArgument(Expression.literal(1))
                                                                                     .addArgument(Expression.identifier("Math").get(Expression.identifier("max"))
                                                                                                            .invoke()
                                                                                                            .addArgument(Expression.literal(0.05))
                                                                                                            .addArgument(Expression.identifier("linearMap").invoke()
                                                                                                                                   .addArgument(Expression.identifier("radius").divide(Expression.literal(12)))
                                                                                                                                   .addArgument(Expression.identifier("radius"))
                                                                                                                                   .addArgument(Expression.identifier("blob").get(Expression.identifier("r")))
                                                                                                                                   .addArgument(Expression.literal(0.6))
                                                                                                                                   .addArgument(Expression.literal(0.2))))));
        BiFunction<String, Expression, Invocation> drawCircle = (whoToDraw, color) ->
                Expression.identifier("drawCircle").invoke()
                          .addArgument(Expression.identifier(whoToDraw).get(Expression.identifier("x"))
                                                 .divide(Expression.identifier("radius")))
                          .addArgument(Expression.identifier(whoToDraw).get(Expression.identifier("y"))
                                                 .divide(Expression.identifier("radius")))
                          .addArgument(Expression.identifier(whoToDraw).get(Expression.identifier("r"))
                                                 .divide(Expression.identifier("radius")))
                          .addArgument(color)
                          .addArgument(Expression.identifier("alpha").invoke().addArgument(Expression.identifier(whoToDraw)));
        ScopeStatement.create()
                      .addStatement(canvasDeclaration)
                      .addStatement(Expression.identifier("context").declareAs(Expression.identifier("canvas").get(Expression.identifier("getContext"))
                                                                                         .invoke()
                                                                                         .addArgument(Expression.literal("2d"))))
                      .addStatement(Expression.identifier("drawCircle").declareAs(Function.create()
                                                                                          .addParameter(Expression.identifier("x"))
                                                                                          .addParameter(Expression.identifier("y"))
                                                                                          .addParameter(Expression.identifier("r"))
                                                                                          .addParameter(Expression.identifier("color"))
                                                                                          .addParameter(Expression.identifier("alpha"))
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("fillStyle")).assign(Expression.identifier("color")))
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("globalAlpha")).assign(Expression.identifier("alpha")))
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("beginPath")).invoke())
                                                                                          .addStatement(arcInvocation)
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("fill")).invoke())
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("fillStyle")).assign(Expression.literal("black")))
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("beginPath")).invoke())
                                                                                          .addStatement(arcInvocation)
                                                                                          .addStatement(Expression.identifier("context").get(Expression.identifier("stroke")).invoke())
                                                                                          .conclude(Expression.identifier("undefined").returned())))
                      .addStatement(Expression.identifier("linearMap").declareAs(Function.create()
                                                                                         .addParameter(Expression.identifier("x0"))
                                                                                         .addParameter(Expression.identifier("x1"))
                                                                                         .addParameter(Expression.identifier("x"))
                                                                                         .addParameter(Expression.identifier("y0"))
                                                                                         .addParameter(Expression.identifier("y1"))
                                                                                         .addStatement(Expression.identifier("x0").equals(Expression.identifier("x1"))
                                                                                                                 .ifStatement()
                                                                                                                 .thenDo(Expression.identifier("y0")))
                                                                                         .conclude(Expression.identifier("y0").add(Expression.identifier("y1").subtract(Expression.identifier("y0"))
                                                                                                                                             .divide(Expression.identifier("x1").subtract(Expression.identifier("x0"))))
                                                                                                             .returned())))
                      .addStatement(Expression.identifier("socket").declareAs(Expression.identifier("WebSocket").construct()
                                                                                        .addArgument(Expression.literal("ws://localhost:81"))))
                      .addStatement(Expression.identifier("socket").get(Expression.identifier("onmessage"))
                                              .assign(Function.create()
                                                                        .addParameter(Expression.identifier("event"))
                                                                        .addStatement(Expression.identifier("view").declareAs(Expression.identifier("JSON").get(Expression.identifier("parse"))
                                                                                                                                        .invoke()
                                                                                                                                        .addArgument(Expression.identifier("event").get(Expression.identifier("data")))))
                                                                        .addStatement(Expression.identifier("radius").declareAs(Expression.identifier("view").get(Expression.identifier("radius"))))
                                                                        .addStatement(Expression.identifier("player").declareAs(Expression.identifier("view").get(Expression.identifier("player"))))
                                                                        .addStatement(Expression.identifier("blobs").declareAs(Expression.identifier("view").get(Expression.identifier("blobs"))))
                                                                        .addStatement(Expression.identifier("context").get(Expression.identifier("clearRect"))
                                                                                                .invoke()
                                                                                                .addArgument(Expression.literal(0))
                                                                                                .addArgument(Expression.literal(0))
                                                                                                .addArgument(Expression.identifier("canvas").get(Expression.identifier("width")))
                                                                                                .addArgument(Expression.identifier("canvas").get(Expression.identifier("height"))))
                                                                        .addStatement(alphaFunctionDeclaration)
                                                                        .addStatement(Expression.identifier("blobs").get(Expression.identifier("forEach"))
                                                                                                .invoke()
                                                                                                .addArgument(Function.create()
                                                                                                                              .addParameter(Expression.identifier("blob"))
                                                                                                                              .addStatement(drawCircle.apply("blob",
                                                                                                                                                             Scope.create()
                                                                                                                                                                  .conclude(Expression.identifier("blob").get(Expression.identifier("human"))
                                                                                                                                                                                      .ifStatement()
                                                                                                                                                                                      .thenDo(Expression.literal("red"))
                                                                                                                                                                                      .elseDo(Expression.literal("yellow")))))
                                                                                                                              .conclude(Expression.identifier("undefined"))))
                                                                        .addStatement(drawCircle.apply("player", Expression.literal("green")))
                                                                        .conclude(Expression.identifier("undefined"))))
                      .addStatement(Expression.identifier("moving").mutableDeclaration())
                      .addStatement(Expression.identifier("moving").assign(Expression.literal(false)))
                      .addStatement(Expression.identifier("sendMovement").declareAs(Function.create()
                                                                                            .addParameter(Expression.identifier("x"))
                                                                                            .addParameter(Expression.identifier("y"))
                                                                                            .addStatement(Expression.identifier("angle").declareAs(Expression.identifier("Math").get(Expression.identifier("atan"))
                                                                                                                                                             .invoke()
                                                                                                                                                             .addArgument(Expression.identifier("y").divide(Expression.identifier("x")))
                                                                                                                                                             .add(Expression.identifier("x").lessThen(Expression.literal(0))
                                                                                                                                                                            .intoNum()
                                                                                                                                                                            .multiply(Expression.identifier("Math").get(Expression.identifier("PI"))))))
                                                                                            .addStatement(Expression.identifier("d").declareAs(Expression.identifier("Math").get(Expression.identifier("sqrt"))
                                                                                                                                                         .invoke()
                                                                                                                                                         .addArgument(Expression.identifier("x").multiply(Expression.identifier("x"))
                                                                                                                                                                                .add(Expression.identifier("y").multiply(Expression.identifier("y"))))))
                                                                                            .addStatement(Expression.identifier("strength").declareAs(Expression.identifier("d").subtract(Expression.literal(0.1))
                                                                                                                                                                .divide(Expression.literal(0.5).subtract(Expression.literal(0.1)))))
                                                                                            .addStatement(Expression.identifier("socket").get(Expression.identifier("send"))
                                                                                                                    .invoke()
                                                                                                                    .addArgument(Expression.identifier("JSON").get(Expression.identifier("stringify"))
                                                                                                                                           .invoke()
                                                                                                                                           .addArgument(ObjectLiteral.create()
                                                                                                                                                                     .addEntry(Expression.identifier("angle"))
                                                                                                                                                                     .addEntry(Expression.identifier("strength")))))
                                                                                            .conclude(Expression.identifier("undefined"))))
                      .addStatement(Expression.identifier("window").get(Expression.identifier("onmousedown"))
                                              .assign(Function.create()
                                                                        .addParameter(

                                                                        )))
                      .inputStream(0)
                      .transferTo(System.out);
    }
}
