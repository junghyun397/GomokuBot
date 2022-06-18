package utils.lang

import reactor.util.function.Tuple2
import reactor.util.function.Tuple3
import reactor.util.function.Tuple4

operator fun <T1, T2> Tuple2<T1, T2>.component1(): T1 = this.t1
operator fun <T1, T2> Tuple2<T1, T2>.component2(): T2 = this.t2

operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component1(): T1 = this.t1
operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component2(): T2 = this.t2
operator fun <T1, T2, T3> Tuple3<T1, T2, T3>.component3(): T3 = this.t3

operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component1(): T1 = this.t1
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component2(): T2 = this.t2
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component3(): T3 = this.t3
operator fun <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4>.component4(): T4 = this.t4
