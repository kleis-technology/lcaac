@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaac.core.lang.expression.optics

import arrow.core.identity
import arrow.core.left
import arrow.core.right
import arrow.optics.*
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.Expression
import ch.kleis.lcaac.core.lang.expression.LcaExpression
import ch.kleis.lcaac.core.lang.expression.ProcessTemplateExpression

inline fun <Q> Expression.Companion.dataExpression(): Prism<Expression<Q>, DataExpression<Q>> = Prism(
  getOrModify = { expression: Expression<Q> ->
    when (expression) {
      is DataExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)

inline fun <Q> Expression.Companion.lcaExpression(): Prism<Expression<Q>, LcaExpression<Q>> = Prism(
  getOrModify = { expression: Expression<Q> ->
    when (expression) {
      is LcaExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> Expression.Companion.processTemplateExpression(): Prism<Expression<Q>, ProcessTemplateExpression<Q>> = Prism(
  getOrModify = { expression: Expression<Q> ->
    when (expression) {
      is ProcessTemplateExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <S,Q> Iso<S, Expression<Q>>.dataExpression(): Prism<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Lens<S, Expression<Q>>.dataExpression(): Optional<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Optional<S, Expression<Q>>.dataExpression(): Optional<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Prism<S, Expression<Q>>.dataExpression(): Prism<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Setter<S, Expression<Q>>.dataExpression(): Setter<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Traversal<S, Expression<Q>>.dataExpression(): Traversal<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Fold<S, Expression<Q>>.dataExpression(): Fold<S, DataExpression<Q>> = this + Expression.dataExpression()
inline fun <S,Q> Every<S, Expression<Q>>.dataExpression(): Every<S, DataExpression<Q>> = this + Expression.dataExpression()

