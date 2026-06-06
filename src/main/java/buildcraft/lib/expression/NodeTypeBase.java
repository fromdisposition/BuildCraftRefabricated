package buildcraft.lib.expression;

import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectBooleanToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectDoubleToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectLongToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectObjectToObject;
import buildcraft.lib.expression.node.func.NodeFuncObjectToBoolean;
import buildcraft.lib.expression.node.func.NodeFuncObjectToDouble;
import buildcraft.lib.expression.node.func.NodeFuncObjectToLong;
import buildcraft.lib.expression.node.func.NodeFuncObjectToObject;
import buildcraft.lib.expression.node.func.StringFunctionBi;
import buildcraft.lib.expression.node.func.StringFunctionPenta;
import buildcraft.lib.expression.node.func.StringFunctionQuad;
import buildcraft.lib.expression.node.func.StringFunctionTri;

public abstract class NodeTypeBase<T> extends FunctionContext {
   public NodeTypeBase(String name) {
      super("Type: " + name);
   }

   protected abstract Class<T> getType();

   public NodeFuncObjectLongToLong<T> put_tl_l(String fname, NodeFuncObjectLongToLong.IFuncObjectLongToLong<T> func) {
      return this.put_ol_l(fname, this.getType(), func);
   }

   public NodeFuncObjectLongToLong<T> put_tl_l(String fname, NodeFuncObjectLongToLong.IFuncObjectLongToLong<T> func, StringFunctionTri stringFunction) {
      return this.put_ol_l(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<A, T> func
   ) {
      return this.put_oll_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectLongLongToObject<A, T> put_oll_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(
      String fname, Class<R> returnType, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<T, R> func
   ) {
      return this.put_oll_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectLongLongToObject<T, R> put_tll_o(
      String fname, Class<R> returnType, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectLongLongToObject<T, T> put_tll_t(String fname, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<T, T> func) {
      return this.put_oll_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectLongLongToObject<T, T> put_tll_t(
      String fname, NodeFuncObjectLongLongToObject.IFuncObjectLongLongToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectLongLongToLong<T> put_tll_l(String fname, NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong<T> func) {
      return this.put_oll_l(fname, this.getType(), func);
   }

   public NodeFuncObjectLongLongToLong<T> put_tll_l(
      String fname, NodeFuncObjectLongLongToLong.IFuncObjectLongLongToLong<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_l(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleToLong<T> put_td_l(String fname, NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong<T> func) {
      return this.put_od_l(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleToLong<T> put_td_l(String fname, NodeFuncObjectDoubleToLong.IFuncObjectDoubleToLong<T> func, StringFunctionTri stringFunction) {
      return this.put_od_l(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectLongDoubleToObject<A, T> put_old_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<A, T> func
   ) {
      return this.put_old_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectLongDoubleToObject<A, T> put_old_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_old_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectLongDoubleToObject<T, R> put_tld_o(
      String fname, Class<R> returnType, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<T, R> func
   ) {
      return this.put_old_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectLongDoubleToObject<T, R> put_tld_o(
      String fname, Class<R> returnType, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_old_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectLongDoubleToObject<T, T> put_tld_t(String fname, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<T, T> func) {
      return this.put_old_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectLongDoubleToObject<T, T> put_tld_t(
      String fname, NodeFuncObjectLongDoubleToObject.IFuncObjectLongDoubleToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_old_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleDoubleToLong<T> put_tdd_l(String fname, NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong<T> func) {
      return this.put_odd_l(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleDoubleToLong<T> put_tdd_l(
      String fname, NodeFuncObjectDoubleDoubleToLong.IFuncObjectDoubleDoubleToLong<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_l(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanToLong<T> put_tb_l(String fname, NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong<T> func) {
      return this.put_ob_l(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanToLong<T> put_tb_l(String fname, NodeFuncObjectBooleanToLong.IFuncObjectBooleanToLong<T> func, StringFunctionTri stringFunction) {
      return this.put_ob_l(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectLongBooleanToObject<A, T> put_olb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<A, T> func
   ) {
      return this.put_olb_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectLongBooleanToObject<A, T> put_olb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olb_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectLongBooleanToObject<T, R> put_tlb_o(
      String fname, Class<R> returnType, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<T, R> func
   ) {
      return this.put_olb_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectLongBooleanToObject<T, R> put_tlb_o(
      String fname, Class<R> returnType, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olb_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectLongBooleanToObject<T, T> put_tlb_t(String fname, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<T, T> func) {
      return this.put_olb_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectLongBooleanToObject<T, T> put_tlb_t(
      String fname, NodeFuncObjectLongBooleanToObject.IFuncObjectLongBooleanToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olb_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanBooleanToLong<T> put_tbb_l(String fname, NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong<T> func) {
      return this.put_obb_l(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanBooleanToLong<T> put_tbb_l(
      String fname, NodeFuncObjectBooleanBooleanToLong.IFuncObjectBooleanBooleanToLong<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_l(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectToLong<T> put_t_l(String fname, NodeFuncObjectToLong.IFuncObjectToLong<T> func) {
      return this.put_o_l(fname, this.getType(), func);
   }

   public NodeFuncObjectToLong<T> put_t_l(String fname, NodeFuncObjectToLong.IFuncObjectToLong<T> func, StringFunctionBi stringFunction) {
      return this.put_o_l(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(String fname, Class<A> argTypeA, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<A, T> func) {
      return this.put_oo_l(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectObjectToLong<A, T> put_to_l(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_l(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(String fname, Class<B> argTypeB, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<T, B> func) {
      return this.put_oo_l(fname, this.getType(), argTypeB, func);
   }

   public <B> NodeFuncObjectObjectToLong<T, B> put_ot_l(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<T, B> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_l(fname, this.getType(), argTypeB, func, stringFunction);
   }

   public NodeFuncObjectObjectToLong<T, T> put_tt_l(String fname, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<T, T> func) {
      return this.put_oo_l(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectToLong<T, T> put_tt_l(
      String fname, NodeFuncObjectObjectToLong.IFuncObjectObjectToLong<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_l(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectToLong<A, B, T> put_too_l(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, B, T> func
   ) {
      return this.put_ooo_l(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectToLong<A, B, T> put_too_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectToLong<T, B, C> put_oto_l(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, B, C> func
   ) {
      return this.put_ooo_l(fname, this.getType(), argTypeB, argTypeC, func);
   }

   public <B, C> NodeFuncObjectObjectObjectToLong<T, B, C> put_oto_l(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, B, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, this.getType(), argTypeB, argTypeC, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectToLong<T, B, T> put_tto_l(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, B, T> func
   ) {
      return this.put_ooo_l(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectToLong<T, B, T> put_tto_l(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectToLong<A, T, C> put_oot_l(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, T, C> func
   ) {
      return this.put_ooo_l(fname, argTypeA, this.getType(), argTypeC, func);
   }

   public <A, C> NodeFuncObjectObjectObjectToLong<A, T, C> put_oot_l(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, T, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, argTypeA, this.getType(), argTypeC, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectToLong<A, T, T> put_tot_l(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, T, T> func
   ) {
      return this.put_ooo_l(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectToLong<A, T, T> put_tot_l(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectToLong<T, T, C> put_ott_l(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, T, C> func
   ) {
      return this.put_ooo_l(fname, this.getType(), this.getType(), argTypeC, func);
   }

   public <C> NodeFuncObjectObjectObjectToLong<T, T, C> put_ott_l(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, T, C> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, this.getType(), this.getType(), argTypeC, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectToLong<T, T, T> put_ttt_l(String fname, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, T, T> func) {
      return this.put_ooo_l(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectToLong<T, T, T> put_ttt_l(
      String fname, NodeFuncObjectObjectObjectToLong.IFuncObjectObjectObjectToLong<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_l(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToLong<A, B, C, T> put_tooo_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, C, T> func
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, argTypeC, this.getType(), func);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToLong<A, B, C, T> put_tooo_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToLong<T, B, C, D> put_otoo_l(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, C, D> func
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, argTypeC, argTypeD, func);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToLong<T, B, C, D> put_otoo_l(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToLong<T, B, C, T> put_ttoo_l(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, C, T> func
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, argTypeC, this.getType(), func);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToLong<T, B, C, T> put_ttoo_l(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToLong<A, T, C, D> put_ooto_l(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, C, D> func
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), argTypeC, argTypeD, func);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToLong<A, T, C, D> put_ooto_l(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToLong<A, T, C, T> put_toto_l(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, C, T> func
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToLong<A, T, C, T> put_toto_l(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToLong<T, T, C, D> put_otto_l(
      String fname, Class<C> argTypeC, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, C, D> func
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), argTypeC, argTypeD, func);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToLong<T, T, C, D> put_otto_l(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectObjectToLong<T, T, C, T> put_ttto_l(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, C, T> func
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectObjectObjectObjectToLong<T, T, C, T> put_ttto_l(
      String fname,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToLong<A, B, T, D> put_ooot_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, T, D> func
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, this.getType(), argTypeD, func);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToLong<A, B, T, D> put_ooot_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToLong<A, B, T, T> put_toot_l(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, T, T> func
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, this.getType(), this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToLong<A, B, T, T> put_toot_l(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToLong<T, B, T, D> put_otot_l(
      String fname, Class<B> argTypeB, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, T, D> func
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, this.getType(), argTypeD, func);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToLong<T, B, T, D> put_otot_l(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectObjectToLong<T, B, T, T> put_ttot_l(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, T, T> func
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, this.getType(), this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectObjectToLong<T, B, T, T> put_ttot_l(
      String fname,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToLong<A, T, T, D> put_oott_l(
      String fname, Class<A> argTypeA, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, T, D> func
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), this.getType(), argTypeD, func);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToLong<A, T, T, D> put_oott_l(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectObjectToLong<A, T, T, T> put_tott_l(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, T, T> func
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectObjectToLong<A, T, T, T> put_tott_l(
      String fname,
      Class<A> argTypeA,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<A, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, argTypeA, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <D> NodeFuncObjectObjectObjectObjectToLong<T, T, T, D> put_ottt_l(
      String fname, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, T, D> func
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), this.getType(), argTypeD, func);
   }

   public <D> NodeFuncObjectObjectObjectObjectToLong<T, T, T, D> put_ottt_l(
      String fname,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectObjectToLong<T, T, T, T> put_tttt_l(
      String fname, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, T, T> func
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectObjectToLong<T, T, T, T> put_tttt_l(
      String fname, NodeFuncObjectObjectObjectObjectToLong.IFuncObjectObjectObjectObjectToLong<T, T, T, T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_l(fname, this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectLongObjectToObject<A, C, T> put_olo_t(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, C, T> func
   ) {
      return this.put_olo_o(fname, argTypeA, argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectLongObjectToObject<A, C, T> put_olo_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, argTypeA, argTypeC, this.getType(), func, stringFunction);
   }

   public <C, R> NodeFuncObjectLongObjectToObject<T, C, R> put_tlo_o(
      String fname, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, C, R> func
   ) {
      return this.put_olo_o(fname, this.getType(), argTypeC, returnType, func);
   }

   public <C, R> NodeFuncObjectLongObjectToObject<T, C, R> put_tlo_o(
      String fname,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, this.getType(), argTypeC, returnType, func, stringFunction);
   }

   public <C> NodeFuncObjectLongObjectToObject<T, C, T> put_tlo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, C, T> func
   ) {
      return this.put_olo_o(fname, this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectLongObjectToObject<T, C, T> put_tlo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, C, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectLongObjectToObject<A, T, R> put_olt_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, T, R> func
   ) {
      return this.put_olo_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectLongObjectToObject<A, T, R> put_olt_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectLongObjectToObject<A, T, T> put_olt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, T, T> func
   ) {
      return this.put_olo_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectLongObjectToObject<A, T, T> put_olt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectLongObjectToObject<T, T, R> put_tlt_o(
      String fname, Class<R> returnType, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, T, R> func
   ) {
      return this.put_olo_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectLongObjectToObject<T, T, R> put_tlt_o(
      String fname, Class<R> returnType, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectLongObjectToObject<T, T, T> put_tlt_t(String fname, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, T, T> func) {
      return this.put_olo_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectLongObjectToObject<T, T, T> put_tlt_t(
      String fname, NodeFuncObjectLongObjectToObject.IFuncObjectLongObjectToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_olo_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectLongToDouble<T> put_tl_d(String fname, NodeFuncObjectLongToDouble.IFuncObjectLongToDouble<T> func) {
      return this.put_ol_d(fname, this.getType(), func);
   }

   public NodeFuncObjectLongToDouble<T> put_tl_d(String fname, NodeFuncObjectLongToDouble.IFuncObjectLongToDouble<T> func, StringFunctionTri stringFunction) {
      return this.put_ol_d(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectDoubleLongToObject<A, T> put_odl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<A, T> func
   ) {
      return this.put_odl_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectDoubleLongToObject<A, T> put_odl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odl_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectDoubleLongToObject<T, R> put_tdl_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<T, R> func
   ) {
      return this.put_odl_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectDoubleLongToObject<T, R> put_tdl_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odl_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectDoubleLongToObject<T, T> put_tdl_t(String fname, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<T, T> func) {
      return this.put_odl_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectDoubleLongToObject<T, T> put_tdl_t(
      String fname, NodeFuncObjectDoubleLongToObject.IFuncObjectDoubleLongToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odl_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectLongLongToDouble<T> put_tll_d(String fname, NodeFuncObjectLongLongToDouble.IFuncObjectLongLongToDouble<T> func) {
      return this.put_oll_d(fname, this.getType(), func);
   }

   public NodeFuncObjectLongLongToDouble<T> put_tll_d(
      String fname, NodeFuncObjectLongLongToDouble.IFuncObjectLongLongToDouble<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_d(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleToDouble<T> put_td_d(String fname, NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble<T> func) {
      return this.put_od_d(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleToDouble<T> put_td_d(
      String fname, NodeFuncObjectDoubleToDouble.IFuncObjectDoubleToDouble<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_od_d(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectDoubleDoubleToObject<A, T> put_odd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<A, T> func
   ) {
      return this.put_odd_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectDoubleDoubleToObject<A, T> put_odd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectDoubleDoubleToObject<T, R> put_tdd_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<T, R> func
   ) {
      return this.put_odd_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectDoubleDoubleToObject<T, R> put_tdd_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectDoubleDoubleToObject<T, T> put_tdd_t(String fname, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<T, T> func) {
      return this.put_odd_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectDoubleDoubleToObject<T, T> put_tdd_t(
      String fname, NodeFuncObjectDoubleDoubleToObject.IFuncObjectDoubleDoubleToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleDoubleToDouble<T> put_tdd_d(String fname, NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble<T> func) {
      return this.put_odd_d(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleDoubleToDouble<T> put_tdd_d(
      String fname, NodeFuncObjectDoubleDoubleToDouble.IFuncObjectDoubleDoubleToDouble<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_d(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanToDouble<T> put_tb_d(String fname, NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble<T> func) {
      return this.put_ob_d(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanToDouble<T> put_tb_d(
      String fname, NodeFuncObjectBooleanToDouble.IFuncObjectBooleanToDouble<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ob_d(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectDoubleBooleanToObject<A, T> put_odb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<A, T> func
   ) {
      return this.put_odb_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectDoubleBooleanToObject<A, T> put_odb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odb_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectDoubleBooleanToObject<T, R> put_tdb_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<T, R> func
   ) {
      return this.put_odb_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectDoubleBooleanToObject<T, R> put_tdb_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odb_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectDoubleBooleanToObject<T, T> put_tdb_t(String fname, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<T, T> func) {
      return this.put_odb_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectDoubleBooleanToObject<T, T> put_tdb_t(
      String fname, NodeFuncObjectDoubleBooleanToObject.IFuncObjectDoubleBooleanToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odb_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanBooleanToDouble<T> put_tbb_d(String fname, NodeFuncObjectBooleanBooleanToDouble.IFuncObjectBooleanBooleanToDouble<T> func) {
      return this.put_obb_d(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanBooleanToDouble<T> put_tbb_d(
      String fname, NodeFuncObjectBooleanBooleanToDouble.IFuncObjectBooleanBooleanToDouble<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_d(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectToDouble<T> put_t_d(String fname, NodeFuncObjectToDouble.IFuncObjectToDouble<T> func) {
      return this.put_o_d(fname, this.getType(), func);
   }

   public NodeFuncObjectToDouble<T> put_t_d(String fname, NodeFuncObjectToDouble.IFuncObjectToDouble<T> func, StringFunctionBi stringFunction) {
      return this.put_o_d(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(String fname, Class<A> argTypeA, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<A, T> func) {
      return this.put_oo_d(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectObjectToDouble<A, T> put_to_d(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_d(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(String fname, Class<B> argTypeB, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<T, B> func) {
      return this.put_oo_d(fname, this.getType(), argTypeB, func);
   }

   public <B> NodeFuncObjectObjectToDouble<T, B> put_ot_d(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<T, B> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_d(fname, this.getType(), argTypeB, func, stringFunction);
   }

   public NodeFuncObjectObjectToDouble<T, T> put_tt_d(String fname, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<T, T> func) {
      return this.put_oo_d(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectToDouble<T, T> put_tt_d(
      String fname, NodeFuncObjectObjectToDouble.IFuncObjectObjectToDouble<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_d(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectToDouble<A, B, T> put_too_d(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, B, T> func
   ) {
      return this.put_ooo_d(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectToDouble<A, B, T> put_too_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectToDouble<T, B, C> put_oto_d(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, B, C> func
   ) {
      return this.put_ooo_d(fname, this.getType(), argTypeB, argTypeC, func);
   }

   public <B, C> NodeFuncObjectObjectObjectToDouble<T, B, C> put_oto_d(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, B, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, this.getType(), argTypeB, argTypeC, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectToDouble<T, B, T> put_tto_d(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, B, T> func
   ) {
      return this.put_ooo_d(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectToDouble<T, B, T> put_tto_d(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectToDouble<A, T, C> put_oot_d(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, T, C> func
   ) {
      return this.put_ooo_d(fname, argTypeA, this.getType(), argTypeC, func);
   }

   public <A, C> NodeFuncObjectObjectObjectToDouble<A, T, C> put_oot_d(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, T, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, argTypeA, this.getType(), argTypeC, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectToDouble<A, T, T> put_tot_d(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, T, T> func
   ) {
      return this.put_ooo_d(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectToDouble<A, T, T> put_tot_d(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectToDouble<T, T, C> put_ott_d(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, T, C> func
   ) {
      return this.put_ooo_d(fname, this.getType(), this.getType(), argTypeC, func);
   }

   public <C> NodeFuncObjectObjectObjectToDouble<T, T, C> put_ott_d(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, T, C> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, this.getType(), this.getType(), argTypeC, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectToDouble<T, T, T> put_ttt_d(String fname, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, T, T> func) {
      return this.put_ooo_d(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectToDouble<T, T, T> put_ttt_d(
      String fname, NodeFuncObjectObjectObjectToDouble.IFuncObjectObjectObjectToDouble<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_d(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, T> put_tooo_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, C, T> func
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, argTypeC, this.getType(), func);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToDouble<A, B, C, T> put_tooo_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, D> put_otoo_d(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, C, D> func
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, argTypeC, argTypeD, func);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, D> put_otoo_d(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, T> put_ttoo_d(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, C, T> func
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, argTypeC, this.getType(), func);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToDouble<T, B, C, T> put_ttoo_d(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, D> put_ooto_d(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, C, D> func
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), argTypeC, argTypeD, func);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, D> put_ooto_d(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, T> put_toto_d(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, C, T> func
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToDouble<A, T, C, T> put_toto_d(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, D> put_otto_d(
      String fname, Class<C> argTypeC, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, C, D> func
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), argTypeC, argTypeD, func);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, D> put_otto_d(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, T> put_ttto_d(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, C, T> func
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectObjectObjectObjectToDouble<T, T, C, T> put_ttto_d(
      String fname,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, D> put_ooot_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, T, D> func
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, this.getType(), argTypeD, func);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, D> put_ooot_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, T> put_toot_d(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, T, T> func
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, this.getType(), this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToDouble<A, B, T, T> put_toot_d(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, D> put_otot_d(
      String fname, Class<B> argTypeB, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, T, D> func
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, this.getType(), argTypeD, func);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, D> put_otot_d(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, T> put_ttot_d(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, T, T> func
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, this.getType(), this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectObjectToDouble<T, B, T, T> put_ttot_d(
      String fname,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, D> put_oott_d(
      String fname, Class<A> argTypeA, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, T, D> func
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), this.getType(), argTypeD, func);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, D> put_oott_d(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, T> put_tott_d(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, T, T> func
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectObjectToDouble<A, T, T, T> put_tott_d(
      String fname,
      Class<A> argTypeA,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<A, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, argTypeA, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <D> NodeFuncObjectObjectObjectObjectToDouble<T, T, T, D> put_ottt_d(
      String fname, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, T, D> func
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), this.getType(), argTypeD, func);
   }

   public <D> NodeFuncObjectObjectObjectObjectToDouble<T, T, T, D> put_ottt_d(
      String fname,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectObjectToDouble<T, T, T, T> put_tttt_d(
      String fname, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, T, T> func
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectObjectToDouble<T, T, T, T> put_tttt_d(
      String fname, NodeFuncObjectObjectObjectObjectToDouble.IFuncObjectObjectObjectObjectToDouble<T, T, T, T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_d(fname, this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectDoubleObjectToObject<A, C, T> put_odo_t(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, C, T> func
   ) {
      return this.put_odo_o(fname, argTypeA, argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectDoubleObjectToObject<A, C, T> put_odo_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, argTypeA, argTypeC, this.getType(), func, stringFunction);
   }

   public <C, R> NodeFuncObjectDoubleObjectToObject<T, C, R> put_tdo_o(
      String fname, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, C, R> func
   ) {
      return this.put_odo_o(fname, this.getType(), argTypeC, returnType, func);
   }

   public <C, R> NodeFuncObjectDoubleObjectToObject<T, C, R> put_tdo_o(
      String fname,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, this.getType(), argTypeC, returnType, func, stringFunction);
   }

   public <C> NodeFuncObjectDoubleObjectToObject<T, C, T> put_tdo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, C, T> func
   ) {
      return this.put_odo_o(fname, this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectDoubleObjectToObject<T, C, T> put_tdo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, C, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectDoubleObjectToObject<A, T, R> put_odt_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, T, R> func
   ) {
      return this.put_odo_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectDoubleObjectToObject<A, T, R> put_odt_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectDoubleObjectToObject<A, T, T> put_odt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, T, T> func
   ) {
      return this.put_odo_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectDoubleObjectToObject<A, T, T> put_odt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectDoubleObjectToObject<T, T, R> put_tdt_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, T, R> func
   ) {
      return this.put_odo_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectDoubleObjectToObject<T, T, R> put_tdt_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectDoubleObjectToObject<T, T, T> put_tdt_t(String fname, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, T, T> func) {
      return this.put_odo_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectDoubleObjectToObject<T, T, T> put_tdt_t(
      String fname, NodeFuncObjectDoubleObjectToObject.IFuncObjectDoubleObjectToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odo_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectLongToBoolean<T> put_tl_b(String fname, NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean<T> func) {
      return this.put_ol_b(fname, this.getType(), func);
   }

   public NodeFuncObjectLongToBoolean<T> put_tl_b(String fname, NodeFuncObjectLongToBoolean.IFuncObjectLongToBoolean<T> func, StringFunctionTri stringFunction) {
      return this.put_ol_b(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectBooleanLongToObject<A, T> put_obl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<A, T> func
   ) {
      return this.put_obl_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectBooleanLongToObject<A, T> put_obl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obl_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectBooleanLongToObject<T, R> put_tbl_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<T, R> func
   ) {
      return this.put_obl_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectBooleanLongToObject<T, R> put_tbl_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obl_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectBooleanLongToObject<T, T> put_tbl_t(String fname, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<T, T> func) {
      return this.put_obl_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectBooleanLongToObject<T, T> put_tbl_t(
      String fname, NodeFuncObjectBooleanLongToObject.IFuncObjectBooleanLongToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obl_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectLongLongToBoolean<T> put_tll_b(String fname, NodeFuncObjectLongLongToBoolean.IFuncObjectLongLongToBoolean<T> func) {
      return this.put_oll_b(fname, this.getType(), func);
   }

   public NodeFuncObjectLongLongToBoolean<T> put_tll_b(
      String fname, NodeFuncObjectLongLongToBoolean.IFuncObjectLongLongToBoolean<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oll_b(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleToBoolean<T> put_td_b(String fname, NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean<T> func) {
      return this.put_od_b(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleToBoolean<T> put_td_b(
      String fname, NodeFuncObjectDoubleToBoolean.IFuncObjectDoubleToBoolean<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_od_b(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectBooleanDoubleToObject<A, T> put_obd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<A, T> func
   ) {
      return this.put_obd_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectBooleanDoubleToObject<A, T> put_obd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obd_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectBooleanDoubleToObject<T, R> put_tbd_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<T, R> func
   ) {
      return this.put_obd_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectBooleanDoubleToObject<T, R> put_tbd_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obd_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectBooleanDoubleToObject<T, T> put_tbd_t(String fname, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<T, T> func) {
      return this.put_obd_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectBooleanDoubleToObject<T, T> put_tbd_t(
      String fname, NodeFuncObjectBooleanDoubleToObject.IFuncObjectBooleanDoubleToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obd_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectDoubleDoubleToBoolean<T> put_tdd_b(String fname, NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean<T> func) {
      return this.put_odd_b(fname, this.getType(), func);
   }

   public NodeFuncObjectDoubleDoubleToBoolean<T> put_tdd_b(
      String fname, NodeFuncObjectDoubleDoubleToBoolean.IFuncObjectDoubleDoubleToBoolean<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_odd_b(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanToBoolean<T> put_tb_b(String fname, NodeFuncObjectBooleanToBoolean.IFuncObjectBooleanToBoolean<T> func) {
      return this.put_ob_b(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanToBoolean<T> put_tb_b(
      String fname, NodeFuncObjectBooleanToBoolean.IFuncObjectBooleanToBoolean<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ob_b(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectBooleanBooleanToObject<A, T> put_obb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<A, T> func
   ) {
      return this.put_obb_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectBooleanBooleanToObject<A, T> put_obb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<A, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectBooleanBooleanToObject<T, R> put_tbb_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<T, R> func
   ) {
      return this.put_obb_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectBooleanBooleanToObject<T, R> put_tbb_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectBooleanBooleanToObject<T, T> put_tbb_t(String fname, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<T, T> func) {
      return this.put_obb_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectBooleanBooleanToObject<T, T> put_tbb_t(
      String fname, NodeFuncObjectBooleanBooleanToObject.IFuncObjectBooleanBooleanToObject<T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncObjectBooleanBooleanToBoolean<T> put_tbb_b(String fname, NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean<T> func) {
      return this.put_obb_b(fname, this.getType(), func);
   }

   public NodeFuncObjectBooleanBooleanToBoolean<T> put_tbb_b(
      String fname, NodeFuncObjectBooleanBooleanToBoolean.IFuncObjectBooleanBooleanToBoolean<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obb_b(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncObjectToBoolean<T> put_t_b(String fname, NodeFuncObjectToBoolean.IFuncObjectToBoolean<T> func) {
      return this.put_o_b(fname, this.getType(), func);
   }

   public NodeFuncObjectToBoolean<T> put_t_b(String fname, NodeFuncObjectToBoolean.IFuncObjectToBoolean<T> func, StringFunctionBi stringFunction) {
      return this.put_o_b(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(String fname, Class<A> argTypeA, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<A, T> func) {
      return this.put_oo_b(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectObjectToBoolean<A, T> put_to_b(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_b(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(String fname, Class<B> argTypeB, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<T, B> func) {
      return this.put_oo_b(fname, this.getType(), argTypeB, func);
   }

   public <B> NodeFuncObjectObjectToBoolean<T, B> put_ot_b(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<T, B> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_b(fname, this.getType(), argTypeB, func, stringFunction);
   }

   public NodeFuncObjectObjectToBoolean<T, T> put_tt_b(String fname, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<T, T> func) {
      return this.put_oo_b(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectToBoolean<T, T> put_tt_b(
      String fname, NodeFuncObjectObjectToBoolean.IFuncObjectObjectToBoolean<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_b(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectToBoolean<A, B, T> put_too_b(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, B, T> func
   ) {
      return this.put_ooo_b(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectToBoolean<A, B, T> put_too_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectToBoolean<T, B, C> put_oto_b(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, B, C> func
   ) {
      return this.put_ooo_b(fname, this.getType(), argTypeB, argTypeC, func);
   }

   public <B, C> NodeFuncObjectObjectObjectToBoolean<T, B, C> put_oto_b(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, B, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, this.getType(), argTypeB, argTypeC, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectToBoolean<T, B, T> put_tto_b(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, B, T> func
   ) {
      return this.put_ooo_b(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectToBoolean<T, B, T> put_tto_b(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectToBoolean<A, T, C> put_oot_b(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, T, C> func
   ) {
      return this.put_ooo_b(fname, argTypeA, this.getType(), argTypeC, func);
   }

   public <A, C> NodeFuncObjectObjectObjectToBoolean<A, T, C> put_oot_b(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, T, C> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, argTypeA, this.getType(), argTypeC, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectToBoolean<A, T, T> put_tot_b(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, T, T> func
   ) {
      return this.put_ooo_b(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectToBoolean<A, T, T> put_tot_b(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectToBoolean<T, T, C> put_ott_b(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, T, C> func
   ) {
      return this.put_ooo_b(fname, this.getType(), this.getType(), argTypeC, func);
   }

   public <C> NodeFuncObjectObjectObjectToBoolean<T, T, C> put_ott_b(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, T, C> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, this.getType(), this.getType(), argTypeC, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectToBoolean<T, T, T> put_ttt_b(
      String fname, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, T, T> func
   ) {
      return this.put_ooo_b(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectToBoolean<T, T, T> put_ttt_b(
      String fname, NodeFuncObjectObjectObjectToBoolean.IFuncObjectObjectObjectToBoolean<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_b(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, T> put_tooo_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, C, T> func
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, argTypeC, this.getType(), func);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToBoolean<A, B, C, T> put_tooo_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, D> put_otoo_b(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, C, D> func
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, argTypeC, argTypeD, func);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, D> put_otoo_b(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, argTypeC, argTypeD, func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, T> put_ttoo_b(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, C, T> func
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, argTypeC, this.getType(), func);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToBoolean<T, B, C, T> put_ttoo_b(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, D> put_ooto_b(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, C, D> func
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), argTypeC, argTypeD, func);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, D> put_ooto_b(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, T> put_toto_b(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, C, T> func
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToBoolean<A, T, C, T> put_toto_b(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, D> put_otto_b(
      String fname, Class<C> argTypeC, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, C, D> func
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), argTypeC, argTypeD, func);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, D> put_otto_b(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, C, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), argTypeC, argTypeD, func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, T> put_ttto_b(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, C, T> func
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectObjectObjectObjectToBoolean<T, T, C, T> put_ttto_b(
      String fname,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, C, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, D> put_ooot_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, T, D> func
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, this.getType(), argTypeD, func);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, D> put_ooot_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, T> put_toot_b(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, T, T> func
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, this.getType(), this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToBoolean<A, B, T, T> put_toot_b(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, D> put_otot_b(
      String fname, Class<B> argTypeB, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, T, D> func
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, this.getType(), argTypeD, func);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, D> put_otot_b(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, this.getType(), argTypeD, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, T> put_ttot_b(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, T, T> func
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, this.getType(), this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectObjectToBoolean<T, B, T, T> put_ttot_b(
      String fname,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, B, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, D> put_oott_b(
      String fname, Class<A> argTypeA, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, T, D> func
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), this.getType(), argTypeD, func);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, D> put_oott_b(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, T> put_tott_b(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, T, T> func
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectObjectToBoolean<A, T, T, T> put_tott_b(
      String fname,
      Class<A> argTypeA,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<A, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, argTypeA, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, D> put_ottt_b(
      String fname, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, T, D> func
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), this.getType(), argTypeD, func);
   }

   public <D> NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, D> put_ottt_b(
      String fname,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, T, D> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), this.getType(), argTypeD, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, T> put_tttt_b(
      String fname, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, T, T> func
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectObjectToBoolean<T, T, T, T> put_tttt_b(
      String fname, NodeFuncObjectObjectObjectObjectToBoolean.IFuncObjectObjectObjectObjectToBoolean<T, T, T, T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_b(fname, this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, C> NodeFuncObjectBooleanObjectToObject<A, C, T> put_obo_t(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, C, T> func
   ) {
      return this.put_obo_o(fname, argTypeA, argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectBooleanObjectToObject<A, C, T> put_obo_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, argTypeA, argTypeC, this.getType(), func, stringFunction);
   }

   public <C, R> NodeFuncObjectBooleanObjectToObject<T, C, R> put_tbo_o(
      String fname, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, C, R> func
   ) {
      return this.put_obo_o(fname, this.getType(), argTypeC, returnType, func);
   }

   public <C, R> NodeFuncObjectBooleanObjectToObject<T, C, R> put_tbo_o(
      String fname,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, this.getType(), argTypeC, returnType, func, stringFunction);
   }

   public <C> NodeFuncObjectBooleanObjectToObject<T, C, T> put_tbo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, C, T> func
   ) {
      return this.put_obo_o(fname, this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectBooleanObjectToObject<T, C, T> put_tbo_t(
      String fname, Class<C> argTypeC, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, C, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectBooleanObjectToObject<A, T, R> put_obt_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, T, R> func
   ) {
      return this.put_obo_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectBooleanObjectToObject<A, T, R> put_obt_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectBooleanObjectToObject<A, T, T> put_obt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, T, T> func
   ) {
      return this.put_obo_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectBooleanObjectToObject<A, T, T> put_obt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectBooleanObjectToObject<T, T, R> put_tbt_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, T, R> func
   ) {
      return this.put_obo_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectBooleanObjectToObject<T, T, R> put_tbt_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectBooleanObjectToObject<T, T, T> put_tbt_t(
      String fname, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, T, T> func
   ) {
      return this.put_obo_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectBooleanObjectToObject<T, T, T> put_tbt_t(
      String fname, NodeFuncObjectBooleanObjectToObject.IFuncObjectBooleanObjectToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_obo_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncLongToObject<T> put_l_t(String fname, NodeFuncLongToObject.IFuncLongToObject<T> func) {
      return this.put_l_o(fname, this.getType(), func);
   }

   public NodeFuncLongToObject<T> put_l_t(String fname, NodeFuncLongToObject.IFuncLongToObject<T> func, StringFunctionBi stringFunction) {
      return this.put_l_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncLongLongToObject<T> put_ll_t(String fname, NodeFuncLongLongToObject.IFuncLongLongToObject<T> func) {
      return this.put_ll_o(fname, this.getType(), func);
   }

   public NodeFuncLongLongToObject<T> put_ll_t(String fname, NodeFuncLongLongToObject.IFuncLongLongToObject<T> func, StringFunctionTri stringFunction) {
      return this.put_ll_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncLongLongLongToObject<T> put_lll_t(String fname, NodeFuncLongLongLongToObject.IFuncLongLongLongToObject<T> func) {
      return this.put_lll_o(fname, this.getType(), func);
   }

   public NodeFuncLongLongLongToObject<T> put_lll_t(
      String fname, NodeFuncLongLongLongToObject.IFuncLongLongLongToObject<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_lll_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncLongLongLongLongToObject<T> put_llll_t(String fname, NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject<T> func) {
      return this.put_llll_o(fname, this.getType(), func);
   }

   public NodeFuncLongLongLongLongToObject<T> put_llll_t(
      String fname, NodeFuncLongLongLongLongToObject.IFuncLongLongLongLongToObject<T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_llll_o(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(String fname, Class<A> argTypeA, NodeFuncObjectLongToObject.IFuncObjectLongToObject<A, T> func) {
      return this.put_ol_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectLongToObject<A, T> put_ol_t(
      String fname, Class<A> argTypeA, NodeFuncObjectLongToObject.IFuncObjectLongToObject<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ol_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(String fname, Class<R> returnType, NodeFuncObjectLongToObject.IFuncObjectLongToObject<T, R> func) {
      return this.put_ol_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectLongToObject<T, R> put_tl_o(
      String fname, Class<R> returnType, NodeFuncObjectLongToObject.IFuncObjectLongToObject<T, R> func, StringFunctionTri stringFunction
   ) {
      return this.put_ol_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectLongToObject<T, T> put_tl_t(String fname, NodeFuncObjectLongToObject.IFuncObjectLongToObject<T, T> func) {
      return this.put_ol_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectLongToObject<T, T> put_tl_t(
      String fname, NodeFuncObjectLongToObject.IFuncObjectLongToObject<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ol_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectLongToObject<A, B, T> put_ool_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, B, T> func
   ) {
      return this.put_ool_o(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectLongToObject<A, B, T> put_ool_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectLongToObject<T, B, R> put_tol_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, B, R> func
   ) {
      return this.put_ool_o(fname, this.getType(), argTypeB, returnType, func);
   }

   public <B, R> NodeFuncObjectObjectLongToObject<T, B, R> put_tol_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, B, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, this.getType(), argTypeB, returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectLongToObject<T, B, T> put_tol_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, B, T> func
   ) {
      return this.put_ool_o(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectLongToObject<T, B, T> put_tol_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectLongToObject<A, T, R> put_otl_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, T, R> func
   ) {
      return this.put_ool_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectLongToObject<A, T, R> put_otl_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectLongToObject<A, T, T> put_otl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, T, T> func
   ) {
      return this.put_ool_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectLongToObject<A, T, T> put_otl_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectLongToObject<T, T, R> put_ttl_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, T, R> func
   ) {
      return this.put_ool_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectLongToObject<T, T, R> put_ttl_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectLongToObject<T, T, T> put_ttl_t(String fname, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, T, T> func) {
      return this.put_ool_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectLongToObject<T, T, T> put_ttl_t(
      String fname, NodeFuncObjectObjectLongToObject.IFuncObjectObjectLongToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ool_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncDoubleToObject<T> put_d_t(String fname, NodeFuncDoubleToObject.IFuncDoubleToObject<T> func) {
      return this.put_d_o(fname, this.getType(), func);
   }

   public NodeFuncDoubleToObject<T> put_d_t(String fname, NodeFuncDoubleToObject.IFuncDoubleToObject<T> func, StringFunctionBi stringFunction) {
      return this.put_d_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncDoubleDoubleToObject<T> put_dd_t(String fname, NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject<T> func) {
      return this.put_dd_o(fname, this.getType(), func);
   }

   public NodeFuncDoubleDoubleToObject<T> put_dd_t(
      String fname, NodeFuncDoubleDoubleToObject.IFuncDoubleDoubleToObject<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_dd_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(String fname, NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject<T> func) {
      return this.put_ddd_o(fname, this.getType(), func);
   }

   public NodeFuncDoubleDoubleDoubleToObject<T> put_ddd_t(
      String fname, NodeFuncDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleToObject<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ddd_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(
      String fname, NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject<T> func
   ) {
      return this.put_dddd_o(fname, this.getType(), func);
   }

   public NodeFuncDoubleDoubleDoubleDoubleToObject<T> put_dddd_t(
      String fname, NodeFuncDoubleDoubleDoubleDoubleToObject.IFuncDoubleDoubleDoubleDoubleToObject<T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_dddd_o(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectDoubleToObject<A, T> put_od_t(String fname, Class<A> argTypeA, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<A, T> func) {
      return this.put_od_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectDoubleToObject<A, T> put_od_t(
      String fname, Class<A> argTypeA, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_od_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectDoubleToObject<T, R> put_td_o(String fname, Class<R> returnType, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<T, R> func) {
      return this.put_od_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectDoubleToObject<T, R> put_td_o(
      String fname, Class<R> returnType, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<T, R> func, StringFunctionTri stringFunction
   ) {
      return this.put_od_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectDoubleToObject<T, T> put_td_t(String fname, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<T, T> func) {
      return this.put_od_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectDoubleToObject<T, T> put_td_t(
      String fname, NodeFuncObjectDoubleToObject.IFuncObjectDoubleToObject<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_od_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectDoubleToObject<A, B, T> put_ood_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, B, T> func
   ) {
      return this.put_ood_o(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectDoubleToObject<A, B, T> put_ood_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectDoubleToObject<T, B, R> put_tod_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, B, R> func
   ) {
      return this.put_ood_o(fname, this.getType(), argTypeB, returnType, func);
   }

   public <B, R> NodeFuncObjectObjectDoubleToObject<T, B, R> put_tod_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, B, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, this.getType(), argTypeB, returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectDoubleToObject<T, B, T> put_tod_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, B, T> func
   ) {
      return this.put_ood_o(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectDoubleToObject<T, B, T> put_tod_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectDoubleToObject<A, T, R> put_otd_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, T, R> func
   ) {
      return this.put_ood_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectDoubleToObject<A, T, R> put_otd_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectDoubleToObject<A, T, T> put_otd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, T, T> func
   ) {
      return this.put_ood_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectDoubleToObject<A, T, T> put_otd_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectDoubleToObject<T, T, R> put_ttd_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, T, R> func
   ) {
      return this.put_ood_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectDoubleToObject<T, T, R> put_ttd_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectDoubleToObject<T, T, T> put_ttd_t(String fname, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, T, T> func) {
      return this.put_ood_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectDoubleToObject<T, T, T> put_ttd_t(
      String fname, NodeFuncObjectObjectDoubleToObject.IFuncObjectObjectDoubleToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ood_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public NodeFuncBooleanToObject<T> put_b_t(String fname, NodeFuncBooleanToObject.IFuncBooleanToObject<T> func) {
      return this.put_b_o(fname, this.getType(), func);
   }

   public NodeFuncBooleanToObject<T> put_b_t(String fname, NodeFuncBooleanToObject.IFuncBooleanToObject<T> func, StringFunctionBi stringFunction) {
      return this.put_b_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncBooleanBooleanToObject<T> put_bb_t(String fname, NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject<T> func) {
      return this.put_bb_o(fname, this.getType(), func);
   }

   public NodeFuncBooleanBooleanToObject<T> put_bb_t(
      String fname, NodeFuncBooleanBooleanToObject.IFuncBooleanBooleanToObject<T> func, StringFunctionTri stringFunction
   ) {
      return this.put_bb_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncBooleanBooleanBooleanToObject<T> put_bbb_t(String fname, NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject<T> func) {
      return this.put_bbb_o(fname, this.getType(), func);
   }

   public NodeFuncBooleanBooleanBooleanToObject<T> put_bbb_t(
      String fname, NodeFuncBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanToObject<T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_bbb_o(fname, this.getType(), func, stringFunction);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToObject<T> put_bbbb_t(
      String fname, NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject<T> func
   ) {
      return this.put_bbbb_o(fname, this.getType(), func);
   }

   public NodeFuncBooleanBooleanBooleanBooleanToObject<T> put_bbbb_t(
      String fname, NodeFuncBooleanBooleanBooleanBooleanToObject.IFuncBooleanBooleanBooleanBooleanToObject<T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_bbbb_o(fname, this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectBooleanToObject<A, T> put_ob_t(String fname, Class<A> argTypeA, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<A, T> func) {
      return this.put_ob_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectBooleanToObject<A, T> put_ob_t(
      String fname, Class<A> argTypeA, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<A, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ob_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectBooleanToObject<T, R> put_tb_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<T, R> func
   ) {
      return this.put_ob_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectBooleanToObject<T, R> put_tb_o(
      String fname, Class<R> returnType, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<T, R> func, StringFunctionTri stringFunction
   ) {
      return this.put_ob_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectBooleanToObject<T, T> put_tb_t(String fname, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<T, T> func) {
      return this.put_ob_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectBooleanToObject<T, T> put_tb_t(
      String fname, NodeFuncObjectBooleanToObject.IFuncObjectBooleanToObject<T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_ob_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectBooleanToObject<A, B, T> put_oob_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, B, T> func
   ) {
      return this.put_oob_o(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectBooleanToObject<A, B, T> put_oob_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, B, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectBooleanToObject<T, B, R> put_tob_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, B, R> func
   ) {
      return this.put_oob_o(fname, this.getType(), argTypeB, returnType, func);
   }

   public <B, R> NodeFuncObjectObjectBooleanToObject<T, B, R> put_tob_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, B, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, this.getType(), argTypeB, returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectBooleanToObject<T, B, T> put_tob_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, B, T> func
   ) {
      return this.put_oob_o(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectBooleanToObject<T, B, T> put_tob_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, B, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectBooleanToObject<A, T, R> put_otb_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, T, R> func
   ) {
      return this.put_oob_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectBooleanToObject<A, T, R> put_otb_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectBooleanToObject<A, T, T> put_otb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, T, T> func
   ) {
      return this.put_oob_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectBooleanToObject<A, T, T> put_otb_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<A, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectBooleanToObject<T, T, R> put_ttb_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, T, R> func
   ) {
      return this.put_oob_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectBooleanToObject<T, T, R> put_ttb_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectBooleanToObject<T, T, T> put_ttb_t(
      String fname, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, T, T> func
   ) {
      return this.put_oob_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectBooleanToObject<T, T, T> put_ttb_t(
      String fname, NodeFuncObjectObjectBooleanToObject.IFuncObjectObjectBooleanToObject<T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_oob_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A> NodeFuncObjectToObject<A, T> put_o_t(String fname, Class<A> argTypeA, NodeFuncObjectToObject.IFuncObjectToObject<A, T> func) {
      return this.put_o_o(fname, argTypeA, this.getType(), func);
   }

   public <A> NodeFuncObjectToObject<A, T> put_o_t(
      String fname, Class<A> argTypeA, NodeFuncObjectToObject.IFuncObjectToObject<A, T> func, StringFunctionBi stringFunction
   ) {
      return this.put_o_o(fname, argTypeA, this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectToObject<T, R> put_t_o(String fname, Class<R> returnType, NodeFuncObjectToObject.IFuncObjectToObject<T, R> func) {
      return this.put_o_o(fname, this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectToObject<T, R> put_t_o(
      String fname, Class<R> returnType, NodeFuncObjectToObject.IFuncObjectToObject<T, R> func, StringFunctionBi stringFunction
   ) {
      return this.put_o_o(fname, this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectToObject<T, T> put_t_t(String fname, NodeFuncObjectToObject.IFuncObjectToObject<T, T> func) {
      return this.put_o_o(fname, this.getType(), this.getType(), func);
   }

   public NodeFuncObjectToObject<T, T> put_t_t(String fname, NodeFuncObjectToObject.IFuncObjectToObject<T, T> func, StringFunctionBi stringFunction) {
      return this.put_o_o(fname, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, B, T> func
   ) {
      return this.put_oo_o(fname, argTypeA, argTypeB, this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectToObject<A, B, T> put_oo_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, B, T> func,
      StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, argTypeA, argTypeB, this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, B, R> func
   ) {
      return this.put_oo_o(fname, this.getType(), argTypeB, returnType, func);
   }

   public <B, R> NodeFuncObjectObjectToObject<T, B, R> put_to_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, B, R> func,
      StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, this.getType(), argTypeB, returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, B, T> func
   ) {
      return this.put_oo_o(fname, this.getType(), argTypeB, this.getType(), func);
   }

   public <B> NodeFuncObjectObjectToObject<T, B, T> put_to_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, B, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, this.getType(), argTypeB, this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, T, R> func
   ) {
      return this.put_oo_o(fname, argTypeA, this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectToObject<A, T, R> put_ot_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, T, R> func,
      StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, argTypeA, this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, T, T> func
   ) {
      return this.put_oo_o(fname, argTypeA, this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectToObject<A, T, T> put_ot_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<A, T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, argTypeA, this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, T, R> func
   ) {
      return this.put_oo_o(fname, this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectToObject<T, T, R> put_tt_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, T, R> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectToObject<T, T, T> put_tt_t(String fname, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, T, T> func) {
      return this.put_oo_o(fname, this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectToObject<T, T, T> put_tt_t(
      String fname, NodeFuncObjectObjectToObject.IFuncObjectObjectToObject<T, T, T> func, StringFunctionTri stringFunction
   ) {
      return this.put_oo_o(fname, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, C, T> func
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), func);
   }

   public <A, B, C> NodeFuncObjectObjectObjectToObject<A, B, C, T> put_ooo_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, C, R> func
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, argTypeC, returnType, func);
   }

   public <B, C, R> NodeFuncObjectObjectObjectToObject<T, B, C, R> put_too_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, argTypeC, returnType, func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, C, T> func
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), func);
   }

   public <B, C> NodeFuncObjectObjectObjectToObject<T, B, C, T> put_too_t(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), func, stringFunction);
   }

   public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, C, R> func
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), argTypeC, returnType, func);
   }

   public <A, C, R> NodeFuncObjectObjectObjectToObject<A, T, C, R> put_oto_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), argTypeC, returnType, func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, C, T> func
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), func);
   }

   public <A, C> NodeFuncObjectObjectObjectToObject<A, T, C, T> put_oto_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, C, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(
      String fname, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, C, R> func
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), argTypeC, returnType, func);
   }

   public <C, R> NodeFuncObjectObjectObjectToObject<T, T, C, R> put_tto_o(
      String fname,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, C, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), argTypeC, returnType, func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, C, T> func
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), func);
   }

   public <C> NodeFuncObjectObjectObjectToObject<T, T, C, T> put_tto_t(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, C, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), func, stringFunction);
   }

   public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, T, R> func
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, this.getType(), returnType, func);
   }

   public <A, B, R> NodeFuncObjectObjectObjectToObject<A, B, T, R> put_oot_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, this.getType(), returnType, func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, T, T> func
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectToObject<A, B, T, T> put_oot_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, B, T, T> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, T, R> func
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, this.getType(), returnType, func);
   }

   public <B, R> NodeFuncObjectObjectObjectToObject<T, B, T, R> put_tot_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, this.getType(), returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, T, T> func
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectToObject<T, B, T, T> put_tot_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, B, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, T, R> func
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectObjectToObject<A, T, T, R> put_ott_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, T, R> func,
      StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, T, T> func
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectToObject<A, T, T, T> put_ott_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<A, T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, T, R> func
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectObjectToObject<T, T, T, R> put_ttt_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, T, R> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(
      String fname, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, T, T> func
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectToObject<T, T, T, T> put_ttt_t(
      String fname, NodeFuncObjectObjectObjectToObject.IFuncObjectObjectObjectToObject<T, T, T, T> func, StringFunctionQuad stringFunction
   ) {
      return this.put_ooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, argTypeD, this.getType(), func);
   }

   public <A, B, C, D> NodeFuncObjectObjectObjectObjectToObject<A, B, C, D, T> put_oooo_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, argTypeD, this.getType(), func, stringFunction);
   }

   public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, argTypeD, returnType, func);
   }

   public <B, C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, R> put_tooo_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, argTypeD, returnType, func, stringFunction);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, argTypeD, this.getType(), func);
   }

   public <B, C, D> NodeFuncObjectObjectObjectObjectToObject<T, B, C, D, T> put_tooo_t(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, argTypeD, this.getType(), func, stringFunction);
   }

   public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, argTypeD, returnType, func);
   }

   public <A, C, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, R> put_otoo_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, argTypeD, returnType, func, stringFunction);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, argTypeD, this.getType(), func);
   }

   public <A, C, D> NodeFuncObjectObjectObjectObjectToObject<A, T, C, D, T> put_otoo_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, argTypeD, this.getType(), func, stringFunction);
   }

   public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, argTypeD, returnType, func);
   }

   public <C, D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, R> put_ttoo_o(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, argTypeD, returnType, func, stringFunction);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(
      String fname, Class<C> argTypeC, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, argTypeD, this.getType(), func);
   }

   public <C, D> NodeFuncObjectObjectObjectObjectToObject<T, T, C, D, T> put_ttoo_t(
      String fname,
      Class<C> argTypeC,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, argTypeD, this.getType(), func, stringFunction);
   }

   public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), argTypeD, returnType, func);
   }

   public <A, B, D, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, R> put_ooto_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), argTypeD, returnType, func, stringFunction);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), argTypeD, this.getType(), func);
   }

   public <A, B, D> NodeFuncObjectObjectObjectObjectToObject<A, B, T, D, T> put_ooto_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), argTypeD, this.getType(), func, stringFunction);
   }

   public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), argTypeD, returnType, func);
   }

   public <B, D, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, R> put_toto_o(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), argTypeD, returnType, func, stringFunction);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(
      String fname, Class<B> argTypeB, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), argTypeD, this.getType(), func);
   }

   public <B, D> NodeFuncObjectObjectObjectObjectToObject<T, B, T, D, T> put_toto_t(
      String fname,
      Class<B> argTypeB,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), argTypeD, this.getType(), func, stringFunction);
   }

   public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), argTypeD, returnType, func);
   }

   public <A, D, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, R> put_otto_o(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), argTypeD, returnType, func, stringFunction);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(
      String fname, Class<A> argTypeA, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), argTypeD, this.getType(), func);
   }

   public <A, D> NodeFuncObjectObjectObjectObjectToObject<A, T, T, D, T> put_otto_t(
      String fname,
      Class<A> argTypeA,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), argTypeD, this.getType(), func, stringFunction);
   }

   public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(
      String fname, Class<D> argTypeD, Class<R> returnType, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), argTypeD, returnType, func);
   }

   public <D, R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, R> put_ttto_o(
      String fname,
      Class<D> argTypeD,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, D, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), argTypeD, returnType, func, stringFunction);
   }

   public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(
      String fname, Class<D> argTypeD, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), argTypeD, this.getType(), func);
   }

   public <D> NodeFuncObjectObjectObjectObjectToObject<T, T, T, D, T> put_ttto_t(
      String fname,
      Class<D> argTypeD,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, D, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), argTypeD, this.getType(), func, stringFunction);
   }

   public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), returnType, func);
   }

   public <A, B, C, R> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, R> put_ooot_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), returnType, func, stringFunction);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), this.getType(), func);
   }

   public <A, B, C> NodeFuncObjectObjectObjectObjectToObject<A, B, C, T, T> put_ooot_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, C, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, argTypeC, this.getType(), this.getType(), func, stringFunction);
   }

   public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), returnType, func);
   }

   public <B, C, R> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, R> put_toot_o(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), returnType, func, stringFunction);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(
      String fname, Class<B> argTypeB, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), this.getType(), func);
   }

   public <B, C> NodeFuncObjectObjectObjectObjectToObject<T, B, C, T, T> put_toot_t(
      String fname,
      Class<B> argTypeB,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, C, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, argTypeC, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), returnType, func);
   }

   public <A, C, R> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, R> put_otot_o(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), returnType, func, stringFunction);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(
      String fname, Class<A> argTypeA, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), this.getType(), func);
   }

   public <A, C> NodeFuncObjectObjectObjectObjectToObject<A, T, C, T, T> put_otot_t(
      String fname,
      Class<A> argTypeA,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, C, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), argTypeC, this.getType(), this.getType(), func, stringFunction);
   }

   public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(
      String fname, Class<C> argTypeC, Class<R> returnType, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), returnType, func);
   }

   public <C, R> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, R> put_ttot_o(
      String fname,
      Class<C> argTypeC,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), returnType, func, stringFunction);
   }

   public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(
      String fname, Class<C> argTypeC, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), this.getType(), func);
   }

   public <C> NodeFuncObjectObjectObjectObjectToObject<T, T, C, T, T> put_ttot_t(
      String fname,
      Class<C> argTypeC,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, C, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), argTypeC, this.getType(), this.getType(), func, stringFunction);
   }

   public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), returnType, func);
   }

   public <A, B, R> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, R> put_oott_o(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(
      String fname, Class<A> argTypeA, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), this.getType(), func);
   }

   public <A, B> NodeFuncObjectObjectObjectObjectToObject<A, B, T, T, T> put_oott_t(
      String fname,
      Class<A> argTypeA,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, B, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, argTypeB, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(
      String fname, Class<B> argTypeB, Class<R> returnType, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), returnType, func);
   }

   public <B, R> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, R> put_tott_o(
      String fname,
      Class<B> argTypeB,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(
      String fname, Class<B> argTypeB, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), this.getType(), func);
   }

   public <B> NodeFuncObjectObjectObjectObjectToObject<T, B, T, T, T> put_tott_t(
      String fname,
      Class<B> argTypeB,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, B, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), argTypeB, this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(
      String fname, Class<A> argTypeA, Class<R> returnType, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), returnType, func);
   }

   public <A, R> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, R> put_ottt_o(
      String fname,
      Class<A> argTypeA,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(
      String fname, Class<A> argTypeA, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public <A> NodeFuncObjectObjectObjectObjectToObject<A, T, T, T, T> put_ottt_t(
      String fname,
      Class<A> argTypeA,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<A, T, T, T, T> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, argTypeA, this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }

   public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(
      String fname, Class<R> returnType, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), returnType, func);
   }

   public <R> NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, R> put_tttt_o(
      String fname,
      Class<R> returnType,
      NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, T, R> func,
      StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), returnType, func, stringFunction);
   }

   public NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(
      String fname, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), this.getType(), func);
   }

   public NodeFuncObjectObjectObjectObjectToObject<T, T, T, T, T> put_tttt_t(
      String fname, NodeFuncObjectObjectObjectObjectToObject.IFuncObjectObjectObjectObjectToObject<T, T, T, T, T> func, StringFunctionPenta stringFunction
   ) {
      return this.put_oooo_o(fname, this.getType(), this.getType(), this.getType(), this.getType(), this.getType(), func, stringFunction);
   }
}
