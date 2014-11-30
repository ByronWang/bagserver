package bagserver;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import nebula.data.DataSession;
import nebula.data.DataStore;
import nebula.data.Entity;
import nebula.data.TransactionCaller;
import nebula.data.impl.EditableEntity;
import nebula.http.Application;
import nebula.http.resource.rest.EntityListResouce;

import org.joda.time.DateTime;
import org.joda.time.Hours;

import util.FileUtil;
import cn.xj.bag.server.manager.UnionpayService;
import cn.xj.bag.server.modal.UpmpNotifyResult;

public class OrderItemFlowListEntityResource extends EntityListResouce {

    UnionpayService unionpay = new UnionpayService();
    final DataStore<Entity> stepStore;
    final DataStore<Entity> itemStore;
    final DataStore<Entity> bidStore;
    final DataStore<Entity> productStore;
    final DataStore<Entity> paymentStore;
    final DataStore<Entity> statusStore;
    final DataStore<Entity> actionStore;

    public OrderItemFlowListEntityResource(Application app) {
        super(app, app.getType("OrderItemFlow"));
        stepStore = datastoreHolder;
        itemStore = app.getStore("OrderItem");
        bidStore = app.getStore("Bid");
        statusStore = app.getStore("OrderStatus");
        actionStore = app.getStore("Action");
        productStore = app.getStore("Product");
        paymentStore = app.getStore("Payment");
    }

    @Override
    protected byte[] buildFrom(List<Entity> dataList) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bout);

        try {
            boolean start = true;
            out.append('[');

            for (int i = dataList.size() - 1; i >= 0; i--) {
                Entity data = dataList.get(i);
                if (!start) {
                    out.append(',');
                } else {
                    start = false;
                }
                jsonHolder.stringifyTo(data, new OutputStreamWriter(out, "utf-8"));
            }
            out.append(']');
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        out.flush();
        out.close();
        return bout.toByteArray();
    }

    @Override
    protected String post(HttpServletRequest req) throws IOException {

        InputStream in = req.getInputStream();
        if (log.isTraceEnabled()) {
            in = FileUtil.print(in);
        }
        final Entity step = jsonHolder.readFrom(new EditableEntity(), new InputStreamReader(in, "utf-8"));

        final StringBuilder sb = new StringBuilder();
        try {
            stepStore.save(new TransactionCaller() {
                @Override
                public void exec(DataSession session) throws Exception {
                    Long actionID = step.get("ActionID");

                    EditableEntity exts = (EditableEntity) step.getEntity("Extends");
                    if (exts == null) {
                        exts = new EditableEntity();
                        step.put("Extends", exts);
                    }

                    Long itemID = step.get("OrderItemID");
                    EditableEntity item = (EditableEntity) itemStore.get(itemID).editable();

                    switch (actionID.intValue()) {
                    case 2:// "选中买手"
                        exts.put("BidSucceedDatetime", new DateTime());
                        List<Entity> bids = bidStore.getClassificator("OrderItem").getData(String.valueOf(itemID));
                        Long succeedID = step.getEntity("Bid").get("ID");
                        Entity bidSucceed = null;

                        for (Entity entity : bids) {
                            EditableEntity bid = (EditableEntity) entity.editable();
                            Long bidID = bid.get("ID");
                            if (bidID.equals(succeedID)) {
                                bid.put("StatusID", 2L);
                                bid.put("StatusName", "成功");
                                bidSucceed = bid;
                            } else {
                                bid.put("StatusID", 3L);
                                bid.put("StatusName", "失败");
                            }
                            session.add(bidStore, bid);
                        }

                        Entity itemProduct = (EditableEntity) item.getEntity("Product");
                        itemProduct.put("Price", bidSucceed.get("SuggestedPrice"));
                        
                        item.put("PurchaserID", bidSucceed.get("PurchaserID"));
                        item.put("Bid", bidSucceed);

                        step.put("StatusID", 1L);
                        step.put("StatusName", statusStore.get(1L).get("Name"));
                        
                        session.add(stepStore, step);

                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 9:// "开始支付"
//						session.add(stepStore, step);

                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 10:// "支付完成"
                        session.add(stepStore, step);

                        Long paymentID = step.get("PaymentID");

                        EditableEntity payment = (EditableEntity) (paymentStore.get(paymentID)).editable();

                        Date datetime = ((DateTime) payment.get("Datetime")).toDate();
                        String orderNo = payment.get("OrderNo");
//						BigDecimal amount = payment.get("Amount");

                        UpmpNotifyResult result = null;
                        try {
                            result = unionpay.query(datetime, orderNo);
                            if ("00".equals(result.getTransStatus())) {
                                if (log.isDebugEnabled()) {
                                    log.debug(result);
                                }
//                              Preconditions.checkArgument(datetime.equals(result.getOrderTime()));
//                              Preconditions.checkArgument(orderNo.equals(result.getOrderNumber()));
//                              Preconditions.checkArgument(amount.equals(result.getSettleAmount()));

                                payment.put("SettleMonth", result.getSettleDate());
                                session.add(stepStore, payment);

                                EditableEntity stepPayConfirmed = new EditableEntity();
                                stepPayConfirmed.extend(step);
                                stepPayConfirmed.put("PaymentID", payment.get("ID"));
                                stepPayConfirmed.put("StatusID", 2L);
                                stepPayConfirmed.put("StatusName", statusStore.get(2L).get("Name"));
                                stepPayConfirmed.put("ActionID", 11L);
                                stepPayConfirmed.put("ActionName", actionStore.get(11L).get("Name"));

                                session.add(stepStore, stepPayConfirmed);
                            } else {
                                throw new RuntimeException("");
                            }
                        } catch (Exception e) { // TODO For Test need delete
                            log.info("$$$$$$$$$$ 实际调用出错，发回测试数据");
                            payment.put("SettleMonth", "201410");
                            session.add(stepStore, payment);

                            EditableEntity stepPayConfirmed = new EditableEntity();
                            stepPayConfirmed.extend(step);
                            stepPayConfirmed.put("PaymentID", payment.get("ID"));
                            stepPayConfirmed.put("StatusID", 2L);
                            stepPayConfirmed.put("StatusName", statusStore.get(2L).get("Name"));
                            stepPayConfirmed.put("ActionID", 11L);
                            stepPayConfirmed.put("ActionName", actionStore.get(11L).get("Name"));

                            session.add(stepStore, stepPayConfirmed);
                        }
                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));

                        break;
                    case 3:// "开始购买"
                        exts.put("PurchasingStartDatetime", new DateTime());
                        session.add(stepStore, step);
                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 4:// "购买完成"
                        exts.put("PurchasingEndDatetime", new DateTime());

                        DateTime begin = exts.get("PurchasingStartDatetime");
                        DateTime end = exts.get("PurchasingEndDatetime");
                        exts.put("PurchasingDuration", (long)Hours.hoursBetween(begin,end).getHours());

                        itemProduct = (EditableEntity) item.getEntity("Product");
                        itemProduct.put("Price", exts.get("ActualPrice"));
                        
                        session.add(stepStore, step);
                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 5:// "开始发货"
                        exts.put("DeliveryStartDatetime", new DateTime());
                        session.add(stepStore, step);
                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 6:// "确认收货"
                        exts.put("DeliveryArriveDatetime", new DateTime());
                        EditableEntity product = new EditableEntity();
                        itemProduct = (EditableEntity) item.getEntity("Product");
                        product.extend(itemProduct);
                        product.put("OrderItemID", itemID);
                        session.add(productStore, product);
                        session.add(stepStore, step);
                        finishOrder(session, step, item);
                        item.put("StatusID", step.get("StatusID"));
                        item.put("StatusName", step.get("StatusName"));
                        item.put("ActionID", step.get("ActionID"));
                        item.put("ActionName", step.get("ActionName"));
                        break;
                    case 7:// "放弃购买"
                        exts.put("CancelPurchasingDatetime", new DateTime());
                        session.add(stepStore, step);
                        if (step.get("PaymentID") != null) {
                            cancelPurchaser(session, step, item);
                        }
                        item.put("StatusID", 6L);
                        item.put("StatusName", statusStore.get(6L).get("Name"));
                        break;
                    case 8:// "放弃订单"
                        exts.put("CancelOrderDatetime", new DateTime());
                        if(step.get("StatusID")==null){
                            step.put("StatusID", 1L);
                            step.put("StatusName", statusStore.get(1L).get("Name"));
                        }
                        session.add(stepStore, step);
                        if (step.get("PaymentID") != null) {
                            cancelOrder(session, step, item);
                        }
                        item.put("StatusID", 5L);
                        item.put("StatusName", statusStore.get(5L).get("Name"));
                        break;
                    }
                    session.add(itemStore, item);
                    session.flush();

                    StringWriter sw = new StringWriter();
                    jsonHolder.stringifyTo(step, sw);

                    sb.append(sw.toString());
                }

                private void finishOrder(DataSession session, Entity step, Entity item) {
                    // TODO 退回多余金额

                    EditableEntity oldPayment = (EditableEntity) paymentStore.get(step.get("PaymentID")).editable();
                    long customerID = (Long) item.get("CustomerID");
                    long purchaserID = (Long) item.get("PurchaserID");

                    long payType = 5;// "支付购买款项"

                    BigDecimal actualAmount = item.getEntity("Bid").get("Amount");

                    String orderNo = String.valueOf(item.get("ID"));
                    String tradeNo = null;
                    DateTime tradeDatetime = new DateTime();
                    BigDecimal amount = oldPayment.get("Amount");

                    if (actualAmount.compareTo(amount) < 0) {
                        // 实际金额小于保证金的情况，按实际金额支付给买手，多余的保证金退还给买家
                        EditableEntity realPayment = makePayment(customerID, purchaserID, payType, 3, 3, 1, tradeDatetime, orderNo, actualAmount, "实际支付订单金额",
                                tradeNo);
                        session.add(paymentStore, realPayment);

                        EditableEntity retPayment = makePayment(customerID, customerID, payType, 3, 3, 1, tradeDatetime, orderNo,
                                amount.subtract(actualAmount), "退还差价", tradeNo);
                        session.add(paymentStore, retPayment);
                    } else {
                        // 实际金额大于保证金的情况，买手自己承担超出部分
                        actualAmount = amount;
                        EditableEntity realPayment = makePayment(customerID, purchaserID, payType, 3, 3, 1, tradeDatetime, orderNo, actualAmount, "实际支付订单金额",
                                tradeNo);
                        session.add(paymentStore, realPayment);
                    }

                }

                private void cancelPurchaser(DataSession session, Entity step, Entity item) {

                    EditableEntity oldPayment = (EditableEntity) paymentStore.get(step.get("PaymentID")).editable();
                    long customerID = (Long) item.get("CustomerID");
                    long purchaserID = (Long) item.get("PurchaserID");
                    long systemID = 1;

                    long payType = 4;// "退款"

                    String orderNo = String.valueOf(step.get("OrderItemID"));

                    String tradeNo = null;
                    DateTime tradeDatetime = new DateTime();
                    BigDecimal amount = oldPayment.get("Amount");

                    String persentPenalty = app.getProperty("cancelPurchaser.persentPenalty", "0.0");

                    BigDecimal amtPayToSystem = amount.multiply(new BigDecimal(persentPenalty));

                    // 惩罚金额转移给系统用户
                    if (amtPayToSystem.compareTo(new BigDecimal(0)) > 0) {
                        EditableEntity paymentForSystem = makePayment(purchaserID, systemID, payType, 3, 3, 1, tradeDatetime, orderNo, amtPayToSystem,
                                "实际支付订单金额", tradeNo);
                        session.add(paymentStore, paymentForSystem);
                    }

                    EditableEntity retPayment = makePayment(customerID, customerID, payType, 3, 3, 1, tradeDatetime, orderNo, amount, "退回订单支付金额", tradeNo);
                    session.add(paymentStore, retPayment);
                }

                private void cancelOrder(DataSession session, Entity step, Entity item) {
                    EditableEntity oldPayment = (EditableEntity) paymentStore.get(step.get("PaymentID")).editable();

                    long customerID = (Long) item.get("CustomerID");
                    long purchaserID = (Long) item.get("PurchaserID");

                    long payType = 4;// "退款"

                    String orderNo = String.valueOf(step.get("OrderItemID"));

                    String tradeNo = null;
                    DateTime tradeDatetime = new DateTime();
                    BigDecimal amount = oldPayment.get("Amount");

                    String persentPenalty = app.getProperty("cancelOrder.persentPenalty", "0.3");
                    BigDecimal amtPayToP = amount.multiply(new BigDecimal(persentPenalty));
                    BigDecimal amtReturn = amount.subtract(amtPayToP);

                    // 默认支付30%赔偿金给买手
                    EditableEntity realPayment = makePayment(customerID, purchaserID, payType, 3, 3, 1, tradeDatetime, orderNo, amtPayToP, "支付赔偿金", tradeNo);
                    session.add(paymentStore, realPayment);

                    // 剩余金额返还给买家
                    EditableEntity retPayment = makePayment(customerID, customerID, payType, 3, 3, 1, tradeDatetime, orderNo, amtReturn, "退回订单支付金额", tradeNo);
                    session.add(paymentStore, retPayment);
                }

                private EditableEntity makePayment(long fromUserID, long toUserID, long payType, long payMethod, long fromAccountType, long toAccountType,
                        DateTime tradeDatetime, String orderNo, BigDecimal amount, String description, String tradeNo) {
                    EditableEntity retPayment = new EditableEntity();
//                  !ID;
//                  @Authorize("Read") From-User;
                    retPayment.put("FromUserID", fromUserID);
//                  @Authorize("Read") To-User;
                    retPayment.put("ToUserID", toUserID);
//                  PayType;
                    retPayment.put("PayTypeName", ((Entity) app.getStore("PayType").get(payType)).get("Name"));
//                  PayMethod;
                    retPayment.put("PayMethodID", payMethod);
                    retPayment.put("PayMethodName", ((Entity) app.getStore("PayMethod").get(payMethod)).get("Name"));
//                  From-AccountType;
                    retPayment.put("FromAccountTypeID", fromAccountType);
                    retPayment.put("FromAccountTypeName", ((Entity) app.getStore("AccountType").get(fromAccountType)).get("Name"));
//                  To-AccountType;
                    retPayment.put("ToAccountTypeID", toAccountType);
                    retPayment.put("ToAccountTypeName", ((Entity) app.getStore("AccountType").get(toAccountType)).get("Name"));
//                  Datetime;// 交易开始日期时间
                    retPayment.put("Datetime", tradeDatetime);
//                  Order-No;// 商户订单号
                    retPayment.put("OrderNo", orderNo);
                    retPayment.put("RealOrderNo", null);
//                  Amount;        // 清算金额
                    retPayment.put("Amount", amount);
//                  Description;
                    retPayment.put("Description", description);
//                  Trade-No; // 交易流水号
                    retPayment.put("TradeNo", tradeNo);
                    return retPayment;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}
