@file:Suppress("NOTHING_TO_INLINE")

package ch.kleis.lcaplugin.core.lang.expression.optics

import arrow.core.left
import arrow.core.right
import arrow.core.identity


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.eIndicatorSpec(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q> -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.eProcess(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.EProcess<Q> -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.eProductSpec(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q> -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.eSubstanceCharacterization(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q> -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.eSubstanceSpec(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q> -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)


inline fun <Q> ch.kleis.lcaplugin.core.lang.expression.LcaExpression.Companion.lcaExchangeExpression(): arrow.optics.Prism<ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = arrow.optics.Prism(
  getOrModify = { lcaExpression: ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q> ->
    when (lcaExpression) {
      is ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression -> lcaExpression.right()
            else -> lcaExpression.left()
    }
  },
  reverseGet = ::identity
)

inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eIndicatorSpec(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.EIndicatorSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eIndicatorSpec()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProcess(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.EProcess<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProcess()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eProductSpec(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.EProductSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eProductSpec()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceCharacterization(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceCharacterization<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceCharacterization()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.eSubstanceSpec(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.ESubstanceSpec<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.eSubstanceSpec()


inline fun <S,Q> arrow.optics.Iso<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Lens<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Optional<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Prism<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Setter<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Traversal<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Fold<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
inline fun <S,Q> arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExpression<Q>>.lcaExchangeExpression(): arrow.optics.Every<S, ch.kleis.lcaplugin.core.lang.expression.LcaExchangeExpression<Q>> = this + ch.kleis.lcaplugin.core.lang.expression.LcaExpression.lcaExchangeExpression()
