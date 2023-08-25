@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.identity
import arrow.core.left
import arrow.core.right

/*
  TODO: Report bug to Arrow Kt
 */

inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.Expression.Companion.dataExpression(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.Expression<Q>, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = arrow.optics.Prism(
  getOrModify = { expression: ch.kleis.lcaplugin.core.lang.expression.Expression<Q> ->
    when (expression) {
      is ch.kleis.lcaplugin.core.lang.expression.DataExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.Expression.Companion.lcaExpression(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.Expression<Q>, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = arrow.optics.Prism(
  getOrModify = { expression: ch.kleis.lcaplugin.core.lang.expression.Expression<Q> ->
    when (expression) {
      is ch.kleis.lcaplugin.core.lang.expression.LcaExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.Expression.Companion.processTemplateExpression(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.Expression<Q>, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = arrow.optics.Prism(
  getOrModify = { expression: ch.kleis.lcaplugin.core.lang.expression.Expression<Q> ->
    when (expression) {
      is ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.Expression.Companion.systemExpression(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.Expression<Q>, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = arrow.optics.Prism(
  getOrModify = { expression: ch.kleis.lcaplugin.core.lang.expression.Expression<Q> ->
    when (expression) {
      is ch.kleis.lcaplugin.core.lang.expression.SystemExpression -> expression.right()
            else -> expression.left()
    }
  },
  reverseGet = ::identity
)

inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.dataExpression(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.DataExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.dataExpression()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.lcaExpression(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.lcaExpression()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.processTemplateExpression(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.ProcessTemplateExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.processTemplateExpression()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.Expression<Q>>.systemExpression(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.SystemExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.Expression.systemExpression()
